package com.park.repositories

import com.park.database.tables.Accounts
import com.park.dto.CreateAccountDTO
import com.park.entities.Account
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*

/**
 * Interface định nghĩa các operations với Account
 */
interface IAccountRepository {
    fun create(dto: CreateAccountDTO): Account
    fun findById(accountId: String): Account?
    fun findByPhoneNumber(phoneNumber: String): Account?
    fun updateLastLogin(accountId: String): Boolean
    fun existsByPhoneNumber(phoneNumber: String): Boolean
}

/**
 * Implementation của AccountRepository
 */
class AccountRepository : IAccountRepository {

    override fun create(dto: CreateAccountDTO): Account {
        return transaction {
            val accountId = UUID.randomUUID().toString()
            val now = Instant.now()

            Accounts.insert {
                it[Accounts.accountId] = accountId
                it[phoneNumber] = dto.phoneNumber
                it[passwordHash] = dto.passwordHash
                it[role] = dto.role
                it[status] = "ACTIVE"
                it[createdAt] = now
                it[updatedAt] = now
            }

            Account(
                accountId = accountId,
                phoneNumber = dto.phoneNumber,
                passwordHash = dto.passwordHash,
                role = dto.role,
                status = "ACTIVE",
                lastLogin = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    override fun findById(accountId: String): Account? {
        return transaction {
            Accounts.selectAll().where { Accounts.accountId eq accountId }
                .singleOrNull()
                ?.let { mapRowToAccount(it) }
        }
    }

    override fun findByPhoneNumber(phoneNumber: String): Account? {
        return transaction {
            Accounts.selectAll().where { Accounts.phoneNumber eq phoneNumber }
                .singleOrNull()
                ?.let { mapRowToAccount(it) }
        }
    }

    override fun updateLastLogin(accountId: String): Boolean {
        return transaction {
            Accounts.update(where = { Accounts.accountId eq accountId }) {
                it[lastLogin] = Instant.now()
                it[updatedAt] = Instant.now()
            } > 0
        }
    }

    override fun existsByPhoneNumber(phoneNumber: String): Boolean {
        return transaction {
            Accounts.selectAll().where { Accounts.phoneNumber eq phoneNumber }
                .count() > 0
        }
    }

    /**
     * Helper method để map ResultRow sang Account entity
     */
    private fun mapRowToAccount(row: ResultRow): Account {
        return Account(
            accountId = row[Accounts.accountId],
            phoneNumber = row[Accounts.phoneNumber],
            passwordHash = row[Accounts.passwordHash],
            role = row[Accounts.role],
            status = row[Accounts.status],
            lastLogin = row[Accounts.lastLogin],
            createdAt = row[Accounts.createdAt],
            updatedAt = row[Accounts.updatedAt]
        )
    }
}
