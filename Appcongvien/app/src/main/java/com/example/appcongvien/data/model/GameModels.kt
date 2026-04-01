package com.example.appcongvien.data.model

// ===== Responses =====
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
    val riskLevel: Int?,
    val isFeatured: Boolean,
    val status: String,
    val averageRating: String?,
    val totalReviews: Int = 0,
    val totalPlays: Int = 0
)

data class GameReviewDTO(
    val reviewId: String,
    val userId: String,
    val userName: String?,
    val gameId: String,
    val rating: Int,
    val comment: String?,
    val createdAt: String
)

// ===== Requests =====
data class CreateReviewRequest(
    val gameId: String,
    val rating: Int,
    val comment: String? = null
)

data class UpdateReviewRequest(
    val rating: Int? = null,
    val comment: String? = null
)
