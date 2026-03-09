package com.park.data.repository

import com.park.data.model.*
import com.park.data.network.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class OrderRepository {

    private fun authHeader() = "Bearer ${ApiClient.getToken()}"

    suspend fun getOrders(page: Int = 1, size: Int = 20): Result<PaginatedData<OrderDTO>> {
        return try {
            val response = ApiClient.http.get("/api/admin/orders") {
                header(HttpHeaders.Authorization, authHeader())
                parameter("page", page)
                parameter("size", size)
            }
            val body = response.body<ApiResponse<PaginatedData<OrderDTO>>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi lấy danh sách đơn hàng"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRevenueChart(period: String = "daily"): Result<RevenueChartData> {
        return try {
            val response = ApiClient.http.get("/api/admin/revenue/chart") {
                header(HttpHeaders.Authorization, authHeader())
                parameter("period", period)
            }
            val body = response.body<ApiResponse<RevenueChartData>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi lấy dữ liệu biểu đồ"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactions(page: Int = 1, size: Int = 20): Result<PaginatedData<TransactionDTO>> {
        return try {
            val response = ApiClient.http.get("/api/admin/transactions") {
                header(HttpHeaders.Authorization, authHeader())
                parameter("page", page)
                parameter("size", size)
            }
            val body = response.body<ApiResponse<PaginatedData<TransactionDTO>>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi lấy lịch sử giao dịch"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
