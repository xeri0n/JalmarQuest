package com.jalmarquest.feature.hub

import com.jalmarquest.core.state.GameStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HubStateMachine(
    private val gameStateManager: GameStateManager,
    initialLocations: List<HubLocation> = defaultHubLocations()
) {
    private val _state = MutableStateFlow(HubState(locations = initialLocations))
    val state: StateFlow<HubState> = _state.asStateFlow()

    fun selectLocation(locationId: HubLocationId) {
        val location = _state.value.locations.firstOrNull { it.id == locationId } ?: return
        _state.update { current ->
            if (current.selectedLocationId == locationId) current
            else {
                gameStateManager.appendChoice("hub_location_${locationId.value}")
                current.copy(selectedLocationId = locationId, activeAction = null)
            }
        }
    }

    fun selectAction(actionId: HubActionId) {
        val current = _state.value
        val location = current.selectedLocation() ?: return
        val action = location.actionOrder.firstOrNull { it.id == actionId } ?: return
        if (current.activeAction?.id == actionId) return
        _state.update {
            gameStateManager.appendChoice(
                "hub_action_${location.id.value}_${action.id.value}"
            )
            it.copy(activeAction = action)
        }
    }

    fun closeAction() {
        _state.update { it.copy(activeAction = null) }
    }

    fun returnToOverview() {
        _state.update { it.copy(selectedLocationId = null, activeAction = null) }
    }
}
