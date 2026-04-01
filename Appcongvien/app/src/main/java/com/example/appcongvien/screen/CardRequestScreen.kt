package com.example.appcongvien.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.components.ParkTopAppBar
import com.example.appcongvien.data.model.CardRequestDTO
import com.example.appcongvien.data.repository.CardRequestRepository
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.CardRequestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardRequestScreen(
    repository: CardRequestRepository,
    onBackClick: () -> Unit = {}
) {
    val viewModel: CardRequestViewModel = viewModel(
        factory = CardRequestViewModel.Factory(repository)
    )
    val uiState by viewModel.uiState.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        if (uiState.successMessage != null || uiState.errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Xác nhận yêu cầu cấp thẻ", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Sau khi gửi, nhân viên sẽ xem xét và duyệt yêu cầu.\nVui lòng đến quầy để nhận thẻ vật lý khi được duyệt.",
                    fontSize = 14.sp,
                    color = AppColors.PrimaryGray,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.submitRequest(note = null, depositAmount = null)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Gửi yêu cầu", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Hủy")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = { ParkTopAppBar(title = "Yêu cầu cấp thẻ", onBackClick = onBackClick) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppColors.SurfaceLight),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.BluePrimary.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = AppColors.BluePrimary, modifier = Modifier.size(24.dp))
                        Column {
                            Text("Cách hoạt động", fontWeight = FontWeight.Bold, color = AppColors.PrimaryDark, fontSize = 14.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "1. Gửi yêu cầu qua app\n2. Nhân viên xem xét và duyệt\n3. Đến quầy để nhận thẻ vật lý\n4. Thẻ sẽ được liên kết với tài khoản của bạn",
                                fontSize = 13.sp, color = AppColors.PrimaryGray, lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // Request form
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Gửi yêu cầu mới", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = AppColors.PrimaryDark)

                        uiState.successMessage?.let {
                            Text(it, color = AppColors.GreenSuccess, fontSize = 13.sp)
                        }
                        uiState.errorMessage?.let {
                            Text(it, color = AppColors.RedError, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { showConfirmDialog = true },
                            enabled = !uiState.isSending,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange)
                        ) {
                            if (uiState.isSending) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Yêu cầu cấp thẻ", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // History
            if (uiState.requests.isNotEmpty()) {
                item {
                    Text(
                        "Yêu cầu của tôi (${uiState.requests.size})",
                        fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = AppColors.PrimaryDark
                    )
                }
                items(uiState.requests) { req ->
                    CardRequestItem(req)
                }
            } else if (!uiState.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Chưa có yêu cầu nào", color = AppColors.PrimaryGray)
                    }
                }
            }
        }
    }
}

@Composable
private fun CardRequestItem(request: CardRequestDTO) {
    val (statusColor, statusLabel, statusIcon) = when (request.status) {
        "PENDING" -> Triple(AppColors.YellowWarning, "Chờ duyệt", Icons.Default.HourglassEmpty)
        "APPROVED" -> Triple(AppColors.BluePrimary, "Đã duyệt", Icons.Default.CheckCircle)
        "COMPLETED" -> Triple(AppColors.GreenSuccess, "Hoàn thành", Icons.Default.CheckCircle)
        "REJECTED" -> Triple(AppColors.RedError, "Từ chối", Icons.Default.CreditCard)
        else -> Triple(AppColors.PrimaryGray, request.status, Icons.Default.CreditCard)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(24.dp))
                Column {
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            statusLabel,
                            color = statusColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (!request.reviewNote.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text("Phản hồi: ${request.reviewNote}", fontSize = 12.sp, color = statusColor)
                    }
                }
            }
            Text(
                request.createdAt.take(10),
                fontSize = 11.sp,
                color = AppColors.PrimaryGray
            )
        }
    }
}
