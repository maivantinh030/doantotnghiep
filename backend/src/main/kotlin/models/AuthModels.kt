package com.park.models

import kotlinx.serialization.Serializable

/**
 * Request body cho đăng ký tài khoản mới
 */
@Serializable
data class RegisterRequest(
    val phoneNumber: String,
    val password: String,
    val fullName: String? = null,
    val email: String? = null,
    val dateOfBirth: String? = null, // Format: YYYY-MM-DD
    val gender: String? = null, // MALE, FEMALE, OTHER
    val referralCode: String? = null // Mã giới thiệu từ người khác
)

/**
 * Request body cho đăng nhập
 */
@Serializable
data class LoginRequest(
    val phoneNumber: String,
    val password: String
)

/**
 * Response trả về sau khi đăng nhập/đăng ký thành công
 */
@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: AuthData? = null
)

/**
 * Dữ liệu người dùng sau khi đăng nhập
 */
@Serializable
data class AuthData(
    val token: String,
    val user: UserInfo
)

/**
 * Thông tin cơ bản của user
 */
@Serializable
data class UserInfo(
    val userId: String,
    val accountId: String,
    val phoneNumber: String,
    val fullName: String?,
    val email: String?,
    val role: String,
    val membershipLevel: String,
    val currentBalance: String, // Decimal as String for precision
    val loyaltyPoints: Int,
    val avatarUrl: String?
)

/**
 * Response lỗi chuẩn
 */
@Serializable
data class ErrorResponse(
    val success: Boolean = false,
    val message: String,
    val errors: Map<String, String>? = null
)
