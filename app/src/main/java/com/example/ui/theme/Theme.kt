package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun FahheemTheme(
    commitmentPercentage: Int = 85,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val (primaryColor, secondaryColor, backgroundColor) = when {
        commitmentPercentage >= 80 -> Triple(GreenPrimary, GreenSecondary, GreenBackground)
        commitmentPercentage >= 60 -> Triple(BluePrimary, BlueSecondary, BlueBackground)
        commitmentPercentage >= 40 -> Triple(OrangePrimary, OrangeSecondary, OrangeBackground)
        else -> Triple(RedPrimary, RedSecondary, RedBackground)
    }

    val lightColorScheme = lightColorScheme(
        primary = ElectricBluePrimary,
        secondary = ElectricBlueSecondary,
        background = FrostedBackground,
        surface = FrostedSurface,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = TextPrimaryDark,
        onSurface = TextPrimaryDark,
        surfaceVariant = FrostedSurfaceVariant,
        outline = CardBorderLight,
        primaryContainer = Color(0xFFE0EDFF),
        onPrimaryContainer = ElectricBluePrimary
    )

    val darkColorScheme = darkColorScheme(
        primary = secondaryColor,
        secondary = primaryColor,
        background = Color(0xFF0F172A),
        surface = Color(0xFF1E293B),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color(0xFFF8FAFC),
        onSurface = Color(0xFFF8FAFC),
        surfaceVariant = Color(0xFF334155),
        outline = Color(0xFF475569)
    )

    val colorScheme = if (darkTheme) darkColorScheme else lightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
