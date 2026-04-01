package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Users : Table("users") {
    val userId = varchar("user_id", 36)
    val accountId = varchar("account_id", 36).nullable().uniqueIndex()
    val fullName = varchar("full_name", 100).nullable()
    val email = varchar("email", 100).nullable()
    val dateOfBirth = date("date_of_birth").nullable()
    val gender = varchar("gender", 10).nullable()           // MALE, FEMALE, OTHER
    val currentBalance = decimal("current_balance", 15, 2).default(java.math.BigDecimal.ZERO)
    val avatarUrl = text("avatar_url").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(userId)
}
