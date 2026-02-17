package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Users : Table("users") {
    val userId = varchar("user_id", 36)
    val accountId = varchar("account_id", 36).uniqueIndex()
    val fullName = varchar("full_name", 100).nullable()
    val email = varchar("email", 100).nullable()
    val dateOfBirth = date("date_of_birth").nullable()
    val gender = varchar("gender", 10).nullable() // MALE, FEMALE, OTHER
    val membershipLevel = varchar("membership_level", 20).default("BRONZE") // BRONZE, SILVER, GOLD, PLATINUM
    val currentBalance = decimal("current_balance", 15, 2).default(java.math.BigDecimal.ZERO)
    val loyaltyPoints = integer("loyalty_points").default(0)
    val avatarUrl = text("avatar_url").nullable()
    val isCardLocked = bool("is_card_locked").default(false)
    val referralCode = varchar("referral_code", 20).nullable().uniqueIndex()
    val referredBy = varchar("referred_by", 36).nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(userId)
}
