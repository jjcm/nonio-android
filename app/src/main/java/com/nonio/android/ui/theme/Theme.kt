package com.nonio.android.ui.theme

import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

val LightColorScheme =
    lightColorScheme(
        primary = Black,
        onPrimary = White,
        primaryContainer = LightGray,
        onPrimaryContainer = Black,
        secondary = Gray,
        onSecondary = Gray1,
        background = White,
        onBackground = Black,
        surface = Gray_LIGHT,
        onSurface = Gray_LIGHT2,
        tertiary = Gray_LIGHT3,
        surfaceTint = IosBlue,
        error = IosRed,
        surfaceContainerHigh = White,
    )

val DarkColorScheme =
    darkColorScheme(
        primary = White,
        onPrimary = Black,
        primaryContainer = DarkGray,
        onPrimaryContainer = White,
        secondary = Gray1,
        onSecondary = White,
        background = Black,
        onBackground = White,
        surface = Gray_DARK,
        onSurface = Gray_DARK2,
        tertiary = White,
        surfaceTint = IosBlue,
        error = IosRed,
        surfaceContainerHigh = Black,
    )

val MarginHorizontalSize = 16.dp

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as ComponentActivity
            val window = activity.window
            if (!darkTheme) {
                window.statusBarColor = White.toArgb()
            } else {
                window.statusBarColor = Black.toArgb()
            }
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
