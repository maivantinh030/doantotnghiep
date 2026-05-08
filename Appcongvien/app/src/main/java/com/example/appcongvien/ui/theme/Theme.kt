package com.example.appcongvien.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

@Immutable
data class ParkColorPalette(
    val backgroundBase: Color,
    val backgroundWarm: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceMuted: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentStrong: Color,
    val accentSoft: Color,
    val accentSoftAlt: Color,
    val headerGradient: List<Color>,
    val actionGradient1: List<Color>,
    val actionGradient2: List<Color>,
    val actionGradient3: List<Color>,
    val darkCardGradient: List<Color>,
    val darkCardTextPrimary: Color,
    val darkCardTextSecondary: Color,
    val cardBackground: Color,
    val serviceIconBg: Color,
    val serviceIcon: Color,
    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val error: Color,
    val errorContainer: Color,
    val info: Color,
    val infoContainer: Color,
    val destructiveSurface: Color,
    val destructiveText: Color,
    val imageScrim: Color,
    val onAccent: Color
)

private val LightPalette = ParkColorPalette(
    backgroundBase = Color(0xFFF5F5F3),
    backgroundWarm = Color(0xFFFFFAF4),
    surface = Color(0xFFFFFFFF),
    surfaceRaised = Color(0xFFF9F8F5),
    surfaceMuted = Color(0xFFEEE9E2),
    borderSubtle = Color(0xFFE2DDD5),
    textPrimary = Color(0xFF1C1C1E),
    textSecondary = Color(0xFF6E6E73),
    textTertiary = Color(0xFF8E8E93),
    accent = Color(0xFFFF7A00),
    accentStrong = Color(0xFFFFA24D),
    accentSoft = Color(0xFFFFE7CC),
    accentSoftAlt = Color(0xFFFFF1E1),
    headerGradient = listOf(
        Color(0xFFFFF7EF),
        Color(0xFFFFE1BF),
        Color(0xFFFFAF66)
    ),
    actionGradient1 = listOf(Color(0xFFFFA64D), Color(0xFFFF7A00)),
    actionGradient2 = listOf(Color(0xFFFFC074), Color(0xFFFF9540)),
    actionGradient3 = listOf(Color(0xFFFFB061), Color(0xFFFF7A00)),
    darkCardGradient = listOf(Color(0xFF2C2C30), Color(0xFF4D4742)),
    darkCardTextPrimary = Color(0xFFFFFFFF),
    darkCardTextSecondary = Color(0xFFFBE4CC),
    cardBackground = Color(0xFFF9F8F5),
    serviceIconBg = Color(0xFFFFF1E1),
    serviceIcon = Color(0xFFFF7A00),
    success = Color(0xFF2F7D32),
    successContainer = Color(0xFFE6F4EA),
    warning = Color(0xFFA66A00),
    warningContainer = Color(0xFFFFF3D6),
    error = Color(0xFFC43D2F),
    errorContainer = Color(0xFFFDE5E3),
    info = Color(0xFF2667C9),
    infoContainer = Color(0xFFEAF2FF),
    destructiveSurface = Color(0xFFFFF2F0),
    destructiveText = Color(0xFFC43D2F),
    imageScrim = Color(0x94000000),
    onAccent = Color.White
)

