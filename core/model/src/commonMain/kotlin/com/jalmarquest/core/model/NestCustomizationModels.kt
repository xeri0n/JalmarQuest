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
 * Alpha 2.3: Each upgrade now supports 3 tiers.
 */
enum class FunctionalUpgradeType {
    SHINY_DISPLAY,          // Shows hoard collection, +10/20/30% hoard XP
    SEED_SILO,              // +50/100/150% seed storage capacity
    SMALL_LIBRARY,          // Unlock 2/4/6 extra Thought Cabinet slots
    PERSONAL_ALCHEMY_STATION, // Craft concoctions in nest (no hub visit)
    SMALL_WORKBENCH,        // Craft items in nest (no hub visit)
    COZY_PERCH,             // Companion rests here, +5/10/15% companion XP
    TROPHY_ROOM,            // Display quest achievement trophies
    COMPANION_ASSIGNMENT_BOARD,  // Alpha 2.3 Part 2.2: Assign companions to passive tasks
    LORE_ARCHIVE,           // Alpha 2.3 Part 2.2: Store/review discovered lore snippets
    AI_DIRECTOR_CONSOLE     // Alpha 2.3 Part 2.2: View AI Director event history + debug
}

/**
 * Alpha 2.3: Upgrade tier levels.
 */
enum class UpgradeTier(val level: Int) {
    TIER_1(1),
    TIER_2(2),
    TIER_3(3)
}

/**
 * A functional upgrade that provides mechanical benefits.
 * Alpha 2.3: Now supports tiered upgrades.
 */
@Serializable
data class FunctionalUpgrade(
    val type: FunctionalUpgradeType,
    val cosmeticItemId: CosmeticItemId,
    val currentTier: UpgradeTier = UpgradeTier.TIER_1,  // Alpha 2.3: Current upgrade tier
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
     * Alpha 2.3: Scales with upgrade tier (50%/100%/150%).
     */
    fun getSeedStorageBonus(): Float {
        val upgrade = functionalUpgrades[FunctionalUpgradeType.SEED_SILO]
        if (upgrade?.isActive != true) return 0f
        return when (upgrade.currentTier) {
            UpgradeTier.TIER_1 -> 0.5f
            UpgradeTier.TIER_2 -> 1.0f
            UpgradeTier.TIER_3 -> 1.5f
        }
    }
    
    /**
     * Get hoard XP bonus from upgrades.
     * Alpha 2.3: Scales with upgrade tier (10%/20%/30%).
     */
    fun getHoardXpBonus(): Float {
        val upgrade = functionalUpgrades[FunctionalUpgradeType.SHINY_DISPLAY]
        if (upgrade?.isActive != true) return 0f
        return when (upgrade.currentTier) {
            UpgradeTier.TIER_1 -> 0.1f
            UpgradeTier.TIER_2 -> 0.2f
            UpgradeTier.TIER_3 -> 0.3f
        }
    }
    
    /**
     * Get extra Thought Cabinet slots from upgrades.
     * Alpha 2.3: Scales with upgrade tier (2/4/6 slots).
     */
    fun getExtraThoughtSlots(): Int {
        val upgrade = functionalUpgrades[FunctionalUpgradeType.SMALL_LIBRARY]
        if (upgrade?.isActive != true) return 0
        return when (upgrade.currentTier) {
            UpgradeTier.TIER_1 -> 2
            UpgradeTier.TIER_2 -> 4
            UpgradeTier.TIER_3 -> 6
        }
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
     * Alpha 2.3: Scales with upgrade tier (5%/10%/15%).
     */
    fun getCompanionXpBonus(): Float {
        val upgrade = functionalUpgrades[FunctionalUpgradeType.COZY_PERCH]
        if (upgrade?.isActive != true) return 0f
        return when (upgrade.currentTier) {
            UpgradeTier.TIER_1 -> 0.05f
            UpgradeTier.TIER_2 -> 0.10f
            UpgradeTier.TIER_3 -> 0.15f
        }
    }
    
    /**
     * Alpha 2.3 Part 2.2: Get max concurrent companion assignments.
     * Scales with COMPANION_ASSIGNMENT_BOARD tier (2/4/6 concurrent tasks).
     */
    fun getMaxCompanionAssignments(): Int {
        val upgrade = functionalUpgrades[FunctionalUpgradeType.COMPANION_ASSIGNMENT_BOARD]
        if (upgrade?.isActive != true) return 0
        return when (upgrade.currentTier) {
            UpgradeTier.TIER_1 -> 2
            UpgradeTier.TIER_2 -> 4
            UpgradeTier.TIER_3 -> 6
        }
    }
    
    /**
     * Alpha 2.3 Part 2.2: Get max stored lore entries.
     * Scales with LORE_ARCHIVE tier (20/50/100 entries).
     */
    fun getMaxLoreArchiveEntries(): Int {
        val upgrade = functionalUpgrades[FunctionalUpgradeType.LORE_ARCHIVE]
        if (upgrade?.isActive != true) return 0
        return when (upgrade.currentTier) {
            UpgradeTier.TIER_1 -> 20
            UpgradeTier.TIER_2 -> 50
            UpgradeTier.TIER_3 -> 100
        }
    }
    
    /**
     * Alpha 2.3 Part 2.2: Get AI Director event history depth.
     * Scales with AI_DIRECTOR_CONSOLE tier (10/25/50 events).
     */
    fun getAiDirectorHistoryDepth(): Int {
        val upgrade = functionalUpgrades[FunctionalUpgradeType.AI_DIRECTOR_CONSOLE]
        if (upgrade?.isActive != true) return 0
        return when (upgrade.currentTier) {
            UpgradeTier.TIER_1 -> 10
            UpgradeTier.TIER_2 -> 25
            UpgradeTier.TIER_3 -> 50
        }
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

/**
 * Alpha 2.3: Result of attempting to upgrade a functional upgrade to the next tier.
 */
sealed class UpgradeTierResult {
    data class Success(val newTier: UpgradeTier, val bonusDescription: String) : UpgradeTierResult()
    data object NotOwned : UpgradeTierResult()
    data object NotActivated : UpgradeTierResult()
    data object AlreadyMaxTier : UpgradeTierResult()
    data object TierNotFound : UpgradeTierResult()
    data class LevelTooLow(val requiredLevel: Int) : UpgradeTierResult()
    data class PrerequisiteNotMet(val prerequisiteTier: UpgradeTier) : UpgradeTierResult()
    data class InsufficientSeeds(val required: Int, val available: Int) : UpgradeTierResult()
    data class InsufficientGlimmer(val required: Int, val available: Int) : UpgradeTierResult()
    data class InsufficientIngredients(
        val ingredientId: IngredientId, 
        val required: Int, 
        val available: Int
    ) : UpgradeTierResult()
}

/**
 * Alpha 2.3: Information about whether player can afford an upgrade tier.
 */
data class UpgradeTierAffordability(
    val canAfford: Boolean,
    val missingSeeds: Int,
    val missingGlimmer: Int,
    val missingIngredients: Map<IngredientId, Int>,
    val levelRequired: Int
)
