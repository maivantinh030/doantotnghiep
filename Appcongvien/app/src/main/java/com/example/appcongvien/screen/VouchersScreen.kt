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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcongvien.ui.theme.AppColors

data class ParkVoucher(
    val id: String,
    val title: String,
    val description: String,
    val discountType: VoucherDiscountType,
    val value: Int,
    val minPurchase: Int,
    val expiryDate: String,
    val category: VoucherCategory,
    val isNewUser: Boolean = false,
    val isLimited: Boolean = false
)

enum class VoucherDiscountType {
    PERCENTAGE, AMOUNT, FREE_PLAY
}

enum class VoucherCategory {
    ALL, GAMES, FOOD, SPECIAL, NEW_USER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VouchersScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onVoucherClaim: (ParkVoucher) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf(VoucherCategory.ALL) }

    val vouchers = remember {
        listOf(
            ParkVoucher(
                id = "1",
                title = "Giảm 20%",
                description = "Giảm 20% cho tất cả trò chơi mạo hiểm",
                discountType = VoucherDiscountType.PERCENTAGE,
                value = 20,
                minPurchase = 100000,
                expiryDate = "31/12/2024",
                category = VoucherCategory.GAMES
            ),
            ParkVoucher(
                id = "2",
                title = "Miễn phí 3 lượt",
                description = "3 lượt chơi miễn phí cho Đu Quay Khổng Lồ",
                discountType = VoucherDiscountType.FREE_PLAY,
                value = 3,
                minPurchase = 0,
                expiryDate = "30/11/2024",
                category = VoucherCategory.GAMES,
                isLimited = true
            ),
            ParkVoucher(
                id = "3",
                title = "Giảm 50K",
                description = "Giảm 50K cho đơn hàng từ 200K",
                discountType = VoucherDiscountType.AMOUNT,
                value = 50000,
                minPurchase = 200000,
                expiryDate = "15/12/2024",
                category = VoucherCategory.ALL
            ),
            ParkVoucher(
                id = "4",
                title = "Chào mừng thành viên",
                description = "Giảm 30% cho khách hàng mới",
                discountType = VoucherDiscountType.PERCENTAGE,
                value = 30,
                minPurchase = 50000,
                expiryDate = "25/12/2024",
                category = VoucherCategory.NEW_USER,
                isNewUser = true
            ),
//            ParkVoucher(
//                id = "5",
//                title = "Combo ăn uống",
//                description = "Giảm 25% cho tất cả combo ăn uống",
//                discountType = VoucherDiscountType.PERCENTAGE,
//                value = 25,
//                minPurchase = 80000,
//                expiryDate = "20/12/2024",
//                category = VoucherCategory.FOOD
//            ),
            ParkVoucher(
                id = "6",
                title = "Sinh nhật vui vẻ",
                description = "Voucher đặc biệt 100K trong tháng sinh nhật",
                discountType = VoucherDiscountType.AMOUNT,
                value = 100000,
                minPurchase = 300000,
                expiryDate = "31/01/2025",
                category = VoucherCategory.SPECIAL,
                isLimited = true
            )
        )
    }

    val categories = remember {
        listOf(
            VoucherCategory.ALL to "Tất cả",
            VoucherCategory.GAMES to "Trò chơi",
//            VoucherCategory.FOOD to "Ăn uống",
            VoucherCategory.NEW_USER to "Thành viên mới",
            VoucherCategory.SPECIAL to "Đặc biệt"
        )
    }

    val filteredVouchers = if (selectedCategory == VoucherCategory.ALL) {
        vouchers
    } else {
        vouchers.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Voucher Khuyến Mãi",
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

            // Header banner
            item {
                VoucherBanner()
            }

            // Category filters
            item {
                Text(
                    text = "Danh mục",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                ) {
                    items(categories) { (category, label) ->
                        FilterChip(
                            onClick = { selectedCategory = category },
                            label = { Text(label) },
                            selected = selectedCategory == category,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppColors.WarmOrange,
                                selectedLabelColor = Color.White,
                                labelColor = AppColors.PrimaryDark
                            )
                        )
                    }
                }
            }

            // Vouchers list
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Có ${filteredVouchers.size} voucher khả dụng",
                        fontSize = 14.sp,
                        color = AppColors.PrimaryGray
                    )
                }
            }

            items(filteredVouchers) { voucher ->
                VoucherCard(
                    voucher = voucher,
                    onClaimClick = { onVoucherClaim(voucher) }
                )
            }

            // Extra space for bottom navigation
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun VoucherBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            AppColors.WarmOrange.copy(alpha = 0.9f),
                            AppColors.WarmOrange.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "🎪 Voucher Park Adventure",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Tiết kiệm hơn với voucher độc quyền!",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Redeem,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VoucherCard(
    voucher: ParkVoucher,
    onClaimClick: () -> Unit
) {
    val (voucherIcon, iconColor) = getVoucherStyle(voucher.discountType, voucher.category)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
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

                // Voucher icon and value
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = iconColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                voucherIcon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = getVoucherDisplayValue(voucher),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = iconColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = voucher.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark
                            )

                            // Special badges
                            if (voucher.isNewUser) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "Mới",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (voucher.isLimited) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF44336).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "Giới hạn",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF44336),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = voucher.description,
                            fontSize = 13.sp,
                            color = AppColors.PrimaryGray
                        )
                    }
                }
            }

            // Details section
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (voucher.minPurchase > 0) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Điều kiện:",
                            fontSize = 12.sp,
                            color = AppColors.PrimaryGray
                        )
                        Text(
                            text = "Đơn từ ${voucher.minPurchase / 1000}K",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.WarmOrange
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HSD: ${voucher.expiryDate}",
                        fontSize = 12.sp,
                        color = AppColors.PrimaryGray
                    )

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Save voucher */ },
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AppColors.WarmOrange
                            )
                        ) {
                            Text(
                                "Lưu",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = onClaimClick,
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.WarmOrange,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                "Nhận ngay",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getVoucherStyle(discountType: VoucherDiscountType, category: VoucherCategory): Pair<ImageVector, Color> {
    return when (discountType) {
        VoucherDiscountType.PERCENTAGE -> Pair(Icons.Default.Percent, AppColors.WarmOrange)
        VoucherDiscountType.AMOUNT -> Pair(Icons.Default.LocalOffer, Color(0xFF4CAF50))
        VoucherDiscountType.FREE_PLAY -> Pair(Icons.Default.Star, Color(0xFFFFD700))
    }
}

fun getVoucherDisplayValue(voucher: ParkVoucher): String {
    return when (voucher.discountType) {
        VoucherDiscountType.PERCENTAGE -> "${voucher.value}%"
        VoucherDiscountType.AMOUNT -> "${voucher.value / 1000}K"
        VoucherDiscountType.FREE_PLAY -> "${voucher.value} lượt"
    }
}

@Preview(showBackground = true)
@Composable
fun VouchersScreenPreview() {
    VouchersScreen()
}