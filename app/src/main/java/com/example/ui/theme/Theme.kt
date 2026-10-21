package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SaffronLight,
    onPrimary = Slate900,
    primaryContainer = NavyLight,
    onPrimaryContainer = PureWhite,
    secondary = SaffronOrange,
    onSecondary = PureWhite,
    secondaryContainer = OnSaffronContainer,
    onSecondaryContainer = SaffronContainer,
    tertiary = GreenLight,
    onTertiary = PureWhite,
    background = DarkBackground,
    onBackground = Slate100,
    surface = DarkSurface,
    onSurface = Slate100,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Slate300,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    onPrimary = PureWhite,
    primaryContainer = NavyContainer,
    onPrimaryContainer = DeepNavy,
    secondary = SaffronOrange,
    onSecondary = PureWhite,
    secondaryContainer = SaffronContainer,
    onSecondaryContainer = OnSaffronContainer,
    tertiary = EmeraldGreen,
    onTertiary = PureWhite,
    tertiaryContainer = GreenContainer,
    onTertiaryContainer = OnGreenContainer,
    background = Slate50,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate300
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Keep consistent clean public-service branding
    dynamicColor: Boolean = false, // Keep consistent public-service branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
