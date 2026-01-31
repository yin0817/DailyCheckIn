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

// 浅色主题颜色
private val LightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF4CAF50),
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFC8E6C9),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF1B5E20),
    secondary = androidx.compose.ui.graphics.Color(0xFF8BC34A),
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFDCEDC8),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF33691E),
    tertiary = androidx.compose.ui.graphics.Color(0xFF009688),
    onTertiary = androidx.compose.ui.graphics.Color.White,
    tertiaryContainer = androidx.compose.ui.graphics.Color(0xFFB2DFDB),
    onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFF004D40),
    error = androidx.compose.ui.graphics.Color(0xFFB00020),
    errorContainer = androidx.compose.ui.graphics.Color(0xFFFFDAD6),
    onError = androidx.compose.ui.graphics.Color.White,
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFF410002),
    background = androidx.compose.ui.graphics.Color(0xFFFAFAFA),
    onBackground = androidx.compose.ui.graphics.Color(0xFF212121),
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color(0xFF212121),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF757575),
    outline = androidx.compose.ui.graphics.Color(0xFFBDBDBD),
    inverseOnSurface = androidx.compose.ui.graphics.Color(0xFFFAFAFA),
    inverseSurface = androidx.compose.ui.graphics.Color(0xFF303030),
    inversePrimary = androidx.compose.ui.graphics.Color(0xFFA5D6A7),
    surfaceTint = androidx.compose.ui.graphics.Color(0xFF4CAF50),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
    scrim = androidx.compose.ui.graphics.Color.Black
)

// 深色主题颜色
private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF81C784),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF1B5E20),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF2E7D32),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFC8E6C9),
    secondary = androidx.compose.ui.graphics.Color(0xFFAED581),
    onSecondary = androidx.compose.ui.graphics.Color(0xFF33691E),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF558B2F),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFDCEDC8),
    tertiary = androidx.compose.ui.graphics.Color(0xFF80CBC4),
    onTertiary = androidx.compose.ui.graphics.Color(0xFF004D40),
    tertiaryContainer = androidx.compose.ui.graphics.Color(0xFF00695C),
    onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFFB2DFDB),
    error = androidx.compose.ui.graphics.Color(0xFFCF6679),
    errorContainer = androidx.compose.ui.graphics.Color(0xFF93000A),
    onError = androidx.compose.ui.graphics.Color(0xFF690005),
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFFFFDAD6),
    background = androidx.compose.ui.graphics.Color(0xFF121212),
    onBackground = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
    surface = androidx.compose.ui.graphics.Color(0xFF1E1E1E),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF424242),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFBDBDBD),
    outline = androidx.compose.ui.graphics.Color(0xFF616161),
    inverseOnSurface = androidx.compose.ui.graphics.Color(0xFF303030),
    inverseSurface = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
    inversePrimary = androidx.compose.ui.graphics.Color(0xFF4CAF50),
    surfaceTint = androidx.compose.ui.graphics.Color(0xFF81C784),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFF424242),
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
