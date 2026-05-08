package com.example.appcongvien.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcongvien.ui.theme.AppColors

@Composable
fun CardSection(
    modifier: Modifier = Modifier,
    balance: String = "0",
    onCardInfoClick: () -> Unit = {},
    onBalanceToggleClick: () -> Unit = {},
    onScanCardClick: () -> Unit = {}
) {
    var isBalanceVisible by remember { mutableStateOf(true) }

    val formattedBalance = remember(balance) {
        try {
            val cleanedBalance = balance.trim()
            val balanceDouble = cleanedBalance.toDoubleOrNull()
            if (balanceDouble == null || balanceDouble == 0.0) {
                "0"
            } else {
                val balanceInt = balanceDouble.toLong()
                java.text.DecimalFormat("#,###").format(balanceInt)
            }
        } catch (_: Exception) {
            balance
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Park Adventure",
                    color = AppColors.CardPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )
                Text(
                    text = "Thành viên cao cấp",
                    color = AppColors.CardSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Surface(
                onClick = onCardInfoClick,
                shape = RoundedCornerShape(12.dp),
                color = AppColors.CardPrimary.copy(alpha = 0.14f),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Card Info",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp),
                    tint = AppColors.CardSecondary
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Số dư khả dụng",
                        fontSize = 12.sp,
                        color = AppColors.CardPrimary.copy(alpha = 0.78f),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = {
                            isBalanceVisible = !isBalanceVisible
                            onBalanceToggleClick()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isBalanceVisible) {
                                Icons.Outlined.Visibility
                            } else {
                                Icons.Outlined.VisibilityOff
                            },
                            contentDescription = if (isBalanceVisible) {
                                "Ẩn số dư"
                            } else {
                                "Hiển thị số dư"
                            },
                            modifier = Modifier.size(16.dp),
                            tint = AppColors.CardPrimary.copy(alpha = 0.72f)
                        )
                    }
                }

                Text(
                    text = if (isBalanceVisible) "$formattedBalance VND" else "••••••• VND",
                    fontSize = 26.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.CardPrimary,
                    letterSpacing = 0.2.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Smart Card",
                    fontSize = 11.sp,
                    color = AppColors.CardPrimary.copy(alpha = 0.68f)
                )
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = AppColors.CardPrimary.copy(alpha = 0.9f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = AppColors.CardPrimary.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Sẵn sàng sử dụng",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.CardPrimary
                    )
                    Text(
                        text = "Thanh toán và check-in nhanh trong công viên",
                        fontSize = 11.sp,
                        color = AppColors.CardPrimary.copy(alpha = 0.7f),
                        lineHeight = 15.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.CardPrimary.copy(alpha = 0.14f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = AppColors.CardSecondary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF374151)
@Composable
fun CardSectionWithScanPreview() {
    Surface(
        color = Color(0xFF374151),
        modifier = Modifier.padding(20.dp)
    ) {
        CardSection()
    }
}
