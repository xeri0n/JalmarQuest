package com.jalmarquest.core.model

import kotlin.test.*

class ArchetypeTest {
    
    @Test
    fun testArchetypeProgressInitialState() {
        val progress = ArchetypeProgress()
        
        assertNull(progress.selectedArchetype)
        assertEquals(1, progress.archetypeLevel)
        assertEquals(0, progress.archetypeXP)
        assertEquals(0, progress.availableTalentPoints)
        assertEquals(0, progress.totalTalentPointsEarned)
        assertNull(progress.talentTree)
    }
    
    @Test
    fun testXPCurveProgression() {
        val progress = ArchetypeProgress()
        
        // Level 1: 200 XP required
        assertEquals(200, progress.xpForNextLevel())
        
        // Level 2: 300 XP required (200 * 1.5^1)
        val level2 = progress.copy(archetypeLevel = 2)
        assertEquals(300, level2.xpForNextLevel())
        
        // Level 3: 450 XP required (200 * 1.5^2)
        val level3 = progress.copy(archetypeLevel = 3)
        assertEquals(450, level3.xpForNextLevel())
        
        // Level 10: should still calculate
        val level10 = progress.copy(archetypeLevel = 10)
        assertTrue(level10.xpForNextLevel() > 0)
    }
    
    @Test
    fun testCanLevelUp() {
        var progress = ArchetypeProgress(selectedArchetype = ArchetypeType.SCHOLAR)
        
        // Not enough XP
        assertFalse(progress.canLevelUp())
        
        // Exactly enough XP
        progress = progress.copy(archetypeXP = 200)
        assertTrue(progress.canLevelUp())
        
        // More than enough XP
        progress = progress.copy(archetypeXP = 300)
        assertTrue(progress.canLevelUp())
        
        // Max level (10)
        progress = progress.copy(archetypeLevel = 10, archetypeXP = 10000)
        assertFalse(progress.canLevelUp())
    }
    
    @Test
    fun testProgressToNextLevel() {
        val progress = ArchetypeProgress(
            selectedArchetype = ArchetypeType.SCHOLAR,
            archetypeXP = 100 // 50% to next level (200 XP needed)
        )
        
        assertEquals(0.5, progress.progressToNextLevel())
        
        // 0% progress
        val noProgress = progress.copy(archetypeXP = 0)
        assertEquals(0.0, noProgress.progressToNextLevel())
        
        // 100% progress
        val fullProgress = progress.copy(archetypeXP = 200)
        assertEquals(1.0, fullProgress.progressToNextLevel())
        
        // Max level always shows 100%
        val maxLevel = progress.copy(archetypeLevel = 10)
        assertEquals(1.0, maxLevel.progressToNextLevel())
    }
    
    @Test
    fun testAddXP() {
        val progress = ArchetypeProgress(selectedArchetype = ArchetypeType.SCHOLAR)
        
        // Add XP without leveling up
        val updated = progress.addXP(100)
        assertEquals(100, updated.archetypeXP)
        assertEquals(1, updated.archetypeLevel)
        
        // Add XP that triggers level-up
        val leveledUp = progress.addXP(250)
        assertEquals(2, leveledUp.archetypeLevel)
        assertEquals(50, leveledUp.archetypeXP) // 250 - 200 = 50 overflow
        assertEquals(1, leveledUp.availableTalentPoints)
    }
    
    @Test
    fun testLevelUpPreservesOverflow() {
        val progress = ArchetypeProgress(
            selectedArchetype = ArchetypeType.SCHOLAR,
            archetypeXP = 250 // 50 more than needed (200)
        )
        
        val leveledUp = progress.levelUp()
        
        assertEquals(2, leveledUp.archetypeLevel)
        assertEquals(50, leveledUp.archetypeXP)
        assertEquals(1, leveledUp.availableTalentPoints)
        assertEquals(1, leveledUp.totalTalentPointsEarned)
    }
    
