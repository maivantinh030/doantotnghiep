package org.example.project.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.example.project.model.*

class TransactionApiClient {
    private val moshi = MoshiProvider.moshi
    private val jsonMediaType = "application/json".toMediaType()

    private val createReqAdapter = moshi.adapter(CreateTransactionRequest::class.java)
    private val txnRespAdapter = moshi.adapter(TransactionsResponse::class.java)
    private val revenueRespAdapter = moshi.adapter(RevenueResponse::class.java)
    private val gameRevenueRespAdapter = moshi.adapter(GameRevenueResponse::class.java)

    fun record(request: CreateTransactionRequest): Result<Unit> {
        return try {
            val json = createReqAdapter.toJson(request)
            ApiClient.post("/transactions/record", json).use { resp ->
                if (resp.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Server error: ${resp.code}"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    fun history(customerId: String): Result<List<TransactionDto>> {
        return try {
            ApiClient.get("/transactions/history/$customerId").use { resp ->
                val body = resp.body?.string()
                if (resp.isSuccessful && body != null) {
                    val dto = txnRespAdapter.fromJson(body)
                    dto?.data?.let { return Result.success(it) }
                    Result.failure(Exception("Không parse được lịch sử"))
                } else {
                    Result.failure(Exception("Server error: ${resp.code}"))
                }
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    fun revenueByDay(): Result<List<RevenuePoint>> = revenueGeneric("/analytics/revenue/day")
    fun revenueByMonth(): Result<List<RevenuePoint>> = revenueGeneric("/analytics/revenue/month")
    fun revenueByGame(): Result<List<GameRevenue>> {
        return try {
            ApiClient.get("/analytics/revenue/game").use { resp ->
                val body = resp.body?.string()
                if (resp.isSuccessful && body != null) {
                    val dto = gameRevenueRespAdapter.fromJson(body)
                    dto?.data?.let { return Result.success(it) }
                    Result.failure(Exception("Không parse được doanh thu theo game"))
                } else Result.failure(Exception("Server error: ${resp.code}"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    private fun revenueGeneric(path: String): Result<List<RevenuePoint>> {
        return try {
            ApiClient.get(path).use { resp ->
                val body = resp.body?.string()
                if (resp.isSuccessful && body != null) {
                    val dto = revenueRespAdapter.fromJson(body)
                    dto?.data?.let { return Result.success(it) }
                    Result.failure(Exception("Không parse được doanh thu"))
                } else Result.failure(Exception("Server error: ${resp.code}"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }
}