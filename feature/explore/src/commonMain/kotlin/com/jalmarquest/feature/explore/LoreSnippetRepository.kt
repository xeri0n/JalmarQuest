package com.jalmarquest.feature.explore

import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.LoreSnippet
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put
import kotlin.random.Random

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
    
    /**
     * Find next available snippet filtered by location and biome.
     * Returns location-specific snippets first, then biome-specific, then generic.
     */
    fun nextAvailableSnippetForLocation(
        choiceLog: ChoiceLog,
        locationId: String?,
        biomeType: String?
    ): String? {
        val acquired = choiceLog.entries.map { it.tag.value }.toSet()
        
        // Filter records by prerequisites and completion
        val availableRecords = recordsInOrder.filter { record ->
            record.prerequisites.all(acquired::contains) &&
                (record.completionTag == null || record.completionTag !in acquired)
        }
        
        // Priority 1: Location-specific snippets
        if (locationId != null) {
            val locationMatch = availableRecords.firstOrNull { record ->
                val snippet = record.snippet
                snippet.allowedLocations.isNotEmpty() && locationId in snippet.allowedLocations
            }
            if (locationMatch != null) return locationMatch.snippet.id
        }
        
        // Priority 2: Biome-specific snippets
        if (biomeType != null) {
            val biomeMatch = availableRecords.firstOrNull { record ->
                val snippet = record.snippet
                snippet.allowedBiomes.isNotEmpty() && biomeType in snippet.allowedBiomes
            }
            if (biomeMatch != null) return biomeMatch.snippet.id
        }
        
        // Priority 3: Generic snippets (no location/biome restrictions)
        return availableRecords.firstOrNull { record ->
            record.snippet.allowedLocations.isEmpty() && record.snippet.allowedBiomes.isEmpty()
        }?.snippet?.id
    }
    
    /**
     * Alpha 2.2: AI Director adaptive snippet selection with weighted preferences.
     * 
     * Selects snippets based on player playstyle recommendations with 60% weight to recommended
     * event type and 40% to variety. Falls back to standard location-based selection if no
     * event type tags are available.
     * 
     * @param choiceLog Player's choice history for prerequisite checking
     * @param locationId Current location ID for location-specific snippets
     * @param biomeType Current biome type for biome-specific snippets
     * @param recommendedEventType AI Director's recommended event type (COMBAT/EXPLORATION/SOCIAL/RESOURCE/NARRATIVE/CHAOS)
     * @return Snippet ID selected based on weighted preferences, or null if none available
     */
    fun nextAvailableSnippetWithRecommendation(
        choiceLog: ChoiceLog,
        locationId: String?,
        biomeType: String?,
        recommendedEventType: String?
    ): String? {
        if (recommendedEventType == null) {
            // No recommendation available, use standard selection
            return nextAvailableSnippetForLocation(choiceLog, locationId, biomeType)
        }
        
        val acquired = choiceLog.entries.map { it.tag.value }.toSet()
        
        // Filter records by prerequisites and completion
        val availableRecords = recordsInOrder.filter { record ->
            record.prerequisites.all(acquired::contains) &&
                (record.completionTag == null || record.completionTag !in acquired)
        }
        
        if (availableRecords.isEmpty()) return null
        
        // Separate snippets by event type matching
        val recommendedTypeSnippets = availableRecords.filter { record ->
            record.snippet.eventType?.equals(recommendedEventType, ignoreCase = true) == true
        }
        
        val otherTypeSnippets = availableRecords.filter { record ->
            record.snippet.eventType != null && 
            !record.snippet.eventType.equals(recommendedEventType, ignoreCase = true)
        }
        
        val untypedSnippets = availableRecords.filter { record ->
            record.snippet.eventType == null
        }
        
        // Weighted selection: 60% recommended type, 40% variety (other types + untyped)
        val roll = Random.nextFloat()
        
        val selectedRecord = when {
            roll < 0.6f && recommendedTypeSnippets.isNotEmpty() -> {
                // 60% chance: Select from recommended type
                recommendedTypeSnippets.random()
            }
            roll < 0.9f && otherTypeSnippets.isNotEmpty() -> {
                // 30% chance: Select from other types for variety
                otherTypeSnippets.random()
            }
            untypedSnippets.isNotEmpty() -> {
                // 10% chance or fallback: Select from untyped snippets
                untypedSnippets.random()
            }
            else -> {
                // Fallback to any available snippet
                availableRecords.random()
            }
        }
        
        return selectedRecord.snippet.id
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
                ) + AdditionalLoreSnippets.getAllSnippets() + BiomeSpecificSnippets.getAllSnippets() + IgnatiusLoreSnippets.getAllSnippets()
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
