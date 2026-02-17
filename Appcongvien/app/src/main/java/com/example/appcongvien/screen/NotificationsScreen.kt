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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcongvien.ui.theme.AppColors

data class ParkNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: String,
    val timeGroup: TimeGroup,
    val isRead: Boolean = false,
    val hasAction: Boolean = false,
    val actionText: String = ""
)

enum class NotificationType {
    PROMOTION,           // Khuyến mãi
    VOUCHER_EXPIRING,    // Voucher hết hạn
    BIRTHDAY,            // Sinh nhật
    EVENT,               // Sự kiện
    BALANCE_LOW,         // Số dư thấp
    GAME_UPDATE,         // Cập nhật game
    MEMBERSHIP,          // Thành viên
    SYSTEM               // Hệ thống
}

enum class TimeGroup {
    TODAY, YESTERDAY, EARLIER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onNotificationAction: (String, String) -> Unit = { _, _ -> }
) {
    var notifications by remember {
        mutableStateOf(getNotifications())
    }

    val unreadCount = notifications.count { !it.isRead }

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
                                        text = unreadCount.toString(),
                                        fontSize = 11.sp,
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
                            onClick = {
                                notifications = notifications.map { it.copy(isRead = true) }
                            }
                        ) {
                            Text(
                                "Đọc tất cả",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.WarmOrange
                )
            )
        }
    ) { paddingValues ->

        if (notifications.isEmpty()) {
            // Empty state
            Box(
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
                            modifier = Modifier
                                .padding(20.dp)
                                .size(40.dp)
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
                        color = AppColors.PrimaryGray
                    )
                }
            }
        } else {
            // Notifications list
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Group notifications by time
                val groupedNotifications = notifications.groupBy { it.timeGroup }

                TimeGroup.values().forEach { timeGroup ->
                    val groupNotifications = groupedNotifications[timeGroup]
                    if (groupNotifications?.isNotEmpty() == true) {

                        // Time group header
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
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        // Notifications in this group
                        items(groupNotifications) { notification ->
                            NotificationCard(
                                notification = notification,
                                onMarkAsRead = {
                                    notifications = notifications.map {
                                        if (it.id == notification.id) it.copy(isRead = true) else it
                                    }
                                },
                                onAction = { actionType ->
                                    onNotificationAction(notification.id, actionType)
                                },
                                onDismiss = {
                                    notifications = notifications.filter { it.id != notification.id }
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // Extra space for bottom navigation
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: ParkNotification,
    onMarkAsRead: () -> Unit,
    onAction: (String) -> Unit,
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

                // Notification icon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = backgroundColor,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
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

                        // Unread indicator
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
                        text = notification.timestamp,
                        fontSize = 11.sp,
                        color = AppColors.PrimaryGray.copy(alpha = 0.7f)
                    )
                }

                // Dismiss button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Xóa thông báo",
                        tint = AppColors.PrimaryGray.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Action buttons
            if (notification.hasAction || !notification.isRead) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    if (!notification.isRead) {
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
                            Text(
                                "Đánh dấu đã đọc",
                                fontSize = 11.sp,
                                color = AppColors.WarmOrange
                            )
                        }
                    }

                    if (notification.hasAction) {
                        androidx.compose.material3.Button(
                            onClick = { onAction(notification.actionText) },
                            modifier = Modifier.height(32.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = AppColors.WarmOrange,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                notification.actionText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getNotificationStyle(type: NotificationType): Triple<ImageVector, Color, Color> {
    return when (type) {
        NotificationType.PROMOTION -> Triple(
            Icons.Default.LocalOffer,
            AppColors.WarmOrange,
            AppColors.WarmOrangeSoft
        )
        NotificationType.VOUCHER_EXPIRING -> Triple(
            Icons.Default.Warning,
            Color(0xFFFFC107),
            Color(0xFFFFC107).copy(alpha = 0.2f)
        )
        NotificationType.BIRTHDAY -> Triple(
            Icons.Default.Cake,
            Color(0xFFE91E63),
            Color(0xFFE91E63).copy(alpha = 0.2f)
        )
        NotificationType.EVENT -> Triple(
            Icons.Default.Event,
            Color(0xFF9C27B0),
            Color(0xFF9C27B0).copy(alpha = 0.2f)
        )
        NotificationType.BALANCE_LOW -> Triple(
            Icons.Default.MonetizationOn,
            Color(0xFFF44336),
            Color(0xFFF44336).copy(alpha = 0.2f)
        )
        NotificationType.GAME_UPDATE -> Triple(
            Icons.Default.Star,
            Color(0xFF2196F3),
            Color(0xFF2196F3).copy(alpha = 0.2f)
        )
        NotificationType.MEMBERSHIP -> Triple(
            Icons.Default.Person,
            Color(0xFFFFD700),
            Color(0xFFFFD700).copy(alpha = 0.2f)
        )
        NotificationType.SYSTEM -> Triple(
            Icons.Default.Notifications,
            AppColors.PrimaryGray,
            AppColors.SurfaceLight
        )
    }
}

fun getNotifications(): List<ParkNotification> {
    return listOf(
        ParkNotification(
            id = "1",
            title = "Voucher sắp hết hạn",
            message = "Voucher giảm 20% của bạn sẽ hết hạn trong 3 ngày. Sử dụng ngay để không bỏ lỡ!",
            type = NotificationType.VOUCHER_EXPIRING,
            timestamp = "2 giờ trước",
            timeGroup = TimeGroup.TODAY,
            isRead = false,
            hasAction = true,
            actionText = "Dùng ngay"
        ),
        ParkNotification(
            id = "2",
            title = "Khuyến mãi mới",
            message = "🎉 Giảm 30% cho tất cả các game trong tuần này! Đừng bỏ lỡ cơ hội vàng.",
            type = NotificationType.PROMOTION,
            timestamp = "4 giờ trước",
            timeGroup = TimeGroup.TODAY,
            isRead = false,
            hasAction = true,
            actionText = "Xem ngay"
        ),
        ParkNotification(
            id = "3",
            title = "Số dư thấp",
            message = "Số dư trong tài khoản của bạn chỉ còn 25,000 đ. Nạp thêm tiền để tiếp tục trải nghiệm!",
            type = NotificationType.BALANCE_LOW,
            timestamp = "6 giờ trước",
            timeGroup = TimeGroup.TODAY,
            isRead = false,
            hasAction = true,
            actionText = "Nạp tiền"
        ),
        ParkNotification(
            id = "4",
            title = "Chúc mừng sinh nhật!",
            message = "🎂 Nhận voucher 200K nhân dịp sinh nhật của bạn. Chúc bạn có một ngày thật vui vẻ!",
            type = NotificationType.BIRTHDAY,
            timestamp = "Hôm qua",
            timeGroup = TimeGroup.YESTERDAY,
            isRead = true,
            hasAction = true,
            actionText = "Nhận quà"
        ),
        ParkNotification(
            id = "5",
            title = "Sự kiện đặc biệt",
            message = "Sự kiện cuối tuần - Nhận thêm 50% điểm tích lũy khi chơi game từ 9h-17h.",
            type = NotificationType.EVENT,
            timestamp = "Hôm qua",
            timeGroup = TimeGroup.YESTERDAY,
            isRead = true
        ),
        ParkNotification(
            id = "6",
            title = "Nâng cấp thành viên",
            message = "Congratulations! Bạn đã đạt đủ điều kiện nâng cấp lên thành viên Bạch Kim.",
            type = NotificationType.MEMBERSHIP,
            timestamp = "3 ngày trước",
            timeGroup = TimeGroup.EARLIER,
            isRead = true,
            hasAction = true,
            actionText = "Nâng cấp"
        )
    )
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    NotificationsScreen()
}


