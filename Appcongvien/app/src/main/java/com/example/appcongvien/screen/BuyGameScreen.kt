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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavController
import com.example.appcongvien.navigation.Screen
import com.example.appcongvien.ui.theme.AppColors

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
    
    // Collect cart state
    val cartItems by cartViewModel.cartItems.collectAsState()
    val selectedVoucher by cartViewModel.selectedVoucher.collectAsState()

    // Calculate totals
    val subtotal = cartItems.sumOf { it.totalPrice }
    val totalSaved = cartItems.sumOf { it.savedAmount }
    val voucherDiscount = 0 // TODO: Calculate based on selected voucher
    val finalTotal = subtotal - voucherDiscount

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
                    onPaymentClick = onPaymentClick
                )
            }
        }
    ) { paddingValues ->

        if (cartItems.isEmpty()) {
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
        } else {
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

                        VoucherCard(
                            selectedVoucher = selectedVoucher,
                            onVoucherClick = { navController.navigate(Screen.VoucherWallet.route) }
                        )
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.WarmOrange,
                    contentColor = Color.White
                )
            ) {
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

@Preview(showBackground = true)
@Composable
fun CheckoutScreenPreview() {
    CheckoutScreen(navController = NavController(LocalContext.current))
}


