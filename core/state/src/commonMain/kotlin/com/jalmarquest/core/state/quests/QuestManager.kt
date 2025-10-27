package com.jalmarquest.core.state.quests

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.perf.PerformanceLogger
import com.jalmarquest.core.state.perf.currentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manages quest lifecycle, progress tracking, and reward delivery.
 * Provides quest availability checking based on prerequisites, faction, level, and choice tags.
 */
class QuestManager(
    private val questCatalog: QuestCatalog,
    private val timestampProvider: () -> Long = { currentTimeMillis() }
) {
    private val mutex = Mutex()
    private val _questLog = MutableStateFlow(QuestLog())
    
    /**
     * Reactive quest log state.
     */
    val questLog: StateFlow<QuestLog> = _questLog.asStateFlow()
    
    /**
     * Load existing quest log (e.g., from saved game).
     */
    suspend fun loadQuestLog(log: QuestLog) {
        mutex.withLock {
            _questLog.value = log
            PerformanceLogger.logStateMutation("QuestManager", "loadQuestLog", mapOf(
                "activeQuests" to log.activeQuests.size,
                "completedQuests" to log.completedQuests.size
            ))
        }
    }
    
    /**
     * Check if a quest is available to accept based on player state.
     */
    fun isQuestAvailable(quest: Quest, player: Player): Boolean {
        // Already active or completed
        if (_questLog.value.isQuestActive(quest.questId)) return false
        if (_questLog.value.isQuestCompleted(quest.questId) && !quest.isRepeatable) return false
        
        // Check all requirements
        for (requirement in quest.requirements) {
            when (requirement) {
                is QuestRequirement.PrerequisiteQuest -> {
                    if (!_questLog.value.isQuestCompleted(requirement.questId)) return false
                }
                is QuestRequirement.MinimumLevel -> {
                    // Player level is archetype level for now
                    if (player.archetypeProgress.archetypeLevel < requirement.level) return false
                }
                is QuestRequirement.MinimumSkill -> {
                    val skill = player.skillTree.getSkillByType(requirement.skillType)
                    if (skill == null || skill.level < requirement.level) return false
                }
                is QuestRequirement.MinimumFactionReputation -> {
                    // TODO: Check faction reputation when faction system is implemented
                    // For now, assume requirement is met
                }
                is QuestRequirement.ArchetypeRequirement -> {
                    if (player.archetypeProgress.selectedArchetype != requirement.archetypeType) return false
                }
                is QuestRequirement.ChoiceTagRequirement -> {
                    val hasTag = player.choiceLog.entries.any { it.tag == requirement.tag }
                    if (!hasTag) return false
                }
                is QuestRequirement.NotChoiceTagRequirement -> {
                    val hasTag = player.choiceLog.entries.any { it.tag == requirement.tag }
                    if (hasTag) return false
                }
            }
        }
        
        return true
    }
    
    /**
     * Get all quests available to the player.
     */
    fun getAvailableQuests(player: Player): List<Quest> {
        return questCatalog.getAllQuests().filter { quest ->
            isQuestAvailable(quest, player)
        }
    }
    
    /**
     * Accept a quest and add it to active quests.
     * Returns true if quest was accepted, false if requirements not met.
     */
    suspend fun acceptQuest(questId: QuestId, player: Player): Boolean {
        val quest = questCatalog.getQuestById(questId) ?: return false
        
        if (!isQuestAvailable(quest, player)) {
            PerformanceLogger.logStateMutation("QuestManager", "acceptQuest_failed", mapOf(
                "questId" to questId.value,
                "reason" to "requirements_not_met"
            ))
            return false
        }
        
        mutex.withLock {
            val progress = QuestProgress(
                questId = questId,
                status = QuestStatus.ACTIVE,
                objectives = quest.objectives,
                acceptedAt = timestampProvider()
            )
            
            _questLog.value = _questLog.value.addActiveQuest(progress)
            
            PerformanceLogger.logStateMutation("QuestManager", "acceptQuest", mapOf(
                "questId" to questId.value,
                "objectiveCount" to quest.objectives.size
            ))
        }
        
        return true
    }
    
    /**
     * Update progress on a quest objective.
     * Returns true if objective was updated, false if quest not active or objective not found.
     */
    suspend fun updateObjective(questId: QuestId, objectiveId: String, amount: Int): Boolean {
        mutex.withLock {
            val activeQuest = _questLog.value.getActiveQuest(questId) ?: return false
            
            val updatedProgress = activeQuest.updateObjective(objectiveId, amount)
            _questLog.value = _questLog.value.updateActiveQuest(questId) { updatedProgress }
            
            val objective = updatedProgress.objectives.find { it.objectiveId == objectiveId }
            PerformanceLogger.logStateMutation("QuestManager", "updateObjective", mapOf(
                "questId" to questId.value,
                "objectiveId" to objectiveId,
                "progress" to (objective?.currentProgress ?: 0),
                "target" to (objective?.targetQuantity ?: 0),
                "complete" to (objective?.isComplete() ?: false)
            ))
        }
        
        return true
    }
    
    /**
     * Complete a quest and deliver rewards.
     * Returns the rewards granted, or null if quest cannot be completed.
     */
    suspend fun completeQuest(questId: QuestId): List<QuestReward>? {
        val quest = questCatalog.getQuestById(questId) ?: return null
        
        mutex.withLock {
            val activeQuest = _questLog.value.getActiveQuest(questId) ?: return null
            
            if (!activeQuest.canTurnIn()) {
                PerformanceLogger.logStateMutation("QuestManager", "completeQuest_failed", mapOf(
                    "questId" to questId.value,
                    "reason" to "objectives_incomplete"
                ))
                return null
            }
            
            _questLog.value = _questLog.value.completeQuest(questId)
            
            PerformanceLogger.logStateMutation("QuestManager", "completeQuest", mapOf(
                "questId" to questId.value,
                "rewardCount" to quest.rewards.size,
                "isRepeatable" to quest.isRepeatable
            ))
            
            return quest.rewards
        }
    }
    
    /**
     * Abandon an active quest.
     */
    suspend fun abandonQuest(questId: QuestId): Boolean {
        mutex.withLock {
            if (!_questLog.value.isQuestActive(questId)) return false
            
            _questLog.value = _questLog.value.abandonQuest(questId)
            
            PerformanceLogger.logStateMutation("QuestManager", "abandonQuest", mapOf(
                "questId" to questId.value
            ))
        }
        
        return true
    }
    
    /**
     * Fail a quest (e.g., time limit expired, incompatible choice made).
     */
    suspend fun failQuest(questId: QuestId): Boolean {
        mutex.withLock {
            if (!_questLog.value.isQuestActive(questId)) return false
            
            _questLog.value = _questLog.value.failQuest(questId)
            
            PerformanceLogger.logStateMutation("QuestManager", "failQuest", mapOf(
                "questId" to questId.value
            ))
        }
        
        return true
    }
    
    /**
     * Check for time-limited quests that have expired.
     * Should be called periodically (e.g., on game load, after exploration).
     */
    suspend fun checkQuestTimeouts() {
        val currentTime = timestampProvider()
        
        mutex.withLock {
            val expiredQuests = _questLog.value.activeQuests.filter { progress ->
                val quest = questCatalog.getQuestById(progress.questId) ?: return@filter false
                val timeLimit = quest.timeLimitMillis ?: return@filter false
                val elapsedMillis = currentTime - progress.acceptedAt
                elapsedMillis >= timeLimit
            }
            
            expiredQuests.forEach { progress ->
                _questLog.value = _questLog.value.failQuest(progress.questId)
                PerformanceLogger.logStateMutation("QuestManager", "questTimeout", mapOf(
                    "questId" to progress.questId.value
                ))
            }
        }
    }
    
    /**
     * Get quest progress for a specific quest.
     */
    fun getQuestProgress(questId: QuestId): QuestProgress? {
        return _questLog.value.getActiveQuest(questId)
    }
    
    /**
     * Get all active quest progress.
     */
    fun getActiveQuests(): List<QuestProgress> {
        return _questLog.value.activeQuests
    }
    
    /**
     * Check if player has completed a specific quest.
     */
    fun hasCompletedQuest(questId: QuestId): Boolean {
        return _questLog.value.isQuestCompleted(questId)
    }
}
