package com.jalmarquest.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource as desktopPainterResource

@Composable
actual fun painterResource(resourcePath: String): Painter {
    // For desktop, load from resources
    return desktopPainterResource(resourcePath)
}
