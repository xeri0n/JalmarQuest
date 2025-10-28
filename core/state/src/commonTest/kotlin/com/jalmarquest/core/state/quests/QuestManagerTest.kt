package com.jalmarquest.core.state.quests

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuestManagerTest {
    
    private fun createTestPlayer(
        archetypeLevel: Int = 1,
        skillLevels: Map<SkillType, Int> = emptyMap(),
        choiceTags: List<ChoiceTag> = emptyList(),
        selectedArchetype: ArchetypeType? = null
    ): Player {
        val skills = skillLevels.map { (type, level) ->
            SkillId(type.name) to Skill(
                id = SkillId(type.name),
                type = type,
                nameKey = "skill.${type.name.lowercase()}.name",
                descriptionKey = "skill.${type.name.lowercase()}.description",
                level = level
            )
        }.toMap()
        
        val choiceEntries = choiceTags.map { tag ->
            ChoiceLogEntry(tag, 1000000L)
        }
        
        return Player(
            id = "test_player",
            name = "Test Quail",
            skillTree = SkillTree(skills = skills),
            archetypeProgress = ArchetypeProgress(
                selectedArchetype = selectedArchetype,
                archetypeLevel = archetypeLevel
            ),
            choiceLog = ChoiceLog(entries = choiceEntries)
        )
    }
    
    @Test
    fun testAcceptQuestSuccess() = runTest {
    val catalog = QuestCatalog()
    val gsm = GameStateManager(initialPlayer = createTestPlayer(), timestampProvider = { 0L })
        val manager = QuestManager(catalog, gsm)
        val player = gsm.playerState.value
        
        val accepted = manager.acceptQuest(QuestId("tutorial_first_exploration"), player)
        
        assertTrue(accepted, "Should accept quest")
        assertTrue(manager.questLog.value.isQuestActive(QuestId("tutorial_first_exploration")))
        assertEquals(1, manager.getActiveQuests().size)
        // Choice tag should be appended when GameStateManager is present
        val tags = gsm.playerState.value.choiceLog.entries.map { it.tag.value }
        assertTrue(tags.contains("tutorial_first_exploration_accepted"))
    }
    
    @Test
    fun testAcceptQuestFailsWhenAlreadyActive() = runTest {
        val catalog = QuestCatalog()
        val manager = QuestManager(catalog, null)
        val player = createTestPlayer()
        
        manager.acceptQuest(QuestId("tutorial_first_exploration"), player)
        val acceptedAgain = manager.acceptQuest(QuestId("tutorial_first_exploration"), player)
        
        assertFalse(acceptedAgain, "Should not accept quest twice")
        assertEquals(1, manager.getActiveQuests().size)
    }
    
    @Test
    fun testAcceptQuestFailsWhenPrerequisiteNotMet() = runTest {
        val catalog = QuestCatalog()
        val manager = QuestManager(catalog, null)
        val player = createTestPlayer()
        
        // Try to accept quest with prerequisite
        val accepted = manager.acceptQuest(QuestId("tutorial_gather_resources"), player)
        
        assertFalse(accepted, "Should not accept quest without prerequisite")
        assertEquals(0, manager.getActiveQuests().size)
    }
    
    @Test
    fun testAcceptQuestSucceedsAfterPrerequisiteCompleted() = runTest {
        val catalog = QuestCatalog()
        val manager = QuestManager(catalog, null)
        val player = createTestPlayer()
        
        // Accept and complete prerequisite (complete its objectives first!)
        manager.acceptQuest(QuestId("tutorial_first_exploration"), player)
        manager.updateObjective(QuestId("tutorial_first_exploration"), "explore_area", 1)  // Complete the objective
        manager.completeQuest(QuestId("tutorial_first_exploration"))
        
        // Now accept quest with prerequisite
        val accepted = manager.acceptQuest(QuestId("tutorial_gather_resources"), player)
        
        assertTrue(accepted, "Should accept quest after prerequisite completed")
        assertTrue(manager.hasCompletedQuest(QuestId("tutorial_first_exploration")))
    }
    
    @Test
    fun testAcceptQuestFailsWhenLevelTooLow() = runTest {
        val catalog = QuestCatalog()
        val manager = QuestManager(catalog, null)
        val player = createTestPlayer(archetypeLevel = 1)
        
        // Complete prerequisites first (with objectives!)
        manager.acceptQuest(QuestId("tutorial_first_exploration"), player)
        manager.updateObjective(QuestId("tutorial_first_exploration"), "explore_area", 1)
        manager.completeQuest(QuestId("tutorial_first_exploration"))
        
        manager.acceptQuest(QuestId("tutorial_gather_resources"), player)
        manager.updateObjective(QuestId("tutorial_gather_resources"), "collect_acorns", 5)
        manager.completeQuest(QuestId("tutorial_gather_resources"))
        
        manager.acceptQuest(QuestId("tutorial_first_craft"), player)
        manager.updateObjective(QuestId("tutorial_first_craft"), "craft_concoction", 1)
        manager.completeQuest(QuestId("tutorial_first_craft"))
        
        // Try to accept level 2 quest with level 1 player
        val accepted = manager.acceptQuest(QuestId("choose_your_path"), player)
        
        assertFalse(accepted, "Should not accept quest when level too low")
    }
    
    @Test
    fun testAcceptQuestSucceedsWhenLevelMet() = runTest {
        val catalog = QuestCatalog()
        val manager = QuestManager(catalog, null)
        val player = createTestPlayer(archetypeLevel = 2)
        
        // Complete prerequisites (with objectives!)
        manager.acceptQuest(QuestId("tutorial_first_exploration"), player)
        manager.updateObjective(QuestId("tutorial_first_exploration"), "explore_area", 1)
        manager.completeQuest(QuestId("tutorial_first_exploration"))
        
        manager.acceptQuest(QuestId("tutorial_gather_resources"), player)
        manager.updateObjective(QuestId("tutorial_gather_resources"), "collect_acorns", 5)
        manager.completeQuest(QuestId("tutorial_gather_resources"))
        
        manager.acceptQuest(QuestId("tutorial_first_craft"), player)
        manager.updateObjective(QuestId("tutorial_first_craft"), "craft_concoction", 1)
        manager.completeQuest(QuestId("tutorial_first_craft"))
        
        // Try with level 2 player
        val accepted = manager.acceptQuest(QuestId("choose_your_path"), player)
        
        assertTrue(accepted, "Should accept quest when level requirement met")
    }
    
    @Test
    fun testUpdateObjective() = runTest {
        val catalog = QuestCatalog()
        val manager = QuestManager(catalog, null)
        val player = createTestPlayer()
        
        // First complete the prerequisite
        manager.acceptQuest(QuestId("tutorial_first_exploration"), player)
        manager.updateObjective(QuestId("tutorial_first_exploration"), "explore_area", 1)
        manager.completeQuest(QuestId("tutorial_first_exploration"))
        
        // Now accept the quest we want to test
        manager.acceptQuest(QuestId("tutorial_gather_resources"), player)
        
        // Update objective progress
        val updated = manager.updateObjective(QuestId("tutorial_gather_resources"), "collect_acorns", 3)
        
        assertTrue(updated, "Should update objective")
        val progress = manager.getQuestProgress(QuestId("tutorial_gather_resources"))
        assertNotNull(progress)
        assertEquals(3, progress.objectives[0].currentProgress)
    }
    
    @Test
    fun testCompleteQuestSuccess() = runTest {
    val catalog = QuestCatalog()
    val gsm = GameStateManager(initialPlayer = createTestPlayer(), timestampProvider = { 0L })
        val manager = QuestManager(catalog, gsm)
        val player = gsm.playerState.value
        
        manager.acceptQuest(QuestId("tutorial_first_exploration"), player)
        manager.updateObjective(QuestId("tutorial_first_exploration"), "explore_area", 1)
        
        val rewards = manager.completeQuest(QuestId("tutorial_first_exploration"))
        
        assertNotNull(rewards)
        assertEquals(2, rewards.size)
        assertTrue(rewards.any { it.type == QuestRewardType.SEEDS && it.quantity == 50 })
        assertTrue(manager.hasCompletedQuest(QuestId("tutorial_first_exploration")))
        assertFalse(manager.questLog.value.isQuestActive(QuestId("tutorial_first_exploration")))
        // Choice tag should be appended when GameStateManager is present
        val tags = gsm.playerState.value.choiceLog.entries.map { it.tag.value }
        assertTrue(tags.contains("tutorial_first_exploration_complete"))
    }
    
    @Test
    fun testCompleteQuestFailsWhenObjectivesIncomplete() = runTest {
        val catalog = QuestCatalog()
        val manager = QuestManager(catalog, null)
        val player = createTestPlayer()
        
        // Complete prerequisite first
        manager.acceptQuest(QuestId("tutorial_first_exploration"), player)
        manager.updateObjective(QuestId("tutorial_first_exploration"), "explore_area", 1)
        manager.completeQuest(QuestId("tutorial_first_exploration"))
        
        // Now accept the quest we want to test
        manager.acceptQuest(QuestId("tutorial_gather_resources"), player)
        // Don't complete objectives
        
        val rewards = manager.completeQuest(QuestId("tutorial_gather_resources"))
        
        assertNull(rewards, "Should not complete quest with incomplete objectives")
        assertTrue(manager.questLog.value.isQuestActive(QuestId("tutorial_gather_resources")))
    }
    
    @Test
    fun testAbandonQuest() = runTest {
        val catalog = QuestCatalog()
        val manager = QuestManager(catalog, null)
        val player = createTestPlayer()
        
        manager.acceptQuest(QuestId("tutorial_first_exploration"), player)
        
        val abandoned = manager.abandonQuest(QuestId("tutorial_first_exploration"))
        
        assertTrue(abandoned)
        assertFalse(manager.questLog.value.isQuestActive(QuestId("tutorial_first_exploration")))
        assertEquals(1, manager.questLog.value.abandonedQuests.size)
    }
    
    @Test
    fun testFailQuest() = runTest {
        val catalog = QuestCatalog()
        val manager = QuestManager(catalog, null)
        val player = createTestPlayer()
        
        manager.acceptQuest(QuestId("tutorial_first_exploration"), player)
        
        val failed = manager.failQuest(QuestId("tutorial_first_exploration"))
        
        assertTrue(failed)
        assertFalse(manager.questLog.value.isQuestActive(QuestId("tutorial_first_exploration")))
        assertEquals(1, manager.questLog.value.failedQuests.size)
    }
    
    @Test
    fun testLoadQuestLog() = runTest {
        val catalog = QuestCatalog()
        val manager = QuestManager(catalog, null)
        
        val existingLog = QuestLog(
            completedQuests = listOf(QuestId("tutorial_first_exploration"))
        )
        
        manager.loadQuestLog(existingLog)
        
        assertTrue(manager.hasCompletedQuest(QuestId("tutorial_first_exploration")))
    }
    
    @Test
    fun testRepeatableQuest() = runTest {
        val catalog = QuestCatalog()
        val manager = QuestManager(catalog, null)
        val player = createTestPlayer(archetypeLevel = 2)
        
        // Accept and complete repeatable quest
        manager.acceptQuest(QuestId("daily_foraging"), player)
        manager.updateObjective(QuestId("daily_foraging"), "gather_ingredients", 10)
        manager.completeQuest(QuestId("daily_foraging"))
        
        // Should be able to accept again
        val acceptedAgain = manager.acceptQuest(QuestId("daily_foraging"), player)
        
        assertTrue(acceptedAgain, "Should accept repeatable quest again")
    }
    
    @Test
    fun testGetAvailableQuests() = runTest {
        val catalog = QuestCatalog()
        val manager = QuestManager(catalog, null)
        val player = createTestPlayer(archetypeLevel = 3)
        
        val available = manager.getAvailableQuests(player)
        
        assertTrue(available.isNotEmpty())
        assertTrue(available.any { it.questId == QuestId("tutorial_first_exploration") })
    }
    
    @Test
    fun testArchetypeRequirement() = runTest {
        val catalog = QuestCatalog()
        val manager = QuestManager(catalog, null)
        
        // Create quest with archetype requirement
        val quest = Quest(
            questId = QuestId("scholar_quest"),
            title = "Scholar Quest",
            description = "A quest for scholars only",
            objectives = listOf(
                QuestObjective("obj1", "Do something", QuestObjectiveType.CUSTOM, targetQuantity = 1)
            ),
            rewards = emptyList(),
            requirements = listOf(
                QuestRequirement.ArchetypeRequirement(ArchetypeType.SCHOLAR)
            )
        )
        catalog.registerQuest(quest)
        
        val playerScholar = createTestPlayer(selectedArchetype = ArchetypeType.SCHOLAR)
        val playerCollector = createTestPlayer(selectedArchetype = ArchetypeType.COLLECTOR)
        
        assertTrue(manager.isQuestAvailable(quest, playerScholar))
        assertFalse(manager.isQuestAvailable(quest, playerCollector))
    }
    
    @Test
    fun testChoiceTagRequirement() = runTest {
        val catalog = QuestCatalog()
        val manager = QuestManager(catalog, null)
        
        // Create quest with choice tag requirement
        val quest = Quest(
            questId = QuestId("brave_quest"),
            title = "Brave Quest",
            description = "A quest for brave quails",
            objectives = listOf(
                QuestObjective("obj1", "Do something", QuestObjectiveType.CUSTOM, targetQuantity = 1)
            ),
            rewards = emptyList(),
            requirements = listOf(
                QuestRequirement.ChoiceTagRequirement(ChoiceTag("brave"))
            )
        )
        catalog.registerQuest(quest)
        
        val playerBrave = createTestPlayer(choiceTags = listOf(ChoiceTag("brave")))
        val playerTimid = createTestPlayer(choiceTags = listOf(ChoiceTag("timid")))
        
        assertTrue(manager.isQuestAvailable(quest, playerBrave))
        assertFalse(manager.isQuestAvailable(quest, playerTimid))
    }
    
    @Test
    fun testNotChoiceTagRequirement() = runTest {
        val catalog = QuestCatalog()
        val manager = QuestManager(catalog, null)
        
        // Create quest that requires NOT having a tag
        val quest = Quest(
            questId = QuestId("peaceful_quest"),
            title = "Peaceful Quest",
            description = "A quest for those who avoided violence",
            objectives = listOf(
                QuestObjective("obj1", "Do something", QuestObjectiveType.CUSTOM, targetQuantity = 1)
            ),
            rewards = emptyList(),
            requirements = listOf(
                QuestRequirement.NotChoiceTagRequirement(ChoiceTag("violent"))
            )
        )
        catalog.registerQuest(quest)
        
        val playerPeaceful = createTestPlayer(choiceTags = listOf(ChoiceTag("diplomatic")))
        val playerViolent = createTestPlayer(choiceTags = listOf(ChoiceTag("violent")))
        
        assertTrue(manager.isQuestAvailable(quest, playerPeaceful))
        assertFalse(manager.isQuestAvailable(quest, playerViolent))
    }
}
