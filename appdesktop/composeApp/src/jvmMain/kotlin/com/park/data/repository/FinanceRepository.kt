package com.park.data.repository

import com.park.data.model.*
import com.park.data.network.ApiClient
import com.park.data.network.apiCall
import io.ktor.client.request.*

class FinanceRepository {

    suspend fun getTransactions(page: Int = 1, size: Int = 50): Result<PaginatedData<TransactionDTO>> =
        apiCall("Lỗi lấy giao dịch") {
            ApiClient.http.get("/api/admin/transactions") {
                parameter("page", page)
                parameter("size", size)
            }
        }
}
