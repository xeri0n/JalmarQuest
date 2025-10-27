package com.jalmarquest.backend.aidirector

import com.jalmarquest.core.model.ChapterEventRequest
import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.ChoiceLogEntry
import com.jalmarquest.core.model.ChoiceTag
import com.jalmarquest.core.model.PlayerNarrativeState
import com.jalmarquest.core.model.QuestLog
import com.jalmarquest.core.model.StatusEffects
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StubAiDirectorClientTest {
    @Test
    fun generatesDeterministicChapterEvent() = runTest {
        val client = StubAiDirectorClient(responseDelayMillis = 0L, idProvider = { "fixed" })
        val request = ChapterEventRequest(
            playerState = PlayerNarrativeState(
                id = "player-1",
                choiceLog = ChoiceLog(listOf(ChoiceLogEntry(ChoiceTag("tag"), 1L))),
                questLog = QuestLog(),
                statusEffects = StatusEffects(emptyList())
            ),
            triggerReason = "unit_test"
        )
        val assembly = GeminiPromptBuilder().assemble(request)

    val response = client.generateChapterEvent(assembly)

        assertEquals("Gemini's Gaze (player-1)", response.worldEventTitle)
        assertTrue(response.snippets.isNotEmpty())
        val snippet = response.snippets.first()
        assertEquals("chapter_fixed", snippet.id)
        val recordedAssembly = client.lastGeneratedAssembly
        assertNotNull(recordedAssembly)
        val systemText = recordedAssembly.payload.systemInstruction.parts.first().text
        assertTrue(systemText.contains("Jalmar Quest"))
        val userText = recordedAssembly.payload.contents.first().parts.first().text
        assertTrue(userText.contains("player_id: player-1"))
    }
}
