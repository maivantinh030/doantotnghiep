package com.park.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.park.data.model.UserDTO
import com.park.ui.component.*
import com.park.ui.theme.AppColors
import com.park.ui.theme.AppTypography
import com.park.viewmodel.UserManagementViewModel

@Composable
fun UserManagementScreen(viewModel: UserManagementViewModel = viewModel { UserManagementViewModel() }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAdjustDialog by remember { mutableStateOf(false) }
    var adjustUserId by remember { mutableStateOf("") }
    var adjustAmount by remember { mutableStateOf("") }
    var adjustReason by remember { mutableStateOf("") }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        if (uiState.successMessage != null || uiState.errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.SurfaceLight)
            .padding(24.dp)
    ) {
        PageHeader(
            title = "Quản lý Người dùng",
            subtitle = "${uiState.totalUsers} thành viên đăng ký"
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.search(it) },
                placeholder = "Tìm theo tên, SĐT...",
                modifier = Modifier.width(260.dp)
            )
            IconButton(onClick = { viewModel.loadUsers() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = AppColors.WarmOrange)
            }
        }

        // Snackbar messages
        Box(modifier = Modifier.fillMaxWidth()) {
            SnackbarMessage(uiState.successMessage, isError = false, modifier = Modifier.align(Alignment.TopEnd))
            SnackbarMessage(uiState.errorMessage, isError = true, modifier = Modifier.align(Alignment.TopEnd))
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.WarmOrange)
            }
        } else {
            // Table Header
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column {
                    // Header row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColors.SurfaceLight)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Text("Người dùng", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(2f))
                        Text("SĐT", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1.5f))
                        Text("Số dư", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                        Text("Hạng", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                        Text("Trạng thái", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                        Text("Thao tác", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1.5f))
                    }

                    Divider(color = AppColors.LightGray)

                    LazyColumn {
                        items(uiState.users) { user ->
                            UserRow(
                                user = user,
                                onLock = { viewModel.lockUser(user.userId) },
                                onUnlock = { viewModel.unlockUser(user.userId) },
                                onAdjustBalance = {
                                    adjustUserId = user.userId
                                    showAdjustDialog = true
                                },
                                onClick = { viewModel.selectUser(user) }
                            )
                            Divider(color = AppColors.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }

    // Adjust Balance Dialog
    if (showAdjustDialog) {
        Dialog(onDismissRequest = { showAdjustDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AppColors.White)) {
                Column(modifier = Modifier.padding(24.dp).width(320.dp)) {
                    Text("Điều chỉnh số dư", style = AppTypography.titleLarge)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = adjustAmount,
                        onValueChange = { adjustAmount = it },
                        label = { Text("Số tiền (+/-)") },
                        placeholder = { Text("VD: 50000 hoặc -20000") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppColors.WarmOrange)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = adjustReason,
                        onValueChange = { adjustReason = it },
                        label = { Text("Lý do") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppColors.WarmOrange)
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAdjustDialog = false }) { Text("Hủy") }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amount = adjustAmount.toDoubleOrNull() ?: 0.0
                                viewModel.adjustBalance(adjustUserId, amount, adjustReason)
                                showAdjustDialog = false
                                adjustAmount = ""
                                adjustReason = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("Xác nhận") }
                    }
                }
            }
        }
    }

    // User Detail Dialog
    uiState.selectedUser?.let { user ->
        Dialog(onDismissRequest = { viewModel.selectUser(null) }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AppColors.White)) {
                Column(modifier = Modifier.padding(24.dp).width(360.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(48.dp).clip(CircleShape).background(AppColors.WarmOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(user.fullName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(user.fullName, style = AppTypography.titleLarge)
                            MembershipBadge(user.membershipLevel)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Divider(color = AppColors.LightGray)
                    Spacer(Modifier.height(12.dp))
                    InfoRow("SĐT", user.phoneNumber)
                    InfoRow("Email", user.email ?: "-")
                    InfoRow("Số dư", "${user.currentBalance} VND")
                    InfoRow("Điểm tích lũy", "${user.loyaltyPoints} điểm")
                    InfoRow("Giới tính", user.gender ?: "-")
                    InfoRow("Ngày sinh", user.dateOfBirth ?: "-")
                    InfoRow("Trạng thái", user.status)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.selectUser(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Đóng") }
                }
            }
        }
    }
}

@Composable
private fun UserRow(
    user: UserDTO,
    onLock: () -> Unit,
    onUnlock: () -> Unit,
    onAdjustBalance: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(2f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(AppColors.WarmOrange.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(user.fullName.take(1).uppercase(), color = AppColors.WarmOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(Modifier.width(8.dp))
            Text(user.fullName, style = AppTypography.bodyMedium, color = AppColors.PrimaryDark, fontWeight = FontWeight.Medium)
        }
        Text(user.phoneNumber, style = AppTypography.bodyMedium, color = AppColors.PrimaryGray, modifier = Modifier.weight(1.5f))
        Text("${user.currentBalance}đ", style = AppTypography.bodyMedium, color = AppColors.PrimaryDark, modifier = Modifier.weight(1f))
        Box(modifier = Modifier.weight(1f)) { MembershipBadge(user.membershipLevel) }
        Box(modifier = Modifier.weight(1f)) { StatusBadge(user.status) }
        Row(modifier = Modifier.weight(1.5f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (user.status.uppercase() == "ACTIVE") {
                IconButton(onClick = onLock, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Lock, contentDescription = "Khóa", tint = AppColors.RedError, modifier = Modifier.size(16.dp))
                }
            } else {
                IconButton(onClick = onUnlock, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.LockOpen, contentDescription = "Mở khóa", tint = AppColors.GreenSuccess, modifier = Modifier.size(16.dp))
                }
            }
            IconButton(onClick = onAdjustBalance, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.AttachMoney, contentDescription = "Điều chỉnh số dư", tint = AppColors.BluePrimary, modifier = Modifier.size(16.dp))
            }
        }
    }
}
