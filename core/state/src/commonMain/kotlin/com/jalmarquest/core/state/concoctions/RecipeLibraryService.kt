package com.jalmarquest.core.state.concoctions

import com.jalmarquest.core.model.*

/**
 * Service for managing recipe catalog and discovery mechanics.
 */
class RecipeLibraryService {
    private val recipeCatalog: List<Recipe> = buildRecipeCatalog()
    
    /**
     * Get all recipes in the catalog.
     */
    fun getAllRecipes(): List<Recipe> = recipeCatalog
    
    /**
     * Get recipe by ID.
     */
    fun getRecipe(id: RecipeId): Recipe? {
        return recipeCatalog.find { it.id == id }
    }
    
    /**
     * Get recipes discovered by the player.
     */
    fun getDiscoveredRecipes(recipeBook: RecipeBook): List<Recipe> {
        return recipeCatalog.filter { recipeBook.hasRecipe(it.id) }
    }
    
    /**
     * Get recipes that can be unlocked via milestones.
     */
    fun getMilestoneRecipes(): List<Recipe> {
        return recipeCatalog.filter { it.discoveryMethod == DiscoveryMethod.MILESTONE }
    }
    
    /**
     * Check if player can craft a recipe (has ingredients and recipe discovered).
     */
    fun canCraft(recipe: Recipe, ingredientInventory: IngredientInventory, recipeBook: RecipeBook): Boolean {
        if (!recipeBook.hasRecipe(recipe.id)) {
            return false
        }
        
        return recipe.requiredIngredients.all { (ingredientId, quantity) ->
            ingredientInventory.hasIngredient(ingredientId, quantity)
        }
    }
    
