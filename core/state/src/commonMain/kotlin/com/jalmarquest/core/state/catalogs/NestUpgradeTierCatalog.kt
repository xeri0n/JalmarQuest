package com.jalmarquest.core.state.catalogs

import com.jalmarquest.core.model.*

/**
 * Alpha 2.3: Nest Upgrade Tier Catalog
 * 
 * Defines costs and requirements for all 3 tiers of the 6 functional nest upgrades.
 * Each tier provides progressively better bonuses at higher costs.
 */

/**
 * Definition of an upgrade tier's costs and requirements.
 */
data class UpgradeTierDefinition(
    val tier: UpgradeTier,
    val seedCost: Int,
    val glimmerCost: Int,
    val requiredIngredients: Map<IngredientId, Int> = emptyMap(),
    val requiredPlayerLevel: Int = 1,
    val prerequisiteTier: UpgradeTier? = null, // Must have this tier before upgrading
    val bonusDescription: String
)

/**
 * Complete upgrade definition with all 3 tiers.
 */
data class NestUpgradeDefinition(
    val upgradeType: FunctionalUpgradeType,
    val name: String,
    val description: String,
    val tiers: Map<UpgradeTier, UpgradeTierDefinition>
)

/**
 * Catalog of all nest upgrade tiers.
 */
class NestUpgradeTierCatalog {
    private val upgrades = mutableMapOf<FunctionalUpgradeType, NestUpgradeDefinition>()
    
    init {
        registerDefaultUpgrades()
    }
    
    /**
     * Get upgrade definition by type.
     */
    fun getUpgrade(type: FunctionalUpgradeType): NestUpgradeDefinition? = upgrades[type]
    
    /**
     * Get tier definition for a specific upgrade and tier.
     */
    fun getTierDefinition(type: FunctionalUpgradeType, tier: UpgradeTier): UpgradeTierDefinition? {
        return upgrades[type]?.tiers?.get(tier)
    }
    
    /**
     * Check if player can afford an upgrade.
     */
    fun canAffordUpgrade(
        type: FunctionalUpgradeType,
        tier: UpgradeTier,
        playerSeeds: Int,
        playerGlimmer: Int,
        ingredientInventory: Map<IngredientId, Int>
    ): Boolean {
        val tierDef = getTierDefinition(type, tier) ?: return false
        
        if (playerSeeds < tierDef.seedCost) return false
        if (playerGlimmer < tierDef.glimmerCost) return false
        
        tierDef.requiredIngredients.forEach { (ingredientId, required) ->
            val available = ingredientInventory[ingredientId] ?: 0
            if (available < required) return false
        }
        
        return true
    }
    
