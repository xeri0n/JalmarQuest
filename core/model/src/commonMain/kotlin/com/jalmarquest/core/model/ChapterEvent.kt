package com.jalmarquest.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerNarrativeState(
    val id: String,
    @SerialName("choice_log") val choiceLog: ChoiceLog,
    @SerialName("quest_log") val questLog: QuestLog,
    @SerialName("status_effects") val statusEffects: StatusEffects
)

@Serializable
data class ChapterEventRequest(
    @SerialName("player_state") val playerState: PlayerNarrativeState,
    @SerialName("trigger_reason") val triggerReason: String? = null
)

@Serializable
data class ChapterEventResponse(
    @SerialName("world_event_title") val worldEventTitle: String,
    @SerialName("world_event_summary") val worldEventSummary: String,
    val snippets: List<LoreSnippet>
)
