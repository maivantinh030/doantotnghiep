package com.park.dto

import com.park.entities.CardRequest
import com.park.entities.User
import kotlinx.serialization.Serializable

@Serializable
data class CardRequestUserInfoDTO(
    val userId: String,
    val phoneNumber: String,
    val fullName: String?,
    val email: String?,
    val dateOfBirth: String?,
    val currentBalance: String,
    val avatarUrl: String?
) {
    companion object {
        fun fromUser(user: User, phoneNumber: String): CardRequestUserInfoDTO {
            return CardRequestUserInfoDTO(
                userId = user.userId,
                phoneNumber = phoneNumber,
                fullName = user.fullName,
                email = user.email,
                dateOfBirth = user.dateOfBirth?.toString(),
                currentBalance = user.currentBalance.toString(),
                avatarUrl = user.avatarUrl
            )
        }
    }
}

@Serializable
data class CardRequestDTO(
    val requestId: String,
    val userId: String,
    val status: String,
    val depositPaidOnline: Boolean,
    val depositAmount: String,
    val note: String?,
    val approvedBy: String?,
    val createdAt: String,
    val updatedAt: String,
    val requester: CardRequestUserInfoDTO? = null
) {
    companion object {
        fun fromEntity(
            req: CardRequest,
            requester: CardRequestUserInfoDTO? = null,
            statusOverride: String? = null
        ): CardRequestDTO {
            return CardRequestDTO(
                requestId = req.requestId,
                userId = req.userId,
                status = statusOverride ?: req.status,
                depositPaidOnline = req.depositPaidOnline,
                depositAmount = req.depositAmount.toString(),
                note = req.note,
                approvedBy = req.approvedBy,
                createdAt = req.createdAt.toString(),
                updatedAt = req.updatedAt.toString(),
                requester = requester
            )
        }
    }
}

// Người dùng gửi yêu cầu cấp thẻ qua app
@Serializable
data class CreateCardRequestDTO(
    val depositPaidOnline: Boolean = false,
    val depositAmount: String,
    val note: String? = null
)

// Nhân viên duyệt yêu cầu
@Serializable
data class ApproveCardRequestDTO(
    val approved: Boolean,
    val note: String? = null
)

@Serializable
data class IssueCardFromRequestDTO(
    val cardId: String,
    val publicKey: String
)
