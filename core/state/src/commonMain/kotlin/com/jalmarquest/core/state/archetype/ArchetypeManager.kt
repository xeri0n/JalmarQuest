package com.jalmarquest.core.state.archetype

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.perf.PerformanceLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Manages archetype selection, talent progression, and archetype-based bonuses.
 * Integrates with GameStateManager to persist archetype state.
 */
class ArchetypeManager(
    private val gameStateManager: GameStateManager,
    private val talentTreeCatalog: TalentTreeCatalog
) {
    private val _archetypeState = MutableStateFlow(gameStateManager.playerState.value.archetypeProgress)
    val archetypeState: StateFlow<ArchetypeProgress> = _archetypeState.asStateFlow()

    /**
     * Select an archetype for the player (one-time choice, typically during onboarding).
     * Initializes the talent tree for the chosen archetype.
     */
    fun selectArchetype(archetypeType: ArchetypeType): Boolean {
        val currentProgress = _archetypeState.value
        
        // Can only select once
        if (currentProgress.selectedArchetype != null) {
            return false
        }
        
        val talentTree = talentTreeCatalog.getTalentTree(archetypeType)
        val updatedProgress = currentProgress.copy(
            selectedArchetype = archetypeType,
            talentTree = talentTree
        )
        
        _archetypeState.update { updatedProgress }
        gameStateManager.updatePlayer { it.copy(archetypeProgress = updatedProgress) }
        gameStateManager.appendChoice("archetype_${archetypeType.name.lowercase()}")
        
        PerformanceLogger.logStateMutation("Archetype", "select", mapOf(
            "archetype" to archetypeType.name
        ))
        
        return true
    }
    
    /**
     * Gain archetype XP and automatically handle level-ups.
     */
    fun gainArchetypeXP(xpAmount: Int): ArchetypeProgress {
        if (xpAmount < 0) throw IllegalArgumentException("XP amount must be non-negative")
        
        val currentProgress = _archetypeState.value
        if (currentProgress.selectedArchetype == null) {
            return currentProgress // No archetype selected yet
        }
        
        val oldLevel = currentProgress.archetypeLevel
        val updatedProgress = currentProgress.addXP(xpAmount)
        val newLevel = updatedProgress.archetypeLevel
        
        _archetypeState.update { updatedProgress }
        gameStateManager.updatePlayer { it.copy(archetypeProgress = updatedProgress) }
        
        // Log level-up if it occurred
        if (newLevel > oldLevel) {
            gameStateManager.appendChoice("archetype_levelup_${newLevel}")
            val archetypeName = currentProgress.selectedArchetype?.name ?: "unknown"
            PerformanceLogger.logStateMutation("Archetype", "levelup", mapOf(
                "newLevel" to newLevel,
                "archetype" to archetypeName
            ))
        }
        
        return updatedProgress
    }
    
    /**
     * Unlock a talent in the archetype's talent tree.
     * Validates requirements and spends talent points.
     */
    fun unlockTalent(talentId: String): Boolean {
        val currentProgress = _archetypeState.value
        val talentTree = currentProgress.talentTree ?: return false
        
        // Find the talent
        val talent = talentTree.talents.find { it.id == talentId } ?: return false
        
        // Check if can unlock (requirements met)
        if (!talentTree.canUnlockTalent(talentId, currentProgress.archetypeLevel)) {
            return false
        }
        
        // Check if player has enough talent points
        if (currentProgress.availableTalentPoints < talent.costInPoints) {
            return false
        }
        
        // Spend points and unlock talent
        val updatedProgress = currentProgress
            .spendTalentPoints(talent.costInPoints)
            .copy(talentTree = talentTree.unlockTalent(talentId))
        
        _archetypeState.update { updatedProgress }
        gameStateManager.updatePlayer { it.copy(archetypeProgress = updatedProgress) }
        gameStateManager.appendChoice("talent_unlock_${talentId}")
        
        PerformanceLogger.logStateMutation("Archetype", "unlockTalent", mapOf(
            "talentId" to talentId,
            "pointsSpent" to talent.costInPoints
        ))
        
        return true
    }
    
    /**
     * Grant bonus talent points (from achievements, milestones, etc.).
     */
    fun grantTalentPoints(points: Int) {
        if (points <= 0) throw IllegalArgumentException("Points must be positive")
        
        val currentProgress = _archetypeState.value
        val updatedProgress = currentProgress.copy(
            availableTalentPoints = currentProgress.availableTalentPoints + points,
            totalTalentPointsEarned = currentProgress.totalTalentPointsEarned + points
        )
        
        _archetypeState.update { updatedProgress }
        gameStateManager.updatePlayer { it.copy(archetypeProgress = updatedProgress) }
        
        PerformanceLogger.logStateMutation("Archetype", "grantPoints", mapOf(
            "points" to points
        ))
    }
    
    /**
     * Get the total bonus magnitude for a specific talent type.
     * Useful for integrating archetype bonuses with other game systems.
     */
    fun getTotalBonus(talentType: TalentType): Int {
        val talentTree = _archetypeState.value.talentTree ?: return 0
        return talentTree.getTotalBonus(talentType)
    }
    
    /**
     * Get all active bonuses as a map of talent types to their total magnitudes.
     */
    fun getActiveBonuses(): Map<TalentType, Int> {
        val talentTree = _archetypeState.value.talentTree ?: return emptyMap()
        
        return TalentType.values()
            .associateWith { talentTree.getTotalBonus(it) }
            .filterValues { it > 0 }
    }
    
    /**
     * Check if a talent's requirements are met.
     */
    fun checkTalentRequirements(talentId: String): Boolean {
        val currentProgress = _archetypeState.value
        val talentTree = currentProgress.talentTree ?: return false
        
        return talentTree.canUnlockTalent(talentId, currentProgress.archetypeLevel)
    }
    
    /**
     * Get all talents that can be unlocked right now.
     */
    fun getUnlockableTalents(): List<Talent> {
        val currentProgress = _archetypeState.value
        val talentTree = currentProgress.talentTree ?: return emptyList()
        
        return talentTree.talents.filter { talent ->
            talentTree.canUnlockTalent(talent.id, currentProgress.archetypeLevel) &&
            currentProgress.availableTalentPoints >= talent.costInPoints
        }
    }
    
    /**
     * Get the selected archetype type.
     */
    fun getSelectedArchetype(): ArchetypeType? {
        return _archetypeState.value.selectedArchetype
    }
    
    /**
     * Check if player has selected an archetype yet.
     */
    fun hasSelectedArchetype(): Boolean {
        return _archetypeState.value.selectedArchetype != null
    }
}

