package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Announcements : Table("announcements") {
    val announcementId = varchar("announcement_id", 36)
    val title = varchar("title", 100)
    val description = text("description").nullable()
    val imageUrl = varchar("image_url", 500)
    val linkType = varchar("link_type", 20).nullable()   // GAME | VOUCHER | SCREEN | null
    val linkValue = varchar("link_value", 255).nullable() // gameId | voucherCode | screenName
    val isActive = bool("is_active").default(true)
    val sortOrder = integer("sort_order").default(0)
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(announcementId)
}
