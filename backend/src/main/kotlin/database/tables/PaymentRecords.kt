package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object PaymentRecords : Table("payment_records") {
    val paymentId = varchar("payment_id", 36)
    val userId = varchar("user_id", 36)
    val method = varchar("method", 20)
    val amount = decimal("amount", 15, 2)
    val status = varchar("status", 20).default("PENDING")
    val createdAt = timestamp("created_at").default(Instant.now())

    override val primaryKey = PrimaryKey(paymentId)
}
