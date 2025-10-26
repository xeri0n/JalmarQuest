package com.jalmarquest.core

import com.jalmarquest.core.model.*

fun main() {
    println("=== Welcome to JalmarQuest ===")
    println()
    
    // Create a sample character
    val heroStats = Stats(
        health = 100,
        maxHealth = 100,
        strength = 15,
        defense = 10,
        agility = 12,
        intelligence = 8
    )
    
    val hero = Character(
        name = "Jalmar the Brave",
        stats = heroStats,
        gold = 50
    )
    
    println("Character created: ${hero.name}")
    println("Stats: HP ${hero.getCurrentHealth()}/${hero.getMaxHealth()}, STR ${hero.stats.strength}, DEF ${hero.stats.defense}, AGI ${hero.stats.agility}, INT ${hero.stats.intelligence}")
    println("Gold: ${hero.gold}")
    println()
    
    // Create sample items
    val ironSword = Item(
        id = "sword_01",
        name = "Iron Sword",
        description = "A sturdy iron sword, suitable for beginners.",
        type = ItemType.WEAPON
    )
    
    val leatherArmor = Item(
        id = "armor_01",
        name = "Leather Armor",
        description = "Basic leather armor that provides modest protection.",
        type = ItemType.ARMOR
    )
    
    val healthPotion = Item(
        id = "potion_01",
        name = "Health Potion",
        description = "Restores 50 health points.",
        type = ItemType.CONSUMABLE
    )
    
    // Add items to inventory
    hero.addItem(ironSword)
    hero.addItem(leatherArmor)
    hero.addItem(healthPotion)
    
    println("=== Inventory ===")
    hero.inventory.forEach { item ->
        println("- ${item.name} (${item.type}): ${item.description}")
    }
    println()
    
    // Equip items
    println("=== Equipping Items ===")
    val swordEquipped = hero.equipItem(ironSword, Slot.WEAPON)
    val armorEquipped = hero.equipItem(leatherArmor, Slot.BODY)
    
    println("Equipped sword: $swordEquipped")
    println("Equipped armor: $armorEquipped")
    println()
    
    // Show equipped items
    println("=== Currently Equipped ===")
    hero.equippedItems.forEach { (slot, item) ->
        if (item != null) {
            println("$slot: ${item.name}")
        } else {
            println("$slot: (empty)")
        }
    }
    println()
    
    // Demonstrate combat
    println("=== Combat Simulation ===")
    println("Before combat: HP ${hero.stats.health}/${hero.stats.maxHealth}")
    
    // Take damage
    val heroAfterDamage = hero.takeDamage(25)
    println("After taking damage: HP ${heroAfterDamage.stats.health}/${heroAfterDamage.stats.maxHealth}")
    
    // Use health potion (healing)
    val heroAfterHealing = heroAfterDamage.heal(50)
    println("After using health potion: HP ${heroAfterHealing.stats.health}/${heroAfterHealing.stats.maxHealth}")
    println()
    
    // Initialize journal
    val journal = Journal()
    println("Journal initialized with ${journal.activeQuests.size} active quests")
    println()
    
    println("Game development in progress...")
}
