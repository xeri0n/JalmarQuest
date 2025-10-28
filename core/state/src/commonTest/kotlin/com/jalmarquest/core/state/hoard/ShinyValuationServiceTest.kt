package com.jalmarquest.core.state.hoard

import com.jalmarquest.core.model.ShinyId
import com.jalmarquest.core.model.ShinyRarity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ShinyValuationServiceTest {
    
    private val service = ShinyValuationService()
    
    @Test
    fun getShiny_goldenCoffeeBean_exists() {
        val shiny = service.getShiny(ShinyId("golden_coffee_bean"))
        
        assertNotNull(shiny)
        assertEquals("golden_coffee_bean", shiny.id.value)
    }
    
    @Test
    fun getShiny_goldenCoffeeBean_hasCorrectRarity() {
        val shiny = service.getShiny(ShinyId("golden_coffee_bean"))
        
        assertNotNull(shiny)
        assertEquals(ShinyRarity.LEGENDARY, shiny.rarity)
    }
    
    @Test
    fun getShiny_goldenCoffeeBean_hasCorrectBaseValue() {
        val shiny = service.getShiny(ShinyId("golden_coffee_bean"))
        
        assertNotNull(shiny)
        assertEquals(5000, shiny.baseValue)
    }
    
    @Test
    fun getShiny_goldenCoffeeBean_hasLocalizationKeys() {
        val shiny = service.getShiny(ShinyId("golden_coffee_bean"))
        
        assertNotNull(shiny)
        assertEquals("shiny_golden_coffee_bean_name", shiny.nameKey)
        assertEquals("shiny_golden_coffee_bean_desc", shiny.descriptionKey)
    }
    
    @Test
    fun getAllShinies_includesGoldenCoffeeBean() {
        val allShinies = service.getAllShinies()
        
        assertTrue(allShinies.any { it.id.value == "golden_coffee_bean" })
    }
    
    @Test
    fun getAllShinies_returns14Shinies() {
        // Verify the catalog has exactly 14 shinies (13 original + 1 Golden Coffee Bean)
        val allShinies = service.getAllShinies()
        
        assertEquals(14, allShinies.size)
    }
    
    @Test
    fun getShiny_returnsNullForNonexistentShiny() {
        val shiny = service.getShiny(ShinyId("nonexistent_shiny"))
        
        assertEquals(null, shiny)
    }
}
