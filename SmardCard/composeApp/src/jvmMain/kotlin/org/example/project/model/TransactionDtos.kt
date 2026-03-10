package org.example.project.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    @SerialName("id") val id: Long,
    @SerialName("customerId") val customerId: String,
    @SerialName("type") val type: String,
    @SerialName("gameCode") val gameCode: Int?,
    @SerialName("tickets") val tickets: Int?,
    @SerialName("amount") val amount: String,
    @SerialName("balanceAfter") val balanceAfter: Int?,
    @SerialName("createdAt") val createdAt: String
)

@Serializable
data class CreateTransactionRequest(
    @SerialName("customerId") val customerId: String,
    @SerialName("type") val type: String,
    @SerialName("amount") val amount: String,
    @SerialName("tickets") val tickets: Int? = null,
    @SerialName("gameCode") val gameCode: Int? = null,
    @SerialName("balanceAfter") val balanceAfter: Int? = null,
    @SerialName("status") val status: String? = null
)

@Serializable
data class TransactionsResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: List<TransactionDto>?,
    @SerialName("message") val message: String? = null
)

@Serializable
data class RevenuePoint(
    @SerialName("label") val label: String,
    @SerialName("totalAmount") val totalAmount: String
)

@Serializable
data class RevenueResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: List<RevenuePoint>?,
    @SerialName("message") val message: String? = null
)

@Serializable
data class GameRevenue(
    @SerialName("gameCode") val gameCode: Int,
    @SerialName("totalAmount") val totalAmount: String,
    @SerialName("totalTickets") val totalTickets: Int
)

@Serializable
data class GameRevenueResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: List<GameRevenue>?,
    @SerialName("message") val message: String? = null
)