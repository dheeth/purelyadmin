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

private val PurelymailDarkColorScheme = darkColorScheme(
    primary = PmPurple300,
    onPrimary = PmPurple900,
    primaryContainer = PmPurple700,
    onPrimaryContainer = PmPurple100,
    secondary = PmEnvelopePink,
    onSecondary = PmLogoDarkPurple,
    secondaryContainer = PmLogoDarkPurple,
    onSecondaryContainer = PmEnvelopePink,
    tertiary = PmLightGreen,
    onTertiary = PmDarkGreen,
    tertiaryContainer = PmDarkGreen,
    onTertiaryContainer = PmLightGreen,
    background = DarkVoidBackground,
    onBackground = Color(0xFFF1EEF8),
    surface = DarkElevatedSurface,
    onSurface = Color(0xFFF1EEF8),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = PmPurple200,
    outline = DarkOutline,
    outlineVariant = Color(0xFF2E1A63),
    error = StatusError,
    onError = Color.White
)

private val PurelymailLightColorScheme = lightColorScheme(
    primary = PmPurple600,
    onPrimary = Color.White,
    primaryContainer = PmPurple100,
    onPrimaryContainer = PmPurple800,
    secondary = PmLogoPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFDF0F7),
    onSecondaryContainer = PmLogoDarkPurple,
    tertiary = PmDarkGreen,
    onTertiary = Color.White,
    tertiaryContainer = PmLightGreen,
    onTertiaryContainer = PmDarkGreen,
    background = LightCleanBackground,
    onBackground = Color(0xFF1C1E21),
    surface = LightCleanSurface,
    onSurface = Color(0xFF1C1E21),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = LightOutline,
    outlineVariant = PmPurple100,
    error = StatusError,
    onError = Color.White
)

@Composable
fun PurelyAdminTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep Purelymail's signature brand theme by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> PurelymailDarkColorScheme
        else -> PurelymailLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
