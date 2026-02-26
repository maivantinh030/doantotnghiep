package com.example.appcongvien.data.model

data class SupportMessageDTO(
    val messageId: String,
    val content: String,
    val senderType: String,
    val createdAt: String
)

data class SendMessageRequest(
    val content: String
)
