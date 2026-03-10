package org.example.project.data.repository

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import org.example.project.data.model.AddGameRequest
import org.example.project.data.model.ApiResponse
import org.example.project.data.model.GameDto
import org.example.project.data.network.ApiClient

class GameRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private fun authHeader() = "Bearer ${ApiClient.getToken()}"

    suspend fun getGames(): Result<List<GameDto>> {
        return try {
            val response = ApiClient.http.get("/games") {
                header(HttpHeaders.Authorization, authHeader())
            }
            val bodyText = response.bodyAsText()
            if (response.status.isSuccess() && bodyText.isNotEmpty()) {
                // Thử parse dạng wrapped ApiResponse<List<GameDto>>
                try {
                    val wrapped = json.decodeFromString<ApiResponse<List<GameDto>>>(bodyText)
                    if (wrapped.success && wrapped.data != null) return Result.success(wrapped.data)
                } catch (_: Exception) {}
                // Fallback: parse dạng plain List<GameDto>
                try {
                    return Result.success(json.decodeFromString<List<GameDto>>(bodyText))
                } catch (_: Exception) {}
                Result.failure(Exception("Không thể parse danh sách game"))
            } else {
                Result.failure(Exception("Lỗi server: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addGame(request: AddGameRequest): Result<Int> {
        return try {
            val response = ApiClient.http.post("/games") {
                header(HttpHeaders.Authorization, authHeader())
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.body<ApiResponse<String>>()
            if (body.success && body.data != null) {
                Result.success(body.data.toInt())
            } else {
                Result.failure(Exception(body.message ?: "Lỗi thêm game"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGame(gameCode: Int): Result<Unit> {
        return try {
            val response = ApiClient.http.delete("/games/$gameCode") {
                header(HttpHeaders.Authorization, authHeader())
            }
            val body = response.body<ApiResponse<String?>>()
            if (body.success) Result.success(Unit)
            else Result.failure(Exception(body.message ?: "Lỗi xóa game"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
