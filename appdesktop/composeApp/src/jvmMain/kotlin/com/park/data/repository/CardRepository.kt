package com.park.data.repository

import com.park.data.model.*
import com.park.data.network.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class CardRepository {

    private fun authHeader() = "Bearer ${ApiClient.getToken()}"

    // Danh sách thẻ AVAILABLE (chưa liên kết)
    suspend fun getAvailableCards(): Result<List<CardDTO>> {
        return try {
            val response = ApiClient.http.get("/api/cards/available") {
                header(HttpHeaders.Authorization, authHeader())
            }
            val body = response.body<ApiResponse<List<CardDTO>>>()
            if (body.success) Result.success(body.data ?: emptyList())
            else Result.failure(Exception(body.message ?: "Lỗi lấy danh sách thẻ"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Đăng ký thẻ trắng vào hệ thống
    suspend fun registerCard(request: RegisterCardRequest): Result<CardDTO> {
        return try {
            val response = ApiClient.http.post("/api/cards/register") {
                header(HttpHeaders.Authorization, authHeader())
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.body<ApiResponse<CardDTO>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi đăng ký thẻ"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Phát hành thẻ cho khách (liên kết + thu cọc)
    suspend fun issueCard(request: IssueCardRequest): Result<CardDTO> {
        return try {
            val response = ApiClient.http.post("/api/cards/issue") {
                header(HttpHeaders.Authorization, authHeader())
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.body<ApiResponse<CardDTO>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi phát hành thẻ"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Xử lý trả thẻ
    suspend fun returnCard(cardId: String): Result<Map<String, String>> {
        return try {
            val response = ApiClient.http.post("/api/cards/$cardId/return") {
                header(HttpHeaders.Authorization, authHeader())
            }
            val body = response.body<ApiResponse<Map<String, String>>>()
            if (body.success) Result.success(body.data ?: emptyMap())
            else Result.failure(Exception(body.message ?: "Lỗi trả thẻ"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Khóa thẻ mất
    suspend fun blockCard(cardId: String, reason: String?): Result<CardDTO> {
        return try {
            val response = ApiClient.http.post("/api/cards/$cardId/block") {
                header(HttpHeaders.Authorization, authHeader())
                contentType(ContentType.Application.Json)
                setBody(BlockCardRequest(reason = reason))
            }
            val body = response.body<ApiResponse<CardDTO>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi khóa thẻ"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Danh sách yêu cầu cấp thẻ từ app
    suspend fun getCardRequests(status: String = "PENDING"): Result<List<CardRequestDTO>> {
        return try {
            val response = ApiClient.http.get("/api/card-requests") {
                header(HttpHeaders.Authorization, authHeader())
                parameter("status", status)
            }
            val body = response.body<ApiResponse<List<CardRequestDTO>>>()
            if (body.success) Result.success(body.data ?: emptyList())
            else Result.failure(Exception(body.message ?: "Lỗi lấy danh sách yêu cầu"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Duyệt / từ chối yêu cầu cấp thẻ
    suspend fun reviewCardRequest(requestId: String, approved: Boolean, note: String?): Result<CardRequestDTO> {
        return try {
            val response = ApiClient.http.post("/api/card-requests/$requestId/review") {
                header(HttpHeaders.Authorization, authHeader())
                contentType(ContentType.Application.Json)
                setBody(ApproveCardRequestDTO(approved = approved, note = note))
            }
            val body = response.body<ApiResponse<CardRequestDTO>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi duyệt yêu cầu"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Đánh dấu hoàn thành (sau khi đã phát thẻ thực tế)
    suspend fun completeCardRequest(requestId: String): Result<CardRequestDTO> {
        return try {
            val response = ApiClient.http.post("/api/card-requests/$requestId/complete") {
                header(HttpHeaders.Authorization, authHeader())
            }
            val body = response.body<ApiResponse<CardRequestDTO>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi hoàn thành yêu cầu"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
