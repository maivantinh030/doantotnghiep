package com.example.appcongvien.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.appcongvien.ui.theme.AppColors

@Composable
fun QuickActions(
    modifier: Modifier = Modifier,
    onTopUpClick: () -> Unit = {},
    onCardRequestClick: () -> Unit = {},
    onBalanceClick: () -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Thao tác nhanh",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryDark,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                icon = Icons.Default.Add,
                title = "Nạp tiền",
                subtitle = "Thêm tiền",
                modifier = Modifier.weight(1f),
                gradientColors = AppColors.ActionGrad1,
                onClick = onTopUpClick
            )
            QuickActionButton(
                icon = Icons.Default.CreditCard,
                title = "Yêu cầu thẻ",
                subtitle = "Đặt trước thẻ",
                modifier = Modifier.weight(1f),
                gradientColors = AppColors.ActionGrad2,
                onClick = onCardRequestClick
            )
            QuickActionButton(
                icon = Icons.Default.Wallet,
                title = "Số dư",
                subtitle = "Xem lịch sử",
                modifier = Modifier.weight(1f),
                gradientColors = AppColors.ActionGrad3,
                onClick = onBalanceClick
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = AppColors.ActionGrad1,
    onClick: () -> Unit = {}
) {
    val accentColor = gradientColors.last()
    val iconBackground = gradientColors.map { it.copy(alpha = 0.22f) }

    Card(
        modifier = modifier
            .height(116.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(iconBackground)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryDark,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.PrimaryGray.copy(alpha = 0.86f),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuickActionsPreview() {
    Surface(color = AppColors.SurfaceLight, modifier = Modifier.padding(16.dp)) {
        QuickActions()
    }
}
