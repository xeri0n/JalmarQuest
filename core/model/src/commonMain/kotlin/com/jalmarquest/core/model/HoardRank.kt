package com.jalmarquest.core.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Represents the player's Hoard Rank - a prestige system based on collecting valuable Shinies.
 * Higher ranks unlock cosmetic rewards, bragging rights, and serve as a Seed sink.
 */

@Serializable
@JvmInline
value class ShinyId(val value: String)

@Serializable
enum class ShinyRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY,
    MYTHIC
}

/**
 * A collectible item for the Hoard. Shinies have no gameplay impact but contribute to Hoard Rank.
 */
@Serializable
data class Shiny(
    val id: ShinyId,
    val nameKey: String, // Localization key for display name
    val descriptionKey: String, // Localization key for lore-rich description
    val rarity: ShinyRarity,
    val baseValue: Long, // Base Seed value
    val discoveredAt: Long? = null // Timestamp when first acquired (null if not yet discovered)
)

/**
 * Hoard Rank tier representing progression milestones.
 */
@Serializable
enum class HoardRankTier {
    SCAVENGER,      // 0-999 total value
    COLLECTOR,      // 1,000-4,999
    CURATOR,        // 5,000-14,999
    MAGNATE,        // 15,000-49,999
    LEGEND,         // 50,000-149,999
    MYTH            // 150,000+
}

/**
 * Player's current Hoard Rank state.
 */
@Serializable
data class HoardRank(
    val totalValue: Long = 0, // Total appraised value of all Shinies
    val tier: HoardRankTier = HoardRankTier.SCAVENGER,
    val shiniesCollected: Int = 0, // Total unique Shinies discovered
    val rank: Int = 0 // Global rank (0 = unranked, 1 = top player)
) {
    companion object {
        /**
         * Calculate tier based on total hoard value.
         */
        fun calculateTier(totalValue: Long): HoardRankTier = when {
            totalValue < 1_000 -> HoardRankTier.SCAVENGER
            totalValue < 5_000 -> HoardRankTier.COLLECTOR
            totalValue < 15_000 -> HoardRankTier.CURATOR
            totalValue < 50_000 -> HoardRankTier.MAGNATE
            totalValue < 150_000 -> HoardRankTier.LEGEND
            else -> HoardRankTier.MYTH
        }

        /**
         * Get the minimum value required for the next tier.
         */
        fun nextTierThreshold(currentTier: HoardRankTier): Long? = when (currentTier) {
            HoardRankTier.SCAVENGER -> 1_000
            HoardRankTier.COLLECTOR -> 5_000
            HoardRankTier.CURATOR -> 15_000
            HoardRankTier.MAGNATE -> 50_000
            HoardRankTier.LEGEND -> 150_000
            HoardRankTier.MYTH -> null // Max tier
        }
    }
}

/**
 * Player's Shiny collection state.
 */
@Serializable
data class ShinyCollection(
    val ownedShinies: List<Shiny> = emptyList()
) {
    /**
     * Check if a specific Shiny has been discovered.
     */
    fun hasShiny(shinyId: ShinyId): Boolean = 
        ownedShinies.any { it.id == shinyId }

    /**
     * Add a Shiny to the collection with discovery timestamp.
     */
    fun addShiny(shiny: Shiny, discoveredAt: Long): ShinyCollection =
        if (hasShiny(shiny.id)) {
            this // Already owned, no duplicate
        } else {
            ShinyCollection(ownedShinies + shiny.copy(discoveredAt = discoveredAt))
        }

    /**
     * Calculate total appraised value of all Shinies.
     */
    fun calculateTotalValue(): Long = ownedShinies.sumOf { it.baseValue }

    /**
     * Get count of Shinies by rarity.
     */
    fun countByRarity(rarity: ShinyRarity): Int =
        ownedShinies.count { it.rarity == rarity }
}
