package com.park.data.repository

import com.park.data.model.*
import com.park.data.network.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class SupportRepository {

    private fun authHeader() = "Bearer ${ApiClient.getToken()}"

    suspend fun getSupportMessages(page: Int = 1, size: Int = 50): Result<PaginatedData<SupportMessageDTO>> {
        return try {
            val response = ApiClient.http.get("/api/admin/support/messages") {
                header(HttpHeaders.Authorization, authHeader())
                parameter("page", page)
                parameter("size", size)
            }
            val body = response.body<ApiResponse<PaginatedData<SupportMessageDTO>>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi lấy tin nhắn hỗ trợ"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun replyToUser(userId: String, content: String): Result<Unit> {
        return try {
            val response = ApiClient.http.post("/api/admin/support/reply") {
                header(HttpHeaders.Authorization, authHeader())
                contentType(ContentType.Application.Json)
                setBody(SendSupportReplyRequest(userId = userId, content = content))
            }
            val body = response.body<ApiResponse<Unit>>()
            if (body.success) Result.success(Unit)
            else Result.failure(Exception(body.message ?: "Lỗi gửi phản hồi"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
