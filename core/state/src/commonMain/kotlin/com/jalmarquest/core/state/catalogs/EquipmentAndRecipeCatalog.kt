package com.jalmarquest.core.state.catalogs

import com.jalmarquest.core.model.SkillType
import kotlinx.serialization.Serializable

/**
 * Catalog of all equipment in the game.
 */

@Serializable
enum class EquipmentSlot {
    HEAD,
    BODY,
    TOOL_MAIN,
    TOOL_OFF,
    ACCESSORY_1,
    ACCESSORY_2
}

@Serializable
data class Equipment(
    val id: String,
    val name: String,
    val description: String,
    val slot: EquipmentSlot,
    val rarity: ItemRarity = ItemRarity.COMMON,
    val damage: Int = 0,
    val defense: Int = 0,
    val luckBonus: Int = 0,
    val stealthBonus: Int = 0,
    val harvestSpeed: Double = 1.0,
    val shopDiscount: Double = 0.0,
    val requiresLevel: Int = 1
)

class EquipmentCatalog {
    private val equipment = mutableMapOf<String, Equipment>()
    
    init {
        registerDefaultEquipment()
    }
    
    fun registerEquipment(item: Equipment) {
        equipment[item.id] = item
    }
    
    fun getEquipmentById(id: String): Equipment? = equipment[id]
    
    fun getEquipmentBySlot(slot: EquipmentSlot): List<Equipment> {
        return equipment.values.filter { it.slot == slot }
    }
    
    fun getAllEquipment(): List<Equipment> = equipment.values.toList()
    
    private fun registerDefaultEquipment() {
        // Quest 8: Practical Plumage
        registerEquipment(Equipment(
            id = "equipment_camo_cloak",
            name = "Camouflage Cloak",
            description = "A cloak woven with speckled leaves for perfect camouflage.",
            slot = EquipmentSlot.HEAD,
            rarity = ItemRarity.UNCOMMON,
            defense = 1,
            luckBonus = 1,
            stealthBonus = 5
        ))
        
        // Quest 9: The Digger's Delight
        registerEquipment(Equipment(
            id = "equipment_reinforced_talons",
            name = "Reinforced Talons",
            description = "Metal-tipped talons for breaking through hard terrain.",
            slot = EquipmentSlot.TOOL_MAIN,
            rarity = ItemRarity.UNCOMMON,
            damage = 1,
            harvestSpeed = 1.2
        ))
        
        // Quest 15: The Hoarder's Exam
        registerEquipment(Equipment(
            id = "equipment_hoarders_eyeglass",
            name = "Hoarder's Eyeglass",
            description = "A magnifying lens that reveals the true value of treasures.",
            slot = EquipmentSlot.ACCESSORY_1,
            rarity = ItemRarity.EPIC,
            luckBonus = 3,
            shopDiscount = 0.05,
            requiresLevel = 4
        ))
        
        // Quest 18: The Territorial Crow
        registerEquipment(Equipment(
            id = "equipment_spiked_helmet",
            name = "Spiked Helmet",
            description = "A helmet adorned with defensive spikes.",
            slot = EquipmentSlot.HEAD,
            rarity = ItemRarity.RARE,
            damage = 2,
            defense = 3,
            requiresLevel = 3
        ))
    }
}


/**
 * Catalog of all crafting and alchemy recipes.
 */

@Serializable
enum class CraftingStation {
    WORKBENCH,
    FORGE,
    SEWING_TABLE,
    ALCHEMY_LAB
}

@Serializable
enum class RecipeDiscoveryMethod {
    SKILL_LEVEL,
    EXPERIMENTATION,
    QUEST_REWARD,
    PURCHASE
}

@Serializable
data class RecipeIngredient(
    val itemId: String,
    val quantity: Int
)

@Serializable
data class Recipe(
    val id: String,
    val name: String,
    val station: CraftingStation,
    val discoveryMethod: RecipeDiscoveryMethod,
    val requiredSkill: SkillType? = null,
    val requiredSkillLevel: Int? = null,
    val ingredients: List<RecipeIngredient>,
    val resultItemId: String,
    val resultQuantity: Int = 1,
    val effectId: String? = null, // For potions/concoctions
    val effectDuration: Int? = null // Duration in minutes
)

class RecipeCatalog {
    private val recipes = mutableMapOf<String, Recipe>()
    
    init {
        registerDefaultRecipes()
    }
    
    fun registerRecipe(recipe: Recipe) {
        recipes[recipe.id] = recipe
    }
    
    fun getRecipeById(id: String): Recipe? = recipes[id]
    
