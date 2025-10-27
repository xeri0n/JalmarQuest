package com.jalmarquest.core.state.auth

import com.jalmarquest.core.model.AuthPreferences
import com.jalmarquest.core.model.GuestProfile
import com.jalmarquest.core.model.SessionToken
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlin.random.Random

class InMemoryGuestAuthGateway : GuestAuthGateway {
    private val mutex = Mutex()
    private val sessions = mutableMapOf<String, GuestAuthResult>()

    override suspend fun provisionGuest(displayName: String?): GuestAuthResult = mutex.withLock {
        val now = Clock.System.now().toEpochMilliseconds()
        val guestId = randomId()
        val profile = GuestProfile(
            id = guestId,
            displayName = displayName?.takeIf { it.isNotBlank() } ?: "Guest-${guestId.takeLast(4)}",
            createdAtMillis = now
        )
        val token = SessionToken(
            value = randomId(),
            issuedAtMillis = now,
            expiresAtMillis = null
        )
        val result = GuestAuthResult(profile, token, AuthPreferences())
        sessions[token.value] = result
        result
    }

    override suspend fun restoreGuest(token: SessionToken): GuestAuthResult? = mutex.withLock {
        sessions[token.value]
    }

    override suspend fun revokeGuestSession(token: SessionToken) {
        mutex.withLock { sessions.remove(token.value) }
    }

    private fun randomId(): String = buildString {
        repeat(4) { append(Random.nextInt(0, 16).toString(16)) }
        append('-')
        repeat(12) { append(Random.nextInt(0, 16).toString(16)) }
    }
}
