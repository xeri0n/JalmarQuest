package com.jalmarquest.core.state.catalogs

import com.jalmarquest.core.model.*

/**
 * Central registry of all world regions.
 * Organizes the 50+ locations into discoverable zones.
 */
class WorldRegionCatalog {
    private val regions = mutableMapOf<WorldRegionId, WorldRegion>()
    
    init {
        registerDefaultRegions()
    }
    
    /**
     * Register a world region.
     */
    fun registerRegion(region: WorldRegion) {
        regions[region.id] = region
    }
    
    /**
     * Get a region by ID.
     */
    fun getRegionById(id: WorldRegionId): WorldRegion? = regions[id]
    
    /**
     * Get all regions.
     */
    fun getAllRegions(): List<WorldRegion> = regions.values.toList()
    
    /**
     * Get accessible regions based on player state.
     */
    fun getAccessibleRegions(
        discoveredRegions: Set<WorldRegionId>,
        playerLevel: Int,
        completedQuests: Set<String>
    ): List<WorldRegion> {
        return regions.values.filter { region ->
            val requirement = region.unlockRequirement
            requirement == null || 
            isRequirementMet(requirement, discoveredRegions, playerLevel, completedQuests)
        }
    }
    
    private fun isRequirementMet(
        requirement: RegionUnlockRequirement,
        discoveredRegions: Set<WorldRegionId>,
        playerLevel: Int,
        completedQuests: Set<String>
    ): Boolean {
        return when (requirement) {
            is RegionUnlockRequirement.QuestCompletion -> 
                requirement.questId in completedQuests
            
            is RegionUnlockRequirement.MinimumLevel -> 
                playerLevel >= requirement.level
            
            is RegionUnlockRequirement.DiscoverRegion -> 
                requirement.regionId in discoveredRegions
            
            is RegionUnlockRequirement.AllOf -> 
                requirement.requirements.all { 
                    isRequirementMet(it, discoveredRegions, playerLevel, completedQuests) 
                }
        }
    }
    
