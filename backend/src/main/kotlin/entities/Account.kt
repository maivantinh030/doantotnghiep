package com.park.entities

import java.time.Instant

/**
 * Entity đại diện cho một record trong bảng accounts
 */
data class Account(
    val accountId: String,
    val phoneNumber: String,
    val passwordHash: String,
    val role: String,
    val status: String,
    val lastLogin: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
)
