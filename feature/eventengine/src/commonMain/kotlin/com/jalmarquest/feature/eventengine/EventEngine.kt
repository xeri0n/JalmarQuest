package com.jalmarquest.feature.eventengine

import com.jalmarquest.core.model.ChapterEventRequest
import com.jalmarquest.core.model.ChapterEventResponse
import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.model.PlayerNarrativeState
import kotlin.random.Random

/**
 * Entry point for the Hybrid Narrative Engine. Later milestones will extend this interface.
 */
interface EventEngine {
    fun evaluateNextEncounter(player: Player): EventResolution
}

sealed interface EventResolution {
    data class Encounter(val snippetId: String) : EventResolution
    data class ChapterEvent(val payload: ChapterEventResponse) : EventResolution
    data object NoEvent : EventResolution
}

class InMemoryEventEngine(
    private val snippetSelector: SnippetSelector,
    private val chapterEventOdds: Double,
    private val chapterEventProvider: ChapterEventProvider = DefaultChapterEventProvider()
) : EventEngine {
    init {
        require(chapterEventOdds in 0.0..1.0) { "Chapter event odds must be a probability" }
    }

    override fun evaluateNextEncounter(player: Player): EventResolution {
        val chapterEvent = snippetSelector.shouldTriggerChapterEvent(chapterEventOdds)
        if (chapterEvent) {
            val request = ChapterEventRequest(
                playerState = PlayerNarrativeState(
                    id = player.id,
                    choiceLog = player.choiceLog,
                    questLog = player.questLog,
                    statusEffects = player.statusEffects
                ),
                triggerReason = "probability_threshold_met"
            )
            val response = chapterEventProvider.generateChapterEvent(request)
            return EventResolution.ChapterEvent(response)
        }

        val snippet = snippetSelector.findMatchingSnippet(player.choiceLog)
        return snippet?.let { EventResolution.Encounter(it) } ?: EventResolution.NoEvent
    }
}

fun interface SnippetSelector {
    fun findMatchingSnippet(choiceLog: ChoiceLog): String?
    fun shouldTriggerChapterEvent(odds: Double): Boolean {
        return odds > 0.0 && snippetRandom() < odds
    }
}

internal fun snippetRandom(): Double = Random.nextDouble()
