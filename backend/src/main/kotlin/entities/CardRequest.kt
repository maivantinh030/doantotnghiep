package com.park.entities

import java.math.BigDecimal
import java.time.Instant

data class CardRequest(
    val requestId: String,
    val userId: String,
    val status: String,                 // PENDING | APPROVED | REJECTED | COMPLETED
    val depositPaidOnline: Boolean,
    val depositAmount: BigDecimal,
    val note: String?,
    val approvedBy: String?,            // admin_id
    val createdAt: Instant,
    val updatedAt: Instant
)
