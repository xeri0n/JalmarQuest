package com.jalmarquest.ui.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * JalmarQuest app theme with professional color scheme and typography.
 * 
 * Supports both light and dark modes with automatic system detection.
 * Optimized for mobile-first design with accessible touch targets.
 */
@Composable
fun JalmarQuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        JalmarQuestDarkColors
    } else {
        JalmarQuestLightColors
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = JalmarQuestTypography,
        content = content
    )
}
