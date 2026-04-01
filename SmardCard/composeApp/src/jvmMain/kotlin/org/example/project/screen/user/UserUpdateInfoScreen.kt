package org.example.project.screen.user

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.SmartCardManager
import org.jetbrains.skia.Image
import java.awt.FileDialog
import java.awt.Frame
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import org.example.project.screen.FloatingBubbles

//data class ValidationState(
//    val isValid: Boolean = true,
//    val errorMessage: String = ""
//)
//
//object ValidationUtils {
//    fun validateName(name: String): ValidationState {
//        return when {
//            name.isEmpty() -> ValidationState(false, "Họ tên không được để trống")
//            name.trim().length < 2 -> ValidationState(false, "Họ tên phải ít nhất 2 ký tự")
//            name.length > 50 -> ValidationState(false, "Họ tên không được quá 50 ký tự")
//            !name.matches(Regex("^[a-zA-ZÀ-ỹ\\s]+$")) -> ValidationState(false, "Họ tên chỉ được chứa chữ cái và khoảng trắng")
//            name.trim().split("\\s+".toRegex()).size < 2 -> ValidationState(false, "Vui lòng nhập đầy đủ họ và tên")
//            else -> ValidationState(true, "")
//        }
//    }
//
//    fun validateDateOfBirth(dateStr: String): ValidationState {
//        return when {
//            dateStr.isEmpty() -> ValidationState(false, "Ngày sinh không được để trống")
//            !dateStr.matches(Regex("^\\d{2}/\\d{2}/\\d{4}$")) -> ValidationState(false, "Định dạng ngày sinh: dd/mm/yyyy")
//            else -> {
//                try {
//                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//                    formatter.isLenient = false
//                    val date = formatter.parse(dateStr)
//                    val calendar = Calendar.getInstance()
//                    calendar.time = date!!
//
//                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
//                    val birthYear = calendar.get(Calendar.YEAR)
//                    val age = currentYear - birthYear
//
//                    when {
//                        age < 16 -> ValidationState(false, "Khách hàng phải từ 16 tuổi trở lên")
//                        age > 100 -> ValidationState(false, "Tuổi không hợp lệ")
//                        else -> ValidationState(true, "")
//                    }
//                } catch (e: Exception) {
//                    ValidationState(false, "Ngày sinh không hợp lệ")
//                }
//            }
//        }
//    }
//
//    fun validatePhoneNumber(phoneNumber: String): ValidationState {
//        return when {
//            phoneNumber.isEmpty() -> ValidationState(false, "Số điện thoại không được để trống")
//            !phoneNumber.matches(Regex("^0\\d{9}$")) -> ValidationState(false, "Số điện thoại phải có 10 số và bắt đầu bằng 0")
//            !phoneNumber.matches(Regex("^(032|033|034|035|036|037|038|039|096|097|098|086|083|084|085|081|082|088|091|094|070|079|077|076|078|090|093|089|056|058|092|059|099)[0-9]{7}$")) ->
//                ValidationState(false, "Đầu số điện thoại không hợp lệ")
//            else -> ValidationState(true, "")
//        }
//    }
//
//    fun validateImage(imageBytes: ByteArray?): ValidationState {
//        return when {
//            imageBytes == null -> ValidationState(false, "Vui lòng chọn ảnh khách hàng")
//            imageBytes.size > 8192 -> ValidationState(false, "Kích thước ảnh quá lớn (tối đa 8KB)")
//            else -> ValidationState(true, "")
//        }
//    }
//
//    fun formatDateInput(input: String): String {
//        val cleaned = input.replace(Regex("[^\\d]"), "")
//        return when {
//            cleaned.length <= 2 -> cleaned
//            cleaned.length <= 4 -> "${cleaned.substring(0, 2)}/${cleaned.substring(2)}"
//            cleaned.length <= 8 -> "${cleaned.substring(0, 2)}/${cleaned.substring(2, 4)}/${cleaned.substring(4)}"
//            else -> "${cleaned.substring(0, 2)}/${cleaned.substring(2, 4)}/${cleaned.substring(4, 8)}"
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun UserUpdateInfoScreen(
//    smartCardManager: SmartCardManager,
//    onBack: () -> Unit
//) {
//    var name by remember { mutableStateOf("") }
//    var dateOfBirth by remember { mutableStateOf(TextFieldValue("")) }
//    var phoneNumber by remember { mutableStateOf("") }
//    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
//    var selectedImage by remember { mutableStateOf<ImageBitmap?>(null) }
//    var updateStatus by remember { mutableStateOf("") }
//    var isUpdating by remember { mutableStateOf(false) }
//    var isLoading by remember { mutableStateOf(true) }
//
//    // Validation states
//    var nameValidation by remember { mutableStateOf(ValidationState()) }
//    var dateValidation by remember { mutableStateOf(ValidationState()) }
//    var phoneValidation by remember { mutableStateOf(ValidationState()) }
//    var imageValidation by remember { mutableStateOf(ValidationState()) }
//    var showValidationErrors by remember { mutableStateOf(false) }
//
//    val scope = rememberCoroutineScope()
//
//    // Load current data from card
//    LaunchedEffect(Unit) {
//        scope.launch {
//            val info = try {
//                smartCardManager.readCustomerInfo()
//            } catch (e: Exception) {
//                updateStatus = "❌ Lỗi đọc thông tin: ${e.message}"
//                isLoading = false
//                return@launch
//            }
//
//            val imageData = try {
//                smartCardManager.readCustomerImage()
//            } catch (e: Exception) {
//                null
//            }
//
//            name = info["name"] ?: ""
//            dateOfBirth = TextFieldValue(info["dateOfBirth"] ?: "")
//            phoneNumber = info["phoneNumber"] ?: ""
//            selectedImageBytes = imageData
//
//            // Convert image bytes to ImageBitmap for display
//            imageData?.let { bytes ->
//                try {
//                    val skiaBitmap = Image.makeFromEncoded(bytes)
//                    selectedImage = skiaBitmap.toComposeImageBitmap()
//                } catch (e: Exception) {
//                    println("Error converting image: ${e.message}")
//                }
//            }
//
//            // Validate loaded data
//            nameValidation = ValidationUtils.validateName(name)
//            dateValidation = ValidationUtils.validateDateOfBirth(dateOfBirth.text)
//            phoneValidation = ValidationUtils.validatePhoneNumber(phoneNumber)
//            imageValidation = ValidationUtils.validateImage(selectedImageBytes)
//
//            isLoading = false
//        }
//    }
//
//    fun validateField(field: String, value: String, imageBytes: ByteArray? = null) {
//        when (field) {
//            "name" -> nameValidation = ValidationUtils.validateName(value)
//            "dateOfBirth" -> dateValidation = ValidationUtils.validateDateOfBirth(value)
//            "phoneNumber" -> phoneValidation = ValidationUtils.validatePhoneNumber(value)
//            "image" -> imageValidation = ValidationUtils.validateImage(imageBytes)
//        }
//    }
//
//    val isFormValid = nameValidation.isValid &&
//            dateValidation.isValid &&
//            phoneValidation.isValid &&
//            imageValidation.isValid &&
//            name.isNotEmpty() &&
//            dateOfBirth.text.isNotEmpty() &&
//            phoneNumber.isNotEmpty() &&
//            selectedImageBytes != null
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
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp)
//        ) {
//            // Header with improved styling
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .shadow(16.dp, RoundedCornerShape(32.dp)),
//                shape = RoundedCornerShape(32.dp),
//                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
//            ) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .background(
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
//                                .background(Color.White.copy(alpha = 0.25f))
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.ArrowBack,
//                                contentDescription = "Back",
//                                tint = Color.White,
//                                modifier = Modifier.size(28.dp)
//                            )
//                        }
//
//                        Spacer(modifier = Modifier.width(16.dp))
//
//                        Column(modifier = Modifier.weight(1f)) {
//                            Text(
//                                text = "✨ Cập nhật thông tin",
//                                fontSize = 24.sp,
//                                fontWeight = FontWeight.ExtraBold,
//                                color = Color.White
//                            )
//                            Spacer(modifier = Modifier.height(4.dp))
//                            Text(
//                                text = "Chỉnh sửa thông tin cá nhân",
//                                fontSize = 15.sp,
//                                fontWeight = FontWeight.Medium,
//                                color = Color.White.copy(alpha = 0.95f)
//                            )
//                        }
//
//                        Box(
//                            modifier = Modifier
//                                .size(64.dp)
//                                .clip(CircleShape)
//                                .background(Color.White.copy(alpha = 0.25f)),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text("✏️", fontSize = 32.sp)
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            if (isLoading) {
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                        CircularProgressIndicator(color = Color(0xFF667EEA))
//                        Spacer(modifier = Modifier.height(16.dp))
//                        Text("Đang tải thông tin...", color = Color(0xFF666666))
//                    }
//                }
//            } else {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    // Left column - Form fields
//                    Card(
//                        modifier = Modifier.weight(2f),
//                        shape = RoundedCornerShape(20.dp),
//                        colors = CardDefaults.cardColors(containerColor = Color.White),
//                        elevation = CardDefaults.cardElevation(6.dp)
//                    ) {
//                        Column(modifier = Modifier.padding(20.dp)) {
//                            Row(verticalAlignment = Alignment.CenterVertically) {
//                                Text("📝", fontSize = 18.sp)
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text(
//                                    text = "Thông tin cá nhân",
//                                    fontSize = 16.sp,
//                                    fontWeight = FontWeight.Bold,
//                                    color = Color(0xFF333333)
//                                )
//                            }
//
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Name Field
//                            OutlinedTextField(
//                                value = name,
//                                onValueChange = {
//                                    name = it.take(50)
//                                    validateField("name", name)
//                                },
//                                label = { Text("Họ và tên", fontWeight = FontWeight.Medium) },
//                                leadingIcon = {
//                                    Icon(
//                                        imageVector = Icons.Default.Person,
//                                        contentDescription = null,
//                                        tint = Color(0xFF667EEA)
//                                    )
//                                },
//                                modifier = Modifier.fillMaxWidth(),
//                                singleLine = true,
//                                shape = RoundedCornerShape(16.dp),
//                                isError = showValidationErrors && !nameValidation.isValid,
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = if (nameValidation.isValid) Color(0xFF667EEA) else Color(0xFFE57373),
//                                    unfocusedBorderColor = if (showValidationErrors && !nameValidation.isValid) Color(0xFFE57373) else Color(0xFFE0E0E0),
//                                    focusedLabelColor = Color(0xFF667EEA),
//                                    cursorColor = Color(0xFF667EEA)
//                                )
//                            )
//                            if (showValidationErrors && !nameValidation.isValid) {
//                                Text(
//                                    text = nameValidation.errorMessage,
//                                    color = Color(0xFFE57373),
//                                    fontSize = 12.sp,
//                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
//                                )
//                            }
//
//                            Spacer(modifier = Modifier.height(12.dp))
//
//                            // Date of Birth Field
//                            OutlinedTextField(
//                                value = dateOfBirth,
//                                onValueChange = { newValue ->
//                                    val formatted = ValidationUtils.formatDateInput(newValue.text)
//                                    if (formatted.length <= 10) {
//                                        dateOfBirth = TextFieldValue(
//                                            text = formatted,
//                                            selection = TextRange(formatted.length)
//                                        )
//                                        validateField("dateOfBirth", formatted)
//                                    }
//                                },
//                                label = { Text("Ngày sinh", fontWeight = FontWeight.Medium) },
//                                placeholder = { Text("01/01/2000", color = Color.Gray) },
//                                leadingIcon = {
//                                    Icon(
//                                        imageVector = Icons.Default.CalendarToday,
//                                        contentDescription = null,
//                                        tint = Color(0xFF667EEA)
//                                    )
//                                },
//                                modifier = Modifier.fillMaxWidth(),
//                                singleLine = true,
//                                shape = RoundedCornerShape(16.dp),
//                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                                isError = showValidationErrors && !dateValidation.isValid,
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = if (dateValidation.isValid) Color(0xFF667EEA) else Color(0xFFE57373),
//                                    unfocusedBorderColor = if (showValidationErrors && !dateValidation.isValid) Color(0xFFE57373) else Color(0xFFE0E0E0),
//                                    focusedLabelColor = Color(0xFF667EEA),
//                                    cursorColor = Color(0xFF667EEA)
//                                ),
//                                supportingText = {
//                                    Text(
//                                        text = "💡 Định dạng: dd/mm/yyyy",
//                                        fontSize = 12.sp,
//                                        color = Color(0xFF9575CD)
//                                    )
//                                }
//                            )
//                            if (showValidationErrors && !dateValidation.isValid) {
//                                Text(
//                                    text = dateValidation.errorMessage,
//                                    color = Color(0xFFE57373),
//                                    fontSize = 12.sp,
//                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
//                                )
//                            }
//
//                            Spacer(modifier = Modifier.height(12.dp))
//
//                            // Phone Number Field
//                            OutlinedTextField(
//                                value = phoneNumber,
//                                onValueChange = {
//                                    if (it.all { char -> char.isDigit() } && it.length <= 10) {
//                                        phoneNumber = it
//                                        validateField("phoneNumber", phoneNumber)
//                                    }
//                                },
//                                label = { Text("Số điện thoại", fontWeight = FontWeight.Medium) },
//                                placeholder = { Text("0901234567", color = Color.Gray) },
//                                leadingIcon = {
//                                    Icon(
//                                        imageVector = Icons.Default.Phone,
//                                        contentDescription = null,
//                                        tint = Color(0xFF667EEA)
//                                    )
//                                },
//                                modifier = Modifier.fillMaxWidth(),
//                                singleLine = true,
//                                shape = RoundedCornerShape(16.dp),
//                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
//                                isError = showValidationErrors && !phoneValidation.isValid,
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = if (phoneValidation.isValid) Color(0xFF667EEA) else Color(0xFFE57373),
//                                    unfocusedBorderColor = if (showValidationErrors && !phoneValidation.isValid) Color(0xFFE57373) else Color(0xFFE0E0E0),
//                                    focusedLabelColor = Color(0xFF667EEA),
//                                    cursorColor = Color(0xFF667EEA)
//                                )
//                            )
//                            if (showValidationErrors && !phoneValidation.isValid) {
//                                Text(
//                                    text = phoneValidation.errorMessage,
//                                    color = Color(0xFFE57373),
//                                    fontSize = 12.sp,
//                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
//                                )
//                            }
//
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Update Button
//                            Button(
//                                onClick = {
//                                    showValidationErrors = true
//                                    if (isFormValid) {
//                                        isUpdating = true
//                                        updateStatus = "Đang cập nhật..."
//                                        scope.launch {
//                                            try {
//                                                val writeSuccess = smartCardManager.writeCustomerInfo(
//                                                    name, dateOfBirth.text, phoneNumber
//                                                )
//
//                                                if (writeSuccess && selectedImageBytes != null) {
//                                                    val imageSuccess = smartCardManager.writeCustomerImage(selectedImageBytes!!)
//                                                    if (imageSuccess) {
//                                                        updateStatus = "✅ Cập nhật thành công!"
//                                                    } else {
//                                                        updateStatus = "⚠️ Cập nhật thông tin OK, nhưng ảnh thất bại"
//                                                    }
//                                                } else if (writeSuccess) {
//                                                    updateStatus = "✅ Cập nhật thông tin thành công!"
//                                                } else {
//                                                    updateStatus = "❌ Cập nhật thất bại"
//                                                }
//                                            } catch (e: Exception) {
//                                                updateStatus = "❌ Lỗi: ${e.message}"
//                                            } finally {
//                                                isUpdating = false
//                                            }
//                                        }
//                                    } else {
//                                        updateStatus = "❌ Vui lòng kiểm tra lại thông tin"
//                                    }
//                                },
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(56.dp),
//                                enabled = !isUpdating,
//                                colors = ButtonDefaults.buttonColors(
//                                    containerColor = Color(0xFF4CAF50),
//                                    disabledContainerColor = Color(0xFFBDBDBD)
//                                ),
//                                shape = RoundedCornerShape(16.dp),
//                                elevation = ButtonDefaults.buttonElevation(
//                                    defaultElevation = 6.dp,
//                                    pressedElevation = 10.dp
//                                )
//                            ) {
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    horizontalArrangement = Arrangement.Center
//                                ) {
//                                    if (isUpdating) {
//                                        CircularProgressIndicator(
//                                            modifier = Modifier.size(24.dp),
//                                            color = Color.White,
//                                            strokeWidth = 2.dp
//                                        )
//                                        Spacer(modifier = Modifier.width(8.dp))
//                                        Text(
//                                            text = "Đang cập nhật...",
//                                            fontSize = 16.sp,
//                                            fontWeight = FontWeight.Bold
//                                        )
//                                    } else {
//                                        Icon(
//                                            imageVector = Icons.Default.Save,
//                                            contentDescription = null,
//                                            modifier = Modifier.size(24.dp)
//                                        )
//                                        Spacer(modifier = Modifier.width(8.dp))
//                                        Text(
//                                            text = "💾 Cập nhật thông tin",
//                                            fontSize = 16.sp,
//                                            fontWeight = FontWeight.Bold
//                                        )
//                                    }
//                                }
//                            }
//
//                            if (updateStatus.isNotEmpty()) {
//                                Spacer(modifier = Modifier.height(12.dp))
//                                Card(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    colors = CardDefaults.cardColors(
//                                        containerColor = when {
//                                            updateStatus.startsWith("✅") -> Color(0xFFE8F5E9)
//                                            updateStatus.startsWith("❌") -> Color(0xFFFFEBEE)
//                                            else -> Color(0xFFFFF3E0)
//                                        }
//                                    ),
//                                    shape = RoundedCornerShape(12.dp)
//                                ) {
//                                    Text(
//                                        text = updateStatus,
//                                        modifier = Modifier.padding(12.dp),
//                                        color = when {
//                                            updateStatus.startsWith("✅") -> Color(0xFF2E7D32)
//                                            updateStatus.startsWith("❌") -> Color(0xFFC62828)
//                                            else -> Color(0xFFE65100)
//                                        },
//                                        fontSize = 14.sp
//                                    )
//                                }
//                            }
//                        }
//                    }
//
//                    // Right column - Image (Updated to match AdminWriteInfoScreen style)
//                    Card(
//                        modifier = Modifier.weight(1f),
//                        shape = RoundedCornerShape(24.dp),
//                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
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
//                                horizontalArrangement = Arrangement.Center
//                            ) {
//                                Text("📸", fontSize = 24.sp)
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text(
//                                    text = "Ảnh đại diện",
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
//                                    .shadow(8.dp, CircleShape)
//                                    .clip(CircleShape)
//                                    .background(
//                                        brush = Brush.radialGradient(
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
//                                        modifier = Modifier.fillMaxSize(),
//                                        contentScale = ContentScale.Crop
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
//                                            tint = Color.White
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
//                                                    val newWidth = (bufferedImage.width * scale).toInt()
//                                                    val newHeight = (bufferedImage.height * scale).toInt()
//
//                                                    val scaled = BufferedImage(newWidth, newHeight, bufferedImage.type)
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
//                                                        param.compressionMode = ImageWriteParam.MODE_EXPLICIT
//                                                        param.compressionQuality = quality
//
//                                                        writer.write(null, javax.imageio.IIOImage(scaledImage, null, null), param)
//                                                        writer.dispose()
//                                                        ios.close()
//
//                                                        compressedBytes = baos.toByteArray()
//                                                        quality -= 0.1f
//                                                    }
//
//                                                    if (compressedBytes.size > 8000) {
//                                                        updateStatus = "❌ Ảnh quá lớn! Vui lòng chọn ảnh khác."
//                                                        return@launch
//                                                    }
//
//                                                    selectedImageBytes = compressedBytes
//                                                } else {
//                                                    selectedImageBytes = bytes
//                                                }
//
//                                                selectedImage = scaledImage.toComposeImageBitmap()
//                                                validateField("image", "", selectedImageBytes)
//                                                updateStatus = "✅ Đã chọn ảnh thành công!"
//
//                                            } catch (e: Exception) {
//                                                updateStatus = "❌ Lỗi đọc ảnh: ${e.message}"
//                                                e.printStackTrace()
//                                            }
//                                        }
//                                    }
//                                },
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(56.dp),
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
//                                    horizontalArrangement = Arrangement.Center
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
//
//                            if (showValidationErrors && !imageValidation.isValid) {
//                                Spacer(modifier = Modifier.height(8.dp))
//                                Text(
//                                    text = imageValidation.errorMessage,
//                                    color = Color(0xFFE57373),
//                                    fontSize = 12.sp
//                                )
//                            }
//                        }
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

data class ValidationState(
    val isValid: Boolean = true,
    val errorMessage:  String = ""
)

object ValidationUtils {
    fun validateName(name: String): ValidationState {
        return when {
            name.isEmpty() -> ValidationState(false, "Họ tên không được để trống")
            name.trim().length < 2 -> ValidationState(false, "Họ tên phải ít nhất 2 ký tự")
            name.length > 50 -> ValidationState(false, "Họ tên không được quá 50 ký tự")
            !name.matches(Regex("^[a-zA-ZÀ-ỹ\\s]+$")) -> ValidationState(false, "Họ tên chỉ được chứa chữ cái và khoảng trắng")
            name.trim().split("\\s+". toRegex()).size < 2 -> ValidationState(false, "Vui lòng nhập đầy đủ họ và tên")
            else -> ValidationState(true, "")
        }
    }

    fun validateDateOfBirth(dateStr: String): ValidationState {
        return when {
            dateStr.isEmpty() -> ValidationState(false, "Ngày sinh không được để trống")
            !dateStr. matches(Regex("^\\d{2}/\\d{2}/\\d{4}$")) -> ValidationState(false, "Định dạng ngày sinh:  dd/mm/yyyy")
            else -> {
                try {
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    formatter.isLenient = false
                    val date = formatter.parse(dateStr)
                    val calendar = Calendar.getInstance()
                    calendar.time = date!!

                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val birthYear = calendar.get(Calendar. YEAR)
                    val age = currentYear - birthYear

                    when {
                        age < 16 -> ValidationState(false, "Khách hàng phải từ 16 tuổi trở lên")
                        age > 100 -> ValidationState(false, "Tuổi không hợp lệ")
                        else -> ValidationState(true, "")
                    }
                } catch (e: Exception) {
                    ValidationState(false, "Ngày sinh không hợp lệ")
                }
            }
        }
    }

    fun validatePhoneNumber(phoneNumber: String): ValidationState {
        return when {
            phoneNumber.isEmpty() -> ValidationState(false, "Số điện thoại không được để trống")
            !phoneNumber.matches(Regex("^0\\d{9}$")) -> ValidationState(false, "Số điện thoại phải có 10 số và bắt đầu bằng 0")
            !phoneNumber.matches(Regex("^(032|033|034|035|036|037|038|039|096|097|098|086|083|084|085|081|082|088|091|094|070|079|077|076|078|090|093|089|056|058|092|059|099)[0-9]{7}$")) ->
                ValidationState(false, "Đầu số điện thoại không hợp lệ")
            else -> ValidationState(true, "")
        }
    }

    fun validateImage(imageBytes: ByteArray? ): ValidationState {
        return when {
            imageBytes == null -> ValidationState(false, "Vui lòng chọn ảnh khách hàng")
            imageBytes.size > 8192 -> ValidationState(false, "Kích thước ảnh quá lớn (tối đa 8KB)")
            else -> ValidationState(true, "")
        }
    }

    fun formatDateInput(input: String): String {
        val cleaned = input.replace(Regex("[^\\d]"), "")
        return when {
            cleaned.length <= 2 -> cleaned
            cleaned.length <= 4 -> "${cleaned.substring(0, 2)}/${cleaned.substring(2)}"
            cleaned.length <= 8 -> "${cleaned.substring(0, 2)}/${cleaned.substring(2, 4)}/${cleaned.substring(4)}"
            else -> "${cleaned.substring(0, 2)}/${cleaned.substring(2, 4)}/${cleaned.substring(4, 8)}"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserUpdateInfoScreen(
    smartCardManager: SmartCardManager,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf(TextFieldValue("")) }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var updateStatus by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    var nameValidation by remember { mutableStateOf(ValidationState()) }
    var dateValidation by remember { mutableStateOf(ValidationState()) }
    var phoneValidation by remember { mutableStateOf(ValidationState()) }
    var imageValidation by remember { mutableStateOf(ValidationState()) }
    var showValidationErrors by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        scope.launch {
            val info = try {
                smartCardManager.readCustomerInfo()
            } catch (e: Exception) {
                updateStatus = "❌ Lỗi đọc thông tin:  ${e.message}"
                isLoading = false
                return@launch
            }

            val imageData = try {
                smartCardManager.readCustomerImage()
            } catch (e: Exception) {
                null
            }

            name = info["name"] ?: ""
            dateOfBirth = TextFieldValue(info["dateOfBirth"] ?: "")
            phoneNumber = info["phoneNumber"] ?: ""
            selectedImageBytes = imageData

            imageData?.let { bytes ->
                try {
                    val skiaBitmap = Image.makeFromEncoded(bytes)
                    selectedImage = skiaBitmap. toComposeImageBitmap()
                } catch (e: Exception) {
                    println("Error converting image: ${e. message}")
                }
            }

            nameValidation = ValidationUtils.validateName(name)
            dateValidation = ValidationUtils.validateDateOfBirth(dateOfBirth.text)
            phoneValidation = ValidationUtils.validatePhoneNumber(phoneNumber)
            imageValidation = ValidationUtils.validateImage(selectedImageBytes)

            isLoading = false
        }
    }

    fun validateField(field: String, value: String, imageBytes: ByteArray?  = null) {
        when (field) {
            "name" -> nameValidation = ValidationUtils.validateName(value)
            "dateOfBirth" -> dateValidation = ValidationUtils.validateDateOfBirth(value)
            "phoneNumber" -> phoneValidation = ValidationUtils.validatePhoneNumber(value)
            "image" -> imageValidation = ValidationUtils.validateImage(imageBytes)
        }
    }

    val isFormValid = nameValidation.isValid &&
            dateValidation.isValid &&
            phoneValidation.isValid &&
            imageValidation. isValid &&
            name.isNotEmpty() &&
            dateOfBirth.text.isNotEmpty() &&
            phoneNumber.isNotEmpty() &&
            selectedImageBytes != null

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
                .verticalScroll(scrollState)  // ✅ SCROLL toàn màn
                .padding(horizontal = 80.dp, vertical = 20.dp)  // ✅ GIỐNG
        ) {
            // ✅ HEADER
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
                                    Color(0xFFFF6B9D),
                                    Color(0xFFC06FBB),
                                    Color(0xFFFEC163)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier. fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White. copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier. size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "✨ Cập nhật thông tin",
                                fontSize = 22.sp,
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
                                    Text("✏️", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Chỉnh sửa thông tin cá nhân",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✏️", fontSize = 32.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier. height(20.dp))

            // ✅ CONTENT CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(12.dp, RoundedCornerShape(28.dp)),
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
                            Text("Đang tải thông tin.. .", color = Color(0xFF666666))
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)
                    ) {
                        // ✅ PHOTO SECTION
                        Card(
                            modifier = Modifier. fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF3E0)
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
                                    Text("📸", fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Ảnh đại diện",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFF6B00)
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Box(
                                    modifier = Modifier
                                        .size(150.dp)
                                        . shadow(12.dp, CircleShape)
                                        . clip(CircleShape)
                                        .background(
                                            brush = Brush. radialGradient(
                                                colors = listOf(
                                                    Color(0xFFFF6B9D),
                                                    Color(0xFFFFA07A),
                                                    Color(0xFFFFD700)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedImage != null) {
                                        Image(
                                            bitmap = selectedImage!! ,
                                            contentDescription = "Customer Photo",
                                            modifier = Modifier. fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                modifier = Modifier.size(64.dp),
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

                                                    // Khởi tạo biến để lưu ảnh đang xử lý
                                                    var workingImage = bufferedImage

                                                    // Danh sách các kích thước để thử (giảm dần)
                                                    val sizeSteps = listOf(200, 150, 128, 100, 80)
                                                    val targetSizeBytes = 8000
                                                    var finalCompressedBytes: ByteArray? = null
                                                    var finalScaledImage: BufferedImage? = null

                                                    // Thử từng kích thước
                                                    for (targetSizePx in sizeSteps) {
                                                        // Resize ảnh xuống kích thước target nếu cần
                                                        val scaledImage = if (workingImage.width > targetSizePx || workingImage.height > targetSizePx) {
                                                            val scale = minOf(
                                                                targetSizePx.toFloat() / workingImage.width,
                                                                targetSizePx.toFloat() / workingImage.height
                                                            )
                                                            val newWidth = (workingImage.width * scale).toInt()
                                                            val newHeight = (workingImage.height * scale).toInt()

                                                            val scaled = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB)
                                                            val g = scaled.createGraphics()
                                                            g.drawImage(workingImage, 0, 0, newWidth, newHeight, null)
                                                            g.dispose()
                                                            scaled
                                                        } else {
                                                            workingImage
                                                        }

                                                        // Thử nén với các mức quality khác nhau
                                                        var quality = 0.7f
                                                        var found = false

                                                        while (quality >= 0.05f) {
                                                            val baos = ByteArrayOutputStream()
                                                            val writer = ImageIO.getImageWritersByFormatName("jpg").next()
                                                            val ios = ImageIO.createImageOutputStream(baos)
                                                            writer.output = ios

                                                            val param = writer.defaultWriteParam
                                                            param.compressionMode = ImageWriteParam.MODE_EXPLICIT
                                                            param.compressionQuality = quality

                                                            writer.write(null, javax.imageio.IIOImage(scaledImage, null, null), param)
                                                            writer.dispose()
                                                            ios.close()

                                                            val testBytes = baos.toByteArray()

                                                            if (testBytes.size <= targetSizeBytes) {
                                                                finalCompressedBytes = testBytes
                                                                finalScaledImage = scaledImage
                                                                found = true
                                                                break
                                                            }

                                                            quality -= 0.05f
                                                        }

                                                        if (found) break

                                                        // Nếu chưa tìm được, cập nhật workingImage để thử kích thước nhỏ hơn
                                                        workingImage = scaledImage
                                                    }

                                                    // Kiểm tra kết quả cuối cùng
                                                    if (finalCompressedBytes == null || finalScaledImage == null || finalCompressedBytes.size > targetSizeBytes) {
                                                        updateStatus = "❌ Không thể nén ảnh xuống dưới ${targetSizeBytes} bytes. Vui lòng chọn ảnh khác."
                                                        return@launch
                                                    }

                                                    // Gán kết quả
                                                    selectedImageBytes = finalCompressedBytes
                                                    selectedImage = finalScaledImage.toComposeImageBitmap()
                                                    validateField("image", "", selectedImageBytes)
                                                    updateStatus = "✅ Đã chọn và nén ảnh thành công! (${finalCompressedBytes.size} bytes)"

                                                } catch (e:  Exception) {
                                                    updateStatus = "❌ Lỗi đọc ảnh: ${e.message}"
                                                    e.printStackTrace()
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFF6B9D)
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
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (showValidationErrors && ! imageValidation.isValid) {
                                    Spacer(modifier = Modifier. height(8.dp))
                                    Text(
                                        text = imageValidation.errorMessage,
                                        color = Color(0xFFE57373),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        HorizontalDivider(
                            color = Color(0xFFFFAB91),
                            thickness = 2.dp,
                            modifier = Modifier. padding(vertical = 10.dp)
                        )

                        Spacer(modifier = Modifier. height(20.dp))

                        // ✅ FORM TITLE
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📝", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Thông tin cá nhân",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFF6B00)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ✅ NAME FIELD
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it. take(50)
                                validateField("name", name)
                            },
                            label = { Text("Họ và tên", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFFFF6B9D),
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                            shape = RoundedCornerShape(16.dp),
                            isError = showValidationErrors && !nameValidation.isValid,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF6B9D),
                                unfocusedBorderColor = if (showValidationErrors && !nameValidation.isValid) Color(0xFFE57373) else Color(0xFFE0E0E0),
                                focusedLabelColor = Color(0xFFFF6B9D),
                                focusedLeadingIconColor = Color(0xFFFF6B9D),
                                cursorColor = Color(0xFFFF6B9D)
                            )
                        )
                        if (showValidationErrors && !nameValidation.isValid) {
                            Text(
                                text = nameValidation.errorMessage,
                                color = Color(0xFFE57373),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // ✅ DATE OF BIRTH FIELD
                        OutlinedTextField(
                                value = dateOfBirth,
                                onValueChange = { newValue ->
                                    val formatted = ValidationUtils.formatDateInput(newValue.text)
                                    if (formatted.length <= 10) {
                                        dateOfBirth = TextFieldValue(
                                            text = formatted,
                                            selection = TextRange(formatted.length)
                                        )
                                        validateField("dateOfBirth", formatted)
                                    }
                                },
                                label = { Text("Ngày sinh",fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                                placeholder = { Text("01/01/2000", color = Color.Gray, fontSize = 15.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = Color(0xFF667EEA),
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = showValidationErrors && !dateValidation.isValid,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (dateValidation.isValid) Color(0xFF667EEA) else Color(0xFFE57373),
                                    unfocusedBorderColor = if (showValidationErrors && !dateValidation.isValid) Color(0xFFE57373) else Color(0xFFE0E0E0),
                                    focusedLabelColor = Color(0xFF667EEA),
                                    cursorColor = Color(0xFF667EEA)
                                ),
                                supportingText = {
                                    Text(
                                        text = "💡 Định dạng: dd/mm/yyyy",
                                        fontSize = 12.sp,
                                        color = Color(0xFF9575CD)
                                    )
                                }
                            )
                            if (showValidationErrors && !dateValidation.isValid) {
                                Text(
                                    text = dateValidation.errorMessage,
                                    color = Color(0xFFE57373),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )
                            }
                        Spacer(modifier = Modifier.height(18.dp))

                        // ✅ PHONE FIELD
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() } && it.length <= 10) {
                                    phoneNumber = it
                                    validateField("phoneNumber", phoneNumber)
                                }
                            },
                            label = { Text("Số điện thoại", fontWeight = FontWeight. Bold, fontSize = 15.sp) },
                            placeholder = { Text("0901234567", color = Color.Gray, fontSize = 15.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = Color(0xFF66BB6A),
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            singleLine = true,
                            textStyle = LocalTextStyle. current.copy(fontSize = 16.sp),
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            isError = showValidationErrors && !phoneValidation. isValid,
                            colors = OutlinedTextFieldDefaults. colors(
                                focusedBorderColor = Color(0xFF66BB6A),
                                unfocusedBorderColor = if (showValidationErrors && ! phoneValidation.isValid) Color(0xFFE57373) else Color(0xFFE0E0E0),
                                focusedLabelColor = Color(0xFF66BB6A),
                                focusedLeadingIconColor = Color(0xFF66BB6A),
                                cursorColor = Color(0xFF66BB6A)
                            )
                        )
                        if (showValidationErrors && !phoneValidation.isValid) {
                            Text(
                                text = phoneValidation.errorMessage,
                                color = Color(0xFFE57373),
                                fontSize = 12.sp,
                                modifier = Modifier. padding(start = 4.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // ✅ UPDATE BUTTON
                        Button(
                            onClick = {
                                showValidationErrors = true
                                if (isFormValid) {
                                    isUpdating = true
                                    updateStatus = "Đang cập nhật..."
                                    scope.launch {
                                        try {
                                            val writeSuccess = smartCardManager.writeCustomerInfo(
                                                name, name, dateOfBirth.text,phoneNumber,phoneNumber
                                            )

                                            if (writeSuccess && selectedImageBytes != null) {
                                                val imageSuccess = smartCardManager.writeCustomerImage(selectedImageBytes!!)
                                                if (imageSuccess) {
                                                    updateStatus = "✅ Cập nhật thành công!"
                                                } else {
                                                    updateStatus = "⚠️ Cập nhật thông tin OK, nhưng ảnh thất bại"
                                                }
                                            } else if (writeSuccess) {
                                                updateStatus = "✅ Cập nhật thông tin thành công!"
                                            } else {
                                                updateStatus = "❌ Cập nhật thất bại"
                                            }
                                        } catch (e: Exception) {
                                            updateStatus = "❌ Lỗi:  ${e.message}"
                                        } finally {
                                            isUpdating = false
                                        }
                                    }
                                } else {
                                    updateStatus = "❌ Vui lòng kiểm tra lại thông tin"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            enabled = ! isUpdating,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                disabledContainerColor = Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(18.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 16.dp
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isUpdating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        color = Color. White,
                                        strokeWidth = 4.dp
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = "Đang cập nhật...",
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
                                        text = "Cập nhật thông tin",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight. ExtraBold
                                    )
                                }
                            }
                        }

                        // ✅ STATUS
                        if (updateStatus.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier
                                    . fillMaxWidth()
                                    .shadow(10.dp, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        updateStatus.startsWith("✅") -> Color(0xFFE8F5E9)
                                        updateStatus.startsWith("⚠️") -> Color(0xFFFFF3E0)
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
                                            updateStatus. startsWith("✅") -> "✅"
                                            updateStatus.startsWith("⚠️") -> "⚠️"
                                            updateStatus.startsWith("❌") -> "❌"
                                            else -> "⏳"
                                        },
                                        fontSize = 28.sp
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = if (updateStatus.length > 2 && (updateStatus.startsWith("✅") || updateStatus.startsWith("⚠️") || updateStatus.startsWith("❌")))
                                            updateStatus.substring(2)
                                        else
                                            updateStatus,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            updateStatus.startsWith("✅") -> Color(0xFF4CAF50)
                                            updateStatus.startsWith("⚠️") -> Color(0xFFFFA726)
                                            else -> Color(0xFFE53935)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
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
    return Image.makeFromEncoded(bytes).toComposeImageBitmap()
}