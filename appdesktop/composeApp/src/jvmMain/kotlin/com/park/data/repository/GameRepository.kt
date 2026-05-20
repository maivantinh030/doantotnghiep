package com.park.data.repository

import com.park.data.model.*
import com.park.data.network.ApiClient
import com.park.data.network.apiCall
import com.park.data.network.apiCallUnit
import io.ktor.client.request.*
import io.ktor.http.*

class GameRepository {

    suspend fun getGames(page: Int = 1, size: Int = 20, search: String? = null): Result<PaginatedData<GameDTO>> =
        apiCall("Lỗi lấy danh sách trò chơi") {
            ApiClient.http.get("/api/games") {
                parameter("page", page)
                parameter("size", size)
                if (!search.isNullOrBlank()) parameter("search", search)
            }
        }

    suspend fun createGame(request: CreateGameRequest): Result<GameDTO> =
        apiCall("Lỗi tạo trò chơi") {
            ApiClient.http.post("/api/games") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    suspend fun updateGame(gameId: String, request: UpdateGameRequest): Result<GameDTO> =
        apiCall("Lỗi cập nhật trò chơi") {
            ApiClient.http.put("/api/games/$gameId") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    suspend fun deleteGame(gameId: String): Result<Unit> =
        apiCallUnit("Lỗi xóa trò chơi") {
            ApiClient.http.delete("/api/games/$gameId")
        }
}
