package com.jalmarquest.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Crafting system for non-consumable items: equipment, tools, and nest upgrades.
 * Separate from Concoctions (consumable potions).
 */

@Serializable
@JvmInline
value class CraftingRecipeId(val value: String)

@Serializable
@JvmInline
value class CraftedItemId(val value: String)

/**
 * Types of crafting recipes.
 */
@Serializable
enum class CraftingRecipeType {
    EQUIPMENT,      // Wearable items that provide stat bonuses
    TOOL,           // Harvest/crafting/utility tools
    NEST_UPGRADE,   // Improvements to the player's nest
    CONSUMABLE,     // One-time use items (different from concoctions)
    FURNITURE,      // Decorative or functional nest furniture
    MATERIAL,       // Refined materials for further crafting
    REFINEMENT,     // Alpha 2.3: Reagent-to-reagent refinement recipes
    WEAPON,         // Alpha 2.3: Weapons (formerly part of EQUIPMENT)
    ARMOR,          // Alpha 2.3: Armor (formerly part of EQUIPMENT)
    TRADE_GOOD      // Alpha 2.3: Items specifically for selling/bartering
}

/**
 * Categories for crafted items.
 */
@Serializable
enum class ItemCategory {
    // Equipment categories
    ARMOR,          // Defensive gear
    WEAPON,         // Offensive gear
    ACCESSORY,      // Stat-boosting trinkets
    
    // Tool categories
    HARVESTING_TOOL,    // Better ingredient gathering
    CRAFTING_TOOL,      // Improved crafting success
    UTILITY_TOOL,       // Lockpicks, magnifying glasses, etc.
    
    // Nest categories
    STORAGE,        // Increase storage capacity
    PRODUCTION,     // Passive seed/resource generation
    COMFORT,        // Critter happiness bonuses
    DEFENSE,        // Nest defense improvements
    
    // Other
    CONSUMABLE,     // One-time use items
    MATERIAL,       // Crafting components
    FURNITURE       // Decorative items
}

/**
 * Equipment slots for wearable items.
 */
@Serializable
enum class EquipmentSlot {
    HEAD,           // Helmets, crowns, masks
    BODY,           // Armor, cloaks, vests
    TALONS,         // Talon wraps, boots, claws
    ACCESSORY_1,    // First accessory slot
    ACCESSORY_2,    // Second accessory slot
    TOOL_MAIN,      // Main tool slot
    TOOL_OFF        // Off-hand tool slot
}

/**
 * Crafting stations required for recipes.
 */
@Serializable
enum class CraftingStation {
    NONE,               // Can craft anywhere (basic recipes)
    WORKBENCH,          // Basic crafting bench
    FORGE,              // For metalwork and weapons
    ALCHEMY_LAB,        // For concoctions (already exists)
    SEWING_TABLE,       // For cloth/feather items
    CARPENTRY_BENCH,    // For wooden items and furniture
    ENCHANTING_TABLE,   // For magical enhancements
    NEST_WORKSHOP       // For nest upgrades
}

/**
 * Stat bonuses provided by equipment.
 */
@Serializable
data class EquipmentStats(
    val damage: Int = 0,                    // Physical damage bonus
    val defense: Int = 0,                   // Physical defense bonus
    val health: Int = 0,                    // Max health increase
    val harvestSpeed: Int = 0,              // % faster harvesting
    val craftingSuccess: Int = 0,           // % better crafting success
    val seedBonus: Int = 0,                 // % more seeds from all sources
    val xpBonus: Int = 0,                   // % more XP from all sources
    val luckBonus: Int = 0,                 // % better luck (rare drops, etc.)
    val movementSpeed: Int = 0,             // % faster movement
    val shopDiscount: Int = 0               // % shop discount
) {
    /**
     * Combine stats from multiple equipment pieces.
     */
    operator fun plus(other: EquipmentStats): EquipmentStats = EquipmentStats(
        damage = damage + other.damage,
        defense = defense + other.defense,
        health = health + other.health,
        harvestSpeed = harvestSpeed + other.harvestSpeed,
        craftingSuccess = craftingSuccess + other.craftingSuccess,
        seedBonus = seedBonus + other.seedBonus,
        xpBonus = xpBonus + other.xpBonus,
        luckBonus = luckBonus + other.luckBonus,
        movementSpeed = movementSpeed + other.movementSpeed,
        shopDiscount = shopDiscount + other.shopDiscount
    )
    
    /**
     * Check if this equipment provides any bonuses.
     */
    fun hasAnyBonus(): Boolean =
        damage > 0 || defense > 0 || health > 0 || harvestSpeed > 0 || 
        craftingSuccess > 0 || seedBonus > 0 || xpBonus > 0 || luckBonus > 0 ||
        movementSpeed > 0 || shopDiscount > 0
}

