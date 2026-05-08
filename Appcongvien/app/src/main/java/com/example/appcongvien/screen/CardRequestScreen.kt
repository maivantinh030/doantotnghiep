package com.example.appcongvien.screen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.components.ParkTopAppBar
import com.example.appcongvien.data.model.CardRequestDTO
import com.example.appcongvien.data.repository.CardRequestRepository
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.CardRequestViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardRequestScreen(
    repository: CardRequestRepository,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as App
    val viewModel: CardRequestViewModel = viewModel(
        factory = CardRequestViewModel.Factory(repository, app.cardRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showExistingCardDialog by remember { mutableStateOf(false) }
    var continueCreateAfterBlock by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        if (uiState.successMessage != null || uiState.errorMessage != null) {
            delay(3000)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.isBlockingCard, uiState.activeCard) {
        if (continueCreateAfterBlock && !uiState.isBlockingCard) {
            if (uiState.activeCard == null) {
                showConfirmDialog = true
            }
            continueCreateAfterBlock = false
        }
    }

    if (showExistingCardDialog) {
        AlertDialog(
            onDismissRequest = { if (!uiState.isBlockingCard) showExistingCardDialog = false },
            title = { Text("Bạn đang có sẵn thẻ", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Bạn đang có sẵn thẻ, không thể tạo thêm.",
                        fontSize = 14.sp,
                        color = AppColors.PrimaryDark
                    )
                    uiState.activeCard?.let { activeCard ->
                        Text(
                            "Thẻ hiện tại: ${activeCard.cardId}",
                            fontSize = 13.sp,
                            color = AppColors.PrimaryGray
                        )
                    }
                    Text(
                        "Nếu bạn đã làm mất thẻ, hãy khóa thẻ cũ để tiếp tục tạo thẻ mới.",
                        fontSize = 13.sp,
                        color = AppColors.PrimaryGray,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        continueCreateAfterBlock = true
                        showExistingCardDialog = false
                        viewModel.blockActiveCard()
                    },
                    enabled = !uiState.isBlockingCard,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.RedError),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (uiState.isBlockingCard) {
                        CircularProgressIndicator(
                            color = AppColors.OnAccent,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Làm mất thẻ", fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExistingCardDialog = false },
                    enabled = !uiState.isBlockingCard
                ) {
                    Text("Đóng")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Xác nhận yêu cầu cấp thẻ", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Sau khi gửi, nhân viên sẽ xem xét và duyệt yêu cầu. Vui lòng đến quầy để nhận thẻ vật lý khi được duyệt.",
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
        topBar = { ParkTopAppBar(title = "Yêu Cầu Cấp Thẻ", onBackClick = onBackClick) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppColors.SurfaceLight),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = AppColors.BluePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                "Cách hoạt động",
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "1. Gửi yêu cầu qua app\n2. Nhân viên xem xét và duyệt\n3. Đến quầy để nhận thẻ vật lý\n4. Thẻ sẽ được liên kết với tài khoản của bạn",
                                fontSize = 13.sp,
                                color = AppColors.PrimaryGray,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // Card hiển thị thẻ đang hoạt động (tách riêng)
            uiState.activeCard?.let {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = AppColors.GreenSuccess.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = AppColors.GreenSuccess,
                                        modifier = Modifier
                                            .padding(6.dp)
                                            .size(20.dp)
                                    )
                                }
                                Text(
                                    "Thẻ đang hoạt động",
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.PrimaryDark,
                                    fontSize = 15.sp
                                )
                            }

                            Surface(
                                color = AppColors.SurfaceLight,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    "Thẻ đã được cấp",
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.GreenSuccess,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                )
                            }

                            Text(
                                "Không thể tạo thêm thẻ mới khi thẻ này vẫn hoạt động.",
                                fontSize = 12.sp,
                                color = AppColors.PrimaryGray,
                                lineHeight = 18.sp
                            )

                            Button(
                                onClick = { showExistingCardDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.RedError.copy(alpha = 0.1f),
                                    contentColor = AppColors.RedError
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 10.dp
                                )
                            ) {
                                Text(
                                    "Báo mất thẻ",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Card gửi yêu cầu mới
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Gửi yêu cầu mới",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = AppColors.PrimaryDark
                        )

                        uiState.successMessage?.let {
                            Text(it, color = AppColors.GreenSuccess, fontSize = 13.sp)
                        }
                        uiState.errorMessage?.let {
                            Text(it, color = AppColors.RedError, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                if (uiState.activeCard != null) {
                                    showExistingCardDialog = true
                                } else {
                                    showConfirmDialog = true
                                }
                            },
                            enabled = !uiState.isSending && !uiState.isCheckingCards && !uiState.isBlockingCard,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange)
                        ) {
                            when {
                                uiState.isSending -> {
                                    CircularProgressIndicator(
                                        color = AppColors.OnAccent,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                }

                                uiState.isCheckingCards -> {
                                    CircularProgressIndicator(
                                        color = AppColors.OnAccent,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                }

                                else -> {
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Yêu cầu cấp thẻ", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.requests.isNotEmpty()) {
                item {
                    Text(
                        "Yêu cầu của tôi (${uiState.requests.size})",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = AppColors.PrimaryDark
                    )
                }
                items(uiState.requests) { req ->
                    CardRequestItem(req)
                }
            } else if (!uiState.isLoading) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
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
        "APPROVED" -> Triple(AppColors.GreenSuccess, "Hoàn thành", Icons.Default.CheckCircle)
        "COMPLETED" -> Triple(AppColors.GreenSuccess, "Hoàn thành", Icons.Default.CheckCircle)
        "REJECTED" -> Triple(AppColors.RedError, "Từ chối", Icons.Default.CreditCard)
        else -> Triple(AppColors.PrimaryGray, request.status, Icons.Default.CreditCard)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
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
