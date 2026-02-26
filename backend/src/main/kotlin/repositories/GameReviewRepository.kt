package com.park.repositories

import com.park.database.tables.GameReviews
import com.park.entities.GameReview
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

interface IGameReviewRepository {
    fun create(review: GameReview): GameReview
    fun findById(reviewId: String): GameReview?
    fun findByGameId(gameId: String, limit: Int, offset: Long): List<GameReview>
    fun findByUserId(userId: String): List<GameReview>
    fun findByUserAndGame(userId: String, gameId: String): GameReview?
    fun countByGameId(gameId: String): Long
    fun averageRatingByGameId(gameId: String): Double
    fun update(reviewId: String, updates: Map<String, Any?>): Boolean
    fun delete(reviewId: String): Boolean
}

class GameReviewRepository : IGameReviewRepository {

    override fun create(review: GameReview): GameReview {
        return transaction {
            GameReviews.insert {
                it[reviewId] = review.reviewId
                it[userId] = review.userId
                it[gameId] = review.gameId
                it[rating] = review.rating
                it[comment] = review.comment
                it[isVerifiedPlay] = review.isVerifiedPlay
                it[isVisible] = review.isVisible
                it[createdAt] = review.createdAt
                it[updatedAt] = review.updatedAt
            }
            review
        }
    }

    override fun findById(reviewId: String): GameReview? {
        return transaction {
            GameReviews.selectAll().where { GameReviews.reviewId eq reviewId }
                .singleOrNull()?.let { mapRow(it) }
        }
    }

    override fun findByGameId(gameId: String, limit: Int, offset: Long): List<GameReview> {
        return transaction {
            GameReviews.selectAll().where {
                (GameReviews.gameId eq gameId) and (GameReviews.isVisible eq true)
            }
                .orderBy(GameReviews.createdAt, SortOrder.DESC)
                .limit(limit).offset(offset)
                .map { mapRow(it) }
        }
    }

    override fun findByUserId(userId: String): List<GameReview> {
        return transaction {
            GameReviews.selectAll().where { GameReviews.userId eq userId }
                .orderBy(GameReviews.createdAt, SortOrder.DESC)
                .map { mapRow(it) }
        }
    }

    override fun findByUserAndGame(userId: String, gameId: String): GameReview? {
        return transaction {
            GameReviews.selectAll().where {
                (GameReviews.userId eq userId) and (GameReviews.gameId eq gameId)
            }.singleOrNull()?.let { mapRow(it) }
        }
    }

    override fun countByGameId(gameId: String): Long {
        return transaction {
            GameReviews.selectAll().where {
                (GameReviews.gameId eq gameId) and (GameReviews.isVisible eq true)
            }.count()
        }
    }

    override fun averageRatingByGameId(gameId: String): Double {
        return transaction {
            GameReviews.select(GameReviews.rating.avg())
                .where { (GameReviews.gameId eq gameId) and (GameReviews.isVisible eq true) }
                .singleOrNull()
                ?.get(GameReviews.rating.avg())
                ?.toDouble() ?: 0.0
        }
    }

    override fun update(reviewId: String, updates: Map<String, Any?>): Boolean {
        return transaction {
            GameReviews.update(where = { GameReviews.reviewId eq reviewId }) { stmt ->
                updates.forEach { (key, value) ->
                    when (key) {
                        "rating" -> stmt[rating] = value as Int
                        "comment" -> stmt[comment] = value as? String
                        "isVisible" -> stmt[isVisible] = value as Boolean
                    }
                }
                stmt[updatedAt] = Instant.now()
            } > 0
        }
    }

    override fun delete(reviewId: String): Boolean {
        return transaction {
            GameReviews.deleteWhere { GameReviews.reviewId eq reviewId } > 0
        }
    }

    private fun mapRow(row: ResultRow): GameReview {
        return GameReview(
            reviewId = row[GameReviews.reviewId],
            userId = row[GameReviews.userId],
            gameId = row[GameReviews.gameId],
            rating = row[GameReviews.rating],
            comment = row[GameReviews.comment],
            isVerifiedPlay = row[GameReviews.isVerifiedPlay],
            isVisible = row[GameReviews.isVisible],
            createdAt = row[GameReviews.createdAt],
            updatedAt = row[GameReviews.updatedAt]
        )
    }
}
