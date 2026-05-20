package com.park.data.repository

import com.park.data.model.*
import com.park.data.network.ApiClient
import com.park.data.network.apiCall
import com.park.data.network.apiCallUnit
import io.ktor.client.request.*
import io.ktor.http.*

class AnnouncementRepository {

    suspend fun getAnnouncements(): Result<List<AnnouncementDTO>> =
        apiCall("Lỗi lấy danh sách banner") {
            ApiClient.http.get("/api/admin/announcements")
        }

    suspend fun createAnnouncement(request: CreateAnnouncementRequest): Result<AnnouncementDTO> =
        apiCall("Lỗi tạo banner") {
            ApiClient.http.post("/api/admin/announcements") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    suspend fun updateAnnouncement(id: String, request: UpdateAnnouncementRequest): Result<Boolean> =
        apiCallUnit("Lỗi cập nhật banner") {
            ApiClient.http.put("/api/admin/announcements/$id") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }.map { true }

    suspend fun deleteAnnouncement(id: String): Result<Boolean> =
        apiCallUnit("Lỗi xóa banner") {
            ApiClient.http.delete("/api/admin/announcements/$id")
        }.map { true }
}
