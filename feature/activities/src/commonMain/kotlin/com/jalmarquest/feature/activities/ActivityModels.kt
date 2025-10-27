package com.jalmarquest.feature.activities

import com.jalmarquest.core.model.ItemId
import com.jalmarquest.core.model.ItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class ActivityId(val value: String)

@Serializable
enum class ActivityType {
    @SerialName("dungeon")
    DUNGEON,

    @SerialName("arena")
    ARENA,

    @SerialName("nest_defense")
    NEST_DEFENSE,

    @SerialName("apex_predator_teaser")
    APEX_PREDATOR_TEASER
}

@Serializable
data class ActivityReward(
    val items: List<ItemStack> = emptyList(),
    val statusEffectKey: String? = null,
    val statusEffectDurationMillis: Long? = null
)

@Serializable
data class SecondaryActivity(
    val id: ActivityId,
    val type: ActivityType,
    val titleKey: String,
    val descriptionKey: String,
    val reward: ActivityReward = ActivityReward()
)

@Serializable
data class ActivityResolution(
    val activityId: ActivityId,
    val type: ActivityType,
    val success: Boolean,
    val awardedItems: List<ItemStack> = emptyList(),
    val appliedStatusEffect: String? = null
)

@Serializable
data class ActivityState(
    val activities: List<SecondaryActivity>,
    val selectedActivityId: ActivityId? = null,
    val lastResolution: ActivityResolution? = null
) {
    fun selectedActivity(): SecondaryActivity? = activities.firstOrNull { it.id == selectedActivityId }
}

fun defaultSecondaryActivities(): List<SecondaryActivity> = listOf(
    SecondaryActivity(
        id = ActivityId("twilight_dungeon"),
        type = ActivityType.DUNGEON,
        titleKey = "hub_activity_dungeon_title",
        descriptionKey = "hub_activity_dungeon_description",
        reward = ActivityReward(
            items = listOf(ItemStack(ItemId("ancient_trinket"), 1)),
            statusEffectKey = "dungeon_bravery",
            statusEffectDurationMillis = 10 * 60 * 1000L
        )
    ),
    SecondaryActivity(
        id = ActivityId("sparring_arena"),
        type = ActivityType.ARENA,
        titleKey = "hub_activity_arena_title",
        descriptionKey = "hub_activity_arena_description",
        reward = ActivityReward(
            items = listOf(ItemStack(ItemId("feather_medal"), 1))
        )
    ),
    SecondaryActivity(
        id = ActivityId("hen_pen_drill"),
        type = ActivityType.NEST_DEFENSE,
        titleKey = "hub_activity_nest_defense_title",
        descriptionKey = "hub_activity_nest_defense_description",
        reward = ActivityReward(
            items = listOf(ItemStack(ItemId("seed_cache"), 3))
        )
    ),
    SecondaryActivity(
        id = ActivityId("apex_teaser"),
        type = ActivityType.APEX_PREDATOR_TEASER,
        titleKey = "hub_activity_apex_title",
        descriptionKey = "hub_activity_apex_description",
        reward = ActivityReward(statusEffectKey = "apex_whispers", statusEffectDurationMillis = 5 * 60 * 1000L)
    )
)
