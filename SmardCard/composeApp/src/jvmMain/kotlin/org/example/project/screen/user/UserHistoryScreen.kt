package org.example.project.screen.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.SmartCardManager
import org.example.project.model.TransactionDto
import org.example.project.network.TransactionApiClient
import org.example.project.screen.FloatingBubbles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHistoryScreen(
    smartCardManager: SmartCardManager,
    onBack: () -> Unit
) {
    val client = remember { TransactionApiClient() }
    var customerId by remember { mutableStateOf("") }
    var transactions by remember { mutableStateOf<List<TransactionDto>>(emptyList()) }
    var status by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val info = smartCardManager.readCustomerInfo()
            val id = info["customerID"] ?: ""
            if (id.isNotBlank()) {
                customerId = id
                // Tự động tải lịch sử
                isLoading = true
                status = "Đang tải lịch sử..."
                val result = client.history(id.trim())
                result.onSuccess {
                    transactions = it
                    status = if (it.isEmpty()) "📭 Chưa có giao dịch" else "✅ Đã tải ${it.size} giao dịch"
                }.onFailure { e ->
                    status = "❌ Lỗi: ${e.message}"
                }
                isLoading = false
            } else {
                status = "⚠️ Không thể đọc thẻ"
            }
        } catch (e: Exception) {
            status = "❌ Lỗi đọc thẻ: ${e.message}"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFE5EC),  // Hồng nhạt
                        Color(0xFFFFF4E6),  // Cam nhạt
                        Color(0xFFE8F5E9)   // Xanh nhạt
                    )
                )
            )
    ) {
        FloatingBubbles()

        Column(
            modifier = Modifier
                . fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 80.dp, vertical = 20.dp)
        ) {
            // ✅ HEADER CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color. Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF667EEA),  // Xanh tím
                                    Color(0xFF764BA2),  // Tím
                                    Color(0xFFF093FB)   // Hồng tím
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White. copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier. size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📜 Lịch Sử Giao Dịch",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier. height(6.dp))
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color. White. copy(alpha = 0.25f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("👤", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (customerId.isNotEmpty()) customerId else "Đang tải...",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💳", fontSize = 32.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier. height(20.dp))

            // ✅ CONTENT CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(12.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp)
                ) {
                    // ✅ STATUS MESSAGE
                    if (status.isNotBlank()) {
                        Card(
                            modifier = Modifier. fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    status.startsWith("✅") -> Color(0xFFE8F5E9)
                                    status.startsWith("⏳") -> Color(0xFFFFF3E0)
                                    status.startsWith("📭") -> Color(0xFFFFF9C4)
                                    else -> Color(0xFFFFEBEE)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when {
                                        status.startsWith("✅") -> "✅"
                                        status.startsWith("⏳") -> "⏳"
                                        status.startsWith("📭") -> "📭"
                                        status.startsWith("⚠️") -> "⚠️"
                                        else -> "❌"
                                    },
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = status. drop(2).trim(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // ✅ SECTION HEADER
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2196F3).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📊", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier. width(12.dp))
                        Text(
                            text = "Lịch sử giao dịch (${transactions.size})",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2196F3)
                        )
                    }

                    Spacer(modifier = Modifier. height(20.dp))

                    // ✅ TRANSACTION LIST
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier. size(52.dp),
                                color = Color(0xFF667EEA),
                                strokeWidth = 5.dp
                            )
                        }
                    } else if (transactions.isEmpty()) {
                        Card(
                            modifier = Modifier. fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF3E0)
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📭", fontSize = 64.sp)
                                Spacer(modifier = Modifier. height(16.dp))
                                Text(
                                    text = "Chưa có giao dịch nào",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement. spacedBy(14.dp)
                        ) {
                            transactions.forEach { txn ->
                                TransactionCard(txn)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(txn: TransactionDto) {
    val chipColor = if (txn.type == "TOPUP") Color(0xFF4CAF50) else Color(0xFFFF7043)
    val bgColor = if (txn.type == "TOPUP") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)

    Card(
        modifier = Modifier. fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment. CenterVertically
        ) {
            // ICON BÊN TRÁI
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                chipColor.copy(alpha = 0.3f),
                                chipColor.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment. Center
            ) {
                Text(
                    text = if (txn.type == "TOPUP") "💰" else "🎮",
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier. width(14.dp))

            // INFO BÊN PHẢI
            Column(modifier = Modifier.weight(1f)) {
                // TYPE + DATE
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor)
                    ) {
                        Text(
                            text = if (txn.type == "TOPUP") "Nạp tiền" else "Mua vé",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = chipColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = txn.createdAt. take(19).replace('T', ' '),
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier. height(8.dp))

                // AMOUNT
                Text(
                    text = "💵 ${formatVnd(txn.amount)} VNĐ",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // DETAILS
                txn.gameCode?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎯", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Game #$it", fontSize = 13.sp, color = Color(0xFF666666))
                    }
                }

                txn.tickets?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎫", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$it lượt", fontSize = 13.sp, color = Color(0xFF666666))
                    }
                }

                txn.balanceAfter?.let {
                    Row(verticalAlignment = Alignment. CenterVertically) {
                        Text("💳", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Số dư: ${formatBalance(it)} VNĐ",
                            fontSize = 13.sp,
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

private fun formatVnd(amount: String): String {
    val base = amount.toDoubleOrNull() ?: return amount
    val vnd = base.toLong()
    return "%,d". format(vnd)
}

private fun formatBalance(balance: Int): String {
    return "%,d".format(balance. toLong() * 1000)
}