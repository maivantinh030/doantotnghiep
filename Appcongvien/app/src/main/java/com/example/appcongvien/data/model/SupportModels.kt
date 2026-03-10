package com.example.appcongvien.data.model

data class SupportMessageDTO(
    val messageId: String,
    val userId: String = "",
    val content: String,
    val senderType: String,
    val createdAt: String
) {
    val isFromAdmin: Boolean get() = senderType == "ADMIN"
}

data class SendMessageRequest(
    val content: String
)
