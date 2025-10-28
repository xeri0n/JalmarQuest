package com.jalmarquest.core.state.worldmap

import com.jalmarquest.core.model.*

/**
 * Builder for constructing the complete world graph.
 * This creates the entire Alpha 2.0 world with 100+ interconnected location nodes.
 */
object WorldGraphBuilder {
    
    /**
     * Build the complete world graph for Jalmar Quest.
     * 
     * Structure:
     * - BUTTONBURGH (hub) at center
     * - 5+ major regions radiating outward
     * - Filler node chains connecting key locations
     * - 100+ total nodes for "enormous world" feel
     */
    fun buildCompleteWorld(): WorldGraph {
        val nodes = mutableMapOf<LocationNodeId, LocationNode>()
        
        // === BUTTONBURGH HUB (Starting Area) ===
        buildButtonburghHub(nodes)
        
        // === FOREST REGION ===
        buildForestRegion(nodes)
        
        // === BEACH REGION ===
        buildBeachRegion(nodes)
        
        // === SWAMP REGION ===
        buildSwampRegion(nodes)
        
        // === MOUNTAIN REGION ===
        buildMountainRegion(nodes)
        
        // === RUINS REGION ===
        buildRuinsRegion(nodes)
        
        // === ADDITIONAL REGIONS FOR SCALE ===
        buildCavesRegion(nodes)
        buildPlainsRegion(nodes)
        
        val startingNodeId = LocationNodeId("BUTTONBURGH_HUB")
        
        return WorldGraph(
            nodes = nodes,
            startingNodeId = startingNodeId,
            totalNodes = nodes.size
        )
    }
    
    // ===================================
    // BUTTONBURGH HUB
    // ===================================
    
    private fun buildButtonburghHub(nodes: MutableMap<LocationNodeId, LocationNode>) {
        // Main hub - player's starting location
        nodes[LocationNodeId("BUTTONBURGH_HUB")] = LocationNode(
            id = LocationNodeId("BUTTONBURGH_HUB"),
            name = "Buttonburgh Town Centre",
            description = "The bustling heart of Buttonburgh, where quails gather to trade, gossip, and plan adventures. Your journey begins here.",
            type = NodeType.HUB,
            position = NodePosition(0.5f, 0.5f), // Center of map
            region = "Buttonburgh",
            connections = listOf(
                // North to Forest
                NodeConnection(LocationNodeId("GRASSLAND_PATH_NORTH_1"), travelTimeSeconds = 30),
                // Northeast to Plains
                NodeConnection(LocationNodeId("GRASSLAND_PATH_NORTHEAST_1"), travelTimeSeconds = 30),
                // East to Beach
                NodeConnection(LocationNodeId("GRASSLAND_PATH_EAST_1"), travelTimeSeconds = 30),
                // South to Swamp
                NodeConnection(LocationNodeId("GRASSLAND_PATH_SOUTH_1"), travelTimeSeconds = 30),
                // West to Mountains
                NodeConnection(LocationNodeId("GRASSLAND_PATH_WEST_1"), travelTimeSeconds = 30),
                // Direct access to player's nest
                NodeConnection(LocationNodeId("THE_NEST"), travelTimeSeconds = 10)
            ),
            npcIds = listOf("NPC_MAYOR", "NPC_SHOPKEEPER", "NPC_QUEST_BOARD"),
            questIds = listOf("quest_welcome_to_buttonburgh"),
            ambientAudioId = "AUDIO_TOWN_BUSTLE",
            hasSpecialEvent = true
        )
        
        // The player's nest (fast travel point)
        nodes[LocationNodeId("THE_NEST")] = LocationNode(
            id = LocationNodeId("THE_NEST"),
            name = "Your Cozy Nest",
            description = "Your personal sanctuary, lovingly crafted from twigs, moss, and treasured trinkets. A safe haven to rest and plan your next adventure.",
            type = NodeType.NEST,
            position = NodePosition(0.48f, 0.52f),
            region = "Buttonburgh",
            connections = listOf(
                NodeConnection(LocationNodeId("BUTTONBURGH_HUB"), travelTimeSeconds = 10)
            ),
            ambientAudioId = "AUDIO_NEST_PEACEFUL",
            isNestScrape = true, // Always an activated fast travel point
            hasSpecialEvent = true
        )
    }
    
    // ===================================
    // FOREST REGION (North)
    // ===================================
    
