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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.data.model.OrderDTO
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.data.model.TransactionDTO
import java.text.NumberFormat
import java.util.Locale
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.WalletViewModel

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
    TOP_UP,      // Nạp tiền
    GAME_PLAY,   // Chơi game
    REFUND,      // Hoàn tiền
    BONUS        // Tiền thưởng
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
    
    // Load data when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadBalance()
        viewModel.loadTransactions(page = 1, size = 10)
    }
    
    // Update UI when balance loads
    LaunchedEffect(balanceState) {
        when (val state = balanceState) {
            is Resource.Success -> {
                // Parse as double first to handle "900000.00" format
                currentBalance = state.data.currentBalance.toDoubleOrNull()?.toInt() ?: 0
                // Use loyaltyPoints from API
                currentPoints = state.data.loyaltyPoints
                // Determine membership tier based on balance
                membershipTier = when {
                    currentBalance >= 1000000 -> "Bạch Kim"
                    currentBalance >= 500000 -> "Vàng"
                    currentBalance >= 200000 -> "Bạc"
                    else -> "Đồng"
                }
            }
            else -> {}
        }
    }
    
    // Update UI when transactions load
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
            TopAppBar(
                title = {
                    Text(
                        "Số Dư & Lịch Sử",
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
        }
    ) { paddingValues ->

        // Show loading when both states are loading
        val isLoading = balanceState is Resource.Loading || transactionsState is Resource.Loading
        val hasError = balanceState is Resource.Error || transactionsState is Resource.Error

        when {
            isLoading && currentBalance == 0 -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.WarmOrange)
                }
            }
            
            hasError && currentBalance == 0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Không thể tải dữ liệu",
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { 
                            viewModel.loadBalance()
                            viewModel.loadTransactions(page = 1, size = 10)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.WarmOrange
                        )
                    ) {
                        Text("Thử lại")
                    }
                }
            }
            
            else -> {
                LazyColumn(
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
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Balance Card
                    item {
                        BalanceCard(
                            balance = currentBalance,
                            showBalance = showBalance,
                            onToggleVisibility = { showBalance = !showBalance },
                            onTopUpClick = onTopUpClick
                        )
                    }

                    // Points & Membership Card
                    item {
                        PointsCard(
                            points = currentPoints,
                            membershipTier = membershipTier
                        )
                    }

                    // Quick Actions
                    item {
                        Text(
                            text = "Lịch Sử",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryDark,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    item {
                        QuickActionsRow(
                            onPaymentHistoryClick = onPaymentHistoryClick,
                            onUsageHistoryClick = onUsageHistoryClick
                        )
                    }

                    // Recent Transactions Section
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Giao Dịch Gần Đây",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark
                            )

                            OutlinedButton(
                                onClick = onPaymentHistoryClick,
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    "Xem tất cả",
                                    fontSize = 12.sp,
                                    color = AppColors.WarmOrange
                                )
                            }
                        }
                    }

                    // Recent Transactions List
                    if (transactions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Chưa có giao dịch nào",
                                    color = AppColors.PrimaryGray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        items(transactions.take(5)) { transaction ->
                            TransactionCard(
                                transaction = transaction,
                                onClick = { selectedTransaction = transaction }
                            )
                        }
                    }

                    // Extra space for bottom navigation
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Transaction detail bottom sheet
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            AppColors.CardGrad1,
                            AppColors.CardGrad2
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Số dư hiện tại",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (showBalance) "%,d VND".format(balance) else "••••••• VND",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            IconButton(
                                onClick = onToggleVisibility,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    if (showBalance) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showBalance) "Ẩn số dư" else "Hiện số dư",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.Wallet,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                // Top Up Button
                Button(
                    onClick = onTopUpClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.9f),
                        contentColor = AppColors.CardPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Nạp Tiền",
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
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
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = AppColors.WarmOrange,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Column {
                    Text(
                        text = "Điểm tích lũy",
                        fontSize = 12.sp,
                        color = AppColors.PrimaryGray
                    )
                    Text(
                        text = "%,d điểm".format(points),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFD700).copy(alpha = 0.2f)
            ) {
                Text(
                    text = "Hạng $membershipTier",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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
            icon = Icons.Default.TrendingUp,
            title = "Lịch sử nạp tiền",
            subtitle = "Xem các lần nạp",
            iconColor = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f),
            onClick = onPaymentHistoryClick
        )

        QuickActionCard(
            icon = Icons.Default.Receipt,
            title = "Lịch sử sử dụng",
            subtitle = "Chi tiêu & game",
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
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
                    color = iconColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.PrimaryDark
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = AppColors.PrimaryGray
                    )
                }
            }

            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = AppColors.PrimaryGray.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun TransactionCard(transaction: BalanceTransaction, onClick: () -> Unit = {}) {
    val (icon, iconColor, backgroundColor) = getTransactionStyle(transaction.type)

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Transaction icon
            Surface(
                shape = CircleShape,
                color = backgroundColor,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Transaction details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = transaction.description,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.PrimaryDark
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = transaction.timestamp,
                        fontSize = 11.sp,
                        color = AppColors.PrimaryGray
                    )

                    if (transaction.gameType.isNotEmpty()) {
                        Text(
                            text = "• ${transaction.gameType}",
                            fontSize = 11.sp,
                            color = AppColors.PrimaryGray
                        )
                    }
                }
            }

            // Amount
            Text(
                text = "${if (transaction.amount > 0) "+" else ""}${transaction.amount} đ",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.amount > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}

