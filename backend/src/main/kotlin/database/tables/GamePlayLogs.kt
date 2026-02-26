package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object GamePlayLogs : Table("game_play_logs") {
    val logId = varchar("log_id", 36)
    val userId = varchar("user_id", 36)
    val gameId = varchar("game_id", 36)
    val terminalId = varchar("terminal_id", 36).nullable()
    val cardId = varchar("card_id", 36).nullable()
    val ticketId = varchar("ticket_id", 36).nullable()
    val method = varchar("method", 20)
    val playedAt = timestamp("played_at").default(Instant.now())

    override val primaryKey = PrimaryKey(logId)
}
