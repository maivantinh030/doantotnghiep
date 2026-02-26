package com.park.repositories

import com.park.database.tables.UserVouchers
import com.park.database.tables.Vouchers
import com.park.entities.UserVoucher
import com.park.entities.Voucher
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.Instant
import java.util.*

interface IVoucherRepository {
    fun create(voucher: Voucher): Voucher
    fun findById(voucherId: String): Voucher?
    fun findByCode(code: String): Voucher?
    fun findAllActive(limit: Int, offset: Long): List<Voucher>
    fun countActive(): Long
    fun update(voucherId: String, updates: Map<String, Any?>): Boolean
    fun delete(voucherId: String): Boolean
    fun incrementUsedCount(voucherId: String): Boolean
}

interface IUserVoucherRepository {
    fun create(userVoucher: UserVoucher): UserVoucher
    fun findByUserId(userId: String, limit: Int, offset: Long): List<UserVoucher>
    fun findByUserAndVoucher(userId: String, voucherId: String): UserVoucher?
    fun countByUserId(userId: String): Long
    fun countByUserAndVoucher(userId: String, voucherId: String): Long
    fun markUsed(id: String, orderId: String): Boolean
}

class VoucherRepository : IVoucherRepository {

    override fun create(voucher: Voucher): Voucher {
        return transaction {
            Vouchers.insert {
                it[voucherId] = voucher.voucherId
                it[code] = voucher.code
                it[title] = voucher.title
                it[description] = voucher.description
                it[discountType] = voucher.discountType
                it[discountValue] = voucher.discountValue
                it[maxDiscount] = voucher.maxDiscount
                it[minOrderValue] = voucher.minOrderValue
                it[usageLimit] = voucher.usageLimit
                it[usedCount] = voucher.usedCount
                it[perUserLimit] = voucher.perUserLimit
                it[applicableGames] = voucher.applicableGames
                it[startDate] = voucher.startDate
                it[endDate] = voucher.endDate
                it[isActive] = voucher.isActive
                it[createdAt] = voucher.createdAt
                it[updatedAt] = voucher.updatedAt
            }
            voucher
        }
    }

    override fun findById(voucherId: String): Voucher? {
        return transaction {
            Vouchers.selectAll().where { Vouchers.voucherId eq voucherId }
                .singleOrNull()?.let { mapRow(it) }
        }
    }

    override fun findByCode(code: String): Voucher? {
        return transaction {
            Vouchers.selectAll().where { Vouchers.code eq code }
                .singleOrNull()?.let { mapRow(it) }
        }
    }

    override fun findAllActive(limit: Int, offset: Long): List<Voucher> {
        val now = Instant.now()
        return transaction {
            Vouchers.selectAll().where {
                (Vouchers.isActive eq true) and
                        (Vouchers.startDate lessEq now) and
                        (Vouchers.endDate greaterEq now)
            }
                .orderBy(Vouchers.endDate, SortOrder.ASC)
                .limit(limit).offset(offset)
                .map { mapRow(it) }
        }
    }

    override fun countActive(): Long {
        val now = Instant.now()
        return transaction {
            Vouchers.selectAll().where {
                (Vouchers.isActive eq true) and
                        (Vouchers.startDate lessEq now) and
                        (Vouchers.endDate greaterEq now)
            }.count()
        }
    }

    override fun update(voucherId: String, updates: Map<String, Any?>): Boolean {
        return transaction {
            Vouchers.update(where = { Vouchers.voucherId eq voucherId }) { stmt ->
                updates.forEach { (key, value) ->
                    when (key) {
                        "title" -> stmt[title] = value as String
                        "description" -> stmt[description] = value as? String
                        "discountValue" -> stmt[discountValue] = value as BigDecimal
                        "maxDiscount" -> stmt[maxDiscount] = value as? BigDecimal
                        "minOrderValue" -> stmt[minOrderValue] = value as BigDecimal
                        "usageLimit" -> stmt[usageLimit] = value as? Int
                        "perUserLimit" -> stmt[perUserLimit] = value as Int
                        "isActive" -> stmt[isActive] = value as Boolean
                        "endDate" -> stmt[endDate] = value as Instant
                    }
                }
                stmt[updatedAt] = Instant.now()
            } > 0
        }
    }

