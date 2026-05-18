package com.example.appcongvien.screen

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.components.ParkTopAppBar
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
    onBackClick: () -> Unit = {},
    onNotificationOpen: (NotificationDTO) -> Unit = {}
) {
    val context = LocalContext.current
    val notificationRepository = (context.applicationContext as App).notificationRepository
    val viewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModel.Factory(notificationRepository)
    )

    val notificationsState by viewModel.notificationsState.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    var displayedNotifications by remember { mutableStateOf<List<NotificationDTO>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.loadNotifications(page = 1, size = 50)
        viewModel.loadUnreadCount()
    }

    LaunchedEffect(notificationsState) {
        if (notificationsState is Resource.Success) {
            displayedNotifications = (notificationsState as Resource.Success).data.items
        }
    }

    Scaffold(
        topBar = {
            ParkTopAppBar(
                onBackClick = onBackClick,
                titleContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Thông báo",
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryDark,
                            fontSize = 20.sp
                        )
                        if (unreadCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = AppColors.WarmOrangeSoftAlt,
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
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
                actions = {
                    if (unreadCount > 0) {
                        TextButton(
                            onClick = {
                                displayedNotifications = displayedNotifications.map { it.copy(isRead = true) }
                                viewModel.markAllAsRead()
                            }
                        ) {
                            Text(
                                text = "Đọc tất cả",
                                color = AppColors.WarmOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
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
                        onOpen = { notification ->
                            handleNotificationOpen(
                                notification = notification,
                                displayedNotifications = displayedNotifications,
                                onDisplayedNotificationsChange = { displayedNotifications = it },
                                viewModel = viewModel,
                                onNotificationOpen = onNotificationOpen
                            )
                        },
                        onMarkAsRead = { notification ->
                            displayedNotifications = displayedNotifications.map {
                                if (it.notificationId == notification.notificationId) {
                                    it.copy(isRead = true)
                                } else {
                                    it
                                }
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
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    AppColors.HeaderGrad1.copy(alpha = 0.14f),
                                    AppColors.SurfaceLight,
                                    AppColors.SurfaceWhite
                                )
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    NotificationStateCard(
                        title = "Không thể tải thông báo",
                        message = (notificationsState as Resource.Error).message,
                        actionLabel = "Thử lại",
                        onAction = {
                            viewModel.loadNotifications(page = 1, size = 50)
                            viewModel.loadUnreadCount()
                        }
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
                        onOpen = { notification ->
                            handleNotificationOpen(
                                notification = notification,
                                displayedNotifications = displayedNotifications,
                                onDisplayedNotificationsChange = { displayedNotifications = it },
                                viewModel = viewModel,
                                onNotificationOpen = onNotificationOpen
                            )
                        },
                        onMarkAsRead = { notification ->
                            displayedNotifications = displayedNotifications.map {
                                if (it.notificationId == notification.notificationId) {
                                    it.copy(isRead = true)
                                } else {
                                    it
                                }
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

private fun handleNotificationOpen(
    notification: NotificationDTO,
    displayedNotifications: List<NotificationDTO>,
    onDisplayedNotificationsChange: (List<NotificationDTO>) -> Unit,
    viewModel: NotificationViewModel,
    onNotificationOpen: (NotificationDTO) -> Unit
) {
    if (!notification.isRead) {
        onDisplayedNotificationsChange(
            displayedNotifications.map {
                if (it.notificationId == notification.notificationId) it.copy(isRead = true) else it
            }
        )
        viewModel.markAsRead(notification.notificationId)
    }
    onNotificationOpen(notification)
}

@Composable
private fun NotificationEmptyState(
    modifier: Modifier,
    paddingValues: PaddingValues
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(
                Brush.verticalGradient(
                    listOf(
                        AppColors.HeaderGrad1.copy(alpha = 0.14f),
                        AppColors.SurfaceLight,
                        AppColors.SurfaceWhite
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        NotificationStateCard(
            title = "Chưa có thông báo",
            message = "Các cập nhật về ưu đãi, giao dịch và tài khoản sẽ xuất hiện tại đây."
        )
    }
}

@Composable
private fun NotificationList(
    modifier: Modifier,
    paddingValues: PaddingValues,
    notifications: List<NotificationDTO>,
    onOpen: (NotificationDTO) -> Unit,
    onMarkAsRead: (NotificationDTO) -> Unit,
    onDismiss: (NotificationDTO) -> Unit
) {
    val groupedNotifications = notifications.groupBy { getTimeGroup(it.createdAt) }
    val localUnreadCount = notifications.count { !it.isRead }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(
                Brush.verticalGradient(
                    listOf(
                        AppColors.HeaderGrad1.copy(alpha = 0.14f),
                        AppColors.SurfaceLight,
                        AppColors.SurfaceWhite
                    )
                )
            ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            NotificationSummaryCard(
                totalCount = notifications.size,
                unreadCount = localUnreadCount
            )
        }

        TimeGroup.entries.forEach { timeGroup ->
            val groupItems = groupedNotifications[timeGroup]
            if (!groupItems.isNullOrEmpty()) {
                item {
                    NotificationGroupLabel(
                        label = when (timeGroup) {
                            TimeGroup.TODAY -> "Hôm nay"
                            TimeGroup.YESTERDAY -> "Hôm qua"
                            TimeGroup.EARLIER -> "Trước đó"
                        }
                    )
                }
                items(groupItems, key = { it.notificationId }) { notification ->
                    NotificationCard(
                        notification = notification,
                        onOpen = { onOpen(notification) },
                        onMarkAsRead = { onMarkAsRead(notification) },
                        onDismiss = { onDismiss(notification) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(84.dp)) }
    }
}

@Composable
private fun NotificationSummaryCard(
    totalCount: Int,
    unreadCount: Int
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = AppColors.SurfaceWhite,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.72f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppColors.WarmOrangeSoftAlt,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = AppColors.WarmOrange,
                    modifier = Modifier.padding(11.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Trung tâm thông báo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
                Text(
                    text = "Bạn có $unreadCount thông báo chưa đọc trên tổng số $totalCount thông báo.",
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = AppColors.PrimaryGray.copy(alpha = 0.84f)
                )
            }
        }
    }
}

@Composable
private fun NotificationGroupLabel(label: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = AppColors.WarmOrangeSoftAlt.copy(alpha = 0.6f)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.WarmOrange,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationDTO,
    onOpen: () -> Unit,
    onMarkAsRead: () -> Unit,
    onDismiss: () -> Unit
) {
    val (icon, iconColor, backgroundColor) = getNotificationStyle(notification.type)

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onOpen,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.SurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.isRead) 1.dp else 2.dp),
        border = BorderStroke(
            1.dp,
            if (notification.isRead) AppColors.BorderSubtle.copy(alpha = 0.72f) else AppColors.WarmOrange.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = backgroundColor,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = notification.title,
                            fontSize = 15.sp,
                            fontWeight = if (notification.isRead) FontWeight.SemiBold else FontWeight.Bold,
                            color = AppColors.PrimaryDark,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (!notification.isRead) {
                            Spacer(modifier = Modifier.width(8.dp))
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
                        lineHeight = 19.sp,
                        color = AppColors.PrimaryGray.copy(alpha = 0.88f)
                    )

                    Text(
                        text = formatNotifTime(notification.createdAt),
                        fontSize = 11.sp,
                        color = AppColors.PrimaryGray.copy(alpha = 0.72f)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Xóa thông báo",
                        tint = AppColors.PrimaryGray.copy(alpha = 0.52f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (!notification.isRead) {
                OutlinedButton(
                    onClick = onMarkAsRead,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AppColors.WarmOrange.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = AppColors.SurfaceWhite,
                        contentColor = AppColors.WarmOrange
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Đánh dấu đã đọc",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationStateCard(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = AppColors.SurfaceWhite,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppColors.WarmOrangeSoftAlt,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = AppColors.WarmOrange,
                    modifier = Modifier.padding(14.dp)
                )
            }
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryDark,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = AppColors.PrimaryGray.copy(alpha = 0.84f),
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = actionLabel,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun getNotificationStyle(type: String?): Triple<ImageVector, Color, Color> {
    return when (type?.uppercase()) {
        "PROMOTION" -> Triple(Icons.Default.LocalOffer, AppColors.WarmOrange, AppColors.WarmOrangeSoft.copy(alpha = 0.74f))
        "VOUCHER", "VOUCHER_EXPIRING" -> Triple(
            Icons.Default.Warning,
            AppColors.YellowWarning,
            AppColors.YellowWarningContainer
        )

        "BIRTHDAY" -> Triple(
            Icons.Default.Cake,
            Color(0xFFE06C8C),
            Color(0xFFFCE8EF)
        )

        "EVENT" -> Triple(
            Icons.Default.Event,
            Color(0xFF0F8B8D),
            Color(0xFFE1F4F3)
        )

        "BALANCE", "BALANCE_LOW" -> Triple(
            Icons.Default.MonetizationOn,
            AppColors.RedError,
            AppColors.RedErrorContainer
        )

        "GAME", "GAME_UPDATE" -> Triple(
            Icons.Default.Star,
            Color(0xFF2C7DA0),
            Color(0xFFE3F0F6)
        )

        "MEMBERSHIP" -> Triple(
            Icons.Default.Person,
            AppColors.YellowWarning,
            AppColors.YellowWarningContainer
        )

        "ORDER", "PAYMENT" -> Triple(
            Icons.Default.CardGiftcard,
            AppColors.GreenSuccess,
            AppColors.GreenSuccessContainer
        )

        else -> Triple(Icons.Default.Notifications, AppColors.PrimaryGray, AppColors.SurfaceLight)
    }
}
