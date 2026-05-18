package com.example.appcongvien.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcongvien.AppDestinations
import com.example.appcongvien.ui.theme.AppColors

@Composable
fun BottomBar(
    currentDestination: AppDestinations,
    onNavigate: (AppDestinations) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = AppColors.SurfaceWhite,
            shadowElevation = 12.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppDestinations.entries.forEach { destination ->
                    BottomBarItem(
                        destination = destination,
                        selected = destination == currentDestination,
                        onClick = { onNavigate(destination) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    destination: AppDestinations,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) AppColors.WarmOrange else Color.Transparent,
        label = "bottomBarItemBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) AppColors.OnAccent else AppColors.PrimaryGray,
        label = "bottomBarItemContent"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                contentDescription = destination.label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = destination.label,
                fontSize = 10.sp,
                color = contentColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
