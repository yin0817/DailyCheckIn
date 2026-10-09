package com.dailycheckin.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/**
 * Stone page, white sheets, one ink-blue seal.
 * The seal is the completed day and today's ring. Everything else is ink or stone.
 */
object AppColors {
    val Canvas = Color(0xFFF4F4F6)
    val Sheet = Color(0xFFFFFFFF)
    val Ink = Color(0xFF16181D)
    val Stone = Color(0xFF5E636E)
    val Line = Color(0xFFE4E5EA)
    val Track = Color(0xFFECEDEF)
    val Seal = Color(0xFF1E3A5F)

    val CanvasNight = Color(0xFF0E1014)
    val SheetNight = Color(0xFF191C22)
    val InkNight = Color(0xFFF2F3F5)
    val StoneNight = Color(0xFFA7ACB8)
    val LineNight = Color(0xFF2C3038)
    val TrackNight = Color(0xFF242830)
    val SegmentNight = Color(0xFF323844)
    val SealNight = Color(0xFFD5E2FA)
    val SealInkNight = Color(0xFF152033)

    val Danger = Color(0xFF8C2F2A)
    val DangerNight = Color(0xFFF0B4AE)
}

private val LightColors = lightColorScheme(
    primary = AppColors.Seal,
    onPrimary = Color.White,
    primaryContainer = AppColors.Seal,
    onPrimaryContainer = Color.White,
    secondary = AppColors.Ink,
    onSecondary = AppColors.Canvas,
    background = AppColors.Canvas,
    onBackground = AppColors.Ink,
    surface = AppColors.Sheet,
    onSurface = AppColors.Ink,
    surfaceVariant = AppColors.Track,
    onSurfaceVariant = AppColors.Stone,
    outline = AppColors.Line,
    outlineVariant = AppColors.Line,
    error = AppColors.Danger,
    onError = Color.White,
    errorContainer = Color(0xFFF8E8E6),
    onErrorContainer = AppColors.Danger,
    surfaceContainerLowest = AppColors.Sheet,
    surfaceContainerLow = AppColors.Sheet,
    surfaceContainer = AppColors.Track,
    surfaceContainerHigh = AppColors.Sheet,
    surfaceContainerHighest = AppColors.Sheet
)

private val DarkColors = darkColorScheme(
    primary = AppColors.SealNight,
    onPrimary = AppColors.SealInkNight,
    primaryContainer = AppColors.SealNight,
    onPrimaryContainer = AppColors.SealInkNight,
    secondary = AppColors.InkNight,
    onSecondary = AppColors.CanvasNight,
    background = AppColors.CanvasNight,
    onBackground = AppColors.InkNight,
    surface = AppColors.SheetNight,
    onSurface = AppColors.InkNight,
    surfaceVariant = AppColors.TrackNight,
    onSurfaceVariant = AppColors.StoneNight,
    outline = AppColors.LineNight,
    outlineVariant = AppColors.LineNight,
    error = AppColors.DangerNight,
    onError = AppColors.CanvasNight,
    errorContainer = Color(0xFF3A221F),
    onErrorContainer = AppColors.DangerNight,
    surfaceContainerLowest = AppColors.CanvasNight,
    surfaceContainerLow = AppColors.SheetNight,
    surfaceContainer = AppColors.TrackNight,
    surfaceContainerHigh = AppColors.SegmentNight,
    surfaceContainerHighest = AppColors.SegmentNight
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun DailyCheckInTheme(
    themeMode: String = "system",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
