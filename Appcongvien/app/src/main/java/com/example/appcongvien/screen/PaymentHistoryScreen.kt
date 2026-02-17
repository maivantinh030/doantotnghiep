package com.example.appcongvien.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcongvien.ui.theme.AppColors

data class PaymentRecord(
    val id: String,
    val method: PaymentMethod,
    val amount: Int,
    val status: PaymentStatus,
    val timestamp: String,
    val transactionId: String
)

enum class PaymentMethod {
    MOMO, ZALOPAY, BANKING, CREDIT_CARD, CASH
}

enum class PaymentStatus {
    SUCCESS, PENDING, FAILED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    // Mock payment data
    val payments = remember {
        listOf(
            PaymentRecord(
                id = "1",
                method = PaymentMethod.MOMO,
                amount = 100000,
                status = PaymentStatus.SUCCESS,
                timestamp = "Hôm nay, 10:15",
                transactionId = "MT240121001"
            ),
            PaymentRecord(
                id = "2",
                method = PaymentMethod.BANKING,
                amount = 200000,
                status = PaymentStatus.SUCCESS,
                timestamp = "Hôm qua, 14:20",
                transactionId = "BK240120002"
            ),
            PaymentRecord(
                id = "3",
                method = PaymentMethod.MOMO,
                amount = 50000,
                status = PaymentStatus.PENDING,
                timestamp = "Hôm qua, 08:45",
                transactionId = "MT240120003"
            ),
            PaymentRecord(
                id = "4",
                method = PaymentMethod.ZALOPAY,
                amount = 150000,
                status = PaymentStatus.SUCCESS,
                timestamp = "2 ngày trước, 16:30",
                transactionId = "ZP240119004"
            ),
            PaymentRecord(
                id = "5",
                method = PaymentMethod.CREDIT_CARD,
                amount = 300000,
                status = PaymentStatus.FAILED,
                timestamp = "3 ngày trước, 11:15",
                transactionId = "CC240118005"
            )
        )
    }

    val totalTopUp = payments.filter { it.status == PaymentStatus.SUCCESS }.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lịch Sử Nạp Tiền",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.WarmOrange
                )
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AppColors.SurfaceLight,
                            Color.White
                        )
                    )
                ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Summary Card
            item {
                SummaryCard(
                    totalAmount = totalTopUp,
                    totalTransactions = payments.filter { it.status == PaymentStatus.SUCCESS }.size
                )
            }

            // Transactions Header
            item {
                Text(
                    text = "Tất Cả Giao Dịch Nạp Tiền",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
            }

            // Payment Records
            items(payments) { payment ->
                PaymentCard(payment = payment)
            }

            // Extra space for bottom navigation
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SummaryCard(
    totalAmount: Int,
    totalTransactions: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                    text = "${totalAmount} đ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
                Text(
                    text = "Tổng nạp",
                    fontSize = 12.sp,
                    color = AppColors.PrimaryGray
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                Text(
                    text = "Giao dịch",
                    fontSize = 12.sp,
                    color = AppColors.PrimaryGray
                )
            }
        }
    }
}

@Composable
fun PaymentCard(payment: PaymentRecord) {
    val (statusIcon, statusColor) = getPaymentStatusStyle(payment.status)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Header row
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
                        text = getPaymentMethodName(payment.method),
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
                                text = getPaymentStatusName(payment.status),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor
                            )
                        }
                    }
                }

                Text(
                    text = "+${payment.amount} đ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }

            // Details
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Mã giao dịch:",
                        fontSize = 12.sp,
                        color = AppColors.PrimaryGray
                    )
                    Text(
                        text = payment.transactionId,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.PrimaryDark
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Thời gian:",
                        fontSize = 12.sp,
                        color = AppColors.PrimaryGray
                    )
                    Text(
                        text = payment.timestamp,
                        fontSize = 12.sp,
                        color = AppColors.PrimaryGray
                    )
                }
            }
        }
    }
}

@Composable
fun getPaymentStatusStyle(status: PaymentStatus): Pair<ImageVector, Color> {
    return when (status) {
        PaymentStatus.SUCCESS -> Pair(Icons.Default.CheckCircle, Color(0xFF4CAF50))
        PaymentStatus.PENDING -> Pair(Icons.Default.Pending, Color(0xFFFFC107))
        PaymentStatus.FAILED -> Pair(Icons.Default.Error, Color(0xFFF44336))
    }
}

fun getPaymentMethodName(method: PaymentMethod): String {
    return when (method) {
        PaymentMethod.MOMO -> "MoMo"
        PaymentMethod.ZALOPAY -> "ZaloPay"
        PaymentMethod.BANKING -> "Chuyển khoản"
        PaymentMethod.CREDIT_CARD -> "Thẻ tín dụng"
        PaymentMethod.CASH -> "Tiền mặt"
    }
}

fun getPaymentStatusName(status: PaymentStatus): String {
    return when (status) {
        PaymentStatus.SUCCESS -> "Thành công"
        PaymentStatus.PENDING -> "Đang xử lý"
        PaymentStatus.FAILED -> "Thất bại"
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentHistoryScreenPreview() {
    PaymentHistoryScreen()
}