    private fun buildForestRegion(nodes: MutableMap<LocationNodeId, LocationNode>) {
        // Filler path nodes leading to forest
        nodes[LocationNodeId("GRASSLAND_PATH_NORTH_1")] = LocationNode(
            id = LocationNodeId("GRASSLAND_PATH_NORTH_1"),
            name = "Northern Grassland",
            description = "Tall grass sways in the breeze as you venture north from Buttonburgh.",
            type = NodeType.FILLER,
            position = NodePosition(0.5f, 0.4f),
            region = "Grasslands",
            connections = listOf(
                NodeConnection(LocationNodeId("BUTTONBURGH_HUB")),
                NodeConnection(LocationNodeId("GRASSLAND_PATH_NORTH_2"))
            ),
            resourceIds = listOf("INGREDIENT_WILDFLOWER", "ITEM_GRASS_SEEDS"),
            enemyIds = listOf("ENEMY_CRICKET")
        )
        
        nodes[LocationNodeId("GRASSLAND_PATH_NORTH_2")] = LocationNode(
            id = LocationNodeId("GRASSLAND_PATH_NORTH_2"),
            name = "Woodland Edge",
            description = "The grasslands give way to the shadow of ancient trees ahead.",
            type = NodeType.FILLER,
            position = NodePosition(0.5f, 0.35f),
            region = "Grasslands",
            connections = listOf(
                NodeConnection(LocationNodeId("GRASSLAND_PATH_NORTH_1")),
                NodeConnection(LocationNodeId("FOREST_ENTRANCE"))
            ),
            resourceIds = listOf("INGREDIENT_MUSHROOM"),
            enemyIds = listOf("ENEMY_BEETLE", "ENEMY_SPIDER")
        )
        
        // Forest Entrance (key location)
        nodes[LocationNodeId("FOREST_ENTRANCE")] = LocationNode(
            id = LocationNodeId("FOREST_ENTRANCE"),
            name = "Whispering Woods Entrance",
            description = "Towering trees mark the entrance to the Whispering Woods. Dappled sunlight filters through the canopy, and the air smells of earth and pine.",
            type = NodeType.KEY_LOCATION,
            position = NodePosition(0.5f, 0.25f),
            region = "Whispering Woods",
            connections = listOf(
                NodeConnection(LocationNodeId("GRASSLAND_PATH_NORTH_2")),
                NodeConnection(LocationNodeId("FOREST_CLEARING")),
                NodeConnection(LocationNodeId("MAGPIE_NEST_PATH")),
                NodeConnection(LocationNodeId("FOREST_TO_RUINS_PATH"))
            ),
            npcIds = listOf("NPC_FOREST_RANGER"),
            questIds = listOf("quest_into_the_woods"),
            ambientAudioId = "AUDIO_FOREST_AMBIENT",
            resourceIds = listOf("INGREDIENT_PINE_CONE", "INGREDIENT_MOSS")
        )
        
        // Forest sub-locations
        nodes[LocationNodeId("FOREST_CLEARING")] = LocationNode(
            id = LocationNodeId("FOREST_CLEARING"),
            name = "Sunlit Clearing",
            description = "A peaceful glade bathed in warm sunlight. Perfect for foraging and rest.",
            type = NodeType.RESOURCE,
            position = NodePosition(0.45f, 0.22f),
            region = "Whispering Woods",
            connections = listOf(
                NodeConnection(LocationNodeId("FOREST_ENTRANCE")),
                NodeConnection(LocationNodeId("POISONED_GROVE"))
            ),
            resourceIds = listOf("INGREDIENT_WILDFLOWER", "INGREDIENT_BERRIES", "INGREDIENT_HERBS"),
            ambientAudioId = "AUDIO_FOREST_CLEARING"
        )
        
        nodes[LocationNodeId("POISONED_GROVE")] = LocationNode(
            id = LocationNodeId("POISONED_GROVE"),
            name = "The Poisoned Grove",
            description = "Dead trees and sickly mist fill this corrupted area. Something unnatural lurks here.",
            type = NodeType.DANGER,
            position = NodePosition(0.42f, 0.18f),
            region = "Whispering Woods",
            connections = listOf(
                NodeConnection(LocationNodeId("FOREST_CLEARING"))
            ),
            enemyIds = listOf("ENEMY_TOXIC_SPIDER", "ENEMY_CORRUPTED_RAVEN"),
            questIds = listOf("quest_cure_the_grove"),
            ambientAudioId = "AUDIO_CORRUPTED_FOREST",
            hasSpecialEvent = true
        )
        
        // Magpie's Nest quest location
        nodes[LocationNodeId("MAGPIE_NEST_PATH")] = LocationNode(
            id = LocationNodeId("MAGPIE_NEST_PATH"),
            name = "Thorny Thicket",
            description = "Dense brambles make passage difficult. You hear the glint of stolen treasures ahead.",
            type = NodeType.FILLER,
            position = NodePosition(0.55f, 0.22f),
            region = "Whispering Woods",
            connections = listOf(
                NodeConnection(LocationNodeId("FOREST_ENTRANCE")),
                NodeConnection(LocationNodeId("MAGPIE_NEST"))
            ),
            enemyIds = listOf("ENEMY_THORN_VINES")
        )
        
        nodes[LocationNodeId("MAGPIE_NEST")] = LocationNode(
            id = LocationNodeId("MAGPIE_NEST"),
            name = "Magpie's Glittering Nest",
            description = "A massive nest filled with shiny stolen objects. The Magpie Bandit King watches you with beady eyes.",
            type = NodeType.POINT_OF_INTEREST,
            position = NodePosition(0.58f, 0.20f),
            region = "Whispering Woods",
            connections = listOf(
                NodeConnection(LocationNodeId("MAGPIE_NEST_PATH"))
            ),
            npcIds = listOf("NPC_MAGPIE_BANDIT_KING"),
            questIds = listOf("quest_bandit_king"),
            enemyIds = listOf("ENEMY_MAGPIE_BANDIT"),
            ambientAudioId = "AUDIO_MAGPIE_LAIR",
            hasSpecialEvent = true
        )
        
        // Forest Nest Scrape (fast travel point)
        nodes[LocationNodeId("FOREST_NEST_SCRAPE")] = LocationNode(
            id = LocationNodeId("FOREST_NEST_SCRAPE"),
            name = "Ancient Oak Nest Scrape",
            description = "A comfortable hollow at the base of an ancient oak. Perfect for a temporary nest.",
            type = NodeType.NEST_SCRAPE,
            position = NodePosition(0.48f, 0.20f),
            region = "Whispering Woods",
            connections = listOf(
                NodeConnection(LocationNodeId("FOREST_ENTRANCE"))
            ),
            isNestScrape = true,
            ambientAudioId = "AUDIO_FOREST_PEACEFUL"
        )
    }
    
    // ===================================
    // BEACH REGION (East)
    // ===================================
    
