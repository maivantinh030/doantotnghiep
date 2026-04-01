package com.park.repositories

import com.park.database.tables.UserPushTokens
import com.park.entities.UserPushToken
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID

interface IUserPushTokenRepository {
    fun upsert(userId: String, token: String, platform: String, deviceId: String? = null): UserPushToken
    fun findByToken(token: String): UserPushToken?
    fun findActiveByUserId(userId: String): List<UserPushToken>
    fun deactivateByToken(token: String): Boolean
    fun deleteByTokens(tokens: Collection<String>): Int
}

class UserPushTokenRepository : IUserPushTokenRepository {

    override fun upsert(userId: String, token: String, platform: String, deviceId: String?): UserPushToken {
        val normalizedToken = token.trim()
        val normalizedPlatform = platform.trim().ifBlank { "ANDROID" }.uppercase()
        val normalizedDeviceId = deviceId?.trim()?.takeIf { it.isNotEmpty() }
        val now = Instant.now()

        return transaction {
            if (normalizedDeviceId != null) {
                UserPushTokens.update(
                    where = {
                        (UserPushTokens.userId eq userId) and
                            (UserPushTokens.deviceId eq normalizedDeviceId) and
                            (UserPushTokens.fcmToken neq normalizedToken)
                    }
                ) {
                    it[isActive] = false
                    it[updatedAt] = now
                }
            }

            val existing = UserPushTokens
                .selectAll()
                .where { UserPushTokens.fcmToken eq normalizedToken }
                .singleOrNull()

            if (existing != null) {
                UserPushTokens.update(where = { UserPushTokens.fcmToken eq normalizedToken }) {
                    it[UserPushTokens.userId] = userId
                    it[UserPushTokens.platform] = normalizedPlatform
                    it[UserPushTokens.deviceId] = normalizedDeviceId
                    it[UserPushTokens.isActive] = true
                    it[UserPushTokens.updatedAt] = now
                }

                mapRow(
                    UserPushTokens.selectAll()
                        .where { UserPushTokens.fcmToken eq normalizedToken }
                        .single()
                )
            } else {
                val entity = UserPushToken(
                    tokenId = UUID.randomUUID().toString(),
                    userId = userId,
                    fcmToken = normalizedToken,
                    platform = normalizedPlatform,
                    deviceId = normalizedDeviceId,
                    isActive = true,
                    createdAt = now,
                    updatedAt = now
                )

                UserPushTokens.insert {
                    it[tokenId] = entity.tokenId
                    it[UserPushTokens.userId] = entity.userId
                    it[fcmToken] = entity.fcmToken
                    it[UserPushTokens.platform] = entity.platform
                    it[UserPushTokens.deviceId] = entity.deviceId
                    it[isActive] = entity.isActive
                    it[createdAt] = entity.createdAt
                    it[updatedAt] = entity.updatedAt
                }

                entity
            }
        }
    }

    override fun findByToken(token: String): UserPushToken? {
        val normalizedToken = token.trim()
        return transaction {
            UserPushTokens.selectAll()
                .where { UserPushTokens.fcmToken eq normalizedToken }
                .singleOrNull()
                ?.let(::mapRow)
        }
    }

    override fun findActiveByUserId(userId: String): List<UserPushToken> {
        return transaction {
            UserPushTokens.selectAll()
                .where {
                    (UserPushTokens.userId eq userId) and
                        (UserPushTokens.isActive eq true)
                }
                .map(::mapRow)
        }
    }

    override fun deactivateByToken(token: String): Boolean {
        val normalizedToken = token.trim()
        return transaction {
            UserPushTokens.update(where = { UserPushTokens.fcmToken eq normalizedToken }) {
                it[isActive] = false
                it[updatedAt] = Instant.now()
            } > 0
        }
    }

    override fun deleteByTokens(tokens: Collection<String>): Int {
        val normalizedTokens = tokens.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (normalizedTokens.isEmpty()) return 0

        return transaction {
            UserPushTokens.deleteWhere { UserPushTokens.fcmToken inList normalizedTokens }
        }
    }

    private fun mapRow(row: ResultRow): UserPushToken {
        return UserPushToken(
            tokenId = row[UserPushTokens.tokenId],
            userId = row[UserPushTokens.userId],
            fcmToken = row[UserPushTokens.fcmToken],
            platform = row[UserPushTokens.platform],
            deviceId = row[UserPushTokens.deviceId],
            isActive = row[UserPushTokens.isActive],
            createdAt = row[UserPushTokens.createdAt],
            updatedAt = row[UserPushTokens.updatedAt]
        )
    }
}
