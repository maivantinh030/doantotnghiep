package org.example.project.screen.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.project.SmartCardManager
import org.example.project.config.ServerConfig
import org.example.project.data.repository.RSARepository
import org.example.project.screen.FloatingBubbles
import java.awt.FileDialog
import java.awt.Frame
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AdminWriteInfoScreen(
//    smartCardManager: SmartCardManager,
//    onBack: () -> Unit
//) {
//    // ✅ TỰ ĐỘNG TẠO CUSTOMER ID (ddMMyy + HHmmss)
//    val customerID = remember {
//        val now = LocalDateTime.now()
//        val formatter = DateTimeFormatter.ofPattern("ddMMyyHHmmss")
//        "KH${now.format(formatter)}"
//    }
//
//    var name by remember { mutableStateOf("") }
//
//    // ✅ SỬA:  Dùng TextFieldValue để quản lý cursor
//    var dateOfBirthState by remember {
//        mutableStateOf(TextFieldValue(text = "", selection = TextRange(0)))
//    }
//    var dateOfBirth by remember { mutableStateOf("") }  // String để gửi lên thẻ
//
//    var phoneNumber by remember { mutableStateOf("") }
//    var selectedImage by remember { mutableStateOf<ImageBitmap?>(null) }
//    var imageData by remember { mutableStateOf<ByteArray?>(null) }
//    var isWriting by remember { mutableStateOf(false) }
//    var status by remember { mutableStateOf("") }
//    var uploadProgress by remember { mutableStateOf(0f) }
//
//    val scope = rememberCoroutineScope()
//    val scrollState = rememberScrollState()
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                brush = Brush.verticalGradient(
//                    colors = listOf(
//                        Color(0xFFE3F2FD),
//                        Color(0xFFF8BBD0),
//                        Color(0xFFFFF9C4)
//                    )
//                )
//            )
//    ) {
//        FloatingBubbles()
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(24.dp)
//        ) {
//            // ✅ HEADER GRADIENT ĐẸP
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .shadow(16.dp, RoundedCornerShape(32.dp)),
//                shape = RoundedCornerShape(32.dp),
//                colors = CardDefaults.cardColors(
//                    containerColor = Color. Transparent
//                )
//            ) {
//                Box(
//                    modifier = Modifier
//                        . fillMaxWidth()
//                        . background(
//                            brush = Brush.horizontalGradient(
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
//                        modifier = Modifier.fillMaxWidth(),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        IconButton(
//                            onClick = onBack,
//                            modifier = Modifier
//                                .size(52.dp)
//                                .clip(CircleShape)
//                                .background(Color. White. copy(alpha = 0.25f))
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.ArrowBack,
//                                contentDescription = "Back",
//                                tint = Color. White,
//                                modifier = Modifier. size(28.dp)
//                            )
//                        }
//
//                        Spacer(modifier = Modifier.width(16.dp))
//
//                        Column(modifier = Modifier.weight(1f)) {
//                            Text(
//                                text = "✨ Ghi Thông Tin Khách Hàng",
//                                fontSize = 24.sp,
//                                fontWeight = FontWeight.ExtraBold,
//                                color = Color. White
//                            )
//                            Spacer(modifier = Modifier. height(4.dp))
//                            Row(verticalAlignment = Alignment.CenterVertically) {
//                                Text("🏷️", fontSize = 16.sp)
//                                Spacer(modifier = Modifier.width(4.dp))
//                                Text(
//                                    text = "Mã hôm nay: KH",
//                                    fontSize = 15.sp,
//                                    fontWeight = FontWeight.Medium,
//                                    color = Color.White. copy(alpha = 0.95f)
//                                )
//                            }
//                        }
//
//                        Box(
//                            modifier = Modifier
//                                .size(64.dp)
//                                .clip(CircleShape)
//                                .background(Color.White.copy(alpha = 0.25f)),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text("✍️", fontSize = 32.sp)
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier. height(24.dp))
//
//            // ✅ FORM CARD
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//                    .shadow(12.dp, RoundedCornerShape(32.dp)),
//                shape = RoundedCornerShape(32.dp),
//                colors = CardDefaults.cardColors(containerColor = Color.White)
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .verticalScroll(scrollState)
//                        .padding(32.dp)
//                ) {
//                    // ✅ PHOTO SECTION
//                    Card(
//                        modifier = Modifier. fillMaxWidth(),
//                        shape = RoundedCornerShape(24.dp),
//                        colors = CardDefaults.cardColors(
//                            containerColor = Color(0xFFFAFAFA)
//                        ),
//                        elevation = CardDefaults.cardElevation(4.dp)
//                    ) {
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(28.dp),
//                            horizontalAlignment = Alignment.CenterHorizontally
//                        ) {
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement. Center
//                            ) {
//                                Text("📸", fontSize = 24.sp)
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text(
//                                    text = "Ảnh khách hàng",
//                                    fontSize = 20.sp,
//                                    fontWeight = FontWeight.Bold,
//                                    color = Color(0xFF333333)
//                                )
//                            }
//
//                            Spacer(modifier = Modifier.height(20.dp))
//
//                            Box(
//                                modifier = Modifier
//                                    .size(180.dp)
//                                    . shadow(8.dp, CircleShape)
//                                    . clip(CircleShape)
//                                    .background(
//                                        brush = Brush. radialGradient(
//                                            colors = listOf(
//                                                Color(0xFFBBDEFB),
//                                                Color(0xFF90CAF9),
//                                                Color(0xFF64B5F6)
//                                            )
//                                        )
//                                    ),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                if (selectedImage != null) {
//                                    Image(
//                                        bitmap = selectedImage!!,
//                                        contentDescription = "Customer Photo",
//                                        modifier = Modifier.fillMaxSize()
//                                    )
//                                } else {
//                                    Column(
//                                        horizontalAlignment = Alignment.CenterHorizontally,
//                                        verticalArrangement = Arrangement.Center
//                                    ) {
//                                        Icon(
//                                            imageVector = Icons.Default.Person,
//                                            contentDescription = null,
//                                            modifier = Modifier.size(72.dp),
//                                            tint = Color. White
//                                        )
//                                        Spacer(modifier = Modifier.height(8.dp))
//                                        Text(
//                                            text = "Chưa có ảnh",
//                                            fontSize = 14.sp,
//                                            fontWeight = FontWeight.Medium,
//                                            color = Color.White
//                                        )
//                                    }
//                                }
//                            }
//
//                            Spacer(modifier = Modifier.height(20.dp))
//
//                            Button(
//                                onClick = {
//                                    val dialog = FileDialog(null as Frame?, "Chọn ảnh", FileDialog.LOAD)
//                                    dialog.setFilenameFilter { _, name ->
//                                        name.lowercase().endsWith(".jpg") ||
//                                                name.lowercase().endsWith(".jpeg") ||
//                                                name.lowercase().endsWith(".png")
//                                    }
//                                    dialog.isVisible = true
//
//                                    val dir = dialog.directory
//                                    val file = dialog.file
//
//                                    if (dir != null && file != null) {
//                                        scope.launch {
//                                            try {
//                                                val imageFile = File(dir, file)
//                                                val bufferedImage = ImageIO.read(imageFile)
//
//                                                val maxWidth = 200
//                                                val maxHeight = 200
//                                                val scaledImage = if (bufferedImage.width > maxWidth || bufferedImage.height > maxHeight) {
//                                                    val scale = minOf(
//                                                        maxWidth.toFloat() / bufferedImage.width,
//                                                        maxHeight.toFloat() / bufferedImage.height
//                                                    )
//                                                    val newWidth = (bufferedImage. width * scale).toInt()
//                                                    val newHeight = (bufferedImage.height * scale).toInt()
//
//                                                    val scaled = java.awt.image.BufferedImage(newWidth, newHeight, bufferedImage.type)
//                                                    val g = scaled.createGraphics()
//                                                    g.drawImage(bufferedImage, 0, 0, newWidth, newHeight, null)
//                                                    g.dispose()
//                                                    scaled
//                                                } else {
//                                                    bufferedImage
//                                                }
//
//                                                val outputStream = ByteArrayOutputStream()
//                                                ImageIO.write(scaledImage, "jpg", outputStream)
//                                                val bytes = outputStream.toByteArray()
//
//                                                if (bytes.size > 8000) {
//                                                    var quality = 0.7f
//                                                    var compressedBytes = bytes
//
//                                                    while (compressedBytes.size > 8000 && quality > 0.1f) {
//                                                        val baos = ByteArrayOutputStream()
//                                                        val writer = ImageIO.getImageWritersByFormatName("jpg").next()
//                                                        val ios = ImageIO.createImageOutputStream(baos)
//                                                        writer.output = ios
//
//                                                        val param = writer.defaultWriteParam
//                                                        param.compressionMode = javax.imageio.ImageWriteParam.MODE_EXPLICIT
//                                                        param.compressionQuality = quality
//
//                                                        writer.write(null, javax.imageio.IIOImage(scaledImage, null, null), param)
//                                                        writer. dispose()
//                                                        ios.close()
//
//                                                        compressedBytes = baos.toByteArray()
//                                                        quality -= 0.1f
//                                                    }
//
//                                                    if (compressedBytes.size > 8000) {
//                                                        status = "❌ Ảnh quá lớn!  Vui lòng chọn ảnh khác."
//                                                        return@launch
//                                                    }
//
//                                                    imageData = compressedBytes
//                                                } else {
//                                                    imageData = bytes
//                                                }
//
//                                                selectedImage = scaledImage. toComposeImageBitmap()
//                                                status = "✅ Đã chọn ảnh thành công!"
//
//                                            } catch (e:  Exception) {
//                                                status = "❌ Lỗi đọc ảnh: ${e. message}"
//                                                e. printStackTrace()
//                                            }
//                                        }
//                                    }
//                                },
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    . height(56.dp),
//                                shape = RoundedCornerShape(16.dp),
//                                colors = ButtonDefaults.buttonColors(
//                                    containerColor = Color(0xFF667EEA)
//                                ),
//                                elevation = ButtonDefaults.buttonElevation(
//                                    defaultElevation = 4.dp,
//                                    pressedElevation = 8.dp
//                                )
//                            ) {
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    horizontalArrangement = Arrangement. Center
//                                ) {
//                                    Icon(
//                                        imageVector = Icons.Default.Upload,
//                                        contentDescription = null,
//                                        modifier = Modifier.size(22.dp)
//                                    )
//                                    Spacer(modifier = Modifier.width(10.dp))
//                                    Text(
//                                        text = if (selectedImage == null) "📁 Chọn ảnh từ máy" else "🔄 Đổi ảnh",
//                                        fontSize = 17.sp,
//                                        fontWeight = FontWeight.Bold
//                                    )
//                                }
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier. height(28.dp))
//
//                    Divider(
//                        color = Color(0xFFE0E0E0),
//                        thickness = 2.dp,
//                        modifier = Modifier. padding(vertical = 8.dp)
//                    )
//
//                    Spacer(modifier = Modifier. height(20.dp))
//
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text("📝", fontSize = 22.sp)
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = "Thông tin cơ bản",
//                            fontSize = 20.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color(0xFF333333)
//                        )
//                    }
//
//                    Spacer(modifier = Modifier. height(20.dp))
//
//                    // MÃ KHÁCH HÀNG (TỰ ĐỘNG)
//                    Card(
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(16.dp),
//                        colors = CardDefaults.cardColors(
//                            containerColor = Color(0xFFE8F5E9)
//                        )
//                    ) {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(16.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Badge,
//                                contentDescription = null,
//                                tint = Color(0xFF4CAF50),
//                                modifier = Modifier.size(24.dp)
//                            )
//                            Spacer(modifier = Modifier.width(12.dp))
//                            Column {
//                                Text(
//                                    text = "Mã khách hàng (tự động)",
//                                    fontSize = 12.sp,
//                                    color = Color(0xFF666666)
//                                )
//                                Text(
//                                    text = customerID,
//                                    fontSize = 18.sp,
//                                    fontWeight = FontWeight.Bold,
//                                    color = Color(0xFF4CAF50)
//                                )
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(18.dp))
//
//                    // HỌ VÀ TÊN
//                    OutlinedTextField(
//                        value = name,
//                        onValueChange = { if (it.length <= 50) name = it },
//                        label = { Text("Họ và tên", fontWeight = FontWeight.Medium) },
//                        leadingIcon = {
//                            Icon(
//                                imageVector = Icons.Default.Person,
//                                contentDescription = null,
//                                tint = Color(0xFF667EEA)
//                            )
//                        },
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(16.dp),
//                        singleLine = true,
//                        colors = OutlinedTextFieldDefaults.colors(
//                            focusedBorderColor = Color(0xFF667EEA),
//                            focusedLabelColor = Color(0xFF667EEA),
//                            focusedLeadingIconColor = Color(0xFF667EEA),
//                            cursorColor = Color(0xFF667EEA)
//                        )
//                    )
//
//                    Spacer(modifier = Modifier.height(18.dp))
//
//                    // ✅ NGÀY SINH - ĐÃ SỬA CURSOR
//                    OutlinedTextField(
//                        value = dateOfBirthState,
//                        onValueChange = { newValue ->
//                            val digitsOnly = newValue.text.filter { it.isDigit() }
//
//                            if (digitsOnly. length <= 8) {
//                                val formatted = when {
//                                    digitsOnly.isEmpty() -> ""
//                                    digitsOnly.length <= 2 -> digitsOnly
//                                    digitsOnly.length <= 4 -> "${digitsOnly.take(2)}/${digitsOnly.drop(2)}"
//                                    else -> "${digitsOnly.take(2)}/${digitsOnly.substring(2, 4)}/${digitsOnly.drop(4)}"
//                                }
//
//                                // ✅ Đặt cursor ở cuối chuỗi
//                                dateOfBirthState = TextFieldValue(
//                                    text = formatted,
//                                    selection = TextRange(formatted.length)
//                                )
//                                dateOfBirth = formatted  // Lưu String để gửi lên thẻ
//                            }
//                        },
//                        label = { Text("Ngày sinh", fontWeight = FontWeight.Medium) },
//                        placeholder = { Text("13/12/2025", color = Color.Gray) },
//                        leadingIcon = {
//                            Icon(
//                                imageVector = Icons.Default.CalendarToday,
//                                contentDescription = null,
//                                tint = Color(0xFF667EEA)
//                            )
//                        },
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(16.dp),
//                        singleLine = true,
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
//                        colors = OutlinedTextFieldDefaults.colors(
//                            focusedBorderColor = Color(0xFF667EEA),
//                            focusedLabelColor = Color(0xFF667EEA),
//                            focusedLeadingIconColor = Color(0xFF667EEA),
//                            cursorColor = Color(0xFF667EEA)
//                        ),
//                        supportingText = {
//                            Text(
//                                text = "💡 Nhập số, tự động thêm /",
//                                fontSize = 12.sp,
//                                color = Color(0xFF9575CD)
//                            )
//                        }
//                    )
//
//                    Spacer(modifier = Modifier.height(18.dp))
//
//                    // SỐ ĐIỆN THOẠI
//                    OutlinedTextField(
//                        value = phoneNumber,
//                        onValueChange = {
//                            if (it.length <= 10 && it.all { c -> c.isDigit() })
//                                phoneNumber = it
//                        },
//                        label = { Text("Số điện thoại", fontWeight = FontWeight.Medium) },
//                        leadingIcon = {
//                            Icon(
//                                imageVector = Icons.Default.Phone,
//                                contentDescription = null,
//                                tint = Color(0xFF667EEA)
//                            )
//                        },
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(16.dp),
//                        singleLine = true,
//                        colors = OutlinedTextFieldDefaults.colors(
//                            focusedBorderColor = Color(0xFF667EEA),
//                            focusedLabelColor = Color(0xFF667EEA),
//                            focusedLeadingIconColor = Color(0xFF667EEA),
//                            cursorColor = Color(0xFF667EEA)
//                        )
//                    )
//
//                    Spacer(modifier = Modifier.height(28.dp))
//
//                    // PROGRESS BAR
//                    if (isWriting && uploadProgress > 0f) {
//                        Card(
//                            modifier = Modifier. fillMaxWidth(),
//                            shape = RoundedCornerShape(20.dp),
//                            colors = CardDefaults.cardColors(
//                                containerColor = Color(0xFFF5F5F5)
//                            ),
//                            elevation = CardDefaults.cardElevation(4.dp)
//                        ) {
//                            Column(modifier = Modifier.padding(20.dp)) {
//                                Row(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    horizontalArrangement = Arrangement.SpaceBetween,
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Row(verticalAlignment = Alignment. CenterVertically) {
//                                        Text("⏳", fontSize = 20.sp)
//                                        Spacer(modifier = Modifier.width(8.dp))
//                                        Text(
//                                            text = "Đang tải lên.. .",
//                                            fontSize = 15.sp,
//                                            fontWeight = FontWeight.Medium,
//                                            color = Color(0xFF666666)
//                                        )
//                                    }
//                                    Text(
//                                        text = "${(uploadProgress * 100).toInt()}%",
//                                        fontSize = 18.sp,
//                                        fontWeight = FontWeight.Bold,
//                                        color = Color(0xFF667EEA)
//                                    )
//                                }
//                                Spacer(modifier = Modifier.height(14.dp))
//                                LinearProgressIndicator(
//                                    progress = { uploadProgress },
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .height(10.dp)
//                                        . clip(RoundedCornerShape(5.dp)),
//                                    color = Color(0xFF667EEA),
//                                    trackColor = Color(0xFFE0E0E0)
//                                )
//                            }
//                        }
//                        Spacer(modifier = Modifier.height(20.dp))
//                    }
//
//                    // BUTTON GHI
//                    Button(
//                        onClick = {
//                            scope.launch {
//                                isWriting = true
//                                status = ""
//                                uploadProgress = 0f
//
//                                try {
//                                    val writeSuccess = smartCardManager.writeCustomerInfo(
//                                         name, dateOfBirth, phoneNumber
//                                    )
//
//                                    if (! writeSuccess) {
//                                        status = "❌ Lỗi ghi thông tin cơ bản"
//                                        isWriting = false
//                                        return@launch
//                                    }
//
//                                    status = "✅ Đã ghi thông tin cơ bản..."
//                                    delay(500)
//
//                                    imageData?. let { data ->
//                                        status = "📤 Đang upload ảnh..."
//                                        uploadProgress = 0.1f
//
//                                        if (! smartCardManager.startPhotoWrite()) {
//                                            status = "❌ Lỗi khởi tạo upload ảnh"
//                                            isWriting = false
//                                            return@launch
//                                        }
//
//                                        delay(200)
//                                        uploadProgress = 0.2f
//
//                                        val chunkSize = 200
//                                        var offset = 0
//                                        val totalChunks = (data.size + chunkSize - 1) / chunkSize
//
//                                        var chunkIndex = 0
//                                        while (offset < data.size) {
//                                            val end = minOf(offset + chunkSize, data.size)
//                                            val chunk = data.copyOfRange(offset, end)
//
//                                            val success = smartCardManager.writePhotoChunk(chunk)
//                                            if (!success) {
//                                                status = "❌ Lỗi upload chunk ${chunkIndex + 1}/$totalChunks"
//                                                isWriting = false
//                                                return@launch
//                                            }
//
//                                            offset = end
//                                            chunkIndex++
//                                            uploadProgress = 0.2f + (chunkIndex. toFloat() / totalChunks) * 0.7f
//                                            delay(50)
//                                        }
//
//                                        uploadProgress = 0.9f
//                                        delay(200)
//
//                                        if (!smartCardManager.finishPhotoWrite()) {
//                                            status = "❌ Lỗi hoàn tất upload ảnh"
//                                            isWriting = false
//                                            return@launch
//                                        }
//
//                                        uploadProgress = 1.0f
//                                        delay(300)
//
//                                        status = "✅ Upload ảnh thành công!"
//                                    } ?: run {
//                                        status = "✅ Ghi thông tin thành công!"
//                                    }
//
//                                    delay(1000)
//
//                                    // ✅ TẠO VÀ UPLOAD RSA KEY
//                                    status = "🔐 Đang tạo RSA key..."
//                                    delay(300)
//
//                                    try {
//                                        // Set Customer ID for RSA
//                                        if (!smartCardManager.setCustomerID(customerID)) {
//                                            status = "⚠️ Không thể set Customer ID cho RSA"
//                                        } else {
//                                            // Generate RSA-1024 keypair
//                                            val keyGen = KeyPairGenerator.getInstance("RSA")
//                                            keyGen.initialize(1024, SecureRandom())
//                                            val keyPair = keyGen.generateKeyPair()
//
//                                            val privateKey = keyPair.private as RSAPrivateKey
//                                            val publicKey = keyPair.public as RSAPublicKey
//
//                                            // Extract modulus and exponent (128 bytes each for RSA-1024)
//                                            val modulusBytes = privateKey.modulus.toByteArray()
//                                            val exponentBytes = privateKey.privateExponent.toByteArray()
//
//                                            // Pad or trim to exactly 128 bytes
//                                            val modulusPadded = ByteArray(128)
//                                            val exponentPadded = ByteArray(128)
//
//                                            val modulusStart = maxOf(0, modulusBytes.size - 128)
//                                            val modulusLength = minOf(128, modulusBytes.size)
//                                            System.arraycopy(modulusBytes, modulusStart, modulusPadded, 128 - modulusLength, modulusLength)
//
//                                            val exponentStart = maxOf(0, exponentBytes.size - 128)
//                                            val exponentLength = minOf(128, exponentBytes.size)
//                                            System.arraycopy(exponentBytes, exponentStart, exponentPadded, 128 - exponentLength, exponentLength)
//
//                                            status = "📤 Đang upload private key lên thẻ..."
//                                            delay(300)
//
//                                            // Upload private key to card
//                                            val expSuccess = smartCardManager.setRSAExponent(exponentPadded)
//                                            val modSuccess = smartCardManager.setRSAModulus(modulusPadded)
//
//                                            if (expSuccess && modSuccess) {
//                                                // Save public key to server
//                                                status = "💾 Đang lưu public key lên server..."
//                                                delay(300)
//
//                                                val publicKeyPEM = publicKeyToPEM(publicKey)
//                                                val registerSuccess = registerPublicKeyToServer(customerID, publicKeyPEM)
//
//                                                if (registerSuccess) {
//                                                    status = "✅ Hoàn tất! Đã ghi ${if (imageData != null) "thông tin + ảnh + RSA key" else "thông tin + RSA key"}"
//                                                } else {
//                                                    status = "⚠️ Đã upload key lên thẻ nhưng lỗi lưu public key lên server"
//                                                }
//                                            } else {
//                                                status = "⚠️ Ghi thông tin thành công nhưng lỗi upload RSA key"
//                                            }
//                                        }
//                                    } catch (rsaException: Exception) {
//                                        status = "⚠️ Ghi thông tin thành công nhưng lỗi tạo RSA: ${rsaException.message}"
//                                        rsaException.printStackTrace()
//                                    }
//
//                                } catch (e: Exception) {
//                                    status = "❌ Lỗi:  ${e.message}"
//                                    e.printStackTrace()
//                                } finally {
//                                    isWriting = false
//                                    uploadProgress = 0f
//                                }
//                            }
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(60.dp),
//                        enabled = ! isWriting &&
//                                customerID.isNotEmpty() &&
//                                name.isNotEmpty() &&
//                                dateOfBirth. isNotEmpty() &&
//                                phoneNumber.isNotEmpty(),
//                        shape = RoundedCornerShape(18.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFF4CAF50),
//                            disabledContainerColor = Color(0xFFBDBDBD)
//                        ),
//                        elevation = ButtonDefaults.buttonElevation(
//                            defaultElevation = 6.dp,
//                            pressedElevation = 10.dp
//                        )
//                    ) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement. Center
//                        ) {
//                            if (isWriting) {
//                                CircularProgressIndicator(
//                                    modifier = Modifier.size(26.dp),
//                                    color = Color. White,
//                                    strokeWidth = 3.dp
//                                )
//                                Spacer(modifier = Modifier.width(14.dp))
//                                Text(
//                                    text = "Đang xử lý...",
//                                    fontSize = 19.sp,
//                                    fontWeight = FontWeight.Bold
//                                )
//                            } else {
//                                Icon(
//                                    imageVector = Icons.Default.Save,
//                                    contentDescription = null,
//                                    modifier = Modifier.size(26.dp)
//                                )
//                                Spacer(modifier = Modifier. width(12.dp))
//                                Text(
//                                    text = "💾 Ghi vào thẻ",
//                                    fontSize = 19.sp,
//                                    fontWeight = FontWeight.Bold
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//
//            // STATUS
//            if (status.isNotEmpty()) {
//                Spacer(modifier = Modifier. height(16.dp))
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .shadow(8.dp, RoundedCornerShape(20.dp)),
//                    shape = RoundedCornerShape(20.dp),
//                    colors = CardDefaults.cardColors(
//                        containerColor = when {
//                            status.startsWith("✅") -> Color(0xFFE8F5E9)
//                            status.startsWith("⚠️") -> Color(0xFFFFF8E1)
//                            else -> Color(0xFFFFEBEE)
//                        }
//                    )
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(20.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = when {
//                                status.startsWith("✅") -> "✅"
//                                status.startsWith("⚠️") -> "⚠️"
//                                else -> "❌"
//                            },
//                            fontSize = 28.sp
//                        )
//                        Spacer(modifier = Modifier.width(14.dp))
//                        Text(
//                            text = status. substring(2),
//                            fontSize = 15.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = when {
//                                status.startsWith("✅") -> Color(0xFF4CAF50)
//                                status.startsWith("⚠️") -> Color(0xFFFFA726)
//                                else -> Color(0xFFE53935)
//                            },
//                            modifier = Modifier. weight(1f)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//private fun BufferedImage.toComposeImageBitmap(): ImageBitmap {
//    val baos = ByteArrayOutputStream()
//    ImageIO.write(this, "PNG", baos)
//    val bytes = baos.toByteArray()
//    return org.jetbrains.skia.Image.makeFromEncoded(bytes).toComposeImageBitmap()
//}
//
///**
// * Convert RSA public key to PEM format
// */
//private fun publicKeyToPEM(publicKey: RSAPublicKey): String {
//    val encoded = publicKey.encoded
//    val base64 = Base64.getEncoder().encodeToString(encoded)
//    val pem = StringBuilder()
//    pem.append("-----BEGIN PUBLIC KEY-----\n")
//
//    // Split into 64-character lines
//    var index = 0
//    while (index < base64.length) {
//        val end = minOf(index + 64, base64.length)
//        pem.append(base64.substring(index, end))
//        pem.append("\n")
//        index = end
//    }
//
//    pem.append("-----END PUBLIC KEY-----")
//    return pem.toString()
//}
//
///**
// * Register public key to server
// */
//private suspend fun registerPublicKeyToServer(customerId: String, publicKeyPEM: String): Boolean {
//    return withContext(Dispatchers.IO) {
//        try {
//            val url = java.net.URL("${ServerConfig.baseUrl}/rsa/register-key")
//            val connection = url.openConnection() as java.net.HttpURLConnection
//
//            connection.requestMethod = "POST"
//            connection.setRequestProperty("Content-Type", "application/json")
//            connection.doOutput = true
//
//            val jsonPayload = """
//                {
//                    "customerId": "$customerId",
//                    "publicKey": "${publicKeyPEM.replace("\n", "\\n")}"
//                }
//            """.trimIndent()
//
//            connection.outputStream.use { os ->
//                os.write(jsonPayload.toByteArray())
//            }
//
//            val responseCode = connection.responseCode
//            val stream = try {
//                if (responseCode in 200..299) connection.inputStream else connection.errorStream
//            } catch (e: Exception) { null }
//            val responseBody = stream?.bufferedReader()?.use { it.readText() }
//
//            println("Register public key response: $responseCode")
//            if (!responseBody.isNullOrBlank()) {
//                println("Register public key body: $responseBody")
//            }
//
//            (responseCode == 201 || responseCode == 200)
//        } catch (e: Exception) {
//            println("Error registering public key: ${e.message}")
//            e.printStackTrace()
//            false
//        }
//    }
//}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWriteInfoScreen(
    smartCardManager: SmartCardManager,
    onBack: () -> Unit
) {
    val customerID = remember {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("ddMMyyHHmmss")
        "KH${now.format(formatter)}"
    }
    val cardID = remember {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("ddMMyyHHmmss")
        "CARD${now.format(formatter)}"
    }

    var name by remember { mutableStateOf("") }
    var dateOfBirthState by remember {
        mutableStateOf(TextFieldValue(text = "", selection = TextRange(0)))
    }
    var dateOfBirth by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var imageData by remember { mutableStateOf<ByteArray?>(null) }
    var isWriting by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var uploadProgress by remember { mutableStateOf(0f) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val rsaRepository = remember { RSARepository() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFE5EC),  // ✅ ĐỔI MÀU
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
                .verticalScroll(scrollState)  // ✅ THÊM SCROLL TOÀN MÀN
                .padding(horizontal = 80.dp, vertical = 20.dp)  // ✅ TĂNG PADDING
        ) {
            // ✅ HEADER
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp)),  // ✅ TĂNG
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color. Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF6B9D),  // ✅ ĐỔI MÀU
                                    Color(0xFFC06FBB),
                                    Color(0xFFFEC163)
                                )
                            )
                        )
                        .padding(20.dp)  // ✅ TĂNG
                ) {
                    Row(
                        modifier = Modifier. fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(48.dp)  // ✅ TĂNG
                                .clip(CircleShape)
                                .background(Color.White. copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier. size(26.dp)  // ✅ TĂNG
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "✨ Ghi Thông Tin Khách Hàng",
                                fontSize = 22.sp,  // ✅ TĂNG
                                fontWeight = FontWeight.ExtraBold,
                                color = Color. White
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
                                    Text("🏷️", fontSize = 18.sp)  // ✅ TĂNG
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Mã hôm nay: KH",
                                        fontSize = 14.sp,  // ✅ TĂNG
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(60.dp)  // ✅ TĂNG
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✍️", fontSize = 32.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier. height(20.dp))

            // ✅ FORM CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()  // ✅ BỎ weight(1f)
                    .shadow(12.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp)  // ✅ TĂNG
                ) {
                    // ✅ PHOTO SECTION
                    Card(
                        modifier = Modifier. fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3E0)  // ✅ ĐỔI MÀU
                        ),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement. Center
                            ) {
                                Text("📸", fontSize = 22.sp)  // ✅ TĂNG
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Ảnh khách hàng",
                                    fontSize = 20.sp,  // ✅ TĂNG
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFF6B00)  // ✅ ĐỔI MÀU
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Box(
                                modifier = Modifier
                                    .size(150.dp)  // ✅ TĂNG
                                    .shadow(12.dp, CircleShape)
                                    . clip(CircleShape)
                                    .background(
                                        brush = Brush. radialGradient(
                                            colors = listOf(
                                                Color(0xFFFF6B9D),  // ✅ ĐỔI MÀU
                                                Color(0xFFFFA07A),
                                                Color(0xFFFFD700)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedImage != null) {
                                    Image(
                                        bitmap = selectedImage!!,
                                        contentDescription = "Customer Photo",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),  // ✅ TĂNG
                                            tint = Color. White
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Chưa có ảnh",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    val dialog = FileDialog(null as Frame?, "Chọn ảnh", FileDialog.LOAD)
                                    dialog.setFilenameFilter { _, name ->
                                        name.lowercase().endsWith(".jpg") ||
                                                name.lowercase().endsWith(".jpeg") ||
                                                name.lowercase().endsWith(".png")
                                    }
                                    dialog.isVisible = true

                                    val dir = dialog.directory
                                    val file = dialog.file

                                    if (dir != null && file != null) {
                                        scope.launch {
                                            try {
                                                val imageFile = File(dir, file)
                                                val bufferedImage = ImageIO.read(imageFile)

                                                val maxWidth = 200
                                                val maxHeight = 200
                                                val scaledImage = if (bufferedImage.width > maxWidth || bufferedImage.height > maxHeight) {
                                                    val scale = minOf(
                                                        maxWidth.toFloat() / bufferedImage.width,
                                                        maxHeight.toFloat() / bufferedImage.height
                                                    )
                                                    val newWidth = (bufferedImage. width * scale).toInt()
                                                    val newHeight = (bufferedImage.height * scale).toInt()

                                                    val scaled = java.awt.image.BufferedImage(newWidth, newHeight, bufferedImage.type)
                                                    val g = scaled.createGraphics()
                                                    g.drawImage(bufferedImage, 0, 0, newWidth, newHeight, null)
                                                    g.dispose()
                                                    scaled
                                                } else {
                                                    bufferedImage
                                                }

                                                val outputStream = ByteArrayOutputStream()
                                                ImageIO.write(scaledImage, "jpg", outputStream)
                                                val bytes = outputStream.toByteArray()

                                                if (bytes.size > 8000) {
                                                    var quality = 0.7f
                                                    var compressedBytes = bytes

                                                    while (compressedBytes.size > 8000 && quality > 0.1f) {
                                                        val baos = ByteArrayOutputStream()
                                                        val writer = ImageIO.getImageWritersByFormatName("jpg").next()
                                                        val ios = ImageIO.createImageOutputStream(baos)
                                                        writer.output = ios

                                                        val param = writer.defaultWriteParam
                                                        param.compressionMode = javax.imageio.ImageWriteParam.MODE_EXPLICIT
                                                        param.compressionQuality = quality
                                                        writer.write(null, javax.imageio.IIOImage(scaledImage, null, null), param)
                                                        writer. dispose()
                                                        ios.close()

                                                        compressedBytes = baos.toByteArray()
                                                        quality -= 0.1f
                                                    }

                                                    if (compressedBytes.size > 8000) {
                                                        status = "❌ Ảnh quá lớn!  Vui lòng chọn ảnh khác."
                                                        return@launch
                                                    }

                                                    imageData = compressedBytes
                                                } else {
                                                    imageData = bytes
                                                }

                                                selectedImage = scaledImage. toComposeImageBitmap()
                                                status = "✅ Đã chọn ảnh thành công!"

                                            } catch (e:  Exception) {
                                                status = "❌ Lỗi đọc ảnh: ${e. message}"
                                                e. printStackTrace()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF6B9D)  // ✅ ĐỔI MÀU
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 6.dp,
                                    pressedElevation = 12.dp
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement. Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Upload,
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (selectedImage == null) "📁 Chọn ảnh từ máy" else "🔄 Đổi ảnh",
                                        fontSize = 16.sp,  // ✅ TĂNG
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier. height(28.dp))

                    HorizontalDivider(
                        color = Color(0xFFFFAB91),  // ✅ ĐỔI MÀU
                        thickness = 2.dp,
                        modifier = Modifier. padding(vertical = 10.dp)
                    )

                    Spacer(modifier = Modifier. height(20.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📝", fontSize = 24.sp)  // ✅ TĂNG
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Thông tin cơ bản",
                            fontSize = 20.sp,  // ✅ TĂNG
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF6B00)  // ✅ ĐỔI MÀU
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // MÃ KHÁCH HÀNG
                    Card(
                        modifier = Modifier. fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8F5E9)
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(28.dp)  // ✅ TĂNG
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Mã khách hàng (tự động)",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = customerID,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // HỌ VÀ TÊN
                    OutlinedTextField(
                        value = name,
                        onValueChange = { newValue ->
                            // ✅ CHỈ CHO PHÉP CHỮ CÁI + KHOẢNG TRẮNG
                            val filtered = newValue.filter { it.isLetter() || it == ' ' }
                            if (filtered.length <= 50) {
                                name = filtered
                            }
                        },
                        label = { Text("Họ và tên", fontWeight = FontWeight.Bold, fontSize = 15.sp) },  // ✅ TĂNG
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFFFF6B9D),  // ✅ ĐỔI MÀU
                                modifier = Modifier.size(24.dp)  // ✅ TĂNG
                            )
                        },
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),  // ✅ TĂNG
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),  // ✅ TĂNG
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF6B9D),
                            focusedLabelColor = Color(0xFFFF6B9D),
                            focusedLeadingIconColor = Color(0xFFFF6B9D),
                            cursorColor = Color(0xFFFF6B9D),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        supportingText = {
                            if (name.isNotEmpty() && !name.all { it.isLetter() || it == ' ' }) {
                                Text(
                                    text = "⚠️ Chỉ được chứa chữ cái",
                                    fontSize = 12.sp,
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "💡 Chỉ nhập chữ cái và khoảng trắng",
                                    fontSize = 12.sp,
                                    color = Color(0xFF9575CD)
                                )
                            }
                        },
                        isError = name.isNotEmpty() && !name.all { it.isLetter() || it == ' ' }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // NGÀY SINH
                    OutlinedTextField(
                        value = dateOfBirthState,
                        onValueChange = { newValue ->
                            val digitsOnly = newValue.text.filter { it.isDigit() }

                            if (digitsOnly. length <= 8) {
                                val formatted = when {
                                    digitsOnly.isEmpty() -> ""
                                    digitsOnly.length <= 2 -> digitsOnly
                                    digitsOnly.length <= 4 -> "${digitsOnly.take(2)}/${digitsOnly.drop(2)}"
                                    else -> "${digitsOnly.take(2)}/${digitsOnly.substring(2, 4)}/${digitsOnly.drop(4)}"
                                }

                                // ✅ Đặt cursor ở cuối chuỗi
                                dateOfBirthState = TextFieldValue(
                                    text = formatted,
                                    selection = TextRange(formatted.length)
                                )
                                dateOfBirth = formatted  // Lưu String để gửi lên thẻ
                            }
                        },
                        label = { Text("Ngày sinh", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        placeholder = { Text("13/12/2025", color = Color.Gray, fontSize = 15.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFFFFA726),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFA726),
                            focusedLabelColor = Color(0xFFFFA726),
                            focusedLeadingIconColor = Color(0xFFFFA726),
                            cursorColor = Color(0xFFFFA726),
                            unfocusedTextColor = Color.Black,  // ✅ THÊM
                            focusedTextColor = Color.Black      // ✅ THÊM
                        ),
                        supportingText = {
                            Text(
                                text = "💡 Nhập số, tự động thêm /",
                                fontSize = 12.sp,
                                color = Color(0xFF9575CD)
                            )
                        }
                    )

//                    Spacer(modifier = Modifier.height(18.dp))

                    // SỐ ĐIỆN THOẠI
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { newValue ->
                            // ✅ CHỈ LẤY SỐ
                            val digitsOnly = newValue.filter { it.isDigit() }

                            // ✅ VALIDATION:  Bắt đầu bằng 0, tối đa 10 số
                            phoneNumber = when {
                                digitsOnly.isEmpty() -> ""
                                digitsOnly[0] != '0' -> phoneNumber  // Giữ nguyên nếu không bắt đầu bằng 0
                                digitsOnly. length > 10 -> phoneNumber  // Giữ nguyên nếu quá 10 số
                                else -> digitsOnly  // Hợp lệ → Cập nhật
                            }
                        },
                        label = { Text("Số điện thoại", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = Color(0xFF66BB6A),  // ✅ ĐỔI MÀU
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        textStyle = LocalTextStyle.current. copy(fontSize = 16.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF66BB6A),
                            focusedLabelColor = Color(0xFF66BB6A),
                            focusedLeadingIconColor = Color(0xFF66BB6A),
                            cursorColor = Color(0xFF66BB6A),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black

                        ),
                        supportingText = {
                            when {
                                phoneNumber.isNotEmpty() && phoneNumber[0] != '0' -> {
                                    Text(
                                        text = "⚠️ Số điện thoại phải bắt đầu từ số 0",
                                        fontSize = 12.sp,
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                phoneNumber.length in 1..9 -> {
                                    Text(
                                        text = "⚠️ Cần đủ 10 số (còn ${10 - phoneNumber.length} số)",
                                        fontSize = 12.sp,
                                        color = Color(0xFFFFA726),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                else -> {
                                    Text(
                                        text = "💡 Bắt đầu bằng 0, tối đa 10 số",
                                        fontSize = 12.sp,
                                        color = Color(0xFF9575CD)
                                    )
                                }
                            }
                        },
                        isError = phoneNumber. isNotEmpty() && (phoneNumber[0] != '0' || phoneNumber.length < 10)

                    )

                    Spacer(modifier = Modifier. height(28.dp))

                    // PROGRESS BAR
                    if (isWriting && uploadProgress > 0f) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF3E0)
                            ),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("⏳", fontSize = 22.sp)  // ✅ TĂNG
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Đang tải lên.. .",
                                            fontSize = 16.sp,  // ✅ TĂNG
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF666666)
                                        )
                                    }
                                    Text(
                                        text = "${(uploadProgress * 100).toInt()}%",
                                        fontSize = 20.sp,  // ✅ TĂNG
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFF6B9D)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                LinearProgressIndicator(
                                    progress = { uploadProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(12.dp)  // ✅ TĂNG
                                        .clip(RoundedCornerShape(6.dp)),
                                    color = Color(0xFFFF6B9D),
                                    trackColor = Color(0xFFFFE0E0)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // BUTTON GHI

                    Button(
                        onClick = {
                            scope.launch {
                                isWriting = true
                                status = ""
                                uploadProgress = 0f

                                try {
                                    // ✅ SỬA:  Bỏ customerID khỏi writeCustomerInfo
                                    val writeSuccess = smartCardManager.writeCustomerInfo(
                                        customerID = customerID,
                                        cardId = cardID,
                                        name = name,
                                        dateOfBirth = dateOfBirth,
                                        phoneNumber = phoneNumber
                                    )

                                    if (! writeSuccess) {
                                        status = "❌ Lỗi ghi thông tin cơ bản"
                                        isWriting = false
                                        return@launch
                                    }

                                    status = "✅ Đã ghi thông tin cơ bản..."
                                    delay(500)

                                    imageData?.let { data ->
                                        status = "📤 Đang upload ảnh..."
                                        uploadProgress = 0.1f

                                        if (! smartCardManager.startPhotoWrite()) {
                                            status = "❌ Lỗi khởi tạo upload ảnh"
                                            isWriting = false
                                            return@launch
                                        }

                                        delay(200)
                                        uploadProgress = 0.2f

                                        val chunkSize = 200
                                        var offset = 0
                                        val totalChunks = (data.size + chunkSize - 1) / chunkSize

                                        var chunkIndex = 0
                                        while (offset < data.size) {
                                            val end = minOf(offset + chunkSize, data.size)
                                            val chunk = data.copyOfRange(offset, end)

                                            val success = smartCardManager.writePhotoChunk(chunk)
                                            if (!success) {
                                                status = "❌ Lỗi upload chunk ${chunkIndex + 1}/$totalChunks"
                                                isWriting = false
                                                return@launch
                                            }

                                            offset = end
                                            chunkIndex++
                                            uploadProgress = 0.2f + (chunkIndex. toFloat() / totalChunks) * 0.7f
                                            delay(50)
                                        }

                                        uploadProgress = 0.9f
                                        delay(200)

                                        if (!smartCardManager.finishPhotoWrite()) {
                                            status = "❌ Lỗi hoàn tất upload ảnh"
                                            isWriting = false
                                            return@launch
                                        }

                                        uploadProgress = 1.0f
                                        delay(300)

                                        status = "✅ Upload ảnh thành công!"
                                    } ?: run {
                                        status = "✅ Ghi thông tin thành công!"
                                    }

                                    delay(1000)

                                    // ✅ SỬA: Gọi thẻ tự generate RSA keypair
                                    status = "🔐 Đang set Customer ID..."
                                    delay(300)

                                    try {
                                        if (!smartCardManager.setCustomerID(customerID)) {
                                            status = "⚠️ Không thể set Customer ID"
                                        } else {
                                            status = "🔐 Đang tạo RSA keypair trong thẻ..."
                                            delay(300)
                                            
                                            // Generate keypair trong thẻ (mất vài giây)
                                            val generateSuccess = withContext(Dispatchers.IO) {
                                                smartCardManager.generateRSAKeyPair()
                                            }
                                            
                                            if (!generateSuccess) {
                                                status = "⚠️ Lỗi tạo RSA keypair trong thẻ"
                                            } else {
                                                status = "📤 Đang lấy public key từ thẻ..."
                                                delay(300)
                                                
                                                // Lấy public key từ thẻ
                                                val publicKeyPEM = withContext(Dispatchers.IO) {
                                                    smartCardManager.getPublicKeyAsPEM()
                                                }
                                                
                                                if (publicKeyPEM == null) {
                                                    status = "⚠️ Không thể lấy public key từ thẻ"
                                                } else {
                                                    status = "💾 Đang lưu public key lên server..."
                                                    delay(300)
                                                    
                                                    val registerSuccess = registerPublicKeyToServer(customerID, publicKeyPEM)
                                                    
                                                    if (registerSuccess) {
                                                        status = "✅ Hoàn tất! Đã ghi ${if (imageData != null) "thông tin + ảnh + RSA key" else "thông tin + RSA key"}"
                                                    } else {
                                                        status = "⚠️ Đã tạo key trong thẻ nhưng lỗi lưu public key lên server"
                                                    }
                                                }
                                            }
                                        }
                                    } catch (rsaException: Exception) {
                                        status = "⚠️ Ghi thông tin thành công nhưng lỗi tạo RSA: ${rsaException.message}"
                                        rsaException.printStackTrace()
                                    }

                                } catch (e: Exception) {
                                    status = "❌ Lỗi:  ${e.message}"
                                    e.printStackTrace()
                                } finally {
                                    isWriting = false
                                    uploadProgress = 0f
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        enabled = ! isWriting &&
                                customerID.isNotEmpty() &&
                                name.isNotEmpty() &&
                                dateOfBirth.isNotEmpty() &&
                                phoneNumber. isNotEmpty(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            disabledContainerColor = Color(0xFFE0E0E0)
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
                            if (isWriting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = Color. White,
                                    strokeWidth = 4.dp
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = "Đang xử lý.. .",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = "Ghi vào thẻ",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            // STATUS
            if (status.isNotEmpty()) {
                Spacer(modifier = Modifier. height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            status.startsWith("✅") -> Color(0xFFE8F5E9)
                            status.startsWith("⚠️") -> Color(0xFFFFF3E0)
                            status.startsWith("🔐") -> Color(0xFFE3F2FD)
                            status.startsWith("📤") -> Color(0xFFFFF4E6)
                            status.startsWith("💾") -> Color(0xFFE8EAF6)
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
                                status.startsWith("⚠️") -> "⚠️"
                                status.startsWith("🔐") -> "🔐"
                                status.startsWith("📤") -> "📤"
                                status.startsWith("💾") -> "💾"
                                else -> "❌"
                            },
                            fontSize = 28.sp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = status. substring(2),
                            fontSize = 16.sp,  // ✅ TĂNG
                            fontWeight = FontWeight.Bold,
                            color = when {
                                status.startsWith("✅") -> Color(0xFF4CAF50)
                                status.startsWith("⚠️") -> Color(0xFFFFA726)
                                status.startsWith("🔐") -> Color(0xFF2196F3)
                                status.startsWith("📤") -> Color(0xFFFF9800)
                                status. startsWith("💾") -> Color(0xFF673AB7)
                                else -> Color(0xFFE53935)
                            },
                            modifier = Modifier. weight(1f)
                        )
                    }
                }
            }
        }
    }
}

private fun BufferedImage.toComposeImageBitmap(): ImageBitmap {
    val baos = ByteArrayOutputStream()
    ImageIO.write(this, "PNG", baos)
    val bytes = baos.toByteArray()
    return org.jetbrains.skia.Image. makeFromEncoded(bytes).toComposeImageBitmap()
}

private suspend fun registerPublicKeyToServer(customerId: String, publicKeyPEM: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("${ServerConfig.baseUrl}/rsa/register-key")
            val connection = url. openConnection() as java.net.HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection. doOutput = true

            val jsonPayload = """
                {
                    "customerId":  "$customerId",
                    "publicKey": "${publicKeyPEM. replace("\n", "\\n")}"
                }
            """.trimIndent()

            connection.outputStream.use { os ->
                os.write(jsonPayload.toByteArray())
            }

            val responseCode = connection.responseCode
            val stream = try {
                if (responseCode in 200..299) connection.inputStream else connection.errorStream
            } catch (e: Exception) { null }
            val responseBody = stream?.bufferedReader()?.use { it.readText() }

            println("Register public key response: $responseCode")
            if (! responseBody.isNullOrBlank()) {
                println("Register public key body: $responseBody")
            }

            (responseCode == 201 || responseCode == 200)
        } catch (e: Exception) {
            println("Error registering public key: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}
