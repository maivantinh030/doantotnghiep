package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Games : Table("games") {
    val gameId = varchar("game_id", 36)
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val shortDescription = varchar("short_description", 255).nullable()
    val category = varchar("category", 50).nullable()
    val pricePerTurn = decimal("price_per_turn", 10, 2)
    val durationMinutes = integer("duration_minutes").nullable()
    val location = varchar("location", 100).nullable()
    val thumbnailUrl = text("thumbnail_url").nullable()
    val galleryUrls = text("gallery_urls").nullable() // JSON array
    val ageRequired = integer("age_required").nullable()
    val heightRequired = integer("height_required").nullable()
    val maxCapacity = integer("max_capacity").nullable()
    val status = varchar("status", 20).default("ACTIVE")
    val riskLevel = integer("risk_level").nullable()
    val isFeatured = bool("is_featured").default(false)
    val averageRating = decimal("average_rating", 2, 1).default(java.math.BigDecimal("0.0"))
    val totalReviews = integer("total_reviews").default(0)
    val totalPlays = integer("total_plays").default(0)
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(gameId)
}
