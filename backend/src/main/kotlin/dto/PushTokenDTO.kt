package com.park.dto

import com.park.entities.UserPushToken
import kotlinx.serialization.Serializable

@Serializable
data class RegisterPushTokenRequest(
    val token: String,
    val platform: String = "ANDROID",
    val deviceId: String? = null
)

@Serializable
data class UnregisterPushTokenRequest(
    val token: String
)

@Serializable
data class PushTokenDTO(
    val tokenId: String,
    val userId: String,
    val token: String,
    val platform: String,
    val deviceId: String?,
    val isActive: Boolean,
    val updatedAt: String
) {
    companion object {
        fun fromEntity(entity: UserPushToken): PushTokenDTO {
            return PushTokenDTO(
                tokenId = entity.tokenId,
                userId = entity.userId,
                token = entity.fcmToken,
                platform = entity.platform,
                deviceId = entity.deviceId,
                isActive = entity.isActive,
                updatedAt = entity.updatedAt.toString()
            )
        }
    }
}
