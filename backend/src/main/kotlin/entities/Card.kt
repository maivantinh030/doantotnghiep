package com.park.entities

import java.time.Instant

data class Card(
    val cardId: String,
    val physicalCardUid: String?,
    val virtualCardUid: String?,
    val cardType: String,
    val userId: String?,
    val cardName: String?,
    val status: String,
    val pinHash: String?,
    val issuedAt: Instant?,
    val blockedAt: Instant?,
    val blockedReason: String?,
    val lastUsedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
)
