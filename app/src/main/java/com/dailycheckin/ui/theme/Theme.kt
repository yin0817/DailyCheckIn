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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

object AppColors {
    val Primary = Color(0xFF5B7FFF)
    val PrimaryLight = Color(0xFFB7C6FF)
    val Ink = Color(0xFF2F4ED8)

    val SuccessContainer = Color(0xFFE3F5EA)
    val SuccessOnContainer = Color(0xFF146C3A)
    val SuccessContainerDark = Color(0xFF1C3328)
    val SuccessOnDark = Color(0xFF9BE0B6)

    val Gray100 = Color(0xFFF4F6FB)
    val Gray800 = Color(0xFF1B2433)
    val Gray900 = Color(0xFF10141C)
}

private val LightColors = lightColorScheme(
    primary = AppColors.Ink,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4E9FF),
    onPrimaryContainer = Color(0xFF1B2F86),
    secondary = AppColors.Ink,
    onSecondary = Color.White,
    background = AppColors.Gray100,
    onBackground = AppColors.Gray800,
    surface = Color.White,
    onSurface = AppColors.Gray800,
    surfaceVariant = Color(0xFFE8ECF5),
    onSurfaceVariant = Color(0xFF5C677A),
    outline = Color(0xFFD5DCEC),
    outlineVariant = Color(0xFFE3E8F2),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColors = darkColorScheme(
    primary = AppColors.PrimaryLight,
    onPrimary = Color(0xFF13215C),
    primaryContainer = Color(0xFF2A3878),
    onPrimaryContainer = Color(0xFFDCE3FF),
    secondary = AppColors.PrimaryLight,
    onSecondary = Color(0xFF13215C),
    background = AppColors.Gray900,
    onBackground = Color(0xFFE8ECF4),
    surface = Color(0xFF181D27),
    onSurface = Color(0xFFE8ECF4),
    surfaceVariant = Color(0xFF262C3A),
    onSurfaceVariant = Color(0xFFC3CAD8),
    outline = Color(0xFF3A4254),
    outlineVariant = Color(0xFF2C3344),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun doneContainerColor(): Color {
    return if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        AppColors.SuccessContainerDark
    } else {
        AppColors.SuccessContainer
    }
}

@Composable
fun doneContentColor(): Color {
    return if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        AppColors.SuccessOnDark
    } else {
        AppColors.SuccessOnContainer
    }
}

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