    private fun buildBeachRegion(nodes: MutableMap<LocationNodeId, LocationNode>) {
        // Filler paths to beach
        nodes[LocationNodeId("GRASSLAND_PATH_EAST_1")] = LocationNode(
            id = LocationNodeId("GRASSLAND_PATH_EAST_1"),
            name = "Eastern Meadow",
            description = "The scent of salt air grows stronger as you head east.",
            type = NodeType.FILLER,
            position = NodePosition(0.6f, 0.5f),
            region = "Grasslands",
            connections = listOf(
                NodeConnection(LocationNodeId("BUTTONBURGH_HUB")),
                NodeConnection(LocationNodeId("GRASSLAND_PATH_EAST_2"))
            ),
            resourceIds = listOf("INGREDIENT_WILDFLOWER"),
            enemyIds = listOf("ENEMY_CRICKET")
        )
        
        nodes[LocationNodeId("GRASSLAND_PATH_EAST_2")] = LocationNode(
            id = LocationNodeId("GRASSLAND_PATH_EAST_2"),
            name = "Sandy Dunes",
            description = "Grass gives way to sandy hills. The ocean roars ahead.",
            type = NodeType.FILLER,
            position = NodePosition(0.7f, 0.5f),
            region = "Dunes",
            connections = listOf(
                NodeConnection(LocationNodeId("GRASSLAND_PATH_EAST_1")),
                NodeConnection(LocationNodeId("BEACH_ENTRANCE"))
            ),
            enemyIds = listOf("ENEMY_SAND_CRAB")
        )
        
        // Beach entrance
        nodes[LocationNodeId("BEACH_ENTRANCE")] = LocationNode(
            id = LocationNodeId("BEACH_ENTRANCE"),
            name = "Sandy Shore",
            description = "Waves crash against the shoreline. Seabirds cry overhead, and the tang of salt fills the air.",
            type = NodeType.KEY_LOCATION,
            position = NodePosition(0.75f, 0.5f),
            region = "Coastal Sands",
            connections = listOf(
                NodeConnection(LocationNodeId("GRASSLAND_PATH_EAST_2")),
                NodeConnection(LocationNodeId("TIDE_POOLS")),
                NodeConnection(LocationNodeId("DRIFTWOOD_COVE")),
                NodeConnection(LocationNodeId("SHIPWRECK"))
            ),
            npcIds = listOf("NPC_HERMIT_CRAB"),
            questIds = listOf("quest_beachcomber"),
            resourceIds = listOf("INGREDIENT_SEAWEED", "INGREDIENT_SHELLS"),
            ambientAudioId = "AUDIO_BEACH_WAVES"
        )
        
        // Beach sub-locations
        nodes[LocationNodeId("TIDE_POOLS")] = LocationNode(
            id = LocationNodeId("TIDE_POOLS"),
            name = "Glimmering Tide Pools",
            description = "Crystal-clear pools teem with tiny crabs and colorful fish.",
            type = NodeType.RESOURCE,
            position = NodePosition(0.78f, 0.47f),
            region = "Coastal Sands",
            connections = listOf(
                NodeConnection(LocationNodeId("BEACH_ENTRANCE"))
            ),
            resourceIds = listOf("INGREDIENT_SEA_SALT", "INGREDIENT_CORAL", "ITEM_SHINY_PEBBLE"),
            ambientAudioId = "AUDIO_TIDE_POOLS"
        )
        
        nodes[LocationNodeId("DRIFTWOOD_COVE")] = LocationNode(
            id = LocationNodeId("DRIFTWOOD_COVE"),
            name = "Driftwood Cove",
            description = "Massive pieces of driftwood create a maze-like playground.",
            type = NodeType.POINT_OF_INTEREST,
            position = NodePosition(0.78f, 0.53f),
            region = "Coastal Sands",
            connections = listOf(
                NodeConnection(LocationNodeId("BEACH_ENTRANCE")),
                NodeConnection(LocationNodeId("BEACH_NEST_SCRAPE"))
            ),
            npcIds = listOf("NPC_SEAGULL_MERCHANT"),
            ambientAudioId = "AUDIO_BEACH_COVE"
        )
        
        nodes[LocationNodeId("SHIPWRECK")] = LocationNode(
            id = LocationNodeId("SHIPWRECK"),
            name = "The Wrecked Galleon",
            description = "A massive shipwreck half-buried in sand. Treasures and dangers await within.",
            type = NodeType.DANGER,
            position = NodePosition(0.80f, 0.50f),
            region = "Coastal Sands",
            connections = listOf(
                NodeConnection(LocationNodeId("BEACH_ENTRANCE"))
            ),
            enemyIds = listOf("ENEMY_GHOST_CRAB", "ENEMY_SEA_RAT"),
            questIds = listOf("quest_shipwreck_treasure"),
            ambientAudioId = "AUDIO_SHIPWRECK",
            hasSpecialEvent = true
        )
        
        // Beach Nest Scrape
        nodes[LocationNodeId("BEACH_NEST_SCRAPE")] = LocationNode(
            id = LocationNodeId("BEACH_NEST_SCRAPE"),
            name = "Dune Nest Scrape",
            description = "A sheltered hollow in the dunes, protected from wind and waves.",
            type = NodeType.NEST_SCRAPE,
            position = NodePosition(0.77f, 0.55f),
            region = "Coastal Sands",
            connections = listOf(
                NodeConnection(LocationNodeId("DRIFTWOOD_COVE"))
            ),
            isNestScrape = true,
            ambientAudioId = "AUDIO_BEACH_PEACEFUL"
        )
    }
    
    // ===================================
    // SWAMP REGION (South)
    // ===================================
    
