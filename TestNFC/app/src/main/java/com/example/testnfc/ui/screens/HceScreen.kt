package com.example.testnfc.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testnfc.R
import com.example.testnfc.nfc.CardEmulatorService
import com.example.testnfc.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HceScreen(onNavigateBack: () -> Unit) {
    // Quan sát state từ companion object của CardEmulatorService
    val isActive by CardEmulatorService.isActive.collectAsState()
    val logs by CardEmulatorService.logs.collectAsState()
    val listState = rememberLazyListState()

    // Tự động cuộn xuống log mới nhất
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.hce_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.WarmOrange,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = AppColors.SurfaceLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Card trạng thái kết nối ──────────────────────────────────
            StatusCard(isActive = isActive)

            // ── Card thông tin AID và lệnh hỗ trợ ───────────────────────
            InfoCard()

            // ── Console log ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.hce_log_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
                IconButton(onClick = { CardEmulatorService.clearLogs() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.hce_clear_logs),
                        tint = AppColors.RedError
                    )
                }
            }

            LogConsole(
                logs = logs,
                listState = listState,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatusCard(isActive: Boolean) {
    val statusText = if (isActive)
        stringResource(R.string.hce_status_active)
    else
        stringResource(R.string.hce_status_waiting)

    val statusColor = if (isActive) AppColors.GreenSuccess else Color(0xFFFF9800)
    val bgColor = if (isActive)
        AppColors.WarmOrangeSoft
    else
        Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (isActive) AppColors.WarmOrange.copy(alpha = 0.15f)
                        else Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isActive) AppColors.WarmOrange else AppColors.PrimaryGray
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Trạng thái HCE",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.PrimaryGray
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
            }
            // Đèn chỉ báo trạng thái
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
        }
    }
}

@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF1565C0)
                )
                Text(
                    text = stringResource(R.string.hce_info_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(
                label = stringResource(R.string.hce_aid_label),
                value = stringResource(R.string.hce_aid_value)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.hce_commands_label),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryDark
            )
            listOf(
                stringResource(R.string.hce_cmd_select),
                stringResource(R.string.hce_cmd_get_message),
                stringResource(R.string.hce_cmd_get_counter),
                stringResource(R.string.hce_cmd_echo)
            ).forEach { cmd ->
                Text(
                    text = cmd,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = AppColors.PrimaryGray
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryDark
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = AppColors.PrimaryGray
        )
    }
}

/**
 * Console log màu tối với màu sắc phân loại theo nội dung log
 */
@Composable
fun LogConsole(
    logs: List<String>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
            .padding(8.dp)
    ) {
        if (logs.isEmpty()) {
            Text(
                text = "Chưa có nhật ký...",
                color = Color(0xFF888888),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(state = listState) {
                items(logs) { log ->
                    Text(
                        text = log,
                        color = logColor(log),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}

/** Chọn màu hiển thị dựa vào emoji ký hiệu trong log */
private fun logColor(log: String): Color = when {
    log.contains("✅") -> Color(0xFF81C784)   // Xanh lá — thành công
    log.contains("❌") -> Color(0xFFE57373)   // Đỏ — lỗi
    log.contains("📨") -> Color(0xFF64B5F6)   // Xanh dương — nhận lệnh
    log.contains("📥") -> Color(0xFF64B5F6)   // Xanh dương — nhận dữ liệu
    log.contains("📤") -> Color(0xFFFFD54F)   // Vàng — gửi dữ liệu
    log.contains("🎉") -> Color(0xFFBA68C8)   // Tím — hoàn thành
    log.contains("🔌") -> Color(0xFFFF8A65)   // Cam — ngắt kết nối
    log.contains("🔍") -> Color(0xFF4FC3F7)   // Xanh nhạt — đang quét
    else -> Color(0xFFCCCCCC)                 // Xám — mặc định
}
