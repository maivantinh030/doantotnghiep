package com.example.appcongvien.screen

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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.AuthViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

    // User profile state
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var membershipLevel by remember { mutableStateOf("") }

    // Load profile when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    // Update UI when profile data loads
    LaunchedEffect(profileState) {
        when (val state = profileState) {
            is Resource.Success -> {
                fullName = state.data.fullName
                phoneNumber = state.data.phoneNumber
                email = state.data.email ?: ""
                dateOfBirth = state.data.dateOfBirth ?: ""
                membershipLevel = state.data.memberLevel ?: "Đồng"
            }
            else -> {}
        }
    }

    // Additional profile data (read-only)
    val profileData = remember(fullName, phoneNumber, email, dateOfBirth, membershipLevel) {
        UserProfileData(
            fullName = fullName,
            phoneNumber = phoneNumber,
            email = email,
            dateOfBirth = dateOfBirth,
            membershipLevel = membershipLevel,
            joinDate = "15/01/2024",
            totalVisits = 23,
            favoriteGame = "Đu Quay Khổng Lồ"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Thông Tin Cá Nhân",
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
                actions = {
                    IconButton(
                        onClick = {
                            if (isEditing) {
                                // Save changes logic would go here
                                isEditing = false
                            } else {
                                isEditing = true
                            }
                        }
                    ) {
                        Icon(
                            if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Lưu" else "Chỉnh sửa",
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

        // Loading State
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Không thể tải thông tin",
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadProfile() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.WarmOrange
                        )
                    ) {
                        Text("Thử lại")
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
                                    AppColors.SurfaceLight,
                                    Color.White
                                )
                            )
                        )
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    // Profile Header with Avatar
                    ProfileHeader(
                        fullName = fullName,
                        membershipLevel = membershipLevel,
                        isEditing = isEditing
                    )

                    // Personal Information Card
                    PersonalInfoCard(
                        fullName = fullName,
                        phoneNumber = phoneNumber,
                        email = email,
                        dateOfBirth = dateOfBirth,
                        isEditing = isEditing,
                        onNameChange = { fullName = it },
                        onEmailChange = { email = it },
                        onDateOfBirthChange = { dateOfBirth = it }
                    )

                    // Account Statistics Card
                    AccountStatsCard(
                        profileData = profileData
                    )

                    // Save Button (only shown when editing)
                    if (isEditing) {
                        Button(
                            onClick = {
                                // Save changes logic
                                isEditing = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.WarmOrange,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Lưu Thay Đổi",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Extra space for bottom navigation
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    fullName: String,
    membershipLevel: String,
    isEditing: Boolean
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Avatar
            Box {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(100.dp),
                    color = AppColors.WarmOrangeSoft
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = fullName.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString(""),
                            fontSize = 36.sp,
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
                            .size(32.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Đổi ảnh",
                            tint = Color.White,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            }

            // Name and membership
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = fullName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFD700).copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Thành viên $membershipLevel",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalInfoCard(
    fullName: String,
    phoneNumber: String,
    email: String,
    dateOfBirth: String,
    isEditing: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Thông Tin Cơ Bản",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryDark
            )

            // Full Name
            OutlinedTextField(
                value = fullName,
                onValueChange = onNameChange,
                label = { Text("Họ và tên") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = AppColors.WarmOrange
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEditing,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.WarmOrange,
                    focusedLabelColor = AppColors.WarmOrange,
                    cursorColor = AppColors.WarmOrange
                )
            )

            // Phone Number (read-only)
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { },
                label = { Text("Số điện thoại") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        tint = AppColors.PrimaryGray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = AppColors.PrimaryGray.copy(alpha = 0.3f),
                    disabledLabelColor = AppColors.PrimaryGray,
                    disabledLeadingIconColor = AppColors.PrimaryGray,
                    disabledTextColor = AppColors.PrimaryGray
                )
            )

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = AppColors.WarmOrange
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEditing,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.WarmOrange,
                    focusedLabelColor = AppColors.WarmOrange,
                    cursorColor = AppColors.WarmOrange
                )
            )

            // Date of Birth
            OutlinedTextField(
                value = dateOfBirth,
                onValueChange = onDateOfBirthChange,
                label = { Text("Ngày sinh") },
                leadingIcon = {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = AppColors.WarmOrange
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEditing,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.WarmOrange,
                    focusedLabelColor = AppColors.WarmOrange,
                    cursorColor = AppColors.WarmOrange
                )
            )
        }
    }
}

@Composable
fun AccountStatsCard(
    profileData: UserProfileData
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Thống Kê Tài Khoản",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryDark
            )

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatBox(
                    title = "Lần đến",
                    value = "${profileData.totalVisits}",
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    title = "Thành viên từ",
                    value = profileData.joinDate,
                    modifier = Modifier.weight(1f)
                )
            }

            // Favorite Game
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = AppColors.SurfaceLight
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Game yêu thích",
                            fontSize = 12.sp,
                            color = AppColors.PrimaryGray
                        )
                        Text(
                            text = profileData.favoriteGame,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.PrimaryDark
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = AppColors.WarmOrange,
                        modifier = Modifier.size(6.dp)
                    ) {}
                }
            }
        }
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = AppColors.WarmOrange.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.WarmOrange
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = AppColors.PrimaryGray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}


