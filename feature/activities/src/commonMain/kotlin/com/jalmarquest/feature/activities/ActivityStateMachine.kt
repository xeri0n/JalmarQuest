package com.jalmarquest.feature.activities

import com.jalmarquest.core.state.GameStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class ActivityStateMachine(
    private val gameStateManager: GameStateManager,
    initialActivities: List<SecondaryActivity> = defaultSecondaryActivities()
) {
    private val _state = MutableStateFlow(ActivityState(activities = initialActivities))
    val state: StateFlow<ActivityState> = _state.asStateFlow()

    fun selectActivity(activityId: ActivityId) {
        val exists = _state.value.activities.any { it.id == activityId }
        if (!exists) return
        _state.update { current ->
            if (current.selectedActivityId == activityId) current
            else current.copy(selectedActivityId = activityId)
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedActivityId = null) }
    }

    fun attemptActivity(activityId: ActivityId): ActivityResolution? {
        val activity = _state.value.activities.firstOrNull { it.id == activityId } ?: return null
        val reward = activity.reward
        gameStateManager.appendChoice("secondary_activity_${activityId.value}")
        reward.items.forEach { stack ->
            gameStateManager.grantItem(stack.id.value, stack.quantity)
        }
        val statusKey = reward.statusEffectKey
        if (statusKey != null) {
            gameStateManager.applyStatusEffect(statusKey, reward.statusEffectDurationMillis.asDurationOrNull())
        }
        val resolution = ActivityResolution(
            activityId = activity.id,
            type = activity.type,
            success = true,
            awardedItems = reward.items,
            appliedStatusEffect = statusKey
        )
        _state.update {
            it.copy(
                selectedActivityId = activityId,
                lastResolution = resolution
            )
        }
        return resolution
    }

    fun clearResolution() {
        _state.update { it.copy(lastResolution = null) }
    }

    private fun Long?.asDurationOrNull(): Duration? = this?.milliseconds
}
