package com.park.repositories

import com.park.database.tables.Announcements
import com.park.entities.Announcement
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.UUID

interface IAnnouncementRepository {
    fun findAllActive(): List<Announcement>
    fun findAll(): List<Announcement>
    fun findById(id: String): Announcement?
    fun create(request: com.park.dto.CreateAnnouncementRequest): Announcement
    fun update(id: String, request: com.park.dto.UpdateAnnouncementRequest): Boolean
    fun delete(id: String): Boolean
}

class AnnouncementRepository : IAnnouncementRepository {

    override fun findAllActive(): List<Announcement> = transaction {
        Announcements.selectAll()
            .where { Announcements.isActive eq true }
            .orderBy(Announcements.sortOrder, SortOrder.ASC)
            .map { mapRow(it) }
    }

    override fun findAll(): List<Announcement> = transaction {
        Announcements.selectAll()
            .orderBy(Announcements.sortOrder, SortOrder.ASC)
            .map { mapRow(it) }
    }

    override fun findById(id: String): Announcement? = transaction {
        Announcements.selectAll()
            .where { Announcements.announcementId eq id }
            .singleOrNull()?.let { mapRow(it) }
    }

    override fun create(request: com.park.dto.CreateAnnouncementRequest): Announcement {
        val now = Instant.now()
        val id = UUID.randomUUID().toString()
        return transaction {
            Announcements.insert {
                it[announcementId] = id
                it[title] = request.title
                it[description] = request.description
                it[imageUrl] = request.imageUrl
                it[linkType] = request.linkType
                it[linkValue] = request.linkValue
                it[isActive] = request.isActive
                it[sortOrder] = request.sortOrder
                it[createdAt] = now
                it[updatedAt] = now
            }
            Announcement(
                announcementId = id,
                title = request.title,
                description = request.description,
                imageUrl = request.imageUrl,
                linkType = request.linkType,
                linkValue = request.linkValue,
                isActive = request.isActive,
                sortOrder = request.sortOrder,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    override fun update(id: String, request: com.park.dto.UpdateAnnouncementRequest): Boolean = transaction {
        Announcements.update({ Announcements.announcementId eq id }) {
            request.title?.let { v -> it[title] = v }
            request.description?.let { v -> it[description] = v }
            request.imageUrl?.let { v -> it[imageUrl] = v }
            request.linkType?.let { v -> it[linkType] = v }
            request.linkValue?.let { v -> it[linkValue] = v }
            request.isActive?.let { v -> it[isActive] = v }
            request.sortOrder?.let { v -> it[sortOrder] = v }
            it[updatedAt] = Instant.now()
        } > 0
    }

    override fun delete(id: String): Boolean = transaction {
        Announcements.deleteWhere { announcementId eq id } > 0
    }

    private fun mapRow(row: ResultRow) = Announcement(
        announcementId = row[Announcements.announcementId],
        title = row[Announcements.title],
        description = row[Announcements.description],
        imageUrl = row[Announcements.imageUrl],
        linkType = row[Announcements.linkType],
        linkValue = row[Announcements.linkValue],
        isActive = row[Announcements.isActive],
        sortOrder = row[Announcements.sortOrder],
        createdAt = row[Announcements.createdAt],
        updatedAt = row[Announcements.updatedAt]
    )
}
