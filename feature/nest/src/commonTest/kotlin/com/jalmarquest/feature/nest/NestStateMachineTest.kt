package com.jalmarquest.feature.nest

import com.jalmarquest.core.model.CritterRole
import com.jalmarquest.core.model.NestLevel
import com.jalmarquest.core.model.NestState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NestStateMachineTest {
    @Test
    fun refreshRecruitment_populatesOffers(): Unit = runTest {
        val now = 1_000L
        val machine = NestStateMachine(
            initialState = NestState(seedStock = 200, lastPassiveTickMillis = now),
            currentTimeMillis = { now }
        )

        machine.refreshRecruitment()

        val state = machine.state.value
        assertEquals(3, state.recruitmentPool.size)
        assertTrue(state.recruitmentPool.all { it.expiresAtMillis > now })
    }

    @Test
    fun acceptRecruitment_movesSeedsAndAssignments(): Unit = runTest {
        var now = 10_000L
        val machine = NestStateMachine(
            initialState = NestState(seedStock = 300, lastPassiveTickMillis = now),
            currentTimeMillis = { now }
        )
        machine.refreshRecruitment(now)

        val offer = machine.state.value.recruitmentPool.first()
        machine.acceptRecruitment(offer.id, CritterRole.Forager, now)

        val updated = machine.state.value
        assertEquals(1, updated.assignments.size)
        assertTrue(updated.assignments.first().critter.id == offer.critter.id)
    assertEquals(300L - offer.seedCost.toLong(), updated.seedStock)
        assertEquals(3, updated.recruitmentPool.size)
    }

    @Test
    fun tick_awardsPassiveSeeds(): Unit = runTest {
        var now = 120_000L
        val initialTick = now - ONE_HOUR
        val machine = NestStateMachine(
            initialState = NestState(seedStock = 0, lastPassiveTickMillis = initialTick),
            currentTimeMillis = { now }
        )

        machine.tick()

        val state = machine.state.value
    assertEquals(6L, state.seedStock)
        assertEquals(now, state.lastPassiveTickMillis)
    }

    @Test
    fun upgrade_advancesLevelWhenComplete(): Unit = runTest {
        var now = 200_000L
        val config = NestConfig.default()
        val sproutSpec = config.specFor(NestLevel.Sprout)
        val machine = NestStateMachine(
            initialState = NestState(seedStock = sproutSpec.upgradeCost!! + 10, lastPassiveTickMillis = now),
            config = config,
            currentTimeMillis = { now }
        )

        machine.requestUpgrade(now)
        val afterRequest = machine.state.value
        assertTrue(afterRequest.upgradeStatus.inProgress)
        assertEquals(NestLevel.Burrow, afterRequest.upgradeStatus.targetLevel)
    assertEquals(10L, afterRequest.seedStock)

        now += sproutSpec.upgradeDurationMillis!!
        machine.tick(now)
        val upgraded = machine.state.value
        assertEquals(NestLevel.Burrow, upgraded.level)
        assertTrue(!upgraded.upgradeStatus.inProgress)
    }

    companion object {
        private const val ONE_HOUR = 3_600_000L
    }
}
