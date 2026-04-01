package com.park.services

import com.park.dto.NotificationDTO
import com.park.entities.Notification
import com.park.repositories.INotificationRepository
import com.park.repositories.NotificationRepository
import java.time.Instant
import java.util.*

class NotificationService(
    private val notificationRepository: INotificationRepository = NotificationRepository(),
    private val firebasePushService: FirebasePushService = FirebasePushService()
) {

    fun getNotifications(userId: String, page: Int, size: Int): Map<String, Any> {
        val offset = ((page - 1) * size).toLong()
        val notifications = notificationRepository.findByUserId(userId, size, offset)
        val total = notificationRepository.countByUserId(userId)
        val unreadCount = notificationRepository.countUnreadByUserId(userId)

        return mapOf(
            "items" to notifications.map { NotificationDTO.fromEntity(it) },
            "total" to total,
            "unreadCount" to unreadCount,
            "page" to page,
            "size" to size,
            "totalPages" to if (size > 0) ((total + size - 1) / size) else 1
        )
    }

    fun getUnreadCount(userId: String): Long {
        return notificationRepository.countUnreadByUserId(userId)
    }

    fun markAsRead(notificationId: String, userId: String): Boolean {
        return notificationRepository.markAsRead(notificationId)
    }

    fun markAllAsRead(userId: String): Boolean {
        return notificationRepository.markAllAsRead(userId)
    }

    fun deleteNotification(notificationId: String): Boolean {
        return notificationRepository.delete(notificationId)
    }

    fun createNotification(userId: String, type: String, title: String, message: String, data: String? = null): NotificationDTO {
        val notification = Notification(
            notificationId = UUID.randomUUID().toString(),
            userId = userId,
            type = type,
            title = title,
            message = message,
            data = data,
            isRead = false,
            readAt = null,
            createdAt = Instant.now()
        )
        val created = notificationRepository.create(notification)
        firebasePushService.sendNotificationToUser(
            userId = userId,
            title = title,
            message = message,
            type = type,
            notificationId = created.notificationId,
            rawData = data
        )
        return NotificationDTO.fromEntity(created)
    }
}
