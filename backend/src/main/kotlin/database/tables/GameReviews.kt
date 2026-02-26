package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object GameReviews : Table("game_reviews") {
    val reviewId = varchar("review_id", 36)
    val userId = varchar("user_id", 36)
    val gameId = varchar("game_id", 36)
    val rating = integer("rating")
    val comment = text("comment").nullable()
    val isVerifiedPlay = bool("is_verified_play").default(false)
    val isVisible = bool("is_visible").default(true)
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(reviewId)
}
