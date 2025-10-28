package com.jalmarquest.core.state.managers

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Alpha 2.3 Part 3.2-3.4: Tests for companion task assignment system.
 * 
 * Tests:
 * - Part 3.2: Assignment Board UI logic (assign, capacity, validation)
 * - Part 3.3: Advanced Profit Formula (trait bonus, station bonus, time bonus, perfection bonus)
 * - Part 3.4: Time Investment & Perfection System (meter tracking, bonuses)
 */
class CompanionTaskAssignmentManagerTest {
    
    private lateinit var gameStateManager: GameStateManager
    private lateinit var traitManager: CompanionTraitManager
    private lateinit var assignmentManager: CompanionTaskAssignmentManager
    private var currentTime = 0L
    
    private val testCompanionId = CompanionId("test_companion")
    
    @BeforeTest
    fun setup() {
        currentTime = 1000000L
        gameStateManager = GameStateManager(
            initialPlayer = testPlayer(),
            accountManager = null,
            timestampProvider = { currentTime }
        )
        
        traitManager = CompanionTraitManager(
            gameStateManager = gameStateManager,
            timestampProvider = { currentTime }
        )
        
        assignmentManager = CompanionTaskAssignmentManager(
            gameStateManager = gameStateManager,
            traitManager = traitManager,
            timestampProvider = { currentTime }
        )
    }
    
    private fun testPlayer(): Player {
        // Create player with recruited companion
        val companionProgress = CompanionProgress(
            companionId = testCompanionId,
            affinity = 50,
            isRecruited = true,
            unlockedAbilities = emptyList(),
            traits = emptyMap()
        )
        
        return Player(
            id = "test",
            name = "Test Player",
            level = 5,
            companionState = CompanionState(
                recruitedCompanions = mapOf(testCompanionId.value to companionProgress)
            ),
            nestCustomization = NestCustomizationState(
                functionalUpgrades = mapOf(
                    FunctionalUpgradeType.COMPANION_ASSIGNMENT_BOARD to FunctionalUpgrade(
                        type = FunctionalUpgradeType.COMPANION_ASSIGNMENT_BOARD,
                        cosmeticItemId = CosmeticItemId("board_1"),
                        currentTier = UpgradeTier.TIER_1,
                        isActive = true
                    )
                )
            )
        )
    }
    
    // ==================== Part 3.2: Assignment Board Tests ====================
    
    @Test
    fun `assign companion to task creates active assignment`() = runTest {
        val result = assignmentManager.assignCompanionToTask(
            companionId = testCompanionId,
            taskType = CompanionTaskType.FORAGING,
            duration = 3600000L // 1 hour
        )
        
        assertTrue(result is AssignmentResult.Success)
        val assignment = (result as AssignmentResult.Success).assignment
        
        assertEquals(testCompanionId, assignment.companionId)
        assertEquals(CompanionTaskType.FORAGING, assignment.taskType)
        assertEquals(3600000L, assignment.duration)
        
        // Verify assignment is in player state
        val player = gameStateManager.playerState.value
        assertTrue(player.companionAssignments.activeAssignments.contains(assignment))
    }
    
    @Test
    fun `cannot assign non-recruited companion`() = runTest {
        val unknownCompanion = CompanionId("unknown")
        
        val result = assignmentManager.assignCompanionToTask(
            companionId = unknownCompanion,
            taskType = CompanionTaskType.FORAGING,
            duration = 3600000L
        )
        
        assertTrue(result is AssignmentResult.CompanionNotRecruited)
    }
    
    @Test
    fun `cannot assign without assignment board upgrade`() = runTest {
        // Remove assignment board upgrade
        gameStateManager.updateNestCustomization { nest ->
            nest.copy(functionalUpgrades = emptyMap())
        }
        
        val result = assignmentManager.assignCompanionToTask(
            companionId = testCompanionId,
            taskType = CompanionTaskType.FORAGING,
            duration = 3600000L
        )
        
        assertTrue(result is AssignmentResult.NoBoardUpgrade)
    }
    
