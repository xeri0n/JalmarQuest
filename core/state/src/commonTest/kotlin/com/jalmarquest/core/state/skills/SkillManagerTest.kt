package com.jalmarquest.core.state.skills

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.archetype.ArchetypeManager
import com.jalmarquest.core.state.archetype.TalentTreeCatalog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class SkillManagerTest {
    private var currentTime = 2000000L

    private fun createTestSkill(
        id: SkillId = SkillId("skill_foraging"),
        type: SkillType = SkillType.FORAGING,
        level: Int = 1,
        currentXP: Int = 0
    ): Skill {
        return Skill(
            id = id,
            type = type,
            nameKey = "skill.${type.name.lowercase()}.name",
            descriptionKey = "skill.${type.name.lowercase()}.description",
            level = level,
            currentXP = currentXP,
            unlockedAbilities = emptySet()
        )
    }

    private fun createTestAbility(
        id: AbilityId = AbilityId("ability_harvest_1"),
        requiredSkill: SkillId = SkillId("skill_foraging"),
        requiredLevel: Int = 2,
        type: AbilityType = AbilityType.HARVEST_BONUS,
        magnitude: Double = 10.0
    ): Ability {
        return Ability(
            id = id,
            nameKey = "ability.harvest_1.name",
            descriptionKey = "ability.harvest_1.description",
            requiredSkill = requiredSkill,
            requiredLevel = requiredLevel,
            type = type,
            magnitude = magnitude.toInt(),
            cooldownSeconds = 0
        )
    }

    @Test
    fun testGainSkillXPAddsXP() = runTest {
        val initialSkill = createTestSkill(currentXP = 50)
        val skillTree = SkillTree(skills = mapOf(initialSkill.id to initialSkill))
        val manager = SkillManager(skillTree)

        val updatedSkill = manager.gainSkillXP(initialSkill.id, 30)

        assertNotNull(updatedSkill)
        assertEquals(80, updatedSkill.currentXP)
        assertEquals(80, manager.getSkill(initialSkill.id)?.currentXP)
    }

    @Test
    fun testGainSkillXPNegativeAmountFails() = runTest {
        val manager = SkillManager()
        
        assertFailsWith<IllegalArgumentException> {
            manager.gainSkillXP(SkillId("skill_foraging"), -10)
        }
    }

    @Test
    fun testGainSkillXPInvalidSkillReturnsNull() = runTest {
        val manager = SkillManager()
        
        val result = manager.gainSkillXP(SkillId("invalid_skill"), 100)
        
        assertNull(result)
    }

    @Test
    fun testGainSkillXPDoesNotAutoLevel() = runTest {
        val initialSkill = createTestSkill(currentXP = 90)
        val skillTree = SkillTree(skills = mapOf(initialSkill.id to initialSkill))
        val manager = SkillManager(skillTree)

        manager.gainSkillXP(initialSkill.id, 20) // 110 XP total, enough to level

        val skill = manager.getSkill(initialSkill.id)
        assertEquals(1, skill?.level) // Should still be level 1
        assertEquals(110, skill?.currentXP)
    }

    @Test
    fun testLevelUpSkillWithEnoughXP() = runTest {
        val initialSkill = createTestSkill(currentXP = 150) // Enough for level 2
        val skillTree = SkillTree(skills = mapOf(initialSkill.id to initialSkill))
        val manager = SkillManager(skillTree)

        val leveledSkill = manager.levelUpSkill(initialSkill.id)

        assertNotNull(leveledSkill)
        assertEquals(2, leveledSkill.level)
        assertEquals(50, leveledSkill.currentXP) // 150 - 100 = 50 overflow
    }

    @Test
    fun testLevelUpSkillWithoutEnoughXP() = runTest {
        val initialSkill = createTestSkill(currentXP = 50) // Not enough for level 2
        val skillTree = SkillTree(skills = mapOf(initialSkill.id to initialSkill))
        val manager = SkillManager(skillTree)

        val result = manager.levelUpSkill(initialSkill.id)

        assertNull(result)
        assertEquals(1, manager.getSkill(initialSkill.id)?.level)
    }

    @Test
    fun testLevelUpSkillAtMaxLevel() = runTest {
        val maxLevelSkill = createTestSkill(level = 10, currentXP = 1000)
        val skillTree = SkillTree(skills = mapOf(maxLevelSkill.id to maxLevelSkill))
        val manager = SkillManager(skillTree)

        val result = manager.levelUpSkill(maxLevelSkill.id)

        assertNull(result)
        assertEquals(10, manager.getSkill(maxLevelSkill.id)?.level)
    }

    @Test
    fun testUnlockAbility() = runTest {
        val skill = createTestSkill()
        val skillTree = SkillTree(skills = mapOf(skill.id to skill))
        val manager = SkillManager(skillTree)
        val abilityId = AbilityId("ability_harvest_1")

        val updatedSkill = manager.unlockAbility(skill.id, abilityId)

        assertNotNull(updatedSkill)
        assertTrue(updatedSkill.hasAbility(abilityId))
        assertTrue(manager.hasAbility(abilityId))
    }

    @Test
    fun testCheckAbilityRequirementsMet() = runTest {
        val skill = createTestSkill(level = 3)
        val skillTree = SkillTree(skills = mapOf(skill.id to skill))
        val manager = SkillManager(skillTree)
        val ability = createTestAbility(requiredLevel = 2)

        val canUnlock = manager.checkAbilityRequirements(ability)

        assertTrue(canUnlock)
    }

    @Test
    fun testCheckAbilityRequirementsNotMet() = runTest {
        val skill = createTestSkill(level = 1)
        val skillTree = SkillTree(skills = mapOf(skill.id to skill))
        val manager = SkillManager(skillTree)
        val ability = createTestAbility(requiredLevel = 5)

        val canUnlock = manager.checkAbilityRequirements(ability)

        assertFalse(canUnlock)
    }

    @Test
    fun testMeetsRequirementLevel() = runTest {
        val skill = createTestSkill(level = 5)
        val skillTree = SkillTree(skills = mapOf(skill.id to skill))
        val manager = SkillManager(skillTree)
        val requirement = SkillRequirement.Level(skill.id, 3)

        assertTrue(manager.meetsRequirement(requirement))
    }

    @Test
    fun testMeetsRequirementTotalPoints() = runTest {
        val skill1 = createTestSkill(id = SkillId("skill_1"), level = 3)
        val skill2 = createTestSkill(id = SkillId("skill_2"), level = 5)
        val skillTree = SkillTree(
            skills = mapOf(skill1.id to skill1, skill2.id to skill2),
            totalSkillPoints = 7 // 2 + 4 = 6 points earned (level - 1)
        )
        val manager = SkillManager(skillTree)
        val requirement = SkillRequirement.TotalPoints(5)

        assertTrue(manager.meetsRequirement(requirement))
    }

    @Test
    fun testGetTotalBonus() = runTest {
        val ability1 = createTestAbility(
            id = AbilityId("ability_1"),
            type = AbilityType.HARVEST_BONUS,
            magnitude = 10.0
        )
        val ability2 = createTestAbility(
            id = AbilityId("ability_2"),
            type = AbilityType.HARVEST_BONUS,
            magnitude = 15.0
        )
        
        val skill = Skill(
            id = SkillId("skill_foraging"),
            type = SkillType.FORAGING,
            nameKey = "skill.foraging.name",
            descriptionKey = "skill.foraging.description",
            level = 1,
            currentXP = 0,
            unlockedAbilities = setOf(ability1.id, ability2.id)
        )
        
        val skillTree = SkillTree(skills = mapOf(skill.id to skill))
        val abilityDefs = mapOf(ability1.id to ability1, ability2.id to ability2)
        val manager = SkillManager(skillTree, abilityDefs)

        val totalBonus = manager.getTotalBonus(AbilityType.HARVEST_BONUS)

        assertEquals(25, totalBonus) // Int, not Double
    }

    @Test
    fun testGetActiveBonuses() = runTest {
        val harvestAbility = createTestAbility(
            id = AbilityId("harvest"),
            type = AbilityType.HARVEST_BONUS,
            magnitude = 20.0
        )
        val craftAbility = createTestAbility(
            id = AbilityId("craft"),
            type = AbilityType.CRAFT_SUCCESS,
            magnitude = 10.0
        )
        
        val skill = Skill(
            id = SkillId("skill_foraging"),
            type = SkillType.FORAGING,
            nameKey = "skill.foraging.name",
            descriptionKey = "skill.foraging.description",
            level = 1,
            currentXP = 0,
            unlockedAbilities = setOf(harvestAbility.id, craftAbility.id)
        )
        
        val skillTree = SkillTree(skills = mapOf(skill.id to skill))
        val abilityDefs = mapOf(harvestAbility.id to harvestAbility, craftAbility.id to craftAbility)
        val manager = SkillManager(skillTree, abilityDefs)

        val bonuses = manager.getActiveBonuses()

        assertEquals(2, bonuses.size)
        assertEquals(20, bonuses[AbilityType.HARVEST_BONUS])
        assertEquals(10, bonuses[AbilityType.CRAFT_SUCCESS])
    }

    @Test
    fun testGetLevelableSkills() = runTest {
        val skill1 = createTestSkill(id = SkillId("skill_1"), currentXP = 150) // Can level
        val skill2 = createTestSkill(id = SkillId("skill_2"), currentXP = 50)  // Cannot level
        val skill3 = createTestSkill(id = SkillId("skill_3"), level = 10)      // Max level
        
        val skillTree = SkillTree(skills = mapOf(
            skill1.id to skill1,
            skill2.id to skill2,
            skill3.id to skill3
        ))
        val manager = SkillManager(skillTree)

        val levelable = manager.getLevelableSkills()

        assertEquals(1, levelable.size)
        assertEquals(skill1.id, levelable[0].id)
    }

    @Test
    fun testGetSkillByType() = runTest {
        val foragingSkill = createTestSkill(
            id = SkillId("skill_foraging"),
            type = SkillType.FORAGING
        )
        val alchemySkill = createTestSkill(
            id = SkillId("skill_alchemy"),
            type = SkillType.ALCHEMY
        )
        
        val skillTree = SkillTree(skills = mapOf(
            foragingSkill.id to foragingSkill,
            alchemySkill.id to alchemySkill
        ))
        val manager = SkillManager(skillTree)

        val retrieved = manager.getSkillByType(SkillType.ALCHEMY)

        assertNotNull(retrieved)
        assertEquals(alchemySkill.id, retrieved.id)
    }

    @Test
    fun testGetTotalSkillPoints() = runTest {
        val skill1 = createTestSkill(id = SkillId("skill_1"), level = 3)
        val skill2 = createTestSkill(id = SkillId("skill_2"), level = 5)
        val skillTree = SkillTree(
            skills = mapOf(skill1.id to skill1, skill2.id to skill2),
            totalSkillPoints = 6 // (3-1) + (5-1) = 6
        )
        val manager = SkillManager(skillTree)

        assertEquals(6, manager.getTotalSkillPoints())
    }

    @Test
    fun testResetSkillTree() = runTest {
        val skill = createTestSkill(level = 5, currentXP = 500)
        val skillTree = SkillTree(skills = mapOf(skill.id to skill))
        val manager = SkillManager(skillTree)

        manager.resetSkillTree()

        val state = manager.skillTreeState.first()
        assertTrue(state.skills.isEmpty())
        assertEquals(0, state.totalSkillPoints)
    }

    @Test
    fun testUpdateSkillTree() = runTest {
        val manager = SkillManager()
        val newSkill = createTestSkill(level = 7, currentXP = 1000)
        val newSkillTree = SkillTree(skills = mapOf(newSkill.id to newSkill))

        manager.updateSkillTree(newSkillTree)

        val state = manager.skillTreeState.first()
        assertEquals(1, state.skills.size)
        assertEquals(7, state.getSkill(newSkill.id)?.level)
    }

    @Test
    fun testGetSkillsByXP() = runTest {
        val skill1 = createTestSkill(id = SkillId("skill_1"), currentXP = 500)
        val skill2 = createTestSkill(id = SkillId("skill_2"), currentXP = 1000)
        val skill3 = createTestSkill(id = SkillId("skill_3"), currentXP = 250)
        
        val skillTree = SkillTree(skills = mapOf(
            skill1.id to skill1,
            skill2.id to skill2,
            skill3.id to skill3
        ))
        val manager = SkillManager(skillTree)

        val sorted = manager.getSkillsByXP()

        assertEquals(3, sorted.size)
        assertEquals(skill2.id, sorted[0].id) // 1000 XP
        assertEquals(skill1.id, sorted[1].id) // 500 XP
        assertEquals(skill3.id, sorted[2].id) // 250 XP
    }

    @Test
    fun testGetSkillsByLevel() = runTest {
        val skill1 = createTestSkill(id = SkillId("skill_1"), level = 3)
        val skill2 = createTestSkill(id = SkillId("skill_2"), level = 7)
        val skill3 = createTestSkill(id = SkillId("skill_3"), level = 5)
        
        val skillTree = SkillTree(skills = mapOf(
            skill1.id to skill1,
            skill2.id to skill2,
            skill3.id to skill3
        ))
        val manager = SkillManager(skillTree)

        val sorted = manager.getSkillsByLevel()

        assertEquals(3, sorted.size)
        assertEquals(skill2.id, sorted[0].id) // Level 7
        assertEquals(skill3.id, sorted[1].id) // Level 5
        assertEquals(skill1.id, sorted[2].id) // Level 3
    }

    @Test
    fun testGetSkillsWithAvailableAbilities() = runTest {
        val foragingSkill = createTestSkill(
            id = SkillId("skill_foraging"),
            type = SkillType.FORAGING,
            level = 3
        )
        
        val skillTree = SkillTree(skills = mapOf(foragingSkill.id to foragingSkill))
        val manager = SkillManager(skillTree)
        
        val ability1 = createTestAbility(
            id = AbilityId("ability_1"),
            requiredSkill = foragingSkill.id,
            requiredLevel = 2
        )
        val ability2 = createTestAbility(
            id = AbilityId("ability_2"),
            requiredSkill = foragingSkill.id,
            requiredLevel = 5 // Too high
        )
        
        val available = manager.getSkillsWithAvailableAbilities(listOf(ability1, ability2))

        assertEquals(1, available.size)
        assertEquals(foragingSkill.id, available[0].first.id)
        assertEquals(1, available[0].second.size)
        assertEquals(ability1.id, available[0].second[0].id)
    }

    @Test
    fun testSkillManagerStateFlowUpdates() = runTest {
        val skill = createTestSkill(currentXP = 50)
        val skillTree = SkillTree(skills = mapOf(skill.id to skill))
        val manager = SkillManager(skillTree)

        // Initial state
        val initialState = manager.skillTreeState.first()
        assertEquals(50, initialState.getSkill(skill.id)?.currentXP)

        // Gain XP
        manager.gainSkillXP(skill.id, 30)

        // State should be updated
        val updatedState = manager.skillTreeState.first()
        assertEquals(80, updatedState.getSkill(skill.id)?.currentXP)
    }
    
    @Test
    fun testArchetypeXPBonusIntegration() {
        // Setup archetype with XP bonuses
        val initialPlayer = Player(id = "test", name = "Test")
        val gameStateManager = GameStateManager(initialPlayer) { currentTime }
        val talentTreeCatalog = TalentTreeCatalog()
        val archetypeManager = ArchetypeManager(gameStateManager, talentTreeCatalog)
        
        // Select Scholar archetype and unlock XP bonuses
        archetypeManager.selectArchetype(ArchetypeType.SCHOLAR)
        archetypeManager.grantTalentPoints(10)
        archetypeManager.unlockTalent("scholar_quick_study") // +15% GENERAL_XP_BONUS
        
        // Create SkillManager with archetype integration
        val skill = createTestSkill(currentXP = 0)
        val skillTree = SkillTree(skills = mapOf(skill.id to skill))
        val manager = SkillManager(skillTree, emptyMap(), archetypeManager)
        
        // Gain 100 XP - should get +15% bonus = 115 XP total
        manager.gainSkillXP(skill.id, 100)
        
        val updatedSkill = manager.skillTreeState.value.getSkill(skill.id)
        assertEquals(115, updatedSkill?.currentXP)
    }
    
    @Test
    fun testArchetypeStackedXPBonuses() {
        // Setup archetype with multiple XP bonuses
        val initialPlayer = Player(id = "test", name = "Test")
        val gameStateManager = GameStateManager(initialPlayer) { currentTime }
        val talentTreeCatalog = TalentTreeCatalog()
        val archetypeManager = ArchetypeManager(gameStateManager, talentTreeCatalog)
        
        archetypeManager.selectArchetype(ArchetypeType.SCHOLAR)
        archetypeManager.grantTalentPoints(20)
        archetypeManager.gainArchetypeXP(5000) // Level up to 7
        
        // Unlock multiple XP bonuses
        archetypeManager.unlockTalent("scholar_quick_study") // +15% GENERAL_XP_BONUS
        archetypeManager.unlockTalent("scholar_voracious_reader") // +25% SKILL_XP_BONUS
        archetypeManager.unlockTalent("scholar_enlightened") // +50% GENERAL_XP_BONUS
        
        // Total: 65% GENERAL + 25% SKILL = 90% bonus
        
        val skill = createTestSkill(currentXP = 0)
        val skillTree = SkillTree(skills = mapOf(skill.id to skill))
        val manager = SkillManager(skillTree, emptyMap(), archetypeManager)
        
        // Gain 100 XP - should get +90% bonus = 190 XP total
        manager.gainSkillXP(skill.id, 100)
        
        val updatedSkill = manager.skillTreeState.value.getSkill(skill.id)
        assertEquals(190, updatedSkill?.currentXP)
    }
}
