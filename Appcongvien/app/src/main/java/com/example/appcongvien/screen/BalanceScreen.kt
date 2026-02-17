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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

data class BalanceTransaction(
    val id: String,
    val type: TransactionType,
    val amount: Int,
    val description: String,
    val timestamp: String,
    val gameType: String = ""
)

enum class TransactionType {
    TOP_UP,      // Nạp tiền
    GAME_PLAY,   // Chơi game
    REFUND,      // Hoàn tiền
    BONUS        // Tiền thưởng
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceScreen(
    modifier: Modifier = Modifier,
    onTopUpClick: () -> Unit = {},
    onPaymentHistoryClick: () -> Unit = {},
    onUsageHistoryClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var showBalance by remember { mutableStateOf(true) }

    // Mock data
    val currentBalance = 250000
    val currentPoints = 1250
    val membershipTier = "Vàng"

    val recentTransactions = remember {
        listOf(
            BalanceTransaction(
                id = "1",
                type = TransactionType.GAME_PLAY,
                amount = -50000,
                description = "Đu Quay Khổng Lồ",
                timestamp = "Hôm nay 14:30",
                gameType = "Thrill Ride"
            ),
            BalanceTransaction(
                id = "2",
                type = TransactionType.TOP_UP,
                amount = 100000,
                description = "Nạp tiền qua MoMo",
                timestamp = "Hôm nay 10:15"
            ),
            BalanceTransaction(
                id = "3",
                type = TransactionType.GAME_PLAY,
                amount = -30000,
                description = "Vòng Quay May Mắn",
                timestamp = "Hôm qua 16:45",
                gameType = "Family Fun"
            ),
            BalanceTransaction(
                id = "4",
                type = TransactionType.BONUS,
                amount = 25000,
                description = "Thưởng thành viên Vàng",
                timestamp = "Hôm qua 09:00"
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Số Dư & Lịch Sử",
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

            // Balance Card
            item {
                BalanceCard(
                    balance = currentBalance,
                    showBalance = showBalance,
                    onToggleVisibility = { showBalance = !showBalance },
                    onTopUpClick = onTopUpClick
                )
            }

            // Points & Membership Card
            item {
                PointsCard(
                    points = currentPoints,
                    membershipTier = membershipTier
                )
            }

            // Quick Actions
            item {
                Text(
                    text = "Lịch Sử",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item {
                QuickActionsRow(
                    onPaymentHistoryClick = onPaymentHistoryClick,
                    onUsageHistoryClick = onUsageHistoryClick
                )
            }

            // Recent Transactions Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Giao Dịch Gần Đây",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )

                    OutlinedButton(
                        onClick = onPaymentHistoryClick,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            "Xem tất cả",
                            fontSize = 12.sp,
                            color = AppColors.WarmOrange
                        )
                    }
                }
            }

            // Recent Transactions List
            items(recentTransactions.take(5)) { transaction ->
                TransactionCard(transaction = transaction)
            }

            // Extra space for bottom navigation
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun BalanceCard(
    balance: Int,
    showBalance: Boolean,
    onToggleVisibility: () -> Unit,
    onTopUpClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            AppColors.CardGrad1,
                            AppColors.CardGrad2
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Số dư hiện tại",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (showBalance) "${balance} VND" else "••••••• VND",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            IconButton(
                                onClick = onToggleVisibility,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    if (showBalance) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showBalance) "Ẩn số dư" else "Hiện số dư",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.Wallet,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                // Top Up Button
                Button(
                    onClick = onTopUpClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.9f),
                        contentColor = AppColors.CardPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Nạp Tiền",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PointsCard(
    points: Int,
    membershipTier: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = AppColors.WarmOrangeSoft,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = AppColors.WarmOrange,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Column {
                    Text(
                        text = "Điểm tích lũy",
                        fontSize = 12.sp,
                        color = AppColors.PrimaryGray
                    )
                    Text(
                        text = "$points điểm",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFD700).copy(alpha = 0.2f)
            ) {
                Text(
                    text = "Hạng $membershipTier",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    onPaymentHistoryClick: () -> Unit,
    onUsageHistoryClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        QuickActionCard(
            icon = Icons.Default.TrendingUp,
            title = "Lịch sử nạp tiền",
            subtitle = "Xem các lần nạp",
            iconColor = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f),
            onClick = onPaymentHistoryClick
        )

        QuickActionCard(
            icon = Icons.Default.Receipt,
            title = "Lịch sử sử dụng",
            subtitle = "Chi tiêu & game",
            iconColor = AppColors.WarmOrange,
            modifier = Modifier.weight(1f),
            onClick = onUsageHistoryClick
        )
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.PrimaryDark
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = AppColors.PrimaryGray
                    )
                }
            }

            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = AppColors.PrimaryGray.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun TransactionCard(transaction: BalanceTransaction) {
    val (icon, iconColor, backgroundColor) = getTransactionStyle(transaction.type)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Transaction icon
            Surface(
                shape = CircleShape,
                color = backgroundColor,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Transaction details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = transaction.description,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.PrimaryDark
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = transaction.timestamp,
                        fontSize = 11.sp,
                        color = AppColors.PrimaryGray
                    )

                    if (transaction.gameType.isNotEmpty()) {
                        Text(
                            text = "• ${transaction.gameType}",
                            fontSize = 11.sp,
                            color = AppColors.PrimaryGray
                        )
                    }
                }
            }

            // Amount
            Text(
                text = "${if (transaction.amount > 0) "+" else ""}${transaction.amount} đ",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.amount > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}

@Composable
fun getTransactionStyle(type: TransactionType): Triple<ImageVector, Color, Color> {
    return when (type) {
        TransactionType.TOP_UP -> Triple(
            Icons.Default.TrendingUp,
            Color(0xFF4CAF50),
            Color(0xFF4CAF50).copy(alpha = 0.2f)
        )
        TransactionType.GAME_PLAY -> Triple(
            Icons.Default.TrendingDown,
            AppColors.WarmOrange,
            AppColors.WarmOrangeSoft
        )
        TransactionType.REFUND -> Triple(
            Icons.Default.MonetizationOn,
            Color(0xFF2196F3),
            Color(0xFF2196F3).copy(alpha = 0.2f)
        )
        TransactionType.BONUS -> Triple(
            Icons.Default.Star,
            Color(0xFFFFD700),
            Color(0xFFFFD700).copy(alpha = 0.2f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BalanceScreenPreview() {
    BalanceScreen()
}