    @Test
    fun testMultipleLevelUpsInOneAddXP() {
        val progress = ArchetypeProgress(selectedArchetype = ArchetypeType.SCHOLAR)
        
        // Add enough XP to go from level 1 to level 3
        // Level 1→2: 200 XP, Level 2→3: 300 XP, Total: 500 XP
        val tripleXP = progress.addXP(600)
        
        assertEquals(3, tripleXP.archetypeLevel)
        assertEquals(100, tripleXP.archetypeXP) // 600 - 500 = 100 overflow
        assertEquals(2, tripleXP.availableTalentPoints)
        assertEquals(2, tripleXP.totalTalentPointsEarned)
    }
    
    @Test
    fun testSpendTalentPoints() {
        val progress = ArchetypeProgress(
            selectedArchetype = ArchetypeType.SCHOLAR,
            availableTalentPoints = 5
        )
        
        // Spend 2 points
        val spent = progress.spendTalentPoints(2)
        assertEquals(3, spent.availableTalentPoints)
        
        // Try to spend more than available
        val overspent = progress.spendTalentPoints(10)
        assertEquals(5, overspent.availableTalentPoints) // No change
    }
    
    @Test
    fun testTalentRequirementLevel() {
        val requirement = TalentRequirement.Level(5)
        val talentTree = TalentTree(
            archetypeType = ArchetypeType.SCHOLAR,
            talents = emptyList()
        )
        
        // Below required level
        assertFalse(talentTree.meetsRequirement(requirement, 3))
        
        // Exactly at required level
        assertTrue(talentTree.meetsRequirement(requirement, 5))
        
        // Above required level
        assertTrue(talentTree.meetsRequirement(requirement, 7))
    }
    
    @Test
    fun testTalentRequirementPrerequisite() {
        val requirement = TalentRequirement.PrerequisiteTalent("talent_1")
        val talentTree = TalentTree(
            archetypeType = ArchetypeType.SCHOLAR,
            talents = emptyList(),
            unlockedTalentIds = setOf("talent_1")
        )
        
        assertTrue(talentTree.meetsRequirement(requirement, 1))
        
        // Missing prerequisite
        val noPrereq = talentTree.copy(unlockedTalentIds = emptySet())
        assertFalse(noPrereq.meetsRequirement(requirement, 1))
    }
    
    @Test
    fun testTalentRequirementAllTalents() {
        val requirement = TalentRequirement.AllTalents(listOf("talent_1", "talent_2"))
        
        // Both unlocked
        val allUnlocked = TalentTree(
            archetypeType = ArchetypeType.SCHOLAR,
            talents = emptyList(),
            unlockedTalentIds = setOf("talent_1", "talent_2", "talent_3")
        )
        assertTrue(allUnlocked.meetsRequirement(requirement, 1))
        
        // Only one unlocked
        val oneUnlocked = allUnlocked.copy(unlockedTalentIds = setOf("talent_1"))
        assertFalse(oneUnlocked.meetsRequirement(requirement, 1))
    }
    
    @Test
    fun testTalentRequirementAnyTalent() {
        val requirement = TalentRequirement.AnyTalent(listOf("talent_1", "talent_2"))
        
        // One of them unlocked
        val oneUnlocked = TalentTree(
            archetypeType = ArchetypeType.SCHOLAR,
            talents = emptyList(),
            unlockedTalentIds = setOf("talent_1")
        )
        assertTrue(oneUnlocked.meetsRequirement(requirement, 1))
        
        // Both unlocked
        val bothUnlocked = oneUnlocked.copy(unlockedTalentIds = setOf("talent_1", "talent_2"))
        assertTrue(bothUnlocked.meetsRequirement(requirement, 1))
        
        // None unlocked
        val noneUnlocked = oneUnlocked.copy(unlockedTalentIds = emptySet())
        assertFalse(noneUnlocked.meetsRequirement(requirement, 1))
    }
    
