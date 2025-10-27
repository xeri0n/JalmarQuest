package com.jalmarquest.app.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jalmarquest.core.di.defaultPlayer
import com.jalmarquest.core.di.defaultCharacterAccount
import com.jalmarquest.core.di.initKoin
import com.jalmarquest.core.di.resolveCrashReporter
import com.jalmarquest.ui.app.JalmarQuestApp
import com.jalmarquest.backend.database.JsonGuestAuthGateway
import com.jalmarquest.core.state.perf.PerformanceLogger
import java.nio.file.Path

fun main() {
    val guestGateway = JsonGuestAuthGateway(desktopSessionPath())
    initKoin(
        initialPlayer = defaultPlayer(),
        initialCharacterAccount = defaultCharacterAccount(),
        guestGateway = guestGateway
    )

    // Install crash reporting early
    val crashReporter = resolveCrashReporter()
    crashReporter.setCustomKey("platform", "Desktop")
    crashReporter.setCustomKey("app_version", "0.1.0-vertical-slice")
    crashReporter.installExceptionHandlers()

    // Log startup completion
    PerformanceLogger.logStartupComplete()

    application {
        Window(onCloseRequest = ::exitApplication, title = "Jalmar Quest") {
            JalmarQuestApp()
        }
    }
}

private fun desktopSessionPath(): Path {
    val home = System.getProperty("user.home")
    return Path.of(home, ".jalmarquest", "guest_sessions.json")
}
