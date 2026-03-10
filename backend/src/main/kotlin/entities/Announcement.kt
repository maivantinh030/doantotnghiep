package com.park.entities

import java.time.Instant

data class Announcement(
    val announcementId: String,
    val title: String,
    val description: String?,
    val imageUrl: String,
    val linkType: String?,
    val linkValue: String?,
    val isActive: Boolean,
    val sortOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant
)
