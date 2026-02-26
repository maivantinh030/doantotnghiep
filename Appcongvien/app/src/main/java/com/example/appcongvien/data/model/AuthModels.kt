package com.example.appcongvien.data.model

// ===== Requests =====
data class RegisterRequest(
    val phoneNumber: String,
    val password: String,
    val fullName: String,
    val email: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val referralCode: String? = null
)

data class LoginRequest(
    val phoneNumber: String,
    val password: String
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class ForgotPasswordRequest(
    val phoneNumber: String
)

// ===== Responses =====
data class AuthData(
    val token: String,
    val user: UserDTO
)

data class UserDTO(
    val userId: String,
    val phoneNumber: String,
    val fullName: String,
    val email: String?,
    val dateOfBirth: String?,
    val gender: String?,
    val avatarUrl: String?,
    val currentBalance: String,
    val memberLevel: String?,
    val referralCode: String?,
    val role: String
)
