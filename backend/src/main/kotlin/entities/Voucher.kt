package com.park.entities

import java.math.BigDecimal
import java.time.Instant

data class Voucher(
    val voucherId: String,
    val code: String,
    val title: String,
    val description: String?,
    val discountType: String,
    val discountValue: BigDecimal,
    val maxDiscount: BigDecimal?,
    val minOrderValue: BigDecimal,
    val usageLimit: Int?,
    val usedCount: Int,
    val perUserLimit: Int,
    val applicableGames: String?,
    val startDate: Instant,
    val endDate: Instant,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)
