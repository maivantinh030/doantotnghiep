package com.park.dto

import com.park.entities.Card
import kotlinx.serialization.Serializable

@Serializable
data class CardDTO(
    val cardId: String,
    val physicalCardUid: String,
    val userId: String?,
    val cardName: String?,
    val status: String,
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
                physicalCardUid = card.physicalCardUid,
                userId = card.userId,
                cardName = card.cardName,
                status = card.status,
                issuedAt = card.issuedAt?.toString(),
                blockedAt = card.blockedAt?.toString(),
                blockedReason = card.blockedReason,
                lastUsedAt = card.lastUsedAt?.toString(),
                createdAt = card.createdAt.toString()
            )
        }
    }
}

@Serializable
data class LinkCardRequest(
    val physicalCardUid: String,
    val cardName: String? = null,
    val pin: String? = null
)

@Serializable
data class UpdateCardRequest(
    val cardName: String? = null,
    val pin: String? = null
)

@Serializable
data class BlockCardRequest(
    val reason: String? = null
)
