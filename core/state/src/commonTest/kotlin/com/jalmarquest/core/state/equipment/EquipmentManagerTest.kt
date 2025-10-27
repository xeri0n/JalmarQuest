package com.jalmarquest.core.state.equipment

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.crafting.CraftingManager
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class EquipmentManagerTest {

    private fun createTestItem(
        id: CraftedItemId,
        category: ItemCategory = ItemCategory.ARMOR,
        slot: EquipmentSlot = EquipmentSlot.HEAD,
        stats: EquipmentStats = EquipmentStats(defense = 10),
        durability: Int? = 100,
        sellValue: Int = 50,
        rarity: CraftingRarity = CraftingRarity.COMMON
    ): CraftedItem {
        return CraftedItem(
            id = id,
            nameKey = "item.${id.value}.name",
            descriptionKey = "item.${id.value}.description",
            category = category,
            equipmentSlot = slot,
            stats = stats,
            durability = durability,
            stackable = false,
            sellValue = sellValue,
            rarity = rarity
        )
    }

    @Test
    fun testEquipItem() = runTest {
        val helmet = createTestItem(
            id = CraftedItemId("helmet"),
            slot = EquipmentSlot.HEAD,
            stats = EquipmentStats(defense = 10)
        )
        
        val craftingManager = CraftingManager()
        craftingManager.addCraftedItems(helmet.id, 1)
        
        val equipmentManager = EquipmentManager(
            craftingManager = craftingManager,
            itemDefinitions = mapOf(helmet.id to helmet)
        )

        equipmentManager.equipItem(helmet.id, EquipmentSlot.HEAD)

        val equipped = equipmentManager.getEquippedItem(EquipmentSlot.HEAD)
        assertNotNull(equipped)
        assertEquals(helmet.id, equipped.id)
    }

    @Test
    fun testEquipItemToWrongSlotFails() = runTest {
        val helmet = createTestItem(
            id = CraftedItemId("helmet"),
            slot = EquipmentSlot.HEAD
        )
        
        val craftingManager = CraftingManager()
        craftingManager.addCraftedItems(helmet.id, 1)
        
        val equipmentManager = EquipmentManager(
            craftingManager = craftingManager,
            itemDefinitions = mapOf(helmet.id to helmet)
        )

        assertFailsWith<IllegalArgumentException> {
            equipmentManager.equipItem(helmet.id, EquipmentSlot.BODY)
        }
    }

    @Test
    fun testUnequipSlot() = runTest {
        val helmet = createTestItem(
            id = CraftedItemId("helmet"),
            slot = EquipmentSlot.HEAD
        )
        
        val craftingManager = CraftingManager()
        craftingManager.addCraftedItems(helmet.id, 1)
        
        val equipmentManager = EquipmentManager(
            craftingManager = craftingManager,
            itemDefinitions = mapOf(helmet.id to helmet)
        )

        equipmentManager.equipItem(helmet.id, EquipmentSlot.HEAD)
        assertTrue(equipmentManager.isSlotOccupied(EquipmentSlot.HEAD))

        equipmentManager.unequipSlot(EquipmentSlot.HEAD)
        assertFalse(equipmentManager.isSlotOccupied(EquipmentSlot.HEAD))
        assertNull(equipmentManager.getEquippedItem(EquipmentSlot.HEAD))
    }

    @Test
    fun testCalculateTotalStats() = runTest {
        val helmet = createTestItem(
            id = CraftedItemId("helmet"),
            slot = EquipmentSlot.HEAD,
            stats = EquipmentStats(defense = 10, health = 5)
        )
        val armor = createTestItem(
            id = CraftedItemId("armor"),
            slot = EquipmentSlot.BODY,
            stats = EquipmentStats(defense = 25, health = 10)
        )
        
        val craftingManager = CraftingManager()
        craftingManager.addCraftedItems(helmet.id, 1)
        craftingManager.addCraftedItems(armor.id, 1)
        
        val equipmentManager = EquipmentManager(
            craftingManager = craftingManager,
            itemDefinitions = mapOf(helmet.id to helmet, armor.id to armor)
        )

        equipmentManager.equipItem(helmet.id, EquipmentSlot.HEAD)
        equipmentManager.equipItem(armor.id, EquipmentSlot.BODY)

        val totalStats = equipmentManager.calculateTotalStats()
        assertEquals(35, totalStats.defense)
        assertEquals(15, totalStats.health)
    }

    @Test
    fun testCalculateTotalStatsWithNoEquipment() = runTest {
        val craftingManager = CraftingManager()
        val equipmentManager = EquipmentManager(craftingManager)

        val totalStats = equipmentManager.calculateTotalStats()
        
        assertEquals(0, totalStats.defense)
        assertEquals(0, totalStats.damage)
        assertFalse(totalStats.hasAnyBonus())
    }

    @Test
    fun testGetTotalDamage() = runTest {
        val sword = createTestItem(
            id = CraftedItemId("sword"),
            category = ItemCategory.WEAPON,
            slot = EquipmentSlot.TOOL_MAIN,
            stats = EquipmentStats(damage = 20)
        )
        
        val craftingManager = CraftingManager()
        craftingManager.addCraftedItems(sword.id, 1)
        
        val equipmentManager = EquipmentManager(
            craftingManager = craftingManager,
            itemDefinitions = mapOf(sword.id to sword)
        )

        equipmentManager.equipItem(sword.id, EquipmentSlot.TOOL_MAIN)

        assertEquals(20, equipmentManager.getTotalDamage())
    }

    @Test
    fun testGetEquippedItems() = runTest {
        val helmet = createTestItem(
            id = CraftedItemId("helmet"),
            slot = EquipmentSlot.HEAD
        )
        val armor = createTestItem(
            id = CraftedItemId("armor"),
            slot = EquipmentSlot.BODY
        )
        
        val craftingManager = CraftingManager()
        craftingManager.addCraftedItems(helmet.id, 1)
        craftingManager.addCraftedItems(armor.id, 1)
        
        val equipmentManager = EquipmentManager(
            craftingManager = craftingManager,
            itemDefinitions = mapOf(helmet.id to helmet, armor.id to armor)
        )

        equipmentManager.equipItem(helmet.id, EquipmentSlot.HEAD)
        equipmentManager.equipItem(armor.id, EquipmentSlot.BODY)

        val equipped = equipmentManager.getEquippedItems()
        assertEquals(2, equipped.size)
        assertEquals(helmet.id, equipped[EquipmentSlot.HEAD]?.id)
        assertEquals(armor.id, equipped[EquipmentSlot.BODY]?.id)
    }

    @Test
    fun testHasAnyEquipment() = runTest {
        val helmet = createTestItem(
            id = CraftedItemId("helmet"),
            slot = EquipmentSlot.HEAD
        )
        
        val craftingManager = CraftingManager()
        craftingManager.addCraftedItems(helmet.id, 1)
        
        val equipmentManager = EquipmentManager(
            craftingManager = craftingManager,
            itemDefinitions = mapOf(helmet.id to helmet)
        )

        assertFalse(equipmentManager.hasAnyEquipment())

        equipmentManager.equipItem(helmet.id, EquipmentSlot.HEAD)
        assertTrue(equipmentManager.hasAnyEquipment())
    }

    @Test
    fun testGetEmptySlots() = runTest {
        val helmet = createTestItem(
            id = CraftedItemId("helmet"),
            slot = EquipmentSlot.HEAD
        )
        
        val craftingManager = CraftingManager()
        craftingManager.addCraftedItems(helmet.id, 1)
        
        val equipmentManager = EquipmentManager(
            craftingManager = craftingManager,
            itemDefinitions = mapOf(helmet.id to helmet)
        )

        assertEquals(7, equipmentManager.getEmptySlots().size)

        equipmentManager.equipItem(helmet.id, EquipmentSlot.HEAD)
        val emptySlots = equipmentManager.getEmptySlots()
        assertEquals(6, emptySlots.size)
        assertFalse(emptySlots.contains(EquipmentSlot.HEAD))
    }

    @Test
    fun testGetOccupiedSlots() = runTest {
        val helmet = createTestItem(
            id = CraftedItemId("helmet"),
            slot = EquipmentSlot.HEAD
        )
        val armor = createTestItem(
            id = CraftedItemId("armor"),
            slot = EquipmentSlot.BODY
        )
        
        val craftingManager = CraftingManager()
        craftingManager.addCraftedItems(helmet.id, 1)
        craftingManager.addCraftedItems(armor.id, 1)
        
        val equipmentManager = EquipmentManager(
            craftingManager = craftingManager,
            itemDefinitions = mapOf(helmet.id to helmet, armor.id to armor)
        )

        equipmentManager.equipItem(helmet.id, EquipmentSlot.HEAD)
        equipmentManager.equipItem(armor.id, EquipmentSlot.BODY)

        val occupied = equipmentManager.getOccupiedSlots()
        assertEquals(2, occupied.size)
        assertTrue(occupied.contains(EquipmentSlot.HEAD))
        assertTrue(occupied.contains(EquipmentSlot.BODY))
    }

    @Test
    fun testUnequipAll() = runTest {
        val helmet = createTestItem(
            id = CraftedItemId("helmet"),
            slot = EquipmentSlot.HEAD
        )
        val armor = createTestItem(
            id = CraftedItemId("armor"),
            slot = EquipmentSlot.BODY
        )
        
        val craftingManager = CraftingManager()
        craftingManager.addCraftedItems(helmet.id, 1)
        craftingManager.addCraftedItems(armor.id, 1)
        
        val equipmentManager = EquipmentManager(
            craftingManager = craftingManager,
            itemDefinitions = mapOf(helmet.id to helmet, armor.id to armor)
        )

        equipmentManager.equipItem(helmet.id, EquipmentSlot.HEAD)
        equipmentManager.equipItem(armor.id, EquipmentSlot.BODY)
        assertEquals(2, equipmentManager.getEquippedItemCount())

        equipmentManager.unequipAll()
        assertEquals(0, equipmentManager.getEquippedItemCount())
    }

    @Test
    fun testGetEquippedByCategory() = runTest {
        val sword = createTestItem(
            id = CraftedItemId("sword"),
            category = ItemCategory.WEAPON,
            slot = EquipmentSlot.TOOL_MAIN
        )
        val helmet = createTestItem(
            id = CraftedItemId("helmet"),
            category = ItemCategory.ARMOR,
            slot = EquipmentSlot.HEAD
        )
        
        val craftingManager = CraftingManager()
        craftingManager.addCraftedItems(sword.id, 1)
        craftingManager.addCraftedItems(helmet.id, 1)
        
        val equipmentManager = EquipmentManager(
            craftingManager = craftingManager,
            itemDefinitions = mapOf(sword.id to sword, helmet.id to helmet)
        )

        equipmentManager.equipItem(sword.id, EquipmentSlot.TOOL_MAIN)
        equipmentManager.equipItem(helmet.id, EquipmentSlot.HEAD)

        val weapons = equipmentManager.getEquippedWeapons()
        val armor = equipmentManager.getEquippedArmor()

        assertEquals(1, weapons.size)
        assertEquals(sword.id, weapons[0].id)
        assertEquals(1, armor.size)
        assertEquals(helmet.id, armor[0].id)
    }

    @Test
    fun testGetTotalEquipmentValue() = runTest {
        val helmet = createTestItem(
            id = CraftedItemId("helmet"),
            slot = EquipmentSlot.HEAD,
            sellValue = 50
        )
        val armor = createTestItem(
            id = CraftedItemId("armor"),
            slot = EquipmentSlot.BODY,
            sellValue = 100
        )
        
        val craftingManager = CraftingManager()
        craftingManager.addCraftedItems(helmet.id, 1)
        craftingManager.addCraftedItems(armor.id, 1)
        
        val equipmentManager = EquipmentManager(
            craftingManager = craftingManager,
            itemDefinitions = mapOf(helmet.id to helmet, armor.id to armor)
        )

        equipmentManager.equipItem(helmet.id, EquipmentSlot.HEAD)
        equipmentManager.equipItem(armor.id, EquipmentSlot.BODY)

        assertEquals(150, equipmentManager.getTotalEquipmentValue())
    }

    @Test
    fun testGetHighestRarity() = runTest {
        val commonHelmet = createTestItem(
            id = CraftedItemId("helmet"),
            slot = EquipmentSlot.HEAD,
            rarity = CraftingRarity.COMMON
        )
        val rareArmor = createTestItem(
            id = CraftedItemId("armor"),
            slot = EquipmentSlot.BODY,
            rarity = CraftingRarity.RARE
        )
        
        val craftingManager = CraftingManager()
        craftingManager.addCraftedItems(commonHelmet.id, 1)
        craftingManager.addCraftedItems(rareArmor.id, 1)
        
        val equipmentManager = EquipmentManager(
            craftingManager = craftingManager,
            itemDefinitions = mapOf(commonHelmet.id to commonHelmet, rareArmor.id to rareArmor)
        )

        equipmentManager.equipItem(commonHelmet.id, EquipmentSlot.HEAD)
        equipmentManager.equipItem(rareArmor.id, EquipmentSlot.BODY)

        assertEquals(CraftingRarity.RARE, equipmentManager.getHighestRarity())
    }

    @Test
    fun testGetEquipmentCountByRarity() = runTest {
        val commonHelmet = createTestItem(
            id = CraftedItemId("helmet"),
            slot = EquipmentSlot.HEAD,
            rarity = CraftingRarity.COMMON
        )
        val commonArmor = createTestItem(
            id = CraftedItemId("armor"),
            slot = EquipmentSlot.BODY,
            rarity = CraftingRarity.COMMON
        )
        val rareBoots = createTestItem(
            id = CraftedItemId("boots"),
            slot = EquipmentSlot.TALONS,
            rarity = CraftingRarity.RARE
        )
        
        val craftingManager = CraftingManager()
        craftingManager.addCraftedItems(commonHelmet.id, 1)
        craftingManager.addCraftedItems(commonArmor.id, 1)
        craftingManager.addCraftedItems(rareBoots.id, 1)
        
        val equipmentManager = EquipmentManager(
            craftingManager = craftingManager,
            itemDefinitions = mapOf(
                commonHelmet.id to commonHelmet,
                commonArmor.id to commonArmor,
                rareBoots.id to rareBoots
            )
        )

        equipmentManager.equipItem(commonHelmet.id, EquipmentSlot.HEAD)
        equipmentManager.equipItem(commonArmor.id, EquipmentSlot.BODY)
        equipmentManager.equipItem(rareBoots.id, EquipmentSlot.TALONS)

        assertEquals(2, equipmentManager.getEquipmentCountByRarity(CraftingRarity.COMMON))
        assertEquals(1, equipmentManager.getEquipmentCountByRarity(CraftingRarity.RARE))
    }

    @Test
    fun testHasFullSet() = runTest {
        val craftingManager = CraftingManager()
        val itemDefs = mutableMapOf<CraftedItemId, CraftedItem>()
        
        // Create all 7 equipment pieces
        val slots = listOf(
            EquipmentSlot.HEAD,
            EquipmentSlot.BODY,
            EquipmentSlot.TALONS,
            EquipmentSlot.ACCESSORY_1,
            EquipmentSlot.ACCESSORY_2,
            EquipmentSlot.TOOL_MAIN,
            EquipmentSlot.TOOL_OFF
        )
        
        slots.forEach { slot ->
            val item = createTestItem(
                id = CraftedItemId("item_${slot.name.lowercase()}"),
                slot = slot
            )
            craftingManager.addCraftedItems(item.id, 1)
            itemDefs[item.id] = item
        }
        
        val equipmentManager = EquipmentManager(
            craftingManager = craftingManager,
            itemDefinitions = itemDefs
        )

        assertFalse(equipmentManager.hasFullSet())

        // Equip all items
        itemDefs.values.forEach { item ->
            equipmentManager.equipItem(item.id, item.equipmentSlot!!)
        }

        assertTrue(equipmentManager.hasFullSet())
    }

    @Test
    fun testGetDurabilitySummary() = runTest {
        val helmet = createTestItem(
            id = CraftedItemId("helmet"),
            slot = EquipmentSlot.HEAD,
            durability = 80
        )
        val armor = createTestItem(
            id = CraftedItemId("armor"),
            slot = EquipmentSlot.BODY,
            durability = 50
        )
        
        val craftingManager = CraftingManager()
        craftingManager.addCraftedItems(helmet.id, 1)
        craftingManager.addCraftedItems(armor.id, 1)
        
        val equipmentManager = EquipmentManager(
            craftingManager = craftingManager,
            itemDefinitions = mapOf(helmet.id to helmet, armor.id to armor)
        )

        equipmentManager.equipItem(helmet.id, EquipmentSlot.HEAD)
        equipmentManager.equipItem(armor.id, EquipmentSlot.BODY)

        val durability = equipmentManager.getDurabilitySummary()
        assertEquals(80, durability[EquipmentSlot.HEAD])
        assertEquals(50, durability[EquipmentSlot.BODY])
    }

    @Test
    fun testHasLowDurabilityItems() = runTest {
        val helmet = createTestItem(
            id = CraftedItemId("helmet"),
            slot = EquipmentSlot.HEAD,
            durability = 15 // Low durability
        )
        val armor = createTestItem(
            id = CraftedItemId("armor"),
            slot = EquipmentSlot.BODY,
            durability = 80
        )
        
        val craftingManager = CraftingManager()
        craftingManager.addCraftedItems(helmet.id, 1)
        craftingManager.addCraftedItems(armor.id, 1)
        
        val equipmentManager = EquipmentManager(
            craftingManager = craftingManager,
            itemDefinitions = mapOf(helmet.id to helmet, armor.id to armor)
        )

        equipmentManager.equipItem(helmet.id, EquipmentSlot.HEAD)
        equipmentManager.equipItem(armor.id, EquipmentSlot.BODY)

        assertTrue(equipmentManager.hasLowDurabilityItems(threshold = 20))
    }

    @Test
    fun testGetEquipmentSummary() = runTest {
        val helmet = createTestItem(
            id = CraftedItemId("helmet"),
            slot = EquipmentSlot.HEAD,
            stats = EquipmentStats(defense = 10),
            sellValue = 50,
            rarity = CraftingRarity.COMMON
        )
        val rareArmor = createTestItem(
            id = CraftedItemId("armor"),
            slot = EquipmentSlot.BODY,
            stats = EquipmentStats(defense = 25),
            sellValue = 150,
            rarity = CraftingRarity.RARE
        )
        
        val craftingManager = CraftingManager()
        craftingManager.addCraftedItems(helmet.id, 1)
        craftingManager.addCraftedItems(rareArmor.id, 1)
        
        val equipmentManager = EquipmentManager(
            craftingManager = craftingManager,
            itemDefinitions = mapOf(helmet.id to helmet, rareArmor.id to rareArmor)
        )

        equipmentManager.equipItem(helmet.id, EquipmentSlot.HEAD)
        equipmentManager.equipItem(rareArmor.id, EquipmentSlot.BODY)

        val summary = equipmentManager.getEquipmentSummary()
        assertEquals(35, summary.totalStats.defense)
        assertEquals(2, summary.equippedCount)
        assertEquals(200, summary.totalValue)
        assertEquals(CraftingRarity.RARE, summary.highestRarity)
        assertFalse(summary.hasFullSet)
    }
}
