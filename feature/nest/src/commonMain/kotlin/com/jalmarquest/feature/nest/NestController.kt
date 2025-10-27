package com.jalmarquest.feature.nest

import com.jalmarquest.core.model.CritterRole
import com.jalmarquest.core.model.NestState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NestController(
    private val scope: CoroutineScope,
    private val stateMachine: NestStateMachine,
    private val passiveTickIntervalMillis: Long = DEFAULT_PASSIVE_TICK_INTERVAL
) {
    val state: StateFlow<NestState> = stateMachine.state

    private var passiveJob: Job? = null

    fun start() {
        if (passiveJob?.isActive == true) return
        passiveJob = scope.launch {
            stateMachine.refreshRecruitment()
            while (isActive) {
                delay(passiveTickIntervalMillis)
                stateMachine.tick()
            }
        }
    }

    fun stop() {
        passiveJob?.cancel()
        passiveJob = null
    }

    fun manualTick() {
        scope.launch { stateMachine.tick() }
    }

    fun refreshRecruitment() {
        scope.launch { stateMachine.refreshRecruitment() }
    }

    fun recruit(offerId: String, role: CritterRole) {
        scope.launch { stateMachine.acceptRecruitment(offerId, role) }
    }

    fun unassign(slotId: String) {
        scope.launch { stateMachine.unassign(slotId) }
    }

    fun requestUpgrade() {
        scope.launch { stateMachine.requestUpgrade() }
    }

    companion object {
        private const val DEFAULT_PASSIVE_TICK_INTERVAL = 5 * 60 * 1000L
    }
}
