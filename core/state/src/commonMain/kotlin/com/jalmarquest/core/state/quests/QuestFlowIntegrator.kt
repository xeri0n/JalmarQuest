package com.jalmarquest.core.state.quests

import com.jalmarquest.core.model.Player
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.ai.NpcReactionManager
import com.jalmarquest.core.state.ai.WorldEventType
import com.jalmarquest.core.state.player.PlayerLocationTracker
import com.jalmarquest.core.state.time.InGameTimeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * QuestFlowIntegrator - Connects Phase 2 quest systems with Phase 3/4 world systems
 * 
 * Integration Points:
 * 1. PlayerLocationTracker → QuestTriggerManager (location-based quest activation)
 * 2. Quest completion → NpcReactionManager (NPCs react to quest outcomes)
 * 3. Quest state changes → DynamicDialogueManager (quest-aware dialogue)
 * 
 * Automatically handles:
 * - Location discovery quest triggers
 * - Quest completion NPC reactions
 * - Quest progress tracking for dialogue context
 */
class QuestFlowIntegrator(
    private val scope: CoroutineScope,
    private val locationTracker: PlayerLocationTracker,
    private val questTriggerManager: QuestTriggerManager,
    private val questManager: QuestManager,
    private val npcReactionManager: NpcReactionManager,
    private val gameStateManager: GameStateManager,
    private val timeManager: InGameTimeManager
) {
    private val activatedLocationTriggers = mutableSetOf<String>()
    private val availableQuests = mutableSetOf<String>()
    
    init {
        observeLocationChanges()
    }
    
    /**
     * Monitor player location changes and check for quest triggers
     */
    private fun observeLocationChanges() {
        locationTracker.currentLocation
            .onEach { playerLocation ->
                if (playerLocation != null) {
                    checkLocationTriggers(playerLocation.locationId)
                }
            }
            .launchIn(scope)
    }
    
    /**
     * Check for location-based quest triggers when player enters a new location
     */
    private fun checkLocationTriggers(locationId: String) {
        val triggers = questTriggerManager.getTriggersForLocation(locationId)
        val player = gameStateManager.playerState.value
        
        triggers.forEach { trigger ->
            // Skip if already activated this session (prevent spam)
            if (activatedLocationTriggers.contains(trigger.questId)) {
                return@forEach
            }
            
            // Check if quest is already completed  
            if (player.questLog.completedQuests.any { it.value == trigger.questId }) {
                return@forEach
            }
            
            // Check if quest is already active
            if (player.questLog.activeQuests.any { it.questId.value == trigger.questId }) {
                return@forEach
            }
            
            // Check if trigger requirements are met
            if (isTriggerAvailable(trigger, player)) {
                activateTrigger(trigger, locationId)
            }
        }
    }
    
    /**
     * Check if trigger requirements are satisfied
     */
    private fun isTriggerAvailable(trigger: QuestTrigger, player: Player): Boolean {
        val completedQuests = player.questLog.completedQuests.map { it.value }.toSet()
        val playerItems = emptyMap<String, Int>() // TODO: Get from inventory system
        val npcAffinities = emptyMap<String, Int>() // TODO: Get from relationship system
        val choiceTags = emptySet<String>() // TODO: Get from choice tracking system
        val currentTime = timeManager.currentTime.value.getTimeOfDay()
        
        return questTriggerManager.isTriggerAvailable(
            trigger = trigger,
            playerLevel = player.skillTree.totalSkillPoints,
            completedQuests = completedQuests,
            playerItems = playerItems,
            npcAffinities = npcAffinities,
            choiceTags = choiceTags,
            currentTime = currentTime
        )
    }
    
    /**
     * Activate a quest trigger
     */
    private fun activateTrigger(trigger: QuestTrigger, locationId: String) {
        activatedLocationTriggers.add(trigger.questId)
        availableQuests.add(trigger.questId)
        
        if (trigger.autoStart) {
            // Quest is available and should auto-start
            // The game loop would handle actually starting it
            logQuestActivation(trigger, locationId, autoStarted = true)
        } else {
            // Make quest available but don't start it
            // This would typically show a notification or marker
            logQuestActivation(trigger, locationId, autoStarted = false)
        }
    }
    
    /**
     * Get quests available at current location
     */
    fun getAvailableQuestsAtLocation(locationId: String): List<String> {
        val triggers = questTriggerManager.getTriggersForLocation(locationId)
        val player = gameStateManager.playerState.value
        
        return triggers
            .filter { !player.questLog.completedQuests.any { cq -> cq.value == it.questId } }
            .filter { !player.questLog.activeQuests.any { aq -> aq.questId.value == it.questId } }
            .filter { isTriggerAvailable(it, player) }
            .map { it.questId }
    }
    
    /**
     * Handle quest completion and trigger NPC reactions
     */
    fun onQuestCompleted(questId: String, choices: List<String> = emptyList()) {
        scope.launch {
            // Find NPCs related to this quest
            val relatedNpcs = findRelatedNpcs(questId)
            
            relatedNpcs.forEach { npcId ->
                // Trigger positive reaction from quest giver
                npcReactionManager.recordEvent(
                    type = WorldEventType.PLAYER_QUEST_COMPLETE,
                    targetNpcId = npcId,
                    questId = questId,
                    metadata = mapOf("context" to "Completed quest: $questId")
                )
            }
            
            logQuestCompletion(questId, relatedNpcs)
        }
    }
    
    /**
     * Handle quest failure
     */
    fun onQuestFailed(questId: String) {
        scope.launch {
            val relatedNpcs = findRelatedNpcs(questId)
            
            relatedNpcs.forEach { npcId ->
                npcReactionManager.recordEvent(
                    type = WorldEventType.PLAYER_QUEST_FAILED,
                    targetNpcId = npcId,
                    questId = questId,
                    metadata = mapOf("context" to "Failed quest: $questId")
                )
            }
            
            logQuestFailure(questId, relatedNpcs)
        }
    }
    
    /**
     * Handle quest progress updates
     */
    fun onQuestProgress(questId: String, objectiveId: String, progress: Int, total: Int) {
        scope.launch {
            // Could trigger dialogue updates or NPC comments
            val percentage = (progress.toFloat() / total * 100).toInt()
            
            if (percentage >= 50 && percentage < 75) {
                // Halfway point - NPCs might encourage
                val relatedNpcs = findRelatedNpcs(questId)
                relatedNpcs.forEach { npcId ->
                    npcReactionManager.recordEvent(
                        type = WorldEventType.PLAYER_QUEST_COMPLETE,
                        targetNpcId = npcId,
                        questId = questId,
                        metadata = mapOf("context" to "Making progress on quest: $questId", "progress" to "$progress/$total")
                    )
                }
            }
            
            logQuestProgress(questId, objectiveId, progress, total)
        }
    }
    
    /**
     * Find NPCs related to a quest (quest givers, involved NPCs)
     */
    private fun findRelatedNpcs(questId: String): List<String> {
        val relatedNpcs = mutableListOf<String>()
        
        // Find NPC triggers for this quest
        questTriggerManager.getAllTriggers()
            .filter { it.questId == questId && it.triggerType == QuestTriggerType.NPC_DIALOGUE }
            .forEach { trigger ->
                relatedNpcs.add(trigger.triggerId)
            }
        
        return relatedNpcs.distinct()
    }
    
    /**
     * Check for NPC dialogue triggers when talking to an NPC
     */
    fun checkNpcDialogueTriggers(npcId: String): List<String> {
        val triggers = questTriggerManager.getTriggersForNpc(npcId)
        val player = gameStateManager.playerState.value
        
        return triggers
            .filter { !player.questLog.completedQuests.any { cq -> cq.value == it.questId } }
            .filter { trigger ->
                !player.questLog.activeQuests.any { aq -> aq.questId.value == trigger.questId }
            }
            .filter { isTriggerAvailable(it, player) }
            .map { it.questId }
            .also { availableQuests ->
                if (availableQuests.isNotEmpty()) {
                    logNpcQuestAvailable(npcId, availableQuests)
                }
            }
    }
    
    /**
     * Clear activated triggers (e.g., on location leave or game reset)
     */
    fun clearActivatedTriggers() {
        activatedLocationTriggers.clear()
        availableQuests.clear()
    }
    
    // Logging methods (replace with actual logging system)
    private fun logQuestActivation(trigger: QuestTrigger, locationId: String, autoStarted: Boolean) {
        println("[QuestFlowIntegrator] Quest '${trigger.questId}' ${if (autoStarted) "auto-started" else "available"} at location '$locationId'")
    }
    
    private fun logQuestCompletion(questId: String, relatedNpcs: List<String>) {
        println("[QuestFlowIntegrator] Quest '$questId' completed. Notified NPCs: ${relatedNpcs.joinToString()}")
    }
    
    private fun logQuestFailure(questId: String, relatedNpcs: List<String>) {
        println("[QuestFlowIntegrator] Quest '$questId' failed. Notified NPCs: ${relatedNpcs.joinToString()}")
    }
    
    private fun logQuestProgress(questId: String, objectiveId: String, progress: Int, total: Int) {
        println("[QuestFlowIntegrator] Quest '$questId' objective '$objectiveId': $progress/$total")
    }
    
    private fun logNpcQuestAvailable(npcId: String, questIds: List<String>) {
        println("[QuestFlowIntegrator] Quests ${questIds.joinToString()} available from NPC '$npcId'")
    }
}
