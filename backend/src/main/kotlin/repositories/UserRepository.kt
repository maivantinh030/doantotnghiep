package com.park.repositories

import com.park.database.tables.Users
import com.park.dto.CreateUserDTO
import com.park.entities.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Interface định nghĩa các operations với User
 */
interface IUserRepository {
    fun create(dto: CreateUserDTO): User
    fun findById(userId: String): User?
    fun findByAccountId(accountId: String): User?
    fun findByEmail(email: String): User?
    fun findByReferralCode(referralCode: String): User?
    fun existsByEmail(email: String): Boolean
    fun update(userId: String, updates: Map<String, Any?>): Boolean
}

/**
 * Implementation của UserRepository
 */
class UserRepository : IUserRepository {

    override fun create(dto: CreateUserDTO): User {
        return transaction {
            val userId = UUID.randomUUID().toString()
            val now = Instant.now()
            val referralCode = generateReferralCode()

            Users.insert {
                it[Users.userId] = userId
                it[accountId] = dto.accountId
                it[fullName] = dto.fullName
                it[email] = dto.email
                it[dateOfBirth] = dto.dateOfBirth?.let { date ->
                    LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
                }
                it[gender] = dto.gender
                it[membershipLevel] = "BRONZE"
                it[currentBalance] = BigDecimal.ZERO
                it[loyaltyPoints] = 0
                it[isCardLocked] = false
                it[Users.referralCode] = referralCode
                it[referredBy] = null // Will be set later if referral code provided
                it[createdAt] = now
                it[updatedAt] = now
            }

            User(
                userId = userId,
                accountId = dto.accountId,
                fullName = dto.fullName,
                email = dto.email,
                dateOfBirth = dto.dateOfBirth?.let { LocalDate.parse(it) },
                gender = dto.gender,
                membershipLevel = "BRONZE",
                currentBalance = BigDecimal.ZERO,
                loyaltyPoints = 0,
                avatarUrl = null,
                isCardLocked = false,
                referralCode = referralCode,
                referredBy = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    override fun findById(userId: String): User? {
        return transaction {
            Users.selectAll().where { Users.userId eq userId }
                .singleOrNull()
                ?.let { mapRowToUser(it) }
        }
    }

    override fun findByAccountId(accountId: String): User? {
        return transaction {
            Users.selectAll().where { Users.accountId eq accountId }
                .singleOrNull()
                ?.let { mapRowToUser(it) }
        }
    }

    override fun findByEmail(email: String): User? {
        return transaction {
            Users.selectAll().where { Users.email eq email }
                .singleOrNull()
                ?.let { mapRowToUser(it) }
        }
    }

    override fun findByReferralCode(referralCode: String): User? {
        return transaction {
            Users.selectAll().where { Users.referralCode eq referralCode }
                .singleOrNull()
                ?.let { mapRowToUser(it) }
        }
    }

    override fun existsByEmail(email: String): Boolean {
        return transaction {
            Users.selectAll().where { Users.email eq email }
                .count() > 0
        }
    }

    override fun update(userId: String, updates: Map<String, Any?>): Boolean {
        return transaction {
            Users.update(where = { Users.userId eq userId }) { stmt ->
                updates.forEach { (key, value) ->
                    when (key) {
                        "fullName" -> stmt[fullName] = value as? String
                        "email" -> stmt[email] = value as? String
                        "avatarUrl" -> stmt[avatarUrl] = value as? String
                        "membershipLevel" -> stmt[membershipLevel] = value as String
                        "currentBalance" -> stmt[currentBalance] = value as BigDecimal
                        "loyaltyPoints" -> stmt[loyaltyPoints] = value as Int
                        "isCardLocked" -> stmt[isCardLocked] = value as Boolean
                        "referredBy" -> stmt[referredBy] = value as? String
                    }
                }
                stmt[updatedAt] = Instant.now()
            } > 0
        }
    }

    /**
     * Helper method để map ResultRow sang User entity
     */
    private fun mapRowToUser(row: ResultRow): User {
        return User(
            userId = row[Users.userId],
            accountId = row[Users.accountId],
            fullName = row[Users.fullName],
            email = row[Users.email],
            dateOfBirth = row[Users.dateOfBirth],
            gender = row[Users.gender],
            membershipLevel = row[Users.membershipLevel],
            currentBalance = row[Users.currentBalance],
            loyaltyPoints = row[Users.loyaltyPoints],
            avatarUrl = row[Users.avatarUrl],
            isCardLocked = row[Users.isCardLocked],
            referralCode = row[Users.referralCode],
            referredBy = row[Users.referredBy],
            createdAt = row[Users.createdAt],
            updatedAt = row[Users.updatedAt]
        )
    }

    /**
     * Tạo mã giới thiệu ngẫu nhiên
     */
    private fun generateReferralCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8)
            .map { chars.random() }
            .joinToString("")
    }
}
