package com.park.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.park.data.model.StatisticsCardStatusDTO
import com.park.data.model.StatisticsGameItemDTO
import com.park.data.repository.UserRepository
import com.park.ui.screen.DashboardQuickRange
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DashboardUiState(
    val isLoading: Boolean = false,
    val quickRange: DashboardQuickRange = DashboardQuickRange.LAST_7_DAYS,
    val revenueLabels: List<String> = emptyList(),
    val revenueValues: List<Float> = emptyList(),
    val playerLabels: List<String> = emptyList(),
    val playerValues: List<Float> = emptyList(),
    val totalPlayers: Int = 0,
    val totalRevenue: Double = 0.0,
    val ticketsSold: Int = 0,
    val activeRides: Int = 0,
    val totalRides: Int = 0,
    val playersDelta: String = "Theo bộ lọc hiện tại",
    val revenueDelta: String = "Theo bộ lọc hiện tại",
    val ticketsDelta: String = "Theo bộ lọc hiện tại",
    val ridesDelta: String = "Theo bộ lọc hiện tại",
    val gameItems: List<StatisticsGameItemDTO> = emptyList(),
    val topGameItems: List<StatisticsGameItemDTO> = emptyList(),
    val warnings: List<String> = emptyList(),
    val cardStatus: StatisticsCardStatusDTO = StatisticsCardStatusDTO(),
    val lastSyncAt: String? = null,
    val errorMessage: String? = null
)

class DashboardViewModel : ViewModel() {
    private val userRepo = UserRepository()
    private val zoneId = ZoneId.of("Asia/Ho_Chi_Minh")
    private val dateFormatter = DateTimeFormatter.ISO_DATE
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadDashboard(DashboardQuickRange.LAST_7_DAYS)
    }

    fun selectQuickRange(range: DashboardQuickRange) {
        if (range == _uiState.value.quickRange) return
        loadDashboard(range)
    }

    fun refresh() {
        loadDashboard(_uiState.value.quickRange)
    }

    private fun loadDashboard(range: DashboardQuickRange) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, quickRange = range, errorMessage = null) }

            val query = resolveRangeQuery(range)

            val trendDeferred = async {
                userRepo.getStatisticsTrend(
                    period = query.period,
                    startDate = query.startDate,
                    endDate = query.endDate,
                    game = null,
                    area = null,
                    status = null
                )
            }
            val gamesDeferred = async {
                userRepo.getStatisticsGames(
                    startDate = query.startDate,
                    endDate = query.endDate,
                    game = null,
                    area = null,
                    status = null,
                    search = null
                )
            }

            val trendResult = trendDeferred.await()
            val gamesResult = gamesDeferred.await()

            var errorMessage: String? = null
            val trend = trendResult.getOrElse {
                errorMessage = it.message ?: "Không thể tải dữ liệu xu hướng dashboard"
                null
            }
            val games = gamesResult.getOrElse {
                errorMessage = errorMessage ?: (it.message ?: "Không thể tải dữ liệu tổng quan dashboard")
                null
            }

            val revenueValuesDouble = trend?.revenueValues.orEmpty()
            val playerValuesInt = trend?.playerValues.orEmpty()
            val gameItems = games?.items.orEmpty()

            val computedWarnings = buildWarnings(
                revenueValues = revenueValuesDouble,
                games = gameItems
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    quickRange = range,
                    revenueLabels = trend?.labels.orEmpty(),
                    revenueValues = revenueValuesDouble.map { value -> value.toFloat() },
                    playerLabels = trend?.labels.orEmpty(),
                    playerValues = playerValuesInt.map { value -> value.toFloat() },
                    totalPlayers = games?.summary?.totalPlayers ?: trend?.totalPlayers ?: 0,
                    totalRevenue = games?.summary?.totalRevenue ?: trend?.totalRevenue ?: 0.0,
                    ticketsSold = games?.summary?.totalPlays ?: 0,
                    activeRides = gameItems.count { row -> row.status.uppercase() == "ACTIVE" },
                    totalRides = games?.summary?.totalGames ?: gameItems.size,
                    playersDelta = "Theo bộ lọc hiện tại",
                    revenueDelta = "Theo bộ lọc hiện tại",
                    ticketsDelta = "Theo bộ lọc hiện tại",
                    ridesDelta = "Theo bộ lọc hiện tại",
                    gameItems = gameItems,
                    topGameItems = gameItems
                        .filter { row -> row.status.uppercase() == "ACTIVE" }
                        .sortedByDescending { it.players }
                        .take(3),
                    warnings = computedWarnings,
                    cardStatus = games?.cardStatus ?: StatisticsCardStatusDTO(),
                    lastSyncAt = if (trend != null || games != null) LocalTime.now().format(timeFormatter) else it.lastSyncAt,
                    errorMessage = errorMessage
                )
            }
        }
    }

    private fun buildWarnings(
        revenueValues: List<Double>,
        games: List<StatisticsGameItemDTO>
    ): List<String> {
        val warnings = mutableListOf<String>()

        if (revenueValues.size >= 2) {
            val previous = revenueValues[revenueValues.lastIndex - 1]
            val current = revenueValues.last()
            if (previous > 0.0) {
                val delta = ((current - previous) / previous) * 100.0
                val pctText = String.format(Locale.US, "%.1f", kotlin.math.abs(delta))
                if (delta < 0) {
                    warnings += "Doanh thu kỳ gần nhất giảm $pctText% so với kỳ trước."
                } else {
                    warnings += "Doanh thu kỳ gần nhất tăng $pctText% so với kỳ trước."
                }
            }
        }

        games.filter { it.status.uppercase() == "ACTIVE" }.minByOrNull { it.players }?.let { lowGame ->
            warnings += "${lowGame.name} đang có lượt người chơi thấp nhất (${lowGame.players})."
        }

        val pausedCount = games.count {
            val status = it.status.uppercase()
            status == "MAINTENANCE" || status == "INACTIVE" || status == "CLOSED" || status == "STOPPED"
        }
        if (pausedCount > 0) {
            warnings += "$pausedCount trò chơi đang bảo trì hoặc tạm dừng."
        }

        if (warnings.isEmpty()) {
            warnings += "Không có cảnh báo quan trọng trong phạm vi dữ liệu hiện tại."
        }

        return warnings.take(3)
    }


    private data class RangeQuery(
        val period: String,
        val startDate: String,
        val endDate: String
    )

    private fun resolveRangeQuery(range: DashboardQuickRange): RangeQuery {
        val today = LocalDate.now(zoneId)
        return when (range) {
            DashboardQuickRange.TODAY -> RangeQuery(
                period = "daily",
                startDate = dateFormatter.format(today),
                endDate = dateFormatter.format(today)
            )

            DashboardQuickRange.LAST_7_DAYS -> RangeQuery(
                period = "daily",
                startDate = dateFormatter.format(today.minusDays(6)),
                endDate = dateFormatter.format(today)
            )

            DashboardQuickRange.LAST_30_DAYS -> RangeQuery(
                period = "weekly",
                startDate = dateFormatter.format(today.minusDays(29)),
                endDate = dateFormatter.format(today)
            )
        }
    }
}