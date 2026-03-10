package com.park.data.repository

import com.park.data.model.ApiResponse
import com.park.data.model.CardLookupByUidRequest
import com.park.data.model.CardLookupResultDTO
import com.park.data.network.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class CardRepository {

    private fun authHeader() = "Bearer ${ApiClient.getToken()}"

    /**
     * Gọi backend để tra cứu thẻ theo UID (physical hoặc virtual).
     */
    suspend fun lookupByUid(uid: String): Result<CardLookupResultDTO> {
        return try {
            val response = ApiClient.http.post("/api/cards/lookup-by-uid") {
                header(HttpHeaders.Authorization, authHeader())
                contentType(ContentType.Application.Json)
                setBody(CardLookupByUidRequest(cardUid = uid))
            }
            val body = response.body<ApiResponse<CardLookupResultDTO>>()
            if (body.success && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body.message ?: "Lỗi tra cứu thẻ theo UID"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

