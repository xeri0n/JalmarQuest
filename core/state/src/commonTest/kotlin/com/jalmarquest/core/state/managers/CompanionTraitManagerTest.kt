package com.jalmarquest.core.state.managers

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.testutil.testPlayer
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Alpha 2.3 Part 3.1: Test suite for companion trait system.
 * 
 * Validates:
 * - Trait XP award and level-up mechanics
 * - Task completion with different difficulties
 * - Bonus multiplier scaling (1.0x → 2.5x)
 * - XP curves and progression
 */
class CompanionTraitManagerTest {
    private lateinit var gameStateManager: GameStateManager
    private lateinit var traitManager: CompanionTraitManager
    private var currentTime = 0L
    private val testCompanionId = CompanionId("companion_test_quail")
    
    @BeforeTest
    fun setup() {
        currentTime = 1000L
        val player = createPlayerWithCompanion()
        gameStateManager = GameStateManager(
            initialPlayer = player,
            accountManager = null,
            timestampProvider = { currentTime }
        )
        traitManager = CompanionTraitManager(
            gameStateManager = gameStateManager,
            timestampProvider = { currentTime }
        )
    }
    
    // ========================================
    // Trait XP Award Tests
    // ========================================
    
    @Test
    fun `award trait XP increases XP counter`() = runTest {
        val result = traitManager.awardTraitXp(
            testCompanionId,
            CompanionTrait.FORAGING,
            50
        )
        
        assertTrue(result is TraitXpResult.Success)
        assertEquals(50, (result as TraitXpResult.Success).currentXp)
        assertEquals(TraitLevel(1), result.newLevel)
        assertEquals(0, result.levelsGained)
    }
    
    @Test
    fun `award enough XP triggers level up`() = runTest {
        // Level 1→2 requires 100 XP
        val result = traitManager.awardTraitXp(
            testCompanionId,
            CompanionTrait.FORAGING,
            150 // Enough for level 2 with 50 XP remaining
        )
        
        assertTrue(result is TraitXpResult.Success)
        val success = result as TraitXpResult.Success
        assertEquals(TraitLevel(2), success.newLevel)
        assertEquals(1, success.levelsGained)
        assertEquals(50, success.currentXp) // Remaining XP after level up
    }
    
    @Test
    fun `multiple level ups in single award`() = runTest {
        // Level 1→2 = 100 XP, Level 2→3 = 250 XP
        // Total = 350 XP needed for 2 levels
        val result = traitManager.awardTraitXp(
            testCompanionId,
            CompanionTrait.FORAGING,
            400 // Should level up twice with 50 XP remaining
        )
        
        assertTrue(result is TraitXpResult.Success)
        val success = result as TraitXpResult.Success
        assertEquals(TraitLevel(3), success.newLevel)
        assertEquals(2, success.levelsGained)
        assertEquals(50, success.currentXp)
    }
    
    @Test
    fun `cannot award negative or zero XP`() = runTest {
        val result = traitManager.awardTraitXp(
            testCompanionId,
            CompanionTrait.FORAGING,
            0
        )
        
        assertTrue(result is TraitXpResult.InvalidAmount)
    }
    
    @Test
    fun `award XP to non-recruited companion fails`() = runTest {
        val unknownCompanion = CompanionId("companion_unknown")
        val result = traitManager.awardTraitXp(
            unknownCompanion,
            CompanionTrait.FORAGING,
            50
        )
        
        assertTrue(result is TraitXpResult.CompanionNotRecruited)
    }
    
    @Test
    fun `trait level caps at 10`() = runTest {
        // Award massive XP to reach level 10
        var result = traitManager.awardTraitXp(
            testCompanionId,
            CompanionTrait.FORAGING,
            20000 // Way more than needed
        )
        
        assertTrue(result is TraitXpResult.Success)
        assertEquals(TraitLevel(10), (result as TraitXpResult.Success).newLevel)
        
        // Try to award more XP - should stay at level 10
        result = traitManager.awardTraitXp(
            testCompanionId,
            CompanionTrait.FORAGING,
            1000
        )
        
        assertTrue(result is TraitXpResult.Success)
        assertEquals(TraitLevel(10), (result as TraitXpResult.Success).newLevel)
        assertEquals(0, result.levelsGained) // No more levels
    }
    
