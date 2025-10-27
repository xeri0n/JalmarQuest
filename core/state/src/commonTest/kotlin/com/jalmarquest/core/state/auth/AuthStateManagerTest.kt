package com.jalmarquest.core.state.auth

import com.jalmarquest.core.model.AuthState
import com.jalmarquest.core.state.analytics.AnalyticsLogger
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AuthStateManagerTest {
    private val analyticsLogger = AnalyticsLogger()

    @BeforeTest
    fun setUp() {
        AuthStateManager.currentTimeMillisProvider = { FAKE_NOW }
    }

    @AfterTest
    fun tearDown() {
        AuthStateManager.currentTimeMillisProvider = { Clock.System.now().toEpochMilliseconds() }
    }

    @Test
    fun signInAsGuestPopulatesAuthState() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val manager = AuthStateManager(
            tokenStorage = AuthTokenStorage(),
            guestGateway = InMemoryGuestAuthGateway(),
            analyticsLogger = analyticsLogger,
            dispatcher = dispatcher
        )

        manager.signInAsGuest("Jalmar")

        val state = manager.authState.value
        assertIs<AuthState.Guest>(state)
        assertEquals("Jalmar", state.profile.displayName)
    }

    @Test
    fun bootstrapRestoresStoredToken() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val gateway = InMemoryGuestAuthGateway()
        val storage = AuthTokenStorage()
        val manager = AuthStateManager(storage, gateway, analyticsLogger, dispatcher)

        manager.signInAsGuest("Quester")
        val token = (manager.authState.value as AuthState.Guest).token

        val freshManager = AuthStateManager(storage, gateway, analyticsLogger, dispatcher)
        freshManager.bootstrap()

        val restored = freshManager.authState.value
        assertIs<AuthState.Guest>(restored)
        assertEquals(token.value, restored.token.value)
    }

    private companion object {
        const val FAKE_NOW = 1700000000000L
    }
}
