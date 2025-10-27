package com.jalmarquest.feature.activities

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ActivitiesController(
    private val scope: CoroutineScope,
    private val stateMachine: ActivityStateMachine
) {
    val state = stateMachine.state

    fun selectActivity(activityId: ActivityId) {
        scope.launch { stateMachine.selectActivity(activityId) }
    }

    fun clearSelection() {
        scope.launch { stateMachine.clearSelection() }
    }

    fun attemptActivity(activityId: ActivityId) {
        scope.launch { stateMachine.attemptActivity(activityId) }
    }

    fun clearResolution() {
        scope.launch { stateMachine.clearResolution() }
    }
}
