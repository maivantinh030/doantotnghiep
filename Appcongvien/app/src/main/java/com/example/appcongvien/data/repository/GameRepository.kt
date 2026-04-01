package com.example.appcongvien.data.repository

import com.example.appcongvien.data.model.*
import com.example.appcongvien.data.network.ApiService

class GameRepository(private val apiService: ApiService) {

    suspend fun getGames(
        page: Int = 1,
        size: Int = 10,
        category: String? = null,
        search: String? = null
    ): Resource<PaginatedData<GameDTO>> {
        return try {
            val response = apiService.getGames(page, size, category, search)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể tải danh sách game")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun getFeaturedGames(limit: Int = 5): Resource<List<GameDTO>> {
        return try {
            val response = apiService.getFeaturedGames(limit)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể tải game nổi bật")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun getCategories(): Resource<List<String>> {
        return try {
            val response = apiService.getCategories()
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error("Không thể tải danh mục")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun getGameDetail(gameId: String): Resource<GameDTO> {
        return try {
            val response = apiService.getGameDetail(gameId)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không tìm thấy game")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun getGameReviews(gameId: String, page: Int = 1, size: Int = 10): Resource<PaginatedData<GameReviewDTO>> {
        return try {
            val response = apiService.getGameReviews(gameId, page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error("Không thể tải đánh giá")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun createReview(request: CreateReviewRequest): Resource<GameReviewDTO> {
        return try {
            val response = apiService.createReview(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể gửi đánh giá")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun getMyReview(gameId: String): Resource<GameReviewDTO?> {
        return try {
            val response = apiService.getMyReview(gameId)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể tải đánh giá của bạn")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun updateReview(reviewId: String, request: UpdateReviewRequest): Resource<GameReviewDTO> {
        return try {
            val response = apiService.updateReview(reviewId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể cập nhật đánh giá")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun deleteReview(reviewId: String): Resource<Unit> {
        return try {
            val response = apiService.deleteReview(reviewId)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể xóa đánh giá")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }
}
