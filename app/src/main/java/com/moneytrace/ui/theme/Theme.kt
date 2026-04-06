package com.moneytrace.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 主色调：翠绿
val Green500 = Color(0xFF00C896)
val Green700 = Color(0xFF00A07A)
val Green100 = Color(0xFFB2F5E5)

// 支出色
val ExpenseRed = Color(0xFFFF6B6B)
// 收入色
val IncomeGreen = Color(0xFF00C896)

private val LightColorScheme = lightColorScheme(
    primary = Green500,
    onPrimary = Color.White,
    primaryContainer = Green100,
    onPrimaryContainer = Green700,
    secondary = Color(0xFF4ECDC4),
    background = Color(0xFFF8F9FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFF0F4F3),
    onSurface = Color(0xFF1A1A2E),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFFE5E7EB)
)

private val DarkColorScheme = darkColorScheme(
    primary = Green500,
    onPrimary = Color.Black,
    primaryContainer = Green700,
    onPrimaryContainer = Green100,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF334155),
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF94A3B8)
)

@Composable
fun MoneyTraceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
