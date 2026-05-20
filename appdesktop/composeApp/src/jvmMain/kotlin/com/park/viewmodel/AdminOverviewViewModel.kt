package com.park.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.park.data.model.CardChannelDTO
import com.park.data.model.CardLifecycleDTO
import com.park.data.model.HeatmapDTO
import com.park.data.model.HourlyTrendDTO
import com.park.data.model.StatisticsGamesResponseDTO
import com.park.data.model.StatisticsTrendDTO
import com.park.data.repository.StatisticsRepository
import com.park.ui.common.formatCountVi
import com.park.ui.common.formatMoneyVi
import com.park.ui.common.formatSignedPercent
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class AdminOverviewUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastSyncAt: String? = null,
    val today: LocalDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")),

    // MTD daily series
    val mtdLabels: List<String> = emptyList(),
    val mtdRevenue: List<Float> = emptyList(),
    val mtdPlays: List<Float> = emptyList(),
    val mtdPlayers: List<Float> = emptyList(),
    val mtdNewUsers: List<Float> = emptyList(),
    val mtdRevenuePrev: List<Float> = emptyList(),
    val mtdPlaysPrev: List<Float> = emptyList(),
    val mtdPlayersPrev: List<Float> = emptyList(),
    val mtdNewUsersPrev: List<Float> = emptyList(),

    // KPI
    val kRevenue: String = "—",
    val kPlays: String = "—",
    val kNewUsers: String = "—",
    val kArpu: String = "—",
    val revenueChange: String = "",
    val revenueUp: Boolean = true,
    val playsChange: String = "",
    val playsUp: Boolean = true,
    val newUsersChange: String = "",
    val newUsersUp: Boolean = true,
    val arpuChange: String = "",
    val arpuUp: Boolean = true,

    // Overview 12 tháng
    val overviewLabels: List<String> = emptyList(),
    val overviewRevenue: List<Float> = emptyList(),
    val overviewPlays: List<Float> = emptyList(),
    val overviewPlayers: List<Float> = emptyList(),

    // Hourly
    val hourLabels: List<String> = emptyList(),
    val hourPlays: List<Float> = emptyList(),
    val peakHourLabel: String = "—",

    // Heatmap
    val heatmap: HeatmapDTO = HeatmapDTO(),

    // Top games (raw values, scaling done in UI)
    val gameLabels: List<String> = emptyList(),
    val gameRevenue: List<Float> = emptyList(),
    val gamePlays: List<Float> = emptyList(),

    // Channel
    val withApp: Int = 0,
    val noApp: Int = 0,

    // Card lifecycle
    val cardActive: Int = 0,
    val cardBlocked: Int = 0,
    val cardAvailable: Int = 0,
    val cardIssuedThisMonth: Int = 0,
    val cardBlockedThisMonth: Int = 0,
    val cardPendingRequests: Int = 0
)

class AdminOverviewViewModel : ViewModel() {
    private val repo = StatisticsRepository()
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss")
    private var currentJob: Job? = null

    private val _uiState = MutableStateFlow(AdminOverviewUiState())
    val uiState: StateFlow<AdminOverviewUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val today = LocalDate.now(zone)
            val mtdStart = today.withDayOfMonth(1)
            val prevMtdStart = mtdStart.minusMonths(1)
            val prevMtdEnd = prevMtdStart.plusDays((today.dayOfMonth - 1).toLong())
                .coerceAtMost(mtdStart.minusDays(1))
            val yearStart = today.minusMonths(11).withDayOfMonth(1)

            val mtdTrendD     = async { repo.getStatisticsTrend("daily", mtdStart.toString(), today.toString()) }
            val prevMtdTrendD = async { repo.getStatisticsTrend("daily", prevMtdStart.toString(), prevMtdEnd.toString()) }
            val yearTrendD    = async { repo.getStatisticsTrend("monthly", yearStart.toString(), today.toString()) }
            val gamesD        = async { repo.getStatisticsGames(mtdStart.toString(), today.toString()) }
            val hourlyD       = async { repo.getHourlyTrend(mtdStart.toString(), today.toString()) }
            val heatmapD      = async { repo.getHeatmap(4) }
            val channelD      = async { repo.getCardChannel(mtdStart.toString(), today.toString()) }
            val lifecycleD    = async { repo.getCardLifecycle(mtdStart.toString(), today.toString()) }

            val results = listOf(mtdTrendD, prevMtdTrendD, yearTrendD, gamesD, hourlyD, heatmapD, channelD, lifecycleD)
                .map { it.await() }