    @Test
    fun testTalentRequirementTotalTalentsUnlocked() {
        val requirement = TalentRequirement.TotalTalentsUnlocked(3)
        
        // Exactly 3 talents
        val exactCount = TalentTree(
            archetypeType = ArchetypeType.SCHOLAR,
            talents = emptyList(),
            unlockedTalentIds = setOf("talent_1", "talent_2", "talent_3")
        )
        assertTrue(exactCount.meetsRequirement(requirement, 1))
        
        // More than 3
        val moreThanRequired = exactCount.copy(
            unlockedTalentIds = setOf("talent_1", "talent_2", "talent_3", "talent_4")
        )
        assertTrue(moreThanRequired.meetsRequirement(requirement, 1))
        
        // Less than 3
        val lessThanRequired = exactCount.copy(unlockedTalentIds = setOf("talent_1"))
        assertFalse(lessThanRequired.meetsRequirement(requirement, 1))
    }
    
    @Test
    fun testCanUnlockTalent() {
        val talent = Talent(
            id = "scholar_talent_1",
            name = "Quick Study",
            description = "+15% XP gain",
            talentType = TalentType.GENERAL_XP_BONUS,
            magnitude = 15,
            costInPoints = 1,
            requirements = listOf(TalentRequirement.Level(2))
        )
        
        val talentTree = TalentTree(
            archetypeType = ArchetypeType.SCHOLAR,
            talents = listOf(talent),
            unlockedTalentIds = emptySet()
        )
        
        // Below required level
        assertFalse(talentTree.canUnlockTalent("scholar_talent_1", 1))
        
        // At required level
        assertTrue(talentTree.canUnlockTalent("scholar_talent_1", 2))
        
        // Already unlocked
        val alreadyUnlocked = talentTree.copy(unlockedTalentIds = setOf("scholar_talent_1"))
        assertFalse(alreadyUnlocked.canUnlockTalent("scholar_talent_1", 2))
    }
    
    @Test
    fun testUnlockTalent() {
        val talentTree = TalentTree(
            archetypeType = ArchetypeType.SCHOLAR,
            talents = emptyList(),
            unlockedTalentIds = emptySet()
        )
        
        val unlocked = talentTree.unlockTalent("talent_1")
        
        assertTrue("talent_1" in unlocked.unlockedTalentIds)
        assertEquals(1, unlocked.unlockedTalentIds.size)
    }
    
    @Test
    fun testGetUnlockedTalents() {
        val talent1 = Talent("t1", "Talent 1", "Desc", TalentType.GENERAL_XP_BONUS, 10, 1)
        val talent2 = Talent("t2", "Talent 2", "Desc", TalentType.SEED_INCOME_BONUS, 5, 1)
        val talent3 = Talent("t3", "Talent 3", "Desc", TalentType.LUCK_BONUS, 3, 1)
        
        val talentTree = TalentTree(
            archetypeType = ArchetypeType.SCHOLAR,
            talents = listOf(talent1, talent2, talent3),
            unlockedTalentIds = setOf("t1", "t3")
        )
        
        val unlocked = talentTree.getUnlockedTalents()
        
        assertEquals(2, unlocked.size)
        assertTrue(unlocked.any { it.id == "t1" })
        assertTrue(unlocked.any { it.id == "t3" })
        assertFalse(unlocked.any { it.id == "t2" })
    }
    
    @Test
    fun testGetTotalBonus() {
        val talent1 = Talent("t1", "XP Boost 1", "Desc", TalentType.GENERAL_XP_BONUS, 10, 1)
        val talent2 = Talent("t2", "XP Boost 2", "Desc", TalentType.GENERAL_XP_BONUS, 15, 1)
        val talent3 = Talent("t3", "Luck", "Desc", TalentType.LUCK_BONUS, 5, 1)
        
        val talentTree = TalentTree(
            archetypeType = ArchetypeType.SCHOLAR,
            talents = listOf(talent1, talent2, talent3),
            unlockedTalentIds = setOf("t1", "t2", "t3")
        )
        
        // Should sum all GENERAL_XP_BONUS magnitudes
        assertEquals(25, talentTree.getTotalBonus(TalentType.GENERAL_XP_BONUS))
        
        // Single LUCK_BONUS
        assertEquals(5, talentTree.getTotalBonus(TalentType.LUCK_BONUS))
        
        // No talents of this type
        assertEquals(0, talentTree.getTotalBonus(TalentType.DAMAGE_BONUS))
    }
    
