package com.park.data.repository

import com.park.data.model.*
import com.park.data.network.ApiClient
import com.park.data.network.apiCall
import io.ktor.client.request.*

class DashboardRepository {

    suspend fun getDashboardStats(): Result<DashboardStats> =
        apiCall("Lỗi lấy thống kê") {
            ApiClient.http.get("/api/admin/dashboard/stats")
        }

    suspend fun getRevenueChart(period: String): Result<RevenueChartData> =
        apiCall("Lỗi lấy biểu đồ") {
            ApiClient.http.get("/api/admin/revenue/chart") {
                parameter("period", period)
            }
        }
}
