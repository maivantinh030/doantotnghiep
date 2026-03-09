package org.example.project.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AdminLoginRequest(
    val username: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class AdminInfo(
    @Json(name = "adminId") val adminId: String,
    @Json(name = "username") val username: String,
    @Json(name = "fullName") val fullName: String,
    @Json(name = "role") val role: String
)

@JsonClass(generateAdapter = true)
data class AdminLoginResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "token") val token: String? = null,
    @Json(name = "adminInfo") val adminInfo: AdminInfo? = null,
    @Json(name = "message") val message: String? = null
)
