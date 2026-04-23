package org.example.project.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameDto(
    @SerialName("gameId") val gameId: String,
    @SerialName("name") val gameName: String,
    @SerialName("description") val gameDescription: String? = null,
    @SerialName("shortDescription") val shortDescription: String? = null,
    @SerialName("thumbnailUrl") val gameImage: String? = null,
    @SerialName("pricePerTurn") val ticketPrice: String,
    @SerialName("status") val status: String = "ACTIVE"
) {
    val isActive: Boolean
        get() = status.equals("ACTIVE", ignoreCase = true)

    val gameCode: Int
        get() = ((gameId.hashCode() and Int.MAX_VALUE) % 9000) + 1000

    val displayDescription: String?
        get() = gameDescription ?: shortDescription
}

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
    @SerialName("data") val data: GamesPageData? = null,
    @SerialName("message") val message: String? = null
)

@Serializable
data class GamesPageData(
    @SerialName("items") val items: List<GameDto> = emptyList()
)

@Serializable
data class UseGameRequest(
    @SerialName("cardId") val cardId: String
)

@Serializable
data class SyncGamePlayRequest(
    @SerialName("clientTransactionId") val clientTransactionId: String,
    @SerialName("cardId") val cardId: String,
    @SerialName("chargedAmount") val chargedAmount: String,
    @SerialName("cardBalanceAfter") val cardBalanceAfter: String,
    @SerialName("playedAt") val playedAt: String
)

@Serializable
data class UseGameResponse(
    @SerialName("logId") val logId: String,
    @SerialName("gameId") val gameId: String,
    @SerialName("userId") val userId: String,
    @SerialName("cardId") val cardId: String,
    @SerialName("clientTransactionId") val clientTransactionId: String? = null,
    @SerialName("chargedAmount") val chargedAmount: String? = null,
    @SerialName("balanceBefore") val balanceBefore: String? = null,
    @SerialName("balanceAfter") val balanceAfter: String? = null,
    @SerialName("cardBalanceAfter") val cardBalanceAfter: String? = null,
    @SerialName("playedAt") val playedAt: String
)

@Serializable
data class UseGameEnvelope(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: UseGameResponse? = null
)

@Serializable
data class CardLookupDto(
    @SerialName("cardId") val cardId: String,
    @SerialName("userId") val userId: String? = null,
    @SerialName("status") val status: String
)

@Serializable
data class CardLookupEnvelope(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: CardLookupDto? = null
)

@Serializable
data class CustomerSnapshotDto(
    @SerialName("userId") val userId: String,
    @SerialName("phoneNumber") val phoneNumber: String = "",
    @SerialName("fullName") val fullName: String? = null,
    @SerialName("currentBalance") val currentBalance: String = "0"
)

@Serializable
data class CustomerSnapshotEnvelope(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: CustomerSnapshotDto? = null
)

@Serializable
data class ErrorResponse(
    @SerialName("message") val message: String? = null
)