/**
 * Catalog of pre-defined talent trees for each archetype.
 * This service provides the base talent tree configurations.
 */
class TalentTreeCatalog {
    
    fun getTalentTree(archetypeType: ArchetypeType): TalentTree {
        return when (archetypeType) {
            ArchetypeType.SCHOLAR -> createScholarTree()
            ArchetypeType.COLLECTOR -> createCollectorTree()
            ArchetypeType.ALCHEMIST -> createAlchemistTree()
            ArchetypeType.SCAVENGER -> createScavengerTree()
            ArchetypeType.SOCIALITE -> createSocialiteTree()
            ArchetypeType.WARRIOR -> createWarriorTree()
        }
    }
    
    private fun createScholarTree(): TalentTree {
        val talents = listOf(
            // Tier 1: Starter talents
            Talent(
                id = "scholar_quick_study",
                name = "Quick Study",
                description = "+15% experience gain from all sources",
                talentType = TalentType.GENERAL_XP_BONUS,
                magnitude = 15,
                costInPoints = 1,
                requirements = emptyList()
            ),
            Talent(
                id = "scholar_deep_thought",
                name = "Deep Thought",
                description = "+20% faster thought internalization",
                talentType = TalentType.INTERNALIZATION_SPEED_BONUS,
                magnitude = 20,
                costInPoints = 1,
                requirements = emptyList()
            ),
            
            // Tier 2: Intermediate talents
            Talent(
                id = "scholar_voracious_reader",
                name = "Voracious Reader",
                description = "+25% skill XP gain",
                talentType = TalentType.SKILL_XP_BONUS,
                magnitude = 25,
                costInPoints = 2,
                requirements = listOf(TalentRequirement.Level(3))
            ),
            Talent(
                id = "scholar_philosopher",
                name = "Philosopher",
                description = "Unlock philosophical dialogue options",
                talentType = TalentType.DIALOGUE_UNLOCK,
                magnitude = 1,
                costInPoints = 2,
                requirements = listOf(
                    TalentRequirement.PrerequisiteTalent("scholar_deep_thought"),
                    TalentRequirement.Level(3)
                )
            ),
            
            // Tier 3: Advanced talents
            Talent(
                id = "scholar_enlightened",
                name = "Enlightened",
                description = "+50% XP gain and unlock secret thoughts",
                talentType = TalentType.GENERAL_XP_BONUS,
                magnitude = 50,
                costInPoints = 3,
                requirements = listOf(
                    TalentRequirement.AllTalents(listOf("scholar_quick_study", "scholar_voracious_reader")),
                    TalentRequirement.Level(7)
                )
            )
        )
        
        return TalentTree(
            archetypeType = ArchetypeType.SCHOLAR,
            talents = talents
        )
    }
    
