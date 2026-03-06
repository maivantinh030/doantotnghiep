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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.data.model.TicketDTO
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.GameViewModel
import com.example.appcongvien.viewmodel.OrderViewModel

// Nhóm các vé của cùng 1 game
data class GameTicketGroup(
    val gameId: String?,
    val gameName: String,
    val tickets: List<TicketDTO>
) {
    val totalRemainingTurns: Int get() = tickets.sumOf { it.remainingTurns }
    val totalOriginalTurns: Int get() = tickets.sumOf { it.originalTurns }
    val hasValidTicket: Boolean get() = tickets.any { it.status == "VALID" }
    // Ngày hết hạn xa nhất trong nhóm vé còn hiệu lực
    val latestExpiry: String? get() = tickets
        .filter { it.status == "VALID" && !it.expiryDate.isNullOrBlank() }
        .maxByOrNull { it.expiryDate ?: "" }
        ?.expiryDate
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyGamesScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as App

    val orderViewModel: OrderViewModel = viewModel(
        factory = OrderViewModel.Factory(app.orderRepository)
    )
    val gameViewModel: GameViewModel = viewModel(
        factory = GameViewModel.Factory(app.gameRepository)
    )

    val ticketsState by orderViewModel.ticketsState.collectAsState()
    val gamesState by gameViewModel.gamesState.collectAsState()

    val gameNameMap = remember(gamesState) {
        when (val s = gamesState) {
            is Resource.Success -> s.data.items?.associate { it.gameId to it.name } ?: emptyMap()
            else -> emptyMap()
        }
    }

    LaunchedEffect(Unit) {
        orderViewModel.loadMyTickets(page = 1, size = 100)
        gameViewModel.loadGames(page = 1, size = 200)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Game Của Tôi", fontWeight = FontWeight.Bold, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.WarmOrange)
            )
        }
    ) { paddingValues ->
        when (val state = ticketsState) {
            null, is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.WarmOrange)
                }
            }

            is Resource.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lỗi: ${state.message}",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            is Resource.Success -> {
                val tickets = state.data.items ?: emptyList()

                if (tickets.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AppColors.WarmOrangeSoft,
                                modifier = Modifier.size(80.dp)
                            ) {
                                Icon(
                                    Icons.Default.SportsEsports,
                                    contentDescription = null,
                                    tint = AppColors.WarmOrange,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                            Text(
                                "Chưa có vé game nào",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.PrimaryDark
                            )
                            Text(
                                "Mua vé để bắt đầu chơi game tại công viên",
                                fontSize = 14.sp,
                                color = AppColors.PrimaryGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                } else {
                    // Nhóm vé theo gameId
                    val groups = tickets
                        .groupBy { it.gameId }
                        .map { (gameId, ticketList) ->
                            GameTicketGroup(
                                gameId = gameId,
                                gameName = gameId?.let { gameNameMap[it] } ?: gameId ?: "Game không rõ",
                                tickets = ticketList
                            )
                        }
                        // Ưu tiên nhóm còn hiệu lực lên đầu
                        .sortedByDescending { it.hasValidTicket }

                    val activeGroups = groups.filter { it.hasValidTicket }
                    val inactiveGroups = groups.filter { !it.hasValidTicket }

                    val totalRemainingTurns = activeGroups.sumOf { it.totalRemainingTurns }
                    val totalGames = activeGroups.size
                    val totalInactive = inactiveGroups.size

                    LazyColumn(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(Brush.verticalGradient(listOf(AppColors.SurfaceLight, Color.White))),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Summary card
                        item {
                            MyGamesSummaryCard(
                                totalGames = totalGames,
                                totalRemainingTurns = totalRemainingTurns,
                                totalInactive = totalInactive
                            )
                        }

                        // Nhóm còn hiệu lực
                        if (activeGroups.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Đang có (${activeGroups.size} game)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.PrimaryDark
                                )
                            }
                            items(activeGroups) { group ->
                                GameTicketGroupCard(group = group)
                            }
                        }

                        // Nhóm đã dùng hết / hết hạn
                        if (inactiveGroups.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Đã hết lượt / Hết hạn (${inactiveGroups.size} game)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.PrimaryDark
                                )
                            }
                            items(inactiveGroups) { group ->
                                GameTicketGroupCard(group = group)
                            }
                        }

                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MyGamesSummaryCard(
    totalGames: Int,
    totalRemainingTurns: Int,
    totalInactive: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Tổng quan vé game",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.PrimaryDark
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem("$totalGames", "Game đang có", Color(0xFF4CAF50))
                SummaryStatItem("$totalRemainingTurns", "Lượt còn lại", AppColors.WarmOrange)
                SummaryStatItem("$totalInactive", "Đã hết lượt", AppColors.PrimaryGray)
            }
        }
    }
}

