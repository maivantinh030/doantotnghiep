package com.park.dto

import com.park.entities.GameReview
import kotlinx.serialization.Serializable

@Serializable
data class GameReviewDTO(
    val reviewId: String,
    val userId: String,
    val gameId: String,
    val rating: Int,
    val comment: String?,
    val isVerifiedPlay: Boolean,
    val createdAt: String,
    val userName: String? = null
) {
    companion object {
        fun fromEntity(review: GameReview, userName: String? = null): GameReviewDTO {
            return GameReviewDTO(
                reviewId = review.reviewId,
                userId = review.userId,
                gameId = review.gameId,
                rating = review.rating,
                comment = review.comment,
                isVerifiedPlay = review.isVerifiedPlay,
                createdAt = review.createdAt.toString(),
                userName = userName
            )
        }
    }
}

@Serializable
data class CreateReviewRequest(
    val gameId: String,
    val rating: Int,
    val comment: String? = null
)

@Serializable
data class UpdateReviewRequest(
    val rating: Int? = null,
    val comment: String? = null
)
