package com.example.appcongvien.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.data.model.NotificationDTO
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.NotificationViewModel

private enum class TimeGroup { TODAY, YESTERDAY, EARLIER }

private fun getTimeGroup(createdAt: String): TimeGroup {
    return try {
        val dateStr = createdAt.substring(0, 10)
        val today = java.time.LocalDate.now().toString()
        val yesterday = java.time.LocalDate.now().minusDays(1).toString()
        when (dateStr) {
            today -> TimeGroup.TODAY
            yesterday -> TimeGroup.YESTERDAY
            else -> TimeGroup.EARLIER
        }
    } catch (_: Exception) {
        TimeGroup.EARLIER
    }
}

private fun formatNotifTime(createdAt: String): String {
    return try {
        val parts = createdAt.substring(0, 10).split("-")
        val time = createdAt.substring(11, 16)
        "${parts[2]}/${parts[1]}/${parts[0]}  $time"
    } catch (_: Exception) {
        createdAt
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val notificationRepository = (context.applicationContext as App).notificationRepository
    val viewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModel.Factory(notificationRepository)
    )

    val notificationsState by viewModel.notificationsState.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    // Local optimistic list – updated from ViewModel state + local mutations
    var displayedNotifications by remember { mutableStateOf<List<NotificationDTO>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.loadNotifications(page = 1, size = 50)
        viewModel.loadUnreadCount()
    }

    // Sync ViewModel state → local list (but only on Success to avoid flicker)
    LaunchedEffect(notificationsState) {
        if (notificationsState is Resource.Success) {
            displayedNotifications = (notificationsState as Resource.Success).data.items
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Thông Báo",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (unreadCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.WarmOrange
                                    )
                                }
                            }
                        }
                    }
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
                actions = {
                    if (unreadCount > 0) {
                        TextButton(
                            onClick = { viewModel.markAllAsRead() }
                        ) {
                            Text("Đọc tất cả", color = Color.White, fontSize = 12.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.WarmOrange
                )
            )
        }
    ) { paddingValues ->
        when (notificationsState) {
            null, is Resource.Loading -> {
                if (displayedNotifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.WarmOrange)
                    }
                } else {
                    NotificationList(
                        modifier = modifier,
                        paddingValues = paddingValues,
                        notifications = displayedNotifications,
                        onMarkAsRead = { notification ->
                            displayedNotifications = displayedNotifications.map {
                                if (it.notificationId == notification.notificationId) it.copy(isRead = true) else it
                            }
                            viewModel.markAsRead(notification.notificationId)
                        },
                        onDismiss = { notification ->
                            displayedNotifications = displayedNotifications.filter {
                                it.notificationId != notification.notificationId
                            }
                            viewModel.deleteNotification(notification.notificationId)
                        }
                    )
                }
            }

            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lỗi: ${(notificationsState as Resource.Error).message}",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            is Resource.Success -> {
                if (displayedNotifications.isEmpty()) {
                    NotificationEmptyState(
                        modifier = modifier,
                        paddingValues = paddingValues
                    )
                } else {
                    NotificationList(
                        modifier = modifier,
                        paddingValues = paddingValues,
                        notifications = displayedNotifications,
                        onMarkAsRead = { notification ->
                            displayedNotifications = displayedNotifications.map {
                                if (it.notificationId == notification.notificationId) it.copy(isRead = true) else it
                            }
                            viewModel.markAsRead(notification.notificationId)
                        },
                        onDismiss = { notification ->
                            displayedNotifications = displayedNotifications.filter {
                                it.notificationId != notification.notificationId
                            }
                            viewModel.deleteNotification(notification.notificationId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationEmptyState(modifier: Modifier, paddingValues: PaddingValues) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Brush.verticalGradient(listOf(AppColors.SurfaceLight, Color.White))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = AppColors.WarmOrangeSoft,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = AppColors.WarmOrange,
                    modifier = Modifier.padding(20.dp).size(40.dp)
                )
            }
            Text(
                text = "Chưa có thông báo",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.PrimaryDark
            )
            Text(
                text = "Bạn sẽ nhận được thông báo về khuyến mãi và cập nhật tại đây",
                fontSize = 14.sp,
                color = AppColors.PrimaryGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun NotificationList(
    modifier: Modifier,
    paddingValues: PaddingValues,
    notifications: List<NotificationDTO>,
    onMarkAsRead: (NotificationDTO) -> Unit,
    onDismiss: (NotificationDTO) -> Unit
) {
    val groupedNotifications = notifications.groupBy { getTimeGroup(it.createdAt) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Brush.verticalGradient(listOf(AppColors.SurfaceLight, Color.White))),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TimeGroup.entries.forEach { timeGroup ->
            val groupItems = groupedNotifications[timeGroup]
            if (!groupItems.isNullOrEmpty()) {
                item {
                    Text(
                        text = when (timeGroup) {
                            TimeGroup.TODAY -> "Hôm nay"
                            TimeGroup.YESTERDAY -> "Hôm qua"
                            TimeGroup.EARLIER -> "Trước đó"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.PrimaryDark,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(groupItems, key = { it.notificationId }) { notification ->
                    NotificationCard(
                        notification = notification,
                        onMarkAsRead = { onMarkAsRead(notification) },
                        onDismiss = { onDismiss(notification) }
                    )
                }
                item { Spacer(modifier = Modifier.height(4.dp)) }
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun NotificationCard(
    notification: NotificationDTO,
    onMarkAsRead: () -> Unit,
    onDismiss: () -> Unit
) {
    val (icon, iconColor, backgroundColor) = getNotificationStyle(notification.type)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead)
                AppColors.WarmOrange.copy(alpha = 0.05f)
            else
                Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (!notification.isRead) 4.dp else 2.dp
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = backgroundColor,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                    }
                }

                // Content
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notification.title,
                            fontSize = 15.sp,
                            fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                            color = AppColors.PrimaryDark,
                            modifier = Modifier.weight(1f)
                        )
                        if (!notification.isRead) {
                            Surface(
                                shape = CircleShape,
                                color = AppColors.WarmOrange,
                                modifier = Modifier.size(8.dp)
                            ) {}
                        }
                    }
                    Text(
                        text = notification.message,
                        fontSize = 13.sp,
                        color = AppColors.PrimaryGray,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = formatNotifTime(notification.createdAt),
                        fontSize = 11.sp,
                        color = AppColors.PrimaryGray.copy(alpha = 0.7f)
                    )
                }

                // Dismiss
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Xóa thông báo",
                        tint = AppColors.PrimaryGray.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Mark as read button
            if (!notification.isRead) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    OutlinedButton(
                        onClick = onMarkAsRead,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = AppColors.WarmOrange
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Đánh dấu đã đọc", fontSize = 11.sp, color = AppColors.WarmOrange)
                    }
                }
            }
        }
    }
}

