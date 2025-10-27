package com.jalmarquest.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class LoreSnippet(
    val id: String,
    @SerialName("event_text") val eventText: String,
    @SerialName("choice_options") val choiceOptions: List<String>,
    val consequences: JsonObject,
    val conditions: JsonObject,
    @SerialName("allowed_locations") val allowedLocations: List<String> = emptyList(), // Empty = available everywhere
    @SerialName("allowed_biomes") val allowedBiomes: List<String> = emptyList() // Empty = available in all biomes
)
