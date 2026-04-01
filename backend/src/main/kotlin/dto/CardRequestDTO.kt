package com.park.dto

import com.park.entities.CardRequest
import kotlinx.serialization.Serializable

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
    val updatedAt: String
) {
    companion object {
        fun fromEntity(req: CardRequest): CardRequestDTO {
            return CardRequestDTO(
                requestId = req.requestId,
                userId = req.userId,
                status = req.status,
                depositPaidOnline = req.depositPaidOnline,
                depositAmount = req.depositAmount.toString(),
                note = req.note,
                approvedBy = req.approvedBy,
                createdAt = req.createdAt.toString(),
                updatedAt = req.updatedAt.toString()
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
