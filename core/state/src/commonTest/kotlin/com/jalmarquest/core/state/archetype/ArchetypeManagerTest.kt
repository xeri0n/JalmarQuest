package com.jalmarquest.core.state.archetype

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import kotlin.test.*

class ArchetypeManagerTest {
    
    private lateinit var gameStateManager: GameStateManager
    private lateinit var talentTreeCatalog: TalentTreeCatalog
    private lateinit var archetypeManager: ArchetypeManager
    private var currentTime = 2000000L
    
    @BeforeTest
    fun setup() {
        val initialPlayer = Player(
            id = "test-player",
            name = "Test Player"
        )
        gameStateManager = GameStateManager(initialPlayer) { currentTime }
        talentTreeCatalog = TalentTreeCatalog()
        archetypeManager = ArchetypeManager(gameStateManager, talentTreeCatalog)
    }
    
    @Test
    fun testInitialState() {
        val state = archetypeManager.archetypeState.value
        
        assertNull(state.selectedArchetype)
        assertEquals(1, state.archetypeLevel)
        assertEquals(0, state.archetypeXP)
        assertEquals(0, state.availableTalentPoints)
        assertNull(state.talentTree)
        assertFalse(archetypeManager.hasSelectedArchetype())
    }
    
    @Test
    fun testSelectArchetype() {
        val result = archetypeManager.selectArchetype(ArchetypeType.SCHOLAR)
        
        assertTrue(result)
        assertEquals(ArchetypeType.SCHOLAR, archetypeManager.getSelectedArchetype())
        assertTrue(archetypeManager.hasSelectedArchetype())
        
        val state = archetypeManager.archetypeState.value
        assertEquals(ArchetypeType.SCHOLAR, state.selectedArchetype)
        assertNotNull(state.talentTree)
        assertEquals(ArchetypeType.SCHOLAR, state.talentTree?.archetypeType)
        
        // Check choice log
        val player = gameStateManager.playerState.value
        assertTrue(player.choiceLog.entries.any { it.tag.value == "archetype_scholar" })
    }
    
    @Test
    fun testCannotSelectArchetwiceTwice() {
        // Select first archetype
        archetypeManager.selectArchetype(ArchetypeType.SCHOLAR)
        
        // Try to select again
        val result = archetypeManager.selectArchetype(ArchetypeType.WARRIOR)
        
        assertFalse(result)
        assertEquals(ArchetypeType.SCHOLAR, archetypeManager.getSelectedArchetype())
    }
    
    @Test
    fun testGainArchetypeXP() {
        archetypeManager.selectArchetype(ArchetypeType.COLLECTOR)
        
        val result = archetypeManager.gainArchetypeXP(100)
        
        assertEquals(100, result.archetypeXP)
        assertEquals(1, result.archetypeLevel)
        
        val state = archetypeManager.archetypeState.value
        assertEquals(100, state.archetypeXP)
    }
    
    @Test
    fun testGainXPWithLevelUp() {
        archetypeManager.selectArchetype(ArchetypeType.SCHOLAR)
        
        // Gain enough XP to level up (200 XP needed for level 1→2)
        val result = archetypeManager.gainArchetypeXP(250)
        
        assertEquals(2, result.archetypeLevel)
        assertEquals(50, result.archetypeXP) // Overflow preserved
        assertEquals(1, result.availableTalentPoints) // Gained 1 talent point
        
        // Check choice log for level-up
        val player = gameStateManager.playerState.value
        assertTrue(player.choiceLog.entries.any { it.tag.value == "archetype_levelup_2" })
    }
    
    @Test
    fun testGainXPWithoutArchetype() {
        // Try to gain XP without selecting archetype
        val result = archetypeManager.gainArchetypeXP(100)
        
        // Should return unchanged progress
        assertEquals(0, result.archetypeXP)
        assertNull(result.selectedArchetype)
    }
    
    @Test
    fun testGainXPNegativeAmount() {
        archetypeManager.selectArchetype(ArchetypeType.ALCHEMIST)
        
        assertFailsWith<IllegalArgumentException> {
            archetypeManager.gainArchetypeXP(-50)
        }
    }
    
    @Test
    fun testUnlockTalent() {
        archetypeManager.selectArchetype(ArchetypeType.SCHOLAR)
        
        // Grant talent points
        archetypeManager.grantTalentPoints(2)
        
        // Unlock a starter talent (no requirements)
        val result = archetypeManager.unlockTalent("scholar_quick_study")
        
        assertTrue(result)
        
        val state = archetypeManager.archetypeState.value
        assertEquals(1, state.availableTalentPoints) // Spent 1 point
        assertTrue("scholar_quick_study" in (state.talentTree?.unlockedTalentIds ?: emptySet()))
        
        // Check choice log
        val player = gameStateManager.playerState.value
        assertTrue(player.choiceLog.entries.any { it.tag.value == "talent_unlock_scholar_quick_study" })
    }
    
