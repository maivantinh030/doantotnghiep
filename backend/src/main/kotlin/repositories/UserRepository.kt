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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

interface IUserRepository {
    fun create(dto: CreateUserDTO): User
    fun findById(userId: String): User?
    fun findByAccountId(accountId: String): User?
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
    fun update(userId: String, updates: Map<String, Any?>): Boolean
}

class UserRepository : IUserRepository {

    private fun generateUserId(): String {
        val suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
        return "KH$suffix"  // 14 chars: KH + 12 digits
    }

    override fun create(dto: CreateUserDTO): User {
        return transaction {
            val userId = generateUserId()
            val now = Instant.now()

            Users.insert {
                it[Users.userId] = userId
                it[accountId] = dto.accountId
                it[fullName] = dto.fullName
                it[email] = dto.email
                it[dateOfBirth] = dto.dateOfBirth?.let { date ->
                    LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
                }
                it[gender] = dto.gender
                it[currentBalance] = BigDecimal.ZERO
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
                currentBalance = BigDecimal.ZERO,
                avatarUrl = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    override fun findById(userId: String): User? {
        return transaction {
            Users.selectAll().where { Users.userId eq userId }
                .singleOrNull()?.let { mapRowToUser(it) }
        }
    }

    override fun findByAccountId(accountId: String): User? {
        return transaction {
            Users.selectAll().where { Users.accountId eq accountId }
                .singleOrNull()?.let { mapRowToUser(it) }
        }
    }

    override fun findByEmail(email: String): User? {
        return transaction {
            Users.selectAll().where { Users.email eq email }
                .singleOrNull()?.let { mapRowToUser(it) }
        }
    }

    override fun existsByEmail(email: String): Boolean {
        return transaction {
            Users.selectAll().where { Users.email eq email }.count() > 0
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
                        "currentBalance" -> stmt[currentBalance] = value as BigDecimal
                    }
                }
                stmt[updatedAt] = Instant.now()
            } > 0
        }
    }

    private fun mapRowToUser(row: ResultRow): User {
        return User(
            userId = row[Users.userId],
            accountId = row[Users.accountId],
            fullName = row[Users.fullName],
            email = row[Users.email],
            dateOfBirth = row[Users.dateOfBirth],
            gender = row[Users.gender],
            currentBalance = row[Users.currentBalance],
            avatarUrl = row[Users.avatarUrl],
            createdAt = row[Users.createdAt],
            updatedAt = row[Users.updatedAt]
        )
    }
}
