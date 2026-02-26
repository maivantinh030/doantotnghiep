package com.park.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.park.ui.component.PageHeader
import com.park.ui.component.SnackbarMessage
import com.park.ui.theme.AppColors
import com.park.ui.theme.AppTypography
import com.park.viewmodel.NotificationViewModel

@Composable
fun NotificationScreen(viewModel: NotificationViewModel = viewModel { NotificationViewModel() }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var targetType by remember { mutableStateOf("ALL") }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        if (uiState.successMessage != null || uiState.errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.SurfaceLight)
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left: Send Notification Form
        Column(modifier = Modifier.weight(1f)) {
            PageHeader(title = "Gửi Thông báo", subtitle = "Push notification đến người dùng")

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Soạn thông báo mới", style = AppTypography.titleLarge, color = AppColors.PrimaryDark)
                    Spacer(Modifier.height(16.dp))

                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.WarmOrange,
                        focusedLabelColor = AppColors.WarmOrange,
                        cursorColor = AppColors.WarmOrange
                    )

                    OutlinedTextField(
                        value = title, onValueChange = { title = it },
                        label = { Text("Tiêu đề *") }, singleLine = true,
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), colors = fieldColors
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = message, onValueChange = { message = it },
                        label = { Text("Nội dung *") }, maxLines = 5, minLines = 3,
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), colors = fieldColors
                    )
                    Spacer(Modifier.height(12.dp))

                    Text("Đối tượng nhận", style = AppTypography.bodyMedium, color = AppColors.PrimaryGray)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(
                            "ALL" to "Tất cả",
                            "PLATINUM" to "Platinum",
                            "GOLD" to "Gold",
                            "SILVER" to "Silver",
                            "BRONZE" to "Bronze"
                        ).forEach { (type, label) ->
                            FilterChip(
                                selected = targetType == type,
                                onClick = { targetType = type },
                                label = { Text(label, fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppColors.WarmOrange,
                                    selectedLabelColor = AppColors.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && message.isNotBlank()) {
                                viewModel.sendNotification(title, message, targetType)
                                title = ""
                                message = ""
                            }
                        },
                        enabled = title.isNotBlank() && message.isNotBlank() && !uiState.isSending,
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        if (uiState.isSending) {
                            CircularProgressIndicator(color = AppColors.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Gửi thông báo", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    SnackbarMessage(uiState.successMessage, isError = false)
                    SnackbarMessage(uiState.errorMessage, isError = true)
                }
            }
        }

        // Right: Notification History
        Column(modifier = Modifier.weight(1f)) {
            Text("Lịch sử thông báo", style = AppTypography.headlineLarge, color = AppColors.PrimaryDark, modifier = Modifier.padding(bottom = 16.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.White),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.WarmOrange)
                    }
                } else if (uiState.notifications.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Chưa có thông báo nào", color = AppColors.PrimaryGray)
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.notifications) { notification ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceLight)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            notification.title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = AppColors.PrimaryDark,
                                            modifier = Modifier.weight(1f)
                                        )
                                        // Display target type badge
                                        notification.targetType?.let { type ->
                                            Surface(
                                                color = when(type) {
                                                    "ALL" -> AppColors.BluePrimary
                                                    "PLATINUM" -> AppColors.PrimaryDark
                                                    "GOLD" -> AppColors.WarmOrange
                                                    "SILVER" -> AppColors.PrimaryGray
                                                    "BRONZE" -> AppColors.LightGray
                                                    else -> AppColors.PrimaryGray
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = when(type) {
                                                        "ALL" -> "Tất cả"
                                                        "PLATINUM" -> "Platinum"
                                                        "GOLD" -> "Gold"
                                                        "SILVER" -> "Silver"
                                                        "BRONZE" -> "Bronze"
                                                        else -> type
                                                    },
                                                    color = AppColors.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(notification.message, style = AppTypography.bodyMedium, color = AppColors.PrimaryGray)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        notification.createdAt.take(19).replace("T", " "),
                                        fontSize = 11.sp,
                                        color = AppColors.PrimaryGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
