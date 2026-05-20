package com.park.smartcard.screen.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.park.smartcard.data.model.TopUpResult
import com.park.smartcard.viewmodel.StaffTopUpMethod
import com.park.smartcard.viewmodel.TopUpViewModel
import org.jetbrains.skia.Image as SkiaImage
import java.net.URL

private val Orange = Color(0xFFFF6B35)

@Composable
fun TopUpScreen(vm: TopUpViewModel = viewModel { TopUpViewModel() }) {
    val s by vm.state.collectAsStateWithLifecycle()
    var amountInput by remember { mutableStateOf("") }

    LaunchedEffect(s.successMessage, s.errorMessage) {
        if (s.successMessage != null || s.errorMessage != null) {
            kotlinx.coroutines.delay(4000)
            vm.clearMessages()
        }
    }

    // Reset amount when customer changes
    LaunchedEffect(s.customer) { if (s.customer == null) amountInput = "" }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.widthIn(max = 600.dp).fillMaxHeight().padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.AccountBalanceWallet, null, tint = Orange, modifier = Modifier.size(28.dp))
                Column {
                    Text("Nạp tiền cho khách", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                    Text("Quẹt thẻ NFC của khách → Nhập số tiền → Nạp", fontSize = 13.sp, color = Color.Gray)
                }
            }

            // Feedback messages
            s.successMessage?.let {
                Surface(color = Color(0xFF4CAF50).copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                        Text(it, color = Color(0xFF2E7D32), fontSize = 13.sp)
                    }
                }
            }
            s.errorMessage?.let {
                Surface(color = Color(0xFFE53935).copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp)) {
                    Text(it, modifier = Modifier.fillMaxWidth().padding(12.dp), color = Color(0xFFB71C1C), fontSize = 13.sp)
                }
            }

            // Main card: connect panel or customer info
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                if (s.customer == null) {
                    CardConnectPanel(
                        isReading = s.isReading,
                        isFetching = s.isFetching,
                        onConnect = { vm.scanCard() }
                    )
                } else {
            CustomerInfoPanel(
                customer = s.customer!!,
                amountInput = amountInput,
                onAmountChange = { amountInput = it },
                isTopingUp = s.isTopingUp,
                isPollingMomo = s.isPollingMomo,
                selectedMethod = s.selectedMethod,
                momoPayment = s.momoPayment,
                momoStatusMessage = s.momoStatusMessage,
                onMethodChange = { vm.selectMethod(it) },
                onTopUp = { vm.topUp(amountInput) },
                onCheckMomoStatus = { vm.checkMomoStatusNow() },
                onCancelMomo = { vm.cancelMomoTopUp() },
                onNewCustomer = { vm.reset(); amountInput = "" }
            )
        }
            }
        }
    }
}

