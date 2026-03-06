package com.example.appcongvien.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appcongvien.data.model.OrderDTO
import com.example.appcongvien.data.model.OrderItemRequest
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.data.model.UserVoucherDTO
import com.example.appcongvien.data.model.VoucherDTO
import com.example.appcongvien.navigation.Screen
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.OrderViewModel

data class CartItem(
    val gameId: String,
    val gameName: String,
    val pricePerTurn: Int,
    val discount: Int = 0,
    var quantity: Int
) {
    val discountedPrice: Int
        get() = if (discount > 0) pricePerTurn * (100 - discount) / 100 else pricePerTurn

    val totalPrice: Int
        get() = discountedPrice * quantity

    val savedAmount: Int
        get() = if (discount > 0) (pricePerTurn - discountedPrice) * quantity else 0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onPaymentClick: () -> Unit = {},
    navController: NavController
) {
    val context = LocalContext.current
    val app = context.applicationContext as com.example.appcongvien.App
    val cartViewModel = app.cartViewModel

    // ViewModels
    val orderViewModel: OrderViewModel = viewModel(
        factory = OrderViewModel.Factory(app.orderRepository)
    )

    // Cart state
    val cartItems by cartViewModel.cartItems.collectAsState()
    val selectedVoucher by cartViewModel.selectedVoucher.collectAsState()
    val selectedUserVoucher by cartViewModel.selectedUserVoucher.collectAsState()

    // Order state
    val createOrderState by orderViewModel.createOrderState.collectAsState()

    // UI states
    var isProcessing by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successOrder by remember { mutableStateOf<OrderDTO?>(null) }

    // Watch order creation result
    LaunchedEffect(createOrderState) {
        when (val state = createOrderState) {
            is Resource.Loading -> {
                isProcessing = true
            }
            is Resource.Success -> {
                isProcessing = false
                successOrder = state.data
                showSuccessDialog = true
            }
            is Resource.Error -> {
                isProcessing = false
                errorMessage = state.message
                showErrorDialog = true
                orderViewModel.resetCreateOrderState()
            }
            null -> {
                isProcessing = false
            }
        }
    }

    // Calculate totals
    val subtotal = cartItems.sumOf { it.totalPrice }
    val totalSaved = cartItems.sumOf { it.savedAmount }
    val voucherDiscount = calculateVoucherDiscount(selectedUserVoucher?.voucher, subtotal)
    val finalTotal = (subtotal - voucherDiscount).coerceAtLeast(0)

    // Payment handler
    val handlePaymentClick = {
        val items = cartItems.map { OrderItemRequest(it.gameId, it.quantity) }
        orderViewModel.createOrder(
            items = items,
            voucherCode = selectedVoucher,
            paymentMethod = "BALANCE"
        )
    }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Thanh Toán Thành Công!",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    successOrder?.let { order ->
                        Text(
                            text = "Đơn hàng #${order.orderId.take(8).uppercase()}",
                            fontSize = 13.sp,
                            color = AppColors.PrimaryGray
                        )
                        Divider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tổng thanh toán:", fontSize = 14.sp)
                            Text(
                                text = formatAmount(order.totalAmount) + " đ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.WarmOrange
                            )
                        }
                        if (order.discountAmount != "0" && order.discountAmount != "0.00") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Đã giảm:", fontSize = 13.sp, color = Color(0xFF4CAF50))
                                Text(
                                    text = "-" + formatAmount(order.discountAmount) + " đ",
                                    fontSize = 13.sp,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Vé chơi game đã được cộng vào tài khoản của bạn.",
                        fontSize = 13.sp,
                        color = AppColors.PrimaryGray,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        cartViewModel.clearCart()
                        orderViewModel.resetCreateOrderState()
                        navController.navigate(Screen.Balance.route) {
                            popUpTo(Screen.Checkout.route) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.WarmOrange
                    )
                ) {
                    Text("Xem số dư & lịch sử")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        cartViewModel.clearCart()
                        orderViewModel.resetCreateOrderState()
                        navController.popBackStack()
                    }
                ) {
                    Text("Về trang chủ", color = AppColors.PrimaryGray)
                }
            }
        )
    }

    // Error dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
            },
            title = {
                Text("Thanh Toán Thất Bại", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = errorMessage,
                    fontSize = 14.sp,
                    color = AppColors.PrimaryDark
                )
            },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.WarmOrange
                    )
                ) {
                    Text("Thử lại")
                }
            },
            dismissButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("Đóng", color = AppColors.PrimaryGray)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Giỏ Hàng",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.WarmOrange
                )
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                CheckoutBottomBar(
                    subtotal = subtotal,
                    totalSaved = totalSaved,
                    voucherDiscount = voucherDiscount,
                    finalTotal = finalTotal,
                    isProcessing = isProcessing,
                    onPaymentClick = handlePaymentClick
                )
            }
        }
    ) { paddingValues ->

        if (cartItems.isEmpty() && !showSuccessDialog) {
            // Empty cart state
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                AppColors.SurfaceLight,
                                Color.White
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AppColors.WarmOrangeSoft,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = AppColors.WarmOrange,
                            modifier = Modifier
                                .padding(20.dp)
                                .size(40.dp)
                        )
                    }

                    Text(
                        text = "Giỏ hàng trống",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )

                    Text(
                        text = "Thêm game vào giỏ hàng để tiếp tục",
                        fontSize = 14.sp,
                        color = AppColors.PrimaryGray,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.WarmOrange,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Khám Phá Game")
                    }
                }
            }
        } else if (!showSuccessDialog) {
            // Cart with items
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                AppColors.SurfaceLight,
                                Color.White
                            )
                        )
                    )
            ) {

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Cart items
                    items(cartItems) { item ->
                        CartItemCard(
                            item = item,
                            onQuantityChange = { newQuantity ->
                                cartViewModel.updateQuantity(item.gameId, newQuantity)
                            },
                            onRemove = { cartViewModel.removeItem(item.gameId) }
                        )
                    }

                    // Voucher section
                    item {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Voucher & Khuyến Mãi",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.PrimaryDark,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        CheckoutVoucherSection(
                            selectedUserVoucher = selectedUserVoucher,
                            subtotal = subtotal,
                            voucherDiscount = voucherDiscount,
                            onVoucherClick = { navController.navigate(Screen.VoucherWallet.route) },
                            onRemoveVoucher = { cartViewModel.selectUserVoucher(null) }
                        )
                    }

                    // Payment method info
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = AppColors.WarmOrangeSoft
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Payment,
                                    contentDescription = null,
                                    tint = AppColors.WarmOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Thanh toán bằng số dư ví",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.PrimaryDark
                                    )
                                    Text(
                                        text = "Đảm bảo số dư đủ trước khi thanh toán",
                                        fontSize = 11.sp,
                                        color = AppColors.PrimaryGray
                                    )
                                }
                            }
                        }
                    }

                    // Extra space for bottom bar
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}

