package com.example.appcongvien.data.repository

import com.example.appcongvien.data.model.*
import com.example.appcongvien.data.network.ApiService

class SupportRepository(private val apiService: ApiService) {

    suspend fun getMessages(page: Int = 1, size: Int = 50): Resource<PaginatedData<SupportMessageDTO>> {
        return try {
            val response = apiService.getSupportMessages(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể tải tin nhắn")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun sendMessage(content: String): Resource<SupportMessageDTO> {
        return try {
            val response = apiService.sendSupportMessage(SendMessageRequest(content))
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể gửi tin nhắn")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }
}
