package com.jalmarquest.core

import com.jalmarquest.core.model.*

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
    
    // Create a sample world with connected locations
    val world = createSampleWorld()
    
    println("Game initialized successfully!")
    println("Sample item: ${sword.name} - ${sword.description}")
    println("Journal created with ${journal.activeQuests.size} active quests")
    println()
    
    // Demonstrate world navigation
    println("=== World Map ===")
    val startLocation = world.getStartingLocation()
    if (startLocation != null) {
        println("Starting location: ${startLocation.name}")
        println("Description: ${startLocation.description}")
        println("Available exits: ${startLocation.connections.keys.joinToString()}")
        
        // Try moving north
        val northLocation = world.getConnectedLocation(startLocation.id, Direction.NORTH)
        if (northLocation != null) {
            println("\nMoving NORTH to: ${northLocation.name}")
            println("Description: ${northLocation.description}")
        }
    }
    println()
    println("Game development in progress...")
}

fun createSampleWorld(): World {
    val townSquare = Location(
        id = "town_square",
        name = "Town Square",
        description = "A bustling square in the center of town. Merchants sell their wares while townsfolk go about their business.",
        connections = mapOf(
            Direction.NORTH to "market",
            Direction.EAST to "tavern",
            Direction.SOUTH to "gate"
        )
    )
    
    val market = Location(
        id = "market",
        name = "Market District",
        description = "The air is filled with the smell of fresh bread and exotic spices. Stalls line the streets.",
        connections = mapOf(
            Direction.SOUTH to "town_square"
        )
    )
    
    val tavern = Location(
        id = "tavern",
        name = "The Rusty Sword Tavern",
        description = "A cozy tavern with warm lighting and the sound of laughter. The barkeep nods at you.",
        connections = mapOf(
            Direction.WEST to "town_square"
        )
    )
    
    val gate = Location(
        id = "gate",
        name = "Town Gate",
        description = "The massive wooden gate stands open. Beyond it lies the road to adventure.",
        connections = mapOf(
            Direction.NORTH to "town_square",
            Direction.SOUTH to "forest"
        )
    )
    
    val forest = Location(
        id = "forest",
        name = "Dark Forest",
        description = "Tall trees block out most of the sunlight. Strange sounds echo in the distance.",
        connections = mapOf(
            Direction.NORTH to "gate"
        )
    )
    
    return World(
        name = "Kingdom of Jalmar",
        locations = mapOf(
            townSquare.id to townSquare,
            market.id to market,
            tavern.id to tavern,
            gate.id to gate,
            forest.id to forest
        ),
        startingLocationId = "town_square"
    )
}
