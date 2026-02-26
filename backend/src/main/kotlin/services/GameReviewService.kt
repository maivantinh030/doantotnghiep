package com.park.services

import com.park.dto.*
import com.park.entities.GameReview
import com.park.repositories.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.util.*

class GameReviewService(
    private val reviewRepository: IGameReviewRepository = GameReviewRepository(),
    private val gameRepository: IGameRepository = GameRepository(),
    private val userRepository: IUserRepository = UserRepository()
) {

    fun getReviewsByGameId(gameId: String, page: Int, size: Int): Map<String, Any> {
        val offset = ((page - 1) * size).toLong()
        val reviews = reviewRepository.findByGameId(gameId, size, offset)
        val total = reviewRepository.countByGameId(gameId)

        val dtos = reviews.map { review ->
            val user = userRepository.findById(review.userId)
            GameReviewDTO.fromEntity(review, user?.fullName)
        }

        return mapOf(
            "reviews" to dtos,
            "total" to total,
            "page" to page,
            "size" to size
        )
    }

    fun createReview(userId: String, request: CreateReviewRequest): Result<GameReviewDTO> {
        if (request.rating < 1 || request.rating > 5) {
            return Result.failure(IllegalArgumentException("Rating phải từ 1 đến 5"))
        }

        gameRepository.findById(request.gameId)
            ?: return Result.failure(NoSuchElementException("Game không tồn tại"))

        val existing = reviewRepository.findByUserAndGame(userId, request.gameId)
        if (existing != null) {
            return Result.failure(IllegalStateException("Bạn đã đánh giá game này rồi"))
        }

        val now = Instant.now()
        val review = GameReview(
            reviewId = UUID.randomUUID().toString(),
            userId = userId,
            gameId = request.gameId,
            rating = request.rating,
            comment = request.comment,
            isVerifiedPlay = false,
            isVisible = true,
            createdAt = now,
            updatedAt = now
        )

        reviewRepository.create(review)
        updateGameRating(request.gameId)

        val user = userRepository.findById(userId)
        return Result.success(GameReviewDTO.fromEntity(review, user?.fullName))
    }

    fun updateReview(reviewId: String, userId: String, request: UpdateReviewRequest): Result<GameReviewDTO> {
        val review = reviewRepository.findById(reviewId)
            ?: return Result.failure(NoSuchElementException("Review không tồn tại"))

        if (review.userId != userId) {
            return Result.failure(IllegalAccessException("Không có quyền chỉnh sửa review này"))
        }

        val updates = mutableMapOf<String, Any?>()
        request.rating?.let {
            if (it < 1 || it > 5) return Result.failure(IllegalArgumentException("Rating phải từ 1 đến 5"))
            updates["rating"] = it
        }
        request.comment?.let { updates["comment"] = it }

        if (updates.isNotEmpty()) {
            reviewRepository.update(reviewId, updates)
            updateGameRating(review.gameId)
        }

        val updated = reviewRepository.findById(reviewId)!!
        val user = userRepository.findById(userId)
        return Result.success(GameReviewDTO.fromEntity(updated, user?.fullName))
    }

    fun deleteReview(reviewId: String, userId: String): Boolean {
        val review = reviewRepository.findById(reviewId) ?: return false
        if (review.userId != userId) return false
        val deleted = reviewRepository.delete(reviewId)
        if (deleted) updateGameRating(review.gameId)
        return deleted
    }

    private fun updateGameRating(gameId: String) {
        val avg = reviewRepository.averageRatingByGameId(gameId)
        val count = reviewRepository.countByGameId(gameId)
        gameRepository.update(gameId, mapOf(
            "averageRating" to BigDecimal(avg).setScale(1, RoundingMode.HALF_UP),
            "totalReviews" to count.toInt()
        ))
    }
}
