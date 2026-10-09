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
 * Paper and ink, with one seal blue.
 * Seal is only for the daily mark and for today.
 */
object AppColors {
    val Paper = Color(0xFFF4F5F2)
    val Ink = Color(0xFF1C1F1A)
    val Moss = Color(0xFF4A5148)
    val Rule = Color(0xFFD5D8D1)
    val Seal = Color(0xFF1E3A8A)

    val PaperNight = Color(0xFF141612)
    val InkNight = Color(0xFFE6E7E2)
    val MossNight = Color(0xFFA3AA9E)
    val RuleNight = Color(0xFF2C312C)
    val SealNight = Color(0xFFC9D4FA)

    val Danger = Color(0xFF8C2F2A)
    val DangerNight = Color(0xFFF0B4AE)
}

private val LightColors = lightColorScheme(
    primary = AppColors.Seal,
    onPrimary = Color.White,
    primaryContainer = AppColors.Seal,
    onPrimaryContainer = Color.White,
    secondary = AppColors.Ink,
    onSecondary = AppColors.Paper,
    background = AppColors.Paper,
    onBackground = AppColors.Ink,
    surface = AppColors.Paper,
    onSurface = AppColors.Ink,
    surfaceVariant = AppColors.Paper,
    onSurfaceVariant = AppColors.Moss,
    outline = AppColors.Rule,
    outlineVariant = AppColors.Rule,
    error = AppColors.Danger,
    onError = Color.White,
    errorContainer = Color(0xFFF6E4E1),
    onErrorContainer = AppColors.Danger,
    surfaceContainerLowest = AppColors.Paper,
    surfaceContainerLow = AppColors.Paper,
    surfaceContainer = Color(0xFFE8EAE4),
    surfaceContainerHigh = Color(0xFFE8EAE4),
    surfaceContainerHighest = Color(0xFFE1E4DC)
)

private val DarkColors = darkColorScheme(
    primary = AppColors.SealNight,
    onPrimary = AppColors.PaperNight,
    primaryContainer = AppColors.SealNight,
    onPrimaryContainer = AppColors.PaperNight,
    secondary = AppColors.InkNight,
    onSecondary = AppColors.PaperNight,
    background = AppColors.PaperNight,
    onBackground = AppColors.InkNight,
    surface = AppColors.PaperNight,
    onSurface = AppColors.InkNight,
    surfaceVariant = AppColors.PaperNight,
    onSurfaceVariant = AppColors.MossNight,
    outline = AppColors.RuleNight,
    outlineVariant = AppColors.RuleNight,
    error = AppColors.DangerNight,
    onError = AppColors.PaperNight,
    errorContainer = Color(0xFF3A221F),
    onErrorContainer = AppColors.DangerNight,
    surfaceContainerLowest = AppColors.PaperNight,
    surfaceContainerLow = AppColors.PaperNight,
    surfaceContainer = Color(0xFF1E221C),
    surfaceContainerHigh = Color(0xFF1E221C),
    surfaceContainerHighest = Color(0xFF262B24)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
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
