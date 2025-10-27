package com.jalmarquest.feature.nest

import com.jalmarquest.core.model.*

/**
 * Catalog of all available cosmetic items for the Nest.
 * 
 * Total Items: 50+
 * - Themes: 10 items (wallpaper, flooring, lighting)
 * - Furniture: 20 items (tables, chairs, shelves, storage)
 * - Decorations: 20+ items (plants, paintings, trinkets, rugs)
 * - Functional: 6 items (gameplay upgrades)
 */
object NestCosmeticCatalog {
    
    private val _allCosmetics = mutableListOf<CosmeticItem>()
    val allCosmetics: List<CosmeticItem> get() = _allCosmetics
    
    /**
     * Initialize the catalog with all cosmetic items.
     */
    fun registerAllCosmetics() {
        if (_allCosmetics.isNotEmpty()) return  // Already registered
        
        // THEMES (10 items) - Auto-apply, not placeable
        registerTheme(
            id = "theme_rustic_burrow",
            name = "Rustic Burrow",
            description = "Warm earth tones with natural wood grain and soft moss accents",
            rarity = CosmeticRarity.COMMON,
            baseGlimmer = 150
        )
        
        registerTheme(
            id = "theme_elegant_nest",
            name = "Elegant Nest",
            description = "Polished stone floors with silk tapestries and golden trim",
            rarity = CosmeticRarity.UNCOMMON,
            baseGlimmer = 400
        )
        
        registerTheme(
            id = "theme_forest_canopy",
            name = "Forest Canopy",
            description = "Living branches overhead with dappled sunlight filtering through leaves",
            rarity = CosmeticRarity.RARE,
            baseGlimmer = 700
        )
        
        registerTheme(
            id = "theme_crystal_cavern",
            name = "Crystal Cavern",
            description = "Shimmering gemstone walls that reflect ambient light in rainbow hues",
            rarity = CosmeticRarity.RARE,
            baseGlimmer = 850
        )
        
        registerTheme(
            id = "theme_autumn_harvest",
            name = "Autumn Harvest",
            description = "Warm amber tones with scattered fallen leaves and harvest decorations",
            rarity = CosmeticRarity.UNCOMMON,
            baseGlimmer = 500
        )
        
        registerTheme(
            id = "theme_winter_solstice",
            name = "Winter Solstice",
            description = "Frosted surfaces with gentle snowfall and cool blue lighting",
            rarity = CosmeticRarity.EPIC,
            baseGlimmer = 1200
        )
        
        registerTheme(
            id = "theme_spring_meadow",
            name = "Spring Meadow",
            description = "Fresh green grass flooring with blooming wildflowers along the walls",
            rarity = CosmeticRarity.UNCOMMON,
            baseGlimmer = 450
        )
        
        registerTheme(
            id = "theme_summer_beach",
            name = "Summer Beach",
            description = "Sandy floor with driftwood accents and gentle ocean sounds",
            rarity = CosmeticRarity.RARE,
            baseGlimmer = 800
        )
        
        registerTheme(
            id = "theme_starlit_night",
            name = "Starlit Night",
            description = "Dark indigo walls with twinkling constellations overhead",
            rarity = CosmeticRarity.EPIC,
            baseGlimmer = 1400
        )
        
        registerTheme(
            id = "theme_royal_chambers",
            name = "Royal Chambers",
            description = "Opulent purple velvet with gold filigree and chandelier lighting",
            rarity = CosmeticRarity.LEGENDARY,
            baseGlimmer = 2000,
            requirement = UnlockRequirement.HoardRank(10)
        )
        
        // FURNITURE (20 items) - Placeable, functional
        registerFurniture(
            id = "furniture_wooden_table",
            name = "Wooden Table",
            description = "Simple oak table perfect for displaying treasures",
            rarity = CosmeticRarity.COMMON,
            baseGlimmer = 100,
            maxInstances = 3
        )
        
        registerFurniture(
            id = "furniture_cozy_chair",
            name = "Cozy Chair",
            description = "Cushioned armchair upholstered in soft fabric",
            rarity = CosmeticRarity.COMMON,
            baseGlimmer = 120,
            maxInstances = 4
        )
        
        registerFurniture(
            id = "furniture_bookshelf",
            name = "Bookshelf",
            description = "Tall shelf for storing lore scrolls and tomes",
            rarity = CosmeticRarity.UNCOMMON,
            baseGlimmer = 400,
            maxInstances = 2
        )
        
        registerFurniture(
            id = "furniture_storage_chest",
            name = "Storage Chest",
            description = "Large wooden chest with iron bindings",
            rarity = CosmeticRarity.COMMON,
            baseGlimmer = 150,
            maxInstances = 5
        )
        
        registerFurniture(
            id = "furniture_hammock",
            name = "Hammock",
            description = "Woven hammock strung between two posts",
            rarity = CosmeticRarity.UNCOMMON,
            baseGlimmer = 350
        )
        
        registerFurniture(
            id = "furniture_canopy_bed",
            name = "Canopy Bed",
            description = "Luxurious bed with flowing curtains",
            rarity = CosmeticRarity.RARE,
            baseGlimmer = 900
        )
        
        registerFurniture(
            id = "furniture_writing_desk",
            name = "Writing Desk",
            description = "Elegant desk with quill and inkwell",
            rarity = CosmeticRarity.UNCOMMON,
            baseGlimmer = 450
        )
        
        registerFurniture(
            id = "furniture_trophy_case",
            name = "Trophy Case",
            description = "Glass display case for quest achievements",
            rarity = CosmeticRarity.RARE,
            baseGlimmer = 800,
            maxInstances = 3
        )
        
        registerFurniture(
            id = "furniture_alchemy_table",
            name = "Alchemy Table",
            description = "Worktable with beakers and mortar & pestle",
            rarity = CosmeticRarity.RARE,
            baseGlimmer = 950
        )
        
        registerFurniture(
            id = "furniture_crafting_bench",
            name = "Crafting Bench",
            description = "Sturdy workbench with tool rack",
            rarity = CosmeticRarity.RARE,
            baseGlimmer = 920
        )
        
        registerFurniture(
            id = "furniture_seed_barrel",
            name = "Seed Barrel",
            description = "Large barrel for seed storage",
            rarity = CosmeticRarity.UNCOMMON,
            baseGlimmer = 400,
            maxInstances = 2
        )
        
        registerFurniture(
            id = "furniture_display_pedestal",
            name = "Display Pedestal",
            description = "Ornate pedestal for showcasing special items",
            rarity = CosmeticRarity.UNCOMMON,
            baseGlimmer = 380,
            maxInstances = 6
        )
        
        registerFurniture(
            id = "furniture_wardrobe",
            name = "Wardrobe",
            description = "Tall wardrobe for storing cosmetic outfits",
            rarity = CosmeticRarity.UNCOMMON,
            baseGlimmer = 500
        )
        
        registerFurniture(
            id = "furniture_mirror",
            name = "Standing Mirror",
            description = "Full-length mirror with gilded frame",
            rarity = CosmeticRarity.RARE,
            baseGlimmer = 700
        )
        
        registerFurniture(
            id = "furniture_fireplace",
            name = "Stone Fireplace",
            description = "Crackling fireplace providing warmth and ambiance",
            rarity = CosmeticRarity.EPIC,
            baseGlimmer = 1300
        )
        
        registerFurniture(
            id = "furniture_fountain",
            name = "Indoor Fountain",
            description = "Ornate fountain with gentle water sounds",
            rarity = CosmeticRarity.EPIC,
            baseGlimmer = 1500
        )
        
        registerFurniture(
            id = "furniture_chandelier",
            name = "Crystal Chandelier",
            description = "Elegant hanging chandelier with prismatic crystals",
            rarity = CosmeticRarity.LEGENDARY,
            baseGlimmer = 2200
        )
        
        registerFurniture(
            id = "furniture_grandfather_clock",
            name = "Grandfather Clock",
            description = "Tall clock that chimes on the hour",
            rarity = CosmeticRarity.RARE,
            baseGlimmer = 850
        )
        
        registerFurniture(
            id = "furniture_map_table",
            name = "Map Table",
            description = "Large table displaying a map of the wilderness",
            rarity = CosmeticRarity.UNCOMMON,
            baseGlimmer = 550
        )
        
        registerFurniture(
            id = "furniture_weapon_rack",
            name = "Weapon Rack",
            description = "Display rack for decorative weapons and tools",
            rarity = CosmeticRarity.COMMON,
            baseGlimmer = 200,
            maxInstances = 3
        )
        
        // DECORATIONS (20+ items) - Small placeable items
        registerDecoration(
            id = "deco_potted_fern",
            name = "Potted Fern",
            description = "Small fern in a clay pot",
            rarity = CosmeticRarity.COMMON,
            baseGlimmer = 80,
            maxInstances = 10
        )
        
        registerDecoration(
            id = "deco_candle_cluster",
            name = "Candle Cluster",
            description = "Group of wax candles providing soft light",
            rarity = CosmeticRarity.COMMON,
            baseGlimmer = 60,
            maxInstances = 8
        )
        
        registerDecoration(
            id = "deco_small_rug",
            name = "Small Rug",
            description = "Woven rug with geometric patterns",
            rarity = CosmeticRarity.COMMON,
            baseGlimmer = 90,
            maxInstances = 6
        )
        
        registerDecoration(
            id = "deco_painting_landscape",
            name = "Landscape Painting",
            description = "Oil painting of the wilderness at sunset",
            rarity = CosmeticRarity.UNCOMMON,
            baseGlimmer = 300,
            maxInstances = 4
        )
        
        registerDecoration(
            id = "deco_vase_flowers",
            name = "Vase of Flowers",
            description = "Ceramic vase with fresh wildflowers",
            rarity = CosmeticRarity.COMMON,
            baseGlimmer = 100,
            maxInstances = 8
        )
        
        registerDecoration(
            id = "deco_hanging_lantern",
            name = "Hanging Lantern",
            description = "Metal lantern suspended from ceiling",
            rarity = CosmeticRarity.UNCOMMON,
            baseGlimmer = 250,
            maxInstances = 6
        )
        
        registerDecoration(
            id = "deco_wall_tapestry",
            name = "Wall Tapestry",
            description = "Embroidered tapestry depicting heroic quails",
            rarity = CosmeticRarity.RARE,
            baseGlimmer = 700,
            maxInstances = 3
        )
        
        registerDecoration(
            id = "deco_globe",
            name = "Decorative Globe",
            description = "Antique globe showing the known world",
            rarity = CosmeticRarity.UNCOMMON,
            baseGlimmer = 400
        )
        
        registerDecoration(
            id = "deco_hourglass",
            name = "Ornate Hourglass",
            description = "Glass hourglass with golden sand",
            rarity = CosmeticRarity.UNCOMMON,
            baseGlimmer = 350
        )
        
        registerDecoration(
            id = "deco_statue_quail",
            name = "Quail Statue",
            description = "Bronze statue of a heroic button quail",
            rarity = CosmeticRarity.RARE,
            baseGlimmer = 900,
            requirement = UnlockRequirement.QuestCompletion("quest_founding_buttonburgh")
        )
        
        registerDecoration(
            id = "deco_crystal_cluster",
            name = "Crystal Cluster",
            description = "Cluster of glowing crystals",
            rarity = CosmeticRarity.RARE,
            baseGlimmer = 800,
            maxInstances = 4
        )
        
        registerDecoration(
            id = "deco_herb_drying_rack",
            name = "Herb Drying Rack",
            description = "Wooden rack with hanging herbs",
            rarity = CosmeticRarity.COMMON,
            baseGlimmer = 120,
            maxInstances = 3
        )
        
        registerDecoration(
            id = "deco_birdcage",
            name = "Decorative Birdcage",
            description = "Ornate empty birdcage (for decoration only!)",
            rarity = CosmeticRarity.UNCOMMON,
            baseGlimmer = 320
        )
        
        registerDecoration(
            id = "deco_wind_chimes",
            name = "Wind Chimes",
            description = "Melodic chimes that tinkle gently",
            rarity = CosmeticRarity.UNCOMMON,
            baseGlimmer = 280,
            maxInstances = 3
        )
        
        registerDecoration(
            id = "deco_treasure_pile",
            name = "Small Treasure Pile",
            description = "Glittering pile of shiny objects",
            rarity = CosmeticRarity.RARE,
            baseGlimmer = 750,
            maxInstances = 5
        )
        
        registerDecoration(
            id = "deco_bookstack",
            name = "Stack of Books",
            description = "Carefully arranged stack of ancient tomes",
            rarity = CosmeticRarity.COMMON,
            baseGlimmer = 110,
            maxInstances = 8
        )
        
        registerDecoration(
            id = "deco_music_box",
            name = "Music Box",
            description = "Delicate music box that plays a soothing melody",
            rarity = CosmeticRarity.RARE,
            baseGlimmer = 850
        )
        
        registerDecoration(
            id = "deco_telescope",
            name = "Brass Telescope",
            description = "Telescope for stargazing through nest windows",
            rarity = CosmeticRarity.EPIC,
            baseGlimmer = 1100
        )
        
        registerDecoration(
            id = "deco_feather_collection",
            name = "Feather Collection",
            description = "Display case with colorful feathers from around the world",
            rarity = CosmeticRarity.UNCOMMON,
            baseGlimmer = 380,
            requirement = UnlockRequirement.PlayerLevel(10)
        )
        
        registerDecoration(
            id = "deco_golden_egg",
            name = "Golden Egg Display",
            description = "Legendary golden egg on velvet cushion",
            rarity = CosmeticRarity.LEGENDARY,
            baseGlimmer = 2500,
            requirement = UnlockRequirement.HoardRank(15)
        )
        
        // FUNCTIONAL UPGRADES (6 items) - Provide gameplay benefits
        registerFunctionalUpgrade(
            upgradeType = FunctionalUpgradeType.SHINY_DISPLAY,
            id = "upgrade_shiny_display",
            name = "Shiny Display Case",
            description = "Showcase your hoard collection. +10% hoard XP gain",
            rarity = CosmeticRarity.EPIC,
            baseGlimmer = 1400
        )
        
        registerFunctionalUpgrade(
            upgradeType = FunctionalUpgradeType.SEED_SILO,
            id = "upgrade_seed_silo",
            name = "Seed Silo",
            description = "Expanded seed storage. +50% seed capacity",
            rarity = CosmeticRarity.RARE,
            baseGlimmer = 1000
        )
        
        registerFunctionalUpgrade(
            upgradeType = FunctionalUpgradeType.SMALL_LIBRARY,
            id = "upgrade_small_library",
            name = "Small Library",
            description = "Unlock 2 additional Thought Cabinet slots",
            rarity = CosmeticRarity.EPIC,
            baseGlimmer = 1600
        )
        
        registerFunctionalUpgrade(
            upgradeType = FunctionalUpgradeType.PERSONAL_ALCHEMY_STATION,
            id = "upgrade_alchemy_station",
            name = "Personal Alchemy Station",
            description = "Craft concoctions without leaving the nest",
            rarity = CosmeticRarity.RARE,
            baseGlimmer = 1200
        )
        
        registerFunctionalUpgrade(
            upgradeType = FunctionalUpgradeType.SMALL_WORKBENCH,
            id = "upgrade_small_workbench",
            name = "Small Workbench",
            description = "Craft items without leaving the nest",
            rarity = CosmeticRarity.RARE,
            baseGlimmer = 1100
        )
        
        registerFunctionalUpgrade(
            upgradeType = FunctionalUpgradeType.COZY_PERCH,
            id = "upgrade_cozy_perch",
            name = "Cozy Perch",
            description = "Comfortable resting spot for companions. +5% companion XP",
            rarity = CosmeticRarity.UNCOMMON,
            baseGlimmer = 800
        )
        
        registerFunctionalUpgrade(
            upgradeType = FunctionalUpgradeType.TROPHY_ROOM,
            id = "upgrade_trophy_room",
            name = "Trophy Room",
            description = "Display your quest achievements with pride. Unlocks trophy display grid.",
            rarity = CosmeticRarity.RARE,
            baseGlimmer = 1200
        )
    }
    
