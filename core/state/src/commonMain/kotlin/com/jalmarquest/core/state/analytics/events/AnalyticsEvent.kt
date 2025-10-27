package com.jalmarquest.core.state.analytics.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class AnalyticsEvent {
    abstract val timestampMillis: Long

    @Serializable
    @SerialName("guest_session_created")
    data class GuestSessionCreated(
        @SerialName("timestamp_millis") override val timestampMillis: Long,
        @SerialName("guest_id") val guestId: String,
        @SerialName("display_name") val displayName: String?
    ) : AnalyticsEvent()

    @Serializable
    @SerialName("session_restored")
    data class SessionRestored(
        @SerialName("timestamp_millis") override val timestampMillis: Long,
        @SerialName("guest_id") val guestId: String
    ) : AnalyticsEvent()

    @Serializable
    @SerialName("session_terminated")
    data class SessionTerminated(
        @SerialName("timestamp_millis") override val timestampMillis: Long,
        val reason: String
    ) : AnalyticsEvent()

    @Serializable
    @SerialName("auth_storage_failure")
    data class AuthStorageFailure(
        @SerialName("timestamp_millis") override val timestampMillis: Long,
        val reason: String,
        val message: String?
    ) : AnalyticsEvent()

    @Serializable
    @SerialName("narrative_generated")
    data class NarrativeGenerated(
        @SerialName("timestamp_millis") override val timestampMillis: Long,
        @SerialName("player_id") val playerId: String,
        @SerialName("mode") val mode: String,
        @SerialName("token_last_four") val tokenLastFour: String?
    ) : AnalyticsEvent()

    @Serializable
    @SerialName("narrative_generation_failed")
    data class NarrativeGenerationFailed(
        @SerialName("timestamp_millis") override val timestampMillis: Long,
        val reason: String,
        @SerialName("mode") val mode: String
    ) : AnalyticsEvent()
}
