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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.components.ParkTopAppBar
import com.example.appcongvien.data.model.CardRequestDTO
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.data.repository.CardRequestRepository
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.CardRequestViewModel
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardRequestScreen(
    repository: CardRequestRepository,
    onBackClick: () -> Unit = {},
    onNavigateTopUp: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as App
    val viewModel: CardRequestViewModel = viewModel(
        factory = CardRequestViewModel.Factory(repository, app.cardRepository, app.walletRepository)
    )
    val requestsState by viewModel.requestsState.collectAsState()
    val cardsState by viewModel.cardsState.collectAsState()
    val balanceState by viewModel.balanceState.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val blockCardState by viewModel.blockCardState.collectAsState()
    val cancelRequestState by viewModel.cancelRequestState.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showExistingCardDialog by remember { mutableStateOf(false) }
    var showInsufficientBalanceDialog by remember { mutableStateOf(false) }
    var continueCreateAfterBlock by remember { mutableStateOf(false) }
    var lastSubmitDepositOnline by remember { mutableStateOf(false) }
    var cancelTargetRequestId by remember { mutableStateOf<String?>(null) }

    val isLoading = requestsState is Resource.Loading
    val requests = (requestsState as? Resource.Success)?.data ?: emptyList()
    val isCheckingCards = cardsState is Resource.Loading
    val activeCard = (cardsState as? Resource.Success)?.data?.firstOrNull { it.status == "ACTIVE" }
    val currentBalance = (balanceState as? Resource.Success)?.data?.currentBalance?.toBigDecimalOrNull()
    val isSending = submitState is Resource.Loading
    val isBlockingCard = blockCardState is Resource.Loading
    val isCancelling = cancelRequestState is Resource.Loading

    val successMessage: String? = when {
        submitState is Resource.Success -> if (lastSubmitDepositOnline) {
            "Yêu cầu đã được gửi và đã cọc 50.000đ. Vui lòng đến quầy để nhận thẻ."
        } else {
            "Yêu cầu đã được gửi. Vui lòng đến quầy để nhận thẻ và nộp cọc."
        }
        blockCardState is Resource.Success -> "Thẻ cũ đã được khóa. Bạn có thể tạo thẻ mới."
        cancelRequestState is Resource.Success -> {
            val req = (cancelRequestState as Resource.Success).data
            if (req.depositPaidOnline) {
                "Đã hủy yêu cầu và hoàn tiền cọc về số dư."
            } else {
                "Đã hủy yêu cầu cấp thẻ."
            }
        }
        else -> null
    }
    val errorMessage: String? = (submitState as? Resource.Error)?.message
        ?: (blockCardState as? Resource.Error)?.message
        ?: (cancelRequestState as? Resource.Error)?.message

    val depositAmount = CardRequestViewModel.DEPOSIT_AMOUNT
    val depositAmountBd = BigDecimal.valueOf(depositAmount)
    val currencyFormat = remember { NumberFormat.getNumberInstance(Locale("vi", "VN")) }
    val depositAmountText = currencyFormat.format(depositAmount) + "đ"
    val balanceText = currentBalance?.let { currencyFormat.format(it) + "đ" } ?: "—"

    LaunchedEffect(successMessage, errorMessage) {
        if (successMessage != null || errorMessage != null) {
            delay(3000)
            viewModel.resetSubmitState()
            viewModel.resetBlockCardState()
            viewModel.resetCancelRequestState()
        }
    }

    LaunchedEffect(blockCardState, activeCard) {
        if (continueCreateAfterBlock &&
            (blockCardState is Resource.Success || blockCardState is Resource.Error)
        ) {
            if (activeCard == null) {
                showConfirmDialog = true
            }
            continueCreateAfterBlock = false
        }
    }

    if (showExistingCardDialog) {
        AlertDialog(
            onDismissRequest = { if (!isBlockingCard) showExistingCardDialog = false },
            title = { Text("Bạn đang có sẵn thẻ", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Bạn đang có sẵn thẻ, không thể tạo thêm.",
                        fontSize = 14.sp,
                        color = AppColors.PrimaryDark
                    )
                    activeCard?.let { activeCard ->
                        Text(
                            "Thẻ hiện tại: ${activeCard.cardId}",
                            fontSize = 13.sp,
                            color = AppColors.PrimaryGray
                        )
                    }
                    Text(
                        "Nếu bạn đã làm mất thẻ, hãy khóa thẻ cũ để tiếp tục tạo thẻ mới.",
                        fontSize = 13.sp,
                        color = AppColors.PrimaryGray,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        continueCreateAfterBlock = true
                        showExistingCardDialog = false
                        viewModel.blockActiveCard()
                    },
                    enabled = !isBlockingCard,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.RedError),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isBlockingCard) {
                        CircularProgressIndicator(
                            color = AppColors.OnAccent,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Làm mất thẻ", fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExistingCardDialog = false },
                    enabled = !isBlockingCard
                ) {
                    Text("Đóng")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Xác nhận yêu cầu cấp thẻ", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Bạn có muốn đặt cọc luôn qua app không?",
                        fontSize = 14.sp,
                        color = AppColors.PrimaryDark,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Tiền cọc: $depositAmountText",
                        fontSize = 14.sp,
                        color = AppColors.PrimaryDark
                    )
                    Text(
                        "Số dư hiện tại: $balanceText",
                        fontSize = 13.sp,
                        color = AppColors.PrimaryGray
                    )
                    Text(
                        "Cọc online: trừ thẳng vào số dư.\nCọc tại quầy: nộp khi đến nhận thẻ.",
                        fontSize = 12.sp,
                        color = AppColors.PrimaryGray,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val balance = currentBalance
                        if (balance == null || balance < depositAmountBd) {
                            showConfirmDialog = false
                            showInsufficientBalanceDialog = true
                        } else {
                            showConfirmDialog = false
                            lastSubmitDepositOnline = true
                            viewModel.submitRequest(
                                note = null,
                                depositPaidOnline = true,
                                depositAmount = depositAmount
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cọc online", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        lastSubmitDepositOnline = false
                        viewModel.submitRequest(
                            note = null,
                            depositPaidOnline = false,
                            depositAmount = depositAmount
                        )
                    }
                ) {
                    Text("Cọc tại quầy")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showInsufficientBalanceDialog) {
        AlertDialog(
            onDismissRequest = { showInsufficientBalanceDialog = false },
            title = { Text("Số dư không đủ", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Bạn cần $depositAmountText để cọc thẻ qua app.",
                        fontSize = 14.sp,
                        color = AppColors.PrimaryDark
                    )
                    Text(
                        "Số dư hiện tại: $balanceText",
                        fontSize = 13.sp,
                        color = AppColors.PrimaryGray
                    )
                    Text(
                        "Bạn có muốn nạp thêm tiền không?",
                        fontSize = 13.sp,
                        color = AppColors.PrimaryGray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInsufficientBalanceDialog = false
                        onNavigateTopUp()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Nạp tiền", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInsufficientBalanceDialog = false }) {
                    Text("Để sau")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    cancelTargetRequestId?.let { targetId ->
        val targetRequest = requests.firstOrNull { it.requestId == targetId }
        AlertDialog(
            onDismissRequest = { if (!isCancelling) cancelTargetRequestId = null },
            title = { Text("Xác nhận hủy yêu cầu", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Bạn có chắc muốn hủy yêu cầu cấp thẻ này?",
                        fontSize = 14.sp,
                        color = AppColors.PrimaryDark
                    )
                    if (targetRequest?.depositPaidOnline == true) {
                        Text(
                            "Tiền cọc $depositAmountText sẽ được hoàn lại số dư.",
                            fontSize = 13.sp,
                            color = AppColors.GreenSuccess
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        cancelTargetRequestId = null
                        viewModel.cancelRequest(targetId)
                    },
                    enabled = !isCancelling,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.RedError),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(
                            color = AppColors.OnAccent,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Hủy yêu cầu", fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { cancelTargetRequestId = null },
                    enabled = !isCancelling
                ) {
                    Text("Quay lại")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = { ParkTopAppBar(title = "Yêu Cầu Cấp Thẻ", onBackClick = onBackClick) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppColors.SurfaceLight),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.BluePrimary.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = AppColors.BluePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                "Cách hoạt động",
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "1. Gửi yêu cầu qua app\n2. Nhân viên xem xét và duyệt\n3. Đến quầy để nhận thẻ vật lý\n4. Thẻ sẽ được liên kết với tài khoản của bạn",
                                fontSize = 13.sp,
                                color = AppColors.PrimaryGray,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // Card hiển thị thẻ đang hoạt động (tách riêng)
            activeCard?.let {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = AppColors.GreenSuccess.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = AppColors.GreenSuccess,
                                        modifier = Modifier
                                            .padding(6.dp)
                                            .size(20.dp)
                                    )
                                }
                                Text(
                                    "Thẻ đang hoạt động",
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.PrimaryDark,
                                    fontSize = 15.sp
                                )
                            }

                            Surface(
                                color = AppColors.SurfaceLight,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    "Thẻ đã được cấp",
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.GreenSuccess,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                )
                            }

                            Text(
                                "Không thể tạo thêm thẻ mới khi thẻ này vẫn hoạt động.",
                                fontSize = 12.sp,
                                color = AppColors.PrimaryGray,
                                lineHeight = 18.sp
                            )

                            Button(
                                onClick = { showExistingCardDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.RedError.copy(alpha = 0.1f),
                                    contentColor = AppColors.RedError
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 10.dp
                                )
                            ) {
                                Text(
                                    "Báo mất thẻ",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Card gửi yêu cầu mới
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Gửi yêu cầu mới",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = AppColors.PrimaryDark
                        )

                        successMessage?.let {
                            Text(it, color = AppColors.GreenSuccess, fontSize = 13.sp)
                        }
                        errorMessage?.let {
                            Text(it, color = AppColors.RedError, fontSize = 13.sp)
                        }

                        val hasPendingRequest = requests.any { it.status == "PENDING" }

                        if (hasPendingRequest) {
                            Surface(
                                color = AppColors.YellowWarning.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    "Bạn đang có yêu cầu chờ duyệt. Không thể gửi thêm yêu cầu mới.",
                                    color = AppColors.YellowWarning,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 18.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (activeCard != null) {
                                    showExistingCardDialog = true
                                } else {
                                    showConfirmDialog = true
                                }
                            },
                            enabled = !isSending && !isCheckingCards && !isBlockingCard && !hasPendingRequest,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.WarmOrange)
                        ) {
                            when {
                                isSending -> {
                                    CircularProgressIndicator(
                                        color = AppColors.OnAccent,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                }

                                isCheckingCards -> {
                                    CircularProgressIndicator(
                                        color = AppColors.OnAccent,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                }

                                hasPendingRequest -> {
                                    Icon(
                                        Icons.Default.HourglassEmpty,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Đang chờ duyệt", fontWeight = FontWeight.SemiBold)
                                }

                                else -> {
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Yêu cầu cấp thẻ", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            if (requests.isNotEmpty()) {
                item {
                    Text(
                        "Yêu cầu của tôi (${requests.size})",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = AppColors.PrimaryDark
                    )
                }
                items(requests) { req ->
                    CardRequestItem(
                        request = req,
                        isCancelling = isCancelling,
                        onCancelClick = if (req.status == "PENDING") {
                            { cancelTargetRequestId = req.requestId }
                        } else null
                    )
                }
            } else if (!isLoading) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Chưa có yêu cầu nào", color = AppColors.PrimaryGray)
                    }
                }
            }
        }
    }
}

@Composable
private fun CardRequestItem(
    request: CardRequestDTO,
    isCancelling: Boolean = false,
    onCancelClick: (() -> Unit)? = null
) {
    val (statusColor, statusLabel, statusIcon) = when (request.status) {
        "PENDING" -> Triple(AppColors.YellowWarning, "Chờ duyệt", Icons.Default.HourglassEmpty)
        "APPROVED" -> Triple(AppColors.GreenSuccess, "Hoàn thành", Icons.Default.CheckCircle)
        "COMPLETED" -> Triple(AppColors.GreenSuccess, "Hoàn thành", Icons.Default.CheckCircle)
        "REJECTED" -> Triple(AppColors.RedError, "Từ chối", Icons.Default.CreditCard)
        "CANCELED" -> Triple(AppColors.PrimaryGray, "Đã hủy", Icons.Default.CreditCard)
        else -> Triple(AppColors.PrimaryGray, request.status, Icons.Default.CreditCard)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                statusLabel,
                                color = statusColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        if (!request.reviewNote.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text("Phản hồi: ${request.reviewNote}", fontSize = 12.sp, color = statusColor)
                        }
                    }
                }
                Text(
                    request.createdAt.take(10),
                    fontSize = 11.sp,
                    color = AppColors.PrimaryGray
                )
            }

            if (onCancelClick != null) {
                Button(
                    onClick = onCancelClick,
                    enabled = !isCancelling,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.RedError.copy(alpha = 0.1f),
                        contentColor = AppColors.RedError
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(
                            color = AppColors.RedError,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Hủy yêu cầu", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
