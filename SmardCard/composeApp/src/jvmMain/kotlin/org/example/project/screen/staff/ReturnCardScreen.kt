package org.example.project.screen.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.project.data.model.CardDTO
import org.example.project.viewmodel.ReturnCardViewModel

private val Orange = Color(0xFFFF6B35)
private val Red = Color(0xFFE53935)

@Composable
fun ReturnCardScreen(vm: ReturnCardViewModel = viewModel { ReturnCardViewModel() }) {
    val s by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(s.successMessage, s.errorMessage) {
        if (s.successMessage != null || s.errorMessage != null) {
            kotlinx.coroutines.delay(5000)
            vm.clearMessages()
        }
    }

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
                Icon(Icons.Default.AssignmentReturn, null, tint = Orange, modifier = Modifier.size(28.dp))
                Column {
                    Text("Trả thẻ / Hoàn cọc", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                    Text("Quẹt thẻ NFC của khách → Hoàn balance + tiền cọc", fontSize = 13.sp, color = Color.Gray)
                }
            }

            // Success result
            s.successMessage?.let { msg ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FFF4)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF4CAF50).copy(alpha = 0.15f)) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.padding(8.dp).size(24.dp))
                        }
                        Column {
                            Text("Trả thẻ thành công", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            Text(msg, fontSize = 13.sp, color = Color(0xFF2E7D32))
                        }
                    }
                }
            }

            s.errorMessage?.let {
                Surface(color = Red.copy(alpha = 0.1f), shape = RoundedCornerShape(10.dp)) {
                    Text(it, modifier = Modifier.fillMaxWidth().padding(12.dp), color = Red.copy(alpha = 0.9f), fontSize = 13.sp)
                }
            }

            // Main card: connect panel or return summary
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                if (s.returnSummary != null) {
                    ReturnSummaryPanel(
                        summary = s.returnSummary!!,
                        onScanAgain = { vm.reset() }
                    )
                } else {
                    ReturnCardConnectPanel(
                        isReading = s.isReading,
                        isFetching = s.isFetching,
                        isReturning = s.isReturning,
                        onConnect = { vm.scanCard() }
                    )
                }
            }
        }
    }

    // Confirm return dialog
    if (s.showConfirmDialog && s.scannedCard != null) {
        ReturnConfirmDialog(
            card = s.scannedCard!!,
            onConfirm = { vm.confirmReturn() },
            onDismiss = { vm.cancelConfirm() }
        )
    }
}

@Composable
private fun ReturnCardConnectPanel(
    isReading: Boolean,
    isFetching: Boolean,
    isReturning: Boolean,
    onConnect: () -> Unit
) {
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
                Icon(Icons.Default.AssignmentReturn, null, tint = Orange, modifier = Modifier.size(48.dp))
            }
        }

        Text(
            "Kết nối thẻ khách hàng",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A2E)
        )

        when {
            isFetching -> {
                CircularProgressIndicator(color = Orange, modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                Text("Đang tải thông tin thẻ...", fontSize = 13.sp, color = Color.Gray)
            }
            isReturning -> {
                CircularProgressIndicator(color = Red, modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                Text("Đang xử lý trả thẻ...", fontSize = 13.sp, color = Color.Gray)
            }
            else -> {
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
}

@Composable
private fun ReturnSummaryPanel(summary: org.example.project.data.model.ReturnSummary, onScanAgain: () -> Unit) {
    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Chi tiết hoàn tiền", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Divider()
        ReturnDetailRow("Card ID", summary.cardId)
        ReturnDetailRow("Hoàn balance", "${summary.refundedBalance} VNĐ", Color(0xFF4CAF50))
        ReturnDetailRow("Hoàn tiền cọc", "${summary.refundedDeposit} VNĐ", Color(0xFF4CAF50))
        val total = (summary.refundedBalance.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO)
            .add(summary.refundedDeposit.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO)
        ReturnDetailRow("Tổng hoàn tiền mặt", "$total VNĐ", Orange, bold = true)
        Spacer(Modifier.height(4.dp))
        Button(
            onClick = onScanAgain,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Orange)
        ) {
            Icon(Icons.Default.Nfc, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Quẹt thẻ tiếp theo")
        }
    }
}

@Composable
private fun ReturnConfirmDialog(card: CardDTO, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp).width(380.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Xác nhận trả thẻ", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                // Warning box
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFA726).copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFF57C00), modifier = Modifier.size(16.dp))
                            Text("Thẻ: ${card.cardId}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5D4037))
                        }
                        Text("Cọc: ${card.depositAmount} VNĐ (${card.depositStatus})", fontSize = 13.sp, color = Color(0xFF6D4C41))
                        Text("Trạng thái: ${card.status}", fontSize = 13.sp, color = Color(0xFF6D4C41))
                    }
                }

                Text("Hành động này sẽ unlink thẻ và hoàn tiền cho khách.", fontSize = 13.sp, color = Color.Gray)

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Hủy") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Red)
                    ) {
                        Text("Xác nhận trả thẻ")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReturnDetailRow(label: String, value: String, valueColor: Color = Color(0xFF1A1A2E), bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, color = valueColor, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}
