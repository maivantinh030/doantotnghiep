package com.park.dto

import com.park.entities.Game
import kotlinx.serialization.Serializable

@Serializable
data class GameDTO(
    val gameId: String,
    val name: String,
    val description: String?,
    val shortDescription: String?,
    val category: String?,
    val pricePerTurn: String,
    val durationMinutes: Int?,
    val location: String?,
    val thumbnailUrl: String?,
    val galleryUrls: List<String>?,
    val ageRequired: Int?,
    val heightRequired: Int?,
    val maxCapacity: Int?,
    val status: String,
    val riskLevel: Int?,
    val isFeatured: Boolean,
    val averageRating: String,
    val totalReviews: Int,
    val totalPlays: Int
) {
    companion object {
        fun fromEntity(game: Game): GameDTO {
            return GameDTO(
                gameId = game.gameId,
                name = game.name,
                description = game.description,
                shortDescription = game.shortDescription,
                category = game.category,
                pricePerTurn = game.pricePerTurn.toString(),
                durationMinutes = game.durationMinutes,
                location = game.location,
                thumbnailUrl = game.thumbnailUrl,
                galleryUrls = parseGalleryUrls(game.galleryUrls),
                ageRequired = game.ageRequired,
                heightRequired = game.heightRequired,
                maxCapacity = game.maxCapacity,
                status = game.status,
                riskLevel = game.riskLevel,
                isFeatured = game.isFeatured,
                averageRating = game.averageRating.toString(),
                totalReviews = game.totalReviews,
                totalPlays = game.totalPlays
            )
        }

        private fun parseGalleryUrls(json: String?): List<String>? {
            if (json.isNullOrBlank()) return null
            return try {
                json.removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim().removeSurrounding("\"") }
                    .filter { it.isNotBlank() }
            } catch (_: Exception) {
                null
            }
        }
    }
}

@Serializable
data class GameListItemDTO(
    val gameId: String,
    val name: String,
    val shortDescription: String?,
    val category: String?,
    val pricePerTurn: String,
    val location: String?,
    val thumbnailUrl: String?,
    val ageRequired: Int?,
    val heightRequired: Int?,
    val status: String,
    val riskLevel: Int?,
    val isFeatured: Boolean,
    val averageRating: String,
    val totalReviews: Int
) {
    companion object {
        fun fromEntity(game: Game): GameListItemDTO {
            return GameListItemDTO(
                gameId = game.gameId,
                name = game.name,
                shortDescription = game.shortDescription,
                category = game.category,
                pricePerTurn = game.pricePerTurn.toString(),
                location = game.location,
                thumbnailUrl = game.thumbnailUrl,
                ageRequired = game.ageRequired,
                heightRequired = game.heightRequired,
                status = game.status,
                riskLevel = game.riskLevel,
                isFeatured = game.isFeatured,
                averageRating = game.averageRating.toString(),
                totalReviews = game.totalReviews
            )
        }
    }
}

@Serializable
data class CreateGameRequest(
    val name: String,
    val description: String? = null,
    val shortDescription: String? = null,
    val category: String? = null,
    val pricePerTurn: String,
    val durationMinutes: Int? = null,
    val location: String? = null,
    val thumbnailUrl: String? = null,
    val galleryUrls: List<String>? = null,
    val ageRequired: Int? = null,
    val heightRequired: Int? = null,
    val maxCapacity: Int? = null,
    val riskLevel: Int? = null,
    val isFeatured: Boolean = false
)

@Serializable
data class UseGameRequest(
    val cardId: String? = null,
    val cardUid: String? = null
)

@Serializable
data class SyncGamePlayRequest(
    val clientTransactionId: String,
    val cardId: String,
    val chargedAmount: String,
    val cardBalanceAfter: String,
    val playedAt: String
)

@Serializable
data class UseGameResponse(
    val logId: String,
    val gameId: String,
    val userId: String,
    val cardId: String,
    val clientTransactionId: String? = null,
    val ticketId: String? = null,
    val remainingTurns: Int? = null,
    val ticketStatus: String? = null,
    val chargedAmount: String? = null,
    val balanceBefore: String? = null,
    val balanceAfter: String? = null,
    val cardBalanceAfter: String? = null,
    val balanceTransactionId: String? = null,
    val playedAt: String
)

@Serializable
data class UpdateGameRequest(
    val name: String? = null,
    val description: String? = null,
    val shortDescription: String? = null,
    val category: String? = null,
    val pricePerTurn: String? = null,
    val durationMinutes: Int? = null,
    val location: String? = null,
    val thumbnailUrl: String? = null,
    val galleryUrls: List<String>? = null,
    val ageRequired: Int? = null,
    val heightRequired: Int? = null,
    val maxCapacity: Int? = null,
    val status: String? = null,
    val riskLevel: Int? = null,
    val isFeatured: Boolean? = null
)
