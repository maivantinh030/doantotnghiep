package com.park.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.park.data.model.CardDTO
import com.park.data.model.CardRequestDTO
import com.park.ui.component.PageHeader
import com.park.ui.theme.AppColors
import com.park.viewmodel.CardManagementViewModel

@Composable
fun CardManagementScreen(viewModel: CardManagementViewModel = CardManagementViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Tabs: 0 = Thẻ AVAILABLE, 1 = Yêu cầu cấp thẻ
    var selectedTab by remember { mutableStateOf(0) }

    // Auto-clear message
    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.SurfaceLight)
            .padding(24.dp)
    ) {
        PageHeader(
            title = "Quản lý Thẻ",
            subtitle = "${uiState.availableCards.size} thẻ sẵn sàng · ${uiState.cardRequests.size} yêu cầu ${uiState.requestStatusFilter}"
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.openRegisterDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Đăng ký thẻ mới")
                }
                OutlinedButton(
                    onClick = { viewModel.loadData() },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Message banner
        uiState.message?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isError) AppColors.RedError.copy(alpha = 0.1f)
                    else Color(0xFF4CAF50).copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    msg,
                    modifier = Modifier.padding(12.dp),
                    color = if (uiState.isError) AppColors.RedError else Color(0xFF2E7D32),
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = AppColors.White,
            contentColor = AppColors.WarmOrange,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Thẻ AVAILABLE", modifier = Modifier.padding(vertical = 12.dp), fontSize = 14.sp)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1; viewModel.loadCardRequests() }) {
                Text("Yêu cầu cấp thẻ", modifier = Modifier.padding(vertical = 12.dp), fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.WarmOrange)
            }
        } else when (selectedTab) {
            0 -> AvailableCardsTab(
                cards = uiState.availableCards,
                onIssue = { viewModel.openIssueDialog(it) },
                onBlock = { viewModel.openBlockDialog(it) }
            )
            1 -> CardRequestsTab(
                requests = uiState.cardRequests,
                currentFilter = uiState.requestStatusFilter,
                onFilterChange = { viewModel.loadCardRequests(it) },
                onReview = { viewModel.openReviewDialog(it) },
                onComplete = { viewModel.completeCardRequest(it.requestId) }
            )
        }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────

    if (uiState.showRegisterDialog) {
        RegisterCardDialog(
            onConfirm = { cardId, name -> viewModel.registerCard(cardId, name) },
            onDismiss = { viewModel.closeRegisterDialog() }
        )
    }

    if (uiState.showIssueDialog && uiState.selectedCard != null) {
        IssueCardDialog(
            card = uiState.selectedCard!!,
            onConfirm = { userId, name, deposit -> viewModel.issueCard(uiState.selectedCard!!.cardId, userId, name, deposit) },
            onDismiss = { viewModel.closeIssueDialog() }
        )
    }

    if (uiState.showReturnDialog && uiState.selectedCard != null) {
        ConfirmDialog(
            title = "Xác nhận trả thẻ",
            message = "Thẻ ${uiState.selectedCard!!.cardId} sẽ được unlink khỏi tài khoản. Balance và tiền cọc sẽ được hoàn lại. Tiếp tục?",
            onConfirm = { viewModel.returnCard(uiState.selectedCard!!.cardId) },
            onDismiss = { viewModel.closeReturnDialog() }
        )
    }

    if (uiState.showBlockDialog && uiState.selectedCard != null) {
        BlockCardDialog(
            card = uiState.selectedCard!!,
            onConfirm = { reason -> viewModel.blockCard(uiState.selectedCard!!.cardId, reason) },
            onDismiss = { viewModel.closeBlockDialog() }
        )
    }

    if (uiState.showReviewDialog && uiState.selectedRequest != null) {
        ReviewRequestDialog(
            request = uiState.selectedRequest!!,
            onConfirm = { approved, note -> viewModel.reviewCardRequest(uiState.selectedRequest!!.requestId, approved, note) },
            onDismiss = { viewModel.closeReviewDialog() }
        )
    }
}

// ── Tabs ──────────────────────────────────────────────────────────────────

@Composable
private fun AvailableCardsTab(
    cards: List<CardDTO>,
    onIssue: (CardDTO) -> Unit,
    onBlock: (CardDTO) -> Unit
) {
    if (cards.isEmpty()) {
        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
            Text("Không có thẻ nào ở trạng thái AVAILABLE", color = AppColors.PrimaryGray)
        }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(cards) { card ->
            CardRow(card = card, onIssue = onIssue, onReturn = null, onBlock = onBlock)
        }
    }
}

