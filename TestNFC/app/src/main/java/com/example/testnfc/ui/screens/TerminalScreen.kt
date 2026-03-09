package com.example.testnfc.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testnfc.data.model.GameItem
import com.example.testnfc.nfc.TerminalNfcStatus
import com.example.testnfc.ui.theme.AppColors
import com.example.testnfc.ui.viewmodel.PlayResult
import com.example.testnfc.ui.viewmodel.TerminalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    activity: Activity,
    game: GameItem,
    viewModel: TerminalViewModel,
    onNavigateBack: () -> Unit
) {
    val nfcStatus by viewModel.nfcStatus.collectAsState()
    val nfcMessage by viewModel.nfcMessage.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Bật NFC khi vào màn hình, tắt khi rời
    DisposableEffect(Unit) {
        viewModel.enableNfc(activity)
        onDispose { viewModel.disableNfc(activity) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Quét thẻ", fontWeight = FontWeight.Bold)
                        Text(
                            text = game.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onNavigateBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.WarmOrange,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = AppColors.SurfaceLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Thông tin game
            GameInfoCard(game = game)

            Spacer(modifier = Modifier.height(8.dp))

            // Khu vực quét NFC
            when {
                uiState.playLoading -> LoadingIndicator()
                uiState.playResult != null -> PlayResultCard(
                    result = uiState.playResult!!,
                    onScanAgain = { viewModel.resetScan() }
                )
                else -> NfcScanArea(status = nfcStatus, message = nfcMessage)
            }
        }
    }
}

@Composable
private fun GameInfoCard(game: GameItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = AppColors.WarmOrangeSoft,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Nfc,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = AppColors.WarmOrange
                )
            }
            Column {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
                if (!game.category.isNullOrBlank()) {
                    Text(
                        text = game.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.PrimaryGray
                    )
                }
                Text(
                    text = "${game.pricePerTurn} VND/lượt",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.WarmOrange,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun NfcScanArea(status: TerminalNfcStatus, message: String) {
    val isScanning = status == TerminalNfcStatus.SCANNING || status == TerminalNfcStatus.READING
    val containerColor = when (status) {
        TerminalNfcStatus.SCANNING, TerminalNfcStatus.READING ->
            AppColors.WarmOrangeSoft
        TerminalNfcStatus.ERROR ->
            Color(0xFFFFEBEE)
        else ->
            Color.White
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = AppColors.WarmOrange.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Nfc,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = if (status == TerminalNfcStatus.ERROR)
                        AppColors.RedError
                    else
                        AppColors.WarmOrange
                )
            }

            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = AppColors.WarmOrange
                )
            }

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                color = AppColors.PrimaryDark
            )
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.WarmOrangeSoft
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = AppColors.WarmOrange
            )
            Text(
                text = "Đang xử lý...",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = AppColors.PrimaryDark
            )
        }
    }
}

@Composable
private fun PlayResultCard(result: PlayResult, onScanAgain: () -> Unit) {
    val isSuccess = result.success
    val containerColor = if (isSuccess)
        Color(0xFFF1F8E9) // Light green
    else
        Color(0xFFFFEBEE) // Light red

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = if (isSuccess)
                    AppColors.GreenSuccess
                else
                    AppColors.RedError
            )

            Text(
                text = if (isSuccess) "Cho phép chơi!" else "Từ chối",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSuccess)
                    AppColors.GreenSuccess
                else
                    AppColors.RedError
            )

            Text(
                text = result.message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = AppColors.PrimaryDark
            )

            // Thông tin thêm nếu thành công
            result.data?.let { data ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.8f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        InfoRow("Còn lại", "${data.remainingTurns} lượt")
                        InfoRow("Trạng thái vé", data.ticketStatus)
                    }
                }
            }

            Button(
                onClick = onScanAgain,
                shape = RoundedCornerShape(12.dp),
                colors = if (isSuccess)
                    ButtonDefaults.buttonColors(
                        containerColor = AppColors.WarmOrange,
                        contentColor = Color.White
                    )
                else
                    ButtonDefaults.buttonColors(
                        containerColor = AppColors.RedError,
                        contentColor = Color.White
                    )
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text("  Quét lại", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.PrimaryGray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.PrimaryDark
        )
    }
}
