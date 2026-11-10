package com.purelymail.admin.ui.theme

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

private val PurelyDarkColorScheme = darkColorScheme(
    primary = PurelyPurpleSecondary,
    onPrimary = Color.White,
    primaryContainer = PurelyPurpleContainerDark,
    onPrimaryContainer = PurelyPurpleLight,
    secondary = PurelyPurpleTertiary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF33206A),
    onSecondaryContainer = Color(0xFFE8DDFF),
    tertiary = Color(0xFF80D8FF),
    onTertiary = Color(0xFF00364C),
    background = DarkBackground,
    onBackground = Color(0xFFEFEBF6),
    surface = DarkSurface,
    onSurface = Color(0xFFEFEBF6),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBC4D8),
    outline = DarkBorder,
    error = StatusError,
    onError = Color.White
)

private val PurelyLightColorScheme = lightColorScheme(
    primary = PurelyPurplePrimary,
    onPrimary = Color.White,
    primaryContainer = PurelyPurpleContainer,
    onPrimaryContainer = PurelyPurpleDark,
    secondary = PurelyPurpleSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEADBFF),
    onSecondaryContainer = PurelyPurpleDark,
    tertiary = Color(0xFF0288D1),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF1B1824),
    surface = LightSurface,
    onSurface = Color(0xFF1B1824),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF5A5568),
    outline = LightBorder,
    error = StatusError,
    onError = Color.White
)

@Composable
fun PurelyAdminTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep sleek custom modern theme consistent by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> PurelyDarkColorScheme
        else -> PurelyLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
