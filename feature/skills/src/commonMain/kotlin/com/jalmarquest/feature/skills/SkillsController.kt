package com.jalmarquest.feature.skills

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.skills.SkillManager
import com.jalmarquest.core.state.crafting.CraftingManager
import com.jalmarquest.core.state.equipment.EquipmentManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Skills UI view state.
 */
data class SkillsViewState(
    val skills: List<Skill> = emptyList(),
    val totalSkillPoints: Int = 0,
    val unlockedAbilities: List<Ability> = emptyList(),
    val levelableSkills: List<Skill> = emptyList(),
    val knownRecipes: List<CraftingRecipe> = emptyList(),
    val equippedItems: Map<EquipmentSlot, CraftedItem> = emptyMap(),
    val totalStats: EquipmentStats = EquipmentStats(),
    val selectedTab: SkillsTab = SkillsTab.SKILLS
)

/**
 * Tabs in the Skills UI.
 */
enum class SkillsTab {
    SKILLS,      // Skill tree & progression
    CRAFTING,    // Recipe list & crafting
    EQUIPMENT    // Equipped items & stats
}

/**
 * Controller for Skills UI.
 * Provides view state and actions for skill management, crafting, and equipment.
 */
class SkillsController(
    private val skillManager: SkillManager,
    private val craftingManager: CraftingManager,
    private val equipmentManager: EquipmentManager,
    private val abilityDefinitions: Map<AbilityId, Ability> = emptyMap()
) {
    
    private val _viewState = MutableStateFlow(SkillsViewState())
    val viewState: StateFlow<SkillsViewState> = _viewState.asStateFlow()
    
    init {
        refreshViewState()
    }
    
    /**
     * Refresh the view state from current managers.
     */
    fun refreshViewState() {
        val skills = SkillType.entries.mapNotNull { type ->
            skillManager.getSkillByType(type)
        }
        
        val abilities = skillManager.getAllUnlockedAbilityIds()
            .mapNotNull { abilityDefinitions[it] }
        
        _viewState.update { current ->
            current.copy(
                skills = skills,
                totalSkillPoints = skillManager.getTotalSkillPoints(),
                unlockedAbilities = abilities,
                levelableSkills = skillManager.getLevelableSkills(),
                knownRecipes = craftingManager.getKnownRecipes(),
                equippedItems = equipmentManager.getEquippedItems(),
                totalStats = equipmentManager.calculateTotalStats()
            )
        }
    }
    
    /**
     * Switch to a different tab.
     */
    fun selectTab(tab: SkillsTab) {
        _viewState.update { it.copy(selectedTab = tab) }
    }
    
    /**
     * Attempt to level up a skill.
     */
    fun levelUpSkill(skillId: SkillId): Boolean {
        val result = skillManager.levelUpSkill(skillId)
        if (result != null) {
            refreshViewState()
            return true
        }
        return false
    }
    
    /**
     * Unlock an ability for a skill.
     */
    fun unlockAbility(skillId: SkillId, abilityId: AbilityId): Boolean {
        val ability = abilityDefinitions[abilityId] ?: return false
        
        // Check requirements
        if (!skillManager.checkAbilityRequirements(ability)) {
            return false
        }
        
        val result = skillManager.unlockAbility(skillId, abilityId)
        if (result != null) {
            refreshViewState()
            return true
        }
        return false
    }
    
    /**
     * Equip an item to a slot.
     */
    fun equipItem(itemId: CraftedItemId, slot: EquipmentSlot): Boolean {
        return try {
            equipmentManager.equipItem(itemId, slot)
            refreshViewState()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Unequip an item from a slot.
     */
    fun unequipItem(slot: EquipmentSlot) {
        equipmentManager.unequipSlot(slot)
        refreshViewState()
    }
    
    /**
     * Get skill details for display.
     */
    fun getSkillDetails(skillId: SkillId): SkillDetails? {
        val skill = skillManager.getSkill(skillId) ?: return null
        val abilities = abilityDefinitions.values.filter { it.requiredSkill == skillId }
        
        return SkillDetails(
            skill = skill,
            availableAbilities = abilities,
            progress = skill.progressToNextLevel()
        )
    }
}

/**
 * Detailed view of a single skill.
 */
data class SkillDetails(
    val skill: Skill,
    val availableAbilities: List<Ability>,
    val progress: Double
)