    // ========================================
    // Task Completion Tests
    // ========================================
    
    @Test
    fun `complete foraging task awards FORAGING trait XP`() = runTest {
        val result = traitManager.completeTask(
            testCompanionId,
            CompanionTaskType.FORAGING,
            1.0f // Normal difficulty
        )
        
        assertTrue(result is TraitXpResult.Success)
        val success = result as TraitXpResult.Success
        assertEquals(15, success.currentXp) // Base XP for foraging
    }
    
    @Test
    fun `task difficulty multiplier scales XP`() = runTest {
        val result = traitManager.completeTask(
            testCompanionId,
            CompanionTaskType.COMBAT,
            2.5f // Hard task
        )
        
        assertTrue(result is TraitXpResult.Success)
        val success = result as TraitXpResult.Success
        // Combat base = 35, × 2.5 = 87
        assertEquals(87, success.currentXp)
    }
    
    @Test
    fun `different task types award different base XP`() = runTest {
        val companion2 = CompanionId("companion_test2")
        setupCompanion(companion2)
        val companion3 = CompanionId("companion_test3")
        setupCompanion(companion3)
        
        val foragingResult = traitManager.completeTask(testCompanionId, CompanionTaskType.FORAGING)
        val combatResult = traitManager.completeTask(companion2, CompanionTaskType.COMBAT)
        val scholarshipResult = traitManager.completeTask(companion3, CompanionTaskType.SCHOLARSHIP)
        
        assertTrue(foragingResult is TraitXpResult.Success)
        assertTrue(combatResult is TraitXpResult.Success)
        assertTrue(scholarshipResult is TraitXpResult.Success)
        
        assertEquals(15, (foragingResult as TraitXpResult.Success).currentXp) // Foraging
        assertEquals(35, (combatResult as TraitXpResult.Success).currentXp) // Combat
        assertEquals(40, (scholarshipResult as TraitXpResult.Success).currentXp) // Scholarship (highest)
    }
    
    @Test
    fun `task types map to correct traits`() = runTest {
        // Complete different tasks
        traitManager.completeTask(testCompanionId, CompanionTaskType.FORAGING)
        traitManager.completeTask(testCompanionId, CompanionTaskType.BREWING)
        traitManager.completeTask(testCompanionId, CompanionTaskType.COMBAT)
        
        // Verify different traits were trained
        val player = gameStateManager.playerState.value
        val progress = player.companionState.getProgress(testCompanionId)
        assertNotNull(progress)
        
        assertNotNull(progress.traits[CompanionTrait.FORAGING.name])
        assertNotNull(progress.traits[CompanionTrait.BREWING.name])
        assertNotNull(progress.traits[CompanionTrait.COMBAT.name])
    }
    
    // ========================================
    // Bonus Multiplier Tests
    // ========================================
    
    @Test
    fun `trait bonus multiplier scales with level`() {
        // Level 1 = 1.0x
        assertEquals(1.0f, TraitLevel(1).getBonusMultiplier(), 0.01f)
        
        // Level 5 = ~1.67x
        assertEquals(1.67f, TraitLevel(5).getBonusMultiplier(), 0.01f)
        
        // Level 10 = 2.5x
        assertEquals(2.5f, TraitLevel(10).getBonusMultiplier(), 0.01f)
    }
    
    @Test
    fun `getTraitBonus returns correct multiplier`() = runTest {
        // Level up FORAGING to level 5
        traitManager.awardTraitXp(
            testCompanionId,
            CompanionTrait.FORAGING,
            1500 // Enough for level 5
        )
        
        val bonus = traitManager.getTraitBonus(testCompanionId, CompanionTrait.FORAGING)
        assertTrue(bonus > 1.6f && bonus < 1.7f) // Level 5 ≈ 1.67x
    }
    
    @Test
    fun `untrained trait defaults to level 1 bonus`() {
        val bonus = traitManager.getTraitBonus(testCompanionId, CompanionTrait.LUCK)
        assertEquals(1.0f, bonus, 0.01f)
    }
    
