package com.example.appcongvien.data.model

data class RegisterPushTokenRequest(
    val token: String,
    val platform: String = "ANDROID",
    val deviceId: String? = null
)

data class UnregisterPushTokenRequest(
    val token: String
)

data class PushTokenDTO(
    val tokenId: String,
    val userId: String,
    val token: String,
    val platform: String,
    val deviceId: String?,
    val isActive: Boolean,
    val updatedAt: String
)
