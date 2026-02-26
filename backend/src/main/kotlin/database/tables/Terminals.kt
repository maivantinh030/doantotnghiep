package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Terminals : Table("terminals") {
    val terminalId = varchar("terminal_id", 36)
    val name = varchar("name", 100)
    val gameId = varchar("game_id", 36).nullable()
    val terminalType = varchar("terminal_type", 20).nullable()
    val location = varchar("location", 100).nullable()
    val status = varchar("status", 20).default("OFFLINE")
    val lastHeartbeat = timestamp("last_heartbeat").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(terminalId)
}
