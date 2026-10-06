package com.example.wellme.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AppBlue,
    onPrimary = AppWhite,
    primaryContainer = AppBlueDark,
    onPrimaryContainer = AppWhite,
    secondary = AppGold,
    onSecondary = AppBlack,
    secondaryContainer = Color(0xFF332A15),
    onSecondaryContainer = AppGoldLight,
    tertiary = AppGold,
    error = AppRed,
    onError = AppWhite,
    errorContainer = AppRedDark,
    onErrorContainer = AppRedLight,
    background = AppBlack,
    surface = Color(0xFF1E293B),
    onBackground = AppWhiteOff,
    onSurface = AppWhiteOff
)

private val LightColorScheme = lightColorScheme(
    primary = AppBlue,
    onPrimary = AppWhite,
    primaryContainer = AppBlueLight,
    onPrimaryContainer = AppBlack,
    secondary = AppGold,
    onSecondary = AppWhite,
    secondaryContainer = AppGoldLight,
    onSecondaryContainer = AppGoldDark,
    tertiary = AppGold,
    error = AppRed,
    onError = AppWhite,
    errorContainer = AppRedLight,
    onErrorContainer = AppRedDark,
    background = AppWhiteOff,
    surface = AppWhite,
    onBackground = AppBlack,
    onSurface = AppBlack
)

@Composable
fun WellMeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
