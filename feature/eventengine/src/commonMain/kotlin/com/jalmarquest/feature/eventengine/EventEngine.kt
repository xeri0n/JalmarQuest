package com.jalmarquest.feature.eventengine

import com.jalmarquest.core.model.ChapterEventRequest
import com.jalmarquest.core.model.ChapterEventResponse
import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.model.PlayerNarrativeState
import com.jalmarquest.core.state.aidirector.AIDirectorManager
import kotlin.random.Random

/**
 * Entry point for the Hybrid Narrative Engine.
 * 
 * **Alpha 2.2 Integration**: AI Director event pacing to prevent player fatigue.
 * - Monitors eventsSinceRest counter to prevent burnout (max 5 events before rest)
 * - Adjusts chapter event frequency based on player fatigue level
 * - Records events and rest periods for adaptive pacing
 */
interface EventEngine {
    suspend fun evaluateNextEncounter(player: Player): EventResolution
}

sealed interface EventResolution {
    data class Encounter(val snippetId: String) : EventResolution
    data class ChapterEvent(val payload: ChapterEventResponse) : EventResolution
    data object NoEvent : EventResolution
    /** Player needs rest - too many events without break (Alpha 2.2) */
    data object RestNeeded : EventResolution
}

class InMemoryEventEngine(
    private val snippetSelector: SnippetSelector,
    private val chapterEventOdds: Double,
    private val chapterEventProvider: ChapterEventProvider = DefaultChapterEventProvider(),
    private val aiDirectorManager: AIDirectorManager? = null
) : EventEngine {
    init {
        require(chapterEventOdds in 0.0..1.0) { "Chapter event odds must be a probability" }
    }

    override suspend fun evaluateNextEncounter(player: Player): EventResolution {
        // Alpha 2.2: Check player fatigue before generating events
        if (aiDirectorManager?.isPlayerFatigued() == true) {
            return EventResolution.RestNeeded
        }
        
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
            
            // Alpha 2.2: Record event for fatigue tracking
            aiDirectorManager?.recordEvent()
            
            return EventResolution.ChapterEvent(response)
        }

        val snippet = snippetSelector.findMatchingSnippet(player.choiceLog)
        
        // Alpha 2.2: Record snippet event for fatigue tracking
        if (snippet != null) {
            aiDirectorManager?.recordEvent()
        }
        
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
