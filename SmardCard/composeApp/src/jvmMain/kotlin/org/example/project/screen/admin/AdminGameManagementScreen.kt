package org.example.project.screen.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx. compose.material. icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose. ui.draw.shadow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.project.SmartCardManager
import org.example.project.screen.FloatingBubbles
import org.example.project.network.GameApiClient
import org.example.project.auth.TokenStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import java.awt.FileDialog
import java.awt.Frame
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

// UI model with optional image
data class GameUi(
    val gameCode: Int,
    val gameName: String,
    val gameDescription: String?,
    val ticketPrice: String,
    val isActive: Boolean,
    val image: ImageBitmap? = null,
    val imageData: ByteArray? = null
)

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AdminGameManagementScreen(
//    smartCardManager: SmartCardManager,
//    onBack: () -> Unit
//) {
//    var games by remember { mutableStateOf<List<GameUi>>(emptyList()) }
//    var isLoading by remember { mutableStateOf(false) }
//    var status by remember { mutableStateOf("") }
//    var showAddDialog by remember { mutableStateOf(false) }
//
//    val scope = rememberCoroutineScope()
//    val gameApiClient = remember { GameApiClient() }
//
//    fun loadGames() {
//        scope.launch {
//            isLoading = true
//            try {
//                val result = withContext(Dispatchers.IO) {
//                    gameApiClient.getAllGames()
//                }
//                result.onSuccess { gameList ->
//                    games = gameList.map { dto ->
//                        val imageBytes = gameApiClient.decodeImage(dto.gameImage)
//                        val imageBitmap = imageBytes?.let {
//                            try {
//                                Image.makeFromEncoded(it).toComposeImageBitmap()
//                            } catch (e: Exception) {
//                                null
//                            }
//                        }
//                        GameUi(
//                            gameCode = dto.gameCode,
//                            gameName = dto.gameName,
//                            gameDescription = dto.gameDescription,
//                            ticketPrice = dto.ticketPrice,
//                            isActive = dto.isActive,
//                            image = imageBitmap,
//                            imageData = imageBytes
//                        )
//                    }
//                    status = if (games.isEmpty())
//                        "📭 Chưa có game nào trên server"
//                    else
//                        "✅ Đã tải ${games.size} game từ server"
//                }.onFailure { e ->
//                    status = "❌ Lỗi: ${e.message}"
//                    games = emptyList()
//                }
//            } catch (e: Exception) {
//                status = "❌ Lỗi: ${e.message}"
//                games = emptyList()
//            }
//            isLoading = false
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
//                        Color(0xFFFFF0F5),
//                        Color(0xFFE0F7FA)
//                    )
//                )
//            )
//    ) {
//        FloatingBubbles()
//
//        Column(
//            modifier = Modifier
//                . fillMaxSize()
//                .padding(16.dp)
//        ) {
//            // Header
//            Card(
//                modifier = Modifier. fillMaxWidth(),
//                shape = RoundedCornerShape(24.dp),
//                colors = CardDefaults.cardColors(containerColor = Color(0xFFBA68C8)),
//                elevation = CardDefaults.cardElevation(8.dp)
//            ) {
//                Row(
//                    modifier = Modifier
//                        . fillMaxWidth()
//                        . padding(20.dp),
//                    verticalAlignment = Alignment. CenterVertically
//                ) {
//                    Button(
//                        onClick = onBack,
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color.White.copy(alpha = 0.2f),
//                            contentColor = Color.White
//                        ),
//                        shape = RoundedCornerShape(16.dp),
//                        modifier = Modifier.size(48.dp),
//                        contentPadding = PaddingValues(0.dp)
//                    ) {
//                        Text("←", fontSize = 20.sp)
//                    }
//
//                    Spacer(modifier = Modifier.width(16.dp))
//
//                    Column(modifier = Modifier.weight(1f)) {
//                        Text(
//                            text = "🎮 Quản Lý Vé Game",
//                            fontSize = 22.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.White
//                        )
//                    }
//
//                    Button(
//                        onClick = { loadGames() },
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color. White. copy(alpha = 0.2f)
//                        ),
//                        shape = RoundedCornerShape(12.dp),
//                        contentPadding = PaddingValues(12.dp)
//                    ) {
//                        Text("🔄", fontSize = 16.sp)
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier. height(16.dp))
//
//            // Add Game Button
//            Button(
//                onClick = { showAddDialog = true },
//                modifier = Modifier. fillMaxWidth(),
//                shape = RoundedCornerShape(16.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF4CAF50)
//                )
//            ) {
//                Text("➕ Thêm game mới", fontSize = 16.sp, fontWeight = FontWeight.Bold)
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Status message
//            if (status.isNotEmpty()) {
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(12.dp),
//                    colors = CardDefaults.cardColors(
//                        containerColor = when {
//                            status.startsWith("✅") -> Color(0xFFE8F5E9)
//                            status.startsWith("📭") -> Color(0xFFFFF9C4)
//                            else -> Color(0xFFFFEBEE)
//                        }
//                    )
//                ) {
//                    Text(
//                        text = status,
//                        modifier = Modifier.padding(16.dp),
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//                Spacer(modifier = Modifier.height(16.dp))
//            }
//
//            // Game List
//            if (isLoading) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .weight(1f),
//                    contentAlignment = Alignment.Center
//                ) {
//                    CircularProgressIndicator(color = Color(0xFFBA68C8))
//                }
//            } else if (games.isEmpty()) {
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .weight(1f),
//                    shape = RoundedCornerShape(24.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color. White),
//                    elevation = CardDefaults.cardElevation(8.dp)
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(32.dp),
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.Center
//                    ) {
//                        Text("🎪", fontSize = 80.sp)
//                        Spacer(modifier = Modifier. height(16.dp))
//                        Text(
//                            text = "Chưa có game nào",
//                            fontSize = 20.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.Gray
//                        )
//                    }
//                }
//            } else {
//                LazyColumn(
//                    modifier = Modifier. weight(1f),
//                    verticalArrangement = Arrangement. spacedBy(12.dp)
//                ) {
//                    items(games) { game ->
//                        AdminGameCard(
//                            game = game,
//                            gameApiClient = gameApiClient,
//                            onDeleteGame = {
//                                scope.launch {
//                                    try {
//                                        val result = withContext(Dispatchers.IO) {
//                                            gameApiClient.deleteGame(game.gameCode)
//                                        }
//                                        result.onSuccess {
//                                            status = "✅ Đã xóa game ${game.gameName}"
//                                            loadGames()
//                                        }.onFailure { e ->
//                                            status = "❌ Lỗi: ${e.message}"
//                                        }
//                                    } catch (e: Exception) {
//                                        status = "❌ Lỗi: ${e.message}"
//                                    }
//                                }
//                            }
//                        )
//                    }
//                }
//            }
//        }
//    }
//
//    // Add Game Dialog
//    if (showAddDialog) {
//        AddGameDialog(
//            onDismiss = { showAddDialog = false },
//            onConfirm = { gameName, gameDescription, ticketPrice, imageData, imageBitmap ->
//                scope.launch {
//                    try {
//                        val result = withContext(Dispatchers.IO) {
//                            gameApiClient.addGame(gameName, gameDescription, ticketPrice, imageData)
//                        }
//                        result.onSuccess { gameCode ->
//                            status = "✅ Đã thêm game '$gameName' với mã $gameCode"
//                            showAddDialog = false
//                            loadGames()
//                        }.onFailure { e ->
//                            status = "❌ Lỗi: ${e.message}"
//                        }
//                    } catch (e: Exception) {
//                        status = "❌ Lỗi: ${e.message}"
//                    }
//                }
//            }
//        )
//    }
//}
//
//@Composable
//fun AdminGameCard(
//    game: GameUi,
//    gameApiClient: GameApiClient,
//    onDeleteGame: () -> Unit
//) {
//    var showRemoveDialog by remember { mutableStateOf(false) }
//
//    Card(
//        modifier = Modifier. fillMaxWidth(),
//        shape = RoundedCornerShape(20.dp),
//        colors = CardDefaults.cardColors(containerColor = Color. White),
//        elevation = CardDefaults. cardElevation(8.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(20.dp)
//        ) {
//            Row(
//                modifier = Modifier. fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(64.dp)
//                        .clip(CircleShape)
//                        .background(
//                            brush = Brush.radialGradient(
//                                colors = listOf(
//                                    Color(0xFFBA68C8),
//                                    Color(0xFFCE93D8)
//                                )
//                            )
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//                    if (game.image != null) {
//                        Image(
//                            bitmap = game.image,
//                            contentDescription = "Game image",
//                            modifier = Modifier.size(64.dp).clip(CircleShape)
//                        )
//                    } else {
//                        Text("🎯", fontSize = 28.sp)
//                    }
//                }
//
//                Spacer(modifier = Modifier.width(16.dp))
//
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        text = game.gameName,
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color(0xFF333333)
//                    )
//                    Spacer(modifier = Modifier. height(4.dp))
//                    if (!game.gameDescription.isNullOrEmpty()) {
//                        Text(
//                            text = game.gameDescription,
//                            fontSize = 12.sp,
//                            color = Color(0xFF888888)
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                    }
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text(
//                            text = "💰 ${game.ticketPrice} VNĐ/vé",
//                            fontSize = 14.sp,
//                            color = Color(0xFF4CAF50),
//                            fontWeight = FontWeight.Medium
//                        )
//                        Spacer(modifier = Modifier.width(12.dp))
//                        Text(
//                            text = if (game.isActive) "✅ Hoạt động" else "❌ Tắt",
//                            fontSize = 12.sp,
//                            color = if (game.isActive) Color(0xFF4CAF50) else Color(0xFFE53935)
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.End
//            ) {
//                OutlinedButton(
//                    onClick = { showRemoveDialog = true },
//                    shape = RoundedCornerShape(12.dp),
//                    colors = ButtonDefaults.outlinedButtonColors(
//                        contentColor = Color(0xFFE53935)
//                    )
//                ) {
//                    Text("🗑️ Xóa", fontSize = 12.sp)
//                }
//            }
//        }
//    }
//
//    // Remove Confirmation Dialog
//    if (showRemoveDialog) {
//        AlertDialog(
//            onDismissRequest = { showRemoveDialog = false },
//            title = { Text("Xác nhận xóa") },
//            text = { Text("Bạn có chắc muốn xóa game \"${game.gameName}\"?") },
//            confirmButton = {
//                Button(
//                    onClick = {
//                        onDeleteGame()
//                        showRemoveDialog = false
//                    },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(0xFFE53935)
//                    )
//                ) {
//                    Text("Xóa")
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = { showRemoveDialog = false }) {
//                    Text("Hủy")
//                }
//            }
//        )
//    }
//}
//
//@Composable
//fun AddGameDialog(
//    onDismiss: () -> Unit,
//    onConfirm: (String, String, String, ByteArray?, ImageBitmap?) -> Unit
//) {
//    var gameName by remember { mutableStateOf("") }
//    var gameDescription by remember { mutableStateOf("") }
//    var ticketPrice by remember { mutableStateOf("") }
//    var imageData by remember { mutableStateOf<ByteArray?>(null) }
//    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text("Thêm game mới") },
//        text = {
//            Column {
//                OutlinedTextField(
//                    value = gameName,
//                    onValueChange = { gameName = it },
//                    label = { Text("Tên game") },
//                    singleLine = true,
//                    modifier = Modifier.fillMaxWidth()
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                OutlinedTextField(
//                    value = gameDescription,
//                    onValueChange = { gameDescription = it },
//                    label = { Text("Mô tả") },
//                    minLines = 2,
//                    maxLines = 3,
//                    modifier = Modifier.fillMaxWidth()
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                OutlinedTextField(
//                    value = ticketPrice,
//                    onValueChange = { ticketPrice = it },
//                    label = { Text("Giá vé (VNĐ)") },
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    singleLine = true,
//                    modifier = Modifier.fillMaxWidth()
//                )
//                Spacer(modifier = Modifier.height(12.dp))
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Button(
//                        onClick = {
//                            val fd = FileDialog(null as Frame?, "Chọn ảnh game", FileDialog.LOAD)
//                            fd.isVisible = true
//                            val fileName = fd.file
//                            val dir = fd.directory
//                            if (fileName != null && dir != null) {
//                                try {
//                                    val file = File(dir, fileName)
//                                    val img = ImageIO.read(file)
//                                    val baos = ByteArrayOutputStream()
//                                    ImageIO.write(img, "png", baos)
//                                    imageData = baos.toByteArray()
//                                    imageBitmap = Image.makeFromEncoded(imageData!!).toComposeImageBitmap()
//                                } catch (e: Exception) {
//                                    println("Error loading image: ${e.message}")
//                                }
//                            }
//                        },
//                        shape = RoundedCornerShape(12.dp)
//                    ) {
//                        Text("🖼️ Chọn ảnh")
//                    }
//
//                    Spacer(modifier = Modifier.width(12.dp))
//                    if (imageBitmap != null) {
//                        Image(
//                            bitmap = imageBitmap!!,
//                            contentDescription = "Game preview",
//                            modifier = Modifier
//                                .size(64.dp)
//                                .clip(RoundedCornerShape(8.dp))
//                        )
//                    } else {
//                        Text("Chưa có ảnh", color = Color.Gray, fontSize = 12.sp)
//                    }
//                }
//            }
//        },
//        confirmButton = {
//            Button(
//                onClick = {
//                    if (gameName.isNotEmpty() && ticketPrice.isNotEmpty()) {
//                        onConfirm(gameName, gameDescription, ticketPrice, imageData, imageBitmap)
//                    }
//                },
//                enabled = gameName.isNotEmpty() && ticketPrice.isNotEmpty()
//            ) {
//                Text("Thêm")
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = onDismiss) {
//                Text("Hủy")
//            }
//        }
//    )
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGameManagementScreen(
    smartCardManager: SmartCardManager,
    onBack: () -> Unit
) {
    var games by remember { mutableStateOf<List<GameUi>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()  // ✅ THÊM
    val gameApiClient = remember { GameApiClient() }

    fun loadGames() {
        scope.launch {
            isLoading = true
            try {
                val result = withContext(Dispatchers.IO) {
                    gameApiClient.getAllGames()
                }
                result.onSuccess { gameList ->
                    games = gameList. map { dto ->
                        val imageBytes = gameApiClient.decodeImage(dto.gameImage)
                        val imageBitmap = imageBytes?. let {
                            try {
                                Image.makeFromEncoded(it).toComposeImageBitmap()
                            } catch (e:  Exception) {
                                null
                            }
                        }
                        GameUi(
                            gameCode = dto.gameCode,
                            gameName = dto.gameName,
                            gameDescription = dto.gameDescription,
                            ticketPrice = dto.ticketPrice,
                            isActive = dto.isActive,
                            image = imageBitmap,
                            imageData = imageBytes
                        )
                    }
                    status = if (games.isEmpty())
                        "📭 Chưa có game nào trên server"
                    else
                        "✅ Đã tải ${games.size} game từ server"
                }. onFailure { e ->
                    status = "❌ Lỗi: ${e. message}"
                    games = emptyList()
                }
            } catch (e: Exception) {
                status = "❌ Lỗi:  ${e.message}"
                games = emptyList()
            }
            isLoading = false
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
                .verticalScroll(scrollState)  // ✅ THÊM scroll
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
                                text = "🎮 Quản Lý Vé Game",
                                fontSize = 22.sp,  // ✅ GIỐNG
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
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
                                    Text("🎯", fontSize = 18.sp)  // ✅ GIỐNG
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Quản lý game & vé",
                                        fontSize = 14.sp,  // ✅ GIỐNG
                                        fontWeight = FontWeight.Bold,
                                        color = Color. White
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
                            Text("🎪", fontSize = 32.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier. height(20.dp))  // ✅ GIỐNG

            // ✅ CONTENT CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()  // ✅ GIỐNG
                    .shadow(12.dp, RoundedCornerShape(28.dp)),  // ✅ GIỐNG
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp)  // ✅ GIỐNG
                ) {
                    // ✅ ADD BUTTON
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),  // ✅ GIỐNG
                        shape = RoundedCornerShape(18.dp),  // ✅ GIỐNG
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 16.dp
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement. Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Thêm game mới",
                                fontSize = 18.sp,
                                fontWeight = FontWeight. ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier. height(28.dp))

                    HorizontalDivider(
                        color = Color(0xFFFFAB91),  // ✅ GIỐNG
                        thickness = 2.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    Spacer(modifier = Modifier. height(20.dp))

                    // ✅ GAME LIST
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎯", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Danh sách game (${games.size})",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF6B00)  // ✅ GIỐNG
                        )
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
                                modifier = Modifier.size(52.dp),
                                color = Color(0xFFFF6B9D),
                                strokeWidth = 5.dp
                            )
                        }
                    } else if (games.isEmpty()) {
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
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Chưa có game nào",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement. spacedBy(18.dp)  // ✅ GIỐNG spacer
                        ) {
                            games.forEach { game ->
                                AdminGameCard(
                                    game = game,
                                    gameApiClient = gameApiClient,
                                    onDeleteGame = {
                                        scope.launch {
                                            try {
                                                val result = withContext(Dispatchers.IO) {
                                                    gameApiClient. deleteGame(game.gameCode)
                                                }
                                                result.onSuccess {
                                                    status = "✅ Đã xóa game ${game.gameName}"
                                                    loadGames()
                                                }. onFailure { e ->
                                                    status = "❌ Lỗi: ${e.message}"
                                                }
                                            } catch (e: Exception) {
                                                status = "❌ Lỗi: ${e. message}"
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ✅ STATUS
            if (status.isNotEmpty()) {
                Spacer(modifier = Modifier. height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(20.dp)),  // ✅ GIỐNG
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            status.startsWith("✅") -> Color(0xFFE8F5E9)
                            status.startsWith("📭") -> Color(0xFFFFF3E0)
                            else -> Color(0xFFFFEBEE)
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
                                status.startsWith("✅") -> "✅"
                                status.startsWith("📭") -> "📭"
                                else -> "❌"
                            },
                            fontSize = 28.sp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = status. substring(2),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                status.startsWith("✅") -> Color(0xFF4CAF50)
                                status.startsWith("📭") -> Color(0xFFFFA726)
                                else -> Color(0xFFE53935)
                            },
                            modifier = Modifier. weight(1f)
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddGameDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { gameName, gameDescription, ticketPrice, imageData, imageBitmap ->
                scope. launch {
                    try {
                        val result = withContext(Dispatchers.IO) {
                            gameApiClient.addGame(gameName, gameDescription, ticketPrice, imageData)
                        }
                        result.onSuccess { gameCode ->
                            status = "✅ Đã thêm game '$gameName' với mã $gameCode"
                            showAddDialog = false
                            loadGames()
                        }.onFailure { e ->
                            status = "❌ Lỗi: ${e.message}"
                        }
                    } catch (e: Exception) {
                        status = "❌ Lỗi: ${e.message}"
                    }
                }
            }
        )
    }
}

@Composable
fun AdminGameCard(
    game: GameUi,
    gameApiClient:  GameApiClient,
    onDeleteGame: () -> Unit
) {
    var showRemoveDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier. fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),  // ✅ GIỐNG InfoCard
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFAFAFA)
        ),
        elevation = CardDefaults. cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),  // ✅ GIỐNG
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF6B9D),
                                Color(0xFFFFA07A),
                                Color(0xFFFFD700)
                            )
                        )
                    ),
                contentAlignment = Alignment. Center
            ) {
                if (game.image != null) {
                    Image(
                        bitmap = game.image,
                        contentDescription = "Game image",
                        modifier = Modifier.size(64.dp).clip(CircleShape)
                    )
                } else {
                    Text("🎯", fontSize = 32.sp)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.gameName,
                    fontSize = 18.sp,  // ✅ GIỐNG
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF333333)
                )
                if (! game.gameDescription.isNullOrEmpty()) {
                    Spacer(modifier = Modifier. height(4.dp))
                    Text(
                        text = game.gameDescription,
                        fontSize = 12.sp,
                        color = Color(0xFF888888)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💰 ${game.ticketPrice} VNĐ/vé",
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight. Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (game.isActive) "✅ Hoạt động" else "❌ Tắt",
                        fontSize = 12.sp,
                        color = if (game.isActive) Color(0xFF4CAF50) else Color(0xFFE53935),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = { showRemoveDialog = true },
                modifier = Modifier. size(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEBEE)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("🗑️", fontSize = 20.sp)
            }
        }
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Xác nhận xóa", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn xóa game \"${game.gameName}\"? ") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteGame()
                        showRemoveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

