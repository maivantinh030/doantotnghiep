package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Tickets : Table("tickets") {
    val ticketId = varchar("ticket_id", 36)
    val userId = varchar("user_id", 36)
    val gameId = varchar("game_id", 36).nullable()
    val purchaseOrderId = varchar("purchase_order_id", 36).nullable()
    val ticketType = varchar("ticket_type", 20).nullable()
    val remainingTurns = integer("remaining_turns").default(1)
    val originalTurns = integer("original_turns").default(1)
    val status = varchar("status", 20).default("VALID")
    val expiryDate = timestamp("expiry_date").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val usedAt = timestamp("used_at").nullable()

    override val primaryKey = PrimaryKey(ticketId)
}
