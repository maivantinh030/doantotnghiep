package org.example.project.screen.admin

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.SmartCardManager
import org.example.project.screen.FloatingBubbles
import org.example.project.network.TransactionApiClient
import org.example.project.model.CreateTransactionRequest

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AdminRechargeScreen(
//    smartCardManager: SmartCardManager,
//    onBack: () -> Unit
//) {
//    var balance by remember { mutableStateOf(0) }
//    var rechargeAmount by remember { mutableStateOf("") }
//    var isLoading by remember { mutableStateOf(false) }
//    var status by remember { mutableStateOf("") }
//
//    val scope = rememberCoroutineScope()
//
//    fun loadBalance() {
//        scope.launch {
//            isLoading = true
//            try {
//                balance = smartCardManager.checkBalance()*1000
//                status = "✅ Đã tải số dư"
//            } catch (e: Exception) {
//                status = "❌ Lỗi:  ${e.message}"
//            }
//            isLoading = false
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        loadBalance()
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
//                .fillMaxSize()
//                .padding(16.dp)
//        ) {
//            // Header
//            Card(
//                modifier = Modifier. fillMaxWidth(),
//                shape = RoundedCornerShape(24.dp),
//                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFB74D)),
//                elevation = CardDefaults. cardElevation(8.dp)
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(20.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Button(
//                        onClick = onBack,
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color. White. copy(alpha = 0.2f),
//                            contentColor = Color. White
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
//                    Text(
//                        text = "💰 Nạp Tiền Vào Thẻ",
//                        fontSize = 22.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.White
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier. height(24.dp))
//
//            // Balance Display
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(24.dp),
//                colors = CardDefaults.cardColors(containerColor = Color. White),
//                elevation = CardDefaults. cardElevation(12.dp)
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(32.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(100.dp)
//                            .clip(CircleShape)
//                            .background(
//                                brush = Brush.radialGradient(
//                                    colors = listOf(
//                                        Color(0xFFFFB74D),
//                                        Color(0xFFFFCC02)
//                                    )
//                                )
//                            ),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text("💵", fontSize = 48.sp)
//                    }
//
//                    Spacer(modifier = Modifier. height(20.dp))
//
//                    Text(
//                        text = "Số dư hiện tại",
//                        fontSize = 16.sp,
//                        color = Color.Gray
//                    )
//
//                    Spacer(modifier = Modifier. height(8.dp))
//
//                    if (isLoading) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(40.dp),
//                            color = Color(0xFFFFB74D)
//                        )
//                    } else {
//                        Text(
//                            text = "${balance} VNĐ",
//                            fontSize = 40.sp,
//                            fontWeight = FontWeight.ExtraBold,
//                            color = Color(0xFFFFB74D)
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Button(
//                        onClick = { loadBalance() },
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFFFFB74D)
//                        ),
//                        shape = RoundedCornerShape(12.dp)
//                    ) {
//                        Text("🔄 Làm mới", fontSize = 14.sp)
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier. height(24.dp))
//
//            // Recharge Section
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(24.dp),
//                colors = CardDefaults.cardColors(containerColor = Color.White),
//                elevation = CardDefaults. cardElevation(8.dp)
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(24.dp)
//                ) {
//                    Text(
//                        text = "➕ Nạp tiền",
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color(0xFF333333)
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    OutlinedTextField(
//                        value = rechargeAmount,
//                        onValueChange = { rechargeAmount = it },
//                        label = { Text("Số tiền nạp") },
//                        placeholder = { Text("Nhập số tiền... ") },
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(12.dp),
//                        colors = OutlinedTextFieldDefaults.colors(
//                            focusedBorderColor = Color(0xFFFFB74D),
//                            focusedLabelColor = Color(0xFFFFB74D)
//                        ),
//                        singleLine = true,
//
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    // Quick Amount Buttons
//                    Text(
//                        text = "Chọn nhanh:",
//                        fontSize = 14.sp,
//                        color = Color.Gray
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        listOf(50000, 100000, 200000, 500000).forEach { amount ->
//                            Button(
//                                onClick = { rechargeAmount = amount.toString() },
//                                modifier = Modifier.weight(1f),
//                                colors = ButtonDefaults.buttonColors(
//                                    containerColor = Color(0xFFFFF3E0)
//                                ),
//                                shape = RoundedCornerShape(12.dp)
//                            ) {
//                                Text(
//                                    text = "${amount/1000}k",
//                                    fontSize = 12.sp,
//                                    color = Color(0xFFFFB74D),
//                                    fontWeight = FontWeight.Bold
//                                )
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Button(
//                        onClick = {
//                            scope.launch {
//                                try {
//                                    val amount = rechargeAmount.toIntOrNull()
//                                    if (amount != null && amount > 0) {
//                                        isLoading = true
//                                        val success = smartCardManager.rechargeBalance(amount/1000)
//                                        if (success) {
//                                            loadBalance()
//                                            rechargeAmount = ""
//                                            status = "✅ Nạp thành công $amount VNĐ"
//                                        } else {
//                                            status = "❌ Nạp tiền thất bại"
//                                        }
//                                    } else {
//                                        status = "❌ Số tiền không hợp lệ"
//                                    }
//                                } catch (e: Exception) {
//                                    status = "❌ Lỗi: ${e.message}"
//                                } finally {
//                                    isLoading = false
//                                }
//                            }
//                        },
//                        modifier = Modifier.fillMaxWidth(),
//                        enabled = !isLoading && rechargeAmount.isNotEmpty(),
//                        shape = RoundedCornerShape(12.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFF4CAF50)
//                        )
//                    ) {
//                        if (isLoading) {
//                            CircularProgressIndicator(
//                                modifier = Modifier. size(16.dp),
//                                color = Color.White,
//                                strokeWidth = 2.dp
//                            )
//                            Spacer(modifier = Modifier.width(8.dp))
//                        }
//                        Text(
//                            text = if (isLoading) "Đang xử lý..." else "💳 Nạp tiền",
//                            fontSize = 16.sp
//                        )
//                    }
//                }
//            }
//
//            // Status message
//            if (status.isNotEmpty()) {
//                Spacer(modifier = Modifier.height(16.dp))
//                Card(
//                    modifier = Modifier. fillMaxWidth(),
//                    shape = RoundedCornerShape(12.dp),
//                    colors = CardDefaults.cardColors(
//                        containerColor = if (status.startsWith("✅"))
//                            Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
//                    )
//                ) {
//                    Text(
//                        text = status,
//                        modifier = Modifier.padding(16.dp),
//                        color = if (status.startsWith("✅"))
//                            Color(0xFF4CAF50) else Color(0xFFE53935),
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//            }
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api:: class)
@Composable
fun AdminRechargeScreen(
    smartCardManager: SmartCardManager,
    onBack: () -> Unit
) {
    var balance by remember { mutableStateOf(0) }
    var rechargeAmount by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val transactionApiClient = remember { TransactionApiClient() }

    fun loadBalance() {
        scope.launch {
            isLoading = true
            try {
                balance = smartCardManager. checkBalance() * 1000
                status = "✅ Đã tải số dư"
            } catch (e: Exception) {
                status = "❌ Lỗi:  ${e.message}"
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadBalance()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFE5EC),  // ✅ GIỐNG AdminWriteInfoScreen
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
                                .background(Color.White. copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier. size(26.dp)  // ✅ GIỐNG
                            )
                        }

                        Spacer(modifier = Modifier. width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "💰 Nạp Tiền Vào Thẻ",
                                fontSize = 22.sp,  // ✅ GIỐNG
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier. height(6.dp))
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults. cardColors(
                                    containerColor = Color. White. copy(alpha = 0.25f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("💳", fontSize = 18.sp)  // ✅ GIỐNG
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Nạp & Quản lý số dư",
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
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💵", fontSize = 32.sp)
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
                    // ✅ BALANCE DISPLAY
                    Card(
                        modifier = Modifier. fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),  // ✅ GIỐNG
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3E0)  // ✅ GIỐNG màu photo card
                        ),
                        elevation = CardDefaults.cardElevation(6.dp)  // ✅ GIỐNG
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),  // ✅ GIỐNG
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement. Center
                            ) {
                                Text("💵", fontSize = 22.sp)  // ✅ GIỐNG
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Số dư hiện tại",
                                    fontSize = 20.sp,  // ✅ GIỐNG
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFF6B00)  // ✅ GIỐNG
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Box(
                                modifier = Modifier
                                    .size(150.dp)  // ✅ GIỐNG avatar
                                    .shadow(12.dp, CircleShape)  // ✅ GIỐNG
                                    .clip(CircleShape)
                                    . background(
                                        brush = Brush. radialGradient(
                                            colors = listOf(
                                                Color(0xFFFF6B9D),  // ✅ GIỐNG
                                                Color(0xFFFFA07A),
                                                Color(0xFFFFD700)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(52.dp),
                                        color = Color.White,
                                        strokeWidth = 5.dp
                                    )
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "${String.format("%,d", balance)}",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "VNĐ",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { loadBalance() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    . height(56.dp),  // ✅ GIỐNG
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF6B9D)  // ✅ GIỐNG
                                ),
                                elevation = ButtonDefaults. buttonElevation(
                                    defaultElevation = 6.dp,
                                    pressedElevation = 12.dp
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement. Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default. Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "🔄 Làm mới",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier. height(28.dp))  // ✅ GIỐNG

                    HorizontalDivider(
                        color = Color(0xFFFFAB91),  // ✅ GIỐNG
                        thickness = 2.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    Spacer(modifier = Modifier. height(20.dp))

                    // ✅ RECHARGE SECTION
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💳", fontSize = 24.sp)  // ✅ CHỈ GIỮ 1 ICON
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Nạp tiền",
                            fontSize = 20.sp,  // ✅ GIỐNG
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF6B00)  // ✅ GIỐNG
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = rechargeAmount,
                        onValueChange = { rechargeAmount = it },
                        label = { Text("Số tiền nạp (VNĐ)", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        placeholder = { Text("Nhập số tiền.. .", color = Color.Gray, fontSize = 15.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),  // ✅ Xanh lá (tiền)
                                modifier = Modifier. size(24.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier. fillMaxWidth().height(64.dp),  // ✅ GIỐNG
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            focusedLabelColor = Color(0xFF4CAF50),
                            focusedLeadingIconColor = Color(0xFF4CAF50),
                            cursorColor = Color(0xFF4CAF50)
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))  // ✅ GIỐNG

                    // ✅ QUICK AMOUNT BUTTONS
                    Text(
                        text = "⚡ Chọn nhanh:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight. Bold,
                        color = Color(0xFF666666)
                    )

                    Spacer(modifier = Modifier. height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(50000, 100000, 200000, 500000).forEach { amount ->
                            Button(
                                onClick = { rechargeAmount = amount.toString() },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFF3E0)  // ✅ GIỐNG
                                ),
                                shape = RoundedCornerShape(12.dp),
                                elevation = ButtonDefaults.buttonElevation(3.dp)
                            ) {
                                Text(
                                    text = "${amount / 1000}k",
                                    fontSize = 13.sp,
                                    color = Color(0xFFFF6B00),  // ✅ GIỐNG
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // ✅ RECHARGE BUTTON
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val amount = rechargeAmount.toIntOrNull()
                                    if (amount != null && amount > 0) {
                                        isLoading = true
                                        val success = smartCardManager.rechargeBalance(amount / 1000)
                                        if (success) {
                                            // Lấy customerId từ thẻ
                                            val customerInfo = smartCardManager.readCustomerInfo()
                                            val customerId = customerInfo["customerID"] ?: ""
                                            
                                            loadBalance()
                                            
                                            // Ghi lịch sử nạp tiền
                                            if (customerId.isNotBlank()) {
                                                val txnReq = CreateTransactionRequest(
                                                    customerId = customerId,
                                                    type = "TOPUP",
                                                    amount = amount.toString(),
                                                    balanceAfter = smartCardManager.checkBalance()
                                                )
                                                transactionApiClient.record(txnReq)
                                                    .onSuccess { println("📝 Đã ghi lịch sử nạp tiền") }
                                                    .onFailure { println("⚠️ Không ghi được lịch sử nạp: ${it.message}") }
                                            }
                                            
                                            rechargeAmount = ""
                                            status = "✅ Nạp thành công ${String.format("%,d", amount)} VNĐ"
                                        } else {
                                            status = "❌ Nạp tiền thất bại"
                                        }
                                    } else {
                                        status = "❌ Số tiền không hợp lệ"
                                    }
                                } catch (e: Exception) {
                                    status = "❌ Lỗi: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),  // ✅ GIỐNG
                        enabled = ! isLoading && rechargeAmount.isNotEmpty(),
                        shape = RoundedCornerShape(18.dp),  // ✅ GIỐNG
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
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier. size(28.dp),
                                    color = Color.White,
                                    strokeWidth = 4.dp
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = "Đang xử lý...",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            } else {
                                Text(
                                    text = "💳 Nạp tiền",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            // ✅ STATUS
            if (status.isNotEmpty()) {
                Spacer(modifier = Modifier. height(16.dp))  // ✅ GIỐNG
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(20.dp)),  // ✅ GIỐNG
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            status.startsWith("✅") -> Color(0xFFE8F5E9)
                            status.startsWith("⚠️") -> Color(0xFFFFF3E0)
                            else -> Color(0xFFFFEBEE)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),  // ✅ GIỐNG
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                status.startsWith("✅") -> "✅"
                                status.startsWith("⚠️") -> "⚠️"
                                else -> "❌"
                            },
                            fontSize = 28.sp  // ✅ GIỐNG
                        )
                        Spacer(modifier = Modifier. width(14.dp))
                        Text(
                            text = status. substring(2),
                            fontSize = 16.sp,  // ✅ GIỐNG
                            fontWeight = FontWeight.Bold,
                            color = when {
                                status.startsWith("✅") -> Color(0xFF4CAF50)
                                status.startsWith("⚠️") -> Color(0xFFFFA726)
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