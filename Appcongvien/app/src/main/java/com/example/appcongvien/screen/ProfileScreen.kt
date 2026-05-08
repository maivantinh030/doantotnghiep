package com.example.appcongvien.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.components.ParkTopAppBar
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.AuthViewModel

data class UserProfileData(
    val fullName: String,
    val phoneNumber: String,
    val email: String,
    val dateOfBirth: String,
    val membershipLevel: String,
    val joinDate: String,
    val totalVisits: Int,
    val favoriteGame: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val authRepository = (context.applicationContext as App).authRepository
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(authRepository)
    )

    val profileState by viewModel.profileState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var membershipLevel by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(profileState) {
        when (val state = profileState) {
            is Resource.Success -> {
                fullName = state.data.fullName
                phoneNumber = state.data.phoneNumber
                email = state.data.email ?: ""
                dateOfBirth = state.data.dateOfBirth ?: ""
                membershipLevel = state.data.memberLevel ?: "Đồng"
            }

            else -> Unit
        }
    }

    val profileData = remember(fullName, phoneNumber, email, dateOfBirth, membershipLevel) {
        UserProfileData(
            fullName = fullName,
            phoneNumber = phoneNumber,
            email = email,
            dateOfBirth = dateOfBirth,
            membershipLevel = membershipLevel.ifBlank { "Đồng" },
            joinDate = "15/01/2024",
            totalVisits = 23,
            favoriteGame = "Đu quay khổng lồ"
        )
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AppColors.WarmOrange,
        unfocusedBorderColor = AppColors.BorderSubtle,
        disabledBorderColor = AppColors.BorderSubtle.copy(alpha = 0.72f),
        focusedLabelColor = AppColors.WarmOrange,
        unfocusedLabelColor = AppColors.PrimaryGray,
        disabledLabelColor = AppColors.PrimaryGray.copy(alpha = 0.75f),
        focusedLeadingIconColor = AppColors.WarmOrange,
        unfocusedLeadingIconColor = AppColors.PrimaryGray.copy(alpha = 0.75f),
        disabledLeadingIconColor = AppColors.PrimaryGray.copy(alpha = 0.65f),
        focusedContainerColor = AppColors.SurfaceWhite,
        unfocusedContainerColor = AppColors.SurfaceWhite,
        disabledContainerColor = AppColors.SurfaceLight.copy(alpha = 0.58f),
        cursorColor = AppColors.WarmOrange,
        disabledTextColor = AppColors.PrimaryGray.copy(alpha = 0.86f)
    )

    Scaffold(
        topBar = {
            ParkTopAppBar(
                title = "Thông tin cá nhân",
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = { isEditing = !isEditing }
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Lưu" else "Chỉnh sửa",
                            tint = if (isEditing) AppColors.WarmOrange else AppColors.PrimaryDark
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (profileState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.WarmOrange)
                }
            }

            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    AppColors.HeaderGrad1.copy(alpha = 0.16f),
                                    AppColors.SurfaceLight,
                                    AppColors.SurfaceWhite
                                )
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ProfileStateCard(
                        title = "Không thể tải hồ sơ",
                        message = "Vui lòng thử lại để cập nhật thông tin cá nhân."
                    ) {
                        Button(
                            onClick = { viewModel.loadProfile() },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange)
                        ) {
                            Text(
                                text = "Thử lại",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    AppColors.HeaderGrad1.copy(alpha = 0.16f),
                                    AppColors.SurfaceLight,
                                    AppColors.SurfaceWhite
                                )
                            )
                        )
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    ProfileHeroCard(
                        fullName = fullName.ifBlank { "Khách hàng Park Adventure" },
                        membershipLevel = membershipLevel.ifBlank { "Đồng" },
                        isEditing = isEditing
                    )

                    PersonalInfoCard(
                        fullName = fullName,
                        phoneNumber = phoneNumber,
                        email = email,
                        dateOfBirth = dateOfBirth,
                        isEditing = isEditing,
                        onNameChange = { fullName = it },
                        onEmailChange = { email = it },
                        onDateOfBirthChange = { dateOfBirth = it },
                        fieldColors = fieldColors
                    )

                    AccountStatsCard(profileData = profileData)

                    if (isEditing) {
                        Button(
                            onClick = { isEditing = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.WarmOrange,
                                contentColor = AppColors.OnAccent
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lưu thay đổi",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(84.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileHeroCard(
    fullName: String,
    membershipLevel: String,
    isEditing: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box {
                Surface(
                    shape = CircleShape,
                    color = AppColors.WarmOrangeSoft.copy(alpha = 0.8f),
                    modifier = Modifier.size(104.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = fullName
                                .split(" ")
                                .mapNotNull { it.firstOrNull() }
                                .take(2)
                                .joinToString("")
                                .ifBlank { "PA" },
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.WarmOrange
                        )
                    }
                }

                if (isEditing) {
                    Surface(
                        shape = CircleShape,
                        color = AppColors.WarmOrange,
                        modifier = Modifier
                            .size(34.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Đổi ảnh",
                            tint = AppColors.OnAccent,
                            modifier = Modifier.padding(7.dp)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = fullName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
                Text(
                    text = "Quản lý thông tin liên hệ và quyền lợi thành viên của bạn.",
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = AppColors.PrimaryGray.copy(alpha = 0.84f)
                )
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AppColors.YellowWarningContainer.copy(alpha = 0.72f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AppColors.YellowWarning,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Thành viên $membershipLevel",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.YellowWarning
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonalInfoCard(
    fullName: String,
    phoneNumber: String,
    email: String,
    dateOfBirth: String,
    isEditing: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors
) {
    ProfileSectionCard(
        title = "Thông tin cơ bản",
        subtitle = "Cập nhật thông tin liên hệ để nhận thông báo và ưu đãi chính xác."
    ) {
        OutlinedTextField(
            value = fullName,
            onValueChange = onNameChange,
            label = { Text("Họ và tên") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEditing,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {},
            label = { Text("Số điện thoại") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEditing,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors
        )

        OutlinedTextField(
            value = dateOfBirth,
            onValueChange = onDateOfBirthChange,
            label = { Text("Ngày sinh") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEditing,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors
        )
    }
}

@Composable
private fun AccountStatsCard(
    profileData: UserProfileData
) {
    ProfileSectionCard(
        title = "Tổng quan tài khoản",
        subtitle = "Xem nhanh mức độ gắn bó và trò chơi bạn yêu thích nhất."
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBox(
                title = "Lần ghé thăm",
                value = profileData.totalVisits.toString(),
                modifier = Modifier.weight(1f)
            )
            StatBox(
                title = "Tham gia từ",
                value = profileData.joinDate,
                modifier = Modifier.weight(1f)
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = AppColors.SurfaceLight.copy(alpha = 0.78f),
            border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.58f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Trò chơi yêu thích",
                        fontSize = 12.sp,
                        color = AppColors.PrimaryGray.copy(alpha = 0.8f)
                    )
                    Text(
                        text = profileData.favoriteGame,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.PrimaryDark
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = AppColors.WarmOrange,
                    modifier = Modifier.size(8.dp)
                ) {}
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = AppColors.PrimaryGray.copy(alpha = 0.82f)
                )
            }
            content()
        }
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = AppColors.WarmOrangeSoft.copy(alpha = 0.62f),
        border = BorderStroke(1.dp, AppColors.WarmOrangeSoft.copy(alpha = 0.92f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = AppColors.PrimaryGray.copy(alpha = 0.78f)
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.WarmOrange
            )
        }
    }
}

@Composable
private fun ProfileStateCard(
    title: String,
    message: String,
    action: @Composable (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = AppColors.SurfaceWhite,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = AppColors.WarmOrangeSoft.copy(alpha = 0.7f),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = AppColors.WarmOrange,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryDark
            )
            Text(
                text = message,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = AppColors.PrimaryGray.copy(alpha = 0.84f)
            )
            action?.invoke()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}
