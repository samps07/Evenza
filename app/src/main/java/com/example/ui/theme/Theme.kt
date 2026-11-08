package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val BentoDarkColorScheme = darkColorScheme(
    primary = BentoPrimaryDark,
    onPrimary = Slate900,
    primaryContainer = BentoBlueBorderDark,
    onPrimaryContainer = BentoBlueTrack,
    secondary = BentoOrangeFill,
    onSecondary = Color.White,
    secondaryContainer = BentoOrangeBorderDark,
    onSecondaryContainer = BentoOrangeTrack,
    background = BentoCanvasDark,
    surface = BentoSurfaceDark,
    surfaceVariant = BentoSurfaceVariantDark,
    onBackground = Color(0xFFF5F2F0),
    onSurface = Color(0xFFF5F2F0),
    onSurfaceVariant = Slate400,
    outline = Slate600
)

private val BentoLightColorScheme = lightColorScheme(
    primary = BentoPrimary,
    onPrimary = Color.White,
    primaryContainer = BentoFabContainer,
    onPrimaryContainer = BentoFabContent,
    secondary = BentoOrangeFill,
    onSecondary = Color.White,
    secondaryContainer = BentoOrangeBgLight,
    onSecondaryContainer = BentoOrangeTextLight,
    background = BentoCanvasLight, // #FDF8F6
    surface = BentoSurfaceLight,
    surfaceVariant = BentoSurfaceVariantLight, // #F7F2F1
    onBackground = Slate800, // #1E293B
    onSurface = Slate800,
    onSurfaceVariant = Slate500, // #64748B
    outline = Slate200 // #E2E8F0
)

@Composable
fun CounterTheme(
    themePreference: String = "System",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themePreference.lowercase()) {
        "dark" -> true
        "light" -> false
        else -> systemDark
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> BentoDarkColorScheme
        else -> BentoLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias for test/previews
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CounterTheme(
        themePreference = if (darkTheme) "Dark" else "Light",
        dynamicColor = dynamicColor,
        content = content
    )
}

@Composable
fun EvenzaTheme(
    themePreference: String = "System",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CounterTheme(
        themePreference = themePreference,
        dynamicColor = dynamicColor,
        content = content
    )
}
