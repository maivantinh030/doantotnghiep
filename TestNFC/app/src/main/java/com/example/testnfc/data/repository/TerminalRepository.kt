package com.example.testnfc.data.repository

import com.example.testnfc.data.model.AdminAuthResponse
import com.example.testnfc.data.model.AdminLoginRequest
import com.example.testnfc.data.model.GameItem
import com.example.testnfc.data.model.UseGameData
import com.example.testnfc.data.model.UseGameRequest
import com.example.testnfc.data.network.ApiClient

class TerminalRepository {

    private val api = ApiClient.api

    suspend fun login(phoneNumber: String, password: String): Result<AdminAuthResponse> {
        return try {
            val response = api.login(AdminLoginRequest(phoneNumber, password))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = response.errorBody()?.string() ?: "Đăng nhập thất bại"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối máy chủ: ${e.message}"))
        }
    }

    suspend fun getGames(token: String): Result<List<GameItem>> {
        return try {
            val response = api.getGames("Bearer $token")
            if (response.isSuccessful) {
                val items = response.body()?.data?.items ?: emptyList()
                Result.success(items)
            } else {
                Result.failure(Exception("Không thể tải danh sách game"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối máy chủ: ${e.message}"))
        }
    }

    suspend fun useGame(token: String, gameId: String, cardUid: String): Result<UseGameData> {
        return try {
            val response = api.useGame(
                token = "Bearer $token",
                gameId = gameId,
                request = UseGameRequest(cardUid = cardUid)
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = body?.message
                    ?: response.errorBody()?.string()
                    ?: "Thẻ không hợp lệ hoặc hết lượt chơi"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối máy chủ: ${e.message}"))
        }
    }
}
