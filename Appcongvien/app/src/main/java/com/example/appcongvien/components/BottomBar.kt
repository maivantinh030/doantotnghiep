package com.example.appcongvien.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcongvien.AppDestinations
import com.example.appcongvien.ui.theme.AppColors

@Composable
fun BottomBar(
    currentDestination: AppDestinations,
    onNavigate: (AppDestinations) -> Unit
) {
    Surface(
        shadowElevation = 10.dp,
        color = AppColors.SurfaceWhite,
        tonalElevation = 0.dp
    ) {
        NavigationBar(
            containerColor = AppColors.SurfaceWhite,
            tonalElevation = 0.dp,
            windowInsets = NavigationBarDefaults.windowInsets
        ) {
            AppDestinations.entries.forEach { destination ->
                val isSelected = destination == currentDestination

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onNavigate(destination) },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) {
                                destination.selectedIcon
                            } else {
                                destination.unselectedIcon
                            },
                            contentDescription = destination.label,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = destination.label,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppColors.OnAccent,
                        unselectedIconColor = AppColors.PrimaryGray,
                        selectedTextColor = AppColors.WarmOrange,
                        unselectedTextColor = AppColors.PrimaryGray,
                        indicatorColor = AppColors.WarmOrange
                    ),
                    alwaysShowLabel = true
                )
            }
        }
    }
}
