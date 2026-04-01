package com.example.appcongvien.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.appcongvien.components.ParkTopAppBar
import com.example.appcongvien.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageHistoryScreen(onBackClick: () -> Unit = {}) {
    Scaffold(
        topBar = { ParkTopAppBar(title = "Lịch sử sử dụng", onBackClick = onBackClick) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Xem lịch sử giao dịch trong phần Số dư & Lịch sử", color = AppColors.PrimaryGray)
        }
    }
}
