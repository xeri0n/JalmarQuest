package com.jalmarquest.core.state.concoctions

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import kotlin.test.*

class ConcoctionCrafterTest {
    
    private lateinit var gameStateManager: GameStateManager
    private lateinit var recipeLibrary: RecipeLibraryService
    private lateinit var harvestService: IngredientHarvestService
    private lateinit var concoctionCrafter: ConcoctionCrafter
    private var currentTime = 2000000L  // Start well past the 30-minute cooldown from timestamp 0
    
    @BeforeTest
    fun setup() {
        currentTime = 2000000L  // Reset time for each test, well past cooldown from 0
        val initialPlayer = Player(
            id = "test-player",
            name = "Test Player"
        )
        gameStateManager = GameStateManager(initialPlayer) { currentTime }
        recipeLibrary = RecipeLibraryService()
        harvestService = IngredientHarvestService()
        concoctionCrafter = ConcoctionCrafter(
            gameStateManager = gameStateManager,
            recipeLibrary = recipeLibrary,
            harvestService = harvestService,
            timestampProvider = { currentTime },
            skillManager = null
        )
    }
    
    @Test
    fun testInitialState() {
        val viewState = concoctionCrafter.viewState.value
        
        assertEquals(0, viewState.ingredientInventory.ingredients.size)
        assertEquals(0, viewState.recipeBook.discoveredRecipes.size)
        assertEquals(0, viewState.activeConcoctions.active.size)
        assertEquals(0, viewState.activeEffects.size)
        assertTrue(viewState.allIngredients.isNotEmpty(), "Ingredient catalog should not be empty")
        assertTrue(viewState.allRecipes.isNotEmpty(), "Recipe catalog should not be empty")
    }
    
    @Test
    fun testHarvestIngredients() {
        val result = concoctionCrafter.harvestAtLocation("forest")
        
        val viewState = concoctionCrafter.viewState.value
        val player = gameStateManager.playerState.value
        
        // Should have harvested some ingredients from forest
        assertTrue(player.ingredientInventory.ingredients.isNotEmpty() || !result.success)
        
        // Check choice log only if harvest was successful
        if (result.success) {
            assertTrue(player.choiceLog.entries.any { it.tag.value.startsWith("concoctions_harvest") })
        }
    }
    
    @Test
    fun testHarvestWithLuckBonus() {
        // Give player a luck boost concoction
        val player = gameStateManager.playerState.value
        val luckConcoction = ActiveConcoction(
            template = ConcoctionTemplate(
                id = ConcoctionId("test_luck"),
                nameKey = "test",
                descriptionKey = "test",
                effects = listOf(ConcoctionEffect(EffectType.LUCK_BOOST, magnitude = 50, isPositive = true)),
                durationSeconds = 300,
                stackLimit = 1
            ),
            appliedAt = currentTime,
            expiresAt = currentTime + 300_000,
            stacks = 1
        )
        
        gameStateManager.updatePlayer { it.copy(
            activeConcoctions = ActiveConcoctions(listOf(luckConcoction))
        )}
        
        // Harvest should use luck bonus (hard to test RNG, but we can verify it runs)
        val result = concoctionCrafter.harvestAtLocation("forest")
        
        // Verify the system didn't crash with luck bonus
        assertNotNull(result)
    }
    
    @Test
    fun testDiscoverRecipe() {
        val recipeId = RecipeId("minor_health_potion")
        
        val success = concoctionCrafter.discoverRecipe(recipeId)
        
        assertTrue(success, "Should successfully discover recipe")
        
        val player = gameStateManager.playerState.value
        assertTrue(player.recipeBook.hasRecipe(recipeId))
        assertTrue(player.choiceLog.entries.any { it.tag.value == "concoctions_discover_minor_health_potion" })
        
        val viewState = concoctionCrafter.viewState.value
        assertTrue(viewState.discoveredRecipes.any { it.id == recipeId })
    }
    
