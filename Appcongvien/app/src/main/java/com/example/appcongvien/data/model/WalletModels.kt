package com.example.appcongvien.data.model

// ===== Responses =====
data class WalletBalanceDTO(
    val currentBalance: String,
    val userId: String,
    val loyaltyPoints: Int = 0
)

data class TransactionDTO(
    val transactionId: String,
    val userId: String? = null,
    val type: String,
    val amount: String,
    val balanceBefore: String? = null,
    val balanceAfter: String? = null,
    val referenceType: String? = null,
    val referenceId: String? = null,
    val description: String?,
    val status: String? = null,
    val createdAt: String
)

data class PaymentRecordDTO(
    val paymentId: String,
    val amount: String,
    val method: String,
    val status: String,
    val createdAt: String
)

// ===== Requests =====
data class TopUpRequest(
    val amount: String,
    val method: String
)