    /**
     * Register all default regions from the game world.
     */
    private fun registerDefaultRegions() {
        // BUTTONBURGH - Starting hub (always accessible)
        registerRegion(
            WorldRegion(
                id = WorldRegionId("buttonburgh"),
                name = "Buttonburgh",
                description = "A thriving quail settlement built in an abandoned mouse burrow. " +
                    "Safe streets, warm lanterns, and friendly faces make this the heart of civilization.",
                biomeType = BiomeType.TOWN,
                difficultyLevel = 1,
                isDiscovered = true,
                unlockRequirement = null, // Always accessible
                primaryLocationIds = listOf(
                    "buttonburgh_centre",
                    "buttonburgh_quills_study",
                    "buttonburgh_alchemy_lab",
                    "buttonburgh_shop",
                    "buttonburgh_hoard_vault",
                    "buttonburgh_dustbath",
                    "buttonburgh_orphanage",
                    "buttonburgh_market_square"
                )
            )
        )
        
        // WHISPERING FOREST - First exploration zone
        registerRegion(
            WorldRegion(
                id = WorldRegionId("forest"),
                name = "Whispering Forest",
                description = "Ancient woodland where towering trees dwarf even the largest predators. " +
                    "Danger lurks in every shadow, but so do wonders beyond imagination.",
                biomeType = BiomeType.FOREST,
                difficultyLevel = 3,
                isDiscovered = false,
                unlockRequirement = RegionUnlockRequirement.MinimumLevel(2),
                primaryLocationIds = listOf(
                    "forest_crows_perch",
                    "forest_ant_hill",
                    "forest_whispering_pines",
                    "forest_mushroom_grove",
                    "forest_babbling_brook",
                    "forest_fallen_oak",
                    "forest_canopy_heights",
                    "forest_spider_webs",
                    "forest_fern_tunnel",
                    "forest_woodpeckers_tree"
                ),
                availableResourceNodes = listOf(
                    ResourceNodeId("node_forest_mushroom_patch"),
                    ResourceNodeId("node_forest_moondew_herb"),
                    ResourceNodeId("node_forest_iron_deposit"),
                    ResourceNodeId("node_forest_nature_essence")
                )
            )
        )
        
        // SUNLIT BEACH - Coastal zone
        registerRegion(
            WorldRegion(
                id = WorldRegionId("beach"),
                name = "Sunlit Beach",
                description = "Where land meets the endless water. The sand shifts beneath tiny feet, " +
                    "and the tide brings both treasures and terrors from the deep.",
                biomeType = BiomeType.BEACH,
                difficultyLevel = 4,
                isDiscovered = false,
                unlockRequirement = RegionUnlockRequirement.AllOf(
                    listOf(
                        RegionUnlockRequirement.MinimumLevel(3),
                        RegionUnlockRequirement.DiscoverRegion(WorldRegionId("forest"))
                    )
                ),
                primaryLocationIds = listOf(
                    "beach_tide_pools",
                    "beach_driftwood_maze",
                    "beach_seashell_grotto"
                ),
                availableResourceNodes = listOf(
                    ResourceNodeId("node_beach_sea_herb"),
                    ResourceNodeId("node_beach_shell_deposit"),
                    ResourceNodeId("node_beach_water_essence")
                )
            )
        )
        
        // MEADOW EXPANSE - Grassland zone
        registerRegion(
            WorldRegion(
                id = WorldRegionId("meadow"),
                name = "Meadow Expanse",
                description = "Rolling fields of wildflowers and tall grass. Open skies mean hawk danger, " +
                    "but the seeds and insects here are plentiful.",
                biomeType = BiomeType.GRASSLAND,
                difficultyLevel = 2,
                isDiscovered = false,
                unlockRequirement = RegionUnlockRequirement.QuestCompletion("quest_first_steps"),
                primaryLocationIds = listOf(
                    "meadow_wildflower_patch",
                    "meadow_old_stone_wall",
                    "meadow_rabbit_warren",
                    "meadow_hawk_shadow"
                ),
                availableResourceNodes = listOf(
                    ResourceNodeId("node_meadow_wildflower"),
                    ResourceNodeId("node_meadow_healing_grass"),
                    ResourceNodeId("node_meadow_stone_outcrop")
                )
            )
        )
        
        // HIDDEN GARDEN - High-level cultivated area
        registerRegion(
            WorldRegion(
                id = WorldRegionId("garden"),
                name = "Hidden Garden",
                description = "The legendary human garden, bursting with exotic seeds and plants. " +
                    "Guarded by cats and gardeners, this paradise is not easily reached.",
                biomeType = BiomeType.GARDEN,
                difficultyLevel = 7,
                isDiscovered = false,
                unlockRequirement = RegionUnlockRequirement.AllOf(
                    listOf(
                        RegionUnlockRequirement.MinimumLevel(6),
                        RegionUnlockRequirement.QuestCompletion("quest_garden_rumors")
                    )
                ),
                primaryLocationIds = listOf(
                    "garden_vegetable_patch",
                    "garden_herb_spiral",
                    "garden_compost_heap",
                    "garden_greenhouse"
                ),
                availableResourceNodes = listOf(
                    ResourceNodeId("node_garden_exotic_herb"),
                    ResourceNodeId("node_garden_compost_essence")
                )
            )
        )
        
        // MOUNTAIN RIDGE - High difficulty zone
        registerRegion(
            WorldRegion(
                id = WorldRegionId("mountain"),
                name = "Mountain Ridge",
                description = "Rocky peaks where the air grows thin and predators grow bold. " +
                    "Few quails dare venture this high, but legends speak of ancient secrets.",
                biomeType = BiomeType.MOUNTAIN,
                difficultyLevel = 6,
                isDiscovered = false,
                unlockRequirement = RegionUnlockRequirement.AllOf(
                    listOf(
                        RegionUnlockRequirement.MinimumLevel(5),
                        RegionUnlockRequirement.DiscoverRegion(WorldRegionId("forest")),
                        RegionUnlockRequirement.DiscoverRegion(WorldRegionId("meadow"))
                    )
                ),
                primaryLocationIds = listOf(
                    "mountain_rocky_outcrop",
                    "mountain_cliff_face",
                    "mountain_eagles_nest",
                    "mountain_cave_entrance"
                ),
                availableResourceNodes = listOf(
                    ResourceNodeId("node_mountain_frost_herb"),
                    ResourceNodeId("node_mountain_ore_vein"),
                    ResourceNodeId("node_mountain_ice_essence")
                )
            )
        )
        
        // WETLANDS - Mid-level marsh zone
        registerRegion(
            WorldRegion(
                id = WorldRegionId("wetland"),
                name = "Murky Wetlands",
                description = "Where water pools in shallow marshes, reeds grow tall, and frogs sing. " +
                    "The mud can trap unwary travelers, but rare plants thrive here.",
                biomeType = BiomeType.WETLAND,
                difficultyLevel = 5,
                isDiscovered = false,
                unlockRequirement = RegionUnlockRequirement.AllOf(
                    listOf(
                        RegionUnlockRequirement.MinimumLevel(4),
                        RegionUnlockRequirement.DiscoverRegion(WorldRegionId("beach"))
                    )
                ),
                primaryLocationIds = listOf(
                    "wetland_cattail_stand",
                    "wetland_frog_pond",
                    "wetland_reed_maze",
                    "wetland_heron_territory"
                ),
                availableResourceNodes = listOf(
                    ResourceNodeId("node_wetland_bog_herb"),
                    ResourceNodeId("node_wetland_murk_mineral"),
                    ResourceNodeId("node_wetland_decay_essence")
                )
            )
        )
        
        // ANCIENT RUINS - Lore-heavy zone
        registerRegion(
            WorldRegion(
                id = WorldRegionId("ruins"),
                name = "Ancient Ruins",
                description = "Crumbling stone structures from a forgotten age. What built these monuments? " +
                    "The answers may lie buried beneath moss and time.",
                biomeType = BiomeType.RUINS,
                difficultyLevel = 8,
                isDiscovered = false,
                unlockRequirement = RegionUnlockRequirement.AllOf(
                    listOf(
                        RegionUnlockRequirement.MinimumLevel(7),
                        RegionUnlockRequirement.QuestCompletion("quest_ancient_mysteries")
                    )
                ),
                primaryLocationIds = listOf(
                    "ruins_collapsed_temple",
                    "ruins_overgrown_courtyard",
                    "ruins_underground_chamber",
                    "ruins_inscription_wall"
                ),
                availableResourceNodes = listOf(
                    ResourceNodeId("node_ruins_cursed_herb"),
                    ResourceNodeId("node_ruins_ancient_mineral"),
                    ResourceNodeId("node_ruins_arcane_essence")
                )
            )
        )
    }
}