    private fun buildSwampRegion(nodes: MutableMap<LocationNodeId, LocationNode>) {
        // Filler paths to swamp
        nodes[LocationNodeId("GRASSLAND_PATH_SOUTH_1")] = LocationNode(
            id = LocationNodeId("GRASSLAND_PATH_SOUTH_1"),
            name = "Southern Grassland",
            description = "The ground grows softer with each step south.",
            type = NodeType.FILLER,
            position = NodePosition(0.5f, 0.6f),
            region = "Grasslands",
            connections = listOf(
                NodeConnection(LocationNodeId("BUTTONBURGH_HUB")),
                NodeConnection(LocationNodeId("GRASSLAND_PATH_SOUTH_2"))
            ),
            resourceIds = listOf("INGREDIENT_REEDS"),
            enemyIds = listOf("ENEMY_MOSQUITO")
        )
        
        nodes[LocationNodeId("GRASSLAND_PATH_SOUTH_2")] = LocationNode(
            id = LocationNodeId("GRASSLAND_PATH_SOUTH_2"),
            name = "Boggy Ground",
            description = "Your feet sink into damp earth. The smell of rot fills the air.",
            type = NodeType.FILLER,
            position = NodePosition(0.5f, 0.65f),
            region = "Wetlands",
            connections = listOf(
                NodeConnection(LocationNodeId("GRASSLAND_PATH_SOUTH_1")),
                NodeConnection(LocationNodeId("SWAMP_ENTRANCE"))
            ),
            enemyIds = listOf("ENEMY_LEECH", "ENEMY_MOSQUITO")
        )
        
        // Swamp entrance
        nodes[LocationNodeId("SWAMP_ENTRANCE")] = LocationNode(
            id = LocationNodeId("SWAMP_ENTRANCE"),
            name = "Murky Water's Edge",
            description = "Dark water stretches before you, covered in lily pads and fog. Strange croaks echo in the mist.",
            type = NodeType.KEY_LOCATION,
            position = NodePosition(0.5f, 0.75f),
            region = "Murkmire Swamp",
            connections = listOf(
                NodeConnection(LocationNodeId("GRASSLAND_PATH_SOUTH_2")),
                NodeConnection(LocationNodeId("LILY_PAD_PATH")),
                NodeConnection(LocationNodeId("WITCH_HUT_PATH"))
            ),
            npcIds = listOf("NPC_FROG_GUIDE"),
            questIds = listOf("quest_swamp_crossing"),
            ambientAudioId = "AUDIO_SWAMP_AMBIENT",
            resourceIds = listOf("INGREDIENT_MOSS", "INGREDIENT_SWAMP_FUNGUS")
        )
        
        // Swamp sub-locations
        nodes[LocationNodeId("LILY_PAD_PATH")] = LocationNode(
            id = LocationNodeId("LILY_PAD_PATH"),
            name = "Lily Pad Trail",
            description = "Massive lily pads form a precarious path across the swamp.",
            type = NodeType.FILLER,
            position = NodePosition(0.48f, 0.78f),
            region = "Murkmire Swamp",
            connections = listOf(
                NodeConnection(LocationNodeId("SWAMP_ENTRANCE")),
                NodeConnection(LocationNodeId("FIREFLY_GROVE"))
            ),
            enemyIds = listOf("ENEMY_SWAMP_SNAKE"),
            ambientAudioId = "AUDIO_SWAMP_WATER"
        )
        
        nodes[LocationNodeId("FIREFLY_GROVE")] = LocationNode(
            id = LocationNodeId("FIREFLY_GROVE"),
            name = "Firefly Grove",
            description = "Thousands of fireflies illuminate this magical grove at night.",
            type = NodeType.POINT_OF_INTEREST,
            position = NodePosition(0.46f, 0.80f),
            region = "Murkmire Swamp",
            connections = listOf(
                NodeConnection(LocationNodeId("LILY_PAD_PATH")),
                NodeConnection(LocationNodeId("SWAMP_NEST_SCRAPE"))
            ),
            resourceIds = listOf("INGREDIENT_LUMINOUS_MOSS", "INGREDIENT_MOONDEW_FERN"),
            questIds = listOf("quest_firefly_light"),
            ambientAudioId = "AUDIO_FIREFLY_GROVE",
            hasSpecialEvent = true
        )
        
        nodes[LocationNodeId("WITCH_HUT_PATH")] = LocationNode(
            id = LocationNodeId("WITCH_HUT_PATH"),
            name = "Crooked Path",
            description = "A winding path through gnarled trees leads to a mysterious dwelling.",
            type = NodeType.FILLER,
            position = NodePosition(0.52f, 0.78f),
            region = "Murkmire Swamp",
            connections = listOf(
                NodeConnection(LocationNodeId("SWAMP_ENTRANCE")),
                NodeConnection(LocationNodeId("WITCH_HUT"))
            ),
            enemyIds = listOf("ENEMY_WILL_O_WISP")
        )
        
        nodes[LocationNodeId("WITCH_HUT")] = LocationNode(
            id = LocationNodeId("WITCH_HUT"),
            name = "The Alchemist's Hut",
            description = "A crooked hut on stilts, filled with bubbling cauldrons and mysterious ingredients. The old crow alchemist practices her craft here.",
            type = NodeType.KEY_LOCATION,
            position = NodePosition(0.54f, 0.80f),
            region = "Murkmire Swamp",
            connections = listOf(
                NodeConnection(LocationNodeId("WITCH_HUT_PATH"))
            ),
            npcIds = listOf("NPC_CROW_ALCHEMIST"),
            questIds = listOf("quest_rare_ingredients", "quest_alchemist_apprentice"),
            ambientAudioId = "AUDIO_ALCHEMY_HUT",
            hasSpecialEvent = true
        )
        
        // Swamp Nest Scrape
        nodes[LocationNodeId("SWAMP_NEST_SCRAPE")] = LocationNode(
            id = LocationNodeId("SWAMP_NEST_SCRAPE"),
            name = "Hollow Log Nest Scrape",
            description = "A dry, hollow log provides shelter from the swamp's dampness.",
            type = NodeType.NEST_SCRAPE,
            position = NodePosition(0.45f, 0.82f),
            region = "Murkmire Swamp",
            connections = listOf(
                NodeConnection(LocationNodeId("FIREFLY_GROVE"))
            ),
            isNestScrape = true,
            ambientAudioId = "AUDIO_SWAMP_PEACEFUL"
        )
    }
    
    // ===================================
    // MOUNTAIN REGION (West)
    // ===================================
    