private val DarkPalette = ParkColorPalette(
    backgroundBase = Color(0xFF141517),
    backgroundWarm = Color(0xFF1A1918),
    surface = Color(0xFF1B1D21),
    surfaceRaised = Color(0xFF23252A),
    surfaceMuted = Color(0xFF2B2E34),
    borderSubtle = Color(0xFF343840),
    textPrimary = Color(0xFFF3F1EC),
    textSecondary = Color(0xFFB8B1A7),
    textTertiary = Color(0xFF98918A),
    accent = Color(0xFFFF9A3D),
    accentStrong = Color(0xFFFFB05E),
    accentSoft = Color(0xFF4A3523),
    accentSoftAlt = Color(0xFF35291D),
    headerGradient = listOf(
        Color(0xFF1B1A19),
        Color(0xFF2A221C),
        Color(0xFF3B2D20)
    ),
    actionGradient1 = listOf(Color(0xFF65422A), Color(0xFFFF9A3D)),
    actionGradient2 = listOf(Color(0xFF7B5734), Color(0xFFFFB05E)),
    actionGradient3 = listOf(Color(0xFF6A4930), Color(0xFFFF9A3D)),
    darkCardGradient = listOf(Color(0xFF2A211A), Color(0xFF4A3828)),
    darkCardTextPrimary = Color(0xFFF7F2EC),
    darkCardTextSecondary = Color(0xFFE0D4C4),
    cardBackground = Color(0xFF23252A),
    serviceIconBg = Color(0xFF35291D),
    serviceIcon = Color(0xFFFF9A3D),
    success = Color(0xFF62C174),
    successContainer = Color(0xFF173424),
    warning = Color(0xFFF0B44C),
    warningContainer = Color(0xFF3B2F14),
    error = Color(0xFFFF9C8F),
    errorContainer = Color(0xFF3D1F1E),
    info = Color(0xFF86BBFF),
    infoContainer = Color(0xFF1A2B40),
    destructiveSurface = Color(0xFF3D1F1E),
    destructiveText = Color(0xFFFF9C8F),
    imageScrim = Color(0xAA000000),
    onAccent = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightPalette.accent,
    secondary = LightPalette.accentSoft,
    tertiary = Color(0xFF2196F3),
    background = LightPalette.backgroundBase,
    surface = LightPalette.surface,
    surfaceVariant = LightPalette.surfaceMuted,
    onPrimary = LightPalette.onAccent,
    onSecondary = LightPalette.textPrimary,
    onTertiary = LightPalette.onAccent,
    onBackground = LightPalette.textPrimary,
    onSurface = LightPalette.textPrimary,
    onSurfaceVariant = LightPalette.textSecondary,
    outline = LightPalette.borderSubtle,
    outlineVariant = LightPalette.surfaceMuted,
    error = LightPalette.error,
    onError = LightPalette.onAccent
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPalette.accent,
    secondary = DarkPalette.accentSoft,
    tertiary = Color(0xFF2196F3),
    background = DarkPalette.backgroundBase,
    surface = DarkPalette.surface,
    surfaceVariant = DarkPalette.surfaceMuted,
    onPrimary = DarkPalette.onAccent,
    onSecondary = DarkPalette.textPrimary,
    onTertiary = DarkPalette.onAccent,
    onBackground = DarkPalette.textPrimary,
    onSurface = DarkPalette.textPrimary,
    onSurfaceVariant = DarkPalette.textSecondary,
    outline = DarkPalette.borderSubtle,
    outlineVariant = DarkPalette.surfaceMuted,
    error = DarkPalette.error,
    onError = DarkPalette.onAccent
)

private val LocalParkColors = compositionLocalOf { LightPalette }

@Stable
object ParkTheme {
    val colors: ParkColorPalette
        @Composable get() = LocalParkColors.current
}

