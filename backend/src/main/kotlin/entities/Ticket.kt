package com.park.entities

import java.time.Instant

data class Ticket(
    val ticketId: String,
    val userId: String,
    val gameId: String?,
    val purchaseOrderId: String?,
    val ticketType: String?,
    val remainingTurns: Int,
    val originalTurns: Int,
    val status: String,
    val expiryDate: Instant?,
    val createdAt: Instant,
    val usedAt: Instant?
)
