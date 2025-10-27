package com.jalmarquest.feature.eventengine

import com.jalmarquest.core.model.ChapterEventRequest
import com.jalmarquest.core.model.ChapterEventResponse
import com.jalmarquest.core.model.LoreSnippet
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.add

fun interface ChapterEventProvider {
    fun generateChapterEvent(request: ChapterEventRequest): ChapterEventResponse
}

class DefaultChapterEventProvider : ChapterEventProvider {
    override fun generateChapterEvent(request: ChapterEventRequest): ChapterEventResponse {
        val playerId = request.playerState.id
        return ChapterEventResponse(
            worldEventTitle = "Whispers Over ${playerId.uppercase()}",
            worldEventSummary = "Gemini stands by: a placeholder chapter event shaped by ${playerId}'s journey.",
            snippets = listOf(
                LoreSnippet(
                    id = "placeholder_chapter_intro",
                    eventText = "A hush rolls across Buttonburgh as news spreads of ${playerId}'s rising legend.",
                    choiceOptions = listOf("Embrace the moment", "Stay humble", "Rally the critters"),
                    consequences = buildJsonObject {
                        put("embrace", buildJsonObject { put("add_choice_tags", buildJsonArray { add("chapter_embrace") }) })
                        put("humble", buildJsonObject { put("add_choice_tags", buildJsonArray { add("chapter_humble") }) })
                        put("rally", buildJsonObject { put("add_choice_tags", buildJsonArray { add("chapter_rally") }) })
                    },
                    conditions = buildJsonObject {
                        put("requires", buildJsonArray { })
                    }
                )
            )
        )
    }
}
