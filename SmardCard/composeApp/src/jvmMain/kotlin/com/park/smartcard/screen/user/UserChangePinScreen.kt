package com.park.smartcard.screen.user

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.park.smartcard.SmartCardManager
import com.park.smartcard.screen.FloatingBubbles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserChangePinScreen(
    smartCardManager:  SmartCardManager,
    onBack: () -> Unit
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isChanging by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF3E0),
                        Color(0xFFFFF0F5),
                        Color(0xFFE0F7FA)
                    )
                )
            )
    ) {
        FloatingBubbles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())  // ✅ THÊM SCROLL
                .padding(horizontal = 80.dp, vertical = 20.dp)  // ✅ PADDING 80DP
        ) {
            // ✅ HEADER CARD
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
                                    Color(0xFFFFB74D),  // Cam
                                    Color(0xFFFFA726),
                                    Color(0xFFFFD54F)
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
                                text = "🔐 Đổi Mã PIN",
                                fontSize = 22.sp,
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
                                    Text("🔑", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Bảo mật tài khoản",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color. White
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
                            Text("🔒", fontSize = 32.sp)
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
                    . shadow(12.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults. cardColors(containerColor = Color. White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp)
                ) {
                    // ✅ TITLE
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔐", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Thay đổi mã PIN",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF6B00)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // ✅ CURRENT PIN
                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 8) {
                                currentPin = it
                                status = ""
                            }
                        },
                        label = { Text("Mã PIN hiện tại", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFFFB74D),
                                modifier = Modifier. size(24.dp)
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        shape = RoundedCornerShape(16.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color. Black),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFB74D),
                            focusedLabelColor = Color(0xFFFFB74D),
                            focusedLeadingIconColor = Color(0xFFFFB74D),
                            cursorColor = Color(0xFFFFB74D),
                            focusedTextColor = Color. Black,
                            unfocusedTextColor = Color.Black
                        ),
                        singleLine = true,
                        supportingText = {
                            Text("💡 Nhập mã PIN hiện tại của bạn", fontSize = 12.sp, color = Color(0xFF9575CD))
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // ✅ NEW PIN
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 8) {
                                newPin = it
                                status = ""
                            }
                        },
                        label = { Text("Mã PIN mới", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFFFA726),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        shape = RoundedCornerShape(16.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color. Black),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFA726),
                            focusedLabelColor = Color(0xFFFFA726),
                            focusedLeadingIconColor = Color(0xFFFFA726),
                            cursorColor = Color(0xFFFFA726),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        singleLine = true,
                        isError = newPin.isNotEmpty() && newPin == currentPin,
                        supportingText = {
                            if (newPin.isNotEmpty() && newPin == currentPin) {
                                Text("⚠️ PIN mới phải khác PIN cũ", fontSize = 12.sp, color = Color. Red, fontWeight = FontWeight.Bold)
                            } else {
                                Text("💡 4-8 ký tự số", fontSize = 12.sp, color = Color(0xFF9575CD))
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // ✅ CONFIRM PIN
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = {
                            if (it.all { char -> char. isDigit() } && it.length <= 8) {
                                confirmPin = it
                                status = ""
                            }
                        },
                        label = { Text("Xác nhận PIN mới", fontWeight = FontWeight. Bold, fontSize = 15.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF66BB6A),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        shape = RoundedCornerShape(16.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color. Black),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF66BB6A),
                            focusedLabelColor = Color(0xFF66BB6A),
                            focusedLeadingIconColor = Color(0xFF66BB6A),
                            cursorColor = Color(0xFF66BB6A),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        singleLine = true,
                        isError = confirmPin.isNotEmpty() && confirmPin != newPin,
                        supportingText = {
                            when {
                                confirmPin.isNotEmpty() && confirmPin != newPin -> {
                                    Text("⚠️ PIN không khớp", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                }
                                confirmPin.isNotEmpty() && confirmPin == newPin -> {
                                    Text("✅ PIN khớp", fontSize = 12.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                }
                                else -> {
                                    Text("💡 Nhập lại PIN mới", fontSize = 12.sp, color = Color(0xFF9575CD))
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // ✅ CHANGE PIN BUTTON
                    Button(
                        onClick = {
                            scope.launch {
                                // ✅ GIỮ NGUYÊN LOGIC
                                if (newPin == currentPin) {
                                    status = "❌ Mã PIN mới phải khác mã PIN cũ!"
                                    return@launch
                                }

                                if (newPin != confirmPin) {
                                    status = "❌ Mã PIN mới không khớp!"
                                    return@launch
                                }

                                if (newPin.length < 4 || newPin. length > 8) {
                                    status = "❌ PIN phải từ 4-8 ký tự!"
                                    return@launch
                                }

                                if (! newPin.all { it.isDigit() }) {
                                    status = "❌ PIN chỉ được chứa số!"
                                    return@launch
                                }

                                isChanging = true
                                try {
                                    val success = smartCardManager.changePIN(currentPin, newPin)

                                    if (success) {
                                        status = "✅ Đổi PIN thành công!"
                                        currentPin = ""
                                        newPin = ""
                                        confirmPin = ""
                                    } else {
                                        status = "❌ Đổi PIN thất bại!  Kiểm tra lại PIN cũ."
                                    }
                                } catch (e:  Exception) {
                                    status = "❌ Lỗi:  ${e.message}"
                                } finally {
                                    isChanging = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        enabled = ! isChanging &&
                                currentPin.isNotEmpty() &&
                                newPin.isNotEmpty() &&
                                confirmPin.isNotEmpty() &&
                                newPin == confirmPin &&
                                newPin != currentPin &&
                                newPin.length >= 4,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB74D),
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
                            if (isChanging) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = Color. White,
                                    strokeWidth = 4.dp
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = "Đang xử lý...",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier. width(14.dp))
                                Text(
                                    text = "Đổi mã PIN",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight. ExtraBold
                                )
                            }
                        }
                    }

                    // ✅ VALIDATION HINTS
                    if (currentPin.isNotEmpty() || newPin.isNotEmpty() || confirmPin.isNotEmpty()) {
                        Spacer(modifier = Modifier. height(20.dp))
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF3E0)
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📋", fontSize = 20.sp)
                                    Spacer(modifier = Modifier. width(8.dp))
                                    Text(
                                        text = "Yêu cầu:",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFF6B00)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                ValidationItem(
                                    text = "PIN mới khác PIN cũ",
                                    isValid = newPin.isEmpty() || newPin != currentPin
                                )
                                ValidationItem(
                                    text = "PIN có 4-8 ký tự số",
                                    isValid = newPin.isEmpty() || (newPin.length >= 4 && newPin.length <= 8 && newPin.all { it. isDigit() })
                                )
                                ValidationItem(
                                    text = "Xác nhận PIN khớp",
                                    isValid = confirmPin.isEmpty() || confirmPin == newPin
                                )
                            }
                        }
                    }
                }
            }

            // ✅ STATUS MESSAGE
            if (status.isNotEmpty()) {
                Spacer(modifier = Modifier. height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (status.startsWith("✅"))
                            Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (status.startsWith("✅")) "✅" else "❌",
                            fontSize = 28.sp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = status. drop(2).trim(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (status.startsWith("✅"))
                                Color(0xFF4CAF50) else Color(0xFFE53935),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ValidationItem(text: String, isValid: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier. padding(vertical = 4.dp)
    ) {
        Text(
            text = if (isValid) "✓" else "○",
            fontSize = 16.sp,
            color = if (isValid) Color(0xFF4CAF50) else Color. Gray,
            fontWeight = FontWeight. Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (isValid) Color(0xFF4CAF50) else Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}

