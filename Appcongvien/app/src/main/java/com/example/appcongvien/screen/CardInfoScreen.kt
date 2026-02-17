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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcongvien.ui.theme.AppColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class MemberCard(
    val cardNumber: String,
    val holderName: String,
    val expiryDate: String,
    val membershipLevel: MembershipLevel,
    val issueDate: String,
    val status: CardStatus,
    val balance: Int,
    val points: Int,
    val isLocked: Boolean = false
)

enum class MembershipLevel(val displayName: String, val color: Color) {
    BRONZE("Đồng", Color(0xFFCD7F32)),
    SILVER("Bạc", Color(0xFFC0C0C0)),
    GOLD("Vàng", Color(0xFFFFD700)),
    PLATINUM("Bạch Kim", Color(0xFFE5E4E2))
}

enum class CardStatus(val displayName: String, val color: Color) {
    ACTIVE("Đang hoạt động", Color(0xFF4CAF50)),
    EXPIRED("Hết hạn", Color(0xFFF44336)),
    SUSPENDED("Tạm khóa", Color(0xFFFF9800))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardInfoScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onLockToggle: () -> Unit = {},
    onScanCard: () -> Unit = {},
    onMembershipDetailsClick: () -> Unit = {}
) {
    var showCardNumber by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Mock card data - in real app, get from ViewModel
    val card = remember {
        MemberCard(
            cardNumber = "PA2024001250",
            holderName = "Mai Văn Tĩnh",
            expiryDate = "12/2025",
            membershipLevel = MembershipLevel.GOLD,
            issueDate = "15/01/2024",
            status = CardStatus.ACTIVE,
            balance = 250000,
            points = 1250,
            isLocked = false
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Thông Tin Thẻ",
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

        Column(
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
                )
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Virtual Card Preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(20.dp),
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
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {

                        // Header row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Park Adventure",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = card.membershipLevel.color.copy(alpha = 0.9f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = card.membershipLevel.displayName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        // Card number section
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (showCardNumber) card.cardNumber else "•••• •••• ••••",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 2.sp
                                )

                                IconButton(
                                    onClick = { showCardNumber = !showCardNumber },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        if (showCardNumber) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showCardNumber) "Ẩn số thẻ" else "Hiện số thẻ",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = card.holderName,
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = card.expiryDate,
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                OutlinedButton(
//                    onClick = onScanCard,
//                    modifier = Modifier.weight(1f),
//                    shape = RoundedCornerShape(12.dp),
//                    colors = ButtonDefaults.outlinedButtonColors(
//                        contentColor = AppColors.WarmOrange
//                    )
//                ) {
//                    Icon(
//                        Icons.Default.QrCode,
//                        contentDescription = null,
//                        modifier = Modifier.size(18.dp)
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        "Quét thẻ",
//                        fontSize = 12.sp,
//                        fontWeight = FontWeight.SemiBold
//                    )
//                }
//
//                OutlinedButton(
//                    onClick = { /* Copy card number */ },
//                    modifier = Modifier.weight(1f),
//                    shape = RoundedCornerShape(12.dp),
//                    colors = ButtonDefaults.outlinedButtonColors(
//                        contentColor = AppColors.WarmOrange
//                    )
//                ) {
//                    Icon(
//                        Icons.Default.ContentCopy,
//                        contentDescription = null,
//                        modifier = Modifier.size(18.dp)
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        "Copy số thẻ",
//                        fontSize = 12.sp,
//                        fontWeight = FontWeight.SemiBold
//                    )
//                }
//            }

            // Security Control
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Trạng thái thẻ",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryDark
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = card.status.color.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = card.status.color,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = card.status.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = card.status.color
                                )
                            }
                        }
                    }

                    // Lock/Unlock button
                    Button(
                        onClick = onLockToggle,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = if (card.isLocked) {
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            )
                        } else {
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF44336),
                                contentColor = Color.White
                            )
                        }
                    ) {
                        Icon(
                            if (card.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (card.isLocked) "Mở Khóa Thẻ" else "Khóa Thẻ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Card Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Chi Tiết Thẻ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )

                    InfoRow(
                        label = "Loại thẻ",
                        value = "Thẻ thành viên ${card.membershipLevel.displayName}",
                        valueColor = card.membershipLevel.color
                    )
                    InfoRow(
                        label = "Ngày phát hành",
                        value = card.issueDate
                    )
                    InfoRow(
                        label = "Ngày hết hạn",
                        value = card.expiryDate
                    )
                    InfoRow(
                        label = "Số dư hiện tại",
                        value = "${card.balance} VND",
                        valueColor = AppColors.WarmOrange
                    )
                    InfoRow(
                        label = "Điểm tích lũy",
                        value = "${card.points} điểm",
                        valueColor = AppColors.WarmOrange
                    )
                }
            }

            // Enhanced Membership Benefits with Details Button
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quyền Lợi Thành Viên",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryDark
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = card.membershipLevel.color.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Hạng ${card.membershipLevel.displayName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = card.membershipLevel.color,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Current level benefits summary
                    when (card.membershipLevel) {
                        MembershipLevel.BRONZE -> {
                            BenefitItem("Giảm giá 5% tất cả game")
                            BenefitItem("1 lượt chơi miễn phí/tháng")
                            BenefitItem("Ưu tiên đặt chỗ")
                        }
                        MembershipLevel.SILVER -> {
                            BenefitItem("Giảm giá 10% tất cả game")
                            BenefitItem("3 lượt chơi miễn phí/tháng")
                            BenefitItem("Ưu tiên hỗ trợ khách hàng")
                            BenefitItem("Tích điểm x1.5")
                        }
                        MembershipLevel.GOLD -> {
                            BenefitItem("Giảm giá 15% tất cả game")
                            BenefitItem("5 lượt chơi miễn phí/tháng")
                            BenefitItem("Hỗ trợ khách hàng VIP")
                            BenefitItem("Tích điểm x2.0")
                            BenefitItem("Quà sinh nhật đặc biệt")
                        }
                        MembershipLevel.PLATINUM -> {
                            BenefitItem("Giảm giá 25% tất cả game")
                            BenefitItem("10 lượt chơi miễn phí/tháng")
                            BenefitItem("Hỗ trợ 24/7 riêng biệt")
                            BenefitItem("Tích điểm x3.0")
                            BenefitItem("Quà sinh nhật cao cấp")
                            BenefitItem("Sự kiện VIP độc quyền")
                        }
                    }

                    // Call-to-action for detailed membership info
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = AppColors.WarmOrange.copy(alpha = 0.1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                        text = "Khám phá hệ thống hạng",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.WarmOrange
                                    )
                                    Text(
                                        text = "Xem chi tiết tất cả hạng thành viên và cách nâng cấp",
                                        fontSize = 12.sp,
                                        color = AppColors.PrimaryGray,
                                        lineHeight = 16.sp
                                    )
                                }

                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = AppColors.WarmOrange,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Button(
                                onClick = onMembershipDetailsClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.WarmOrange,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Xem Chi Tiết Hạng Thành Viên",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Extra space for bottom navigation
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = AppColors.PrimaryDark
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = AppColors.PrimaryGray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun BenefitItem(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = AppColors.WarmOrange,
            modifier = Modifier.size(6.dp)
        ) {}

        Text(
            text = text,
            fontSize = 13.sp,
            color = AppColors.PrimaryGray,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CardInfoScreenPreview() {
    CardInfoScreen()
}


