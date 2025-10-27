package com.jalmarquest.core.model

import kotlinx.serialization.Serializable
import kotlin.math.pow

/**
 * Archetype represents a player's chosen character class/role that influences
 * dialogue options, talent trees, and gameplay bonuses.
 */
@Serializable
enum class ArchetypeType {
    /**
     * The Scholar - Focuses on knowledge, lore, and intellectual pursuits.
     * Bonuses: Faster thought internalization, bonus XP from reading/research, unique dialogue options.
     */
    SCHOLAR,
    
    /**
     * The Collector - Obsessed with gathering shinies and treasures.
     * Bonuses: Better hoard values, increased luck for rare items, shop discounts.
     */
    COLLECTOR,
    
    /**
     * The Alchemist - Master of concoctions and crafting.
     * Bonuses: Better crafting success, extended potion durations, recipe discovery bonuses.
     */
    ALCHEMIST,
    
    /**
     * The Scavenger - Expert at foraging and survival.
     * Bonuses: Better harvesting yields, faster movement, bonus ingredients from all locations.
     */
    SCAVENGER,
    
    /**
     * The Socialite - Charismatic and well-connected.
     * Bonuses: Better NPC relationships, dialogue unlocks, companion affinity bonuses.
     */
    SOCIALITE,
    
    /**
     * The Warrior - Combat-focused and brave.
     * Bonuses: Increased damage/defense, better combat rewards, unique combat abilities.
     */
    WARRIOR
}

/**
 * Talent represents a unique ability or passive bonus that can be unlocked
 * within an archetype's talent tree.
 */
@Serializable
data class Talent(
    val id: String,
    val name: String,
    val description: String,
    val talentType: TalentType,
    val magnitude: Int,
    val costInPoints: Int,
    val requirements: List<TalentRequirement> = emptyList()
)

/**
 * Type of talent effect.
 */
@Serializable
enum class TalentType {
    // Progression bonuses
    TALENT_POINT_GAIN,      // Gain extra talent points
    SKILL_XP_BONUS,         // Bonus XP for specific skills
    GENERAL_XP_BONUS,       // Bonus XP for all activities
    
    // Resource bonuses
    SEED_INCOME_BONUS,      // Passive seed generation
    INGREDIENT_YIELD_BONUS, // More ingredients when harvesting
    CRAFTING_COST_REDUCTION,// Lower ingredient costs
    
    // Progression unlocks
    DIALOGUE_UNLOCK,        // New dialogue options
    RECIPE_UNLOCK,          // Unlock specific recipes
    THOUGHT_UNLOCK,         // Unlock specific thoughts
    FEATURE_UNLOCK,         // Unlock game features
    
    // Combat bonuses (for Warrior archetype)
    DAMAGE_BONUS,
    DEFENSE_BONUS,
    HEALTH_BONUS,
    
    // Social bonuses (for Socialite archetype)
    COMPANION_AFFINITY_BONUS,
    SHOP_DISCOUNT,
    SELL_PRICE_BONUS,
    
    // Utility bonuses
    MOVEMENT_SPEED_BONUS,
    LUCK_BONUS,
    HOARD_VALUE_BONUS,
    INTERNALIZATION_SPEED_BONUS,
    
    // Active abilities
    ACTIVE_ABILITY          // Unlocks a special active ability
}

/**
 * Requirements that must be met before a talent can be unlocked.
 */
@Serializable
sealed class TalentRequirement {
    /**
     * Requires a specific archetype level.
     */
    @Serializable
    data class Level(val requiredLevel: Int) : TalentRequirement()
    
    /**
     * Requires another talent to be unlocked first.
     */
    @Serializable
    data class PrerequisiteTalent(val talentId: String) : TalentRequirement()
    
    /**
     * Requires all of the specified talents to be unlocked.
     */
    @Serializable
    data class AllTalents(val talentIds: List<String>) : TalentRequirement()
    
    /**
     * Requires any one of the specified talents to be unlocked.
     */
    @Serializable
    data class AnyTalent(val talentIds: List<String>) : TalentRequirement()
    
    /**
     * Requires a minimum total number of talents unlocked in this tree.
     */
    @Serializable
    data class TotalTalentsUnlocked(val count: Int) : TalentRequirement()
}

/**
 * TalentTree represents the full progression tree for an archetype.
 */
