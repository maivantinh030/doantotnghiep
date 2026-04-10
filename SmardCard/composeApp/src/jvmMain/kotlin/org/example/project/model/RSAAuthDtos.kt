package org.example.project.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChallengeResponse(
    @SerialName("challenge") val challenge: String,
    @SerialName("expiresAt") val expiresAt: Long
)

@Serializable
data class RSAVerifyRequest(
    @SerialName("cardId") val cardId: String,
    @SerialName("challenge") val challenge: String,
    @SerialName("signature") val signature: String
)

@Serializable
data class RSAVerifyResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String
)

@Serializable
data class RegisterKeyRequest(
    @SerialName("cardId") val cardId: String,
    @SerialName("publicKey") val publicKey: String
)
