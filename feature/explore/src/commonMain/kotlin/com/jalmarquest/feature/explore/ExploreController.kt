package com.jalmarquest.feature.explore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ExploreController(
    private val scope: CoroutineScope,
    private val stateMachine: ExploreStateMachine
) {
    val state = stateMachine.state

    fun beginExploration() {
        scope.launch { stateMachine.beginExploration() }
    }

    fun chooseOption(optionIndex: Int) {
        scope.launch { stateMachine.chooseOption(optionIndex) }
    }

    fun continueAfterResolution() {
        scope.launch { stateMachine.continueAfterResolution() }
    }
    
    fun rest() {
        scope.launch { stateMachine.rest() }
    }
}
