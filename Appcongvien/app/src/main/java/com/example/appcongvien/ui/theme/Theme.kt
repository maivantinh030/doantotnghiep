package com.example.appcongvien.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = OrangeSecondary,
    secondary = OrangeLight,
    tertiary = Pink80,
    background = Color(0xFF161719),
    surface = Color(0xFF1F2023),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    secondary = Color(0xFFFFE4C7),
    tertiary = BluePrimary,
    background = Color(0xFFF5F5F3),
    surface = Color.White,
    surfaceVariant = Color(0xFFF9F8F5),
    onPrimary = Color.White,
    onSecondary = Color(0xFF1C1C1E),
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1C1E),
    onSurface = Color(0xFF1C1C1E),
    onSurfaceVariant = Color(0xFF6E6E73),
    outline = Color(0xFFE2DDD5),
    outlineVariant = Color(0xFFEEE9E2),
    error = RedError,
    onError = Color.White
)

@Composable
fun AppcongvienTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
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
    val PrimaryDark = Color(0xFF1C1C1E)
    val PrimaryGray = Color(0xFF6E6E73)
    val SecondaryGray = Color(0xFF8E8E93)
    val SurfaceLight = Color(0xFFF5F5F3)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val SurfaceMuted = Color(0xFFEEE9E2)
    val BorderSubtle = Color(0xFFE2DDD5)

    val WarmOrange = Color(0xFFFF7A00)
    val WarmOrangeLight = Color(0xFFFFA24D)
    val WarmOrangeSoft = Color(0xFFFFE7CC)
    val WarmOrangeGrad1 = Color(0xFFFFB766)
    val WarmOrangeGrad2 = Color(0xFFFF7A00)

    val HeaderGrad1 = Color(0xFFFFF7EF)
    val HeaderGrad2 = Color(0xFFFFE1BF)
    val HeaderGrad3 = Color(0xFFFFAF66)

    val CardPrimary = Color(0xFF2C2C30)
    val CardSecondary = Color(0xFF636369)
    val CardBackground = Color(0xFFF9F8F5)
    val CardGrad1 = Color(0xFF2C2C30)
    val CardGrad2 = Color(0xFF44444A)

    val ActionGrad1 = listOf(Color(0xFFFFA64D), Color(0xFFFF7A00))
    val ActionGrad2 = listOf(Color(0xFFFFC074), Color(0xFFFF9540))
    val ActionGrad3 = listOf(Color(0xFFFFB061), Color(0xFFFF7A00))

    val ServiceIconBg = Color(0xFFFFF1E1)
    val ServiceIcon = WarmOrange
    val ServiceCard = SurfaceWhite

    val BluePrimary = com.example.appcongvien.ui.theme.BluePrimary
    val GreenSuccess = com.example.appcongvien.ui.theme.GreenSuccess
    val RedError = com.example.appcongvien.ui.theme.RedError
    val YellowWarning = com.example.appcongvien.ui.theme.YellowWarning
}
