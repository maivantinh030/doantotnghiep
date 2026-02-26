package com.park.entities

import java.math.BigDecimal
import java.time.Instant

data class PaymentRecord(
    val paymentId: String,
    val userId: String,
    val method: String,
    val amount: BigDecimal,
    val status: String,
    val createdAt: Instant
)
