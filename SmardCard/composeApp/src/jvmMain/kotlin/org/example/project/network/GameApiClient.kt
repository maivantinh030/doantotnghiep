package org.example.project.network

import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import org.example.project.model.*

class GameApiClient {
    private val json = Json { ignoreUnknownKeys = true }

    fun getAllGames(): Result<List<GameDto>> = runBlocking {
        try {
            val response = ApiClient.http.get("/api/games")
            val bodyText = response.bodyAsText()
            if (!response.status.isSuccess()) {
                return@runBlocking Result.failure(Exception(readErrorMessage(bodyText, "Server error: ${response.status.value}")))
            }

            Result.success(parseGames(bodyText))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getGame(gameId: String): Result<GameDto> = runBlocking {
        try {
            val response = ApiClient.http.get("/api/games/$gameId")
            val bodyText = response.bodyAsText()
            if (!response.status.isSuccess()) {
                return@runBlocking Result.failure(Exception(readErrorMessage(bodyText, "Server error: ${response.status.value}")))
            }

            val dataElement = readDataElement(bodyText)
                ?: return@runBlocking Result.failure(Exception("Game response is missing data"))

            Result.success(json.decodeFromJsonElement<GameDto>(dataElement))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun lookupCard(cardId: String): Result<CardLookupDto> = runBlocking {
        try {
            val response = ApiClient.http.post("/api/cards/tap") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("cardId" to cardId))
            }
            val bodyText = response.bodyAsText()
            if (!response.status.isSuccess()) {
                return@runBlocking Result.failure(Exception(readErrorMessage(bodyText, "Server error: ${response.status.value}")))
            }

            val payload = json.decodeFromString<CardLookupEnvelope>(bodyText)
            if (!payload.success || payload.data == null) {
                Result.failure(Exception(payload.message ?: "Card lookup failed"))
            } else {
                Result.success(payload.data)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCustomer(userId: String): Result<CustomerSnapshotDto> = runBlocking {
        try {
            val response = ApiClient.http.get("/api/staff/customers/$userId")
            val bodyText = response.bodyAsText()
            if (!response.status.isSuccess()) {
                return@runBlocking Result.failure(Exception(readErrorMessage(bodyText, "Server error: ${response.status.value}")))
            }

            val payload = json.decodeFromString<CustomerSnapshotEnvelope>(bodyText)
            if (!payload.success || payload.data == null) {
                Result.failure(Exception(payload.message ?: "Customer lookup failed"))
            } else {
                Result.success(payload.data)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun playGame(gameId: String, cardId: String): Result<UseGameResponse> = runBlocking {
        try {
            val response = ApiClient.http.post("/api/games/$gameId/play") {
                contentType(ContentType.Application.Json)
                setBody(UseGameRequest(cardId = cardId))
            }
            val bodyText = response.bodyAsText()
            if (!response.status.isSuccess()) {
                return@runBlocking Result.failure(Exception(readErrorMessage(bodyText, "Server error: ${response.status.value}")))
            }

            val payload = json.decodeFromString<UseGameEnvelope>(bodyText)
            if (!payload.success || payload.data == null) {
                Result.failure(Exception(payload.message ?: "Play game failed"))
            } else {
                Result.success(payload.data)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun syncPlay(gameId: String, request: SyncGamePlayRequest): Result<UseGameResponse> = runBlocking {
        try {
            val response = ApiClient.http.post("/api/games/$gameId/sync-play") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val bodyText = response.bodyAsText()
            if (!response.status.isSuccess()) {
                return@runBlocking Result.failure(Exception(readErrorMessage(bodyText, "Server error: ${response.status.value}")))
            }

            val payload = json.decodeFromString<UseGameEnvelope>(bodyText)
            if (!payload.success || payload.data == null) {
                Result.failure(Exception(payload.message ?: "Sync game play failed"))
            } else {
                Result.success(payload.data)
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

    fun decodeImage(raw: String?): ByteArray? {
        val value = raw?.trim().orEmpty()
        if (value.isBlank() || value.startsWith("http://") || value.startsWith("https://")) {
            return null
        }
        return try {
            java.util.Base64.getDecoder().decode(value)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseGames(bodyText: String): List<GameDto> {
        val rootElement = json.parseToJsonElement(bodyText)
        val dataElement = when (rootElement) {
            is JsonArray -> rootElement
            is JsonObject -> rootElement["data"] ?: rootElement
            else -> null
        } ?: return emptyList()

        val listElement = when (dataElement) {
            is JsonArray -> dataElement
            is JsonObject -> dataElement["items"]?.jsonArray ?: JsonArray(emptyList())
            else -> JsonArray(emptyList())
        }
        return json.decodeFromJsonElement(listElement)
    }

    private fun readDataElement(bodyText: String): JsonElement? {
        val rootElement = json.parseToJsonElement(bodyText)
        return when (rootElement) {
            is JsonObject -> rootElement["data"] ?: rootElement
            else -> null
        }
    }

    private fun readErrorMessage(bodyText: String, fallback: String): String {
        return runCatching {
            json.decodeFromString<ErrorResponse>(bodyText).message
        }.getOrNull().orEmpty().ifBlank { fallback }
    }
}
