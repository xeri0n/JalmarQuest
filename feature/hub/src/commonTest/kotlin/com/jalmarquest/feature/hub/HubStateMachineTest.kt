package com.jalmarquest.feature.hub

import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.Inventory
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.model.QuestLog
import com.jalmarquest.core.model.StatusEffects
import com.jalmarquest.core.state.GameStateManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private fun testPlayer(): Player = Player(
    id = "test",
    name = "Jalmar",
    choiceLog = ChoiceLog(emptyList()),
    questLog = QuestLog(),
    statusEffects = StatusEffects(emptyList()),
    inventory = Inventory(emptyList())
)

class HubStateMachineTest {

    private fun stateMachine(): Pair<HubStateMachine, GameStateManager> {
        val manager = GameStateManager(initialPlayer = testPlayer()) { 0L }
        val machine = HubStateMachine(gameStateManager = manager)
        return machine to manager
    }

    @Test
    fun defaultLocationsAvailable() {
        val (machine, _) = stateMachine()
        val locations = machine.state.value.locations
        assertTrue(locations.isNotEmpty(), "Expected hub locations to be pre-populated")
        assertEquals(HubLocationId("pack_rat_hoard"), locations.first().id)
    }

    @Test
    fun selectingLocationUpdatesStateAndLogsChoice() {
        val (machine, manager) = stateMachine()
        val targetId = HubLocationId("pack_rat_hoard")

        machine.selectLocation(targetId)

        assertEquals(targetId, machine.state.value.selectedLocationId)
        val appended = manager.playerState.value.choiceLog.entries
        assertEquals(1, appended.size)
        assertEquals("hub_location_${targetId.value}", appended.first().tag.value)
    }

    @Test
    fun selectingActionActivatesActionAndLogs() {
        val (machine, manager) = stateMachine()
        val location = machine.state.value.locations.first()
        val action = location.actionOrder.first()

        machine.selectLocation(location.id)
        machine.selectAction(action.id)

        val state = machine.state.value
        assertNotNull(state.activeAction)
        assertEquals(action.id, state.activeAction?.id)
        val tags = manager.playerState.value.choiceLog.entries.map { it.tag.value }
        assertTrue(tags.contains("hub_action_${location.id.value}_${action.id.value}"))
    }
}
