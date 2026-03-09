package org.example.project

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.project.model.GameDto
import org.example.project.screen.ConnectScreen
import org.example.project.screen.FloatingBubbles
import org.example.project.screen.GameSelectionScreen

// ===== HELPER FUNCTIONS =====

private fun getGameName(gameCode: Int): String {
    return when (gameCode) {
        1001 -> "Tàu Lượn"
        1002 -> "Đu Quay"
        1003 -> "Nhà Phao"
        1004 -> "Tàu Cướp Biển"
        1005 -> "Bể Bơi"
        1006 -> "Con Lắc 360°"
        1007 -> "Nhà Ma"
        1008 -> "Đua Xe"
        else -> "Game #$gameCode"
    }
}

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

// ===== ENUMS =====

private enum class GamePlayAppScreen {
    SELECTION,
    CONNECT,
    PLAYING
}

// ===== MAIN APP =====

@Composable
private fun GamePlayApp(initialGame: GameDto?) {
    var currentScreen by remember { mutableStateOf(
        if (initialGame != null) GamePlayAppScreen.CONNECT else GamePlayAppScreen.SELECTION
    ) }
    var selectedGame by remember { mutableStateOf(initialGame) }
    val smartCardManager = remember { SmartCardManager() }

    when (currentScreen) {
        GamePlayAppScreen.SELECTION -> {
            GameSelectionScreen(
                onGameSelected = { game ->
                    selectedGame = game
                    currentScreen = GamePlayAppScreen.CONNECT
                },
                onBack = {
                    // Return to main menu - user needs to handle this at higher level
                }
            )
        }

        GamePlayAppScreen.CONNECT -> {
            ConnectScreen(
                onCardConnected = {
                    currentScreen = GamePlayAppScreen.PLAYING
                },
                smartCardManager = smartCardManager,
                requireRSAAuth = true
            )
        }

        GamePlayAppScreen.PLAYING -> {
            if (selectedGame != null) {
                GamePlayScreen(
                    smartCardManager = smartCardManager,
                    game = selectedGame!!,
                    onComplete = {
                        currentScreen = GamePlayAppScreen.SELECTION
                        selectedGame = null
                    }
                )
            }
        }
    }
}

// ===== GAME PLAY SCREEN =====

@Composable
private fun GamePlayScreen(
    smartCardManager: SmartCardManager,
    game: GameDto,
    onComplete: () -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var currentTickets by remember { mutableStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("⏳ Đang đọc thẻ...") }

    val scope = rememberCoroutineScope()

    fun processCard() {
        scope.launch {
            isProcessing = true
            statusMessage = "⏳ Đang đọc thẻ..."

            try {
                println("═══════════════════════════════════")
                println("🎮 QUÉT THẺ VÀO CHƠI")
                println("Game: ${game.gameName}")
                println("Code: ${game.gameCode}")
                println("───────────────────────────────────")
                println("✅ RSA đã được xác thực ở màn hình Connect")

                // Tìm game cụ thể trên thẻ
                statusMessage = "⏳ Đang kiểm tra lượt chơi..."
                println("🔍 Tìm game code: ${game.gameCode}")
                val targetGame = smartCardManager.findGame(game.gameCode)
                println("📊 Kết quả: ${if (targetGame != null) "Tìm thấy - ${targetGame.tickets} lượt" else "Không tìm thấy"}")

                if (targetGame == null || targetGame.tickets <= 0) {
                    println("❌ Không có lượt chơi")
                    statusMessage = "❌ KHÔNG CÓ LƯỢT!\n\nKhách chưa mua lượt ${game.gameName}"
                    delay(3000)
                    smartCardManager.disconnect()
                    println("═══════════════════════════════════")
                    onComplete()
                    return@launch
                }

                currentTickets = targetGame.tickets
                println("Lượt hiện tại: $currentTickets")
                delay(500)

                // Trừ lượt
                statusMessage = "⏳ Đang trừ lượt..."
                println("➖ Gửi lệnh DECREASE_GAME_TICKETS: gameCode=${game.gameCode}, tickets=1")
                val success = smartCardManager.decreaseGameTickets(game.gameCode, 1)
                println("📤 Đã gửi INS 0x12 - Kết quả: ${if (success) "SUCCESS" else "FAILED"}")

                if (success) {
                    println("✅ Trừ lượt thành công!")
                    println("Còn lại: ${currentTickets - 1} lượt")
                    statusMessage = "✅ THÀNH CÔNG!\n\nCho phép vào chơi\nCòn ${currentTickets - 1} lượt"
                } else {
                    println("❌ Trừ lượt thất bại!")
                    statusMessage = "❌ LỖI!\n\nKhông thể trừ lượt"
                }

                println("═══════════════════════════════════")
                delay(3000)
                smartCardManager.disconnect()
                onComplete()

            } catch (e: Exception) {
                println("❌ Exception: ${e.message}")
                e.printStackTrace()
                statusMessage = "❌ LỖI!\n\n${e.message}"
                delay(3000)
                smartCardManager.disconnect()
                onComplete()
            } finally {
                isProcessing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        processCard()
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
                .padding(60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // GAME INFO CARD
            Card(
                modifier = Modifier
                    .width(700.dp)
                    . shadow(24.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color. Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = getGameColors(game.gameCode)
                            )
                        )
                        .padding(40.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f))
                                .shadow(8.dp, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = getGameEmoji(game.gameCode),
                                fontSize = 64.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = game.gameName,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = "Game #${game.gameCode}",
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier. height(40.dp))

            // STATUS CARD
            Card(
                modifier = Modifier
                    .width(700.dp)
                    .shadow(16.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        statusMessage.startsWith("✅") -> Color(0xFFE8F5E9)
                        statusMessage.startsWith("❌") -> Color(0xFFFFEBEE)
                        else -> Color(0xFFFFF8E1)
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = Color(0xFFFF6B9D),
                            strokeWidth = 6.dp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Text(
                        text = statusMessage,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        lineHeight = 40.sp
                    )

                    if (customerName.isNotEmpty() && ! statusMessage.startsWith("⏳")) {
                        Spacer(modifier = Modifier. height(24.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("👤", fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Khách hàng",
                                    fontSize = 14.sp,
                                    color = Color. Gray
                                )
                                Text(
                                    customerName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===== MAIN ENTRY POINT =====

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "🎮 SmartCard Park - GAMEPLAY"
    ) {
        GamePlayApp(initialGame = null)  // ✅ Bắt đầu từ màn hình chọn game từ server
    }
}