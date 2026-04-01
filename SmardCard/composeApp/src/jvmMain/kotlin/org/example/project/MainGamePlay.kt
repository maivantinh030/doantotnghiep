package org.example.project

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.project.auth.AdminSession
import org.example.project.auth.TokenStore
import org.example.project.model.GameDto
import org.example.project.network.GameApiClient
import org.example.project.screen.FloatingBubbles
import org.example.project.screen.GameSelectionScreen
import java.math.BigDecimal
import java.text.DecimalFormat

private enum class GamePlayAppScreen {
    LOGIN,
    SELECTION,
    PLAYING
}

private fun pickGameEmoji(gameName: String): String {
    val lower = gameName.lowercase()
    return when {
        listOf("tau luon", "roller", "coaster").any { lower.contains(it) } -> "🎢"
        listOf("du quay", "wheel", "ferris").any { lower.contains(it) } -> "🎡"
        listOf("nha ma", "ghost", "haunted").any { lower.contains(it) } -> "👻"
        listOf("dua xe", "race", "kart").any { lower.contains(it) } -> "🏎️"
        listOf("hoi", "swing", "pendulum").any { lower.contains(it) } -> "🎠"
        listOf("nuoc", "water", "boi").any { lower.contains(it) } -> "🏊"
        else -> "🎮"
    }
}

private fun pickGameColors(gameName: String): List<Color> {
    val lower = gameName.lowercase()
    return when {
        listOf("tau luon", "roller", "coaster").any { lower.contains(it) } ->
            listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53))
        listOf("du quay", "wheel", "ferris").any { lower.contains(it) } ->
            listOf(Color(0xFF4ECDC4), Color(0xFF44A3AA))
        listOf("nha ma", "ghost", "haunted").any { lower.contains(it) } ->
            listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
        listOf("dua xe", "race", "kart").any { lower.contains(it) } ->
            listOf(Color(0xFFEF4444), Color(0xFFF59E0B))
        listOf("nuoc", "water", "boi").any { lower.contains(it) } ->
            listOf(Color(0xFF0EA5E9), Color(0xFF38BDF8))
        else -> listOf(Color(0xFF7C3AED), Color(0xFFEC4899))
    }
}

private fun formatMoney(amountText: String?): String {
    if (amountText.isNullOrBlank()) return "--"
    return try {
        val amount = BigDecimal(amountText)
        "${DecimalFormat("#,##0").format(amount)} VND"
    } catch (_: Exception) {
        "$amountText VND"
    }
}

