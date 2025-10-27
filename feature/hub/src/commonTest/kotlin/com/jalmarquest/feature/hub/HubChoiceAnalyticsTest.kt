package com.jalmarquest.feature.hub

import com.jalmarquest.core.model.Player
import com.jalmarquest.core.state.GameStateManager
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Integration test validating that HubStateMachine correctly logs choice tags
 * for location and action selections.
 */
class HubChoiceAnalyticsTest {
    @Test
    fun selectLocation_appendsChoiceTag() = runTest {
        val player = Player(id = "test", name = "Test Player")
        val gameStateManager = GameStateManager(
            initialPlayer = player,
            timestampProvider = { System.currentTimeMillis() }
        )
        val stateMachine = HubStateMachine(gameStateManager)

        stateMachine.selectLocation(HubLocationId("pack_rat_hoard"))

        val choiceLog = gameStateManager.playerState.value.choiceLog.entries
        assertTrue(choiceLog.any { it.tag.value.contains("hub_location_pack_rat_hoard") })
    }

    @Test
    fun selectAction_appendsChoiceTag() = runTest {
        val player = Player(id = "test", name = "Test Player")
        val gameStateManager = GameStateManager(
            initialPlayer = player,
            timestampProvider = { System.currentTimeMillis() }
        )
        val stateMachine = HubStateMachine(gameStateManager)

        stateMachine.selectLocation(HubLocationId("quailsmith"))
        val location = stateMachine.state.value.selectedLocation()!!
        val craftAction = location.actionOrder.first { it.type == HubActionType.CRAFT }
        
        stateMachine.selectAction(craftAction.id)

        val choiceLog = gameStateManager.playerState.value.choiceLog.entries
        assertTrue(choiceLog.any { it.tag.value.contains("hub_action_") })
    }

    @Test
    fun multipleNavigations_accumulateChoiceTags() = runTest {
        val player = Player(id = "test", name = "Test Player")
        val gameStateManager = GameStateManager(
            initialPlayer = player,
            timestampProvider = { System.currentTimeMillis() }
        )
        val stateMachine = HubStateMachine(gameStateManager)

        stateMachine.selectLocation(HubLocationId("pack_rat_hoard"))
        stateMachine.returnToOverview()
        stateMachine.selectLocation(HubLocationId("quailsmith"))

        val choiceLog = gameStateManager.playerState.value.choiceLog.entries
        assertTrue(choiceLog.any { it.tag.value.contains("hub_location_pack_rat_hoard") })
        assertTrue(choiceLog.any { it.tag.value.contains("hub_location_quailsmith") })
    }
}
