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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.R
import com.example.appcongvien.components.CardSection
import com.example.appcongvien.components.HeaderSection
import com.example.appcongvien.components.ImageCarousel
import com.example.appcongvien.components.QuickActions
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.AuthViewModel
import com.example.appcongvien.viewmodel.WalletViewModel

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
    onMyGamesClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onSupportClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
){
    val context = LocalContext.current
    val app = context.applicationContext as App
    
    // Initialize ViewModels
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(app.authRepository)
    )
    val walletViewModel: WalletViewModel = viewModel(
        factory = WalletViewModel.Factory(app.walletRepository)
    )
    
    // State collectors
    val profileState by authViewModel.profileState.collectAsState()
    val balanceState by walletViewModel.balanceState.collectAsState()
    
    // UI State - use derivedStateOf for better reactivity
    val userName = remember(profileState) {
        when (val state = profileState) {
            is Resource.Success -> state.data.fullName
            else -> "Người dùng"
        }
    }
    
    val currentBalance = remember(balanceState) {
        when (val state = balanceState) {
            is Resource.Success -> state.data.currentBalance
            else -> "0"
        }
    }
    
    val currentPoints = remember(balanceState) {
        when (val state = balanceState) {
            is Resource.Success -> {
                // Use loyaltyPoints from API
                "${state.data.loyaltyPoints}"
            }
            else -> "0"
        }
    }
    
    // Load data when screen opens
    LaunchedEffect(Unit) {
        authViewModel.loadProfile()
        walletViewModel.loadBalance()
    }
    
    val scrollState = rememberScrollState()
    val promotionImages = listOf(
        R.drawable.ic_launcher_background,
        R.drawable.ic_launcher_background,
        R.drawable.ic_launcher_background
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
                userName = userName,
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
                    balance = currentBalance,
                    points = currentPoints,
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
                    onGameListClick = onGameListClick,
                    onHistoryClick = onBalanceClick,
                    onProfileClick = onSettingsClick,
                    onRedeemPointsClick = {},
                    onMyGamesClick = onMyGamesClick
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