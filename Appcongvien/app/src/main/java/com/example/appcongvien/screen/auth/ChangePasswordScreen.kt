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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcongvien.components.ParkTopAppBar
import com.example.appcongvien.ui.theme.AppColors

@Composable
fun ChangePasswordScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onPasswordChanged: () -> Unit = {}
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val entryEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 480, easing = entryEasing),
        label = "changePasswordAlpha"
    )
    val contentOffsetY by animateDpAsState(
        targetValue = if (contentVisible) 0.dp else 16.dp,
        animationSpec = tween(durationMillis = 480, easing = entryEasing),
        label = "changePasswordOffset"
    )
    val secondaryTextColor = AppColors.PrimaryGray.copy(alpha = 0.8f)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AppColors.WarmOrange,
        unfocusedBorderColor = AppColors.BorderSubtle,
        disabledBorderColor = AppColors.BorderSubtle.copy(alpha = 0.7f),
        focusedLabelColor = AppColors.WarmOrange,
        unfocusedLabelColor = AppColors.PrimaryGray,
        focusedLeadingIconColor = AppColors.WarmOrange,
        unfocusedLeadingIconColor = AppColors.PrimaryGray.copy(alpha = 0.75f),
        focusedTrailingIconColor = AppColors.PrimaryGray,
        unfocusedTrailingIconColor = AppColors.PrimaryGray.copy(alpha = 0.75f),
        cursorColor = AppColors.WarmOrange,
        focusedContainerColor = AppColors.SurfaceWhite,
        unfocusedContainerColor = AppColors.SurfaceWhite,
        disabledContainerColor = AppColors.SurfaceWhite,
        errorBorderColor = AppColors.RedError,
        errorLeadingIconColor = AppColors.RedError,
        errorTrailingIconColor = AppColors.RedError
    )

    val isFormValid = oldPassword.isNotBlank() &&
        newPassword.isNotBlank() &&
        confirmPassword.isNotBlank() &&
        newPassword == confirmPassword &&
        newPassword.length >= 6 &&
        newPassword != oldPassword

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    Scaffold(
        topBar = {
            ParkTopAppBar(
                title = "Đổi Mật Khẩu",
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isSuccess) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = AppColors.GreenSuccess.copy(alpha = 0.14f),
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AppColors.GreenSuccess,
                                        modifier = Modifier.padding(20.dp)
                                    )
                                }

                                Text(
                                    text = "Đổi mật khẩu thành công",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.PrimaryDark,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "Mật khẩu của bạn đã được cập nhật. Hãy dùng mật khẩu mới cho lần đăng nhập tiếp theo.",
                                    fontSize = 14.sp,
                                    color = secondaryTextColor,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )

                                Button(
                                    onClick = onPasswordChanged,
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
                                        text = "Hoàn thành",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = AppColors.WarmOrangeSoft.copy(alpha = 0.72f),
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = AppColors.WarmOrange,
                                        modifier = Modifier.padding(20.dp)
                                    )
                                }

                                Text(
                                    text = "Thay đổi mật khẩu",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.PrimaryDark,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "Nhập mật khẩu hiện tại và mật khẩu mới để bảo vệ tài khoản của bạn.",
                                    fontSize = 14.sp,
                                    color = secondaryTextColor,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }

                            OutlinedTextField(
                                value = oldPassword,
                                onValueChange = { oldPassword = it },
                                label = { Text("Mật khẩu hiện tại") },
                                placeholder = { Text("Nhập mật khẩu hiện tại") },
                                leadingIcon = {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    androidx.compose.material3.IconButton(
                                        onClick = { oldPasswordVisible = !oldPasswordVisible }
                                    ) {
                                        androidx.compose.material3.Icon(
                                            imageVector = if (oldPasswordVisible) {
                                                Icons.Default.Visibility
                                            } else {
                                                Icons.Default.VisibilityOff
                                            },
                                            contentDescription = if (oldPasswordVisible) {
                                                "Ẩn mật khẩu"
                                            } else {
                                                "Hiện mật khẩu"
                                            }
                                        )
                                    }
                                },
                                visualTransformation = if (oldPasswordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions.Default,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                enabled = !isLoading,
                                colors = fieldColors
                            )

                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("Mật khẩu mới") },
                                placeholder = { Text("Tối thiểu 6 ký tự") },
                                leadingIcon = {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    androidx.compose.material3.IconButton(
                                        onClick = { newPasswordVisible = !newPasswordVisible }
                                    ) {
                                        androidx.compose.material3.Icon(
                                            imageVector = if (newPasswordVisible) {
                                                Icons.Default.Visibility
                                            } else {
                                                Icons.Default.VisibilityOff
                                            },
                                            contentDescription = if (newPasswordVisible) {
                                                "Ẩn mật khẩu"
                                            } else {
                                                "Hiện mật khẩu"
                                            }
                                        )
                                    }
                                },
                                visualTransformation = if (newPasswordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                enabled = !isLoading,
                                colors = fieldColors,
                                supportingText = when {
                                    newPassword.isNotEmpty() && newPassword.length < 6 -> {
                                        { Text("Mật khẩu phải có ít nhất 6 ký tự", color = AppColors.RedError) }
                                    }

                                    newPassword.isNotEmpty() && newPassword == oldPassword -> {
                                        { Text("Mật khẩu mới phải khác mật khẩu hiện tại", color = AppColors.RedError) }
                                    }

                                    else -> null
                                }
                            )

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Xác nhận mật khẩu mới") },
                                placeholder = { Text("Nhập lại mật khẩu mới") },
                                leadingIcon = {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    androidx.compose.material3.IconButton(
                                        onClick = { confirmPasswordVisible = !confirmPasswordVisible }
                                    ) {
                                        androidx.compose.material3.Icon(
                                            imageVector = if (confirmPasswordVisible) {
                                                Icons.Default.Visibility
                                            } else {
                                                Icons.Default.VisibilityOff
                                            },
                                            contentDescription = if (confirmPasswordVisible) {
                                                "Ẩn mật khẩu"
                                            } else {
                                                "Hiện mật khẩu"
                                            }
                                        )
                                    }
                                },
                                visualTransformation = if (confirmPasswordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (!isLoading && isFormValid) {
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
                                colors = fieldColors,
                                supportingText = if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                                    { Text("Mật khẩu xác nhận không khớp", color = AppColors.RedError) }
                                } else {
                                    null
                                }
                            )

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = AppColors.WarmOrangeSoft.copy(alpha = 0.42f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Gợi ý tạo mật khẩu mạnh",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.WarmOrange
                                    )
                                    Text(
                                        text = "• Sử dụng ít nhất 8 ký tự\n• Kết hợp chữ hoa, chữ thường và số\n• Không dùng thông tin cá nhân dễ đoán",
                                        fontSize = 12.sp,
                                        color = secondaryTextColor,
                                        lineHeight = 18.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    isLoading = true
                                    isLoading = false
                                    isSuccess = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                enabled = !isLoading && isFormValid,
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
                                        text = "Đổi mật khẩu",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChangePasswordScreenPreview() {
    ChangePasswordScreen()
}
