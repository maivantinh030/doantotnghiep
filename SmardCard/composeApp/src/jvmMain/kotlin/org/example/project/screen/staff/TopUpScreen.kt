package org.example.project.screen.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.project.viewmodel.TopUpViewModel

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
                        onTopUp = { vm.topUp(amountInput) },
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
    customer: org.example.project.data.model.CustomerDTO,
    amountInput: String,
    onAmountChange: (String) -> Unit,
    isTopingUp: Boolean,
    onTopUp: () -> Unit,
    onNewCustomer: () -> Unit
) {
    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
        Text("Nhập số tiền nạp", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)

        OutlinedTextField(
            value = amountInput,
            onValueChange = onAmountChange,
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
                enabled = amountInput.isNotBlank() && !isTopingUp
            ) {
                if (isTopingUp) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Nạp tiền", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            OutlinedButton(
                onClick = onNewCustomer,
                modifier = Modifier.height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Nfc, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Thẻ khác")
            }
        }
    }
}
