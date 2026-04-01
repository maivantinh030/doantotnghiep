package com.example.appcongvien.data.repository

import com.example.appcongvien.data.model.*
import com.example.appcongvien.data.network.ApiService

class CardRequestRepository(private val apiService: ApiService) {

    suspend fun createCardRequest(request: CreateCardRequestRequest): Resource<CardRequestDTO> {
        return try {
            val response = apiService.createCardRequest(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể gửi yêu cầu")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun getMyCardRequests(): Resource<List<CardRequestDTO>> {
        return try {
            val response = apiService.getMyCardRequests()
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data ?: emptyList())
            } else {
                Resource.Error(response.body()?.message ?: "Không thể tải yêu cầu")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }
}
