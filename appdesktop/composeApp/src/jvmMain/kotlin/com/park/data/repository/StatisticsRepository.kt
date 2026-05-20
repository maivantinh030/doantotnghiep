package com.park.data.repository

import com.park.data.model.*
import com.park.data.network.ApiClient
import com.park.data.network.apiCall
import io.ktor.client.request.*

class StatisticsRepository {

    suspend fun getStatisticsFilters(): Result<StatisticsFiltersDTO> =
        apiCall("Lỗi tải bộ lọc thống kê") {
            ApiClient.http.get("/api/admin/statistics/filters")
        }

    suspend fun getStatisticsTrend(
        period: String,
        startDate: String? = null,
        endDate: String? = null,
        game: String? = null,
        area: String? = null,
        status: String? = null
    ): Result<StatisticsTrendDTO> = apiCall("Lỗi tải biểu đồ xu hướng") {
        ApiClient.http.get("/api/admin/statistics/trend") {
            parameter("period", period)
            if (!startDate.isNullOrBlank()) parameter("startDate", startDate)
            if (!endDate.isNullOrBlank()) parameter("endDate", endDate)
            if (!game.isNullOrBlank()) parameter("game", game)
            if (!area.isNullOrBlank()) parameter("area", area)
            if (!status.isNullOrBlank()) parameter("status", status)
        }
    }

    suspend fun getStatisticsGames(
        startDate: String? = null,
        endDate: String? = null,
        game: String? = null,
        area: String? = null,
        status: String? = null,
        search: String? = null
    ): Result<StatisticsGamesResponseDTO> = apiCall("Lỗi tải thống kê theo game") {
        ApiClient.http.get("/api/admin/statistics/games") {
            if (!startDate.isNullOrBlank()) parameter("startDate", startDate)
            if (!endDate.isNullOrBlank()) parameter("endDate", endDate)
            if (!game.isNullOrBlank()) parameter("game", game)
            if (!area.isNullOrBlank()) parameter("area", area)
            if (!status.isNullOrBlank()) parameter("status", status)
            if (!search.isNullOrBlank()) parameter("search", search)
        }
    }

    suspend fun getStatisticsTable(
        page: Int = 1,
        size: Int = 10,
        startDate: String? = null,
        endDate: String? = null,
        game: String? = null,
        area: String? = null,
        status: String? = null,
        search: String? = null
    ): Result<StatisticsTableResponseDTO> = apiCall("Lỗi tải bảng thống kê") {
        ApiClient.http.get("/api/admin/statistics/table") {
            parameter("page", page)
            parameter("size", size)
            if (!startDate.isNullOrBlank()) parameter("startDate", startDate)
            if (!endDate.isNullOrBlank()) parameter("endDate", endDate)
            if (!game.isNullOrBlank()) parameter("game", game)
            if (!area.isNullOrBlank()) parameter("area", area)
            if (!status.isNullOrBlank()) parameter("status", status)
            if (!search.isNullOrBlank()) parameter("search", search)
        }
    }

    suspend fun getHourlyTrend(
        startDate: String? = null,
        endDate: String? = null,
        game: String? = null,
        area: String? = null,
        status: String? = null
    ): Result<HourlyTrendDTO> = apiCall("Lỗi tải hourly") {
        ApiClient.http.get("/api/admin/statistics/hourly") {
            if (!startDate.isNullOrBlank()) parameter("startDate", startDate)
            if (!endDate.isNullOrBlank()) parameter("endDate", endDate)
            if (!game.isNullOrBlank()) parameter("game", game)
            if (!area.isNullOrBlank()) parameter("area", area)
            if (!status.isNullOrBlank()) parameter("status", status)
        }
    }

    suspend fun getDowTrend(
        startDate: String? = null,
        endDate: String? = null,
        gameId: String? = null
    ): Result<DowTrendDTO> = apiCall("Lỗi tải dow") {
        ApiClient.http.get("/api/admin/statistics/dow") {
            if (!startDate.isNullOrBlank()) parameter("startDate", startDate)
            if (!endDate.isNullOrBlank()) parameter("endDate", endDate)
            if (!gameId.isNullOrBlank()) parameter("gameId", gameId)
        }
    }

    suspend fun getHeatmap(weeks: Int = 4): Result<HeatmapDTO> =
        apiCall("Lỗi tải heatmap") {
            ApiClient.http.get("/api/admin/statistics/heatmap") {
                parameter("weeks", weeks)
            }
        }

    suspend fun getCardChannel(
        startDate: String? = null,
        endDate: String? = null
    ): Result<CardChannelDTO> = apiCall("Lỗi tải channel") {
        ApiClient.http.get("/api/admin/statistics/card-channel") {
            if (!startDate.isNullOrBlank()) parameter("startDate", startDate)
            if (!endDate.isNullOrBlank()) parameter("endDate", endDate)
        }
    }

    suspend fun getCardLifecycle(
        startDate: String? = null,
        endDate: String? = null
    ): Result<CardLifecycleDTO> = apiCall("Lỗi tải card lifecycle") {
        ApiClient.http.get("/api/admin/statistics/card-lifecycle") {
            if (!startDate.isNullOrBlank()) parameter("startDate", startDate)
            if (!endDate.isNullOrBlank()) parameter("endDate", endDate)
        }
    }

    suspend fun getGameDetail(gameId: String, days: Int = 90): Result<GameDetailDTO> =
        apiCall("Lỗi tải game detail") {
            ApiClient.http.get("/api/admin/statistics/games/$gameId/detail") {
                parameter("days", days)
            }
        }
}