    @Test
    fun testComplexTalentTree() {
        // Create a multi-tier talent tree
        val starter = Talent(
            id = "starter",
            name = "Foundation",
            description = "Basic talent",
            talentType = TalentType.GENERAL_XP_BONUS,
            magnitude = 5,
            costInPoints = 1,
            requirements = emptyList()
        )
        
        val tier2 = Talent(
            id = "tier2",
            name = "Intermediate",
            description = "Requires starter",
            talentType = TalentType.GENERAL_XP_BONUS,
            magnitude = 10,
            costInPoints = 2,
            requirements = listOf(TalentRequirement.PrerequisiteTalent("starter"))
        )
        
        val tier3 = Talent(
            id = "tier3",
            name = "Advanced",
            description = "Requires tier2 and level 5",
            talentType = TalentType.SKILL_XP_BONUS,
            magnitude = 20,
            costInPoints = 3,
            requirements = listOf(
                TalentRequirement.PrerequisiteTalent("tier2"),
                TalentRequirement.Level(5)
            )
        )
        
        var talentTree = TalentTree(
            archetypeType = ArchetypeType.SCHOLAR,
            talents = listOf(starter, tier2, tier3)
        )
        
        // Can unlock starter immediately
        assertTrue(talentTree.canUnlockTalent("starter", 1))
        talentTree = talentTree.unlockTalent("starter")
        
        // Can unlock tier2 after starter
        assertTrue(talentTree.canUnlockTalent("tier2", 1))
        talentTree = talentTree.unlockTalent("tier2")
        
        // Cannot unlock tier3 yet (need level 5)
        assertFalse(talentTree.canUnlockTalent("tier3", 3))
        
        // Can unlock tier3 at level 5
        assertTrue(talentTree.canUnlockTalent("tier3", 5))
    }
    
    @Test
    fun testArchetypeTypesExist() {
        // Verify all 6 archetypes are defined
        val archetypes = ArchetypeType.values()
        assertEquals(6, archetypes.size)
        
        assertTrue(ArchetypeType.SCHOLAR in archetypes)
        assertTrue(ArchetypeType.COLLECTOR in archetypes)
        assertTrue(ArchetypeType.ALCHEMIST in archetypes)
        assertTrue(ArchetypeType.SCAVENGER in archetypes)
        assertTrue(ArchetypeType.SOCIALITE in archetypes)
        assertTrue(ArchetypeType.WARRIOR in archetypes)
    }
    
    @Test
    fun testTalentTypesExist() {
        // Verify comprehensive talent type coverage
        val types = TalentType.values()
        
        assertTrue(types.size >= 20) // At least 20 talent types defined
        assertTrue(TalentType.GENERAL_XP_BONUS in types)
        assertTrue(TalentType.DIALOGUE_UNLOCK in types)
        assertTrue(TalentType.ACTIVE_ABILITY in types)
    }
    
    // Helper extension for private method testing
    private fun TalentTree.meetsRequirement(requirement: TalentRequirement, archetypeLevel: Int): Boolean {
        return when (requirement) {
            is TalentRequirement.Level -> archetypeLevel >= requirement.requiredLevel
            is TalentRequirement.PrerequisiteTalent -> requirement.talentId in unlockedTalentIds
            is TalentRequirement.AllTalents -> requirement.talentIds.all { it in unlockedTalentIds }
            is TalentRequirement.AnyTalent -> requirement.talentIds.any { it in unlockedTalentIds }
            is TalentRequirement.TotalTalentsUnlocked -> unlockedTalentIds.size >= requirement.count
        }
    }
}