    @Test
    fun testUnlockTalentWithoutPoints() {
        archetypeManager.selectArchetype(ArchetypeType.WARRIOR)
        
        // No talent points available
        val result = archetypeManager.unlockTalent("warrior_strong")
        
        assertFalse(result)
        
        val state = archetypeManager.archetypeState.value
        assertFalse("warrior_strong" in (state.talentTree?.unlockedTalentIds ?: emptySet()))
    }
    
    @Test
    fun testUnlockTalentWithUnmetRequirements() {
        archetypeManager.selectArchetype(ArchetypeType.SCHOLAR)
        archetypeManager.grantTalentPoints(5)
        
        // Try to unlock tier 2 talent without meeting level requirement
        val result = archetypeManager.unlockTalent("scholar_voracious_reader") // Requires level 3
        
        assertFalse(result)
        assertEquals(5, archetypeManager.archetypeState.value.availableTalentPoints) // No points spent
    }
    
    @Test
    fun testUnlockTalentWithPrerequisite() {
        archetypeManager.selectArchetype(ArchetypeType.SCHOLAR)
        archetypeManager.grantTalentPoints(5)
        
        // Level up to 3
        archetypeManager.gainArchetypeXP(700) // Enough to reach level 3
        
        // Unlock prerequisite first
        archetypeManager.unlockTalent("scholar_deep_thought")
        
        // Now unlock the talent that requires it
        val result = archetypeManager.unlockTalent("scholar_philosopher")
        
        assertTrue(result)
        
        val state = archetypeManager.archetypeState.value
        assertTrue("scholar_philosopher" in (state.talentTree?.unlockedTalentIds ?: emptySet()))
    }
    
    @Test
    fun testGrantTalentPoints() {
        archetypeManager.selectArchetype(ArchetypeType.SCAVENGER)
        
        archetypeManager.grantTalentPoints(3)
        
        val state = archetypeManager.archetypeState.value
        assertEquals(3, state.availableTalentPoints)
        assertEquals(3, state.totalTalentPointsEarned)
    }
    
    @Test
    fun testGrantTalentPointsInvalid() {
        archetypeManager.selectArchetype(ArchetypeType.SOCIALITE)
        
        assertFailsWith<IllegalArgumentException> {
            archetypeManager.grantTalentPoints(0)
        }
        
        assertFailsWith<IllegalArgumentException> {
            archetypeManager.grantTalentPoints(-5)
        }
    }
    
    @Test
    fun testGetTotalBonus() {
        archetypeManager.selectArchetype(ArchetypeType.SCHOLAR)
        archetypeManager.grantTalentPoints(5)
        
        // Unlock talents with XP bonuses
        archetypeManager.unlockTalent("scholar_quick_study") // +15% XP
        
        val bonus = archetypeManager.getTotalBonus(TalentType.GENERAL_XP_BONUS)
        
        assertEquals(15, bonus)
    }
    
    @Test
    fun testGetTotalBonusMultipleTalents() {
        archetypeManager.selectArchetype(ArchetypeType.SCHOLAR)
        archetypeManager.grantTalentPoints(10)
        archetypeManager.gainArchetypeXP(5000) // Level up to 7+ (200+300+450+675+1012+1519 = 4156)
        
        // Unlock multiple XP bonus talents
        archetypeManager.unlockTalent("scholar_quick_study") // +15% XP
        archetypeManager.unlockTalent("scholar_voracious_reader") // +25% Skill XP
        archetypeManager.unlockTalent("scholar_enlightened") // +50% XP (requires level 7)
        
        val totalXPBonus = archetypeManager.getTotalBonus(TalentType.GENERAL_XP_BONUS)
        val skillXPBonus = archetypeManager.getTotalBonus(TalentType.SKILL_XP_BONUS)
        
        assertEquals(65, totalXPBonus) // 15 + 50
        assertEquals(25, skillXPBonus)
    }
    
    @Test
    fun testGetActiveBonuses() {
        archetypeManager.selectArchetype(ArchetypeType.COLLECTOR)
        archetypeManager.grantTalentPoints(5)
        
        archetypeManager.unlockTalent("collector_keen_eye") // +10 luck
        archetypeManager.unlockTalent("collector_hoarder") // +15% hoard value
        
        val bonuses = archetypeManager.getActiveBonuses()
        
        assertEquals(2, bonuses.size)
        assertEquals(10, bonuses[TalentType.LUCK_BONUS])
        assertEquals(15, bonuses[TalentType.HOARD_VALUE_BONUS])
        assertNull(bonuses[TalentType.DAMAGE_BONUS]) // Not active
    }
    
