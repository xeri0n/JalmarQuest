package com.jalmarquest.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuestTest {
    
    @Test
    fun testQuestObjectiveCompletion() {
        val objective = QuestObjective(
            objectiveId = "collect_acorns",
            description = "Collect 10 acorns",
            type = QuestObjectiveType.COLLECT_ITEMS,
            targetId = "acorn",
            targetQuantity = 10,
            currentProgress = 0
        )
        
        assertFalse(objective.isComplete(), "Should not be complete initially")
        assertEquals(0.0, objective.progressPercentage(), 0.01)
        
        val updated = objective.updateProgress(5)
        assertEquals(5, updated.currentProgress)
        assertEquals(0.5, updated.progressPercentage(), 0.01)
        assertFalse(updated.isComplete())
        
        val completed = updated.updateProgress(5)
        assertEquals(10, completed.currentProgress)
        assertEquals(1.0, completed.progressPercentage(), 0.01)
        assertTrue(completed.isComplete())
    }
    
    @Test
    fun testQuestObjectiveProgressClamping() {
        val objective = QuestObjective(
            objectiveId = "test",
            description = "Test",
            type = QuestObjectiveType.COLLECT_ITEMS,
            targetQuantity = 10
        )
        
        // Test over-progress clamping
        val overprogress = objective.updateProgress(20)
        assertEquals(10, overprogress.currentProgress, "Should clamp to max")
        
        // Test negative progress clamping
        val negativeprogress = objective.updateProgress(-5)
        assertEquals(0, negativeprogress.currentProgress, "Should clamp to 0")
    }
    
    @Test
    fun testQuestAllRequiredObjectivesComplete() {
        val quest = Quest(
            questId = QuestId("test_quest"),
            title = "Test Quest",
            description = "A test quest",
            objectives = listOf(
                QuestObjective("obj1", "Required 1", QuestObjectiveType.COLLECT_ITEMS, targetQuantity = 5, currentProgress = 5),
                QuestObjective("obj2", "Required 2", QuestObjectiveType.DEFEAT_ENEMIES, targetQuantity = 3, currentProgress = 3),
                QuestObjective("obj3", "Optional", QuestObjectiveType.REACH_LOCATION, targetQuantity = 1, currentProgress = 0, isOptional = true)
            ),
            rewards = emptyList()
        )
        
        assertTrue(quest.areRequiredObjectivesComplete(), "Should be complete when all required objectives done")
    }
    
    @Test
    fun testQuestRequiredObjectivesIncomplete() {
        val quest = Quest(
            questId = QuestId("test_quest"),
            title = "Test Quest",
            description = "A test quest",
            objectives = listOf(
                QuestObjective("obj1", "Required 1", QuestObjectiveType.COLLECT_ITEMS, targetQuantity = 5, currentProgress = 5),
                QuestObjective("obj2", "Required 2", QuestObjectiveType.DEFEAT_ENEMIES, targetQuantity = 3, currentProgress = 1),
            ),
            rewards = emptyList()
        )
        
        assertFalse(quest.areRequiredObjectivesComplete(), "Should not be complete when required objectives incomplete")
    }
    
    @Test
    fun testQuestOverallProgress() {
        val quest = Quest(
            questId = QuestId("test_quest"),
            title = "Test Quest",
            description = "A test quest",
            objectives = listOf(
                QuestObjective("obj1", "Collect 10", QuestObjectiveType.COLLECT_ITEMS, targetQuantity = 10, currentProgress = 5),  // 50%
                QuestObjective("obj2", "Defeat 5", QuestObjectiveType.DEFEAT_ENEMIES, targetQuantity = 5, currentProgress = 5),    // 100%
                QuestObjective("obj3", "Talk to NPC", QuestObjectiveType.TALK_TO_NPC, targetQuantity = 1, currentProgress = 0),    // 0%
            ),
            rewards = emptyList()
        )
        
        // (0.5 + 1.0 + 0.0) / 3 = 0.5
        assertEquals(0.5, quest.overallProgress(), 0.01)
    }
    
    @Test
    fun testQuestProgressCanTurnIn() {
        val progress = QuestProgress(
            questId = QuestId("test"),
            status = QuestStatus.ACTIVE,
            objectives = listOf(
                QuestObjective("obj1", "Required", QuestObjectiveType.COLLECT_ITEMS, targetQuantity = 5, currentProgress = 5),
                QuestObjective("obj2", "Optional", QuestObjectiveType.COLLECT_ITEMS, targetQuantity = 3, currentProgress = 0, isOptional = true)
            ),
            acceptedAt = 1000000L
        )
        
        assertTrue(progress.canTurnIn(), "Should be able to turn in when all required objectives complete")
    }
    
    @Test
    fun testQuestProgressCannotTurnInWhenIncomplete() {
        val progress = QuestProgress(
            questId = QuestId("test"),
            status = QuestStatus.ACTIVE,
            objectives = listOf(
                QuestObjective("obj1", "Required", QuestObjectiveType.COLLECT_ITEMS, targetQuantity = 5, currentProgress = 3)
            ),
            acceptedAt = 1000000L
        )
        
        assertFalse(progress.canTurnIn(), "Should not be able to turn in when objectives incomplete")
    }
    
    @Test
    fun testQuestProgressCannotTurnInWhenNotActive() {
        val progress = QuestProgress(
            questId = QuestId("test"),
            status = QuestStatus.COMPLETED,
            objectives = listOf(
                QuestObjective("obj1", "Required", QuestObjectiveType.COLLECT_ITEMS, targetQuantity = 5, currentProgress = 5)
            ),
            acceptedAt = 1000000L,
            completedAt = 2000000L
        )
        
        assertFalse(progress.canTurnIn(), "Should not be able to turn in when already completed")
    }
    
    @Test
    fun testQuestProgressUpdateObjective() {
        val progress = QuestProgress(
            questId = QuestId("test"),
            status = QuestStatus.ACTIVE,
            objectives = listOf(
                QuestObjective("obj1", "Collect", QuestObjectiveType.COLLECT_ITEMS, targetQuantity = 10, currentProgress = 5),
                QuestObjective("obj2", "Defeat", QuestObjectiveType.DEFEAT_ENEMIES, targetQuantity = 5, currentProgress = 2)
            ),
            acceptedAt = 1000000L
        )
        
        val updated = progress.updateObjective("obj1", 3)
        assertEquals(8, updated.objectives[0].currentProgress, "Should update first objective")
        assertEquals(2, updated.objectives[1].currentProgress, "Should not affect second objective")
    }
    
    @Test
    fun testQuestLogIsQuestActive() {
        val questLog = QuestLog(
            activeQuests = listOf(
                QuestProgress(QuestId("quest1"), QuestStatus.ACTIVE, emptyList(), 1000000L),
                QuestProgress(QuestId("quest2"), QuestStatus.ACTIVE, emptyList(), 1000000L)
            )
        )
        
        assertTrue(questLog.isQuestActive(QuestId("quest1")))
        assertTrue(questLog.isQuestActive(QuestId("quest2")))
        assertFalse(questLog.isQuestActive(QuestId("quest3")))
    }
    
    @Test
    fun testQuestLogIsQuestCompleted() {
        val questLog = QuestLog(
            completedQuests = listOf(QuestId("quest1"), QuestId("quest2"))
        )
        
        assertTrue(questLog.isQuestCompleted(QuestId("quest1")))
        assertFalse(questLog.isQuestCompleted(QuestId("quest3")))
    }
    
    @Test
    fun testQuestLogGetActiveQuest() {
        val progress1 = QuestProgress(QuestId("quest1"), QuestStatus.ACTIVE, emptyList(), 1000000L)
        val progress2 = QuestProgress(QuestId("quest2"), QuestStatus.ACTIVE, emptyList(), 2000000L)
        val questLog = QuestLog(activeQuests = listOf(progress1, progress2))
        
        val found = questLog.getActiveQuest(QuestId("quest1"))
        assertNotNull(found)
        assertEquals(QuestId("quest1"), found.questId)
        
        val notFound = questLog.getActiveQuest(QuestId("quest3"))
        assertNull(notFound)
    }
    
    @Test
    fun testQuestLogAddActiveQuest() {
        val questLog = QuestLog()
        val progress = QuestProgress(QuestId("quest1"), QuestStatus.ACTIVE, emptyList(), 1000000L)
        
        val updated = questLog.addActiveQuest(progress)
        assertEquals(1, updated.activeQuests.size)
        assertTrue(updated.isQuestActive(QuestId("quest1")))
    }
    
    @Test
    fun testQuestLogUpdateActiveQuest() {
        val progress = QuestProgress(
            questId = QuestId("quest1"),
            status = QuestStatus.ACTIVE,
            objectives = listOf(
                QuestObjective("obj1", "Collect", QuestObjectiveType.COLLECT_ITEMS, targetQuantity = 10, currentProgress = 5)
            ),
            acceptedAt = 1000000L
        )
        val questLog = QuestLog(activeQuests = listOf(progress))
        
        val updated = questLog.updateActiveQuest(QuestId("quest1")) { quest ->
            quest.updateObjective("obj1", 3)
        }
        
        val updatedQuest = updated.getActiveQuest(QuestId("quest1"))
        assertNotNull(updatedQuest)
        assertEquals(8, updatedQuest.objectives[0].currentProgress)
    }
    
    @Test
    fun testQuestLogCompleteQuest() {
        val progress = QuestProgress(QuestId("quest1"), QuestStatus.ACTIVE, emptyList(), 1000000L)
        val questLog = QuestLog(activeQuests = listOf(progress))
        
        val updated = questLog.completeQuest(QuestId("quest1"))
        
        assertFalse(updated.isQuestActive(QuestId("quest1")), "Should remove from active")
        assertTrue(updated.isQuestCompleted(QuestId("quest1")), "Should add to completed")
        assertEquals(0, updated.activeQuests.size)
        assertEquals(1, updated.completedQuests.size)
    }
    
    @Test
    fun testQuestLogAbandonQuest() {
        val progress = QuestProgress(QuestId("quest1"), QuestStatus.ACTIVE, emptyList(), 1000000L)
        val questLog = QuestLog(activeQuests = listOf(progress))
        
        val updated = questLog.abandonQuest(QuestId("quest1"))
        
        assertFalse(updated.isQuestActive(QuestId("quest1")))
        assertEquals(0, updated.activeQuests.size)
        assertEquals(1, updated.abandonedQuests.size)
        assertTrue(updated.abandonedQuests.contains(QuestId("quest1")))
    }
    
    @Test
    fun testQuestLogFailQuest() {
        val progress = QuestProgress(QuestId("quest1"), QuestStatus.ACTIVE, emptyList(), 1000000L)
        val questLog = QuestLog(activeQuests = listOf(progress))
        
        val updated = questLog.failQuest(QuestId("quest1"))
        
        assertFalse(updated.isQuestActive(QuestId("quest1")))
        assertEquals(0, updated.activeQuests.size)
        assertEquals(1, updated.failedQuests.size)
        assertTrue(updated.failedQuests.contains(QuestId("quest1")))
    }
    
    @Test
    fun testQuestRewardCreation() {
        val reward = QuestReward(
            type = QuestRewardType.SEEDS,
            quantity = 100,
            description = "100 Seeds"
        )
        
        assertEquals(QuestRewardType.SEEDS, reward.type)
        assertEquals(100, reward.quantity)
        assertNull(reward.targetId)
    }
    
    @Test
    fun testQuestRewardWithTargetId() {
        val reward = QuestReward(
            type = QuestRewardType.ITEMS,
            targetId = "golden_acorn",
            quantity = 1,
            description = "Golden Acorn"
        )
        
        assertEquals(QuestRewardType.ITEMS, reward.type)
        assertEquals("golden_acorn", reward.targetId)
        assertEquals(1, reward.quantity)
    }
    
    @Test
    fun testQuestWithMultipleRewards() {
        val quest = Quest(
            questId = QuestId("rich_quest"),
            title = "Rich Quest",
            description = "A quest with many rewards",
            objectives = listOf(
                QuestObjective("obj1", "Do something", QuestObjectiveType.COLLECT_ITEMS, targetQuantity = 1)
            ),
            rewards = listOf(
                QuestReward(QuestRewardType.SEEDS, quantity = 500, description = "500 Seeds"),
                QuestReward(QuestRewardType.EXPERIENCE, quantity = 100, description = "100 XP"),
                QuestReward(QuestRewardType.ITEMS, targetId = "rare_item", quantity = 1, description = "Rare Item")
            )
        )
        
        assertEquals(3, quest.rewards.size)
        assertTrue(quest.rewards.any { it.type == QuestRewardType.SEEDS })
        assertTrue(quest.rewards.any { it.type == QuestRewardType.EXPERIENCE })
        assertTrue(quest.rewards.any { it.type == QuestRewardType.ITEMS })
    }
    
    @Test
    fun testQuestWithPrerequisites() {
        val quest = Quest(
            questId = QuestId("advanced_quest"),
            title = "Advanced Quest",
            description = "A quest with requirements",
            objectives = listOf(
                QuestObjective("obj1", "Do something", QuestObjectiveType.COLLECT_ITEMS, targetQuantity = 1)
            ),
            rewards = emptyList(),
            requirements = listOf(
                QuestRequirement.PrerequisiteQuest(QuestId("intro_quest")),
                QuestRequirement.MinimumLevel(5),
                QuestRequirement.MinimumSkill(SkillType.COMBAT, 3)
            )
        )
        
        assertEquals(3, quest.requirements.size)
        assertTrue(quest.requirements.any { it is QuestRequirement.PrerequisiteQuest })
        assertTrue(quest.requirements.any { it is QuestRequirement.MinimumLevel })
        assertTrue(quest.requirements.any { it is QuestRequirement.MinimumSkill })
    }
    
    @Test
    fun testRepeatableQuest() {
        val progress = QuestProgress(
            questId = QuestId("daily_quest"),
            status = QuestStatus.COMPLETED,
            objectives = emptyList(),
            acceptedAt = 1000000L,
            completedAt = 2000000L,
            timesCompleted = 5
        )
        
        assertEquals(5, progress.timesCompleted)
    }
    
    @Test
    fun testQuestRemoveActiveQuest() {
        val progress1 = QuestProgress(QuestId("quest1"), QuestStatus.ACTIVE, emptyList(), 1000000L)
        val progress2 = QuestProgress(QuestId("quest2"), QuestStatus.ACTIVE, emptyList(), 2000000L)
        val questLog = QuestLog(activeQuests = listOf(progress1, progress2))
        
        val updated = questLog.removeActiveQuest(QuestId("quest1"))
        assertEquals(1, updated.activeQuests.size)
        assertFalse(updated.isQuestActive(QuestId("quest1")))
        assertTrue(updated.isQuestActive(QuestId("quest2")))
    }
}
