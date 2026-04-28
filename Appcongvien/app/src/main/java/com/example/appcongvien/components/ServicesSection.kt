package com.example.appcongvien.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attractions
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcongvien.ui.theme.AppColors

data class FeatureItem(
    val title: String,
    val icon: ImageVector,
    val subtitle: String = ""
)

val featureList = listOf(
    FeatureItem("Trò chơi", Icons.Default.Attractions, "Duyệt game"),
    FeatureItem("Lịch sử", Icons.Default.History, "Giao dịch"),
    FeatureItem("Hồ sơ", Icons.Default.Person, "Tài khoản"),
    FeatureItem("Bản đồ", Icons.Default.Map, "Dẫn đường"),
    FeatureItem("Yêu cầu thẻ", Icons.Default.CreditCard, "Cấp thẻ"),
    FeatureItem("Hỗ trợ", Icons.Default.HeadsetMic, "Chat CSKH")
)

@Composable
fun FeatureSection(
    onGameListClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onCardRequestClick: () -> Unit = {},
    onSupportClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Dịch vụ",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryDark
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCard(
                feature = featureList[0],
                modifier = Modifier.weight(1f),
                onClick = onGameListClick
            )
            FeatureCard(
                feature = featureList[1],
                modifier = Modifier.weight(1f),
                onClick = onHistoryClick
            )
            FeatureCard(
                feature = featureList[2],
                modifier = Modifier.weight(1f),
                onClick = onProfileClick
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCard(
                feature = featureList[3],
                modifier = Modifier.weight(1f),
                onClick = { }
            )
            FeatureCard(
                feature = featureList[4],
                modifier = Modifier.weight(1f),
                onClick = onCardRequestClick
            )
            FeatureCard(
                feature = featureList[5],
                modifier = Modifier.weight(1f),
                onClick = onSupportClick
            )
        }
    }
}

@Composable
fun FeatureCard(
    feature: FeatureItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.7f)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppColors.WarmOrangeSoft.copy(alpha = 0.85f),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = AppColors.WarmOrange,
                    modifier = Modifier
                        .padding(11.dp)
                        .fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = feature.title,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryDark,
                lineHeight = 15.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FeatureSectionPreview() {
    Surface(
        color = AppColors.SurfaceLight,
        modifier = Modifier.padding(16.dp)
    ) {
        FeatureSection()
    }
}
