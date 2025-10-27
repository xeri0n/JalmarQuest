package com.jalmarquest.core.model

import kotlin.test.*

/**
 * Tests for Skill system data models.
 */
class SkillTest {
    
    @Test
    fun testSkillXPCurveProgression() {
        val skill = Skill(
            id = SkillId("foraging"),
            type = SkillType.FORAGING,
            nameKey = "skill_foraging",
            descriptionKey = "skill_foraging_desc"
        )
        
        // Level 1→2 requires 100 XP
        assertEquals(100, skill.xpForNextLevel())
        
        // Level 5→6 requires 2000 XP
        val level5 = skill.copy(level = 5)
        assertEquals(2_000, level5.xpForNextLevel())
        
        // Level 9→10 requires 10,000 XP
        val level9 = skill.copy(level = 9)
        assertEquals(10_000, level9.xpForNextLevel())
        
        // Level 10 is max
        val level10 = skill.copy(level = 10)
        assertNull(level10.xpForNextLevel())
    }
    
    @Test
    fun testSkillCanLevelUp() {
        val skill = Skill(
            id = SkillId("alchemy"),
            type = SkillType.ALCHEMY,
            nameKey = "skill_alchemy",
            descriptionKey = "skill_alchemy_desc",
            currentXP = 99
        )
        
        // 99 XP is not enough for level 1→2 (needs 100)
        assertFalse(skill.canLevelUp())
        
        // 100 XP is exactly enough
        assertTrue(skill.copy(currentXP = 100).canLevelUp())
        
        // 150 XP is more than enough
        assertTrue(skill.copy(currentXP = 150).canLevelUp())
    }
    
    @Test
    fun testSkillProgressCalculation() {
        val skill = Skill(
            id = SkillId("combat"),
            type = SkillType.COMBAT,
            nameKey = "skill_combat",
            descriptionKey = "skill_combat_desc",
            currentXP = 50
        )
        
        // 50/100 = 50% progress
        assertEquals(0.5, skill.progressToNextLevel(), 0.01)
        
        // 0/100 = 0% progress
        assertEquals(0.0, skill.copy(currentXP = 0).progressToNextLevel(), 0.01)
        
        // 100/100 = 100% progress
        assertEquals(1.0, skill.copy(currentXP = 100).progressToNextLevel(), 0.01)
        
        // Max level always shows 100%
        val maxLevel = skill.copy(level = 10, currentXP = 0)
        assertEquals(1.0, maxLevel.progressToNextLevel(), 0.01)
    }
    
    @Test
    fun testSkillAddXP() {
        val skill = Skill(
            id = SkillId("bartering"),
            type = SkillType.BARTERING,
            nameKey = "skill_bartering",
            descriptionKey = "skill_bartering_desc",
            currentXP = 50
        )
        
        val updated = skill.addXP(75)
        assertEquals(125, updated.currentXP)
        assertEquals(1, updated.level) // Level doesn't auto-increase
    }
    
    @Test
    fun testSkillLevelUp() {
        val skill = Skill(
            id = SkillId("hoarding"),
            type = SkillType.HOARDING,
            nameKey = "skill_hoarding",
            descriptionKey = "skill_hoarding_desc",
            level = 1,
            currentXP = 150 // 50 overflow beyond 100 requirement
        )
        
        val leveled = skill.levelUp()
        
        assertEquals(2, leveled.level)
        assertEquals(50, leveled.currentXP) // Overflow preserved
    }
    
    @Test
    fun testSkillLevelUpRequiresEnoughXP() {
        val skill = Skill(
            id = SkillId("test"),
            type = SkillType.FORAGING,
            nameKey = "test",
            descriptionKey = "test",
            currentXP = 50 // Not enough for level up
        )
        
        assertFailsWith<IllegalArgumentException> {
            skill.levelUp()
        }
    }
    
