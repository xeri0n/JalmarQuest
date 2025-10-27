package com.jalmarquest.backend.aidirector

import com.jalmarquest.core.model.ChapterEventResponse
import com.jalmarquest.core.model.LoreSnippet
import kotlinx.coroutines.delay
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put
import java.util.UUID

class StubAiDirectorClient(
    private val responseDelayMillis: Long = 0L,
    private val idProvider: () -> String = { UUID.randomUUID().toString() }
) : AiDirectorClient {
    @Volatile
    private var lastGeneratedAssemblyInternal: PromptAssemblyResult? = null

    val lastGeneratedAssembly: PromptAssemblyResult?
        get() = lastGeneratedAssemblyInternal

    override suspend fun generateChapterEvent(assembly: PromptAssemblyResult): ChapterEventResponse {
        if (responseDelayMillis > 0) {
            delay(responseDelayMillis)
        }
        lastGeneratedAssemblyInternal = assembly
        val playerId = assembly.request.playerState.id
        val snippetIdSuffix = idProvider()
        val snippetId = "chapter_${snippetIdSuffix}"

        val snippet = LoreSnippet(
            id = snippetId,
            eventText = "Gemini studies ${playerId}'s Choice Log, weaving a bespoke chapter moment.",
            choiceOptions = listOf("Accept the omen", "Question the omen", "Ignore it"),
            consequences = buildJsonObject {
                put("accept", buildJsonObject {
                    put("add_choice_tags", buildJsonArray { add("gemini_accepted_$snippetIdSuffix") })
                })
                put("question", buildJsonObject {
                    put("add_choice_tags", buildJsonArray { add("gemini_questioned_$snippetIdSuffix") })
                })
                put("ignore", buildJsonObject {
                    put("add_choice_tags", buildJsonArray { add("gemini_ignored_$snippetIdSuffix") })
                })
            },
            conditions = buildJsonObject {
                put("requires", buildJsonArray { })
            }
        )

        return ChapterEventResponse(
            worldEventTitle = "Gemini's Gaze (${playerId})",
            worldEventSummary = "A deterministic stub response providing predictable lore snippets for testing.",
            snippets = listOf(snippet)
        )
    }
}
