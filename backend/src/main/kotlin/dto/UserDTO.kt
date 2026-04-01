package com.park.dto

import com.park.entities.User
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val userId: String,
    val accountId: String?,
    val phoneNumber: String,
    val fullName: String?,
    val email: String?,
    val dateOfBirth: String?,
    val gender: String?,
    val role: String,
    val currentBalance: String,
    val avatarUrl: String?
) {
    companion object {
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
                currentBalance = user.currentBalance.toString(),
                avatarUrl = user.avatarUrl
            )
        }
    }
}

data class CreateUserDTO(
    val accountId: String,
    val fullName: String?,
    val email: String?,
    val dateOfBirth: String?,
    val gender: String?
)
