package com.jalmarquest.feature.nest

import com.jalmarquest.core.model.CritterRole
import com.jalmarquest.core.model.NestLevel

data class NestConfig(
    val levelSpecs: Map<NestLevel, LevelSpec>,
    val recruitment: RecruitmentSpec,
    val roleBonuses: Map<CritterRole, Double>
) {
    init {
        require(levelSpecs.isNotEmpty()) { "NestConfig requires at least one level spec" }
    }

    fun specFor(level: NestLevel): LevelSpec = levelSpecs[level]
        ?: error("Missing level spec for $level")

    fun nextLevel(current: NestLevel): LevelSpec? {
        val ordered = NestLevel.ordered
        val index = ordered.indexOf(current)
        if (index == -1 || index >= ordered.lastIndex) return null
        return specFor(ordered[index + 1])
    }

    val maxRecruitmentOffers: Int get() = recruitment.maxActiveOffers

    data class LevelSpec(
        val level: NestLevel,
        val capacity: Int,
        val basePassiveSeedsPerHour: Double,
        val upgradeCost: Long?,
        val upgradeDurationMillis: Long?,
        val allowedRoles: Set<CritterRole>
    ) {
        init {
            require(capacity > 0) { "capacity must be positive" }
            require(basePassiveSeedsPerHour >= 0) { "basePassiveSeedsPerHour must be non-negative" }
            if (upgradeCost != null) {
                require(upgradeCost >= 0) { "upgradeCost cannot be negative" }
            }
            if (upgradeDurationMillis != null) {
                require(upgradeDurationMillis > 0) { "upgradeDurationMillis must be positive" }
            }
        }
    }

    data class RecruitmentSpec(
        val maxActiveOffers: Int,
        val offerLifetimeMillis: Long,
        val baseSeedCost: Int,
        val variableSeedCost: Int
    ) {
        init {
            require(maxActiveOffers > 0) { "maxActiveOffers must be positive" }
            require(offerLifetimeMillis > 0) { "offerLifetimeMillis must be positive" }
            require(baseSeedCost >= 0) { "baseSeedCost cannot be negative" }
            require(variableSeedCost >= 0) { "variableSeedCost cannot be negative" }
        }
    }

    companion object {
        fun default(): NestConfig {
            val levelSpecs = mapOf(
                NestLevel.Sprout to LevelSpec(
                    level = NestLevel.Sprout,
                    capacity = 2,
                    basePassiveSeedsPerHour = 6.0,
                    upgradeCost = 120,
                    upgradeDurationMillis = 15 * 60 * 1000L,
                    allowedRoles = setOf(CritterRole.Forager, CritterRole.Scout)
                ),
                NestLevel.Burrow to LevelSpec(
                    level = NestLevel.Burrow,
                    capacity = 4,
                    basePassiveSeedsPerHour = 12.0,
                    upgradeCost = 260,
                    upgradeDurationMillis = 30 * 60 * 1000L,
                    allowedRoles = setOf(CritterRole.Forager, CritterRole.Scout, CritterRole.Caretaker)
                ),
                NestLevel.Roost to LevelSpec(
                    level = NestLevel.Roost,
                    capacity = 6,
                    basePassiveSeedsPerHour = 20.0,
                    upgradeCost = 540,
                    upgradeDurationMillis = 60 * 60 * 1000L,
                    allowedRoles = setOf(CritterRole.Forager, CritterRole.Scout, CritterRole.Caretaker, CritterRole.Guardian)
                ),
                NestLevel.Haven to LevelSpec(
                    level = NestLevel.Haven,
                    capacity = 8,
                    basePassiveSeedsPerHour = 32.0,
                    upgradeCost = null,
                    upgradeDurationMillis = null,
                    allowedRoles = setOf(CritterRole.Forager, CritterRole.Scout, CritterRole.Caretaker, CritterRole.Guardian)
                )
            )

            val recruitment = RecruitmentSpec(
                maxActiveOffers = 3,
                offerLifetimeMillis = 20 * 60 * 1000L,
                baseSeedCost = 30,
                variableSeedCost = 25
            )

            val roleBonuses = mapOf(
                CritterRole.Forager to 5.0,
                CritterRole.Scout to 2.5,
                CritterRole.Caretaker to 3.0,
                CritterRole.Guardian to 1.5
            )

            return NestConfig(levelSpecs, recruitment, roleBonuses)
        }
    }
}
