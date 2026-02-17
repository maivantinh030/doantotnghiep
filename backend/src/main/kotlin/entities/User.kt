package com.park.entities

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

/**
 * Entity đại diện cho một record trong bảng users
 */
data class User(
    val userId: String,
    val accountId: String,
    val fullName: String?,
    val email: String?,
    val dateOfBirth: LocalDate?,
    val gender: String?,
    val membershipLevel: String,
    val currentBalance: BigDecimal,
    val loyaltyPoints: Int,
    val avatarUrl: String?,
    val isCardLocked: Boolean,
    val referralCode: String?,
    val referredBy: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)
