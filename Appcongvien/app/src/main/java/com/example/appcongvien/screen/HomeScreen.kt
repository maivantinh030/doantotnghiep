package com.example.appcongvien.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.components.CardSection
import com.example.appcongvien.components.HeaderSection
import com.example.appcongvien.components.ImageCarousel
import com.example.appcongvien.components.ServicesRow
import com.example.appcongvien.data.model.AnnouncementDTO
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.AnnouncementViewModel
import com.example.appcongvien.viewmodel.AuthViewModel
import com.example.appcongvien.viewmodel.NotificationViewModel
import com.example.appcongvien.viewmodel.WalletViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onCardInfoClick: () -> Unit = {},
    onCardRequestClick: () -> Unit = {},
    onBalanceClick: () -> Unit = {},
    onTopUpClick: () -> Unit = {},
    onGameClick: (String) -> Unit = {},
    onGameListClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onSupportClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as App

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(app.authRepository)
    )
    val walletViewModel: WalletViewModel = viewModel(
        factory = WalletViewModel.Factory(app.walletRepository)
    )
    val announcementViewModel: AnnouncementViewModel = viewModel(
        factory = AnnouncementViewModel.Factory(app.announcementRepository)
    )
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModel.Factory(app.notificationRepository)
    )

    val profileState by authViewModel.profileState.collectAsState()
    val balanceState by walletViewModel.balanceState.collectAsState()
    val announcementsState by announcementViewModel.announcementsState.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

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

    val announcements: List<AnnouncementDTO> = remember(announcementsState) {
        when (val state = announcementsState) {
            is Resource.Success -> state.data
            else -> emptyList()
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.loadProfile()
        walletViewModel.loadBalance()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationViewModel.loadUnreadCount()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val overlapOffset = 72.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        AppColors.BackgroundWarm,
                        AppColors.SurfaceLight,
                        AppColors.SurfaceWhite
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AppColors.BackgroundWarm.copy(alpha = 0.84f),
                            AppColors.HeaderGrad1,
                            AppColors.HeaderGrad2,
                            AppColors.HeaderGrad3.copy(alpha = 0.88f)
                        )
                    )
                )
        ) {
            HeaderSection(
                userName = userName,
                unreadCount = unreadCount,
                onNotificationsClick = onNotificationsClick,
                onProfileClick = onSettingsClick,
                modifier = Modifier.padding(top = 18.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 4.dp,
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = -overlapOffset)
                .height(140.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                AppColors.CardGrad1,
                                AppColors.CardGrad2
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                CardSection(
                    balance = currentBalance,
                    onCardInfoClick = onCardInfoClick,
                    onBalanceToggleClick = onBalanceClick
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .offset(y = -overlapOffset)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(AppColors.SurfaceWhite)
                .padding(top = 24.dp)
                .fillMaxHeight()
            ,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ServicesRow(
                onTopUpClick = onTopUpClick,
                onHistoryClick = onBalanceClick,
                onGameListClick = onGameListClick,
                onCardRequestClick = onCardRequestClick,
                onProfileClick = onSettingsClick,
                onSupportClick = onSupportClick
            )

            ImageCarousel(
                announcements = announcements,
                onAnnouncementClick = { item ->
                    when (item.linkType) {
                        "GAME" -> item.linkValue?.let { onGameClick(it) }
                        "SCREEN" -> when (item.linkValue) {
                            "games" -> onGameListClick()
                            "balance" -> onBalanceClick()
                            "card_request" -> onCardRequestClick()
                            else -> {}
                        }
                        else -> {}
                    }
                }
            )
        }
    }
}

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    userName: String,
    currentBalance: String,
    announcements: List<AnnouncementDTO>,
    onCardInfoClick: () -> Unit = {},
    onCardRequestClick: () -> Unit = {},
    onBalanceClick: () -> Unit = {},
    onTopUpClick: () -> Unit = {},
    onGameClick: (String) -> Unit = {},
    onGameListClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onSupportClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {
    val overlapOffset = 72.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        AppColors.BackgroundWarm,
                        AppColors.SurfaceLight,
                        AppColors.SurfaceWhite
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AppColors.BackgroundWarm.copy(alpha = 0.84f),
                            AppColors.HeaderGrad1,
                            AppColors.HeaderGrad2,
                            AppColors.HeaderGrad3.copy(alpha = 0.88f)
                        )
                    )
                )
        ) {
            HeaderSection(
                userName = userName,
                onNotificationsClick = onNotificationsClick,
                onProfileClick = onSettingsClick,
                modifier = Modifier.padding(top = 18.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 4.dp,
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = -overlapOffset)
                .height(140.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                AppColors.CardGrad1,
                                AppColors.CardGrad2
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                CardSection(
                    balance = currentBalance,
                    onCardInfoClick = onCardInfoClick,
                    onBalanceToggleClick = onBalanceClick
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .offset(y = -overlapOffset)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(AppColors.SurfaceWhite)
                .padding(top = 24.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ServicesRow(
                onTopUpClick = onTopUpClick,
                onHistoryClick = onBalanceClick,
                onGameListClick = onGameListClick,
                onCardRequestClick = onCardRequestClick,
                onProfileClick = onSettingsClick,
                onSupportClick = onSupportClick
            )

            ImageCarousel(
                announcements = announcements,
                onAnnouncementClick = { item ->
                    when (item.linkType) {
                        "GAME" -> item.linkValue?.let { onGameClick(it) }

                        "SCREEN" -> when (item.linkValue) {
                            "games" -> onGameListClick()
                            "balance" -> onBalanceClick()
                            "card_request" -> onCardRequestClick()
                            else -> {}
                        }

                        else -> {}
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {

    val demoAnnouncements = listOf(
        AnnouncementDTO(
            announcementId = "1",
            title = "Khuyến mãi hè",
            description = "Giảm giá 50% tất cả trò chơi cuối tuần",
            imageUrl = "https://picsum.photos/600/300",
            linkType = "GAME",
            linkValue = "game_1",
            isActive = true,
            sortOrder = 1,
            createdAt = null,
            updatedAt = null
        ),
        AnnouncementDTO(
            announcementId = "2",
            title = "Sự kiện mới",
            description = "Check-in nhận quà miễn phí",
            imageUrl = "http://picsum.photos/601/300",
            linkType = "SCREEN",
            linkValue = "games",
            isActive = true,
            sortOrder = 2,
            createdAt = null,
            updatedAt = null
        )
    )

    HomeScreenContent(
        userName = "Nguyễn Văn A",
        currentBalance = "150000",
        announcements = demoAnnouncements
    )
}