package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object BookingOrders : Table("booking_orders") {
    val orderId = varchar("order_id", 36)
    val userId = varchar("user_id", 36)
    val subtotal = decimal("subtotal", 15, 2)
    val discountAmount = decimal("discount_amount", 15, 2).default(java.math.BigDecimal.ZERO)
    val totalAmount = decimal("total_amount", 15, 2)
    val voucherId = varchar("voucher_id", 36).nullable()
    val paymentMethod = varchar("payment_method", 20).nullable()
    val status = varchar("status", 20).default("PENDING")
    val note = text("note").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val completedAt = timestamp("completed_at").nullable()
    val cancelledAt = timestamp("cancelled_at").nullable()
    val cancelledReason = text("cancelled_reason").nullable()

    override val primaryKey = PrimaryKey(orderId)
}
