package com.example.appcongvien.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
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
    val messages: List<SupportMessageDTO> = when (val state = messagesState) {
        is Resource.Success -> state.data.items ?: emptyList()
        else -> emptyList()
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(sendState) {
        if (sendState is Resource.Success) {
            viewModel.resetSendState()
        }
    }

    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AppColors.WarmOrange,
        unfocusedBorderColor = AppColors.BorderSubtle,
        focusedContainerColor = AppColors.SurfaceWhite,
        unfocusedContainerColor = AppColors.SurfaceWhite,
        cursorColor = AppColors.WarmOrange,
        focusedPlaceholderColor = AppColors.PrimaryGray.copy(alpha = 0.72f),
        unfocusedPlaceholderColor = AppColors.PrimaryGray.copy(alpha = 0.72f)
    )

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
                            shape = RoundedCornerShape(14.dp),
                            color = AppColors.WarmOrangeSoft.copy(alpha = 0.76f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = AppColors.WarmOrange,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Hỗ trợ khách hàng",
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Park Adventure",
                                fontSize = 12.sp,
                                color = AppColors.PrimaryGray.copy(alpha = 0.8f)
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
                    Brush.verticalGradient(
                        listOf(
                            AppColors.HeaderGrad1.copy(alpha = 0.14f),
                            AppColors.SurfaceLight,
                            AppColors.SurfaceWhite
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (val state = messagesState) {
                    is Resource.Loading -> {
                        CircularProgressIndicator(
                            color = AppColors.WarmOrange,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is Resource.Error -> {
                        ChatStateCard(
                            title = "Không thể tải cuộc trò chuyện",
                            message = state.message,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 16.dp)
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            state = listState
                        ) {
                            item { WelcomeMessage() }
                            items(messages) { message ->
                                SupportBubble(message = message)
                            }
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                    }
                }
            }

            if (sendState is Resource.Error) {
                Text(
                    text = (sendState as Resource.Error).message,
                    color = Color(0xFFC43D2F),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AppColors.SurfaceWhite,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.72f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                text = "Nhập câu hỏi của bạn...",
                                color = AppColors.PrimaryGray
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        minLines = 1,
                        maxLines = 4,
                        colors = inputColors
                    )
                    FloatingActionButton(
                        onClick = {
                            val text = messageText.trim()
                            if (text.isNotBlank()) {
                                viewModel.sendMessage(text)
                                messageText = ""
                            }
                        },
                        modifier = Modifier.size(50.dp),
                        containerColor = if (sendState is Resource.Loading) {
                            AppColors.WarmOrange.copy(alpha = 0.55f)
                        } else {
                            AppColors.WarmOrange
                        },
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
                                imageVector = Icons.Default.Send,
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
                color = AppColors.WarmOrangeSoft.copy(alpha = 0.76f),
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = null,
                    tint = AppColors.WarmOrange,
                    modifier = Modifier.padding(7.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            modifier = Modifier.widthIn(max = 296.dp),
            shape = RoundedCornerShape(
                topStart = if (isAdmin) 8.dp else 20.dp,
                topEnd = if (isAdmin) 20.dp else 8.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isAdmin) AppColors.SurfaceWhite else AppColors.WarmOrange
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = if (isAdmin) {
                BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.72f))
            } else {
                null
            }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = if (isAdmin) AppColors.PrimaryDark else Color.White
                )
                Text(
                    text = message.createdAt.take(16).replace("T", " "),
                    fontSize = 10.sp,
                    color = if (isAdmin) AppColors.PrimaryGray.copy(alpha = 0.78f) else Color.White.copy(alpha = 0.72f)
                )
            }
        }

        if (!isAdmin) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = AppColors.WarmOrangeSoft.copy(alpha = 0.76f),
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = AppColors.WarmOrange,
                    modifier = Modifier.padding(7.dp)
                )
            }
        }
    }
}

@Composable
private fun WelcomeMessage() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppColors.WarmOrangeSoft.copy(alpha = 0.78f),
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = null,
                    tint = AppColors.WarmOrange,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Text(
                text = "Hỗ trợ khách hàng",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryDark
            )
            Text(
                text = "Chúng tôi luôn sẵn sàng hỗ trợ bạn với các thắc mắc về tài khoản, thanh toán và trải nghiệm tại công viên.",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = AppColors.PrimaryGray.copy(alpha = 0.84f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ChatStateCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = AppColors.SurfaceWhite,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppColors.WarmOrangeSoft.copy(alpha = 0.7f),
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = null,
                    tint = AppColors.WarmOrange,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryDark,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = AppColors.PrimaryGray.copy(alpha = 0.84f),
                textAlign = TextAlign.Center
            )
        }
    }
}
