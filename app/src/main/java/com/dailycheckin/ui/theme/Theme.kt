package com.dailycheckin.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 浅色主题颜色 - 现代清新风格
private val LightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF2E7D32),           // 深绿色主色
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFB9F6CA),  // 浅薄荷绿
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF1B5E20),
    secondary = androidx.compose.ui.graphics.Color(0xFF43A047),         // 中绿色
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFC8E6C9),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF1B5E20),
    tertiary = androidx.compose.ui.graphics.Color(0xFF00897B),          // 青色
    onTertiary = androidx.compose.ui.graphics.Color.White,
    tertiaryContainer = androidx.compose.ui.graphics.Color(0xFFA7FFEB),
    onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFF004D40),
    error = androidx.compose.ui.graphics.Color(0xFFD32F2F),
    errorContainer = androidx.compose.ui.graphics.Color(0xFFFFCDD2),
    onError = androidx.compose.ui.graphics.Color.White,
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFFB71C1C),
    background = androidx.compose.ui.graphics.Color(0xFFF8FBF8),        // 微绿白色背景
    onBackground = androidx.compose.ui.graphics.Color(0xFF1C1B1F),
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color(0xFF1C1B1F),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE8F5E9),    // 浅绿色变体
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF49454F),
    outline = androidx.compose.ui.graphics.Color(0xFFC8E6C9),
    inverseOnSurface = androidx.compose.ui.graphics.Color(0xFFF4F4F4),
    inverseSurface = androidx.compose.ui.graphics.Color(0xFF313131),
    inversePrimary = androidx.compose.ui.graphics.Color(0xFF69F0AE),
    surfaceTint = androidx.compose.ui.graphics.Color(0xFF2E7D32),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
    scrim = androidx.compose.ui.graphics.Color.Black
)

// 深色主题颜色 - 护眼深色风格
private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF69F0AE),           // 亮绿色主色
    onPrimary = androidx.compose.ui.graphics.Color(0xFF003300),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF1B5E20),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFB9F6CA),
    secondary = androidx.compose.ui.graphics.Color(0xFFA5D6A7),
    onSecondary = androidx.compose.ui.graphics.Color(0xFF1B5E20),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF2E7D32),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFC8E6C9),
    tertiary = androidx.compose.ui.graphics.Color(0xFF64FFDA),          // 青色
    onTertiary = androidx.compose.ui.graphics.Color(0xFF003D33),
    tertiaryContainer = androidx.compose.ui.graphics.Color(0xFF00695C),
    onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFFA7FFEB),
    error = androidx.compose.ui.graphics.Color(0xFFFF8A80),
    errorContainer = androidx.compose.ui.graphics.Color(0xFF93000A),
    onError = androidx.compose.ui.graphics.Color(0xFF690005),
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFFFFCDD2),
    background = androidx.compose.ui.graphics.Color(0xFF0D1F12),        // 深绿黑色背景
    onBackground = androidx.compose.ui.graphics.Color(0xFFE1E3DE),
    surface = androidx.compose.ui.graphics.Color(0xFF121F17),           // 深绿色表面
    onSurface = androidx.compose.ui.graphics.Color(0xFFE1E3DE),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF1E3A28),    // 深绿色变体
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFC0C9BF),
    outline = androidx.compose.ui.graphics.Color(0xFF3D5A47),
    inverseOnSurface = androidx.compose.ui.graphics.Color(0xFF1C1B1F),
    inverseSurface = androidx.compose.ui.graphics.Color(0xFFE1E3DE),
    inversePrimary = androidx.compose.ui.graphics.Color(0xFF2E7D32),
    surfaceTint = androidx.compose.ui.graphics.Color(0xFF69F0AE),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFF2E4A38),
    scrim = androidx.compose.ui.graphics.Color.Black
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
