package com.park.data.repository

import com.park.data.model.*
import com.park.data.network.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class GameRepository {

    private fun authHeader() = "Bearer ${ApiClient.getToken()}"

    suspend fun getGames(page: Int = 1, size: Int = 20, search: String? = null): Result<PaginatedData<GameDTO>> {
        return try {
            val response = ApiClient.http.get("/api/games") {
                header(HttpHeaders.Authorization, authHeader())
                parameter("page", page)
                parameter("size", size)
                if (!search.isNullOrBlank()) parameter("search", search)
            }
            val body = response.body<ApiResponse<PaginatedData<GameDTO>>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi lấy danh sách trò chơi"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createGame(request: CreateGameRequest): Result<GameDTO> {
        return try {
            val response = ApiClient.http.post("/api/games") {
                header(HttpHeaders.Authorization, authHeader())
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.body<ApiResponse<GameDTO>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi tạo trò chơi"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGame(gameId: String, request: UpdateGameRequest): Result<GameDTO> {
        return try {
            val response = ApiClient.http.put("/api/games/$gameId") {
                header(HttpHeaders.Authorization, authHeader())
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.body<ApiResponse<GameDTO>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi cập nhật trò chơi"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGame(gameId: String): Result<Unit> {
        return try {
            val response = ApiClient.http.delete("/api/games/$gameId") {
                header(HttpHeaders.Authorization, authHeader())
            }
            val body = response.body<ApiResponse<Unit>>()
            if (body.success) Result.success(Unit)
            else Result.failure(Exception(body.message ?: "Lỗi xóa trò chơi"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
