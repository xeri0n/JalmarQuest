package com.jalmarquest.core.state.auth

import com.jalmarquest.core.model.SessionToken
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthTokenStorageTest {
    @AfterTest
    fun tearDown() {
        AuthTokenStorageHooks.override(null)
        AuthTokenStorageHooks.clearDefault()
    }

    @Test
    fun savesAndLoadsToken() = runTest {
        val driver = RecordingTokenDriver()
        AuthTokenStorageHooks.override(driver)

        val storage = AuthTokenStorage()
        val token = token("alpha")

        storage.saveToken(token)

        assertEquals(token, storage.loadToken())
        assertEquals(token, driver.snapshot)
    }

    @Test
    fun clearingTokenRemovesFromDriver() = runTest {
        val driver = RecordingTokenDriver()
        AuthTokenStorageHooks.override(driver)

        val storage = AuthTokenStorage()
        storage.saveToken(token("beta"))
        storage.saveToken(null)

        val freshStorage = AuthTokenStorage()
        assertNull(freshStorage.loadToken())
        assertNull(driver.snapshot)
    }

    @Test
    fun loadTokenIgnoresDriverFailures() = runTest {
        AuthTokenStorageHooks.override(object : TokenPersistenceDriver {
            override suspend fun readToken(): SessionToken? {
                error("corrupted token")
            }

            override suspend fun writeToken(token: SessionToken?) = Unit
        })

        val storage = AuthTokenStorage()
        assertNull(storage.loadToken())
    }

    @Test
    fun withDriverRestoresPreviousOverride() = runTest {
        val original = RecordingTokenDriver()
        val temp = RecordingTokenDriver()
        AuthTokenStorageHooks.override(original)

        AuthTokenStorageHooks.withDriver(temp) {
            assertEquals(temp, AuthTokenStorageHooks.currentDriver())
        }

        assertEquals(original, AuthTokenStorageHooks.currentDriver())
    }

    private fun token(suffix: String) = SessionToken(
        value = "token-$suffix",
        issuedAtMillis = 42L,
        expiresAtMillis = null
    )

    private class RecordingTokenDriver : TokenPersistenceDriver {
        var snapshot: SessionToken? = null

        override suspend fun readToken(): SessionToken? = snapshot

        override suspend fun writeToken(token: SessionToken?) {
            snapshot = token
        }
    }
}
