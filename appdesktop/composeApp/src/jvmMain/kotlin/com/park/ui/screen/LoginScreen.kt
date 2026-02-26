package com.park.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.park.ui.theme.AppColors
import com.park.ui.theme.AppTypography
import com.park.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel { AuthViewModel() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoginSuccess()
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left - Brand Panel
        Box(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(AppColors.WarmOrange, AppColors.OrangeDark)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("PA", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(24.dp))
                Text("Park Adventure", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Admin Dashboard", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                Spacer(Modifier.height(40.dp))
                Text(
                    "Quản lý toàn bộ hệ thống\ncông viên giải trí",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Right - Login Form
        Box(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxHeight()
                .background(AppColors.SurfaceLight),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.width(400.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(40.dp)) {
                    Text(
                        "Đăng nhập",
                        style = AppTypography.headlineLarge,
                        color = AppColors.PrimaryDark
                    )
                    Text(
                        "Vui lòng nhập thông tin tài khoản admin",
                        style = AppTypography.bodyMedium,
                        color = AppColors.PrimaryGray,
                        modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                    )

                    var phone by remember { mutableStateOf("") }
                    var password by remember { mutableStateOf("") }
                    var showPassword by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Số điện thoại") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.WarmOrange,
                            focusedLabelColor = AppColors.WarmOrange,
                            cursorColor = AppColors.WarmOrange,
                            focusedLeadingIconColor = AppColors.WarmOrange
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mật khẩu") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.WarmOrange,
                            focusedLabelColor = AppColors.WarmOrange,
                            cursorColor = AppColors.WarmOrange,
                            focusedLeadingIconColor = AppColors.WarmOrange
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uiState.errorMessage != null) {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AppColors.RedError.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                uiState.errorMessage!!,
                                color = AppColors.RedError,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp, 8.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.login(phone, password) },
                        enabled = phone.isNotBlank() && password.isNotBlank() && !uiState.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Đăng nhập", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
