package com.example.appcongvien.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.appcongvien.components.ParkTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // Quick amount options
    val quickAmounts = listOf(50_000, 100_000, 200_000, 500_000, 1_000_000)

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
                            AppColors.SurfaceLight,
                            Color.White
                        )
                    )
                )
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            // Current Balance Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    AppColors.CardGrad1,
                                    AppColors.CardGrad2
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Số Dư Hiện Tại",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "250,000 VND",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "1,250 điểm thưởng",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Top-up Section
            Text(
                text = "Chọn Số Tiền Nạp",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.PrimaryDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input
            OutlinedTextField(
                value = topUpAmount,
                onValueChange = { newValue ->
                    // Only allow numbers
                    if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                        topUpAmount = newValue
                    }
                },
                label = { Text("Nhập số tiền (VND)") },
                placeholder = { Text("Ví dụ: 100000") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.WarmOrange,
                    focusedLabelColor = AppColors.WarmOrange,
                    cursorColor = AppColors.WarmOrange
                ),
                leadingIcon = {
                    Icon(
                        Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = AppColors.WarmOrange
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Select Amounts
            Text(
                text = "Chọn Nhanh",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.PrimaryGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Amount chips in rows
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // First row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickAmounts.take(3).forEach { amount ->
                        QuickAmountChip(
                            amount = amount,
                            isSelected = topUpAmount == amount.toString(),
                            onClick = { topUpAmount = amount.toString() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Second row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickAmounts.drop(3).forEach { amount ->
                        QuickAmountChip(
                            amount = amount,
                            isSelected = topUpAmount == amount.toString(),
                            onClick = { topUpAmount = amount.toString() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Empty space for alignment
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Payment Method Section
            Text(
                text = "Phương Thức Thanh Toán",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.PrimaryDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            // MoMo Payment Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    AppColors.WarmOrangeSoft
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AppColors.WarmOrangeSoft,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Smartphone,
                            contentDescription = null,
                            tint = AppColors.WarmOrange,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Ví MoMo",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.PrimaryDark
                        )
                        Text(
                            "Thanh toán qua ứng dụng MoMo",
                            fontSize = 12.sp,
                            color = AppColors.PrimaryGray
                        )
                    }

                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AppColors.WarmOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Top Up Button
            Button(
                onClick = {
                    if (topUpAmount.isNotEmpty() && topUpAmount.toIntOrNull() != null) {
                        isLoading = true
                        // Simulate MoMo payment flow
                        // In real app: launch MoMo intent or show QR code
                    }
                },
                enabled = topUpAmount.isNotEmpty() &&
                        topUpAmount.toIntOrNull() != null &&
                        topUpAmount.toInt() >= 10_000 &&
                        !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.WarmOrange,
                    contentColor = Color.White,
                    disabledContainerColor = AppColors.PrimaryGray
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.QrCode,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Nạp Tiền Qua MoMo",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Helper text
            if (topUpAmount.isNotEmpty() && topUpAmount.toIntOrNull() != null && topUpAmount.toInt() < 10_000) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Số tiền nạp tối thiểu là 10,000 VND",
                    fontSize = 12.sp,
                    color = Color.Red.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Extra space for bottom navigation
            Spacer(modifier = Modifier.height(100.dp))
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
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) AppColors.WarmOrange else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) AppColors.WarmOrange else AppColors.PrimaryGray.copy(alpha = 0.3f)
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
                fontWeight = FontWeight.SemiBold,
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