package com.jalmarquest.core.state.concoctions

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.perf.PerformanceLogger
import com.jalmarquest.core.state.skills.SkillManager
import com.jalmarquest.core.state.archetype.ArchetypeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * State machine for concoction crafting, consumption, and temporal effect management.
 * Integrates with SkillManager for Alchemy skill bonuses and ArchetypeManager for talent bonuses.
 */
class ConcoctionCrafter(
    private val gameStateManager: GameStateManager,
    private val recipeLibrary: RecipeLibraryService,
    private val harvestService: IngredientHarvestService,
    private val timestampProvider: () -> Long,
    private val skillManager: SkillManager? = null,
    private val archetypeManager: ArchetypeManager? = null
) {
    private val _viewState = MutableStateFlow(ConcoctionViewState())
    val viewState: StateFlow<ConcoctionViewState> = _viewState
    
    init {
        refreshViewState()
    }
    
    /**
     * Harvest ingredients at a location.
     * Applies Foraging skill bonus and archetype INGREDIENT_YIELD_BONUS to harvest luck.
     */
    fun harvestAtLocation(location: String): HarvestResult {
        val player = gameStateManager.playerState.value
        
        // Calculate luck bonus from active concoctions
        val luckBonus = player.activeConcoctions.getActiveEffects()
            .filter { it.type == EffectType.LUCK_BOOST && it.isPositive }
            .sumOf { it.magnitude }
        
        // Add Foraging skill bonus to luck
        val foragingBonus = skillManager?.getTotalBonus(AbilityType.HARVEST_BONUS) ?: 0
        
        // Add archetype ingredient yield bonus
        val archetypeBonus = archetypeManager?.getTotalBonus(TalentType.INGREDIENT_YIELD_BONUS) ?: 0
        
        val totalLuck = luckBonus + foragingBonus + archetypeBonus
        
        val harvested = harvestService.harvestAtLocation(location, totalLuck)
        
        if (harvested.isEmpty()) {
            return HarvestResult(emptyList(), success = false)
        }
        
        // Add harvested ingredients to inventory
        var updatedInventory = player.ingredientInventory
        harvested.forEach { (ingredientId, quantity) ->
            updatedInventory = updatedInventory.addIngredient(ingredientId, quantity)
        }
        
        gameStateManager.updatePlayer { it.copy(ingredientInventory = updatedInventory) }
        gameStateManager.appendChoice("concoctions_harvest_$location")
        
        // Award Foraging XP
        if (skillManager != null) {
            val foragingSkill = skillManager.getSkillByType(SkillType.FORAGING)
            if (foragingSkill != null) {
                val xpPerIngredient = 5
                val totalXP = harvested.size * xpPerIngredient
                skillManager.gainSkillXP(foragingSkill.id, totalXP)
            }
        }
        
        PerformanceLogger.logStateMutation("Concoctions", "harvest", mapOf(
            "location" to location,
            "count" to harvested.size
        ))
        
        refreshViewState()
        return HarvestResult(harvested, success = true)
    }
    
    /**
     * Craft a concoction from a recipe.
     * Applies Alchemy skill bonus to success rate and awards Alchemy XP.
     */
    fun craftConcoction(recipeId: RecipeId): CraftResult {
        val recipe = recipeLibrary.getRecipe(recipeId) ?: return CraftResult.RecipeNotFound
        val player = gameStateManager.playerState.value
        
        if (!player.recipeBook.hasRecipe(recipeId)) {
            return CraftResult.RecipeNotDiscovered
        }
        
        // Check if player has required ingredients
        val hasIngredients = recipe.requiredIngredients.all { (ingredientId, quantity) ->
            player.ingredientInventory.hasIngredient(ingredientId, quantity)
        }
        
        if (!hasIngredients) {
            return CraftResult.InsufficientIngredients
        }
        
        // Consume ingredients
        var updatedInventory = player.ingredientInventory
        recipe.requiredIngredients.forEach { (ingredientId, quantity) ->
            updatedInventory = updatedInventory.removeIngredient(ingredientId, quantity)
        }
        
        // Create the concoction and add to inventory (for now, auto-consume)
        val now = timestampProvider()
        
        // Apply Alchemy skill bonus to duration (10% per bonus point)
        val alchemyBonus = skillManager?.getTotalBonus(AbilityType.CRAFT_SUCCESS) ?: 0
        val durationMultiplier = 1.0 + (alchemyBonus * 0.1)
        val enhancedDuration = (recipe.resultingConcoction.durationSeconds * durationMultiplier).toLong()
        
        val activeConcoction = ActiveConcoction(
            template = recipe.resultingConcoction,
            appliedAt = now,
            expiresAt = now + (enhancedDuration * 1000L),
            stacks = 1
        )
        
        val updatedConcoctions = player.activeConcoctions.addConcoction(activeConcoction)
        
        gameStateManager.updatePlayer { it.copy(
            ingredientInventory = updatedInventory,
            activeConcoctions = updatedConcoctions
        )}
        
        gameStateManager.appendChoice("concoctions_craft_${recipeId.value}")
        
        // Award Alchemy XP
        if (skillManager != null) {
            val alchemySkill = skillManager.getSkillByType(SkillType.ALCHEMY)
            if (alchemySkill != null) {
                val xpAmount = 20 // Base XP for crafting a concoction
                skillManager.gainSkillXP(alchemySkill.id, xpAmount)
            }
        }
        
        PerformanceLogger.logStateMutation("Concoctions", "craft", mapOf(
            "recipeId" to recipeId.value,
            "effectCount" to recipe.resultingConcoction.effects.size
        ))
        
        refreshViewState()
        return CraftResult.Success(activeConcoction)
    }
    
    /**
     * Discover a recipe through gameplay milestone.
     */
    fun discoverRecipe(recipeId: RecipeId, method: DiscoveryMethod = DiscoveryMethod.MILESTONE): Boolean {
        val recipe = recipeLibrary.getRecipe(recipeId) ?: return false
        val player = gameStateManager.playerState.value
        
        if (player.recipeBook.hasRecipe(recipeId)) {
            return false // Already discovered
        }
        
        val now = timestampProvider()
        val updatedRecipeBook = player.recipeBook.discoverRecipe(recipeId)
        
        gameStateManager.updatePlayer { it.copy(recipeBook = updatedRecipeBook) }
        gameStateManager.appendChoice("concoctions_discover_${recipeId.value}")
        
        PerformanceLogger.logStateMutation("Concoctions", "discover", mapOf(
            "recipeId" to recipeId.value,
            "method" to method.name
        ))
        
        refreshViewState()
        return true
    }
    
    /**
     * Attempt to discover a recipe through experimentation.
     * Uses random ingredients to potentially unlock EXPERIMENTATION recipes.
     */
    fun experimentCraft(ingredientIds: List<IngredientId>): ExperimentResult {
        val player = gameStateManager.playerState.value
        val now = timestampProvider()
        
        // Check experimentation cooldown (30 minutes)
        val cooldownMillis = 30 * 60 * 1000L
        if (now - player.recipeBook.lastExperimentAt < cooldownMillis) {
            val remainingMs = cooldownMillis - (now - player.recipeBook.lastExperimentAt)
            return ExperimentResult.OnCooldown(remainingMs)
        }
        
        // Check if player has ingredients
        val hasIngredients = ingredientIds.all { player.ingredientInventory.hasIngredient(it) }
        if (!hasIngredients) {
            return ExperimentResult.InsufficientIngredients
        }
        
        // Find matching EXPERIMENTATION recipes
        val experimentalRecipes = recipeLibrary.getAllRecipes()
            .filter { it.discoveryMethod == DiscoveryMethod.EXPERIMENTATION }
            .filter { !player.recipeBook.hasRecipe(it.id) }
        
        val matchedRecipe = experimentalRecipes.find { recipe ->
            recipe.requiredIngredients.keys == ingredientIds.toSet()
        }
        
        // Consume ingredients regardless of success
        var updatedInventory = player.ingredientInventory
        ingredientIds.forEach { ingredientId ->
            updatedInventory = updatedInventory.removeIngredient(ingredientId, 1)
        }
        
        val updatedRecipeBook = player.recipeBook.copy(lastExperimentAt = now)
        
        if (matchedRecipe != null) {
            // Success! Discover the recipe
            val finalRecipeBook = updatedRecipeBook.discoverRecipe(matchedRecipe.id)
            gameStateManager.updatePlayer { it.copy(
                ingredientInventory = updatedInventory,
                recipeBook = finalRecipeBook
            )}
            gameStateManager.appendChoice("concoctions_experiment_success_${matchedRecipe.id.value}")
            
            refreshViewState()
            return ExperimentResult.Success(matchedRecipe)
        } else {
            // Failure - ingredients consumed, no discovery
            gameStateManager.updatePlayer { it.copy(
                ingredientInventory = updatedInventory,
                recipeBook = updatedRecipeBook
            )}
            gameStateManager.appendChoice("concoctions_experiment_failure")
            
            refreshViewState()
            return ExperimentResult.Failure
        }
    }
    
    /**
     * Update active concoctions by removing expired effects.
     */
    fun updateExpiredEffects() {
        val now = timestampProvider()
        val player = gameStateManager.playerState.value
        
        val updatedConcoctions = player.activeConcoctions.removeExpired(now)
        
        if (updatedConcoctions.active.size != player.activeConcoctions.active.size) {
            gameStateManager.updatePlayer { it.copy(activeConcoctions = updatedConcoctions) }
            refreshViewState()
        }
    }
    
    /**
     * Refresh view state from player state.
     */
    fun refreshViewState(player: Player = gameStateManager.playerState.value) {
        val now = timestampProvider()
        val activeEffects = player.activeConcoctions.removeExpired(now).getActiveEffects()
        val discoveredRecipes = recipeLibrary.getDiscoveredRecipes(player.recipeBook)
        val craftableRecipes = discoveredRecipes.filter { recipe ->
            recipe.requiredIngredients.all { (ingredientId, quantity) ->
                player.ingredientInventory.hasIngredient(ingredientId, quantity)
            }
        }
        
        _viewState.update {
            ConcoctionViewState(
                ingredientInventory = player.ingredientInventory,
                recipeBook = player.recipeBook,
                activeConcoctions = player.activeConcoctions.removeExpired(now),
                activeEffects = activeEffects,
                discoveredRecipes = discoveredRecipes,
                craftableRecipes = craftableRecipes,
                allIngredients = harvestService.getAllIngredients(),
                allRecipes = recipeLibrary.getAllRecipes()
            )
        }
    }
}

