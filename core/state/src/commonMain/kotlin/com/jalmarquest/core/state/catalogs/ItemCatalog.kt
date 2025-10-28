package com.jalmarquest.core.state.catalogs

import kotlinx.serialization.Serializable

/**
 * Catalog of all items in the game including quest items, ingredients, equipment, and consumables.
 */

@Serializable
enum class ItemType {
    QUEST,
    FORAGING_INGREDIENT,
    ALCHEMY_INGREDIENT,
    CRAFTING_MATERIAL,
    EQUIPMENT,
    CONSUMABLE,
    SHINY,
    LORE
}

@Serializable
data class GameItem(
    val id: String,
    val name: String,
    val description: String,
    val type: ItemType,
    val rarity: ItemRarity = ItemRarity.COMMON,
    val stackable: Boolean = true,
    val maxStackSize: Int = 99
)

@Serializable
enum class ItemRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY
}

class ItemCatalog {
    private val items = mutableMapOf<String, GameItem>()
    
    init {
        registerDefaultItems()
    }
    
    fun registerItem(item: GameItem) {
        items[item.id] = item
    }
    
    fun getItemById(id: String): GameItem? = items[id]
    
    fun getItemsByType(type: ItemType): List<GameItem> {
        return items.values.filter { it.type == type }
    }
    
    fun getAllItems(): List<GameItem> = items.values.toList()
    