@Serializable
data class TalentTree(
    val archetypeType: ArchetypeType,
    val talents: List<Talent> = emptyList(),
    val unlockedTalentIds: Set<String> = emptySet()
) {
    /**
     * Check if a talent can be unlocked (requirements met).
     */
    fun canUnlockTalent(talentId: String, archetypeLevel: Int): Boolean {
        val talent = talents.find { it.id == talentId } ?: return false
        
        // Already unlocked
        if (talentId in unlockedTalentIds) return false
        
        // Check all requirements
        return talent.requirements.all { requirement ->
            meetsRequirement(requirement, archetypeLevel)
        }
    }
    
    private fun meetsRequirement(requirement: TalentRequirement, archetypeLevel: Int): Boolean {
        return when (requirement) {
            is TalentRequirement.Level -> archetypeLevel >= requirement.requiredLevel
            is TalentRequirement.PrerequisiteTalent -> requirement.talentId in unlockedTalentIds
            is TalentRequirement.AllTalents -> requirement.talentIds.all { it in unlockedTalentIds }
            is TalentRequirement.AnyTalent -> requirement.talentIds.any { it in unlockedTalentIds }
            is TalentRequirement.TotalTalentsUnlocked -> unlockedTalentIds.size >= requirement.count
        }
    }
    
    /**
     * Unlock a talent (assuming requirements are met).
     */
    fun unlockTalent(talentId: String): TalentTree {
        return copy(unlockedTalentIds = unlockedTalentIds + talentId)
    }
    
    /**
     * Get all unlocked talents.
     */
    fun getUnlockedTalents(): List<Talent> {
        return talents.filter { it.id in unlockedTalentIds }
    }
    
    /**
     * Get total bonus magnitude for a specific talent type.
     */
    fun getTotalBonus(talentType: TalentType): Int {
        return getUnlockedTalents()
            .filter { it.talentType == talentType }
            .sumOf { it.magnitude }
    }
}

/**
 * ArchetypeProgress tracks the player's progression within their chosen archetype.
 */
@Serializable
data class ArchetypeProgress(
    val selectedArchetype: ArchetypeType? = null,
    val archetypeLevel: Int = 1,
    val archetypeXP: Int = 0,
    val availableTalentPoints: Int = 0,
    val totalTalentPointsEarned: Int = 0,
    val talentTree: TalentTree? = null
) {
    /**
     * XP required to reach the next archetype level.
     * Uses exponential scaling: 200 * (1.5^(level - 1))
     */
    fun xpForNextLevel(): Int {
        return (200 * 1.5.pow((archetypeLevel - 1).toDouble())).toInt()
    }
    
    /**
     * Check if player can level up.
     */
    fun canLevelUp(): Boolean {
        return archetypeXP >= xpForNextLevel() && archetypeLevel < 10
    }
    
    /**
     * Calculate progress percentage to next level.
     */
    fun progressToNextLevel(): Double {
        if (archetypeLevel >= 10) return 1.0
        val xpNeeded = xpForNextLevel()
        return (archetypeXP.toDouble() / xpNeeded).coerceIn(0.0, 1.0)
    }
    
    /**
     * Add XP and handle level-ups.
     * Returns updated progress with overflow XP preserved.
     */
    fun addXP(xpAmount: Int): ArchetypeProgress {
        var updated = copy(archetypeXP = archetypeXP + xpAmount)
        
        // Auto level-up if enough XP
        while (updated.canLevelUp()) {
            updated = updated.levelUp()
        }
        
        return updated
    }
    
    /**
     * Level up the archetype (grants talent points).
     * Preserves overflow XP.
     */
    fun levelUp(): ArchetypeProgress {
        if (!canLevelUp()) return this
        
        val xpNeeded = xpForNextLevel()
        val overflow = archetypeXP - xpNeeded
        
        // Grant 1 talent point per level
        return copy(
            archetypeLevel = archetypeLevel + 1,
            archetypeXP = overflow.coerceAtLeast(0),
            availableTalentPoints = availableTalentPoints + 1,
            totalTalentPointsEarned = totalTalentPointsEarned + 1
        )
    }
    
    /**
     * Spend talent points to unlock a talent.
     */
    fun spendTalentPoints(cost: Int): ArchetypeProgress {
        if (availableTalentPoints < cost) return this
        return copy(availableTalentPoints = availableTalentPoints - cost)
    }
}
