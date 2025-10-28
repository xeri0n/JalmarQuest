package com.jalmarquest.core.state.managers

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.perf.PerformanceLogger
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

/**
 * Alpha 2.3 Part 3.2-3.4: Companion Task Assignment System
 * 
 * Manages companion assignments to passive tasks with:
 * - Advanced profit formula using trait levels and station tiers
 * - Time investment tracking for perfection meter
 * - Task completion and reward calculation
 */
class CompanionTaskAssignmentManager(
    private val gameStateManager: GameStateManager,
    private val traitManager: CompanionTraitManager,
    private val timestampProvider: () -> Long
) {
    private val mutex = Mutex()
    
    val playerState: StateFlow<Player> = gameStateManager.playerState
    
    /**
     * Alpha 2.3 Part 3.2: Assign a companion to a task.
     * 
     * @param companionId The companion to assign
     * @param taskType Type of task to perform
     * @param duration Duration in milliseconds
     * @return Result indicating success or failure reason
     */
    suspend fun assignCompanionToTask(
        companionId: CompanionId,
        taskType: CompanionTaskType,
        duration: Long
    ): AssignmentResult = mutex.withLock {
        val startTime = timestampProvider()
        val player = gameStateManager.playerState.value
        
        // Validate companion is recruited
        if (!player.companionState.isCompanionRecruited(companionId)) {
            return AssignmentResult.CompanionNotRecruited
        }
        
        // Check assignment board capacity
        val maxAssignments = player.nestCustomization.getMaxCompanionAssignments()
        if (maxAssignments == 0) {
            return AssignmentResult.NoBoardUpgrade
        }
        
        val currentAssignments = player.companionAssignments.activeAssignments.size
        if (currentAssignments >= maxAssignments) {
            return AssignmentResult.TooManyAssignments(maxAssignments)
        }
        
        // Check if companion is already assigned
        if (player.companionAssignments.activeAssignments.any { it.companionId == companionId }) {
            return AssignmentResult.CompanionAlreadyAssigned
        }
        
        // Create assignment
        val assignment = CompanionTaskAssignment(
            companionId = companionId,
            taskType = taskType,
            startTime = timestampProvider(),
            duration = duration
        )
        
        // Update player state
        gameStateManager.updateCompanionAssignments { assignments ->
            assignments.copy(
                activeAssignments = assignments.activeAssignments + assignment
            )
        }
        
        // Log analytics
        gameStateManager.appendChoice(
            "companion_task_assign_${companionId.value}_${taskType.name}_${duration / 1000}s"
        )
        
        PerformanceLogger.logStateMutation(
            "CompanionTaskAssignmentManager",
            "assignCompanionToTask",
            mapOf(
                "companion" to companionId.value,
                "task" to taskType.name,
                "duration_ms" to duration.toString(),
                "operation_time_ms" to (timestampProvider() - startTime).toString()
            )
        )
        
        return AssignmentResult.Success(assignment)
    }
    
    /**
     * Alpha 2.3 Part 3.3: Complete a task and calculate rewards using advanced profit formula.
     * 
     * Formula: BaseReward × TraitBonus × StationTierBonus × TimeInvestmentBonus × PerfectionBonus
     * 
     * Where:
     * - BaseReward: Task-specific base value (seeds, items, etc.)
     * - TraitBonus: Companion's trait level multiplier (1.0x - 2.5x)
     * - StationTierBonus: Assignment board tier bonus (1.0x/1.1x/1.25x)
     * - TimeInvestmentBonus: Longer tasks = better rewards (1.0x - 2.0x)
     * - PerfectionBonus: Hidden meter for optimization (0% - 20% bonus)
     */
    suspend fun completeTask(
        assignment: CompanionTaskAssignment
    ): CompletionResult = mutex.withLock {
        val startTime = timestampProvider()
        val player = gameStateManager.playerState.value
        
        // Verify assignment exists
        if (!player.companionAssignments.activeAssignments.contains(assignment)) {
            return CompletionResult.AssignmentNotFound
        }
        
        // Check if task is complete
        val currentTime = timestampProvider()
        if (!assignment.isComplete(currentTime)) {
            val remainingTime = assignment.getRemainingTime(currentTime)
            return CompletionResult.NotYetComplete(remainingTime)
        }
        
        // Calculate rewards using advanced formula
        val rewards = calculateTaskRewards(assignment, player)
        
        // Award trait XP
        val traitXpResult = traitManager.awardTraitXp(
            assignment.companionId,
            assignment.taskType.associatedTrait(),
            rewards.traitXp
        )
        
        // Apply rewards to player
        applyRewards(rewards)
        
        // Update perfection meter
        updatePerfectionMeter(assignment, player)
        
        // Remove assignment
        gameStateManager.updateCompanionAssignments { assignments ->
            assignments.copy(
                activeAssignments = assignments.activeAssignments - assignment,
                completedTaskCount = assignments.completedTaskCount + 1
            )
        }
        
        // Log analytics
        gameStateManager.appendChoice(
            "companion_task_complete_${assignment.companionId.value}_${assignment.taskType.name}"
        )
        
        PerformanceLogger.logStateMutation(
            "CompanionTaskAssignmentManager",
            "completeTask",
            mapOf(
                "companion" to assignment.companionId.value,
                "task" to assignment.taskType.name,
                "seeds_earned" to rewards.seeds.toString(),
                "perfection_bonus" to rewards.perfectionBonus.toString(),
                "operation_time_ms" to (timestampProvider() - startTime).toString()
            )
        )
        
        return CompletionResult.Success(rewards, traitXpResult)
    }
    
    /**
     * Alpha 2.3 Part 3.3: Calculate task rewards using advanced profit formula.
     */
    private fun calculateTaskRewards(
        assignment: CompanionTaskAssignment,
        player: Player
    ): TaskRewards {
        // Base rewards per task type
        val baseSeeds = when (assignment.taskType) {
            CompanionTaskType.FORAGING -> 50
            CompanionTaskType.SCOUTING -> 40
            CompanionTaskType.BREWING -> 30
            CompanionTaskType.SMITHING -> 35
            CompanionTaskType.COMBAT -> 60
            CompanionTaskType.TRADING -> 45
            CompanionTaskType.SCHOLARSHIP -> 25
            CompanionTaskType.EXPLORATION -> 55
        }
        
        // 1. Trait Bonus (1.0x - 2.5x based on trait level)
        val traitBonus = traitManager.getTraitBonus(
            assignment.companionId,
            assignment.taskType.associatedTrait()
        )
        
        // 2. Station Tier Bonus (depends on COMPANION_ASSIGNMENT_BOARD tier)
        val boardTier = player.nestCustomization.functionalUpgrades[FunctionalUpgradeType.COMPANION_ASSIGNMENT_BOARD]?.currentTier
        val stationBonus = when (boardTier) {
            UpgradeTier.TIER_1 -> 1.0f
            UpgradeTier.TIER_2 -> 1.1f
            UpgradeTier.TIER_3 -> 1.25f
            null -> 1.0f
        }
        
        // 3. Alpha 2.3 Part 3.4: Time Investment Bonus (longer tasks = better rewards)
        // Short task (< 1 hour) = 1.0x
        // Medium task (1-6 hours) = 1.2x
        // Long task (6-24 hours) = 1.5x
        // Very long task (24+ hours) = 2.0x
        val hoursDuration = assignment.duration / (1000 * 60 * 60)
        val timeBonus = when {
            hoursDuration < 1 -> 1.0f
            hoursDuration < 6 -> 1.2f
            hoursDuration < 24 -> 1.5f
            else -> 2.0f
        }
        
        // 4. Alpha 2.3 Part 3.4: Perfection Bonus (hidden meter 0% - 20%)
        val perfectionBonus = calculatePerfectionBonus(player)
        
        // Apply formula: BaseReward × TraitBonus × StationBonus × TimeBonus × (1 + PerfectionBonus)
        val totalMultiplier = traitBonus * stationBonus * timeBonus * (1.0f + perfectionBonus)
        val finalSeeds = (baseSeeds * totalMultiplier).toInt()
        
        // Trait XP award (scales with task duration)
        val traitXp = (15 * timeBonus).toInt()
        
        return TaskRewards(
            seeds = finalSeeds,
            traitXp = traitXp,
            traitBonus = traitBonus,
            stationBonus = stationBonus,
            timeBonus = timeBonus,
            perfectionBonus = perfectionBonus
        )
    }
    
    /**
     * Alpha 2.3 Part 3.4: Calculate perfection bonus based on player's optimization.
     * 
     * Perfection increases when:
     * - Companions are assigned to tasks matching their highest traits
     * - Tasks are completed without early cancellation
     * - Longer tasks are preferred
     * - All assignment slots are utilized
     */
    private fun calculatePerfectionBonus(player: Player): Float {
        val perfectionMeter = player.companionAssignments.perfectionMeter
        // Convert 0-100 meter to 0.0-0.2 (0% - 20% bonus)
        return (perfectionMeter / 100f) * 0.2f
    }
    
    /**
     * Alpha 2.3 Part 3.4: Update perfection meter based on task completion quality.
     */
    private suspend fun updatePerfectionMeter(
        assignment: CompanionTaskAssignment,
        player: Player
    ) {
        val currentMeter = player.companionAssignments.perfectionMeter
        
        // Calculate quality score (0-10)
        var qualityScore = 5f // Base score
        
        // Bonus: Task matches companion's highest trait
        val highestTrait = getHighestTrait(assignment.companionId, player)
        if (highestTrait == assignment.taskType.associatedTrait()) {
            qualityScore += 2f
        }
        
        // Bonus: Long task (6+ hours)
        val hoursDuration = assignment.duration / (1000 * 60 * 60)
        if (hoursDuration >= 6) {
            qualityScore += 2f
        }
        
        // Bonus: All slots utilized
        val maxSlots = player.nestCustomization.getMaxCompanionAssignments()
        val usedSlots = player.companionAssignments.activeAssignments.size
        if (usedSlots >= maxSlots && maxSlots > 0) {
            qualityScore += 1f
        }
        
        // Update meter (gradual increase towards quality score × 10)
        val targetMeter = (qualityScore * 10).toInt().coerceIn(0, 100)
        val newMeter = ((currentMeter * 0.9f) + (targetMeter * 0.1f)).toInt().coerceIn(0, 100)
        
        gameStateManager.updateCompanionAssignments { assignments ->
            assignments.copy(perfectionMeter = newMeter)
        }
    }
    
    /**
     * Get companion's highest trait.
     */
    private fun getHighestTrait(companionId: CompanionId, player: Player): CompanionTrait? {
        val progress = player.companionState.getProgress(companionId) ?: return null
        return progress.traits.values
            .maxByOrNull { it.level.level }
            ?.trait
    }
    
    /**
     * Apply rewards to player inventory.
     */
    private suspend fun applyRewards(rewards: TaskRewards) {
        // Award seeds
        gameStateManager.updateSeedInventory { inventory ->
            inventory.copy(storedSeeds = inventory.storedSeeds + rewards.seeds)
        }
    }
    
    /**
     * Cancel an active assignment.
     */
    suspend fun cancelAssignment(companionId: CompanionId): CancelResult = mutex.withLock {
        val player = gameStateManager.playerState.value
        val assignment = player.companionAssignments.activeAssignments
            .find { it.companionId == companionId }
            ?: return CancelResult.NotAssigned
        
        gameStateManager.updateCompanionAssignments { assignments ->
            assignments.copy(
                activeAssignments = assignments.activeAssignments - assignment
            )
        }
        
        // Penalty: Reduce perfection meter on cancellation
        val currentMeter = player.companionAssignments.perfectionMeter
        val newMeter = (currentMeter * 0.9f).toInt().coerceIn(0, 100)
        
        gameStateManager.updateCompanionAssignments { assignments ->
            assignments.copy(perfectionMeter = newMeter)
        }
        
        return CancelResult.Success
    }
}

/**
 * Alpha 2.3 Part 3.3: Calculated task rewards.
 */
data class TaskRewards(
    val seeds: Int,
    val traitXp: Int,
    val traitBonus: Float,
    val stationBonus: Float,
    val timeBonus: Float,
    val perfectionBonus: Float
)

/**
 * Result of assigning a companion to a task.
 */
sealed class AssignmentResult {
    data class Success(val assignment: CompanionTaskAssignment) : AssignmentResult()
    data object CompanionNotRecruited : AssignmentResult()
    data object NoBoardUpgrade : AssignmentResult()
    data class TooManyAssignments(val maxAllowed: Int) : AssignmentResult()
    data object CompanionAlreadyAssigned : AssignmentResult()
}

/**
 * Result of completing a task.
 */
sealed class CompletionResult {
    data class Success(val rewards: TaskRewards, val traitXpResult: TraitXpResult) : CompletionResult()
    data object AssignmentNotFound : CompletionResult()
    data class NotYetComplete(val remainingTime: Long) : CompletionResult()
}

/**
 * Result of canceling an assignment.
 */
sealed class CancelResult {
    data object Success : CancelResult()
    data object NotAssigned : CancelResult()
}
