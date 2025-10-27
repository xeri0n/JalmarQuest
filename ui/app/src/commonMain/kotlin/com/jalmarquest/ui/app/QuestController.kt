package com.jalmarquest.ui.app

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.quests.QuestCatalog
import com.jalmarquest.core.state.quests.QuestManager
import kotlinx.coroutines.flow.*

/**
 * Quest tab types for organizing quests
 */
enum class QuestTab {
    ACTIVE,      // Currently in progress
    AVAILABLE,   // Can be accepted
    COMPLETED    // Finished quests
}

/**
 * UI state for the Quest Log
 */
data class QuestViewState(
    val activeQuests: List<QuestWithDetails> = emptyList(),
    val availableQuests: List<QuestWithDetails> = emptyList(),
    val completedQuests: List<QuestWithDetails> = emptyList(),
    val selectedQuest: QuestWithDetails? = null,
    val errorMessage: String? = null
)

/**
 * Wrapper combining Quest metadata with player progress
 */
data class QuestWithDetails(
    val quest: Quest,
    val progress: QuestProgress?,
    val progressPercentage: Int = 0,
    val canComplete: Boolean = false
)

/**
 * Controller for Quest UI - manages quest state and actions
 */
class QuestController(
    private val questManager: QuestManager,
    private val questCatalog: QuestCatalog,
    private val gameStateManager: GameStateManager
) {
    private val _viewState = MutableStateFlow(QuestViewState())
    val viewState: StateFlow<QuestViewState> = _viewState.asStateFlow()

    init {
        // Initial state build
        refresh()
    }

    /**
     * Refresh the view state from current quest log and player state
     */
    fun refresh() {
        val questLog = questManager.questLog.value
        val player = gameStateManager.playerState.value
        _viewState.value = buildViewState(questLog, player)
    }

    /**
     * Build view state from quest log and player state
     */
    private fun buildViewState(questLog: QuestLog, player: Player): QuestViewState {
        // Active quests with progress
        val activeQuests = questLog.activeQuests.mapNotNull { progress ->
            val quest = questCatalog.getQuestById(progress.questId) ?: return@mapNotNull null
            val canComplete = progress.canTurnIn()
            val progressPct = calculateProgressPercentage(progress)
            QuestWithDetails(
                quest = quest,
                progress = progress,
                progressPercentage = progressPct,
                canComplete = canComplete
            )
        }

        // Available quests (not active, not completed, requirements met)
        val allQuests = questCatalog.getAllQuests()
        val availableQuests = allQuests.filter { quest ->
            !questLog.isQuestActive(quest.questId) &&
            !questLog.isQuestCompleted(quest.questId) &&
            questManager.isQuestAvailable(quest, player)
        }.map { quest ->
            QuestWithDetails(
                quest = quest,
                progress = null,
                progressPercentage = 0,
                canComplete = false
            )
        }

        // Completed quests
        val completedQuests = questLog.completedQuests.mapNotNull { questId ->
            val quest = questCatalog.getQuestById(questId) ?: return@mapNotNull null
            QuestWithDetails(
                quest = quest,
                progress = null,
                progressPercentage = 100,
                canComplete = false
            )
        }

        return QuestViewState(
            activeQuests = activeQuests,
            availableQuests = availableQuests,
            completedQuests = completedQuests,
            selectedQuest = _viewState.value.selectedQuest,
            errorMessage = null
        )
    }

    /**
     * Calculate quest completion percentage
     */
    private fun calculateProgressPercentage(progress: QuestProgress): Int {
        if (progress.objectives.isEmpty()) return 0
        val completedCount = progress.objectives.count { it.isComplete() }
        return (completedCount * 100) / progress.objectives.size
    }

    /**
     * Select a quest to show details
     */
    fun selectQuest(questId: QuestId) {
        val current = _viewState.value
        val questDetails = (current.activeQuests + current.availableQuests + current.completedQuests)
            .find { it.quest.questId == questId }
        
        _viewState.value = current.copy(selectedQuest = questDetails)
    }

    /**
     * Clear quest selection
     */
    fun clearSelection() {
        _viewState.value = _viewState.value.copy(selectedQuest = null, errorMessage = null)
    }

    /**
     * Accept a quest (async operation)
     */
    suspend fun acceptQuest(questId: QuestId) {
        val player = gameStateManager.playerState.value
        val success = questManager.acceptQuest(questId, player)
        
        if (success) {
            refresh()
            clearSelection()
        } else {
            _viewState.value = _viewState.value.copy(
                errorMessage = "Cannot accept quest - requirements not met"
            )
        }
    }

    /**
     * Complete a quest and apply rewards
     */
    suspend fun completeQuest(questId: QuestId) {
        val rewards = questManager.completeQuest(questId)
        
        if (rewards != null) {
            // Apply rewards to player state
            applyQuestRewards(rewards)
            refresh()
            clearSelection()
        } else {
            _viewState.value = _viewState.value.copy(
                errorMessage = "Cannot complete quest - objectives not finished"
            )
        }
    }

    /**
     * Abandon an active quest
     */
    suspend fun abandonQuest(questId: QuestId) {
        val success = questManager.abandonQuest(questId)
        if (success) {
            refresh()
            clearSelection()
        }
    }

    /**
     * Apply quest rewards to player state
     * TODO: Implement all reward types once APIs are available
     */
    private fun applyQuestRewards(rewards: List<QuestReward>) {
        for (reward in rewards) {
            when (reward.type) {
                QuestRewardType.ITEMS -> {
                    // Grant items to inventory
                    val itemId = reward.targetId ?: continue
                    gameStateManager.grantItem(itemId, reward.quantity)
                }
                
                QuestRewardType.EXPERIENCE -> {
                    // TODO: Add XP via ArchetypeManager when API is available
                    // For now, just log it
                    println("Quest reward: ${reward.quantity} XP (not yet implemented)")
                }
                
                QuestRewardType.SEEDS -> {
                    // TODO: Add seeds via HoardRankManager when API is available
                    println("Quest reward: ${reward.quantity} Seeds (not yet implemented)")
                }
                
                QuestRewardType.RECIPE -> {
                    // TODO: Add recipe via RecipeBook when API is available
                    val recipeId = reward.targetId ?: continue
                    println("Quest reward: Recipe $recipeId (not yet implemented)")
                }
                
                QuestRewardType.THOUGHT -> {
                    // TODO: Add thought via ThoughtCabinetManager when API is available
                    val thoughtId = reward.targetId ?: continue
                    println("Quest reward: Thought $thoughtId (not yet implemented)")
                }
                
                QuestRewardType.SKILL_POINTS,
                QuestRewardType.ARCHETYPE_TALENT_POINT -> {
                    // TODO: Add skill points via ArchetypeManager when API is available
                    println("Quest reward: ${reward.quantity} talent points (not yet implemented)")
                }
                
                QuestRewardType.SHINY -> {
                    // TODO: Add shiny via ShinyCollection when API is available
                    val shinyId = reward.targetId ?: continue
                    println("Quest reward: Shiny $shinyId (not yet implemented)")
                }
                
                QuestRewardType.FACTION_REPUTATION -> {
                    // TODO: Implement faction reputation system
                    println("Quest reward: Faction reputation (not yet implemented)")
                }
                
                QuestRewardType.LORE_UNLOCK -> {
                    // TODO: Implement lore unlock system
                    println("Quest reward: Lore unlock (not yet implemented)")
                }
                
                QuestRewardType.COMPANION_AFFINITY -> {
                    // TODO: Implement companion affinity system
                    println("Quest reward: Companion affinity (not yet implemented)")
                }
                
                QuestRewardType.ABILITY,
                QuestRewardType.COSMETIC -> {
                    // TODO: Implement ability and cosmetic reward types
                    println("Quest reward: ${reward.type} (not yet implemented)")
                }
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _viewState.value = _viewState.value.copy(errorMessage = null)
    }
}
