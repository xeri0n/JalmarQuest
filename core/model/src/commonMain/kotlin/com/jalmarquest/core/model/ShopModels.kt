package com.jalmarquest.core.model

import kotlinx.serialization.Serializable

/**
 * Shop system data models for cosmetic items and purchases
 * Part of Milestone 5 Phase 3: Shop & Cosmetic Storefront
 */

// ===== IDENTIFIERS =====

@Serializable
data class ShopItemId(val value: String) {
    override fun toString(): String = value
}

// ===== ENUMS =====

/**
 * Shop item categories for filtering and rotation pools
 */
@Serializable
enum class ShopCategory {
    /** Cosmetic items for visual customization */
    COSMETICS,
    
    /** Bundled item packs at discounted prices */
    BUNDLES,
    
    /** Limited-time seasonal exclusive items */
    SEASONAL,
    
    /** Rotating daily special offers */
    DAILY_DEALS
}

/**
 * Types of cosmetic items that can be equipped
 * Corresponds to equipment slots in player customization
 */
@Serializable
enum class CosmeticType {
    /** Head slot - decorative crowns, hats, headpieces */
    CROWN,
    
    /** Back slot - cloaks, capes, wings */
    CLOAK,
    
    /** Shoulder slot - mantles, epaulettes, pauldrons */
    MANTLE,
    
    /** Neck slot - necklaces, pendants, amulets */
    NECKLACE,
    
    /** Effect slot - auras, particle effects, glows */
    AURA,
    
    /** Full outfit - complete cosmetic sets (replaces all slots) */
    REGALIA
}

/**
 * Rotation frequency for shop items
 * Determines how often items appear in shop rotations
 */
@Serializable
enum class RotationFrequency {
    /** Always available, never rotates out */
    PERMANENT,
    
    /** Rotates every 24 hours (daily deals) */
    DAILY,
    
    /** Rotates every 7 days (weekly featured) */
    WEEKLY,
    
    /** Available only during specific season/event */
    SEASONAL
}

// ===== DATA MODELS =====

/**
 * Shop item definition
 * Cosmetics, bundles, or special offers available for purchase with Glimmer Shards
 */
@Serializable
data class ShopItem(
    /** Unique identifier for this shop item */
    val id: ShopItemId,
    
    /** Display name of the item */
    val name: String,
    
    /** Description of the item and its effects */
    val description: String,
    
    /** Cost in Glimmer Shards */
    val glimmerCost: Int,
    
    /** Shop category (COSMETICS, BUNDLES, etc.) */
    val category: ShopCategory,
    
    /** How often this item rotates in shop */
    val rotationFrequency: RotationFrequency,
    
    /** For cosmetics: which equipment slot this occupies */
    val cosmeticType: CosmeticType? = null,
    
    /** Maximum number of times this can be purchased (null = unlimited) */
    val stock: Int? = null,
    
    /** For limited items: Unix timestamp when availability ends (null = no expiration) */
    val expirationTime: Long? = null,
    
    /** Visual rarity tier for UI display (1=common, 5=legendary) */
    val rarityTier: Int = 1
) {
    init {
        require(glimmerCost >= 0) { "Glimmer cost cannot be negative" }
        require(rarityTier in 1..5) { "Rarity tier must be between 1 and 5" }
        if (category == ShopCategory.COSMETICS) {
            requireNotNull(cosmeticType) { "Cosmetic items must specify cosmeticType" }
        }
    }
}

/**
 * Player's equipped cosmetics
 * Tracks which cosmetic items are currently being displayed
 */
@Serializable
data class EquippedCosmetics(
    /** Crown/hat equipped in head slot (null = none) */
    val crown: ShopItemId? = null,
    
    /** Cloak/cape equipped in back slot (null = none) */
    val cloak: ShopItemId? = null,
    
    /** Mantle/shoulders equipped in shoulder slot (null = none) */
    val mantle: ShopItemId? = null,
    
    /** Necklace/pendant equipped in neck slot (null = none) */
    val necklace: ShopItemId? = null,
    
    /** Aura/effect equipped in effect slot (null = none) */
    val aura: ShopItemId? = null,
    
    /** Full regalia outfit (if equipped, overrides all other slots) */
    val regalia: ShopItemId? = null
) {
    /**
     * Get all equipped cosmetic IDs as a list
     */
    fun getAllEquipped(): List<ShopItemId> = listOfNotNull(
        crown, cloak, mantle, necklace, aura, regalia
    )
    
    /**
     * Check if any cosmetics are equipped
     */
    fun hasAnyEquipped(): Boolean = getAllEquipped().isNotEmpty()
    
    /**
     * Equip a cosmetic item in the appropriate slot based on its type
     */
    fun equip(itemId: ShopItemId, type: CosmeticType): EquippedCosmetics {
        return when (type) {
            CosmeticType.CROWN -> copy(crown = itemId)
            CosmeticType.CLOAK -> copy(cloak = itemId)
            CosmeticType.MANTLE -> copy(mantle = itemId)
            CosmeticType.NECKLACE -> copy(necklace = itemId)
            CosmeticType.AURA -> copy(aura = itemId)
            CosmeticType.REGALIA -> copy(regalia = itemId, crown = null, cloak = null, mantle = null, necklace = null, aura = null)
        }
    }
    
    /**
     * Unequip a cosmetic from the specified slot
     */
    fun unequip(type: CosmeticType): EquippedCosmetics {
        return when (type) {
            CosmeticType.CROWN -> copy(crown = null)
            CosmeticType.CLOAK -> copy(cloak = null)
            CosmeticType.MANTLE -> copy(mantle = null)
            CosmeticType.NECKLACE -> copy(necklace = null)
            CosmeticType.AURA -> copy(aura = null)
            CosmeticType.REGALIA -> copy(regalia = null)
        }
    }
}

/**
 * Player's shop state
 * Tracks purchases, equipped cosmetics, and rotation timers
 */
@Serializable
data class ShopState(
    /** Set of all shop items the player has purchased */
    val purchasedItems: Set<ShopItemId> = emptySet(),
    
    /** Currently equipped cosmetics */
    val equippedCosmetics: EquippedCosmetics = EquippedCosmetics(),
    
    /** Unix timestamp of last daily rotation check */
    val lastDailyRotation: Long = 0L,
    
    /** Unix timestamp of last weekly rotation check */
    val lastWeeklyRotation: Long = 0L
) {
    /**
     * Check if player owns a specific shop item
     */
    fun ownsItem(itemId: ShopItemId): Boolean = itemId in purchasedItems
    
    /**
     * Add a purchased item to the set
     */
    fun addPurchase(itemId: ShopItemId): ShopState = copy(
        purchasedItems = purchasedItems + itemId
    )
    
    /**
     * Update equipped cosmetics
     */
    fun updateEquipped(newEquipped: EquippedCosmetics): ShopState = copy(
        equippedCosmetics = newEquipped
    )
    
    /**
     * Update rotation timestamps
     */
    fun updateRotations(daily: Long? = null, weekly: Long? = null): ShopState = copy(
        lastDailyRotation = daily ?: lastDailyRotation,
        lastWeeklyRotation = weekly ?: lastWeeklyRotation
    )
}