    /**
     * Register all default nest upgrades with 3 tiers each.
     */
    private fun registerDefaultUpgrades() {
        // SHINY_DISPLAY: Hoard Collection Showcase
        upgrades[FunctionalUpgradeType.SHINY_DISPLAY] = NestUpgradeDefinition(
            upgradeType = FunctionalUpgradeType.SHINY_DISPLAY,
            name = "Hoard Collection Display",
            description = "A magnificent display case for your most prized shinies. Increases hoard XP gains.",
            tiers = mapOf(
                UpgradeTier.TIER_1 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_1,
                    seedCost = 500,
                    glimmerCost = 1000,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_spider_silk") to 10,
                        IngredientId("ingredient_iron_ore_fragment") to 5
                    ),
                    requiredPlayerLevel = 3,
                    bonusDescription = "+10% Hoard XP"
                ),
                UpgradeTier.TIER_2 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_2,
                    seedCost = 1500,
                    glimmerCost = 3000,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_silver_ore") to 8,
                        IngredientId("ingredient_precious_gem_dust") to 3,
                        IngredientId("ingredient_fine_silk_thread") to 5
                    ),
                    requiredPlayerLevel = 6,
                    prerequisiteTier = UpgradeTier.TIER_1,
                    bonusDescription = "+20% Hoard XP • Expanded Display Capacity"
                ),
                UpgradeTier.TIER_3 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_3,
                    seedCost = 4000,
                    glimmerCost = 8000,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_mythril_alloy") to 5,
                        IngredientId("ingredient_arcane_essence") to 2,
                        IngredientId("ingredient_legendary_feather") to 4
                    ),
                    requiredPlayerLevel = 9,
                    prerequisiteTier = UpgradeTier.TIER_2,
                    bonusDescription = "+30% Hoard XP • Magical Illumination • Rotating Display"
                )
            )
        )
        
        // SEED_SILO: Seed Storage Expansion
        upgrades[FunctionalUpgradeType.SEED_SILO] = NestUpgradeDefinition(
            upgradeType = FunctionalUpgradeType.SEED_SILO,
            name = "Seed Storage Silo",
            description = "Expanded seed storage capacity with preservation enchantments.",
            tiers = mapOf(
                UpgradeTier.TIER_1 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_1,
                    seedCost = 800,
                    glimmerCost = 800,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_sturdy_chitin") to 15,
                        IngredientId("ingredient_hardened_shell") to 10,
                        IngredientId("ingredient_dense_clay") to 8
                    ),
                    requiredPlayerLevel = 2,
                    bonusDescription = "+50% Seed Storage Capacity"
                ),
                UpgradeTier.TIER_2 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_2,
                    seedCost = 2000,
                    glimmerCost = 2500,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_iron_ingot") to 10,
                        IngredientId("ingredient_shell_armor_plate") to 6,
                        IngredientId("ingredient_nature_essence") to 3
                    ),
                    requiredPlayerLevel = 5,
                    prerequisiteTier = UpgradeTier.TIER_1,
                    bonusDescription = "+100% Seed Storage • Preservation Enchantment (seeds never spoil)"
                ),
                UpgradeTier.TIER_3 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_3,
                    seedCost = 5000,
                    glimmerCost = 7000,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_mythril_alloy") to 8,
                        IngredientId("ingredient_life_crystal") to 3,
                        IngredientId("ingredient_ancient_scales") to 5
                    ),
                    requiredPlayerLevel = 8,
                    prerequisiteTier = UpgradeTier.TIER_2,
                    bonusDescription = "+150% Seed Storage • Auto-Sorting • Duplication Chance (5%)"
                )
            )
        )
        
        // SMALL_LIBRARY: Thought Cabinet Expansion
        upgrades[FunctionalUpgradeType.SMALL_LIBRARY] = NestUpgradeDefinition(
            upgradeType = FunctionalUpgradeType.SMALL_LIBRARY,
            name = "Nest Library",
            description = "A collection of wisdom and lore that expands your mental capacity.",
            tiers = mapOf(
                UpgradeTier.TIER_1 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_1,
                    seedCost = 600,
                    glimmerCost = 1200,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_white_feather") to 12,
                        IngredientId("ingredient_black_feather") to 12,
                        IngredientId("item_blank_parchment") to 20
                    ),
                    requiredPlayerLevel = 3,
                    bonusDescription = "+2 Thought Cabinet Slots"
                ),
                UpgradeTier.TIER_2 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_2,
                    seedCost = 1800,
                    glimmerCost = 3500,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_runic_stone") to 6,
                        IngredientId("item_basic_ink") to 15,
                        IngredientId("ingredient_ethereal_wisp") to 4
                    ),
                    requiredPlayerLevel = 6,
                    prerequisiteTier = UpgradeTier.TIER_1,
                    bonusDescription = "+4 Thought Cabinet Slots • Faster Thought Research (20%)"
                ),
                UpgradeTier.TIER_3 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_3,
                    seedCost = 4500,
                    glimmerCost = 9000,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_arcane_catalyst") to 3,
                        IngredientId("ingredient_ancient_magic_essence") to 4,
                        IngredientId("ingredient_shadow_core") to 2
                    ),
                    requiredPlayerLevel = 9,
                    prerequisiteTier = UpgradeTier.TIER_2,
                    bonusDescription = "+6 Thought Cabinet Slots • Instant Slot Swap • Passive XP Gain"
                )
            )
        )
        
        // PERSONAL_ALCHEMY_STATION: In-Nest Alchemy
        upgrades[FunctionalUpgradeType.PERSONAL_ALCHEMY_STATION] = NestUpgradeDefinition(
            upgradeType = FunctionalUpgradeType.PERSONAL_ALCHEMY_STATION,
            name = "Personal Alchemy Station",
            description = "Craft potions and concoctions without leaving your nest.",
            tiers = mapOf(
                UpgradeTier.TIER_1 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_1,
                    seedCost = 1000,
                    glimmerCost = 1500,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_glowing_mushroom") to 8,
                        IngredientId("ingredient_toxic_secretion") to 6,
                        IngredientId("ingredient_iron_ore_fragment") to 10
                    ),
                    requiredPlayerLevel = 4,
                    bonusDescription = "Craft Alchemy Recipes in Nest • Basic Recipes Only"
                ),
                UpgradeTier.TIER_2 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_2,
                    seedCost = 2500,
                    glimmerCost = 4000,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_purified_herb_essence") to 5,
                        IngredientId("ingredient_elemental_essence") to 4,
                        IngredientId("ingredient_silver_bar") to 3
                    ),
                    requiredPlayerLevel = 7,
                    prerequisiteTier = UpgradeTier.TIER_1,
                    bonusDescription = "All Alchemy Recipes • +10% Success Rate • Batch Crafting (x3)"
                ),
                UpgradeTier.TIER_3 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_3,
                    seedCost = 6000,
                    glimmerCost = 10000,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_life_crystal") to 4,
                        IngredientId("ingredient_arcane_catalyst") to 2,
                        IngredientId("ingredient_ice_elemental_shard") to 3
                    ),
                    requiredPlayerLevel = 10,
                    prerequisiteTier = UpgradeTier.TIER_2,
                    bonusDescription = "+20% Success Rate • Batch Crafting (x5) • No Ingredient Cost (10% chance)"
                )
            )
        )
        
        // SMALL_WORKBENCH: In-Nest Crafting
        upgrades[FunctionalUpgradeType.SMALL_WORKBENCH] = NestUpgradeDefinition(
            upgradeType = FunctionalUpgradeType.SMALL_WORKBENCH,
            name = "Nest Workbench",
            description = "Craft weapons, armor, and tools from the comfort of your nest.",
            tiers = mapOf(
                UpgradeTier.TIER_1 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_1,
                    seedCost = 900,
                    glimmerCost = 1300,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_iron_ore_fragment") to 20,
                        IngredientId("ingredient_spider_silk") to 15,
                        IngredientId("ingredient_sturdy_chitin") to 12
                    ),
                    requiredPlayerLevel = 4,
                    bonusDescription = "Craft Basic Weapons & Armor in Nest"
                ),
                UpgradeTier.TIER_2 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_2,
                    seedCost = 2200,
                    glimmerCost = 3800,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_iron_ingot") to 12,
                        IngredientId("ingredient_fine_silk_thread") to 8,
                        IngredientId("ingredient_shell_armor_plate") to 6
                    ),
                    requiredPlayerLevel = 7,
                    prerequisiteTier = UpgradeTier.TIER_1,
                    bonusDescription = "All Crafting Recipes • +10% Durability • Tool Repair"
                ),
                UpgradeTier.TIER_3 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_3,
                    seedCost = 5500,
                    glimmerCost = 9500,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_mythril_alloy") to 6,
                        IngredientId("ingredient_precious_feather_down") to 4,
                        IngredientId("ingredient_shadow_core") to 2
                    ),
                    requiredPlayerLevel = 10,
                    prerequisiteTier = UpgradeTier.TIER_2,
                    bonusDescription = "+20% Durability • Masterwork Chance (15%) • Enchantment Slots"
                )
            )
        )
        
        // COZY_PERCH: Companion XP Boost
        upgrades[FunctionalUpgradeType.COZY_PERCH] = NestUpgradeDefinition(
            upgradeType = FunctionalUpgradeType.COZY_PERCH,
            name = "Cozy Companion Perch",
            description = "A comfortable resting spot that helps your companion grow stronger.",
            tiers = mapOf(
                UpgradeTier.TIER_1 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_1,
                    seedCost = 700,
                    glimmerCost = 1100,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_coarse_wool") to 10,
                        IngredientId("ingredient_white_feather") to 15,
                        IngredientId("ingredient_spider_silk") to 8
                    ),
                    requiredPlayerLevel = 3,
                    bonusDescription = "+5% Companion XP Gain"
                ),
                UpgradeTier.TIER_2 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_2,
                    seedCost = 2000,
                    glimmerCost = 3200,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_precious_feather_down") to 6,
                        IngredientId("ingredient_life_essence") to 3,
                        IngredientId("ingredient_fine_silk_thread") to 10
                    ),
                    requiredPlayerLevel = 6,
                    prerequisiteTier = UpgradeTier.TIER_1,
                    bonusDescription = "+10% Companion XP • Passive Stamina Regen • Mood Boost"
                ),
                UpgradeTier.TIER_3 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_3,
                    seedCost = 5000,
                    glimmerCost = 8500,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_legendary_feather") to 5,
                        IngredientId("ingredient_life_crystal") to 3,
                        IngredientId("ingredient_sky_essence") to 2
                    ),
                    requiredPlayerLevel = 9,
                    prerequisiteTier = UpgradeTier.TIER_2,
                    bonusDescription = "+15% Companion XP • Double Task Rewards • Trait XP Boost (25%)"
                )
            )
        )
        
        // Alpha 2.3 Part 2.2: COMPANION_ASSIGNMENT_BOARD
        upgrades[FunctionalUpgradeType.COMPANION_ASSIGNMENT_BOARD] = NestUpgradeDefinition(
            upgradeType = FunctionalUpgradeType.COMPANION_ASSIGNMENT_BOARD,
            name = "Companion Assignment Board",
            description = "Assign idle companions to passive tasks like foraging, scouting, and brewing.",
            tiers = mapOf(
                UpgradeTier.TIER_1 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_1,
                    seedCost = 1500,
                    glimmerCost = 2000,
                    requiredIngredients = mapOf(
                        IngredientId("item_basic_wood_plank") to 20,
                        IngredientId("ingredient_common_feather") to 10,
                        IngredientId("item_basic_ink") to 5
                    ),
                    requiredPlayerLevel = 3,
                    prerequisiteTier = null,
                    bonusDescription = "2 Concurrent Assignments • Basic Task Types"
                ),
                UpgradeTier.TIER_2 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_2,
                    seedCost = 3000,
                    glimmerCost = 4000,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_iron_ingot") to 15,
                        IngredientId("ingredient_ethereal_wisp") to 8,
                        IngredientId("ingredient_runic_stone") to 5
                    ),
                    requiredPlayerLevel = 6,
                    prerequisiteTier = UpgradeTier.TIER_1,
                    bonusDescription = "4 Concurrent Assignments • Advanced Tasks • +10% Task Speed"
                ),
                UpgradeTier.TIER_3 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_3,
                    seedCost = 6000,
                    glimmerCost = 9000,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_mythril_alloy") to 10,
                        IngredientId("ingredient_arcane_catalyst") to 6,
                        IngredientId("ingredient_ancient_magic_essence") to 4
                    ),
                    requiredPlayerLevel = 10,
                    prerequisiteTier = UpgradeTier.TIER_2,
                    bonusDescription = "6 Concurrent Assignments • Master Tasks • +25% Task Speed • Automatic Collection"
                )
            )
        )
        
        // Alpha 2.3 Part 2.2: LORE_ARCHIVE
        upgrades[FunctionalUpgradeType.LORE_ARCHIVE] = NestUpgradeDefinition(
            upgradeType = FunctionalUpgradeType.LORE_ARCHIVE,
            name = "Lore Archive",
            description = "Store and review discovered lore snippets, world events, and narrative moments.",
            tiers = mapOf(
                UpgradeTier.TIER_1 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_1,
                    seedCost = 1000,
                    glimmerCost = 1500,
                    requiredIngredients = mapOf(
                        IngredientId("item_basic_wood_plank") to 15,
                        IngredientId("item_basic_ink") to 10,
                        IngredientId("ingredient_common_feather") to 5
                    ),
                    requiredPlayerLevel = 2,
                    prerequisiteTier = null,
                    bonusDescription = "Store 20 Lore Entries • Basic Search • Chronological Sort"
                ),
                UpgradeTier.TIER_2 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_2,
                    seedCost = 2500,
                    glimmerCost = 3500,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_runic_stone") to 12,
                        IngredientId("item_basic_ink") to 20,
                        IngredientId("ingredient_shell_armor_plate") to 8
                    ),
                    requiredPlayerLevel = 5,
                    prerequisiteTier = UpgradeTier.TIER_1,
                    bonusDescription = "Store 50 Lore Entries • Advanced Search • Tag Filtering • Favorites"
                ),
                UpgradeTier.TIER_3 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_3,
                    seedCost = 5000,
                    glimmerCost = 8000,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_arcane_catalyst") to 8,
                        IngredientId("ingredient_ancient_magic_essence") to 12,
                        IngredientId("ingredient_shadow_core") to 6
                    ),
                    requiredPlayerLevel = 9,
                    prerequisiteTier = UpgradeTier.TIER_2,
                    bonusDescription = "Store 100 Lore Entries • Full-Text Search • AI Summaries • Export to Thought"
                )
            )
        )
        
        // Alpha 2.3 Part 2.2: AI_DIRECTOR_CONSOLE
        upgrades[FunctionalUpgradeType.AI_DIRECTOR_CONSOLE] = NestUpgradeDefinition(
            upgradeType = FunctionalUpgradeType.AI_DIRECTOR_CONSOLE,
            name = "AI Director Console",
            description = "Review AI-generated event history, world state analysis, and narrative debug info.",
            tiers = mapOf(
                UpgradeTier.TIER_1 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_1,
                    seedCost = 2000,
                    glimmerCost = 3000,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_iron_ingot") to 10,
                        IngredientId("ingredient_ethereal_wisp") to 8,
                        IngredientId("ingredient_nature_essence") to 5
                    ),
                    requiredPlayerLevel = 4,
                    prerequisiteTier = null,
                    bonusDescription = "View 10 Recent Events • Basic Event Details • Choice Tag Tracking"
                ),
                UpgradeTier.TIER_2 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_2,
                    seedCost = 4000,
                    glimmerCost = 6000,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_mythril_alloy") to 12,
                        IngredientId("ingredient_life_crystal") to 8,
                        IngredientId("ingredient_runic_stone") to 10
                    ),
                    requiredPlayerLevel = 7,
                    prerequisiteTier = UpgradeTier.TIER_1,
                    bonusDescription = "View 25 Recent Events • World State Analysis • Butterfly Effect Chains"
                ),
                UpgradeTier.TIER_3 to UpgradeTierDefinition(
                    tier = UpgradeTier.TIER_3,
                    seedCost = 7000,
                    glimmerCost = 10000,
                    requiredIngredients = mapOf(
                        IngredientId("ingredient_arcane_catalyst") to 10,
                        IngredientId("ingredient_ancient_magic_essence") to 15,
                        IngredientId("ingredient_legendary_feather") to 5
                    ),
                    requiredPlayerLevel = 10,
                    prerequisiteTier = UpgradeTier.TIER_2,
                    bonusDescription = "View 50 Recent Events • Full Narrative Debug • AI Prompt Inspector • Export Timeline"
                )
            )
        )
    }
}
