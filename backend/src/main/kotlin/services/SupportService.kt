package com.park.services

import com.park.dto.SendMessageRequest
import com.park.dto.SupportChatHistoryResponse
import com.park.dto.SupportMessageDTO
import com.park.entities.SupportMessage
import com.park.repositories.ISupportRepository
import com.park.repositories.SupportRepository
import java.time.Instant
import java.util.*

class SupportService(
    private val supportRepository: ISupportRepository = SupportRepository()
) {

    fun getChatHistory(userId: String, page: Int, size: Int): SupportChatHistoryResponse {
        val offset = ((page - 1) * size).toLong()
        val messages = supportRepository.findByUserId(userId, size, offset)
        val total = supportRepository.countByUserId(userId)
        val unreadCount = supportRepository.countUnreadByUserId(userId)

        // Đánh dấu tin nhắn từ admin đã đọc
        supportRepository.markAllAsReadForUser(userId)

        val totalPages = if (size > 0) ((total + size - 1) / size).toInt() else 1
        return SupportChatHistoryResponse(
            items = messages.map { SupportMessageDTO.fromEntity(it) },
            total = total,
            page = page,
            size = size,
            totalPages = totalPages,
            unreadCount = unreadCount
        )
    }

    fun sendMessage(userId: String, request: SendMessageRequest): Result<SupportMessageDTO> {
        if (request.content.isBlank()) {
            return Result.failure(IllegalArgumentException("Nội dung tin nhắn không được để trống"))
        }

        val message = SupportMessage(
            messageId = UUID.randomUUID().toString(),
            userId = userId,
            senderId = userId,
            senderType = "USER",
            content = request.content,
            isRead = false,
            createdAt = Instant.now()
        )

        val created = supportRepository.create(message)
        return Result.success(SupportMessageDTO.fromEntity(created))
    }

    fun sendAdminMessage(userId: String, adminId: String, content: String): Result<SupportMessageDTO> {
        val message = SupportMessage(
            messageId = UUID.randomUUID().toString(),
            userId = userId,
            senderId = adminId,
            senderType = "ADMIN",
            content = content,
            isRead = false,
            createdAt = Instant.now()
        )

        val created = supportRepository.create(message)
        return Result.success(SupportMessageDTO.fromEntity(created))
    }
}