    fun getRecipesByStation(station: CraftingStation): List<Recipe> {
        return recipes.values.filter { it.station == station }
    }
    
    fun getAllRecipes(): List<Recipe> = recipes.values.toList()
    
    private fun registerDefaultRecipes() {
        // Basic healing potion (existing)
        registerRecipe(Recipe(
            id = "basic_healing_potion",
            name = "Basic Healing Potion",
            station = CraftingStation.ALCHEMY_LAB,
            discoveryMethod = RecipeDiscoveryMethod.QUEST_REWARD,
            requiredSkill = SkillType.ALCHEMY,
            requiredSkillLevel = 1,
            ingredients = listOf(
                RecipeIngredient("ingredient_common_herb", 2)
            ),
            resultItemId = "potion_healing",
            effectId = "effect_health_regen",
            effectDuration = 10
        ))
        
        // Quest 2: The High Perch
        registerRecipe(Recipe(
            id = "recipe_potion_of_short_flight",
            name = "Potion of Short Flight",
            station = CraftingStation.ALCHEMY_LAB,
            discoveryMethod = RecipeDiscoveryMethod.EXPERIMENTATION,
            requiredSkill = SkillType.ALCHEMY,
            requiredSkillLevel = 2,
            ingredients = listOf(
                RecipeIngredient("ingredient_common_herb", 3),
                RecipeIngredient("ingredient_rare_feather", 1)
            ),
            resultItemId = "potion_short_flight",
            effectId = "effect_short_flight",
            effectDuration = 5
        ))
        
        // Quest 3: The Night Forager
        registerRecipe(Recipe(
            id = "recipe_potion_of_calm",
            name = "Potion of Calm",
            station = CraftingStation.ALCHEMY_LAB,
            discoveryMethod = RecipeDiscoveryMethod.QUEST_REWARD,
            requiredSkill = SkillType.ALCHEMY,
            requiredSkillLevel = 2,
            ingredients = listOf(
                RecipeIngredient("item_moondew_fern", 1),
                RecipeIngredient("ingredient_common_herb", 2)
            ),
            resultItemId = "potion_calm",
            effectId = "effect_calm",
            effectDuration = 15
        ))
        
        // Quest 5: A Soothing Silence
        registerRecipe(Recipe(
            id = "recipe_muffler_cog",
            name = "Muffler Cog",
            station = CraftingStation.WORKBENCH,
            discoveryMethod = RecipeDiscoveryMethod.QUEST_REWARD,
            requiredSkill = SkillType.FORAGING, // Using FORAGING as crafting proxy
            requiredSkillLevel = 2,
            ingredients = listOf(
                RecipeIngredient("rare_metal_ingredient", 2),
                RecipeIngredient("uncommon_cloth_ingredient", 1)
            ),
            resultItemId = "item_muffler_cog"
        ))
        
        // Quest 6: The Lost Clutch
        registerRecipe(Recipe(
            id = "recipe_elixir_of_keen_sight",
            name = "Elixir of Keen Sight",
            station = CraftingStation.ALCHEMY_LAB,
            discoveryMethod = RecipeDiscoveryMethod.EXPERIMENTATION,
            requiredSkill = SkillType.ALCHEMY,
            requiredSkillLevel = 3,
            ingredients = listOf(
                RecipeIngredient("ingredient_night_vision", 1),
                RecipeIngredient("ingredient_luck_boost", 2),
                RecipeIngredient("ingredient_common_herb", 1)
            ),
            resultItemId = "potion_keen_sight",
            effectId = "effect_keen_sight",
            effectDuration = 20
        ))
        
        // Quest 7: The Coziest Nest
        registerRecipe(Recipe(
            id = "recipe_insulated_nest_lining",
            name = "Insulated Nest Lining",
            station = CraftingStation.SEWING_TABLE,
            discoveryMethod = RecipeDiscoveryMethod.SKILL_LEVEL,
            requiredSkill = SkillType.FORAGING,
            requiredSkillLevel = 2,
            ingredients = listOf(
                RecipeIngredient("ingredient_dry_moss", 5),
                RecipeIngredient("ingredient_spider_silk", 2)
            ),
            resultItemId = "item_insulated_nest_lining"
        ))
        
        // Quest 8: Practical Plumage
        registerRecipe(Recipe(
            id = "recipe_camo_cloak",
            name = "Camouflage Cloak",
            station = CraftingStation.WORKBENCH,
            discoveryMethod = RecipeDiscoveryMethod.QUEST_REWARD,
            requiredSkill = SkillType.FORAGING,
            requiredSkillLevel = 2,
            ingredients = listOf(
                RecipeIngredient("ingredient_speckled_leaf", 5),
                RecipeIngredient("ingredient_sticky_sap", 2)
            ),
            resultItemId = "equipment_camo_cloak"
        ))
        
        // Quest 9: The Digger's Delight
        registerRecipe(Recipe(
            id = "recipe_reinforced_talons",
            name = "Reinforced Talons",
            station = CraftingStation.FORGE,
            discoveryMethod = RecipeDiscoveryMethod.QUEST_REWARD,
            requiredSkill = SkillType.FORAGING,
            requiredSkillLevel = 3,
            ingredients = listOf(
                RecipeIngredient("rare_metal_ingredient", 3),
                RecipeIngredient("uncommon_leather_ingredient", 1)
            ),
            resultItemId = "equipment_reinforced_talons"
        ))
        
        // Quest 10: The Antbassador
        registerRecipe(Recipe(
            id = "recipe_potion_of_ant_talk",
            name = "Potion of Ant Talk",
            station = CraftingStation.ALCHEMY_LAB,
            discoveryMethod = RecipeDiscoveryMethod.QUEST_REWARD,
            requiredSkill = SkillType.ALCHEMY,
            requiredSkillLevel = 3,
            ingredients = listOf(
                RecipeIngredient("ingredient_rare_mushroom", 1),
                RecipeIngredient("ingredient_sticky_sap", 2)
            ),
            resultItemId = "potion_ant_talk",
            effectId = "effect_ant_talk",
            effectDuration = 10
        ))
        
        // Quest 11: The Stone-Stuck Seed
        registerRecipe(Recipe(
            id = "recipe_potion_of_quail_might",
            name = "Potion of Quail Might",
            station = CraftingStation.ALCHEMY_LAB,
            discoveryMethod = RecipeDiscoveryMethod.QUEST_REWARD,
            requiredSkill = SkillType.ALCHEMY,
            requiredSkillLevel = 3,
            ingredients = listOf(
                RecipeIngredient("ingredient_rare_mushroom", 2),
                RecipeIngredient("ingredient_mountain_mineral", 1)
            ),
            resultItemId = "potion_quail_might",
            effectId = "effect_quail_might",
            effectDuration = 5
        ))
        
        // Quest 12: The Fading Elder
        registerRecipe(Recipe(
            id = "recipe_potion_of_vitality",
            name = "Potion of Vitality",
            station = CraftingStation.ALCHEMY_LAB,
            discoveryMethod = RecipeDiscoveryMethod.QUEST_REWARD,
            requiredSkill = SkillType.ALCHEMY,
            requiredSkillLevel = 4,
            ingredients = listOf(
                RecipeIngredient("ingredient_sunsgrace_flower", 1),
                RecipeIngredient("ingredient_rare_mushroom", 2)
            ),
            resultItemId = "potion_vitality",
            effectId = "effect_health_regen",
            effectDuration = 60
        ))
        
        // Quest 13: The Chameleon's Challenge
        registerRecipe(Recipe(
            id = "recipe_potion_of_invisibility",
            name = "Potion of Invisibility",
            station = CraftingStation.ALCHEMY_LAB,
            discoveryMethod = RecipeDiscoveryMethod.QUEST_REWARD,
            requiredSkill = SkillType.ALCHEMY,
            requiredSkillLevel = 5,
            ingredients = listOf(
                RecipeIngredient("ingredient_rare_mushroom", 1),
                RecipeIngredient("ingredient_spider_silk", 2)
            ),
            resultItemId = "potion_invisibility",
            effectId = "effect_invisibility",
            effectDuration = 5
        ))
        
        // Quest 14: The Poisoned Grove
        registerRecipe(Recipe(
            id = "recipe_potion_of_cleansing",
            name = "Potion of Cleansing",
            station = CraftingStation.ALCHEMY_LAB,
            discoveryMethod = RecipeDiscoveryMethod.QUEST_REWARD,
            requiredSkill = SkillType.ALCHEMY,
            requiredSkillLevel = 4,
            ingredients = listOf(
                RecipeIngredient("item_cleansing_catalyst", 1),
                RecipeIngredient("ingredient_common_herb", 3)
            ),
            resultItemId = "item_potion_of_cleansing"
        ))
        
        // Quest 16: The Silent Scholar
        registerRecipe(Recipe(
            id = "recipe_potion_of_clarity",
            name = "Potion of Clarity",
            station = CraftingStation.ALCHEMY_LAB,
            discoveryMethod = RecipeDiscoveryMethod.QUEST_REWARD,
            requiredSkill = SkillType.ALCHEMY,
            requiredSkillLevel = 3,
            ingredients = listOf(
                RecipeIngredient("ingredient_rare_mushroom", 1),
                RecipeIngredient("ingredient_common_herb", 2)
            ),
            resultItemId = "potion_clarity",
            effectId = "effect_clarity",
            effectDuration = 20
        ))
        
        // Quest 18: The Territorial Crow
        registerRecipe(Recipe(
            id = "recipe_spiked_helmet",
            name = "Spiked Helmet",
            station = CraftingStation.FORGE,
            discoveryMethod = RecipeDiscoveryMethod.QUEST_REWARD,
            requiredSkill = SkillType.COMBAT,
            requiredSkillLevel = 3,
            ingredients = listOf(
                RecipeIngredient("rare_metal_ingredient", 2),
                RecipeIngredient("ingredient_rare_feather", 1)
            ),
            resultItemId = "equipment_spiked_helmet"
        ))
        
        registerRecipe(Recipe(
            id = "recipe_shiny_distraction",
            name = "Shiny Distraction",
            station = CraftingStation.WORKBENCH,
            discoveryMethod = RecipeDiscoveryMethod.QUEST_REWARD,
            requiredSkill = SkillType.HOARDING,
            requiredSkillLevel = 2,
            ingredients = listOf(
                RecipeIngredient("common_shiny_fragment", 5),
                RecipeIngredient("ingredient_sticky_sap", 1)
            ),
            resultItemId = "item_shiny_distraction"
        ))
        
        // Quest 19: Enlightenment Project Essences
        registerRecipe(Recipe(
            id = "recipe_foraging_essence",
            name = "Essence of Foraging",
            station = CraftingStation.ALCHEMY_LAB,
            discoveryMethod = RecipeDiscoveryMethod.QUEST_REWARD,
            requiredSkill = SkillType.FORAGING,
            requiredSkillLevel = 10,
            ingredients = listOf(
                RecipeIngredient("ingredient_rare_herb", 10)
            ),
            resultItemId = "item_foraging_essence"
        ))
        
        registerRecipe(Recipe(
            id = "recipe_alchemy_essence",
            name = "Essence of Alchemy",
            station = CraftingStation.ALCHEMY_LAB,
            discoveryMethod = RecipeDiscoveryMethod.QUEST_REWARD,
            requiredSkill = SkillType.ALCHEMY,
            requiredSkillLevel = 10,
            ingredients = listOf(
                RecipeIngredient("potion_healing", 3) // Placeholder for legendary potion
            ),
            resultItemId = "item_alchemy_essence"
        ))
        
        registerRecipe(Recipe(
            id = "recipe_combat_essence",
            name = "Essence of Combat",
            station = CraftingStation.FORGE,
            discoveryMethod = RecipeDiscoveryMethod.QUEST_REWARD,
            requiredSkill = SkillType.COMBAT,
            requiredSkillLevel = 10,
            ingredients = listOf(
                RecipeIngredient("equipment_spiked_helmet", 1) // Placeholder for legendary equipment
            ),
            resultItemId = "item_combat_essence"
        ))
        
        registerRecipe(Recipe(
            id = "recipe_barter_essence",
            name = "Essence of Bartering",
            station = CraftingStation.WORKBENCH,
            discoveryMethod = RecipeDiscoveryMethod.QUEST_REWARD,
            requiredSkill = SkillType.BARTERING,
            requiredSkillLevel = 10,
            ingredients = listOf(
                RecipeIngredient("ingredient_common_herb", 0) // Special: requires 10000 seeds
            ),
            resultItemId = "item_barter_essence"
        ))
        
        registerRecipe(Recipe(
            id = "recipe_hoard_essence",
            name = "Essence of Hoarding",
            station = CraftingStation.WORKBENCH,
            discoveryMethod = RecipeDiscoveryMethod.QUEST_REWARD,
            requiredSkill = SkillType.HOARDING,
            requiredSkillLevel = 10,
            ingredients = listOf(
                RecipeIngredient("shiny_bottle_cap", 1) // Placeholder for legendary shiny
            ),
            resultItemId = "item_hoard_essence"
        ))
        
        registerRecipe(Recipe(
            id = "recipe_scholar_essence",
            name = "Essence of Scholarship",
            station = CraftingStation.ALCHEMY_LAB,
            discoveryMethod = RecipeDiscoveryMethod.QUEST_REWARD,
            requiredSkill = SkillType.SCHOLARSHIP,
            requiredSkillLevel = 10,
            ingredients = listOf(
                RecipeIngredient("ingredient_rare_mushroom", 5) // Special: requires 5 internalized thoughts
            ),
            resultItemId = "item_scholar_essence"
        ))
    }
}
