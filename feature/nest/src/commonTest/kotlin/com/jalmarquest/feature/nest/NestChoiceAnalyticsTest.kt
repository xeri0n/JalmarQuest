package com.jalmarquest.feature.nest

import com.jalmarquest.core.model.CritterRole
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.state.GameStateManager
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Integration test validating that NestStateMachine correctly logs choice tags
 * for recruitment, assignment, and upgrade actions.
 */
class NestChoiceAnalyticsTest {
    @Test
    fun acceptRecruitment_appendsChoiceTag() = runTest {
        val player = Player(id = "test", name = "Test Player")
        val gameStateManager = GameStateManager(
            initialPlayer = player,
            timestampProvider = { System.currentTimeMillis() }
        )
        val config = NestConfig.default()
        val stateMachine = NestStateMachine(
            config = config,
            gameStateManager = gameStateManager,
            initialState = com.jalmarquest.core.model.NestState(seedStock = 1000)
        )

        val now = System.currentTimeMillis()
        stateMachine.refreshRecruitment(now)
        val offer = stateMachine.state.value.recruitmentPool.first()
        
        stateMachine.acceptRecruitment(offer.id, CritterRole.Forager, now)

        val choiceLog = gameStateManager.playerState.value.choiceLog.entries
        assertTrue(choiceLog.any { it.tag.value.contains("nest_recruit_") && it.tag.value.contains("_as_forager") })
    }

    @Test
    fun unassign_appendsChoiceTag() = runTest {
        val player = Player(id = "test", name = "Test Player")
        val gameStateManager = GameStateManager(
            initialPlayer = player,
            timestampProvider = { System.currentTimeMillis() }
        )
        val config = NestConfig.default()
        val stateMachine = NestStateMachine(
            config = config,
            gameStateManager = gameStateManager,
            initialState = com.jalmarquest.core.model.NestState(seedStock = 1000)
        )

        val now = System.currentTimeMillis()
        stateMachine.refreshRecruitment(now)
        val offer = stateMachine.state.value.recruitmentPool.first()
        stateMachine.acceptRecruitment(offer.id, CritterRole.Forager, now)
        
        val slotId = stateMachine.state.value.assignments.first().slotId
        stateMachine.unassign(slotId, now)

        val choiceLog = gameStateManager.playerState.value.choiceLog.entries
        assertTrue(choiceLog.any { it.tag.value.contains("nest_unassign_") })
    }

    @Test
    fun requestUpgrade_appendsChoiceTag() = runTest {
        val player = Player(id = "test", name = "Test Player")
        val gameStateManager = GameStateManager(
            initialPlayer = player,
            timestampProvider = { System.currentTimeMillis() }
        )
        val config = NestConfig.default()
        val spec = config.specFor(com.jalmarquest.core.model.NestLevel.Sprout)
        val upgradeCost = spec.upgradeCost ?: 100
        
        val stateMachine = NestStateMachine(
            config = config,
            gameStateManager = gameStateManager,
            initialState = com.jalmarquest.core.model.NestState(seedStock = upgradeCost)
        )

        stateMachine.requestUpgrade()

        val choiceLog = gameStateManager.playerState.value.choiceLog.entries
        assertTrue(choiceLog.any { it.tag.value.contains("nest_upgrade_to_") })
    }
}