    @Test
    fun `respects max concurrent assignments based on tier`() = runTest {
        // Tier 1 allows 2 concurrent assignments
        val companion1 = CompanionId("companion1")
        val companion2 = CompanionId("companion2")
        val companion3 = CompanionId("companion3")
        
        // Add 3 recruited companions
        gameStateManager.updateCompanionState { state ->
            state.copy(
                recruitedCompanions = mapOf(
                    companion1.value to CompanionProgress(companion1, affinity = 0, isRecruited = true),
                    companion2.value to CompanionProgress(companion2, affinity = 0, isRecruited = true),
                    companion3.value to CompanionProgress(companion3, affinity = 0, isRecruited = true)
                )
            )
        }
        
        // First assignment succeeds
        val result1 = assignmentManager.assignCompanionToTask(companion1, CompanionTaskType.FORAGING, 3600000L)
        assertTrue(result1 is AssignmentResult.Success)
        
        // Second assignment succeeds (at limit)
        val result2 = assignmentManager.assignCompanionToTask(companion2, CompanionTaskType.SCOUTING, 3600000L)
        assertTrue(result2 is AssignmentResult.Success)
        
        // Third assignment fails (over limit)
        val result3 = assignmentManager.assignCompanionToTask(companion3, CompanionTaskType.COMBAT, 3600000L)
        assertTrue(result3 is AssignmentResult.TooManyAssignments)
        assertEquals(2, (result3 as AssignmentResult.TooManyAssignments).maxAllowed)
    }
    
    @Test
    fun `cannot assign already assigned companion`() = runTest {
        // First assignment
        assignmentManager.assignCompanionToTask(testCompanionId, CompanionTaskType.FORAGING, 3600000L)
        
        // Try to assign same companion again
        val result = assignmentManager.assignCompanionToTask(testCompanionId, CompanionTaskType.SCOUTING, 3600000L)
        
        assertTrue(result is AssignmentResult.CompanionAlreadyAssigned)
    }
    
    @Test
    fun `assignment logs choice tag`() = runTest {
        assignmentManager.assignCompanionToTask(testCompanionId, CompanionTaskType.FORAGING, 3600000L)
        
        val choices = gameStateManager.playerState.value.choiceLog.entries
        assertTrue(choices.any { 
            it.tag.value.startsWith("companion_task_assign_${testCompanionId.value}_FORAGING_")
        })
    }
    
    // ==================== Part 3.3: Profit Formula Tests ====================
    
    @Test
    fun `task rewards scale with trait level`() = runTest {
        // Set up companion with level 1 foraging trait
        traitManager.awardTraitXp(testCompanionId, CompanionTrait.FORAGING, 0)
        
        val assignment = CompanionTaskAssignment(
            companionId = testCompanionId,
            taskType = CompanionTaskType.FORAGING,
            startTime = currentTime,
            duration = 3600000L // 1 hour
        )
        
        gameStateManager.updateCompanionAssignments { state ->
            state.copy(activeAssignments = listOf(assignment))
        }
        
        // Complete task after 1 hour
        currentTime += 3600000L
        
        val result = assignmentManager.completeTask(assignment)
        assertTrue(result is CompletionResult.Success)
        
        val rewards = (result as CompletionResult.Success).rewards
        // Base foraging = 50 seeds, trait level 1 = 1.0x, tier 1 = 1.0x, medium time = 1.2x
        // 50 * 1.0 * 1.0 * 1.2 = 60 seeds
        assertEquals(60, rewards.seeds)
    }
    
