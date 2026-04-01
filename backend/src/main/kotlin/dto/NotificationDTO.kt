package com.park.dto

import com.park.entities.Notification
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object NotificationDataSchemas {
    const val GAME_PLAY = "notification.game_play.v1"
    const val SYSTEM_BROADCAST = "notification.system_broadcast.v1"
}

@Serializable
data class GamePlayNotificationData(
    val schema: String = NotificationDataSchemas.GAME_PLAY,
    val category: String = "GAME",
    val action: String = "PLAYED",
    val version: Int = 1,
    val gameId: String,
    val gameName: String,
    val logId: String,
    val cardId: String,
    val chargedAmount: String,
    val balanceBefore: String,
    val balanceAfter: String,
    val playedAt: String
)

@Serializable
data class BroadcastNotificationData(
    val schema: String = NotificationDataSchemas.SYSTEM_BROADCAST,
    val category: String = "SYSTEM",
    val action: String = "BROADCAST",
    val version: Int = 1,
    val broadcastId: String,
    val targetType: String,
    val sentBy: String
)

object NotificationDataCodec {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    fun encode(payload: GamePlayNotificationData): String = json.encodeToString(payload)

    fun encode(payload: BroadcastNotificationData): String = json.encodeToString(payload)

    fun decodeBroadcast(raw: String?): BroadcastNotificationData? {
        if (raw.isNullOrBlank()) return null
        return runCatching { json.decodeFromString<BroadcastNotificationData>(raw) }.getOrNull()
    }
}

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
