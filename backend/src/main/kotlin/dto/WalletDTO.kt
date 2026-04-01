package com.park.dto

import com.park.entities.BalanceTransaction
import com.park.entities.PaymentRecord
import kotlinx.serialization.Serializable

@Serializable
data class BalanceTransactionDTO(
    val transactionId: String,
    val userId: String,
    val amount: String,
    val balanceBefore: String,
    val balanceAfter: String,
    val type: String,
    val referenceType: String?,
    val referenceId: String?,
    val description: String?,
    val createdAt: String
) {
    companion object {
        fun fromEntity(tx: BalanceTransaction): BalanceTransactionDTO {
            return BalanceTransactionDTO(
                transactionId = tx.transactionId,
                userId = tx.userId,
                amount = tx.amount.toString(),
                balanceBefore = tx.balanceBefore.toString(),
                balanceAfter = tx.balanceAfter.toString(),
                type = tx.type,
                referenceType = tx.referenceType,
                referenceId = tx.referenceId,
                description = tx.description,
                createdAt = tx.createdAt.toString()
            )
        }
    }
}

@Serializable
data class PaymentRecordDTO(
    val paymentId: String,
    val userId: String,
    val method: String,
    val amount: String,
    val status: String,
    val createdAt: String
) {
    companion object {
        fun fromEntity(pr: PaymentRecord): PaymentRecordDTO {
            return PaymentRecordDTO(
                paymentId = pr.paymentId,
                userId = pr.userId,
                method = pr.method,
                amount = pr.amount.toString(),
                status = pr.status,
                createdAt = pr.createdAt.toString()
            )
        }
    }
}

@Serializable
data class TopUpRequest(
    val amount: String,
    val method: String = "MOMO"     // MOMO | VNPAY | BANKING | CASH
)

@Serializable
data class WalletBalanceDTO(
    val currentBalance: String
)
