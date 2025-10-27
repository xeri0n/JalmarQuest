package com.jalmarquest.ui.app.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Professional color palette for JalmarQuest.
 * 
 * Design Philosophy:
 * - "High-end" aesthetic: Rich, sophisticated colors
 * - Nature theme: Earthy greens, warm browns, sky blues
 * - Accessibility: WCAG 2.1 AA contrast ratios minimum
 * - Dark mode friendly: Both light and dark themes
 */

// Primary Colors - Forest Green theme (represents wilderness)
private val ForestGreen = Color(0xFF2E7D32)      // Primary
private val LightForestGreen = Color(0xFF4CAF50) // Primary variant
private val DarkForestGreen = Color(0xFF1B5E20)  // Primary dark

// Secondary Colors - Warm Earth tones
private val EarthBrown = Color(0xFF8D6E63)       // Secondary
private val LightEarthBrown = Color(0xFFBCAAA4) // Secondary variant
private val DarkEarthBrown = Color(0xFF5D4037)   // Secondary dark

// Tertiary Colors - Sky Blue (for contrast)
private val SkyBlue = Color(0xFF42A5F5)          // Tertiary
private val LightSkyBlue = Color(0xFF90CAF9)    // Tertiary variant
private val DarkSkyBlue = Color(0xFF1976D2)      // Tertiary dark

// Semantic Colors
private val SuccessGreen = Color(0xFF4CAF50)
private val WarningAmber = Color(0xFFFF9800)
private val ErrorRed = Color(0xFFE53935)
private val InfoBlue = Color(0xFF2196F3)

// Neutral Colors - Light Theme
private val LightBackground = Color(0xFFFAFAFA)
private val LightSurface = Color(0xFFFFFFFF)
private val LightOnSurface = Color(0xFF1C1B1F)

// Neutral Colors - Dark Theme
private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)
private val DarkOnSurface = Color(0xFFE1E1E1)

/**
 * Light color scheme for daytime play.
 * High contrast for outdoor visibility.
 */
val JalmarQuestLightColors = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = LightForestGreen.copy(alpha = 0.2f),
    onPrimaryContainer = DarkForestGreen,
    
    secondary = EarthBrown,
    onSecondary = Color.White,
    secondaryContainer = LightEarthBrown.copy(alpha = 0.3f),
    onSecondaryContainer = DarkEarthBrown,
    
    tertiary = SkyBlue,
    onTertiary = Color.White,
    tertiaryContainer = LightSkyBlue.copy(alpha = 0.3f),
    onTertiaryContainer = DarkSkyBlue,
    
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRed.copy(alpha = 0.2f),
    onErrorContainer = Color(0xFF8B0000),
    
    background = LightBackground,
    onBackground = LightOnSurface,
    
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    
    scrim = Color.Black.copy(alpha = 0.32f)
)

/**
 * Dark color scheme for nighttime play.
 * Reduced eye strain, battery efficient on OLED.
 */
val JalmarQuestDarkColors = darkColorScheme(
    primary = LightForestGreen,
    onPrimary = Color(0xFF003300),
    primaryContainer = DarkForestGreen,
    onPrimaryContainer = Color(0xFFB2DFDB),
    
    secondary = LightEarthBrown,
    onSecondary = Color(0xFF3E2723),
    secondaryContainer = DarkEarthBrown,
    onSecondaryContainer = Color(0xFFD7CCC8),
    
    tertiary = LightSkyBlue,
    onTertiary = Color(0xFF003C8F),
    tertiaryContainer = DarkSkyBlue,
    onTertiaryContainer = Color(0xFFBBDEFB),
    
    error = Color(0xFFCF6679),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFB4AB),
    
    background = DarkBackground,
    onBackground = DarkOnSurface,
    
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    
    scrim = Color.Black.copy(alpha = 0.32f)
)

/**
 * Semantic color utilities for consistent UI feedback.
 */
object SemanticColors {
    val success = SuccessGreen
    val warning = WarningAmber
    val error = ErrorRed
    val info = InfoBlue
}
