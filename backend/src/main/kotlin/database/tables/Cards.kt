package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Cards : Table("cards") {
    val cardId = varchar("card_id", 36)
    val physicalCardUid = varchar("physical_card_uid", 50).uniqueIndex().nullable()
    val virtualCardUid = varchar("virtual_card_uid", 50).uniqueIndex().nullable()
    val cardType = varchar("card_type", 10).default("PHYSICAL") // PHYSICAL | VIRTUAL | BOTH
    val userId = varchar("user_id", 36).nullable()
    val cardName = varchar("card_name", 50).nullable()
    val status = varchar("status", 20).default("INACTIVE")
    val pinHash = varchar("pin_hash", 255).nullable()
    val issuedAt = timestamp("issued_at").nullable()
    val blockedAt = timestamp("blocked_at").nullable()
    val blockedReason = text("blocked_reason").nullable()
    val lastUsedAt = timestamp("last_used_at").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(cardId)
}
