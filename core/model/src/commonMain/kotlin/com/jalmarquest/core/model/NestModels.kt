package com.jalmarquest.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class NestLevel {
    @SerialName("sprout")
    Sprout,
    @SerialName("burrow")
    Burrow,
    @SerialName("roost")
    Roost,
    @SerialName("haven")
    Haven;

    companion object {
        val ordered = values().toList()
    }
}

@Serializable
enum class CritterRole {
    @SerialName("forager")
    Forager,
    @SerialName("scout")
    Scout,
    @SerialName("guardian")
    Guardian,
    @SerialName("caretaker")
    Caretaker
}

@Serializable
enum class CritterTemperament {
    @SerialName("bold")
    Bold,
    @SerialName("curious")
    Curious,
    @SerialName("tranquil")
    Tranquil,
    @SerialName("wary")
    Wary
}

@Serializable
data class NestCritter(
    val id: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("role_affinity") val roleAffinity: CritterRole,
    val temperament: CritterTemperament,
    val traits: List<String> = emptyList()
)

@Serializable
data class NestAssignment(
    @SerialName("slot_id") val slotId: String,
    val role: CritterRole,
    val critter: NestCritter,
    @SerialName("assigned_at_millis") val assignedAtMillis: Long
)

@Serializable
data class CritterRecruitmentOffer(
    val id: String,
    val critter: NestCritter,
    @SerialName("seed_cost") val seedCost: Int,
    @SerialName("expires_at_millis") val expiresAtMillis: Long
)

@Serializable
data class NestUpgradeStatus(
    @SerialName("in_progress") val inProgress: Boolean = false,
    @SerialName("target_level") val targetLevel: NestLevel? = null,
    @SerialName("completes_at_millis") val completesAtMillis: Long? = null
)

@Serializable
data class NestState(
    val level: NestLevel = NestLevel.Sprout,
    @SerialName("seed_stock") val seedStock: Long = 0,
    val assignments: List<NestAssignment> = emptyList(),
    @SerialName("recruitment_pool") val recruitmentPool: List<CritterRecruitmentOffer> = emptyList(),
    @SerialName("last_passive_tick_millis") val lastPassiveTickMillis: Long = 0L,
    @SerialName("upgrade_status") val upgradeStatus: NestUpgradeStatus = NestUpgradeStatus()
) {
    val assignedCritterIds: Set<String> get() = assignments.mapTo(mutableSetOf()) { it.critter.id }
}
