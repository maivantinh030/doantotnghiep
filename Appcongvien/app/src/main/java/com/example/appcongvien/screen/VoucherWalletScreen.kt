package com.example.appcongvien.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.data.model.UserVoucherDTO
import com.example.appcongvien.data.model.VoucherDTO
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.VoucherViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

enum class VoucherStatus {
    ACTIVE, EXPIRING_SOON, EXPIRED, USED
}

fun UserVoucherDTO.resolveStatus(): VoucherStatus {
    if (isUsed) return VoucherStatus.USED
    val endDate = voucher?.endDate ?: return VoucherStatus.ACTIVE
    val days = daysUntilExpiry(endDate)
    return when {
        days < 0 -> VoucherStatus.EXPIRED
        days <= 7 -> VoucherStatus.EXPIRING_SOON
        else -> VoucherStatus.ACTIVE
    }
}

fun daysUntilExpiry(endDateStr: String?): Int {
    if (endDateStr == null) return 999
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val endDate = sdf.parse(endDateStr.take(10)) ?: return 0
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        ((endDate.time - today.time) / (1000L * 60 * 60 * 24)).toInt()
    } catch (e: Exception) {
        0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherWalletScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onVoucherSelected: ((UserVoucherDTO) -> Unit)? = null
) {
    val app = LocalContext.current.applicationContext as App
    val voucherViewModel: VoucherViewModel = viewModel(factory = VoucherViewModel.Factory(app.voucherRepository))

    val myVouchersState by voucherViewModel.myVouchersState.collectAsState()

    LaunchedEffect(Unit) {
        voucherViewModel.loadMyVouchers(size = 50)
    }

    val myVouchers = when (val state = myVouchersState) {
        is Resource.Success -> state.data.items
        else -> emptyList()
    }

    val expiringSoonVouchers = remember(myVouchers) { myVouchers.filter { it.resolveStatus() == VoucherStatus.EXPIRING_SOON } }
    val activeVouchers = remember(myVouchers) { myVouchers.filter { it.resolveStatus() == VoucherStatus.ACTIVE } }
    val usedVouchers = remember(myVouchers) { myVouchers.filter { it.resolveStatus() == VoucherStatus.USED } }
    val expiredVouchers = remember(myVouchers) { myVouchers.filter { it.resolveStatus() == VoucherStatus.EXPIRED } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Ví Voucher", fontWeight = FontWeight.Bold, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.WarmOrange)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Brush.verticalGradient(listOf(AppColors.SurfaceLight, Color.White))),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = myVouchersState) {
                is Resource.Loading -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.WarmOrange)
                    }
                }
                is Resource.Error -> item {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                is Resource.Success -> {
                    item {
                        WalletSummary(
                            totalVouchers = myVouchers.size,
                            activeVouchers = activeVouchers.size + expiringSoonVouchers.size,
                            expiringSoon = expiringSoonVouchers.size
                        )
                    }

                    if (expiringSoonVouchers.isNotEmpty()) {
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Sắp hết hạn (${expiringSoonVouchers.size})",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.PrimaryDark
                                )
                            }
                        }
                        items(expiringSoonVouchers) { uv ->
                            MyVoucherCard(
                                userVoucher = uv,
                                onSelectVoucher = onVoucherSelected?.let { cb ->
                                    { cb(uv); onBackClick() }
                                }
                            )
                        }
                    }

                    if (activeVouchers.isNotEmpty()) {
                        item {
                            Text(
                                text = "Có thể sử dụng (${activeVouchers.size})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark
                            )
                        }
                        items(activeVouchers) { uv ->
                            MyVoucherCard(
                                userVoucher = uv,
                                onSelectVoucher = onVoucherSelected?.let { cb ->
                                    { cb(uv); onBackClick() }
                                }
                            )
                        }
                    }

                    if (usedVouchers.isNotEmpty()) {
                        item {
                            Text(
                                text = "Đã sử dụng (${usedVouchers.size})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark
                            )
                        }
                        items(usedVouchers) { uv ->
                            MyVoucherCard(userVoucher = uv)
                        }
                    }

                    if (expiredVouchers.isNotEmpty()) {
                        item {
                            Text(
                                text = "Đã hết hạn (${expiredVouchers.size})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark
                            )
                        }
                        items(expiredVouchers) { uv ->
                            MyVoucherCard(userVoucher = uv)
                        }
                    }

                    if (myVouchers.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Bạn chưa có voucher nào.\nHãy nhận voucher từ trang Khuyến Mãi!",
                                    textAlign = TextAlign.Center,
                                    color = AppColors.PrimaryGray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
                null -> {}
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun WalletSummary(
    totalVouchers: Int,
    activeVouchers: Int,
    expiringSoon: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            AppColors.WarmOrange.copy(alpha = 0.1f),
                            AppColors.WarmOrange.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ví Voucher của bạn",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )
                    Surface(
                        shape = CircleShape,
                        color = AppColors.WarmOrangeSoft,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Text(
                            text = "$totalVouchers",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.WarmOrange,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryItem(label = "Có thể dùng", value = activeVouchers, color = Color(0xFF4CAF50))
                    SummaryItem(label = "Sắp hết hạn", value = expiringSoon, color = Color(0xFFFFC107))
                }
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$value", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 12.sp, color = AppColors.PrimaryGray)
    }
}

@Composable
fun MyVoucherCard(
    userVoucher: UserVoucherDTO,
    onSelectVoucher: (() -> Unit)? = null
) {
    val voucher: VoucherDTO? = userVoucher.voucher
    val status = userVoucher.resolveStatus()
    val discountType = voucher?.discountType?.toVoucherDiscountType() ?: VoucherDiscountType.AMOUNT
    val (voucherIcon, baseColor) = getVoucherStyleByType(discountType)

    val iconColor = when (status) {
        VoucherStatus.EXPIRED, VoucherStatus.USED -> baseColor.copy(alpha = 0.5f)
        else -> baseColor
    }
    val cardBg = when (status) {
        VoucherStatus.ACTIVE -> Color.White
        VoucherStatus.EXPIRING_SOON -> Color(0xFFFFF8E1)
        VoucherStatus.EXPIRED -> Color(0xFFFAFAFA)
        VoucherStatus.USED -> Color(0xFFF1F8E9)
    }
    val titleColor = when (status) {
        VoucherStatus.EXPIRED, VoucherStatus.USED -> AppColors.PrimaryGray
        else -> AppColors.PrimaryDark
    }

    val displayValue = voucher?.let {
        val v = it.discountValue.toBigDecimalOrNull()?.toInt() ?: 0
        when (discountType) {
            VoucherDiscountType.PERCENTAGE -> "$v%"
            VoucherDiscountType.AMOUNT -> "${v / 1000}K"
            VoucherDiscountType.FREE_PLAY -> "$v lượt"
        }
    } ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (status == VoucherStatus.ACTIVE || status == VoucherStatus.EXPIRING_SOON) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = iconColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(60.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            voucherIcon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = displayValue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = iconColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = voucher?.title ?: "Voucher",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                    if (!voucher?.description.isNullOrBlank()) {
                        Text(
                            text = voucher!!.description!!,
                            fontSize = 13.sp,
                            color = AppColors.PrimaryGray.copy(
                                alpha = if (status == VoucherStatus.EXPIRED || status == VoucherStatus.USED) 0.6f else 1f
                            )
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val minOrder = voucher?.minOrderValue?.toBigDecimalOrNull()?.toInt() ?: 0
                if (minOrder > 0) {
                    Text(
                        text = "Điều kiện: Đơn từ ${minOrder / 1000}K",
                        fontSize = 12.sp,
                        color = AppColors.PrimaryGray
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "HSD: ${formatInstantDate(voucher?.endDate)}",
                            fontSize = 12.sp,
                            color = when (status) {
                                VoucherStatus.EXPIRING_SOON -> Color(0xFFFFC107)
                                VoucherStatus.EXPIRED -> Color(0xFFF44336)
                                else -> AppColors.PrimaryGray
                            },
                            fontWeight = if (status == VoucherStatus.EXPIRING_SOON) FontWeight.Bold else FontWeight.Normal
                        )
                        if (status == VoucherStatus.EXPIRING_SOON) {
                            val days = daysUntilExpiry(voucher?.endDate)
                            Text(
                                text = "Còn $days ngày",
                                fontSize = 11.sp,
                                color = Color(0xFFF44336),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Nhận: ${formatInstantDate(userVoucher.createdAt)}",
                        fontSize = 11.sp,
                        color = AppColors.PrimaryGray.copy(alpha = 0.7f)
                    )
                }
            }

            when (status) {
                VoucherStatus.ACTIVE, VoucherStatus.EXPIRING_SOON -> {
                    Button(
                        onClick = { onSelectVoucher?.invoke() },
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (onSelectVoucher != null) AppColors.WarmOrange else AppColors.PrimaryGray.copy(alpha = 0.4f),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (onSelectVoucher != null) "Áp dụng vào giỏ hàng" else "Sử dụng tại quầy",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                VoucherStatus.USED -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Đã sử dụng thành công",
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                VoucherStatus.EXPIRED -> {
                    Text(
                        text = "Voucher đã hết hạn",
                        fontSize = 12.sp,
                        color = Color(0xFFF44336),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