    private fun createCollectorTree(): TalentTree {
        val talents = listOf(
            Talent(
                id = "collector_keen_eye",
                name = "Keen Eye",
                description = "+10% luck for finding rare items",
                talentType = TalentType.LUCK_BONUS,
                magnitude = 10,
                costInPoints = 1,
                requirements = emptyList()
            ),
            Talent(
                id = "collector_hoarder",
                name = "Hoarder's Pride",
                description = "+15% shiny value",
                talentType = TalentType.HOARD_VALUE_BONUS,
                magnitude = 15,
                costInPoints = 1,
                requirements = emptyList()
            ),
            Talent(
                id = "collector_bargain_hunter",
                name = "Bargain Hunter",
                description = "-20% shop prices",
                talentType = TalentType.SHOP_DISCOUNT,
                magnitude = 20,
                costInPoints = 2,
                requirements = listOf(TalentRequirement.Level(3))
            ),
            Talent(
                id = "collector_master_appraiser",
                name = "Master Appraiser",
                description = "+30% sell prices",
                talentType = TalentType.SELL_PRICE_BONUS,
                magnitude = 30,
                costInPoints = 2,
                requirements = listOf(TalentRequirement.Level(5))
            )
        )
        
        return TalentTree(
            archetypeType = ArchetypeType.COLLECTOR,
            talents = talents
        )
    }
    
    private fun createAlchemistTree(): TalentTree {
        val talents = listOf(
            Talent(
                id = "alchemist_steady_hand",
                name = "Steady Hand",
                description = "+20% crafting success rate",
                talentType = TalentType.CRAFTING_COST_REDUCTION,
                magnitude = 20,
                costInPoints = 1,
                requirements = emptyList()
            ),
            Talent(
                id = "alchemist_master_brewer",
                name = "Master Brewer",
                description = "Unlock rare concoction recipes",
                talentType = TalentType.RECIPE_UNLOCK,
                magnitude = 1,
                costInPoints = 2,
                requirements = listOf(TalentRequirement.Level(4))
            )
        )
        
        return TalentTree(
            archetypeType = ArchetypeType.ALCHEMIST,
            talents = talents
        )
    }
    
    private fun createScavengerTree(): TalentTree {
        val talents = listOf(
            Talent(
                id = "scavenger_forager",
                name = "Expert Forager",
                description = "+25% ingredient yield from harvesting",
                talentType = TalentType.INGREDIENT_YIELD_BONUS,
                magnitude = 25,
                costInPoints = 1,
                requirements = emptyList()
            ),
            Talent(
                id = "scavenger_swift",
                name = "Swift Feet",
                description = "+15% movement speed",
                talentType = TalentType.MOVEMENT_SPEED_BONUS,
                magnitude = 15,
                costInPoints = 1,
                requirements = emptyList()
            )
        )
        
        return TalentTree(
            archetypeType = ArchetypeType.SCAVENGER,
            talents = talents
        )
    }
    
    private fun createSocialiteTree(): TalentTree {
        val talents = listOf(
            Talent(
                id = "socialite_charming",
                name = "Natural Charm",
                description = "+20% companion affinity gain",
                talentType = TalentType.COMPANION_AFFINITY_BONUS,
                magnitude = 20,
                costInPoints = 1,
                requirements = emptyList()
            ),
            Talent(
                id = "socialite_silver_tongue",
                name = "Silver Tongue",
                description = "Unlock persuasion dialogue options",
                talentType = TalentType.DIALOGUE_UNLOCK,
                magnitude = 1,
                costInPoints = 2,
                requirements = listOf(TalentRequirement.Level(3))
            )
        )
        
        return TalentTree(
            archetypeType = ArchetypeType.SOCIALITE,
            talents = talents
        )
    }
    
    private fun createWarriorTree(): TalentTree {
        val talents = listOf(
            Talent(
                id = "warrior_strong",
                name = "Mighty Strikes",
                description = "+20% damage in combat",
                talentType = TalentType.DAMAGE_BONUS,
                magnitude = 20,
                costInPoints = 1,
                requirements = emptyList()
            ),
            Talent(
                id = "warrior_tough",
                name = "Iron Feathers",
                description = "+20% defense in combat",
                talentType = TalentType.DEFENSE_BONUS,
                magnitude = 20,
                costInPoints = 1,
                requirements = emptyList()
            ),
            Talent(
                id = "warrior_hardy",
                name = "Hardy Constitution",
                description = "+30 maximum health",
                talentType = TalentType.HEALTH_BONUS,
                magnitude = 30,
                costInPoints = 2,
                requirements = listOf(TalentRequirement.Level(4))
            )
        )
        
        return TalentTree(
            archetypeType = ArchetypeType.WARRIOR,
            talents = talents
        )
    }
}
