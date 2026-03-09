package com.example.appcongvien.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.appcongvien.components.ParkTopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
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
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.WarmOrange)
                }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = Color.Red,
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
                        .background(
                            Brush.verticalGradient(
                                listOf(AppColors.SurfaceLight, Color.White)
                            )
                        ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        PaymentSummaryCard(
                            totalAmount = totalTopUp,
                            totalTransactions = successPayments.size
                        )
                    }

                    item {
                        Text(
                            text = "Tất Cả Giao Dịch Nạp Tiền",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryDark
                        )
                    }

                    if (payments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Chưa có giao dịch nào",
                                    color = AppColors.PrimaryGray
                                )
                            }
                        }
                    } else {
                        items(payments) { payment ->
                            PaymentRecordCard(payment = payment)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
            null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.WarmOrange)
                }
            }
        }
    }
}

@Composable
fun PaymentSummaryCard(totalAmount: Int, totalTransactions: Int) {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${formatter.format(totalAmount)}đ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
                Text(text = "Tổng nạp", fontSize = 12.sp, color = AppColors.PrimaryGray)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = AppColors.WarmOrangeSoft,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        tint = AppColors.WarmOrange,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$totalTransactions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
                Text(text = "Giao dịch", fontSize = 12.sp, color = AppColors.PrimaryGray)
            }
        }
    }
}

@Composable
fun PaymentRecordCard(payment: PaymentRecordDTO) {
    val amount = payment.amount.toDoubleOrNull()?.toInt() ?: 0
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    val (statusIcon, statusColor) = when (payment.status) {
        "SUCCESS" -> Pair(Icons.Default.CheckCircle, Color(0xFF4CAF50))
        "PENDING" -> Pair(Icons.Default.Pending, Color(0xFFFFC107))
        else -> Pair(Icons.Default.Error, Color(0xFFF44336))
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = methodLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = statusLabel,
                                fontSize = 10.sp,
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
                    color = Color(0xFF4CAF50)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Mã giao dịch:", fontSize = 12.sp, color = AppColors.PrimaryGray)
                    Text(
                        text = payment.paymentId.take(8).uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.PrimaryDark
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Thời gian:", fontSize = 12.sp, color = AppColors.PrimaryGray)
                    Text(
                        text = formatPaymentTimestamp(payment.createdAt),
                        fontSize = 12.sp,
                        color = AppColors.PrimaryGray
                    )
                }
            }
        }
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
    } catch (e: Exception) {
        timestamp
    }
}
