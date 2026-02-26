package com.park.entities

import java.time.Instant

data class UserVoucher(
    val id: String,
    val userId: String,
    val voucherId: String,
    val source: String?,
    val isUsed: Boolean,
    val usedAt: Instant?,
    val usedOrderId: String?,
    val createdAt: Instant
)
