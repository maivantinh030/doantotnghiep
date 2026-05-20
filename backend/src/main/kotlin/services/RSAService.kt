package com.park.services

import com.park.dto.ChallengeResponse
import com.park.dto.RSAVerifyRequest
import com.park.dto.RSAVerifyResponse
import com.park.repositories.IRSAPublicKeyRepository
import com.park.repositories.RSAPublicKeyRepository
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class RSAService(
    private val publicKeyRepository: IRSAPublicKeyRepository = RSAPublicKeyRepository()
) {
    private val challengeExpiry = ConcurrentHashMap<String, Long>()
    private val challengeTtlMillis = 2 * 60 * 1000L

    fun registerPublicKey(cardId: String, pemOrBase64PublicKey: String): Result<Unit> {
        return runCatching {
            val normalizedCardId = cardId.trim()
            require(normalizedCardId.isNotBlank()) { "Mã thẻ không hợp lệ" }

            // Validate key format before writing to DB
            parsePublicKey(pemOrBase64PublicKey)

            publicKeyRepository.upsertActive(normalizedCardId, pemOrBase64PublicKey)
        }
    }

    fun createChallenge(): ChallengeResponse {
        val challengeBytes = ByteArray(32)
        Random.nextBytes(challengeBytes)
        val challenge = Base64.getEncoder().encodeToString(challengeBytes)
        val expiresAt = Instant.now().toEpochMilli() + challengeTtlMillis
        challengeExpiry[challenge] = expiresAt
        return ChallengeResponse(challenge = challenge, expiresAt = expiresAt)
    }

    fun verifySignature(request: RSAVerifyRequest): RSAVerifyResponse {
        val cardId = request.cardId.trim()
        if (cardId.isBlank()) {
            return RSAVerifyResponse(success = false, message = "Mã thẻ không hợp lệ")
        }

        val publicKeyPem = publicKeyRepository.findActivePublicKeyPem(cardId)
            ?: return RSAVerifyResponse(success = false, message = "Chưa đăng ký public key cho mã thẻ này")

        val expiresAt = challengeExpiry.remove(request.challenge)
            ?: return RSAVerifyResponse(success = false, message = "Challenge không tồn tại hoặc đã được sử dụng")

        if (Instant.now().toEpochMilli() > expiresAt) {
            return RSAVerifyResponse(success = false, message = "Challenge da het han")
        }

        return try {
            val publicKey = parsePublicKey(publicKeyPem)
            val challengeBytes = Base64.getDecoder().decode(request.challenge)
            val signatureBytes = Base64.getDecoder().decode(request.signature)

            // JavaCard uses ALG_RSA_SHA_PKCS1 => SHA1withRSA
            val verifier = Signature.getInstance("SHA1withRSA")
            verifier.initVerify(publicKey)
            verifier.update(challengeBytes)
            val ok = verifier.verify(signatureBytes)

            if (ok) {
                RSAVerifyResponse(success = true, message = "Xac thuc RSA thanh cong")
            } else {
                RSAVerifyResponse(success = false, message = "Chữ ký không hợp lệ")
            }
        } catch (_: Exception) {
            RSAVerifyResponse(success = false, message = "Dữ liệu challenge/signature không hợp lệ")
        }
    }

    private fun parsePublicKey(pemOrBase64PublicKey: String): PublicKey {
        val cleaned = pemOrBase64PublicKey
            .trim()
            .replace("\\n", "\n")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\n", "")
            .replace("\r", "")
            .trim()

        val keyBytes = Base64.getDecoder().decode(cleaned)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }
}
