package com.jalmarquest.feature.skills

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.Player
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CraftingManager(
    private val recipeCatalog: RecipeCatalog,
    private val mutex: Mutex = Mutex()
) {
    private var _state = CraftingState()
    val state: CraftingState get() = _state

    data class BatchCraftRequest(
        val recipeId: RecipeId,
        val quantity: Int,
        val stationId: CraftingStationId
    )

    data class BatchCraftResult(
        val successful: Int,
        val failed: Int,
        val itemsProduced: Map<ItemId, Int>,
        val ingredientsConsumed: Map<IngredientId, Int>,
        val itemsConsumed: Map<ItemId, Int>,
        val totalCraftingTime: Long,
        val experienceGained: Int
    )

    suspend fun batchCraft(
        request: BatchCraftRequest,
        player: Player,
        currentTimeMillis: Long
    ): BatchCraftResult = mutex.withLock {
        val recipe = recipeCatalog.getRecipe(request.recipeId)
            ?: return BatchCraftResult(0, request.quantity, emptyMap(), emptyMap(), emptyMap(), 0L, 0)

        var successful = 0
        var failed = 0
        val itemsProduced = mutableMapOf<ItemId, Int>()
        val ingredientsConsumed = mutableMapOf<IngredientId, Int>()
        val itemsConsumed = mutableMapOf<ItemId, Int>()
        var totalTime = 0L
        var totalXp = 0

        for (i in 1..request.quantity) {
            // Check if we still have resources
            val canCraft = canCraft(recipe, player)
            if (!canCraft) {
                failed++
                continue
            }

            // Consume resources
            recipe.ingredientRequirements.forEach { (id, amount) ->
                ingredientsConsumed[id] = (ingredientsConsumed[id] ?: 0) + amount
            }
            recipe.itemRequirements.forEach { (id, amount) ->
                itemsConsumed[id] = (itemsConsumed[id] ?: 0) + amount
            }

            // Calculate success with skill bonuses
            val successChance = calculateCraftingSuccessChance(recipe, player)
            if (kotlin.random.Random.nextFloat() < successChance) {
                successful++
                recipe.outputItems.forEach { (id, amount) ->
                    itemsProduced[id] = (itemsProduced[id] ?: 0) + amount
                }
                totalXp += recipe.experienceReward
            } else {
                failed++
            }

            totalTime += recipe.craftingTime
        }

        // Update state
        _state = _state.copy(
            lastCraftTime = currentTimeMillis,
            totalItemsCrafted = _state.totalItemsCrafted + successful
        )

        BatchCraftResult(
            successful = successful,
            failed = failed,
            itemsProduced = itemsProduced,
            ingredientsConsumed = ingredientsConsumed,
            itemsConsumed = itemsConsumed,
            totalCraftingTime = totalTime,
            experienceGained = totalXp
        )
    }

    private fun calculateCraftingSuccessChance(recipe: CraftingRecipe, player: Player): Float {
        val baseChance = 0.8f
        val skillBonus = player.skillProgress?.skills?.get(SkillType.CRAFTING)?.level?.times(0.02f) ?: 0f
        return (baseChance + skillBonus).coerceIn(0.5f, 1.0f)
    }

    private fun canCraft(recipe: CraftingRecipe, player: Player): Boolean {
        // Check if player has required ingredients and items
        val hasIngredients = recipe.ingredientRequirements.all { (id, amount) ->
            player.inventory.getItemAmount(id) >= amount
        }
        val hasItems = recipe.itemRequirements.all { (id, amount) ->
            player.inventory.getItemAmount(id) >= amount
        }
        return hasIngredients && hasItems
    }
}