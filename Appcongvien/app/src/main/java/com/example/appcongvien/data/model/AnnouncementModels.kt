package com.example.appcongvien.data.model

data class AnnouncementDTO(
    val announcementId: String,
    val title: String,
    val description: String?,
    val imageUrl: String,
    val linkType: String?,  // GAME | VOUCHER | SCREEN | null
    val linkValue: String?, // gameId | voucherCode | screenName
    val isActive: Boolean,
    val sortOrder: Int,
    val createdAt: String?,
    val updatedAt: String?
)
