package org.example.project.screen.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import org.example.project.data.model.CardRequestDTO
import org.example.project.viewmodel.CardRequestsViewModel

private val Orange = Color(0xFFFF6B35)

@Composable
fun CardRequestsScreen(vm: CardRequestsViewModel = viewModel { CardRequestsViewModel() }) {
    val s by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(s.successMessage, s.errorMessage) {
        if (s.successMessage != null || s.errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            vm.clearMessages()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Inbox, null, tint = Orange, modifier = Modifier.size(28.dp))
                Column {
                    Text("Yêu cầu cấp thẻ", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                    Text("Duyệt yêu cầu từ khách dùng app", fontSize = 13.sp, color = Color.Gray)
                }
            }
            IconButton(onClick = { vm.load() }) {
                Icon(Icons.Default.Refresh, null, tint = Orange)
            }
        }

        // Feedback
        s.successMessage?.let { Surface(color = Color(0xFF4CAF50).copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp)) { Text(it, modifier = Modifier.fillMaxWidth().padding(12.dp), color = Color(0xFF2E7D32), fontSize = 13.sp) } }
        s.errorMessage?.let { Surface(color = Color(0xFFE53935).copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp)) { Text(it, modifier = Modifier.fillMaxWidth().padding(12.dp), color = Color(0xFFB71C1C), fontSize = 13.sp) } }

        // Filter chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("PENDING" to "Chờ duyệt", "APPROVED" to "Đã duyệt", "REJECTED" to "Từ chối", "COMPLETED" to "Hoàn thành").forEach { (status, label) ->
                FilterChip(
                    selected = s.statusFilter == status,
                    onClick = { vm.load(status) },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Orange.copy(alpha = 0.15f),
                        selectedLabelColor = Orange
                    )
                )
            }
        }

        if (s.isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Orange)
            }
        } else if (s.requests.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("Không có yêu cầu nào", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(s.requests) { req ->
                    CardRequestItem(
                        req = req,
                        onReview = { vm.openReviewDialog(req) },
                        onComplete = { vm.issueApprovedRequest(req) }
                    )
                }
            }
        }
    }

    if (s.showReviewDialog && s.selectedRequest != null) {
        ReviewDialog(
            req = s.selectedRequest!!,
            isLoading = s.isSubmitting,
            onConfirm = { approved, note -> vm.review(approved, note) },
            onDismiss = { vm.closeReviewDialog() }
        )
    }
}

@Composable
private fun CardRequestItem(req: CardRequestDTO, onReview: () -> Unit, onComplete: () -> Unit) {
    val (statusColor, statusLabel) = when (req.status) {
        "PENDING" -> Color(0xFFF59E0B) to "Chờ duyệt"
        "APPROVED" -> Color(0xFF10B981) to "Đã duyệt"
        "REJECTED" -> Color(0xFFE53935) to "Từ chối"
        "COMPLETED" -> Color.Gray to "Hoàn thành"
        else -> Color.Gray to req.status
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("User: ${req.userId.take(8)}...", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(alpha = 0.15f)) {
                        Text(statusLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    "Cọc: ${req.depositAmount} VNĐ · ${if (req.depositPaidOnline) "Online" else "Tại quầy"}",
                    fontSize = 12.sp, color = Color.Gray
                )
                req.note?.let { Text("Ghi chú: $it", fontSize = 12.sp, color = Color.Gray) }
                req.createdAt?.take(10)?.let { Text(it, fontSize = 11.sp, color = Color.LightGray) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (req.status == "PENDING") {
                    Button(
                        onClick = onReview,
                        colors = ButtonDefaults.buttonColors(containerColor = Orange),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) { Text("Xem xét", fontSize = 13.sp) }
                }
                if (req.status == "APPROVED") {
                    OutlinedButton(
                        onClick = onComplete,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) { Text("Ghi thẻ", fontSize = 13.sp) }
                }
            }
        }
    }
}

@Composable
private fun ReviewDialog(
    req: CardRequestDTO,
    isLoading: Boolean,
    onConfirm: (Boolean, String?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp).width(400.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Xem xét yêu cầu cấp thẻ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Divider()
                Text("User ID: ${req.userId}", fontSize = 13.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                    TextButton(onClick = onDismiss) { Text("Hủy") }
                    OutlinedButton(
                        onClick = { onConfirm(false, null) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                        enabled = !isLoading
                    ) { Text("Từ chối") }
                    Button(
                        onClick = { onConfirm(true, null) },
                        colors = ButtonDefaults.buttonColors(containerColor = Orange),
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Duyệt")
                    }
                }
            }
        }
    }
}