@Composable
private fun GamePlayApp(initialGame: GameDto?) {
    var currentScreen by remember {
        mutableStateOf(
            when {
                TokenStore.getToken().isNullOrBlank() -> GamePlayAppScreen.LOGIN
                initialGame != null -> GamePlayAppScreen.PLAYING
                else -> GamePlayAppScreen.SELECTION
            }
        )
    }
    var selectedGame by remember { mutableStateOf(initialGame) }
    val smartCardManager = remember { SmartCardManager() }

    when (currentScreen) {
        GamePlayAppScreen.LOGIN -> {
            AdminLoginGate(
                onLoginSuccess = {
                    currentScreen = if (selectedGame != null) {
                        GamePlayAppScreen.PLAYING
                    } else {
                        GamePlayAppScreen.SELECTION
                    }
                }
            )
        }

        GamePlayAppScreen.SELECTION -> {
            GameSelectionScreen(
                onGameSelected = { game ->
                    selectedGame = game
                    currentScreen = GamePlayAppScreen.PLAYING
                },
                onBack = {}
            )
        }

        GamePlayAppScreen.PLAYING -> {
            val game = selectedGame
            if (game != null) {
                GamePlayScreen(
                    smartCardManager = smartCardManager,
                    game = game,
                    onComplete = {
                        currentScreen = GamePlayAppScreen.SELECTION
                        selectedGame = null
                    },
                    onSessionExpired = {
                        TokenStore.clear()
                        currentScreen = GamePlayAppScreen.LOGIN
                        selectedGame = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminLoginGate(
    onLoginSuccess: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val adminSession = remember { AdminSession() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF3E0),
                        Color(0xFFFFE4EC),
                        Color(0xFFE0F2FE)
                    )
                )
            )
    ) {
        FloatingBubbles()

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .width(560.dp)
                .shadow(24.dp, RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFFB7185), Color(0xFF8B5CF6))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎮", fontSize = 42.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Dang nhap terminal game",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Can tai khoan admin de quet the va tru tien theo gia game tren he thong.",
                    textAlign = TextAlign.Center,
                    color = Color(0xFF4B5563),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    singleLine = true,
                    label = { Text("So dien thoai admin") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    singleLine = true,
                    label = { Text("Mat khau") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Text(
                            text = errorMessage,
                            modifier = Modifier.padding(14.dp),
                            color = Color(0xFFB91C1C)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = ""

                            val result = adminSession.login(phoneNumber.trim(), password)
                            result
                                .onSuccess { onLoginSuccess() }
                                .onFailure { errorMessage = it.message ?: "Dang nhap that bai" }

                            isLoading = false
                        }
                    },
                    enabled = !isLoading && phoneNumber.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C3AED),
                        disabledContainerColor = Color(0xFFD1D5DB)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text("Mo che do quet the", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GamePlayScreen(
    smartCardManager: SmartCardManager,
    game: GameDto,
    onComplete: () -> Unit,
    onSessionExpired: () -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var cardIdFromCard by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("Dang cho quet the...") }
    val scope = rememberCoroutineScope()
    val gameApiClient = remember { GameApiClient() }

    fun finish(sessionExpired: Boolean) {
        scope.launch {
            delay(4200)
            smartCardManager.disconnect()
            if (sessionExpired) {
                onSessionExpired()
            } else {
                onComplete()
            }
        }
    }

    fun processCard() {
        scope.launch {
            isProcessing = true
            var sessionExpired = false
            customerName = ""
            cardIdFromCard = ""

            try {
                statusMessage = "Dang ket noi va xac thuc Admin PIN..."
                val connectResult = withContext(Dispatchers.IO) {
                    smartCardManager.connectAndVerifyAdminPINEncrypted(adminPin = "9999")
                }
                if (connectResult.isFailure) {
                    throw IllegalStateException(
                        connectResult.exceptionOrNull()?.message ?: "Khong ket noi/xac thuc duoc the"
                    )
                }

                statusMessage = "Dang doc CardID tu the..."
                val cardInfo = withContext(Dispatchers.IO) {
                    smartCardManager.readCustomerInfo()
                }
                customerName = cardInfo["name"].orEmpty()
                val detectedCardId = cardInfo["cardUUID"]?.trim()
                    ?: throw IllegalStateException("Khong doc duoc CardID tu the")
                if (detectedCardId.isBlank()) {
                    throw IllegalStateException("CardID tren the dang rong")
                }

                cardIdFromCard = detectedCardId

                statusMessage = "Dang tru ${formatMoney(game.ticketPrice)} tren he thong..."
                val playResult = withContext(Dispatchers.IO) {
                    gameApiClient.playGame(gameId = game.gameId, cardId = detectedCardId)
                }

                playResult
                    .onSuccess { response ->
                        statusMessage = buildString {
                            append("Thanh cong!\n")
                            append("Game: ${game.gameName}\n")
                            append("Da tru: ${formatMoney(response.chargedAmount ?: game.ticketPrice)}\n")
                            append("So du con lai: ${formatMoney(response.balanceAfter)}\n")
                            append("Thong bao da duoc gui cho user.")
                        }
                    }
                    .onFailure { error ->
                        val message = error.message ?: "Khong the xu ly luot choi"
                        sessionExpired = message.contains("Unauthorized", ignoreCase = true) ||
                            message.contains("Forbidden", ignoreCase = true) ||
                            message.contains("token", ignoreCase = true)

                        statusMessage = buildString {
                            append("Khong the choi game.\n")
                            append(message)
                        }
                    }
            } catch (e: Exception) {
                statusMessage = "Co loi khi quet the.\n${e.message ?: "Unknown error"}"
            } finally {
                isProcessing = false
                finish(sessionExpired)
            }
        }
    }

    LaunchedEffect(game.gameId) {
        processCard()
    }

    val colors = pickGameColors(game.gameName)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF3E0),
                        Color(0xFFFFE4EC),
                        Color(0xFFE0F2FE)
                    )
                )
            )
    ) {
        FloatingBubbles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .width(760.dp)
                    .shadow(24.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(brush = Brush.linearGradient(colors))
                        .padding(36.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = pickGameEmoji(game.gameName),
                                fontSize = 58.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = game.gameName,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.22f)
                            )
                        ) {
                            Text(
                                text = "${formatMoney(game.ticketPrice)} / luot",
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier
                    .width(760.dp)
                    .shadow(16.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        statusMessage.startsWith("Thanh cong!") -> Color(0xFFE8F5E9)
                        statusMessage.startsWith("Khong the") || statusMessage.startsWith("Co loi") -> Color(0xFFFFEBEE)
                        else -> Color(0xFFFFF8E1)
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(60.dp),
                            color = Color(0xFF7C3AED),
                            strokeWidth = 5.dp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    Text(
                        text = statusMessage,
                        fontSize = 24.sp,
                        lineHeight = 34.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )

                    if (customerName.isNotBlank() || cardIdFromCard.isNotBlank()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(20.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (customerName.isNotBlank()) {
                                InfoRow(label = "Khach hang", value = customerName)
                            }
                            if (cardIdFromCard.isNotBlank()) {
                                InfoRow(label = "CardID tren the", value = cardIdFromCard)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF6B7280),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            color = Color(0xFF111827),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SmartCard Park - Game Play"
    ) {
        MaterialTheme {
            GamePlayApp(initialGame = null)
        }
    }
}
