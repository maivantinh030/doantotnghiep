package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object UserPushTokens : Table("user_push_tokens") {
    val tokenId = varchar("token_id", 36)
    val userId = varchar("user_id", 36)
    val fcmToken = varchar("fcm_token", 512).uniqueIndex()
    val platform = varchar("platform", 20).default("ANDROID")
    val deviceId = varchar("device_id", 128).nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(tokenId)
}
