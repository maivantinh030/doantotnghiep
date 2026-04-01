package com.park.entities

import java.math.BigDecimal
import java.time.Instant

data class Card(
    val cardId: String,
    val userId: String?,
    val cardName: String?,
    val status: String,                 // AVAILABLE | ACTIVE | BLOCKED
    val depositAmount: BigDecimal,
    val depositStatus: String,          // NONE | PAID | REFUNDED | FORFEITED
    val issuedAt: Instant?,
    val blockedAt: Instant?,
    val blockedReason: String?,
    val lastUsedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
)
