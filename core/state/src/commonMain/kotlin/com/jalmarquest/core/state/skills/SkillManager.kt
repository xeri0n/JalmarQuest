package com.jalmarquest.core.state.skills

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.archetype.ArchetypeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Manages skill progression, ability unlocking, and skill-based bonuses.
 * Integrates with GameStateManager to persist skill state.
 * Also stores ability definitions for calculating bonuses.
 * Integrates with ArchetypeManager for XP bonuses.
 */
class SkillManager(
    private val initialSkillTree: SkillTree = SkillTree(),
    private val abilityDefinitions: Map<AbilityId, Ability> = emptyMap(),
    private val archetypeManager: ArchetypeManager? = null
) {
    private val _skillTreeState = MutableStateFlow(initialSkillTree)
    val skillTreeState: StateFlow<SkillTree> = _skillTreeState.asStateFlow()

    /**
     * Adds experience points to a specific skill.
     * Applies archetype bonuses (SKILL_XP_BONUS and GENERAL_XP_BONUS) if available.
     * Does NOT automatically level up - use levelUpSkill() for that.
     * Returns the updated Skill.
     */
    fun gainSkillXP(skillId: SkillId, xpAmount: Int): Skill? {
        if (xpAmount < 0) throw IllegalArgumentException("XP amount must be non-negative")
        
        val currentSkill = _skillTreeState.value.getSkill(skillId) ?: return null
        
        // Apply archetype XP bonuses
        var finalXP = xpAmount
        if (archetypeManager != null) {
            val skillXPBonus = archetypeManager.getTotalBonus(TalentType.SKILL_XP_BONUS)
            val generalXPBonus = archetypeManager.getTotalBonus(TalentType.GENERAL_XP_BONUS)
            val totalBonusPercent = skillXPBonus + generalXPBonus
            
            if (totalBonusPercent > 0) {
                val bonusXP = (xpAmount * totalBonusPercent) / 100
                finalXP = xpAmount + bonusXP
            }
        }
        
        val updatedSkill = currentSkill.addXP(finalXP)
        
        val newSkillTree = _skillTreeState.value.updateSkill(skillId, updatedSkill)
        _skillTreeState.update { newSkillTree }
        
        return updatedSkill
    }

    /**
     * Levels up a skill if it has enough XP.
     * Preserves XP overflow to next level.
     * Returns the leveled-up skill or null if requirements not met.
     */
    fun levelUpSkill(skillId: SkillId): Skill? {
        val currentSkill = _skillTreeState.value.getSkill(skillId) ?: return null
        
        if (!currentSkill.canLevelUp()) {
            return null // Not enough XP or already max level
        }
        
        val leveledSkill = currentSkill.levelUp()
        val newSkillTree = _skillTreeState.value.updateSkill(skillId, leveledSkill)
        _skillTreeState.update { newSkillTree }
        
        return leveledSkill
    }

    /**
     * Unlocks an ability for a skill.
     * Does NOT check requirements - use checkAbilityRequirements() first.
     */
    fun unlockAbility(skillId: SkillId, abilityId: AbilityId): Skill? {
        val currentSkill = _skillTreeState.value.getSkill(skillId) ?: return null
        val updatedSkill = currentSkill.unlockAbility(abilityId)
        
        val newSkillTree = _skillTreeState.value.updateSkill(skillId, updatedSkill)
        _skillTreeState.update { newSkillTree }
        
        return updatedSkill
    }

    /**
     * Checks if a skill requirement is met.
     */
    fun meetsRequirement(requirement: SkillRequirement): Boolean {
        return _skillTreeState.value.meetsRequirement(requirement)
    }

    /**
     * Gets all unlocked abilities across all skills.
     * Returns AbilityIds - use abilityDefinitions to get full Ability objects.
     */
    fun getAllUnlockedAbilityIds(): Set<AbilityId> {
        return _skillTreeState.value.getAllUnlockedAbilities()
    }

    /**
     * Gets all unlocked abilities as full Ability objects.
     */
    fun getAllUnlockedAbilities(): List<Ability> {
        val unlockedIds = getAllUnlockedAbilityIds()
        return abilityDefinitions.values.filter { it.id in unlockedIds }
    }

    /**
     * Calculates the total bonus for a specific ability type.
     * For example, getTotalBonus(AbilityType.HARVEST_BONUS) returns sum of all harvest bonus magnitudes.
     */
    fun getTotalBonus(abilityType: AbilityType): Int {
        return _skillTreeState.value.getTotalBonus(abilityType, abilityDefinitions)
    }

    /**
     * Gets all passive bonuses currently active.
     * Returns map of AbilityType -> total magnitude.
     */
    fun getActiveBonuses(): Map<AbilityType, Int> {
        val bonuses = mutableMapOf<AbilityType, Int>()
        val passiveTypes = listOf(
            AbilityType.HARVEST_BONUS,
            AbilityType.CRAFT_SUCCESS,
            AbilityType.RECIPE_DISCOVERY,
            AbilityType.DAMAGE_BONUS,
            AbilityType.DEFENSE_BONUS,
            AbilityType.SHOP_DISCOUNT,
            AbilityType.SELL_PRICE_BONUS,
            AbilityType.HOARD_VALUE_BONUS,
            AbilityType.XP_GAIN_BONUS,
            AbilityType.INTERNALIZATION_SPEED
        )
        
        passiveTypes.forEach { type ->
            val bonus = getTotalBonus(type)
            if (bonus > 0) {
                bonuses[type] = bonus
            }
        }
        
        return bonuses
    }

    /**
     * Gets a specific skill by ID.
     */
    fun getSkill(skillId: SkillId): Skill? {
        return _skillTreeState.value.getSkill(skillId)
    }

    /**
     * Gets a skill by its type (e.g., FORAGING, ALCHEMY).
     */
    fun getSkillByType(type: SkillType): Skill? {
        return _skillTreeState.value.getSkillByType(type)
    }

    /**
     * Gets all skills that can be leveled up right now.
     */
    fun getLevelableSkills(): List<Skill> {
        return _skillTreeState.value.skills.values.filter { it.canLevelUp() }
    }

    /**
     * Gets the total skill points spent across all skills.
     */
    fun getTotalSkillPoints(): Int {
        return _skillTreeState.value.totalSkillPoints
    }

    /**
     * Resets the entire skill tree (for testing or new game+).
     */
    fun resetSkillTree() {
        _skillTreeState.update { SkillTree() }
    }

    /**
     * Updates the entire skill tree state (used by GameStateManager during load).
     */
    fun updateSkillTree(newSkillTree: SkillTree) {
        _skillTreeState.update { newSkillTree }
    }

    /**
     * Checks if an ability can be unlocked based on requirements.
     * Ability must reference a skill requirement.
     */
    fun checkAbilityRequirements(ability: Ability): Boolean {
        val requirement = SkillRequirement.Level(ability.requiredSkill, ability.requiredLevel)
        return meetsRequirement(requirement)
    }

    /**
     * Gets all skills sorted by total XP (for leaderboard or UI display).
     */
    fun getSkillsByXP(): List<Skill> {
        return _skillTreeState.value.skills.values.sortedByDescending { it.currentXP }
    }

    /**
     * Gets all skills sorted by level.
     */
    fun getSkillsByLevel(): List<Skill> {
        return _skillTreeState.value.skills.values.sortedByDescending { it.level }
    }

    /**
     * Checks if player has a specific ability unlocked.
     */
    fun hasAbility(abilityId: AbilityId): Boolean {
        return getAllUnlockedAbilityIds().contains(abilityId)
    }

    /**
     * Gets skills that have abilities available to unlock at current level.
     */
    fun getSkillsWithAvailableAbilities(availableAbilities: List<Ability>): List<Pair<Skill, List<Ability>>> {
        val result = mutableListOf<Pair<Skill, List<Ability>>>()
        
        _skillTreeState.value.skills.values.forEach { skill ->
            val unlockable = availableAbilities.filter { ability ->
                ability.requiredSkill == skill.id &&
                ability.requiredLevel <= skill.level &&
                !skill.hasAbility(ability.id)
            }
            
            if (unlockable.isNotEmpty()) {
                result.add(skill to unlockable)
            }
        }
        
        return result
    }
}
