package com.park.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.park.data.model.CreateVoucherRequest
import com.park.data.model.UpdateVoucherRequest
import com.park.data.model.VoucherDTO
import com.park.ui.component.*
import com.park.ui.theme.AppColors
import com.park.ui.theme.AppTypography
import com.park.viewmodel.VoucherManagementViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun VoucherManagementScreen(viewModel: VoucherManagementViewModel = viewModel { VoucherManagementViewModel() }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        if (uiState.successMessage != null || uiState.errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.SurfaceLight)
            .padding(24.dp)
    ) {
        PageHeader(
            title = "Quản lý Voucher",
            subtitle = "${uiState.total} voucher trong hệ thống"
        ) {
            Button(
                onClick = { viewModel.showCreateDialog() },
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Tạo Voucher")
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            SnackbarMessage(uiState.successMessage, isError = false, modifier = Modifier.align(Alignment.TopEnd))
            SnackbarMessage(uiState.errorMessage, isError = true, modifier = Modifier.align(Alignment.TopEnd))
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.WarmOrange)
            }
        } else {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(AppColors.SurfaceLight).padding(16.dp, 12.dp)
                    ) {
                        Text("Mã Voucher", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                        Text("Tên", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(2f))
                        Text("Loại", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                        Text("Giá trị", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                        Text("Đã dùng", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                        Text("Hết hạn", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1.5f))
                        Text("Trạng thái", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                        Text("Thao tác", style = AppTypography.labelSmall, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
                    }
                    Divider(color = AppColors.LightGray)
                    LazyColumn {
                        items(uiState.vouchers) { voucher ->
                            VoucherRow(
                                voucher = voucher,
                                onEdit = { viewModel.showEditDialog(voucher) },
                                onDelete = { viewModel.deleteVoucher(voucher.voucherId) }
                            )
                            Divider(color = AppColors.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }

    if (uiState.showCreateDialog) {
        VoucherFormDialog(
            title = "Tạo Voucher mới",
            onDismiss = { viewModel.dismissDialogs() },
            onConfirm = { code, title, description, discountType, discountValue, maxDiscount, minOrderValue,
                          usageLimit, perUserLimit, applicableGames, startDate, endDate, isActive ->
                viewModel.createVoucher(
                    CreateVoucherRequest(
                        code = code,
                        title = title,
                        description = description,
                        discountType = discountType,
                        discountValue = discountValue,
                        maxDiscount = maxDiscount,
                        minOrderValue = minOrderValue,
                        usageLimit = usageLimit,
                        perUserLimit = perUserLimit,
                        applicableGames = applicableGames,
                        startDate = startDate,
                        endDate = endDate,
                        isActive = isActive
                    )
                )
            }
        )
    }

    if (uiState.showEditDialog && uiState.selectedVoucher != null) {
        val voucher = uiState.selectedVoucher!!
        VoucherFormDialog(
            title = "Cập nhật Voucher",
            initialCode = voucher.code,
            initialTitle = voucher.title,
            initialDescription = voucher.description ?: "",
            initialDiscountType = voucher.discountType,
            initialDiscountValue = voucher.discountValue.toDoubleOrNull() ?: 0.0,
            initialMaxDiscount = voucher.maxDiscount?.toDoubleOrNull(),
            initialMinOrderValue = voucher.minOrderValue?.toDoubleOrNull() ?: 0.0,
            initialUsageLimit = voucher.usageLimit,
            initialPerUserLimit = voucher.perUserLimit ?: 1,
            initialApplicableGames = voucher.applicableGames,
            initialStartDate = voucher.startDate ?: "",
            initialEndDate = voucher.endDate ?: "",
            initialIsActive = voucher.isActive,
            isEditMode = true,
            onDismiss = { viewModel.dismissDialogs() },
            onConfirm = { _, title, description, _, discountValue, maxDiscount, minOrderValue,
                          usageLimit, perUserLimit, _, _, endDate, isActive ->
                viewModel.updateVoucher(
                    voucher.voucherId,
                    UpdateVoucherRequest(
                        title = title,
                        description = description,
                        discountValue = discountValue,
                        maxDiscount = maxDiscount,
                        minOrderValue = minOrderValue,
                        usageLimit = usageLimit,
                        perUserLimit = perUserLimit,
                        endDate = endDate,
                        isActive = isActive
                    )
                )
            }
        )
    }
}

@Composable
private fun VoucherRow(voucher: VoucherDTO, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            voucher.code, fontWeight = FontWeight.Medium, fontSize = 13.sp,
            color = AppColors.WarmOrange, modifier = Modifier.weight(1f)
        )
        Text(voucher.title, style = AppTypography.bodyMedium, color = AppColors.PrimaryDark, modifier = Modifier.weight(2f))
        Text(voucher.discountType, style = AppTypography.bodyMedium, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
        val valueDisplay = if (voucher.discountType == "PERCENTAGE") "${voucher.discountValue}%" else formatCurrencyFull(voucher.discountValue.toDoubleOrNull() ?: 0.0)
        Text(valueDisplay, style = AppTypography.bodyMedium, color = AppColors.GreenSuccess, modifier = Modifier.weight(1f))
        Text("${voucher.usedCount}/${voucher.usageLimit ?: "∞"}", style = AppTypography.bodyMedium, color = AppColors.PrimaryGray, modifier = Modifier.weight(1f))
        Text(voucher.endDate ?: "Không hạn", style = AppTypography.bodyMedium, color = AppColors.PrimaryGray, modifier = Modifier.weight(1.5f))
        Box(modifier = Modifier.weight(1f)) {
            StatusBadge(if (voucher.isActive) "ACTIVE" else "INACTIVE")
        }
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = AppColors.BluePrimary, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = AppColors.RedError, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun VoucherFormDialog(
    title: String,
    initialCode: String = "",
    initialTitle: String = "",
    initialDescription: String = "",
    initialDiscountType: String = "PERCENT",
    initialDiscountValue: Double = 0.0,
    initialMaxDiscount: Double? = null,
    initialMinOrderValue: Double = 0.0,
    initialUsageLimit: Int? = null,
    initialPerUserLimit: Int = 1,
    initialApplicableGames: List<String>? = null,
    initialStartDate: String = "",
    initialEndDate: String = "",
    initialIsActive: Boolean = true,
    isEditMode: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (
        code: String, title: String, description: String?, discountType: String, 
        discountValue: Double, maxDiscount: Double?, minOrderValue: Double,
        usageLimit: Int?, perUserLimit: Int, applicableGames: List<String>?,
        startDate: String, endDate: String, isActive: Boolean
    ) -> Unit
) {
    // Auto-generate code for new vouchers
    val autoGeneratedCode = remember {
        if (initialCode.isBlank()) {
            val timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
            val random = (1000..9999).random()
            "VCHR-$timestamp-$random"
        } else {
            initialCode
        }
    }
    
    var code by remember { mutableStateOf(autoGeneratedCode) }
    var voucherTitle by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var discountType by remember { mutableStateOf(initialDiscountType) }
    var discountValueText by remember { mutableStateOf(if (initialDiscountValue > 0) initialDiscountValue.toString() else "") }
    var maxDiscountText by remember { mutableStateOf(initialMaxDiscount?.toString() ?: "") }
    var minOrderValueText by remember { mutableStateOf(if (initialMinOrderValue > 0) initialMinOrderValue.toString() else "") }
    var usageLimitText by remember { mutableStateOf(initialUsageLimit?.toString() ?: "") }
    var perUserLimitText by remember { mutableStateOf(initialPerUserLimit.toString()) }
    var applicableGamesText by remember { mutableStateOf(initialApplicableGames?.joinToString(", ") ?: "") }
    var startDate by remember { mutableStateOf(initialStartDate) }
    var endDate by remember { mutableStateOf(initialEndDate) }
    var isActive by remember { mutableStateOf(initialIsActive) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AppColors.White)) {
            Column(modifier = Modifier.padding(24.dp).width(500.dp).heightIn(max = 700.dp)) {
                Text(title, style = AppTypography.headlineLarge)
                Spacer(Modifier.height(16.dp))
                
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        val fieldColors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.WarmOrange, 
                            focusedLabelColor = AppColors.WarmOrange
                        )

                        // Show code field (read-only for edit, auto-generated for create)
                        OutlinedTextField(
                            value = code,
                            onValueChange = { },
                            label = { Text("Mã voucher (tự động)") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors,
                            enabled = false,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = AppColors.PrimaryGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = voucherTitle, onValueChange = { voucherTitle = it }, 
                            label = { Text("Tên voucher *") },
                            shape = RoundedCornerShape(12.dp), singleLine = true, 
                            modifier = Modifier.fillMaxWidth(), colors = fieldColors
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = description, onValueChange = { description = it }, 
                            label = { Text("Mô tả") },
                            shape = RoundedCornerShape(12.dp), 
                            modifier = Modifier.fillMaxWidth().height(100.dp), 
                            colors = fieldColors,
                            maxLines = 3
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        // Discount Type
                        Text("Loại giảm giá *", style = AppTypography.bodyMedium, color = AppColors.PrimaryGray)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("PERCENT" to "Phần trăm (%)", "FIXED_AMOUNT" to "Số tiền cố định").forEach { (type, label) ->
                                FilterChip(
                                    selected = discountType == type,
                                    onClick = { discountType = type },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AppColors.WarmOrange,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = discountValueText, onValueChange = { discountValueText = it },
                                label = { Text(if (discountType == "PERCENT") "Phần trăm giảm (%) *" else "Số tiền giảm (VND) *") },
                                shape = RoundedCornerShape(12.dp), singleLine = true, 
                                modifier = Modifier.weight(1f), colors = fieldColors
                            )
                            
                            if (discountType == "PERCENT") {
                                OutlinedTextField(
                                    value = maxDiscountText, onValueChange = { maxDiscountText = it },
                                    label = { Text("Giảm tối đa (VND)") },
                                    shape = RoundedCornerShape(12.dp), singleLine = true, 
                                    modifier = Modifier.weight(1f), colors = fieldColors
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = minOrderValueText, onValueChange = { minOrderValueText = it },
                            label = { Text("Giá trị đơn tối thiểu (VND)") },
                            shape = RoundedCornerShape(12.dp), singleLine = true, 
                            modifier = Modifier.fillMaxWidth(), colors = fieldColors,
                            placeholder = { Text("0 = Không yêu cầu") }
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = usageLimitText, onValueChange = { usageLimitText = it },
                                label = { Text("Tổng số lượt dùng") },
                                shape = RoundedCornerShape(12.dp), singleLine = true, 
                                modifier = Modifier.weight(1f), colors = fieldColors,
                                placeholder = { Text("Không giới hạn") }
                            )
                            
                            OutlinedTextField(
                                value = perUserLimitText, onValueChange = { perUserLimitText = it },
                                label = { Text("Số lượt/user *") },
                                shape = RoundedCornerShape(12.dp), singleLine = true, 
                                modifier = Modifier.weight(1f), colors = fieldColors
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = applicableGamesText, onValueChange = { applicableGamesText = it },
                            label = { Text("Game ID được áp dụng") },
                            shape = RoundedCornerShape(12.dp), 
                            modifier = Modifier.fillMaxWidth(), colors = fieldColors,
                            placeholder = { Text("Để trống = Tất cả game. Hoặc nhập ID cách nhau bằng dấu phẩy") },
                            maxLines = 2
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DateTimePickerField(
                                value = startDate,
                                onValueChange = { startDate = it },
                                label = "Ngày bắt đầu *",
                                modifier = Modifier.weight(1f),
                                colors = fieldColors
                            )
                            
                            DateTimePickerField(
                                value = endDate,
                                onValueChange = { endDate = it },
                                label = "Ngày hết hạn *",
                                modifier = Modifier.weight(1f),
                                colors = fieldColors
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        
                        // Is Active Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kích hoạt ngay", style = AppTypography.bodyMedium, color = AppColors.PrimaryDark)
                            Switch(
                                checked = isActive,
                                onCheckedChange = { isActive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AppColors.WarmOrange
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Hủy") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val value = discountValueText.toDoubleOrNull() ?: 0.0
                            val maxDiscount = maxDiscountText.toDoubleOrNull()
                            val minOrderValue = minOrderValueText.toDoubleOrNull() ?: 0.0
                            val usageLimit = usageLimitText.toIntOrNull()
                            val perUserLimit = perUserLimitText.toIntOrNull() ?: 1
                            val applicableGames = applicableGamesText
                                .split(",")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                                .takeIf { it.isNotEmpty() }
                            
                            if (voucherTitle.isNotBlank() && value > 0 
                                && startDate.isNotBlank() && endDate.isNotBlank()) {
                                onConfirm(
                                    code, voucherTitle, description.takeIf { it.isNotBlank() },
                                    discountType, value, maxDiscount, minOrderValue,
                                    usageLimit, perUserLimit, applicableGames,
                                    startDate, endDate, isActive
                                )
                            }
                        },
                        enabled = voucherTitle.isNotBlank() 
                            && discountValueText.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Lưu") }
                }
            }
        }
    }
}

@Composable
private fun DateTimePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    var showDialog by remember { mutableStateOf(false) }
    
    // Parse existing value or use current time
    val defaultDateTime = remember(value) {
        if (value.isNotBlank()) {
            try {
                // Try parsing as ISO instant (with Z timezone)
                val instant = Instant.parse(value)
                LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            } catch (e: Exception) {
                try {
                    // Fallback to ISO_DATE_TIME
                    LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME)
                } catch (e2: Exception) {
                    LocalDateTime.now()
                }
            }
        } else {
            LocalDateTime.now()
        }
    }
    
    OutlinedTextField(
        value = if (value.isNotBlank()) {
            try {
                // Try parsing as ISO instant (with Z timezone)
                val instant = Instant.parse(value)
                val dt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            } catch (e: Exception) {
                try {
                    // Fallback to ISO_DATE_TIME
                    val dt = LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME)
                    dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                } catch (e2: Exception) {
                    value
                }
            }
        } else "",
        onValueChange = { },
        label = { Text(label) },
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        modifier = modifier.clickable { showDialog = true },
        colors = colors,
        enabled = false,
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Chọn ngày",
                    tint = AppColors.WarmOrange
                )
            }
        }
    )
    
    if (showDialog) {
        DateTimePickerDialog(
            initialDateTime = defaultDateTime,
            onDismiss = { showDialog = false },
            onConfirm = { dateTime ->
                // Convert to UTC and format with Z suffix
                val zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault())
                val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"))
                val formatted = utcDateTime.format(DateTimeFormatter.ISO_INSTANT)
                onValueChange(formatted)
                showDialog = false
            }
        )
    }
}

@Composable
private fun DateTimePickerDialog(
    initialDateTime: LocalDateTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalDateTime) -> Unit
) {
    var year by remember { mutableStateOf(initialDateTime.year) }
    var month by remember { mutableStateOf(initialDateTime.monthValue) }
    var day by remember { mutableStateOf(initialDateTime.dayOfMonth) }
    var hour by remember { mutableStateOf(initialDateTime.hour) }
    var minute by remember { mutableStateOf(initialDateTime.minute) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.White)
        ) {
            Column(modifier = Modifier.padding(24.dp).width(400.dp)) {
                Text("Chọn ngày và giờ", style = AppTypography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                
                // Date Selection
                Text("Ngày", style = AppTypography.labelSmall, color = AppColors.PrimaryGray)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Day
                    OutlinedTextField(
                        value = day.toString(),
                        onValueChange = { 
                            it.toIntOrNull()?.let { value -> 
                                if (value in 1..31) day = value 
                            }
                        },
                        label = { Text("Ngày") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    // Month
                    OutlinedTextField(
                        value = month.toString(),
                        onValueChange = { 
                            it.toIntOrNull()?.let { value -> 
                                if (value in 1..12) month = value 
                            }
                        },
                        label = { Text("Tháng") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    // Year
                    OutlinedTextField(
                        value = year.toString(),
                        onValueChange = { 
                            it.toIntOrNull()?.let { value -> 
                                if (value in 2020..2050) year = value 
                            }
                        },
                        label = { Text("Năm") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Time Selection
                Text("Giờ", style = AppTypography.labelSmall, color = AppColors.PrimaryGray)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hour.toString().padStart(2, '0'),
                        onValueChange = { 
                            it.toIntOrNull()?.let { value -> 
                                if (value in 0..23) hour = value 
                            }
                        },
                        label = { Text("Giờ") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Text(":", style = AppTypography.headlineLarge, modifier = Modifier.align(Alignment.CenterVertically))
                    OutlinedTextField(
                        value = minute.toString().padStart(2, '0'),
                        onValueChange = { 
                            it.toIntOrNull()?.let { value -> 
                                if (value in 0..59) minute = value 
                            }
                        },
                        label = { Text("Phút") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                
                Spacer(Modifier.height(24.dp))
                
                // Preview
                Text(
                    "Xem trước: ${day.toString().padStart(2, '0')}/${month.toString().padStart(2, '0')}/$year ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}",
                    style = AppTypography.bodyMedium,
                    color = AppColors.PrimaryGray
                )
                
                Spacer(Modifier.height(16.dp))
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            try {
                                val dateTime = LocalDateTime.of(year, month, day, hour, minute)
                                onConfirm(dateTime)
                            } catch (e: Exception) {
                                // Invalid date, do nothing
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Xác nhận")
                    }
                }
            }
        }
    }
}
