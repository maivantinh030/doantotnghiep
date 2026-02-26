package com.park.entities

import java.time.Instant

data class SupportMessage(
    val messageId: String,
    val userId: String,
    val senderId: String,
    val senderType: String,
    val content: String,
    val isRead: Boolean,
    val createdAt: Instant
)
