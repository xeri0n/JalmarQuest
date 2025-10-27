package com.jalmarquest.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.Color

@Composable
actual fun painterResource(resourcePath: String): Painter {
    // iOS implementation - placeholder
    return ColorPainter(Color.Black)
}
