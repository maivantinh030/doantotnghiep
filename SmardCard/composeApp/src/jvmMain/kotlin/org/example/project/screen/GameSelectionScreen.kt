package org.example.project.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.network.GameApiClient
import org.example.project.model.GameDto
import org.jetbrains.skia.Image as SkiaImage
import java.math.BigDecimal
import java.text.DecimalFormat

private fun getGameEmoji(gameCode: Int): String {
    return when (gameCode) {
        1001 -> "🎢"
        1002 -> "🎡"
        1003 -> "🏰"
        1004 -> "🏴‍☠️"
        1005 -> "🏊"
        1006 -> "🎪"
        1007 -> "👻"
        1008 -> "🏎️"
        else -> "🎮"
    }
}

private fun getGameColors(gameCode: Int): List<Color> {
    return when (gameCode) {
        1001 -> listOf(Color(0xFFFF6B6B), Color(0xFFFF8E8E))
        1002 -> listOf(Color(0xFF4ECDC4), Color(0xFF6EE5DB))
        1003 -> listOf(Color(0xFFFFBE0B), Color(0xFFFFD60A))
        1004 -> listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA))
        1005 -> listOf(Color(0xFF3B82F6), Color(0xFF60A5FA))
        1006 -> listOf(Color(0xFFEC4899), Color(0xFFF472B6))
        1007 -> listOf(Color(0xFF6366F1), Color(0xFF818CF8))
        1008 -> listOf(Color(0xFFEF4444), Color(0xFFF87171))
        else -> listOf(Color(0xFF9E9E9E), Color(0xFFBDBDBD))
    }
}

private fun formatGamePrice(priceText: String): String {
    return try {
        val amount = BigDecimal(priceText)
        "${DecimalFormat("#,##0").format(amount)} VND/lượt"
    } catch (_: Exception) {
        "$priceText VND/lượt"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSelectionScreen(
    onGameSelected: (GameDto) -> Unit,
    onBack: () -> Unit
) {
    var games by remember { mutableStateOf<List<GameDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val gameApiClient = remember { GameApiClient() }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                println("🎮 Lấy danh sách games từ server...")
                val result = gameApiClient.getAllGames()
                if (result.isSuccess) {
                    val allGames = result.getOrNull() ?: emptyList()
                    // Chỉ hiển thị các game đang hoạt động (isActive = true)
                    games = allGames.filter { it.isActive }
                    println("✅ Đã lấy ${allGames.size} games từ server, hiển thị ${games.size} game đang hoạt động")
                } else {
                    errorMessage = "Lỗi tải games: ${result.exceptionOrNull()?.message}"
                    println("❌ $errorMessage")
                }
            } catch (e: Exception) {
                errorMessage = "Lỗi: ${e.message}"
                println("❌ Exception: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF3E0),
                        Color(0xFFFFE0F0),
                        Color(0xFFE0F7FA)
                    )
                )
            )
    ) {
        FloatingBubbles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF5C6BC0)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("←", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "🎮 Chọn Trò Chơi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Quét thẻ để chơi trò yêu thích",
                            fontSize = 12.sp,
                            color = Color.White.copy(0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF5C6BC0))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("⏳ Đang tải danh sách games...", color = Color(0xFF666666))
                        }
                    }
                }

                errorMessage.isNotEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .shadow(12.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "❌ Lỗi tải games",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = errorMessage,
                                    fontSize = 14.sp,
                                    color = Color(0xFFD32F2F)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onBack,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C6BC0)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Quay lại")
                                }
                            }
                        }
                    }
                }

                games.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📭 Không có trò chơi nào",
                            fontSize = 16.sp,
                            color = Color(0xFF999999)
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        games.forEach { game ->
                            GameSelectionCard(
                                game = game,
                                onSelect = { onGameSelected(game) }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameSelectionCard(
    game: GameDto,
    onSelect: () -> Unit
) {
    var gameImage by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(game.gameImage) {
        if (game.gameImage != null) {
            try {
                val imageBytes = java.util.Base64.getDecoder().decode(game.gameImage)
                val skiaBitmap = SkiaImage.makeFromEncoded(imageBytes)
                gameImage = skiaBitmap.toComposeImageBitmap()
            } catch (e: Exception) {
                println("Lỗi decode ảnh game ${game.gameCode}: ${e.message}")
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .clickable { onSelect() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image or Emoji
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = getGameColors(game.gameCode)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (gameImage != null) {
                    Image(
                        bitmap = gameImage!!,
                        contentDescription = game.gameName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = getGameEmoji(game.gameCode),
                        fontSize = 48.sp
                    )
                }
            }

            // Game Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = game.gameName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = game.displayDescription ?: "Khong co mo ta",
                    fontSize = 13.sp,
                    color = Color(0xFF666666),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Text(
                            text = formatGamePrice(game.ticketPrice),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    if (game.isActive) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                        ) {
                            Text(
                                text = "✅ Hoạt động",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                color = Color(0xFF1565C0)
                            )
                        }
                    } else {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Text(
                                text = "❌ Bảo trì",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                }
            }

            // Arrow
            Text(
                text = "→",
                fontSize = 24.sp,
                color = Color(0xFF5C6BC0),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
