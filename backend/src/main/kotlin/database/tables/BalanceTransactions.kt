package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object BalanceTransactions : Table("balance_transactions") {
    val transactionId = varchar("transaction_id", 36)
    val userId = varchar("user_id", 36)
    val amount = decimal("amount", 15, 2)
    val balanceBefore = decimal("balance_before", 15, 2)
    val balanceAfter = decimal("balance_after", 15, 2)
    val type = varchar("type", 20)
    val referenceType = varchar("reference_type", 50).nullable()
    val referenceId = varchar("reference_id", 36).nullable()
    val description = text("description").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val createdBy = varchar("created_by", 36).nullable()

    override val primaryKey = PrimaryKey(transactionId)
}
