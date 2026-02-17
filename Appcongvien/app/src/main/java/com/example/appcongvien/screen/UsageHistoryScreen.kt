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
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingDown
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

data class UsageRecord(
    val id: String,
    val gameName: String,
    val gameType: GameCategory,
    val originalPrice: Int,
    val discountAmount: Int,
    val finalAmount: Int,
    val turnsPlayed: Int,
    val timestamp: String,
    val voucherUsed: String = ""
)

enum class GameCategory {
    THRILL_RIDE, FAMILY_FUN, ARCADE, ADVENTURE, KIDS_ZONE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageHistoryScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    // Mock usage data
    val usageRecords = remember {
        listOf(
            UsageRecord(
                id = "1",
                gameName = "Đu Quay Khổng Lồ",
                gameType = GameCategory.THRILL_RIDE,
                originalPrice = 60000,
                discountAmount = 10000,
                finalAmount = 50000,
                turnsPlayed = 2,
                timestamp = "Hôm nay, 14:30"
            ),
            UsageRecord(
                id = "2",
                gameName = "Vòng Quay May Mắn",
                gameType = GameCategory.FAMILY_FUN,
                originalPrice = 30000,
                discountAmount = 0,
                finalAmount = 30000,
                turnsPlayed = 1,
                timestamp = "Hôm qua, 16:45"
            ),
            UsageRecord(
                id = "3",
                gameName = "Tàu Lượn Siêu Tốc",
                gameType = GameCategory.THRILL_RIDE,
                originalPrice = 80000,
                discountAmount = 16000,
                finalAmount = 64000,
                turnsPlayed = 1,
                timestamp = "Hôm qua, 15:20",
                voucherUsed = "Giảm 20%"
            ),
            UsageRecord(
                id = "4",
                gameName = "Nhà Ma Bí Ẩn",
                gameType = GameCategory.ADVENTURE,
                originalPrice = 70000,
                discountAmount = 10500,
                finalAmount = 59500,
                turnsPlayed = 1,
                timestamp = "2 ngày trước, 18:15"
            ),
            UsageRecord(
                id = "5",
                gameName = "Khu Vui Chơi Trẻ Em",
                gameType = GameCategory.KIDS_ZONE,
                originalPrice = 25000,
                discountAmount = 0,
                finalAmount = 25000,
                turnsPlayed = 3,
                timestamp = "3 ngày trước, 10:30"
            )
        )
    }

    val totalSpent = usageRecords.sumOf { it.finalAmount }
    val totalSaved = usageRecords.sumOf { it.discountAmount }
    val totalTurns = usageRecords.sumOf { it.turnsPlayed }

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

            // Summary Cards
            item {
                UsageSummaryCard(
                    totalSpent = totalSpent,
                    totalSaved = totalSaved,
                    totalTurns = totalTurns
                )
            }

            // Usage Records Header
            item {
                Text(
                    text = "Chi Tiết Sử Dụng",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
            }

            // Usage Records
            items(usageRecords) { record ->
                UsageCard(usage = record)
            }

            // Extra space for bottom navigation
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun UsageSummaryCard(
    totalSpent: Int,
    totalSaved: Int,
    totalTurns: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    icon = Icons.Default.TrendingDown,
                    iconColor = AppColors.WarmOrange,
                    title = "Chi tiêu",
                    value = "${totalSpent} đ"
                )

                SummaryItem(
                    icon = Icons.Default.LocalOffer,
                    iconColor = Color(0xFF4CAF50),
                    title = "Tiết kiệm",
                    value = "${totalSaved} đ"
                )
            }

            // Bottom row - centered
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                SummaryItem(
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
fun SummaryItem(
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
fun UsageCard(usage: UsageRecord) {
    val categoryColor = getGameCategoryColor(usage.gameType)

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

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = usage.gameName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = categoryColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = getGameCategoryName(usage.gameType),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = categoryColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "${usage.turnsPlayed} lượt",
                            fontSize = 12.sp,
                            color = AppColors.PrimaryGray
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "-${usage.finalAmount} đ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF44336)
                    )

                    if (usage.discountAmount > 0) {
                        Text(
                            text = "Tiết kiệm ${usage.discountAmount}đ",
                            fontSize = 10.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            // Pricing details
            if (usage.discountAmount > 0 || usage.voucherUsed.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = AppColors.SurfaceLight
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (usage.voucherUsed.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Áp dụng:",
                                    fontSize = 11.sp,
                                    color = AppColors.PrimaryGray
                                )
                                Text(
                                    text = usage.voucherUsed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.WarmOrange
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Giá gốc:",
                                fontSize = 11.sp,
                                color = AppColors.PrimaryGray
                            )
                            Text(
                                text = "${usage.originalPrice}đ",
                                fontSize = 11.sp,
                                color = if (usage.discountAmount > 0) AppColors.PrimaryGray else AppColors.PrimaryDark
                            )
                        }

                        if (usage.discountAmount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Giảm giá:",
                                    fontSize = 11.sp,
                                    color = AppColors.PrimaryGray
                                )
                                Text(
                                    text = "-${usage.discountAmount}đ",
                                    fontSize = 11.sp,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                }
            }

            // Timestamp
            Text(
                text = usage.timestamp,
                fontSize = 11.sp,
                color = AppColors.PrimaryGray.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun getGameCategoryColor(category: GameCategory): Color {
    return when (category) {
        GameCategory.THRILL_RIDE -> Color(0xFFF44336)
        GameCategory.FAMILY_FUN -> AppColors.WarmOrange
        GameCategory.ARCADE -> Color(0xFF9C27B0)
        GameCategory.ADVENTURE -> Color(0xFF795548)
        GameCategory.KIDS_ZONE -> Color(0xFF4CAF50)
    }
}

fun getGameCategoryName(category: GameCategory): String {
    return when (category) {
        GameCategory.THRILL_RIDE -> "Mạo hiểm"
        GameCategory.FAMILY_FUN -> "Gia đình"
        GameCategory.ARCADE -> "Arcade"
        GameCategory.ADVENTURE -> "Phiêu lưu"
        GameCategory.KIDS_ZONE -> "Trẻ em"
    }
}

@Preview(showBackground = true)
@Composable
fun UsageHistoryScreenPreview() {
    UsageHistoryScreen()
}