    @Test
    fun testCheckTalentRequirements() {
        archetypeManager.selectArchetype(ArchetypeType.WARRIOR)
        
        // Check starter talent (no requirements)
        assertTrue(archetypeManager.checkTalentRequirements("warrior_strong"))
        
        // Check talent with level requirement
        assertFalse(archetypeManager.checkTalentRequirements("warrior_hardy")) // Requires level 4
        
        // Level up
        archetypeManager.gainArchetypeXP(1500) // Should reach level 4+
        
        // Now should meet requirements
        assertTrue(archetypeManager.checkTalentRequirements("warrior_hardy"))
    }
    
    @Test
    fun testGetUnlockableTalents() {
        archetypeManager.selectArchetype(ArchetypeType.ALCHEMIST)
        archetypeManager.grantTalentPoints(3)
        
        val unlockable = archetypeManager.getUnlockableTalents()
        
        // Should only get talents that meet requirements AND player has points for
        assertTrue(unlockable.isNotEmpty())
        assertTrue(unlockable.all { it.costInPoints <= 3 })
        
        // Level-locked talents shouldn't be included
        assertFalse(unlockable.any { it.requirements.any { req -> req is TalentRequirement.Level && (req as TalentRequirement.Level).requiredLevel > 1 } })
    }
    
    @Test
    fun testMultipleArchetypes() {
        // Test that each archetype has a valid talent tree
        ArchetypeType.values().forEach { archetype ->
            val tree = talentTreeCatalog.getTalentTree(archetype)
            
            assertEquals(archetype, tree.archetypeType)
            assertTrue(tree.talents.isNotEmpty(), "Archetype $archetype should have talents")
        }
    }
    
    @Test
    fun testStatePersistence() {
        archetypeManager.selectArchetype(ArchetypeType.SCAVENGER)
        archetypeManager.gainArchetypeXP(500)
        archetypeManager.unlockTalent("scavenger_forager")
        
        // Get current state from GameStateManager
        val player = gameStateManager.playerState.value
        
        assertEquals(ArchetypeType.SCAVENGER, player.archetypeProgress.selectedArchetype)
        assertEquals(3, player.archetypeProgress.archetypeLevel) // 500 XP = level 3 (200 for L2, 300 for L3)
        assertTrue("scavenger_forager" in (player.archetypeProgress.talentTree?.unlockedTalentIds ?: emptySet()))
    }
    
    @Test
    fun testTalentTreeCatalogScholar() {
        val tree = talentTreeCatalog.getTalentTree(ArchetypeType.SCHOLAR)
        
        assertEquals(ArchetypeType.SCHOLAR, tree.archetypeType)
        assertTrue(tree.talents.size >= 5)
        
        // Verify starter talents exist
        assertTrue(tree.talents.any { it.id == "scholar_quick_study" })
        assertTrue(tree.talents.any { it.id == "scholar_deep_thought" })
    }
    
    @Test
    fun testTalentTreeCatalogCollector() {
        val tree = talentTreeCatalog.getTalentTree(ArchetypeType.COLLECTOR)
        
        assertEquals(ArchetypeType.COLLECTOR, tree.archetypeType)
        assertTrue(tree.talents.any { it.id == "collector_keen_eye" })
        assertTrue(tree.talents.any { it.id == "collector_hoarder" })
    }
    
    @Test
    fun testCompleteArchetypeProgression() {
        // Full progression scenario
        archetypeManager.selectArchetype(ArchetypeType.WARRIOR)
        
        // Gain XP and level up multiple times
        archetypeManager.gainArchetypeXP(2000) // Multiple levels
        
        val state1 = archetypeManager.archetypeState.value
        assertTrue(state1.archetypeLevel > 1)
        assertTrue(state1.availableTalentPoints > 0)
        
        // Unlock multiple talents
        archetypeManager.unlockTalent("warrior_strong")
        archetypeManager.unlockTalent("warrior_tough")
        
        val state2 = archetypeManager.archetypeState.value
        assertEquals(2, state2.talentTree?.unlockedTalentIds?.size)
        
        // Verify bonuses are active
        val bonuses = archetypeManager.getActiveBonuses()
        assertTrue(bonuses.containsKey(TalentType.DAMAGE_BONUS))
        assertTrue(bonuses.containsKey(TalentType.DEFENSE_BONUS))
    }
}
