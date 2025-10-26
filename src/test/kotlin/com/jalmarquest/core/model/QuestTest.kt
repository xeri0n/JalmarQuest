package com.jalmarquest.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QuestTest {
    
    @Test
    fun testQuestCreation() {
        val quest = Quest(
            id = "quest_001",
            title = "Fetch the Ancient Scroll",
            description = "Retrieve the ancient scroll from the library.",
            objectives = listOf(
                Objective(
                    id = "obj_001",
                    description = "Find the library key",
                    targetCount = 1
                )
            ),
            rewards = Rewards(experience = 100, gold = 50)
        )
        
        assertEquals("quest_001", quest.id)
        assertEquals("Fetch the Ancient Scroll", quest.title)
        assertEquals(QuestStatus.NOT_STARTED, quest.status)
        assertEquals(1, quest.objectives.size)
        assertEquals(100, quest.rewards.experience)
    }
    
    @Test
    fun testQuestCompletion() {
        val quest = Quest(
            id = "quest_002",
            title = "Defeat Goblins",
            description = "Clear the goblin camp.",
            objectives = listOf(
                Objective(
                    id = "obj_001",
                    description = "Defeat 5 goblins",
                    targetCount = 5,
                    currentCount = 5
                )
            ),
            rewards = Rewards(experience = 200)
        )
        
        assertTrue(quest.isComplete())
        assertEquals(100, quest.getProgress())
    }
    
    @Test
    fun testQuestIncomplete() {
        val quest = Quest(
            id = "quest_003",
            title = "Gather Resources",
            description = "Collect materials for crafting.",
            objectives = listOf(
                Objective(
                    id = "obj_001",
                    description = "Collect 10 wood",
                    targetCount = 10,
                    currentCount = 7
                )
            ),
            rewards = Rewards(gold = 30)
        )
        
        assertFalse(quest.isComplete())
        assertEquals(0, quest.getProgress())
    }
    
    @Test
    fun testMultipleObjectives() {
        val quest = Quest(
            id = "quest_004",
            title = "Help the Villagers",
            description = "Complete various tasks for the village.",
            objectives = listOf(
                Objective(
                    id = "obj_001",
                    description = "Repair 3 houses",
                    targetCount = 3,
                    currentCount = 3
                ),
                Objective(
                    id = "obj_002",
                    description = "Deliver 5 food packages",
                    targetCount = 5,
                    currentCount = 3
                )
            ),
            rewards = Rewards(experience = 150, gold = 75)
        )
        
        assertFalse(quest.isComplete())
        assertEquals(50, quest.getProgress())
    }
    
    @Test
    fun testAllObjectivesComplete() {
        val quest = Quest(
            id = "quest_005",
            title = "Master Quest",
            description = "Complete all objectives.",
            objectives = listOf(
                Objective(
                    id = "obj_001",
                    description = "Task 1",
                    targetCount = 1,
                    currentCount = 1
                ),
                Objective(
                    id = "obj_002",
                    description = "Task 2",
                    targetCount = 1,
                    currentCount = 1
                ),
                Objective(
                    id = "obj_003",
                    description = "Task 3",
                    targetCount = 1,
                    currentCount = 1
                )
            ),
            rewards = Rewards(experience = 500, gold = 200, items = listOf("rare_sword_001"))
        )
        
        assertTrue(quest.isComplete())
        assertEquals(100, quest.getProgress())
        assertEquals(1, quest.rewards.items.size)
    }
    
    @Test
    fun testEmptyObjectives() {
        val quest = Quest(
            id = "quest_006",
            title = "Tutorial Quest",
            description = "Welcome to the game!",
            objectives = emptyList(),
            rewards = Rewards(experience = 10)
        )
        
        assertTrue(quest.isComplete())
        assertEquals(0, quest.getProgress())
    }
    
    @Test
    fun testQuestStatusTransitions() {
        val notStarted = Quest(
            id = "quest_007",
            title = "New Adventure",
            description = "Begin your journey.",
            objectives = listOf(
                Objective(id = "obj_001", description = "Start", targetCount = 1)
            ),
            rewards = Rewards(experience = 50),
            status = QuestStatus.NOT_STARTED
        )
        
        val inProgress = notStarted.copy(status = QuestStatus.IN_PROGRESS)
        val completed = inProgress.copy(status = QuestStatus.COMPLETED)
        
        assertEquals(QuestStatus.NOT_STARTED, notStarted.status)
        assertEquals(QuestStatus.IN_PROGRESS, inProgress.status)
        assertEquals(QuestStatus.COMPLETED, completed.status)
    }
}

