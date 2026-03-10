package com.example.appcongvien.data.repository

import com.example.appcongvien.data.model.AnnouncementDTO
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.data.network.ApiService

class AnnouncementRepository(private val apiService: ApiService) {

    suspend fun getAnnouncements(): Resource<List<AnnouncementDTO>> {
        return try {
            val response = apiService.getAnnouncements()
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data ?: emptyList())
            } else {
                Resource.Error(response.body()?.message ?: "Không thể tải banner")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }
}
