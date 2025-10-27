package com.jalmarquest.backend.database

import kotlinx.coroutines.runBlocking
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteIfExists
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalPathApi::class)
class JsonGuestAuthGatewayTest {
    private val tempDir = createTempDirectory(prefix = "guest-auth-")
    private val storagePath = tempDir.resolve("sessions.json")

    @BeforeTest
    fun cleanupBefore() {
        storagePath.deleteIfExists()
    }

    @AfterTest
    fun cleanupAfter() {
        storagePath.deleteIfExists()
        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun provisionRestoreAndRevokeGuest() = runBlocking {
        val gateway = JsonGuestAuthGateway(storagePath)

        val result = gateway.provisionGuest("Test Quail")
        assertEquals("Test Quail", result.profile.displayName)

        val restored = gateway.restoreGuest(result.token)
        assertNotNull(restored)
        assertEquals(result.token.value, restored.token.value)

        gateway.revokeGuestSession(result.token)
        val afterRevoke = gateway.restoreGuest(result.token)
        assertNull(afterRevoke)
    }

    @Test
    fun persistsSessionsAcrossInstances() = runBlocking {
        val gateway = JsonGuestAuthGateway(storagePath)
        val result = gateway.provisionGuest("Persistent Quail")
        val token = result.token

        val restored = JsonGuestAuthGateway(storagePath).restoreGuest(token)

        assertNotNull(restored)
        assertEquals(token.value, restored.token.value)
        assertEquals("Persistent Quail", restored.profile.displayName)
    }
}
