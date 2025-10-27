package com.jalmarquest.core.model

import kotlin.test.*

/**
 * Tests for Crafting system data models.
 */
class CraftingTest {
    
    @Test
    fun testEquipmentStatsCombination() {
        val helmet = EquipmentStats(
            defense = 10,
            health = 50,
            craftingSuccess = 5
        )
        
        val armor = EquipmentStats(
            defense = 25,
            health = 100,
            movementSpeed = -10
        )
        
        val combined = helmet + armor
        
        assertEquals(35, combined.defense)
        assertEquals(150, combined.health)
        assertEquals(5, combined.craftingSuccess)
        assertEquals(-10, combined.movementSpeed)
        assertEquals(0, combined.damage) // Not present in either
    }
    
    @Test
    fun testEquipmentStatsHasAnyBonus() {
        val empty = EquipmentStats()
        assertFalse(empty.hasAnyBonus())
        
        val withDamage = EquipmentStats(damage = 5)
        assertTrue(withDamage.hasAnyBonus())
        
        val withDefense = EquipmentStats(defense = 10)
        assertTrue(withDefense.hasAnyBonus())
        
        val withMultiple = EquipmentStats(health = 50, xpBonus = 10)
        assertTrue(withMultiple.hasAnyBonus())
    }
    
    @Test
    fun testCraftedItemCreation() {
        val beakGuard = CraftedItem(
            id = CraftedItemId("beak_guard_iron"),
            nameKey = "item_beak_guard_iron",
            descriptionKey = "item_beak_guard_iron_desc",
            category = ItemCategory.ARMOR,
            equipmentSlot = EquipmentSlot.HEAD,
            stats = EquipmentStats(defense = 15, health = 25),
            durability = 100,
            sellValue = 500,
            rarity = CraftingRarity.UNCOMMON
        )
        
        assertEquals(EquipmentSlot.HEAD, beakGuard.equipmentSlot)
        assertEquals(15, beakGuard.stats.defense)
        assertEquals(100, beakGuard.durability)
        assertEquals(CraftingRarity.UNCOMMON, beakGuard.rarity)
    }
    
    @Test
    fun testCraftingRecipeWithMultipleRequirements() {
        val recipe = CraftingRecipe(
            id = CraftingRecipeId("iron_talon_wraps"),
            nameKey = "recipe_iron_talon_wraps",
            descriptionKey = "recipe_iron_talon_wraps_desc",
            type = CraftingRecipeType.EQUIPMENT,
            requiredStation = CraftingStation.FORGE,
            requiredIngredients = mapOf(
                IngredientId("iron_ore") to 5,
                IngredientId("leather_scraps") to 3
            ),
            requiredItems = mapOf(
                ItemId("seeds") to 100
            ),
            requiredSkills = mapOf(
                SkillId("combat") to 3
            ),
            craftingTime = 300, // 5 minutes
            resultItem = CraftedItem(
                id = CraftedItemId("iron_talon_wraps"),
                nameKey = "item_iron_talon_wraps",
                descriptionKey = "item_iron_talon_wraps_desc",
                category = ItemCategory.ARMOR,
                equipmentSlot = EquipmentSlot.TALONS,
                stats = EquipmentStats(damage = 10, defense = 5)
            ),
            skillXPReward = mapOf(SkillId("combat") to 50),
            baseSuccessRate = 85,
            discoveryMethod = CraftingDiscoveryMethod.SKILL_LEVEL
        )
        
        assertEquals(CraftingStation.FORGE, recipe.requiredStation)
        assertEquals(5, recipe.requiredIngredients[IngredientId("iron_ore")])
        assertEquals(3, recipe.requiredSkills[SkillId("combat")])
        assertEquals(300, recipe.craftingTime)
        assertEquals(85, recipe.baseSuccessRate)
        assertEquals(50, recipe.skillXPReward[SkillId("combat")])
    }
    
