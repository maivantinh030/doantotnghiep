package com.park.repositories

import com.park.database.tables.SupportMessages
import com.park.entities.SupportMessage
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

interface ISupportRepository {
    fun create(message: SupportMessage): SupportMessage
    fun findByUserId(userId: String, limit: Int, offset: Long): List<SupportMessage>
    fun findAll(limit: Int, offset: Long): List<SupportMessage>
    fun countByUserId(userId: String): Long
    fun countAll(): Long
    fun countUnreadByUserId(userId: String): Long
    fun markAsRead(messageId: String): Boolean
    fun markAllAsReadForUser(userId: String): Boolean
}

class SupportRepository : ISupportRepository {

    override fun create(message: SupportMessage): SupportMessage {
        return transaction {
            SupportMessages.insert {
                it[messageId] = message.messageId
                it[userId] = message.userId
                it[senderId] = message.senderId
                it[senderType] = message.senderType
                it[content] = message.content
                it[isRead] = message.isRead
                it[createdAt] = message.createdAt
            }
            message
        }
    }

    override fun findByUserId(userId: String, limit: Int, offset: Long): List<SupportMessage> {
        return transaction {
            SupportMessages.selectAll().where { SupportMessages.userId eq userId }
                .orderBy(SupportMessages.createdAt, SortOrder.ASC)
                .limit(limit).offset(offset)
                .map { mapRow(it) }
        }
    }

    override fun findAll(limit: Int, offset: Long): List<SupportMessage> {
        return transaction {
            SupportMessages.selectAll()
                .orderBy(SupportMessages.createdAt, SortOrder.ASC)
                .limit(limit).offset(offset)
                .map { mapRow(it) }
        }
    }

    override fun countByUserId(userId: String): Long {
        return transaction {
            SupportMessages.selectAll().where { SupportMessages.userId eq userId }.count()
        }
    }

    override fun countAll(): Long {
        return transaction { SupportMessages.selectAll().count() }
    }

    override fun countUnreadByUserId(userId: String): Long {
        return transaction {
            SupportMessages.selectAll().where {
                (SupportMessages.userId eq userId) and
                        (SupportMessages.senderType eq "ADMIN") and
                        (SupportMessages.isRead eq false)
            }.count()
        }
    }

    override fun markAsRead(messageId: String): Boolean {
        return transaction {
            SupportMessages.update(where = { SupportMessages.messageId eq messageId }) {
                it[isRead] = true
            } > 0
        }
    }

    override fun markAllAsReadForUser(userId: String): Boolean {
        return transaction {
            SupportMessages.update(where = {
                (SupportMessages.userId eq userId) and
                        (SupportMessages.senderType eq "ADMIN") and
                        (SupportMessages.isRead eq false)
            }) {
                it[isRead] = true
            } > 0
        }
    }

    private fun mapRow(row: ResultRow): SupportMessage {
        return SupportMessage(
            messageId = row[SupportMessages.messageId],
            userId = row[SupportMessages.userId],
            senderId = row[SupportMessages.senderId],
            senderType = row[SupportMessages.senderType],
            content = row[SupportMessages.content],
            isRead = row[SupportMessages.isRead],
            createdAt = row[SupportMessages.createdAt]
        )
    }
}
