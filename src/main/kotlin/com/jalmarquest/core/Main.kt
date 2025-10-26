package com.jalmarquest.core

import com.jalmarquest.core.model.Item
import com.jalmarquest.core.model.ItemType
import com.jalmarquest.core.model.Journal

fun main() {
    println("=== Welcome to JalmarQuest ===")
    println()
    
    // Initialize a new journal
    val journal = Journal()
    
    // Create a sample item
    val sword = Item(
        id = "sword_01",
        name = "Iron Sword",
        description = "A sturdy iron sword, suitable for beginners.",
        type = ItemType.WEAPON
    )
    
    println("Game initialized successfully!")
    println("Sample item: ${sword.name} - ${sword.description}")
    println("Journal created with ${journal.activeQuests.size} active quests")
    println()
    println("Game development in progress...")
}
