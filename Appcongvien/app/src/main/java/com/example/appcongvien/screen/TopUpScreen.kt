package com.example.appcongvien.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcongvien.components.ParkTopAppBar
import com.example.appcongvien.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onTopUpSuccess: () -> Unit = {}
) {
    var topUpAmount by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val quickAmounts = listOf(50_000, 100_000, 200_000, 500_000, 1_000_000)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AppColors.WarmOrange,
        unfocusedBorderColor = AppColors.BorderSubtle,
        focusedLabelColor = AppColors.WarmOrange,
        unfocusedLabelColor = AppColors.PrimaryGray,
        focusedLeadingIconColor = AppColors.WarmOrange,
        unfocusedLeadingIconColor = AppColors.PrimaryGray.copy(alpha = 0.75f),
        cursorColor = AppColors.WarmOrange,
        focusedContainerColor = AppColors.SurfaceWhite,
        unfocusedContainerColor = AppColors.SurfaceWhite
    )

    Scaffold(
        topBar = {
            ParkTopAppBar(
                title = "Nạp Tiền",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFFAF4),
                            AppColors.SurfaceLight,
                            AppColors.SurfaceWhite
                        )
                    )
                )
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    AppColors.CardGrad1,
                                    Color(0xFF524B45)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = AppColors.WarmOrangeSoft,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Số dư hiện tại",
                                color = Color.White.copy(alpha = 0.76f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = "250,000 VND",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Text(
                            text = "1,250 điểm thưởng khả dụng",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Chọn số tiền nạp",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
                Text(
                    text = "Nhập số tiền mong muốn hoặc chọn nhanh từ các mức phổ biến bên dưới.",
                    fontSize = 13.sp,
                    color = AppColors.PrimaryGray.copy(alpha = 0.82f),
                    lineHeight = 18.sp
                )

                OutlinedTextField(
                    value = topUpAmount,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                            topUpAmount = newValue
                        }
                    },
                    label = { Text("Nhập số tiền (VND)") },
                    placeholder = { Text("Ví dụ: 100000") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null
                        )
                    }
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AppColors.WarmOrangeSoft.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = "Mức nạp tối thiểu là 10,000 VND.",
                        fontSize = 12.sp,
                        color = AppColors.WarmOrange,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Chọn nhanh",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        quickAmounts.take(3).forEach { amount ->
                            QuickAmountChip(
                                amount = amount,
                                isSelected = topUpAmount == amount.toString(),
                                onClick = { topUpAmount = amount.toString() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        quickAmounts.drop(3).forEach { amount ->
                            QuickAmountChip(
                                amount = amount,
                                isSelected = topUpAmount == amount.toString(),
                                onClick = { topUpAmount = amount.toString() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Phương thức thanh toán",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, AppColors.WarmOrangeSoft.copy(alpha = 0.9f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = AppColors.WarmOrangeSoft.copy(alpha = 0.75f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
                                contentDescription = null,
                                tint = AppColors.WarmOrange,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ví MoMo",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark
                            )
                            Text(
                                text = "Thanh toán qua ứng dụng MoMo nhanh và tiện lợi.",
                                fontSize = 12.sp,
                                color = AppColors.PrimaryGray.copy(alpha = 0.82f),
                                lineHeight = 17.sp
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF3BA55D).copy(alpha = 0.14f),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF3BA55D),
                                modifier = Modifier.padding(7.dp)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (topUpAmount.isNotEmpty() && topUpAmount.toIntOrNull() != null) {
                        isLoading = true
                        onTopUpSuccess()
                        isLoading = false
                    }
                },
                enabled = topUpAmount.isNotEmpty() &&
                    topUpAmount.toIntOrNull() != null &&
                    topUpAmount.toInt() >= 10_000 &&
                    !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.WarmOrange,
                    contentColor = Color.White,
                    disabledContainerColor = AppColors.WarmOrange.copy(alpha = 0.55f),
                    disabledContentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nạp tiền qua MoMo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (topUpAmount.isNotEmpty() && topUpAmount.toIntOrNull() != null && topUpAmount.toInt() < 10_000) {
                Text(
                    text = "Số tiền nạp tối thiểu là 10,000 VND",
                    fontSize = 12.sp,
                    color = Color(0xFFE45A4F),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun QuickAmountChip(
    amount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayText = when {
        amount >= 1_000_000 -> "${amount / 1_000_000}M"
        amount >= 1_000 -> "${amount / 1_000}K"
        else -> amount.toString()
    }

    Surface(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) AppColors.WarmOrange else AppColors.SurfaceWhite,
        border = BorderStroke(
            1.dp,
            if (isSelected) AppColors.WarmOrange else AppColors.BorderSubtle
        ),
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = displayText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else AppColors.PrimaryDark
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TopUpScreenPreview() {
    TopUpScreen()
}