    @Test
    fun `task rewards scale with station tier`() = runTest {
        // Upgrade assignment board to tier 2
        gameStateManager.updateNestCustomization { nest ->
            nest.copy(
                functionalUpgrades = mapOf(
                    FunctionalUpgradeType.COMPANION_ASSIGNMENT_BOARD to FunctionalUpgrade(
                        type = FunctionalUpgradeType.COMPANION_ASSIGNMENT_BOARD,
                        cosmeticItemId = CosmeticItemId("board_1"),
                        currentTier = UpgradeTier.TIER_2,
                        isActive = true
                    )
                )
            )
        }
        
        val assignment = CompanionTaskAssignment(
            companionId = testCompanionId,
            taskType = CompanionTaskType.FORAGING,
            startTime = currentTime,
            duration = 3600000L
        )
        
        gameStateManager.updateCompanionAssignments { state ->
            state.copy(activeAssignments = listOf(assignment))
        }
        
        currentTime += 3600000L
        val result = assignmentManager.completeTask(assignment)
        
        val rewards = (result as CompletionResult.Success).rewards
        assertEquals(1.1f, rewards.stationBonus) // Tier 2 bonus
    }
    
    @Test
    fun `task rewards scale with time investment`() = runTest {
        // Short task (30 minutes) = 1.0x
        val shortAssignment = CompanionTaskAssignment(
            companionId = testCompanionId,
            taskType = CompanionTaskType.FORAGING,
            startTime = currentTime,
            duration = 1800000L // 30 minutes
        )
        
        gameStateManager.updateCompanionAssignments { state ->
            state.copy(activeAssignments = listOf(shortAssignment))
        }
        
        currentTime += 1800000L
        val shortResult = assignmentManager.completeTask(shortAssignment)
        val shortRewards = (shortResult as CompletionResult.Success).rewards
        
        assertEquals(1.0f, shortRewards.timeBonus)
        
        // Long task (12 hours) = 1.5x
        currentTime = 1000000L // Reset time
        val longAssignment = CompanionTaskAssignment(
            companionId = testCompanionId,
            taskType = CompanionTaskType.FORAGING,
            startTime = currentTime,
            duration = 43200000L // 12 hours
        )
        
        gameStateManager.updateCompanionAssignments { state ->
            state.copy(activeAssignments = listOf(longAssignment))
        }
        
        currentTime += 43200000L
        val longResult = assignmentManager.completeTask(longAssignment)
        val longRewards = (longResult as CompletionResult.Success).rewards
        
        assertEquals(1.5f, longRewards.timeBonus)
    }
    
    @Test
    fun `different task types have different base rewards`() = runTest {
        // COMBAT has highest base (60), SCHOLARSHIP has lowest (25)
        val combatAssignment = CompanionTaskAssignment(
            companionId = testCompanionId,
            taskType = CompanionTaskType.COMBAT,
            startTime = currentTime,
            duration = 1800000L
        )
        
        gameStateManager.updateCompanionAssignments { state ->
            state.copy(activeAssignments = listOf(combatAssignment))
        }
        
        currentTime += 1800000L
        val combatResult = assignmentManager.completeTask(combatAssignment)
        val combatSeeds = (combatResult as CompletionResult.Success).rewards.seeds
        
        // Reset for scholarship
        currentTime = 1000000L
        val scholarshipAssignment = CompanionTaskAssignment(
            companionId = testCompanionId,
            taskType = CompanionTaskType.SCHOLARSHIP,
            startTime = currentTime,
            duration = 1800000L
        )
        
        gameStateManager.updateCompanionAssignments { state ->
            state.copy(activeAssignments = listOf(scholarshipAssignment))
        }
        
        currentTime += 1800000L
        val scholarshipResult = assignmentManager.completeTask(scholarshipAssignment)
        val scholarshipSeeds = (scholarshipResult as CompletionResult.Success).rewards.seeds
        
        // Combat should earn more than scholarship
        assertTrue(combatSeeds > scholarshipSeeds)
    }
    
    @Test
    fun `task completion awards trait XP`() = runTest {
        val assignment = CompanionTaskAssignment(
            companionId = testCompanionId,
            taskType = CompanionTaskType.FORAGING,
            startTime = currentTime,
            duration = 3600000L
        )
        
        gameStateManager.updateCompanionAssignments { state ->
            state.copy(activeAssignments = listOf(assignment))
        }
        
        currentTime += 3600000L
        assignmentManager.completeTask(assignment)
        
        // Verify trait XP was awarded
        val traitLevel = traitManager.getTraitLevel(testCompanionId, CompanionTrait.FORAGING)
        assertTrue(traitLevel.level >= 1)
    }
    
