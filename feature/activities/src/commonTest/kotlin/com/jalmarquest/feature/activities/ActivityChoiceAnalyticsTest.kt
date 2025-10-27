package com.jalmarquest.feature.activities

import com.jalmarquest.core.model.Player
import com.jalmarquest.core.state.GameStateManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Integration test validating that ActivityStateMachine correctly logs choice tags
 * to GameStateManager for analytics tracking.
 */
class ActivityChoiceAnalyticsTest {
    @Test
    fun attemptActivity_appendsChoiceTag() = runTest {
        val player = Player(id = "test", name = "Test Player")
        val gameStateManager = GameStateManager(
            initialPlayer = player,
            timestampProvider = { System.currentTimeMillis() }
        )
        val stateMachine = ActivityStateMachine(gameStateManager)

        val initialChoices = gameStateManager.playerState.value.choiceLog.entries.size

        val activityId = ActivityId("twilight_dungeon")
        stateMachine.attemptActivity(activityId)

        val finalChoices = gameStateManager.playerState.value.choiceLog.entries
        assertEquals(initialChoices + 1, finalChoices.size)
        assertTrue(finalChoices.last().tag.value.contains("secondary_activity_twilight_dungeon"))
    }

    @Test
    fun multipleAttempts_accumulateChoiceTags() = runTest {
        val player = Player(id = "test", name = "Test Player")
        val gameStateManager = GameStateManager(
            initialPlayer = player,
            timestampProvider = { System.currentTimeMillis() }
        )
        val stateMachine = ActivityStateMachine(gameStateManager)

        stateMachine.attemptActivity(ActivityId("twilight_dungeon"))
        stateMachine.attemptActivity(ActivityId("sparring_arena"))
        stateMachine.attemptActivity(ActivityId("hen_pen_drill"))

        val choiceLog = gameStateManager.playerState.value.choiceLog.entries
        assertTrue(choiceLog.any { it.tag.value.contains("secondary_activity_twilight_dungeon") })
        assertTrue(choiceLog.any { it.tag.value.contains("secondary_activity_sparring_arena") })
        assertTrue(choiceLog.any { it.tag.value.contains("secondary_activity_hen_pen_drill") })
    }
}
