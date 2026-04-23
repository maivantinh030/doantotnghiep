package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object GamePlayLogs : Table("game_play_logs") {
    val logId = varchar("log_id", 36)
    val clientTransactionId = varchar("client_transaction_id", 64).nullable().uniqueIndex()
    val userId = varchar("user_id", 36)
    val gameId = varchar("game_id", 36)
    val cardId = varchar("card_id", 36).nullable()
    val method = varchar("method", 20).default("CARD")      // CARD | BALANCE
    val amountCharged = decimal("amount_charged", 10, 2).default(java.math.BigDecimal.ZERO)
    val cardBalanceAfter = decimal("card_balance_after", 15, 2).nullable()
    val playedAt = timestamp("played_at").default(Instant.now())

    override val primaryKey = PrimaryKey(logId)
}
