package com.park.data.repository

import com.park.data.model.*
import com.park.data.network.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class NotificationRepository {

    private fun authHeader() = "Bearer ${ApiClient.getToken()}"

    suspend fun sendNotification(request: SendNotificationRequest): Result<SendNotificationResponse> {
        return try {
            val response = ApiClient.http.post("/api/admin/notifications/send") {
                header(HttpHeaders.Authorization, authHeader())
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.body<ApiResponse<SendNotificationResponse>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi gửi thông báo"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNotifications(page: Int = 1, size: Int = 20): Result<PaginatedData<AdminSentNotificationDTO>> {
        return try {
            val response = ApiClient.http.get("/api/admin/notifications") {
                header(HttpHeaders.Authorization, authHeader())
                parameter("page", page)
                parameter("size", size)
            }
            val body = response.body<ApiResponse<PaginatedData<AdminSentNotificationDTO>>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi lấy thông báo"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
