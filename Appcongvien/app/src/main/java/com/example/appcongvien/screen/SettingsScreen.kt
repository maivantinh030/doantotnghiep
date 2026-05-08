package com.example.appcongvien.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcongvien.components.ParkTopAppBar
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.ui.theme.ThemeMode

data class UserProfile(
    val name: String,
    val membershipLevel: String,
    val joinDate: String,
    val referralCode: String,
    val totalReferrals: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var language by remember { mutableStateOf("Tiếng Việt") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Đăng xuất",
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn đăng xuất khỏi tài khoản không?",
                    fontSize = 14.sp,
                    color = AppColors.PrimaryGray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    }
                ) {
                    Text(
                        text = "Đăng xuất",
                        color = AppColors.DestructiveText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = "Hủy",
                        color = AppColors.PrimaryGray
                    )
                }
            }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Text(
                    text = "Chọn giao diện",
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        ThemeModeOption(
                            mode = mode,
                            selected = mode == themeMode,
                            onSelect = {
                                onThemeModeChange(mode)
                                showThemeDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(
                        text = "Đóng",
                        color = AppColors.PrimaryGray
                    )
                }
            }
        )
    }

    val user = remember {
        UserProfile(
            name = "Mai Văn Tĩnh",
            membershipLevel = "Vàng",
            joinDate = "15/01/2024",
            referralCode = "PARK2024MT",
            totalReferrals = 5
        )
    }

    Scaffold(
        topBar = {
            ParkTopAppBar(
                title = "Cài đặt",
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
                            AppColors.HeaderGrad1.copy(alpha = 0.14f),
                            AppColors.SurfaceLight,
                            AppColors.SurfaceWhite
                        )
                    )
                ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                SettingsProfileHeader(
                    user = user,
                    onProfileClick = onProfileClick
                )
            }

            item {
                ReferralSection(
                    referralCode = user.referralCode,
                    totalReferrals = user.totalReferrals
                )
            }

            item {
                SettingsSection(title = "Tài khoản") {
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = "Thông tin cá nhân",
                        description = "Xem và chỉnh sửa hồ sơ của bạn",
                        onClick = onProfileClick
                    )
                    SettingsDivider()
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "Đổi mật khẩu",
                        description = "Cập nhật mật khẩu để tăng bảo mật",
                        onClick = onChangePasswordClick
                    )
                }
            }

            item {
                SettingsSection(title = "Ứng dụng") {
                    SettingsItemWithValue(
                        icon = Icons.Default.Language,
                        title = "Ngôn ngữ",
                        description = "Chọn ngôn ngữ hiển thị",
                        value = language,
                        onClick = {
                            language = if (language == "Tiếng Việt") "English" else "Tiếng Việt"
                        }
                    )
                    SettingsDivider()
                    SettingsItemWithValue(
                        icon = Icons.Default.DarkMode,
                        title = "Giao diện",
                        description = "Theo hệ thống hoặc chọn sáng, tối thủ công",
                        value = themeMode.displayName(),
                        onClick = { showThemeDialog = true }
                    )
                    SettingsDivider()
                    SettingsItemWithSwitch(
                        icon = Icons.Default.Notifications,
                        title = "Thông báo",
                        description = "Nhận nhắc nhở về ưu đãi và lịch sử giao dịch",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }
            }

            item {
                SettingsSection(title = "Hỗ trợ") {
                    SettingsItem(
                        icon = Icons.Default.Help,
                        title = "Trợ giúp",
                        description = "Tìm câu trả lời cho các thắc mắc thường gặp",
                        onClick = onHelpClick
                    )
                    SettingsDivider()
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "Về ứng dụng",
                        description = "Phiên bản, quyền riêng tư và thông tin sản phẩm",
                        onClick = onAboutClick
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showLogoutDialog = true },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.DestructiveSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = BorderStroke(1.dp, AppColors.DestructiveText.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsLeadingIcon(
                            icon = Icons.Default.Logout,
                            iconTint = AppColors.DestructiveText,
                            backgroundColor = AppColors.RedErrorContainer
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Đăng xuất",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.DestructiveText
                            )
                            Text(
                                text = "Thoát khỏi tài khoản hiện tại trên thiết bị này",
                                fontSize = 12.sp,
                                color = AppColors.DestructiveText.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = AppColors.DestructiveText.copy(alpha = 0.72f)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }
}

@Composable
private fun SettingsProfileHeader(
    user: UserProfile,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onProfileClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.72f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = AppColors.WarmOrangeSoft.copy(alpha = 0.78f),
                modifier = Modifier.size(68.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = AppColors.WarmOrange,
                    modifier = Modifier.padding(18.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = user.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
                Text(
                    text = "Quản lý hồ sơ, quyền lợi thành viên và các tùy chọn cá nhân.",
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = AppColors.PrimaryGray.copy(alpha = 0.82f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = AppColors.YellowWarningContainer.copy(alpha = 0.72f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AppColors.YellowWarning,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = user.membershipLevel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.YellowWarning
                            )
                        }
                    }
                    Text(
                        text = "Tham gia ${user.joinDate}",
                        fontSize = 12.sp,
                        color = AppColors.PrimaryGray.copy(alpha = 0.78f)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AppColors.PrimaryGray.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ReferralSection(
    referralCode: String,
    totalReferrals: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.72f))
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsLeadingIcon(
                        icon = Icons.Default.PersonAdd,
                        iconTint = AppColors.WarmOrange,
                        backgroundColor = AppColors.WarmOrangeSoft.copy(alpha = 0.78f)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Mã giới thiệu",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryDark
                        )
                        Text(
                            text = "Mời bạn bè tham gia để cùng nhận quà ưu đãi.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = AppColors.PrimaryGray.copy(alpha = 0.82f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AppColors.WarmOrangeSoft.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "$totalReferrals bạn bè",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.WarmOrange,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = AppColors.SurfaceLight.copy(alpha = 0.78f),
                border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.58f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Mã của bạn",
                            fontSize = 12.sp,
                            color = AppColors.PrimaryGray.copy(alpha = 0.82f)
                        )
                        Text(
                            text = referralCode,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryDark,
                            letterSpacing = 1.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReferralIconButton(
                            icon = Icons.Default.ContentCopy,
                            contentDescription = "Sao chép"
                        )
                        ReferralIconButton(
                            icon = Icons.Default.Share,
                            contentDescription = "Chia sẻ"
                        )
                    }
                }
            }

            Text(
                text = "Mỗi lượt giới thiệu thành công sẽ giúp cả bạn và bạn bè nhận được ưu đãi hấp dẫn hơn trong lần ghé công viên tiếp theo.",
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = AppColors.PrimaryGray.copy(alpha = 0.84f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ReferralIconButton(
    icon: ImageVector,
    contentDescription: String
) {
    IconButton(
        onClick = {},
        modifier = Modifier.size(38.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = AppColors.WarmOrange,
            modifier = Modifier.size(34.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = AppColors.OnAccent,
                modifier = Modifier.padding(7.dp)
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.PrimaryGray.copy(alpha = 0.86f),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.72f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    textColor: Color = AppColors.PrimaryDark
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsLeadingIcon(
                icon = icon,
                iconTint = AppColors.WarmOrange,
                backgroundColor = AppColors.WarmOrangeSoft.copy(alpha = 0.72f)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = AppColors.PrimaryGray.copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AppColors.PrimaryGray.copy(alpha = 0.68f)
            )
        }
    }
}

@Composable
private fun SettingsItemWithValue(
    icon: ImageVector,
    title: String,
    description: String,
    value: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsLeadingIcon(
                icon = icon,
                iconTint = AppColors.WarmOrange,
                backgroundColor = AppColors.WarmOrangeSoft.copy(alpha = 0.72f)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.PrimaryDark
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = AppColors.PrimaryGray.copy(alpha = 0.8f)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    fontSize = 13.sp,
                    color = AppColors.PrimaryGray
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = AppColors.PrimaryGray.copy(alpha = 0.68f)
                )
            }
        }
    }
}

@Composable
private fun SettingsItemWithSwitch(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsLeadingIcon(
                icon = icon,
                iconTint = AppColors.WarmOrange,
                backgroundColor = AppColors.WarmOrangeSoft.copy(alpha = 0.72f)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.PrimaryDark
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = AppColors.PrimaryGray.copy(alpha = 0.8f)
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppColors.OnAccent,
                    checkedTrackColor = AppColors.WarmOrange,
                    uncheckedThumbColor = AppColors.SurfaceWhite,
                    uncheckedTrackColor = AppColors.PrimaryGray.copy(alpha = 0.45f)
                )
            )
        }
    }
}

@Composable
private fun SettingsLeadingIcon(
    icon: ImageVector,
    iconTint: Color,
    backgroundColor: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        modifier = Modifier.size(42.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.padding(10.dp)
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = AppColors.BorderSubtle.copy(alpha = 0.72f),
        thickness = 1.dp
    )
}

@Composable
private fun ThemeModeOption(
    mode: ThemeMode,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) {
            AppColors.WarmOrangeSoft.copy(alpha = 0.7f)
        } else {
            Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = null
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mode.displayName(),
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.PrimaryDark
                )
                Text(
                    text = mode.description(),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = AppColors.PrimaryGray.copy(alpha = 0.82f)
                )
            }
        }
    }
}

private fun ThemeMode.displayName(): String = when (this) {
    ThemeMode.SYSTEM -> "Theo hệ thống"
    ThemeMode.LIGHT -> "Sáng"
    ThemeMode.DARK -> "Tối"
}

private fun ThemeMode.description(): String = when (this) {
    ThemeMode.SYSTEM -> "Tự đổi theo giao diện sáng hoặc tối của thiết bị"
    ThemeMode.LIGHT -> "Luôn dùng giao diện sáng"
    ThemeMode.DARK -> "Luôn dùng giao diện tối"
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}
