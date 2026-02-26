package com.park.entities

import java.math.BigDecimal
import java.time.Instant

data class BalanceTransaction(
    val transactionId: String,
    val userId: String,
    val amount: BigDecimal,
    val balanceBefore: BigDecimal,
    val balanceAfter: BigDecimal,
    val type: String,
    val referenceType: String?,
    val referenceId: String?,
    val description: String?,
    val createdAt: Instant,
    val createdBy: String?
)
