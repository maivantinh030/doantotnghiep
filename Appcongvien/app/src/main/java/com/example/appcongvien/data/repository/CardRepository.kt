package com.example.appcongvien.data.repository

import com.example.appcongvien.data.model.*
import com.example.appcongvien.data.network.ApiService

class CardRepository(private val apiService: ApiService) {

    suspend fun getMyCards(): Resource<List<CardDTO>> {
        return try {
            val response = apiService.getMyCards()
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể tải danh sách thẻ")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun blockCard(cardId: String, reason: String? = null): Resource<CardDTO> {
        return try {
            val response = apiService.blockCard(cardId, BlockCardRequest(reason))
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể khóa thẻ")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }
}