// Helper to format amount string
private fun formatAmount(amount: String): String {
    return try {
        val value = amount.toDoubleOrNull() ?: 0.0
        "%,.0f".format(value)
    } catch (e: Exception) {
        amount
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // Header: Game name and remove button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.gameName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Price and quantity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Price section
                Column {
                    if (item.discount > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.pricePerTurn} đ",
                                fontSize = 12.sp,
                                color = AppColors.PrimaryGray,
                                textDecoration = TextDecoration.LineThrough
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "-${item.discount}%",
                                    fontSize = 10.sp,
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${item.discountedPrice} đ/lượt",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.WarmOrange
                    )
                }

                // Quantity controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onQuantityChange(item.quantity - 1) },
                        enabled = item.quantity > 1,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (item.quantity > 1) AppColors.WarmOrange else AppColors.PrimaryGray.copy(alpha = 0.3f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Giảm",
                                tint = Color.White,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AppColors.SurfaceLight,
                        modifier = Modifier.width(40.dp)
                    ) {
                        Text(
                            text = item.quantity.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryDark,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    IconButton(
                        onClick = { onQuantityChange(item.quantity + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AppColors.WarmOrange,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Tăng",
                                tint = Color.White,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total for this item
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (item.savedAmount > 0) {
                    Text(
                        text = "Tiết kiệm: ${item.savedAmount} đ",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Text(
                    text = "Tổng: ${item.totalPrice} đ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
            }
        }
    }
}

@Composable
fun VoucherCard(
    selectedVoucher: String?,
    onVoucherClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onVoucherClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = AppColors.WarmOrangeSoft,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = AppColors.WarmOrange,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Column {
                    Text(
                        text = selectedVoucher ?: "Chọn voucher",
                        fontSize = 14.sp,
                        fontWeight = if (selectedVoucher != null) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedVoucher != null) AppColors.PrimaryDark else AppColors.PrimaryGray
                    )
                    Text(
                        text = if (selectedVoucher != null) "Áp dụng thành công" else "Nhấn để chọn voucher",
                        fontSize = 12.sp,
                        color = if (selectedVoucher != null) Color(0xFF4CAF50) else AppColors.PrimaryGray
                    )
                }
            }

            Text(
                text = ">",
                fontSize = 16.sp,
                color = AppColors.PrimaryGray
            )
        }
    }
}

@Composable
fun CheckoutBottomBar(
    subtotal: Int,
    totalSaved: Int,
    voucherDiscount: Int,
    finalTotal: Int,
    isProcessing: Boolean = false,
    onPaymentClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Price breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tạm tính:",
                    fontSize = 14.sp,
                    color = AppColors.PrimaryGray
                )
                Text(
                    text = "${subtotal} đ",
                    fontSize = 14.sp,
                    color = AppColors.PrimaryDark
                )
            }

            if (totalSaved > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tiết kiệm:",
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "-${totalSaved} đ",
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (voucherDiscount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Voucher:",
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "-${voucherDiscount} đ",
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Divider(color = AppColors.PrimaryGray.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tổng cộng:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
                Text(
                    text = "${finalTotal} đ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.WarmOrange
                )
            }

            Button(
                onClick = onPaymentClick,
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.WarmOrange,
                    contentColor = Color.White,
                    disabledContainerColor = AppColors.WarmOrange.copy(alpha = 0.6f),
                    disabledContentColor = Color.White
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đang xử lý...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Thanh Toán",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun calculateVoucherDiscount(voucher: VoucherDTO?, subtotal: Int): Int {
    if (voucher == null) return 0
    val minOrder = voucher.minOrderValue?.toBigDecimalOrNull()?.toInt() ?: 0
    if (subtotal < minOrder) return 0
    return when (voucher.discountType.uppercase()) {
        "PERCENT", "PERCENTAGE" -> {
            val pct = voucher.discountValue.toDoubleOrNull() ?: 0.0
            val discount = (subtotal * pct / 100).toInt()
            val maxDiscount = voucher.maxDiscount?.toDoubleOrNull()?.toInt() ?: Int.MAX_VALUE
            minOf(discount, maxDiscount)
        }
        "FIXED_AMOUNT", "AMOUNT" -> (voucher.discountValue.toDoubleOrNull() ?: 0.0).toInt()
        else -> 0
    }
}

@Composable
fun CheckoutVoucherSection(
    selectedUserVoucher: UserVoucherDTO?,
    subtotal: Int,
    voucherDiscount: Int,
    onVoucherClick: () -> Unit,
    onRemoveVoucher: () -> Unit
) {
    val voucher = selectedUserVoucher?.voucher
    val minOrder = voucher?.minOrderValue?.toBigDecimalOrNull()?.toInt() ?: 0
    val isNotApplicable = voucher != null && subtotal < minOrder

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = if (selectedUserVoucher == null) onVoucherClick else ({}),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isNotApplicable -> Color(0xFFFFF3E0)
                voucher != null -> Color(0xFFF1F8E9)
                else -> Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (voucher != null && !isNotApplicable) Color(0xFF4CAF50).copy(alpha = 0.15f) else AppColors.WarmOrangeSoft,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = if (voucher != null && !isNotApplicable) Color(0xFF4CAF50) else AppColors.WarmOrange,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Column {
                        if (voucher != null) {
                            Text(
                                text = voucher.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.PrimaryDark
                            )
                            Text(
                                text = when {
                                    isNotApplicable -> "Chưa đủ điều kiện (tối thiểu ${minOrder / 1000}K)"
                                    voucherDiscount > 0 -> "Giảm ${"%,d".format(voucherDiscount)} đ"
                                    else -> "Áp dụng thành công"
                                },
                                fontSize = 12.sp,
                                color = when {
                                    isNotApplicable -> Color(0xFFF44336)
                                    else -> Color(0xFF4CAF50)
                                }
                            )
                        } else {
                            Text(
                                text = "Chọn voucher",
                                fontSize = 14.sp,
                                color = AppColors.PrimaryGray
                            )
                            Text(
                                text = "Nhấn để chọn từ ví voucher",
                                fontSize = 12.sp,
                                color = AppColors.PrimaryGray
                            )
                        }
                    }
                }

                if (voucher != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = onVoucherClick,
                            colors = ButtonDefaults.textButtonColors(contentColor = AppColors.WarmOrange)
                        ) {
                            Text("Đổi", fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = onRemoveVoucher,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF44336))
                        ) {
                            Text("Xóa", fontSize = 12.sp)
                        }
                    }
                } else {
                    Text(text = ">", fontSize = 16.sp, color = AppColors.PrimaryGray)
                }
            }

            if (isNotApplicable) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF3E0)
                ) {
                    Text(
                        text = "Đơn hàng cần tối thiểu ${"%,d".format(minOrder)} đ để áp dụng voucher này",
                        fontSize = 11.sp,
                        color = Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CheckoutScreenPreview() {
    CheckoutScreen(navController = NavController(LocalContext.current))
}
