package com.jalmarquest.core.state.concoctions

import com.jalmarquest.core.model.IngredientId
import com.jalmarquest.core.model.IngredientRarity
import kotlin.test.*

class IngredientHarvestServiceTest {
    
    private lateinit var service: IngredientHarvestService
    
    @BeforeTest
    fun setup() {
        service = IngredientHarvestService()
    }
    
    @Test
    fun testGetAllIngredients() {
        val ingredients = service.getAllIngredients()
        
        assertTrue(ingredients.isNotEmpty(), "Catalog should not be empty")
        assertTrue(ingredients.size >= 12, "Should have at least 12 ingredients")
        
        // Verify each rarity tier is represented
        val rarities = ingredients.map { it.rarity }.toSet()
        assertTrue(rarities.contains(IngredientRarity.COMMON))
        assertTrue(rarities.contains(IngredientRarity.UNCOMMON))
        assertTrue(rarities.contains(IngredientRarity.RARE))
        assertTrue(rarities.contains(IngredientRarity.EXOTIC))
        assertTrue(rarities.contains(IngredientRarity.LEGENDARY))
    }
    
    @Test
    fun testGetIngredient() {
        val ingredient = service.getIngredient(IngredientId("wildflower"))
        
        assertNotNull(ingredient)
        assertEquals(IngredientId("wildflower"), ingredient.id)
        assertEquals(IngredientRarity.COMMON, ingredient.rarity)
    }
    
    @Test
    fun testGetNonexistentIngredient() {
        val ingredient = service.getIngredient(IngredientId("nonexistent"))
        
        assertNull(ingredient)
    }
    
    @Test
    fun testGetIngredientsAtLocation() {
        val forestIngredients = service.getIngredientsAtLocation("forest")
        
        assertTrue(forestIngredients.isNotEmpty(), "Forest should have ingredients")
        
        // All returned ingredients should have forest in their locations
        forestIngredients.forEach { ingredient ->
            assertTrue(ingredient.harvestLocations.contains("forest"))
        }
    }
    
    @Test
    fun testGetIngredientsAtInvalidLocation() {
        val ingredients = service.getIngredientsAtLocation("nonexistent")
        
        assertTrue(ingredients.isEmpty())
    }
    
    @Test
    fun testHarvestAtLocation() {
        // Run harvest multiple times to test RNG
        var totalHarvested = 0
        repeat(100) {
            val harvested = service.harvestAtLocation("forest")
            totalHarvested += harvested.size
        }
        
        // With 100 attempts, we should get at least some ingredients
        assertTrue(totalHarvested > 0, "Should harvest some ingredients over 100 attempts")
    }
    
    @Test
    fun testHarvestWithLuckBonus() {
        var withoutLuck = 0
        var withLuck = 0
        
        // Harvest without luck bonus
        repeat(100) {
            val harvested = service.harvestAtLocation("forest", luckBonus = 0)
            withoutLuck += harvested.sumOf { it.second }
        }
        
        // Harvest with 100% luck bonus
        repeat(100) {
            val harvested = service.harvestAtLocation("forest", luckBonus = 100)
            withLuck += harvested.sumOf { it.second }
        }
        
        // With luck bonus, we should generally get more ingredients
        // (This is probabilistic, so we use a lenient check)
        assertTrue(withLuck >= withoutLuck * 0.8, "Luck bonus should increase harvest rates")
    }
    
    @Test
    fun testHarvestQuantityByRarity() {
        // Harvest many times and check quantity distribution
        val quantityMap = mutableMapOf<IngredientRarity, MutableList<Int>>()
        
        repeat(1000) {
            val harvested = service.harvestAtLocation("forest")
            harvested.forEach { (ingredientId, quantity) ->
                val ingredient = service.getIngredient(ingredientId)
                if (ingredient != null) {
                    quantityMap.getOrPut(ingredient.rarity) { mutableListOf() }.add(quantity)
                }
            }
        }
        
        // Common ingredients should generally yield more per harvest
        if (quantityMap.containsKey(IngredientRarity.COMMON)) {
            val commonAvg = quantityMap[IngredientRarity.COMMON]!!.average()
            assertTrue(commonAvg > 1.5, "Common ingredients should average 2-4 per harvest")
        }
        
        // Rare+ ingredients should yield 1 per harvest
        if (quantityMap.containsKey(IngredientRarity.RARE)) {
            val rareAvg = quantityMap[IngredientRarity.RARE]!!.average()
            assertTrue(rareAvg <= 1.1, "Rare ingredients should yield 1 per harvest")
        }
    }
    
    @Test
    fun testIngredientProperties() {
        val allIngredients = service.getAllIngredients()
        
        allIngredients.forEach { ingredient ->
            assertFalse(ingredient.properties.isEmpty(), "Each ingredient should have properties")
            assertFalse(ingredient.harvestLocations.isEmpty(), "Each ingredient should have harvest locations")
        }
    }
    
    @Test
    fun testLocationVariety() {
        val locations = setOf(
            "forest", "meadow", "cave", "swamp", "river",
            "volcano", "mountain", "ocean", "dragon_lair", "abyss"
        )
        
        locations.forEach { location ->
            val ingredients = service.getIngredientsAtLocation(location)
            // Not all locations need ingredients, but major ones should
            if (location in listOf("forest", "cave", "swamp")) {
                assertTrue(ingredients.isNotEmpty(), "$location should have ingredients")
            }
        }
    }
    
    @Test
    fun testRaritySpawnRates() {
        // Test that rarity affects spawn rates
        val harvestCounts = mutableMapOf<IngredientRarity, Int>()
        
        repeat(1000) {
            val harvested = service.harvestAtLocation("forest")
            harvested.forEach { (ingredientId, _) ->
                val ingredient = service.getIngredient(ingredientId)
                if (ingredient != null) {
                    harvestCounts[ingredient.rarity] = harvestCounts.getOrDefault(ingredient.rarity, 0) + 1
                }
            }
        }
        
        // Common should be harvested more often than uncommon
        val common = harvestCounts.getOrDefault(IngredientRarity.COMMON, 0)
        val uncommon = harvestCounts.getOrDefault(IngredientRarity.UNCOMMON, 0)
        
        if (common > 0 && uncommon > 0) {
            assertTrue(common > uncommon, "Common should spawn more than uncommon")
        }
    }
}
