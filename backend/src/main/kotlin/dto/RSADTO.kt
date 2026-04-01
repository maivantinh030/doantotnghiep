package com.park.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterKeyRequest(
    val cardId: String,
    val publicKey: String
)

@Serializable
data class ChallengeResponse(
    val challenge: String,
    val expiresAt: Long
)

@Serializable
data class RSAVerifyRequest(
    val cardId: String,
    val challenge: String,
    val signature: String
)

@Serializable
data class RSAVerifyResponse(
    val success: Boolean,
    val message: String
)

