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

@Serializable
data class SupportChatHistoryResponse(
    val items: List<SupportMessageDTO>,
    val total: Long,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val unreadCount: Long
)

/** Tin nhắn gửi qua WebSocket */
@Serializable
data class WsSupportMessage(
    val messageId: String,
    val userId: String,
    val content: String,
    val senderType: String,
    val userName: String? = null,
    val createdAt: String
)

@Serializable
data class SupportHistoryApiResponse(
    val success: Boolean = true,
    val message: String = "",
    val data: SupportChatHistoryResponse? = null
)
