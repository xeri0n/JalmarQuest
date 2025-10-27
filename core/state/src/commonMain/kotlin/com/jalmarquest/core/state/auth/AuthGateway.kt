package com.jalmarquest.core.state.auth

import com.jalmarquest.core.model.AuthPreferences
import com.jalmarquest.core.model.GuestProfile
import com.jalmarquest.core.model.SessionToken

interface GuestAuthGateway {
    suspend fun provisionGuest(displayName: String?): GuestAuthResult
    suspend fun restoreGuest(token: SessionToken): GuestAuthResult?
    suspend fun revokeGuestSession(token: SessionToken)
}

data class GuestAuthResult(
    val profile: GuestProfile,
    val token: SessionToken,
    val preferences: AuthPreferences
)
