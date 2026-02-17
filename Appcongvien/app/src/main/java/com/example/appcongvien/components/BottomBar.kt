package com.example.appcongvien.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcongvien.AppDestinations
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.ui.theme.AppColors.PrimaryGray
import com.example.appcongvien.ui.theme.AppColors.WarmOrange

@Composable
fun BottomBar(
    currentDestination: AppDestinations,
    onNavigate: (AppDestinations) -> Unit
) {
    // Tạo độ nổi (Elevation) cho thanh Bar bằng Shadow
    Surface(
        shadowElevation = 8.dp, // Đổ bóng nhẹ để tách nền
        color = White,
        tonalElevation = 0.dp // Tắt lớp phủ màu mặc định của M3
    ) {
        NavigationBar(
            containerColor = White, // Nền trắng sạch
            tonalElevation = 0.dp,
            windowInsets = NavigationBarDefaults.windowInsets, // Xử lý tai thỏ/gesture bar
        ) {
            AppDestinations.entries.forEach { destination ->
                val isSelected = destination == currentDestination

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onNavigate(destination) },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = destination.label,
                        )
                    },
                    label = {
                        Text(
                            text = destination.label,
                            fontSize = 12.sp, // Nhỏ gọn
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    // --- QUAN TRỌNG: COLOR CUSTOMIZATION ---
                    colors = NavigationBarItemDefaults.colors(
                        // Màu icon
                        selectedIconColor = White, // Icon trắng khi chọn
                        unselectedIconColor = PrimaryGray, // Icon xám khi chưa chọn

                        // Màu Text
                        selectedTextColor = WarmOrange, // Chữ cam khi chọn
                        unselectedTextColor = PrimaryGray, // Chữ xám khi chưa chọn

                        // Màu cái "Viên thuốc" (Indicator) nền sau icon
                        indicatorColor = WarmOrange // Nền cam
                    ),
                    alwaysShowLabel = true // Luôn hiện label để cân đối layout
                )
            }
        }
    }
}