package org.example.project.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.project.SmartCardManager
import org.example.project.network.RSAApiClient
import java.util.Base64

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRSAAuthScreen(
    smartCardManager: SmartCardManager,
    onBack: () -> Unit
) {
    val rsaApi = remember { RSAApiClient() }
    var currentCardId by remember { mutableStateOf("") }
    var rsaStatus by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    
    var challengeBase64 by remember { mutableStateOf("") }
    var signatureBase64 by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Load initial status
    LaunchedEffect(Unit) {
        scope.launch {
            loading = true
            withContext(Dispatchers.IO) {
                try {
                    currentCardId = smartCardManager.readCustomerInfo()["cardUUID"]?.trim().orEmpty()
                    rsaStatus = smartCardManager.getRSAStatus()
                } catch (e: Exception) {
                    errorMessage = "Lỗi: ${e.message}"
                }
            }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔐 Xác Thực RSA", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFEF5350),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF3E0),
                            Color(0xFFFFE5E5)
                        )
                    )
                )
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (rsaStatus) Color(0xFF81C784) else Color(0xFFFFB74D)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (rsaStatus) "✅ RSA Đã Sẵn Sàng" else "⚠️ RSA Chưa Thiết Lập",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (currentCardId.isNotEmpty()) {
                        Text(
                            text = "Card ID: $currentCardId",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Error/Success Messages
            if (errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEF5350))
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = Color.White
                    )
                }
            }

            if (successMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF66BB6A))
                ) {
                    Text(
                        text = successMessage,
                        modifier = Modifier.padding(16.dp),
                        color = Color.White
                    )
                }
            }

            // Test Challenge Signing
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Kiểm Tra Xác Thực RSA",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF5350)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Lấy challenge từ server và ký bằng RSA key trên thẻ",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            scope.launch {
                                loading = true
                                errorMessage = ""
                                successMessage = ""
                                withContext(Dispatchers.IO) {
                                    try {
                                        if (currentCardId.isBlank()) {
                                            errorMessage = "Không tìm thấy Card ID trên thẻ"
                                            return@withContext
                                        }

                                        val challengeResult = rsaApi.getChallenge()
                                        val challengeDto = challengeResult.getOrElse {
                                            errorMessage = it.message ?: "Không lấy được challenge"
                                            return@withContext
                                        }

                                        val challengeBytes = try {
                                            Base64.getDecoder().decode(challengeDto.challenge)
                                        } catch (e: Exception) {
                                            errorMessage = "Challenge server trả về không hợp lệ"
                                            return@withContext
                                        }

                                        if (challengeBytes.size != 32) {
                                            errorMessage = "Challenge phải dài 32 byte"
                                            return@withContext
                                        }

                                        val signature = smartCardManager.signChallenge(challengeBytes)
                                        if (signature == null) {
                                            errorMessage = "❌ Không thể ký challenge (chưa có RSA key?)"
                                            return@withContext
                                        }

                                        val signatureB64 = Base64.getEncoder().encodeToString(signature)
                                        val verifyResult = rsaApi.verifySignature(currentCardId, challengeDto.challenge, signatureB64)
                                        val verify = verifyResult.getOrElse {
                                            errorMessage = it.message ?: "Xác thực thất bại"
                                            return@withContext
                                        }

                                        challengeBase64 = challengeDto.challenge
                                        signatureBase64 = signatureB64
                                        rsaStatus = smartCardManager.getRSAStatus()

                                        if (verify.success) {
                                            successMessage = "✅ Xác thực RSA thành công: ${verify.message}"
                                        } else {
                                            errorMessage = "❌ Xác thực RSA thất bại: ${verify.message}"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Lỗi: ${e.message}"
                                    }
                                }
                                loading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading && rsaStatus,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A))
                    ) {
                        Text("Ký Challenge Test", fontSize = 16.sp)
                    }
                    
                    if (challengeBase64.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Challenge (Base64):",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = challengeBase64,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    
                    if (signatureBase64.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Signature (Base64):",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = signatureBase64,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFEF5350))
                }
            }
        }
    }
}
