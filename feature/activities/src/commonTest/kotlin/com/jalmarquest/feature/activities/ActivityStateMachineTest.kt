package com.jalmarquest.feature.activities

import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.Inventory
import com.jalmarquest.core.model.ItemId
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.model.QuestLog
import com.jalmarquest.core.model.StatusEffects
import com.jalmarquest.core.state.GameStateManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private fun basePlayer(): Player = Player(
    id = "jalmar",
    name = "Jalmar",
    choiceLog = ChoiceLog(emptyList()),
    questLog = QuestLog(),
    statusEffects = StatusEffects(emptyList()),
    inventory = Inventory(emptyList())
)

class ActivityStateMachineTest {

    private fun stateMachine(): Pair<ActivityStateMachine, GameStateManager> {
        val manager = GameStateManager(initialPlayer = basePlayer()) { 0L }
        val machine = ActivityStateMachine(gameStateManager = manager)
        return machine to manager
    }

    @Test
    fun exposesDefaultActivities() {
        val (machine, _) = stateMachine()
        val activities = machine.state.value.activities
        assertTrue(activities.isNotEmpty(), "Expected default secondary activities")
        assertEquals(ActivityId("twilight_dungeon"), activities.first().id)
    }

    @Test
    fun attemptingActivityLogsChoiceAndAwardsItems() {
        val (machine, manager) = stateMachine()
        val activity = machine.state.value.activities.first()

        machine.attemptActivity(activity.id)

        val choiceTags = manager.playerState.value.choiceLog.entries.map { it.tag.value }
        assertTrue(choiceTags.contains("secondary_activity_${activity.id.value}"))
        val expectedItems = activity.reward.items
        expectedItems.forEach { stack ->
            val qty = manager.playerState.value.inventory.totalQuantity(stack.id)
            assertEquals(stack.quantity, qty, "Expected reward item ${stack.id.value}")
        }
    }

    @Test
    fun applyingStatusEffectRecordedWhenPresent() {
        val (machine, manager) = stateMachine()
        val activity = machine.state.value.activities.first { it.reward.statusEffectKey != null }

        machine.attemptActivity(activity.id)

        val statusKey = activity.reward.statusEffectKey
        assertNotNull(statusKey)
        val statusEntry = manager.playerState.value.statusEffects.entries.firstOrNull { it.key == statusKey }
        assertNotNull(statusEntry, "Expected status effect $statusKey to be applied")
        if (activity.reward.statusEffectDurationMillis != null) {
            assertTrue(statusEntry.expiresAtMillis != null)
        }
    }

    @Test
    fun lastResolutionUpdatedAfterAttempt() {
        val (machine, _) = stateMachine()
        val activity = machine.state.value.activities.first()

        val resolution = machine.attemptActivity(activity.id)

        assertNotNull(resolution)
        assertEquals(activity.id, machine.state.value.lastResolution?.activityId)
    }
}