    @Test
    fun testDiscoverDuplicateRecipe() {
        val recipeId = RecipeId("minor_health_potion")
        
        concoctionCrafter.discoverRecipe(recipeId)
        val secondAttempt = concoctionCrafter.discoverRecipe(recipeId)
        
        assertFalse(secondAttempt, "Should not be able to discover same recipe twice")
    }
    
    @Test
    fun testCraftConcoction() {
        // Discover recipe
        val recipeId = RecipeId("minor_health_potion")
        concoctionCrafter.discoverRecipe(recipeId)
        
        // Add required ingredients
        val recipe = recipeLibrary.getRecipe(recipeId)!!
        var player = gameStateManager.playerState.value
        var inventory = player.ingredientInventory
        recipe.requiredIngredients.forEach { (ingredientId, quantity) ->
            inventory = inventory.addIngredient(ingredientId, quantity)
        }
        gameStateManager.updatePlayer { it.copy(ingredientInventory = inventory) }
        
        // Craft the concoction
        val result = concoctionCrafter.craftConcoction(recipeId)
        
        assertTrue(result is CraftResult.Success, "Crafting should succeed")
        
        player = gameStateManager.playerState.value
        
        // Ingredients should be consumed
        recipe.requiredIngredients.forEach { (ingredientId, quantity) ->
            assertEquals(0, player.ingredientInventory.getQuantity(ingredientId))
        }
        
        // Concoction should be active
        assertEquals(1, player.activeConcoctions.active.size)
        assertTrue(player.choiceLog.entries.any { it.tag.value == "concoctions_craft_minor_health_potion" })
        
        val viewState = concoctionCrafter.viewState.value
        assertTrue(viewState.activeEffects.isNotEmpty())
    }
    
    @Test
    fun testCraftWithoutRecipe() {
        val recipeId = RecipeId("minor_health_potion")
        
        // Try to craft without discovering recipe
        val result = concoctionCrafter.craftConcoction(recipeId)
        
        assertTrue(result is CraftResult.RecipeNotDiscovered)
    }
    
    @Test
    fun testCraftWithoutIngredients() {
        val recipeId = RecipeId("minor_health_potion")
        concoctionCrafter.discoverRecipe(recipeId)
        
        // Try to craft without ingredients
        val result = concoctionCrafter.craftConcoction(recipeId)
        
        assertTrue(result is CraftResult.InsufficientIngredients)
    }
    
    @Test
    fun testConcoctionStacking() {
        // Discover and prepare recipe
        val recipeId = RecipeId("minor_health_potion")
        concoctionCrafter.discoverRecipe(recipeId)
        val recipe = recipeLibrary.getRecipe(recipeId)!!
        
        // Add enough ingredients for 3 crafts
        var inventory = gameStateManager.playerState.value.ingredientInventory
        recipe.requiredIngredients.forEach { (ingredientId, quantity) ->
            inventory = inventory.addIngredient(ingredientId, quantity * 3)
        }
        gameStateManager.updatePlayer { it.copy(ingredientInventory = inventory) }
        
        // Craft 3 times
        concoctionCrafter.craftConcoction(recipeId)
        currentTime += 1000
        concoctionCrafter.craftConcoction(recipeId)
        currentTime += 1000
        concoctionCrafter.craftConcoction(recipeId)
        
        val player = gameStateManager.playerState.value
        
        // Should have 1 concoction entry with 3 stacks (stack limit is 3)
        assertEquals(1, player.activeConcoctions.active.size)
        assertEquals(3, player.activeConcoctions.active.first().stacks)
    }
    
    @Test
    fun testEffectExpiration() {
        // Add a short-lived concoction
        val concoction = ActiveConcoction(
            template = ConcoctionTemplate(
                id = ConcoctionId("test_potion"),
                nameKey = "test",
                descriptionKey = "test",
                effects = listOf(ConcoctionEffect(EffectType.HEALTH_REGEN, magnitude = 5, isPositive = true)),
                durationSeconds = 10,
                stackLimit = 1
            ),
            appliedAt = currentTime,
            expiresAt = currentTime + 10_000, // 10 seconds
            stacks = 1
        )
        
        gameStateManager.updatePlayer { it.copy(
            activeConcoctions = ActiveConcoctions(listOf(concoction))
        )}
        
        var player = gameStateManager.playerState.value
        assertEquals(1, player.activeConcoctions.active.size)
        
        // Advance time past expiration
        currentTime += 11_000
        concoctionCrafter.updateExpiredEffects()
        
        player = gameStateManager.playerState.value
        assertEquals(0, player.activeConcoctions.active.size)
    }
    
