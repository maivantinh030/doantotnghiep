package com.park.smartcard.data.model

import kotlinx.serialization.Serializable

// ===== Common =====
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

// ===== Auth =====
@Serializable
data class AdminLoginRequest(
    val phoneNumber: String,
    val password: String
)

@Serializable
data class AdminInfo(
    val adminId: String,
    val accountId: String,
    val phoneNumber: String,
    val fullName: String,
    val employeeCode: String? = null,
    val role: String = "ADMIN"
)

@Serializable
data class AuthData(
    val token: String,
    val admin: AdminInfo
)

// ===== Customer (Staff lookup) =====

@Serializable
data class CustomerDTO(
    val userId: String,
    val phoneNumber: String,
    val fullName: String? = null,
    val email: String? = null,
    val currentBalance: String = "0",
    val avatarUrl: String? = null,
    val dateOfBirth: String? = null  // format YYYY-MM-DD từ backend
)

// ===== Smart Card =====

@Serializable
data class CardDTO(
    val cardId: String,
    val cardName: String? = null,
    val userId: String? = null,
    val status: String,
    val depositAmount: String = "0",
    val depositStatus: String = "NONE",
    val issuedAt: String? = null,
    val blockedAt: String? = null,
    val blockedReason: String? = null,
    val lastUsedAt: String? = null,
    val createdAt: String
)

@Serializable
data class RegisterCardRequest(
    val cardId: String,
    val cardName: String? = null
)

@Serializable
data class IssueCardRequest(
    val cardId: String,
    val userId: String,
    val cardName: String? = null,
    val depositAmount: String = "0"
)

@Serializable
data class BlockCardRequest(
    val reason: String? = null
)

@Serializable
data class CardLookupRequest(
    val cardId: String
)

// ===== Card Request (từ mobile app) =====

@Serializable
data class CardRequestDTO(
    val requestId: String,
    val userId: String,
    val status: String,
    val depositPaidOnline: Boolean = false,
    val depositAmount: String = "0",
    val note: String? = null,
    val approvedBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val requester: CustomerDTO? = null
)

@Serializable
data class ApproveCardRequestDTO(
    val approved: Boolean,
    val note: String? = null
)

@Serializable
data class IssueCardFromRequestDTO(
    val cardId: String,
    val publicKey: String
)

// ===== Top Up =====

@Serializable
data class TopUpRequest(
    val amount: String,
    val method: String = "CASH"
)

@Serializable
data class TopUpResult(
    val paymentId: String,
    val userId: String,
    val method: String,
    val amount: String,
    val status: String,
    val currentBalanceAfter: String? = null,
    val createdAt: String,
    val orderId: String? = null,
    val payUrl: String? = null,
    val qrCodeUrl: String? = null
)

// ===== Return Summary =====

@Serializable
data class ReturnSummary(
    val cardId: String,
    val refundedBalance: String,
    val refundedDeposit: String
)

// ===== Direct Issue (cấp thẻ trực tiếp tại quầy) =====

@Serializable
data class DirectIssueRequest(
    val customerID: String,
    val cardID: String,
    val fullName: String,
    val dateOfBirth: String? = null,
    val phoneNumber: String,
    val publicKey: String
)
