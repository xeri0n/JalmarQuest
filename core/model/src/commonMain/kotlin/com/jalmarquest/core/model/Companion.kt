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
 * Alpha 2.3 Part 3.1: Companion trait types.
 * Traits improve as companions complete tasks, providing scaling bonuses.
 */
@Serializable
enum class CompanionTrait {
    @SerialName("foraging")
    FORAGING,           // Improved resource gathering
    
    @SerialName("scouting")
    SCOUTING,           // Better exploration rewards
    
    @SerialName("brewing")
    BREWING,            // Enhanced concoction crafting
    
    @SerialName("smithing")
    SMITHING,           // Better equipment crafting
    
    @SerialName("combat")
    COMBAT,             // Stronger in battles
    
    @SerialName("trading")
    TRADING,            // Better shop prices
    
    @SerialName("scholarship")
    SCHOLARSHIP,        // Faster thought internalization
    
    @SerialName("luck")
    LUCK                // Better RNG outcomes
}

/**
 * Alpha 2.3 Part 3.2: Task types that companions can be assigned to.
 * Each task type trains a specific companion trait.
 */
@Serializable
enum class CompanionTaskType {
    @SerialName("foraging")
    FORAGING,       // Gathering resources
    
    @SerialName("scouting")
    SCOUTING,       // Exploring locations
    
    @SerialName("brewing")
    BREWING,        // Crafting concoctions
    
    @SerialName("smithing")
    SMITHING,       // Crafting equipment
    
    @SerialName("combat")
    COMBAT,         // Fighting enemies
    
    @SerialName("trading")
    TRADING,        // Buying/selling at shops
    
    @SerialName("scholarship")
    SCHOLARSHIP,    // Internalizing thoughts
    
    @SerialName("exploration")
    EXPLORATION;    // General exploration
    
    /**
     * Get the trait associated with this task type.
     */
    fun associatedTrait(): CompanionTrait = when (this) {
        FORAGING -> CompanionTrait.FORAGING
        SCOUTING -> CompanionTrait.SCOUTING
        BREWING -> CompanionTrait.BREWING
        SMITHING -> CompanionTrait.SMITHING
        COMBAT -> CompanionTrait.COMBAT
        TRADING -> CompanionTrait.TRADING
        SCHOLARSHIP -> CompanionTrait.SCHOLARSHIP
        EXPLORATION -> CompanionTrait.LUCK
    }
}

/**
 * Alpha 2.3 Part 3.1: Trait level (1-10 progression).
 */
@Serializable
@JvmInline
value class TraitLevel(val level: Int) {
    init {
        require(level in 1..10) { "Trait level must be between 1 and 10, got $level" }
    }
    
    /**
     * Get bonus multiplier for this trait level.
     * Level 1 = 1.0x, Level 10 = 2.5x (linear scaling).
     */
    fun getBonusMultiplier(): Float = 1.0f + (level - 1) * 0.1667f
    
    /**
     * Get XP required to reach next level.
     * Progressive curve: 100 → 250 → 450 → 700 → 1000 → 1350 → 1750 → 2200 → 2700 → MAX
     */
    fun getXpToNextLevel(): Int {
        return when (level) {
            1 -> 100
            2 -> 250
            3 -> 450
            4 -> 700
            5 -> 1000
            6 -> 1350
            7 -> 1750
            8 -> 2200
            9 -> 2700
            10 -> Int.MAX_VALUE // Already at max
            else -> Int.MAX_VALUE
        }
    }
    
    /**
     * Check if can level up.
     */
    fun canLevelUp(): Boolean = level < 10
}

/**
 * Alpha 2.3 Part 3.1: Progress tracking for a single trait.
 */
@Serializable
data class TraitProgress(
    val trait: CompanionTrait,
    val level: TraitLevel = TraitLevel(1),
    val currentXp: Int = 0
) {
    /**
     * Check if ready to level up.
     */
    fun canLevelUp(): Boolean = level.canLevelUp() && currentXp >= level.getXpToNextLevel()
    
    /**
     * Level up the trait (consumes XP).
     */
    fun levelUp(): TraitProgress {
        require(canLevelUp()) { "Cannot level up trait $trait (level ${level.level}, XP $currentXp)" }
        val xpRequired = level.getXpToNextLevel()
        return copy(
            level = TraitLevel(level.level + 1),
            currentXp = currentXp - xpRequired
        )
    }
    
    /**
     * Add XP to this trait.
     */
    fun addXp(xp: Int): TraitProgress {
        require(xp >= 0) { "XP cannot be negative" }
        return copy(currentXp = currentXp + xp)
    }
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
    val reachedMilestones: List<Int> = emptyList(), // Affinity thresholds reached
    
    /**
     * Alpha 2.3 Part 3.1: Companion trait levels.
     * Maps trait type to current progress (level + XP).
     */
    val traits: Map<String, TraitProgress> = emptyMap() // trait.name -> progress
) {
    /**
     * Alpha 2.3 Part 3.1: Get trait progress for a specific trait.
     */
    fun getTraitProgress(trait: CompanionTrait): TraitProgress? {
        return traits[trait.name]
    }
    
    /**
     * Alpha 2.3 Part 3.1: Get trait level (defaults to 1 if not started).
     */
    fun getTraitLevel(trait: CompanionTrait): TraitLevel {
        return traits[trait.name]?.level ?: TraitLevel(1)
    }
    
    /**
     * Alpha 2.3 Part 3.1: Get trait bonus multiplier.
     */
    fun getTraitBonus(trait: CompanionTrait): Float {
        return getTraitLevel(trait).getBonusMultiplier()
    }
}

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

/**
 * Alpha 2.3 Part 3.2: Active task assignment for a companion.
 */
@Serializable
data class CompanionTaskAssignment(
    @SerialName("companion_id")
    val companionId: CompanionId,
    
    @SerialName("task_type")
    val taskType: CompanionTaskType,
    
    @SerialName("start_time")
    val startTime: Long,
    
    @SerialName("duration")
    val duration: Long // in milliseconds
) {
    fun isComplete(currentTime: Long): Boolean = currentTime >= (startTime + duration)
    
    fun getRemainingTime(currentTime: Long): Long {
        val endTime = startTime + duration
        return (endTime - currentTime).coerceAtLeast(0)
    }
    
    fun getProgress(currentTime: Long): Float {
        if (duration == 0L) return 1.0f
        val elapsed = currentTime - startTime
        return (elapsed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    }
}

/**
 * Alpha 2.3 Part 3.2: Player's companion assignment state.
 */
@Serializable
data class CompanionAssignmentState(
    @SerialName("active_assignments")
    val activeAssignments: List<CompanionTaskAssignment> = emptyList(),
    
    @SerialName("completed_task_count")
    val completedTaskCount: Int = 0,
    
    @SerialName("perfection_meter")
    val perfectionMeter: Int = 0 // 0-100, hidden optimization score for Alpha 2.3 Part 3.4
)