    // ==================== Part 3.4: Perfection System Tests ====================
    
    @Test
    fun `perfection meter starts at 0`() = runTest {
        val player = gameStateManager.playerState.value
        assertEquals(0, player.companionAssignments.perfectionMeter)
    }
    
    @Test
    fun `perfection meter increases with quality completions`() = runTest {
        // Train foraging trait to make it companion's highest
        traitManager.awardTraitXp(testCompanionId, CompanionTrait.FORAGING, 500)
        
        // Assign long task matching highest trait
        val assignment = CompanionTaskAssignment(
            companionId = testCompanionId,
            taskType = CompanionTaskType.FORAGING,
            startTime = currentTime,
            duration = 43200000L // 12 hours
        )
        
        gameStateManager.updateCompanionAssignments { state ->
            state.copy(activeAssignments = listOf(assignment))
        }
        
        currentTime += 43200000L
        assignmentManager.completeTask(assignment)
        
        val newMeter = gameStateManager.playerState.value.companionAssignments.perfectionMeter
        assertTrue(newMeter > 0, "Perfection meter should increase after quality completion")
    }
    
    @Test
    fun `perfection bonus increases rewards`() = runTest {
        // Manually set high perfection meter
        gameStateManager.updateCompanionAssignments { state ->
            state.copy(perfectionMeter = 100)
        }
        
        val assignment = CompanionTaskAssignment(
            companionId = testCompanionId,
            taskType = CompanionTaskType.FORAGING,
            startTime = currentTime,
            duration = 3600000L
        )
        
        gameStateManager.updateCompanionAssignments { state ->
            state.copy(activeAssignments = listOf(assignment))
        }
        
        currentTime += 3600000L
        val result = assignmentManager.completeTask(assignment)
        
        val rewards = (result as CompletionResult.Success).rewards
        // Perfection meter 100 = 0.2 (20% bonus)
        assertEquals(0.2f, rewards.perfectionBonus)
        
        // Verify bonus is applied to seeds
        // Base 50 * 1.0 trait * 1.0 station * 1.2 time * 1.2 perfection = 72
        assertEquals(72, rewards.seeds)
    }
    
    @Test
    fun `canceling assignment reduces perfection meter`() = runTest {
        // Set initial perfection
        gameStateManager.updateCompanionAssignments { state ->
            state.copy(perfectionMeter = 50)
        }
        
        // Assign and then cancel
        assignmentManager.assignCompanionToTask(testCompanionId, CompanionTaskType.FORAGING, 3600000L)
        assignmentManager.cancelAssignment(testCompanionId)
        
        val newMeter = gameStateManager.playerState.value.companionAssignments.perfectionMeter
        assertTrue(newMeter < 50, "Canceling should reduce perfection meter")
    }
    
    @Test
    fun `completed task count increments`() = runTest {
        val assignment = CompanionTaskAssignment(
            companionId = testCompanionId,
            taskType = CompanionTaskType.FORAGING,
            startTime = currentTime,
            duration = 3600000L
        )
        
        gameStateManager.updateCompanionAssignments { state ->
            state.copy(activeAssignments = listOf(assignment))
        }
        
        val initialCount = gameStateManager.playerState.value.companionAssignments.completedTaskCount
        
        currentTime += 3600000L
        assignmentManager.completeTask(assignment)
        
        val newCount = gameStateManager.playerState.value.companionAssignments.completedTaskCount
        assertEquals(initialCount + 1, newCount)
    }
    
