package com.example.appcongvien.navigation

import android.content.Intent
import com.example.appcongvien.data.model.NotificationDTO

fun extractNotificationNavigationRequest(intent: Intent?): NotificationNavigationRequest? {
    return intent.toNotificationNavigationRequest()
}

fun mapNotificationToRoute(notification: NotificationDTO): NotificationNavigationRequest {
    return notification.toNavigationRequest()
}
