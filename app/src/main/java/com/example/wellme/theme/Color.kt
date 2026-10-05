package com.example.wellme.theme

import androidx.compose.ui.graphics.Color

// Minimalist Modern Palette (Clean Professional Blue & Slate)
val PrimaryBlue = Color(0xFF2563EB)           // Modern clean blue accent
val PrimaryBlueLight = Color(0xFFEFF6FF)       // Soft blue tint
val PrimaryBlueDark = Color(0xFF1E40AF)        // Deep blue
val NeutralDark = Color(0xFF0F172A)            // Slate 900
val NeutralLight = Color(0xFFF8FAFC)           // Slate 50

// Backward compatibility aliases mapping old green/neumorphic colors to minimalist palette
val EmeraldGreen = PrimaryBlue
val EmeraldLight = PrimaryBlueLight
val EmeraldDark = NeutralDark
val TealAccent = Color(0xFF0EA5E9)             // Sky blue
val GoldAccent = Color(0xFFD97706)             // Amber

// Clean Minimalist Light Theme
val LightBackground = Color(0xFFF8FAFC)        // Crisp off-white / light slate
val LightSurface = Color(0xFFFFFFFF)           // Pure White
val LightOnPrimary = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xFF0F172A)
val LightOnSurface = Color(0xFF0F172A)

// Clean Minimalist Dark Theme
val DarkBackground = Color(0xFF0F172A)         // Slate 900
val DarkSurface = Color(0xFF1E293B)            // Slate 800
val DarkOnPrimary = Color(0xFFFFFFFF)
val DarkOnBackground = Color(0xFFF8FAFC)
val DarkOnSurface = Color(0xFFF8FAFC)
val DarkPrimaryContainer = Color(0xFF1E40AF)
val DarkOnPrimaryContainer = Color(0xFFEFF6FF)

// Neumorphic legacy fallback colors mapped to clean minimalist values
val NeumorphicLightBg = Color(0xFFFFFFFF)
val NeumorphicLightTopShadow = Color(0xFFFFFFFF)
val NeumorphicLightBottomShadow = Color(0xFFE2E8F0)
val NeumorphicDarkBg = Color(0xFF1E293B)
val NeumorphicDarkTopShadow = Color(0xFF1E293B)
val NeumorphicDarkBottomShadow = Color(0xFF0F172A)
