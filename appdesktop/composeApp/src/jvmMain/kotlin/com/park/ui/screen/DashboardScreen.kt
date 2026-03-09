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
import com.park.ui.component.StatusBadge
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
            PageHeader(
                title = "Dashboard",
                subtitle = "Tổng quan hệ thống Park Adventure"
            )
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
                        title = "Đơn hàng",
                        value = stats.totalOrders.toString(),
                        icon = Icons.Default.ShoppingCart,
                        iconColor = AppColors.GreenSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Voucher",
                        value = stats.activeVouchers.toString(),
                        icon = Icons.Default.LocalOffer,
                        iconColor = AppColors.YellowWarning,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Doanh thu",
                        value = formatCurrencyShort(stats.totalRevenue),
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

            // ── Recent Orders ────────────────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.White),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Đơn hàng gần đây",
                            style = AppTypography.titleLarge,
                            color = AppColors.PrimaryDark
                        )
                        Spacer(Modifier.height(12.dp))

                        if (uiState.recentOrders.isEmpty()) {
                            Text(
                                "Chưa có đơn hàng nào",
                                style = AppTypography.bodyMedium,
                                color = AppColors.PrimaryGray
                            )
                        } else {
                            // Header row
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Mã đơn", style = AppTypography.bodyMedium, color = AppColors.PrimaryGray,
                                    modifier = Modifier.weight(2f))
                                Text("Người dùng", style = AppTypography.bodyMedium, color = AppColors.PrimaryGray,
                                    modifier = Modifier.weight(2f))
                                Text("Số tiền", style = AppTypography.bodyMedium, color = AppColors.PrimaryGray,
                                    modifier = Modifier.weight(1.5f))
                                Text("Trạng thái", style = AppTypography.bodyMedium, color = AppColors.PrimaryGray,
                                    modifier = Modifier.weight(1.5f))
                            }
                            Divider(color = AppColors.LightGray)
                            uiState.recentOrders.forEach { order ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "#${order.orderId.take(8)}",
                                        style = AppTypography.bodyMedium,
                                        color = AppColors.PrimaryDark,
                                        modifier = Modifier.weight(2f)
                                    )
                                    Text(
                                        order.userName ?: order.userId.take(8),
                                        style = AppTypography.bodyMedium,
                                        color = AppColors.PrimaryDark,
                                        modifier = Modifier.weight(2f)
                                    )
                                    Text(
                                        formatCurrencyShort(order.finalAmount.toDoubleOrNull() ?: 0.0),
                                        style = AppTypography.bodyMedium,
                                        color = AppColors.GreenSuccess,
                                        modifier = Modifier.weight(1.5f)
                                    )
                                    Box(modifier = Modifier.weight(1.5f)) {
                                        StatusBadge(order.status)
                                    }
                                }
                                Divider(color = AppColors.LightGray.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}
