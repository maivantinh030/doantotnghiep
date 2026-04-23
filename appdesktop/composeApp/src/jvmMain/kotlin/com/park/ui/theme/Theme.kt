package com.park.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.sp

// ===== Fonts =====
val InterFontFamily = FontFamily(
    Font("font/Inter-Regular.ttf", FontWeight.Normal),
    Font("font/Inter-Medium.ttf", FontWeight.Medium),
    Font("font/Inter-SemiBold.ttf", FontWeight.SemiBold),
    Font("font/Inter-Bold.ttf", FontWeight.Bold)
)

// ===== Color Palette =====
object AppColors {
    // Brand & Interactive Palette (Orange Scale)
    val WarmOrange = Color(0xFFFF6B35)
    val OrangeDark = Color(0xFFE55722)
    val OrangeSoft = Color(0xFFFFF3EE)
    val OrangeText = Color(0xFFB95E1D)

    // Secondary & Data Palette
    val DataBlue = Color(0xFF2E77F4)

    // Surface & Border Palette
    val MainBackground = Color(0xFFF4F6FA)
    val CardSurface = Color(0xFFFFFFFF)
    val SidebarBg = Color(0xFF1E2332)
    val SidebarSelected = Color(0xFF2D3348)
    val BorderLight = Color(0xFFE5E7EB)

    // Typography Palette
    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF6B7280)
    val SidebarText = Color(0xFFB0B7C3)

    // Semantic / Status Colors
    val GreenSuccess = Color(0xFF129A58)
    val SoftSuccessSurface = Color(0xFFE8F8EE)
    
    val YellowWarning = Color(0xFFD18700)
    val SoftWarningSurface = Color(0xFFFFF5E4)
    
    val RedError = Color(0xFFD14343)
    val SoftErrorSurface = Color(0xFFFDECEC)

    // Compatibility aliases for any missed spots
    val ActionBlue = WarmOrange
    val PrimaryDark = TextPrimary
    val PrimaryGray = TextSecondary
    val LightGray = BorderLight
    val PlayerBlue = DataBlue
    val RevenueOrange = WarmOrange
    val SuccessText = GreenSuccess
    val WarningText = YellowWarning
    val ErrorText = RedError
    
    // Legacy aliases to fix compilation errors
    val White = Color(0xFFFFFFFF)
    val LightBlueFill = Color(0xFFE9EEF9)
    val BluePrimary = DataBlue
    val OrangeLight = OrangeSoft
}

private val AdminColorScheme = lightColorScheme(
    primary = AppColors.WarmOrange,
    onPrimary = Color.White,
    primaryContainer = AppColors.OrangeSoft,
    secondary = AppColors.DataBlue,
    background = AppColors.MainBackground,
    surface = AppColors.CardSurface,
    onBackground = AppColors.TextPrimary,
    onSurface = AppColors.TextPrimary,
    error = AppColors.RedError,
    outline = AppColors.BorderLight,
    surfaceVariant = AppColors.MainBackground
)

// ===== Typography =====
val AppTypographyOptions = androidx.compose.material3.Typography(
    displayLarge = TextStyle(fontFamily = InterFontFamily, fontSize = 32.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontFamily = InterFontFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontFamily = InterFontFamily, fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontFamily = InterFontFamily, fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontFamily = InterFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal),
    labelSmall = TextStyle(fontFamily = InterFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium)
)

object AppTypography {
    val displayLarge = TextStyle(fontFamily = InterFontFamily, fontSize = 32.sp, fontWeight = FontWeight.Bold)
    val headlineLarge = TextStyle(fontFamily = InterFontFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    val titleLarge = TextStyle(fontFamily = InterFontFamily, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    val bodyLarge = TextStyle(fontFamily = InterFontFamily, fontSize = 16.sp, fontWeight = FontWeight.Normal)
    val bodyMedium = TextStyle(fontFamily = InterFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal)
    val labelSmall = TextStyle(fontFamily = InterFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium)
}

@Composable
fun AdminTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AdminColorScheme,
        typography = AppTypographyOptions,
        content = content
    )
}
