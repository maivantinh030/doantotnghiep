package com.park.entities

import java.time.Instant

data class Admin(
    val adminId: String,
    val accountId: String,
    val fullName: String,
    val employeeCode: String?,
    val lastActionAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
)
