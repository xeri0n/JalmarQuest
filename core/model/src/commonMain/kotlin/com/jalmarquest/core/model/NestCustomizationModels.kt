package com.jalmarquest.core.model

import kotlinx.serialization.Serializable

/**
 * Models for the Nest Housing System (Phase 6).
 * 
 * The Nest is the player's customizable home base, featuring:
 * - 50+ cosmetic items (themes, furniture, decorations)
 * - 6 functional upgrades (Hoard display, Seed silo, Library, etc.)
 * - Trophy Room for quest achievements
 * - Edit Mode for placement and customization
 */

/**
 * Unique identifier for cosmetic items.
 */
@Serializable
data class CosmeticItemId(val value: String)

/**
 * Categories of cosmetic items for the Nest.
 */
enum class CosmeticCategory {
    THEME,          // Wallpaper, flooring, ambient lighting (10+ items)
    FURNITURE,      // Tables, chairs, shelves, storage (20+ items)
    DECORATION,     // Plants, paintings, trinkets, rugs (20+ items)
    FUNCTIONAL      // Upgrades that provide gameplay benefits (6 items)
}

/**
 * Rarity tiers for cosmetic items (affects Glimmer cost).
 */
enum class CosmeticRarity(val glimmerMultiplier: Float) {
    COMMON(1.0f),       // 100-300 Glimmer
    UNCOMMON(1.5f),     // 400-600 Glimmer
    RARE(2.0f),         // 700-1000 Glimmer
    EPIC(3.0f),         // 1100-1500 Glimmer
    LEGENDARY(4.0f)     // 1600-2500 Glimmer
}

/**
 * A cosmetic item that can be placed in the Nest.
 */
@Serializable
data class CosmeticItem(
    val id: CosmeticItemId,
    val name: String,
    val description: String,
    val category: CosmeticCategory,
    val rarity: CosmeticRarity,
    val glimmerCost: Int,
    val unlockRequirement: UnlockRequirement? = null,
    val isPlaceable: Boolean = true,  // False for themes (auto-apply)
    val maxInstances: Int = 1,        // How many can be placed simultaneously
    val visualAssetKey: String        // Key for sprite/asset lookup (future)
)

/**
 * Requirements to unlock a cosmetic item.
 */
@Serializable
sealed class UnlockRequirement {
    @Serializable
    data class QuestCompletion(val questId: String) : UnlockRequirement()
    
    @Serializable
    data class PlayerLevel(val minimumLevel: Int) : UnlockRequirement()
    
    @Serializable
    data class HoardRank(val minimumRank: Int) : UnlockRequirement()
    
    @Serializable
    data class AchievementUnlock(val achievementId: String) : UnlockRequirement()
}

/**
 * Position data for a placed cosmetic item.
 */
@Serializable
data class PlacedCosmetic(
    val cosmeticId: CosmeticItemId,
    val instanceId: String,  // Unique ID for this placement (for multiple instances)
    val x: Float = 0f,       // Grid position X (0-10 range)
    val y: Float = 0f,       // Grid position Y (0-10 range)
    val rotation: Float = 0f // Rotation in degrees (0-360)
)

/**
 * Functional upgrade types that provide gameplay benefits.
 */
enum class FunctionalUpgradeType {
    SHINY_DISPLAY,          // Shows hoard collection, +10% hoard XP
    SEED_SILO,              // +50% seed storage capacity
    SMALL_LIBRARY,          // Unlock 2 extra Thought Cabinet slots
    PERSONAL_ALCHEMY_STATION, // Craft concoctions in nest (no hub visit)
    SMALL_WORKBENCH,        // Craft items in nest (no hub visit)
    COZY_PERCH,             // Companion rests here, +5% companion XP
    TROPHY_ROOM             // Display quest achievement trophies
}

/**
 * A functional upgrade that provides mechanical benefits.
 */
@Serializable
data class FunctionalUpgrade(
    val type: FunctionalUpgradeType,
    val cosmeticItemId: CosmeticItemId,
    val isActive: Boolean = false  // Upgraded and placed in nest
)

/**
 * Trophy display for quest achievements.
 */
@Serializable
data class TrophyDisplay(
    val questId: String,
    val displayName: String,
    val description: String,
    val placedInRoom: Boolean = false
)

/**
 * Complete state of the player's Nest customization.
 */
@Serializable
data class NestCustomizationState(
    val ownedCosmetics: Set<CosmeticItemId> = emptySet(),
    val placedCosmetics: List<PlacedCosmetic> = emptyList(),
    val activeTheme: CosmeticItemId? = null,  // Current wallpaper/flooring
    val functionalUpgrades: Map<FunctionalUpgradeType, FunctionalUpgrade> = emptyMap(),
    val trophyDisplay: List<TrophyDisplay> = emptyList(),
    val editModeActive: Boolean = false
) {
    /**
     * Check if player owns a specific cosmetic.
     */
    fun ownsCosmetic(id: CosmeticItemId): Boolean = id in ownedCosmetics
    
    /**
     * Count how many instances of a cosmetic are currently placed.
     */
    fun getPlacedCount(cosmeticId: CosmeticItemId): Int {
        return placedCosmetics.count { it.cosmeticId == cosmeticId }
    }
    
    /**
     * Check if a functional upgrade is active.
     */
    fun hasActiveUpgrade(type: FunctionalUpgradeType): Boolean {
        return functionalUpgrades[type]?.isActive == true
    }
    
    /**
     * Get seed storage capacity modifier from upgrades.
     */
    fun getSeedStorageBonus(): Float {
        return if (hasActiveUpgrade(FunctionalUpgradeType.SEED_SILO)) 0.5f else 0f
    }
    
    /**
     * Get hoard XP bonus from upgrades.
     */
    fun getHoardXpBonus(): Float {
        return if (hasActiveUpgrade(FunctionalUpgradeType.SHINY_DISPLAY)) 0.1f else 0f
    }
    
    /**
     * Get extra Thought Cabinet slots from upgrades.
     */
    fun getExtraThoughtSlots(): Int {
        return if (hasActiveUpgrade(FunctionalUpgradeType.SMALL_LIBRARY)) 2 else 0
    }
    
    /**
     * Check if alchemy can be done in nest.
     */
    fun canCraftInNest(): Boolean {
        return hasActiveUpgrade(FunctionalUpgradeType.PERSONAL_ALCHEMY_STATION) ||
               hasActiveUpgrade(FunctionalUpgradeType.SMALL_WORKBENCH)
    }
    
    /**
     * Get companion XP bonus from upgrades.
     */
    fun getCompanionXpBonus(): Float {
        return if (hasActiveUpgrade(FunctionalUpgradeType.COZY_PERCH)) 0.05f else 0f
    }
}

/**
 * Result of attempting to purchase a cosmetic item.
 */
sealed class CosmeticPurchaseResult {
    data object Success : CosmeticPurchaseResult()
    data object InsufficientGlimmer : CosmeticPurchaseResult()
    data object AlreadyOwned : CosmeticPurchaseResult()
    data class RequirementNotMet(val requirement: UnlockRequirement) : CosmeticPurchaseResult()
}

/**
 * Result of attempting to place a cosmetic in the nest.
 */
sealed class PlacementResult {
    data class Success(val instanceId: String) : PlacementResult()
    data object NotOwned : PlacementResult()
    data object MaxInstancesReached : PlacementResult()
    data object InvalidPosition : PlacementResult()
}
