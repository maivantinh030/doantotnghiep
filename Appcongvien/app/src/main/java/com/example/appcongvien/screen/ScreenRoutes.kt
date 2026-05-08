package com.example.appcongvien.screen

import androidx.compose.runtime.Composable
import com.example.appcongvien.data.model.NotificationDTO
import com.example.appcongvien.data.repository.CardRequestRepository
import com.example.appcongvien.ui.theme.ThemeMode

@Composable
fun CardInfoRoute(onBackClick: () -> Unit = {}) {
    CardInfoScreen(onBackClick = onBackClick)
}

@Composable
fun CardRequestRoute(
    repository: CardRequestRepository,
    onBackClick: () -> Unit = {}
) {
    CardRequestScreen(
        repository = repository,
        onBackClick = onBackClick
    )
}

@Composable
fun ProfileRoute(onBackClick: () -> Unit = {}) {
    ProfileScreen(onBackClick = onBackClick)
}

@Composable
fun SupportChatRoute(onBackClick: () -> Unit = {}) {
    SupportChatScreen(onBackClick = onBackClick)
}

@Composable
fun NotificationsRoute(
    onBackClick: () -> Unit = {},
    onNotificationOpen: (NotificationDTO) -> Unit = {}
) {
    NotificationsScreen(
        onBackClick = onBackClick,
        onNotificationOpen = onNotificationOpen
    )
}
