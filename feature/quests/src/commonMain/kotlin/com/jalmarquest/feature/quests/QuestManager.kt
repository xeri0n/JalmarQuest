class QuestManager(
    private val gameStateManager: GameStateManager,
    private val questCatalog: QuestCatalog
) {
    // FIX: Ensure quest state transitions are bulletproof
    suspend fun updateQuestProgress(
        questId: String,
        objectiveId: String,
        progress: Int
    ) {
        gameStateManager.batchUpdate { player ->
            val questLog = player.questLog
            val quest = questLog.activeQuests.find { it.questId == questId }
                ?: return@batchUpdate player // Quest not active
            
            val updatedObjectives = quest.objectives.map { obj ->
                if (obj.id == objectiveId) {
                    obj.copy(
                        currentProgress = minOf(progress, obj.requiredProgress),
                        isComplete = progress >= obj.requiredProgress
                    )
                } else obj
            }
            
            val updatedQuest = quest.copy(
                objectives = updatedObjectives,
                status = if (updatedObjectives.all { it.isComplete }) {
                    QuestStatus.READY_TO_COMPLETE
                } else {
                    QuestStatus.IN_PROGRESS
                }
            )
            
            // FIX: Trigger auto-complete for certain quests
            val finalQuest = if (updatedQuest.status == QuestStatus.READY_TO_COMPLETE &&
                questCatalog.getQuest(questId)?.autoComplete == true) {
                completeQuestInternal(updatedQuest, player)
            } else {
                updatedQuest
            }
            
            player.copy(
                questLog = questLog.copy(
                    activeQuests = questLog.activeQuests.map {
                        if (it.questId == questId) finalQuest else it
                    }
                )
            )
        }
    }
    
    private fun completeQuestInternal(quest: QuestProgress, player: Player): QuestProgress {
        // Award rewards
        val questData = questCatalog.getQuest(quest.questId) ?: return quest
        
        var updatedPlayer = player
        questData.rewards.forEach { reward ->
            when (reward) {
                is Reward.Experience -> {
                    updatedPlayer = updatedPlayer.copy(
                        experience = updatedPlayer.experience + reward.amount
                    )
                }
                is Reward.Item -> {
                    updatedPlayer = updatedPlayer.copy(
                        inventory = updatedPlayer.inventory.addItem(reward.item)
                    )
                }
                is Reward.Recipe -> {
                    updatedPlayer = updatedPlayer.copy(
                        knownRecipes = updatedPlayer.knownRecipes + reward.recipeId
                    )
                }
                // ...handle other reward types...
            }
        }
        
        return quest.copy(
            status = QuestStatus.COMPLETED,
            completedAt = currentTimeMillis()
        )
    }
}