/**
 * View state for Concoctions UI.
 */
data class ConcoctionViewState(
    val ingredientInventory: IngredientInventory = IngredientInventory(),
    val recipeBook: RecipeBook = RecipeBook(),
    val activeConcoctions: ActiveConcoctions = ActiveConcoctions(),
    val activeEffects: List<ConcoctionEffect> = emptyList(),
    val discoveredRecipes: List<Recipe> = emptyList(),
    val craftableRecipes: List<Recipe> = emptyList(),
    val allIngredients: List<Ingredient> = emptyList(),
    val allRecipes: List<Recipe> = emptyList()
)

/**
 * Result of harvesting attempt.
 */
data class HarvestResult(
    val harvested: List<Pair<IngredientId, Int>>,
    val success: Boolean
)

/**
 * Result of crafting attempt.
 */
sealed class CraftResult {
    data class Success(val concoction: ActiveConcoction) : CraftResult()
    object RecipeNotFound : CraftResult()
    object RecipeNotDiscovered : CraftResult()
    object InsufficientIngredients : CraftResult()
}

/**
 * Result of experimentation attempt.
 */
sealed class ExperimentResult {
    data class Success(val discoveredRecipe: Recipe) : ExperimentResult()
    object Failure : ExperimentResult()
    object InsufficientIngredients : ExperimentResult()
    data class OnCooldown(val remainingMs: Long) : ExperimentResult()
}
