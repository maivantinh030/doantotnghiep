package org.example.project.network

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.example.project.model.*

class RSAApiClient {
    fun getChallenge(): Result<ChallengeResponse> = runBlocking {
        try {
            val response = ApiClient.http.get("/rsa/challenge")
            if (response.status.isSuccess()) {
                val dto = response.body<ChallengeResponse>()
                Result.success(dto)
            } else {
                Result.failure(Exception("Server error: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun verifySignature(customerId: String, challenge: String, signatureBase64: String): Result<RSAVerifyResponse> = runBlocking {
        try {
            val req = RSAVerifyRequest(customerId, challenge, signatureBase64)
            val response = ApiClient.http.post("/rsa/verify") {
                contentType(ContentType.Application.Json)
                setBody(req)
            }
            if (response.status.isSuccess()) {
                val dto = response.body<RSAVerifyResponse>()
                Result.success(dto)
            } else {
                Result.failure(Exception("Server error: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
