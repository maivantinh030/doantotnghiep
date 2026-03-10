package org.example.project.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameDto(
    @SerialName("gameCode") val gameCode: Int,
    @SerialName("gameName") val gameName: String,
    @SerialName("gameDescription") val gameDescription: String?,
    @SerialName("gameImage") val gameImage: String?, // Base64 encoded
    @SerialName("ticketPrice") val ticketPrice: String,
    @SerialName("isActive") val isActive: Boolean
)

@Serializable
data class AddGameRequest(
    @SerialName("gameName") val gameName: String,
    @SerialName("gameDescription") val gameDescription: String,
    @SerialName("ticketPrice") val ticketPrice: String,
    @SerialName("gameImage") val gameImage: String? = null
)

@Serializable
data class ApiResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String?,
    @SerialName("data") val data: String? = null
)

@Serializable
data class GamesListResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: List<GameDto>?,
    @SerialName("message") val message: String? = null
)
