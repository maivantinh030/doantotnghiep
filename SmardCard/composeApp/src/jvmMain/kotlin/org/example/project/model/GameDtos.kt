package org.example.project.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GameDto(
    @Json(name = "gameCode") val gameCode: Int,
    @Json(name = "gameName") val gameName: String,
    @Json(name = "gameDescription") val gameDescription: String?,
    @Json(name = "gameImage") val gameImage: String?, // Base64 encoded
    @Json(name = "ticketPrice") val ticketPrice: String,
    @Json(name = "isActive") val isActive: Boolean
)

@JsonClass(generateAdapter = true)
data class AddGameRequest(
    @Json(name = "gameName") val gameName: String,
    @Json(name = "gameDescription") val gameDescription: String,
    @Json(name = "ticketPrice") val ticketPrice: String,
    @Json(name = "gameImage") val gameImage: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String?,
    @Json(name = "data") val data: String? = null
)

@JsonClass(generateAdapter = true)
data class GamesListResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: List<GameDto>?,
    @Json(name = "message") val message: String? = null
)
