package com.example.testnfc.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OrangeLight,
    secondary = BlueLight,
    tertiary = OrangeSoft,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    error = RedError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    secondary = BluePrimary,
    tertiary = OrangeLight,
    background = SurfaceLight,
    surface = SurfaceWhite,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = PrimaryDark,
    onSurface = PrimaryDark,
    primaryContainer = OrangeSoft,
    onPrimaryContainer = WarmOrange,
    secondaryContainer = Color(0xFFE3F2FD),
    onSecondaryContainer = Color(0xFF1565C0),
    error = RedError,
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFC62828),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = PrimaryGray
)

@Composable
fun TestNFCTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled to ensure consistent Park Adventure branding
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}