package com.example.appcongvien.screen

import com.example.appcongvien.components.FeatureSection
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appcongvien.R
import com.example.appcongvien.components.CardSection
import com.example.appcongvien.components.HeaderSection
import com.example.appcongvien.components.ImageCarousel
import com.example.appcongvien.components.QuickActions
import com.example.appcongvien.ui.theme.AppColors

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onCardInfoClick: () -> Unit = {},
    onLockCardClick: () -> Unit = {},
    onBalanceClick: () -> Unit = {},
    onTopUpClick: () -> Unit = {},
    onBuyGameClick: () -> Unit = {},
    onVouchersClick: () -> Unit = {},
    onVoucherWalletClick: () -> Unit = {},
    onReferralClick: () -> Unit = {},
    onMemberCardClick: () -> Unit = {},
    onGameClick: (String) -> Unit = {},
    onGameListClick:() -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onSupportClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
){
    val scrollState = rememberScrollState()
    val promotionImages = listOf(
        R.drawable.ic_launcher_background,  // Hình lãi suất cao
        R.drawable.ic_launcher_background,  // Hình nhận thưởng
        R.drawable.ic_launcher_background   // Hình khác
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(
                Brush.verticalGradient(
                    listOf(
                        AppColors.SurfaceLight,
                        Color.White
                    )
                )
            )
    ) {

        // Header Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(
                    RoundedCornerShape(
                        bottomStart = 32.dp,
                        bottomEnd = 32.dp
                    )
                )
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AppColors.HeaderGrad1,   // Light cream
                            AppColors.HeaderGrad2,   // Medium orange
                            AppColors.HeaderGrad3    // Warm orange
                        )
                    )
                )
        ){
            HeaderSection(
                onNotificationsClick = onNotificationsClick,
                modifier = Modifier.padding(top = 20.dp)
            )
        }
        // Card Section - Using offset instead of negative padding
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .offset(y = (-80).dp) // ✅ Use offset instead of negative padding
                .height(200.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                AppColors.CardGrad1,    // Charcoal
                                AppColors.CardGrad2     // Lighter charcoal
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                CardSection(
                    onCardInfoClick = onCardInfoClick,
                    onBalanceToggleClick = onBalanceClick
                )
            }
        }

        // Content Area - Also offset to maintain spacing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-80).dp) // Same offset to maintain layout
                .clip(
                    RoundedCornerShape(
                        topStart = 32.dp,
                        topEnd = 32.dp
                    )
                )
                .background(Color.White)
                .padding(top = 24.dp)
        ){
            Column {
                QuickActions(
                    onTopUpClick = onTopUpClick,
                    onPaymentClick = onBuyGameClick,
                    onVoucherClick = onVouchersClick
                )

                Spacer(modifier = Modifier.height(24.dp))
                ImageCarousel(
                    images = promotionImages,
                    onImageClick = { index ->
                        // Xử lý khi click vào hình
                        println("Clicked image at position: $index")
                        // Mở màn hình chi tiết hoặc web view...
                    }
                )

                FeatureSection(
                    onCardInfoClick = onCardInfoClick,
                    onGameListClick = onGameListClick,
                    onHistoryClick = onBalanceClick,
                    onProfileClick = onSettingsClick,
//                    onLockCardClick = onLockCardClick
                )

                // Extra space for bottom navigation
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview(){
    HomeScreen()
}