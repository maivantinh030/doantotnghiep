package com.park.dto

import com.park.entities.Announcement
import kotlinx.serialization.Serializable

@Serializable
data class AnnouncementDTO(
    val announcementId: String,
    val title: String,
    val description: String?,
    val imageUrl: String,
    val linkType: String?,
    val linkValue: String?,
    val isActive: Boolean,
    val sortOrder: Int,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun fromEntity(a: Announcement) = AnnouncementDTO(
            announcementId = a.announcementId,
            title = a.title,
            description = a.description,
            imageUrl = a.imageUrl,
            linkType = a.linkType,
            linkValue = a.linkValue,
            isActive = a.isActive,
            sortOrder = a.sortOrder,
            createdAt = a.createdAt.toString(),
            updatedAt = a.updatedAt.toString()
        )
    }
}

@Serializable
data class CreateAnnouncementRequest(
    val title: String,
    val description: String? = null,
    val imageUrl: String,
    val linkType: String? = null,
    val linkValue: String? = null,
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)

@Serializable
data class UpdateAnnouncementRequest(
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val linkType: String? = null,
    val linkValue: String? = null,
    val isActive: Boolean? = null,
    val sortOrder: Int? = null
)
