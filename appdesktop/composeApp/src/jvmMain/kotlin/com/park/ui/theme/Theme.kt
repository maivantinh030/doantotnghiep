package com.park.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ===== Color Palette =====
object AppColors {
    val WarmOrange = Color(0xFFFF6B35)
    val OrangeDark = Color(0xFFE55722)
    val OrangeLight = Color(0xFFFFF3EE)
    val BluePrimary = Color(0xFF2196F3)
    val SurfaceLight = Color(0xFFF8F9FA)
    val White = Color(0xFFFFFFFF)
    val PrimaryDark = Color(0xFF1A1A1A)
    val PrimaryGray = Color(0xFF6B7280)
    val LightGray = Color(0xFFE5E7EB)
    val RedError = Color(0xFFF44336)
    val GreenSuccess = Color(0xFF4CAF50)
    val YellowWarning = Color(0xFFFFC107)
    val SidebarBg = Color(0xFF1E2332)
    val SidebarSelected = Color(0xFF2D3348)
    val SidebarText = Color(0xFFB0B7C3)
}

private val AdminColorScheme = lightColorScheme(
    primary = AppColors.WarmOrange,
    onPrimary = AppColors.White,
    primaryContainer = AppColors.OrangeLight,
    secondary = AppColors.BluePrimary,
    background = AppColors.SurfaceLight,
    surface = AppColors.White,
    onBackground = AppColors.PrimaryDark,
    onSurface = AppColors.PrimaryDark,
    error = AppColors.RedError,
)

// ===== Typography =====
object AppTypography {
    val displayLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold)
    val headlineLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
    val titleLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    val bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)
    val bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)
    val labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
}

@Composable
fun AdminTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AdminColorScheme,
        content = content
    )
}
