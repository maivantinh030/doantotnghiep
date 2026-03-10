package org.example.project.data.repository

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.example.project.data.model.ChallengeResponse
import org.example.project.data.model.RSAVerifyRequest
import org.example.project.data.model.RSAVerifyResponse
import org.example.project.data.model.RegisterKeyRequest
import org.example.project.data.network.ApiClient

class RSARepository {

    private fun authHeader() = "Bearer ${ApiClient.getToken()}"

    suspend fun getChallenge(): Result<ChallengeResponse> {
        return try {
            val response = ApiClient.http.get("/rsa/challenge") {
                header(HttpHeaders.Authorization, authHeader())
            }
            if (response.status.isSuccess()) {
                Result.success(response.body<ChallengeResponse>())
            } else {
                Result.failure(Exception("Lỗi server: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifySignature(
        customerId: String,
        challenge: String,
        signature: String
    ): Result<RSAVerifyResponse> {
        return try {
            val response = ApiClient.http.post("/rsa/verify") {
                header(HttpHeaders.Authorization, authHeader())
                contentType(ContentType.Application.Json)
                setBody(RSAVerifyRequest(customerId, challenge, signature))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body<RSAVerifyResponse>())
            } else {
                Result.failure(Exception("Lỗi server: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerKey(customerId: String, publicKey: String): Result<Unit> {
        return try {
            val response = ApiClient.http.post("/rsa/register-key") {
                header(HttpHeaders.Authorization, authHeader())
                contentType(ContentType.Application.Json)
                setBody(RegisterKeyRequest(customerId, publicKey))
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Lỗi đăng ký khóa: ${response.status.value}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
