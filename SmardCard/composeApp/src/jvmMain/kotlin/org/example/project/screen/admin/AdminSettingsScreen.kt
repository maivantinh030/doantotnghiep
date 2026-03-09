package org.example.project.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.SmartCardManager
import org.example.project.screen.FloatingBubbles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    smartCardManager: SmartCardManager,
    onBack: () -> Unit
) {
    var pinTries by remember { mutableStateOf(3) }
    var pinCreated by remember { mutableStateOf(false) }
    var pinValidated by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun loadPINStatus() {
        scope.launch {
            try {
                val (tries, created, validated) = smartCardManager.getPINStatus()
                pinTries = tries
                pinCreated = created
                pinValidated = validated
                status = "✅ Đã tải trạng thái PIN"
            } catch (e:  Exception) {
                status = "❌ Lỗi:  ${e.message}"
            }
        }
    }

    LaunchedEffect(Unit) {
        loadPINStatus()
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
                .padding(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults. cardColors(containerColor = Color(0xFF90A4AE)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        . fillMaxWidth()
                        . padding(20.dp),
                    verticalAlignment = Alignment. CenterVertically
                ) {
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("←", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "⚙️ Cài Đặt Hệ Thống",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = { loadPINStatus() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Text("🔄", fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status message
            if (status.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (status. startsWith("✅"))
                            Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Settings Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    . verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement. spacedBy(16.dp)
            ) {
                // PIN Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    . clip(CircleShape)
                                    .background(Color(0xFFE3F2FD)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔐", fontSize = 24.sp)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = "Trạng Thái PIN",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        SettingItem(
                            label = "Số lần thử còn lại",
                            value = "$pinTries/3",
                            color = when {
                                pinTries == 3 -> Color(0xFF4CAF50)
                                pinTries > 0 -> Color(0xFFFF9800)
                                else -> Color(0xFFE53935)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SettingItem(
                            label = "PIN đã được tạo",
                            value = if (pinCreated) "✅ Có" else "❌ Chưa",
                            color = if (pinCreated) Color(0xFF4CAF50) else Color(0xFFE53935)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SettingItem(
                            label = "PIN đã xác thực",
                            value = if (pinValidated) "✅ Có" else "❌ Chưa",
                            color = if (pinValidated) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9800)
                            )
                        ) {
                            Text("🔄 Reset bộ đếm PIN", fontSize = 14.sp)
                        }
                    }
                }

                // System Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    . size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3E5F5)),
                                contentAlignment = Alignment. Center
                            ) {
                                Text("ℹ️", fontSize = 24.sp)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = "Thông Tin Hệ Thống",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                        }

                        Spacer(modifier = Modifier. height(16.dp))

                        SettingItem(
                            label = "Phiên bản",
                            value = "1.0.0",
                            color = Color(0xFF9E9E9E)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SettingItem(
                            label = "Loại thẻ",
                            value = "JavaCard",
                            color = Color(0xFF9E9E9E)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SettingItem(
                            label = "AID",
                            value = "11 11 11 11 11 00",
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }

                // About Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎡", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SmartCard Park Manager",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hệ thống quản lý thẻ công viên",
                            fontSize = 14.sp,
                            color = Color. Gray
                        )
                    }
                }
            }
        }
    }

    // Reset PIN Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Xác nhận reset") },
            text = { Text("Bạn có chắc muốn reset bộ đếm PIN?  Số lần thử sẽ được đặt lại về 3.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                smartCardManager. resetPinCounter()
                                loadPINStatus()
                                status = "✅ Đã reset bộ đếm PIN"
                                showResetDialog = false
                            } catch (e: Exception) {
                                status = "❌ Lỗi: ${e.message}"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    )
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun SettingItem(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement. SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight. Bold,
            color = color
        )
    }
}