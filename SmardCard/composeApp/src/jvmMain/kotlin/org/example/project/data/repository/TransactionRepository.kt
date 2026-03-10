package org.example.project.data.repository

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.example.project.data.model.ApiResponse
import org.example.project.data.model.CreateTransactionRequest
import org.example.project.data.model.GameRevenue
import org.example.project.data.model.RevenuePoint
import org.example.project.data.model.TransactionDto
import org.example.project.data.network.ApiClient

class TransactionRepository {

    private fun authHeader() = "Bearer ${ApiClient.getToken()}"

    suspend fun recordTransaction(request: CreateTransactionRequest): Result<Unit> {
        return try {
            val response = ApiClient.http.post("/transactions/record") {
                header(HttpHeaders.Authorization, authHeader())
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.body<ApiResponse<String?>>()
            if (body.success) Result.success(Unit)
            else Result.failure(Exception(body.message ?: "Lỗi ghi giao dịch"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHistory(customerId: String): Result<List<TransactionDto>> {
        return try {
            val response = ApiClient.http.get("/transactions/history/$customerId") {
                header(HttpHeaders.Authorization, authHeader())
            }
            val body = response.body<ApiResponse<List<TransactionDto>>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi lấy lịch sử giao dịch"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRevenueByDay(): Result<List<RevenuePoint>> {
        return try {
            val response = ApiClient.http.get("/analytics/revenue/day") {
                header(HttpHeaders.Authorization, authHeader())
            }
            val body = response.body<ApiResponse<List<RevenuePoint>>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi lấy doanh thu theo ngày"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRevenueByMonth(): Result<List<RevenuePoint>> {
        return try {
            val response = ApiClient.http.get("/analytics/revenue/month") {
                header(HttpHeaders.Authorization, authHeader())
            }
            val body = response.body<ApiResponse<List<RevenuePoint>>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi lấy doanh thu theo tháng"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRevenueByGame(): Result<List<GameRevenue>> {
        return try {
            val response = ApiClient.http.get("/analytics/revenue/game") {
                header(HttpHeaders.Authorization, authHeader())
            }
            val body = response.body<ApiResponse<List<GameRevenue>>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi lấy doanh thu theo game"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