    @Test
    fun `cannot complete task before duration ends`() = runTest {
        val assignment = CompanionTaskAssignment(
            companionId = testCompanionId,
            taskType = CompanionTaskType.FORAGING,
            startTime = currentTime,
            duration = 3600000L
        )
        
        gameStateManager.updateCompanionAssignments { state ->
            state.copy(activeAssignments = listOf(assignment))
        }
        
        // Try to complete after 30 minutes (task needs 1 hour)
        currentTime += 1800000L
        
        val result = assignmentManager.completeTask(assignment)
        assertTrue(result is CompletionResult.NotYetComplete)
        
        val remainingTime = (result as CompletionResult.NotYetComplete).remainingTime
        assertEquals(1800000L, remainingTime) // 30 minutes remaining
    }
    
    @Test
    fun `task assignment removes assignment from active list on completion`() = runTest {
        val assignment = CompanionTaskAssignment(
            companionId = testCompanionId,
            taskType = CompanionTaskType.FORAGING,
            startTime = currentTime,
            duration = 3600000L
        )
        
        gameStateManager.updateCompanionAssignments { state ->
            state.copy(activeAssignments = listOf(assignment))
        }
        
        currentTime += 3600000L
        assignmentManager.completeTask(assignment)
        
        val activeAssignments = gameStateManager.playerState.value.companionAssignments.activeAssignments
        assertFalse(activeAssignments.contains(assignment))
    }
    
    @Test
    fun `task completion logs choice tag`() = runTest {
        val assignment = CompanionTaskAssignment(
            companionId = testCompanionId,
            taskType = CompanionTaskType.FORAGING,
            startTime = currentTime,
            duration = 3600000L
        )
        
        gameStateManager.updateCompanionAssignments { state ->
            state.copy(activeAssignments = listOf(assignment))
        }
        
        currentTime += 3600000L
        assignmentManager.completeTask(assignment)
        
        val choices = gameStateManager.playerState.value.choiceLog.entries
        assertTrue(choices.any { 
            it.tag.value == "companion_task_complete_${testCompanionId.value}_FORAGING"
        })
    }
    
    @Test
    fun `assignment progress calculation`() = runTest {
        val assignment = CompanionTaskAssignment(
            companionId = testCompanionId,
            taskType = CompanionTaskType.FORAGING,
            startTime = 1000000L,
            duration = 3600000L
        )
        
        // 0% complete
        assertEquals(0.0f, assignment.getProgress(1000000L))
        
        // 25% complete (900 seconds / 3600 seconds)
        assertEquals(0.25f, assignment.getProgress(1900000L))
        
        // 50% complete
        assertEquals(0.5f, assignment.getProgress(2800000L))
        
        // 100% complete
        assertEquals(1.0f, assignment.getProgress(4600000L))
        
        // Over 100% caps at 1.0
        assertEquals(1.0f, assignment.getProgress(10000000L))
    }
    
    @Test
    fun `tier 3 assignment board allows 6 concurrent assignments`() = runTest {
        // Upgrade to tier 3
        gameStateManager.updateNestCustomization { nest ->
            nest.copy(
                functionalUpgrades = mapOf(
                    FunctionalUpgradeType.COMPANION_ASSIGNMENT_BOARD to FunctionalUpgrade(
                        type = FunctionalUpgradeType.COMPANION_ASSIGNMENT_BOARD,
                        cosmeticItemId = CosmeticItemId("board_tier3"),
                        currentTier = UpgradeTier.TIER_3,
                        isActive = true
                    )
                )
            )
        }
        
        // Add 6 companions
        val companions = (1..6).map { CompanionId("companion$it") }
        gameStateManager.updateCompanionState { state ->
            state.copy(
                recruitedCompanions = companions.associate { 
                    it.value to CompanionProgress(
                        companionId = it,
                        affinity = 50,
                        isRecruited = true
                    )
                }
            )
        }
        
        // All 6 assignments should succeed
        companions.forEach { companion ->
            val result = assignmentManager.assignCompanionToTask(
                companion,
                CompanionTaskType.FORAGING,
                3600000L
            )
            assertTrue(result is AssignmentResult.Success, "Assignment for ${companion.value} should succeed")
        }
        
        val activeCount = gameStateManager.playerState.value.companionAssignments.activeAssignments.size
        assertEquals(6, activeCount)
    }
}
