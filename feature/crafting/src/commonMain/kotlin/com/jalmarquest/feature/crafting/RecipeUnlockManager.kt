class RecipeUnlockManager(
    private val gameStateManager: GameStateManager,
    private val recipeLibrary: RecipeLibraryService
) {
    // FIX: Ensure all unlock methods work correctly
    suspend fun unlockRecipeViaExperimentation(
        reagents: List<Reagent>,
        stationType: StationType
    ): ExperimentResult {
        val matchingRecipe = recipeLibrary.findRecipeByIngredients(reagents, stationType)
        
        return if (matchingRecipe != null && !isRecipeKnown(matchingRecipe.id)) {
            gameStateManager.batchUpdate { player ->
                player.copy(
                    knownRecipes = player.knownRecipes + matchingRecipe.id,
                    // FIX: Also grant discovery XP
                    experience = player.experience + 50
                )
            }
            
            ExperimentResult.Success(
                recipe = matchingRecipe,
                message = "Discovery! You've learned: ${matchingRecipe.name}"
            )
        } else {
            ExperimentResult.Failure(
                message = "The combination yields nothing useful."
            )
        }
    }
    
    suspend fun unlockRecipeFromScroll(scrollItem: Item) {
        require(scrollItem.type == ItemType.RECIPE_SCROLL) {
            "Item must be a recipe scroll"
        }
        
        val recipeId = scrollItem.metadata["recipe_id"] as? String
            ?: throw IllegalStateException("Recipe scroll missing recipe_id")
        
        gameStateManager.batchUpdate { player ->
            player.copy(
                knownRecipes = player.knownRecipes + recipeId,
                // FIX: Consume the scroll
                inventory = player.inventory.removeItem(scrollItem.id)
            )
        }
    }
}