    override fun delete(voucherId: String): Boolean {
        return transaction {
            Vouchers.deleteWhere { Vouchers.voucherId eq voucherId } > 0
        }
    }

    override fun incrementUsedCount(voucherId: String): Boolean {
        return transaction {
            Vouchers.update(where = { Vouchers.voucherId eq voucherId }) {
                with(SqlExpressionBuilder) {
                    it[usedCount] = usedCount + 1
                }
                it[updatedAt] = Instant.now()
            } > 0
        }
    }

    private fun mapRow(row: ResultRow): Voucher {
        return Voucher(
            voucherId = row[Vouchers.voucherId],
            code = row[Vouchers.code],
            title = row[Vouchers.title],
            description = row[Vouchers.description],
            discountType = row[Vouchers.discountType],
            discountValue = row[Vouchers.discountValue],
            maxDiscount = row[Vouchers.maxDiscount],
            minOrderValue = row[Vouchers.minOrderValue],
            usageLimit = row[Vouchers.usageLimit],
            usedCount = row[Vouchers.usedCount],
            perUserLimit = row[Vouchers.perUserLimit],
            applicableGames = row[Vouchers.applicableGames],
            startDate = row[Vouchers.startDate],
            endDate = row[Vouchers.endDate],
            isActive = row[Vouchers.isActive],
            createdAt = row[Vouchers.createdAt],
            updatedAt = row[Vouchers.updatedAt]
        )
    }
}

class UserVoucherRepository : IUserVoucherRepository {

    override fun create(userVoucher: UserVoucher): UserVoucher {
        return transaction {
            UserVouchers.insert {
                it[id] = userVoucher.id
                it[userId] = userVoucher.userId
                it[voucherId] = userVoucher.voucherId
                it[voucherSource] = userVoucher.source
                it[isUsed] = userVoucher.isUsed
                it[createdAt] = userVoucher.createdAt
            }
            userVoucher
        }
    }

    override fun findByUserId(userId: String, limit: Int, offset: Long): List<UserVoucher> {
        return transaction {
            UserVouchers.selectAll().where { UserVouchers.userId eq userId }
                .orderBy(UserVouchers.createdAt, SortOrder.DESC)
                .limit(limit).offset(offset)
                .map { mapRow(it) }
        }
    }

    override fun findByUserAndVoucher(userId: String, voucherId: String): UserVoucher? {
        return transaction {
            UserVouchers.selectAll().where {
                (UserVouchers.userId eq userId) and (UserVouchers.voucherId eq voucherId)
            }.singleOrNull()?.let { mapRow(it) }
        }
    }

    override fun countByUserId(userId: String): Long {
        return transaction {
            UserVouchers.selectAll().where { UserVouchers.userId eq userId }.count()
        }
    }

    override fun countByUserAndVoucher(userId: String, voucherId: String): Long {
        return transaction {
            UserVouchers.selectAll().where {
                (UserVouchers.userId eq userId) and (UserVouchers.voucherId eq voucherId)
            }.count()
        }
    }

    override fun markUsed(id: String, orderId: String): Boolean {
        return transaction {
            UserVouchers.update(where = { UserVouchers.id eq id }) {
                it[isUsed] = true
                it[usedAt] = Instant.now()
                it[usedOrderId] = orderId
            } > 0
        }
    }

    private fun mapRow(row: ResultRow): UserVoucher {
        return UserVoucher(
            id = row[UserVouchers.id],
            userId = row[UserVouchers.userId],
            voucherId = row[UserVouchers.voucherId],
            source = row[UserVouchers.voucherSource],
            isUsed = row[UserVouchers.isUsed],
            usedAt = row[UserVouchers.usedAt],
            usedOrderId = row[UserVouchers.usedOrderId],
            createdAt = row[UserVouchers.createdAt]
        )
    }
}
