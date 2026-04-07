package com.park.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.park.data.model.StatisticsCardStatusDTO
import com.park.data.model.StatisticsGameItemDTO
import com.park.data.model.StatisticsSummaryDTO
import com.park.data.model.StatisticsTopItemDTO
import com.park.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek
import kotlin.collections.distinct
import kotlin.collections.filter
import kotlin.collections.map
import kotlin.collections.plus
import kotlin.collections.sorted
import kotlin.fold
import kotlin.takeIf
import kotlin.takeUnless
import kotlin.text.isNotBlank
import kotlin.text.trim
import kotlin.to

enum class StatisticsPeriod(val apiValue: String, val label: String) {
    DAY("daily", "Ngày"),
    WEEK("weekly", "Tuần"),
    MONTH("monthly", "Tháng")
}

data class StatisticsUiState(
    val isFilterLoading: Boolean = false,
    val isTrendLoading: Boolean = false,
    val isGamesLoading: Boolean = false,
    val isTableLoading: Boolean = false,
    val dateOptions: List<String> = StatisticsViewModel.DATE_OPTIONS,
    val selectedDateRange: String = StatisticsViewModel.DEFAULT_DATE_RANGE,
    val selectedGame: String = StatisticsViewModel.ALL_GAMES_LABEL,
    val selectedArea: String = StatisticsViewModel.ALL_AREAS_LABEL,
    val selectedPeriod: StatisticsPeriod = StatisticsPeriod.DAY,
    val tableSearchQuery: String = "",
    val gameOptions: List<String> = listOf(StatisticsViewModel.ALL_GAMES_LABEL),
    val areaOptions: List<String> = listOf(StatisticsViewModel.ALL_AREAS_LABEL),
    val trendLabels: List<String> = emptyList(),
    val trendRevenueValues: List<Float> = emptyList(),
    val trendPlayerValues: List<Float> = emptyList(),
    val hasLoadedTrend: Boolean = false,
    val games: List<StatisticsGameItemDTO> = emptyList(),
    val topRevenue: List<StatisticsTopItemDTO> = emptyList(),
    val lowPlayers: List<StatisticsTopItemDTO> = emptyList(),
    val cardStatus: StatisticsCardStatusDTO = StatisticsCardStatusDTO(),
    val hasLoadedGames: Boolean = false,
    val summary: StatisticsSummaryDTO = StatisticsSummaryDTO(),
    val tableItems: List<StatisticsGameItemDTO> = emptyList(),
    val tablePage: Int = 1,
    val tableSize: Int = 5,
    val tableTotal: Long = 0,
    val tableTotalPages: Long = 0,
    val hasLoadedTable: Boolean = false,
    val lastSyncAt: String? = null,
    val errorMessage: String? = null
)

class StatisticsViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val zoneId = ZoneId.of("Asia/Ho_Chi_Minh")
    private val dateFormatter = DateTimeFormatter.ISO_DATE
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState

    init {
        loadFilterOptions()
        applyFilters(resetTablePage = true)
    }

    fun refresh() {
        loadFilterOptions()
        applyFilters(resetTablePage = true)
    }

    fun onDateRangeChange(value: String) {
        _uiState.update { it.copy(selectedDateRange = value) }
    }

    fun onGameChange(value: String) {
        _uiState.update { it.copy(selectedGame = value) }
    }

    fun onAreaChange(value: String) {
        _uiState.update { it.copy(selectedArea = value) }
    }

    fun onGroupingChange(period: StatisticsPeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
        loadTrend()
    }

    fun onTableSearchChange(value: String) {
        _uiState.update { it.copy(tableSearchQuery = value) }
        loadTable(page = 1)
    }

    fun applyFilters(resetTablePage: Boolean = true) {
        loadTrend()
        loadGames()
        loadTable(page = if (resetTablePage) 1 else _uiState.value.tablePage)
    }

    fun nextTablePage() {
        val state = _uiState.value
        if (state.tablePage < state.tableTotalPages) {
            loadTable(page = state.tablePage + 1)
        }
    }

    fun previousTablePage() {
        val state = _uiState.value
        if (state.tablePage > 1) {
            loadTable(page = state.tablePage - 1)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun loadFilterOptions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isFilterLoading = true, errorMessage = null) }

            userRepository.getStatisticsFilters().fold(
                onSuccess = { filters ->
                    val games = listOf(ALL_GAMES_LABEL) +
                        filters.games.map { it.name.trim() }.filter { it.isNotBlank() }.distinct().sorted()
                    val areas = listOf(ALL_AREAS_LABEL) + filters.areas.distinct().sorted()

                    _uiState.update { state ->
                        state.copy(
                            isFilterLoading = false,
                            gameOptions = games,
                            areaOptions = areas,
                            selectedGame = state.selectedGame.takeIf { it in games } ?: ALL_GAMES_LABEL,
                            selectedArea = state.selectedArea.takeIf { it in areas } ?: ALL_AREAS_LABEL,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isFilterLoading = false,
                            errorMessage = error.message ?: "Không thể tải bộ lọc thống kê"
                        )
                    }
                }
            )
        }
    }

    private fun loadTrend() {
        viewModelScope.launch {
            val state = _uiState.value
            val (startDate, endDate) = resolveTrendDateRange(state.selectedPeriod)
            val game = state.selectedGame.takeUnless { it == ALL_GAMES_LABEL }
            val area = state.selectedArea.takeUnless { it == ALL_AREAS_LABEL }

            _uiState.update { it.copy(isTrendLoading = true, errorMessage = null) }

            userRepository.getStatisticsTrend(
                period = state.selectedPeriod.apiValue,
                startDate = startDate,
                endDate = endDate,
                game = game,
                area = area,
                status = null
            ).fold(
                onSuccess = { trend ->
                    _uiState.update {
                        it.copy(
                            isTrendLoading = false,
                            trendLabels = trend.labels,
                            trendRevenueValues = trend.revenueValues.map { value -> value.toFloat() },
                            trendPlayerValues = trend.playerValues.map { value -> value.toFloat() },
                            hasLoadedTrend = true,
                            lastSyncAt = LocalTime.now().format(timeFormatter)
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isTrendLoading = false,
                            errorMessage = error.message ?: "Không thể tải dữ liệu xu hướng"
                        )
                    }
                }
            )
        }
    }

    private fun resolveTrendDateRange(period: StatisticsPeriod): Pair<String, String> {
        val today = LocalDate.now(zoneId)
        val (start, end) = when (period) {
            StatisticsPeriod.DAY -> {
                today.minusDays(6) to today
            }

            StatisticsPeriod.WEEK -> {
                val thisMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                thisMonday.minusWeeks(3) to today
            }

            StatisticsPeriod.MONTH -> {
                val year = today.year
                LocalDate.of(year, 1, 1) to LocalDate.of(year, 12, 31)
            }
        }
        return dateFormatter.format(start) to dateFormatter.format(end)
    }

    private fun loadGames() {
        viewModelScope.launch {
            val state = _uiState.value
            val (startDate, endDate) = resolveDateRange(state.selectedDateRange)
            val area = state.selectedArea.takeUnless { it == ALL_AREAS_LABEL }

            _uiState.update { it.copy(isGamesLoading = true, errorMessage = null) }

            userRepository.getStatisticsGames(
                startDate = startDate,
                endDate = endDate,
                // Keep comparison insights meaningful: always compare across all games.
                game = null,
                area = area,
                status = null,
                search = null
            ).fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isGamesLoading = false,
                            games = response.items,
                            topRevenue = response.topRevenue,
                            lowPlayers = response.lowPlayers,
                            cardStatus = response.cardStatus,
                            hasLoadedGames = true,
                            summary = response.summary,
                            lastSyncAt = LocalTime.now().format(timeFormatter)
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isGamesLoading = false,
                            errorMessage = error.message ?: "Không thể tải thống kê theo trò chơi"
                        )
                    }
                }
            )
        }
    }

    private fun loadTable(page: Int) {
        viewModelScope.launch {
            val state = _uiState.value
            val (startDate, endDate) = resolveDateRange(state.selectedDateRange)
            val game = state.selectedGame.takeUnless { it == ALL_GAMES_LABEL }
            val area = state.selectedArea.takeUnless { it == ALL_AREAS_LABEL }
            val search = state.tableSearchQuery.takeIf { it.isNotBlank() }

            _uiState.update { it.copy(isTableLoading = true, errorMessage = null) }

            userRepository.getStatisticsTable(
                page = page,
                size = state.tableSize,
                startDate = startDate,
                endDate = endDate,
                game = game,
                area = area,
                status = null,
                search = search
            ).fold(
                onSuccess = { table ->
                    _uiState.update {
                        it.copy(
                            isTableLoading = false,
                            tableItems = table.items,
                            tablePage = table.page,
                            tableSize = table.size,
                            tableTotal = table.total,
                            tableTotalPages = table.totalPages,
                            hasLoadedTable = true,
                            summary = table.summary,
                            lastSyncAt = LocalTime.now().format(timeFormatter)
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isTableLoading = false,
                            errorMessage = error.message ?: "Không thể tải bảng thống kê"
                        )
                    }
                }
            )
        }
    }

    private fun resolveDateRange(option: String): Pair<String, String> {
        val today = LocalDate.now(zoneId)
        val start = when (option) {
            DATE_RANGE_TODAY -> today
            DATE_RANGE_LAST_30_DAYS -> today.minusDays(29)
            DATE_RANGE_THIS_QUARTER -> {
                val quarterStartMonth = ((today.monthValue - 1) / 3) * 3 + 1
                LocalDate.of(today.year, quarterStartMonth, 1)
            }

            else -> today.minusDays(6)
        }
        return dateFormatter.format(start) to dateFormatter.format(today)
    }

    companion object {
        const val ALL_GAMES_LABEL = "Tất cả trò chơi"
        const val ALL_AREAS_LABEL = "Tất cả khu vực"

        const val DATE_RANGE_TODAY = "Hôm nay"
        const val DATE_RANGE_LAST_7_DAYS = "7 ngày gần nhất"
        const val DATE_RANGE_LAST_30_DAYS = "30 ngày gần nhất"
        const val DATE_RANGE_THIS_QUARTER = "Quý này"
        const val DEFAULT_DATE_RANGE = DATE_RANGE_LAST_7_DAYS

        val DATE_OPTIONS = listOf(
            DATE_RANGE_TODAY,
            DATE_RANGE_LAST_7_DAYS,
            DATE_RANGE_LAST_30_DAYS,
            DATE_RANGE_THIS_QUARTER
        )
    }
}
