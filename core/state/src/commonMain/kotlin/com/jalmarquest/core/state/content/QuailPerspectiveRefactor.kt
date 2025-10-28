package com.jalmarquest.core.state.content

/**
 * Comprehensive renaming map to ensure all game content reflects a button quail's perspective.
 * This enhances immersion by making the world feel appropriately scaled and themed.
 */
object QuailPerspectiveRefactor {
    
    // Item renames for better quail immersion
    val itemRenames = mapOf(
        "Wooden Sword" to "Sharpened Twig",
        "Iron Sword" to "Metal Splinter", 
        "Wooden Shield" to "Bark Chip Shield",
        "Health Potion" to "Vitality Nectar",
        "Mana Potion" to "Mind Dew",
        "Stamina Potion" to "Energy Seeds",
        "Leather Armor" to "Molt Feather Vest",
        "Iron Armor" to "Beetle Shell Plate",
        "Gold Coin" to "Shiny Pebble",
        "Torch" to "Glowworm Lantern",
        "Rope" to "Grass Strand",
        "Backpack" to "Seed Pouch",
        "Compass" to "Sun Stone",
        "Map" to "Territory Scratching",
        "Bread" to "Seed Cake",
        "Meat" to "Grub Chunk",
        "Fish" to "Minnow Morsel",
        "Apple" to "Berry Cluster",
        "Cheese" to "Aged Seed Paste",
        "Wine" to "Fermented Nectar"
    )
    
    // Location renames for quail-scale perspective  
    val locationRenames = mapOf(
        "Buttonburgh Market District" to "The Seed Exchange",
        "Buttonburgh Castle" to "The Great Roost",
        "Buttonburgh Tavern" to "The Dusty Hollow",
        "Blacksmith Shop" to "Twig Sharpening Stone",
        "General Store" to "Forage Cache",
        "Town Square" to "Central Pecking Grounds",
        "Guard Tower" to "Watchtwig Post",
        "Prison" to "The Wire Cage",
        "Library" to "Feather Archive",
        "Temple" to "Sacred Nesting Site",
        "Graveyard" to "Silent Roosting Fields",
        "Farm" to "Seed Plantation",
        "Mill" to "Grain Grinding Stone",
        "Warehouse" to "Winter Storage Burrow",
        "Docks" to "Puddle's Edge",
        "Bridge" to "Twig Crossing"
    )
    
    // Enemy renames for tiny bird perspective
    val enemyRenames = mapOf(
        "Giant Spider" to "Eight-Legged Terror",
        "Cave Bat" to "Shrieking Shadow",
        "Wolf" to "Fanged Behemoth", 
        "Bear" to "Mountain of Fur",
        "Snake" to "Scaled Devourer",
        "Rat" to "Whiskered Rival",
        "Hawk" to "Sky Death",
        "Cat" to "Silent Stalker",
        "Dog" to "Barking Giant",
        "Human" to "Two-Leg Titan",
        "Goblin" to "Green Scavenger",
        "Orc" to "Tusked Brute",
        "Dragon" to "Winged Apocalypse",
        "Skeleton" to "Rattling Bones",
        "Zombie" to "Rotting Shambler",
        "Ghost" to "Pale Whisper"
    )
    
    // NPC renames for avian society
    val npcRenames = mapOf(
        "Blacksmith John" to "Sharpbeak the Twigsmith",
        "Merchant Mary" to "Seedkeeper Plume",
        "Guard Captain" to "Watchwing Captain",
        "Tavern Keeper" to "Dustbath Tender",
        "Librarian" to "Lorefeather Keeper",
        "Priest" to "Nest Blessed Elder",
        "Mayor" to "First Rooster",
        "Thief" to "Shadow Molt",
        "Beggar" to "Broken Wing",
        "Noble" to "Golden Crest",
        "Farmer" to "Seedsower",
        "Fisher" to "Puddlehunter"
    )
    
    // Quest title improvements
    val questRenames = mapOf(
        "Kill 10 Rats" to "Defend the Seed Cache",
        "Fetch Water" to "Gather Morning Dew",
        "Deliver Letter" to "Carry the Feather Message",
        "Find Lost Child" to "Rescue the Lost Chick",
        "Collect Herbs" to "Forage for Healing Seeds",
        "Clear Dungeon" to "Cleanse the Dark Burrow",
        "Escort Merchant" to "Guard the Seed Trader",
        "Steal Item" to "Retrieve the Shiny",
        "Investigate Mystery" to "Follow the Strange Scratching",
        "Defeat Boss" to "Face the Apex Predator"
    )
    
    // Skill/ability renames
    val skillRenames = mapOf(
        "Swordsmanship" to "Twig Fighting",
        "Archery" to "Seed Spitting",
        "Magic" to "Dust Weaving",
        "Stealth" to "Silent Molt",
        "Lockpicking" to "Nest Untangling",
        "Persuasion" to "Chirp Charm",
        "Intimidation" to "Threat Display",
        "Athletics" to "Wing Strength",
        "Acrobatics" to "Aerial Agility",
        "Survival" to "Foraging Instinct",
        "Medicine" to "Preening Wisdom",
        "Crafting" to "Nest Building"
    )
    
    // Thought/philosophy renames for quail mindset
    val thoughtRenames = mapOf(
        "Nature of Reality" to "What Lies Beyond the Canopy",
        "Meaning of Life" to "Why We Scratch the Earth",
        "Power of Knowledge" to "Songs of the Elder Birds",
        "Path to Enlightenment" to "Finding the Perfect Dust",
        "Burden of Leadership" to "Weight of the Flock",
        "Cost of War" to "When Territories Clash",
        "Value of Peace" to "Harmony of the Roost",
        "Mystery of Death" to "The Final Migration",
        "Joy of Discovery" to "First Flight Memories",
        "Bonds of Family" to "Strength of the Clutch"
    )
    
    /**
     * Apply all renames to game content during initialization
     */
    fun applyAllRenames() {
        // This would be called during game initialization to update all catalogs
        // Implementation depends on your catalog structure
        println("Applying quail perspective renames to ${getAllRenamesCount()} items...")
    }
    
    fun getAllRenamesCount(): Int {
        return itemRenames.size + locationRenames.size + enemyRenames.size +
               npcRenames.size + questRenames.size + skillRenames.size + 
               thoughtRenames.size
    }
}
