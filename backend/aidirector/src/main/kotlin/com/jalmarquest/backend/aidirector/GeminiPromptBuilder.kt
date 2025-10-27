package com.jalmarquest.backend.aidirector

import com.jalmarquest.core.model.ChapterEventRequest
import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.PlayerNarrativeState
import com.jalmarquest.core.model.QuestLog
import com.jalmarquest.core.model.StatusEffects
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class GeminiPromptBuilder(
    private val supportedLocales: List<String> = listOf("en-US", "nb-NO")
) {
    fun buildSystemPrompt(request: ChapterEventRequest): String {
        val playerId = request.playerState.id
        val localeSummary = supportedLocales.joinToString()
        return buildString {
            appendLine("You are the AI Game Master for Jalmar Quest, a cozy text-based RPG starring Jalmar the button quail.")
            appendLine("Honor these pillars:")
            appendLine("- Butterfly Effect Engine: track every choice and seed long-term consequences.")
            appendLine("- Tone: sincere tiny-hero adventure with playful, self-aware humor.")
            appendLine("- Accessibility: produce narration that shines when narrated with TTS.")
            appendLine()
            appendLine("Output MUST be JSON matching ChapterEventResponse with keys world_event_title, world_event_summary, and snippets[].")
            appendLine("Each snippet requires: id, eventText, choiceOptions (3 options), consequences (JSON), conditions (JSON).")
            appendLine()
            appendLine("Supported locales: $localeSummary (default ${supportedLocales.first()}). Keep vocabulary quail-authentic.")
            appendLine("Never contradict the player's established history. Current player id: $playerId")
        }
    }

    fun assemble(request: ChapterEventRequest): PromptAssemblyResult {
        val systemPrompt = buildSystemPrompt(request)
        val userPrompt = buildPlayerContextPayload(request.playerState, request.triggerReason)
        val payload = GeminiGenerateContentRequest(
            systemInstruction = GeminiContent(role = ROLE_SYSTEM, parts = listOf(GeminiPart(systemPrompt))),
            contents = listOf(
                GeminiContent(role = ROLE_USER, parts = listOf(GeminiPart(userPrompt)))
            )
        )
        return PromptAssemblyResult(
            request = request,
            systemPrompt = systemPrompt,
            userPrompt = userPrompt,
            payload = payload
        )
    }

    private fun buildPlayerContextPayload(
        playerState: PlayerNarrativeState,
        triggerReason: String?
    ): String {
        val choiceSummary = summarizeChoices(playerState.choiceLog)
        val questSummary = summarizeQuests(playerState.questLog)
        val statusSummary = summarizeStatus(playerState.statusEffects)
        val triggerText = triggerReason?.let { "trigger_reason: $it" } ?: "trigger_reason: unspecified"

        return buildString {
            appendLine("player_id: ${playerState.id}")
            appendLine(triggerText)
            appendLine("recent_choices:\n$choiceSummary")
            appendLine("quest_log:\n$questSummary")
            appendLine("status_effects:\n$statusSummary")
            appendLine("Guidance: craft a short, vivid world event plus three branching options.")
            appendLine("Each option should reference small-scale, authentic quail experiences that could scale into future consequences.")
        }
    }

    private fun summarizeChoices(choiceLog: ChoiceLog): String {
        if (choiceLog.entries.isEmpty()) {
            return "- none recorded"
        }
        return choiceLog.entries
            .takeLast(MAX_EXPLICIT_CHOICE_COUNT)
            .joinToString(separator = "\n") {
                "- tag: ${it.tag.value}, timestamp: ${it.timestampMillis}"
            }
    }

    private fun summarizeQuests(questLog: QuestLog): String {
        if (questLog.activeQuests.isEmpty() && questLog.completedQuests.isEmpty()) {
            return "- no quests tracked"
        }
        
        val parts = mutableListOf<String>()
        
        if (questLog.activeQuests.isNotEmpty()) {
            parts.add("Active:")
            questLog.activeQuests.forEach { progress ->
                val completedObjs = progress.objectives.count { it.isComplete() }
                val totalObjs = progress.objectives.size
                parts.add("  - ${progress.questId.value} ($completedObjs/$totalObjs objectives)")
            }
        }
        
        if (questLog.completedQuests.isNotEmpty()) {
            parts.add("Completed: ${questLog.completedQuests.size} quests")
        }
        
        return parts.joinToString("\n")
    }

    private fun summarizeStatus(statusEffects: StatusEffects): String {
        if (statusEffects.entries.isEmpty()) {
            return "- no active effects"
        }
        return statusEffects.entries.joinToString(separator = "\n") { effect ->
            val expires = effect.expiresAtMillis?.toString() ?: "persistent"
            "- ${effect.key} (expires: $expires)"
        }
    }

    companion object {
        private const val ROLE_SYSTEM = "system"
        private const val ROLE_USER = "user"
        private const val MAX_EXPLICIT_CHOICE_COUNT = 5
    }
}

@Serializable
data class GeminiGenerateContentRequest(
    @SerialName("system_instruction") val systemInstruction: GeminiContent,
    val contents: List<GeminiContent>
)

@Serializable
data class GeminiContent(
    val role: String,
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String
)

data class PromptAssemblyResult(
    val request: ChapterEventRequest,
    val systemPrompt: String,
    val userPrompt: String,
    val payload: GeminiGenerateContentRequest
)

@Serializable
data class GeminiGenerateContentResponse(
    val candidates: List<GeminiResponseCandidate>? = null
)

@Serializable
data class GeminiResponseCandidate(
    val content: GeminiResponseContent? = null
)

@Serializable
data class GeminiResponseContent(
    val parts: List<GeminiResponsePart>? = null
)

@Serializable
data class GeminiResponsePart(
    val text: String? = null
)
