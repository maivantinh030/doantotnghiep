package com.park.entities

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

data class User(
    val userId: String,
    val accountId: String?,
    val fullName: String?,
    val email: String?,
    val dateOfBirth: LocalDate?,
    val gender: String?,
    val currentBalance: BigDecimal,
    val avatarUrl: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)