    private fun buildRecipeCatalog(): List<Recipe> {
        return listOf(
            // Basic healing potion
            Recipe(
                id = RecipeId("minor_health_potion"),
                nameKey = "recipe.minor_health_potion.name",
                descriptionKey = "recipe.minor_health_potion.description",
                requiredIngredients = mapOf(
                    IngredientId("wildflower") to 3,
                    IngredientId("river_moss") to 2
                ),
                discoveryMethod = DiscoveryMethod.MILESTONE,
                resultingConcoction = ConcoctionTemplate(
                    id = ConcoctionId("minor_health_potion"),
                    nameKey = "concoction.minor_health_potion.name",
                    descriptionKey = "concoction.minor_health_potion.description",
                    effects = listOf(
                        ConcoctionEffect(EffectType.HEALTH_REGEN, magnitude = 5, isPositive = true)
                    ),
                    durationSeconds = 300, // 5 minutes
                    stackLimit = 3
                )
            ),
            
            // Seed boost potion
            Recipe(
                id = RecipeId("fortune_brew"),
                nameKey = "recipe.fortune_brew.name",
                descriptionKey = "recipe.fortune_brew.description",
                requiredIngredients = mapOf(
                    IngredientId("moonpetal") to 2,
                    IngredientId("oak_bark") to 3
                ),
                discoveryMethod = DiscoveryMethod.MILESTONE,
                resultingConcoction = ConcoctionTemplate(
                    id = ConcoctionId("fortune_brew"),
                    nameKey = "concoction.fortune_brew.name",
                    descriptionKey = "concoction.fortune_brew.description",
                    effects = listOf(
                        ConcoctionEffect(EffectType.SEED_BOOST, magnitude = 25, isPositive = true),
                        ConcoctionEffect(EffectType.LUCK_BOOST, magnitude = 10, isPositive = true)
                    ),
                    durationSeconds = 600, // 10 minutes
                    stackLimit = 1
                )
            ),
            
            // Experience boost
            Recipe(
                id = RecipeId("wisdom_elixir"),
                nameKey = "recipe.wisdom_elixir.name",
                descriptionKey = "recipe.wisdom_elixir.description",
                requiredIngredients = mapOf(
                    IngredientId("starlight_shard") to 1,
                    IngredientId("moonpetal") to 3
                ),
                discoveryMethod = DiscoveryMethod.EXPERIMENTATION,
                resultingConcoction = ConcoctionTemplate(
                    id = ConcoctionId("wisdom_elixir"),
                    nameKey = "concoction.wisdom_elixir.name",
                    descriptionKey = "concoction.wisdom_elixir.description",
                    effects = listOf(
                        ConcoctionEffect(EffectType.EXPERIENCE_BOOST, magnitude = 50, isPositive = true),
                        ConcoctionEffect(EffectType.CLARITY, magnitude = 1, isPositive = true)
                    ),
                    durationSeconds = 1800, // 30 minutes
                    stackLimit = 1
                )
            ),
            
            // Combat potion
            Recipe(
                id = RecipeId("berserker_draught"),
                nameKey = "recipe.berserker_draught.name",
                descriptionKey = "recipe.berserker_draught.description",
                requiredIngredients = mapOf(
                    IngredientId("ember_ash") to 2,
                    IngredientId("venom_sac") to 1,
                    IngredientId("oak_bark") to 2
                ),
                discoveryMethod = DiscoveryMethod.EXPERIMENTATION,
                resultingConcoction = ConcoctionTemplate(
                    id = ConcoctionId("berserker_draught"),
                    nameKey = "concoction.berserker_draught.name",
                    descriptionKey = "concoction.berserker_draught.description",
                    effects = listOf(
                        ConcoctionEffect(EffectType.DAMAGE_BOOST, magnitude = 30, isPositive = true),
                        ConcoctionEffect(EffectType.VULNERABILITY, magnitude = 15, isPositive = false)
                    ),
                    durationSeconds = 300, // 5 minutes
                    stackLimit = 2
                )
            ),
            
            // Defense potion
            Recipe(
                id = RecipeId("ironbark_tonic"),
                nameKey = "recipe.ironbark_tonic.name",
                descriptionKey = "recipe.ironbark_tonic.description",
                requiredIngredients = mapOf(
                    IngredientId("oak_bark") to 5,
                    IngredientId("deep_coral") to 1
                ),
                discoveryMethod = DiscoveryMethod.PURCHASE,
                resultingConcoction = ConcoctionTemplate(
                    id = ConcoctionId("ironbark_tonic"),
                    nameKey = "concoction.ironbark_tonic.name",
                    descriptionKey = "concoction.ironbark_tonic.description",
                    effects = listOf(
                        ConcoctionEffect(EffectType.DEFENSE_BOOST, magnitude = 40, isPositive = true)
                    ),
                    durationSeconds = 900, // 15 minutes
                    stackLimit = 1
                )
            ),
            
            // Utility: Night vision
            Recipe(
                id = RecipeId("owl_sight_brew"),
                nameKey = "recipe.owl_sight_brew.name",
                descriptionKey = "recipe.owl_sight_brew.description",
                requiredIngredients = mapOf(
                    IngredientId("cave_fungus") to 3,
                    IngredientId("moonpetal") to 2
                ),
                discoveryMethod = DiscoveryMethod.QUEST_REWARD,
                resultingConcoction = ConcoctionTemplate(
                    id = ConcoctionId("owl_sight_brew"),
                    nameKey = "concoction.owl_sight_brew.name",
                    descriptionKey = "concoction.owl_sight_brew.description",
                    effects = listOf(
                        ConcoctionEffect(EffectType.NIGHT_VISION, magnitude = 1, isPositive = true)
                    ),
                    durationSeconds = 1200, // 20 minutes
                    stackLimit = 1
                )
            ),
            
            // Legendary: Ultimate power
            Recipe(
                id = RecipeId("dragon_essence"),
                nameKey = "recipe.dragon_essence.name",
                descriptionKey = "recipe.dragon_essence.description",
                requiredIngredients = mapOf(
                    IngredientId("dragon_scale") to 1,
                    IngredientId("phoenix_feather") to 1,
                    IngredientId("starlight_shard") to 2
                ),
                discoveryMethod = DiscoveryMethod.COMPANION_GIFT,
                resultingConcoction = ConcoctionTemplate(
                    id = ConcoctionId("dragon_essence"),
                    nameKey = "concoction.dragon_essence.name",
                    descriptionKey = "concoction.dragon_essence.description",
                    effects = listOf(
                        ConcoctionEffect(EffectType.DAMAGE_BOOST, magnitude = 50, isPositive = true),
                        ConcoctionEffect(EffectType.DEFENSE_BOOST, magnitude = 50, isPositive = true),
                        ConcoctionEffect(EffectType.HEALTH_REGEN, magnitude = 10, isPositive = true),
                        ConcoctionEffect(EffectType.SEED_BOOST, magnitude = 100, isPositive = true)
                    ),
                    durationSeconds = 600, // 10 minutes
                    stackLimit = 1
                )
            ),
            
            // Poison (debuff example)
            Recipe(
                id = RecipeId("vile_toxin"),
                nameKey = "recipe.vile_toxin.name",
                descriptionKey = "recipe.vile_toxin.description",
                requiredIngredients = mapOf(
                    IngredientId("venom_sac") to 2,
                    IngredientId("cave_fungus") to 3,
                    IngredientId("void_essence") to 1
                ),
                discoveryMethod = DiscoveryMethod.EXPERIMENTATION,
                resultingConcoction = ConcoctionTemplate(
                    id = ConcoctionId("vile_toxin"),
                    nameKey = "concoction.vile_toxin.name",
                    descriptionKey = "concoction.vile_toxin.description",
                    effects = listOf(
                        ConcoctionEffect(EffectType.POISON, magnitude = 3, isPositive = false),
                        ConcoctionEffect(EffectType.WEAKNESS, magnitude = 20, isPositive = false)
                    ),
                    durationSeconds = 180, // 3 minutes
                    stackLimit = 1
                )
            )
        )
    }
}
