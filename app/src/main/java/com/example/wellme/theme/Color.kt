package com.example.wellme.theme

import androidx.compose.ui.graphics.Color

// Strict App Palette: Blue, Black, White, Red, Gold
val AppBlue = Color(0xFF2563EB)         // Blue Primary
val AppBlueDark = Color(0xFF1E40AF)     // Dark Blue
val AppBlueLight = Color(0xFFEFF6FF)    // Soft Blue Tint

val AppBlack = Color(0xFF0F172A)        // Slate / Neutral Dark
val AppBlackPure = Color(0xFF000000)    // Pure Black

val AppWhite = Color(0xFFFFFFFF)        // Pure White
val AppWhiteOff = Color(0xFFF8FAFC)     // Crisp Background White

val AppRed = Color(0xFFEF4444)          // Red Warning / Error / Destructive
val AppRedDark = Color(0xFF991B1B)      // Dark Red
val AppRedLight = Color(0xFFFEF2F2)     // Light Red Tint

val AppGold = Color(0xFFF59E0B)         // Gold Accent / Highlight
val AppGoldDark = Color(0xFFB45309)     // Dark Gold
val AppGoldLight = Color(0xFFFFFBEB)    // Light Gold Tint

// Backward compatibility aliases strictly mapped to the allowed 5-color palette
val PrimaryBlue = AppBlue
val PrimaryBlueLight = AppBlueLight
val PrimaryBlueDark = AppBlueDark
val NeutralDark = AppBlack
val NeutralLight = AppWhiteOff

val EmeraldGreen = AppBlue
val EmeraldLight = AppBlueLight
val EmeraldDark = AppBlack
val TealAccent = AppBlue
val GoldAccent = AppGold

// Light Mode Palette
val LightBackground = AppWhiteOff
val LightSurface = AppWhite
val LightOnPrimary = AppWhite
val LightOnBackground = AppBlack
val LightOnSurface = AppBlack

// Dark Mode Palette
val DarkBackground = AppBlack
val DarkSurface = Color(0xFF1E293B)     // Dark Card Surface
val DarkOnPrimary = AppWhite
val DarkOnBackground = AppWhiteOff
val DarkOnSurface = AppWhiteOff
val DarkPrimaryContainer = AppBlueDark
val DarkOnPrimaryContainer = AppWhite