    // Helper registration methods
    private fun registerTheme(
        id: String,
        name: String,
        description: String,
        rarity: CosmeticRarity,
        baseGlimmer: Int,
        requirement: UnlockRequirement? = null
    ) {
        _allCosmetics.add(
            CosmeticItem(
                id = CosmeticItemId(id),
                name = name,
                description = description,
                category = CosmeticCategory.THEME,
                rarity = rarity,
                glimmerCost = (baseGlimmer * rarity.glimmerMultiplier).toInt(),
                unlockRequirement = requirement,
                isPlaceable = false,  // Themes auto-apply
                maxInstances = 1,
                visualAssetKey = id
            )
        )
    }
    
    private fun registerFurniture(
        id: String,
        name: String,
        description: String,
        rarity: CosmeticRarity,
        baseGlimmer: Int,
        maxInstances: Int = 1,
        requirement: UnlockRequirement? = null
    ) {
        _allCosmetics.add(
            CosmeticItem(
                id = CosmeticItemId(id),
                name = name,
                description = description,
                category = CosmeticCategory.FURNITURE,
                rarity = rarity,
                glimmerCost = (baseGlimmer * rarity.glimmerMultiplier).toInt(),
                unlockRequirement = requirement,
                isPlaceable = true,
                maxInstances = maxInstances,
                visualAssetKey = id
            )
        )
    }
    
