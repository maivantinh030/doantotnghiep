package org.example.project.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import org.example.project.data.model.GameRevenue
import org.example.project.data.model.RevenuePoint
import org.example.project.screen.FloatingBubbles
import org.example.project.viewmodel.RevenueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRevenueScreen(
    onBack: () -> Unit,
    viewModel: RevenueViewModel = remember { RevenueViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val dayData = uiState.dayData
    val monthData = uiState.monthData
    val gameData = uiState.gameData
    val isLoading = uiState.isLoading
    val status = uiState.errorMessage?.let { "❌ $it" }
        ?: if (dayData.isNotEmpty() || monthData.isNotEmpty() || gameData.isNotEmpty()) "✅ Đã tải dữ liệu"
        else ""


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFE5EC),  // ✅ GIỐNG AdminGameManagement
                        Color(0xFFFFF4E6),
                        Color(0xFFE8F5E9)
                    )
                )
            )
    ) {
        FloatingBubbles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())  // ✅ THÊM scroll
                .padding(horizontal = 80.dp, vertical = 20.dp)  // ✅ GIỐNG AdminGameManagement
        ) {
            // ✅ HEADER CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp)),  // ✅ GIỐNG
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color. Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF9C27B0),  // Tím
                                    Color(0xFFBA68C8),
                                    Color(0xFFCE93D8)
                                )
                            )
                        )
                        .padding(20.dp)  // ✅ GIỐNG
                ) {
                    Row(
                        modifier = Modifier. fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(48.dp)  // ✅ GIỐNG
                                .clip(CircleShape)
                                .background(Color. White. copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color. White,
                                modifier = Modifier. size(26.dp)  // ✅ GIỐNG
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📊 Thống Kê Doanh Thu",
                                fontSize = 22.sp,  // ✅ GIỐNG
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color. White.copy(alpha = 0.25f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("💰", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Báo cáo chi tiết",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { viewModel.loadAll() },
                            enabled = ! isLoading,
                            modifier = Modifier
                                .size(60.dp)  // ✅ GIỐNG
                                . clip(CircleShape)
                                .background(Color.White. copy(alpha = 0.3f))
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 3.dp,
                                    color = Color. White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier. height(20.dp))  // ✅ GIỐNG

            // ✅ CONTENT CARD
            Card(
                modifier = Modifier
                    . fillMaxWidth()
                    . wrapContentHeight()  // ✅ GIỐNG
                    .shadow(12.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp)  // ✅ GIỐNG
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
                                        else -> "❌"
                                    },
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = status .drop(2).trim(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // 📅 DOANH THU THEO NGÀY
                    SectionHeader(
                        title = "Doanh Thu Theo Ngày",
                        icon = "📅",
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier. height(16.dp))
                    RevenueList(dayData)

                    Spacer(modifier = Modifier. height(28.dp))
                    HorizontalDivider(
                        color = Color(0xFFE0E0E0),
                        thickness = 2.dp
                    )
                    Spacer(modifier = Modifier.height(28.dp))

                    // 📆 DOANH THU THEO THÁNG
                    SectionHeader(
                        title = "Doanh Thu Theo Tháng",
                        icon = "📆",
                        color = Color(0xFF2196F3)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    RevenueList(monthData)

                    Spacer(modifier = Modifier.height(28.dp))
                    HorizontalDivider(
                        color = Color(0xFFE0E0E0),
                        thickness = 2.dp
                    )
                    Spacer(modifier = Modifier.height(28.dp))

                    // 🎮 DOANH THU THEO GAME
                    SectionHeader(
                        title = "Doanh Thu Theo Game",
                        icon = "🎮",
                        color = Color(0xFFFF9800)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GameRevenueList(gameData)
                }
            }
        }
    }
}

// ✅ SECTION HEADER
@Composable
private fun SectionHeader(title: String, icon: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color. copy(alpha = 0.2f)),
            contentAlignment = Alignment. Center
        ) {
            Text(icon, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

// ✅ REVENUE LIST
@Composable
private fun RevenueList(items: List<RevenuePoint>) {
    if (items.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
        ) {
            Box(
                modifier = Modifier. fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", fontSize = 40.sp)
                    Spacer(modifier = Modifier. height(8.dp))
                    Text("Chưa có dữ liệu", color = Color. Gray, fontSize = 14.sp)
                }
            }
        }
    } else {
        Column(verticalArrangement = Arrangement. spacedBy(12.dp)) {
            items. forEach { item ->
                Card(
                    modifier = Modifier. fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),  // ✅ GIỐNG AdminGameCard
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),  // ✅ GIỐNG
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = item. label,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color(0xFF333333)
                            )
                            Spacer(modifier = Modifier. height(4.dp))
                            Text(
                                text = "Doanh thu",
                                fontSize = 12.sp,
                                color = Color. Gray
                            )
                        }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults. cardColors(
                                containerColor = Color(0xFF4CAF50).copy(alpha = 0.15f)
                            )
                        ) {
                            Text(
                                text = "${formatVnd(item.totalAmount)} VNĐ",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ✅ GAME REVENUE LIST
@Composable
private fun GameRevenueList(items: List<GameRevenue>) {
    if (items.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults. cardColors(containerColor = Color(0xFFFAFAFA))
        ) {
            Box(
                modifier = Modifier. fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎮", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Chưa có dữ liệu game", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),  // ✅ GIỐNG
                    colors = CardDefaults. cardColors(containerColor = Color(0xFFFAFAFA)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),  // ✅ GIỐNG
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ICON GAME
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush. radialGradient(
                                        colors = listOf(
                                            Color(0xFFFF9800),
                                            Color(0xFFFFB74D)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎯", fontSize = 32.sp)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // INFO
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Game #${item.gameCode}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF333333)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎫", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${item.totalTickets} lượt",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // REVENUE
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2196F3).copy(alpha = 0.15f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = formatVnd(item.totalAmount)+" VNĐ",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1565C0)
                                )
//                                Text(
//                                    text = "VNĐ",
//                                    fontSize = 12.sp,
//                                    color = Color(0xFF1565C0)
//                                )
                            }
                        }
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