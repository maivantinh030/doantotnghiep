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
    val currentBalanceAfter: String? = null,
    val createdAt: String,
    val orderId: String?    = null,    // thêm mới
    val payUrl: String?     = null,
    val qrCodeUrl: String?  = null     // thêm mới
) {
    companion object {
        fun fromEntity(pr: PaymentRecord): PaymentRecordDTO {
            val payUrl = pr.qrData
            return PaymentRecordDTO(
                paymentId = pr.paymentId,
                userId = pr.userId,
                method = pr.method,
                amount = pr.amount.toString(),
                status = pr.status,
                currentBalanceAfter = null,
                createdAt = pr.createdAt.toString(),
                orderId = pr.orderId,
                payUrl = payUrl,
                qrCodeUrl = payUrl?.let { buildQrCodeUrl(it) }
            )
        }

        private fun buildQrCodeUrl(payUrl: String): String {
            return "https://api.qrserver.com/v1/create-qr-code/?size=400x400" +
                    "&data=${java.net.URLEncoder.encode(payUrl, "UTF-8")}"
        }
    }
}

@Serializable
data class TopUpRequest(
    val amount: String,
    val method: String = "MOMO",
    val description: String? = null
// MOMO | VNPAY | BANKING | CASH
)

@Serializable
data class WalletBalanceDTO(
    val currentBalance: String
)
