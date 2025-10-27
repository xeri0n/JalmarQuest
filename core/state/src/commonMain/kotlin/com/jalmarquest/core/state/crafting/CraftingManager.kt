package com.jalmarquest.core.state.crafting

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.skills.SkillManager
import com.jalmarquest.core.state.perf.currentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Result of a crafting attempt.
 */
sealed class CraftingResult {
    data class Success(val item: CraftedItem, val quantity: Int) : CraftingResult()
    data class Failure(val reason: String) : CraftingResult()
}

/**
 * Manages crafting operations, recipe unlocking, and item creation.
 * Integrates with SkillManager for XP rewards and requirement validation.
 */
class CraftingManager(
    private val initialCraftingKnowledge: CraftingKnowledge = CraftingKnowledge(),
    private val recipeDefinitions: Map<CraftingRecipeId, CraftingRecipe> = emptyMap(),
    private val skillManager: SkillManager? = null
) {
    private val _craftingKnowledgeState = MutableStateFlow(initialCraftingKnowledge)
    val craftingKnowledgeState: StateFlow<CraftingKnowledge> = _craftingKnowledgeState.asStateFlow()

    /**
     * Gets all recipes that can be crafted at a specific station.
     * Filters by known recipes and station type.
     */
    fun getCraftableRecipes(station: CraftingStation): List<CraftingRecipe> {
        val knowledge = _craftingKnowledgeState.value
        return recipeDefinitions.values.filter { recipe ->
            knowledge.knowsRecipe(recipe.id) && recipe.requiredStation == station
        }
    }

    /**
     * Gets all recipes known to the player.
     */
    fun getKnownRecipes(): List<CraftingRecipe> {
        val knowledge = _craftingKnowledgeState.value
        return recipeDefinitions.values.filter { knowledge.knowsRecipe(it.id) }
    }

    /**
     * Gets all recipes of a specific type.
     */
    fun getRecipesByType(type: CraftingRecipeType): List<CraftingRecipe> {
        return recipeDefinitions.values.filter { it.type == type }
    }

    /**
     * Checks if a recipe can be crafted right now.
     * Validates: recipe known, ingredients available, items available, skills met.
     */
    fun canCraft(
        recipeId: CraftingRecipeId,
        inventory: Map<IngredientId, Int>,
        items: Map<ItemId, Int>
    ): Boolean {
        val recipe = recipeDefinitions[recipeId] ?: return false
        val knowledge = _craftingKnowledgeState.value
        
        // Must know the recipe
        if (!knowledge.knowsRecipe(recipeId)) return false
        
        // Check ingredient requirements
        recipe.requiredIngredients.forEach { (ingredientId, required) ->
            val available = inventory[ingredientId] ?: 0
            if (available < required) return false
        }
        
        // Check item requirements
        recipe.requiredItems.forEach { (itemId, required) ->
            val available = items[itemId] ?: 0
            if (available < required) return false
        }
        
        // Check skill requirements
        if (skillManager != null) {
            recipe.requiredSkills.forEach { (skillId, requiredLevel) ->
                val skill = skillManager.getSkill(skillId)
                if (skill == null || skill.level < requiredLevel) return false
            }
        }
        
        return true
    }

    /**
     * Attempts to craft an item from a recipe.
     * Returns Success with the crafted item or Failure with a reason.
     */
    fun craftItem(
        recipeId: CraftingRecipeId,
        inventory: Map<IngredientId, Int>,
        items: Map<ItemId, Int>
    ): CraftingResult {
        val recipe = recipeDefinitions[recipeId] 
            ?: return CraftingResult.Failure("Recipe not found")
        
        if (!canCraft(recipeId, inventory, items)) {
            return CraftingResult.Failure("Cannot craft: requirements not met")
        }
        
        // Calculate success chance
        val baseChance = recipe.baseSuccessRate
        val craftBonus = skillManager?.getTotalBonus(AbilityType.CRAFT_SUCCESS) ?: 0
        val totalChance = (baseChance + craftBonus).coerceIn(0, 100)
        
        // For now, assume success (deterministic for testing)
        // In production, use: val success = Random.nextInt(100) < totalChance
        val success = true
        
        if (!success) {
            return CraftingResult.Failure("Crafting failed")
        }
        
        // Add crafted items to knowledge
        _craftingKnowledgeState.update { knowledge ->
            val updated = knowledge.addCraftedItems(recipe.resultItem.id, recipe.resultQuantity)
            updated.updateCraftTimestamp(currentTimeMillis())
        }
        
        // Award skill XP
        if (skillManager != null) {
            recipe.skillXPReward.forEach { (skillId, xp) ->
                skillManager.gainSkillXP(skillId, xp)
            }
        }
        
        return CraftingResult.Success(recipe.resultItem, recipe.resultQuantity)
    }

    /**
     * Unlocks a recipe for the player.
     */
    fun unlockRecipe(recipeId: CraftingRecipeId): Boolean {
        val recipe = recipeDefinitions[recipeId] ?: return false
        
        _craftingKnowledgeState.update { knowledge ->
            knowledge.learnRecipe(recipeId)
        }
        
        return true
    }

    /**
     * Checks if a recipe is known.
     */
    fun knowsRecipe(recipeId: CraftingRecipeId): Boolean {
        return _craftingKnowledgeState.value.knowsRecipe(recipeId)
    }

    /**
     * Gets a specific recipe by ID.
     */
    fun getRecipe(recipeId: CraftingRecipeId): CraftingRecipe? {
        return recipeDefinitions[recipeId]
    }

    /**
     * Gets the quantity of a specific crafted item.
     */
    fun getItemQuantity(itemId: CraftedItemId): Int {
        return _craftingKnowledgeState.value.getItemQuantity(itemId)
    }

    /**
     * Checks if player has at least one of an item.
     */
    fun hasItem(itemId: CraftedItemId): Boolean {
        return _craftingKnowledgeState.value.hasItem(itemId)
    }

    /**
     * Adds crafted items to inventory.
     */
    fun addCraftedItems(itemId: CraftedItemId, quantity: Int) {
        _craftingKnowledgeState.update { knowledge ->
            knowledge.addCraftedItems(itemId, quantity)
        }
    }

    /**
     * Removes crafted items from inventory.
     * Throws IllegalArgumentException if trying to remove more than owned.
     */
    fun removeCraftedItems(itemId: CraftedItemId, quantity: Int) {
        _craftingKnowledgeState.update { knowledge ->
            knowledge.removeCraftedItems(itemId, quantity)
        }
    }

    /**
     * Equips an item to a specific slot.
     * Throws IllegalArgumentException if item not owned.
     */
    fun equipItem(itemId: CraftedItemId, slot: EquipmentSlot) {
        _craftingKnowledgeState.update { knowledge ->
            knowledge.equipItem(slot, itemId)
        }
    }

    /**
     * Unequips an item from a slot.
     */
    fun unequipSlot(slot: EquipmentSlot) {
        _craftingKnowledgeState.update { knowledge ->
            knowledge.unequipSlot(slot)
        }
    }

    /**
     * Gets the item equipped in a specific slot.
     */
    fun getEquippedItem(slot: EquipmentSlot): CraftedItemId? {
        return _craftingKnowledgeState.value.getEquippedItem(slot)
    }

    /**
     * Gets all equipped items.
     */
    fun getEquippedItems(): Map<EquipmentSlot, CraftedItemId> {
        return _craftingKnowledgeState.value.equippedItems
    }

    /**
     * Checks if an item is equipped in any slot.
     */
    fun isEquipped(itemId: CraftedItemId): Boolean {
        return _craftingKnowledgeState.value.isEquipped(itemId)
    }

    /**
     * Gets all recipes that can be discovered based on current conditions.
     * Filters by discovery method and skill requirements.
     */
    fun getDiscoverableRecipes(): List<CraftingRecipe> {
        return recipeDefinitions.values.filter { recipe ->
            !knowsRecipe(recipe.id) && canDiscoverRecipe(recipe)
        }
    }

    /**
     * Checks if a recipe can be discovered based on its discovery method.
     */
    private fun canDiscoverRecipe(recipe: CraftingRecipe): Boolean {
        return when (recipe.discoveryMethod) {
            CraftingDiscoveryMethod.STARTER -> true
            CraftingDiscoveryMethod.SKILL_LEVEL -> {
                // Check if player meets skill requirements
                if (skillManager == null) return false
                recipe.requiredSkills.all { (skillId, level) ->
                    val skill = skillManager.getSkill(skillId)
                    skill != null && skill.level >= level
                }
            }
            else -> false // Other methods require explicit unlock
        }
    }

    /**
     * Attempts to discover new recipes based on current skill levels.
     * Returns list of newly discovered recipes.
     */
    fun discoverRecipes(): List<CraftingRecipe> {
        val discovered = mutableListOf<CraftingRecipe>()
        
        getDiscoverableRecipes().forEach { recipe ->
            if (unlockRecipe(recipe.id)) {
                discovered.add(recipe)
            }
        }
        
        return discovered
    }

    /**
     * Gets recipes filtered by discovery method.
     */
    fun getRecipesByDiscoveryMethod(method: CraftingDiscoveryMethod): List<CraftingRecipe> {
        return recipeDefinitions.values.filter { it.discoveryMethod == method }
    }

    /**
     * Updates the entire crafting knowledge state (used by GameStateManager during load).
     */
    fun updateCraftingKnowledge(newKnowledge: CraftingKnowledge) {
        _craftingKnowledgeState.update { newKnowledge }
    }

    /**
     * Resets all crafting knowledge (for testing or new game+).
     */
    fun resetCraftingKnowledge() {
        _craftingKnowledgeState.update { CraftingKnowledge() }
    }

    /**
     * Gets timestamp of last craft.
     */
    fun getLastCraftTimestamp(): Long {
        return _craftingKnowledgeState.value.lastCraftAt
    }

    /**
     * Gets total number of unique recipes known.
     */
    fun getKnownRecipeCount(): Int {
        return _craftingKnowledgeState.value.knownRecipes.size
    }

    /**
     * Gets total number of unique items crafted.
     */
    fun getUniqueCraftedItemCount(): Int {
        return _craftingKnowledgeState.value.craftedItems.size
    }

    /**
     * Gets total quantity of all crafted items.
     */
    fun getTotalCraftedItemQuantity(): Int {
        return _craftingKnowledgeState.value.craftedItems.values.sum()
    }
}
