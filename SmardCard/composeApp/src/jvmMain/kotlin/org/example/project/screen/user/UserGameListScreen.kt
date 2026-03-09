package org.example.project.screen.user

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.GameEntry
import org.example.project.SmartCardManager
import org.example.project.screen.FloatingBubbles
import org.example.project.network.GameApiClient
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image as SkiaImage

// Server metadata for game (name + decoded image)
//data class ServerGameInfo(
//    val name: String?,
//    val image: ImageBitmap?
//)
//
//// ✅ Map tên game từ gameCode
//fun getGameName(gameCode: Int): String {
//    return when (gameCode) {
//        1001 -> "Tàu Lượn"
//        1002 -> "Đu Quay"
//        1003 -> "Nhà Phao"
//        1004 -> "Tàu Cướp Biển"
//        1005 -> "Bể Bơi"
//        1006 -> "Con Lắc 360°"
//        1007 -> "Nhà Ma"
//        1008 -> "Đua Xe"
//        else -> "Game #$gameCode"
//    }
//}
//
//fun getGameEmoji(gameCode: Int): String {
//    return when (gameCode) {
//        1001 -> "🎢"
//        1002 -> "🎡"
//        1003 -> "🏰"
//        1004 -> "🏴‍☠️"
//        1005 -> "🏊"
//        1006 -> "🎪"
//        1007 -> "👻"
//        1008 -> "🏎️"
//        else -> "🎮"
//    }
//}
//
//fun getGameColors(gameCode: Int): List<Color> {
//    return when (gameCode) {
//        1001 -> listOf(Color(0xFFFF6B6B), Color(0xFFFF8E8E))
//        1002 -> listOf(Color(0xFF4ECDC4), Color(0xFF6EE5DB))
//        1003 -> listOf(Color(0xFFFFBE0B), Color(0xFFFFD60A))
//        1004 -> listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA))
//        1005 -> listOf(Color(0xFF3B82F6), Color(0xFF60A5FA))
//        1006 -> listOf(Color(0xFFEC4899), Color(0xFFF472B6))
//        1007 -> listOf(Color(0xFF6366F1), Color(0xFF818CF8))
//        1008 -> listOf(Color(0xFFEF4444), Color(0xFFF87171))
//        else -> listOf(Color(0xFF9E9E9E), Color(0xFFBDBDBD))
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun UserGameListScreen(
//    smartCardManager: SmartCardManager,
//    onBack: () -> Unit
//) {
//    var games by remember { mutableStateOf<List<GameEntry>>(emptyList()) }
//    var serverGames by remember { mutableStateOf<Map<Int, ServerGameInfo>>(emptyMap()) }
//    var isLoading by remember { mutableStateOf(false) }
//    var status by remember { mutableStateOf("") }
//
//    val scope = rememberCoroutineScope()
//    val gameApiClient = remember { GameApiClient() }
//
//    fun loadGames() {
//        scope.launch {
//            isLoading = true
//            status = "⏳ Đang đọc lượt chơi..."
//            try {
//                println("🔍 Đang đọc games từ thẻ...")
//                val gameList = smartCardManager.readGames()
//                println("📊 Đọc được ${gameList.size} games từ thẻ")
//
//                gameList.forEach { game ->
//                    println("   - Game ${game.gameCode}: ${game.tickets} lượt")
//                }
//
//                // Load server games for metadata (name + image)
//                val serverResult = gameApiClient.getAllGames()
//                serverResult.onSuccess { list ->
//                    serverGames = list.associateBy(
//                        { it.gameCode },
//                        { dto ->
//                            val bytes = gameApiClient.decodeImage(dto.gameImage)
//                            val img = bytes?.let { b ->
//                                try { SkiaImage.makeFromEncoded(b).toComposeImageBitmap() } catch (_: Exception) { null }
//                            }
//                            ServerGameInfo(dto.gameName, img)
//                        }
//                    )
//                }.onFailure { e ->
//                    println("⚠️ Không tải được danh sách game server: ${e.message}")
//                }
//
//                games = gameList.filter { it.tickets > 0 }
//
//                status = if (games.isEmpty()) {
//                    "⚠️ Bạn chưa có lượt chơi nào"
//                } else {
//                    "✅ Đã tải ${games.size} loại lượt"
//                }
//            } catch (e: Exception) {
//                println("❌ Lỗi đọc games: ${e.message}")
//                e.printStackTrace()
//                status = "❌ Lỗi:  ${e.message}"
//            } finally {
//                isLoading = false
//            }
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        loadGames()
//    }
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
//                                    Color(0xFF667EEA),
//                                    Color(0xFF764BA2),
//                                    Color(0xFFF093FB)
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
//                                text = "🎫 Lượt Chơi Của Tôi",
//                                fontSize = 26.sp,
//                                fontWeight = FontWeight.ExtraBold,
//                                color = Color.White
//                            )
//                            Spacer(modifier = Modifier. height(6.dp))
//                            Card(
//                                shape = RoundedCornerShape(16.dp),
//                                colors = CardDefaults.cardColors(
//                                    containerColor = Color. White.copy(alpha = 0.25f)
//                                )
//                            ) {
//                                Row(
//                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Text("🎮", fontSize = 18.sp)
//                                    Spacer(modifier = Modifier.width(6.dp))
//                                    Text(
//                                        text = "${games.sumOf { it.tickets }} lượt",
//                                        fontSize = 16.sp,
//                                        fontWeight = FontWeight.Bold,
//                                        color = Color.White
//                                    )
//                                }
//                            }
//                        }
//
//                        IconButton(
//                            onClick = { loadGames() },
//                            modifier = Modifier
//                                .size(56.dp)
//                                .clip(CircleShape)
//                                .background(Color.White.copy(alpha = 0.3f))
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default. Refresh,
//                                contentDescription = "Refresh",
//                                tint = Color.White,
//                                modifier = Modifier.size(28.dp)
//                            )
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
//                    . shadow(16.dp, RoundedCornerShape(32.dp)),
//                shape = RoundedCornerShape(32.dp),
//                colors = CardDefaults.cardColors(containerColor = Color.White)
//            ) {
//                if (isLoading) {
//                    Box(
//                        modifier = Modifier. fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                            CircularProgressIndicator(
//                                modifier = Modifier. size(48.dp),
//                                color = Color(0xFF667EEA),
//                                strokeWidth = 4.dp
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//                            Text("Đang tải lượt chơi.. .", color = Color.Gray)
//                        }
//                    }
//                } else if (games.isEmpty()) {
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment. Center
//                    ) {
//                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                            Text("🎫", fontSize = 64.sp)
//                            Spacer(modifier = Modifier.height(16.dp))
//                            Text(
//                                "Bạn chưa có lượt chơi nào",
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.Medium,
//                                color = Color. Gray
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            Text(
//                                "Hãy mua lượt để chơi game! ",
//                                fontSize = 14.sp,
//                                color = Color.Gray
//                            )
//                        }
//                    }
//                } else {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(24.dp)
//                    ) {
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(
//                                text = "🎮 Lượt đã mua",
//                                fontSize = 22.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFF333333)
//                            )
//
//                            Card(
//                                shape = RoundedCornerShape(20.dp),
//                                colors = CardDefaults.cardColors(
//                                    containerColor = Color(0xFF667EEA).copy(alpha = 0.15f)
//                                )
//                            ) {
//                                Text(
//                                    text = "${games.size} loại",
//                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
//                                    fontSize = 14.sp,
//                                    fontWeight = FontWeight.Bold,
//                                    color = Color(0xFF667EEA)
//                                )
//                            }
//                        }
//
//                        Spacer(modifier = Modifier. height(16.dp))
//
//                        LazyColumn(
//                            verticalArrangement = Arrangement. spacedBy(12.dp),
//                            modifier = Modifier. weight(1f)
//                        ) {
//                            items(games) { game ->
//                                val meta = serverGames[game.gameCode]
//                                GameTicketItemCard(game, meta)
//                            }
//                        }
//                    }
//                }
//            }
//
//            if (status.isNotEmpty() && !isLoading) {
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
//    }
//}
//
//@Composable
//private fun GameTicketItemCard(game: GameEntry, meta: ServerGameInfo?) {
//    val colors = getGameColors(game. gameCode)
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(110.dp),
//        shape = RoundedCornerShape(20.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(6.dp)
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(
//                    brush = Brush.horizontalGradient(colors)
//                )
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(16.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                // BÊN TRÁI:  Icon + Tên
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(70.dp)
//                            .clip(CircleShape)
//                            .background(Color.White.copy(alpha = 0.3f))
//                            .shadow(4.dp, CircleShape),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        meta?.image?.let { img ->
//                            Image(
//                                bitmap = img,
//                                contentDescription = "Game image",
//                                modifier = Modifier.size(56.dp).clip(CircleShape)
//                            )
//                        } ?: Text(
//                            text = meta?.name?.let { getGameEmoji(game.gameCode) } ?: getGameEmoji(game.gameCode),
//                            fontSize = 36.sp
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.width(16.dp))
//
//                    Column {
//                        Text(
//                            text = meta?.name ?: getGameName(game. gameCode),
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.White
//                        )
//                        Spacer(modifier = Modifier. height(4.dp))
//                    }
//                }
//
//                // BÊN PHẢI:  Số lượt
//                Card(
//                    shape = RoundedCornerShape(16.dp),
//                    colors = CardDefaults.cardColors(
//                        containerColor = Color.White
//                    ),
//                    elevation = CardDefaults.cardElevation(4.dp)
//                ) {
//                    Column(
//                        modifier = Modifier. padding(16.dp),
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Text(
//                            text = "${game.tickets}",
//                            fontSize = 28.sp,
//                            fontWeight = FontWeight.ExtraBold,
//                            color = colors[0]
//                        )
//                        Text(
//                            text = "lượt",
//                            fontSize = 13.sp,
//                            color = Color.Gray
//                        )
//                    }
//                }
//            }
//        }
//    }
//}

data class ServerGameInfo(
    val name:  String?,
    val image: ImageBitmap?
)

fun getGameName(gameCode: Int): String {
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

fun getGameEmoji(gameCode: Int): String {
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

fun getGameColors(gameCode: Int): List<Color> {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserGameListScreen(
    smartCardManager: SmartCardManager,
    onBack: () -> Unit
) {
    var games by remember { mutableStateOf<List<GameEntry>>(emptyList()) }
    var serverGames by remember { mutableStateOf<Map<Int, ServerGameInfo>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()  // ✅ THÊM
    val gameApiClient = remember { GameApiClient() }

    fun loadGames() {
        scope.launch {
            isLoading = true
            status = "⏳ Đang đọc lượt chơi..."
            try {
                println("🔍 Đang đọc games từ thẻ...")
                val gameList = smartCardManager.readGames()
                println("📊 Đọc được ${gameList.size} games từ thẻ")

                gameList.forEach { game ->
                    println("   - Game ${game.gameCode}: ${game.tickets} lượt")
                }

                val serverResult = gameApiClient.getAllGames()
                serverResult.onSuccess { list ->
                    serverGames = list.associateBy(
                        { it.gameCode },
                        { dto ->
                            val bytes = gameApiClient.decodeImage(dto.gameImage)
                            val img = bytes?.let { b ->
                                try { SkiaImage.makeFromEncoded(b).toComposeImageBitmap() } catch (_: Exception) { null }
                            }
                            ServerGameInfo(dto.gameName, img)
                        }
                    )
                }.onFailure { e ->
                    println("⚠️ Không tải được danh sách game server: ${e.message}")
                }

                games = gameList. filter { it.tickets > 0 }

                status = if (games.isEmpty()) {
                    "Bạn chưa có lượt chơi nào"
                } else {
                    "Đã tải ${games. size} loại lượt"
                }
            } catch (e: Exception) {
                println("❌ Lỗi đọc games: ${e.message}")
                e.printStackTrace()
                status = "❌ Lỗi:  ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadGames()
    }

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
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(48.dp)  // ✅ GIỐNG
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons. Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier. size(26.dp)  // ✅ GIỐNG
                            )
                        }

                        Spacer(modifier = Modifier. width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🎫 Lượt Chơi Của Tôi",
                                fontSize = 22.sp,  // ✅ GIỐNG
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
                                    Text("🎮", fontSize = 18.sp)  // ✅ GIỐNG
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${games.sumOf { it.tickets }} lượt",
                                        fontSize = 14.sp,  // ✅ GIỐNG
                                        fontWeight = FontWeight.Bold,
                                        color = Color. White
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { loadGames() },
                            modifier = Modifier
                                .size(60.dp)  // ✅ GIỐNG
                                . clip(CircleShape)
                                .background(Color.White. copy(alpha = 0.3f))
                        ) {
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

            Spacer(modifier = Modifier. height(20.dp))  // ✅ GIỐNG

            // ✅ CONTENT CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(12.dp, RoundedCornerShape(28.dp)),  // ✅ GIỐNG
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier. size(52.dp),
                                color = Color(0xFFFF6B9D),
                                strokeWidth = 5.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Đang tải lượt chơi.. .", color = Color(0xFF666666))
                        }
                    }
                } else if (games.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier. fillMaxWidth(0.8f),
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
                                Text("🎫", fontSize = 64.sp)
                                Spacer(modifier = Modifier. height(16.dp))
                                Text(
                                    "Bạn chưa có lượt chơi nào",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF666666)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Hãy mua lượt để chơi game!",
                                    fontSize = 14.sp,
                                    color = Color(0xFF888888)
                                )
                            }
                        }
                    }
                } else {
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
                                Spacer(modifier = Modifier. width(10.dp))
                                Text(
                                    text = "Lượt đã mua",
                                    fontSize = 20.sp,  // ✅ GIỐNG
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFF6B00)  // ✅ GIỐNG
                                )
                            }

                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFF6B9D).copy(alpha = 0.15f)
                                )
                            ) {
                                Text(
                                    text = "${games.size} loại",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF6B9D)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Column(
                            verticalArrangement = Arrangement. spacedBy(18.dp)
                        ) {
                            games.forEach { game ->
                                val meta = serverGames[game.gameCode]
                                GameTicketItemCard(game, meta)
                            }
                        }
                    }
                }
            }
            // ✅ STATUS (ĐÃ SỬA)
            if (status.isNotEmpty() && !isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            status.contains("✅") || status.contains("thành công", ignoreCase = true) -> Color(0xFFE8F5E9)  // ✅ XANH
                            status.contains("⚠️") || status.contains("chưa có", ignoreCase = true) -> Color(0xFFFFF3E0)  // ✅ VÀNG
                            status.contains("⏳") || status.contains("đang", ignoreCase = true) -> Color(0xFFE3F2FD)  // ✅ XANH NHẠT
                            status.contains("❌") || status.contains("lỗi", ignoreCase = true) -> Color(0xFFFFEBEE)  // ❌ ĐỎ
                            else -> Color(0xFFE8F5E9)  // ✅ MẶC ĐỊNH XANH (THÀNH CÔNG)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                status.contains("✅") || status.contains("thành công", ignoreCase = true) -> "✅"
                                status. contains("⚠️") || status.contains("chưa có", ignoreCase = true) -> "⚠️"
                                status.contains("⏳") || status.contains("đang", ignoreCase = true) -> "⏳"
                                status.contains("❌") || status.contains("lỗi", ignoreCase = true) -> "❌"
                                else -> "✅"  // ✅ MẶC ĐỊNH XANH
                            },
                            fontSize = 28.sp
                        )
                        Spacer(modifier = Modifier. width(14.dp))
                        Text(
                            text = status,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                status.contains("✅") || status.contains("thành công", ignoreCase = true) -> Color(0xFF4CAF50)
                                status.contains("⚠️") || status.contains("chưa có", ignoreCase = true) -> Color(0xFFFFA726)
                                status.contains("⏳") || status.contains("đang", ignoreCase = true) -> Color(0xFF2196F3)
                                status.contains("❌") || status.contains("lỗi", ignoreCase = true) -> Color(0xFFE53935)
                                else -> Color(0xFF4CAF50)  // ✅ MẶC ĐỊNH XANH
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameTicketItemCard(game: GameEntry, meta: ServerGameInfo?) {
    val colors = getGameColors(game.gameCode)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = RoundedCornerShape(18.dp),  // ✅ GIỐNG
        colors = CardDefaults. cardColors(containerColor = Color.White),
        elevation = CardDefaults. cardElevation(6.dp)  // ✅ GIỐNG
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(colors)
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
                            .background(Color.White.copy(alpha = 0.3f))
                            .shadow(4.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        meta?.image?.let { img ->
                            Image(
                                bitmap = img,
                                contentDescription = "Game image",
                                modifier = Modifier.size(56.dp).clip(CircleShape)
                            )
                        } ?: Text(
                            text = getGameEmoji(game.gameCode),
                            fontSize = 36.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = meta?.name ?: getGameName(game.gameCode),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,  // ✅ ĐỔI
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Lượt chơi có sẵn",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White. copy(alpha = 0.9f)
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier. padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${game.tickets}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors[0]
                        )
                    }
                }
            }
        }
    }
}

