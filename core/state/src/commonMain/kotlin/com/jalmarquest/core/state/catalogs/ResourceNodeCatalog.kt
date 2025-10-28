package com.jalmarquest.core.state.catalogs

import com.jalmarquest.core.model.*

/**
 * Central registry of all harvestable resource nodes.
 * Alpha 2.3: Reagent & Recipe System Overhaul
 * 
 * Resource nodes provide crafting reagents through foraging/gathering.
 * - RARE_HERB_NODE: Medicinal herbs, alchemical plants (Alchemy skill)
 * - RARE_MINERAL_NODE: Ore, stone, metal fragments (Combat/Forging skill)
 * - RARE_ESSENCE_NODE: Magical essences, elemental cores (Alchemy/Scholarship skill)
 */
class ResourceNodeCatalog {
    private val nodes = mutableMapOf<ResourceNodeId, ResourceNode>()
    
    init {
        registerDefaultNodes()
    }
    
    /**
     * Register a resource node.
     */
    fun registerNode(node: ResourceNode) {
        nodes[node.id] = node
    }
    
    /**
     * Get a node by ID.
     */
    fun getNodeById(id: ResourceNodeId): ResourceNode? = nodes[id]
    
    /**
     * Get all nodes.
     */
    fun getAllNodes(): List<ResourceNode> = nodes.values.toList()
    
    /**
     * Get nodes by type.
     */
    fun getNodesByType(type: ResourceNodeType): List<ResourceNode> =
        nodes.values.filter { it.nodeType == type }
    
