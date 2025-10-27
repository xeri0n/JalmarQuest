package com.jalmarquest.core.state.analytics

import com.jalmarquest.core.state.analytics.events.AnalyticsEvent

actual class AnalyticsLogger {
    actual suspend fun log(event: AnalyticsEvent) {
        println("[Analytics][iOS TODO] $event")
    }
}
