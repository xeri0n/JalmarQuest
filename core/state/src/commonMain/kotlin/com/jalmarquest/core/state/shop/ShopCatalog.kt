package com.jalmarquest.core.state.shop

import com.jalmarquest.core.model.*

/**
 * Shop catalog content definition
 * Defines all available shop items, rotation pools, and featured selections
 * Part of Milestone 5 Phase 3: Shop & Cosmetic Storefront
 */

/**
 * Central catalog of all shop items
 * Manages item definitions and rotation pools
 */
class ShopCatalog {
    private val allItems = mutableMapOf<ShopItemId, ShopItem>()
    
    /**
     * Register a shop item in the catalog
     */
    fun addItem(item: ShopItem) {
        allItems[item.id] = item
    }
    
    /**
     * Get a shop item by ID
     */
    fun getItem(id: ShopItemId): ShopItem? = allItems[id]
    
    /**
     * Get all items matching a category
     */
    fun getItemsByCategory(category: ShopCategory): List<ShopItem> {
        return allItems.values.filter { it.category == category }
    }
    
    /**
     * Get all permanent items (always available)
     */
    fun getPermanentItems(): List<ShopItem> {
        return allItems.values.filter { it.rotationFrequency == RotationFrequency.PERMANENT }
    }
    
    /**
     * Get daily rotation pool items
     */
    fun getDailyRotationPool(): List<ShopItem> {
        return allItems.values.filter { it.rotationFrequency == RotationFrequency.DAILY }
    }
    
    /**
     * Get weekly rotation pool items
     */
    fun getWeeklyRotationPool(): List<ShopItem> {
        return allItems.values.filter { it.rotationFrequency == RotationFrequency.WEEKLY }
    }
    
    /**
     * Get all cosmetic items
     */
    fun getAllCosmetics(): List<ShopItem> {
        return allItems.values.filter { it.category == ShopCategory.COSMETICS }
    }
    
    /**
     * Get cosmetics by type
     */
    fun getCosmeticsByType(type: CosmeticType): List<ShopItem> {
        return allItems.values.filter { it.cosmeticType == type }
    }
}

/**
 * Extension function to register all default shop items
 * Called during DI initialization to populate catalog
 */
