package com.jalmarquest.core.state.crafting

import com.jalmarquest.core.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages recipe unlocking and discovery.
 * Alpha 2.3: Reagent & Recipe System Overhaul
 * 
 * Handles three primary discovery methods:
 * 1. EXPERIMENTATION: Random discovery through crafting attempts
 * 2. NPC_DIALOGUE: Learning recipes from NPCs/companions
 * 3. RECIPE_SCROLL: Using consumable recipe scroll items
 */
class RecipeUnlockManager(
    initialCraftingKnowledge: CraftingKnowledge = CraftingKnowledge(),
    private val recipeLibrary: Map<CraftingRecipeId, CraftingRecipe> = emptyMap()
) {
    private val _craftingKnowledge = MutableStateFlow(initialCraftingKnowledge)
    val craftingKnowledge: StateFlow<CraftingKnowledge> = _craftingKnowledge.asStateFlow()
    
    /**
     * Attempt to discover a recipe through experimentation.
     * Returns the discovered recipe if successful, null otherwise.
     * 
     * Experimentation discovery requires:
     * - Recipe has discoveryMethod = EXPERIMENTATION
     * - Recipe is not already known
     * - Random chance based on player skill level
     */
    fun attemptExperimentationDiscovery(
        skillLevels: Map<SkillId, Int>,
        randomSeed: Float = kotlin.random.Random.nextFloat()
    ): CraftingRecipe? {
        val currentKnowledge = _craftingKnowledge.value
        
        // Get all recipes that can be discovered through experimentation
        val experimentalRecipes = recipeLibrary.values.filter { recipe ->
            recipe.discoveryMethod == CraftingDiscoveryMethod.EXPERIMENTATION &&
            !currentKnowledge.knowsRecipe(recipe.id)
        }
        
        if (experimentalRecipes.isEmpty()) return null
        
        // Calculate discovery chance based on relevant skills
        // Higher skill levels increase discovery chance
        experimentalRecipes.forEach { recipe ->
            val relevantSkillLevel = recipe.requiredSkills.maxOfOrNull { (skillId, _) ->
                skillLevels[skillId] ?: 0
            } ?: 0
            
            // Base 2% discovery chance, +1% per skill level
            val discoveryChance = 0.02f + (relevantSkillLevel * 0.01f)
            
            if (randomSeed < discoveryChance) {
                // Recipe discovered!
                _craftingKnowledge.value = currentKnowledge.learnRecipe(recipe.id)
                return recipe
            }
        }
        
        return null
    }
    
    /**
     * Learn a recipe from an NPC or companion.
     * Used when dialogue/quest rewards grant recipes.
     * 
     * @param recipeId The recipe to learn
     * @param source Optional source identifier (NPC name, companion ID)
     * @return true if recipe was learned, false if already known or recipe doesn't exist
     */
    fun learnFromNPC(
        recipeId: CraftingRecipeId,
        source: String? = null
    ): Boolean {
        val recipe = recipeLibrary[recipeId] ?: return false
        val currentKnowledge = _craftingKnowledge.value
        
        if (currentKnowledge.knowsRecipe(recipeId)) {
            return false // Already known
        }
        
        // Verify recipe can be taught by NPCs
        if (recipe.discoveryMethod != CraftingDiscoveryMethod.COMPANION_TAUGHT &&
            recipe.discoveryMethod != CraftingDiscoveryMethod.PURCHASE &&
            recipe.discoveryMethod != CraftingDiscoveryMethod.QUEST_REWARD) {
            return false
        }
        
        _craftingKnowledge.value = currentKnowledge.learnRecipe(recipeId)
        return true
    }
    
    /**
     * Learn a recipe from a recipe scroll consumable.
     * 
     * @param recipeId The recipe encoded in the scroll
     * @return true if recipe was learned, false if already known or invalid
     */
    fun learnFromScroll(recipeId: CraftingRecipeId): Boolean {
        val recipe = recipeLibrary[recipeId] ?: return false
        val currentKnowledge = _craftingKnowledge.value
        
        if (currentKnowledge.knowsRecipe(recipeId)) {
            return false // Already known
        }
        
        _craftingKnowledge.value = currentKnowledge.learnRecipe(recipeId)
        return true
    }
    
    /**
     * Unlock a recipe by reaching a skill milestone.
     * Called when player levels up a skill.
     * 
     * @param skillId The skill that was leveled up
     * @param newLevel The new skill level
     * @return List of recipes unlocked by this milestone
     */
    fun unlockBySkillLevel(
        skillId: SkillId,
        newLevel: Int
    ): List<CraftingRecipe> {
        val currentKnowledge = _craftingKnowledge.value
        val unlockedRecipes = mutableListOf<CraftingRecipe>()
        
        recipeLibrary.values.forEach { recipe ->
            if (recipe.discoveryMethod == CraftingDiscoveryMethod.SKILL_LEVEL &&
                !currentKnowledge.knowsRecipe(recipe.id) &&
                recipe.requiredSkills[skillId] != null &&
                recipe.requiredSkills[skillId]!! <= newLevel
            ) {
                _craftingKnowledge.value = _craftingKnowledge.value.learnRecipe(recipe.id)
                unlockedRecipes.add(recipe)
            }
        }
        
        return unlockedRecipes
    }
    
    /**
     * Unlock a recipe by completing a quest.
     * 
     * @param questId The quest that was completed
     * @param grantedRecipeIds List of recipe IDs granted by the quest
     * @return List of recipes actually unlocked (excluding already known)
     */
    fun unlockByQuest(
        questId: String,
        grantedRecipeIds: List<CraftingRecipeId>
    ): List<CraftingRecipe> {
        val currentKnowledge = _craftingKnowledge.value
        val unlockedRecipes = mutableListOf<CraftingRecipe>()
        
        grantedRecipeIds.forEach { recipeId ->
            val recipe = recipeLibrary[recipeId]
            if (recipe != null && !currentKnowledge.knowsRecipe(recipeId)) {
                _craftingKnowledge.value = _craftingKnowledge.value.learnRecipe(recipeId)
                unlockedRecipes.add(recipe)
            }
        }
        
        return unlockedRecipes
    }
    
    /**
     * Unlock a recipe by purchasing it from a shop/NPC.
     * 
     * @param recipeId The recipe to purchase
     * @param cost The seed cost (for validation)
     * @return true if recipe was purchased, false if already known or invalid
     */
    fun purchaseRecipe(
        recipeId: CraftingRecipeId,
        cost: Int
    ): Boolean {
        val recipe = recipeLibrary[recipeId] ?: return false
        val currentKnowledge = _craftingKnowledge.value
        
        if (currentKnowledge.knowsRecipe(recipeId)) {
            return false // Already known
        }
        
        if (recipe.discoveryMethod != CraftingDiscoveryMethod.PURCHASE) {
            return false // Not purchasable
        }
        
        _craftingKnowledge.value = currentKnowledge.learnRecipe(recipeId)
        return true
    }
    
    /**
     * Unlock a recipe by internalizing a thought.
     * Used with the Thought Cabinet system.
     * 
     * @param thoughtId The thought that was internalized
     * @param grantedRecipeIds List of recipe IDs granted by the thought
     * @return List of recipes actually unlocked
     */
    fun unlockByThought(
        thoughtId: String,
        grantedRecipeIds: List<CraftingRecipeId>
    ): List<CraftingRecipe> {
        val currentKnowledge = _craftingKnowledge.value
        val unlockedRecipes = mutableListOf<CraftingRecipe>()
        
        grantedRecipeIds.forEach { recipeId ->
            val recipe = recipeLibrary[recipeId]
            if (recipe != null && 
                recipe.discoveryMethod == CraftingDiscoveryMethod.THOUGHT_UNLOCK &&
                !currentKnowledge.knowsRecipe(recipeId)) {
                _craftingKnowledge.value = _craftingKnowledge.value.learnRecipe(recipeId)
                unlockedRecipes.add(recipe)
            }
        }
        
        return unlockedRecipes
    }
    
    /**
     * Get all recipes that can be discovered through experimentation.
     * Useful for showing potential discoveries to the player.
     */
    fun getExperimentalRecipes(): List<CraftingRecipe> {
        val currentKnowledge = _craftingKnowledge.value
        return recipeLibrary.values.filter { recipe ->
            recipe.discoveryMethod == CraftingDiscoveryMethod.EXPERIMENTATION &&
            !currentKnowledge.knowsRecipe(recipe.id)
        }
    }
    
    /**
     * Get all recipes that can be purchased.
     */
    fun getPurchasableRecipes(): List<CraftingRecipe> {
        val currentKnowledge = _craftingKnowledge.value
        return recipeLibrary.values.filter { recipe ->
            recipe.discoveryMethod == CraftingDiscoveryMethod.PURCHASE &&
            !currentKnowledge.knowsRecipe(recipe.id)
        }
    }
    
    /**
     * Get progress towards discovering all recipes.
     */
    fun getDiscoveryProgress(): RecipeDiscoveryProgress {
        val currentKnowledge = _craftingKnowledge.value
        val totalRecipes = recipeLibrary.size
        val knownRecipes = currentKnowledge.knownRecipes.size
        
        return RecipeDiscoveryProgress(
            totalRecipes = totalRecipes,
            discoveredRecipes = knownRecipes,
            completionPercentage = if (totalRecipes > 0) {
                (knownRecipes.toFloat() / totalRecipes.toFloat() * 100).toInt()
            } else 0
        )
    }
}

/**
 * Progress tracking for recipe discovery.
 */
data class RecipeDiscoveryProgress(
    val totalRecipes: Int,
    val discoveredRecipes: Int,
    val completionPercentage: Int
)
