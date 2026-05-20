package com.park.repositories

import com.park.database.tables.BalanceTransactions
import com.park.database.tables.PaymentRecords
import com.park.entities.BalanceTransaction
import com.park.entities.PaymentRecord
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

interface IBalanceTransactionRepository {
    fun create(tx: BalanceTransaction): BalanceTransaction
    fun findByUserId(userId: String, limit: Int, offset: Long): List<BalanceTransaction>
    fun findByUserIdAndType(userId: String, type: String, limit: Int, offset: Long): List<BalanceTransaction>
    fun countByUserId(userId: String): Long
    fun findLatestByReference(referenceType: String, referenceId: String): BalanceTransaction?
    fun countAll(): Long
    fun findAllForAdmin(limit: Int, offset: Long): List<com.park.dto.AdminTransactionDTO>
}

interface IPaymentRepository {
    fun create(payment: PaymentRecord): PaymentRecord
    fun findById(paymentId: String): PaymentRecord?
    fun findByUserId(userId: String, limit: Int, offset: Long): List<PaymentRecord>
    fun countByUserId(userId: String): Long
    fun updateStatus(paymentId: String, status: String): Boolean
    fun updateMomoSuccess(orderId: String,momoTransId: String):Boolean
    fun findByOrderId(orderId: String): PaymentRecord?
    fun sumSuccessAmount(): java.math.BigDecimal
    fun findSuccessSince(start: Instant): List<PaymentRecord>
}

class BalanceTransactionRepository : IBalanceTransactionRepository {

    override fun create(tx: BalanceTransaction): BalanceTransaction {
        return transaction {
            BalanceTransactions.insert {
                it[transactionId] = tx.transactionId
                it[userId] = tx.userId
                it[amount] = tx.amount
                it[balanceBefore] = tx.balanceBefore
                it[balanceAfter] = tx.balanceAfter
                it[type] = tx.type
                it[referenceType] = tx.referenceType
                it[referenceId] = tx.referenceId
                it[description] = tx.description
                it[createdAt] = tx.createdAt
                it[createdBy] = tx.createdBy
            }
            tx
        }
    }

    override fun findByUserId(userId: String, limit: Int, offset: Long): List<BalanceTransaction> {
        return transaction {
            BalanceTransactions.selectAll().where { BalanceTransactions.userId eq userId }
                .orderBy(BalanceTransactions.createdAt, SortOrder.DESC)
                .limit(limit).offset(offset)
                .map { mapRow(it) }
        }
    }

    override fun findByUserIdAndType(userId: String, type: String, limit: Int, offset: Long): List<BalanceTransaction> {
        return transaction {
            BalanceTransactions.selectAll().where {
                (BalanceTransactions.userId eq userId) and (BalanceTransactions.type eq type)
            }
                .orderBy(BalanceTransactions.createdAt, SortOrder.DESC)
                .limit(limit).offset(offset)
                .map { mapRow(it) }
        }
    }

    override fun countByUserId(userId: String): Long {
        return transaction {
            BalanceTransactions.selectAll().where { BalanceTransactions.userId eq userId }.count()
        }
    }

    override fun findLatestByReference(referenceType: String, referenceId: String): BalanceTransaction? {
        return transaction {
            BalanceTransactions.selectAll().where {
                (BalanceTransactions.referenceType eq referenceType) and
                    (BalanceTransactions.referenceId eq referenceId)
            }
                .orderBy(BalanceTransactions.createdAt, SortOrder.DESC)
                .limit(1)
                .singleOrNull()?.let { mapRow(it) }
        }
    }

    override fun countAll(): Long {
        return transaction { BalanceTransactions.selectAll().count() }
    }

    override fun findAllForAdmin(limit: Int, offset: Long): List<com.park.dto.AdminTransactionDTO> {
        return transaction {
            val query = BalanceTransactions
                .join(com.park.database.tables.Users, JoinType.LEFT, BalanceTransactions.userId, com.park.database.tables.Users.userId)
            query.selectAll()
                .orderBy(BalanceTransactions.createdAt, SortOrder.DESC)
                .limit(limit).offset(offset)
                .map { row ->
                    com.park.dto.AdminTransactionDTO(
                        transactionId = row[BalanceTransactions.transactionId],
                        userId = row[BalanceTransactions.userId],
                        amount = row[BalanceTransactions.amount].toString(),
                        balanceBefore = row[BalanceTransactions.balanceBefore].toString(),
                        balanceAfter = row[BalanceTransactions.balanceAfter].toString(),
                        type = row[BalanceTransactions.type],
                        referenceType = row[BalanceTransactions.referenceType],
                        referenceId = row[BalanceTransactions.referenceId],
                        description = row[BalanceTransactions.description],
                        createdAt = row[BalanceTransactions.createdAt].toString(),
                        createdBy = row[BalanceTransactions.createdBy]
                    )
                }
        }
    }

