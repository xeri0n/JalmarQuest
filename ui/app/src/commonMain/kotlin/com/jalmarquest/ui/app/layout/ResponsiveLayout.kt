package com.jalmarquest.ui.app.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Responsive layout system for mobile/tablet/desktop breakpoints.
 * 
 * Provides standardized spacing, touch targets, and layout decisions
 * based on screen size and density.
 * 
 * Accessibility Guidelines:
 * - Minimum touch target: 48dp x 48dp (WCAG 2.5.5 Level AAA)
 * - Recommended spacing between targets: 8dp minimum
 * - Text minimum size: 16sp for body, 14sp for captions
 */

/**
 * Screen size breakpoints following Material Design guidelines.
 */
enum class ScreenSize {
    COMPACT,   // Phone portrait: < 600dp width
    MEDIUM,    // Phone landscape, small tablet: 600dp - 839dp
    EXPANDED;  // Large tablet, desktop: >= 840dp
    
    companion object {
        fun fromWidth(widthDp: Dp): ScreenSize = when {
            widthDp < 600.dp -> COMPACT
            widthDp < 840.dp -> MEDIUM
            else -> EXPANDED
        }
    }
}

/**
 * Standardized spacing system based on 4dp grid.
 */
object AppSpacing {
    val tiny = 4.dp
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val extraLarge = 32.dp
    val huge = 48.dp
    
    /**
     * Minimum touch target size per WCAG 2.5.5 Level AAA.
     */
    val minTouchTarget = 48.dp
    
    /**
     * Recommended spacing between interactive elements.
     */
    val interactivePadding = 8.dp
}

/**
 * Layout configuration that adapts to screen size.
 */
data class ResponsiveLayoutConfig(
    val screenSize: ScreenSize,
    val contentPadding: Dp,
    val cardSpacing: Dp,
    val buttonHeight: Dp,
    val minimumTouchTarget: Dp,
    val maxContentWidth: Dp
) {
    companion object {
        fun forScreenSize(screenSize: ScreenSize): ResponsiveLayoutConfig = when (screenSize) {
            ScreenSize.COMPACT -> ResponsiveLayoutConfig(
                screenSize = ScreenSize.COMPACT,
                contentPadding = AppSpacing.medium,
                cardSpacing = AppSpacing.small,
                buttonHeight = AppSpacing.minTouchTarget,
                minimumTouchTarget = AppSpacing.minTouchTarget,
                maxContentWidth = Dp.Infinity
            )
            
            ScreenSize.MEDIUM -> ResponsiveLayoutConfig(
                screenSize = ScreenSize.MEDIUM,
                contentPadding = AppSpacing.large,
                cardSpacing = AppSpacing.medium,
                buttonHeight = 52.dp,
                minimumTouchTarget = AppSpacing.minTouchTarget,
                maxContentWidth = 720.dp
            )
            
            ScreenSize.EXPANDED -> ResponsiveLayoutConfig(
                screenSize = ScreenSize.EXPANDED,
                contentPadding = AppSpacing.extraLarge,
                cardSpacing = AppSpacing.large,
                buttonHeight = 56.dp,
                minimumTouchTarget = 44.dp, // Desktop can be slightly smaller
                maxContentWidth = 1200.dp
            )
        }
    }
}

/**
 * Composable function to get current screen size.
 */
@Composable
fun rememberScreenSize(): ScreenSize {
    val density = LocalDensity.current
    // Note: In real implementation, we'd use BoxWithConstraints or WindowSizeClass
    // For now, defaulting to COMPACT (mobile-first)
    return ScreenSize.COMPACT
}

/**
 * Composable function to get responsive layout configuration.
 */
@Composable
fun rememberResponsiveLayoutConfig(): ResponsiveLayoutConfig {
    val screenSize = rememberScreenSize()
    return ResponsiveLayoutConfig.forScreenSize(screenSize)
}

/**
 * Utility to calculate adaptive touch target size.
 * Ensures minimum 48dp on mobile, can be smaller on desktop.
 */
fun calculateTouchTargetSize(
    baseSize: Dp,
    screenSize: ScreenSize
): Dp {
    val minSize = when (screenSize) {
        ScreenSize.COMPACT -> 48.dp
        ScreenSize.MEDIUM -> 48.dp
        ScreenSize.EXPANDED -> 44.dp
    }
    return maxOf(baseSize, minSize)
}
