package com.park.services

import com.park.dto.AnnouncementDTO
import com.park.dto.CreateAnnouncementRequest
import com.park.dto.UpdateAnnouncementRequest
import com.park.repositories.AnnouncementRepository
import com.park.repositories.IAnnouncementRepository

class AnnouncementService(
    private val repository: IAnnouncementRepository = AnnouncementRepository()
) {
    fun getActiveAnnouncements(): List<AnnouncementDTO> =
        repository.findAllActive().map { AnnouncementDTO.fromEntity(it) }

    fun getAllAnnouncements(): List<AnnouncementDTO> =
        repository.findAll().map { AnnouncementDTO.fromEntity(it) }

    fun createAnnouncement(request: CreateAnnouncementRequest): Result<AnnouncementDTO> = runCatching {
        AnnouncementDTO.fromEntity(repository.create(request))
    }

    fun updateAnnouncement(id: String, request: UpdateAnnouncementRequest): Result<Boolean> = runCatching {
        repository.findById(id) ?: error("Announcement not found")
        repository.update(id, request)
    }

    fun deleteAnnouncement(id: String): Result<Boolean> = runCatching {
        repository.findById(id) ?: error("Announcement not found")
        repository.delete(id)
    }
}
