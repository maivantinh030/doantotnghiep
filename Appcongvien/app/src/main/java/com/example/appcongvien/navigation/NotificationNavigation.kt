package com.example.appcongvien.navigation

import android.content.Intent
import com.example.appcongvien.data.model.NotificationDTO
import com.example.appcongvien.data.model.parseGamePlayData

data class NotificationNavigationRequest(
    val route: String,
    val notificationId: String?
)

object NotificationNavigationExtras {
    const val EXTRA_OPEN_FROM_PUSH = "com.example.appcongvien.extra.OPEN_FROM_PUSH"
    const val EXTRA_NOTIFICATION_ID = "com.example.appcongvien.extra.NOTIFICATION_ID"
    const val EXTRA_NOTIFICATION_TYPE = "com.example.appcongvien.extra.NOTIFICATION_TYPE"
    const val EXTRA_NOTIFICATION_DATA = "com.example.appcongvien.extra.NOTIFICATION_DATA"
}

fun NotificationDTO.toNavigationRequest(): NotificationNavigationRequest {
    parseGamePlayData()?.let { gamePlay ->
        return NotificationNavigationRequest(
            route = Screen.GameDetail.createRoute(gamePlay.gameId),
            notificationId = notificationId.takeIf { it.isNotBlank() }
        )
    }

    val fallbackRoute = when (type?.uppercase()) {
        "PAYMENT", "TOPUP" -> Screen.PaymentHistory.route
        "BALANCE", "BALANCE_LOW" -> Screen.Balance.route
        "CARD", "CARD_REQUEST" -> Screen.CardInfo.route
        "GAME", "GAME_UPDATE" -> Screen.GameList.route
        else -> Screen.Notifications.route
    }

    return NotificationNavigationRequest(
        route = fallbackRoute,
        notificationId = notificationId.takeIf { it.isNotBlank() }
    )
}

fun Intent?.toNotificationNavigationRequest(): NotificationNavigationRequest? {
    if (this == null || !getBooleanExtra(NotificationNavigationExtras.EXTRA_OPEN_FROM_PUSH, false)) {
        return null
    }

    val notificationId = getStringExtra(NotificationNavigationExtras.EXTRA_NOTIFICATION_ID)
    val type = getStringExtra(NotificationNavigationExtras.EXTRA_NOTIFICATION_TYPE)
    val data = getStringExtra(NotificationNavigationExtras.EXTRA_NOTIFICATION_DATA)

    return NotificationDTO(
        notificationId = notificationId.orEmpty(),
        userId = "",
        type = type,
        title = "",
        message = "",
        data = data,
        isRead = false,
        readAt = null,
        createdAt = ""
    ).toNavigationRequest()
}
