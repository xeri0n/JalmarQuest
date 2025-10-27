package com.jalmarquest.core.state.analytics

import android.util.Log
import com.jalmarquest.core.state.analytics.events.AnalyticsEvent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual class AnalyticsLogger {
    private val json = Json { encodeDefaults = true }

    actual suspend fun log(event: AnalyticsEvent) {
        val payload = runCatching { json.encodeToString(event) }
            .getOrElse { throwable ->
                safeLog(LogLevel.WARN, "Failed to encode analytics event", throwable)
                return
            }
        safeLog(LogLevel.INFO, payload)
    }

    private fun safeLog(level: LogLevel, message: String, throwable: Throwable? = null) {
        runCatching {
            when (level) {
                LogLevel.INFO -> Log.i(TAG, message)
                LogLevel.WARN -> Log.w(TAG, message, throwable)
            }
        }.onFailure {
            // Unit tests run on the JVM without Android logging; fall back to stdout.
            if (throwable != null) {
                println("$TAG $message\n${throwable.stackTraceToString()}")
            } else {
                println("$TAG $message")
            }
        }
    }

    private enum class LogLevel { INFO, WARN }

    private companion object {
        const val TAG = "AnalyticsLogger"
    }
}