    private fun buildMountainRegion(nodes: MutableMap<LocationNodeId, LocationNode>) {
        // Filler paths to mountains
        nodes[LocationNodeId("GRASSLAND_PATH_WEST_1")] = LocationNode(
            id = LocationNodeId("GRASSLAND_PATH_WEST_1"),
            name = "Western Grassland",
            description = "Rocky outcroppings dot the landscape as you head west.",
            type = NodeType.FILLER,
            position = NodePosition(0.4f, 0.5f),
            region = "Grasslands",
            connections = listOf(
                NodeConnection(LocationNodeId("BUTTONBURGH_HUB")),
                NodeConnection(LocationNodeId("GRASSLAND_PATH_WEST_2"))
            ),
            resourceIds = listOf("INGREDIENT_WILDFLOWER"),
            enemyIds = listOf("ENEMY_CRICKET")
        )
        
        nodes[LocationNodeId("GRASSLAND_PATH_WEST_2")] = LocationNode(
            id = LocationNodeId("GRASSLAND_PATH_WEST_2"),
            name = "Rocky Foothills",
            description = "The ground rises sharply. Towering peaks loom ahead.",
            type = NodeType.FILLER,
            position = NodePosition(0.3f, 0.5f),
            region = "Foothills",
            connections = listOf(
                NodeConnection(LocationNodeId("GRASSLAND_PATH_WEST_1")),
                NodeConnection(LocationNodeId("MOUNTAIN_ENTRANCE"))
            ),
            enemyIds = listOf("ENEMY_MOUNTAIN_GOAT", "ENEMY_EAGLE")
        )
        
        // Mountain entrance
        nodes[LocationNodeId("MOUNTAIN_ENTRANCE")] = LocationNode(
            id = LocationNodeId("MOUNTAIN_ENTRANCE"),
            name = "Base of the Peaks",
            description = "Massive stone mountains rise before you, their peaks hidden in clouds. The air grows thin and cold.",
            type = NodeType.KEY_LOCATION,
            position = NodePosition(0.25f, 0.5f),
            region = "Stone Peaks",
            connections = listOf(
                NodeConnection(LocationNodeId("GRASSLAND_PATH_WEST_2")),
                NodeConnection(LocationNodeId("MOUNTAIN_PATH_1")),
                NodeConnection(LocationNodeId("MINE_ENTRANCE"))
            ),
            npcIds = listOf("NPC_MOUNTAIN_GUIDE"),
            questIds = listOf("quest_climb_the_peaks"),
            ambientAudioId = "AUDIO_MOUNTAIN_WIND",
            resourceIds = listOf("INGREDIENT_MOUNTAIN_HERB", "ITEM_STONE")
        )
        
        // Mountain climbing path
        nodes[LocationNodeId("MOUNTAIN_PATH_1")] = LocationNode(
            id = LocationNodeId("MOUNTAIN_PATH_1"),
            name = "Steep Ascent",
            description = "A narrow path winds up the mountainside. One wrong step could be fatal.",
            type = NodeType.FILLER,
            position = NodePosition(0.23f, 0.45f),
            region = "Stone Peaks",
            connections = listOf(
                NodeConnection(LocationNodeId("MOUNTAIN_ENTRANCE")),
                NodeConnection(LocationNodeId("MOUNTAIN_PATH_2"))
            ),
            enemyIds = listOf("ENEMY_MOUNTAIN_GOAT"),
            ambientAudioId = "AUDIO_MOUNTAIN_CLIMB"
        )
        
        nodes[LocationNodeId("MOUNTAIN_PATH_2")] = LocationNode(
            id = LocationNodeId("MOUNTAIN_PATH_2"),
            name = "Cloud Line",
            description = "You've climbed above the clouds. The world stretches endlessly below.",
            type = NodeType.FILLER,
            position = NodePosition(0.22f, 0.40f),
            region = "Stone Peaks",
            connections = listOf(
                NodeConnection(LocationNodeId("MOUNTAIN_PATH_1")),
                NodeConnection(LocationNodeId("MOUNTAIN_SUMMIT"))
            ),
            resourceIds = listOf("INGREDIENT_SKY_BLOSSOM"),
            ambientAudioId = "AUDIO_MOUNTAIN_HIGH"
        )
        
        nodes[LocationNodeId("MOUNTAIN_SUMMIT")] = LocationNode(
            id = LocationNodeId("MOUNTAIN_SUMMIT"),
            name = "The Summit",
            description = "The highest peak in the region. From here, you can see the entire world spread below you—a tiny hero atop a giant's perch.",
            type = NodeType.POINT_OF_INTEREST,
            position = NodePosition(0.20f, 0.35f),
            region = "Stone Peaks",
            connections = listOf(
                NodeConnection(LocationNodeId("MOUNTAIN_PATH_2")),
                NodeConnection(LocationNodeId("MOUNTAIN_NEST_SCRAPE"))
            ),
            questIds = listOf("quest_summit_view"),
            ambientAudioId = "AUDIO_MOUNTAIN_SUMMIT",
            hasSpecialEvent = true
        )
        
        // Mine branch
        nodes[LocationNodeId("MINE_ENTRANCE")] = LocationNode(
            id = LocationNodeId("MINE_ENTRANCE"),
            name = "Abandoned Mine Entrance",
            description = "An old mine shaft dug by humans long ago. Dark passages lead deep into the mountain.",
            type = NodeType.DANGER,
            position = NodePosition(0.25f, 0.53f),
            region = "Stone Peaks",
            connections = listOf(
                NodeConnection(LocationNodeId("MOUNTAIN_ENTRANCE")),
                NodeConnection(LocationNodeId("MINE_TUNNELS"))
            ),
            enemyIds = listOf("ENEMY_BAT", "ENEMY_CAVE_RAT"),
            ambientAudioId = "AUDIO_MINE_ENTRANCE"
        )
        
        nodes[LocationNodeId("MINE_TUNNELS")] = LocationNode(
            id = LocationNodeId("MINE_TUNNELS"),
            name = "Deep Mine Tunnels",
            description = "Dark, winding tunnels filled with abandoned equipment and glinting ore.",
            type = NodeType.DANGER,
            position = NodePosition(0.23f, 0.55f),
            region = "Underground",
            connections = listOf(
                NodeConnection(LocationNodeId("MINE_ENTRANCE")),
                NodeConnection(LocationNodeId("CRYSTAL_CHAMBER"))
            ),
            enemyIds = listOf("ENEMY_CAVE_BAT", "ENEMY_ROCK_SPIDER"),
            resourceIds = listOf("ITEM_IRON_ORE", "ITEM_COAL"),
            ambientAudioId = "AUDIO_MINE_DEEP"
        )
        
        nodes[LocationNodeId("CRYSTAL_CHAMBER")] = LocationNode(
            id = LocationNodeId("CRYSTAL_CHAMBER"),
            name = "Glimmering Crystal Chamber",
            description = "A hidden chamber filled with massive crystals that glow with inner light. A dragon's hoard of natural treasures.",
            type = NodeType.POINT_OF_INTEREST,
            position = NodePosition(0.21f, 0.57f),
            region = "Underground",
            connections = listOf(
                NodeConnection(LocationNodeId("MINE_TUNNELS")),
                NodeConnection(LocationNodeId("DEEP_SHAFT"))
            ),
            resourceIds = listOf("ITEM_CRYSTAL_SHARD", "ITEM_GLIMMER_SHARD", "ITEM_PRECIOUS_GEM"),
            questIds = listOf("quest_crystal_collection"),
            ambientAudioId = "AUDIO_CRYSTAL_CAVE",
            hasSpecialEvent = true
        )
        
        // Mountain Nest Scrape
        nodes[LocationNodeId("MOUNTAIN_NEST_SCRAPE")] = LocationNode(
            id = LocationNodeId("MOUNTAIN_NEST_SCRAPE"),
            name = "Summit Nest Scrape",
            description = "A sheltered crevice near the summit, protected from harsh winds.",
            type = NodeType.NEST_SCRAPE,
            position = NodePosition(0.19f, 0.37f),
            region = "Stone Peaks",
            connections = listOf(
                NodeConnection(LocationNodeId("MOUNTAIN_SUMMIT"))
            ),
            isNestScrape = true,
            ambientAudioId = "AUDIO_MOUNTAIN_PEACEFUL"
        )
    }
    