/**
 * A crafted item that can be equipped, used, or placed in the nest.
 */
@Serializable
data class CraftedItem(
    val id: CraftedItemId,
    val nameKey: String,                    // Localization key for name
    val descriptionKey: String,             // Localization key for lore description
    val category: ItemCategory,
    val equipmentSlot: EquipmentSlot? = null,   // If equipment, which slot
    val stats: EquipmentStats = EquipmentStats(), // Stat bonuses if equipment
    val durability: Int? = null,            // Max durability (null = infinite)
    val stackable: Boolean = false,         // Can multiple be owned
    val sellValue: Int = 0,                 // Seeds value when selling
    val rarity: CraftingRarity = CraftingRarity.COMMON,
    
    // Alpha 2.3: Additional fields for new crafting system
    val itemType: CraftedItemType? = null,  // More specific item type
    val maxStackSize: Int = 1,              // Max stack size if stackable
    val bonuses: ItemBonuses? = null,       // Simplified bonuses for non-equipment items
    val effect: ConsumableEffect? = null,   // Effect when consumed (for consumables)
    val effectPower: Int = 0                // Strength of the effect
)

/**
 * Rarity tiers for crafted items.
 */
@Serializable
enum class CraftingRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY,
    MYTHIC
}

/**
 * A recipe for crafting an item.
 */
@Serializable
data class CraftingRecipe(
    val id: CraftingRecipeId,
    val nameKey: String,                        // Localization key
    val descriptionKey: String,                 // Localization key for recipe lore
    val type: CraftingRecipeType,
    val requiredStation: CraftingStation,
    val requiredIngredients: Map<IngredientId, Int> = emptyMap(), // Ingredients needed
    val requiredItems: Map<ItemId, Int> = emptyMap(),  // Regular items needed
    val requiredSkills: Map<SkillId, Int> = emptyMap(), // Skill level requirements
    val craftingTime: Int = 0,                  // Seconds to craft (0 = instant)
    val resultItem: CraftedItem,                // What you get when crafting
    val resultQuantity: Int = 1,                // How many you get
    val skillXPReward: Map<SkillId, Int> = emptyMap(), // XP gained when crafting
    val baseSuccessRate: Int = 100,             // % chance to succeed (before skill bonuses)
    val discoveryMethod: CraftingDiscoveryMethod = CraftingDiscoveryMethod.UNKNOWN
)

/**
 * How a crafting recipe can be discovered.
 */
@Serializable
enum class CraftingDiscoveryMethod {
    UNKNOWN,            // Not yet discovered
    STARTER,            // Known from the beginning
    MILESTONE,          // Unlocked via gameplay milestone
    SKILL_LEVEL,        // Unlocked by reaching skill level
    EXPERIMENTATION,    // Discovered through random crafting
    PURCHASE,           // Bought from NPC or shop
    QUEST_REWARD,       // Received as quest reward
    THOUGHT_UNLOCK,     // Unlocked by internalizing a thought
    COMPANION_TAUGHT    // Taught by a companion
}

/**
 * Player's crafting knowledge and crafted items.
 */
