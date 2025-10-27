package com.jalmarquest.core.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals

class LoreSnippetSerializationTest {
    private val json = Json { encodeDefaults = true }

    @Test
    fun roundTripSerializationMaintainsStructure() {
        val snippet = LoreSnippet(
            id = "test-snippet",
            eventText = "Test event text",
            choiceOptions = listOf("Option A", "Option B"),
            consequences = buildJsonObject { put("test", "value") },
            conditions = JsonObject(emptyMap())
        )

        val encoded = json.encodeToString(snippet)
        val decoded = json.decodeFromString<LoreSnippet>(encoded)

        assertEquals(snippet, decoded)
    }
}