    @Test
    fun testExperimentationSuccess() {
        // Find an experimentation recipe
        val experimentRecipe = recipeLibrary.getAllRecipes()
            .first { it.discoveryMethod == DiscoveryMethod.EXPERIMENTATION }
        
        // Add at least 1 of each required ingredient
        var inventory = gameStateManager.playerState.value.ingredientInventory
        experimentRecipe.requiredIngredients.keys.forEach { ingredientId ->
            inventory = inventory.addIngredient(ingredientId, 1)
        }
        gameStateManager.updatePlayer { it.copy(ingredientInventory = inventory) }
        
        // Experiment with the ingredient IDs (just the keys, not quantities)
        val ingredientIds = experimentRecipe.requiredIngredients.keys.toList()
        val result = concoctionCrafter.experimentCraft(ingredientIds)
        
        // Result should be success (discovering the recipe)
        assertTrue(result is ExperimentResult.Success, "Should discover recipe with matching ingredient set")
        
        val player = gameStateManager.playerState.value
        assertTrue(player.recipeBook.hasRecipe(experimentRecipe.id))
        assertTrue(player.choiceLog.entries.any { it.tag.value.startsWith("concoctions_experiment_success") })
    }
    
    @Test
    fun testExperimentationFailure() {
        // Add random ingredients that we know don't form a recipe together
        // Use ingredients that aren't likely to match any experimentation recipe
        var inventory = gameStateManager.playerState.value.ingredientInventory
        inventory = inventory.addIngredient(IngredientId("wildflower"), 1)
        gameStateManager.updatePlayer { it.copy(ingredientInventory = inventory) }
        
        val result = concoctionCrafter.experimentCraft(
            listOf(IngredientId("wildflower"))  // Single ingredient unlikely to match
        )
        
        // Should fail since single wildflower doesn't match any recipe
        assertTrue(result is ExperimentResult.Failure,
            "Should fail with non-matching ingredient combination")
        
        val player = gameStateManager.playerState.value
        assertTrue(player.choiceLog.entries.any { it.tag.value == "concoctions_experiment_failure" })
        
        // Ingredient should be consumed
        assertEquals(0, player.ingredientInventory.getQuantity(IngredientId("wildflower")))
    }
    
    @Test
    fun testExperimentationCooldown() {
        // Add ingredients
        var inventory = gameStateManager.playerState.value.ingredientInventory
        inventory = inventory.addIngredient(IngredientId("wildflower"), 2)
        gameStateManager.updatePlayer { it.copy(ingredientInventory = inventory) }
        
        // First experiment (will fail but will set cooldown)
        concoctionCrafter.experimentCraft(listOf(IngredientId("wildflower")))
        
        // Add more ingredients
        inventory = gameStateManager.playerState.value.ingredientInventory
        inventory = inventory.addIngredient(IngredientId("wildflower"), 1)
        gameStateManager.updatePlayer { it.copy(ingredientInventory = inventory) }
        
        // Try to experiment again immediately
        val result = concoctionCrafter.experimentCraft(listOf(IngredientId("wildflower")))
        
        assertTrue(result is ExperimentResult.OnCooldown, 
            "Should be on cooldown after first experiment")
        
        // Advance time past cooldown (30 minutes)
        currentTime += 31 * 60 * 1000L
        
        val result2 = concoctionCrafter.experimentCraft(listOf(IngredientId("wildflower")))
        
        assertFalse(result2 is ExperimentResult.OnCooldown, 
            "Should not be on cooldown after 30 minutes")
    }
    
