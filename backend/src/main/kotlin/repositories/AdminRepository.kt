package com.park.repositories

import com.park.database.tables.Admins
import com.park.entities.Admin
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*

interface IAdminRepository {
    fun create(adminId: String, accountId: String, fullName: String, employeeCode: String?): Admin
    fun findByAccountId(accountId: String): Admin?
    fun findById(adminId: String): Admin?
    fun updateLastAction(adminId: String): Boolean
}

class AdminRepository : IAdminRepository {

    override fun create(adminId: String, accountId: String, fullName: String, employeeCode: String?): Admin {
        return transaction {
            val now = Instant.now()
            Admins.insert {
                it[Admins.adminId] = adminId
                it[Admins.accountId] = accountId
                it[Admins.fullName] = fullName
                it[Admins.employeeCode] = employeeCode
                it[Admins.createdAt] = now
                it[Admins.updatedAt] = now
            }
            Admin(
                adminId = adminId,
                accountId = accountId,
                fullName = fullName,
                employeeCode = employeeCode,
                lastActionAt = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    override fun findByAccountId(accountId: String): Admin? {
        return transaction {
            Admins.selectAll().where { Admins.accountId eq accountId }
                .singleOrNull()?.let { mapRow(it) }
        }
    }

    override fun findById(adminId: String): Admin? {
        return transaction {
            Admins.selectAll().where { Admins.adminId eq adminId }
                .singleOrNull()?.let { mapRow(it) }
        }
    }

    override fun updateLastAction(adminId: String): Boolean {
        return transaction {
            Admins.update(where = { Admins.adminId eq adminId }) {
                it[lastActionAt] = Instant.now()
                it[updatedAt] = Instant.now()
            } > 0
        }
    }

    private fun mapRow(row: ResultRow): Admin {
        return Admin(
            adminId = row[Admins.adminId],
            accountId = row[Admins.accountId],
            fullName = row[Admins.fullName],
            employeeCode = row[Admins.employeeCode],
            lastActionAt = row[Admins.lastActionAt],
            createdAt = row[Admins.createdAt],
            updatedAt = row[Admins.updatedAt]
        )
    }
}