@Composable
private fun CardConnectPanel(isReading: Boolean, isFetching: Boolean, onConnect: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = Orange.copy(alpha = 0.12f),
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CreditCard, null, tint = Orange, modifier = Modifier.size(48.dp))
            }
        }

        Text(
            "Kết nối thẻ khách hàng",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A2E)
        )

        if (isFetching) {
            CircularProgressIndicator(color = Orange, modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
            Text("Đang tải thông tin khách hàng...", fontSize = 13.sp, color = Color.Gray)
        } else {
            Button(
                onClick = onConnect,
                enabled = !isReading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                modifier = Modifier.height(48.dp).widthIn(min = 180.dp)
            ) {
                if (isReading) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Đang kết nối...")
                } else {
                    Icon(Icons.Default.CreditCard, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Kết nối thẻ", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun CustomerInfoPanel(
    customer: com.park.smartcard.data.model.CustomerDTO,
    amountInput: String,
    onAmountChange: (String) -> Unit,
    isTopingUp: Boolean,
    isPollingMomo: Boolean,
    selectedMethod: StaffTopUpMethod,
    momoPayment: TopUpResult?,
    momoStatusMessage: String?,
    onMethodChange: (StaffTopUpMethod) -> Unit,
    onTopUp: () -> Unit,
    onCheckMomoStatus: () -> Unit,
    onCancelMomo: () -> Unit,
    onNewCustomer: () -> Unit
) {
    Column(
        modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Customer info
        Surface(color = Color(0xFFF0FFF4), shape = RoundedCornerShape(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(20.dp), color = Orange.copy(alpha = 0.12f)) {
                        Icon(Icons.Default.Person, null, tint = Orange, modifier = Modifier.padding(8.dp).size(22.dp))
                    }
                    Column {
                        Text(customer.fullName ?: "Chưa có tên", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(customer.phoneNumber, fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Số dư hiện tại", fontSize = 11.sp, color = Color.Gray)
                    Text("${customer.currentBalance} VNĐ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Orange)
                }
            }
        }

        Divider()
        Text("Chọn phương thức", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PaymentMethodButton(
                label = "Tiền mặt",
                icon = Icons.Default.Payments,
                selected = selectedMethod == StaffTopUpMethod.CASH,
                enabled = !isTopingUp && !isPollingMomo,
                modifier = Modifier.weight(1f),
                onClick = { onMethodChange(StaffTopUpMethod.CASH) }
            )
            PaymentMethodButton(
                label = "MoMo QR",
                icon = Icons.Default.QrCode2,
                selected = selectedMethod == StaffTopUpMethod.MOMO,
                enabled = !isTopingUp && !isPollingMomo,
                modifier = Modifier.weight(1f),
                onClick = { onMethodChange(StaffTopUpMethod.MOMO) }
            )
        }

        Text("Nhập số tiền nạp", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)

        OutlinedTextField(
            value = amountInput,
            onValueChange = onAmountChange,
            enabled = !isPollingMomo,
            label = { Text("Số tiền (VNĐ)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Icon(Icons.Default.AttachMoney, null, tint = Orange) },
            placeholder = { Text("Ví dụ: 100000") }
        )

        // Quick amounts
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("50000", "100000", "200000", "500000").forEach { quick ->
                OutlinedButton(
                    onClick = { onAmountChange(quick) },
                    enabled = !isPollingMomo,
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = if (amountInput == quick)
                        ButtonDefaults.outlinedButtonColors(containerColor = Orange.copy(alpha = 0.1f))
                    else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("${quick.toLong() / 1000}K", fontSize = 12.sp, color = if (amountInput == quick) Orange else Color.Gray)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onTopUp,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                enabled = amountInput.isNotBlank() && !isTopingUp && !isPollingMomo
            ) {
                if (isTopingUp) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(
                        if (selectedMethod == StaffTopUpMethod.MOMO) Icons.Default.QrCode2 else Icons.Default.AccountBalanceWallet,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (selectedMethod == StaffTopUpMethod.MOMO) "Tạo QR MoMo" else "Nạp tiền",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            OutlinedButton(
                onClick = onNewCustomer,
                modifier = Modifier.height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isTopingUp && !isPollingMomo
            ) {
                Icon(Icons.Default.Nfc, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Thẻ khác")
            }
        }

        if (selectedMethod == StaffTopUpMethod.MOMO && momoPayment != null) {
            MomoQrPanel(
                payment = momoPayment,
                isPolling = isPollingMomo,
                statusMessage = momoStatusMessage,
                onCheckStatus = onCheckMomoStatus,
                onCancel = onCancelMomo
            )
        }
    }
}

@Composable
private fun PaymentMethodButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) Orange.copy(alpha = 0.12f) else Color.Transparent,
            contentColor = if (selected) Orange else Color(0xFF555555)
        )
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MomoQrPanel(
    payment: TopUpResult,
    isPolling: Boolean,
    statusMessage: String?,
    onCheckStatus: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        color = Color(0xFFFFF3F8),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFD82D8B).copy(alpha = 0.14f)) {
                    Icon(Icons.Default.QrCode2, null, tint = Color(0xFFD82D8B), modifier = Modifier.padding(8.dp).size(22.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("QR thanh toán MoMo", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E))
                    Text("Khách dùng MoMo quét mã này để nạp tiền vào ví/thẻ.", fontSize = 12.sp, color = Color.Gray)
                }
            }

            RemoteQrImage(payment.qrCodeUrl)

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TopUpInfoRow("Mã đơn", payment.orderId ?: payment.paymentId)
                TopUpInfoRow("Số tiền", "${payment.amount} VNĐ")
                TopUpInfoRow("Trạng thái", payment.status)
            }

            statusMessage?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPolling) CircularProgressIndicator(Modifier.size(16.dp), color = Color(0xFFD82D8B), strokeWidth = 2.dp)
                    Text(it, fontSize = 12.sp, color = Color(0xFFD82D8B))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onCheckStatus,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Kiểm tra")
                }
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isPolling
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Đóng QR")
                }
            }
        }
    }
}

@Composable
private fun RemoteQrImage(qrCodeUrl: String?) {
    var qrImage by remember(qrCodeUrl) { mutableStateOf<ImageBitmap?>(null) }
    var failed by remember(qrCodeUrl) { mutableStateOf(false) }

    LaunchedEffect(qrCodeUrl) {
        qrImage = null
        failed = false
        if (qrCodeUrl.isNullOrBlank()) {
            failed = true
            return@LaunchedEffect
        }
        qrImage = withContext(Dispatchers.IO) {
            try {
                SkiaImage.makeFromEncoded(URL(qrCodeUrl).readBytes()).toComposeImageBitmap()
            } catch (_: Exception) {
                failed = true
                null
            }
        }
    }

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(240.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(12.dp)) {
            when {
                qrImage != null -> Image(qrImage!!, contentDescription = "QR MoMo", modifier = Modifier.fillMaxSize())
                failed -> Text("Không tải được QR", color = Color(0xFFB71C1C), fontSize = 13.sp)
                else -> CircularProgressIndicator(color = Color(0xFFD82D8B), modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
            }
        }
    }
}

@Composable
private fun TopUpInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E))
    }
}