            val firstError = results.firstNotNullOfOrNull { (it as Result<*>).exceptionOrNull() }
            @Suppress("UNCHECKED_CAST")
            val mtd     = (results[0] as Result<StatisticsTrendDTO>).getOrNull()
            @Suppress("UNCHECKED_CAST")
            val prevMtd = (results[1] as Result<StatisticsTrendDTO>).getOrNull()
            @Suppress("UNCHECKED_CAST")
            val year    = (results[2] as Result<StatisticsTrendDTO>).getOrNull()
            @Suppress("UNCHECKED_CAST")
            val games   = (results[3] as Result<StatisticsGamesResponseDTO>).getOrNull()
            @Suppress("UNCHECKED_CAST")
            val hr      = (results[4] as Result<HourlyTrendDTO>).getOrNull()
            @Suppress("UNCHECKED_CAST")
            val hm      = (results[5] as Result<HeatmapDTO>).getOrNull()
            @Suppress("UNCHECKED_CAST")
            val ch      = (results[6] as Result<CardChannelDTO>).getOrNull()
            @Suppress("UNCHECKED_CAST")
            val life    = (results[7] as Result<CardLifecycleDTO>).getOrNull()

            val totalRev       = mtd?.totalRevenue ?: 0.0
            val totalRevPrev   = prevMtd?.totalRevenue ?: 0.0
            val totalPlays     = mtd?.totalPlays ?: 0
            val totalPlaysPrev = prevMtd?.totalPlays ?: 0
            val newUsers       = mtd?.newUserCount ?: 0
            val newUsersPrev   = prevMtd?.newUserCount ?: 0
            val arpuCur = if (totalPlays > 0) totalRev / totalPlays else 0.0
            val arpuPrev = if (totalPlaysPrev > 0) totalRevPrev / totalPlaysPrev else 0.0

            val topGames = games?.items.orEmpty().sortedByDescending { it.revenue }.take(7)

            _uiState.update {
                AdminOverviewUiState(
                    isLoading = false,
                    errorMessage = firstError?.message,
                    lastSyncAt = LocalTime.now(zone).format(timeFmt),
                    today = today,
                    mtdLabels = mtd?.labels.orEmpty(),
                    mtdRevenue = mtd?.revenueValues?.map { it.toFloat() }.orEmpty(),
                    mtdPlays = mtd?.playValues?.map { it.toFloat() }.orEmpty(),
                    mtdPlayers = mtd?.playerValues?.map { it.toFloat() }.orEmpty(),
                    mtdNewUsers = mtd?.newUserValues?.map { it.toFloat() }.orEmpty(),
                    mtdRevenuePrev = prevMtd?.revenueValues?.map { it.toFloat() }.orEmpty(),
                    mtdPlaysPrev = prevMtd?.playValues?.map { it.toFloat() }.orEmpty(),
                    mtdPlayersPrev = prevMtd?.playerValues?.map { it.toFloat() }.orEmpty(),
                    mtdNewUsersPrev = prevMtd?.newUserValues?.map { it.toFloat() }.orEmpty(),
                    kRevenue = formatMoneyVi(totalRev),
                    kPlays = formatCountVi(totalPlays.toLong()),
                    kNewUsers = formatCountVi(newUsers.toLong()),
                    kArpu = if (totalPlays > 0) formatMoneyVi(arpuCur) else "—",
                    revenueChange = pctChange(totalRev, totalRevPrev).first,
                    revenueUp = pctChange(totalRev, totalRevPrev).second,
                    playsChange = pctChange(totalPlays.toDouble(), totalPlaysPrev.toDouble()).first,
                    playsUp = pctChange(totalPlays.toDouble(), totalPlaysPrev.toDouble()).second,
                    newUsersChange = pctChange(newUsers.toDouble(), newUsersPrev.toDouble()).first,
                    newUsersUp = pctChange(newUsers.toDouble(), newUsersPrev.toDouble()).second,
                    arpuChange = pctChange(arpuCur, arpuPrev).first,
                    arpuUp = pctChange(arpuCur, arpuPrev).second,
                    overviewLabels = year?.labels.orEmpty(),
                    overviewRevenue = year?.revenueValues?.map { it.toFloat() }.orEmpty(),
                    overviewPlays = year?.playValues?.map { it.toFloat() }.orEmpty(),
                    overviewPlayers = year?.playerValues?.map { it.toFloat() }.orEmpty(),
                    hourLabels = hr?.labels.orEmpty(),
                    hourPlays = hr?.playValues?.map { it.toFloat() }.orEmpty(),
                    peakHourLabel = hr?.peakHourLabel ?: "—",
                    heatmap = hm ?: HeatmapDTO(),
                    gameLabels = topGames.map { it.name },
                    gameRevenue = topGames.map { it.revenue.toFloat() },
                    gamePlays = topGames.map { it.plays.toFloat() },
                    withApp = ch?.viaApp ?: 0,
                    noApp = ch?.viaCounter ?: 0,
                    cardActive = games?.cardStatus?.active ?: 0,
                    cardBlocked = games?.cardStatus?.blocked ?: 0,
                    cardAvailable = games?.cardStatus?.available ?: 0,
                    cardIssuedThisMonth = life?.issuedThisMonth ?: 0,
                    cardBlockedThisMonth = life?.blockedThisMonth ?: 0,
                    cardPendingRequests = life?.pendingRequests ?: 0
                )
            }
        }
    }

    private fun pctChange(cur: Double, prev: Double): Pair<String, Boolean> {
        if (prev <= 0.0) return "" to true
        val delta = (cur - prev) / prev * 100.0
        return formatSignedPercent(delta) to (delta >= 0)
    }
}
