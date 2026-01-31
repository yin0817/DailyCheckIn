package com.dailycheckin.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 品牌色 - 紫蓝渐变风格
object AppColors {
    val Purple500 = Color(0xFF6366F1)      // 主紫色
    val Purple400 = Color(0xFF818CF8)      // 浅紫色
    val Purple600 = Color(0xFF4F46E5)      // 深紫色
    val Indigo400 = Color(0xFF6366F1)      // 靛蓝
    val Pink500 = Color(0xFFEC4899)        // 粉色
    val Cyan400 = Color(0xFF22D3EE)        // 青色
    val Amber500 = Color(0xFFF59E0B)       // 琥珀色
    val Emerald500 = Color(0xFF10B981)     // 翠绿色
}

// 浅色主题 - 简约白底紫色调
private val LightColors = lightColorScheme(
    primary = Color(0xFF6366F1),                // 紫色主色
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),       // 浅紫色容器
    onPrimaryContainer = Color(0xFF3730A3),
    secondary = Color(0xFF8B5CF6),              // 紫罗兰
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF5B21B6),
    tertiary = Color(0xFF06B6D4),               // 青色
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCFFAFE),
    onTertiaryContainer = Color(0xFF0E7490),
    error = Color(0xFFEF4444),
    errorContainer = Color(0xFFFEE2E2),
    onError = Color.White,
    onErrorContainer = Color(0xFFB91C1C),
    background = Color(0xFFFAFAFC),             // 微灰白背景
    onBackground = Color(0xFF18181B),
    surface = Color.White,
    onSurface = Color(0xFF18181B),
    surfaceVariant = Color(0xFFF4F4F5),         // 浅灰
    onSurfaceVariant = Color(0xFF52525B),
    outline = Color(0xFFE4E4E7),
    inverseOnSurface = Color(0xFFF4F4F5),
    inverseSurface = Color(0xFF27272A),
    inversePrimary = Color(0xFFA5B4FC),
    surfaceTint = Color(0xFF6366F1),
    outlineVariant = Color(0xFFE4E4E7),
    scrim = Color.Black
)

// 深色主题 - 深邃紫黑
private val DarkColors = darkColorScheme(
    primary = Color(0xFFA5B4FC),                // 浅紫主色
    onPrimary = Color(0xFF312E81),
    primaryContainer = Color(0xFF4338CA),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFFC4B5FD),              // 浅紫罗兰
    onSecondary = Color(0xFF4C1D95),
    secondaryContainer = Color(0xFF6D28D9),
    onSecondaryContainer = Color(0xFFEDE9FE),
    tertiary = Color(0xFF67E8F9),               // 亮青色
    onTertiary = Color(0xFF164E63),
    tertiaryContainer = Color(0xFF0891B2),
    onTertiaryContainer = Color(0xFFCFFAFE),
    error = Color(0xFFFCA5A5),
    errorContainer = Color(0xFF991B1B),
    onError = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    background = Color(0xFF0F0F1A),             // 深紫黑背景
    onBackground = Color(0xFFF4F4F5),
    surface = Color(0xFF18181F),                // 深色表面
    onSurface = Color(0xFFF4F4F5),
    surfaceVariant = Color(0xFF27272F),         // 深灰紫
    onSurfaceVariant = Color(0xFFA1A1AA),
    outline = Color(0xFF3F3F46),
    inverseOnSurface = Color(0xFF18181B),
    inverseSurface = Color(0xFFF4F4F5),
    inversePrimary = Color(0xFF6366F1),
    surfaceTint = Color(0xFFA5B4FC),
    outlineVariant = Color(0xFF3F3F46),
    scrim = Color.Black
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
            // 透明状态栏
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
