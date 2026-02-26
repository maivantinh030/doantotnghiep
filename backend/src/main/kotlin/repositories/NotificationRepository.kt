package com.park.repositories

import com.park.database.tables.Notifications
import com.park.entities.Notification
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

interface INotificationRepository {
    fun create(notification: Notification): Notification
    fun findByUserId(userId: String, limit: Int, offset: Long): List<Notification>
    fun findByType(type: String, limit: Int, offset: Long): List<Notification>
    fun countByUserId(userId: String): Long
    fun countByType(type: String): Long
    fun countUnreadByUserId(userId: String): Long
    fun markAsRead(notificationId: String): Boolean
    fun markAllAsRead(userId: String): Boolean
    fun delete(notificationId: String): Boolean
}

class NotificationRepository : INotificationRepository {

    override fun create(notification: Notification): Notification {
        return transaction {
            Notifications.insert {
                it[notificationId] = notification.notificationId
                it[userId] = notification.userId
                it[type] = notification.type
                it[title] = notification.title
                it[message] = notification.message
                it[data] = notification.data
                it[isRead] = notification.isRead
                it[createdAt] = notification.createdAt
            }
            notification
        }
    }

    override fun findByUserId(userId: String, limit: Int, offset: Long): List<Notification> {
        return transaction {
            Notifications.selectAll().where { Notifications.userId eq userId }
                .orderBy(Notifications.createdAt, SortOrder.DESC)
                .limit(limit).offset(offset)
                .map { mapRow(it) }
        }
    }

    override fun findByType(type: String, limit: Int, offset: Long): List<Notification> {
        return transaction {
            Notifications.selectAll().where { Notifications.type eq type }
                .orderBy(Notifications.createdAt, SortOrder.DESC)
                .limit(limit).offset(offset)
                .map { mapRow(it) }
        }
    }

    override fun countByUserId(userId: String): Long {
        return transaction {
            Notifications.selectAll().where { Notifications.userId eq userId }.count()
        }
    }

    override fun countByType(type: String): Long {
        return transaction {
            Notifications.selectAll().where { Notifications.type eq type }.count()
        }
    }

    override fun countUnreadByUserId(userId: String): Long {
        return transaction {
            Notifications.selectAll().where {
                (Notifications.userId eq userId) and (Notifications.isRead eq false)
            }.count()
        }
    }

    override fun markAsRead(notificationId: String): Boolean {
        return transaction {
            Notifications.update(where = { Notifications.notificationId eq notificationId }) {
                it[isRead] = true
                it[readAt] = Instant.now()
            } > 0
        }
    }

    override fun markAllAsRead(userId: String): Boolean {
        return transaction {
            Notifications.update(where = {
                (Notifications.userId eq userId) and (Notifications.isRead eq false)
            }) {
                it[isRead] = true
                it[readAt] = Instant.now()
            } > 0
        }
    }

    override fun delete(notificationId: String): Boolean {
        return transaction {
            Notifications.deleteWhere { Notifications.notificationId eq notificationId } > 0
        }
    }

    private fun mapRow(row: ResultRow): Notification {
        return Notification(
            notificationId = row[Notifications.notificationId],
            userId = row[Notifications.userId],
            type = row[Notifications.type],
            title = row[Notifications.title],
            message = row[Notifications.message],
            data = row[Notifications.data],
            isRead = row[Notifications.isRead],
            readAt = row[Notifications.readAt],
            createdAt = row[Notifications.createdAt]
        )
    }
}
