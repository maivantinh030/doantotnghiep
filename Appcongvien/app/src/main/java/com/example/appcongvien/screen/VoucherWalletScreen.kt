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
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.remember
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

data class MyVoucher(
    val id: String,
    val title: String,
    val description: String,
    val discountType: VoucherDiscountType,
    val value: Int,
    val minPurchase: Int,
    val expiryDate: String,
    val category: VoucherCategory,
    val status: VoucherStatus,
    val savedDate: String,
    val daysUntilExpiry: Int
)

enum class VoucherStatus {
    ACTIVE, EXPIRING_SOON, EXPIRED, USED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherWalletScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onVoucherUse: (MyVoucher) -> Unit = {}
) {
    val myVouchers = remember {
        listOf(
            MyVoucher(
                id = "1",
                title = "Giảm 20%",
                description = "Giảm 20% cho tất cả trò chơi mạo hiểm",
                discountType = VoucherDiscountType.PERCENTAGE,
                value = 20,
                minPurchase = 100000,
                expiryDate = "25/03/2026",
                category = VoucherCategory.GAMES,
                status = VoucherStatus.ACTIVE,
                savedDate = "15/11/2025",
                daysUntilExpiry = 45
            ),
            MyVoucher(
                id = "2",
                title = "Miễn phí 3 lượt",
                description = "3 lượt chơi miễn phí cho Đu Quay Khổng Lồ",
                discountType = VoucherDiscountType.FREE_PLAY,
                value = 3,
                minPurchase = 0,
                expiryDate = "24/01/2026",
                category = VoucherCategory.GAMES,
                status = VoucherStatus.EXPIRING_SOON,
                savedDate = "21/01/2026",
                daysUntilExpiry = 3
            ),
            MyVoucher(
                id = "3",
                title = "Giảm 50K",
                description = "Giảm 50K cho đơn hàng từ 200K",
                discountType = VoucherDiscountType.AMOUNT,
                value = 50000,
                minPurchase = 200000,
                expiryDate = "20/02/2026",
                category = VoucherCategory.ALL,
                status = VoucherStatus.ACTIVE,
                savedDate = "15/12/2025",
                daysUntilExpiry = 20
            ),
//            MyVoucher(
//                id = "4",
//                title = "Combo ăn uống",
//                description = "Giảm 25% cho tất cả combo ăn uống",
//                discountType = VoucherDiscountType.PERCENTAGE,
//                value = 25,
//                minPurchase = 80000,
//                expiryDate = "01/11/2024",
//                category = VoucherCategory.FOOD,
//                status = VoucherStatus.EXPIRED,
//                savedDate = "05/10/2024",
//                daysUntilExpiry = -20
//            ),
            MyVoucher(
                id = "5",
                title = "Chào mừng thành viên",
                description = "Giảm 30% cho khách hàng mới",
                discountType = VoucherDiscountType.PERCENTAGE,
                value = 30,
                minPurchase = 50000,
                expiryDate = "10/11/2026",
                category = VoucherCategory.NEW_USER,
                status = VoucherStatus.USED,
                savedDate = "25/10/2026",
                daysUntilExpiry = -10
            )
        )
    }

    val activeVouchers = myVouchers.filter { it.status == VoucherStatus.ACTIVE }
    val expiringSoonVouchers = myVouchers.filter { it.status == VoucherStatus.EXPIRING_SOON }
    val expiredVouchers = myVouchers.filter { it.status == VoucherStatus.EXPIRED }
    val usedVouchers = myVouchers.filter { it.status == VoucherStatus.USED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ví Voucher",
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

            // Wallet summary
            item {
                WalletSummary(
                    totalVouchers = myVouchers.size,
                    activeVouchers = activeVouchers.size,
                    expiringSoon = expiringSoonVouchers.size
                )
            }

            // Expiring Soon Section
            if (expiringSoonVouchers.isNotEmpty()) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Sắp hết hạn (${expiringSoonVouchers.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryDark
                        )
                    }
                }

                items(expiringSoonVouchers) { voucher ->
                    MyVoucherCard(
                        voucher = voucher,
                        onUseClick = { onVoucherUse(voucher) }
                    )
                }
            }

            // Active Vouchers Section
            if (activeVouchers.isNotEmpty()) {
                item {
                    Text(
                        text = "Có thể sử dụng (${activeVouchers.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )
                }

                items(activeVouchers) { voucher ->
                    MyVoucherCard(
                        voucher = voucher,
                        onUseClick = { onVoucherUse(voucher) }
                    )
                }
            }

            // Used Vouchers Section
            if (usedVouchers.isNotEmpty()) {
                item {
                    Text(
                        text = "Đã sử dụng (${usedVouchers.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )
                }

                items(usedVouchers) { voucher ->
                    MyVoucherCard(
                        voucher = voucher,
                        onUseClick = { onVoucherUse(voucher) }
                    )
                }
            }

            // Expired Vouchers Section
            if (expiredVouchers.isNotEmpty()) {
                item {
                    Text(
                        text = "Đã hết hạn (${expiredVouchers.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )
                }

                items(expiredVouchers) { voucher ->
                    MyVoucherCard(
                        voucher = voucher,
                        onUseClick = { onVoucherUse(voucher) }
                    )
                }
            }

            // Extra space for bottom navigation
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun WalletSummary(
    totalVouchers: Int,
    activeVouchers: Int,
    expiringSoon: Int
) {
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
                            AppColors.WarmOrange.copy(alpha = 0.1f),
                            AppColors.WarmOrange.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎫 Ví Voucher của bạn",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )

                    Surface(
                        shape = CircleShape,
                        color = AppColors.WarmOrangeSoft,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Text(
                            text = "$totalVouchers",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.WarmOrange,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryItem(
                        label = "Có thể dùng",
                        value = activeVouchers,
                        color = Color(0xFF4CAF50)
                    )

                    SummaryItem(
                        label = "Sắp hết hạn",
                        value = expiringSoon,
                        color = Color(0xFFFFC107)
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryItem(
    label: String,
    value: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$value",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = AppColors.PrimaryGray
        )
    }
}

@Composable
fun MyVoucherCard(
    voucher: MyVoucher,
    onUseClick: () -> Unit
) {
    val (voucherIcon, iconColor) = getMyVoucherStyle(voucher.discountType, voucher.status)
    val cardColors = getVoucherCardColors(voucher.status)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColors.first
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (voucher.status == VoucherStatus.ACTIVE || voucher.status == VoucherStatus.EXPIRING_SOON) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Header with status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        Text(
                            text = voucher.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = cardColors.second
                        )
                        Text(
                            text = voucher.description,
                            fontSize = 13.sp,
                            color = AppColors.PrimaryGray.copy(
                                alpha = if (voucher.status == VoucherStatus.EXPIRED || voucher.status == VoucherStatus.USED) 0.6f else 1f
                            )
                        )
                    }
                }

//                // Status badge
//                Surface(
//                    shape = RoundedCornerShape(8.dp),
//                    color = getStatusBadgeColor(voucher.status).copy(alpha = 0.2f)
//                ) {
//                    Text(
//                        text = getStatusText(voucher.status),
//                        fontSize = 10.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = getStatusBadgeColor(voucher.status),
//                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
//                    )
//                }
            }

            // Voucher details
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (voucher.minPurchase > 0) {
                    Text(
                        text = "Điều kiện: Đơn từ ${voucher.minPurchase / 1000}K",
                        fontSize = 12.sp,
                        color = AppColors.PrimaryGray
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "HSD: ${voucher.expiryDate}",
                            fontSize = 12.sp,
                            color = when (voucher.status) {
                                VoucherStatus.EXPIRING_SOON -> Color(0xFFFFC107)
                                VoucherStatus.EXPIRED -> Color(0xFFF44336)
                                else -> AppColors.PrimaryGray
                            },
                            fontWeight = if (voucher.status == VoucherStatus.EXPIRING_SOON) FontWeight.Bold else FontWeight.Normal
                        )

                        if (voucher.status == VoucherStatus.EXPIRING_SOON) {
                            Text(
                                text = "Còn ${voucher.daysUntilExpiry} ngày",
                                fontSize = 11.sp,
                                color = Color(0xFFF44336),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Lưu: ${voucher.savedDate}",
                        fontSize = 11.sp,
                        color = AppColors.PrimaryGray.copy(alpha = 0.7f)
                    )
                }
            }

            // Action buttons
            when (voucher.status) {
                VoucherStatus.ACTIVE, VoucherStatus.EXPIRING_SOON -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
//                        OutlinedButton(
//                            onClick = { /* Share voucher */ },
//                            modifier = Modifier.height(36.dp),
//                            colors = ButtonDefaults.outlinedButtonColors(
//                                contentColor = AppColors.WarmOrange
//                            )
//                        ) {
//                            Text(
//                                "Chia sẻ",
//                                fontSize = 12.sp
//                            )
//                        }

                        Button(
                            onClick = onUseClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.WarmOrange,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                "Sử dụng ngay",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                VoucherStatus.USED -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Đã sử dụng thành công",
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                VoucherStatus.EXPIRED -> {
                    Text(
                        text = "Voucher đã hết hạn",
                        fontSize = 12.sp,
                        color = Color(0xFFF44336),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun getMyVoucherStyle(discountType: VoucherDiscountType, status: VoucherStatus): Pair<ImageVector, Color> {
    val baseColor = when (discountType) {
        VoucherDiscountType.PERCENTAGE -> AppColors.WarmOrange
        VoucherDiscountType.AMOUNT -> Color(0xFF4CAF50)
        VoucherDiscountType.FREE_PLAY -> Color(0xFFFFD700)
    }

    val finalColor = when (status) {
        VoucherStatus.EXPIRED, VoucherStatus.USED -> baseColor.copy(alpha = 0.5f)
        else -> baseColor
    }

    val icon = when (discountType) {
        VoucherDiscountType.PERCENTAGE -> Icons.Default.Percent
        VoucherDiscountType.AMOUNT -> Icons.Default.LocalOffer
        VoucherDiscountType.FREE_PLAY -> Icons.Default.Star
    }

    return Pair(icon, finalColor)
}

fun getVoucherCardColors(status: VoucherStatus): Pair<Color, Color> {
    return when (status) {
        VoucherStatus.ACTIVE -> Pair(Color.White, AppColors.PrimaryDark)
        VoucherStatus.EXPIRING_SOON -> Pair(Color(0xFFFFF8E1), AppColors.PrimaryDark)
        VoucherStatus.EXPIRED -> Pair(Color(0xFFFAFAFA), AppColors.PrimaryGray)
        VoucherStatus.USED -> Pair(Color(0xFFF1F8E9), AppColors.PrimaryGray)
    }
}

fun getStatusBadgeColor(status: VoucherStatus): Color {
    return when (status) {
        VoucherStatus.ACTIVE -> Color(0xFF4CAF50)
        VoucherStatus.EXPIRING_SOON -> Color(0xFFFFC107)
        VoucherStatus.EXPIRED -> Color(0xFFF44336)
        VoucherStatus.USED -> Color(0xFF9E9E9E)
    }
}

fun getStatusText(status: VoucherStatus): String {
    return when (status) {
        VoucherStatus.ACTIVE -> "Có hiệu lực"
        VoucherStatus.EXPIRING_SOON -> "Sắp hết hạn"
        VoucherStatus.EXPIRED -> "Hết hạn"
        VoucherStatus.USED -> "Đã dùng"
    }
}

fun getVoucherDisplayValue(voucher: MyVoucher): String {
    return when (voucher.discountType) {
        VoucherDiscountType.PERCENTAGE -> "${voucher.value}%"
        VoucherDiscountType.AMOUNT -> "${voucher.value / 1000}K"
        VoucherDiscountType.FREE_PLAY -> "${voucher.value} lượt"
    }
}

@Preview(showBackground = true)
@Composable
fun VoucherWalletScreenPreview() {
    VoucherWalletScreen()
}