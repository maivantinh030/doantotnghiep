package com.park.services

import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MessagingErrorCode
import com.park.plugins.FirebaseAdminState
import com.park.repositories.IUserPushTokenRepository
import com.park.repositories.UserPushTokenRepository

class FirebasePushService(
    private val userPushTokenRepository: IUserPushTokenRepository = UserPushTokenRepository()
) {

    fun sendNotificationToUser(
        userId: String,
        title: String,
        message: String,
        type: String? = null,
        notificationId: String? = null,
        rawData: String? = null
    ) {
        if (!FirebaseAdminState.isInitialized()) {
            return
        }

        val pushTokens = userPushTokenRepository.findActiveByUserId(userId)
        if (pushTokens.isEmpty()) {
            return
        }

        val invalidTokens = mutableListOf<String>()

        pushTokens.forEach { pushToken ->
            val payload = buildMap {
                put("title", title)
                put("message", message)
                put("screen", "notifications")
                type?.takeIf { it.isNotBlank() }?.let { put("type", it) }
                notificationId?.takeIf { it.isNotBlank() }?.let { put("notificationId", it) }
                rawData?.takeIf { it.isNotBlank() }?.let { put("data", it) }
            }

            val request = Message.builder()
                .setToken(pushToken.fcmToken)
                .putAllData(payload)
                .setAndroidConfig(
                    AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build()
                )
                .build()

            try {
                FirebaseMessaging.getInstance().send(request)
            } catch (e: FirebaseMessagingException) {
                if (e.messagingErrorCode == MessagingErrorCode.UNREGISTERED ||
                    e.messagingErrorCode == MessagingErrorCode.INVALID_ARGUMENT
                ) {
                    invalidTokens += pushToken.fcmToken
                }
                println("FCM send failed for user $userId: ${e.message}")
            } catch (e: Exception) {
                println("Unexpected FCM error for user $userId: ${e.message}")
            }
        }

        if (invalidTokens.isNotEmpty()) {
            userPushTokenRepository.deleteByTokens(invalidTokens)
        }
    }
}
