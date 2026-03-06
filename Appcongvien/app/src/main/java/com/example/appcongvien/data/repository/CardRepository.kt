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

    suspend fun linkCard(request: LinkCardRequest): Resource<CardDTO> {
        return try {
            val response = apiService.linkCard(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể liên kết thẻ")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun updateCard(cardId: String, request: UpdateCardRequest): Resource<CardDTO> {
        return try {
            val response = apiService.updateCard(cardId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể cập nhật thẻ")
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

    suspend fun unblockCard(cardId: String): Resource<CardDTO> {
        return try {
            val response = apiService.unblockCard(cardId)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể mở khóa thẻ")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun unlinkCard(cardId: String): Resource<Unit> {
        return try {
            val response = apiService.unlinkCard(cardId)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể hủy liên kết thẻ")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun createVirtualCard(): Resource<CardDTO> {
        return try {
            val response = apiService.createVirtualCard()
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể tạo thẻ ảo")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun generateVirtualCard(cardId: String): Resource<CardDTO> {
        return try {
            val response = apiService.generateVirtualCard(cardId)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể tạo thẻ ảo")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun removeVirtualCard(cardId: String): Resource<CardDTO> {
        return try {
            val response = apiService.removeVirtualCard(cardId)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể xóa thẻ ảo")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }
}
