package com.example.appcongvien.screen.auth

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcongvien.components.ParkTopAppBar
import com.example.appcongvien.ui.theme.AppColors

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onOtpSent: () -> Unit = {}
) {
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val entryEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 480, easing = entryEasing),
        label = "forgotContentAlpha"
    )
    val contentOffsetY by animateDpAsState(
        targetValue = if (contentVisible) 0.dp else 16.dp,
        animationSpec = tween(durationMillis = 480, easing = entryEasing),
        label = "forgotContentOffset"
    )
    val secondaryTextColor = AppColors.PrimaryGray.copy(alpha = 0.8f)
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

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    Scaffold(
        topBar = {
            ParkTopAppBar(
                title = "Quên Mật Khẩu",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AppColors.BackgroundWarm,
                            AppColors.SurfaceLight,
                            AppColors.SurfaceWhite
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                AppColors.WarmOrangeSoft.copy(alpha = 0.5f),
                                AppColors.SurfaceLight.copy(alpha = 0.18f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .offset(y = contentOffsetY)
                    .alpha(contentAlpha),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.72f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (isSuccess) {
                            Surface(
                                shape = CircleShape,
                                color = AppColors.GreenSuccess.copy(alpha = 0.14f),
                                modifier = Modifier.size(78.dp)
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AppColors.GreenSuccess,
                                    modifier = Modifier.padding(19.dp)
                                )
                            }

                            Text(
                                text = "Đã gửi mã OTP",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Chúng tôi đã gửi mã xác thực 6 chữ số đến số điện thoại của bạn. Vui lòng kiểm tra tin nhắn SMS.",
                                fontSize = 14.sp,
                                color = secondaryTextColor,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = AppColors.WarmOrangeSoft.copy(alpha = 0.55f)
                            ) {
                                Text(
                                    text = "Số điện thoại: $phoneNumber",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.WarmOrange,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }

                            Button(
                                onClick = onOtpSent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.WarmOrange,
                                    contentColor = AppColors.OnAccent
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    text = "Tiếp tục",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            TextButton(onClick = onBackClick) {
                                Text(
                                    text = "Quay lại đăng nhập",
                                    color = AppColors.WarmOrange,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = AppColors.WarmOrangeSoft.copy(alpha = 0.72f),
                                modifier = Modifier.size(78.dp)
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = AppColors.WarmOrange,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }

                            Text(
                                text = "Khôi phục mật khẩu",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Nhập số điện thoại đã đăng ký để nhận mã OTP khôi phục mật khẩu.",
                                fontSize = 14.sp,
                                color = secondaryTextColor,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )

                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("Số điện thoại") },
                                placeholder = { Text("Nhập số điện thoại") },
                                leadingIcon = {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (!isLoading && phoneNumber.isNotBlank()) {
                                            isLoading = true
                                            isLoading = false
                                            isSuccess = true
                                        }
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                enabled = !isLoading,
                                colors = fieldColors
                            )

                            Button(
                                onClick = {
                                    isLoading = true
                                    isLoading = false
                                    isSuccess = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                enabled = !isLoading && phoneNumber.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.WarmOrange,
                                    contentColor = AppColors.OnAccent,
                                    disabledContainerColor = AppColors.WarmOrange.copy(alpha = 0.55f),
                                    disabledContentColor = AppColors.OnAccent
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = AppColors.OnAccent,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Text(
                                        text = "Gửi mã OTP",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            TextButton(onClick = onBackClick) {
                                Text(
                                    text = "Quay lại đăng nhập",
                                    color = AppColors.WarmOrange,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                if (!isSuccess) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Bạn sẽ nhận được tin nhắn SMS chứa mã OTP 6 chữ số để xác thực tài khoản.",
                        color = secondaryTextColor,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen()
}
