package org.example.project.network

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.example.project.model.*

class TransactionApiClient {
    fun record(request: CreateTransactionRequest): Result<Unit> = runBlocking {
        try {
            val resp = ApiClient.http.post("/transactions/record") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (resp.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Server error: ${resp.status.value}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    fun history(customerId: String): Result<List<TransactionDto>> = runBlocking {
        try {
            val resp = ApiClient.http.get("/transactions/history/$customerId")
            if (resp.status.isSuccess()) {
                val dto = resp.body<TransactionsResponse>()
                dto.data?.let { return@runBlocking Result.success(it) }
                Result.failure(Exception("Không parse được lịch sử"))
            } else {
                Result.failure(Exception("Server error: ${resp.status.value}"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    fun revenueByDay(): Result<List<RevenuePoint>> = revenueGeneric("/analytics/revenue/day")
    
    fun revenueByMonth(): Result<List<RevenuePoint>> = revenueGeneric("/analytics/revenue/month")
    
    fun revenueByGame(): Result<List<GameRevenue>> = runBlocking {
        try {
            val resp = ApiClient.http.get("/analytics/revenue/game")
            if (resp.status.isSuccess()) {
                val dto = resp.body<GameRevenueResponse>()
                dto.data?.let { return@runBlocking Result.success(it) }
                Result.failure(Exception("Không parse được doanh thu theo game"))
            } else Result.failure(Exception("Server error: ${resp.status.value}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    private fun revenueGeneric(path: String): Result<List<RevenuePoint>> = runBlocking {
        try {
            val resp = ApiClient.http.get(path)
            if (resp.status.isSuccess()) {
                val dto = resp.body<RevenueResponse>()
                dto.data?.let { return@runBlocking Result.success(it) }
                Result.failure(Exception("Không parse được doanh thu"))
            } else Result.failure(Exception("Server error: ${resp.status.value}"))
        } catch (e: Exception) { Result.failure(e) }
    }
}