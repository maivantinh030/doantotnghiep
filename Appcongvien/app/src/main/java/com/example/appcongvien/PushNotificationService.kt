package com.example.appcongvien

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotificationService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        (applicationContext as App).pushTokenRepository.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.data["title"]
            ?: message.notification?.title
            ?: "Thong bao moi"
        val body = message.data["message"]
            ?: message.notification?.body
            ?: return

        PushNotificationHelper.createNotificationChannel(this)
        PushNotificationHelper.showNotification(
            context = this,
            title = title,
            message = body,
            data = message.data
        )
    }
}
