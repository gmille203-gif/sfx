package com.sfx.viperglass.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ViPER Glass — iOS Liquid Glass tokens
val ViperRed = Color(0xFFFF3B30)
val ViperCyan = Color(0xFF00D9FF)
val ViperBgDark = Color(0xFF09090B)
val ViperCardDark = Color(0xFF1C1C1E)
val ViperCardLight = Color(0xFFF2F2F7)
val ViperGreen = Color(0xFF30D158)

private val DarkScheme = darkColorScheme(
    primary = ViperRed,
    secondary = ViperCyan,
    background = ViperBgDark,
    surface = ViperCardDark,
    surfaceVariant = Color(0xFF2C2C2E),
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightScheme = lightColorScheme(
    primary = ViperRed,
    secondary = Color(0xFF007AFF),
    background = Color(0xFFF2F2F7),
    surface = Color.White,
    surfaceVariant = Color(0xFFE5E5EA),
    onBackground = Color(0xFF1C1C1E),
    onSurface = Color(0xFF1C1C1E)
)

@Composable
fun ViPERGlassTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamic: Boolean = false,
    content: @Composable () -> Unit
) {
    val scheme = when {
        dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(androidx.compose.ui.platform.LocalContext.current)
            else dynamicLightColorScheme(androidx.compose.ui.platform.LocalContext.current)
        }
        darkTheme -> DarkScheme
        else -> LightScheme
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
