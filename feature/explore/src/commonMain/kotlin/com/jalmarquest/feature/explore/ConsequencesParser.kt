package com.jalmarquest.feature.explore

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.contentOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class ConsequencesParser {
    fun parse(record: SnippetRecord, selectionIndex: Int): ConsequenceBundle {
        val snippet = record.snippet
        val optionText = snippet.choiceOptions.getOrNull(selectionIndex)
            ?: error("Choice index $selectionIndex is out of bounds for snippet ${snippet.id}")
        val key = record.choiceKeyByOptionIndex[selectionIndex]
            ?: optionText.lowercase().replace("[^a-z0-9]+".toRegex(), "_").trim('_')
        val node = (snippet.consequences[key] ?: snippet.consequences.values.firstOrNull())
            ?.jsonObject
            ?: JsonObject(emptyMap())

        val choiceTags = node.arrayOf("add_choice_tags")?.elementsAsStrings().orEmpty()
        val seedDelta = node.intValue("grant_seeds")?.toLong() ?: 0L
        val narration = node.stringValue("narration")
        val statusEffects = node.arrayOf("grant_status_effects")
            ?.let { array ->
                array.mapNotNull { element ->
                    val obj = element as? JsonObject ?: return@mapNotNull null
                    val keyValue = obj.stringValue("key") ?: return@mapNotNull null
                    val minutes = obj.intValue("duration_minutes")
                    val duration = minutes?.takeIf { it > 0 }?.minutes
                    StatusEffectGrant(keyValue, duration)
                }
            }
            .orEmpty()

        val rewardFlags = node.booleanValue("flag_autosave") ?: false

        // Optional: items to grant
        val itemGrants = node.arrayOf("grant_items")
            ?.mapNotNull { element ->
                val obj = element as? JsonObject ?: return@mapNotNull null
                val itemId = obj.stringValue("item_id") ?: return@mapNotNull null
                val qty = obj.intValue("quantity") ?: 0
                if (qty <= 0) return@mapNotNull null
                ItemGrant(itemId, qty)
            }?.toList().orEmpty()

        // Optional: faction reputation adjustments
        val factionRep = node.arrayOf("grant_faction_reputation")
            ?.mapNotNull { element ->
                val obj = element as? JsonObject ?: return@mapNotNull null
                val factionId = obj.stringValue("faction_id") ?: return@mapNotNull null
                val amount = obj.intValue("amount") ?: 0
                if (amount == 0) return@mapNotNull null
                FactionRepChange(factionId, amount)
            }?.toList().orEmpty()

        return ConsequenceBundle(
            completionTag = record.completionTag,
            choiceTags = choiceTags,
            statusEffects = statusEffects,
            seedDelta = seedDelta,
            narration = narration,
            autosaveRequested = rewardFlags,
            optionText = optionText,
            itemGrants = itemGrants,
            factionRepChanges = factionRep
        )
    }

    private fun JsonObject.stringValue(key: String): String? =
        (this[key] as? JsonPrimitive)?.contentOrNull

    private fun JsonObject.intValue(key: String): Int? =
        (this[key] as? JsonPrimitive)?.intOrNull

    private fun JsonObject.booleanValue(key: String): Boolean? =
        (this[key] as? JsonPrimitive)?.booleanOrNull

    private fun JsonObject.arrayOf(key: String): JsonArray? =
        this[key] as? JsonArray
}

data class ConsequenceBundle(
    val completionTag: String?,
    val choiceTags: List<String>,
    val statusEffects: List<StatusEffectGrant>,
    val seedDelta: Long,
    val narration: String?,
    val autosaveRequested: Boolean,
    val optionText: String,
    val itemGrants: List<ItemGrant> = emptyList(),
    val factionRepChanges: List<FactionRepChange> = emptyList()
)

data class StatusEffectGrant(
    val key: String,
    val duration: Duration?
)

data class ItemGrant(
    val itemId: String,
    val quantity: Int
)

data class FactionRepChange(
    val factionId: String,
    val amount: Int
)

private fun JsonArray.elementsAsStrings(): List<String> = buildList {
    for (element in this@elementsAsStrings) {
        val value = (element as? JsonPrimitive)?.contentOrNull ?: continue
        add(value)
    }
}
