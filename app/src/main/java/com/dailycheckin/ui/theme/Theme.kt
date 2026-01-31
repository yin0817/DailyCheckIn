package com.dailycheckin.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 柔和的渐变色系
object AppColors {
    // 主色 - 柔和的靛蓝
    val Primary = Color(0xFF5B7FFF)
    val PrimaryLight = Color(0xFF8BA4FF)
    val PrimaryDark = Color(0xFF3D5AFE)
    
    // 强调色 - 珊瑚橙
    val Accent = Color(0xFFFF7B7B)
    val AccentLight = Color(0xFFFFADAD)
    
    // 成功色
    val Success = Color(0xFF4CAF50)
    val SuccessLight = Color(0xFFE8F5E9)
    
    // 中性色
    val Gray100 = Color(0xFFF7F8FA)
    val Gray200 = Color(0xFFEEF0F4)
    val Gray400 = Color(0xFFB0B8C4)
    val Gray600 = Color(0xFF6B7280)
    val Gray800 = Color(0xFF1F2937)
    val Gray900 = Color(0xFF111827)
}

private val LightColors = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = Color.White,
    primaryContainer = AppColors.PrimaryLight.copy(alpha = 0.2f),
    onPrimaryContainer = AppColors.PrimaryDark,
    secondary = AppColors.Accent,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = AppColors.Gray800,
    surface = Color.White,
    onSurface = AppColors.Gray800,
    surfaceVariant = AppColors.Gray100,
    onSurfaceVariant = AppColors.Gray600,
    outline = AppColors.Gray200
)

private val DarkColors = darkColorScheme(
    primary = AppColors.PrimaryLight,
    onPrimary = AppColors.Gray900,
    primaryContainer = AppColors.Primary.copy(alpha = 0.3f),
    onPrimaryContainer = AppColors.PrimaryLight,
    secondary = AppColors.AccentLight,
    onSecondary = AppColors.Gray900,
    background = Color(0xFF0D1117),
    onBackground = Color(0xFFF0F6FC),
    surface = Color(0xFF161B22),
    onSurface = Color(0xFFF0F6FC),
    surfaceVariant = Color(0xFF21262D),
    onSurfaceVariant = AppColors.Gray400,
    outline = Color(0xFF30363D)
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
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
