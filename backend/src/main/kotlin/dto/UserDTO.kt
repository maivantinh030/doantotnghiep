package com.park.dto

import com.park.entities.User
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * DTO để trả về thông tin user cho client
 */
@Serializable
data class UserDTO(
    val userId: String,
    val accountId: String,
    val phoneNumber: String,
    val fullName: String?,
    val email: String?,
    val dateOfBirth: String?, // ISO format: YYYY-MM-DD
    val gender: String?,
    val role: String,
    val membershipLevel: String,
    val currentBalance: String, // BigDecimal as String
    val loyaltyPoints: Int,
    val avatarUrl: String?,
    val referralCode: String?,
    val isCardLocked: Boolean
) {
    companion object {
        /**
         * Convert từ Entity + Account info sang DTO
         */
        fun fromEntity(user: User, phoneNumber: String, role: String): UserDTO {
            return UserDTO(
                userId = user.userId,
                accountId = user.accountId,
                phoneNumber = phoneNumber,
                fullName = user.fullName,
                email = user.email,
                dateOfBirth = user.dateOfBirth?.toString(),
                gender = user.gender,
                role = role,
                membershipLevel = user.membershipLevel,
                currentBalance = user.currentBalance.toString(),
                loyaltyPoints = user.loyaltyPoints,
                avatarUrl = user.avatarUrl,
                referralCode = user.referralCode,
                isCardLocked = user.isCardLocked
            )
        }
    }
}

/**
 * DTO để tạo user mới
 */
data class CreateUserDTO(
    val accountId: String,
    val fullName: String?,
    val email: String?,
    val dateOfBirth: String?, // ISO format: YYYY-MM-DD
    val gender: String?,
    val referralCode: String?
)
