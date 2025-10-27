package com.jalmarquest.core.state.equipment

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.crafting.CraftingManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * Manages equipment, calculates total stats from equipped items,
 * and integrates equipment bonuses with game systems.
 */
class EquipmentManager(
    private val craftingManager: CraftingManager,
    private val itemDefinitions: Map<CraftedItemId, CraftedItem> = emptyMap()
) {
    
    /**
     * Equips an item to a specific slot.
     * Delegates to CraftingManager for state management.
     */
    fun equipItem(itemId: CraftedItemId, slot: EquipmentSlot) {
        val item = itemDefinitions[itemId]
            ?: throw IllegalArgumentException("Item $itemId not found in definitions")
        
        // Validate item can be equipped in this slot
        if (item.equipmentSlot != slot) {
            throw IllegalArgumentException(
                "Item ${item.nameKey} cannot be equipped in slot $slot (requires ${item.equipmentSlot})"
            )
        }
        
        craftingManager.equipItem(itemId, slot)
    }

    /**
     * Unequips an item from a specific slot.
     */
    fun unequipSlot(slot: EquipmentSlot) {
        craftingManager.unequipSlot(slot)
    }

    /**
     * Gets all currently equipped items as a map of slot -> item definition.
     */
    fun getEquippedItems(): Map<EquipmentSlot, CraftedItem> {
        val equippedIds = craftingManager.getEquippedItems()
        return equippedIds.mapNotNull { (slot, itemId) ->
            itemDefinitions[itemId]?.let { slot to it }
        }.toMap()
    }

    /**
     * Gets the item equipped in a specific slot.
     */
    fun getEquippedItem(slot: EquipmentSlot): CraftedItem? {
        val itemId = craftingManager.getEquippedItem(slot) ?: return null
        return itemDefinitions[itemId]
    }

    /**
     * Calculates total stats from all equipped items.
     * Combines stats using EquipmentStats.plus() operator.
     */
    fun calculateTotalStats(): EquipmentStats {
        val equipped = getEquippedItems()
        if (equipped.isEmpty()) {
            return EquipmentStats() // Empty stats
        }
        
        return equipped.values
            .map { it.stats }
            .reduce { acc, stats -> acc + stats }
    }

    /**
     * Gets a specific stat value from total equipment.
     */
    fun getTotalDamage(): Int = calculateTotalStats().damage
    fun getTotalDefense(): Int = calculateTotalStats().defense
    fun getTotalHealth(): Int = calculateTotalStats().health
    fun getTotalHarvestSpeed(): Int = calculateTotalStats().harvestSpeed
    fun getTotalCraftingSuccess(): Int = calculateTotalStats().craftingSuccess
    fun getTotalSeedBonus(): Int = calculateTotalStats().seedBonus
    fun getTotalXPBonus(): Int = calculateTotalStats().xpBonus
    fun getTotalLuckBonus(): Int = calculateTotalStats().luckBonus
    fun getTotalMovementSpeed(): Int = calculateTotalStats().movementSpeed
    fun getTotalShopDiscount(): Int = calculateTotalStats().shopDiscount

    /**
     * Checks if any equipment is currently equipped.
     */
    fun hasAnyEquipment(): Boolean {
        return craftingManager.getEquippedItems().isNotEmpty()
    }

    /**
     * Gets the number of equipped items.
     */
    fun getEquippedItemCount(): Int {
        return craftingManager.getEquippedItems().size
    }

    /**
     * Checks if a specific slot is occupied.
     */
    fun isSlotOccupied(slot: EquipmentSlot): Boolean {
        return craftingManager.getEquippedItem(slot) != null
    }

    /**
     * Gets all empty equipment slots.
     */
    fun getEmptySlots(): List<EquipmentSlot> {
        return EquipmentSlot.entries.filter { !isSlotOccupied(it) }
    }

    /**
     * Gets all occupied equipment slots.
     */
    fun getOccupiedSlots(): List<EquipmentSlot> {
        return EquipmentSlot.entries.filter { isSlotOccupied(it) }
    }

    /**
     * Checks if an item is currently equipped.
     */
    fun isEquipped(itemId: CraftedItemId): Boolean {
        return craftingManager.isEquipped(itemId)
    }

    /**
     * Unequips all items.
     */
    fun unequipAll() {
        getOccupiedSlots().forEach { slot ->
            unequipSlot(slot)
        }
    }

    /**
     * Gets equipment stats as a StateFlow that updates when equipment changes.
     */
    fun observeTotalStats(): StateFlow<EquipmentStats> {
        return craftingManager.craftingKnowledgeState.map { knowledge ->
            val equippedIds = knowledge.equippedItems
            if (equippedIds.isEmpty()) {
                EquipmentStats()
            } else {
                equippedIds.values
                    .mapNotNull { itemId -> itemDefinitions[itemId]?.stats }
                    .reduceOrNull { acc, stats -> acc + stats }
                    ?: EquipmentStats()
            }
        } as StateFlow<EquipmentStats>
    }

    /**
     * Gets equipment by category (armor, weapons, tools).
     */
    fun getEquippedByCategory(category: ItemCategory): List<CraftedItem> {
        return getEquippedItems().values.filter { it.category == category }
    }

    /**
     * Gets all weapons currently equipped.
     */
    fun getEquippedWeapons(): List<CraftedItem> {
        return getEquippedByCategory(ItemCategory.WEAPON)
    }

    /**
     * Gets all armor currently equipped.
     */
    fun getEquippedArmor(): List<CraftedItem> {
        return getEquippedByCategory(ItemCategory.ARMOR)
    }

    /**
     * Gets all tools currently equipped.
     */
    fun getEquippedTools(): List<CraftedItem> {
        return listOf(
            ItemCategory.HARVESTING_TOOL,
            ItemCategory.CRAFTING_TOOL,
            ItemCategory.UTILITY_TOOL
        ).flatMap { getEquippedByCategory(it) }
    }

    /**
     * Gets all accessories currently equipped.
     */
    fun getEquippedAccessories(): List<CraftedItem> {
        return getEquippedByCategory(ItemCategory.ACCESSORY)
    }

    /**
     * Calculates total equipment value (sell value).
     */
    fun getTotalEquipmentValue(): Int {
        return getEquippedItems().values.sumOf { it.sellValue }
    }

    /**
     * Gets the highest rarity equipment currently equipped.
     */
    fun getHighestRarity(): CraftingRarity? {
        return getEquippedItems().values
            .map { it.rarity }
            .maxByOrNull { it.ordinal }
    }

    /**
     * Gets count of equipment by rarity.
     */
    fun getEquipmentCountByRarity(rarity: CraftingRarity): Int {
        return getEquippedItems().values.count { it.rarity == rarity }
    }

    /**
     * Checks if player has a full set of equipment (all 7 slots filled).
     */
    fun hasFullSet(): Boolean {
        return getEquippedItemCount() == 7
    }

    /**
     * Gets equipment durability summary.
     * Returns map of slot -> remaining durability percentage.
     */
    fun getDurabilitySummary(): Map<EquipmentSlot, Int?> {
        return getEquippedItems().mapValues { (_, item) ->
            item.durability
        }
    }

    /**
     * Checks if any equipped item has low durability (< 20%).
     */
    fun hasLowDurabilityItems(threshold: Int = 20): Boolean {
        return getEquippedItems().values.any { item ->
            item.durability?.let { it < threshold } ?: false
        }
    }

    /**
     * Gets summary of equipment stats for display.
     */
    fun getEquipmentSummary(): EquipmentSummary {
        val stats = calculateTotalStats()
        return EquipmentSummary(
            totalStats = stats,
            equippedCount = getEquippedItemCount(),
            totalValue = getTotalEquipmentValue(),
            highestRarity = getHighestRarity(),
            hasFullSet = hasFullSet()
        )
    }
}

/**
 * Summary of player's current equipment.
 */
data class EquipmentSummary(
    val totalStats: EquipmentStats,
    val equippedCount: Int,
    val totalValue: Int,
    val highestRarity: CraftingRarity?,
    val hasFullSet: Boolean
)
