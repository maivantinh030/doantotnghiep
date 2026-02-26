package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Notifications : Table("notifications") {
    val notificationId = varchar("notification_id", 36)
    val userId = varchar("user_id", 36)
    val type = varchar("type", 20).nullable()
    val title = varchar("title", 100)
    val message = text("message")
    val data = text("data").nullable() // JSON
    val isRead = bool("is_read").default(false)
    val readAt = timestamp("read_at").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())

    override val primaryKey = PrimaryKey(notificationId)
}
