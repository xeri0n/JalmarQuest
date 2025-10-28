package com.jalmarquest.core.state.narrative

import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.model.PlayerSettings
import com.jalmarquest.core.model.QuestLog
import com.jalmarquest.core.model.StatusEffects
import com.jalmarquest.core.state.GameStateManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DialogueVariantManagerTest {
    
    @Test
    fun testFilteredDialogueWhenNoFilterModeDisabled() {
        val gameStateManager = createGameStateManager(noFilterEnabled = false)
        val contentFilterManager = ContentFilterManager(gameStateManager)
        val dialogueManager = DialogueVariantManager(contentFilterManager)
        
        val dialogue = dialogueManager.getDialogue("npc_borken", DialogueType.GREETING)
        
        assertNotNull(dialogue)
        assertTrue(dialogue.contains("adventurer"), "Expected filtered dialogue to be family-friendly")
        assertTrue(!dialogue.contains("optimist") || dialogue.contains("Welcome"), "Expected filtered greeting")
    }
    
    @Test
    fun testUnfilteredDialogueWhenNoFilterModeEnabled() {
        val gameStateManager = createGameStateManager(noFilterEnabled = true)
        val contentFilterManager = ContentFilterManager(gameStateManager)
        val dialogueManager = DialogueVariantManager(contentFilterManager)
        
        val dialogue = dialogueManager.getDialogue("npc_borken", DialogueType.GREETING)
        
        assertNotNull(dialogue)
        assertTrue(dialogue.contains("optimist") || dialogue.contains("coping"), "Expected unfiltered dialogue to be edgier")
    }
    
    @Test
    fun testPackRatDialogueVariants() {
        val gameStateManager = createGameStateManager(noFilterEnabled = false)
        val contentFilterManager = ContentFilterManager(gameStateManager)
        val dialogueManager = DialogueVariantManager(contentFilterManager)
        
        val filteredShop = dialogueManager.getDialogue("npc_pack_rat", DialogueType.SHOP_GREETING)
        assertNotNull(filteredShop)
        assertTrue(filteredShop.contains("treasures") || filteredShop.contains("story"))
        
        // Now enable No Filter Mode
        gameStateManager.updatePlayerSettings { it.copy(isNoFilterModeEnabled = true) }
        
        val unfilteredShop = dialogueManager.getDialogue("npc_pack_rat", DialogueType.SHOP_GREETING)
        assertNotNull(unfilteredShop)
        assertTrue(unfilteredShop.contains("overpriced") || unfilteredShop.contains("victim"))
    }
    
    @Test
    fun testWorriedWickerDialogueVariants() {
        val gameStateManager = createGameStateManager(noFilterEnabled = false)
        val contentFilterManager = ContentFilterManager(gameStateManager)
        val dialogueManager = DialogueVariantManager(contentFilterManager)
        
        val filtered = dialogueManager.getDialogue("npc_worried_wicker", DialogueType.RANDOM_1)
        assertNotNull(filtered)
        
        // Enable No Filter Mode
        gameStateManager.updatePlayerSettings { it.copy(isNoFilterModeEnabled = true) }
        
        val unfiltered = dialogueManager.getDialogue("npc_worried_wicker", DialogueType.RANDOM_1)
        assertNotNull(unfiltered)
        assertTrue(filtered != unfiltered, "Filtered and unfiltered dialogue should differ")
    }
    
    @Test
    fun testHasVariantsCheck() {
        val gameStateManager = createGameStateManager(noFilterEnabled = false)
        val contentFilterManager = ContentFilterManager(gameStateManager)
        val dialogueManager = DialogueVariantManager(contentFilterManager)
        
        assertTrue(dialogueManager.hasVariants("npc_borken"))
        assertTrue(dialogueManager.hasVariants("npc_pack_rat"))
        assertTrue(dialogueManager.hasVariants("npc_worried_wicker"))
        assertTrue(!dialogueManager.hasVariants("npc_nonexistent"))
    }
    
    @Test
    fun testMultipleDialogueTypes() {
        val gameStateManager = createGameStateManager(noFilterEnabled = false)
        val contentFilterManager = ContentFilterManager(gameStateManager)
        val dialogueManager = DialogueVariantManager(contentFilterManager)
        
        val greeting = dialogueManager.getDialogue("npc_borken", DialogueType.GREETING)
        val farewell = dialogueManager.getDialogue("npc_borken", DialogueType.FAREWELL)
        val random1 = dialogueManager.getDialogue("npc_borken", DialogueType.RANDOM_1)
        
        assertNotNull(greeting)
        assertNotNull(farewell)
        assertNotNull(random1)
        
        // All should be different
        assertTrue(greeting != farewell)
        assertTrue(greeting != random1)
        assertTrue(farewell != random1)
    }
    
    @Test
    fun testCoffeeDialogueVariants_coffeeGratitude() {
        val gameStateManager = createGameStateManager(noFilterEnabled = false, hasCoffee = true)
        val contentFilterManager = ContentFilterManager(gameStateManager)
        val dialogueManager = DialogueVariantManager(contentFilterManager)
        
        val dialogue = dialogueManager.getDialogue("npc_exhausted_coder", DialogueType.COFFEE_GRATITUDE)
        
        assertNotNull(dialogue)
        assertTrue(dialogue!!.contains("coffee") || dialogue.contains("donation") || dialogue.contains("support"))
    }
    
    @Test
    fun testCoffeeDialogueVariants_coffeeEnergized() {
        val gameStateManager = createGameStateManager(noFilterEnabled = false, hasCoffee = true)
        val contentFilterManager = ContentFilterManager(gameStateManager)
        val dialogueManager = DialogueVariantManager(contentFilterManager)
        
        val dialogue = dialogueManager.getDialogue("npc_exhausted_coder", DialogueType.COFFEE_ENERGIZED)
        
        assertNotNull(dialogue)
        assertTrue(dialogue!!.contains("caffeine") || dialogue.contains("energy") || dialogue.contains("awake"))
    }
    
    @Test
    fun testCoffeeDialogueVariants_randomCoffee() {
        val gameStateManager = createGameStateManager(noFilterEnabled = false, hasCoffee = true)
        val contentFilterManager = ContentFilterManager(gameStateManager)
        val dialogueManager = DialogueVariantManager(contentFilterManager)
        
        val random1 = dialogueManager.getDialogue("npc_exhausted_coder", DialogueType.RANDOM_COFFEE_1)
        val random2 = dialogueManager.getDialogue("npc_exhausted_coder", DialogueType.RANDOM_COFFEE_2)
        val random3 = dialogueManager.getDialogue("npc_exhausted_coder", DialogueType.RANDOM_COFFEE_3)
        
        assertNotNull(random1)
        assertNotNull(random2)
        assertNotNull(random3)
        
        // All should be different
        assertTrue(random1 != random2)
        assertTrue(random1 != random3)
        assertTrue(random2 != random3)
    }
    
    @Test
    fun testCoffeeDialogueVariants_requiresCoffeePurchase() {
        // Without coffee purchase, coffee dialogue types should return null or fallback
        val gameStateManagerNoCoffee = createGameStateManager(noFilterEnabled = false, hasCoffee = false)
        val contentFilterManagerNoCoffee = ContentFilterManager(gameStateManagerNoCoffee)
        val dialogueManagerNoCoffee = DialogueVariantManager(contentFilterManagerNoCoffee)
        
        val dialogueNoCoffee = dialogueManagerNoCoffee.getDialogue("npc_exhausted_coder", DialogueType.COFFEE_GRATITUDE)
        
        // Should not get coffee-specific dialogue without purchase
        // (Implementation may return null or fallback to regular dialogue)
        if (dialogueNoCoffee != null) {
            // If fallback exists, should be a regular dialogue variant
            assertFalse(dialogueNoCoffee.contains("donation") && dialogueNoCoffee.contains("coffee"))
        }
    }

    private fun createGameStateManager(noFilterEnabled: Boolean, hasCoffee: Boolean = false): GameStateManager {
        val player = Player(
            id = "test_player",
            name = "Test Player",
            choiceLog = ChoiceLog(emptyList()),
            questLog = QuestLog(),
            statusEffects = StatusEffects(emptyList()),
            playerSettings = PlayerSettings(
                isNoFilterModeEnabled = noFilterEnabled,
                hasPurchasedCreatorCoffee = hasCoffee
            )
        )
        
        return GameStateManager(
            initialPlayer = player,
            timestampProvider = { 0L }
        )
    }
}
