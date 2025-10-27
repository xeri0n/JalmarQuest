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
class AiDirectorServiceTest {
    @Test
    fun `service routes between sandbox and live clients and enforces throttling`() = runTest {
        val scheduler = testScheduler
        val observerCalls = mutableListOf<AiDirectorMode>()
        val liveClient = RecordingClient("Live Title", scheduler)
        val sandboxClient = RecordingClient("Sandbox Title", scheduler)
        val service = AiDirectorService(
            promptBuilder = GeminiPromptBuilder(),
            liveClient = liveClient,
            sandboxClient = sandboxClient,
            rateLimitingConfig = RateLimitingConfig(maxRequests = 1, intervalMillis = 1_000L),
            clock = { scheduler.currentTime },
            observer = PromptDispatchObserver { _, _, mode -> observerCalls += mode }
        )
        val request = request()

        val sandboxResponse = service.generateChapterEvent(request, AiDirectorMode.Sandbox)
        assertEquals("Sandbox Title", sandboxResponse.worldEventTitle)
        assertEquals(1, sandboxClient.invocations)
        assertEquals(0, liveClient.invocations)
        assertEquals(listOf(AiDirectorMode.Sandbox), observerCalls)

        val liveResponse = service.generateChapterEvent(request, AiDirectorMode.Live)
        assertEquals("Live Title", liveResponse.worldEventTitle)
        assertEquals(1, liveClient.invocations)
        assertTrue(scheduler.currentTime == 0L)

        val secondLiveResponse = service.generateChapterEvent(request, AiDirectorMode.Live)
        assertEquals("Live Title", secondLiveResponse.worldEventTitle)
        assertEquals(2, liveClient.invocations)
        assertTrue(scheduler.currentTime >= 1_000L)
        assertEquals(listOf(AiDirectorMode.Sandbox, AiDirectorMode.Live, AiDirectorMode.Live), observerCalls)
    }

    private fun request(): ChapterEventRequest = ChapterEventRequest(
        playerState = PlayerNarrativeState(
            id = "player",
            choiceLog = ChoiceLog(emptyList()),
            questLog = QuestLog(),
            statusEffects = StatusEffects(emptyList())
        ),
        triggerReason = "unit"
    )

    private class RecordingClient(
        private val title: String,
        private val scheduler: kotlinx.coroutines.test.TestCoroutineScheduler
    ) : AiDirectorClient {
        var invocations: Int = 0
            private set
        val dispatchTimes = mutableListOf<Long>()

        override suspend fun generateChapterEvent(assembly: PromptAssemblyResult): ChapterEventResponse {
            invocations += 1
            dispatchTimes += scheduler.currentTime
            return ChapterEventResponse(
                worldEventTitle = title,
                worldEventSummary = "",
                snippets = emptyList()
            )
        }
    }
}
