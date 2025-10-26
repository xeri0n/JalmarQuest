package com.jalmarquest.core.model

import kotlin.math.roundToInt

data class Quest(
    val id: String,
    val title: String,
    val description: String,
    val objectives: List<Objective>,
    val rewards: Rewards,
    val status: QuestStatus = QuestStatus.NOT_STARTED
) {
    fun isComplete(): Boolean = objectives.all { it.isComplete() }
    
    fun getProgress(): Int {
        if (objectives.isEmpty()) return 0
        val totalProgress = objectives.sumOf { obj ->
            if (obj.targetCount == 0) 0.0 else (obj.currentCount.toDouble() / obj.targetCount) * 100
        }
        return (totalProgress / objectives.size).roundToInt()
    }
}

data class Objective(
    val id: String,
    val description: String,
    val targetCount: Int = 1,
    val currentCount: Int = 0
) {
    fun isComplete(): Boolean = currentCount >= targetCount
    
    fun progress(amount: Int = 1): Objective {
        return copy(currentCount = (currentCount + amount).coerceAtMost(targetCount))
    }
}

data class Rewards(
    val experience: Int = 0,
    val gold: Int = 0,
    val items: List<String> = emptyList() // Item IDs
)

enum class QuestStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
