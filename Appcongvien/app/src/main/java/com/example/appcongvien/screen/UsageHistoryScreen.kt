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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.appcongvien.App
import com.example.appcongvien.components.ParkTopAppBar
import com.example.appcongvien.data.model.GamePlayHistoryDTO
import com.example.appcongvien.data.model.GamePlayHistoryPageDTO
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.data.network.RetrofitClient
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.GameViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageHistoryScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val gameRepository = (context.applicationContext as App).gameRepository
    val viewModel: GameViewModel = viewModel(factory = GameViewModel.Factory(gameRepository))

    val playHistoryState by viewModel.playHistoryState.collectAsState()
    val backgroundBrush = Brush.verticalGradient(
        listOf(
            AppColors.BackgroundWarm,
            AppColors.SurfaceLight,
            AppColors.SurfaceWhite
        )
    )

    LaunchedEffect(Unit) {
        viewModel.loadMyGamePlays(page = 1, size = 200)
    }

    Scaffold(
        topBar = {
            ParkTopAppBar(
                title = "Lịch Sử Chơi Game",
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = { viewModel.loadMyGamePlays(page = 1, size = 200) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Làm mới",
                            tint = AppColors.PrimaryDark
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = playHistoryState) {
            is Resource.Loading, null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(backgroundBrush),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.WarmOrange)
                }
            }

            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(backgroundBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = AppColors.RedError,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            is Resource.Success -> {
                val data = state.data
                val plays = data.items
                val totalSpent = runCatching { BigDecimal(data.totalAmount) }
                    .getOrDefault(BigDecimal.ZERO)

                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(backgroundBrush),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        UsageSummaryCard(
                            totalSpent = totalSpent,
                            totalPlays = data.total.toInt(),
                            uniqueGames = data.uniqueGames
                        )
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Tất cả lượt chơi",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark
                            )
                            Text(
                                text = "Xem lại các trò chơi bạn đã chơi, số tiền và phương thức thanh toán.",
                                fontSize = 13.sp,
                                color = AppColors.PrimaryGray.copy(alpha = 0.82f),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    if (plays.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.7f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = AppColors.WarmOrangeSoft.copy(alpha = 0.55f),
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SportsEsports,
                                            contentDescription = null,
                                            tint = AppColors.WarmOrange,
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }
                                    Text(
                                        text = "Chưa có lượt chơi nào",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.PrimaryDark
                                    )
                                    Text(
                                        text = "Hãy quẹt thẻ tại quầy trò chơi để bắt đầu những trải nghiệm đầu tiên nhé!",
                                        fontSize = 13.sp,
                                        color = AppColors.PrimaryGray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(plays, key = { it.logId }) { play ->
                            GamePlayRecordCard(play = play)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageSummaryCard(
    totalSpent: BigDecimal,
    totalPlays: Int,
    uniqueGames: Int
) {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
    val totalSpentInt = totalSpent.toLong()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.75f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UsageSummaryMetric(
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                iconTint = AppColors.RedError,
                iconBackground = AppColors.RedErrorContainer,
                value = "${formatter.format(totalSpentInt)}đ",
                label = "Tổng chi",
                modifier = Modifier.weight(1f)
            )
            UsageSummaryMetric(
                icon = Icons.Default.VideogameAsset,
                iconTint = AppColors.WarmOrange,
                iconBackground = AppColors.WarmOrangeSoft.copy(alpha = 0.55f),
                value = "$totalPlays",
                label = "Lượt chơi",
                modifier = Modifier.weight(1f)
            )
            UsageSummaryMetric(
                icon = Icons.Default.SportsEsports,
                iconTint = AppColors.GreenSuccess,
                iconBackground = AppColors.GreenSuccessContainer,
                value = "$uniqueGames",
                label = "Trò chơi",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun UsageSummaryMetric(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = iconBackground,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.padding(12.dp)
            )
        }
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryDark,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = AppColors.PrimaryGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GamePlayRecordCard(play: GamePlayHistoryDTO) {
    val amount = runCatching { BigDecimal(play.amountCharged) }.getOrDefault(BigDecimal.ZERO).toLong()
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
    val imageModel = remember(play.gameThumbnailUrl) { resolveUsageImageUrl(play.gameThumbnailUrl) }

    val (methodIcon, methodTint, methodBackground, methodLabel) = when (play.method.uppercase()) {
        "CARD" -> MethodAppearance(
            icon = Icons.Default.CreditCard,
            tint = AppColors.InfoPrimary,
            background = AppColors.InfoContainer,
            label = "Quẹt thẻ"
        )

        "BALANCE" -> MethodAppearance(
            icon = Icons.Default.AccountBalanceWallet,
            tint = AppColors.WarmOrange,
            background = AppColors.WarmOrangeSoft.copy(alpha = 0.55f),
            label = "Số dư ví"
        )

        else -> MethodAppearance(
            icon = Icons.Default.History,
            tint = AppColors.PrimaryGray,
            background = AppColors.SurfaceMuted,
            label = play.method
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AppColors.WarmOrangeSoft.copy(alpha = 0.45f),
                    modifier = Modifier.size(56.dp)
                ) {
                    if (imageModel != null) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = play.gameName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = AppColors.WarmOrange,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = play.gameName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )
                    if (!play.gameCategory.isNullOrBlank()) {
                        Text(
                            text = play.gameCategory,
                            fontSize = 12.sp,
                            color = AppColors.PrimaryGray
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = methodBackground
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = methodIcon,
                                contentDescription = null,
                                tint = methodTint,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = methodLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = methodTint
                            )
                        }
                    }
                }

                Text(
                    text = "-${formatter.format(amount)}đ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.RedError
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                UsageMetaRow(
                    label = "Mã lượt chơi",
                    value = play.logId.take(8).uppercase()
                )
                UsageMetaRow(
                    label = "Thời gian",
                    value = formatPlayTimestamp(play.playedAt)
                )
                play.cardBalanceAfter?.let { remaining ->
                    val remainingLong = runCatching { BigDecimal(remaining).toLong() }.getOrNull()
                    if (remainingLong != null) {
                        UsageMetaRow(
                            label = "Số dư sau khi chơi",
                            value = "${formatter.format(remainingLong)}đ"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = AppColors.PrimaryGray
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.PrimaryDark
        )
    }
}

private data class MethodAppearance(
    val icon: ImageVector,
    val tint: Color,
    val background: Color,
    val label: String
)

private fun resolveUsageImageUrl(url: String?): String? {
    val value = url?.trim()?.takeIf { it.isNotBlank() } ?: return null
    return if (value.startsWith("http://") || value.startsWith("https://")) {
        value
    } else {
        RetrofitClient.BASE_URL.trimEnd('/') + if (value.startsWith("/")) value else "/$value"
    }
}

private fun formatPlayTimestamp(timestamp: String): String {
    return try {
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        // Backend trả Instant.toString() (ví dụ 2025-04-12T08:30:00Z) hoặc dạng không có Z
        val cleaned = timestamp.substringBefore('Z').substringBefore('+')
        val parsers = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        )
        parsers.forEach { it.timeZone = java.util.TimeZone.getTimeZone("UTC") }
        val date = parsers.firstNotNullOfOrNull {
            runCatching { it.parse(cleaned) }.getOrNull()
        } ?: return timestamp
        outputFormat.format(date)
    } catch (_: Exception) {
        timestamp
    }
}
