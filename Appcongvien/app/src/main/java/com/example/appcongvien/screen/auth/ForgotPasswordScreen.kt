package com.example.appcongvien.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.appcongvien.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onOtpSent: () -> Unit = {}
) {
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Quên Mật Khẩu",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.WarmOrange
                )
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
                            AppColors.SurfaceLight,
                            Color.White
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {

                        if (isSuccess) {
                            // Success State
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                                modifier = Modifier.size(80.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.padding(20.dp)
                                )
                            }

                            Text(
                                text = "Đã gửi mã OTP",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Chúng tôi đã gửi mã xác thực 6 chữ số đến số điện thoại của bạn. Vui lòng kiểm tra tin nhắn SMS.",
                                fontSize = 14.sp,
                                color = AppColors.PrimaryGray,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )

                            Text(
                                text = "Số điện thoại: ${phoneNumber}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.WarmOrange,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Continue Button
                            Button(
                                onClick = onOtpSent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.WarmOrange,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Tiếp tục",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Back to Login
                            TextButton(onClick = onBackClick) {
                                Text(
                                    "Quay lại đăng nhập",
                                    color = AppColors.WarmOrange,
                                    fontSize = 14.sp
                                )
                            }

                        } else {
                            // Input State
                            Surface(
                                shape = CircleShape,
                                color = AppColors.WarmOrangeSoft,
                                modifier = Modifier.size(80.dp)
                            ) {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = AppColors.WarmOrange,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }

                            Text(
                                text = "Khôi phục mật khẩu",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Nhập số điện thoại đã đăng ký để nhận mã OTP khôi phục mật khẩu",
                                fontSize = 14.sp,
                                color = AppColors.PrimaryGray,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )

                            // Phone Number Input
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("Số điện thoại") },
                                placeholder = { Text("Nhập số điện thoại") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = AppColors.WarmOrange
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.WarmOrange,
                                    focusedLabelColor = AppColors.WarmOrange,
                                    cursorColor = AppColors.WarmOrange
                                )
                            )

                            // Send OTP Button
                            Button(
                                onClick = {
                                    isLoading = true
                                    // Simulate OTP sending
                                    isLoading = false
                                    isSuccess = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                enabled = !isLoading && phoneNumber.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.WarmOrange,
                                    contentColor = Color.White,
                                    disabledContainerColor = AppColors.PrimaryGray.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        "Gửi mã OTP",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Back to Login
                            TextButton(onClick = onBackClick) {
                                Text(
                                    "Quay lại đăng nhập",
                                    color = AppColors.WarmOrange,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // Help Text
                if (!isSuccess) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Bạn sẽ nhận được tin nhắn SMS chứa mã OTP 6 chữ số để xác thực tài khoản",
                        color = AppColors.PrimaryGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
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


