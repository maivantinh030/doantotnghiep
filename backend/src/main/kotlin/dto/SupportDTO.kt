package com.park.dto

import com.park.entities.SupportMessage
import kotlinx.serialization.Serializable

@Serializable
data class SupportMessageDTO(
    val messageId: String,
    val userId: String,
    val senderId: String,
    val senderType: String,
    val content: String,
    val isRead: Boolean,
    val createdAt: String
) {
    companion object {
        fun fromEntity(msg: SupportMessage): SupportMessageDTO {
            return SupportMessageDTO(
                messageId = msg.messageId,
                userId = msg.userId,
                senderId = msg.senderId,
                senderType = msg.senderType,
                content = msg.content,
                isRead = msg.isRead,
                createdAt = msg.createdAt.toString()
            )
        }
    }
}

@Serializable
data class SendMessageRequest(
    val content: String
)
