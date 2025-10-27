package com.jalmarquest.core.model

import kotlinx.serialization.Serializable

/**
 * Represents a unique thought that can be discovered and internalized.
 * Inspired by Disco Elysium's Thought Cabinet - thoughts are abstract concepts
 * that provide character development through contemplation.
 */
@Serializable
data class ThoughtId(val value: String)

/**
 * A thought template defining a discoverable philosophical concept.
 */
@Serializable
data class Thought(
    val id: ThoughtId,
    val nameKey: String,  // Localization key for thought name
    val descriptionKey: String,  // Localization key for initial description
    val problemDescriptionKey: String,  // Localization key for "while internalizing" description
    val solutionDescriptionKey: String,  // Localization key for "after internalization" description
    val internalizationTimeSeconds: Int,  // Time required to internalize (e.g., 1 hour)
    val internalizationCostSeeds: Int = 0,  // Optional Seed cost to start internalizing
    val discoveryCondition: ThoughtDiscoveryCondition,  // How this thought is unlocked
    val problemEffects: List<ThoughtEffect> = emptyList(),  // Debuffs while internalizing
    val solutionEffects: List<ThoughtEffect>,  // Buffs after internalization
    val unlocksFeature: String? = null  // Optional feature unlock (e.g., "guild_creation", "apex_predator_hunt")
)

/**
 * Conditions for discovering a thought.
 */
@Serializable
sealed class ThoughtDiscoveryCondition {
    /**
     * Thought is discovered automatically at game start.
     */
    @Serializable
    data object StartingThought : ThoughtDiscoveryCondition()
    
    /**
     * Thought is discovered when player reaches a specific milestone.
     */
    @Serializable
    data class Milestone(val milestoneId: String) : ThoughtDiscoveryCondition()
    
    /**
     * Thought is discovered when player makes a specific choice.
     */
    @Serializable
    data class Choice(val choiceTag: String) : ThoughtDiscoveryCondition()
    
    /**
     * Thought is discovered when player has a specific archetype.
     */
    @Serializable
    data class Archetype(val archetypeId: String) : ThoughtDiscoveryCondition()
    
    /**
     * Thought is discovered through faction reputation thresholds.
     */
    @Serializable
    data class Faction(val factionId: String, val reputationThreshold: Int) : ThoughtDiscoveryCondition()
    
    /**
     * Thought is discovered when multiple conditions are met.
     */
    @Serializable
    data class All(val conditions: List<ThoughtDiscoveryCondition>) : ThoughtDiscoveryCondition()
    
    /**
     * Thought is discovered when any condition is met.
     */
    @Serializable
    data class Any(val conditions: List<ThoughtDiscoveryCondition>) : ThoughtDiscoveryCondition()
}

/**
 * Effects applied by a thought (either problem or solution).
 */
@Serializable
data class ThoughtEffect(
    val type: ThoughtEffectType,
    val magnitude: Int,  // Can be positive or negative
    val description: String  // Human-readable effect description
)

/**
 * Types of effects thoughts can have.
 */
@Serializable
enum class ThoughtEffectType {
    // Core stat modifiers
    SEED_INCOME,  // % modifier to Seed generation
    EXPERIENCE_GAIN,  // % modifier to XP
    COMBAT_DAMAGE,  // % modifier to damage dealt
    COMBAT_DEFENSE,  // % modifier to damage taken
    HEALTH_REGEN,  // Passive health regeneration
    
    // Crafting/gathering modifiers
    CRAFTING_SUCCESS_RATE,  // % chance for bonus items when crafting
    HARVEST_YIELD,  // % bonus to ingredient/resource harvesting
    RECIPE_DISCOVERY_CHANCE,  // % bonus to discovering new recipes
    
    // Social/narrative modifiers
    DIALOGUE_OPTION_UNLOCK,  // Unlocks special dialogue branches
    FACTION_REPUTATION_GAIN,  // % modifier to faction rep gains
    COMPANION_AFFECTION_GAIN,  // % modifier to companion relationship
    
    // Economic modifiers
    SHOP_DISCOUNT,  // % discount on shop purchases
    HOARD_VALUE_MULTIPLIER,  // % bonus to Shiny valuations
    
