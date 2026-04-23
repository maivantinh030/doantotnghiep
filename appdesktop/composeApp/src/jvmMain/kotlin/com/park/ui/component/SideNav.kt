package com.park.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.park.navigation.AdminScreen
import com.park.ui.theme.AppColors
import com.park.ui.theme.AppTypography

data class NavItem(
    val screen: AdminScreen,
    val icon: ImageVector,
    val label: String
)

val adminNavItems = listOf(
    NavItem(AdminScreen.DASHBOARD, Icons.Default.Dashboard, "Dashboard"),
    NavItem(AdminScreen.USERS, Icons.Default.People, "Người dùng"),
    NavItem(AdminScreen.GAMES, Icons.Default.SportsEsports, "Trò chơi"),
//    NavItem(AdminScreen.CARDS, Icons.Default.CreditCard, "Quản lý Thẻ"),
    NavItem(AdminScreen.FINANCE, Icons.Default.AttachMoney, "Tài chính"),
    NavItem(AdminScreen.STATISTICS, Icons.Default.QueryStats, "Thống kê"),
    NavItem(AdminScreen.NOTIFICATIONS, Icons.Default.Notifications, "Thông báo"),
    NavItem(AdminScreen.SUPPORT, Icons.Default.HeadsetMic, "Hỗ trợ"),
    NavItem(AdminScreen.ANNOUNCEMENTS, Icons.Default.Campaign, "Carousel"),
)

@Composable
fun SideNav(
    currentScreen: AdminScreen,
    adminName: String,
    onNavigate: (AdminScreen) -> Unit,
    onLogout: () -> Unit
) {
    var isCollapsed by remember { mutableStateOf(false) }
    val sidebarWidth by animateDpAsState(targetValue = if (isCollapsed) 72.dp else 220.dp, label = "sidebarWidth")

    Column(
        modifier = Modifier
            .width(sidebarWidth)
            .fillMaxHeight()
            .background(AppColors.SidebarBg)
            .padding(vertical = 16.dp)
    ) {
        // Logo & App Name
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.WarmOrange),
                contentAlignment = Alignment.Center
            ) {
                Text("PA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            if (!isCollapsed) {
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Park Adventure",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                    Text("Admin Panel", color = AppColors.SidebarText, fontSize = 11.sp)
                }
            }
            IconButton(
                onClick = { isCollapsed = !isCollapsed },
                modifier = Modifier.size(if (isCollapsed) 0.dp else 24.dp)
            ) {
                if (!isCollapsed) {
                    Icon(Icons.Default.MenuOpen, contentDescription = "Collapse", tint = AppColors.SidebarText)
                }
            }
        }
        if (isCollapsed) {
            IconButton(
                onClick = { isCollapsed = !isCollapsed },
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp).size(24.dp)
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Expand", tint = AppColors.SidebarText)
            }
        }

        Divider(color = AppColors.SidebarSelected, modifier = Modifier.padding(vertical = 8.dp))

        // Admin Profile
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isCollapsed) Arrangement.Center else Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AppColors.WarmOrange),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    adminName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            if (!isCollapsed) {
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(adminName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                    Text("Administrator", color = AppColors.SidebarText, fontSize = 11.sp)
                }
            }
        }

        Divider(color = AppColors.SidebarSelected, modifier = Modifier.padding(vertical = 8.dp))

        // Nav Items
        adminNavItems.forEach { item ->
            NavItemRow(
                item = item,
                isSelected = currentScreen == item.screen,
                isCollapsed = isCollapsed,
                onClick = { onNavigate(item.screen) }
            )
        }

        Spacer(Modifier.weight(1f))

        Divider(color = AppColors.SidebarSelected, modifier = Modifier.padding(vertical = 8.dp))

        // Logout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogout() }
                .padding(horizontal = if (isCollapsed) 0.dp else 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isCollapsed) Arrangement.Center else Arrangement.Start
        ) {
            Icon(Icons.Default.Logout, contentDescription = "Đăng xuất", tint = AppColors.RedError, modifier = Modifier.size(20.dp))
            if (!isCollapsed) {
                Spacer(Modifier.width(10.dp))
                Text("Đăng xuất", color = AppColors.RedError, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun NavItemRow(
    item: NavItem,
    isSelected: Boolean,
    isCollapsed: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) AppColors.SidebarSelected else Color.Transparent
    val contentColor = if (isSelected) AppColors.WarmOrange else AppColors.SidebarText

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = if (isCollapsed) 0.dp else 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isCollapsed) Arrangement.Center else Arrangement.Start
    ) {
        if (!isCollapsed) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AppColors.WarmOrange)
                )
                Spacer(Modifier.width(8.dp))
            } else {
                Spacer(Modifier.width(11.dp))
            }
        }
        Icon(item.icon, contentDescription = item.label, tint = contentColor, modifier = Modifier.size(20.dp))
        if (!isCollapsed) {
            Spacer(Modifier.width(10.dp))
            Text(item.label, color = contentColor, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1)
        }
    }
}
