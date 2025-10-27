package com.jalmarquest.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Player skills that provide passive bonuses and unlock abilities.
 * Each skill can be leveled from 1 to 10 through earning XP.
 */

@Serializable
@JvmInline
value class SkillId(val value: String)

@Serializable
@JvmInline
value class AbilityId(val value: String)

/**
 * Available skill types in the game.
 */
@Serializable
enum class SkillType {
    FORAGING,       // Ingredient harvesting bonuses, find rare ingredients
    ALCHEMY,        // Concoction crafting bonuses, discover recipes faster
    COMBAT,         // Damage, defense, health bonuses
    BARTERING,      // Shop discounts, better sell prices
    HOARDING,       // Shiny valuation bonuses, hoard rank progression
    SCHOLARSHIP     // Thought internalization speed, XP gain bonuses
}

/**
 * Represents a single skill with level, XP, and progression data.
 */
@Serializable
data class Skill(
    val id: SkillId,
    val type: SkillType,
    val nameKey: String,              // Localization key for skill name
    val descriptionKey: String,        // Localization key for skill description
    val level: Int = 1,                // Current level (1-10)
    val currentXP: Int = 0,            // XP towards next level
    val unlockedAbilities: Set<AbilityId> = emptySet() // Abilities unlocked by this skill
) {
    /**
     * XP required to reach the next level.
     * Exponential curve: level 1→2 needs 100 XP, level 9→10 needs 10,000 XP.
     */
    fun xpForNextLevel(): Int? {
        if (level >= 10) return null // Max level
        return when (level) {
            1 -> 100
            2 -> 250
            3 -> 500
            4 -> 1_000
            5 -> 2_000
            6 -> 3_500
            7 -> 5_500
            8 -> 8_000
            9 -> 10_000
            else -> null
        }
    }
    
    /**
     * Check if this skill is ready to level up.
     */
    fun canLevelUp(): Boolean {
        val required = xpForNextLevel() ?: return false
        return currentXP >= required
    }
    
    /**
     * Progress towards next level as percentage (0.0 to 1.0).
     */
    fun progressToNextLevel(): Double {
        val required = xpForNextLevel() ?: return 1.0
        return (currentXP.toDouble() / required).coerceIn(0.0, 1.0)
    }
    
    /**
     * Check if an ability is unlocked.
     */
    fun hasAbility(abilityId: AbilityId): Boolean = 
        unlockedAbilities.contains(abilityId)
    
    /**
     * Add XP and return updated skill (does not auto-level).
     */
    fun addXP(amount: Int): Skill {
        require(amount >= 0) { "XP amount cannot be negative" }
        return copy(currentXP = currentXP + amount)
    }
    
    /**
     * Level up the skill, resetting XP overflow.
     */
    fun levelUp(): Skill {
        require(canLevelUp()) { "Not enough XP to level up" }
        require(level < 10) { "Already at max level" }
        
        val required = xpForNextLevel()!!
        val overflow = currentXP - required
        
        return copy(
            level = level + 1,
            currentXP = overflow.coerceAtLeast(0)
        )
    }
    
    /**
     * Unlock an ability for this skill.
     */
    fun unlockAbility(abilityId: AbilityId): Skill =
        copy(unlockedAbilities = unlockedAbilities + abilityId)
}

/**
 * An ability that can be unlocked through skill progression.
 */
@Serializable
data class Ability(
    val id: AbilityId,
    val nameKey: String,
    val descriptionKey: String,
    val requiredSkill: SkillId,
    val requiredLevel: Int,         // Skill level required to unlock
    val type: AbilityType,
    val magnitude: Int = 0,         // Magnitude for passive bonuses
    val cooldownSeconds: Int = 0    // Cooldown for active abilities (0 = passive)
)

/**
 * Types of abilities.
 */
