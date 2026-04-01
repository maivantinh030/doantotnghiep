package com.park.data.repository

import com.park.data.model.*
import com.park.data.network.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*



class UserRepository {

    private fun authHeader() = "Bearer ${ApiClient.getToken()}"

    suspend fun getDashboardStats(): Result<DashboardStats> {
        return try {
            val response = ApiClient.http.get("/api/admin/dashboard/stats") {
                header(HttpHeaders.Authorization, authHeader())
            }
            val body = response.body<ApiResponse<DashboardStats>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi lấy thống kê"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsers(page: Int = 1, size: Int = 20, search: String? = null): Result<PaginatedData<UserDTO>> {
        return try {
            val response = ApiClient.http.get("/api/admin/users") {
                header(HttpHeaders.Authorization, authHeader())
                parameter("page", page)
                parameter("size", size)
                if (!search.isNullOrBlank()) parameter("search", search)
            }
            val body = response.body<ApiResponse<PaginatedData<UserDTO>>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi lấy danh sách người dùng"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: String): Result<UserDTO> {
        return try {
            val response = ApiClient.http.get("/api/admin/users/$userId") {
                header(HttpHeaders.Authorization, authHeader())
            }
            val body = response.body<ApiResponse<UserDTO>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi lấy thông tin người dùng"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun lockUser(userId: String): Result<Unit> {
        return try {
            val response = ApiClient.http.post("/api/admin/users/$userId/lock") {
                header(HttpHeaders.Authorization, authHeader())
            }
            val body = response.body<ApiResponse<Unit>>()
            if (body.success) Result.success(Unit)
            else Result.failure(Exception(body.message ?: "Lỗi khóa tài khoản"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unlockUser(userId: String): Result<Unit> {
        return try {
            val response = ApiClient.http.post("/api/admin/users/$userId/unlock") {
                header(HttpHeaders.Authorization, authHeader())
            }
            val body = response.body<ApiResponse<Unit>>()
            if (body.success) Result.success(Unit)
            else Result.failure(Exception(body.message ?: "Lỗi mở khóa tài khoản"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adjustBalance(userId: String, amount: Double, reason: String): Result<Unit> {
        return try {
            val response = ApiClient.http.post("/api/admin/users/$userId/adjust-balance") {
                header(HttpHeaders.Authorization, authHeader())
                contentType(ContentType.Application.Json)
                setBody(AdjustBalanceRequest(amount = amount, reason = reason))
            }
            val body = response.body<ApiResponse<Unit>>()
            if (body.success) Result.success(Unit)
            else Result.failure(Exception(body.message ?: "Lỗi điều chỉnh số dư"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactions(page: Int = 1, size: Int = 50): Result<PaginatedData<TransactionDTO>> {
        return try {
            val response = ApiClient.http.get("/api/admin/transactions") {
                header(HttpHeaders.Authorization, authHeader())
                parameter("page", page)
                parameter("size", size)
            }
            val body = response.body<ApiResponse<PaginatedData<TransactionDTO>>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi lấy giao dịch"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRevenueChart(period: String): Result<RevenueChartData> {
        return try {
            val response = ApiClient.http.get("/api/admin/revenue/chart") {
                header(HttpHeaders.Authorization, authHeader())
                parameter("period", period)
            }
            val body = response.body<ApiResponse<RevenueChartData>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi lấy biểu đồ"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
