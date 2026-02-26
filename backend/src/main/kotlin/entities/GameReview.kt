package com.park.entities

import java.time.Instant

data class GameReview(
    val reviewId: String,
    val userId: String,
    val gameId: String,
    val rating: Int,
    val comment: String?,
    val isVerifiedPlay: Boolean,
    val isVisible: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)
