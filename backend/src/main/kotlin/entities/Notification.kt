package com.park.entities

import java.time.Instant

data class Notification(
    val notificationId: String,
    val userId: String,
    val type: String?,
    val title: String,
    val message: String,
    val data: String?,
    val isRead: Boolean,
    val readAt: Instant?,
    val createdAt: Instant
)