fun ShopCatalog.registerDefaultItems() {
    // ===== CROWN COSMETICS (Head Slot) =====
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_crown_autumn_leaf"),
        name = "Autumn Leaf Crown",
        description = "Delicate crown woven from golden autumn leaves, shimmering with seasonal magic.",
        glimmerCost = 300,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.PERMANENT,
        cosmeticType = CosmeticType.CROWN,
        rarityTier = 2
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_crown_honeycomb"),
        name = "Honeycomb Tiara",
        description = "Sweet-smelling tiara crafted from enchanted honeycomb, still dripping with golden nectar.",
        glimmerCost = 450,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.DAILY,
        cosmeticType = CosmeticType.CROWN,
        rarityTier = 3
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_crown_starlight"),
        name = "Starlight Circlet",
        description = "Ethereal circlet that captures the light of distant stars, glowing softly in darkness.",
        glimmerCost = 800,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.WEEKLY,
        cosmeticType = CosmeticType.CROWN,
        rarityTier = 4
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_crown_mushroom_cap"),
        name = "Mushroom Cap Crown",
        description = "Whimsical crown shaped like a red-spotted mushroom cap, emitting gentle spores.",
        glimmerCost = 250,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.DAILY,
        cosmeticType = CosmeticType.CROWN,
        rarityTier = 2
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_crown_crystal_shard"),
        name = "Crystal Shard Diadem",
        description = "Rare diadem formed from crystallized moonlight, refracting rainbow patterns.",
        glimmerCost = 1200,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.WEEKLY,
        cosmeticType = CosmeticType.CROWN,
        rarityTier = 5
    ))
    
        // === EXCLUSIVE COSMETICS (Rewards & Special Unlocks) ===
        
        // Alpha 2.2 Phase 5C: Patron's Crown - Exclusive reward for Creator Coffee donation
        addItem(ShopItem(
            id = ShopItemId("cosmetic_crown_patron"),
            name = "Patron's Crown",
            description = "Exclusive golden crown awarded to supporters of JalmarQuest development. A mark of honor for those who fuel the creator's coffee addiction. Not available for purchase - granted only to those who buy the creator a coffee.",
            glimmerCost = 0, // Not purchasable with Glimmer - reward-only
            category = ShopCategory.COSMETICS,
            rotationFrequency = RotationFrequency.PERMANENT,
            cosmeticType = CosmeticType.CROWN,
            stock = 1, // One-time grant
            rarityTier = 5 // Legendary tier (highest available)
        ))
        
        // === CLOAK COSMETICS ===    // ===== CLOAK COSMETICS (Back Slot) =====
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_cloak_harvest"),
        name = "Harvest Cloak",
        description = "Warm cloak dyed in autumn hues, adorned with embroidered wheat stalks.",
        glimmerCost = 400,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.PERMANENT,
        cosmeticType = CosmeticType.CLOAK,
        rarityTier = 2
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_cloak_butterfly_wing"),
        name = "Butterfly Wing Cape",
        description = "Delicate cape resembling iridescent butterfly wings, fluttering with each movement.",
        glimmerCost = 600,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.DAILY,
        cosmeticType = CosmeticType.CLOAK,
        rarityTier = 3
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_cloak_shadow_silk"),
        name = "Shadow Silk Cloak",
        description = "Mysterious cloak woven from shadow silk, shifting between visible and invisible.",
        glimmerCost = 900,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.WEEKLY,
        cosmeticType = CosmeticType.CLOAK,
        rarityTier = 4
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_cloak_forest_guardian"),
        name = "Forest Guardian Cape",
        description = "Living cape made of moss and tiny flowers, growing and blooming naturally.",
        glimmerCost = 750,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.DAILY,
        cosmeticType = CosmeticType.CLOAK,
        rarityTier = 3
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_cloak_storm_feather"),
        name = "Storm Feather Mantle",
        description = "Dramatic mantle of storm-touched feathers, crackling with faint electricity.",
        glimmerCost = 1100,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.WEEKLY,
        cosmeticType = CosmeticType.CLOAK,
        rarityTier = 5
    ))
    
    // ===== MANTLE COSMETICS (Shoulder Slot) =====
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_mantle_golden_feather"),
        name = "Golden Feather Mantle",
        description = "Luxurious shoulder piece adorned with shimmering golden feathers.",
        glimmerCost = 350,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.PERMANENT,
        cosmeticType = CosmeticType.MANTLE,
        rarityTier = 2
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_mantle_pinecone"),
        name = "Pinecone Pauldrons",
        description = "Rustic shoulder guards crafted from enchanted pinecones and evergreen sprigs.",
        glimmerCost = 300,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.DAILY,
        cosmeticType = CosmeticType.MANTLE,
        rarityTier = 2
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_mantle_firefly_lights"),
        name = "Firefly Light Shoulders",
        description = "Magical shoulder piece surrounded by friendly fireflies that provide soft illumination.",
        glimmerCost = 700,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.WEEKLY,
        cosmeticType = CosmeticType.MANTLE,
        rarityTier = 4
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_mantle_ice_crystal"),
        name = "Ice Crystal Epaulettes",
        description = "Frost-kissed shoulder guards that never melt, emanating gentle cold.",
        glimmerCost = 650,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.DAILY,
        cosmeticType = CosmeticType.MANTLE,
        rarityTier = 3
    ))
    
    // ===== NECKLACE COSMETICS (Neck Slot) =====
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_necklace_maple_seed"),
        name = "Maple Seed Necklace",
        description = "Simple necklace featuring a polished maple seed that spins when you move.",
        glimmerCost = 200,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.PERMANENT,
        cosmeticType = CosmeticType.NECKLACE,
        rarityTier = 1
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_necklace_acorn_charm"),
        name = "Lucky Acorn Charm",
        description = "Charmed acorn pendant said to bring good fortune in gathering tasks.",
        glimmerCost = 250,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.DAILY,
        cosmeticType = CosmeticType.NECKLACE,
        rarityTier = 2
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_necklace_dewdrop_pendant"),
        name = "Morning Dewdrop Pendant",
        description = "Pristine pendant containing a perpetual dewdrop that never evaporates.",
        glimmerCost = 500,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.DAILY,
        cosmeticType = CosmeticType.NECKLACE,
        rarityTier = 3
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_necklace_moonstone"),
        name = "Moonstone Amulet",
        description = "Legendary amulet containing a shard of the moon, glowing with lunar energy.",
        glimmerCost = 1000,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.WEEKLY,
        cosmeticType = CosmeticType.NECKLACE,
        rarityTier = 5
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_necklace_berry_cluster"),
        name = "Berry Cluster Necklace",
        description = "Vibrant necklace adorned with clusters of preserved autumn berries.",
        glimmerCost = 300,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.DAILY,
        cosmeticType = CosmeticType.NECKLACE,
        rarityTier = 2
    ))
    
    // ===== AURA COSMETICS (Effect Slot) =====
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_aura_amber_glow"),
        name = "Amber Glow Aura",
        description = "Warm amber light that surrounds you with a gentle, comforting radiance.",
        glimmerCost = 550,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.PERMANENT,
        cosmeticType = CosmeticType.AURA,
        rarityTier = 3
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_aura_pollen_cloud"),
        name = "Pollen Cloud Aura",
        description = "Swirling cloud of golden pollen particles that drift around you magically.",
        glimmerCost = 600,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.DAILY,
        cosmeticType = CosmeticType.AURA,
        rarityTier = 3
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_aura_starfall"),
        name = "Starfall Aura",
        description = "Miniature shooting stars orbit around you, leaving trails of stardust.",
        glimmerCost = 950,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.WEEKLY,
        cosmeticType = CosmeticType.AURA,
        rarityTier = 4
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_aura_leaf_spiral"),
        name = "Leaf Spiral Aura",
        description = "Spiral of autumn leaves that dance and twirl perpetually around you.",
        glimmerCost = 450,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.DAILY,
        cosmeticType = CosmeticType.AURA,
        rarityTier = 2
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_aura_rainbow_shimmer"),
        name = "Rainbow Shimmer Aura",
        description = "Prismatic shimmer effect that changes colors based on your emotions.",
        glimmerCost = 1300,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.WEEKLY,
        cosmeticType = CosmeticType.AURA,
        rarityTier = 5
    ))
    
    // ===== REGALIA COSMETICS (Full Outfit Slot) =====
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_regalia_golden_harvest"),
        name = "Golden Harvest Regalia",
        description = "Complete ceremonial outfit celebrating the autumn harvest, radiating golden light.",
        glimmerCost = 1500,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.PERMANENT,
        cosmeticType = CosmeticType.REGALIA,
        rarityTier = 5
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_regalia_winter_frost"),
        name = "Winter Frost Regalia",
        description = "Majestic winter outfit adorned with icicles and snowflakes, chilling the air around you.",
        glimmerCost = 1500,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.SEASONAL,
        cosmeticType = CosmeticType.REGALIA,
        rarityTier = 5
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_regalia_spring_blossom"),
        name = "Spring Blossom Regalia",
        description = "Vibrant spring ensemble covered in blooming flowers and fresh greenery.",
        glimmerCost = 1500,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.SEASONAL,
        cosmeticType = CosmeticType.REGALIA,
        rarityTier = 5
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_regalia_summer_sun"),
        name = "Summer Sun Regalia",
        description = "Radiant summer attire that glows with the warmth of endless sunlight.",
        glimmerCost = 1500,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.SEASONAL,
        cosmeticType = CosmeticType.REGALIA,
        rarityTier = 5
    ))
    
    addItem(ShopItem(
        id = ShopItemId("cosmetic_regalia_mushroom_monarch"),
        name = "Mushroom Monarch Regalia",
        description = "Whimsical full outfit transforming you into a regal mushroom forest dweller.",
        glimmerCost = 1400,
        category = ShopCategory.COSMETICS,
        rotationFrequency = RotationFrequency.WEEKLY,
        cosmeticType = CosmeticType.REGALIA,
        rarityTier = 5
    ))
    
    // ===== BUNDLES =====
    
    addItem(ShopItem(
        id = ShopItemId("bundle_starter_cosmetics"),
        name = "Starter Cosmetic Bundle",
        description = "Essential cosmetics pack: Autumn Leaf Crown + Harvest Cloak + Maple Seed Necklace (25% off).",
        glimmerCost = 675, // 25% off 900
        category = ShopCategory.BUNDLES,
        rotationFrequency = RotationFrequency.PERMANENT,
        rarityTier = 2
    ))
    
    addItem(ShopItem(
        id = ShopItemId("bundle_premium_effects"),
        name = "Premium Effects Bundle",
        description = "Spectacular effects pack: Starfall Aura + Firefly Light Shoulders + Crystal Shard Diadem (30% off).",
        glimmerCost = 1820, // 30% off 2600
        category = ShopCategory.BUNDLES,
        rotationFrequency = RotationFrequency.WEEKLY,
        rarityTier = 5
    ))
}
