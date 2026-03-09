package org.example.project.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChallengeResponse(
    @Json(name = "challenge") val challenge: String,
    @Json(name = "expiresAt") val expiresAt: Long
)

@JsonClass(generateAdapter = true)
data class RSAVerifyRequest(
    @Json(name = "customerId") val customerId: String,
    @Json(name = "challenge") val challenge: String,
    @Json(name = "signature") val signature: String
)

@JsonClass(generateAdapter = true)
data class RSAVerifyResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class RegisterKeyRequest(
    @Json(name = "customerId") val customerId: String,
    @Json(name = "publicKey") val publicKey: String
)
