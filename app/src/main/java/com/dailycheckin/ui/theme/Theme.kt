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
 * One flat plane and a clear blue.
 * Blue is the brand, the empty ring, and the completed day. No raised sheets.
 */
object AppColors {
    val Canvas = Color(0xFFFFFFFF)
    val Ink = Color(0xFF12151C)
    val Stone = Color(0xFF5C6778)
    val Line = Color(0xFFE3E8F0)
    val Blue = Color(0xFF3B82F6)

    val CanvasNight = Color(0xFF0B1220)
    val InkNight = Color(0xFFF4F7FB)
    val StoneNight = Color(0xFF9AA8BD)
    val LineNight = Color(0xFF1E2A3D)
    val BlueNight = Color(0xFF60A5FA)
    val OnBlueNight = Color(0xFF071426)

    val Danger = Color(0xFF8C2F2A)
    val DangerNight = Color(0xFFF0B4AE)
}

private val LightColors = lightColorScheme(
    primary = AppColors.Blue,
    onPrimary = Color.White,
    primaryContainer = AppColors.Blue,
    onPrimaryContainer = Color.White,
    secondary = AppColors.Ink,
    onSecondary = AppColors.Canvas,
    background = AppColors.Canvas,
    onBackground = AppColors.Ink,
    surface = AppColors.Canvas,
    onSurface = AppColors.Ink,
    surfaceVariant = AppColors.Canvas,
    onSurfaceVariant = AppColors.Stone,
    outline = AppColors.Line,
    outlineVariant = AppColors.Line,
    error = AppColors.Danger,
    onError = Color.White,
    errorContainer = Color(0xFFF8E8E6),
    onErrorContainer = AppColors.Danger,
    surfaceContainerLowest = AppColors.Canvas,
    surfaceContainerLow = AppColors.Canvas,
    surfaceContainer = AppColors.Canvas,
    surfaceContainerHigh = AppColors.Canvas,
    surfaceContainerHighest = AppColors.Canvas
)

private val DarkColors = darkColorScheme(
    primary = AppColors.BlueNight,
    onPrimary = AppColors.OnBlueNight,
    primaryContainer = AppColors.BlueNight,
    onPrimaryContainer = AppColors.OnBlueNight,
    secondary = AppColors.InkNight,
    onSecondary = AppColors.CanvasNight,
    background = AppColors.CanvasNight,
    onBackground = AppColors.InkNight,
    surface = AppColors.CanvasNight,
    onSurface = AppColors.InkNight,
    surfaceVariant = AppColors.CanvasNight,
    onSurfaceVariant = AppColors.StoneNight,
    outline = AppColors.LineNight,
    outlineVariant = AppColors.LineNight,
    error = AppColors.DangerNight,
    onError = AppColors.CanvasNight,
    errorContainer = Color(0xFF3A221F),
    onErrorContainer = AppColors.DangerNight,
    surfaceContainerLowest = AppColors.CanvasNight,
    surfaceContainerLow = AppColors.CanvasNight,
    surfaceContainer = AppColors.CanvasNight,
    surfaceContainerHigh = AppColors.CanvasNight,
    surfaceContainerHighest = AppColors.CanvasNight
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
