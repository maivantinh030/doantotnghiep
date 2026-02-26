package com.example.appcongvien.screen

import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcongvien.App
import com.example.appcongvien.data.model.CardDTO
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.ui.theme.AppColors
import com.example.appcongvien.viewmodel.CardViewModel

enum class CardStatus(val displayName: String, val color: Color) {
    ACTIVE("Đang hoạt động", Color(0xFF4CAF50)),
    EXPIRED("Hết hạn", Color(0xFFF44336)),
    SUSPENDED("Tạm khóa", Color(0xFFFF9800))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardInfoScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onMembershipDetailsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val cardRepository = (context.applicationContext as App).cardRepository
    val viewModel: CardViewModel = viewModel(
        factory = CardViewModel.Factory(cardRepository)
    )

    var showCardNumber by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val cardsState by viewModel.cardsState.collectAsState()
    val blockCardState by viewModel.blockCardState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMyCards()
    }

    // Reload after block/unblock success
    LaunchedEffect(blockCardState) {
        if (blockCardState is Resource.Success) {
            viewModel.loadMyCards()
            viewModel.resetBlockCardState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Thông Tin Thẻ",
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
        when (val state = cardsState) {
            null, is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.WarmOrange)
                }
            }

            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lỗi: ${state.message}",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            is Resource.Success -> {
                val card = state.data.firstOrNull()
                if (card == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = AppColors.PrimaryGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "Bạn chưa liên kết thẻ",
                                fontSize = 16.sp,
                                color = AppColors.PrimaryGray
                            )
                        }
                    }
                } else {
                    CardInfoContent(
                        modifier = modifier,
                        paddingValues = paddingValues,
                        card = card,
                        showCardNumber = showCardNumber,
                        onToggleCardNumber = { showCardNumber = !showCardNumber },
                        scrollState = scrollState,
                        onLockToggle = {
                            if (card.status == "BLOCKED") {
                                viewModel.unblockCard(card.cardId)
                            } else {
                                viewModel.blockCard(card.cardId)
                            }
                        },
                        isLockLoading = blockCardState is Resource.Loading,
                        onMembershipDetailsClick = onMembershipDetailsClick
                    )
                }
            }
        }
    }
}

@Composable
private fun CardInfoContent(
    modifier: Modifier,
    paddingValues: PaddingValues,
    card: CardDTO,
    showCardNumber: Boolean,
    onToggleCardNumber: () -> Unit,
    scrollState: ScrollState,
    onLockToggle: () -> Unit,
    isLockLoading: Boolean,
    onMembershipDetailsClick: () -> Unit
) {
    val isLocked = card.status == "BLOCKED"
    val cardStatus = when (card.status) {
        "ACTIVE" -> CardStatus.ACTIVE
        "BLOCKED" -> CardStatus.SUSPENDED
        else -> CardStatus.EXPIRED
    }

    fun formatDate(isoString: String?): String {
        if (isoString == null) return "—"
        return try {
            val parts = isoString.substring(0, 10).split("-")
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } catch (_: Exception) {
            isoString
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(
                Brush.verticalGradient(
                    listOf(AppColors.SurfaceLight, Color.White)
                )
            )
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Virtual Card Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(AppColors.CardGrad1, AppColors.CardGrad2)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Park Adventure",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (!card.cardName.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = card.cardName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    // Card number section
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (showCardNumber) card.physicalCardUid else "•••• •••• ••••",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                            IconButton(
                                onClick = onToggleCardNumber,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    if (showCardNumber) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showCardNumber) "Ẩn UID thẻ" else "Hiện UID thẻ",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = formatDate(card.issuedAt),
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Security Control
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trạng thái thẻ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = cardStatus.color.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = cardStatus.color,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = cardStatus.displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = cardStatus.color
                            )
                        }
                    }
                }

                Button(
                    onClick = onLockToggle,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLockLoading && card.status != "INACTIVE",
                    shape = RoundedCornerShape(12.dp),
                    colors = if (isLocked) {
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336),
                            contentColor = Color.White
                        )
                    }
                ) {
                    if (isLockLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            if (isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isLocked) "Mở Khóa Thẻ" else "Khóa Thẻ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (isLocked && card.blockedReason != null) {
                    Text(
                        text = "Lý do khóa: ${card.blockedReason}",
                        fontSize = 13.sp,
                        color = Color(0xFFFF9800)
                    )
                }
            }
        }

        // Card Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Chi Tiết Thẻ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
                InfoRow(label = "Mã UID thẻ vật lý", value = card.physicalCardUid)
                InfoRow(label = "Tên thẻ", value = card.cardName ?: "—")
                InfoRow(label = "Ngày phát hành", value = formatDate(card.issuedAt))
                if (card.blockedAt != null) {
                    InfoRow(
                        label = "Ngày khóa",
                        value = formatDate(card.blockedAt),
                        valueColor = Color(0xFFF44336)
                    )
                }
                if (card.lastUsedAt != null) {
                    InfoRow(
                        label = "Sử dụng lần cuối",
                        value = formatDate(card.lastUsedAt)
                    )
                }
            }
        }

        // Membership Benefits
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Quyền Lợi Thành Viên",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
                BenefitItem("Ưu đãi giảm giá tại tất cả khu vui chơi")
                BenefitItem("Ưu tiên đặt chỗ và vào cửa nhanh")
                BenefitItem("Tích điểm và đổi quà hấp dẫn")

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.WarmOrange.copy(alpha = 0.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Khám phá hệ thống hạng",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.WarmOrange
                                )
                                Text(
                                    text = "Xem chi tiết tất cả hạng thành viên và cách nâng cấp",
                                    fontSize = 12.sp,
                                    color = AppColors.PrimaryGray,
                                    lineHeight = 16.sp
                                )
                            }
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = AppColors.WarmOrange,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Button(
                            onClick = onMembershipDetailsClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.WarmOrange,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Xem Chi Tiết Hạng Thành Viên",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

private fun formatDate(isoString: String?): String {
    if (isoString == null) return "—"
    return try {
        val parts = isoString.substring(0, 10).split("-")
        "${parts[2]}/${parts[1]}/${parts[0]}"
    } catch (_: Exception) {
        isoString
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = AppColors.PrimaryDark
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = AppColors.PrimaryGray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun BenefitItem(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = AppColors.WarmOrange,
            modifier = Modifier.size(6.dp)
        ) {}
        Text(
            text = text,
            fontSize = 13.sp,
            color = AppColors.PrimaryGray,
            modifier = Modifier.weight(1f)
        )
    }
}
