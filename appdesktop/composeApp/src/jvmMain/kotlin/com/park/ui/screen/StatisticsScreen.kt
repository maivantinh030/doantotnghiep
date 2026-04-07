package com.park.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.park.data.model.StatisticsGameItemDTO
import com.park.viewmodel.StatisticsPeriod
import com.park.viewmodel.StatisticsViewModel
import kotlin.math.roundToInt

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = viewModel { StatisticsViewModel() }
) {
    val apiState = viewModel.uiState.collectAsStateWithLifecycle().value

    val grouping = apiState.selectedPeriod.toGrouping()
    val gameOptions = if (apiState.gameOptions.isNotEmpty()) apiState.gameOptions else listOf(StatisticsViewModel.ALL_GAMES_LABEL)
    val areaOptions = if (apiState.areaOptions.isNotEmpty()) apiState.areaOptions else listOf(StatisticsViewModel.ALL_AREAS_LABEL)

    val revenueSeries = LineSeriesData(apiState.trendLabels, apiState.trendRevenueValues)
    val playerSeries = LineSeriesData(apiState.trendLabels, apiState.trendPlayerValues)

    val chartRows = apiState.games
        .map { it.toRideStatRow() }
        .filter { it.players > 0 }
        .take(7)

    val topRevenue = apiState.topRevenue.map { RankedRide(it.name, formatVndCompact(it.revenue.toLong())) }
    val lowestPlayers = apiState.lowPlayers.map { RankedRide(it.name, "${formatNumber(it.players)} người chơi") }

    val tableRows = apiState.tableItems.map { it.toRideStatRow() }
    val tablePage = apiState.tablePage
    val tableSize = apiState.tableSize
    val tableTotal = apiState.tableTotal
    val tableRevenue = apiState.summary.totalRevenue.toLong()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(topContentBackground)
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            SectionHeader(
                title = "Thống kê",
                subtitle = "Phân tích chi tiết hoạt động công viên"
            ) {
                Button(
                    onClick = { viewModel.refresh() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE9EEF9),
                        contentColor = Color(0xFF1D4ED8)
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Text("Làm mới")
                }
            }
        }

        item {
            val syncText = when {
                apiState.isFilterLoading || apiState.isTrendLoading || apiState.isGamesLoading || apiState.isTableLoading -> {
                    "Đang đồng bộ dữ liệu từ API..."
                }

                apiState.lastSyncAt != null -> {
                    "Đã đồng bộ dữ liệu API lúc ${apiState.lastSyncAt}"
                }

                else -> {
                    "Chưa đồng bộ được dữ liệu API"
                }
            }

            val syncColor = when {
                apiState.isFilterLoading || apiState.isTrendLoading || apiState.isGamesLoading || apiState.isTableLoading -> Color(0xFF1D4ED8)
                apiState.lastSyncAt != null -> Color(0xFF129A58)
                else -> Color(0xFFD18700)
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = syncText,
                    color = syncColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                apiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color(0xFFD14343),
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            FilterPanel(
                dateRange = apiState.selectedDateRange,
                onDateRangeChange = viewModel::onDateRangeChange,
                dateOptions = apiState.dateOptions,
                selectedGame = apiState.selectedGame,
                onGameChange = viewModel::onGameChange,
                gameOptions = gameOptions,
                selectedArea = apiState.selectedArea,
                onAreaChange = viewModel::onAreaChange,
                areaOptions = areaOptions,
                selectedGrouping = grouping,
                onGroupingChange = { selected ->
                    viewModel.onGroupingChange(selected.toPeriod())
                },
                onApplyFilter = {
                    viewModel.applyFilters(resetTablePage = true)
                }
            )
        }

        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val compact = maxWidth < 980.dp
                if (compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ChartCard(
                            title = "Doanh thu theo thời gian",
                            options = StatisticGrouping.entries.map { it.label },
                            selectedOption = grouping.label,
                            onOptionSelected = { label ->
                                val nextGrouping = StatisticGrouping.entries.firstOrNull { it.label == label } ?: StatisticGrouping.DAY
                                viewModel.onGroupingChange(nextGrouping.toPeriod())
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LineTrendChart(
                                series = revenueSeries,
                                lineColor = RevenueColor,
                                axisFormatter = ::revenueAxisLabel,
                                modifier = Modifier.height(250.dp)
                            )
                        }
                        ChartCard(
                            title = "Người chơi theo thời gian",
                            options = StatisticGrouping.entries.map { it.label },
                            selectedOption = grouping.label,
                            onOptionSelected = { label ->
                                val nextGrouping = StatisticGrouping.entries.firstOrNull { it.label == label } ?: StatisticGrouping.DAY
                                viewModel.onGroupingChange(nextGrouping.toPeriod())
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LineTrendChart(
                                series = playerSeries,
                                lineColor = PlayerColor,
                                axisFormatter = ::playerAxisLabel,
                                modifier = Modifier.height(250.dp)
                            )
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ChartCard(
                            title = "Doanh thu theo thời gian",
                            options = StatisticGrouping.entries.map { it.label },
                            selectedOption = grouping.label,
                            onOptionSelected = { label ->
                                val nextGrouping = StatisticGrouping.entries.firstOrNull { it.label == label } ?: StatisticGrouping.DAY
                                viewModel.onGroupingChange(nextGrouping.toPeriod())
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            LineTrendChart(
                                series = revenueSeries,
                                lineColor = RevenueColor,
                                axisFormatter = ::revenueAxisLabel,
                                modifier = Modifier.height(250.dp)
                            )
                        }
                        ChartCard(
                            title = "Người chơi theo thời gian",
                            options = StatisticGrouping.entries.map { it.label },
                            selectedOption = grouping.label,
                            onOptionSelected = { label ->
                                val nextGrouping = StatisticGrouping.entries.firstOrNull { it.label == label } ?: StatisticGrouping.DAY
                                viewModel.onGroupingChange(nextGrouping.toPeriod())
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            LineTrendChart(
                                series = playerSeries,
                                lineColor = PlayerColor,
                                axisFormatter = ::playerAxisLabel,
                                modifier = Modifier.height(250.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            val chartNames = chartRows.map { it.name }
            val playerValues = chartRows.map { it.players.toFloat() }
            val revenueValues = chartRows.map { it.revenue.toFloat() }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val compact = maxWidth < 980.dp
                if (compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ChartCard(
                            title = "Số lượng người chơi theo trò",
                            options = listOf("Top 7"),
                            selectedOption = "Top 7",
                            onOptionSelected = {},
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BarComparisonChart(
                                labels = chartNames,
                                values = playerValues,
                                barColor = PlayerColor,
                                axisFormatter = ::playerAxisLabel,
                                modifier = Modifier.height(250.dp)
                            )
                        }
                        ChartCard(
                            title = "Doanh thu theo trò",
                            options = listOf("Top 7"),
                            selectedOption = "Top 7",
                            onOptionSelected = {},
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BarComparisonChart(
                                labels = chartNames,
                                values = revenueValues,
                                barColor = RevenueColor,
                                axisFormatter = ::revenueAxisLabel,
                                modifier = Modifier.height(250.dp)
                            )
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ChartCard(
                            title = "Số lượng người chơi theo trò",
                            options = listOf("Top 7"),
                            selectedOption = "Top 7",
                            onOptionSelected = {},
                            modifier = Modifier.weight(1f)
                        ) {
                            BarComparisonChart(
                                labels = chartNames,
                                values = playerValues,
                                barColor = PlayerColor,
                                axisFormatter = ::playerAxisLabel,
                                modifier = Modifier.height(250.dp)
                            )
                        }
                        ChartCard(
                            title = "Doanh thu theo trò",
                            options = listOf("Top 7"),
                            selectedOption = "Top 7",
                            onOptionSelected = {},
                            modifier = Modifier.weight(1f)
                        ) {
                            BarComparisonChart(
                                labels = chartNames,
                                values = revenueValues,
                                barColor = RevenueColor,
                                axisFormatter = ::revenueAxisLabel,
                                modifier = Modifier.height(250.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val compact = maxWidth < 980.dp
                if (compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        InsightCard(title = "Tỷ trọng người chơi theo trò", modifier = Modifier.fillMaxWidth()) {
                            DonutChartBreakdown(entries = chartRows)
                        }
                        InsightCard(title = "Top 5 doanh thu cao nhất", modifier = Modifier.fillMaxWidth()) {
                            topRevenue.forEachIndexed { index, ride ->
                                RankingLine(index = index, name = ride.name, value = ride.valueText)
                            }
                        }
                        InsightCard(title = "Top 5 ít khách", modifier = Modifier.fillMaxWidth()) {
                            lowestPlayers.forEachIndexed { index, ride ->
                                RankingLine(index = index, name = ride.name, value = ride.valueText)
                            }
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InsightCard(title = "Tỷ trọng người chơi theo trò", modifier = Modifier.weight(1.2f)) {
                            DonutChartBreakdown(entries = chartRows)
                        }
                        InsightCard(title = "Top 5 doanh thu cao nhất", modifier = Modifier.weight(1f)) {
                            topRevenue.forEachIndexed { index, ride ->
                                RankingLine(index = index, name = ride.name, value = ride.valueText)
                            }
                        }
                        InsightCard(title = "Top 5 ít khách", modifier = Modifier.weight(1f)) {
                            lowestPlayers.forEachIndexed { index, ride ->
                                RankingLine(index = index, name = ride.name, value = ride.valueText)
                            }
                        }
                    }
                }
            }
        }

        item {
            DataTableSection(
                rows = tableRows,
                searchQuery = apiState.tableSearchQuery,
                onSearchQueryChange = viewModel::onTableSearchChange,
                selectedGame = apiState.selectedGame,
                onGameChange = {
                    viewModel.onGameChange(it)
                    viewModel.applyFilters(resetTablePage = true)
                },
                gameOptions = gameOptions,
                page = tablePage,
                size = tableSize,
                total = tableTotal,
                totalRevenue = tableRevenue,
                isLoading = apiState.isTableLoading,
                onPreviousPage = viewModel::previousTablePage,
                onNextPage = viewModel::nextTablePage
            )
        }
    }
}

@Composable
private fun RankingLine(
    index: Int,
    name: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = "${index + 1}.",
            color = Color(0xFF1D4ED8),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(
            text = name,
            color = Color(0xFF111827),
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = Color(0xFF667085),
            fontSize = 12.sp
        )
    }
}

private fun StatisticGrouping.toPeriod(): StatisticsPeriod {
    return when (this) {
        StatisticGrouping.DAY -> StatisticsPeriod.DAY
        StatisticGrouping.WEEK -> StatisticsPeriod.WEEK
        StatisticGrouping.MONTH -> StatisticsPeriod.MONTH
    }
}

private fun StatisticsPeriod.toGrouping(): StatisticGrouping {
    return when (this) {
        StatisticsPeriod.DAY -> StatisticGrouping.DAY
        StatisticsPeriod.WEEK -> StatisticGrouping.WEEK
        StatisticsPeriod.MONTH -> StatisticGrouping.MONTH
    }
}

private fun StatisticsGameItemDTO.toRideStatRow(): RideStatRow {
    return RideStatRow(
        name = name,
        area = area ?: "Unknown",
        plays = plays,
        players = players,
        revenue = revenue.toLong(),
        ticketPrice = ticketPrice.roundToInt(),
        status = status.toRideStatus()
    )
}

private fun String.toRideStatus(): RideStatus {
    return when (trim().uppercase()) {
        "MAINTENANCE", "MAINTAIN", "MAINTAINING" -> RideStatus.MAINTENANCE
        "INACTIVE", "CLOSED", "STOPPED" -> RideStatus.INACTIVE
        else -> RideStatus.ACTIVE
    }
}
