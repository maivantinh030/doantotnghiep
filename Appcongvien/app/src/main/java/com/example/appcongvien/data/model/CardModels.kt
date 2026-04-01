package com.example.appcongvien.data.model

// ===== Card Responses =====
data class CardDTO(
    val cardId: String,
    val userId: String? = null,
    val cardName: String? = null,
    val status: String,             // "AVAILABLE" | "ACTIVE" | "BLOCKED"
    val depositAmount: String? = null,
    val depositStatus: String? = null, // "NONE" | "PAID" | "REFUNDED" | "FORFEITED"
    val issuedAt: String? = null,
    val blockedAt: String? = null,
    val blockedReason: String? = null,
    val lastUsedAt: String? = null,
    val createdAt: String
)

// ===== Card Request =====
data class CardRequestDTO(
    val requestId: String,
    val userId: String,
    val status: String,             // "PENDING" | "APPROVED" | "REJECTED" | "COMPLETED"
    val depositPaidOnline: Boolean = false,
    val depositAmount: String? = null,
    val note: String? = null,
    val reviewNote: String? = null,
    val createdAt: String,
    val reviewedAt: String? = null
)

// ===== Requests =====
data class CreateCardRequestRequest(
    val depositPaidOnline: Boolean = false,
    val depositAmount: String = "0",
    val note: String? = null
)

data class UpdateCardRequest(
    val cardName: String? = null
)

data class BlockCardRequest(
    val reason: String? = null
)
