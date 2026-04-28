package com.example.appcongvien.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.components.ParkTopAppBar
import com.example.appcongvien.data.model.PaymentRecordDTO
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.WalletViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val walletRepository = (context.applicationContext as App).walletRepository
    val viewModel: WalletViewModel = viewModel(factory = WalletViewModel.Factory(walletRepository))

    val paymentsState by viewModel.paymentsState.collectAsState()
    val backgroundBrush = Brush.verticalGradient(
        listOf(
            Color(0xFFFFFAF4),
            AppColors.SurfaceLight,
            AppColors.SurfaceWhite
        )
    )

    LaunchedEffect(Unit) {
        viewModel.loadPayments(page = 1, size = 50)
    }

    Scaffold(
        topBar = {
            ParkTopAppBar(
                title = "Lịch Sử Nạp Tiền",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        when (val state = paymentsState) {
            is Resource.Loading, null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(backgroundBrush),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.WarmOrange)
                }
            }

            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(backgroundBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = Color(0xFFE45A4F),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            is Resource.Success -> {
                val payments = state.data.items.orEmpty()
                val successPayments = payments.filter { it.status == "SUCCESS" }
                val totalTopUp = successPayments.sumOf {
                    it.amount.toDoubleOrNull()?.toInt() ?: 0
                }

                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(backgroundBrush),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        PaymentSummaryCard(
                            totalAmount = totalTopUp,
                            totalTransactions = successPayments.size
                        )
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Tất cả giao dịch nạp tiền",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark
                            )
                            Text(
                                text = "Kiểm tra phương thức, trạng thái và thời gian của từng lần nạp.",
                                fontSize = 13.sp,
                                color = AppColors.PrimaryGray.copy(alpha = 0.82f),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    if (payments.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.7f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = AppColors.WarmOrangeSoft.copy(alpha = 0.55f),
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Receipt,
                                            contentDescription = null,
                                            tint = AppColors.WarmOrange,
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }
                                    Text(
                                        text = "Chưa có giao dịch nạp tiền",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.PrimaryDark
                                    )
                                    Text(
                                        text = "Những lần nạp thành công sẽ được ghi lại tại đây.",
                                        fontSize = 13.sp,
                                        color = AppColors.PrimaryGray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(payments, key = { it.paymentId }) { payment ->
                            PaymentRecordCard(payment = payment)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentSummaryCard(totalAmount: Int, totalTransactions: Int) {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.75f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryMetric(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                iconTint = Color(0xFF3BA55D),
                iconBackground = Color(0xFF3BA55D).copy(alpha = 0.14f),
                value = "${formatter.format(totalAmount)}đ",
                label = "Tổng nạp",
                modifier = Modifier.weight(1f)
            )
            SummaryMetric(
                icon = Icons.Default.Receipt,
                iconTint = AppColors.WarmOrange,
                iconBackground = AppColors.WarmOrangeSoft.copy(alpha = 0.55f),
                value = "$totalTransactions",
                label = "Giao dịch",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryMetric(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = iconBackground,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.padding(12.dp)
            )
        }
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryDark,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = AppColors.PrimaryGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PaymentRecordCard(payment: PaymentRecordDTO) {
    val amount = payment.amount.toDoubleOrNull()?.toInt() ?: 0
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
    val (statusIcon, statusColor, statusBackground) = when (payment.status) {
        "SUCCESS" -> Triple(
            Icons.Default.CheckCircle,
            Color(0xFF3BA55D),
            Color(0xFF3BA55D).copy(alpha = 0.14f)
        )

        "PENDING" -> Triple(
            Icons.Default.Pending,
            Color(0xFFB7791F),
            Color(0xFFFFF1D6)
        )

        else -> Triple(
            Icons.Default.Error,
            Color(0xFFE45A4F),
            Color(0xFFE45A4F).copy(alpha = 0.14f)
        )
    }
    val statusLabel = when (payment.status) {
        "SUCCESS" -> "Thành công"
        "PENDING" -> "Đang xử lý"
        else -> "Thất bại"
    }
    val methodLabel = when (payment.method.uppercase()) {
        "MOMO" -> "MoMo"
        "ZALOPAY" -> "ZaloPay"
        "VNPAY" -> "VNPay"
        "BANKING" -> "Chuyển khoản"
        "CREDIT_CARD" -> "Thẻ tín dụng"
        "CASH" -> "Tiền mặt"
        else -> payment.method
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = methodLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = statusBackground
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = statusLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor
                            )
                        }
                    }
                }

                Text(
                    text = "+${formatter.format(amount)}đ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3BA55D)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMetaRow(
                    label = "Mã giao dịch",
                    value = payment.paymentId.take(8).uppercase()
                )
                PaymentMetaRow(
                    label = "Thời gian",
                    value = formatPaymentTimestamp(payment.createdAt)
                )
            }
        }
    }
}

@Composable
private fun PaymentMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = AppColors.PrimaryGray
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.PrimaryDark
        )
    }
}

fun formatPaymentTimestamp(timestamp: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val clean = timestamp.substringBefore('Z').substringBefore('+')
        val date = inputFormat.parse(clean) ?: return timestamp
        outputFormat.format(date)
    } catch (_: Exception) {
        timestamp
    }
}
