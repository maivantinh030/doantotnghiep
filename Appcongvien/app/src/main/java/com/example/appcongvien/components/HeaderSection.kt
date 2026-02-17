package com.example.appcongvien.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcongvien.ui.theme.AppColors

@Composable
fun HeaderSection(
    modifier: Modifier = Modifier,
    userName: String = "Mai Văn Tĩnh",
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Chào mừng trở lại 👋",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userName,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Notification Button
            Surface(
                onClick = onNotificationsClick,
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = AppColors.WarmOrange,
                            contentColor = Color.White,
                            modifier = Modifier.size(18.dp)
                        ) {
                            Text(
                                "3",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                ) {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = "Thông báo",
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.Center),
                        tint = Color.White
                    )
                }
            }

            // Profile Avatar
            Surface(
                onClick = onProfileClick,
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(48.dp)
            ) {
                // Using a placeholder for avatar - replace with actual implementation
                Text(
                    text = userName.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString(""),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE55722)
@Composable
fun HeaderSectionPreview(){
    Surface(
        color = AppColors.WarmOrange,
        modifier = Modifier.padding(16.dp)
    ) {
        HeaderSection()
    }
}