@Serializable
data class CraftingKnowledge(
    @SerialName("known_recipes")
    val knownRecipes: Set<CraftingRecipeId> = emptySet(),
    @SerialName("crafted_items")
    val craftedItems: Map<CraftedItemId, Int> = emptyMap(), // Item ID -> quantity owned
    @SerialName("equipped_items")
    val equippedItems: Map<EquipmentSlot, CraftedItemId> = emptyMap(),
    @SerialName("last_craft_at")
    val lastCraftAt: Long = 0 // Timestamp of last craft (for cooldowns)
) {
    /**
     * Check if a recipe is known.
     */
    fun knowsRecipe(recipeId: CraftingRecipeId): Boolean =
        knownRecipes.contains(recipeId)
    
    /**
     * Learn a new recipe.
     */
    fun learnRecipe(recipeId: CraftingRecipeId): CraftingKnowledge =
        copy(knownRecipes = knownRecipes + recipeId)
    
    /**
     * Get quantity of a crafted item.
     */
    fun getItemQuantity(itemId: CraftedItemId): Int =
        craftedItems[itemId] ?: 0
    
    /**
     * Check if item is owned.
     */
    fun hasItem(itemId: CraftedItemId): Boolean =
        getItemQuantity(itemId) > 0
    
    /**
     * Add crafted items to inventory.
     */
    fun addCraftedItems(itemId: CraftedItemId, quantity: Int): CraftingKnowledge {
        val current = getItemQuantity(itemId)
        return copy(craftedItems = craftedItems + (itemId to current + quantity))
    }
    
    /**
     * Remove crafted items from inventory.
     */
    fun removeCraftedItems(itemId: CraftedItemId, quantity: Int): CraftingKnowledge {
        val current = getItemQuantity(itemId)
        require(current >= quantity) { "Not enough items to remove" }
        val newQuantity = current - quantity
        return if (newQuantity == 0) {
            copy(craftedItems = craftedItems - itemId)
        } else {
            copy(craftedItems = craftedItems + (itemId to newQuantity))
        }
    }
    
    /**
     * Equip an item to a slot.
     */
    fun equipItem(slot: EquipmentSlot, itemId: CraftedItemId): CraftingKnowledge {
        require(hasItem(itemId)) { "Cannot equip item that is not owned" }
        return copy(equippedItems = equippedItems + (slot to itemId))
    }
    
    /**
     * Unequip an item from a slot.
     */
    fun unequipSlot(slot: EquipmentSlot): CraftingKnowledge =
        copy(equippedItems = equippedItems - slot)
    
    /**
     * Get equipped item in a slot.
     */
    fun getEquippedItem(slot: EquipmentSlot): CraftedItemId? =
        equippedItems[slot]
    
    /**
     * Check if an item is equipped.
     */
    fun isEquipped(itemId: CraftedItemId): Boolean =
        equippedItems.values.contains(itemId)
    
    /**
     * Update last craft timestamp.
     */
    fun updateCraftTimestamp(timestamp: Long): CraftingKnowledge =
        copy(lastCraftAt = timestamp)
}

/**
 * Alpha 2.3: More specific item types for the new crafting system.
 */
@Serializable
enum class CraftedItemType {
    INGREDIENT,     // Refined ingredient for further crafting
    WEAPON,         // Offensive equipment
    ARMOR,          // Defensive equipment
    TOOL,           // Utility equipment
    CONSUMABLE,     // One-time use item
    MATERIAL,       // Crafting component
    TRADE_GOOD,     // Item for selling/bartering
    FURNITURE       // Decorative/functional furniture
}

/**
 * Alpha 2.3: Simplified item bonuses for non-equipment items.
 */
@Serializable
data class ItemBonuses(
    val attack: Int = 0,        // Attack damage bonus
    val defense: Int = 0,       // Defense bonus
    val foraging: Int = 0,      // Foraging yield bonus
    val hoarding: Int = 0,      // Hoarding/shiny bonus
    val alchemy: Int = 0,       // Alchemy success bonus
    val scholarship: Int = 0    // Scholarship/research bonus
)

/**
 * Alpha 2.3: Effects that consumable items can apply.
 */
@Serializable
enum class ConsumableEffect {
    RESTORE_HEALTH,     // Restores HP
    RESTORE_STAMINA,    // Restores stamina/energy
    APPLY_POISON,       // Applies poison damage over time
    APPLY_BUFF,         // Temporary stat boost
    APPLY_DEBUFF,       // Temporary stat reduction
    ENHANCE_SHINY,      // Increases shiny item value
    REVEAL_SECRETS,     // Reveals hidden information
    GRANT_VISION        // Temporary enhanced vision/detection
}
