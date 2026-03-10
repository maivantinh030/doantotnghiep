package com.example.appcongvien.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.components.ParkTopAppBar
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.data.model.SupportMessageDTO
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.SupportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportChatScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as App
    val viewModel: SupportViewModel = viewModel(
        factory = SupportViewModel.Factory(
            repository = app.supportRepository,
            wsClient = app.supportWebSocketClient,
            tokenManager = app.tokenManager
        )
    )

    val messagesState by viewModel.messagesState.collectAsStateWithLifecycle()
    val sendState by viewModel.sendState.collectAsStateWithLifecycle()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    @Suppress("USELESS_ELVIS")
    val messages: List<SupportMessageDTO> = when (val s = messagesState) {
        is Resource.Success -> s.data.items ?: emptyList()
        else -> emptyList()
    }

    // Cuộn xuống cuối khi có tin nhắn mới
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Reset trạng thái gửi
    LaunchedEffect(sendState) {
        if (sendState is Resource.Success) {
            viewModel.resetSendState()
        }
    }

    Scaffold(
        topBar = {
            ParkTopAppBar(
                onBackClick = onBackClick,
                titleContent = {
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
                                text = "Hỗ Trợ Khách Hàng",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Park Adventure",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(listOf(AppColors.SurfaceLight, Color.White))
                )
        ) {
            // Messages list
            Box(modifier = Modifier.weight(1f)) {
                when (val s = messagesState) {
                    is Resource.Loading -> {
                        CircularProgressIndicator(
                            color = AppColors.WarmOrange,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is Resource.Error -> {
                        Text(
                            text = s.message,
                            color = Color.Red,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            state = listState
                        ) {
                            item { WelcomeMessage() }
                            items(messages) { msg ->
                                SupportBubble(message = msg)
                            }
                        }
                    }
                }
            }

            // Error khi gửi
            if (sendState is Resource.Error) {
                Text(
                    text = (sendState as Resource.Error).message,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Input area
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text("Nhập câu hỏi của bạn...", color = AppColors.PrimaryGray)
                        },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.WarmOrange,
                            cursorColor = AppColors.WarmOrange
                        )
                    )
                    FloatingActionButton(
                        onClick = {
                            val text = messageText.trim()
                            if (text.isNotBlank()) {
                                viewModel.sendMessage(text)
                                messageText = ""
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = if (sendState is Resource.Loading)
                            AppColors.WarmOrange.copy(alpha = 0.5f)
                        else AppColors.WarmOrange,
                        contentColor = Color.White
                    ) {
                        if (sendState is Resource.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Gửi tin nhắn",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportBubble(message: SupportMessageDTO) {
    val isAdmin = message.isFromAdmin
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isAdmin) Arrangement.Start else Arrangement.End
    ) {
        if (isAdmin) {
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
                topStart = if (isAdmin) 4.dp else 20.dp,
                topEnd = if (isAdmin) 20.dp else 4.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isAdmin) Color.White else AppColors.WarmOrange
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = if (isAdmin) AppColors.PrimaryDark else Color.White
                )
                Text(
                    text = message.createdAt.take(16).replace("T", " "),
                    fontSize = 10.sp,
                    color = if (isAdmin) AppColors.PrimaryGray else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        if (!isAdmin) {
            Spacer(modifier = Modifier.width(8.dp))
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
