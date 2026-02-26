package com.example.appcongvien.data.model

data class NotificationDTO(
    val notificationId: String,
    val userId: String,
    val type: String?,          // "PROMOTION" | "VOUCHER" | "BIRTHDAY" | "EVENT" | "BALANCE" | "SYSTEM" | ...
    val title: String,
    val message: String,        // backend field là "message"
    val data: String?,          // JSON extra data (nullable)
    val isRead: Boolean,
    val readAt: String?,
    val createdAt: String
)

data class UnreadCountDTO(
    val count: Int
)
