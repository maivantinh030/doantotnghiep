package com.park.entities

import java.math.BigDecimal
import java.time.Instant

data class MomoTransaction(
    val orderId: String,
    val userId: String,
    val amount: BigDecimal,
    val status: String,
    val description: String,
    val momoTransId: String?,
    val qrData: String?,
    val createdAt: Instant,
    val completedAt: Instant?
)