package com.jalmarquest.backend.aidirector

import com.jalmarquest.core.model.ChapterEventRequest
import com.jalmarquest.core.model.ChapterEventResponse
import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.PlayerNarrativeState
import com.jalmarquest.core.model.QuestLog
import com.jalmarquest.core.model.StatusEffects
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RateLimitingAiDirectorClientTest {
    @Test
    fun `rate limiter delays calls that exceed quota`() = runTest {
        val scheduler = testScheduler
        val dispatchTimes = mutableListOf<Long>()
        val delegate = AiDirectorClient { _ ->
            dispatchTimes += scheduler.currentTime
            ChapterEventResponse(
                worldEventTitle = "test",
                worldEventSummary = "",
                snippets = emptyList()
            )
        }
        val client = RateLimitingAiDirectorClient(
            delegate = delegate,
            config = RateLimitingConfig(maxRequests = 2, intervalMillis = 1_000L),
            clock = { scheduler.currentTime }
        )
        val assembly = dummyAssembly()

        client.generateChapterEvent(assembly)
        client.generateChapterEvent(assembly)
        client.generateChapterEvent(assembly)

        assertEquals(listOf(0L, 0L, 1_000L), dispatchTimes)
        assertTrue(scheduler.currentTime >= 1_000L)
    }

    private fun dummyAssembly(): PromptAssemblyResult {
        val request = ChapterEventRequest(
            playerState = PlayerNarrativeState(
                id = "player",
                choiceLog = ChoiceLog(emptyList()),
                questLog = QuestLog(),
                statusEffects = StatusEffects(emptyList())
            ),
            triggerReason = "unit"
        )
        val builder = GeminiPromptBuilder()
        return builder.assemble(request)
    }
}
