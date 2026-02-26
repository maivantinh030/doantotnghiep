package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object UserVouchers : Table("user_vouchers") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36)
    val voucherId = varchar("voucher_id", 36)
    val voucherSource = varchar("source", 20).nullable()
    val isUsed = bool("is_used").default(false)
    val usedAt = timestamp("used_at").nullable()
    val usedOrderId = varchar("used_order_id", 36).nullable()
    val createdAt = timestamp("created_at").default(Instant.now())

    override val primaryKey = PrimaryKey(id)
}
