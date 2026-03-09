package com.example.appcongvien.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.appcongvien.components.ParkTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcongvien.ui.theme.AppColors

data class MembershipTier(
    val name: String,
    val color: Color,
    val gradientColors: List<Color>,
    val discountPercent: Int,
    val freeTurnsPerMonth: Int,
    val birthdayGift: String,
    val minPoints: Int,
    val specialBenefits: List<String>
)

data class MembershipProgress(
    val currentPoints: Int,
    val currentTier: MembershipTier,
    val nextTier: MembershipTier?,
    val progressToNext: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberCardScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val currentPoints = 1250

    val allTiers = remember {
        listOf(
            MembershipTier(
                name = "Đồng",
                color = Color(0xFFCD7F32),
                gradientColors = listOf(Color(0xFFCD7F32), Color(0xFFB87333)),
                discountPercent = 5,
                freeTurnsPerMonth = 1,
                birthdayGift = "Voucher 50K",
                minPoints = 0,
                specialBenefits = listOf(
                    "Giảm giá 5% tất cả game",
                    "1 lượt chơi miễn phí/tháng",
                    "Ưu tiên đặt chỗ"
                )
            ),
            MembershipTier(
                name = "Bạc",
                color = Color(0xFFC0C0C0),
                gradientColors = listOf(Color(0xFFC0C0C0), Color(0xFFA8A8A8)),
                discountPercent = 10,
                freeTurnsPerMonth = 3,
                birthdayGift = "Voucher 100K",
                minPoints = 1000,
                specialBenefits = listOf(
                    "Giảm giá 10% tất cả game",
                    "3 lượt chơi miễn phí/tháng",
                    "Ưu tiên hỗ trợ khách hàng",
                    "Tích điểm x1.5"
                )
            ),
            MembershipTier(
                name = "Vàng",
                color = Color(0xFFFFD700),
                gradientColors = listOf(Color(0xFFFFD700), Color(0xFFDAA520)),
                discountPercent = 15,
                freeTurnsPerMonth = 5,
                birthdayGift = "Voucher 200K",
                minPoints = 2000,
                specialBenefits = listOf(
                    "Giảm giá 15% tất cả game",
                    "5 lượt chơi miễn phí/tháng",
                    "Hỗ trợ khách hàng VIP",
                    "Tích điểm x2.0",
                    "Quà sinh nhật đặc biệt",
                    "Ưu tiên game mới"
                )
            ),
            MembershipTier(
                name = "Bạch Kim",
                color = Color(0xFFE5E4E2),
                gradientColors = listOf(Color(0xFFE5E4E2), Color(0xFFD3D3D3)),
                discountPercent = 25,
                freeTurnsPerMonth = 10,
                birthdayGift = "Voucher 500K + Quà VIP",
                minPoints = 5000,
                specialBenefits = listOf(
                    "Giảm giá 25% tất cả game",
                    "10 lượt chơi miễn phí/tháng",
                    "Hỗ trợ 24/7 riêng biệt",
                    "Tích điểm x3.0",
                    "Quà sinh nhật cao cấp",
                    "Truy cập sớm game mới",
                    "Sự kiện VIP độc quyền",
                    "Concierge service"
                )
            )
        )
    }

    val membershipProgress = remember(currentPoints) {
        val currentTier = allTiers.lastOrNull { it.minPoints <= currentPoints } ?: allTiers.first()
        val nextTier = allTiers.find { it.minPoints > currentPoints }
        val progressToNext = if (nextTier != null) {
            val pointsInCurrentTier = currentPoints - currentTier.minPoints
            val pointsNeededForNext = nextTier.minPoints - currentTier.minPoints
            pointsInCurrentTier.toFloat() / pointsNeededForNext
        } else {
            1f
        }

        MembershipProgress(currentPoints, currentTier, nextTier, progressToNext)
    }

    val progressAnimation by animateFloatAsState(
        targetValue = membershipProgress.progressToNext,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "progress"
    )

    Scaffold(
        topBar = {
            ParkTopAppBar(
                title = "Thẻ Thành Viên",
                onBackClick = onBackClick
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

            // Current Membership Card
            item {
                CurrentMembershipCard(membershipProgress = membershipProgress)
            }

            // Progress to Next Tier
            if (membershipProgress.nextTier != null) {
                item {
                    NextTierProgressCard(
                        membershipProgress = membershipProgress,
                        progressAnimation = progressAnimation
                    )
                }
            }

            // Current Benefits
            item {
                CurrentBenefitsCard(currentTier = membershipProgress.currentTier)
            }

            // All Membership Tiers
            item {
                Text(
                    text = "Hệ Thống Hạng Thành Viên",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(allTiers.reversed()) { tier ->
                TierCard(
                    tier = tier,
                    isCurrent = tier.name == membershipProgress.currentTier.name,
                    isAchieved = membershipProgress.currentPoints >= tier.minPoints
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
fun CurrentMembershipCard(membershipProgress: MembershipProgress) {
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
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(membershipProgress.currentTier.gradientColors)
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
                            text = "Hạng hiện tại",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = membershipProgress.currentTier.name,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            repeat(getTierStars(membershipProgress.currentTier.name)) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            Icons.Default.Stars,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(16.dp)
                                .size(32.dp)
                        )
                    }
                }

                // Points and Discount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Điểm tích lũy",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${membershipProgress.currentPoints} điểm",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Giảm giá",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${membershipProgress.currentTier.discountPercent}%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Free turns
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "${membershipProgress.currentTier.freeTurnsPerMonth} lượt chơi miễn phí mỗi tháng",
                            fontSize = 13.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NextTierProgressCard(
    membershipProgress: MembershipProgress,
    progressAnimation: Float
) {
    val nextTier = membershipProgress.nextTier!!
    val pointsNeeded = nextTier.minPoints - membershipProgress.currentPoints

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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tiến độ lên hạng",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )
                    Text(
                        text = "Hạng tiếp theo: ${nextTier.name}",
                        fontSize = 12.sp,
                        color = AppColors.PrimaryGray
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = nextTier.color.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = nextTier.color,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Progress bar
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${membershipProgress.currentPoints} điểm",
                        fontSize = 12.sp,
                        color = AppColors.PrimaryGray
                    )
                    Text(
                        text = "${nextTier.minPoints} điểm",
                        fontSize = 12.sp,
                        color = AppColors.PrimaryGray
                    )
                }

                LinearProgressIndicator(
                    progress = { progressAnimation },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = AppColors.WarmOrange,
                    trackColor = AppColors.SurfaceLight
                )

                Text(
                    text = "Còn thiếu $pointsNeeded điểm để lên hạng ${nextTier.name}",
                    fontSize = 12.sp,
                    color = AppColors.PrimaryGray,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CurrentBenefitsCard(currentTier: MembershipTier) {
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocalOffer,
                    contentDescription = null,
                    tint = AppColors.WarmOrange,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Quyền lợi hạng ${currentTier.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currentTier.specialBenefits.forEach { benefit ->
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
                            text = benefit,
                            fontSize = 13.sp,
                            color = AppColors.PrimaryGray,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Birthday gift highlight
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.WarmOrange.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = AppColors.WarmOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Quà sinh nhật: ${currentTier.birthdayGift}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.WarmOrange
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TierCard(
    tier: MembershipTier,
    isCurrent: Boolean,
    isAchieved: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent)
                tier.color.copy(alpha = 0.1f)
            else
                Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation =  2.dp
        ),
//        border = if (isCurrent)
//            androidx.compose.foundation.BorderStroke(2.dp, tier.color)
//        else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // Header
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
                        shape = CircleShape,
                        color = tier.color,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            repeat(getTierStars(tier.name)) { index ->
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(if (getTierStars(tier.name) == 1) 20.dp else 12.dp)
                                )
                            }
                        }
                    }

                    Column {
                        Text(
                            text = tier.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) tier.color else AppColors.PrimaryDark
                        )
                        Text(
                            text = "Từ ${tier.minPoints} điểm",
                            fontSize = 12.sp,
                            color = AppColors.PrimaryGray
                        )
                    }
                }

                // Status indicator
                when {
                    isCurrent -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = tier.color.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Hiện tại",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = tier.color,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    isAchieved -> {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Đã đạt",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Quick benefits preview
            if (!isCurrent) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BenefitChip(
                        text = "${tier.discountPercent}% giảm giá",
                        modifier = Modifier.weight(1f)
                    )
                    BenefitChip(
                        text = "${tier.freeTurnsPerMonth} lượt free",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun BenefitChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = AppColors.SurfaceLight
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = AppColors.PrimaryGray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

fun getTierStars(tierName: String): Int {
    return when (tierName) {
        "Đồng" -> 1
        "Bạc" -> 2
        "Vàng" -> 3
        "Bạch Kim" -> 4
        else -> 1
    }
}

@Preview(showBackground = true)
@Composable
fun MemberCardScreenPreview() {
    MemberCardScreen()
}


