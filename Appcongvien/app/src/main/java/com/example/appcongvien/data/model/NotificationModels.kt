package com.example.appcongvien.data.model

import org.json.JSONObject

object NotificationDataSchemas {
    const val GAME_PLAY = "notification.game_play.v1"
    const val SYSTEM_BROADCAST = "notification.system_broadcast.v1"
}

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
)

data class UnreadCountDTO(
    val count: Int
)

data class NotificationDataEnvelope(
    val schema: String?,
    val category: String?,
    val action: String?,
    val version: Int?
)

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

data class BroadcastNotificationData(
    val schema: String = NotificationDataSchemas.SYSTEM_BROADCAST,
    val category: String = "SYSTEM",
    val action: String = "BROADCAST",
    val version: Int = 1,
    val broadcastId: String,
    val targetType: String,
    val sentBy: String
)

fun NotificationDTO.parseDataEnvelope(): NotificationDataEnvelope? {
    return NotificationDataParser.parseEnvelope(data)
}

fun NotificationDTO.parseGamePlayData(): GamePlayNotificationData? {
    return NotificationDataParser.parseGamePlay(data)
}

fun NotificationDTO.parseBroadcastData(): BroadcastNotificationData? {
    return NotificationDataParser.parseBroadcast(data)
}

object NotificationDataParser {
    fun parseEnvelope(raw: String?): NotificationDataEnvelope? {
        val json = raw.toJsonObject() ?: return null
        return NotificationDataEnvelope(
            schema = json.stringOrNull("schema"),
            category = json.stringOrNull("category"),
            action = json.stringOrNull("action"),
            version = json.intOrNull("version")
        )
    }

    fun parseGamePlay(raw: String?): GamePlayNotificationData? {
        val json = raw.toJsonObject() ?: return null
        val gameId = json.stringOrNull("gameId") ?: return null
        val logId = json.stringOrNull("logId") ?: return null
        val cardId = json.stringOrNull("cardId") ?: return null
        val chargedAmount = json.stringOrNull("chargedAmount") ?: return null
        val balanceAfter = json.stringOrNull("balanceAfter") ?: return null

        return GamePlayNotificationData(
            schema = json.stringOrNull("schema") ?: NotificationDataSchemas.GAME_PLAY,
            category = json.stringOrNull("category") ?: "GAME",
            action = json.stringOrNull("action") ?: "PLAYED",
            version = json.intOrNull("version") ?: 1,
            gameId = gameId,
            gameName = json.stringOrNull("gameName") ?: "",
            logId = logId,
            cardId = cardId,
            chargedAmount = chargedAmount,
            balanceBefore = json.stringOrNull("balanceBefore") ?: "",
            balanceAfter = balanceAfter,
            playedAt = json.stringOrNull("playedAt") ?: ""
        )
    }

    fun parseBroadcast(raw: String?): BroadcastNotificationData? {
        val json = raw.toJsonObject() ?: return null
        val broadcastId = json.stringOrNull("broadcastId") ?: return null
        val targetType = json.stringOrNull("targetType") ?: return null
        val sentBy = json.stringOrNull("sentBy") ?: return null

        return BroadcastNotificationData(
            schema = json.stringOrNull("schema") ?: NotificationDataSchemas.SYSTEM_BROADCAST,
            category = json.stringOrNull("category") ?: "SYSTEM",
            action = json.stringOrNull("action") ?: "BROADCAST",
            version = json.intOrNull("version") ?: 1,
            broadcastId = broadcastId,
            targetType = targetType,
            sentBy = sentBy
        )
    }

    private fun String?.toJsonObject(): JSONObject? {
        if (this.isNullOrBlank()) return null
        return runCatching { JSONObject(this) }.getOrNull()
    }

    private fun JSONObject.stringOrNull(key: String): String? {
        if (!has(key) || isNull(key)) return null
        return optString(key).takeIf { it.isNotBlank() && it != "null" }
    }

    private fun JSONObject.intOrNull(key: String): Int? {
        if (!has(key) || isNull(key)) return null
        return runCatching { getInt(key) }.getOrNull()
    }
}
