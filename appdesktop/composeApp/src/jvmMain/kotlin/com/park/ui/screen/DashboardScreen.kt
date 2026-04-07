package com.park.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.park.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    onOpenStatistics: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel { DashboardViewModel() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val quickRange = uiState.quickRange
    val quickRangeOptions = DashboardQuickRange.entries.map { it.label }

    val revenueSeries = LineSeriesData(
        labels = uiState.revenueLabels,
        values = uiState.revenueValues
    )
    val playerSeries = LineSeriesData(
        labels = uiState.playerLabels,
        values = uiState.playerValues
    )

    val topRides = uiState.topGameItems
        .map { RankedRide(it.name, "${formatNumber(it.players)} người chơi") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(topContentBackground)
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            DashboardHeader(
                title = "Dashboard",
                subtitle = "Tổng quan hệ thống Park Adventure",
                range = quickRange,
                onRangeSelected = viewModel::selectQuickRange
            )
        }

        item {
            val syncText = when {
                uiState.isLoading -> "Đang đồng bộ dashboard từ API..."
                uiState.lastSyncAt != null -> "Dữ liệu API cập nhật lúc ${uiState.lastSyncAt}"
                else -> "Chưa có dữ liệu dashboard từ API"
            }
            val syncColor = when {
                uiState.isLoading -> Color(0xFF1D4ED8)
                uiState.lastSyncAt != null -> Color(0xFF129A58)
                else -> Color(0xFFD18700)
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = syncText,
                    color = syncColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color(0xFFD14343),
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val compact = maxWidth < 980.dp
                if (compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            KpiCard(
                                title = "Tổng người chơi",
                                value = formatNumber(uiState.totalPlayers),
                                delta = uiState.playersDelta,
                                icon = Icons.Default.Groups,
                                accent = Color(0xFF2E77F4),
                                modifier = Modifier.weight(1f)
                            )
                            KpiCard(
                                title = "Tổng doanh thu",
                                value = formatVndCompact(uiState.totalRevenue.toLong()),
                                delta = uiState.revenueDelta,
                                icon = Icons.Default.AttachMoney,
                                accent = Color(0xFF1F9D64),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            KpiCard(
                                title = "Vé đã bán",
                                value = formatNumber(uiState.ticketsSold),
                                delta = uiState.ticketsDelta,
                                icon = Icons.Default.ConfirmationNumber,
                                accent = Color(0xFFF59E0B),
                                modifier = Modifier.weight(1f)
                            )
                            KpiCard(
                                title = "Số trò hoạt động",
                                value = "${uiState.activeRides} / ${uiState.totalRides} trò",
                                delta = uiState.ridesDelta,
                                icon = Icons.Default.SportsEsports,
                                accent = Color(0xFF8B5CF6),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KpiCard(
                            title = "Tổng người chơi",
                            value = formatNumber(uiState.totalPlayers),
                            delta = uiState.playersDelta,
                            icon = Icons.Default.Groups,
                            accent = Color(0xFF2E77F4),
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "Tổng doanh thu",
                            value = formatVndCompact(uiState.totalRevenue.toLong()),
                            delta = uiState.revenueDelta,
                            icon = Icons.Default.AttachMoney,
                            accent = Color(0xFF1F9D64),
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "Vé đã bán",
                            value = formatNumber(uiState.ticketsSold),
                            delta = uiState.ticketsDelta,
                            icon = Icons.Default.ConfirmationNumber,
                            accent = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "Số trò hoạt động",
                            value = "${uiState.activeRides} / ${uiState.totalRides} trò",
                            delta = uiState.ridesDelta,
                            icon = Icons.Default.SportsEsports,
                            accent = Color(0xFF8B5CF6),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val compact = maxWidth < 980.dp
                if (compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ChartCard(
                            title = "Doanh thu theo thời gian",
                            options = quickRangeOptions,
                            selectedOption = quickRange.label,
                            onOptionSelected = { label ->
                                DashboardQuickRange.entries.firstOrNull { it.label == label }
                                    ?.let(viewModel::selectQuickRange)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LineTrendChart(
                                series = revenueSeries,
                                lineColor = RevenueColor,
                                axisFormatter = ::revenueAxisLabel,
                                modifier = Modifier.height(240.dp)
                            )
                        }
                        ChartCard(
                            title = "Người chơi theo thời gian",
                            options = quickRangeOptions,
                            selectedOption = quickRange.label,
                            onOptionSelected = { label ->
                                DashboardQuickRange.entries.firstOrNull { it.label == label }
                                    ?.let(viewModel::selectQuickRange)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LineTrendChart(
                                series = playerSeries,
                                lineColor = PlayerColor,
                                axisFormatter = ::playerAxisLabel,
                                modifier = Modifier.height(240.dp)
                            )
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ChartCard(
                            title = "Doanh thu theo thời gian",
                            options = quickRangeOptions,
                            selectedOption = quickRange.label,
                            onOptionSelected = { label ->
                                DashboardQuickRange.entries.firstOrNull { it.label == label }
                                    ?.let(viewModel::selectQuickRange)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            LineTrendChart(
                                series = revenueSeries,
                                lineColor = RevenueColor,
                                axisFormatter = ::revenueAxisLabel,
                                modifier = Modifier.height(240.dp)
                            )
                        }
                        ChartCard(
                            title = "Người chơi theo thời gian",
                            options = quickRangeOptions,
                            selectedOption = quickRange.label,
                            onOptionSelected = { label ->
                                DashboardQuickRange.entries.firstOrNull { it.label == label }
                                    ?.let(viewModel::selectQuickRange)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            LineTrendChart(
                                series = playerSeries,
                                lineColor = PlayerColor,
                                axisFormatter = ::playerAxisLabel,
                                modifier = Modifier.height(240.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            val warningItems = if (uiState.warnings.isNotEmpty()) {
                uiState.warnings
            } else {
                listOf("Khong co canh bao trong bo du lieu hien tai")
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val compact = maxWidth < 980.dp

                if (compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        InsightCard(title = "Top trò chơi (toàn bộ)", modifier = Modifier.fillMaxWidth()) {
                            if (topRides.isEmpty()) {
                                Text("Chưa có dữ liệu trò chơi", color = Color(0xFF667085), fontSize = 13.sp)
                            } else {
                                topRides.forEachIndexed { index, ride ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .height(24.dp)
                                                .weight(0.12f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                color = Color(0xFF1D4ED8),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Column(modifier = Modifier.weight(0.88f)) {
                                            Text(ride.name, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
                                            Text(ride.valueText, fontSize = 12.sp, color = Color(0xFF667085))
                                        }
                                    }
                                }
                            }
                        }

                        InsightCard(title = "Cảnh báo", modifier = Modifier.fillMaxWidth()) {
                            warningItems.take(3).forEachIndexed { index, warning ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (index == 0) Icons.Default.ErrorOutline else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (index == 0) Color(0xFFF59E0B) else Color(0xFF2E77F4)
                                    )
                                    Text(
                                        text = warning,
                                        color = Color(0xFF475467),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        InsightCard(title = "Tình trạng thẻ", modifier = Modifier.fillMaxWidth()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                StatusCard(
                                    label = "Active",
                                    value = uiState.cardStatus.active.toString(),
                                    accent = Color(0xFF1F9D64),
                                    modifier = Modifier.weight(1f)
                                )
                                StatusCard(
                                    label = "Available",
                                    value = uiState.cardStatus.available.toString(),
                                    accent = Color(0xFFF59E0B),
                                    modifier = Modifier.weight(1f)
                                )
                                StatusCard(
                                    label = "Blocked",
                                    value = uiState.cardStatus.blocked.toString(),
                                    accent = Color(0xFFD14343),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InsightCard(title = "Top trò chơi (toàn bộ)", modifier = Modifier.weight(1f)) {
                            if (topRides.isEmpty()) {
                                Text("Chưa có dữ liệu trò chơi", color = Color(0xFF667085), fontSize = 13.sp)
                            } else {
                                topRides.forEachIndexed { index, ride ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .height(24.dp)
                                                .weight(0.12f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                color = Color(0xFF1D4ED8),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Column(modifier = Modifier.weight(0.88f)) {
                                            Text(ride.name, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
                                            Text(ride.valueText, fontSize = 12.sp, color = Color(0xFF667085))
                                        }
                                    }
                                }
                            }
                        }

                        InsightCard(title = "Cảnh báo", modifier = Modifier.weight(1f)) {
                            warningItems.take(3).forEachIndexed { index, warning ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (index == 0) Icons.Default.ErrorOutline else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (index == 0) Color(0xFFF59E0B) else Color(0xFF2E77F4)
                                    )
                                    Text(
                                        text = warning,
                                        color = Color(0xFF475467),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        InsightCard(title = "Tình trạng thẻ", modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                StatusCard(
                                    label = "Active",
                                    value = uiState.cardStatus.active.toString(),
                                    accent = Color(0xFF1F9D64),
                                    modifier = Modifier.weight(1f)
                                )
                                StatusCard(
                                    label = "Available",
                                    value = uiState.cardStatus.available.toString(),
                                    accent = Color(0xFFF59E0B),
                                    modifier = Modifier.weight(1f)
                                )
                                StatusCard(
                                    label = "Blocked",
                                    value = uiState.cardStatus.blocked.toString(),
                                    accent = Color(0xFFD14343),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onOpenStatistics,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1D4ED8),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.QueryStats, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("Xem thống kê chi tiết")
                }
            }
        }
    }
}