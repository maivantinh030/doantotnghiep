package com.park.smartcard.screen.user

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit. sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.park.smartcard.SmartCardManager
import com.park.smartcard.screen.FloatingBubbles
import com.park.smartcard.network.GameApiClient
import com.park.smartcard.network.TransactionApiClient
import com.park.smartcard.model.GameDto
import com.park.smartcard.model.CreateTransactionRequest
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image as SkiaImage

// ✅ HÀM FORMAT SỐ TIỀN
//fun formatMoney(amount: Int): String {
//    return "${amount * 1000}". reversed().chunked(3).joinToString(",").reversed()
//}
//
//data class GameTicket(
//    val gameCode: Int,
//    val name:  String,
//    val emoji: String,
//    val price: Int,  // Đơn vị:  nghìn đồng
//    val gradientColors: List<Color>,
//    var quantity: Int = 0,
//    val image: ImageBitmap? = null
//)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun UserBuyTicketsScreen(
//    smartCardManager: SmartCardManager,
//    onBack: () -> Unit
//) {
//    var balance by remember { mutableStateOf(0) }
//    var isLoading by remember { mutableStateOf(false) }
//    var status by remember { mutableStateOf("") }
//    var showConfirmDialog by remember { mutableStateOf(false) }
//
//    var cart by remember { mutableStateOf<List<GameTicket>>(emptyList()) }
//    val gameApiClient = remember { GameApiClient() }
//
//    val scope = rememberCoroutineScope()
//
//    fun loadBalance() {
//        scope.launch {
//            val bal = smartCardManager.checkBalance()
//            balance = if (bal >= 0) bal else 0
//        }
//    }
//
//    fun pickEmoji(name: String): String {
//        val lower = name.lowercase()
//        return when {
//            listOf("tàu", "roller", "coaster").any { lower.contains(it) } -> "🎢"
//            listOf("đu quay", "wheel", "ferris").any { lower.contains(it) } -> "🎡"
//            listOf("đua", "race", "xe").any { lower.contains(it) } -> "🏎️"
//            listOf("nhảy", "jump", "vr").any { lower.contains(it) } -> "🕶️"
//            listOf("bóng", "basket", "rổ").any { lower.contains(it) } -> "🏀"
//            listOf("cá", "fishing").any { lower.contains(it) } -> "🎣"
//            listOf("ma", "ghost").any { lower.contains(it) } -> "👻"
//            else -> "🎮"
//        }
//    }
//
//    fun gradientFor(code: Int): List<Color> {
//        return when (code % 6) {
//            0 -> listOf(Color(0xFFFF6B6B), Color(0xFFFF8E8E))
//            1 -> listOf(Color(0xFF4ECDC4), Color(0xFF6EE5DB))
//            2 -> listOf(Color(0xFFFFBE0B), Color(0xFFFFD60A))
//            3 -> listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA))
//            4 -> listOf(Color(0xFF3B82F6), Color(0xFF60A5FA))
//            else -> listOf(Color(0xFFEC4899), Color(0xFFF472B6))
//        }
//    }
//
//    fun parsePriceThousands(ticketPrice: String): Int {
//        // Expect like "12.00" -> 12 (thousand VND units used by UI)
//        return ticketPrice.substringBefore('.')
//            .toIntOrNull()
//            ?: ticketPrice.toDoubleOrNull()?.toInt()
//            ?: 0
//    }
//
//    fun loadGames() {
//        scope.launch {
//            isLoading = true
//            status = ""
//            val result = gameApiClient.getAllGames()
//            result.onSuccess { list ->
//                val tickets = list.filter { it.isActive }.map { dto ->
//                    val bytes = gameApiClient.decodeImage(dto.gameImage)
//                    val imgBitmap = bytes?.let {
//                        try { SkiaImage.makeFromEncoded(it).toComposeImageBitmap() } catch (_: Exception) { null }
//                    }
//                    GameTicket(
//                        gameCode = dto.gameCode,
//                        name = dto.gameName,
//                        emoji = pickEmoji(dto.gameName),
//                        price = parsePriceThousands(dto.ticketPrice),
//                        gradientColors = gradientFor(dto.gameCode),
//                        quantity = 0,
//                        image = imgBitmap
//                    )
//                }
//                cart = tickets
//                status = if (tickets.isEmpty()) "📭 Không có trò chơi hoạt động" else "✅ Đã tải ${tickets.size} trò chơi từ server"
//            }.onFailure { e ->
//                status = "❌ Lỗi tải game: ${e.message}"
//                cart = emptyList()
//            }
//            isLoading = false
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        loadBalance()
//        loadGames()
//    }
//
//    val totalTickets = cart.sumOf { it.quantity }
//    val totalAmount = cart.sumOf { it.quantity * it.price }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                brush = Brush.verticalGradient(
//                    colors = listOf(
//                        Color(0xFFFFF3E0),
//                        Color(0xFFFFE0F0),
//                        Color(0xFFE0F7FA)
//                    )
//                )
//            )
//    ) {
//        FloatingBubbles()
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(20.dp)
//        ) {
//            // HEADER
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .shadow(20.dp, RoundedCornerShape(32.dp)),
//                shape = RoundedCornerShape(32.dp),
//                colors = CardDefaults.cardColors(containerColor = Color. Transparent)
//            ) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .background(
//                            brush = Brush.linearGradient(
//                                colors = listOf(
//                                    Color(0xFFFF6B9D),
//                                    Color(0xFFC06CD5),
//                                    Color(0xFF6E8EFB)
//                                )
//                            )
//                        )
//                        .padding(24.dp)
//                ) {
//                    Row(
//                        modifier = Modifier. fillMaxWidth(),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        IconButton(
//                            onClick = onBack,
//                            modifier = Modifier
//                                .size(52.dp)
//                                .clip(CircleShape)
//                                .background(Color.White. copy(alpha = 0.3f))
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.ArrowBack,
//                                contentDescription = "Back",
//                                tint = Color.White,
//                                modifier = Modifier. size(28.dp)
//                            )
//                        }
//
//                        Spacer(modifier = Modifier.width(16.dp))
//
//                        Column(modifier = Modifier.weight(1f)) {
//                            Text(
//                                text = "🎟️ Mua Lượt Chơi",
//                                fontSize = 26.sp,
//                                fontWeight = FontWeight.ExtraBold,
//                                color = Color.White
//                            )
//                            Spacer(modifier = Modifier. height(6.dp))
//                            Card(
//                                shape = RoundedCornerShape(16.dp),
//                                colors = CardDefaults.cardColors(
//                                    containerColor = Color. White. copy(alpha = 0.25f)
//                                )
//                            ) {
//                                Row(
//                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Text("💰", fontSize = 18.sp)
//                                    Spacer(modifier = Modifier. width(6.dp))
//                                    Text(
//                                        text = "${formatMoney(balance)} VNĐ",
//                                        fontSize = 16.sp,
//                                        fontWeight = FontWeight.Bold,
//                                        color = Color.White
//                                    )
//                                }
//                            }
//                        }
//
//                        Box(
//                            modifier = Modifier
//                                .size(70.dp)
//                                .clip(CircleShape)
//                                .background(Color.White.copy(alpha = 0.3f)),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text("🎫", fontSize = 36.sp)
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier. height(20.dp))
//
//            // GAME LIST
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//                    .shadow(16.dp, RoundedCornerShape(32.dp)),
//                shape = RoundedCornerShape(32.dp),
//                colors = CardDefaults.cardColors(containerColor = Color.White)
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(24.dp)
//                ) {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = "🎮 Chọn trò chơi",
//                            fontSize = 22.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color(0xFF333333)
//                        )
//
//                        if (totalTickets > 0) {
//                            Card(
//                                shape = RoundedCornerShape(20.dp),
//                                colors = CardDefaults.cardColors(
//                                    containerColor = Color(0xFFFF6B9D).copy(alpha = 0.15f)
//                                )
//                            ) {
//                                Text(
//                                    text = "$totalTickets lượt",
//                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
//                                    fontSize = 14.sp,
//                                    fontWeight = FontWeight.Bold,
//                                    color = Color(0xFFFF6B9D)
//                                )
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier. height(16.dp))
//
//                    LazyColumn(
//                        verticalArrangement = Arrangement. spacedBy(12.dp),
//                        modifier = Modifier. weight(1f)
//                    ) {
//                        items(cart) { item ->
//                            GameTicketCard(
//                                game = item,
//                                onQuantityChange = { delta ->
//                                    cart = cart.map {
//                                        if (it. gameCode == item.gameCode) {
//                                            val newQty = (it.quantity + delta).coerceIn(0, 99)
//                                            it.copy(quantity = newQty)
//                                        } else it
//                                    }
//                                }
//                            )
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // SUMMARY
//            AnimatedVisibility(
//                visible = totalTickets > 0,
//                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
//                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
//            ) {
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .shadow(12.dp, RoundedCornerShape(28.dp)),
//                    shape = RoundedCornerShape(28.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color. Transparent)
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(
//                                brush = Brush.linearGradient(
//                                    colors = listOf(
//                                        Color(0xFF4CAF50),
//                                        Color(0xFF45B649),
//                                        Color(0xFF3FA142)
//                                    )
//                                )
//                            )
//                            .padding(20.dp)
//                    ) {
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Column {
//                                Text(
//                                    text = "Tổng cộng",
//                                    fontSize = 15.sp,
//                                    color = Color.White. copy(alpha = 0.9f)
//                                )
//                                Spacer(modifier = Modifier. height(4.dp))
//                                Text(
//                                    text = "$totalTickets lượt • ${formatMoney(totalAmount)} VNĐ",
//                                    fontSize = 22.sp,
//                                    fontWeight = FontWeight.ExtraBold,
//                                    color = Color.White
//                                )
//                            }
//
//                            Button(
//                                onClick = { if (totalTickets > 0) showConfirmDialog = true },
//                                enabled = totalTickets > 0 && balance >= totalAmount && !isLoading,
//                                shape = RoundedCornerShape(20.dp),
//                                colors = ButtonDefaults.buttonColors(
//                                    containerColor = Color. White,
//                                    contentColor = Color(0xFF4CAF50),
//                                    disabledContainerColor = Color. Gray
//                                ),
//                                modifier = Modifier. height(56.dp),
//                                elevation = ButtonDefaults.buttonElevation(
//                                    defaultElevation = 6.dp,
//                                    pressedElevation = 12.dp
//                                )
//                            ) {
//                                if (isLoading) {
//                                    CircularProgressIndicator(
//                                        modifier = Modifier.size(24.dp),
//                                        color = Color(0xFF4CAF50),
//                                        strokeWidth = 3.dp
//                                    )
//                                } else {
//                                    Row(
//                                        verticalAlignment = Alignment.CenterVertically,
//                                        horizontalArrangement = Arrangement. Center
//                                    ) {
//                                        Icon(
//                                            imageVector = Icons.Default.ShoppingCart,
//                                            contentDescription = null,
//                                            modifier = Modifier.size(24.dp)
//                                        )
//                                        Spacer(modifier = Modifier.width(8.dp))
//                                        Text(
//                                            text = "Thanh toán",
//                                            fontSize = 17.sp,
//                                            fontWeight = FontWeight.Bold
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            if (status.isNotEmpty()) {
//                Spacer(modifier = Modifier. height(12.dp))
//                Card(
//                    modifier = Modifier. fillMaxWidth(),
//                    shape = RoundedCornerShape(20.dp),
//                    colors = CardDefaults.cardColors(
//                        containerColor = when {
//                            status.startsWith("✅") -> Color(0xFFE8F5E9)
//                            status.startsWith("⚠️") -> Color(0xFFFFF8E1)
//                            else -> Color(0xFFFFEBEE)
//                        }
//                    ),
//                    elevation = CardDefaults.cardElevation(4.dp)
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = when {
//                                status.startsWith("✅") -> "✅"
//                                status.startsWith("⚠️") -> "⚠️"
//                                else -> "❌"
//                            },
//                            fontSize = 24.sp
//                        )
//                        Spacer(modifier = Modifier.width(12.dp))
//                        Text(
//                            text = status,
//                            fontSize = 14.sp,
//                            fontWeight = FontWeight.Medium,
//                            modifier = Modifier.weight(1f)
//                        )
//                    }
//                }
//            }
//        }
//
//        // CONFIRM DIALOG
//        if (showConfirmDialog) {
//            AlertDialog(
//                onDismissRequest = { showConfirmDialog = false },
//                shape = RoundedCornerShape(28.dp),
//                title = {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text("🛒", fontSize = 28.sp)
//                        Spacer(modifier = Modifier.width(12.dp))
//                        Text(
//                            "Xác nhận mua lượt",
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 20.sp
//                        )
//                    }
//                },
//                text = {
//                    Column {
//                        Text(
//                            "Bạn đang mua:",
//                            fontWeight = FontWeight.Medium,
//                            fontSize = 15.sp,
//                            color = Color.Gray
//                        )
//                        Spacer(modifier = Modifier. height(12.dp))
//
//                        cart.filter { it.quantity > 0 }.forEach { game ->
//                            Card(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(vertical = 4.dp),
//                                shape = RoundedCornerShape(12.dp),
//                                colors = CardDefaults.cardColors(
//                                    containerColor = game.gradientColors[0]. copy(alpha = 0.1f)
//                                )
//                            ) {
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(12.dp),
//                                    horizontalArrangement = Arrangement. SpaceBetween,
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Row(verticalAlignment = Alignment.CenterVertically) {
//                                        Text(game.emoji, fontSize = 24.sp)
//                                        Spacer(modifier = Modifier. width(8.dp))
//                                        Column {
//                                            Text(
//                                                game.name,
//                                                fontWeight = FontWeight.Bold,
//                                                fontSize = 15.sp
//                                            )
//                                            Text(
//                                                "${game.quantity} lượt × ${formatMoney(game.price)}",
//                                                fontSize = 13.sp,
//                                                color = Color.Gray
//                                            )
//                                        }
//                                    }
//                                    Text(
//                                        "${formatMoney(game.quantity * game. price)} đ",
//                                        fontWeight = FontWeight.Bold,
//                                        fontSize = 16.sp,
//                                        color = game.gradientColors[0]
//                                    )
//                                }
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(12.dp))
//                        HorizontalDivider()
//                        Spacer(modifier = Modifier.height(12.dp))
//
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(
//                                "Tổng thanh toán:",
//                                fontWeight = FontWeight.Bold,
//                                fontSize = 16.sp
//                            )
//                            Text(
//                                "${formatMoney(totalAmount)} VNĐ",
//                                fontWeight = FontWeight.ExtraBold,
//                                fontSize = 20.sp,
//                                color = Color(0xFF4CAF50)
//                            )
//                        }
//                    }
//                },
//                confirmButton = {
//                    Button(
//                        onClick = {
//                            showConfirmDialog = false
//                            isLoading = true
//                            status = "⏳ Đang xử lý thanh toán..."
//
//                            scope.launch {
//                                try {
//                                    println("💳 Thanh toán $totalAmount (${formatMoney(totalAmount)} VNĐ)...")
//                                    val paymentSuccess = smartCardManager.makePayment(totalAmount)
//
//                                    if (! paymentSuccess) {
//                                        status = "❌ Thanh toán thất bại!  Không đủ số dư."
//                                        isLoading = false
//                                        return@launch
//                                    }
//
//                                    println("✅ Thanh toán thành công!")
//                                    delay(500)
//
//                                    status = "📝 Đang ghi lượt lên thẻ..."
//                                    var successCount = 0
//                                    var failCount = 0
//
//                                    cart.filter { it.quantity > 0 }.forEach { game ->
//                                        println("🎫 Ghi ${game.quantity} lượt cho game ${game.name} (code: ${game.gameCode})...")
//
//                                        val ticketSuccess = smartCardManager.addOrIncreaseTickets(
//                                            game.gameCode,
//                                            game.quantity
//                                        )
//
//                                        if (ticketSuccess) {
//                                            successCount++
//                                            println("   ✅ Thành công!")
//                                        } else {
//                                            failCount++
//                                            println("   ❌ Thất bại!")
//                                        }
//
//                                        delay(200)
//                                    }
//
//                                    if (failCount == 0) {
//                                        status = "✅ Mua lượt thành công!  Đã thêm $totalTickets lượt vào thẻ."
//                                        cart = cart.map { it.copy(quantity = 0) }
//                                        loadBalance()
//                                    } else {
//                                        status = "⚠️ Thanh toán OK nhưng có $failCount lượt không ghi được.  Vui lòng liên hệ quản lý!"
//                                    }
//
//                                } catch (e: Exception) {
//                                    println("❌ Exception: ${e.message}")
//                                    e.printStackTrace()
//                                    status = "❌ Lỗi:  ${e.message}"
//                                } finally {
//                                    delay(3000)
//                                    isLoading = false
//                                }
//                            }
//                        },
//                        shape = RoundedCornerShape(16.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFF4CAF50)
//                        ),
//                        modifier = Modifier. height(50.dp)
//                    ) {
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Icon(Icons.Default.CheckCircle, contentDescription = null)
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text("Xác nhận", fontSize = 16.sp, fontWeight = FontWeight.Bold)
//                        }
//                    }
//                },
//                dismissButton = {
//                    TextButton(
//                        onClick = { showConfirmDialog = false },
//                        shape = RoundedCornerShape(16.dp)
//                    ) {
//                        Text("Hủy", fontSize = 16.sp)
//                    }
//                }
//            )
//        }
//    }
//}
//
//@Composable
//private fun GameTicketCard(
//    game: GameTicket,
//    onQuantityChange: (Int) -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(110.dp),
//        shape = RoundedCornerShape(20.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults. cardElevation(
//            defaultElevation = if (game.quantity > 0) 8.dp else 4.dp
//        )
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(
//                    brush = Brush.horizontalGradient(game.gradientColors)
//                )
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(16.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier. weight(1f)
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(70.dp)
//                            .clip(CircleShape)
//                            .background(Color.White. copy(alpha = 0.3f))
//                            .shadow(4.dp, CircleShape),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        if (game.image != null) {
//                            Image(
//                                bitmap = game.image,
//                                contentDescription = "Game image",
//                                modifier = Modifier.size(56.dp).clip(CircleShape)
//                            )
//                        } else {
//                            Text(text = game.emoji, fontSize = 36.sp)
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.width(16.dp))
//
//                    Column {
//                        Text(
//                            text = game.name,
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.White
//                        )
//                        Spacer(modifier = Modifier. height(4.dp))
//                        Text(
//                            text = "${formatMoney(game.price)} VNĐ/lượt",
//                            fontSize = 14.sp,
//                            fontWeight = FontWeight.Medium,
//                            color = Color.White. copy(alpha = 0.9f)
//                        )
//                    }
//                }
//
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement. spacedBy(12.dp),
//                    modifier = Modifier
//                        .clip(RoundedCornerShape(16.dp))
//                        .background(Color.White)
//                        .padding(horizontal = 8.dp, vertical = 8.dp)
//                ) {
//                    IconButton(
//                        onClick = { onQuantityChange(-1) },
//                        modifier = Modifier
//                            .size(36.dp)
//                            .clip(CircleShape)
//                            .background(game.gradientColors[0].copy(alpha = 0.15f))
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Remove,
//                            contentDescription = "Giảm",
//                            tint = game.gradientColors[0],
//                            modifier = Modifier. size(20.dp)
//                        )
//                    }
//
//                    Text(
//                        text = "${game.quantity}",
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = game.gradientColors[0],
//                        modifier = Modifier. widthIn(min = 30.dp),
//                        textAlign = TextAlign.Center
//                    )
//
//                    IconButton(
//                        onClick = { onQuantityChange(1) },
//                        modifier = Modifier
//                            .size(36.dp)
//                            .clip(CircleShape)
//                            .background(game.gradientColors[0].copy(alpha = 0.15f))
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Add,
//                            contentDescription = "Tăng",
//                            tint = game.gradientColors[0],
//                            modifier = Modifier.size(20.dp)
//                        )
//                    }
//                }
//            }
//
//            if (game.quantity > 0) {
//                Box(
//                    modifier = Modifier
//                        .align(Alignment.TopEnd)
//                        .padding(8.dp)
//                        .widthIn(min = 32.dp)
//                        .height(32.dp)
//                        .clip(RoundedCornerShape(16.dp))
//                        .background(Color.White)
//                        .shadow(4.dp, RoundedCornerShape(16.dp))
//                        .padding(horizontal = 8.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = "${game.quantity}",
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = game.gradientColors[0]
//                    )
//                }
//            }
//        }
//    }
//}

fun formatMoney(amount: Int): String {
    return "${amount * 1000}".reversed().chunked(3).joinToString(",").reversed()
}

data class GameTicket(
    val gameCode: Int,
    val name: String,
    val emoji: String,
    val price:  Int,
    val gradientColors: List<Color>,
    var quantity: Int = 0,
    val image: ImageBitmap? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserBuyTicketsScreen(
    smartCardManager: SmartCardManager,
    onBack: () -> Unit
) {
    var balance by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    var cart by remember { mutableStateOf<List<GameTicket>>(emptyList()) }
    val gameApiClient = remember { GameApiClient() }
    val transactionApiClient = remember { TransactionApiClient() }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()  // ✅ THÊM

    fun loadBalance() {
        scope.launch {
            val bal = smartCardManager.checkBalance()
            balance = if (bal >= 0) bal else 0
        }
    }

    fun pickEmoji(name: String): String {
        val lower = name.lowercase()
        return when {
            listOf("tàu", "roller", "coaster").any { lower.contains(it) } -> "🎢"
            listOf("đu quay", "wheel", "ferris").any { lower.contains(it) } -> "🎡"
            listOf("đua", "race", "xe").any { lower.contains(it) } -> "🏎️"
            listOf("nhảy", "jump", "vr").any { lower.contains(it) } -> "🕶️"
            listOf("bóng", "basket", "rổ").any { lower.contains(it) } -> "🏀"
            listOf("cá", "fishing").any { lower.contains(it) } -> "🎣"
            listOf("ma", "ghost").any { lower.contains(it) } -> "👻"
            else -> "🎮"
        }
    }

    fun gradientFor(code: Int): List<Color> {
        return when (code % 6) {
            0 -> listOf(Color(0xFFFF6B6B), Color(0xFFFF8E8E))
            1 -> listOf(Color(0xFF4ECDC4), Color(0xFF6EE5DB))
            2 -> listOf(Color(0xFFFFBE0B), Color(0xFFFFD60A))
            3 -> listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA))
            4 -> listOf(Color(0xFF3B82F6), Color(0xFF60A5FA))
            else -> listOf(Color(0xFFEC4899), Color(0xFFF472B6))
        }
    }

    fun parsePriceThousands(ticketPrice: String): Int {
        return ticketPrice.substringBefore('.')
            .toIntOrNull()
            ?: ticketPrice.toDoubleOrNull()?.toInt()
            ?: 0
    }

    fun loadGames() {
        scope.launch {
            isLoading = true
            status = ""
            val result = gameApiClient.getAllGames()
            result.onSuccess { list ->
                val tickets = list.filter { it.isActive }. map { dto ->
                    val bytes = gameApiClient.decodeImage(dto.gameImage)
                    val imgBitmap = bytes?.let {
                        try { SkiaImage.makeFromEncoded(it).toComposeImageBitmap() } catch (_: Exception) { null }
                    }
                    GameTicket(
                        gameCode = dto.gameCode,
                        name = dto.gameName,
                        emoji = pickEmoji(dto.gameName),
                        price = parsePriceThousands(dto.ticketPrice),
                        gradientColors = gradientFor(dto. gameCode),
                        quantity = 0,
                        image = imgBitmap
                    )
                }
                cart = tickets
                status = if (tickets.isEmpty()) "📭 Không có trò chơi hoạt động" else "✅ Đã tải ${tickets.size} trò chơi từ server"
            }.onFailure { e ->
                status = "❌ Lỗi tải game: ${e.message}"
                cart = emptyList()
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadBalance()
        loadGames()
    }

    val totalTickets = cart.sumOf { it.quantity }
    val totalAmount = cart.sumOf { it. quantity * it.price }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFE5EC),  // ✅ GIỐNG
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
                .verticalScroll(scrollState)  // ✅ THÊM scroll toàn màn
                .padding(horizontal = 80.dp, vertical = 20.dp)  // ✅ GIỐNG
        ) {
            // ✅ HEADER
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
                                    Color(0xFFFF6B9D),  // ✅ GIỐNG
                                    Color(0xFFC06FBB),
                                    Color(0xFFFEC163)
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
                                .background(Color.White. copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier. size(26.dp)  // ✅ GIỐNG
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🎟️ Mua Lượt Chơi",
                                fontSize = 22.sp,  // ✅ GIỐNG
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier. height(6.dp))
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
                                    Text("💰", fontSize = 18.sp)  // ✅ GIỐNG
                                    Spacer(modifier = Modifier. width(6.dp))
                                    Text(
                                        text = "${formatMoney(balance)} VNĐ",
                                        fontSize = 14.sp,  // ✅ GIỐNG
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(60.dp)  // ✅ GIỐNG
                                . clip(CircleShape)
                                .background(Color.White. copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎫", fontSize = 32.sp)  // ✅ GIỐNG
                        }
                    }
                }
            }

            Spacer(modifier = Modifier. height(20.dp))  // ✅ GIỐNG

            // ✅ GAME LIST CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    . shadow(12.dp, RoundedCornerShape(28.dp)),  // ✅ GIỐNG
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp)  // ✅ GIỐNG
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎮", fontSize = 24.sp)  // ✅ GIỐNG
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Chọn trò chơi",
                                fontSize = 20.sp,  // ✅ GIỐNG
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFF6B00)  // ✅ GIỐNG
                            )
                        }

                        if (totalTickets > 0) {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFF6B9D).copy(alpha = 0.15f)
                                )
                            ) {
                                Text(
                                    text = "$totalTickets lượt",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF6B9D)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier. size(52.dp),
                                color = Color(0xFFFF6B9D),
                                strokeWidth = 5.dp
                            )
                        }
                    } else if (cart.isEmpty()) {
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
                                Text("🎪", fontSize = 64.sp)
                                Spacer(modifier = Modifier. height(16.dp))
                                Text(
                                    text = "Chưa có trò chơi nào",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement. spacedBy(18.dp)
                        ) {
                            cart.forEach { item ->
                                GameTicketCard(
                                    game = item,
                                    onQuantityChange = { delta ->
                                        cart = cart.map {
                                            if (it. gameCode == item.gameCode) {
                                                val newQty = (it.quantity + delta).coerceIn(0, 99)
                                                it.copy(quantity = newQty)
                                            } else it
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ✅ SUMMARY
            if (totalTickets > 0) {
                Spacer(modifier = Modifier. height(20.dp))
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
                                        Color(0xFF4CAF50),
                                        Color(0xFF66BB6A)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement. SpaceBetween,
                            verticalAlignment = Alignment. CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Tổng cộng",
                                    fontSize = 14.sp,
                                    color = Color.White. copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier. height(4.dp))
                                Text(
                                    text = "$totalTickets lượt • ${formatMoney(totalAmount)} VNĐ",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = { if (totalTickets > 0) showConfirmDialog = true },
                                enabled = totalTickets > 0 && balance >= totalAmount && !isLoading,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color. White,
                                    contentColor = Color(0xFF4CAF50),
                                    disabledContainerColor = Color(0xFFE0E0E0)
                                ),
                                modifier = Modifier. height(56.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 6.dp,
                                    pressedElevation = 12.dp
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color(0xFF4CAF50),
                                        strokeWidth = 3.dp
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement. Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingCart,
                                            contentDescription = null,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Thanh toán",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ✅ STATUS
            // ✅ STATUS (ĐÃ SỬA - BỎ ICON TRONG TEXT)
            if (status.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults. cardColors(
                        containerColor = when {
                            status.startsWith("✅") || status.contains("thành công", ignoreCase = true) || status.contains("Đã tải", ignoreCase = true) -> Color(0xFFE8F5E9)
                            status.startsWith("📭") || status.contains("Không có", ignoreCase = true) -> Color(0xFFFFF3E0)
                            status.startsWith("⏳") || status.contains("Đang", ignoreCase = true) -> Color(0xFFE3F2FD)
                            status.startsWith("⚠️") -> Color(0xFFFFF3E0)
                            else -> Color(0xFFFFEBEE)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment. CenterVertically
                    ) {
                        Text(
                            text = when {
                                status.startsWith("✅") || status.contains("thành công", ignoreCase = true) || status.contains("Đã tải", ignoreCase = true) -> "✅"
                                status.startsWith("📭") || status.contains("Không có", ignoreCase = true) -> "📭"
                                status.startsWith("⏳") || status.contains("Đang", ignoreCase = true) -> "⏳"
                                status.startsWith("⚠️") -> "⚠️"
                                else -> "❌"
                            },
                            fontSize = 28.sp
                        )
                        Spacer(modifier = Modifier. width(14.dp))
                        Text(
                            text = status. removePrefix("✅ ").removePrefix("📭 ").removePrefix("⏳ ").removePrefix("⚠️ ").removePrefix("❌ "),  // ✅ BỎ EMOJI ĐẦU
                            fontSize = 16.sp,
                            fontWeight = FontWeight. Bold,
                            color = when {
                                status.startsWith("✅") || status.contains("thành công", ignoreCase = true) || status.contains("Đã tải", ignoreCase = true) -> Color(0xFF4CAF50)
                                status.startsWith("📭") || status.contains("Không có", ignoreCase = true) -> Color(0xFFFFA726)
                                status.startsWith("⏳") || status.contains("Đang", ignoreCase = true) -> Color(0xFF2196F3)
                                status.startsWith("⚠️") -> Color(0xFFFFA726)
                                else -> Color(0xFFE53935)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ✅ CONFIRM DIALOG (giữ nguyên logic)
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                shape = RoundedCornerShape(28.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🛒", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Xác nhận mua lượt",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            "Bạn đang mua:",
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier. height(12.dp))

                        cart.filter { it.quantity > 0 }.forEach { game ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = game.gradientColors[0]. copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement. SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(game.emoji, fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                game.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                "${game.quantity} lượt × ${formatMoney(game.price)}",
                                                fontSize = 13.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    Text(
                                        "${formatMoney(game.quantity * game.price)} đ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = game.gradientColors[0]
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Tổng thanh toán:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                "${formatMoney(totalAmount)} VNĐ",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDialog = false
                            isLoading = true
                            status = "⏳ Đang xử lý thanh toán..."

                            scope.launch {
                                try {
                                    println("💳 Thanh toán $totalAmount (${formatMoney(totalAmount)} VNĐ)...")
                                    val paymentSuccess = smartCardManager.makePayment(totalAmount)

                                    if (! paymentSuccess) {
                                        status = "❌ Thanh toán thất bại!  Không đủ số dư."
                                        isLoading = false
                                        return@launch
                                    }

                                    println("✅ Thanh toán thành công!")
                                    delay(500)

                                    status = "Đang ghi lượt lên thẻ..."
                                    var successCount = 0
                                    var failCount = 0
                                    
                                    // Lấy customerId từ thẻ
                                    val customerInfo = smartCardManager.readCustomerInfo()
                                    val customerId = customerInfo["customerID"] ?: ""

                                    cart.filter { it.quantity > 0 }.forEach { game ->
                                        println("🎫 Ghi ${game.quantity} lượt cho game ${game.name} (code: ${game.gameCode})...")

                                        val ticketSuccess = smartCardManager.addOrIncreaseTickets(
                                            game.gameCode,
                                            game.quantity
                                        )

                                        if (ticketSuccess) {
                                            successCount++
                                            println("   ✅ Thành công!")
                                            
                                            // Ghi lịch sử mua vé
                                            if (customerId.isNotBlank()) {
                                                val txnReq = CreateTransactionRequest(
                                                    customerId = customerId,
                                                    type = "PURCHASE",
                                                    amount = (game.quantity * game.price * 1000).toString(), // price is nghìn đồng → nhân 1000 để ghi VNĐ
                                                    gameCode = game.gameCode,
                                                    tickets = game.quantity,
                                                    balanceAfter = smartCardManager.checkBalance()
                                                )
                                                transactionApiClient.record(txnReq)
                                                    .onSuccess { println("📝 Đã ghi lịch sử mua vé game ${game.gameCode}") }
                                                    .onFailure { println("⚠️ Không ghi được lịch sử: ${it.message}") }
                                            }
                                        } else {
                                            failCount++
                                            println("   ❌ Thất bại!")
                                        }

                                        delay(200)
                                    }

                                    if (failCount == 0) {
                                        status = "✅ Mua lượt thành công!  Đã thêm $totalTickets lượt vào thẻ."
                                        cart = cart.map { it.copy(quantity = 0) }
                                        loadBalance()
                                    } else {
                                        status = "⚠️ Thanh toán OK nhưng có $failCount lượt không ghi được.  Vui lòng liên hệ quản lý!"
                                    }

                                } catch (e: Exception) {
                                    println("❌ Exception: ${e.message}")
                                    e.printStackTrace()
                                    status = "❌ Lỗi:  ${e.message}"
                                } finally {
                                    delay(3000)
                                    isLoading = false
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Xác nhận", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showConfirmDialog = false },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Hủy", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
private fun GameTicketCard(
    game: GameTicket,
    onQuantityChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = RoundedCornerShape(18.dp),  // ✅ GIỐNG InfoCard
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (game.quantity > 0) 6.dp else 4.dp  // ✅ GIỐNG
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(game.gradientColors)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color.White. copy(alpha = 0.3f))
                            .shadow(4.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (game.image != null) {
                            Image(
                                bitmap = game.image,
                                contentDescription = "Game image",
                                modifier = Modifier.size(56.dp).clip(CircleShape)
                            )
                        } else {
                            Text(text = game.emoji, fontSize = 36.sp)
                        }
                    }

                    Spacer(modifier = Modifier. width(16.dp))

                    Column {
                        Text(
                            text = game.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,  // ✅ ĐỔI
                            color = Color.White
                        )
                        Spacer(modifier = Modifier. height(4.dp))
                        Text(
                            text = "${formatMoney(game.price)} VNĐ/lượt",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,  // ✅ ĐỔI
                            color = Color.White. copy(alpha = 0.9f)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement. spacedBy(12.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    IconButton(
                        onClick = { onQuantityChange(-1) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(game.gradientColors[0].copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Giảm",
                            tint = game.gradientColors[0],
                            modifier = Modifier. size(20.dp)
                        )
                    }

                    Text(
                        text = "${game.quantity}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = game.gradientColors[0],
                        modifier = Modifier. widthIn(min = 30.dp),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = { onQuantityChange(1) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(game.gradientColors[0].copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tăng",
                            tint = game.gradientColors[0],
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (game.quantity > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .widthIn(min = 32.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${game.quantity}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = game.gradientColors[0]
                    )
                }
            }
        }
    }
}