@Serializable
enum class AbilityType {
    // Passive bonuses
    HARVEST_BONUS,          // +magnitude% harvest yield
    CRAFT_SUCCESS,          // +magnitude% crafting success rate
    RECIPE_DISCOVERY,       // +magnitude% recipe discovery chance
    DAMAGE_BONUS,           // +magnitude damage
    DEFENSE_BONUS,          // +magnitude defense
    SHOP_DISCOUNT,          // magnitude% discount at shops
    SELL_PRICE_BONUS,       // +magnitude% when selling items
    HOARD_VALUE_BONUS,      // +magnitude% shiny valuation
    XP_GAIN_BONUS,          // +magnitude% XP gain
    INTERNALIZATION_SPEED,  // +magnitude% thought internalization speed
    
    // Active abilities
    POWER_HARVEST,          // Guarantee rare ingredient drop
    MASTER_CRAFT,           // Next craft has 100% success rate
    CRITICAL_STRIKE,        // Deal 2x damage next attack
    MERCHANT_CHARM,         // Temporary 50% shop discount
    TREASURE_SENSE,         // Reveal nearby shinies
    STUDY_SESSION           // Double thought internalization progress
}

/**
 * Player's skill progression state.
 */
@Serializable
data class SkillTree(
    @SerialName("skills")
    val skills: Map<SkillId, Skill> = emptyMap(),
    @SerialName("total_skill_points")
    val totalSkillPoints: Int = 0  // Total levels earned across all skills
) {
    /**
     * Get a skill by ID, or null if not found.
     */
    fun getSkill(skillId: SkillId): Skill? = skills[skillId]
    
    /**
     * Get a skill by type.
     */
    fun getSkillByType(type: SkillType): Skill? = 
        skills.values.find { it.type == type }
    
    /**
     * Check if a skill requirement is met.
     */
    fun meetsRequirement(requirement: SkillRequirement): Boolean = when (requirement) {
        is SkillRequirement.Level -> {
            val skill = getSkill(requirement.skillId)
            skill != null && skill.level >= requirement.minLevel
        }
        is SkillRequirement.TotalPoints -> totalSkillPoints >= requirement.minPoints
        is SkillRequirement.All -> requirement.requirements.all { meetsRequirement(it) }
        is SkillRequirement.Any -> requirement.requirements.any { meetsRequirement(it) }
    }
    
    /**
     * Update a skill in the tree.
     */
    fun updateSkill(skillId: SkillId, skill: Skill): SkillTree {
        val oldSkill = skills[skillId]
        val pointDelta = if (oldSkill != null) skill.level - oldSkill.level else skill.level - 1
        
        return copy(
            skills = skills + (skillId to skill),
            totalSkillPoints = totalSkillPoints + pointDelta
        )
    }
    
    /**
     * Add XP to a skill.
     */
    fun addSkillXP(skillId: SkillId, amount: Int): SkillTree {
        val skill = skills[skillId] ?: return this
        return updateSkill(skillId, skill.addXP(amount))
    }
    
    /**
     * Get all unlocked abilities across all skills.
     */
    fun getAllUnlockedAbilities(): Set<AbilityId> =
        skills.values.flatMap { it.unlockedAbilities }.toSet()
    
    /**
     * Calculate total bonus for a specific ability type.
     */
    fun getTotalBonus(abilityType: AbilityType, abilities: Map<AbilityId, Ability>): Int {
        val unlockedAbilityIds = getAllUnlockedAbilities()
        return abilities.values
            .filter { it.id in unlockedAbilityIds && it.type == abilityType }
            .sumOf { it.magnitude }
    }
}

/**
 * Requirements for unlocking skills or abilities.
 */
@Serializable
sealed class SkillRequirement {
    @Serializable
    @SerialName("level")
    data class Level(
        val skillId: SkillId,
        val minLevel: Int
    ) : SkillRequirement()
    
    @Serializable
    @SerialName("total_points")
    data class TotalPoints(
        val minPoints: Int
    ) : SkillRequirement()
    
    @Serializable
    @SerialName("all")
    data class All(
        val requirements: List<SkillRequirement>
    ) : SkillRequirement()
    
    @Serializable
    @SerialName("any")
    data class Any(
        val requirements: List<SkillRequirement>
    ) : SkillRequirement()
}
