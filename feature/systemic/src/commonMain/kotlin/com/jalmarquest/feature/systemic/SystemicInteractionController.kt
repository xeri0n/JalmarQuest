package com.jalmarquest.feature.systemic

import com.jalmarquest.core.model.AvailabilityMessage
import com.jalmarquest.core.model.EnvironmentTag
import com.jalmarquest.core.model.InteractionId
import com.jalmarquest.core.model.ResolutionMessage
import com.jalmarquest.core.state.GameStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class SystemicInteractionController(
    private val scope: CoroutineScope,
    private val engine: SystemicInteractionEngine,
    private val gameStateManager: GameStateManager
) {
    private val mutex = Mutex()
    private val _state = MutableStateFlow(SystemicInteractionState())
    val state: StateFlow<SystemicInteractionState> = _state.asStateFlow()

    private var environmentTags: Set<EnvironmentTag> = emptySet()

    fun updateEnvironmentTags(tags: Set<String>) {
        scope.launch {
            mutex.withLock {
                environmentTags = tags.filter { it.isNotBlank() }
                    .mapTo(linkedSetOf()) { EnvironmentTag(it.trim()) }
                val newOptions = engine.evaluate(currentContext())
                _state.value = _state.value.copy(options = newOptions)
            }
        }
    }

    fun refreshOptions() {
        scope.launch {
            mutex.withLock {
                val newOptions = engine.evaluate(currentContext())
                _state.value = _state.value.copy(options = newOptions)
            }
        }
    }

    fun attemptInteraction(interactionId: String) {
        scope.launch {
            mutex.withLock {
                val id = InteractionId(interactionId)
                val context = currentContext()
                when (val result = engine.resolve(id, context)) {
                    is InteractionResolution.Success -> {
                        val applied = applyEffects(result.effects)
                        val updatedOptions = engine.evaluate(currentContext())
                        val event = if (applied) {
                            InteractionEvent(
                                interactionId = result.interactionId,
                                success = true,
                                message = result.message
                            )
                        } else {
                            InteractionEvent(
                                interactionId = result.interactionId,
                                success = false,
                                message = ResolutionMessage("The moment slips away before Jalmar can act."),
                                reasons = listOf(AvailabilityMessage("Resources shifted during preparation."))
                            )
                        }
                        _state.value = SystemicInteractionState(
                            options = updatedOptions,
                            lastEvent = event
                        )
                    }
                    is InteractionResolution.Failure -> {
                        _state.value = _state.value.copy(
                            lastEvent = InteractionEvent(
                                interactionId = result.interactionId,
                                success = false,
                                message = result.message,
                                reasons = result.reasons
                            )
                        )
                    }
                }
            }
        }
    }

    private fun currentContext(): InteractionContext {
        val player = gameStateManager.playerState.value
        return InteractionContext.fromPlayer(player, environmentTags)
    }

    private fun applyEffects(effects: InteractionEffects): Boolean {
        for (requirement in effects.consumeItems) {
            val consumed = gameStateManager.consumeItem(requirement.itemId.value, requirement.quantity)
            if (!consumed) {
                return false
            }
        }
        for (stack in effects.grantItems) {
            gameStateManager.grantItem(stack.id.value, stack.quantity)
        }
        for (status in effects.grantStatuses) {
            val duration: Duration? = status.durationMinutes?.minutes
            gameStateManager.applyStatusEffect(status.statusKey.value, duration)
        }
        for (status in effects.clearStatuses) {
            gameStateManager.clearStatus(status.value)
        }
        for (tag in effects.appendChoiceTags) {
            gameStateManager.appendChoice(tag.value)
        }
        return true
    }
}

data class SystemicInteractionState(
    val options: List<ContextualOption> = emptyList(),
    val lastEvent: InteractionEvent? = null
)

data class InteractionEvent(
    val interactionId: InteractionId,
    val success: Boolean,
    val message: ResolutionMessage,
    val reasons: List<AvailabilityMessage> = emptyList()
)
