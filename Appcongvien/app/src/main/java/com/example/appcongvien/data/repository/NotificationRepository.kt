package com.example.appcongvien.data.repository

import com.example.appcongvien.data.model.*
import com.example.appcongvien.data.network.ApiService

class NotificationRepository(private val apiService: ApiService) {

    suspend fun getNotifications(page: Int = 1, size: Int = 20): Resource<PaginatedData<NotificationDTO>> {
        return try {
            val response = apiService.getNotifications(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể tải thông báo")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun getUnreadCount(): Resource<Int> {
        return try {
            val response = apiService.getUnreadCount()
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!.count)
            } else {
                Resource.Error("Không thể lấy số thông báo")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun markAsRead(notificationId: String): Resource<Unit> {
        return try {
            val response = apiService.markAsRead(notificationId)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Không thể đánh dấu đã đọc")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun markAllAsRead(): Resource<Unit> {
        return try {
            val response = apiService.markAllAsRead()
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Không thể đánh dấu tất cả đã đọc")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun deleteNotification(notificationId: String): Resource<Unit> {
        return try {
            val response = apiService.deleteNotification(notificationId)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Không thể xóa thông báo")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }
}