    @Test
    fun testSkillLevelUpAtMaxLevelFails() {
        val skill = Skill(
            id = SkillId("test"),
            type = SkillType.SCHOLARSHIP,
            nameKey = "test",
            descriptionKey = "test",
            level = 10,
            currentXP = 10000
        )
        
        assertFailsWith<IllegalArgumentException> {
            skill.levelUp()
        }
    }
    
    @Test
    fun testSkillAbilityManagement() {
        val skill = Skill(
            id = SkillId("test"),
            type = SkillType.ALCHEMY,
            nameKey = "test",
            descriptionKey = "test"
        )
        
        assertFalse(skill.hasAbility(AbilityId("master_craft")))
        
        val withAbility = skill.unlockAbility(AbilityId("master_craft"))
        assertTrue(withAbility.hasAbility(AbilityId("master_craft")))
        
        // Can unlock multiple abilities
        val withTwo = withAbility.unlockAbility(AbilityId("recipe_mastery"))
        assertTrue(withTwo.hasAbility(AbilityId("master_craft")))
        assertTrue(withTwo.hasAbility(AbilityId("recipe_mastery")))
    }
    
    @Test
    fun testSkillTreeGetters() {
        val foraging = Skill(
            id = SkillId("foraging"),
            type = SkillType.FORAGING,
            nameKey = "foraging",
            descriptionKey = "foraging_desc",
            level = 3
        )
        
        val alchemy = Skill(
            id = SkillId("alchemy"),
            type = SkillType.ALCHEMY,
            nameKey = "alchemy",
            descriptionKey = "alchemy_desc",
            level = 5
        )
        
        val tree = SkillTree(
            skills = mapOf(
                SkillId("foraging") to foraging,
                SkillId("alchemy") to alchemy
            )
        )
        
        assertEquals(foraging, tree.getSkill(SkillId("foraging")))
        assertEquals(alchemy, tree.getSkillByType(SkillType.ALCHEMY))
        assertNull(tree.getSkill(SkillId("nonexistent")))
    }
    
    @Test
    fun testSkillTreeRequirementChecking() {
        val tree = SkillTree(
            skills = mapOf(
                SkillId("foraging") to Skill(
                    id = SkillId("foraging"),
                    type = SkillType.FORAGING,
                    nameKey = "foraging",
                    descriptionKey = "desc",
                    level = 5
                ),
                SkillId("alchemy") to Skill(
                    id = SkillId("alchemy"),
                    type = SkillType.ALCHEMY,
                    nameKey = "alchemy",
                    descriptionKey = "desc",
                    level = 3
                )
            ),
            totalSkillPoints = 8
        )
        
        // Level requirement met
        assertTrue(tree.meetsRequirement(
            SkillRequirement.Level(SkillId("foraging"), 5)
        ))
        
        // Level requirement not met
        assertFalse(tree.meetsRequirement(
            SkillRequirement.Level(SkillId("foraging"), 6)
        ))
        
        // Total points requirement met
        assertTrue(tree.meetsRequirement(
            SkillRequirement.TotalPoints(8)
        ))
        
        // Total points requirement not met
        assertFalse(tree.meetsRequirement(
            SkillRequirement.TotalPoints(10)
        ))
        
        // All requirements met
        assertTrue(tree.meetsRequirement(
            SkillRequirement.All(listOf(
                SkillRequirement.Level(SkillId("foraging"), 5),
                SkillRequirement.Level(SkillId("alchemy"), 3)
            ))
        ))
        
        // All requirements not met
        assertFalse(tree.meetsRequirement(
            SkillRequirement.All(listOf(
                SkillRequirement.Level(SkillId("foraging"), 5),
                SkillRequirement.Level(SkillId("alchemy"), 5)
            ))
        ))
        
        // Any requirement met
        assertTrue(tree.meetsRequirement(
            SkillRequirement.Any(listOf(
                SkillRequirement.Level(SkillId("foraging"), 10),
                SkillRequirement.Level(SkillId("alchemy"), 3)
            ))
        ))
    }
    
