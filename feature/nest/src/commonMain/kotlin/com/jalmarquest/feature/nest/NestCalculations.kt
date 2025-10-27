package com.jalmarquest.feature.nest

import com.jalmarquest.core.model.NestState

fun NestState.passiveSeedsPerHour(config: NestConfig): Double {
    val spec = config.specFor(level)
    val bonus = assignments.sumOf { assignment -> config.roleBonuses[assignment.role] ?: 0.0 }
    return spec.basePassiveSeedsPerHour + bonus
}

fun NestState.availableCapacity(config: NestConfig): Int {
    val spec = config.specFor(level)
    return spec.capacity - assignments.size
}

fun NestState.canUpgrade(config: NestConfig): Boolean {
    val next = config.nextLevel(level) ?: return false
    val cost = next.upgradeCost ?: return false
    return !upgradeStatus.inProgress && seedStock >= cost
}
