package com.park.entities

import java.time.Instant

data class UserPushToken(
    val tokenId: String,
    val userId: String,
    val fcmToken: String,
    val platform: String,
    val deviceId: String?,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)
