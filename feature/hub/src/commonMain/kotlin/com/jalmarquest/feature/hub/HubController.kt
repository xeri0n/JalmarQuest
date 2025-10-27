package com.jalmarquest.feature.hub

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class HubController(
    private val scope: CoroutineScope,
    private val stateMachine: HubStateMachine
) {
    val state = stateMachine.state

    fun selectLocation(locationId: HubLocationId) {
        scope.launch { stateMachine.selectLocation(locationId) }
    }

    fun selectAction(actionId: HubActionId) {
        scope.launch { stateMachine.selectAction(actionId) }
    }

    fun closeAction() {
        scope.launch { stateMachine.closeAction() }
    }

    fun returnToOverview() {
        scope.launch { stateMachine.returnToOverview() }
    }
}