@Composable
fun getTransactionStyle(type: TransactionType): Triple<ImageVector, Color, Color> {
    return when (type) {
        TransactionType.TOP_UP -> Triple(
            Icons.Default.TrendingUp,
            Color(0xFF4CAF50),
            Color(0xFF4CAF50).copy(alpha = 0.2f)
        )
        TransactionType.GAME_PLAY -> Triple(
            Icons.Default.TrendingDown,
            AppColors.WarmOrange,
            AppColors.WarmOrangeSoft
        )
        TransactionType.REFUND -> Triple(
            Icons.Default.MonetizationOn,
            Color(0xFF2196F3),
            Color(0xFF2196F3).copy(alpha = 0.2f)
        )
        TransactionType.BONUS -> Triple(
            Icons.Default.Star,
            Color(0xFFFFD700),
            Color(0xFFFFD700).copy(alpha = 0.2f)
        )
    }
}

// Helper function to map TransactionDTO to BalanceTransaction
fun mapTransactionDTO(dto: TransactionDTO): BalanceTransaction {
    val type = when (dto.type.uppercase()) {
        "TOP_UP", "TOPUP", "DEPOSIT" -> TransactionType.TOP_UP
        "GAME_PLAY", "GAMEPLAY", "PURCHASE", "PAYMENT" -> TransactionType.GAME_PLAY
        "REFUND" -> TransactionType.REFUND
        "BONUS", "REWARD" -> TransactionType.BONUS
        else -> TransactionType.GAME_PLAY
    }
    
    // API already returns negative values for expenses (e.g. "-60000.00")
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

// Helper function to format timestamp
fun formatTimestamp(timestamp: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        val clean = timestamp.substringBefore('Z').substringBefore('+')
        val date = inputFormat.parse(clean) ?: return timestamp
        outputFormat.format(date)
    } catch (e: Exception) {
        timestamp
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(
    transaction: BalanceTransaction,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val orderRepository = (context.applicationContext as App).orderRepository
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val (icon, iconColor, backgroundColor) = getTransactionStyle(transaction.type)

    // Fetch order detail if this is an ORDER transaction
    var orderDetail by remember { mutableStateOf<OrderDTO?>(null) }
    var orderLoading by remember { mutableStateOf(false) }

    val isOrderTransaction = transaction.referenceType.uppercase() == "ORDER" && transaction.referenceId.isNotEmpty()

    LaunchedEffect(transaction.referenceId) {
        if (isOrderTransaction) {
            orderLoading = true
            val result = orderRepository.getOrderDetail(transaction.referenceId)
            if (result is Resource.Success) {
                orderDetail = result.data
            }
            orderLoading = false
        }
    }

    val typeLabel = when (transaction.type) {
        TransactionType.TOP_UP -> "Nạp tiền"
        TransactionType.GAME_PLAY -> "Thanh toán"
        TransactionType.REFUND -> "Hoàn tiền"
        TransactionType.BONUS -> "Thưởng"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: icon + amount
            Surface(shape = CircleShape, color = backgroundColor, modifier = Modifier.size(64.dp)) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.padding(16.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${if (transaction.amount >= 0) "+" else ""}${formatter.format(transaction.amount)}đ",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.amount >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = transaction.description, fontSize = 14.sp, color = AppColors.PrimaryGray)
            Spacer(modifier = Modifier.height(24.dp))

            if (isOrderTransaction) {
                // Order detail section
                if (orderLoading) {
                    CircularProgressIndicator(color = AppColors.WarmOrange, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    orderDetail?.let { order ->
                        // Items purchased
                        val items = order.items.orEmpty()
                        if (items.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceLight),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    Text(
                                        text = "Sản phẩm",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.PrimaryDark
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    items.forEach { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.gameName ?: "Game #${item.gameId.take(6)}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = AppColors.PrimaryDark
                                                )
                                                Text(
                                                    text = "x${item.quantity} × ${formatter.format(item.unitPrice.toDoubleOrNull()?.toInt() ?: 0)}đ",
                                                    fontSize = 12.sp,
                                                    color = AppColors.PrimaryGray
                                                )
                                            }
                                            Text(
                                                text = "${formatter.format(item.lineTotal.toDoubleOrNull()?.toInt() ?: 0)}đ",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AppColors.PrimaryDark
                                            )
                                        }
                                        if (item != items.last()) {
                                            Divider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Price breakdown
                        val subtotal = order.subtotal.toDoubleOrNull()?.toInt() ?: 0
                        val discount = order.discountAmount.toDoubleOrNull()?.toInt() ?: 0
                        val total = order.totalAmount.toDoubleOrNull()?.toInt() ?: 0

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceLight),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                DetailRow("Tạm tính", "${formatter.format(subtotal)}đ")
                                if (discount > 0) {
                                    Divider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Giảm giá voucher", fontSize = 13.sp, color = Color(0xFF4CAF50))
                                        Text(
                                            "-${formatter.format(discount)}đ",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF4CAF50)
                                        )
                                    }
                                }
                                Divider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tổng thanh toán", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.PrimaryDark)
                                    Text(
                                        "${formatter.format(total)}đ",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.WarmOrange
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // Basic transaction info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceLight),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    DetailRow("Loại giao dịch", typeLabel)
                    Divider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
                    DetailRow("Thời gian", transaction.timestamp)
                    if (transaction.balanceBefore != 0 || transaction.balanceAfter != 0) {
                        Divider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
                        DetailRow("Số dư trước", "${formatter.format(transaction.balanceBefore)}đ")
                        Divider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
                        DetailRow("Số dư sau", "${formatter.format(transaction.balanceAfter)}đ")
                    }
                    Divider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
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
        Text(text = label, fontSize = 13.sp, color = AppColors.PrimaryGray)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.PrimaryDark)
    }
}

@Preview(showBackground = true)
@Composable
fun BalanceScreenPreview() {
    BalanceScreen()
}


