package org.example.project.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.example.project.model.AddGameRequest
import org.example.project.model.ApiResponse
import org.example.project.model.GameDto
import org.example.project.model.GamesListResponse

class GameApiClient {
    private val moshi = MoshiProvider.moshi
    private val jsonMediaType = "application/json".toMediaType()

    private val gameListAdapter = moshi.adapter(GamesListResponse::class.java)
    private val gameListRawAdapter = moshi.adapter<List<GameDto>>(com.squareup.moshi.Types.newParameterizedType(List::class.java, GameDto::class.java))
    private val addGameRequestAdapter = moshi.adapter(AddGameRequest::class.java)
    private val apiResponseAdapter = moshi.adapter(ApiResponse::class.java)
    private val gameDtoAdapter = moshi.adapter(GameDto::class.java)

    fun getAllGames(): Result<List<GameDto>> {
        return try {
            ApiClient.get("/games").use { response ->
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    // Try wrapped response { success, data }
                    val wrapped = try { gameListAdapter.fromJson(body) } catch (_: Exception) { null }
                    if (wrapped?.success == true && wrapped.data != null) {
                        return Result.success(wrapped.data)
                    }
                    // Fallback to plain list
                    val raw = try { gameListRawAdapter.fromJson(body) } catch (_: Exception) { null }
                    if (raw != null) {
                        return Result.success(raw)
                    }
                    Result.failure(Exception("Failed to parse games list"))
                } else {
                    Result.failure(Exception("Server error: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getGame(gameCode: Int): Result<GameDto> {
        return try {
            ApiClient.get("/games/$gameCode").use { response ->
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val dto = gameDtoAdapter.fromJson(body)
                    if (dto != null) Result.success(dto) else Result.failure(Exception("Failed to parse game"))
                } else {
                    Result.failure(Exception("Server error: ${response.code}"))
                }
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    fun addGame(gameName: String, gameDescription: String, ticketPrice: String, imageData: ByteArray? = null): Result<Int> {
        return try {
            val imageBase64 = imageData?.let { java.util.Base64.getEncoder().encodeToString(it) }
            val req = AddGameRequest(gameName, gameDescription, ticketPrice, imageBase64)
            val json = addGameRequestAdapter.toJson(req)
            ApiClient.post("/games", json).use { response ->
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val apiResp = apiResponseAdapter.fromJson(body)
                    if (apiResp?.success == true && apiResp.data != null) {
                        Result.success(apiResp.data.toInt())
                    } else {
                        Result.failure(Exception(apiResp?.message ?: "Unknown error"))
                    }
                } else {
                    Result.failure(Exception("Server error: ${response.code} - ${response.body?.string()}"))
                }
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    fun deleteGame(gameCode: Int): Result<Unit> {
        return try {
            ApiClient.delete("/games/$gameCode").use { response ->
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val apiResp = apiResponseAdapter.fromJson(body)
                    if (apiResp?.success == true) Result.success(Unit) else Result.failure(Exception(apiResp?.message ?: "Unknown error"))
                } else {
                    Result.failure(Exception("Server error: ${response.code}"))
                }
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    fun decodeImage(base64: String?): ByteArray? = try { base64?.let { java.util.Base64.getDecoder().decode(it) } } catch (_: Exception) { null }
}