// AddGameDialog giữ nguyên logic, chỉ sửa giao diện một chút
@Composable
fun AddGameDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, ByteArray?, ImageBitmap?) -> Unit
) {
    var gameName by remember { mutableStateOf("") }
    var gameDescription by remember { mutableStateOf("") }
    var ticketPrice by remember { mutableStateOf("") }
    var imageData by remember { mutableStateOf<ByteArray?>(null) }
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm game mới", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = {
            Column {
                OutlinedTextField(
                    value = gameName,
                    onValueChange = { gameName = it },
                    label = { Text("Tên game", fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = gameDescription,
                    onValueChange = { gameDescription = it },
                    label = { Text("Mô tả", fontWeight = FontWeight.Bold) },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = ticketPrice,
                    onValueChange = { ticketPrice = it },
                    label = { Text("Giá vé (VNĐ)", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {
                            val fd = FileDialog(null as Frame?, "Chọn ảnh game", FileDialog.LOAD)
                            fd.isVisible = true
                            val fileName = fd.file
                            val dir = fd.directory
                            if (fileName != null && dir != null) {
                                try {
                                    val file = File(dir, fileName)
                                    val img = ImageIO.read(file)
                                    val baos = ByteArrayOutputStream()
                                    ImageIO.write(img, "png", baos)
                                    imageData = baos.toByteArray()
                                    imageBitmap = Image.makeFromEncoded(imageData!! ).toComposeImageBitmap()
                                } catch (e: Exception) {
                                    println("Error loading image: ${e.message}")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🖼️ Chọn ảnh", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap!! ,
                            contentDescription = "Game preview",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Text("Chưa có ảnh", color = Color. Gray, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (gameName.isNotEmpty() && ticketPrice.isNotEmpty()) {
                        onConfirm(gameName, gameDescription, ticketPrice, imageData, imageBitmap)
                    }
                },
                enabled = gameName.isNotEmpty() && ticketPrice.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Thêm", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", fontWeight = FontWeight.Bold)
            }
        }
    )
}