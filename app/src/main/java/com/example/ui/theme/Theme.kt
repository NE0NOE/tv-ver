package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// Apex Signage 10-Foot Dark Tonal Color Scheme
// Designed for sustained 1080p TV operation with WCAG AAA contrast & Android 6 GPU efficiency
private val ApexSignageColorScheme = darkColorScheme(
    primary = ApexPrimaryMauve,
    onPrimary = ApexTextPrimary,
    primaryContainer = ApexPrimaryContainer,
    onPrimaryContainer = ApexOnPrimaryContainer,
    inversePrimary = ApexPrimaryHighLum,
    secondary = ApexSecondaryRoseSlate,
    onSecondary = ApexTextPrimary,
    secondaryContainer = ApexSecondaryContainer,
    onSecondaryContainer = ApexOnSecondaryContainer,
    tertiary = ApexTertiaryLive,
    onTertiary = ApexTextPrimary,
    tertiaryContainer = ApexTertiaryContainer,
    onTertiaryContainer = ApexOnTertiaryContainer,
    background = ApexSurface,
    onBackground = ApexOnSurface,
    surface = ApexSurface,
    onSurface = ApexOnSurface,
    surfaceVariant = ApexSurfaceVariant,
    onSurfaceVariant = ApexOnSurfaceVariant,
    surfaceTint = ApexPrimaryHighLum,
    inverseSurface = ApexOnSurface,
    inverseOnSurface = ApexSurface,
    error = ApexError,
    onError = ApexOnError,
    errorContainer = ApexErrorContainer,
    onErrorContainer = ApexOnErrorContainer,
    outline = ApexOutline,
    outlineVariant = ApexOutlineVariant,
    scrim = ApexModalScrim,
    surfaceBright = ApexSurfaceBright,
    surfaceDim = ApexSurfaceDim,
    surfaceContainer = ApexSurfaceContainer,
    surfaceContainerHigh = ApexSurfaceContainerHigh,
    surfaceContainerHighest = ApexSurfaceContainerHighest,
    surfaceContainerLow = ApexSurfaceContainerLow,
    surfaceContainerLowest = ApexSurfaceContainerLowest
)

// Pill-shaped / Soft Rounded geometric standard from spec:
// sm: 0.5rem (8dp), DEFAULT: 1rem (16dp), md: 1.5rem (24dp), lg: 2rem (32dp), xl: 3rem (48dp)
val ApexShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(48.dp)
)

@Composable
fun ApexSignageTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ApexSignageColorScheme,
        typography = ApexTypography,
        shapes = ApexShapes,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ApexSignageTheme(content = content)
}
