package com.jalmarquest.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GuestProfile(
    val id: String,
    val displayName: String,
    @SerialName("created_at_millis") val createdAtMillis: Long
)

@Serializable
data class SessionToken(
    val value: String,
    @SerialName("issued_at_millis") val issuedAtMillis: Long,
    @SerialName("expires_at_millis") val expiresAtMillis: Long? = null
)

@Serializable
data class AuthPreferences(
    @SerialName("analytics_opt_in") val analyticsOptIn: Boolean = true,
    @SerialName("last_locale") val lastLocale: String? = null
)

@Serializable
sealed class AuthState {
    @Serializable
    @SerialName("signed_out")
    data object SignedOut : AuthState()

    @Serializable
    @SerialName("guest")
    data class Guest(
        val profile: GuestProfile,
        @SerialName("session_token") val token: SessionToken,
        val preferences: AuthPreferences
    ) : AuthState()
}
