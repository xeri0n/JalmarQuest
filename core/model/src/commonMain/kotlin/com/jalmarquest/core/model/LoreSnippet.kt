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
    val conditions: JsonObject
)
