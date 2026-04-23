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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    val app = LocalContext.current.applicationContext as App
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(app.authRepository))

    val loginState by authViewModel.loginState.collectAsState()
    val focusManager = LocalFocusManager.current

    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var serverError by remember { mutableStateOf<String?>(null) }
    var contentVisible by remember { mutableStateOf(false) }

    val isLoading = loginState is Resource.Loading
    val entryEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 520, easing = entryEasing),
        label = "loginContentAlpha"
    )
    val contentOffsetY by animateDpAsState(
        targetValue = if (contentVisible) 0.dp else 16.dp,
        animationSpec = tween(durationMillis = 520, easing = entryEasing),
        label = "loginContentOffset"
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
        cursorColor = AppColors.WarmOrange,
        focusedContainerColor = AppColors.SurfaceWhite,
        unfocusedContainerColor = AppColors.SurfaceWhite,
        disabledContainerColor = AppColors.SurfaceWhite,
        errorBorderColor = AppColors.RedError,
        errorLeadingIconColor = AppColors.RedError
    )

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is Resource.Success -> {
                authViewModel.resetLoginState()
                onLoginSuccess()
            }

            is Resource.Error -> {
                serverError = state.message
            }

            else -> {}
        }
    }

    fun attemptLogin() {
        phoneError = null
        passwordError = null
        serverError = null

        val phoneRegex = Regex("^0[0-9]{9}$")
        var valid = true

        if (phoneNumber.isBlank()) {
            phoneError = "Vui lòng nhập số điện thoại"
            valid = false
        } else if (!phoneRegex.matches(phoneNumber.trim())) {
            phoneError = "Số điện thoại không hợp lệ (10 chữ số, bắt đầu bằng 0)"
            valid = false
        }

        if (password.isBlank()) {
            passwordError = "Vui lòng nhập mật khẩu"
            valid = false
        } else if (password.length < 6) {
            passwordError = "Mật khẩu phải có ít nhất 6 ký tự"
            valid = false
        }

        if (valid) {
            focusManager.clearFocus()
            authViewModel.login(phoneNumber.trim(), password)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
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
                            AppColors.WarmOrangeSoft.copy(alpha = 0.55f),
                            AppColors.SurfaceLight.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .offset(y = contentOffsetY)
                .alpha(contentAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = AppColors.SurfaceWhite.copy(alpha = 0.96f),
                shadowElevation = 12.dp,
                modifier = Modifier.size(96.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🎪",
                        fontSize = 34.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Park Adventure",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryDark,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Chào mừng bạn trở lại!",
                fontSize = 17.sp,
                color = secondaryTextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Đăng nhập",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            phoneError = null
                            serverError = null
                        },
                        label = { Text("Số điện thoại") },
                        placeholder = { Text("Nhập số điện thoại") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null
                            )
                        },
                        isError = phoneError != null,
                        supportingText = if (phoneError != null) {
                            { Text(phoneError!!, color = AppColors.RedError, fontSize = 12.sp) }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        colors = fieldColors
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                            serverError = null
                        },
                        label = { Text("Mật khẩu") },
                        placeholder = { Text("Nhập mật khẩu") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) {
                                        Icons.Default.Visibility
                                    } else {
                                        Icons.Default.VisibilityOff
                                    },
                                    contentDescription = if (passwordVisible) {
                                        "Ẩn mật khẩu"
                                    } else {
                                        "Hiện mật khẩu"
                                    },
                                    tint = AppColors.PrimaryGray
                                )
                            }
                        },
                        isError = passwordError != null,
                        supportingText = if (passwordError != null) {
                            { Text(passwordError!!, color = AppColors.RedError, fontSize = 12.sp) }
                        } else null,
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { attemptLogin() }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        colors = fieldColors
                    )

                    TextButton(
                        onClick = onForgotPasswordClick,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "Quên mật khẩu?",
                            color = AppColors.WarmOrange,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (serverError != null) {
                        Text(
                            text = serverError!!,
                            color = AppColors.RedError,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = AppColors.RedError.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }

                    Button(
                        onClick = { attemptLogin() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !isLoading,
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
                                text = "Đăng nhập",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chưa có tài khoản? ",
                            color = secondaryTextColor,
                            fontSize = 14.sp
                        )
                        TextButton(onClick = onRegisterClick) {
                            Text(
                                text = "Đăng ký ngay",
                                color = AppColors.WarmOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Bằng việc đăng nhập, bạn đồng ý với\nĐiều khoản sử dụng và Chính sách bảo mật",
                color = secondaryTextColor,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}
