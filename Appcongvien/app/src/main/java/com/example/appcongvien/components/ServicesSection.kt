package com.example.appcongvien.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attractions
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
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
    FeatureItem("Thông tin thẻ", Icons.Default.ConfirmationNumber, "Xem chi tiết"),
    FeatureItem("Trò chơi", Icons.Default.Attractions, "Duyệt game"),
    FeatureItem("Lịch sử", Icons.Default.History, "Giao dịch"),
    FeatureItem("Hồ sơ", Icons.Default.Person, "Tài khoản của tôi"),
    FeatureItem("Khóa thẻ", Icons.Default.Lock, "Bảo mật"),
    FeatureItem("Bản đồ ", Icons.Default.Map, "Dẫn đường")
)

@Composable
fun FeatureSection(
    onCardInfoClick: () -> Unit = {},
    onGameListClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLockCardClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Dịch vụ",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.PrimaryDark
        )

        Spacer(Modifier.height(16.dp))

        // Row 1 - First 3 items
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCard(
                feature = featureList[0],
                modifier = Modifier.weight(1f),
                onClick = { onCardInfoClick() }
            )
            FeatureCard(
                feature = featureList[1],
                modifier = Modifier.weight(1f),
                onClick = { onGameListClick() }
            )
            FeatureCard(
                feature = featureList[2],
                modifier = Modifier.weight(1f),
                onClick = { onHistoryClick() }
            )
        }

        Spacer(Modifier.height(12.dp))

        // Row 2 - Last 3 items
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCard(
                feature = featureList[3],
                modifier = Modifier.weight(1f),
                onClick = { onProfileClick() }
            )
            FeatureCard(
                feature = featureList[4],
                modifier = Modifier.weight(1f),
                onClick = { onLockCardClick() }
            )
            FeatureCard(
                feature = featureList[5],
                modifier = Modifier.weight(1f),
                onClick = { }
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
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = AppColors.WarmOrangeSoft,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    feature.icon,
                    contentDescription = null,
                    tint = AppColors.WarmOrange,
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxSize()
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = feature.title,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.PrimaryDark,
                lineHeight = 13.sp
            )


        }
    }
}

@Preview(showBackground = true)
@Composable
fun FeatureSectionPreview(){
    Surface(
        color = Color.White,
        modifier = Modifier.padding(16.dp)
    ) {
        FeatureSection()
    }
}