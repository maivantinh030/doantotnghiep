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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
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
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onRegisterSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val entryEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 480, easing = entryEasing),
        label = "registerContentAlpha"
    )
    val contentOffsetY by animateDpAsState(
        targetValue = if (contentVisible) 0.dp else 16.dp,
        animationSpec = tween(durationMillis = 480, easing = entryEasing),
        label = "registerContentOffset"
    )
    val secondaryTextColor = AppColors.PrimaryGray.copy(alpha = 0.8f)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AppColors.WarmOrange,
        unfocusedBorderColor = AppColors.BorderSubtle,
        disabledBorderColor = AppColors.BorderSubtle.copy(alpha = 0.7f),
        focusedLabelColor = AppColors.WarmOrange,
        unfocusedLabelColor = AppColors.PrimaryGray,
        disabledLabelColor = AppColors.PrimaryGray.copy(alpha = 0.7f),
        focusedLeadingIconColor = AppColors.WarmOrange,
        unfocusedLeadingIconColor = AppColors.PrimaryGray.copy(alpha = 0.75f),
        disabledLeadingIconColor = AppColors.PrimaryGray.copy(alpha = 0.5f),
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

    val isFormValid = fullName.isNotBlank() &&
        phoneNumber.isNotBlank() &&
        email.isNotBlank() &&
        password.isNotBlank() &&
        confirmPassword.isNotBlank() &&
        password == confirmPassword &&
        password.length >= 6

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    Scaffold(
        topBar = {
            ParkTopAppBar(
                title = "Đăng Ký",
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
                            Color(0xFFFFF7EF),
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
                                AppColors.WarmOrangeSoft.copy(alpha = 0.52f),
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
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AppColors.SurfaceWhite.copy(alpha = 0.98f),
                                shadowElevation = 8.dp,
                                modifier = Modifier.size(76.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🎟",
                                        fontSize = 28.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Tạo tài khoản mới",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Điền thông tin để bắt đầu hành trình giải trí tại công viên.",
                                fontSize = 14.sp,
                                color = secondaryTextColor,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Họ và tên") },
                            placeholder = { Text("Nhập họ và tên đầy đủ") },
                            leadingIcon = {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            enabled = !isLoading,
                            colors = fieldColors
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
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            enabled = !isLoading,
                            colors = fieldColors
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            placeholder = { Text("Nhập địa chỉ email") },
                            leadingIcon = {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            enabled = !isLoading,
                            colors = fieldColors
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Mật khẩu") },
                            placeholder = { Text("Tối thiểu 6 ký tự") },
                            leadingIcon = {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                androidx.compose.material3.IconButton(
                                    onClick = { passwordVisible = !passwordVisible }
                                ) {
                                    androidx.compose.material3.Icon(
                                        imageVector = if (passwordVisible) {
                                            Icons.Default.Visibility
                                        } else {
                                            Icons.Default.VisibilityOff
                                        },
                                        contentDescription = if (passwordVisible) {
                                            "Ẩn mật khẩu"
                                        } else {
                                            "Hiện mật khẩu"
                                        }
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            enabled = !isLoading,
                            colors = fieldColors,
                            supportingText = if (password.isNotEmpty() && password.length < 6) {
                                { Text("Mật khẩu phải có ít nhất 6 ký tự", color = AppColors.RedError) }
                            } else {
                                null
                            }
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Xác nhận mật khẩu") },
                            placeholder = { Text("Nhập lại mật khẩu") },
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
                                    focusManager.clearFocus()
                                    if (!isLoading && isFormValid) {
                                        isLoading = true
                                        onRegisterSuccess()
                                    }
                                }
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            enabled = !isLoading,
                            colors = fieldColors,
                            supportingText = if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                                { Text("Mật khẩu không khớp", color = AppColors.RedError) }
                            } else {
                                null
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                isLoading = true
                                onRegisterSuccess()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = !isLoading && isFormValid,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.WarmOrange,
                                contentColor = Color.White,
                                disabledContainerColor = AppColors.WarmOrange.copy(alpha = 0.55f),
                                disabledContentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = "Đăng ký",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "Bằng việc đăng ký, bạn đồng ý với Điều khoản sử dụng và Chính sách bảo mật của chúng tôi",
                            fontSize = 12.sp,
                            color = secondaryTextColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Đã có tài khoản? ",
                                color = secondaryTextColor,
                                fontSize = 14.sp
                            )
                            TextButton(onClick = onBackClick) {
                                Text(
                                    text = "Đăng nhập ngay",
                                    color = AppColors.WarmOrange,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
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
fun RegisterScreenPreview() {
    RegisterScreen()
}
