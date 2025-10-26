package com.jalmarquest.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource as androidPainterResource

@Composable
actual fun painterResource(resourcePath: String): Painter {
    // Map resource path to Android drawable resource ID
    // The R class is generated for the ui.app module
    val resourceId = when (resourcePath) {
        "mainmenu.png", "mainmenu.jpg" -> {
            // Use reflection to get the resource ID from the app's R class
            try {
                val rClass = Class.forName("com.jalmarquest.app.android.R\$drawable")
                val field = rClass.getField("mainmenu")
                field.getInt(null)
            } catch (e: Exception) {
                // Fallback: return a placeholder or throw
                throw IllegalArgumentException("Resource not found: $resourcePath", e)
            }
        }
        else -> throw IllegalArgumentException("Unknown resource: $resourcePath")
    }
    return androidPainterResource(id = resourceId)
}
