package com.jalmarquest.backend.aidirector

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SandboxFixtureLoaderTest {
    @Test
    fun `loads sandbox fixtures from default resource`() {
        val document = SandboxFixtureLoader().load()

        assertTrue(document.fixtures.isNotEmpty())
        val fixture = document.fixtures.first()
        assertEquals("buttonburgh_market_intro", fixture.id)
        assertTrue(fixture.response.snippets.first().choiceOptions.size == 3)
        assertEquals("sandbox", document.metadata.defaultMode)
    }

    @Test
    fun `throws descriptive error when resource missing`() {
        val loader = SandboxFixtureLoader(resourcePath = "missing.json") { null }

        assertFailsWith<IllegalStateException> { loader.load() }
    }
}
