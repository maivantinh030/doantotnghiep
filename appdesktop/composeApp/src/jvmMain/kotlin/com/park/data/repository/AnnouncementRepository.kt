package com.park.data.repository

import com.park.data.model.*
import com.park.data.network.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AnnouncementRepository {

    private fun authHeader() = "Bearer ${ApiClient.getToken()}"

    suspend fun getAnnouncements(): Result<List<AnnouncementDTO>> {
        return try {
            val response = ApiClient.http.get("/api/admin/announcements") {
                header(HttpHeaders.Authorization, authHeader())
            }
            val body = response.body<ApiResponse<List<AnnouncementDTO>>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi lấy danh sách banner"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAnnouncement(request: CreateAnnouncementRequest): Result<AnnouncementDTO> {
        return try {
            val response = ApiClient.http.post("/api/admin/announcements") {
                header(HttpHeaders.Authorization, authHeader())
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.body<ApiResponse<AnnouncementDTO>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi tạo banner"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAnnouncement(id: String, request: UpdateAnnouncementRequest): Result<Boolean> {
        return try {
            val response = ApiClient.http.put("/api/admin/announcements/$id") {
                header(HttpHeaders.Authorization, authHeader())
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.body<ApiResponse<Unit>>()
            if (body.success) Result.success(true)
            else Result.failure(Exception(body.message ?: "Lỗi cập nhật banner"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAnnouncement(id: String): Result<Boolean> {
        return try {
            val response = ApiClient.http.delete("/api/admin/announcements/$id") {
                header(HttpHeaders.Authorization, authHeader())
            }
            val body = response.body<ApiResponse<Unit>>()
            if (body.success) Result.success(true)
            else Result.failure(Exception(body.message ?: "Lỗi xóa banner"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