    private fun registerDefaultItems() {
        // Existing basic items
        registerItem(GameItem(
            id = "acorn",
            name = "Acorn",
            description = "A humble acorn. Perfect for foraging quails.",
            type = ItemType.FORAGING_INGREDIENT
        ))
        
        registerItem(GameItem(
            id = "decorative_feather",
            name = "Decorative Feather",
            description = "A beautiful feather for nest decoration.",
            type = ItemType.SHINY,
            rarity = ItemRarity.UNCOMMON
        ))
        
        // Common ingredients
        registerItem(GameItem(
            id = "ingredient_common_herb",
            name = "Common Herb",
            description = "A basic herb found throughout the land.",
            type = ItemType.FORAGING_INGREDIENT
        ))
        
        registerItem(GameItem(
            id = "ingredient_dry_moss",
            name = "Dry Moss",
            description = "Soft, dry moss perfect for nesting.",
            type = ItemType.FORAGING_INGREDIENT
        ))
        
        registerItem(GameItem(
            id = "ingredient_speckled_leaf",
            name = "Speckled Leaf",
            description = "A leaf with natural camouflage patterns.",
            type = ItemType.FORAGING_INGREDIENT
        ))
        
        registerItem(GameItem(
            id = "ingredient_sticky_sap",
            name = "Sticky Sap",
            description = "Viscous tree sap that bonds materials.",
            type = ItemType.FORAGING_INGREDIENT
        ))
        
        // Rare ingredients
        registerItem(GameItem(
            id = "ingredient_rare_herb",
            name = "Rare Herb",
            description = "An uncommon medicinal herb.",
            type = ItemType.FORAGING_INGREDIENT,
            rarity = ItemRarity.RARE
        ))
        
        registerItem(GameItem(
            id = "ingredient_rare_mushroom",
            name = "Rare Mushroom",
            description = "A mysterious fungus with alchemical properties.",
            type = ItemType.FORAGING_INGREDIENT,
            rarity = ItemRarity.RARE
        ))
        
        registerItem(GameItem(
            id = "ingredient_rare_feather",
            name = "Rare Feather",
            description = "A pristine feather from a rare bird.",
            type = ItemType.CRAFTING_MATERIAL,
            rarity = ItemRarity.RARE
        ))
        
        registerItem(GameItem(
            id = "ingredient_spider_silk",
            name = "Spider Silk",
            description = "Delicate but incredibly strong silk threads.",
            type = ItemType.FORAGING_INGREDIENT,
            rarity = ItemRarity.RARE
        ))
        
        registerItem(GameItem(
            id = "ingredient_night_vision",
            name = "Night Vision Herb",
            description = "A herb that enhances sight in darkness.",
            type = ItemType.FORAGING_INGREDIENT,
            rarity = ItemRarity.RARE
        ))
        
        registerItem(GameItem(
            id = "ingredient_luck_boost",
            name = "Four-Leaf Clover",
            description = "A lucky charm that enhances fortune.",
            type = ItemType.FORAGING_INGREDIENT,
            rarity = ItemRarity.UNCOMMON
        ))
        
        registerItem(GameItem(
            id = "ingredient_mountain_mineral",
            name = "Mountain Mineral",
            description = "A crystalline mineral from high peaks.",
            type = ItemType.CRAFTING_MATERIAL,
            rarity = ItemRarity.UNCOMMON
        ))
        
        // Crafting materials
        registerItem(GameItem(
            id = "rare_metal_ingredient",
            name = "Rare Metal",
            description = "A precious metal used in advanced crafting.",
            type = ItemType.CRAFTING_MATERIAL,
            rarity = ItemRarity.RARE
        ))
        
        registerItem(GameItem(
            id = "uncommon_cloth_ingredient",
            name = "Fine Cloth",
            description = "High-quality fabric.",
            type = ItemType.CRAFTING_MATERIAL,
            rarity = ItemRarity.UNCOMMON
        ))
        
        registerItem(GameItem(
            id = "uncommon_leather_ingredient",
            name = "Tanned Leather",
            description = "Durable leather for crafting.",
            type = ItemType.CRAFTING_MATERIAL,
            rarity = ItemRarity.UNCOMMON
        ))
        
        registerItem(GameItem(
            id = "common_shiny_fragment",
            name = "Shiny Fragment",
            description = "A small, glittering piece of something valuable.",
            type = ItemType.SHINY,
            rarity = ItemRarity.COMMON
        ))
        
        // Quest 1: The Giga-Seed
        registerItem(GameItem(
            id = "quest_item_giga_seed",
            name = "Giga-Seed",
            description = "It hums with a strange, intellectual energy.",
            type = ItemType.QUEST,
            rarity = ItemRarity.LEGENDARY,
            stackable = false
        ))
        
        // Quest 2: The High Perch
        registerItem(GameItem(
            id = "item_sunpetal_flower",
            name = "Sunpetal Flower",
            description = "A radiant flower that only grows in high places.",
            type = ItemType.QUEST,
            rarity = ItemRarity.RARE,
            stackable = false
        ))
        
        // Quest 3: The Night Forager
        registerItem(GameItem(
            id = "item_moondew_fern",
            name = "Moondew Fern",
            description = "A silvery fern that glitters with cold dew. It smells of moonlight.",
            type = ItemType.FORAGING_INGREDIENT,
            rarity = ItemRarity.RARE
        ))
        
        // Quest 4: The Beetle Brouhaha
        registerItem(GameItem(
            id = "item_beetle_azure",
            name = "Azure Beetle",
            description = "A beautiful blue beetle found in the Forest.",
            type = ItemType.QUEST,
            rarity = ItemRarity.RARE
        ))
        
        registerItem(GameItem(
            id = "item_beetle_emerald",
            name = "Emerald Beetle",
            description = "A brilliant green beetle found in the Forest.",
            type = ItemType.QUEST,
            rarity = ItemRarity.RARE
        ))
        
        registerItem(GameItem(
            id = "item_beetle_ruby",
            name = "Ruby Beetle",
            description = "A crimson beetle found at the Beach.",
            type = ItemType.QUEST,
            rarity = ItemRarity.RARE
        ))
        
        registerItem(GameItem(
            id = "item_beetle_obsidian",
            name = "Obsidian Beetle",
            description = "A jet-black beetle found in the Ruins.",
            type = ItemType.QUEST,
            rarity = ItemRarity.RARE
        ))
        
        registerItem(GameItem(
            id = "item_beetle_opal",
            name = "Opal Beetle",
            description = "An iridescent beetle found in the Mountains.",
            type = ItemType.QUEST,
            rarity = ItemRarity.RARE
        ))
        
        // Quest 5: A Soothing Silence
        registerItem(GameItem(
            id = "item_muffler_cog",
            name = "Muffler Cog",
            description = "A complex cog wrapped in dense cloth, designed to dampen sound.",
            type = ItemType.QUEST,
            rarity = ItemRarity.UNCOMMON,
            stackable = false
        ))
        
        registerItem(GameItem(
            id = "shiny_bottle_cap",
            name = "Bottle Cap",
            description = "A shiny metal bottle cap. Simple but captivating.",
            type = ItemType.SHINY,
            rarity = ItemRarity.COMMON
        ))
        
        // Quest 6: The Lost Clutch
        registerItem(GameItem(
            id = "quest_hidden_egg",
            name = "Hidden Egg",
            description = "A tiny, perfectly camouflaged quail egg.",
            type = ItemType.QUEST,
            rarity = ItemRarity.UNCOMMON,
            maxStackSize = 6
        ))
        
        // Quest 7: The Coziest Nest
        registerItem(GameItem(
            id = "item_insulated_nest_lining",
            name = "Insulated Nest Lining",
            description = "Warm, comfortable lining for a cozy nest.",
            type = ItemType.QUEST,
            rarity = ItemRarity.UNCOMMON,
            stackable = false
        ))
        
        // Quest 10: The Antbassador
        registerItem(GameItem(
            id = "item_sweet_sap",
            name = "Sweet Sap",
            description = "A sugary sap that ants find irresistible.",
            type = ItemType.FORAGING_INGREDIENT,
            rarity = ItemRarity.UNCOMMON
        ))
        
        // Quest 11: The Stone-Stuck Seed
        // (Uses existing ingredients)
        
        // Quest 12: The Fading Elder
        registerItem(GameItem(
            id = "ingredient_sunsgrace_flower",
            name = "Sunsgrace Flower",
            description = "A pure white flower that only blooms at dawn on mountain peaks.",
            type = ItemType.FORAGING_INGREDIENT,
            rarity = ItemRarity.LEGENDARY
        ))
        
        // Quest 13: The Chameleon's Challenge
        registerItem(GameItem(
            id = "quest_item_pack_rats_lens",
            name = "Pack Rat's Lens",
            description = "A beautiful, multifaceted crystal lens.",
            type = ItemType.QUEST,
            rarity = ItemRarity.RARE,
            stackable = false
        ))
        
        // Quest 14: The Poisoned Grove
        registerItem(GameItem(
            id = "item_cleansing_catalyst",
            name = "Cleansing Catalyst",
            description = "A pure white, chalky stone found in the Ruins.",
            type = ItemType.QUEST,
            rarity = ItemRarity.RARE
        ))
        
        registerItem(GameItem(
            id = "item_potion_of_cleansing",
            name = "Potion of Cleansing",
            description = "A purifying elixir that can heal blighted land.",
            type = ItemType.QUEST,
            rarity = ItemRarity.RARE,
            stackable = false
        ))
        
        // Quest 17: The Barter's Challenge - Trade chain items
        registerItem(GameItem(
            id = "item_special_tea",
            name = "Hoot's Special Tea",
            description = "Smells strongly of mint and... something else.",
            type = ItemType.QUEST,
            stackable = false
        ))
        
        registerItem(GameItem(
            id = "item_pips_pigment",
            name = "Pip's Pigment",
            description = "A unique paint made from rare flowers.",
            type = ItemType.QUEST,
            stackable = false
        ))
        
        registerItem(GameItem(
            id = "item_tinks_gear",
            name = "Tink's Gear",
            description = "A finely machined mechanical component.",
            type = ItemType.QUEST,
            stackable = false
        ))
        
        // Quest 18: The Territorial Crow
        registerItem(GameItem(
            id = "item_shiny_distraction",
            name = "Shiny Distraction",
            description = "A fake shiny designed to fool birds.",
            type = ItemType.QUEST,
            rarity = ItemRarity.COMMON,
            stackable = false
        ))
        
        // Quest 19: The Enlightenment Project - Essences
        registerItem(GameItem(
            id = "item_foraging_essence",
            name = "Essence of Foraging",
            description = "Glows with the energy of the earth.",
            type = ItemType.QUEST,
            rarity = ItemRarity.LEGENDARY,
            stackable = false
        ))
        
        registerItem(GameItem(
            id = "item_alchemy_essence",
            name = "Essence of Alchemy",
            description = "Swirls with transformative power.",
            type = ItemType.QUEST,
            rarity = ItemRarity.LEGENDARY,
            stackable = false
        ))
        
        registerItem(GameItem(
            id = "item_combat_essence",
            name = "Essence of Combat",
            description = "Radiates martial prowess.",
            type = ItemType.QUEST,
            rarity = ItemRarity.LEGENDARY,
            stackable = false
        ))
        
        registerItem(GameItem(
            id = "item_barter_essence",
            name = "Essence of Bartering",
            description = "Shimmers with the promise of fair deals.",
            type = ItemType.QUEST,
            rarity = ItemRarity.LEGENDARY,
            stackable = false
        ))
        
        registerItem(GameItem(
            id = "item_hoard_essence",
            name = "Essence of Hoarding",
            description = "Glitters with accumulated wealth.",
            type = ItemType.QUEST,
            rarity = ItemRarity.LEGENDARY,
            stackable = false
        ))
        
        registerItem(GameItem(
            id = "item_scholar_essence",
            name = "Essence of Scholarship",
            description = "Pulses with ancient knowledge.",
            type = ItemType.QUEST,
            rarity = ItemRarity.LEGENDARY,
            stackable = false
        ))
        
        // ===== ALPHA 2.2: SPECIAL ITEMS =====
        // Borken's Pointy Stick - Companion item for chaos character
        registerItem(GameItem(
            id = "item_borkens_pointy_stick",
            name = "Borken's Pointy Stick",
            description = "An unremarkable stick that somehow feels... ominous. Borken insists it's 'pointy enough to solve problems.' You're not sure what problems require a stick.",
            type = ItemType.LORE,
            rarity = ItemRarity.UNCOMMON,
            stackable = false,
            maxStackSize = 1
        ))
    }
}
