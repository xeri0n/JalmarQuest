package com.jalmarquest.core.state.concoctions

import com.jalmarquest.core.model.*
import kotlin.random.Random

/**
 * Service for managing ingredient harvesting with location-based spawning and rarity weighting.
 */
class IngredientHarvestService(
    private val random: Random = Random.Default
) {
    private val ingredientCatalog: List<Ingredient> = buildIngredientCatalog()
    
    /**
     * Harvest ingredients from a specific location.
     * Returns list of harvested ingredients (may be empty based on RNG).
     */
    fun harvestAtLocation(
        location: String,
        luckBonus: Int = 0 // % bonus from active concoctions
    ): List<Pair<IngredientId, Int>> {
        val availableIngredients = ingredientCatalog.filter { 
            it.harvestLocations.contains(location) 
        }
        
        if (availableIngredients.isEmpty()) {
            return emptyList()
        }
        
        val harvested = mutableListOf<Pair<IngredientId, Int>>()
        
        // Roll for each rarity tier
        for (ingredient in availableIngredients) {
            val baseChance = getRaritySpawnChance(ingredient.rarity)
            val adjustedChance = (baseChance + (luckBonus / 100.0 * baseChance)).coerceAtMost(1.0)
            
            if (random.nextDouble() < adjustedChance) {
                val quantity = when (ingredient.rarity) {
                    IngredientRarity.COMMON -> random.nextInt(2, 5)
                    IngredientRarity.UNCOMMON -> random.nextInt(1, 3)
                    IngredientRarity.RARE -> 1
                    IngredientRarity.EXOTIC -> 1
                    IngredientRarity.LEGENDARY -> 1
                }
                harvested.add(ingredient.id to quantity)
            }
        }
        
        return harvested
    }
    
    /**
     * Get all ingredients that can be found at a location.
     */
    fun getIngredientsAtLocation(location: String): List<Ingredient> {
        return ingredientCatalog.filter { it.harvestLocations.contains(location) }
    }
    
    /**
     * Get ingredient by ID.
     */
    fun getIngredient(id: IngredientId): Ingredient? {
        return ingredientCatalog.find { it.id == id }
    }
    
    /**
     * Get all ingredients in catalog.
     */
    fun getAllIngredients(): List<Ingredient> = ingredientCatalog
    
    private fun getRaritySpawnChance(rarity: IngredientRarity): Double {
        return when (rarity) {
            IngredientRarity.COMMON -> 0.50
            IngredientRarity.UNCOMMON -> 0.30
            IngredientRarity.RARE -> 0.15
            IngredientRarity.EXOTIC -> 0.04
            IngredientRarity.LEGENDARY -> 0.01
        }
    }
    
    private fun buildIngredientCatalog(): List<Ingredient> {
        return listOf(
            // Common ingredients
            Ingredient(
                id = IngredientId("wildflower"),
                nameKey = "ingredient.wildflower.name",
                descriptionKey = "ingredient.wildflower.description",
                rarity = IngredientRarity.COMMON,
                harvestLocations = listOf("forest", "meadow"),
                properties = setOf(IngredientProperty.RESTORATIVE, IngredientProperty.CALMING)
            ),
            Ingredient(
                id = IngredientId("oak_bark"),
                nameKey = "ingredient.oak_bark.name",
                descriptionKey = "ingredient.oak_bark.description",
                rarity = IngredientRarity.COMMON,
                harvestLocations = listOf("forest"),
                properties = setOf(IngredientProperty.FORTIFYING, IngredientProperty.EARTHY)
            ),
            Ingredient(
                id = IngredientId("river_moss"),
                nameKey = "ingredient.river_moss.name",
                descriptionKey = "ingredient.river_moss.description",
                rarity = IngredientRarity.COMMON,
                harvestLocations = listOf("swamp", "river"),
                properties = setOf(IngredientProperty.AQUATIC, IngredientProperty.RESTORATIVE)
            ),
            
            // Uncommon ingredients
            Ingredient(
                id = IngredientId("moonpetal"),
                nameKey = "ingredient.moonpetal.name",
                descriptionKey = "ingredient.moonpetal.description",
                rarity = IngredientRarity.UNCOMMON,
                harvestLocations = listOf("forest", "meadow"),
                properties = setOf(IngredientProperty.MYSTICAL, IngredientProperty.CALMING)
            ),
            Ingredient(
                id = IngredientId("cave_fungus"),
                nameKey = "ingredient.cave_fungus.name",
                descriptionKey = "ingredient.cave_fungus.description",
                rarity = IngredientRarity.UNCOMMON,
                harvestLocations = listOf("cave"),
                properties = setOf(IngredientProperty.TOXIC, IngredientProperty.EARTHY)
            ),
            Ingredient(
                id = IngredientId("ember_ash"),
                nameKey = "ingredient.ember_ash.name",
                descriptionKey = "ingredient.ember_ash.description",
                rarity = IngredientRarity.UNCOMMON,
                harvestLocations = listOf("volcano"),
                properties = setOf(IngredientProperty.FIERY, IngredientProperty.ENERGIZING)
            ),
            
            // Rare ingredients
            Ingredient(
                id = IngredientId("starlight_shard"),
                nameKey = "ingredient.starlight_shard.name",
                descriptionKey = "ingredient.starlight_shard.description",
                rarity = IngredientRarity.RARE,
                harvestLocations = listOf("mountain", "meadow"),
                properties = setOf(IngredientProperty.MYSTICAL, IngredientProperty.ENERGIZING)
            ),
            Ingredient(
                id = IngredientId("venom_sac"),
                nameKey = "ingredient.venom_sac.name",
                descriptionKey = "ingredient.venom_sac.description",
                rarity = IngredientRarity.RARE,
                harvestLocations = listOf("swamp", "cave"),
                properties = setOf(IngredientProperty.TOXIC, IngredientProperty.VOLATILE)
            ),
            
            // Exotic ingredients
            Ingredient(
                id = IngredientId("phoenix_feather"),
                nameKey = "ingredient.phoenix_feather.name",
                descriptionKey = "ingredient.phoenix_feather.description",
                rarity = IngredientRarity.EXOTIC,
                harvestLocations = listOf("volcano"),
                properties = setOf(IngredientProperty.FIERY, IngredientProperty.RESTORATIVE, IngredientProperty.MYSTICAL)
            ),
            Ingredient(
                id = IngredientId("deep_coral"),
                nameKey = "ingredient.deep_coral.name",
                descriptionKey = "ingredient.deep_coral.description",
                rarity = IngredientRarity.EXOTIC,
                harvestLocations = listOf("ocean"),
                properties = setOf(IngredientProperty.AQUATIC, IngredientProperty.FORTIFYING)
            ),
            
            // Legendary ingredients
            Ingredient(
                id = IngredientId("dragon_scale"),
                nameKey = "ingredient.dragon_scale.name",
                descriptionKey = "ingredient.dragon_scale.description",
                rarity = IngredientRarity.LEGENDARY,
                harvestLocations = listOf("dragon_lair"),
                properties = setOf(IngredientProperty.FIERY, IngredientProperty.FORTIFYING, IngredientProperty.MYSTICAL)
            ),
            Ingredient(
                id = IngredientId("void_essence"),
                nameKey = "ingredient.void_essence.name",
                descriptionKey = "ingredient.void_essence.description",
                rarity = IngredientRarity.LEGENDARY,
                harvestLocations = listOf("abyss"),
                properties = setOf(IngredientProperty.MYSTICAL, IngredientProperty.VOLATILE, IngredientProperty.TOXIC)
            )
        )
    }
}
