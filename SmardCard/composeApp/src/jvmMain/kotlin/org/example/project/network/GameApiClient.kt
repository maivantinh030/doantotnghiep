package org.example.project.network

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.example.project.model.AddGameRequest
import org.example.project.model.ApiResponse
import org.example.project.model.GameDto
import org.example.project.model.GamesListResponse

class GameApiClient {
    private val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

    fun getAllGames(): Result<List<GameDto>> = runBlocking {
        try {
            val response = ApiClient.http.get("/games")
            val bodyText = response.bodyAsText()
            if (response.status.isSuccess() && bodyText.isNotEmpty()) {
                // Try wrapped
                try {
                    val wrapped = json.decodeFromString<GamesListResponse>(bodyText)
                    if (wrapped.success && wrapped.data != null) {
                        return@runBlocking Result.success(wrapped.data)
                    }
                } catch (_: Exception) {}

                // Fallback to plain list
                try {
                    val raw = json.decodeFromString<List<GameDto>>(bodyText)
                    return@runBlocking Result.success(raw)
                } catch (_: Exception) {}

                Result.failure(Exception("Failed to parse games list"))
            } else {
                Result.failure(Exception("Server error: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getGame(gameCode: Int): Result<GameDto> = runBlocking {
        try {
            val response = ApiClient.http.get("/games/$gameCode")
            if (response.status.isSuccess()) {
                val dto = response.body<GameDto>()
                Result.success(dto)
            } else {
                Result.failure(Exception("Server error: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun addGame(gameName: String, gameDescription: String, ticketPrice: String, imageData: ByteArray? = null): Result<Int> = runBlocking {
        try {
            val imageBase64 = imageData?.let { java.util.Base64.getEncoder().encodeToString(it) }
            val req = AddGameRequest(gameName, gameDescription, ticketPrice, imageBase64)
            val response = ApiClient.http.post("/games") {
                contentType(ContentType.Application.Json)
                setBody(req)
            }
            if (response.status.isSuccess()) {
                val apiResp = response.body<ApiResponse>()
                if (apiResp.success && apiResp.data != null) {
                    Result.success(apiResp.data.toInt())
                } else {
                    Result.failure(Exception(apiResp.message ?: "Unknown error"))
                }
            } else {
                Result.failure(Exception("Server error: ${response.status.value} - ${response.bodyAsText()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteGame(gameCode: Int): Result<Unit> = runBlocking {
        try {
            val response = ApiClient.http.delete("/games/$gameCode")
            if (response.status.isSuccess()) {
                val apiResp = response.body<ApiResponse>()
                if (apiResp.success) Result.success(Unit) else Result.failure(Exception(apiResp.message ?: "Unknown error"))
            } else {
                Result.failure(Exception("Server error: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun decodeImage(base64: String?): ByteArray? = try { base64?.let { java.util.Base64.getDecoder().decode(it) } } catch (_: Exception) { null }
}
