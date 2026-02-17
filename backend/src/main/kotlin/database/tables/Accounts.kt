package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Accounts : Table("accounts") {
    val accountId = varchar("account_id", 36)
    val phoneNumber = varchar("phone_number", 15).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 20).default("USER") // USER, ADMIN, STAFF
    val status = varchar("status", 20).default("ACTIVE") // ACTIVE, BANNED
    val lastLogin = timestamp("last_login").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(accountId)
}
