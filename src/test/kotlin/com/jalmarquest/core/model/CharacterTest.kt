package com.jalmarquest.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CharacterTest {
    
    private fun createTestCharacter(): Character {
        val stats = Stats(
            health = 100,
            maxHealth = 100,
            strength = 15,
            defense = 10,
            agility = 12,
            intelligence = 8
        )
        return Character(
            name = "Test Hero",
            stats = stats,
            gold = 50
        )
    }
    
    private fun createTestItem(id: String = "item_01", type: ItemType = ItemType.WEAPON): Item {
        return Item(
            id = id,
            name = "Test Item",
            description = "A test item",
            type = type
        )
    }
    
    @Test
    fun testCharacterCreation() {
        val character = createTestCharacter()
        assertEquals("Test Hero", character.name)
        assertEquals(100, character.getCurrentHealth())
        assertEquals(100, character.getMaxHealth())
        assertEquals(50, character.gold)
        assertTrue(character.inventory.isEmpty())
        assertTrue(character.isAlive())
    }
    
    @Test
    fun testAddItem() {
        val character = createTestCharacter()
        val item = createTestItem()
        
        character.addItem(item)
        
        assertEquals(1, character.inventory.size)
        assertTrue(character.hasItem("item_01"))
    }
    
    @Test
    fun testRemoveItem() {
        val character = createTestCharacter()
        val item = createTestItem()
        
        character.addItem(item)
        val removed = character.removeItem(item)
        
        assertTrue(removed)
        assertEquals(0, character.inventory.size)
        assertFalse(character.hasItem("item_01"))
    }
    
    @Test
    fun testRemoveNonExistentItem() {
        val character = createTestCharacter()
        val item = createTestItem()
        
        val removed = character.removeItem(item)
        
        assertFalse(removed)
    }
    
    @Test
    fun testGetItemById() {
        val character = createTestCharacter()
        val item = createTestItem(id = "sword_01")
        
        character.addItem(item)
        val foundItem = character.getItemById("sword_01")
        
        assertEquals(item, foundItem)
    }
    
    @Test
    fun testGetItemByIdNotFound() {
        val character = createTestCharacter()
        
        val foundItem = character.getItemById("nonexistent")
        
        assertNull(foundItem)
    }
    
    @Test
    fun testEquipItem() {
        val character = createTestCharacter()
        val weapon = createTestItem(id = "sword_01", type = ItemType.WEAPON)
        
        character.addItem(weapon)
        val equipped = character.equipItem(weapon, Slot.WEAPON)
        
        assertTrue(equipped)
        assertEquals(weapon, character.equippedItems[Slot.WEAPON])
        assertTrue(character.isItemEquipped("sword_01"))
    }
    
    @Test
    fun testEquipItemNotInInventory() {
        val character = createTestCharacter()
        val weapon = createTestItem(id = "sword_01", type = ItemType.WEAPON)
        
        val equipped = character.equipItem(weapon, Slot.WEAPON)
        
        assertFalse(equipped)
        assertNull(character.equippedItems[Slot.WEAPON])
    }
    
    @Test
    fun testUnequipItem() {
        val character = createTestCharacter()
        val weapon = createTestItem(id = "sword_01", type = ItemType.WEAPON)
        
        character.addItem(weapon)
        character.equipItem(weapon, Slot.WEAPON)
        
        val unequipped = character.unequipItem(Slot.WEAPON)
        
        assertEquals(weapon, unequipped)
        assertNull(character.equippedItems[Slot.WEAPON])
        assertFalse(character.isItemEquipped("sword_01"))
    }
    
    @Test
    fun testUnequipEmptySlot() {
        val character = createTestCharacter()
        
        val unequipped = character.unequipItem(Slot.WEAPON)
        
        assertNull(unequipped)
    }
    
    @Test
    fun testReplaceEquippedItem() {
        val character = createTestCharacter()
        val sword1 = createTestItem(id = "sword_01")
        val sword2 = createTestItem(id = "sword_02")
        
        character.addItem(sword1)
        character.addItem(sword2)
        
        character.equipItem(sword1, Slot.WEAPON)
        assertEquals(sword1, character.equippedItems[Slot.WEAPON])
        
        character.equipItem(sword2, Slot.WEAPON)
        assertEquals(sword2, character.equippedItems[Slot.WEAPON])
        assertTrue(character.hasItem("sword_01"))
        assertTrue(character.hasItem("sword_02"))
    }
    
    @Test
    fun testMultipleItemsInInventory() {
        val character = createTestCharacter()
        val sword = createTestItem(id = "sword_01", type = ItemType.WEAPON)
        val armor = createTestItem(id = "armor_01", type = ItemType.ARMOR)
        val potion = createTestItem(id = "potion_01", type = ItemType.CONSUMABLE)
        
        character.addItem(sword)
        character.addItem(armor)
        character.addItem(potion)
        
        assertEquals(3, character.inventory.size)
        assertTrue(character.hasItem("sword_01"))
        assertTrue(character.hasItem("armor_01"))
        assertTrue(character.hasItem("potion_01"))
    }
}
