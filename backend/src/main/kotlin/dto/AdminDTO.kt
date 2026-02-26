package com.park.dto

import com.park.entities.Admin
import kotlinx.serialization.Serializable

@Serializable
data class AdminLoginRequest(
    val phoneNumber: String,
    val password: String
)

@Serializable
data class CreateAdminRequest(
    val phoneNumber: String,
    val password: String,
    val fullName: String,
    val employeeCode: String? = null
)

@Serializable
data class AdminInfo(
    val adminId: String,
    val accountId: String,
    val phoneNumber: String,       // phoneNumber dùng làm username, khớp với desktop AdminProfile.username
    val fullName: String,
    val employeeCode: String?,
    val role: String = "ADMIN"
) {
    companion object {
        fun fromEntity(admin: Admin, phoneNumber: String): AdminInfo {
            return AdminInfo(
                adminId = admin.adminId,
                accountId = admin.accountId,
                phoneNumber = phoneNumber,
                fullName = admin.fullName,
                employeeCode = admin.employeeCode
            )
        }
    }
}

@Serializable
data class AdminAuthResponse(
    val success: Boolean,
    val message: String,
    val data: AdminAuthData? = null
)

@Serializable
data class AdminAuthData(
    val token: String,
    val admin: AdminInfo         // đổi từ "admin" → "user" để khớp với desktop AuthData.user
)

@Serializable
data class AdminUserDTO(
    val userId: String,
    val accountId: String,
    val phoneNumber: String,
    val fullName: String?,
    val email: String?,
    val membershipLevel: String,
    val currentBalance: String,
    val loyaltyPoints: Int,
    val accountStatus: String,
    val isCardLocked: Boolean,
    val createdAt: String
)

@Serializable
data class AdjustBalanceRequest(
    val amount: Double,
    val description: String? = null
)

@Serializable
data class UpdateMembershipRequest(
    val membershipLevel: String // BRONZE, SILVER, GOLD, PLATINUM
)

@Serializable
data class SendNotificationRequest(
    val title: String,
    val message: String,
    val targetType: String = "ALL", // ALL, GOLD, SILVER, BRONZE, PLATINUM, etc.
    val targetUserId: String? = null // specific user if targetType = "USER"
)

@Serializable
data class AdminReplyRequest(
    val userId: String,
    val content: String
)

@Serializable
data class AdminTransactionDTO(
    val transactionId: String,
    val userId: String,
    val amount: String,
    val balanceBefore: String,
    val balanceAfter: String,
    val type: String,
    val referenceType: String?,
    val referenceId: String?,
    val description: String?,
    val createdAt: String,
    val createdBy: String?
)

@Serializable
data class AdminOrderDTO(
    val orderId: String,
    val userId: String,
    val userName: String?,
    val subtotal: String,
    val discountAmount: String,
    val finalAmount: String,
    val paymentMethod: String?,
    val status: String,
    val createdAt: String
)

@Serializable
data class AdminSentNotificationDTO(
    val notificationId: String,
    val title: String,
    val message: String,
    val targetType: String?,
    val createdAt: String
)

@Serializable
data class AdminSupportMessageDTO(
    val messageId: String,
    val userId: String,
    val userName: String? = null,
    val content: String,
    val isFromAdmin: Boolean,
    val createdAt: String? = null
)
