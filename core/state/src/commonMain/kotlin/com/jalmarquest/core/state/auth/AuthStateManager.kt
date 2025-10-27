package com.jalmarquest.core.state.auth

import com.jalmarquest.core.model.AuthState
import com.jalmarquest.core.model.AuthState.Guest
import com.jalmarquest.core.model.AuthState.SignedOut
import com.jalmarquest.core.model.SessionToken
import com.jalmarquest.core.state.analytics.AnalyticsLogger
import com.jalmarquest.core.state.analytics.events.AnalyticsEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.datetime.Clock

class AuthStateManager(
    private val tokenStorage: AuthTokenStorage,
    private val guestGateway: GuestAuthGateway,
    private val analyticsLogger: AnalyticsLogger,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val _authState = MutableStateFlow<AuthState>(SignedOut)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    suspend fun bootstrap() {
        val stored = runCatching { tokenStorage.loadToken() }.getOrElse { throwable ->
            if (throwable !is CancellationException) {
                analyticsLogger.log(
                    AnalyticsEvent.AuthStorageFailure(
                        timestampMillis = currentTimeMillis(),
                        reason = "load_failed",
                        message = throwable.message
                    )
                )
            }
            null
        } ?: return

        restoreSession(stored)
    }

    suspend fun signInAsGuest(displayName: String? = null) {
        withContext(dispatcher) {
            val result = guestGateway.provisionGuest(displayName)
            tokenStorage.saveToken(result.token)
            updateState(Guest(result.profile, result.token, result.preferences))
            analyticsLogger.log(
                AnalyticsEvent.GuestSessionCreated(
                    timestampMillis = currentTimeMillis(),
                    guestId = result.profile.id,
                    displayName = result.profile.displayName
                )
            )
        }
    }

    suspend fun signOut() {
        val current = authState.value
        if (current is Guest) {
            runCatching { guestGateway.revokeGuestSession(current.token) }
                .onFailure { throwable ->
                    if (throwable !is CancellationException) {
                        analyticsLogger.log(
                            AnalyticsEvent.AuthStorageFailure(
                                timestampMillis = currentTimeMillis(),
                                reason = "revoke_failed",
                                message = throwable.message
                            )
                        )
                    }
                }
        }
        runCatching { tokenStorage.saveToken(null) }
        updateState(SignedOut)
        analyticsLogger.log(
            AnalyticsEvent.SessionTerminated(
                timestampMillis = currentTimeMillis(),
                reason = "user_sign_out"
            )
        )
    }

    private suspend fun restoreSession(token: SessionToken) {
        val result = runCatching { guestGateway.restoreGuest(token) }.getOrElse { throwable ->
            handleGatewayFailure("restore_failed", throwable)
            null
        } ?: run {
            tokenStorage.saveToken(null)
            return
        }

        updateState(Guest(result.profile, result.token, result.preferences))
        analyticsLogger.log(
            AnalyticsEvent.SessionRestored(
                timestampMillis = currentTimeMillis(),
                guestId = result.profile.id
            )
        )
    }

    private suspend fun handleGatewayFailure(reason: String, throwable: Throwable) {
        if (throwable is CancellationException) throw throwable
        analyticsLogger.log(
            AnalyticsEvent.AuthStorageFailure(
                timestampMillis = currentTimeMillis(),
                reason = reason,
                message = throwable.message
            )
        )
    }

    private fun updateState(newState: AuthState) {
        _authState.value = newState
    }

    private fun currentTimeMillis(): Long = currentTimeMillisProvider()

    companion object {
        internal var currentTimeMillisProvider: () -> Long = { Clock.System.now().toEpochMilliseconds() }
    }
}