    // Unique unlocks (magnitude typically 1 for binary unlocks)
    FEATURE_UNLOCK,  // Unlocks a gameplay feature
    ACTIVITY_UNLOCK,  // Unlocks an activity type
    
    // Meta/misc
    THOUGHT_SLOT_INCREASE,  // Increases max active thought slots
    INTERNALIZATION_SPEED,  // % modifier to internalization time
}

/**
 * Player's discovered thoughts and internalization progress.
 */
@Serializable
data class ThoughtCabinet(
    val discoveredThoughts: Set<ThoughtId> = emptySet(),  // All thoughts player has found
    val activeSlots: List<ThoughtSlot> = emptyList(),  // Currently internalizing (max 3 by default)
    val internalized: Set<ThoughtId> = emptySet(),  // Completed thoughts (effects are active)
    val maxSlots: Int = 3  // Can be increased by certain thoughts
) {
    /**
     * Check if a thought has been discovered.
     */
    fun hasDiscovered(thoughtId: ThoughtId): Boolean =
        thoughtId in discoveredThoughts
    
    /**
     * Check if a thought is currently being internalized.
     */
    fun isInternalizing(thoughtId: ThoughtId): Boolean =
        activeSlots.any { it.thoughtId == thoughtId }
    
    /**
     * Check if a thought has been fully internalized.
     */
    fun isInternalized(thoughtId: ThoughtId): Boolean =
        thoughtId in internalized
    
    /**
     * Get the internalization slot for a specific thought, if any.
     */
    fun getSlot(thoughtId: ThoughtId): ThoughtSlot? =
        activeSlots.find { it.thoughtId == thoughtId }
    
    /**
     * Check if there are available slots for new internalization.
     */
    fun hasAvailableSlot(): Boolean =
        activeSlots.size < maxSlots
    
    /**
     * Add a newly discovered thought.
     */
    fun discoverThought(thoughtId: ThoughtId): ThoughtCabinet =
        copy(discoveredThoughts = discoveredThoughts + thoughtId)
    
    /**
     * Start internalizing a thought (add to active slots).
     */
    fun startInternalizing(slot: ThoughtSlot): ThoughtCabinet =
        copy(activeSlots = activeSlots + slot)
    
    /**
     * Update progress on an active slot.
     */
    fun updateSlot(thoughtId: ThoughtId, updatedSlot: ThoughtSlot): ThoughtCabinet =
        copy(activeSlots = activeSlots.map { if (it.thoughtId == thoughtId) updatedSlot else it })
    
    /**
     * Complete internalization (move from active to internalized).
     */
    fun completeInternalization(thoughtId: ThoughtId): ThoughtCabinet =
        copy(
            activeSlots = activeSlots.filterNot { it.thoughtId == thoughtId },
            internalized = internalized + thoughtId
        )
    
    /**
     * Abandon an active internalization (free the slot).
     */
    fun abandonInternalization(thoughtId: ThoughtId): ThoughtCabinet =
        copy(activeSlots = activeSlots.filterNot { it.thoughtId == thoughtId })
    
    /**
     * Forget an internalized thought (remove effects, can re-internalize).
     */
    fun forgetThought(thoughtId: ThoughtId): ThoughtCabinet =
        copy(internalized = internalized - thoughtId)
}

/**
 * Represents an active internalization slot.
 */
@Serializable
data class ThoughtSlot(
    val thoughtId: ThoughtId,
    val startedAt: Long,  // Timestamp when internalization began
    val completesAt: Long  // Timestamp when it will be complete
) {
    /**
     * Check if the thought has finished internalizing.
     */
    fun isComplete(currentTime: Long): Boolean =
        currentTime >= completesAt
    
    /**
     * Get remaining time in milliseconds.
     */
    fun remainingMs(currentTime: Long): Long =
        (completesAt - currentTime).coerceAtLeast(0)
    
    /**
     * Get progress as a percentage (0.0 to 1.0).
     */
    fun progress(currentTime: Long): Float {
        val totalDuration = completesAt - startedAt
        val elapsed = currentTime - startedAt
        return if (totalDuration > 0) {
            (elapsed.toFloat() / totalDuration).coerceIn(0f, 1f)
        } else {
            1f
        }
    }
}
