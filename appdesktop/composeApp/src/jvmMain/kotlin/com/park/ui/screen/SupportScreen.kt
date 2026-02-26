package com.park.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.park.data.model.SupportMessageDTO
import com.park.ui.component.PageHeader
import com.park.ui.component.SnackbarMessage
import com.park.ui.theme.AppColors
import com.park.ui.theme.AppTypography
import com.park.viewmodel.SupportViewModel

@Composable
fun SupportScreen(viewModel: SupportViewModel = viewModel { SupportViewModel() }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        if (uiState.successMessage != null || uiState.errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    // Group messages by userId
    val groupedByUser = uiState.messages.groupBy { it.userId }
    val uniqueUsers = groupedByUser.keys.toList()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.SurfaceLight)
    ) {
        // Left: Users list
        Column(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
                .background(AppColors.White)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hỗ trợ Khách hàng", style = AppTypography.titleLarge, color = AppColors.PrimaryDark, modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.loadMessages() }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = AppColors.WarmOrange, modifier = Modifier.size(18.dp))
                }
            }
            Divider(color = AppColors.LightGray)
            Spacer(Modifier.height(8.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(color = AppColors.WarmOrange, modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
            } else if (uniqueUsers.isEmpty()) {
                Text("Không có yêu cầu hỗ trợ", color = AppColors.PrimaryGray, fontSize = 13.sp)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(uniqueUsers) { userId ->
                        val msgs = groupedByUser[userId] ?: emptyList()
                        val lastMsg = msgs.lastOrNull()
                        val displayName = lastMsg?.userName ?: userId.take(8) + "..."
                        val isSelected = uiState.selectedUserId == userId

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AppColors.OrangeLight else Color.Transparent)
                                .clickable { viewModel.selectUser(userId) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(36.dp).clip(CircleShape).background(AppColors.WarmOrange.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(displayName.take(1).uppercase(), color = AppColors.WarmOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(displayName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.PrimaryDark)
                                Text(
                                    lastMsg?.content?.take(30) ?: "",
                                    fontSize = 11.sp, color = AppColors.PrimaryGray,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Right: Chat area
        Column(modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp)) {
            if (uiState.selectedUserId == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chọn một cuộc hội thoại để xem và trả lời", color = AppColors.PrimaryGray)
                }
            } else {
                val msgs = groupedByUser[uiState.selectedUserId] ?: emptyList()

                // Messages
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    reverseLayout = false,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(msgs) { msg ->
                        ChatBubble(msg)
                    }
                }

                Spacer(Modifier.height(8.dp))
                SnackbarMessage(uiState.successMessage, isError = false)
                SnackbarMessage(uiState.errorMessage, isError = true)

                // Reply input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.replyContent,
                        onValueChange = { viewModel.setReplyContent(it) },
                        placeholder = { Text("Nhập phản hồi...") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.WarmOrange,
                            cursorColor = AppColors.WarmOrange
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = { viewModel.sendReply() },
                        containerColor = AppColors.WarmOrange,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Gửi", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: SupportMessageDTO) {
    val isAdmin = message.isFromAdmin
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isAdmin) Arrangement.End else Arrangement.Start
    ) {
        if (!isAdmin) {
            Box(
                Modifier.size(28.dp).clip(CircleShape).background(AppColors.PrimaryGray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("U", color = AppColors.PrimaryGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(6.dp))
        }
        Column(horizontalAlignment = if (isAdmin) Alignment.End else Alignment.Start) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(
                        topStart = if (isAdmin) 12.dp else 2.dp,
                        topEnd = if (isAdmin) 2.dp else 12.dp,
                        bottomStart = 12.dp, bottomEnd = 12.dp
                    ))
                    .background(if (isAdmin) AppColors.WarmOrange else AppColors.LightGray)
                    .padding(12.dp, 8.dp)
            ) {
                Text(message.content, color = if (isAdmin) Color.White else AppColors.PrimaryDark, fontSize = 14.sp)
            }
            Text(
                message.createdAt?.take(16) ?: "",
                fontSize = 10.sp, color = AppColors.PrimaryGray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        if (isAdmin) {
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier.size(28.dp).clip(CircleShape).background(AppColors.WarmOrange.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("A", color = AppColors.WarmOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
