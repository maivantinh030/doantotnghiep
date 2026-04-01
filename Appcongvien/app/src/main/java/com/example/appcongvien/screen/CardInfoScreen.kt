package com.example.appcongvien.screen

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.data.model.CardDTO
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.components.ParkTopAppBar
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.CardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardInfoScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val cardRepository = (context.applicationContext as App).cardRepository
    val viewModel: CardViewModel = viewModel(
        factory = CardViewModel.Factory(cardRepository)
    )

    var showCardId by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val cardsState by viewModel.cardsState.collectAsState()
    val blockCardState by viewModel.blockCardState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMyCards() }

    LaunchedEffect(blockCardState) {
        if (blockCardState is Resource.Success) {
            viewModel.loadMyCards()
            viewModel.resetBlockCardState()
        }
    }

    Scaffold(topBar = { ParkTopAppBar(title = "Thông Tin Thẻ", onBackClick = onBackClick) }) { paddingValues ->
        when (val state = cardsState) {
            null, is Resource.Loading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.WarmOrange)
                }
            }
            is Resource.Error -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Lỗi: ${state.message}", color = Color.Red, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                }
            }
            is Resource.Success -> {
                val card = state.data.firstOrNull()
                if (card == null) {
                    Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = AppColors.PrimaryGray, modifier = Modifier.size(64.dp))
                            Text("Bạn chưa có thẻ nào", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.PrimaryDark)
                            Text(
                                "Hãy đến quầy của công viên hoặc gửi yêu cầu cấp thẻ qua app để được cấp thẻ Smart Card.",
                                fontSize = 14.sp, color = AppColors.PrimaryGray, textAlign = TextAlign.Center, lineHeight = 20.sp
                            )
                        }
                    }
                } else {
                    CardInfoContent(
                        modifier = modifier,
                        paddingValues = paddingValues,
                        card = card,
                        showCardId = showCardId,
                        onToggleCardId = { showCardId = !showCardId },
                        scrollState = scrollState,
                        onBlockCard = { viewModel.blockCard(card.cardId, "Yêu cầu khóa từ người dùng") },
                        isBlockLoading = blockCardState is Resource.Loading
                    )
                }
            }
        }
    }
}

@Composable
private fun CardInfoContent(
    modifier: Modifier,
    paddingValues: PaddingValues,
    card: CardDTO,
    showCardId: Boolean,
    onToggleCardId: () -> Unit,
    scrollState: ScrollState,
    onBlockCard: () -> Unit,
    isBlockLoading: Boolean
) {
    val isBlocked = card.status == "BLOCKED"
    val statusColor = when (card.status) {
        "ACTIVE" -> Color(0xFF4CAF50)
        "BLOCKED" -> Color(0xFFF44336)
        else -> AppColors.PrimaryGray
    }
    val statusLabel = when (card.status) {
        "ACTIVE" -> "Đang hoạt động"
        "BLOCKED" -> "Đã khóa"
        else -> card.status
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Brush.verticalGradient(listOf(AppColors.SurfaceLight, Color.White)))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card visual
        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.horizontalGradient(listOf(AppColors.CardGrad1, AppColors.CardGrad2)))
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Park Adventure", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            if (!card.cardName.isNullOrBlank()) {
                                Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.25f)) {
                                    Text(card.cardName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                        Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (showCardId) card.cardId else "•••• •••• ••••",
                                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 2.sp
                            )
                            IconButton(onClick = onToggleCardId, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    if (showCardId) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(formatDate(card.issuedAt), fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Status & block action
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Trạng thái thẻ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.PrimaryDark)
                    Surface(shape = RoundedCornerShape(12.dp), color = statusColor.copy(alpha = 0.15f)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
                            Text(statusLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
                        }
                    }
                }

                if (!isBlocked) {
                    Button(
                        onClick = onBlockCard,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isBlockLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336), contentColor = Color.White)
                    ) {
                        if (isBlockLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Báo mất thẻ / Khóa thẻ", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Text("Chỉ nhân viên mới có thể mở khóa thẻ sau khi khóa.", fontSize = 12.sp, color = AppColors.PrimaryGray)
                } else {
                    Text("Thẻ đã bị khóa. Vui lòng đến quầy nhân viên để được hỗ trợ.", fontSize = 13.sp, color = Color(0xFFF44336))
                    if (card.blockedReason != null) {
                        Text("Lý do: ${card.blockedReason}", fontSize = 13.sp, color = AppColors.PrimaryGray)
                    }
                }
            }
        }

        // Card details
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Chi Tiết Thẻ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.PrimaryDark)
                InfoRow(label = "Ma the", value = card.cardId)
                InfoRow(label = "Tên thẻ", value = card.cardName ?: "—")
                InfoRow(label = "Ngày phát hành", value = formatDate(card.issuedAt))
                if (!card.depositAmount.isNullOrBlank() && card.depositAmount != "0") {
                    InfoRow(label = "Tiền cọc", value = "${formatAmount(card.depositAmount)} VND")
                    InfoRow(
                        label = "Trạng thái cọc",
                        value = when (card.depositStatus) {
                            "PAID" -> "Đã nộp"
                            "REFUNDED" -> "Đã hoàn"
                            "FORFEITED" -> "Đã thu"
                            else -> "—"
                        },
                        valueColor = when (card.depositStatus) {
                            "PAID" -> Color(0xFF4CAF50)
                            "FORFEITED" -> Color(0xFFF44336)
                            "REFUNDED" -> AppColors.PrimaryDark
                            else -> AppColors.PrimaryDark
                        }
                    )
                }
                if (card.lastUsedAt != null) {
                    InfoRow(label = "Sử dụng lần cuối", value = formatDate(card.lastUsedAt))
                }
                if (card.blockedAt != null) {
                    InfoRow(label = "Ngày khóa", value = formatDate(card.blockedAt), valueColor = Color(0xFFF44336))
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

private fun formatDate(isoString: String?): String {
    if (isoString == null) return "—"
    return try {
        val parts = isoString.substring(0, 10).split("-")
        "${parts[2]}/${parts[1]}/${parts[0]}"
    } catch (_: Exception) { isoString }
}

private fun formatAmount(amount: String?): String {
    if (amount == null) return "0"
    return try {
        val n = amount.toDoubleOrNull()?.toLong() ?: 0L
        java.text.DecimalFormat("#,###").format(n)
    } catch (_: Exception) { amount }
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color = AppColors.PrimaryDark) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 14.sp, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = valueColor, textAlign = TextAlign.End)
    }
}
