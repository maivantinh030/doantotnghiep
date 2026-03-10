package org.example.project.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminLoginRequest(
    // Backend dùng phoneNumber cho cả user & admin (xem backend/API_TESTING.http)
    val phoneNumber: String,
    val password: String
)

@Serializable
data class AdminInfo(
    @SerialName("adminId") val adminId: String,
    @SerialName("username") val username: String,
    @SerialName("fullName") val fullName: String,
    @SerialName("role") val role: String
)

@Serializable
data class AdminLoginResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("token") val token: String? = null,
    @SerialName("adminInfo") val adminInfo: AdminInfo? = null,
    @SerialName("message") val message: String? = null
)

// Các DTO phản ánh đúng cấu trúc JSON backend /api/admin/auth/login trả về
@Serializable
data class BackendAdminInfo(
    @SerialName("adminId") val adminId: String,
    @SerialName("accountId") val accountId: String,
    @SerialName("phoneNumber") val phoneNumber: String,
    @SerialName("fullName") val fullName: String,
    @SerialName("employeeCode") val employeeCode: String?,
    @SerialName("role") val role: String
)

@Serializable
data class BackendAdminAuthData(
    @SerialName("token") val token: String,
    @SerialName("admin") val admin: BackendAdminInfo
)

@Serializable
data class BackendAdminAuthResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String,
    @SerialName("data") val data: BackendAdminAuthData?
)
