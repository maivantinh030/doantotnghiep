package org.example.project.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TransactionDto(
    @Json(name = "id") val id: Long,
    @Json(name = "customerId") val customerId: String,
    @Json(name = "type") val type: String,
    @Json(name = "gameCode") val gameCode: Int?,
    @Json(name = "tickets") val tickets: Int?,
    @Json(name = "amount") val amount: String,
    @Json(name = "balanceAfter") val balanceAfter: Int?,
    @Json(name = "createdAt") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class CreateTransactionRequest(
    @Json(name = "customerId") val customerId: String,
    @Json(name = "type") val type: String,
    @Json(name = "amount") val amount: String,
    @Json(name = "tickets") val tickets: Int? = null,
    @Json(name = "gameCode") val gameCode: Int? = null,
    @Json(name = "balanceAfter") val balanceAfter: Int? = null,
    @Json(name = "status") val status: String? = null
)

@JsonClass(generateAdapter = true)
data class TransactionsResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: List<TransactionDto>?,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class RevenuePoint(
    @Json(name = "label") val label: String,
    @Json(name = "totalAmount") val totalAmount: String
)

@JsonClass(generateAdapter = true)
data class RevenueResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: List<RevenuePoint>?,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class GameRevenue(
    @Json(name = "gameCode") val gameCode: Int,
    @Json(name = "totalAmount") val totalAmount: String,
    @Json(name = "totalTickets") val totalTickets: Int
)

@JsonClass(generateAdapter = true)
data class GameRevenueResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: List<GameRevenue>?,
    @Json(name = "message") val message: String? = null
)