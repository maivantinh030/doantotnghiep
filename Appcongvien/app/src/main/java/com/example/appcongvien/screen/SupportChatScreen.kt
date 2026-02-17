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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcongvien.ui.theme.AppColors
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: String,
    val senderName: String = "",
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageStatus {
    SENDING, SENT, DELIVERED, READ
}

data class SupportAgent(
    val name: String,
    val isOnline: Boolean,
    val responseTime: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportChatScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Support agent info
    val supportAgent = remember {
        SupportAgent(
            name = "Minh - CSKH",
            isOnline = true,
            responseTime = "Thường phản hồi trong vài phút"
        )
    }

    // Mock messages
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "1",
                text = "Xin chào! Tôi là ${supportAgent.name}, rất vui được hỗ trợ bạn hôm nay. Bạn cần hỗ trợ gì về Park Adventure?",
                isFromUser = false,
                timestamp = "10:00",
                senderName = supportAgent.name
            ),
            ChatMessage(
                id = "2",
                text = "Chào bạn! Tôi muốn hỏi về cách nạp tiền vào thẻ thành viên",
                isFromUser = true,
                timestamp = "10:01",
                senderName = "Bạn"
            ),
            ChatMessage(
                id = "3",
                text = "Bạn có thể nạp tiền bằng nhiều cách:\n\n• Qua ứng dụng MoMo\n• Tại quầy lễ tân\n• Máy nạp tiền tự động\n\nBạn muốn hướng dẫn chi tiết cách nào nhất?",
                isFromUser = false,
                timestamp = "10:02",
                senderName = supportAgent.name
            )
        )
    }

    // Auto scroll to bottom when new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AppColors.WarmOrangeSoft,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = AppColors.WarmOrange,
                                modifier = Modifier.padding(6.dp)
                            )
                        }

                        Column {
                            Text(
                                text = supportAgent.name,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (supportAgent.isOnline) Color(0xFF4CAF50) else AppColors.PrimaryGray,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                                Text(
                                    text = if (supportAgent.isOnline) "Đang online" else "Offline",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.WarmOrange
                )
            )
        }
    ) { paddingValues ->

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
        ) {

            // Support info banner
            if (supportAgent.isOnline) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.SupportAgent,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = supportAgent.responseTime,
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Messages list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                reverseLayout = true,
                state = listState
            ) {
                items(messages.reversed()) { message ->
                    ChatBubble(message = message)
                }

                // Welcome message
                item {
                    WelcomeMessage()
                }
            }

            // Input area
            ChatInputArea(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendMessage = {
                    if (messageText.isNotBlank()) {
                        val newMessage = ChatMessage(
                            id = System.currentTimeMillis().toString(),
                            text = messageText,
                            isFromUser = true,
                            timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                            senderName = "Bạn"
                        )
                        messages.add(0, newMessage)
                        messageText = ""

                        // Simulate agent response after delay
                        // In real app, this would be handled by backend/websocket
                    }
                }
            )
        }
    }
}

@Composable
fun WelcomeMessage() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.WarmOrange.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = AppColors.WarmOrange,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.SupportAgent,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Text(
                text = "Hỗ Trợ Khách Hàng",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryDark
            )

            Text(
                text = "Chúng tôi luôn sẵn sàng hỗ trợ bạn với mọi thắc mắc về Park Adventure!",
                fontSize = 12.sp,
                color = AppColors.PrimaryGray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {

        if (!message.isFromUser) {
            // Agent avatar
            Surface(
                shape = CircleShape,
                color = AppColors.WarmOrangeSoft,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.SupportAgent,
                    contentDescription = null,
                    tint = AppColors.WarmOrange,
                    modifier = Modifier.padding(6.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = if (message.isFromUser) 20.dp else 4.dp,
                topEnd = if (message.isFromUser) 4.dp else 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) AppColors.WarmOrange else Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (message.isFromUser) 4.dp else 2.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = if (message.isFromUser) Color.White else AppColors.PrimaryDark
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.timestamp,
                        fontSize = 10.sp,
                        color = if (message.isFromUser)
                            Color.White.copy(alpha = 0.7f)
                        else
                            AppColors.PrimaryGray
                    )

                    if (message.isFromUser) {
                        MessageStatusIndicator(status = message.status)
                    }
                }
            }
        }

        if (message.isFromUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User avatar
            Surface(
                shape = CircleShape,
                color = AppColors.WarmOrangeSoft,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = AppColors.WarmOrange,
                    modifier = Modifier.padding(6.dp)
                )
            }
        }
    }
}

@Composable
fun MessageStatusIndicator(status: MessageStatus) {
    when (status) {
        MessageStatus.SENDING -> {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(12.dp)
            ) {}
        }
        MessageStatus.SENT -> {
            Text(
                text = "✓",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        MessageStatus.DELIVERED -> {
            Text(
                text = "✓✓",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        MessageStatus.READ -> {
            Text(
                text = "✓✓",
                fontSize = 10.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun ChatInputArea(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {

            // Attachment button
            Surface(
                onClick = { /* Handle attachment */ },
                shape = CircleShape,
                color = AppColors.SurfaceLight,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.AttachFile,
                    contentDescription = "Đính kèm",
                    tint = AppColors.PrimaryGray,
                    modifier = Modifier.padding(12.dp)
                )
            }

            // Message input
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "Nhập câu hỏi của bạn...",
                        color = AppColors.PrimaryGray
                    )
                },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.WarmOrange,
                    cursorColor = AppColors.WarmOrange,
                    focusedPlaceholderColor = AppColors.PrimaryGray
                )
            )

            // Send button
            FloatingActionButton(
                onClick = onSendMessage,
                modifier = Modifier.size(48.dp),
                containerColor = AppColors.WarmOrange,
                contentColor = Color.White
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Gửi tin nhắn",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SupportChatScreenPreview() {
    SupportChatScreen()
}