    // ===================================
    // RUINS REGION (Northwest)
    // ===================================
    
    private fun buildRuinsRegion(nodes: MutableMap<LocationNodeId, LocationNode>) {
        // Connection from forest to ruins
        nodes[LocationNodeId("FOREST_TO_RUINS_PATH")] = LocationNode(
            id = LocationNodeId("FOREST_TO_RUINS_PATH"),
            name = "Overgrown Trail",
            description = "Ancient stonework peeks through the undergrowth.",
            type = NodeType.FILLER,
            position = NodePosition(0.40f, 0.25f),
            region = "Whispering Woods",
            connections = listOf(
                NodeConnection(LocationNodeId("FOREST_ENTRANCE")),
                NodeConnection(LocationNodeId("RUINS_ENTRANCE"))
            ),
            enemyIds = listOf("ENEMY_SPIDER")
        )
        
        // Ruins entrance
        nodes[LocationNodeId("RUINS_ENTRANCE")] = LocationNode(
            id = LocationNodeId("RUINS_ENTRANCE"),
            name = "Crumbling Gate",
            description = "A massive stone gate, half-collapsed with age. Beyond it lie the remnants of a forgotten civilization.",
            type = NodeType.KEY_LOCATION,
            position = NodePosition(0.35f, 0.22f),
            region = "Ancient Ruins",
            connections = listOf(
                NodeConnection(LocationNodeId("FOREST_TO_RUINS_PATH")),
                NodeConnection(LocationNodeId("STATUE_PLAZA")),
                NodeConnection(LocationNodeId("TEMPLE_PATH"))
            ),
            npcIds = listOf("NPC_ARCHAEOLOGIST"),
            questIds = listOf("quest_ruins_exploration"),
            ambientAudioId = "AUDIO_RUINS_AMBIENT"
        )
        
        // Ruins sub-locations
        nodes[LocationNodeId("STATUE_PLAZA")] = LocationNode(
            id = LocationNodeId("STATUE_PLAZA"),
            name = "Plaza of Giants",
            description = "Massive stone statues of ancient kings tower over a cracked plaza.",
            type = NodeType.POINT_OF_INTEREST,
            position = NodePosition(0.33f, 0.20f),
            region = "Ancient Ruins",
            connections = listOf(
                NodeConnection(LocationNodeId("RUINS_ENTRANCE"))
            ),
            enemyIds = listOf("ENEMY_STONE_GOLEM"),
            resourceIds = listOf("ITEM_ANCIENT_COIN", "ITEM_RUNE_FRAGMENT"),
            ambientAudioId = "AUDIO_RUINS_PLAZA",
            hasSpecialEvent = true
        )
        
        nodes[LocationNodeId("TEMPLE_PATH")] = LocationNode(
            id = LocationNodeId("TEMPLE_PATH"),
            name = "Vine-Covered Steps",
            description = "Stone steps wind upward toward a crumbling temple.",
            type = NodeType.FILLER,
            position = NodePosition(0.32f, 0.24f),
            region = "Ancient Ruins",
            connections = listOf(
                NodeConnection(LocationNodeId("RUINS_ENTRANCE")),
                NodeConnection(LocationNodeId("ANCIENT_TEMPLE"))
            ),
            enemyIds = listOf("ENEMY_GUARDIAN_SNAKE")
        )
        
        nodes[LocationNodeId("ANCIENT_TEMPLE")] = LocationNode(
            id = LocationNodeId("ANCIENT_TEMPLE"),
            name = "The Lost Temple",
            description = "An ancient temple dedicated to forgotten gods. Strange magic still lingers here.",
            type = NodeType.DANGER,
            position = NodePosition(0.30f, 0.26f),
            region = "Ancient Ruins",
            connections = listOf(
                NodeConnection(LocationNodeId("TEMPLE_PATH")),
                NodeConnection(LocationNodeId("SANCTUM"))
            ),
            enemyIds = listOf("ENEMY_TEMPLE_GUARDIAN", "ENEMY_CURSED_SPIRIT"),
            questIds = listOf("quest_temple_curse"),
            ambientAudioId = "AUDIO_TEMPLE_EERIE",
            hasSpecialEvent = true
        )
        
        nodes[LocationNodeId("SANCTUM")] = LocationNode(
            id = LocationNodeId("SANCTUM"),
            name = "Inner Sanctum",
            description = "The heart of the temple. An altar glows with ancient power.",
            type = NodeType.POINT_OF_INTEREST,
            position = NodePosition(0.28f, 0.27f),
            region = "Ancient Ruins",
            connections = listOf(
                NodeConnection(LocationNodeId("ANCIENT_TEMPLE")),
                NodeConnection(LocationNodeId("RUINS_NEST_SCRAPE"))
            ),
            questIds = listOf("quest_sanctum_blessing"),
            ambientAudioId = "AUDIO_SANCTUM_MYSTICAL",
            hasSpecialEvent = true
        )
        
        // Ruins Nest Scrape
        nodes[LocationNodeId("RUINS_NEST_SCRAPE")] = LocationNode(
            id = LocationNodeId("RUINS_NEST_SCRAPE"),
            name = "Altar Nest Scrape",
            description = "A peaceful nook behind the ancient altar, surprisingly comfortable.",
            type = NodeType.NEST_SCRAPE,
            position = NodePosition(0.27f, 0.28f),
            region = "Ancient Ruins",
            connections = listOf(
                NodeConnection(LocationNodeId("SANCTUM"))
            ),
            isNestScrape = true,
            ambientAudioId = "AUDIO_RUINS_PEACEFUL"
        )
    }
    
