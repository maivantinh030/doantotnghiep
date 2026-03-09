package com.park.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.park.ui.component.PageHeader
import com.park.ui.component.StatusBadge
import com.park.ui.component.formatCurrencyFull
import com.park.ui.theme.AppColors
import com.park.ui.theme.AppTypography
import com.park.viewmodel.FinanceTab
import com.park.viewmodel.FinanceViewModel

@Composable
fun FinanceScreen(viewModel: FinanceViewModel = viewModel { FinanceViewModel() }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.SurfaceLight)
            .padding(24.dp)
    ) {
        PageHeader(
            title = "Tài chính & Doanh thu",
            subtitle = "Theo dõi đơn hàng và giao dịch hệ thống"
        )

        // Tab selector
        Row(modifier = Modifier.padding(bottom = 16.dp)) {
            listOf(FinanceTab.ORDERS to "Đơn hàng", FinanceTab.TRANSACTIONS to "Giao dịch").forEach { (tab, label) ->
                val isSelected = uiState.currentTab == tab
                Button(
                    onClick = { viewModel.selectTab(tab) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) AppColors.WarmOrange else AppColors.LightGray,
                        contentColor = if (isSelected) AppColors.White else AppColors.PrimaryGray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) { Text(label) }
            }
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.WarmOrange)
            }
        } else when (uiState.currentTab) {
            FinanceTab.ORDERS -> {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().background(AppColors.SurfaceLight).padding(16.dp, 12.dp)
                        ) {
                            Text("Mã đơn", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1.5f))
                            Text("Người dùng", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1.5f))
                            Text("Tổng tiền", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                            Text("Thanh toán", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                            Text("Trạng thái", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                            Text("Ngày tạo", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1.5f))
                        }
                        Divider(color = AppColors.LightGray)
                        LazyColumn {
                            items(uiState.orders) { order ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp, 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(order.orderId.take(8) + "...", style = AppTypography.bodyMedium, color = AppColors.PrimaryGray, modifier = Modifier.weight(1.5f))
                                    Text(order.userName ?: order.userId.take(8), style = AppTypography.bodyMedium, color = AppColors.PrimaryDark, modifier = Modifier.weight(1.5f))
                                    Text(formatCurrencyFull(order.finalAmount.toDoubleOrNull() ?: 0.0), style = AppTypography.bodyMedium, color = AppColors.GreenSuccess, modifier = Modifier.weight(1f))
                                    Text(order.paymentMethod ?: "-", style = AppTypography.bodyMedium, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                                    Box(modifier = Modifier.weight(1f)) { StatusBadge(order.status) }
                                    Text(order.createdAt?.take(10) ?: "-", style = AppTypography.bodyMedium, color = AppColors.PrimaryGray, modifier = Modifier.weight(1.5f))
                                }
                                Divider(color = AppColors.LightGray.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
            FinanceTab.TRANSACTIONS -> {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().background(AppColors.SurfaceLight).padding(16.dp, 12.dp)
                        ) {
                            Text("Mã GD", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1.5f))
                            Text("Người dùng", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1.5f))
                            Text("Loại", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                            Text("Số tiền", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                            Text("Ghi chú", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(2f))
                            Text("Ngày", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1.5f))
                        }
                        Divider(color = AppColors.LightGray)
                        LazyColumn {
                            items(uiState.transactions) { tx ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp, 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tx.transactionId.take(8) + "...", style = AppTypography.bodyMedium, color = AppColors.PrimaryGray, modifier = Modifier.weight(1.5f))
                                    Text(tx.userName ?: tx.userId.take(8), style = AppTypography.bodyMedium, color = AppColors.PrimaryDark, modifier = Modifier.weight(1.5f))
                                    Text(tx.type, style = AppTypography.bodyMedium, color = AppColors.BluePrimary, modifier = Modifier.weight(1f))
                                    Text(formatCurrencyFull(tx.amount.toDoubleOrNull() ?: 0.0), style = AppTypography.bodyMedium, color = AppColors.GreenSuccess, modifier = Modifier.weight(1f))
                                    Text(tx.description ?: "-", style = AppTypography.bodyMedium, color = AppColors.PrimaryGray, modifier = Modifier.weight(2f))
                                    Text(tx.createdAt?.take(10) ?: "-", style = AppTypography.bodyMedium, color = AppColors.PrimaryGray, modifier = Modifier.weight(1.5f))
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
