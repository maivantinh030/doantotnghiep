package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Cards : Table("cards") {
    val cardId = varchar("card_id", 36)
    val physicalCardUid = varchar("physical_card_uid", 50).uniqueIndex()
    val userId = varchar("user_id", 36).nullable()          // NULL = thẻ chưa liên kết
    val cardName = varchar("card_name", 50).nullable()
    val status = varchar("status", 20).default("AVAILABLE") // AVAILABLE | ACTIVE | BLOCKED
    val depositAmount = decimal("deposit_amount", 15, 2).default(java.math.BigDecimal.ZERO)
    val depositStatus = varchar("deposit_status", 20).default("NONE") // NONE | PAID | REFUNDED | FORFEITED
    val issuedAt = timestamp("issued_at").nullable()
    val blockedAt = timestamp("blocked_at").nullable()
    val blockedReason = text("blocked_reason").nullable()
    val lastUsedAt = timestamp("last_used_at").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(cardId)
}
