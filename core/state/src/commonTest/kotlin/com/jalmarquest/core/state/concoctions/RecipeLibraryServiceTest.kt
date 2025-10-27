package com.jalmarquest.core.state.concoctions

import com.jalmarquest.core.model.*
import kotlin.test.*

class RecipeLibraryServiceTest {
    
    private lateinit var service: RecipeLibraryService
    
    @BeforeTest
    fun setup() {
        service = RecipeLibraryService()
    }
    
    @Test
    fun testGetAllRecipes() {
        val recipes = service.getAllRecipes()
        
        assertTrue(recipes.isNotEmpty(), "Recipe catalog should not be empty")
        assertTrue(recipes.size >= 8, "Should have at least 8 recipes")
    }
    
    @Test
    fun testGetRecipe() {
        val recipe = service.getRecipe(RecipeId("minor_health_potion"))
        
        assertNotNull(recipe)
        assertEquals(RecipeId("minor_health_potion"), recipe.id)
        assertEquals(DiscoveryMethod.MILESTONE, recipe.discoveryMethod)
    }
    
    @Test
    fun testGetNonexistentRecipe() {
        val recipe = service.getRecipe(RecipeId("nonexistent"))
        
        assertNull(recipe)
    }
    
    @Test
    fun testGetDiscoveredRecipes() {
        var recipeBook = RecipeBook()
        recipeBook = recipeBook.discoverRecipe(RecipeId("minor_health_potion"))
        recipeBook = recipeBook.discoverRecipe(RecipeId("fortune_brew"))
        
        val discovered = service.getDiscoveredRecipes(recipeBook)
        
        assertEquals(2, discovered.size)
        assertTrue(discovered.any { it.id == RecipeId("minor_health_potion") })
        assertTrue(discovered.any { it.id == RecipeId("fortune_brew") })
    }
    
    @Test
    fun testGetMilestoneRecipes() {
        val milestoneRecipes = service.getMilestoneRecipes()
        
        assertTrue(milestoneRecipes.isNotEmpty(), "Should have milestone recipes")
        assertTrue(milestoneRecipes.all { it.discoveryMethod == DiscoveryMethod.MILESTONE })
    }
    
    @Test
    fun testCanCraftWithRecipeAndIngredients() {
        val recipe = service.getRecipe(RecipeId("minor_health_potion"))!!
        
        var recipeBook = RecipeBook()
        recipeBook = recipeBook.discoverRecipe(RecipeId("minor_health_potion"))
        
        var inventory = IngredientInventory()
        recipe.requiredIngredients.forEach { (ingredientId, quantity) ->
            inventory = inventory.addIngredient(ingredientId, quantity)
        }
        
        val canCraft = service.canCraft(recipe, inventory, recipeBook)
        
        assertTrue(canCraft)
    }
    
    @Test
    fun testCannotCraftWithoutRecipe() {
        val recipe = service.getRecipe(RecipeId("minor_health_potion"))!!
        val recipeBook = RecipeBook() // Empty, no recipes discovered
        
        var inventory = IngredientInventory()
        recipe.requiredIngredients.forEach { (ingredientId, quantity) ->
            inventory = inventory.addIngredient(ingredientId, quantity)
        }
        
        val canCraft = service.canCraft(recipe, inventory, recipeBook)
        
        assertFalse(canCraft, "Should not be able to craft without discovering recipe")
    }
    
    @Test
    fun testCannotCraftWithoutIngredients() {
        val recipe = service.getRecipe(RecipeId("minor_health_potion"))!!
        
        var recipeBook = RecipeBook()
        recipeBook = recipeBook.discoverRecipe(RecipeId("minor_health_potion"))
        
        val inventory = IngredientInventory() // Empty inventory
        
        val canCraft = service.canCraft(recipe, inventory, recipeBook)
        
        assertFalse(canCraft, "Should not be able to craft without ingredients")
    }
    
    @Test
    fun testCannotCraftWithPartialIngredients() {
        val recipe = service.getRecipe(RecipeId("minor_health_potion"))!!
        
        var recipeBook = RecipeBook()
        recipeBook = recipeBook.discoverRecipe(RecipeId("minor_health_potion"))
        
        var inventory = IngredientInventory()
        // Add only partial ingredients
        val firstIngredient = recipe.requiredIngredients.entries.first()
        inventory = inventory.addIngredient(firstIngredient.key, firstIngredient.value - 1)
        
        val canCraft = service.canCraft(recipe, inventory, recipeBook)
        
        assertFalse(canCraft, "Should not be able to craft with insufficient ingredients")
    }
    
    @Test
    fun testRecipeHasEffects() {
        val recipes = service.getAllRecipes()
        
        recipes.forEach { recipe ->
            assertTrue(recipe.resultingConcoction.effects.isNotEmpty(), "Each recipe should have effects")
            assertTrue(recipe.resultingConcoction.durationSeconds > 0, "Duration should be positive")
            assertTrue(recipe.resultingConcoction.stackLimit > 0, "Stack limit should be positive")
        }
    }
    
    @Test
    fun testRecipeHasIngredients() {
        val recipes = service.getAllRecipes()
        
        recipes.forEach { recipe ->
            assertTrue(recipe.requiredIngredients.isNotEmpty(), "Each recipe should require ingredients")
            recipe.requiredIngredients.forEach { (_, quantity) ->
                assertTrue(quantity > 0, "Ingredient quantities should be positive")
            }
        }
    }
    
    @Test
    fun testDiscoveryMethodVariety() {
        val recipes = service.getAllRecipes()
        val methods = recipes.map { it.discoveryMethod }.toSet()
        
        // Should have multiple discovery methods
        assertTrue(methods.size > 1, "Should have variety in discovery methods")
        assertTrue(methods.contains(DiscoveryMethod.MILESTONE))
        assertTrue(methods.contains(DiscoveryMethod.EXPERIMENTATION))
    }
    
    @Test
    fun testRecipeEffectTypes() {
        val recipes = service.getAllRecipes()
        val effectTypes = recipes.flatMap { recipe ->
            recipe.resultingConcoction.effects.map { it.type }
        }.toSet()
        
        // Should have variety in effect types
        assertTrue(effectTypes.size > 3, "Should have variety in effect types")
    }
    
    @Test
    fun testRecipeStackLimits() {
        val recipes = service.getAllRecipes()
        
        recipes.forEach { recipe ->
            assertTrue(recipe.resultingConcoction.stackLimit in 1..5, 
                "Stack limit should be reasonable (1-5)")
        }
    }
    
    @Test
    fun testRecipeDurations() {
        val recipes = service.getAllRecipes()
        
        recipes.forEach { recipe ->
            assertTrue(recipe.resultingConcoction.durationSeconds >= 60, 
                "Duration should be at least 1 minute")
            assertTrue(recipe.resultingConcoction.durationSeconds <= 3600, 
                "Duration should be at most 1 hour")
        }
    }
}