    @Test
    fun testCraftingKnowledgeRecipeManagement() {
        val knowledge = CraftingKnowledge()
        
        assertFalse(knowledge.knowsRecipe(CraftingRecipeId("test_recipe")))
        
        val learned = knowledge.learnRecipe(CraftingRecipeId("test_recipe"))
        assertTrue(learned.knowsRecipe(CraftingRecipeId("test_recipe")))
        
        // Learning again doesn't duplicate
        val learnedAgain = learned.learnRecipe(CraftingRecipeId("test_recipe"))
        assertEquals(1, learnedAgain.knownRecipes.size)
    }
    
    @Test
    fun testCraftingKnowledgeItemQuantities() {
        val knowledge = CraftingKnowledge()
        
        assertEquals(0, knowledge.getItemQuantity(CraftedItemId("helmet")))
        assertFalse(knowledge.hasItem(CraftedItemId("helmet")))
        
        val withItems = knowledge.addCraftedItems(CraftedItemId("helmet"), 1)
        assertEquals(1, withItems.getItemQuantity(CraftedItemId("helmet")))
        assertTrue(withItems.hasItem(CraftedItemId("helmet")))
        
        // Add more
        val withMore = withItems.addCraftedItems(CraftedItemId("helmet"), 2)
        assertEquals(3, withMore.getItemQuantity(CraftedItemId("helmet")))
    }
    
    @Test
    fun testCraftingKnowledgeRemoveItems() {
        val knowledge = CraftingKnowledge()
            .addCraftedItems(CraftedItemId("sword"), 5)
        
        val afterRemove = knowledge.removeCraftedItems(CraftedItemId("sword"), 2)
        assertEquals(3, afterRemove.getItemQuantity(CraftedItemId("sword")))
        
        // Remove all
        val allRemoved = afterRemove.removeCraftedItems(CraftedItemId("sword"), 3)
        assertEquals(0, allRemoved.getItemQuantity(CraftedItemId("sword")))
        assertFalse(allRemoved.hasItem(CraftedItemId("sword")))
    }
    
    @Test
    fun testCraftingKnowledgeRemoveMoreThanOwnedFails() {
        val knowledge = CraftingKnowledge()
            .addCraftedItems(CraftedItemId("shield"), 2)
        
        assertFailsWith<IllegalArgumentException> {
            knowledge.removeCraftedItems(CraftedItemId("shield"), 3)
        }
    }
    
    @Test
    fun testCraftingKnowledgeEquipment() {
        val knowledge = CraftingKnowledge()
            .addCraftedItems(CraftedItemId("iron_helmet"), 1)
        
        assertNull(knowledge.getEquippedItem(EquipmentSlot.HEAD))
        assertFalse(knowledge.isEquipped(CraftedItemId("iron_helmet")))
        
        val equipped = knowledge.equipItem(EquipmentSlot.HEAD, CraftedItemId("iron_helmet"))
        assertEquals(CraftedItemId("iron_helmet"), equipped.getEquippedItem(EquipmentSlot.HEAD))
        assertTrue(equipped.isEquipped(CraftedItemId("iron_helmet")))
    }
    
    @Test
    fun testCraftingKnowledgeEquipNotOwnedFails() {
        val knowledge = CraftingKnowledge()
        
        assertFailsWith<IllegalArgumentException> {
            knowledge.equipItem(EquipmentSlot.HEAD, CraftedItemId("nonexistent"))
        }
    }
    
    @Test
    fun testCraftingKnowledgeUnequip() {
        val knowledge = CraftingKnowledge()
            .addCraftedItems(CraftedItemId("armor"), 1)
            .equipItem(EquipmentSlot.BODY, CraftedItemId("armor"))
        
        assertEquals(CraftedItemId("armor"), knowledge.getEquippedItem(EquipmentSlot.BODY))
        
        val unequipped = knowledge.unequipSlot(EquipmentSlot.BODY)
        assertNull(unequipped.getEquippedItem(EquipmentSlot.BODY))
        assertFalse(unequipped.isEquipped(CraftedItemId("armor")))
    }
    
