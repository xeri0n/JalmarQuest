package com.jalmarquest.core.state.analytics

import com.jalmarquest.core.state.analytics.events.AnalyticsEvent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual class AnalyticsLogger {
    private val json = Json { encodeDefaults = true }

    actual suspend fun log(event: AnalyticsEvent) {
        val payload = runCatching { json.encodeToString(event) }
            .getOrElse { throwable ->
                println("[AnalyticsLogger] Failed to encode event: ${throwable.message}")
                return
            }
        println("[Analytics] $payload")
    }
}
