package com.park.data.repository

import com.park.data.model.*
import com.park.data.network.ApiClient
import com.park.data.network.apiCall
import io.ktor.client.request.*
import io.ktor.http.*

class NotificationRepository {

    suspend fun sendNotification(request: SendNotificationRequest): Result<SendNotificationResponse> =
        apiCall("Lỗi gửi thông báo") {
            ApiClient.http.post("/api/admin/notifications/send") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    suspend fun getNotifications(page: Int = 1, size: Int = 20): Result<PaginatedData<AdminSentNotificationDTO>> =
        apiCall("Lỗi lấy thông báo") {
            ApiClient.http.get("/api/admin/notifications") {
                parameter("page", page)
                parameter("size", size)
            }
        }
}
