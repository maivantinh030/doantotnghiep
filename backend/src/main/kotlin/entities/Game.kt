package com.park.entities

import java.math.BigDecimal
import java.time.Instant

/**
 * Entity đại diện cho một record trong bảng games
 */
data class Game(
    val gameId: String,
    val name: String,
    val description: String?,
    val shortDescription: String?,
    val category: String?,
    val pricePerTurn: BigDecimal,
    val durationMinutes: Int?,
    val location: String?,
    val thumbnailUrl: String?,
    val galleryUrls: String?, // JSON array string
    val ageRequired: Int?,
    val heightRequired: Int?,
    val maxCapacity: Int?,
    val status: String,
    val riskLevel: Int?,
    val isFeatured: Boolean,
    val averageRating: BigDecimal,
    val totalReviews: Int,
    val totalPlays: Int,
    val createdAt: Instant,
    val updatedAt: Instant
)
