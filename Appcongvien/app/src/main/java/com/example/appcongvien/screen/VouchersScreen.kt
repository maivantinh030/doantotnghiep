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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.data.model.VoucherDTO
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.VoucherViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

enum class VoucherDiscountType {
    PERCENTAGE, AMOUNT, FREE_PLAY
}

fun String.toVoucherDiscountType(): VoucherDiscountType = when (this.uppercase()) {
    "PERCENT", "PERCENTAGE" -> VoucherDiscountType.PERCENTAGE
    "FIXED_AMOUNT", "AMOUNT" -> VoucherDiscountType.AMOUNT
    "FREE_PLAY", "FREEPLAY", "FREE" -> VoucherDiscountType.FREE_PLAY
    else -> VoucherDiscountType.AMOUNT
}

fun formatInstantDate(dateStr: String?): String {
    if (dateStr == null) return "N/A"
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val normalized = dateStr.substringBefore(".").replace("Z", "")
        val date = sdf.parse(normalized) ?: return dateStr.take(10)
        val displayFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        displayFmt.format(date)
    } catch (e: Exception) {
        try {
            val d = dateStr.take(10)
            "${d.substring(8, 10)}/${d.substring(5, 7)}/${d.substring(0, 4)}"
        } catch (e2: Exception) {
            dateStr
        }
    }
}

fun VoucherDTO.getDisplayValue(): String {
    val value = discountValue.toBigDecimalOrNull()?.toInt() ?: 0
    return when (discountType.toVoucherDiscountType()) {
        VoucherDiscountType.PERCENTAGE -> "$value%"
        VoucherDiscountType.AMOUNT -> "${value / 1000}K"
        VoucherDiscountType.FREE_PLAY -> "$value lượt"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VouchersScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val app = LocalContext.current.applicationContext as App
    val voucherViewModel: VoucherViewModel = viewModel(factory = VoucherViewModel.Factory(app.voucherRepository))

    val vouchersState by voucherViewModel.vouchersState.collectAsState()
    val claimState by voucherViewModel.claimState.collectAsState()

    var selectedType by remember { mutableStateOf<VoucherDiscountType?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        voucherViewModel.loadVouchers(size = 50)
    }

    LaunchedEffect(claimState) {
        when (val state = claimState) {
            is Resource.Success -> {
                scope.launch { snackbarHostState.showSnackbar("Đã nhận voucher thành công!") }
                voucherViewModel.resetClaimState()
            }
            is Resource.Error -> {
                scope.launch { snackbarHostState.showSnackbar(state.message) }
                voucherViewModel.resetClaimState()
            }
            else -> {}
        }
    }

    val vouchers = when (val state = vouchersState) {
        is Resource.Success -> state.data.items
        else -> emptyList()
    }

    val filteredVouchers = if (selectedType == null) {
        vouchers
    } else {
        vouchers.filter { it.discountType.toVoucherDiscountType() == selectedType }
    }

    val filters = listOf(
        null to "Tất cả",
        VoucherDiscountType.PERCENTAGE to "Giảm %",
        VoucherDiscountType.AMOUNT to "Giảm tiền",
        VoucherDiscountType.FREE_PLAY to "Miễn phí"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Voucher Khuyến Mãi", fontWeight = FontWeight.Bold, color = Color.White)
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
            item { VoucherBanner() }

            item {
                Text("Danh mục", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.PrimaryDark)
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(filters) { (type, label) ->
                        FilterChip(
                            onClick = { selectedType = type },
                            label = { Text(label) },
                            selected = selectedType == type,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppColors.WarmOrange,
                                selectedLabelColor = Color.White,
                                labelColor = AppColors.PrimaryDark
                            )
                        )
                    }
                }
            }

            when (val state = vouchersState) {
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
                        Text(
                            text = "Có ${filteredVouchers.size} voucher khả dụng",
                            fontSize = 14.sp,
                            color = AppColors.PrimaryGray
                        )
                    }
                    items(filteredVouchers) { voucher ->
                        VoucherCard(
                            voucher = voucher,
                            isClaimLoading = claimState is Resource.Loading,
                            onClaimClick = { voucherViewModel.claimVoucher(voucher.voucherId) }
                        )
                    }
                }
                null -> {}
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun VoucherBanner() {
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
                            AppColors.WarmOrange.copy(alpha = 0.9f),
                            AppColors.WarmOrange.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Voucher Park Adventure",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Tiết kiệm hơn với voucher độc quyền!",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Redeem,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VoucherCard(
    voucher: VoucherDTO,
    isClaimLoading: Boolean = false,
    onClaimClick: () -> Unit
) {
    val discountType = voucher.discountType.toVoucherDiscountType()
    val (voucherIcon, iconColor) = getVoucherStyleByType(discountType)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
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
                            text = voucher.getDisplayValue(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = iconColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = voucher.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )
                    if (!voucher.description.isNullOrBlank()) {
                        Text(
                            text = voucher.description,
                            fontSize = 13.sp,
                            color = AppColors.PrimaryGray
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val minOrder = voucher.minOrderValue?.toBigDecimalOrNull()?.toInt() ?: 0
                if (minOrder > 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Điều kiện:", fontSize = 12.sp, color = AppColors.PrimaryGray)
                        Text(
                            "Đơn từ ${minOrder / 1000}K",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.WarmOrange
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HSD: ${formatInstantDate(voucher.endDate)}",
                        fontSize = 12.sp,
                        color = AppColors.PrimaryGray
                    )

                    Button(
                        onClick = onClaimClick,
                        enabled = !isClaimLoading,
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.WarmOrange,
                            contentColor = Color.White
                        )
                    ) {
                        if (isClaimLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Nhận ngay", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

fun getVoucherStyleByType(discountType: VoucherDiscountType): Pair<ImageVector, Color> {
    return when (discountType) {
        VoucherDiscountType.PERCENTAGE -> Pair(Icons.Default.Percent, AppColors.WarmOrange)
        VoucherDiscountType.AMOUNT -> Pair(Icons.Default.LocalOffer, Color(0xFF4CAF50))
        VoucherDiscountType.FREE_PLAY -> Pair(Icons.Default.Star, Color(0xFFFFD700))
    }
}
