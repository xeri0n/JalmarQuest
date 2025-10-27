package com.jalmarquest.feature.nest

import com.jalmarquest.core.model.CritterRole
import com.jalmarquest.core.model.NestAssignment
import com.jalmarquest.core.model.NestState
import com.jalmarquest.core.model.NestUpgradeStatus
import com.jalmarquest.core.model.NestLevel
import com.jalmarquest.core.state.GameStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlin.math.floor

class NestStateMachine(
    initialState: NestState = NestState(),
    private val config: NestConfig = NestConfig.default(),
    private val recruitmentEngine: NestRecruitmentEngine = NestRecruitmentEngine(),
    private val currentTimeMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() },
    private val gameStateManager: GameStateManager? = null
) {
    private val mutex = Mutex()
    private val _state = MutableStateFlow(
        if (initialState.lastPassiveTickMillis == 0L) {
            initialState.copy(lastPassiveTickMillis = currentTimeMillis())
        } else {
            initialState
        }
    )
    val state: StateFlow<NestState> = _state.asStateFlow()

    suspend fun refreshRecruitment(now: Long = currentTimeMillis()) {
        mutex.withLock {
            val updated = recruitmentEngine.ensureOffers(_state.value, now, config)
            _state.value = updated
        }
    }

    suspend fun acceptRecruitment(offerId: String, role: CritterRole, now: Long = currentTimeMillis()) {
        mutex.withLock {
            val current = sanitisedState(now)
            val offer = current.recruitmentPool.firstOrNull { it.id == offerId }
                ?: error("Recruitment offer $offerId is not available")
            val spec = config.specFor(current.level)
            require(role in spec.allowedRoles) { "Role $role is not unlocked at level ${current.level}" }
            require(current.assignments.size < spec.capacity) { "Nest slots are full" }
            require(offer.seedCost.toLong() <= current.seedStock) { "Not enough seeds to recruit" }
            require(offer.critter.id !in current.assignedCritterIds) { "Critter already assigned" }

            // Log choice for analytics
            gameStateManager?.appendChoice("nest_recruit_${offer.critter.id}_as_${role.name.lowercase()}")

            val slotId = nextSlotId(role, current)
            val assignment = NestAssignment(
                slotId = slotId,
                role = role,
                critter = offer.critter,
                assignedAtMillis = now
            )

            val remainingSeeds = current.seedStock - offer.seedCost
            val updatedAssignments = current.assignments + assignment
            val poolWithoutOffer = current.recruitmentPool.filterNot { it.id == offerId }
            val refreshed = recruitmentEngine.ensureOffers(
                current.copy(
                    seedStock = remainingSeeds,
                    assignments = updatedAssignments,
                    recruitmentPool = poolWithoutOffer
                ),
                now,
                config
            )
            _state.value = refreshed
        }
    }

    suspend fun unassign(slotId: String, now: Long = currentTimeMillis()) {
        mutex.withLock {
            val current = sanitisedState(now)
            val assignment = current.assignments.firstOrNull { it.slotId == slotId }
            val filtered = current.assignments.filterNot { it.slotId == slotId }
            if (filtered.size == current.assignments.size) return
            
            // Log choice for analytics
            assignment?.let {
                gameStateManager?.appendChoice("nest_unassign_${it.critter.id}")
            }
            
            _state.value = current.copy(assignments = filtered)
        }
    }

    suspend fun requestUpgrade(now: Long = currentTimeMillis()) {
        mutex.withLock {
            val current = sanitisedState(now)
            if (current.upgradeStatus.inProgress) return
            val currentSpec = config.specFor(current.level)
            val nextSpec = config.nextLevel(current.level) ?: return
            val cost = currentSpec.upgradeCost ?: return
            require(cost <= current.seedStock) { "Not enough seeds to upgrade" }
            
            // Log choice for analytics
            gameStateManager?.appendChoice("nest_upgrade_to_${nextSpec.level.name.lowercase()}")
            
            val duration = currentSpec.upgradeDurationMillis ?: 0L
            val completesAt = now + duration
            _state.value = current.copy(
                seedStock = current.seedStock - cost,
                upgradeStatus = NestUpgradeStatus(
                    inProgress = true,
                    targetLevel = nextSpec.level,
                    completesAtMillis = completesAt
                )
            )
        }
    }

    suspend fun tick(now: Long = currentTimeMillis()) {
        mutex.withLock {
            var current = sanitisedState(now)
            current = maybeCompleteUpgrade(current, now)
            current = applyPassiveSeeds(current, now)
            current = recruitmentEngine.ensureOffers(current, now, config)
            _state.value = current
        }
    }

    private fun sanitisedState(now: Long): NestState {
        val state = _state.value
        val activePool = state.recruitmentPool.filter { it.expiresAtMillis > now }
        val upgrade = when {
            !state.upgradeStatus.inProgress -> state.upgradeStatus
            state.upgradeStatus.completesAtMillis == null -> state.upgradeStatus
            else -> state.upgradeStatus
        }
        if (activePool.size == state.recruitmentPool.size && upgrade == state.upgradeStatus) {
            return state
        }
        return state.copy(recruitmentPool = activePool, upgradeStatus = upgrade)
    }

    private fun maybeCompleteUpgrade(state: NestState, now: Long): NestState {
        val status = state.upgradeStatus
        if (!status.inProgress) return state
        val completesAt = status.completesAtMillis ?: return state
        if (now < completesAt) return state
        val target = status.targetLevel ?: return state.copy(
            upgradeStatus = NestUpgradeStatus()
        )
        val targetSpec = config.specFor(target)
        val clearedAssignments = state.assignments.take(targetSpec.capacity)
        return state.copy(
            level = target,
            assignments = clearedAssignments,
            upgradeStatus = NestUpgradeStatus()
        )
    }

    private fun applyPassiveSeeds(state: NestState, now: Long): NestState {
        val last = state.lastPassiveTickMillis
        if (now <= last) return state
        val deltaMillis = now - last
        if (deltaMillis < MIN_PASSIVE_INTERVAL_MILLIS) {
            return state.copy(lastPassiveTickMillis = now)
        }
        val hours = deltaMillis.toDouble() / MILLIS_PER_HOUR
        val rate = passiveSeedRate(state)
        val generated = floor(rate * hours).toLong()
        if (generated <= 0) {
            return state.copy(lastPassiveTickMillis = now)
        }
        return state.copy(
            seedStock = state.seedStock + generated,
            lastPassiveTickMillis = now
        )
    }

    private fun passiveSeedRate(state: NestState): Double {
        val spec = config.specFor(state.level)
        val bonus = state.assignments.sumOf { assignment ->
            config.roleBonuses[assignment.role] ?: 0.0
        }
        return spec.basePassiveSeedsPerHour + bonus
    }

    private fun nextSlotId(role: CritterRole, state: NestState): String {
        val count = state.assignments.count { it.role == role } + 1
        return "${role.name.lowercase()}-$count"
    }

    companion object {
        private const val MILLIS_PER_HOUR = 60_000.0 * 60.0
        private const val MIN_PASSIVE_INTERVAL_MILLIS = 60_000L
    }
}
