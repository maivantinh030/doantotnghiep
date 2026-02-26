package com.park.dto

import com.park.entities.Notification
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDTO(
    val notificationId: String,
    val userId: String,
    val type: String?,
    val title: String,
    val message: String,
    val data: String?,
    val isRead: Boolean,
    val readAt: String?,
    val createdAt: String
) {
    companion object {
        fun fromEntity(n: Notification): NotificationDTO {
            return NotificationDTO(
                notificationId = n.notificationId,
                userId = n.userId,
                type = n.type,
                title = n.title,
                message = n.message,
                data = n.data,
                isRead = n.isRead,
                readAt = n.readAt?.toString(),
                createdAt = n.createdAt.toString()
            )
        }
    }
}
