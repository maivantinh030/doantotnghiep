package com.park.services

import com.park.dto.AnnouncementDTO
import com.park.dto.CreateAnnouncementRequest
import com.park.dto.UpdateAnnouncementRequest
import com.park.repositories.AnnouncementRepository
import com.park.repositories.IAnnouncementRepository

class AnnouncementService(
    private val repository: IAnnouncementRepository = AnnouncementRepository()
) {
    /** Chỉ giữ phần path /uploads/... dù client gửi full URL hay path */
    private fun normalizeImageUrl(url: String): String {
        val idx = url.indexOf("/uploads/")
        return if (idx > 0) url.substring(idx) else url
    }

    fun getActiveAnnouncements(): List<AnnouncementDTO> =
        repository.findAllActive().map { AnnouncementDTO.fromEntity(it) }

    fun getAllAnnouncements(): List<AnnouncementDTO> =
        repository.findAll().map { AnnouncementDTO.fromEntity(it) }

    fun createAnnouncement(request: CreateAnnouncementRequest): Result<AnnouncementDTO> = runCatching {
        AnnouncementDTO.fromEntity(repository.create(request.copy(imageUrl = normalizeImageUrl(request.imageUrl))))
    }

    fun updateAnnouncement(id: String, request: UpdateAnnouncementRequest): Result<Boolean> = runCatching {
        repository.findById(id) ?: error("Announcement not found")
        repository.update(id, request.copy(imageUrl = request.imageUrl?.let { normalizeImageUrl(it) }))
    }

    fun deleteAnnouncement(id: String): Result<Boolean> = runCatching {
        repository.findById(id) ?: error("Announcement not found")
        repository.delete(id)
    }
}