    @Test
    fun testSkillTreeUpdateSkill() {
        val initialSkill = Skill(
            id = SkillId("foraging"),
            type = SkillType.FORAGING,
            nameKey = "foraging",
            descriptionKey = "desc",
            level = 2
        )
        
        val tree = SkillTree(
            skills = mapOf(SkillId("foraging") to initialSkill),
            totalSkillPoints = 1 // 1 point for level 2
        )
        
        val leveledSkill = initialSkill.copy(level = 5)
        val updated = tree.updateSkill(SkillId("foraging"), leveledSkill)
        
        assertEquals(5, updated.getSkill(SkillId("foraging"))?.level)
        assertEquals(4, updated.totalSkillPoints) // Gained 3 points (5 - 2)
    }
    
    @Test
    fun testSkillTreeAddXP() {
        val tree = SkillTree(
            skills = mapOf(
                SkillId("combat") to Skill(
                    id = SkillId("combat"),
                    type = SkillType.COMBAT,
                    nameKey = "combat",
                    descriptionKey = "desc",
                    currentXP = 50
                )
            )
        )
        
        val updated = tree.addSkillXP(SkillId("combat"), 25)
        assertEquals(75, updated.getSkill(SkillId("combat"))?.currentXP)
    }
    
    @Test
    fun testSkillTreeGetAllUnlockedAbilities() {
        val tree = SkillTree(
            skills = mapOf(
                SkillId("foraging") to Skill(
                    id = SkillId("foraging"),
                    type = SkillType.FORAGING,
                    nameKey = "foraging",
                    descriptionKey = "desc",
                    unlockedAbilities = setOf(AbilityId("power_harvest"), AbilityId("herb_sense"))
                ),
                SkillId("alchemy") to Skill(
                    id = SkillId("alchemy"),
                    type = SkillType.ALCHEMY,
                    nameKey = "alchemy",
                    descriptionKey = "desc",
                    unlockedAbilities = setOf(AbilityId("master_craft"))
                )
            )
        )
        
        val abilities = tree.getAllUnlockedAbilities()
        assertEquals(3, abilities.size)
        assertTrue(abilities.contains(AbilityId("power_harvest")))
        assertTrue(abilities.contains(AbilityId("herb_sense")))
        assertTrue(abilities.contains(AbilityId("master_craft")))
    }
    
    @Test
    fun testSkillTreeGetTotalBonus() {
        val tree = SkillTree(
            skills = mapOf(
                SkillId("foraging") to Skill(
                    id = SkillId("foraging"),
                    type = SkillType.FORAGING,
                    nameKey = "foraging",
                    descriptionKey = "desc",
                    unlockedAbilities = setOf(AbilityId("harvest1"), AbilityId("harvest2"))
                )
            )
        )
        
        val abilities = mapOf(
            AbilityId("harvest1") to Ability(
                id = AbilityId("harvest1"),
                nameKey = "harvest1",
                descriptionKey = "desc",
                requiredSkill = SkillId("foraging"),
                requiredLevel = 2,
                type = AbilityType.HARVEST_BONUS,
                magnitude = 10
            ),
            AbilityId("harvest2") to Ability(
                id = AbilityId("harvest2"),
                nameKey = "harvest2",
                descriptionKey = "desc",
                requiredSkill = SkillId("foraging"),
                requiredLevel = 5,
                type = AbilityType.HARVEST_BONUS,
                magnitude = 15
            ),
            AbilityId("craft1") to Ability(
                id = AbilityId("craft1"),
                nameKey = "craft1",
                descriptionKey = "desc",
                requiredSkill = SkillId("alchemy"),
                requiredLevel = 3,
                type = AbilityType.CRAFT_SUCCESS,
                magnitude = 20
            )
        )
        
        // Sum of harvest bonuses = 10 + 15 = 25
        val harvestBonus = tree.getTotalBonus(AbilityType.HARVEST_BONUS, abilities)
        assertEquals(25, harvestBonus)
        
        // Craft ability not unlocked
        val craftBonus = tree.getTotalBonus(AbilityType.CRAFT_SUCCESS, abilities)
        assertEquals(0, craftBonus)
    }
}
