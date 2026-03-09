package org.example.project.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.example.project.model.ApiResponse
import org.example.project.model.ChallengeResponse
import org.example.project.model.RSAVerifyRequest
import org.example.project.model.RSAVerifyResponse
import org.example.project.model.RegisterKeyRequest

class RSAApiClient {
    private val moshi = MoshiProvider.moshi
    private val jsonMediaType = "application/json".toMediaType()

    private val challengeAdapter = moshi.adapter(ChallengeResponse::class.java)
    private val verifyReqAdapter = moshi.adapter(RSAVerifyRequest::class.java)
    private val verifyRespAdapter = moshi.adapter(RSAVerifyResponse::class.java)
    private val registerReqAdapter = moshi.adapter(RegisterKeyRequest::class.java)
    private val apiRespAdapter = moshi.adapter(ApiResponse::class.java)

    fun getChallenge(): Result<ChallengeResponse> {
        return try {
            ApiClient.get("/rsa/challenge").use { response ->
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val dto = challengeAdapter.fromJson(body)
                    if (dto != null) Result.success(dto) else Result.failure(Exception("Không parse được challenge"))
                } else {
                    Result.failure(Exception("Server error: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    fun verifySignature(customerId: String, challenge: String, signatureBase64: String): Result<RSAVerifyResponse> {
        return try {
            val req = RSAVerifyRequest(customerId, challenge, signatureBase64)
            val json = verifyReqAdapter.toJson(req)
            ApiClient.post("/rsa/verify", json).use { response ->
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val dto = verifyRespAdapter.fromJson(body)
                    if (dto != null) Result.success(dto) else Result.failure(Exception("Không parse được kết quả verify"))
                } else {
                    Result.failure(Exception("Server error: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
