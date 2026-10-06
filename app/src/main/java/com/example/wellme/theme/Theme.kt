package com.example.wellme.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
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
    background = AppWhiteOff,
    surface = AppWhite,
    onBackground = AppBlack,
    onSurface = AppBlack,
    outline = Color(0xFFCBD5E1),
    onSurfaceVariant = Color(0xFF64748B)
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
    onSurface = AppBlack,
    outline = Color(0xFFCBD5E1),
    onSurfaceVariant = Color(0xFF64748B)
)

@Composable
fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
    cursorColor = MaterialTheme.colorScheme.primary
)

@Composable
fun WellMeTheme(
    darkTheme: Boolean = false, // Force clean Light Mode app-wide
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