    private fun mapRow(row: ResultRow): BalanceTransaction {
        return BalanceTransaction(
            transactionId = row[BalanceTransactions.transactionId],
            userId = row[BalanceTransactions.userId],
            amount = row[BalanceTransactions.amount],
            balanceBefore = row[BalanceTransactions.balanceBefore],
            balanceAfter = row[BalanceTransactions.balanceAfter],
            type = row[BalanceTransactions.type],
            referenceType = row[BalanceTransactions.referenceType],
            referenceId = row[BalanceTransactions.referenceId],
            description = row[BalanceTransactions.description],
            createdAt = row[BalanceTransactions.createdAt],
            createdBy = row[BalanceTransactions.createdBy]
        )
    }
}

class PaymentRepository : IPaymentRepository {

    override fun create(payment: PaymentRecord): PaymentRecord {
        return transaction {
            PaymentRecords.insert {
                it[paymentId] = payment.paymentId
                it[userId] = payment.userId
                it[method] = payment.method
                it[amount] = payment.amount
                it[status] = payment.status
                it[createdAt] = payment.createdAt
                it[orderId]     = payment.orderId
                it[momoTransId] = payment.momoTransId
                it[qrData]      = payment.qrData
                it[completedAt] = payment.completedAt
            }
            payment
        }
    }

    override fun findById(paymentId: String): PaymentRecord? {
        return transaction {
            PaymentRecords.selectAll().where { PaymentRecords.paymentId eq paymentId }
                .singleOrNull()?.let { mapRow(it) }
        }
    }

    override fun findByUserId(userId: String, limit: Int, offset: Long): List<PaymentRecord> {
        return transaction {
            PaymentRecords.selectAll().where { PaymentRecords.userId eq userId }
                .orderBy(PaymentRecords.createdAt, SortOrder.DESC)
                .limit(limit).offset(offset)
                .map { mapRow(it) }
        }
    }

    override fun countByUserId(userId: String): Long {
        return transaction {
            PaymentRecords.selectAll().where { PaymentRecords.userId eq userId }.count()
        }
    }

    override fun updateStatus(paymentId: String, status: String): Boolean {
        return transaction {
            PaymentRecords.update(where = { PaymentRecords.paymentId eq paymentId }) {
                it[PaymentRecords.status] = status
            } > 0
        }
    }
    override fun updateMomoSuccess(orderId: String, momoTransId: String): Boolean {
        return transaction {
            PaymentRecords.update(
                where = {
                    (PaymentRecords.orderId eq orderId) and
                            (PaymentRecords.status eq "PENDING")  // chống duplicate IPN
                }
            ) {
                it[status]              = "SUCCESS"
                it[PaymentRecords.momoTransId] = momoTransId
                it[completedAt]         = Instant.now()
            } > 0
        }
    }

    override fun findByOrderId(orderId: String): PaymentRecord? {
        return transaction {
            PaymentRecords.selectAll()
                .where{ PaymentRecords.orderId eq orderId }
                .singleOrNull()?.let { mapRow(it) }
        }
    }

    override fun sumSuccessAmount(): java.math.BigDecimal {
        return transaction {
            PaymentRecords.selectAll()
                .where { PaymentRecords.status eq "SUCCESS" }
                .sumOf { it[PaymentRecords.amount] }
        }
    }

    override fun findSuccessSince(start: Instant): List<PaymentRecord> {
        return transaction {
            PaymentRecords.selectAll()
                .where {
                    (PaymentRecords.status eq "SUCCESS") and
                        (PaymentRecords.createdAt greaterEq start)
                }
                .map { mapRow(it) }
        }
    }

    private fun mapRow(row: ResultRow): PaymentRecord {
        return PaymentRecord(
            paymentId = row[PaymentRecords.paymentId],
            userId = row[PaymentRecords.userId],
            method = row[PaymentRecords.method],
            amount = row[PaymentRecords.amount],
            status = row[PaymentRecords.status],
            createdAt = row[PaymentRecords.createdAt],
            orderId = row[PaymentRecords.orderId],
            momoTransId = row[PaymentRecords.momoTransId],
            qrData = row[PaymentRecords.qrData],
            completedAt = row[PaymentRecords.completedAt]

        )
    }
}