    @Test
    fun testCraftingKnowledgeEquipToMultipleSlots() {
        val knowledge = CraftingKnowledge()
            .addCraftedItems(CraftedItemId("helmet"), 1)
            .addCraftedItems(CraftedItemId("armor"), 1)
            .addCraftedItems(CraftedItemId("boots"), 1)
            .addCraftedItems(CraftedItemId("ring"), 1)
        
        val fullyEquipped = knowledge
            .equipItem(EquipmentSlot.HEAD, CraftedItemId("helmet"))
            .equipItem(EquipmentSlot.BODY, CraftedItemId("armor"))
            .equipItem(EquipmentSlot.TALONS, CraftedItemId("boots"))
            .equipItem(EquipmentSlot.ACCESSORY_1, CraftedItemId("ring"))
        
        assertEquals(CraftedItemId("helmet"), fullyEquipped.getEquippedItem(EquipmentSlot.HEAD))
        assertEquals(CraftedItemId("armor"), fullyEquipped.getEquippedItem(EquipmentSlot.BODY))
        assertEquals(CraftedItemId("boots"), fullyEquipped.getEquippedItem(EquipmentSlot.TALONS))
        assertEquals(CraftedItemId("ring"), fullyEquipped.getEquippedItem(EquipmentSlot.ACCESSORY_1))
        assertEquals(4, fullyEquipped.equippedItems.size)
    }
    
    @Test
    fun testCraftingKnowledgeReplaceEquippedItem() {
        val knowledge = CraftingKnowledge()
            .addCraftedItems(CraftedItemId("helmet_basic"), 1)
            .addCraftedItems(CraftedItemId("helmet_advanced"), 1)
            .equipItem(EquipmentSlot.HEAD, CraftedItemId("helmet_basic"))
        
        assertEquals(CraftedItemId("helmet_basic"), knowledge.getEquippedItem(EquipmentSlot.HEAD))
        
        // Equip different item to same slot (replaces)
        val replaced = knowledge.equipItem(EquipmentSlot.HEAD, CraftedItemId("helmet_advanced"))
        assertEquals(CraftedItemId("helmet_advanced"), replaced.getEquippedItem(EquipmentSlot.HEAD))
    }
    
    @Test
    fun testCraftingKnowledgeTimestamp() {
        val knowledge = CraftingKnowledge()
        
        assertEquals(0, knowledge.lastCraftAt)
        
        val updated = knowledge.updateCraftTimestamp(123456789L)
        assertEquals(123456789L, updated.lastCraftAt)
    }
    
    @Test
    fun testCraftingStationEnumValues() {
        // Ensure all expected stations exist
        assertEquals(CraftingStation.NONE, CraftingStation.valueOf("NONE"))
        assertEquals(CraftingStation.WORKBENCH, CraftingStation.valueOf("WORKBENCH"))
        assertEquals(CraftingStation.FORGE, CraftingStation.valueOf("FORGE"))
        assertEquals(CraftingStation.ALCHEMY_LAB, CraftingStation.valueOf("ALCHEMY_LAB"))
        assertEquals(CraftingStation.SEWING_TABLE, CraftingStation.valueOf("SEWING_TABLE"))
        assertEquals(CraftingStation.CARPENTRY_BENCH, CraftingStation.valueOf("CARPENTRY_BENCH"))
        assertEquals(CraftingStation.ENCHANTING_TABLE, CraftingStation.valueOf("ENCHANTING_TABLE"))
        assertEquals(CraftingStation.NEST_WORKSHOP, CraftingStation.valueOf("NEST_WORKSHOP"))
    }
    
    @Test
    fun testEquipmentSlotEnumValues() {
        // Ensure all expected slots exist
        assertEquals(7, EquipmentSlot.values().size)
        assertTrue(EquipmentSlot.values().contains(EquipmentSlot.HEAD))
        assertTrue(EquipmentSlot.values().contains(EquipmentSlot.BODY))
        assertTrue(EquipmentSlot.values().contains(EquipmentSlot.TALONS))
        assertTrue(EquipmentSlot.values().contains(EquipmentSlot.ACCESSORY_1))
        assertTrue(EquipmentSlot.values().contains(EquipmentSlot.ACCESSORY_2))
        assertTrue(EquipmentSlot.values().contains(EquipmentSlot.TOOL_MAIN))
        assertTrue(EquipmentSlot.values().contains(EquipmentSlot.TOOL_OFF))
    }
}
