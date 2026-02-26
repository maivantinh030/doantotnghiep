package com.example.appcongvien.screen

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.appcongvien.data.model.OrderDTO
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageHistoryScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val orderRepository = (context.applicationContext as App).orderRepository
    val viewModel: OrderViewModel = viewModel(
        factory = OrderViewModel.Factory(orderRepository)
    )

    val ordersState by viewModel.ordersState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadOrders(page = 1, size = 50)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lịch Sử Sử Dụng",
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
        when (val state = ordersState) {
            null, is Resource.Loading -> {
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
                        text = "Lỗi: ${state.message}",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            is Resource.Success -> {
                val orders = state.data.items

                if (orders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = AppColors.PrimaryGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "Chưa có lịch sử sử dụng",
                                fontSize = 16.sp,
                                color = AppColors.PrimaryGray
                            )
                        }
                    }
                } else {
                    val completedOrders = orders.filter { it.status == "COMPLETED" }
                    val totalSpent = completedOrders.sumOf {
                        it.totalAmount.toDoubleOrNull() ?: 0.0
                    }.toLong()
                    val totalSaved = completedOrders.sumOf {
                        it.discountAmount.toDoubleOrNull() ?: 0.0
                    }.toLong()
                    val totalTurns = completedOrders.sumOf { order ->
                        order.items?.sumOf { it.quantity } ?: 0
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
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            UsageSummaryCard(
                                totalSpent = totalSpent,
                                totalSaved = totalSaved,
                                totalTurns = totalTurns
                            )
                        }

                        item {
                            Text(
                                text = "Chi Tiết Đơn Hàng (${orders.size})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark
                            )
                        }

                        items(orders) { order ->
                            OrderUsageCard(order = order)
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsageSummaryCard(
    totalSpent: Long,
    totalSaved: Long,
    totalTurns: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                UsageSummaryItem(
                    icon = Icons.Default.TrendingDown,
                    iconColor = AppColors.WarmOrange,
                    title = "Chi tiêu",
                    value = "%,d đ".format(totalSpent)
                )
                UsageSummaryItem(
                    icon = Icons.Default.LocalOffer,
                    iconColor = Color(0xFF4CAF50),
                    title = "Tiết kiệm",
                    value = "%,d đ".format(totalSaved)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                UsageSummaryItem(
                    icon = Icons.Default.SportsEsports,
                    iconColor = Color(0xFF2196F3),
                    title = "Lượt chơi",
                    value = "$totalTurns lượt"
                )
            }
        }
    }
}

@Composable
fun UsageSummaryItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = iconColor.copy(alpha = 0.2f),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.padding(12.dp)
            )
        }
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryDark
        )
        Text(
            text = title,
            fontSize = 12.sp,
            color = AppColors.PrimaryGray
        )
    }
}

@Composable
fun OrderUsageCard(order: OrderDTO) {
    val statusColor = when (order.status) {
        "COMPLETED" -> Color(0xFF4CAF50)
        "CANCELLED" -> Color(0xFFF44336)
        else -> Color(0xFFFF9800)
    }
    val statusIcon = when (order.status) {
        "COMPLETED" -> Icons.Default.CheckCircle
        "CANCELLED" -> Icons.Default.Error
        else -> Icons.Default.Pending
    }
    val statusLabel = when (order.status) {
        "COMPLETED" -> "Hoàn thành"
        "CANCELLED" -> "Đã hủy"
        else -> "Chờ xử lý"
    }

    val subtotalVal = order.subtotal.toDoubleOrNull() ?: 0.0
    val discountVal = order.discountAmount.toDoubleOrNull() ?: 0.0
    val totalVal = order.totalAmount.toDoubleOrNull() ?: 0.0
    val totalTurns = order.items?.sumOf { it.quantity } ?: 0

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
            // Header: order ID + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Đơn #${order.orderId.take(8).uppercase()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )
                    if (totalTurns > 0) {
                        Text(
                            text = "$totalTurns lượt chơi",
                            fontSize = 12.sp,
                            color = AppColors.PrimaryGray
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
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

            // Items list
            if (!order.items.isNullOrEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    order.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.SportsEsports,
                                    contentDescription = null,
                                    tint = AppColors.WarmOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Game ${item.gameId.take(8)}",
                                    fontSize = 12.sp,
                                    color = AppColors.PrimaryDark
                                )
                            }
                            Text(
                                text = "x${item.quantity}  •  %,.0f đ".format(
                                    item.lineTotal.toDoubleOrNull() ?: 0.0
                                ),
                                fontSize = 12.sp,
                                color = AppColors.PrimaryGray
                            )
                        }
                    }
                }
                HorizontalDivider(color = AppColors.SurfaceLight)
            }

            // Pricing summary
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = AppColors.SurfaceLight
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (!order.voucherId.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Voucher:", fontSize = 11.sp, color = AppColors.PrimaryGray)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = AppColors.WarmOrange,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = order.voucherId.take(8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.WarmOrange
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Giá gốc:", fontSize = 11.sp, color = AppColors.PrimaryGray)
                        Text(
                            text = "%,.0f đ".format(subtotalVal),
                            fontSize = 11.sp,
                            color = if (discountVal > 0) AppColors.PrimaryGray else AppColors.PrimaryDark
                        )
                    }
                    if (discountVal > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Giảm giá:", fontSize = 11.sp, color = AppColors.PrimaryGray)
                            Text(
                                text = "-%,.0f đ".format(discountVal),
                                fontSize = 11.sp,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Thực trả:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryDark
                        )
                        Text(
                            text = "%,.0f đ".format(totalVal),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF44336)
                        )
                    }
                }
            }

            // Timestamp
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = null,
                    tint = AppColors.PrimaryGray,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = formatOrderDate(order.createdAt),
                    fontSize = 11.sp,
                    color = AppColors.PrimaryGray.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun formatOrderDate(isoString: String): String {
    return try {
        val datePart = isoString.substring(0, 10).split("-")
        val timePart = isoString.substring(11, 16)
        "${datePart[2]}/${datePart[1]}/${datePart[0]}  $timePart"
    } catch (_: Exception) {
        isoString
    }
}
