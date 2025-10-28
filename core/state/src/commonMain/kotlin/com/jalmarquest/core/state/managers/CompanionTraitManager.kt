package com.jalmarquest.core.state.managers

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.perf.PerformanceLogger
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Alpha 2.3 Part 3.1: Companion Trait Manager
 * 
 * Manages companion trait progression when companions complete tasks.
 * Traits improve from level 1 to 10, providing scaling bonuses.
 */
class CompanionTraitManager(
    private val gameStateManager: GameStateManager,
    private val timestampProvider: () -> Long
) {
    private val mutex = Mutex()
    
    val playerState: StateFlow<Player> = gameStateManager.playerState
    
    /**
     * Award trait XP to a companion for completing a task.
     * Automatically levels up trait if enough XP is earned.
     * 
     * @param companionId The companion who completed the task
     * @param trait The trait that was trained
     * @param xpAmount Amount of XP to award (typically 10-50 per task)
     * @return Result indicating success, level-ups, and new level
     */
    suspend fun awardTraitXp(
        companionId: CompanionId,
        trait: CompanionTrait,
        xpAmount: Int
    ): TraitXpResult = mutex.withLock {
        val startTime = timestampProvider()
        
        if (xpAmount <= 0) {
            return TraitXpResult.InvalidAmount
        }
        
        val player = gameStateManager.playerState.value
        val companionProgress = player.companionState.getProgress(companionId)
            ?: return TraitXpResult.CompanionNotRecruited
        
        // Get current trait progress (or initialize at level 1)
        val currentProgress = companionProgress.getTraitProgress(trait)
            ?: TraitProgress(trait = trait, level = TraitLevel(1), currentXp = 0)
        
        // Add XP
        var newProgress = currentProgress.addXp(xpAmount)
        var levelsGained = 0
        val levelUpsAchieved = mutableListOf<Int>() // Track each level for logging
        
        // Auto-level up if enough XP
        while (newProgress.canLevelUp()) {
            newProgress = newProgress.levelUp()
            levelsGained++
            levelUpsAchieved.add(newProgress.level.level)
        }
        
        // Update companion progress with new trait data
        val updatedTraits = companionProgress.traits + (trait.name to newProgress)
        val updatedCompanionProgress = companionProgress.copy(traits = updatedTraits)
        
        // Update player state
        gameStateManager.updateCompanionState { companionState ->
            val updatedCompanions = companionState.recruitedCompanions + 
                (companionId.value to updatedCompanionProgress)
            companionState.copy(recruitedCompanions = updatedCompanions)
        }
        
        // Log analytics - log each level up separately
        for (achievedLevel in levelUpsAchieved) {
            gameStateManager.appendChoice(
                "companion_trait_levelup_${companionId.value}_${trait.name}_${achievedLevel}"
            )
        }
        
        PerformanceLogger.logStateMutation(
            "CompanionTraitManager",
            "awardTraitXp",
            mapOf(
                "companion" to companionId.value,
                "trait" to trait.name,
                "xp" to xpAmount.toString(),
                "levels_gained" to levelsGained.toString(),
                "new_level" to newProgress.level.level.toString(),
                "duration_ms" to (timestampProvider() - startTime).toString()
            )
        )
        
        return TraitXpResult.Success(
            newLevel = newProgress.level,
            levelsGained = levelsGained,
            currentXp = newProgress.currentXp,
            xpToNextLevel = newProgress.level.getXpToNextLevel()
        )
    }
    
    /**
     * Complete a task with a companion, awarding appropriate trait XP.
     * Task difficulty determines XP award.
     * 
     * @param companionId The companion completing the task
     * @param taskType Type of task (determines which trait gets XP)
     * @param taskDifficulty Difficulty multiplier (1.0 = normal, 2.0 = hard, etc.)
     * @return Result with XP awarded and any level-ups
     */
    suspend fun completeTask(
        companionId: CompanionId,
        taskType: CompanionTaskType,
        taskDifficulty: Float = 1.0f
    ): TraitXpResult {
        val trait = taskType.associatedTrait()
        val baseXp = when (taskType) {
            CompanionTaskType.FORAGING -> 15
            CompanionTaskType.SCOUTING -> 20
            CompanionTaskType.BREWING -> 25
            CompanionTaskType.SMITHING -> 30
            CompanionTaskType.COMBAT -> 35
            CompanionTaskType.TRADING -> 20
            CompanionTaskType.SCHOLARSHIP -> 40
            CompanionTaskType.EXPLORATION -> 25
        }
        
        val xpAmount = (baseXp * taskDifficulty).toInt()
        return awardTraitXp(companionId, trait, xpAmount)
    }
    
    /**
     * Get the current level of a trait for a companion.
     */
    fun getTraitLevel(companionId: CompanionId, trait: CompanionTrait): TraitLevel {
        val player = gameStateManager.playerState.value
        return player.companionState.getProgress(companionId)?.getTraitLevel(trait) 
            ?: TraitLevel(1)
    }
    
    /**
     * Get the bonus multiplier for a companion's trait.
     */
    fun getTraitBonus(companionId: CompanionId, trait: CompanionTrait): Float {
        val player = gameStateManager.playerState.value
        return player.companionState.getProgress(companionId)?.getTraitBonus(trait) 
            ?: 1.0f
    }
    
    /**
     * Get all traits for a companion with their current progress.
     */
    fun getAllTraits(companionId: CompanionId): Map<CompanionTrait, TraitProgress> {
        val player = gameStateManager.playerState.value
        val companionProgress = player.companionState.getProgress(companionId) ?: return emptyMap()
        
        return CompanionTrait.values().associateWith { trait ->
            companionProgress.getTraitProgress(trait) 
                ?: TraitProgress(trait = trait, level = TraitLevel(1), currentXp = 0)
        }
    }
}

/**
 * Result of awarding trait XP.
 */
sealed class TraitXpResult {
    data class Success(
        val newLevel: TraitLevel,
        val levelsGained: Int,
        val currentXp: Int,
        val xpToNextLevel: Int
    ) : TraitXpResult()
    
    data object CompanionNotRecruited : TraitXpResult()
    data object InvalidAmount : TraitXpResult()
}