@Composable
private fun CardRequestsTab(
    requests: List<CardRequestDTO>,
    currentFilter: String,
    onFilterChange: (String) -> Unit,
    onReview: (CardRequestDTO) -> Unit,
    onComplete: (CardRequestDTO) -> Unit
) {
    Column {
        // Filter chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("PENDING", "APPROVED", "REJECTED", "COMPLETED").forEach { status ->
                FilterChip(
                    selected = currentFilter == status,
                    onClick = { onFilterChange(status) },
                    label = { Text(status, fontSize = 12.sp) }
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        if (requests.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("Không có yêu cầu nào", color = AppColors.PrimaryGray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(requests) { req ->
                    CardRequestRow(
                        request = req,
                        onReview = onReview,
                        onComplete = onComplete
                    )
                }
            }
        }
    }
}

// ── Row components ────────────────────────────────────────────────────────

@Composable
private fun CardRow(
    card: CardDTO,
    onIssue: ((CardDTO) -> Unit)?,
    onReturn: ((CardDTO) -> Unit)?,
    onBlock: ((CardDTO) -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                    .background(AppColors.WarmOrange.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CreditCard, contentDescription = null, tint = AppColors.WarmOrange, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(card.cardId, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("${card.cardName ?: "Chưa đặt tên"} · ${card.status}", fontSize = 12.sp, color = AppColors.PrimaryGray)
                if (card.depositAmount != "0" && card.depositAmount != "0.00") {
                    Text("Cọc: ${card.depositAmount} · ${card.depositStatus}", fontSize = 11.sp, color = AppColors.WarmOrange)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                onIssue?.let {
                    OutlinedButton(
                        onClick = { it(card) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Phát hành", fontSize = 12.sp)
                    }
                }
                onReturn?.let {
                    OutlinedButton(
                        onClick = { it(card) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Trả thẻ", fontSize = 12.sp)
                    }
                }
                onBlock?.let {
                    OutlinedButton(
                        onClick = { it(card) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.RedError),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Khóa", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CardRequestRow(
    request: CardRequestDTO,
    onReview: (CardRequestDTO) -> Unit,
    onComplete: (CardRequestDTO) -> Unit
) {
    val statusColor = when (request.status) {
        "PENDING" -> Color(0xFFF59E0B)
        "APPROVED" -> Color(0xFF10B981)
        "REJECTED" -> AppColors.RedError
        "COMPLETED" -> AppColors.PrimaryGray
        else -> AppColors.PrimaryGray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("User: ${request.userId.take(8)}...", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Surface(shape = RoundedCornerShape(4.dp), color = statusColor.copy(alpha = 0.15f)) {
                        Text(request.status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Bold)
                    }
                }
                Text("Cọc: ${request.depositAmount} · ${if (request.depositPaidOnline) "Online" else "Tại quầy"}", fontSize = 12.sp, color = AppColors.PrimaryGray)
                request.note?.let { Text("Ghi chú: $it", fontSize = 11.sp, color = AppColors.PrimaryGray) }
                request.createdAt?.let { Text(it.take(10), fontSize = 11.sp, color = AppColors.PrimaryGray) }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (request.status == "PENDING") {
                    Button(
                        onClick = { onReview(request) },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Xem xét", fontSize = 12.sp)
                    }
                }
                if (request.status == "APPROVED") {
                    OutlinedButton(
                        onClick = { onComplete(request) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Hoàn thành", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ── Dialogs ───────────────────────────────────────────────────────────────

@Composable
private fun RegisterCardDialog(onConfirm: (String, String?) -> Unit, onDismiss: () -> Unit) {
    var cardId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp).width(360.dp)) {
                Text("Đăng ký thẻ mới", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = cardId, onValueChange = { cardId = it }, label = { Text("Card ID thẻ (cardId) *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên thẻ (tùy chọn)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Hủy") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onConfirm(cardId, name.takeIf { it.isNotBlank() }) }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange)) {
                        Text("Đăng ký")
                    }
                }
            }
        }
    }
}

@Composable
private fun IssueCardDialog(card: CardDTO, onConfirm: (String, String?, String) -> Unit, onDismiss: () -> Unit) {
    var userId by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    var deposit by remember { mutableStateOf("0") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp).width(380.dp)) {
                Text("Phát hành thẻ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Card ID: ${card.cardId}", fontSize = 13.sp, color = AppColors.PrimaryGray)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = userId, onValueChange = { userId = it }, label = { Text("User ID *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = cardName, onValueChange = { cardName = it }, label = { Text("Tên thẻ (tùy chọn)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = deposit, onValueChange = { deposit = it }, label = { Text("Tiền cọc (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Hủy") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onConfirm(userId, cardName.takeIf { it.isNotBlank() }, deposit) }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange)) {
                        Text("Phát hành")
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockCardDialog(card: CardDTO, onConfirm: (String?) -> Unit, onDismiss: () -> Unit) {
    var reason by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp).width(360.dp)) {
                Text("Khóa thẻ", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.RedError)
                Text("Card ID: ${card.cardId}", fontSize = 13.sp, color = AppColors.PrimaryGray)
                Spacer(Modifier.height(8.dp))
                Text("⚠ Thẻ sẽ bị khóa vĩnh viễn. Tiền cọc sẽ bị tịch thu nếu thẻ đang có liên kết.", fontSize = 13.sp, color = AppColors.RedError.copy(alpha = 0.8f))
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Lý do khóa (tùy chọn)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Hủy") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onConfirm(reason.takeIf { it.isNotBlank() }) }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.RedError)) {
                        Text("Khóa thẻ")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewRequestDialog(request: CardRequestDTO, onConfirm: (Boolean, String?) -> Unit, onDismiss: () -> Unit) {
    var note by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp).width(380.dp)) {
                Text("Xem xét yêu cầu cấp thẻ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))
                Text("User ID: ${request.userId}", fontSize = 13.sp)
                Text("Tiền cọc: ${request.depositAmount} · ${if (request.depositPaidOnline) "Đã thanh toán online" else "Thanh toán tại quầy"}", fontSize = 13.sp)
                request.note?.let { Text("Ghi chú: $it", fontSize = 13.sp, color = AppColors.PrimaryGray) }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Ghi chú phản hồi (tùy chọn)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                    TextButton(onClick = onDismiss) { Text("Hủy") }
                    OutlinedButton(onClick = { onConfirm(false, note.takeIf { it.isNotBlank() }) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.RedError)) {
                        Text("Từ chối")
                    }
                    Button(onClick = { onConfirm(true, note.takeIf { it.isNotBlank() }) }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange)) {
                        Text("Duyệt")
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmDialog(title: String, message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp).width(360.dp)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))
                Text(message, fontSize = 14.sp)
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Hủy") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange)) {
                        Text("Xác nhận")
                    }
                }
            }
        }
    }
}

