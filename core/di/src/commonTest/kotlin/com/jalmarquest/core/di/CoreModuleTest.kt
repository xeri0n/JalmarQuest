package com.jalmarquest.core.di

import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.AuthPreferences
import com.jalmarquest.core.model.GuestProfile
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.model.QuestLog
import com.jalmarquest.core.model.SessionToken
import com.jalmarquest.core.model.StatusEffects
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.auth.AuthController
import com.jalmarquest.core.state.auth.GuestAuthGateway
import com.jalmarquest.core.state.auth.GuestAuthResult
import kotlinx.coroutines.CoroutineScope
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class CoreModuleTest {
    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun moduleProvidesGameStateManager() {
        val player = Player(
            id = "test",
            name = "Tester",
            choiceLog = ChoiceLog(emptyList()),
            questLog = QuestLog(),
            statusEffects = StatusEffects(emptyList())
        )

        val gateway = object : GuestAuthGateway {
            override suspend fun provisionGuest(displayName: String?): GuestAuthResult {
                val now = System.currentTimeMillis()
                return GuestAuthResult(
                    profile = GuestProfile(id = "guest", displayName = displayName ?: "Guest", createdAtMillis = now),
                    token = SessionToken(value = "token", issuedAtMillis = now, expiresAtMillis = null),
                    preferences = AuthPreferences()
                )
            }

            override suspend fun restoreGuest(token: SessionToken): GuestAuthResult? = null

            override suspend fun revokeGuestSession(token: SessionToken) {}
        }

        val account = defaultCharacterAccount("test-account")
        val koin = koinApplication { modules(coreModule(player, account, gateway)) }.koin
        val manager = koin.get<GameStateManager>()
        val controller = koin.get<AuthController> { parametersOf(CoroutineScope(EmptyCoroutineContext)) }

        assertNotNull(manager)
        assertNotNull(controller)
    }
}
