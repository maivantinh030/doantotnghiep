package com.park.entities

import java.math.BigDecimal
import java.time.Instant

data class BookingOrder(
    val orderId: String,
    val userId: String,
    val subtotal: BigDecimal,
    val discountAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val voucherId: String?,
    val paymentMethod: String?,
    val status: String,
    val note: String?,
    val createdAt: Instant,
    val completedAt: Instant?,
    val cancelledAt: Instant?,
    val cancelledReason: String?
)

data class BookingOrderDetail(
    val detailId: String,
    val orderId: String,
    val gameId: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val lineTotal: BigDecimal
)