@Composable
private fun SummaryStatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 11.sp, color = AppColors.PrimaryGray, textAlign = TextAlign.Center)
    }
}

@Composable
fun GameTicketGroupCard(group: GameTicketGroup) {
    val accentColor = if (group.hasValidTicket) AppColors.WarmOrange else AppColors.PrimaryGray

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: tên game + tổng lượt
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Column {
                        Text(
                            text = group.gameName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryDark,
                            maxLines = 2
                        )
                        Text(
                            text = "${group.tickets.size} vé",
                            fontSize = 12.sp,
                            color = AppColors.PrimaryGray
                        )
                    }
                }

                // Tổng lượt còn lại nổi bật
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${group.totalRemainingTurns}",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Text(
                        text = "/ ${group.totalOriginalTurns} lượt",
                        fontSize = 11.sp,
                        color = AppColors.PrimaryGray
                    )
                }
            }

            // Progress bar tổng
            val totalProgress = if (group.totalOriginalTurns > 0)
                group.totalRemainingTurns.toFloat() / group.totalOriginalTurns.toFloat()
            else 0f

            LinearProgressIndicator(
                progress = { totalProgress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = accentColor,
                trackColor = AppColors.SurfaceLight
            )

            // Ngày hết hạn sớm nhất
            val latestExpiry = group.latestExpiry
            if (!latestExpiry.isNullOrBlank()) {
                Text(
                    text = "Hết hạn: ${formatTicketDate(latestExpiry)}",
                    fontSize = 11.sp,
                    color = AppColors.PrimaryGray
                )
            }

            // Chi tiết từng vé nếu có nhiều hơn 1
            if (group.tickets.size > 1) {
                HorizontalDivider(color = AppColors.SurfaceLight)
                Text(
                    text = "Chi tiết từng vé",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.PrimaryGray
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    group.tickets.forEach { ticket ->
                        TicketSubRow(ticket = ticket)
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketSubRow(ticket: TicketDTO) {
    val (statusColor, statusIcon, statusLabel) = when (ticket.status) {
        "VALID" -> Triple(Color(0xFF4CAF50), Icons.Default.CheckCircle, "Còn hiệu lực")
        "USED" -> Triple(AppColors.PrimaryGray, Icons.Default.HourglassEmpty, "Đã dùng")
        else -> Triple(Color(0xFFF44336), Icons.Default.Error, "Hết hạn")
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = AppColors.SurfaceLight
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mã vé
            Text(
                text = "Vé #${ticket.ticketId.take(8).uppercase()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.PrimaryDark
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lượt còn lại
                Text(
                    text = "${ticket.remainingTurns}/${ticket.originalTurns} lượt",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (ticket.status == "VALID") AppColors.WarmOrange else AppColors.PrimaryGray
                )

                // Badge trạng thái
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = statusLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }
                }
            }
        }
    }
}

private fun formatTicketDate(isoString: String): String {
    return try {
        val datePart = isoString.substring(0, 10).split("-")
        "${datePart[2]}/${datePart[1]}/${datePart[0]}"
    } catch (_: Exception) {
        isoString
    }
}