@Composable
private fun getNotificationStyle(type: String?): Triple<ImageVector, Color, Color> {
    return when (type?.uppercase()) {
        "PROMOTION" -> Triple(Icons.Default.LocalOffer, AppColors.WarmOrange, AppColors.WarmOrangeSoft)
        "VOUCHER", "VOUCHER_EXPIRING" -> Triple(
            Icons.Default.Warning,
            Color(0xFFFFC107),
            Color(0xFFFFC107).copy(alpha = 0.2f)
        )
        "BIRTHDAY" -> Triple(Icons.Default.Cake, Color(0xFFE91E63), Color(0xFFE91E63).copy(alpha = 0.2f))
        "EVENT" -> Triple(Icons.Default.Event, Color(0xFF9C27B0), Color(0xFF9C27B0).copy(alpha = 0.2f))
        "BALANCE", "BALANCE_LOW" -> Triple(
            Icons.Default.MonetizationOn,
            Color(0xFFF44336),
            Color(0xFFF44336).copy(alpha = 0.2f)
        )
        "GAME", "GAME_UPDATE" -> Triple(Icons.Default.Star, Color(0xFF2196F3), Color(0xFF2196F3).copy(alpha = 0.2f))
        "MEMBERSHIP" -> Triple(Icons.Default.Person, Color(0xFFFFD700), Color(0xFFFFD700).copy(alpha = 0.2f))
        "ORDER", "PAYMENT" -> Triple(
            Icons.Default.CardGiftcard,
            Color(0xFF4CAF50),
            Color(0xFF4CAF50).copy(alpha = 0.2f)
        )
        else -> Triple(Icons.Default.Notifications, AppColors.PrimaryGray, AppColors.SurfaceLight)
    }
}