    // ===================================
    // CAVES REGION (Deep Underground)
    // ===================================
    
    private fun buildCavesRegion(nodes: MutableMap<LocationNodeId, LocationNode>) {
        // Connect from mine to deeper caves
        // Connect mine to deeper caves via deep shaft
        nodes[LocationNodeId("DEEP_SHAFT")] = LocationNode(
            id = LocationNodeId("DEEP_SHAFT"),
            name = "Descent into Darkness",
            description = "A deep shaft leads further underground than any mine dares go.",
            type = NodeType.FILLER,
            position = NodePosition(0.20f, 0.60f),
            region = "Underground",
            connections = listOf(
                NodeConnection(LocationNodeId("CRYSTAL_CHAMBER")),
                NodeConnection(LocationNodeId("UNDERGROUND_LAKE"))
            ),
            enemyIds = listOf("ENEMY_CAVE_BAT"),
            ambientAudioId = "AUDIO_DEEP_DESCENT"
        )
        
        // Underground lake
        nodes[LocationNodeId("UNDERGROUND_LAKE")] = LocationNode(
            id = LocationNodeId("UNDERGROUND_LAKE"),
            name = "Subterranean Lake",
            description = "A vast underground lake glows with bioluminescent algae. The ceiling is lost in darkness above.",
            type = NodeType.KEY_LOCATION,
            position = NodePosition(0.18f, 0.63f),
            region = "Deep Caves",
            connections = listOf(
                NodeConnection(LocationNodeId("DEEP_SHAFT")),
                NodeConnection(LocationNodeId("MUSHROOM_FOREST")),
                NodeConnection(LocationNodeId("DARK_DEPTHS"))
            ),
            resourceIds = listOf("INGREDIENT_LUMINOUS_ALGAE", "INGREDIENT_CAVE_MOSS"),
            npcIds = listOf("NPC_BLIND_FISH"),
            ambientAudioId = "AUDIO_UNDERGROUND_LAKE"
        )
        
        // Mushroom forest
        nodes[LocationNodeId("MUSHROOM_FOREST")] = LocationNode(
            id = LocationNodeId("MUSHROOM_FOREST"),
            name = "Giant Mushroom Forest",
            description = "Massive glowing mushrooms create an alien forest deep underground.",
            type = NodeType.RESOURCE,
            position = NodePosition(0.16f, 0.61f),
            region = "Deep Caves",
            connections = listOf(
                NodeConnection(LocationNodeId("UNDERGROUND_LAKE")),
                NodeConnection(LocationNodeId("CAVES_NEST_SCRAPE"))
            ),
            resourceIds = listOf("INGREDIENT_GIANT_MUSHROOM", "INGREDIENT_SPORE_DUST", "INGREDIENT_GLOW_CAP"),
            questIds = listOf("quest_mushroom_harvest"),
            ambientAudioId = "AUDIO_MUSHROOM_FOREST"
        )
        
        // Dark depths
        nodes[LocationNodeId("DARK_DEPTHS")] = LocationNode(
            id = LocationNodeId("DARK_DEPTHS"),
            name = "The Endless Dark",
            description = "Complete darkness. Strange sounds echo from unseen passages. Only the bravest dare venture here.",
            type = NodeType.DANGER,
            position = NodePosition(0.17f, 0.66f),
            region = "Deep Caves",
            connections = listOf(
                NodeConnection(LocationNodeId("UNDERGROUND_LAKE")),
                NodeConnection(LocationNodeId("ANCIENT_CHAMBER"))
            ),
            enemyIds = listOf("ENEMY_CAVE_HORROR", "ENEMY_BLIND_PREDATOR"),
            ambientAudioId = "AUDIO_DEEP_DARKNESS"
        )
        
        nodes[LocationNodeId("ANCIENT_CHAMBER")] = LocationNode(
            id = LocationNodeId("ANCIENT_CHAMBER"),
            name = "Chamber of Echoes",
            description = "A vast chamber filled with ancient carvings and mysterious artifacts from a civilization older than memory.",
            type = NodeType.POINT_OF_INTEREST,
            position = NodePosition(0.15f, 0.68f),
            region = "Deep Caves",
            connections = listOf(
                NodeConnection(LocationNodeId("DARK_DEPTHS"))
            ),
            resourceIds = listOf("ITEM_ANCIENT_ARTIFACT", "ITEM_MYSTERIOUS_ORB"),
            questIds = listOf("quest_ancient_secrets"),
            ambientAudioId = "AUDIO_ANCIENT_CHAMBER",
            hasSpecialEvent = true
        )
        
        // Caves Nest Scrape
        nodes[LocationNodeId("CAVES_NEST_SCRAPE")] = LocationNode(
            id = LocationNodeId("CAVES_NEST_SCRAPE"),
            name = "Mushroom Nest Scrape",
            description = "A cozy nest woven among the giant mushroom stalks.",
            type = NodeType.NEST_SCRAPE,
            position = NodePosition(0.15f, 0.60f),
            region = "Deep Caves",
            connections = listOf(
                NodeConnection(LocationNodeId("MUSHROOM_FOREST"))
            ),
            isNestScrape = true,
            ambientAudioId = "AUDIO_CAVES_PEACEFUL"
        )
    }
    
