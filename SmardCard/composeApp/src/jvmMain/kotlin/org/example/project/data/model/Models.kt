package org.example.project.data.model

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

// ===== Games =====
@Serializable
data class GameDto(
    val gameCode: Int,
    val gameName: String,
    val gameDescription: String? = null,
    val gameImage: String? = null,
    val ticketPrice: String,
    val isActive: Boolean = true
)

@Serializable
data class AddGameRequest(
    val gameName: String,
    val gameDescription: String,
    val ticketPrice: String,
    val gameImage: String? = null
)

@Serializable
data class UpdateGameRequest(
    val gameName: String? = null,
    val gameDescription: String? = null,
    val ticketPrice: String? = null,
    val isActive: Boolean? = null
)

// ===== Transactions =====
@Serializable
data class TransactionDto(
    val id: Long,
    val customerId: String,
    val type: String,
    val gameCode: Int? = null,
    val tickets: Int? = null,
    val amount: String,
    val balanceAfter: Int? = null,
    val createdAt: String
)

@Serializable
data class CreateTransactionRequest(
    val customerId: String,
    val type: String,
    val amount: String,
    val tickets: Int? = null,
    val gameCode: Int? = null,
    val balanceAfter: Int? = null,
    val status: String? = null
)

@Serializable
data class RevenuePoint(
    val label: String,
    val totalAmount: String
)

@Serializable
data class GameRevenue(
    val gameCode: Int,
    val totalAmount: String,
    val totalTickets: Int
)

// ===== RSA =====
@Serializable
data class ChallengeResponse(
    val challenge: String,
    val expiresAt: Long
)

@Serializable
data class RSAVerifyRequest(
    val customerId: String,
    val challenge: String,
    val signature: String
)

@Serializable
data class RSAVerifyResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class RegisterKeyRequest(
    val customerId: String,
    val publicKey: String
)