    /**
     * Register all default resource nodes.
     */
    private fun registerDefaultNodes() {
        // ===== BUTTONBURGH (Town) - No rare nodes, safe zone =====
        // No resource nodes in town
        
        // ===== WHISPERING FOREST - Common herbs, moderate minerals, rare essences =====
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_forest_mushroom_patch"),
                nodeType = ResourceNodeType.RARE_HERB_NODE,
                name = "Ancient Mushroom Patch",
                description = "Bioluminescent fungi grow in clusters around rotting logs. Prized by alchemists.",
                baseDifficultyLevel = 2,
                harvestTime = 8,
                respawnTime = 600, // 10 minutes
                lootTable = listOf(
                    NodeLootDrop("ingredient_glowing_mushroom", 0.8f, 1, 3),
                    NodeLootDrop("ingredient_forest_spore", 0.6f, 1, 2),
                    NodeLootDrop("ingredient_rich_soil", 0.4f, 1, 1)
                )
            )
        )
        
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_forest_moondew_herb"),
                nodeType = ResourceNodeType.RARE_HERB_NODE,
                name = "Moondew Fern Cluster",
                description = "Rare ferns that absorb moonlight. Their fronds shimmer with alchemical potential.",
                baseDifficultyLevel = 3,
                harvestTime = 10,
                respawnTime = 900, // 15 minutes
                lootTable = listOf(
                    NodeLootDrop("ingredient_moondew_frond", 0.7f, 1, 2),
                    NodeLootDrop("ingredient_dew_essence", 0.5f, 1, 1),
                    NodeLootDrop("ingredient_forest_spore", 0.3f, 1, 1)
                )
            )
        )
        
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_forest_iron_deposit"),
                nodeType = ResourceNodeType.RARE_MINERAL_NODE,
                name = "Exposed Iron Vein",
                description = "Rusty iron ore protrudes from the earth. Perfect for basic weapon forging.",
                baseDifficultyLevel = 3,
                harvestTime = 12,
                respawnTime = 1200, // 20 minutes
                lootTable = listOf(
                    NodeLootDrop("ingredient_iron_ore_fragment", 0.9f, 2, 4),
                    NodeLootDrop("ingredient_stone_scale", 0.5f, 1, 2),
                    NodeLootDrop("ingredient_fine_sand", 0.3f, 1, 1)
                )
            )
        )
        
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_forest_nature_essence"),
                nodeType = ResourceNodeType.RARE_ESSENCE_NODE,
                name = "Whispering Spirit Wisp",
                description = "A floating mote of forest magic. Capturing it requires patience and scholarship.",
                baseDifficultyLevel = 4,
                harvestTime = 15,
                respawnTime = 1800, // 30 minutes
                lootTable = listOf(
                    NodeLootDrop("ingredient_nature_essence", 0.6f, 1, 1),
                    NodeLootDrop("ingredient_ethereal_wisp", 0.4f, 1, 1),
                    NodeLootDrop("ingredient_forest_spore", 0.5f, 1, 2)
                )
            )
        )
        
        // ===== SUNLIT BEACH - Uncommon minerals, water essences =====
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_beach_sea_herb"),
                nodeType = ResourceNodeType.RARE_HERB_NODE,
                name = "Kelp Tangle",
                description = "Seaweed rich in minerals and salts. Used in coastal alchemy traditions.",
                baseDifficultyLevel = 3,
                harvestTime = 7,
                respawnTime = 600,
                lootTable = listOf(
                    NodeLootDrop("ingredient_sea_kelp", 0.8f, 2, 4),
                    NodeLootDrop("ingredient_sea_salt", 0.9f, 2, 5),
                    NodeLootDrop("ingredient_regenerative_tissue", 0.3f, 1, 1)
                )
            )
        )
        
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_beach_shell_deposit"),
                nodeType = ResourceNodeType.RARE_MINERAL_NODE,
                name = "Fossilized Shell Bed",
                description = "Ancient mollusk shells hardened into mineral deposits. Useful for crafting.",
                baseDifficultyLevel = 4,
                harvestTime = 10,
                respawnTime = 900,
                lootTable = listOf(
                    NodeLootDrop("ingredient_hardened_shell", 0.8f, 2, 4),
                    NodeLootDrop("ingredient_tough_shell_fragment", 0.6f, 1, 2),
                    NodeLootDrop("ingredient_fine_sand", 0.7f, 2, 3)
                )
            )
        )
        
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_beach_water_essence"),
                nodeType = ResourceNodeType.RARE_ESSENCE_NODE,
                name = "Tide Pool Spirit",
                description = "Where sea meets shore, elemental water magic pools in concentrated form.",
                baseDifficultyLevel = 5,
                harvestTime = 12,
                respawnTime = 1500,
                lootTable = listOf(
                    NodeLootDrop("ingredient_water_essence", 0.6f, 1, 2),
                    NodeLootDrop("ingredient_sea_salt", 0.5f, 1, 2),
                    NodeLootDrop("ingredient_regenerative_tissue", 0.4f, 1, 1)
                )
            )
        )
        
        // ===== MEADOW EXPANSE - Common herbs, few minerals =====
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_meadow_wildflower"),
                nodeType = ResourceNodeType.RARE_HERB_NODE,
                name = "Wildflower Bloom",
                description = "Vibrant flowers with medicinal properties. The bees jealously guard them.",
                baseDifficultyLevel = 2,
                harvestTime = 6,
                respawnTime = 450,
                lootTable = listOf(
                    NodeLootDrop("ingredient_wildflower_petal", 0.9f, 3, 6),
                    NodeLootDrop("ingredient_pollen_dust", 0.7f, 2, 4),
                    NodeLootDrop("ingredient_dew_essence", 0.3f, 1, 1)
                )
            )
        )
        
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_meadow_healing_grass"),
                nodeType = ResourceNodeType.RARE_HERB_NODE,
                name = "Healing Grass Tuft",
                description = "Known to quails for generations. A single blade can soothe minor wounds.",
                baseDifficultyLevel = 1,
                harvestTime = 5,
                respawnTime = 300,
                lootTable = listOf(
                    NodeLootDrop("ingredient_healing_grass", 0.9f, 2, 5),
                    NodeLootDrop("ingredient_rich_soil", 0.5f, 1, 2)
                )
            )
        )
        
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_meadow_stone_outcrop"),
                nodeType = ResourceNodeType.RARE_MINERAL_NODE,
                name = "Weathered Stone Pile",
                description = "Crumbling stones left by ancient glaciers. Contains trace minerals.",
                baseDifficultyLevel = 2,
                harvestTime = 8,
                respawnTime = 900,
                lootTable = listOf(
                    NodeLootDrop("ingredient_stone_scale", 0.7f, 1, 3),
                    NodeLootDrop("ingredient_fine_sand", 0.8f, 2, 4),
                    NodeLootDrop("ingredient_iron_ore_fragment", 0.3f, 1, 1)
                )
            )
        )
        
        // ===== HIDDEN GARDEN - Rare herbs, cultivated plants, no minerals =====
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_garden_exotic_herb"),
                nodeType = ResourceNodeType.RARE_HERB_NODE,
                name = "Human Herb Garden",
                description = "Cultivated herbs from distant lands. Each plant tells a story of human cultivation.",
                baseDifficultyLevel = 6,
                harvestTime = 10,
                respawnTime = 1200,
                lootTable = listOf(
                    NodeLootDrop("ingredient_exotic_basil", 0.7f, 1, 2),
                    NodeLootDrop("ingredient_rare_sage", 0.6f, 1, 2),
                    NodeLootDrop("ingredient_precious_thyme", 0.5f, 1, 1),
                    NodeLootDrop("ingredient_wildflower_petal", 0.8f, 2, 4)
                )
            )
        )
        
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_garden_compost_essence"),
                nodeType = ResourceNodeType.RARE_ESSENCE_NODE,
                name = "Compost Heap Core",
                description = "At the heart of decay lies fertile magic. Life essence concentrated.",
                baseDifficultyLevel = 7,
                harvestTime = 15,
                respawnTime = 2400,
                lootTable = listOf(
                    NodeLootDrop("ingredient_life_essence", 0.6f, 1, 2),
                    NodeLootDrop("ingredient_nature_essence", 0.5f, 1, 1),
                    NodeLootDrop("ingredient_rich_soil", 0.9f, 3, 5)
                )
            )
        )
        
        // ===== MOUNTAIN RIDGE - Rare minerals, frost essences, hardy plants =====
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_mountain_frost_herb"),
                nodeType = ResourceNodeType.RARE_HERB_NODE,
                name = "Frost Lichen Patch",
                description = "Resilient lichen that grows on frozen stone. Contains cold-resistant compounds.",
                baseDifficultyLevel = 6,
                harvestTime = 10,
                respawnTime = 1500,
                lootTable = listOf(
                    NodeLootDrop("ingredient_frost_lichen", 0.8f, 1, 3),
                    NodeLootDrop("ingredient_frost_crystal", 0.6f, 1, 2),
                    NodeLootDrop("ingredient_coarse_wool", 0.3f, 1, 1) // From mountain goats
                )
            )
        )
        
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_mountain_ore_vein"),
                nodeType = ResourceNodeType.RARE_MINERAL_NODE,
                name = "High-Grade Ore Deposit",
                description = "Rich veins of precious metals exposed by erosion. Difficult but rewarding.",
                baseDifficultyLevel = 7,
                harvestTime = 15,
                respawnTime = 1800,
                lootTable = listOf(
                    NodeLootDrop("ingredient_silver_ore", 0.7f, 1, 2),
                    NodeLootDrop("ingredient_iron_ore_fragment", 0.9f, 3, 5),
                    NodeLootDrop("ingredient_stone_scale", 0.8f, 2, 4),
                    NodeLootDrop("ingredient_refined_mythril", 0.2f, 1, 1) // Very rare
                )
            )
        )
        
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_mountain_ice_essence"),
                nodeType = ResourceNodeType.RARE_ESSENCE_NODE,
                name = "Eternal Ice Shard",
                description = "Ice that never melts, infused with elemental cold. Handle with care.",
                baseDifficultyLevel = 7,
                harvestTime = 12,
                respawnTime = 2100,
                lootTable = listOf(
                    NodeLootDrop("ingredient_ice_essence", 0.7f, 1, 2),
                    NodeLootDrop("ingredient_frost_crystal", 0.8f, 2, 3),
                    NodeLootDrop("ingredient_elemental_essence", 0.4f, 1, 1)
                )
            )
        )
        
        // ===== MURKY WETLANDS - Poison herbs, bog essences, murky minerals =====
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_wetland_bog_herb"),
                nodeType = ResourceNodeType.RARE_HERB_NODE,
                name = "Bog Root Cluster",
                description = "Medicinal roots that thrive in stagnant water. Used in antidotes and poisons.",
                baseDifficultyLevel = 5,
                harvestTime = 9,
                respawnTime = 1200,
                lootTable = listOf(
                    NodeLootDrop("ingredient_bog_root", 0.8f, 2, 4),
                    NodeLootDrop("ingredient_toxic_secretion", 0.6f, 1, 2),
                    NodeLootDrop("ingredient_carnivorous_sap", 0.4f, 1, 1)
                )
            )
        )
        
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_wetland_murk_mineral"),
                nodeType = ResourceNodeType.RARE_MINERAL_NODE,
                name = "Murky Clay Deposit",
                description = "Dense clay mixed with minerals. Useful for crafting protective gear.",
                baseDifficultyLevel = 4,
                harvestTime = 10,
                respawnTime = 1200,
                lootTable = listOf(
                    NodeLootDrop("ingredient_dense_clay", 0.9f, 3, 5),
                    NodeLootDrop("ingredient_fine_sand", 0.7f, 2, 3),
                    NodeLootDrop("ingredient_iron_ore_fragment", 0.4f, 1, 2)
                )
            )
        )
        
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_wetland_decay_essence"),
                nodeType = ResourceNodeType.RARE_ESSENCE_NODE,
                name = "Swamp Spirit Vapor",
                description = "Where life decays, dark magic gathers. Dangerous to harvest, potent in alchemy.",
                baseDifficultyLevel = 6,
                harvestTime = 14,
                respawnTime = 1800,
                lootTable = listOf(
                    NodeLootDrop("ingredient_decay_essence", 0.6f, 1, 2),
                    NodeLootDrop("ingredient_shadow_essence", 0.4f, 1, 1),
                    NodeLootDrop("ingredient_toxic_secretion", 0.5f, 1, 2)
                )
            )
        )
        
        // ===== ANCIENT RUINS - Arcane essences, rare minerals, corrupted herbs =====
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_ruins_cursed_herb"),
                nodeType = ResourceNodeType.RARE_HERB_NODE,
                name = "Cursed Nightshade",
                description = "Plants twisted by ancient magic. Deadly poison or powerful cure, depending on skill.",
                baseDifficultyLevel = 8,
                harvestTime = 12,
                respawnTime = 2400,
                lootTable = listOf(
                    NodeLootDrop("ingredient_cursed_nightshade", 0.7f, 1, 2),
                    NodeLootDrop("ingredient_cursed_dust", 0.6f, 1, 2),
                    NodeLootDrop("ingredient_venom_sac", 0.5f, 1, 1)
                )
            )
        )
        
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_ruins_ancient_mineral"),
                nodeType = ResourceNodeType.RARE_MINERAL_NODE,
                name = "Runic Stone Fragment",
                description = "Stones carved with unknown runes. The symbols still pulse with faint energy.",
                baseDifficultyLevel = 8,
                harvestTime = 15,
                respawnTime = 2400,
                lootTable = listOf(
                    NodeLootDrop("ingredient_runic_stone", 0.7f, 1, 2),
                    NodeLootDrop("ingredient_ancient_scales", 0.5f, 1, 1),
                    NodeLootDrop("ingredient_scrap_metal", 0.8f, 2, 4)
                )
            )
        )
        
        registerNode(
            ResourceNode(
                id = ResourceNodeId("node_ruins_arcane_essence"),
                nodeType = ResourceNodeType.RARE_ESSENCE_NODE,
                name = "Arcane Rift Fragment",
                description = "A tear in reality where raw magic leaks through. The most potent essence source.",
                baseDifficultyLevel = 9,
                harvestTime = 18,
                respawnTime = 3600, // 1 hour
                lootTable = listOf(
                    NodeLootDrop("ingredient_arcane_essence", 0.6f, 1, 1),
                    NodeLootDrop("ingredient_arcane_core_fragment", 0.4f, 1, 1),
                    NodeLootDrop("ingredient_ancient_magic_essence", 0.3f, 1, 1),
                    NodeLootDrop("ingredient_elemental_essence", 0.5f, 1, 2)
                )
            )
        )
    }
}
