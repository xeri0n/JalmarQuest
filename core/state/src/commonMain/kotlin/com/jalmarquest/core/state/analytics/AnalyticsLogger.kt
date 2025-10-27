package com.jalmarquest.core.state.analytics

import com.jalmarquest.core.state.analytics.events.AnalyticsEvent

expect class AnalyticsLogger() {
    suspend fun log(event: AnalyticsEvent)
}