    private fun registerDecoration(
        id: String,
        name: String,
        description: String,
        rarity: CosmeticRarity,
        baseGlimmer: Int,
        maxInstances: Int = 1,
        requirement: UnlockRequirement? = null
    ) {
        _allCosmetics.add(
            CosmeticItem(
                id = CosmeticItemId(id),
                name = name,
                description = description,
                category = CosmeticCategory.DECORATION,
                rarity = rarity,
                glimmerCost = (baseGlimmer * rarity.glimmerMultiplier).toInt(),
                unlockRequirement = requirement,
                isPlaceable = true,
                maxInstances = maxInstances,
                visualAssetKey = id
            )
        )
    }
    
    private fun registerFunctionalUpgrade(
        upgradeType: FunctionalUpgradeType,
        id: String,
        name: String,
        description: String,
        rarity: CosmeticRarity,
        baseGlimmer: Int
    ) {
        _allCosmetics.add(
            CosmeticItem(
                id = CosmeticItemId(id),
                name = name,
                description = description,
                category = CosmeticCategory.FUNCTIONAL,
                rarity = rarity,
                glimmerCost = (baseGlimmer * rarity.glimmerMultiplier).toInt(),
                unlockRequirement = null,
                isPlaceable = true,
                maxInstances = 1,
                visualAssetKey = id
            )
        )
    }
    
    /**
     * Get cosmetic by ID.
     */
    fun getCosmeticById(id: CosmeticItemId): CosmeticItem? {
        return allCosmetics.find { it.id == id }
    }
    
    /**
     * Get all cosmetics in a category.
     */
    fun getCosmeticsByCategory(category: CosmeticCategory): List<CosmeticItem> {
        return allCosmetics.filter { it.category == category }
    }
    
    /**
     * Get all available themes.
     */
    fun getAllThemes(): List<CosmeticItem> = getCosmeticsByCategory(CosmeticCategory.THEME)
    
    /**
     * Get all furniture items.
     */
    fun getAllFurniture(): List<CosmeticItem> = getCosmeticsByCategory(CosmeticCategory.FURNITURE)
    
    /**
     * Get all decoration items.
     */
    fun getAllDecorations(): List<CosmeticItem> = getCosmeticsByCategory(CosmeticCategory.DECORATION)
    
    /**
     * Get all functional upgrades.
     */
    fun getAllFunctionalUpgrades(): List<CosmeticItem> = getCosmeticsByCategory(CosmeticCategory.FUNCTIONAL)
}
