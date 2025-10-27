package com.jalmarquest.feature.explore

import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.LoreSnippet
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put

class LoreSnippetRepository(
    records: List<SnippetRecord>
) {
    private val recordsInOrder = records
    private val recordsById = records.associateBy { it.snippet.id }

    fun getSnippet(id: String): SnippetRecord? = recordsById[id]

    fun nextAvailableSnippet(choiceLog: ChoiceLog): String? {
        val acquired = choiceLog.entries.map { it.tag.value }.toSet()
        return recordsInOrder.firstOrNull { record ->
            record.prerequisites.all(acquired::contains) &&
                (record.completionTag == null || record.completionTag !in acquired)
        }?.snippet?.id
    }

    companion object {
        fun defaultCatalog(): LoreSnippetRepository {
            val puddle = LoreSnippet(
                id = "explore_garden_gate",
                eventText = "Jalmar pads up to the garden gate. The moonlight turns a humble puddle into a shimmering lake, while clover towers like a forest canopy.",
                choiceOptions = listOf(
                    "Inspect the puddle",
                    "Stride through the clover",
                    "Retreat to the nest"
                ),
                consequences = buildJsonObject {
                    put("inspect", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("explore_puddle_reflection"))
                        put("grant_status_effects", buildStatusArray(
                            buildStatusEffect(
                                key = "dampened_feathers",
                                durationMinutes = 30
                            )
                        ))
                        put("narration", "Jalmar spots glittering beetle shells and feels the chill of the water seep into their feathers.")
                    })
                    put("stride", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("explore_clover_trail"))
                        put("grant_status_effects", buildStatusArray(
                            buildStatusEffect(
                                key = "forest_poise",
                                durationMinutes = 60
                            )
                        ))
                        put("narration", "Clover stems sway aside as Jalmar charts a path fit for tiny legends.")
                    })
                    put("retreat", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("explore_retreat"))
                        put("narration", "The nest’s warmth calls louder tonight; Jalmar promises to return at dawn.")
                    })
                },
                conditions = emptyConditions()
            )

            val clover = LoreSnippet(
                id = "explore_clover_copse",
                eventText = "Beyond the gate the clover opens into a clearing where a garden gnome stands like a silent titan.",
                choiceOptions = listOf(
                    "Challenge the gnome",
                    "Salvage fallen twigs",
                    "Circle back quietly"
                ),
                consequences = buildJsonObject {
                    put("challenge", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("explore_gnome_challenge"))
                        put("grant_status_effects", buildStatusArray(
                            buildStatusEffect(
                                key = "bristled_bravery",
                                durationMinutes = 45
                            )
                        ))
                        put("narration", "Jalmar’s chirp echoes; the gnome’s shadow falls in respectful silence.")
                    })
                    put("salvage", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("explore_gather_twigs"))
                        put("narration", "A bundle of twigs becomes future armor and stories to trade back home.")
                    })
                    put("circle", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("explore_circle_back"))
                        put("narration", "No need to wake the titan tonight; the path is safely mapped.")
                    })
                },
                conditions = emptyConditions()
            )

            return LoreSnippetRepository(
                listOf(
                    SnippetRecord(
                        snippet = puddle,
                        choiceKeyByOptionIndex = mapOf(
                            0 to "inspect",
                            1 to "stride",
                            2 to "retreat"
                        ),
                        completionTag = "explore_completed_garden_gate",
                        prerequisites = emptySet(),
                        title = "Garden Gate Recon",
                        historySummary = "Scouted the garden gate under moonlit clover."
                    ),
                    SnippetRecord(
                        snippet = clover,
                        choiceKeyByOptionIndex = mapOf(
                            0 to "challenge",
                            1 to "salvage",
                            2 to "circle"
                        ),
                        completionTag = "explore_completed_clover_copse",
                        prerequisites = setOf("explore_completed_garden_gate"),
                        title = "Clover Copse Watch",
                        historySummary = "Mapped the clover clearing and its towering guardian."
                    )
                )
            )
        }

        private fun buildChoiceTags(vararg tags: String): JsonArray = buildJsonArray {
            tags.forEach(::add)
        }

        private fun buildStatusArray(vararg status: JsonObject): JsonArray = buildJsonArray {
            status.forEach(::add)
        }

        private fun buildStatusEffect(key: String, durationMinutes: Int? = null): JsonObject = buildJsonObject {
            put("key", key)
            durationMinutes?.let { put("duration_minutes", it) }
        }

        private fun emptyConditions(): JsonObject = buildJsonObject {
            put("requires", buildJsonArray { })
        }
    }
}

data class SnippetRecord(
    val snippet: LoreSnippet,
    val choiceKeyByOptionIndex: Map<Int, String>,
    val completionTag: String?,
    val prerequisites: Set<String>,
    val title: String,
    val historySummary: String
)