    // ========================================
    // XP Curve Tests
    // ========================================
    
    @Test
    fun `XP requirements increase progressively`() {
        assertEquals(100, TraitLevel(1).getXpToNextLevel())
        assertEquals(250, TraitLevel(2).getXpToNextLevel())
        assertEquals(450, TraitLevel(3).getXpToNextLevel())
        assertEquals(1000, TraitLevel(5).getXpToNextLevel())
        assertEquals(2700, TraitLevel(9).getXpToNextLevel())
    }
    
    @Test
    fun `level 10 cannot level up further`() {
        assertFalse(TraitLevel(10).canLevelUp())
        assertEquals(Int.MAX_VALUE, TraitLevel(10).getXpToNextLevel())
    }
    
    // ========================================
    // Choice Tag Tests
    // ========================================
    
    @Test
    fun `level up logs choice tag`() = runTest {
        traitManager.awardTraitXp(
            testCompanionId,
            CompanionTrait.FORAGING,
            150 // Level 1→2
        )
        
        val choices = gameStateManager.playerState.value.choiceLog.entries
        assertTrue(choices.any { 
            it.tag.value == "companion_trait_levelup_${testCompanionId.value}_FORAGING_2" 
        })
    }
    
    @Test
    fun `multiple level ups log multiple tags`() = runTest {
        traitManager.awardTraitXp(
            testCompanionId,
            CompanionTrait.FORAGING,
            400 // Level 1→2→3
        )
        
        val choices = gameStateManager.playerState.value.choiceLog.entries
        assertTrue(choices.any { it.tag.value.contains("FORAGING_2") })
        assertTrue(choices.any { it.tag.value.contains("FORAGING_3") })
    }
    
    // ========================================
    // Helper Query Tests
    // ========================================
    
    @Test
    fun `getTraitLevel returns current level`() = runTest {
        traitManager.awardTraitXp(testCompanionId, CompanionTrait.SCOUTING, 300)
        
        val level = traitManager.getTraitLevel(testCompanionId, CompanionTrait.SCOUTING)
        assertEquals(2, level.level) // Should be level 2
    }
    
    @Test
    fun `getAllTraits returns all 8 traits`() {
        val allTraits = traitManager.getAllTraits(testCompanionId)
        
        assertEquals(8, allTraits.size)
        assertTrue(allTraits.containsKey(CompanionTrait.FORAGING))
        assertTrue(allTraits.containsKey(CompanionTrait.SCOUTING))
        assertTrue(allTraits.containsKey(CompanionTrait.BREWING))
        assertTrue(allTraits.containsKey(CompanionTrait.SMITHING))
        assertTrue(allTraits.containsKey(CompanionTrait.COMBAT))
        assertTrue(allTraits.containsKey(CompanionTrait.TRADING))
        assertTrue(allTraits.containsKey(CompanionTrait.SCHOLARSHIP))
        assertTrue(allTraits.containsKey(CompanionTrait.LUCK))
    }
    
    @Test
    fun `getAllTraits shows default level 1 for untrained traits`() {
        val allTraits = traitManager.getAllTraits(testCompanionId)
        
        allTraits.values.forEach { progress ->
            assertEquals(TraitLevel(1), progress.level)
            assertEquals(0, progress.currentXp)
        }
    }
    
    // ========================================
    // Helper Methods
    // ========================================
    
    private fun createPlayerWithCompanion(): Player {
        val player = testPlayer()
        val companionProgress = CompanionProgress(
            companionId = testCompanionId,
            affinity = 50,
            isRecruited = true,
            traits = emptyMap() // Start with no traits trained
        )
        return player.copy(
            companionState = CompanionState(
                activeCompanion = testCompanionId,
                recruitedCompanions = mapOf(testCompanionId.value to companionProgress)
            )
        )
    }
    
    private suspend fun setupCompanion(companionId: CompanionId) {
        gameStateManager.updateCompanionState { state ->
            val progress = CompanionProgress(
                companionId = companionId,
                affinity = 50,
                isRecruited = true,
                traits = emptyMap()
            )
            state.copy(
                recruitedCompanions = state.recruitedCompanions + (companionId.value to progress)
            )
        }
    }
}
