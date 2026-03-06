package com.park.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.park.ui.component.StatsCard
import com.park.ui.theme.AppColors
import com.park.ui.theme.AppTypography
import com.park.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel { DashboardViewModel() }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stats = uiState.stats

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.SurfaceLight)
            .padding(24.dp)
    ) {
        PageHeader(
            title = "Dashboard",
            subtitle = "Tổng quan hệ thống Park Adventure"
        )

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.WarmOrange)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 220.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    StatsCard(
                        title = "Tổng người dùng",
                        value = stats.totalUsers.toString(),
                        icon = Icons.Default.People,
                        iconColor = AppColors.BluePrimary
                    )
                }
                item {
                    StatsCard(
                        title = "Trò chơi",
                        value = stats.totalGames.toString(),
                        icon = Icons.Default.SportsEsports,
                        iconColor = AppColors.WarmOrange
                    )
                }
                item {
                    StatsCard(
                        title = "Tổng đơn hàng",
                        value = stats.totalOrders.toString(),
                        icon = Icons.Default.ShoppingCart,
                        iconColor = AppColors.GreenSuccess
                    )
                }
                item {
                    StatsCard(
                        title = "Voucher hoạt động",
                        value = stats.activeVouchers.toString(),
                        icon = Icons.Default.LocalOffer,
                        iconColor = AppColors.YellowWarning
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Quick links section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Truy cập nhanh", style = AppTypography.titleLarge, color = AppColors.PrimaryDark)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Sử dụng thanh điều hướng bên trái để quản lý:\n" +
                        "• Người dùng: Xem, khóa/mở khóa tài khoản, điều chỉnh số dư\n" +
                        "• Trò chơi: Thêm, sửa, xóa và quản lý trạng thái\n" +
                        "• Voucher: Tạo và quản lý chương trình khuyến mãi\n" +
                        "• Tài chính: Theo dõi đơn hàng và giao dịch\n" +
                        "• Thông báo: Gửi push notification đến người dùng\n" +
                        "• Hỗ trợ: Phản hồi yêu cầu hỗ trợ từ khách hàng",
                        style = AppTypography.bodyMedium,
                        color = AppColors.PrimaryGray
                    )
                }
            }
        }
    }
}
