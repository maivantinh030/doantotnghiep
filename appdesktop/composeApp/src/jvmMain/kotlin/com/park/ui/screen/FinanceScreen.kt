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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.park.ui.component.PageHeader
import com.park.ui.component.formatCurrencyFull
import com.park.ui.theme.AppColors
import com.park.ui.theme.AppTypography
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
            title = "Tài chính & Giao dịch",
            subtitle = "${uiState.totalTransactions} giao dịch"
        )

        Spacer(Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.WarmOrange)
            }
        } else {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth().background(AppColors.SurfaceLight).padding(16.dp, 12.dp)
                    ) {
                        Text("Mã GD", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1.5f))
                        Text("User", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1.5f))
                        Text("Loại", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1.5f))
                        Text("Số tiền", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                        Text("Ghi chú", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(2f))
                        Text("Ngày", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1.5f))
                    }
                    Divider(color = AppColors.LightGray)
                    LazyColumn {
                        items(uiState.transactions) { tx ->
                            val amount = tx.amount.toDoubleOrNull() ?: 0.0
                            val amountColor = if (amount >= 0) AppColors.GreenSuccess else AppColors.RedError
                            val typeColor = when (tx.type) {
                                "TOPUP" -> Color(0xFF10B981)
                                "PAYMENT" -> AppColors.WarmOrange
                                "REFUND" -> Color(0xFF3B82F6)
                                "DEPOSIT_PAID" -> Color(0xFF8B5CF6)
                                "DEPOSIT_REFUND" -> Color(0xFF3B82F6)
                                "DEPOSIT_FORFEITED" -> AppColors.RedError
                                "ADJUSTMENT" -> AppColors.PrimaryGray
                                else -> AppColors.PrimaryGray
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp, 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tx.transactionId.take(8) + "...", style = AppTypography.bodyMedium, color = AppColors.PrimaryGray, modifier = Modifier.weight(1.5f))
                                Text(tx.userName ?: tx.userId.take(8), style = AppTypography.bodyMedium, color = AppColors.PrimaryDark, modifier = Modifier.weight(1.5f))
                                Text(tx.type, style = AppTypography.bodyMedium, color = typeColor, modifier = Modifier.weight(1.5f))
                                Text(formatCurrencyFull(amount), style = AppTypography.bodyMedium, color = amountColor, modifier = Modifier.weight(1f))
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
