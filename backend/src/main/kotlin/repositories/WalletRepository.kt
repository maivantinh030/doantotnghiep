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
}

interface IPaymentRepository {
    fun create(payment: PaymentRecord): PaymentRecord
    fun findById(paymentId: String): PaymentRecord?
    fun findByUserId(userId: String, limit: Int, offset: Long): List<PaymentRecord>
    fun countByUserId(userId: String): Long
    fun updateStatus(paymentId: String, status: String): Boolean
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

    private fun mapRow(row: ResultRow): PaymentRecord {
        return PaymentRecord(
            paymentId = row[PaymentRecords.paymentId],
            userId = row[PaymentRecords.userId],
            method = row[PaymentRecords.method],
            amount = row[PaymentRecords.amount],
            status = row[PaymentRecords.status],
            createdAt = row[PaymentRecords.createdAt]
        )
    }
}
