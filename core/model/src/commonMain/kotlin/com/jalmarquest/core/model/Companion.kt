package com.jalmarquest.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Unique identifier for a companion
 */
@Serializable
@JvmInline
value class CompanionId(val value: String)

/**
 * Definition of a companion NPC that can join the player
 */
@Serializable
data class Companion(
    @SerialName("companion_id")
    val companionId: CompanionId,
    
    val name: String,
    
    val description: String,
    
    @SerialName("npc_id")
    val npcId: String?, // Associated NPC if applicable
    
    val species: String, // "quail", "sparrow", "finch", etc.
    
    val personality: String, // "Energetic", "Shy", "Brave", etc.
    
    @SerialName("base_abilities")
    val baseAbilities: List<CompanionAbility> = emptyList(),
    
    @SerialName("gift_preferences")
    val giftPreferences: Map<String, Int> = emptyMap(), // itemId -> affinity gain
    
    @SerialName("affinity_milestones")
    val affinityMilestones: List<AffinityMilestone> = emptyList()
)

/**
 * An ability that a companion provides
 */
@Serializable
data class CompanionAbility(
    @SerialName("ability_id")
    val abilityId: String,
    
    val name: String,
    
    val description: String,
    
    @SerialName("required_affinity")
    val requiredAffinity: Int = 0, // 0-100
    
    val effects: List<CompanionEffect> = emptyList()
)

/**
 * Effect provided by a companion ability
 */
@Serializable
data class CompanionEffect(
    val type: CompanionEffectType,
    val magnitude: Int
)

/**
 * Types of effects companions can provide
 */
@Serializable
enum class CompanionEffectType {
    @SerialName("seed_bonus")
    SEED_BONUS,           // +% to seed gains
    
    @SerialName("forage_bonus")
    FORAGE_BONUS,         // +% chance to find extra items when foraging
    
    @SerialName("combat_assist")
    COMBAT_ASSIST,        // Assists in combat (damage bonus)
    
    @SerialName("crafting_bonus")
    CRAFTING_BONUS,       // +% to crafting success
    
    @SerialName("xp_bonus")
    XP_BONUS,             // +% to experience gains
    
    @SerialName("dialogue_unlock")
    DIALOGUE_UNLOCK,      // Unlocks special dialogue options
    
    @SerialName("quest_assist")
    QUEST_ASSIST,         // Helps with specific quest types
    
    @SerialName("recipe_unlock")
    RECIPE_UNLOCK,        // Teaches a recipe
    
    @SerialName("thought_unlock")
    THOUGHT_UNLOCK,       // Shares a thought
    
    @SerialName("shop_discount")
    SHOP_DISCOUNT,        // +% discount at shops
    
    @SerialName("navigation_assist")
    NAVIGATION_ASSIST     // Reveals hidden paths/shortcuts
}

/**
 * Milestone rewards for reaching affinity levels
 */
@Serializable
data class AffinityMilestone(
    @SerialName("affinity_threshold")
    val affinityThreshold: Int, // 0-100
    
    val reward: MilestoneReward
)

/**
 * Reward for reaching affinity milestone
 */
@Serializable
sealed class MilestoneReward {
    @Serializable
    @SerialName("ability")
    data class AbilityUnlock(
        @SerialName("ability_id")
        val abilityId: String
    ) : MilestoneReward()
    
    @Serializable
    @SerialName("recipe")
    data class RecipeUnlock(
        @SerialName("recipe_id")
        val recipeId: String
    ) : MilestoneReward()
    
    @Serializable
    @SerialName("thought")
    data class ThoughtUnlock(
        @SerialName("thought_id")
        val thoughtId: String
    ) : MilestoneReward()
    
    @Serializable
    @SerialName("item")
    data class ItemGift(
        @SerialName("item_id")
        val itemId: String,
        val quantity: Int = 1
    ) : MilestoneReward()
}

/**
 * Player's progress with a specific companion
 */
@Serializable
data class CompanionProgress(
    @SerialName("companion_id")
    val companionId: CompanionId,
    
    val affinity: Int = 0, // 0-100
    
    @SerialName("is_recruited")
    val isRecruited: Boolean = false,
    
    @SerialName("unlocked_abilities")
    val unlockedAbilities: List<String> = emptyList(),
    
    @SerialName("gifts_given")
    val giftsGiven: Int = 0,
    
    @SerialName("last_gift_timestamp")
    val lastGiftTimestamp: Long = 0,
    
    @SerialName("reached_milestones")
    val reachedMilestones: List<Int> = emptyList() // Affinity thresholds reached
)

/**
 * Collection of player's companion relationships
 */
@Serializable
data class CompanionState(
    @SerialName("active_companion")
    val activeCompanion: CompanionId? = null,
    
    @SerialName("recruited_companions")
    val recruitedCompanions: Map<String, CompanionProgress> = emptyMap(), // companionId.value -> progress
    
    @SerialName("discovered_companions")
    val discoveredCompanions: List<String> = emptyList() // companionId.value - met but not recruited
) {
    /**
     * Get progress for a specific companion
     */
    fun getProgress(companionId: CompanionId): CompanionProgress? {
        return recruitedCompanions[companionId.value]
    }
    
    /**
     * Check if companion is recruited
     */
    fun isCompanionRecruited(companionId: CompanionId): Boolean {
        return recruitedCompanions[companionId.value]?.isRecruited == true
    }
    
    /**
     * Check if companion is discovered
     */
    fun isCompanionDiscovered(companionId: CompanionId): Boolean {
        return discoveredCompanions.contains(companionId.value) || 
               isCompanionRecruited(companionId)
    }
    
    /**
     * Get affinity level for a companion
     */
    fun getAffinity(companionId: CompanionId): Int {
        return recruitedCompanions[companionId.value]?.affinity ?: 0
    }
}
