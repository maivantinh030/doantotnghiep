package com.example.appcongvien.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcongvien.ui.theme.AppColors

/**
 * Compact, reusable TopAppBar dùng chung cho toàn bộ app.
 *
 * @param title Tiêu đề dạng String (dùng cho hầu hết các screen)
 * @param onBackClick Callback khi nhấn nút quay lại
 * @param actions Optional trailing actions (icons, badges, …)
 * @param titleContent Nếu cần title phức tạp (Row, Badge…), truyền composable này thay cho [title]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkTopAppBar(
    title: String = "",
    onBackClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    titleContent: (@Composable () -> Unit)? = null
) {
    TopAppBar(
        title = {
            if (titleContent != null) {
                titleContent()
            } else {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
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
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppColors.WarmOrange
        ),
        windowInsets = WindowInsets(0.dp)
    )
}
