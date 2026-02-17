package com.example.appcongvien.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = OrangeLight,
    secondary = BlueLight,
    tertiary = Pink80,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    secondary = BluePrimary,
    tertiary = Pink40,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    error = RedError,
    onError = Color.White
)

@Composable
fun AppcongvienTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
object AppColors {
    // Primary Colors (Neutrals)
    val PrimaryDark = Color(0xFF1A1A1A)      // Text chính
    val PrimaryGray = Color(0xFF6B7280)      // Text phụ
    val SurfaceLight = Color(0xFFF8F9FA)     // Background nhẹ
    val SurfaceWhite = Color(0xFFFFFFFF)     // Card background

    // Warm Orange Accents
    val WarmOrange = Color(0xFFE55722)       // Primary accent
    val WarmOrangeLight = Color(0xFFFF7A00)  // Hover/Active
    val WarmOrangeSoft = Color(0xFFFFE4D6)   // Background tint
    val WarmOrangeGrad1 = Color(0xFFE55722)  // Gradient start
    val WarmOrangeGrad2 = Color(0xFFFF8C42)  // Gradient end

    // Header Gradient (Warm Orange)
    val HeaderGrad1 = Color(0xFFFFF4E6)      // Light cream
    val HeaderGrad2 = Color(0xFFFFB366)      // Medium orange
    val HeaderGrad3 = Color(0xFFE55722)      // Warm orange

    // Card Section (Charcoal Gray)
    val CardPrimary = Color(0xFF374151)      // Gray-700
    val CardSecondary = Color(0xFF6B7280)    // Gray-500
    val CardBackground = Color(0xFFF9FAFB)   // Gray-50
    val CardGrad1 = Color(0xFF374151)        // Charcoal
    val CardGrad2 = Color(0xFF4B5563)        // Lighter charcoal

    // Quick Actions (Orange variants)
    val ActionGrad1 = listOf(Color(0xFFE55722), Color(0xFFFF7A00))
    val ActionGrad2 = listOf(Color(0xFFFF8C42), Color(0xFFFFAD73))
    val ActionGrad3 = listOf(Color(0xFFE55722), Color(0xFFFF9F66))

    // Services (Neutral with orange accent)
    val ServiceIconBg = Color(0xFFFFE4D6)    // Soft orange
    val ServiceIcon = Color(0xFFE55722)      // Warm orange
    val ServiceCard = Color(0xFFFFFFFF)      // White
}