package com.park.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.park.ui.component.PageHeader
import com.park.ui.component.RevenueBarChart
import com.park.ui.component.StatsCard
import com.park.ui.component.formatCurrencyFull
import com.park.ui.component.formatCurrencyShort
import com.park.ui.theme.AppColors
import com.park.ui.theme.AppTypography
import com.park.viewmodel.DashboardViewModel
import com.park.viewmodel.RevenuePeriod

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel { DashboardViewModel() }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stats = uiState.stats

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.SurfaceLight)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PageHeader(
                    title = "Dashboard",
                    subtitle = "Tổng quan hệ thống Park Adventure"
                )

            }
        }

        if (uiState.isLoading) {
            item {
                Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.WarmOrange)
                }
            }
        } else {
            // ── Stats Cards ──────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatsCard(
                        title = "Người dùng",
                        value = stats.totalUsers.toString(),
                        icon = Icons.Default.People,
                        iconColor = AppColors.BluePrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Trò chơi",
                        value = stats.totalGames.toString(),
                        icon = Icons.Default.SportsEsports,
                        iconColor = AppColors.WarmOrange,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Thẻ đang dùng",
                        value = stats.activeCards.toString(),
                        icon = Icons.Default.CreditCard,
                        iconColor = AppColors.GreenSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Thẻ sẵn sàng",
                        value = stats.availableCards.toString(),
                        icon = Icons.Default.CreditScore,
                        iconColor = AppColors.YellowWarning,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Doanh thu nạp",
                        value = formatCurrencyShort(stats.totalTopUpRevenue),
                        icon = Icons.Default.AttachMoney,
                        iconColor = AppColors.GreenSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Revenue Chart ────────────────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.White),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Biểu đồ doanh thu",
                                    style = AppTypography.titleLarge,
                                    color = AppColors.PrimaryDark
                                )
                                if (!uiState.isChartLoading) {
                                    Text(
                                        "Tổng: ${formatCurrencyFull(uiState.chartData.totalInPeriod)}",
                                        style = AppTypography.bodyMedium,
                                        color = AppColors.GreenSuccess
                                    )
                                }
                            }
                            // Period tab buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                RevenuePeriod.entries.forEach { period ->
                                    val isSelected = uiState.selectedPeriod == period
                                    Button(
                                        onClick = { viewModel.selectPeriod(period) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) AppColors.WarmOrange else AppColors.SurfaceLight,
                                            contentColor = if (isSelected) AppColors.White else AppColors.PrimaryGray
                                        ),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(period.label, style = AppTypography.bodyMedium)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        if (uiState.isChartLoading) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = AppColors.WarmOrange, modifier = Modifier.size(32.dp))
                            }
                        } else {
                            RevenueBarChart(data = uiState.chartData)
                        }
                    }
                }
            }

            // ── Card Summary ─────────────────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.White),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Tình trạng thẻ", style = AppTypography.titleLarge, color = AppColors.PrimaryDark)
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            CardStatItem("ACTIVE", stats.activeCards.toString(), AppColors.GreenSuccess, Modifier.weight(1f))
                            CardStatItem("AVAILABLE", stats.availableCards.toString(), AppColors.YellowWarning, Modifier.weight(1f))
                            CardStatItem("BLOCKED", stats.blockedCards.toString(), AppColors.RedError, Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardStatItem(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = AppTypography.titleLarge, color = color, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Text(label, style = AppTypography.bodyMedium, color = AppColors.PrimaryGray)
    }
}