class ObjectiveTest {
    
    @Test
    fun testObjectiveCreation() {
        val objective = Objective(
            id = "obj_001",
            description = "Collect 10 apples",
            targetCount = 10,
            currentCount = 0
        )
        
        assertEquals("obj_001", objective.id)
        assertEquals("Collect 10 apples", objective.description)
        assertEquals(10, objective.targetCount)
        assertEquals(0, objective.currentCount)
        assertFalse(objective.isComplete())
    }
    
    @Test
    fun testObjectiveProgress() {
        val objective = Objective(
            id = "obj_001",
            description = "Defeat enemies",
            targetCount = 5,
            currentCount = 2
        )
        
        val updated = objective.progress(2)
        
        assertEquals(4, updated.currentCount)
        assertFalse(updated.isComplete())
    }
    
    @Test
    fun testObjectiveCompletion() {
        val objective = Objective(
            id = "obj_001",
            description = "Talk to NPC",
            targetCount = 1,
            currentCount = 0
        )
        
        val completed = objective.progress(1)
        
        assertTrue(completed.isComplete())
        assertEquals(1, completed.currentCount)
    }
    
    @Test
    fun testObjectiveProgressCap() {
        val objective = Objective(
            id = "obj_001",
            description = "Collect items",
            targetCount = 5,
            currentCount = 4
        )
        
        val overcapped = objective.progress(10)
        
        assertEquals(5, overcapped.currentCount)
        assertTrue(overcapped.isComplete())
    }
    
    @Test
    fun testObjectiveDefaultCount() {
        val objective = Objective(
            id = "obj_001",
            description = "Simple task",
            targetCount = 1
        )
        
        assertEquals(0, objective.currentCount)
        assertFalse(objective.isComplete())
    }
    
    @Test
    fun testObjectiveProgressIncrement() {
        var objective = Objective(
            id = "obj_001",
            description = "Step by step",
            targetCount = 3,
            currentCount = 0
        )
        
        objective = objective.progress()
        assertEquals(1, objective.currentCount)
        
        objective = objective.progress()
        assertEquals(2, objective.currentCount)
        
        objective = objective.progress()
        assertEquals(3, objective.currentCount)
        assertTrue(objective.isComplete())
    }
}

class RewardsTest {
    
    @Test
    fun testRewardsCreation() {
        val rewards = Rewards(
            experience = 100,
            gold = 50,
            items = listOf("sword_001", "potion_001")
        )
        
        assertEquals(100, rewards.experience)
        assertEquals(50, rewards.gold)
        assertEquals(2, rewards.items.size)
    }
    
    @Test
    fun testEmptyRewards() {
        val rewards = Rewards()
        
        assertEquals(0, rewards.experience)
        assertEquals(0, rewards.gold)
        assertTrue(rewards.items.isEmpty())
    }
    
    @Test
    fun testRewardsWithOnlyExperience() {
        val rewards = Rewards(experience = 250)
        
        assertEquals(250, rewards.experience)
        assertEquals(0, rewards.gold)
        assertTrue(rewards.items.isEmpty())
    }
    
    @Test
    fun testRewardsWithOnlyGold() {
        val rewards = Rewards(gold = 100)
        
        assertEquals(0, rewards.experience)
        assertEquals(100, rewards.gold)
        assertTrue(rewards.items.isEmpty())
    }
    
    @Test
    fun testRewardsWithOnlyItems() {
        val rewards = Rewards(items = listOf("rare_gem_001"))
        
        assertEquals(0, rewards.experience)
        assertEquals(0, rewards.gold)
        assertEquals(1, rewards.items.size)
    }
}
