package org.example.project.screen.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.Normalizer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val Orange = Color(0xFFFF6B35)
private val DisplayDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

@Composable
fun CardRequestsScreen(vm: CardRequestsViewModel = viewModel { CardRequestsViewModel() }) {
    val s by vm.state.collectAsStateWithLifecycle()
    val filteredRequests = remember(s.requests, s.searchQuery) {
        s.requests.filter { matchesSearch(it, s.searchQuery) }
    }

    LaunchedEffect(s.successMessage, s.errorMessage) {
        if (s.successMessage != null || s.errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            vm.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Inbox, null, tint = Orange, modifier = Modifier.size(28.dp))
                Column {
                    Text(
                        "Yêu cầu cấp thẻ",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                    Text(
                        "Duyệt yêu cầu từ khách dùng app",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }
            IconButton(onClick = { vm.load() }) {
                Icon(Icons.Default.Refresh, null, tint = Orange)
            }
        }

        s.successMessage?.let {
            Surface(
                color = Color(0xFF4CAF50).copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    it,
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    color = Color(0xFF2E7D32),
                    fontSize = 13.sp
                )
            }
        }
        s.errorMessage?.let {
            Surface(
                color = Color(0xFFE53935).copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    it,
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    color = Color(0xFFB71C1C),
                    fontSize = 13.sp
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "PENDING" to "Chờ duyệt",
                "REJECTED" to "Từ chối",
                "COMPLETED" to "Hoàn thành"
            ).forEach { (status, label) ->
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

        OutlinedTextField(
            value = s.searchQuery,
            onValueChange = vm::updateSearchQuery,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Orange) },
            trailingIcon = {
                if (s.searchQuery.isNotBlank()) {
                    IconButton(onClick = { vm.updateSearchQuery("") }) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            },
            label = { Text("Tìm người dùng") },
            placeholder = { Text("Nhập tên, số điện thoại, email hoặc mã khách hàng") },
            shape = RoundedCornerShape(12.dp)
        )

        Text(
            "Hiển thị ${filteredRequests.size}/${s.requests.size} yêu cầu",
            fontSize = 12.sp,
            color = Color.Gray
        )

        when {
            s.isLoading -> {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Orange)
                }
            }

            s.requests.isEmpty() -> {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("Không có yêu cầu nào", color = Color.Gray)
                }
            }

            filteredRequests.isEmpty() -> {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("Không tìm thấy người dùng phù hợp", color = Color.Gray)
                }
            }

            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredRequests, key = { it.requestId }) { req ->
                        CardRequestItem(
                            req = req,
                            onReview = { vm.openReviewDialog(req) }
                        )
                    }
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
private fun CardRequestItem(req: CardRequestDTO, onReview: () -> Unit) {
    val requester = req.requester
    val (statusColor, statusLabel) = when (req.status) {
        "PENDING" -> Color(0xFFF59E0B) to "Chờ duyệt"
        "APPROVED" -> Color.Gray to "Hoàn thành"
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
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(shape = RoundedCornerShape(18.dp), color = Orange.copy(alpha = 0.12f)) {
                        Icon(Icons.Default.Person, null, tint = Orange, modifier = Modifier.padding(10.dp).size(22.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            requester?.fullName?.takeUnless { it.isNullOrBlank() } ?: "Chưa có họ tên",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color(0xFF1A1A2E)
                        )
                        Text(
                            requester?.phoneNumber?.takeUnless { it.isBlank() }
                                ?: requester?.email?.takeUnless { it.isNullOrBlank() }
                                ?: "Không có SĐT hoặc email",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            "Mã khách hàng: ${req.userId}",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(alpha = 0.15f)) {
                        Text(
                            statusLabel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    requester?.currentBalance?.let {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF3F4F6)) {
                            Text(
                                "Số dư ${formatMoney(it)}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                color = Color(0xFF374151),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF3F4F6))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                requester?.email?.takeUnless { it.isNullOrBlank() }?.let {
                    RequestDetailRow(label = "Email", value = it)
                }
                requester?.dateOfBirth?.takeUnless { it.isNullOrBlank() }?.let {
                    RequestDetailRow(label = "Ngày sinh", value = it)
                }
                RequestDetailRow(
                    label = "Tiền cọc",
                    value = "${formatMoney(req.depositAmount)} | ${if (req.depositPaidOnline) "Đã thanh toán online" else "Thu tại quầy"}"
                )
                req.note?.takeUnless { it.isBlank() }?.let {
                    RequestDetailRow(label = "Ghi chú", value = it)
                }
                req.createdAt?.let {
                    RequestDetailRow(label = "Tạo lúc", value = formatDateTime(it))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                if (req.status == "PENDING") {
                    Button(
                        onClick = onReview,
                        colors = ButtonDefaults.buttonColors(containerColor = Orange),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("Xem xét", fontSize = 13.sp)
                    }
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
    val requester = req.requester

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(24.dp).width(420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Xem xét yêu cầu cấp thẻ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                HorizontalDivider()
                RequestDetailRow(label = "Mã khách hàng", value = req.userId)
                RequestDetailRow(
                    label = "Họ tên",
                    value = requester?.fullName?.takeUnless { it.isNullOrBlank() } ?: "Chưa có"
                )
                RequestDetailRow(
                    label = "Số điện thoại",
                    value = requester?.phoneNumber?.takeUnless { it.isBlank() } ?: "Chưa có"
                )
                requester?.email?.takeUnless { it.isNullOrBlank() }?.let {
                    RequestDetailRow(label = "Email", value = it)
                }
                requester?.currentBalance?.let {
                    RequestDetailRow(label = "Số dư hiện tại", value = formatMoney(it))
                }
                RequestDetailRow(
                    label = "Tiền cọc",
                    value = "${formatMoney(req.depositAmount)} | ${if (req.depositPaidOnline) "Đã thanh toán online" else "Thu tại quầy"}"
                )
                req.note?.takeUnless { it.isBlank() }?.let {
                    RequestDetailRow(label = "Ghi chú", value = it)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) { Text("Hủy") }
                    OutlinedButton(
                        onClick = { onConfirm(false, null) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                        enabled = !isLoading
                    ) {
                        Text("Từ chối")
                    }
                    Button(
                        onClick = { onConfirm(true, null) },
                        colors = ButtonDefaults.buttonColors(containerColor = Orange),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Duyệt")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestDetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontSize = 11.sp, color = Color(0xFF9CA3AF))
        Text(value, fontSize = 13.sp, color = Color(0xFF374151))
    }
}

private fun matchesSearch(req: CardRequestDTO, query: String): Boolean {
    if (query.isBlank()) return true
    val keyword = normalize(query)
    val requester = req.requester
    return listOf(
        req.userId,
        requester?.fullName,
        requester?.phoneNumber,
        requester?.email
    ).any { normalize(it).contains(keyword) }
}

private fun normalize(value: String?): String {
    if (value.isNullOrBlank()) return ""
    val normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
    return normalized.replace("\\p{Mn}+".toRegex(), "").lowercase().trim()
}

private fun formatMoney(amount: String?): String {
    val number = amount?.toBigDecimalOrNull() ?: return amount ?: "0 VND"
    val rounded = number.setScale(0, RoundingMode.HALF_UP)
    return "${DecimalFormat("#,##0").format(rounded)} VND"
}

private fun formatDateTime(value: String): String {
    return try {
        Instant.parse(value)
            .atZone(ZoneId.systemDefault())
            .format(DisplayDateFormatter)
    } catch (_: Exception) {
        value.replace("T", " ").take(16)
    }
}
