package com.park.data.repository

import com.park.data.model.*
import com.park.data.network.ApiClient
import com.park.data.network.apiCall
import com.park.data.network.apiCallUnit
import io.ktor.client.request.*
import io.ktor.http.*

class SupportRepository {

    suspend fun getSupportMessages(page: Int = 1, size: Int = 50): Result<PaginatedData<SupportMessageDTO>> =
        apiCall("Lỗi lấy tin nhắn hỗ trợ") {
            ApiClient.http.get("/api/admin/support/messages") {
                parameter("page", page)
                parameter("size", size)
            }
        }

    suspend fun replyToUser(userId: String, content: String): Result<Unit> =
        apiCallUnit("Lỗi gửi phản hồi") {
            ApiClient.http.post("/api/admin/support/reply") {
                contentType(ContentType.Application.Json)
                setBody(SendSupportReplyRequest(userId = userId, content = content))
            }
        }
}
