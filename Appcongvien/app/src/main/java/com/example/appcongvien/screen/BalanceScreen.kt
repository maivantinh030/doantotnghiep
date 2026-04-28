package com.example.appcongvien.screen

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.components.ParkTopAppBar
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.data.model.TransactionDTO
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.WalletViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

data class BalanceTransaction(
    val id: String,
    val type: TransactionType,
    val amount: Int,
    val description: String,
    val timestamp: String,
    val gameType: String = "",
    val balanceBefore: Int = 0,
    val balanceAfter: Int = 0,
    val referenceId: String = "",
    val referenceType: String = ""
)

enum class TransactionType {
    TOP_UP,
    GAME_PLAY,
    REFUND,
    BONUS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceScreen(
    modifier: Modifier = Modifier,
    onTopUpClick: () -> Unit = {},
    onPaymentHistoryClick: () -> Unit = {},
    onUsageHistoryClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val walletRepository = (context.applicationContext as App).walletRepository
    val viewModel: WalletViewModel = viewModel(
        factory = WalletViewModel.Factory(walletRepository)
    )

    val balanceState by viewModel.balanceState.collectAsState()
    val transactionsState by viewModel.transactionsState.collectAsState()

    var showBalance by remember { mutableStateOf(true) }
    var currentBalance by remember { mutableStateOf(0) }
    var currentPoints by remember { mutableStateOf(0) }
    var membershipTier by remember { mutableStateOf("Đồng") }
    var transactions by remember { mutableStateOf<List<BalanceTransaction>>(emptyList()) }
    var selectedTransaction by remember { mutableStateOf<BalanceTransaction?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadBalance()
        viewModel.loadTransactions(page = 1, size = 10)
    }

    LaunchedEffect(balanceState) {
        when (val state = balanceState) {
            is Resource.Success -> {
                currentBalance = state.data.currentBalance.toDoubleOrNull()?.toInt() ?: 0
                membershipTier = when {
                    currentBalance >= 1_000_000 -> "Bạch Kim"
                    currentBalance >= 500_000 -> "Vàng"
                    currentBalance >= 200_000 -> "Bạc"
                    else -> "Đồng"
                }
            }

            else -> {}
        }
    }

    LaunchedEffect(transactionsState) {
        when (val state = transactionsState) {
            is Resource.Success -> {
                transactions = state.data.items.map { mapTransactionDTO(it) }
            }

            else -> {}
        }
    }

    Scaffold(
        topBar = {
            ParkTopAppBar(
                title = "Số Dư & Lịch Sử",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        val isLoading = balanceState is Resource.Loading || transactionsState is Resource.Loading
        val hasError = balanceState is Resource.Error || transactionsState is Resource.Error
        val backgroundBrush = Brush.verticalGradient(
            listOf(
                Color(0xFFFFFAF4),
                AppColors.SurfaceLight,
                AppColors.SurfaceWhite
            )
        )

        when {
            isLoading && currentBalance == 0 -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(backgroundBrush),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.WarmOrange)
                }
            }

            hasError && currentBalance == 0 -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(backgroundBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Không thể tải dữ liệu ví",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Thử tải lại để cập nhật số dư và lịch sử giao dịch mới nhất.",
                                fontSize = 14.sp,
                                color = AppColors.PrimaryGray,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            Button(
                                onClick = {
                                    viewModel.loadBalance()
                                    viewModel.loadTransactions(page = 1, size = 10)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.WarmOrange,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text("Thử lại", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(backgroundBrush),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        BalanceCard(
                            balance = currentBalance,
                            showBalance = showBalance,
                            onToggleVisibility = { showBalance = !showBalance },
                            onTopUpClick = onTopUpClick
                        )
                    }

                    item {
                        PointsCard(
                            points = currentPoints,
                            membershipTier = membershipTier
                        )
                    }

                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Lịch sử giao dịch",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark
                            )
                            Text(
                                text = "Theo dõi nhanh nạp tiền, chi tiêu và các biến động trong ví.",
                                fontSize = 13.sp,
                                color = AppColors.PrimaryGray.copy(alpha = 0.82f),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    item {
                        QuickActionsRow(
                            onPaymentHistoryClick = onPaymentHistoryClick,
                            onUsageHistoryClick = onUsageHistoryClick
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Giao dịch gần đây",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.PrimaryDark
                                )
                                Text(
                                    text = "Hiển thị 5 giao dịch mới nhất trong tài khoản.",
                                    fontSize = 12.sp,
                                    color = AppColors.PrimaryGray.copy(alpha = 0.8f)
                                )
                            }

                            OutlinedButton(
                                onClick = onPaymentHistoryClick,
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, AppColors.BorderSubtle),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = AppColors.WarmOrange
                                )
                            ) {
                                Text(
                                    text = "Xem tất cả",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (transactions.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.7f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = AppColors.WarmOrangeSoft.copy(alpha = 0.55f),
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Receipt,
                                            contentDescription = null,
                                            tint = AppColors.WarmOrange,
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }
                                    Text(
                                        text = "Chưa có giao dịch nào",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.PrimaryDark
                                    )
                                    Text(
                                        text = "Các giao dịch nạp tiền và sử dụng game sẽ hiển thị tại đây.",
                                        fontSize = 13.sp,
                                        color = AppColors.PrimaryGray,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(transactions.take(5), key = { it.id }) { transaction ->
                            TransactionCard(
                                transaction = transaction,
                                onClick = { selectedTransaction = transaction }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    selectedTransaction?.let { tx ->
        TransactionDetailSheet(
            transaction = tx,
            onDismiss = { selectedTransaction = null }
        )
    }
}

@Composable
fun BalanceCard(
    balance: Int,
    showBalance: Boolean,
    onToggleVisibility: () -> Unit,
    onTopUpClick: () -> Unit
) {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            AppColors.CardGrad1,
                            Color(0xFF524B45)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Số dư hiện tại",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.74f),
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (showBalance) {
                                    "${formatter.format(balance)} VND"
                                } else {
                                    "••••••• VND"
                                },
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            IconButton(
                                onClick = onToggleVisibility,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (showBalance) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
                                    contentDescription = if (showBalance) "Ẩn số dư" else "Hiện số dư",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "Sẵn sàng cho thanh toán và nạp thêm trong công viên.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.68f),
                            lineHeight = 17.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.14f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wallet,
                            contentDescription = null,
                            tint = AppColors.WarmOrangeSoft,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                Button(
                    onClick = onTopUpClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = AppColors.CardPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nạp tiền ngay",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PointsCard(
    points: Int,
    membershipTier: String
) {
    val (tierBg, tierColor) = when (membershipTier) {
        "Bạch Kim" -> Color(0xFFE8EEF5) to Color(0xFF5F6F86)
        "Vàng" -> Color(0xFFFFF1D6) to Color(0xFFBF7A00)
        "Bạc" -> Color(0xFFF1F2F4) to Color(0xFF70757D)
        else -> Color(0xFFF8E7DA) to Color(0xFFB87333)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.75f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AppColors.WarmOrangeSoft.copy(alpha = 0.75f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AppColors.WarmOrange,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Điểm tích lũy",
                        fontSize = 12.sp,
                        color = AppColors.PrimaryGray.copy(alpha = 0.82f)
                    )
                    Text(
                        text = "%,d điểm".format(points),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = tierBg
            ) {
                Text(
                    text = "Hạng $membershipTier",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = tierColor,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    onPaymentHistoryClick: () -> Unit,
    onUsageHistoryClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            title = "Lịch sử nạp tiền",
            subtitle = "Xem các lần nạp",
            iconColor = Color(0xFF3BA55D),
            modifier = Modifier.weight(1f),
            onClick = onPaymentHistoryClick
        )

        QuickActionCard(
            icon = Icons.Default.Receipt,
            title = "Lịch sử sử dụng",
            subtitle = "Chi tiêu và game",
            iconColor = AppColors.WarmOrange,
            modifier = Modifier.weight(1f),
            onClick = onUsageHistoryClick
        )
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = iconColor.copy(alpha = 0.14f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.padding(11.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = AppColors.PrimaryGray.copy(alpha = 0.82f)
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = AppColors.PrimaryGray.copy(alpha = 0.56f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun TransactionCard(transaction: BalanceTransaction, onClick: () -> Unit = {}) {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
    val (icon, iconColor, backgroundColor) = getTransactionStyle(transaction.type)
    val typeLabel = when (transaction.type) {
        TransactionType.TOP_UP -> "Nạp tiền"
        TransactionType.GAME_PLAY -> "Sử dụng"
        TransactionType.REFUND -> "Hoàn tiền"
        TransactionType.BONUS -> "Thưởng"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = backgroundColor,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = transaction.description,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark,
                    lineHeight = 18.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = iconColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = iconColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = transaction.timestamp,
                        fontSize = 11.sp,
                        color = AppColors.PrimaryGray.copy(alpha = 0.82f)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${if (transaction.amount > 0) "+" else "-"}${formatter.format(abs(transaction.amount))}đ",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.amount > 0) Color(0xFF3BA55D) else Color(0xFFE45A4F)
                )
                if (transaction.referenceId.isNotEmpty()) {
                    Text(
                        text = transaction.referenceId.take(6).uppercase(),
                        fontSize = 10.sp,
                        color = AppColors.PrimaryGray.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

fun getTransactionStyle(type: TransactionType): Triple<ImageVector, Color, Color> {
    return when (type) {
        TransactionType.TOP_UP -> Triple(
            Icons.AutoMirrored.Filled.TrendingUp,
            Color(0xFF3BA55D),
            Color(0xFF3BA55D).copy(alpha = 0.14f)
        )

        TransactionType.GAME_PLAY -> Triple(
            Icons.AutoMirrored.Filled.TrendingDown,
            AppColors.WarmOrange,
            AppColors.WarmOrangeSoft.copy(alpha = 0.65f)
        )

        TransactionType.REFUND -> Triple(
            Icons.Default.MonetizationOn,
            Color(0xFF2F80ED),
            Color(0xFF2F80ED).copy(alpha = 0.14f)
        )

        TransactionType.BONUS -> Triple(
            Icons.Default.Star,
            Color(0xFFB8860B),
            Color(0xFFFFF1CC)
        )
    }
}

fun mapTransactionDTO(dto: TransactionDTO): BalanceTransaction {
    val type = when (dto.type.uppercase()) {
        "TOP_UP", "TOPUP", "DEPOSIT" -> TransactionType.TOP_UP
        "GAME_PLAY", "GAMEPLAY", "PURCHASE", "PAYMENT" -> TransactionType.GAME_PLAY
        "REFUND" -> TransactionType.REFUND
        "BONUS", "REWARD" -> TransactionType.BONUS
        else -> TransactionType.GAME_PLAY
    }

    val adjustedAmount = dto.amount.toDoubleOrNull()?.toInt() ?: 0

    return BalanceTransaction(
        id = dto.transactionId,
        type = type,
        amount = adjustedAmount,
        description = dto.description ?: "Giao dịch",
        timestamp = formatTimestamp(dto.createdAt),
        gameType = "",
        balanceBefore = dto.balanceBefore?.toDoubleOrNull()?.toInt() ?: 0,
        balanceAfter = dto.balanceAfter?.toDoubleOrNull()?.toInt() ?: 0,
        referenceId = dto.referenceId ?: "",
        referenceType = dto.referenceType ?: ""
    )
}

fun formatTimestamp(timestamp: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val clean = timestamp.substringBefore('Z').substringBefore('+')
        val date = inputFormat.parse(clean) ?: return timestamp
        outputFormat.format(date)
    } catch (_: Exception) {
        timestamp
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(
    transaction: BalanceTransaction,
    onDismiss: () -> Unit
) {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val (icon, iconColor, backgroundColor) = getTransactionStyle(transaction.type)
    val typeLabel = when (transaction.type) {
        TransactionType.TOP_UP -> "Nạp tiền"
        TransactionType.GAME_PLAY -> "Thanh toán"
        TransactionType.REFUND -> "Hoàn tiền"
        TransactionType.BONUS -> "Thưởng"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = AppColors.SurfaceWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = backgroundColor,
                modifier = Modifier.size(68.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.padding(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${if (transaction.amount >= 0) "+" else "-"}${formatter.format(abs(transaction.amount))}đ",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.amount >= 0) Color(0xFF3BA55D) else Color(0xFFE45A4F)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = transaction.description,
                fontSize = 14.sp,
                color = AppColors.PrimaryGray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    DetailRow("Loại giao dịch", typeLabel)
                    HorizontalDivider(color = AppColors.BorderSubtle.copy(alpha = 0.75f), thickness = 0.8.dp)
                    DetailRow("Thời gian", transaction.timestamp)
                    if (transaction.balanceBefore != 0 || transaction.balanceAfter != 0) {
                        HorizontalDivider(color = AppColors.BorderSubtle.copy(alpha = 0.75f), thickness = 0.8.dp)
                        DetailRow("Số dư trước", "${formatter.format(transaction.balanceBefore)}đ")
                        HorizontalDivider(color = AppColors.BorderSubtle.copy(alpha = 0.75f), thickness = 0.8.dp)
                        DetailRow("Số dư sau", "${formatter.format(transaction.balanceAfter)}đ")
                    }
                    HorizontalDivider(color = AppColors.BorderSubtle.copy(alpha = 0.75f), thickness = 0.8.dp)
                    DetailRow("Mã giao dịch", transaction.id.take(8).uppercase())
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = AppColors.PrimaryGray
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.PrimaryDark
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BalanceScreenPreview() {
    BalanceScreen()
}
