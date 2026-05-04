package com.example.appcongvien.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appcongvien.ui.theme.AppColors

/**
 * Compact, reusable TopAppBar dùng chung cho toàn bộ app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkTopAppBar(
    title: String = "",
    onBackClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    titleContent: (@Composable () -> Unit)? = null
) {
    val contentColor = AppColors.PrimaryDark

    Surface(
        color = AppColors.SurfaceLight,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, AppColors.BorderSubtle.copy(alpha = 0.75f))
    ) {
        TopAppBar(
            title = {
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    if (titleContent != null) {
                        titleContent()
                    } else {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                }
            },
            navigationIcon = {
                Surface(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(40.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = AppColors.WarmOrangeSoft.copy(alpha = 0.55f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = AppColors.WarmOrange,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            },
            actions = {
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    actions()
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                navigationIconContentColor = contentColor,
                titleContentColor = contentColor,
                actionIconContentColor = contentColor
            ),
            windowInsets = WindowInsets(0.dp)
        )
    }
}
