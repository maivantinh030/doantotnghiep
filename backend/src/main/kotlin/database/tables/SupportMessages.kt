package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object SupportMessages : Table("support_messages") {
    val messageId = varchar("message_id", 36)
    val userId = varchar("user_id", 36)
    val senderId = varchar("sender_id", 36)
    val senderType = varchar("sender_type", 10)
    val content = text("content")
    val isRead = bool("is_read").default(false)
    val createdAt = timestamp("created_at").default(Instant.now())

    override val primaryKey = PrimaryKey(messageId)
}