    // ===================================
    // PLAINS REGION (Northeast - Extended content)
    // ===================================
    
    private fun buildPlainsRegion(nodes: MutableMap<LocationNodeId, LocationNode>) {
        // Connection from Buttonburgh to plains
        nodes[LocationNodeId("GRASSLAND_PATH_NORTHEAST_1")] = LocationNode(
            id = LocationNodeId("GRASSLAND_PATH_NORTHEAST_1"),
            name = "Rolling Hills",
            description = "Gentle hills roll endlessly toward the horizon.",
            type = NodeType.FILLER,
            position = NodePosition(0.60f, 0.40f),
            region = "Grasslands",
            connections = listOf(
                NodeConnection(LocationNodeId("BUTTONBURGH_HUB")),
                NodeConnection(LocationNodeId("GRASSLAND_PATH_NORTHEAST_2"))
            ),
            resourceIds = listOf("INGREDIENT_WILDFLOWER", "INGREDIENT_GRASS_SEEDS"),
            enemyIds = listOf("ENEMY_FIELD_MOUSE")
        )
        
        nodes[LocationNodeId("GRASSLAND_PATH_NORTHEAST_2")] = LocationNode(
            id = LocationNodeId("GRASSLAND_PATH_NORTHEAST_2"),
            name = "Flower Fields",
            description = "Endless fields of wildflowers sway in the breeze.",
            type = NodeType.FILLER,
            position = NodePosition(0.65f, 0.35f),
            region = "Great Plains",
            connections = listOf(
                NodeConnection(LocationNodeId("GRASSLAND_PATH_NORTHEAST_1")),
                NodeConnection(LocationNodeId("PLAINS_VILLAGE"))
            ),
            resourceIds = listOf("INGREDIENT_WILDFLOWER", "INGREDIENT_POLLEN"),
            enemyIds = listOf("ENEMY_GRASSHOPPER")
        )
        
        // Plains village
        nodes[LocationNodeId("PLAINS_VILLAGE")] = LocationNode(
            id = LocationNodeId("PLAINS_VILLAGE"),
            name = "Meadowbrook Village",
            description = "A peaceful quail village nestled among the flowers. Smoke rises from tiny chimneys.",
            type = NodeType.HUB,
            position = NodePosition(0.70f, 0.30f),
            region = "Great Plains",
            connections = listOf(
                NodeConnection(LocationNodeId("GRASSLAND_PATH_NORTHEAST_2")),
                NodeConnection(LocationNodeId("WINDMILL")),
                NodeConnection(LocationNodeId("WHEAT_FIELDS"))
            ),
            npcIds = listOf("NPC_VILLAGE_ELDER", "NPC_FARMER", "NPC_BAKER"),
            questIds = listOf("quest_village_troubles", "quest_harvest_festival"),
            ambientAudioId = "AUDIO_VILLAGE_PEACEFUL"
        )
        
        nodes[LocationNodeId("WINDMILL")] = LocationNode(
            id = LocationNodeId("WINDMILL"),
            name = "The Old Windmill",
            description = "A towering windmill creaks in the wind, grinding grain for the village.",
            type = NodeType.POINT_OF_INTEREST,
            position = NodePosition(0.72f, 0.28f),
            region = "Great Plains",
            connections = listOf(
                NodeConnection(LocationNodeId("PLAINS_VILLAGE"))
            ),
            npcIds = listOf("NPC_MILLER"),
            questIds = listOf("quest_windmill_repair"),
            ambientAudioId = "AUDIO_WINDMILL"
        )
        
        nodes[LocationNodeId("WHEAT_FIELDS")] = LocationNode(
            id = LocationNodeId("WHEAT_FIELDS"),
            name = "Golden Wheat Fields",
            description = "Vast fields of golden wheat stretch to the horizon.",
            type = NodeType.RESOURCE,
            position = NodePosition(0.73f, 0.32f),
            region = "Great Plains",
            connections = listOf(
                NodeConnection(LocationNodeId("PLAINS_VILLAGE")),
                NodeConnection(LocationNodeId("PLAINS_NEST_SCRAPE"))
            ),
            resourceIds = listOf("INGREDIENT_WHEAT", "INGREDIENT_GRAIN"),
            ambientAudioId = "AUDIO_WHEAT_FIELDS"
        )
        
        // Plains Nest Scrape
        nodes[LocationNodeId("PLAINS_NEST_SCRAPE")] = LocationNode(
            id = LocationNodeId("PLAINS_NEST_SCRAPE"),
            name = "Haystack Nest Scrape",
            description = "A warm, comfortable nest hidden in a haystack.",
            type = NodeType.NEST_SCRAPE,
            position = NodePosition(0.75f, 0.33f),
            region = "Great Plains",
            connections = listOf(
                NodeConnection(LocationNodeId("WHEAT_FIELDS"))
            ),
            isNestScrape = true,
            ambientAudioId = "AUDIO_PLAINS_PEACEFUL"
        )
    }
}
