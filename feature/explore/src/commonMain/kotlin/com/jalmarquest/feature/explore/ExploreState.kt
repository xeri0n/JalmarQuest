package com.jalmarquest.feature.explore

import com.jalmarquest.core.model.ChapterEventResponse
import com.jalmarquest.core.model.LoreSnippet

/**
 * UI-facing state emitted by [ExploreStateMachine].
 */
data class ExploreState(
    val phase: ExplorePhase = ExplorePhase.Idle,
    val history: List<ExploreHistoryEntry> = emptyList()
)

sealed class ExplorePhase {
    data object Idle : ExplorePhase()
    data object Loading : ExplorePhase()
    data class Encounter(val snippet: LoreSnippet) : ExplorePhase()
    data class Chapter(val response: ChapterEventResponse) : ExplorePhase()
    data class Resolution(val summary: ResolutionSummary) : ExplorePhase()
    data class Error(val message: String) : ExplorePhase()
    /** Alpha 2.2: Player needs rest due to event fatigue */
    data class RestNeeded(val eventsSinceRest: Int) : ExplorePhase()
}

data class ResolutionSummary(
    val snippetId: String,
    val title: String,
    val choiceText: String?,
    val rewardSummaries: List<String>,
    val autosaveTag: String,
    val timestampMillis: Long
)

data class ExploreHistoryEntry(
    val snippetId: String,
    val title: String,
    val narratedSummary: String,
    val choiceSummary: String?,
    val rewardSummaries: List<String>,
    val autosaveTag: String,
    val timestampMillis: Long
)
