package com.park.dto

import com.park.entities.Card
import kotlinx.serialization.Serializable

@Serializable
data class CardDTO(
    val cardId: String,
    val userId: String?,
    val cardName: String?,
    val status: String,
    val depositAmount: String,
    val depositStatus: String,
    val issuedAt: String?,
    val blockedAt: String?,
    val blockedReason: String?,
    val lastUsedAt: String?,
    val createdAt: String
) {
    companion object {
        fun fromEntity(card: Card): CardDTO {
            return CardDTO(
                cardId = card.cardId,
                userId = card.userId,
                cardName = card.cardName,
                status = card.status,
                depositAmount = card.depositAmount.toString(),
                depositStatus = card.depositStatus,
                issuedAt = card.issuedAt?.toString(),
                blockedAt = card.blockedAt?.toString(),
                blockedReason = card.blockedReason,
                lastUsedAt = card.lastUsedAt?.toString(),
                createdAt = card.createdAt.toString()
            )
        }
    }
}

// Nhân viên đăng ký thêm thẻ vào hệ thống (tạo bản ghi thẻ trắng)
@Serializable
data class RegisterCardRequest(
    val cardId: String,
    val cardName: String? = null
)

// Nhân viên phát hành thẻ cho khách (liên kết thẻ với tài khoản, thu tiền cọc)
@Serializable
data class IssueCardRequest(
    val cardId: String,
    val userId: String,
    val depositAmount: String,
    val cardName: String? = null
)

// Nhân viên xử lý trả thẻ
@Serializable
data class ReturnCardRequest(
    val cardId: String
)

// Khóa thẻ khi mất (không cần thẻ vật lý)
@Serializable
data class BlockCardRequest(
    val reason: String? = null
)

// Terminal gửi yêu cầu chơi game bằng thẻ
@Serializable
data class CardPlayRequest(
    val cardId: String,
    val gameId: String,
    val terminalId: String? = null,
    val signature: String,      // base64 RSA signature
    val challenge: String       // base64 challenge đã ký
)

// Kết quả tra cứu thẻ theo UID
@Serializable
data class CardLookupRequest(
    val cardId: String
)

// Nhân viên cấp thẻ trực tiếp: ghi thẻ → lưu user + card + RSA key
@Serializable
data class DirectIssueRequest(
    val customerID: String,     // userId trong DB (vd: "KH260324143022")
    val cardID: String,         // cardId trong DB (vd: "CARD260324143022")
    val fullName: String,
    val dateOfBirth: String? = null,
    val phoneNumber: String,
    val publicKey: String       // PEM public key từ thẻ
)
