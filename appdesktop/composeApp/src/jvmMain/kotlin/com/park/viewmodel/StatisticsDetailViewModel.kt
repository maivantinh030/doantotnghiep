package com.park.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.park.data.model.GameDetailDTO
import com.park.data.repository.StatisticsRepository
import com.park.ui.common.TimeRange
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class GameLite(
    val id: String,
    val name: String,
    val category: String,
    val color: Color,
    val ticketPriceK: Int     // VND k = pricePerTurn / 1000
)

data class StatisticsDetailUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val games: List<GameLite> = emptyList(),
    val gameDetails: Map<String, GameDetailDTO> = emptyMap(),
    val range: TimeRange = TimeRange.Last30Days,
    val selectedGameId: String? = null,
    val today: LocalDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))
)

class StatisticsDetailViewModel : ViewModel() {
    private val repo = StatisticsRepository()
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val gameColors = listOf(
        Color(0xFF378ADD), Color(0xFF7F77DD), Color(0xFF1D9E75),
        Color(0xFFD85A30), Color(0xFFEF9F27), Color(0xFF0F6E56), Color(0xFF993556),
    )
    private var currentJob: Job? = null
    private var refetchJob: Job? = null

    /** Số ngày daily series đang cache cho mỗi game. Tăng dynamically khi user pick range xa hơn. */
    private var cachedDays: Int = 90

    private val _uiState = MutableStateFlow(StatisticsDetailUiState())
    val uiState: StateFlow<StatisticsDetailUiState> = _uiState.asStateFlow()

    init { loadInitial() }

    fun setRange(r: TimeRange) {
        // Refresh today mỗi lần đổi range để tránh stale (app chạy lâu, qua nửa đêm).
        val today = LocalDate.now(zone)
        _uiState.update { it.copy(range = r, today = today) }
        val needDays = daysNeededFor(r, today)
        // - Custom range (user vừa "Áp dụng" picker): LUÔN refetch để chắc chắn data tươi.
        // - Preset: chỉ refetch khi cache hiện không đủ phủ range.
        val shouldRefetch = (r is TimeRange.Custom) || (needDays > cachedDays)
        if (shouldRefetch) {
            refetchAllDetails(needDays)
        }
    }

    fun selectGame(id: String?) {
        _uiState.update { it.copy(selectedGameId = id) }
    }

    fun refresh() = loadInitial()

    /** Tính số ngày tối thiểu cần để cover `range` tính ngược từ today. Cap 365. */
    private fun daysNeededFor(range: TimeRange, today: LocalDate): Int {
        val from = range.fromDate(today)
        return (ChronoUnit.DAYS.between(from, today).toInt() + 1)
            .coerceAtLeast(7)
            .coerceAtMost(365)
    }

    private fun refetchAllDetails(days: Int) {
        refetchJob?.cancel()
        val games = _uiState.value.games
        if (games.isEmpty()) return
        refetchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val today = LocalDate.now(zone)
            val details = games.map { g ->
                async { g.id to repo.getGameDetail(g.id, days).getOrNull() }
            }.awaitAll().mapNotNull { (id, dto) -> dto?.let { id to it } }.toMap()
            cachedDays = days
            _uiState.update {
                it.copy(
                    isLoading = false,
                    today = today,            // sync today với thời điểm fetch
                    gameDetails = details,
                    errorMessage = if (details.size < games.size)
                        "Một số trò chơi chưa tải được dữ liệu chi tiết cho kỳ này" else null
                )
            }
        }
    }

    private fun loadInitial() {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val today = LocalDate.now(zone)
            // Truyền 1 năm để KHÔNG bị backend default 30 ngày bỏ sót game.
            val from = today.minusYears(1).toString()
            val to = today.toString()
            val gamesResult = repo.getStatisticsGames(startDate = from, endDate = to)

            val items = gamesResult.getOrNull()?.items.orEmpty()
            if (items.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        today = today,
                        errorMessage = gamesResult.exceptionOrNull()?.message
                            ?: "Không có dữ liệu trò chơi"
                    )
                }
                return@launch
            }

            val lite = items.mapIndexed { idx, g ->
                GameLite(
                    id = g.gameId,
                    name = g.name,
                    category = g.category ?: g.area ?: "Khác",
                    color = gameColors[idx % gameColors.size],
                    ticketPriceK = (g.ticketPrice.toInt() / 1000).coerceAtLeast(0)
                )
            }

            // Fetch detail song song
            val details = lite.map { g ->
                async { g.id to repo.getGameDetail(g.id, 90).getOrNull() }
            }.awaitAll().mapNotNull { (id, dto) -> dto?.let { id to it } }.toMap()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    games = lite,
                    gameDetails = details,
                    today = today,
                    errorMessage = if (details.size < lite.size)
                        "Một số trò chơi chưa tải được dữ liệu chi tiết" else null
                )
            }
        }
    }
}
