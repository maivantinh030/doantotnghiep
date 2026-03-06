package com.example.appcongvien.data.model

// ===== Responses =====
data class CardDTO(
    val cardId: String,
    val physicalCardUid: String? = null,
    val virtualCardUid: String? = null,
    val cardType: String = "PHYSICAL", // "PHYSICAL" | "VIRTUAL" | "BOTH"
    val userId: String?,
    val cardName: String?,
    val status: String,                // "ACTIVE" | "BLOCKED" | "INACTIVE"
    val issuedAt: String?,
    val blockedAt: String?,
    val blockedReason: String?,
    val lastUsedAt: String?,
    val createdAt: String
)

// ===== Requests =====
data class LinkCardRequest(
    val physicalCardUid: String,
    val cardName: String? = null,
    val pin: String? = null
)

data class UpdateCardRequest(
    val cardName: String? = null
)

data class BlockCardRequest(
    val reason: String? = null
)