    @Test
    fun testActiveEffectCalculation() {
        // Add concoction with multiple effects
        val concoction = ActiveConcoction(
            template = ConcoctionTemplate(
                id = ConcoctionId("multi_effect"),
                nameKey = "test",
                descriptionKey = "test",
                effects = listOf(
                    ConcoctionEffect(EffectType.HEALTH_REGEN, magnitude = 5, isPositive = true),
                    ConcoctionEffect(EffectType.SEED_BOOST, magnitude = 25, isPositive = true)
                ),
                durationSeconds = 300,
                stackLimit = 3
            ),
            appliedAt = currentTime,
            expiresAt = currentTime + 300_000,
            stacks = 2 // Stacked twice
        )
        
        gameStateManager.updatePlayer { it.copy(
            activeConcoctions = ActiveConcoctions(listOf(concoction))
        )}
        
        concoctionCrafter.refreshViewState()
        val viewState = concoctionCrafter.viewState.value
        
        assertEquals(2, viewState.activeEffects.size)
        
        // Effects should be scaled by stack count
        val healthRegen = viewState.activeEffects.find { it.type == EffectType.HEALTH_REGEN }
        assertNotNull(healthRegen)
        assertEquals(10, healthRegen.magnitude) // 5 * 2 stacks
        
        val seedBoost = viewState.activeEffects.find { it.type == EffectType.SEED_BOOST }
        assertNotNull(seedBoost)
        assertEquals(50, seedBoost.magnitude) // 25 * 2 stacks
    }
    
    @Test
    fun testIngredientInventoryOperations() {
        val ingredientId = IngredientId("wildflower")
        var inventory = IngredientInventory()
        
        // Add
        inventory = inventory.addIngredient(ingredientId, 5)
        assertEquals(5, inventory.getQuantity(ingredientId))
        assertTrue(inventory.hasIngredient(ingredientId, 5))
        
        // Remove
        inventory = inventory.removeIngredient(ingredientId, 2)
        assertEquals(3, inventory.getQuantity(ingredientId))
        
        // Remove all
        inventory = inventory.removeIngredient(ingredientId, 3)
        assertEquals(0, inventory.getQuantity(ingredientId))
        assertFalse(inventory.hasIngredient(ingredientId))
    }
    
    @Test
    fun testRecipeBookOperations() {
        var recipeBook = RecipeBook()
        val recipeId = RecipeId("test_recipe")
        
        assertFalse(recipeBook.hasRecipe(recipeId))
        
        recipeBook = recipeBook.discoverRecipe(recipeId)
        assertTrue(recipeBook.hasRecipe(recipeId))
        assertTrue(recipeBook.discoveredRecipes.contains(recipeId))
    }
    
    @Test
    fun testViewStateRefresh() {
        // Discover some recipes
        concoctionCrafter.discoverRecipe(RecipeId("minor_health_potion"))
        concoctionCrafter.discoverRecipe(RecipeId("fortune_brew"))
        
        // Add some ingredients
        var inventory = gameStateManager.playerState.value.ingredientInventory
        inventory = inventory.addIngredient(IngredientId("wildflower"), 10)
        inventory = inventory.addIngredient(IngredientId("river_moss"), 5)
        gameStateManager.updatePlayer { it.copy(ingredientInventory = inventory) }
        
        concoctionCrafter.refreshViewState()
        val viewState = concoctionCrafter.viewState.value
        
        assertEquals(2, viewState.discoveredRecipes.size)
        assertTrue(viewState.craftableRecipes.isNotEmpty(), "Should have at least one craftable recipe")
    }
    
    @Test
    fun testHarvestFromInvalidLocation() {
        val result = concoctionCrafter.harvestAtLocation("nonexistent_location")
        
        assertFalse(result.success)
        assertEquals(0, result.harvested.size)
    }
    
    @Test
    fun testCraftInvalidRecipe() {
        val result = concoctionCrafter.craftConcoction(RecipeId("nonexistent"))
        
        assertTrue(result is CraftResult.RecipeNotFound)
    }
}
