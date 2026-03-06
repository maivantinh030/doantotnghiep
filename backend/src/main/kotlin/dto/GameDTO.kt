package com.park.dto

import com.park.entities.Game
import kotlinx.serialization.Serializable

/**
 * DTO trả về thông tin game cho client
 */
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
                // Parse JSON array string: ["url1","url2"]
                json.removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim().removeSurrounding("\"") }
                    .filter { it.isNotBlank() }
            } catch (e: Exception) {
                null
            }
        }
    }
}

/**
 * DTO cho danh sách game (chỉ trả về thông tin cơ bản)
 */
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

/**
 * Request tạo game mới (Admin)
 */
@Serializable
data class CreateGameRequest(
    val name: String,
    val description: String? = null,
    val shortDescription: String? = null,
    val category: String? = null,
    val pricePerTurn: String, // Decimal as String
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

/**
 * Request sử dụng game - terminal gửi lên sau khi quét NFC
 */
@Serializable
data class UseGameRequest(
    val cardUid: String,       // UID vật lý hoặc ảo đọc từ NFC
    val terminalId: String? = null
)

/**
 * Response sau khi sử dụng game thành công
 */
@Serializable
data class UseGameResponse(
    val logId: String,
    val gameId: String,
    val userId: String,
    val cardId: String,
    val ticketId: String,
    val remainingTurns: Int,
    val ticketStatus: String,
    val playedAt: String
)

/**
 * Request cập nhật game (Admin)
 */
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
