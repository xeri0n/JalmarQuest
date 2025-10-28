package com.jalmarquest.feature.eventengine

import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.ChapterEventResponse
import com.jalmarquest.core.model.ChoiceLogEntry
import com.jalmarquest.core.model.ChoiceTag
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.model.QuestLog
import com.jalmarquest.core.model.StatusEffects
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.jalmarquest.feature.eventengine.ChapterEventProvider

private class FixedSelector(
    private val snippetId: String? = null,
    private val forceChapterEvent: Boolean = false
) : SnippetSelector {
    override fun findMatchingSnippet(choiceLog: ChoiceLog): String? = snippetId
    override fun shouldTriggerChapterEvent(odds: Double): Boolean = forceChapterEvent
}

class InMemoryEventEngineTest {
    private val basePlayer = Player(
        id = "test",
        name = "Tester",
        choiceLog = ChoiceLog(listOf(ChoiceLogEntry(ChoiceTag("tag"), 10L))),
        questLog = QuestLog(),
        statusEffects = StatusEffects(emptyList())
    )

    @Test
    fun returnsEncounterWhenSnippetAvailable() = runTest {
        val engine = InMemoryEventEngine(FixedSelector(snippetId = "snippet-1"), chapterEventOdds = 0.0)
        val result = engine.evaluateNextEncounter(basePlayer)
        assertEquals(EventResolution.Encounter("snippet-1"), result)
    }

    @Test
    fun returnsChapterEventWhenForced() = runTest {
        val engine = InMemoryEventEngine(
            snippetSelector = FixedSelector(forceChapterEvent = true),
            chapterEventOdds = 1.0,
            chapterEventProvider = ChapterEventProvider { request ->
                ChapterEventResponse(
                    worldEventTitle = "Test Event",
                    worldEventSummary = "Summary",
                    snippets = emptyList()
                )
            }
        )
        val result = engine.evaluateNextEncounter(basePlayer)
        check(result is EventResolution.ChapterEvent)
        assertEquals("Test Event", result.payload.worldEventTitle)
    }
}
