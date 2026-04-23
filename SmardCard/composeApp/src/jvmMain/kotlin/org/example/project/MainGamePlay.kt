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
import androidx.compose.material3.OutlinedButton
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
import org.example.project.data.repository.PendingGamePlay
import org.example.project.data.repository.PendingGamePlayRepository
import org.example.project.data.repository.isAuthError
import org.example.project.data.repository.isLikelyNetworkError
import org.example.project.model.GameDto
import org.example.project.network.GameApiClient
import org.example.project.network.RSAApiClient
import org.example.project.screen.FloatingBubbles
import org.example.project.screen.GameSelectionScreen
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Base64
import java.util.UUID

private enum class GamePlayAppScreen {
    LOGIN,
    SELECTION,
    PLAYING
}

private enum class StatusTone {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

private data class OnlinePlayContext(
    val latestGame: GameDto,
    val chargedAmount: Int,
    val customerName: String?
)

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
        else -> listOf(Color(0xFF0F766E), Color(0xFF2563EB))
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

private fun formatMoney(amount: Int?): String = formatMoney(amount?.toString())

private fun formatPlayedAt(raw: String?): String {
    if (raw.isNullOrBlank()) return "--"
    return raw.replace("T", " ").removeSuffix("Z")
}

private fun parseAmountToInt(raw: String?): Int? {
    if (raw.isNullOrBlank()) return null
    return runCatching { BigDecimal(raw.trim()).toInt() }.getOrNull()
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
            selectedGame?.let { game ->
                GamePlayScreen(
                    smartCardManager = smartCardManager,
                    game = game,
                    onBackToSelection = {
                        currentScreen = GamePlayAppScreen.SELECTION
                        selectedGame = null
                    },
                    onSessionExpired = {
                        TokenStore.clear()
                        currentScreen = GamePlayAppScreen.LOGIN
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
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFFB7185), Color(0xFF8B5CF6))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("TG", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Đăng nhập terminal game",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cần tài khoản admin để quẹt thẻ, trừ tiền trên thẻ và đồng bộ lượt chơi lên hệ thống.",
                    textAlign = TextAlign.Center,
                    color = Color(0xFF4B5563),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    singleLine = true,
                    label = { Text("Số điện thoại admin") },
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
                    label = { Text("Mật khẩu") },
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
                                .onFailure { errorMessage = it.message ?: "Đăng nhập thất bại" }

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
                        Text("Mở terminal quẹt thẻ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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

@Composable
private fun GamePlayScreen(
    smartCardManager: SmartCardManager,
    game: GameDto,
    onBackToSelection: () -> Unit,
    onSessionExpired: () -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var cardIdFromCard by remember { mutableStateOf("") }
    var lastSecurityStatus by remember { mutableStateOf<String?>(null) }
    var lastChargedAmount by remember { mutableStateOf<String?>(null) }
    var lastRemainingBalance by remember { mutableStateOf<String?>(null) }
    var lastSyncStatus by remember { mutableStateOf<String?>(null) }
    var lastPlayedAt by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("Sẵn sàng. Bấm 'Quẹt thẻ' để bắt đầu.") }
    var statusTone by remember { mutableStateOf(StatusTone.INFO) }
    var pendingCount by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()
    val gameApiClient = remember { GameApiClient() }
    val rsaApiClient = remember { RSAApiClient() }
    val pendingRepository = remember { PendingGamePlayRepository() }

    suspend fun refreshPendingCount() {
        pendingCount = withContext(Dispatchers.IO) { pendingRepository.count() }
    }

    suspend fun flushPendingQueue(): Result<Int> {
        val flushResult = withContext(Dispatchers.IO) {
            pendingRepository.flush { play ->
                gameApiClient.syncPlay(play.gameId, play.toSyncRequest())
            }
        }
        pendingCount = flushResult.remainingCount

        if (flushResult.failure != null) {
            val error = flushResult.failure
            val prefix = if (flushResult.syncedCount > 0) {
                "Đã đồng bộ ${flushResult.syncedCount} lượt chơi chờ. "
            } else {
                ""
            }
            val message = when {
                error.isAuthError() -> prefix + "Phiên đăng nhập không còn hợp lệ."
                error.isLikelyNetworkError() -> prefix + (error?.message ?: "Không kết nối được server.")
                else -> prefix + (error?.message ?: "Không đồng bộ được lượt chơi chờ.")
            }
            return Result.failure(Exception(message, error))
        }

        return Result.success(flushResult.syncedCount)
    }

    suspend fun verifyRsaOnline(cardId: String): Result<Boolean?> {
        val rsaReady = withContext(Dispatchers.IO) { smartCardManager.getRSAStatus() }
        if (!rsaReady) {
            return Result.failure(IllegalStateException("Thẻ này chưa có RSA hợp lệ để chơi online."))
        }

        val challengeResult = withContext(Dispatchers.IO) { rsaApiClient.getChallenge() }
        if (challengeResult.isFailure) {
            val error = challengeResult.exceptionOrNull()
            return when {
                error.isLikelyNetworkError() -> Result.success(null)
                error.isAuthError() -> Result.failure(Exception("Phiên đăng nhập không còn hợp lệ.", error))
                else -> Result.failure(Exception(error?.message ?: "Không thể lấy challenge RSA từ server.", error))
            }
        }

        val challengeDto = challengeResult.getOrThrow()
        val challengeBytes = try {
            Base64.getDecoder().decode(challengeDto.challenge)
        } catch (e: Exception) {
            return Result.failure(IllegalStateException("Challenge RSA từ server không hợp lệ."))
        }

        if (challengeBytes.size != 32) {
            return Result.failure(IllegalStateException("Challenge RSA từ server không hợp lệ."))
        }

        val signature = withContext(Dispatchers.IO) { smartCardManager.signChallenge(challengeBytes) }
            ?: return Result.failure(IllegalStateException("Thẻ không thể ký RSA cho chế độ online."))

        val verifyResult = withContext(Dispatchers.IO) {
            rsaApiClient.verifySignature(
                cardId = cardId,
                challenge = challengeDto.challenge,
                signatureBase64 = Base64.getEncoder().encodeToString(signature)
            )
        }
        if (verifyResult.isFailure) {
            val error = verifyResult.exceptionOrNull()
            return when {
                error.isLikelyNetworkError() -> Result.success(null)
                error.isAuthError() -> Result.failure(Exception("Phiên đăng nhập không còn hợp lệ.", error))
                else -> Result.failure(Exception(error?.message ?: "Không thể xác thực RSA với server.", error))
            }
        }

        val verifyResponse = verifyResult.getOrThrow()
        if (!verifyResponse.success) {
            return Result.failure(IllegalStateException("Xác thực RSA thất bại: ${verifyResponse.message}"))
        }

        return Result.success(true)
    }

    suspend fun resolveOnlineContext(cardId: String): Result<OnlinePlayContext?> {
        val cardLookup = withContext(Dispatchers.IO) { gameApiClient.lookupCard(cardId) }
        if (cardLookup.isFailure) {
            val error = cardLookup.exceptionOrNull()
            return when {
                error.isLikelyNetworkError() -> Result.success(null)
                error.isAuthError() -> Result.failure(Exception("Phiên đăng nhập không còn hợp lệ.", error))
                else -> Result.failure(Exception(error?.message ?: "Không kiểm tra được thẻ trên server.", error))
            }
        }

        val card = cardLookup.getOrThrow()
        if (!card.status.equals("ACTIVE", ignoreCase = true)) {
            return Result.failure(IllegalStateException("Thẻ hiện không hoạt động trên hệ thống."))
        }

        val userId = card.userId
            ?: return Result.failure(IllegalStateException("Thẻ chưa liên kết với tài khoản nào."))

        val latestGameResult = withContext(Dispatchers.IO) { gameApiClient.getGame(game.gameId) }
        if (latestGameResult.isFailure) {
            val error = latestGameResult.exceptionOrNull()
            return when {
                error.isLikelyNetworkError() -> Result.success(null)
                error.isAuthError() -> Result.failure(Exception("Phiên đăng nhập không còn hợp lệ.", error))
                else -> Result.failure(Exception(error?.message ?: "Không tải được thông tin game mới nhất.", error))
            }
        }

        val latestGame = latestGameResult.getOrThrow()
        if (!latestGame.isActive) {
            return Result.failure(IllegalStateException("Trò chơi này đang tạm dừng trên hệ thống."))
        }

        val customerResult = withContext(Dispatchers.IO) { gameApiClient.getCustomer(userId) }
        if (customerResult.isFailure) {
            val error = customerResult.exceptionOrNull()
            return when {
                error.isLikelyNetworkError() -> Result.success(null)
                error.isAuthError() -> Result.failure(Exception("Phiên đăng nhập không còn hợp lệ.", error))
                else -> Result.failure(Exception(error?.message ?: "Không tải được số dư mới nhất của khách.", error))
            }
        }

        val customer = customerResult.getOrThrow()
        val chargedAmount = parseAmountToInt(latestGame.ticketPrice)
            ?: return Result.failure(IllegalStateException("Giá game không hợp lệ."))
        if (chargedAmount <= 0) {
            return Result.failure(IllegalStateException("Giá game phải lớn hơn 0."))
        }

        val serverBalance = parseAmountToInt(customer.currentBalance)
            ?: return Result.failure(IllegalStateException("Số dư server không hợp lệ."))
        if (!withContext(Dispatchers.IO) { smartCardManager.setBalance(serverBalance) }) {
            return Result.failure(IllegalStateException("Không đồng bộ được số dư mới nhất xuống thẻ."))
        }

        return Result.success(
            OnlinePlayContext(
                latestGame = latestGame,
                chargedAmount = chargedAmount,
                customerName = customer.fullName
            )
        )
    }

    suspend fun enqueuePendingPlay(play: PendingGamePlay) {
        withContext(Dispatchers.IO) { pendingRepository.enqueue(play) }
        refreshPendingCount()
    }

    suspend fun flushPendingSilently() {
        val result = flushPendingQueue()
        if (result.isSuccess) {
            val syncedCount = result.getOrNull() ?: 0
            if (syncedCount > 0 && !isProcessing && statusTone != StatusTone.SUCCESS) {
                statusTone = StatusTone.INFO
                statusMessage = "Đã đồng bộ $syncedCount lượt chơi chờ."
            }
        } else if (!isProcessing && result.exceptionOrNull().isAuthError()) {
            statusTone = StatusTone.WARNING
            statusMessage = if (pendingCount > 0) {
                "Còn $pendingCount lượt chơi đang chờ đồng bộ. Vui lòng đăng nhập lại để tiếp tục."
            } else {
                "Phiên đăng nhập không còn hợp lệ. Vui lòng đăng nhập lại."
            }
        }
    }

    fun processCard() {
        scope.launch {
            isProcessing = true
            var shouldForceLogin = false

            customerName = ""
            cardIdFromCard = ""
            lastSecurityStatus = null
            lastChargedAmount = null
            lastRemainingBalance = null
            lastSyncStatus = null
            lastPlayedAt = null

            try {
                statusTone = StatusTone.INFO
                statusMessage = "Đang kết nối và xác thực Admin PIN..."
                val connectResult = withContext(Dispatchers.IO) {
                    smartCardManager.connectAndVerifyAdminPINEncrypted(adminPin = "9999")
                }
                if (connectResult.isFailure) {
                    throw IllegalStateException(
                        connectResult.exceptionOrNull()?.message ?: "Không kết nối/xác thực được thẻ."
                    )
                }

                statusMessage = "Đang đọc thông tin thẻ..."
                val cardInfo = withContext(Dispatchers.IO) { smartCardManager.readCustomerInfo() }
                customerName = cardInfo["name"].orEmpty()

                val detectedCardId = cardInfo["cardUUID"]?.trim().orEmpty()
                if (detectedCardId.isBlank()) {
                    throw IllegalStateException("Không đọc được cardId trên thẻ.")
                }
                cardIdFromCard = detectedCardId

                var onlineContext: OnlinePlayContext? = null

                statusMessage = "Đang đồng bộ lượt chơi chờ trước khi quẹt..."
                val flushResult = flushPendingQueue()
                val canUseOnlinePath = when {
                    flushResult.isSuccess -> true
                    flushResult.exceptionOrNull().isLikelyNetworkError() -> false
                    flushResult.exceptionOrNull().isAuthError() -> {
                        shouldForceLogin = true
                        throw IllegalStateException(flushResult.exceptionOrNull()?.message ?: "Phiên đăng nhập không còn hợp lệ.")
                    }
                    else -> throw IllegalStateException(flushResult.exceptionOrNull()?.message ?: "Không đồng bộ được lượt chơi chờ.")
                }

                if (canUseOnlinePath) {
                    val syncedCount = flushResult.getOrNull() ?: 0
                    statusMessage = if (syncedCount > 0) {
                        "Đã đồng bộ $syncedCount lượt chơi chờ. Đang xác thực RSA..."
                    } else {
                        "Đang xác thực RSA trước khi chơi..."
                    }

                    val rsaResult = verifyRsaOnline(detectedCardId)
                    if (rsaResult.isFailure) {
                        val error = rsaResult.exceptionOrNull()
                        if (error.isAuthError()) {
                            shouldForceLogin = true
                        }
                        throw IllegalStateException(error?.message ?: "Không thể xác thực RSA với server.")
                    }

                    val rsaVerified = rsaResult.getOrNull()
                    if (rsaVerified == null) {
                        lastSecurityStatus = "Offline - bỏ qua RSA"
                        statusTone = StatusTone.WARNING
                        statusMessage = "Mất mạng khi xác thực RSA. Chuyển sang offline-path và dùng số dư trên thẻ."
                    } else {
                        lastSecurityStatus = "RSA da xac thuc"
                        statusMessage = "RSA hợp lệ. Đang kiểm tra thẻ và trò chơi trên server..."
                    }

                    if (rsaVerified == null) {
                        onlineContext = null
                    } else {
                    val onlineResult = resolveOnlineContext(detectedCardId)
                    if (onlineResult.isFailure) {
                        val error = onlineResult.exceptionOrNull()
                        if (error.isAuthError()) {
                            shouldForceLogin = true
                        }
                        throw IllegalStateException(error?.message ?: "Không kiểm tra được thẻ trên server.")
                    }
                    onlineContext = onlineResult.getOrNull()
                    }
                }

                if (onlineContext?.customerName?.isNotBlank() == true && customerName.isBlank()) {
                    customerName = onlineContext.customerName.orEmpty()
                }

                if (onlineContext == null && lastSecurityStatus == null) {
                    lastSecurityStatus = "Offline - bỏ qua RSA"
                }

                statusTone = if (onlineContext == null) StatusTone.WARNING else StatusTone.INFO
                statusMessage = if (onlineContext == null) {
                    if (pendingCount > 0) {
                        "Offline - bỏ qua RSA. Sẽ trừ tiền bằng số dư trên thẻ và lưu chờ đồng bộ sau."
                    } else {
                        "Offline - bỏ qua RSA. Sẽ xử lý bằng số dư hiện có trên thẻ."
                    }
                } else {
                    "Đã đồng bộ số dư server xuống thẻ. Đang kiểm tra số dư hiện tại..."
                }

                val chargedAmount = onlineContext?.chargedAmount
                    ?: parseAmountToInt(game.ticketPrice)
                    ?: throw IllegalStateException("Giá game không hợp lệ.")
                if (chargedAmount <= 0) {
                    throw IllegalStateException("Giá game phải lớn hơn 0.")
                }

                val balanceBefore = withContext(Dispatchers.IO) { smartCardManager.checkBalance() }
                if (balanceBefore < 0) {
                    throw IllegalStateException("Không đọc được số dư hiện tại trên thẻ.")
                }
                if (balanceBefore < chargedAmount) {
                    lastChargedAmount = formatMoney(chargedAmount)
                    lastRemainingBalance = formatMoney(balanceBefore)
                    lastSyncStatus = "Không đủ số dư để trừ tiền"
                    statusTone = StatusTone.ERROR
                    throw IllegalStateException("Số dư trên thẻ không đủ để chơi ${onlineContext?.latestGame?.gameName ?: game.gameName}.")
                }

                statusTone = StatusTone.INFO
                statusMessage = "Đang trừ ${formatMoney(chargedAmount)} trên thẻ..."
                val remainingBalance = withContext(Dispatchers.IO) {
                    smartCardManager.deductBalance(chargedAmount)
                } ?: throw IllegalStateException("Không trừ được tiền trên thẻ.")

                val pendingPlay = PendingGamePlay(
                    clientTransactionId = UUID.randomUUID().toString(),
                    gameId = game.gameId,
                    cardId = detectedCardId,
                    chargedAmount = chargedAmount.toString(),
                    cardBalanceAfter = remainingBalance.toString(),
                    playedAt = java.time.Instant.now().toString()
                )

                lastChargedAmount = formatMoney(chargedAmount)
                lastRemainingBalance = formatMoney(remainingBalance)
                lastPlayedAt = formatPlayedAt(pendingPlay.playedAt)

                if (onlineContext != null) {
                    statusMessage = "Đã trừ tiền trên thẻ. Đang đồng bộ lượt chơi lên server..."
                    val syncResult = withContext(Dispatchers.IO) {
                        gameApiClient.syncPlay(game.gameId, pendingPlay.toSyncRequest())
                    }

                    if (syncResult.isSuccess) {
                        val response = syncResult.getOrThrow()
                        statusTone = StatusTone.SUCCESS
                        statusMessage = "Thẻ hợp lệ - được chơi"
                        lastSecurityStatus = "RSA da xac thuc"
                        lastChargedAmount = formatMoney(response.chargedAmount ?: chargedAmount.toString())
                        lastRemainingBalance = formatMoney(
                            response.cardBalanceAfter ?: response.balanceAfter ?: remainingBalance.toString()
                        )
                        lastSyncStatus = "Đã đồng bộ lên server"
                        lastPlayedAt = formatPlayedAt(response.playedAt)
                    } else {
                        val error = syncResult.exceptionOrNull()
                        enqueuePendingPlay(pendingPlay)
                        statusTone = StatusTone.SUCCESS
                        statusMessage = "Thẻ hợp lệ - được chơi"
                        lastSyncStatus = when {
                            error.isAuthError() -> "Đang chờ đồng bộ - cần đăng nhập lại"
                            error.isLikelyNetworkError() -> "Đang chờ đồng bộ"
                            else -> "Đang chờ đồng bộ - sẽ thử lại sau"
                        }
                    }
                } else {
                    enqueuePendingPlay(pendingPlay)
                    statusTone = StatusTone.SUCCESS
                    statusMessage = "Thẻ hợp lệ - được chơi"
                    lastSecurityStatus = "Offline - bỏ qua RSA"
                    lastSyncStatus = "Đang chờ đồng bộ"
                }
            } catch (e: Exception) {
                if (statusTone != StatusTone.SUCCESS) {
                    statusTone = StatusTone.ERROR
                }
                statusMessage = e.message ?: "Có lỗi khi quẹt thẻ."
            } finally {
                withContext(Dispatchers.IO) { smartCardManager.disconnect() }
                isProcessing = false

                if (shouldForceLogin) {
                    delay(1200)
                    onSessionExpired()
                }
            }
        }
    }

    LaunchedEffect(game.gameId) {
        refreshPendingCount()
        flushPendingSilently()
    }

    LaunchedEffect(game.gameId) {
        while (true) {
            delay(15_000)
            flushPendingSilently()
        }
    }

    val colors = pickGameColors(game.gameName)
    val statusCardColor = when (statusTone) {
        StatusTone.SUCCESS -> Color(0xFFE8F5E9)
        StatusTone.WARNING -> Color(0xFFFFF8E1)
        StatusTone.ERROR -> Color(0xFFFFEBEE)
        StatusTone.INFO -> Color(0xFFF8FAFC)
    }

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
                    .width(860.dp)
                    .shadow(24.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(brush = Brush.linearGradient(colors))
                        .padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Terminal game",
                                    color = Color.White.copy(alpha = 0.92f),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Đã chọn game, màn hình này sẽ giữ nguyên để quẹt tiếp",
                                    color = Color.White.copy(alpha = 0.82f),
                                    fontSize = 13.sp
                                )
                            }

                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.18f)
                                )
                            ) {
                                Text(
                                    text = if (pendingCount > 0) {
                                        "$pendingCount lượt chờ đồng bộ"
                                    } else {
                                        "Không có lượt chờ"
                                    },
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = game.gameName.take(2).uppercase(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = game.gameName,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Chọn đúng trò chơi xong thì bấm Quẹt thẻ. Terminal sẽ trừ tiền trên thẻ trước, sau đó mới đồng bộ lên server.",
                            color = Color.White.copy(alpha = 0.92f),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                text = "${formatMoney(game.ticketPrice)} / lượt",
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { processCard() },
                                enabled = !isProcessing,
                                modifier = Modifier.height(54.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = colors.first()
                                )
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.5.dp,
                                        color = colors.first()
                                    )
                                } else {
                                    Text("Quẹt thẻ", fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = onBackToSelection,
                                enabled = !isProcessing,
                                modifier = Modifier.height(54.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("Chọn trò khác", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Card(
                modifier = Modifier
                    .width(860.dp)
                    .shadow(16.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = statusCardColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(56.dp),
                            color = Color(0xFF7C3AED),
                            strokeWidth = 5.dp
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    Text(
                        text = statusMessage,
                        fontSize = 24.sp,
                        lineHeight = 34.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (statusTone == StatusTone.SUCCESS) {
                            "Nếu banner hiện 'Thẻ hợp lệ - được chơi' thì người chơi có thể vào game ngay."
                        } else {
                            "Hệ thống sẽ ở lại màn hình này để nhận lần quẹt tiếp theo cho cùng trò chơi."
                        },
                        color = Color(0xFF4B5563),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    if (customerName.isNotBlank() || lastRemainingBalance != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(20.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (customerName.isNotBlank()) {
                                InfoRow(label = "Khách hàng", value = customerName)
                            }
                            if (lastRemainingBalance != null) {
                                InfoRow(label = "Số dư còn lại trên thẻ", value = lastRemainingBalance.orEmpty())
                            }
                        }
                    }
                }
            }
        }
    }
}
