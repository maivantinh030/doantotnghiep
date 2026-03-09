package org.example.project.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.SmartCardManager
import org.example.project.screen.FloatingBubbles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminResetUserPinScreen(
    smartCardManager: SmartCardManager,
    onBack: () -> Unit
) {
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    fun handleReset() {
        if (newPin.isEmpty() || confirmPin.isEmpty()) {
            status = "❌ Vui lòng nhập đầy đủ thông tin"
            return
        }

        if (newPin.length < 4 || newPin.length > 8) {
            status = "❌ PIN phải có từ 4 đến 8 ký tự"
            return
        }

        if (newPin != confirmPin) {
            status = "❌ PIN xác nhận không khớp"
            return
        }

        isLoading = true
        status = ""

        scope.launch {
            try {
                val success = smartCardManager.resetUserPIN(newPin)
                if (success) {
                    status = "✅ Đã reset user PIN thành công!"
                    newPin = ""
                    confirmPin = ""
                } else {
                    status = "❌ Không thể reset user PIN. Vui lòng kiểm tra lại."
                }
            } catch (e: Exception) {
                status = "❌ Lỗi: ${e.message}"
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
                .verticalScroll(scrollState)  // ✅ THÊM SCROLL
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
                                    Color(0xFF9C27B0),  // Tím
                                    Color(0xFFBA68C8),
                                    Color(0xFFCE93D8)
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
                                .background(Color.White.copy(alpha = 0.3f))
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
                                text = "🔐 Reset User PIN",
                                fontSize = 22.sp,
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
                                    modifier = Modifier. padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🔑", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Chỉ admin có quyền",
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
                                .background(Color. White.copy(alpha = 0.3f)),
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
                    .shadow(12.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp)
                ) {
                    // ✅ INFO CARD
                    Card(
                        modifier = Modifier. fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3E0)
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    . clip(CircleShape)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFFFFB74D),
                                                Color(0xFFFFA726)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("ℹ️", fontSize = 28.sp)
                            }

                            Spacer(modifier = Modifier. width(16.dp))

                            Column {
                                Text(
                                    text = "Reset User PIN",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFF6B00)
                                )
                                Spacer(modifier = Modifier. height(4.dp))
                                Text(
                                    text = "Chỉ admin mới có quyền reset user PIN",
                                    fontSize = 13.sp,
                                    color = Color(0xFF666666),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    HorizontalDivider(
                        color = Color(0xFFE0E0E0),
                        thickness = 2.dp,
                        modifier = Modifier. padding(vertical = 10.dp)
                    )

                    Spacer(modifier = Modifier. height(20.dp))

                    // ✅ FORM SECTION
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔐", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Thông tin PIN mới",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF6B00)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // NEW PIN
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = {
                            if (it.length <= 8) newPin = it
                        },
                        label = { Text("PIN mới (4-8 ký tự)", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        placeholder = { Text("Nhập PIN mới", color = Color.Gray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF9C27B0),
                                modifier = Modifier. size(24.dp)
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.Black),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF9C27B0),
                            focusedLabelColor = Color(0xFF9C27B0),
                            focusedLeadingIconColor = Color(0xFF9C27B0),
                            cursorColor = Color(0xFF9C27B0),
                            focusedTextColor = Color. Black,
                            unfocusedTextColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // CONFIRM PIN
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = {
                            if (it.length <= 8) confirmPin = it
                        },
                        label = { Text("Xác nhận PIN", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        placeholder = { Text("Nhập lại PIN mới", color = Color.Gray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFBA68C8),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier. fillMaxWidth().wrapContentHeight(),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.Black),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFBA68C8),
                            focusedLabelColor = Color(0xFFBA68C8),
                            focusedLeadingIconColor = Color(0xFFBA68C8),
                            cursorColor = Color(0xFFBA68C8),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // RESET BUTTON
                    Button(
                        onClick = { handleReset() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        enabled = !isLoading && newPin.isNotEmpty() && confirmPin.isNotEmpty(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9C27B0),
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
                            if (isLoading) {
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
                                    imageVector = Icons.Default. Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier. width(14.dp))
                                Text(
                                    text = "Reset User PIN",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
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
