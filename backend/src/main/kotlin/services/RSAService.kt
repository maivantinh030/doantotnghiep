package com.park.services

import com.park.database.tables.RSAPublicKeys
import com.park.dto.ChallengeResponse
import com.park.dto.RSAVerifyRequest
import com.park.dto.RSAVerifyResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class RSAService {
    private val challengeExpiry = ConcurrentHashMap<String, Long>()
    private val challengeTtlMillis = 2 * 60 * 1000L

    fun registerPublicKey(cardId: String, pemOrBase64PublicKey: String): Result<Unit> {
        return runCatching {
            val normalizedCardId = cardId.trim()
            require(normalizedCardId.isNotBlank()) { "cardId khong hop le" }

            // Validate key format before writing to DB
            parsePublicKey(pemOrBase64PublicKey)

            val now = Instant.now()
            transaction {
                val exists = RSAPublicKeys
                    .selectAll()
                    .where { RSAPublicKeys.cardId eq normalizedCardId }
                    .count() > 0

                if (exists) {
                    RSAPublicKeys.update({ RSAPublicKeys.cardId eq normalizedCardId }) {
                        it[RSAPublicKeys.publicKeyPem] = pemOrBase64PublicKey
                        it[RSAPublicKeys.status] = "ACTIVE"
                        it[RSAPublicKeys.updatedAt] = now
                    }
                } else {
                    RSAPublicKeys.insert {
                        it[RSAPublicKeys.cardId] = normalizedCardId
                        it[RSAPublicKeys.publicKeyPem] = pemOrBase64PublicKey
                        it[RSAPublicKeys.status] = "ACTIVE"
                        it[RSAPublicKeys.createdAt] = now
                        it[RSAPublicKeys.updatedAt] = now
                    }
                }
            }
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
            return RSAVerifyResponse(success = false, message = "cardId khong hop le")
        }

        val publicKeyPem = transaction {
            RSAPublicKeys
                .selectAll()
                .where { RSAPublicKeys.cardId eq cardId }
                .singleOrNull()
                ?.let { row ->
                    if (row[RSAPublicKeys.status] == "ACTIVE") row[RSAPublicKeys.publicKeyPem] else null
                }
        } ?: return RSAVerifyResponse(success = false, message = "Chua dang ky public key cho cardId nay")

        val expiresAt = challengeExpiry.remove(request.challenge)
            ?: return RSAVerifyResponse(success = false, message = "Challenge khong ton tai hoac da duoc su dung")

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
                RSAVerifyResponse(success = false, message = "Chu ky khong hop le")
            }
        } catch (_: Exception) {
            RSAVerifyResponse(success = false, message = "Du lieu challenge/signature khong hop le")
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