@Composable
fun AppcongvienTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    SideEffect {
        AppColors.updateFromPalette(palette)
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalParkColors provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

object AppColors {
    var PrimaryDark by mutableStateOf(LightPalette.textPrimary)
        private set
    var PrimaryGray by mutableStateOf(LightPalette.textSecondary)
        private set
    var SecondaryGray by mutableStateOf(LightPalette.textTertiary)
        private set
    var SurfaceLight by mutableStateOf(LightPalette.backgroundBase)
        private set
    var SurfaceWhite by mutableStateOf(LightPalette.surface)
        private set
    var SurfaceRaised by mutableStateOf(LightPalette.surfaceRaised)
        private set
    var SurfaceMuted by mutableStateOf(LightPalette.surfaceMuted)
        private set
    var BorderSubtle by mutableStateOf(LightPalette.borderSubtle)
        private set

    var BackgroundWarm by mutableStateOf(LightPalette.backgroundWarm)
        private set
    var WarmOrange by mutableStateOf(LightPalette.accent)
        private set
    var WarmOrangeLight by mutableStateOf(LightPalette.accentStrong)
        private set
    var WarmOrangeSoft by mutableStateOf(LightPalette.accentSoft)
        private set
    var WarmOrangeSoftAlt by mutableStateOf(LightPalette.accentSoftAlt)
        private set
    var WarmOrangeGrad1 by mutableStateOf(LightPalette.actionGradient1.first())
        private set
    var WarmOrangeGrad2 by mutableStateOf(LightPalette.actionGradient1.last())
        private set

    var HeaderGrad1 by mutableStateOf(LightPalette.headerGradient[0])
        private set
    var HeaderGrad2 by mutableStateOf(LightPalette.headerGradient[1])
        private set
    var HeaderGrad3 by mutableStateOf(LightPalette.headerGradient[2])
        private set

    var CardPrimary by mutableStateOf(LightPalette.darkCardTextPrimary)
        private set
    var CardSecondary by mutableStateOf(LightPalette.darkCardTextSecondary)
        private set
    var CardBackground by mutableStateOf(LightPalette.cardBackground)
        private set
    var CardGrad1 by mutableStateOf(LightPalette.darkCardGradient.first())
        private set
    var CardGrad2 by mutableStateOf(LightPalette.darkCardGradient.last())
        private set

    var ActionGrad1 by mutableStateOf(LightPalette.actionGradient1)
        private set
    var ActionGrad2 by mutableStateOf(LightPalette.actionGradient2)
        private set
    var ActionGrad3 by mutableStateOf(LightPalette.actionGradient3)
        private set

    var ServiceIconBg by mutableStateOf(LightPalette.serviceIconBg)
        private set
    var ServiceIcon by mutableStateOf(LightPalette.serviceIcon)
        private set

    var BluePrimary by mutableStateOf(Color(0xFF2196F3))
        private set
    var GreenSuccess by mutableStateOf(LightPalette.success)
        private set
    var GreenSuccessContainer by mutableStateOf(LightPalette.successContainer)
        private set
    var RedError by mutableStateOf(LightPalette.error)
        private set
    var RedErrorContainer by mutableStateOf(LightPalette.errorContainer)
        private set
    var YellowWarning by mutableStateOf(LightPalette.warning)
        private set
    var YellowWarningContainer by mutableStateOf(LightPalette.warningContainer)
        private set
    var InfoPrimary by mutableStateOf(LightPalette.info)
        private set
    var InfoContainer by mutableStateOf(LightPalette.infoContainer)
        private set
    var DestructiveSurface by mutableStateOf(LightPalette.destructiveSurface)
        private set
    var DestructiveText by mutableStateOf(LightPalette.destructiveText)
        private set
    var ImageScrim by mutableStateOf(LightPalette.imageScrim)
        private set
    var OnAccent by mutableStateOf(LightPalette.onAccent)
        private set

    internal fun updateFromPalette(palette: ParkColorPalette) {
        PrimaryDark = palette.textPrimary
        PrimaryGray = palette.textSecondary
        SecondaryGray = palette.textTertiary
        SurfaceLight = palette.backgroundBase
        SurfaceWhite = palette.surface
        SurfaceRaised = palette.surfaceRaised
        SurfaceMuted = palette.surfaceMuted
        BorderSubtle = palette.borderSubtle
        BackgroundWarm = palette.backgroundWarm
        WarmOrange = palette.accent
        WarmOrangeLight = palette.accentStrong
        WarmOrangeSoft = palette.accentSoft
        WarmOrangeSoftAlt = palette.accentSoftAlt
        WarmOrangeGrad1 = palette.actionGradient1.first()
        WarmOrangeGrad2 = palette.actionGradient1.last()
        HeaderGrad1 = palette.headerGradient[0]
        HeaderGrad2 = palette.headerGradient[1]
        HeaderGrad3 = palette.headerGradient[2]
        CardPrimary = palette.darkCardTextPrimary
        CardSecondary = palette.darkCardTextSecondary
        CardBackground = palette.cardBackground
        CardGrad1 = palette.darkCardGradient.first()
        CardGrad2 = palette.darkCardGradient.last()
        ActionGrad1 = palette.actionGradient1
        ActionGrad2 = palette.actionGradient2
        ActionGrad3 = palette.actionGradient3
        ServiceIconBg = palette.serviceIconBg
        ServiceIcon = palette.serviceIcon
        GreenSuccess = palette.success
        GreenSuccessContainer = palette.successContainer
        RedError = palette.error
        RedErrorContainer = palette.errorContainer
        YellowWarning = palette.warning
        YellowWarningContainer = palette.warningContainer
        InfoPrimary = palette.info
        InfoContainer = palette.infoContainer
        DestructiveSurface = palette.destructiveSurface
        DestructiveText = palette.destructiveText
        ImageScrim = palette.imageScrim
        OnAccent = palette.onAccent
    }
}
