package com.jalmarquest.core.state

import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.Inventory
import com.jalmarquest.core.model.ItemId
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.model.QuestLog
import com.jalmarquest.core.model.StatusEffect
import com.jalmarquest.core.model.StatusEffects
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GameStateManagerTest {
    private val basePlayer = Player(
        id = "player-test",
        name = "Jalmar",
        choiceLog = ChoiceLog(emptyList()),
        questLog = QuestLog(),
        statusEffects = StatusEffects(emptyList())
    )

    @Test
    fun appendChoiceAddsEntry() {
        var now = 100L
        val manager = GameStateManager(basePlayer) { now }
        manager.appendChoice("test_choice")

        val result = manager.playerState.value.choiceLog.entries
        assertEquals(1, result.size)
        assertEquals("test_choice", result.first().tag.value)
    }

    @Test
    fun grantItemAddsToInventory() {
        val manager = GameStateManager(basePlayer) { 0L }

        manager.grantItem("twig", 2)

        val total = manager.playerState.value.inventory.totalQuantity(ItemId("twig"))
        assertEquals(2, total)
    }

    @Test
    fun consumeItemRemovesWhenSufficient() {
        val playerWithItems = basePlayer.copy(
            inventory = Inventory(listOf(com.jalmarquest.core.model.ItemStack(ItemId("seed"), 5)))
        )
        val manager = GameStateManager(playerWithItems) { 0L }

        val consumed = manager.consumeItem("seed", 3)

        assertTrue(consumed)
        assertEquals(2, manager.playerState.value.inventory.totalQuantity(ItemId("seed")))
    }

    @Test
    fun consumeItemFailsWhenInsufficient() {
        val playerWithItems = basePlayer.copy(
            inventory = Inventory(listOf(com.jalmarquest.core.model.ItemStack(ItemId("seed"), 2)))
        )
        val manager = GameStateManager(playerWithItems) { 0L }

        val consumed = manager.consumeItem("seed", 3)

        assertFalse(consumed)
        assertEquals(2, manager.playerState.value.inventory.totalQuantity(ItemId("seed")))
    }

    @Test
    fun grantItemRejectsNegativeQuantity() {
        val manager = GameStateManager(basePlayer) { 0L }

        assertFailsWith<IllegalArgumentException> {
            manager.grantItem("twig", -1)
        }
    }

    @Test
    fun updateInventoryAppliesTransformation() {
        val manager = GameStateManager(basePlayer) { 0L }

        manager.updateInventory { inventory ->
            inventory.add(com.jalmarquest.core.model.ItemStack(ItemId("dust"), 1))
        }

        assertEquals(1, manager.playerState.value.inventory.totalQuantity(ItemId("dust")))
    }

    @Test
    fun clearStatusRemovesEffect() {
        val playerWithStatus = basePlayer.copy(
            statusEffects = StatusEffects(listOf(StatusEffect("damp_plummage", expiresAtMillis = 200L)))
        )
        val manager = GameStateManager(playerWithStatus) { 0L }

        manager.clearStatus("damp_plummage")

        assertTrue(manager.playerState.value.statusEffects.entries.isEmpty())
    }
    
    // Alpha 2.2 Phase 7: Creator Coffee Rewards Tests
    
    @Test
    fun grantCreatorCoffeeRewards_grantsShinyAndAffinity() = kotlinx.coroutines.test.runTest {
        val playerWithCoffee = basePlayer.copy(
            playerSettings = com.jalmarquest.core.model.PlayerSettings(hasPurchasedCreatorCoffee = true)
        )
        val manager = GameStateManager(playerWithCoffee) { 0L }
        val valuationService = com.jalmarquest.core.state.hoard.ShinyValuationService()
        val leaderboardService = com.jalmarquest.core.state.hoard.LeaderboardService()
        val hoardManager = com.jalmarquest.core.state.hoard.HoardRankManager(
            gameStateManager = manager,
            valuationService = valuationService,
            leaderboardService = leaderboardService,
            timestampProvider = { 0L }
        )
        val npcManager = com.jalmarquest.core.state.npc.NpcRelationshipManager(
            timestampProvider = { 0L }
        )
        
        val result = manager.grantCreatorCoffeeRewards(hoardManager, npcManager)
        
        assertTrue(result, "Should return true when rewards granted")
        assertTrue(
            manager.playerState.value.playerSettings.hasReceivedCoffeeRewards,
            "hasReceivedCoffeeRewards flag should be set"
        )
        
        // Verify shiny was granted
        val shinies = hoardManager.viewState.value.collection.ownedShinies
        assertTrue(
            shinies.any { it.id.value == "golden_coffee_bean" },
            "Golden Coffee Bean shiny should be granted"
        )
        
        // Verify affinity was added
        val affinity = npcManager.getAffinity("npc_exhausted_coder")
        assertEquals(50, affinity, "Should have +50 affinity with Exhausted Coder")
        
        // Verify choice tag was logged
        val choiceTags = manager.playerState.value.choiceLog.entries.map { it.tag.value }
        assertTrue(
            choiceTags.contains("coffee_rewards_granted"),
            "Should log coffee_rewards_granted choice tag"
        )
    }
    
    @Test
    fun grantCreatorCoffeeRewards_failsIfNotPurchased() = kotlinx.coroutines.test.runTest {
        val manager = GameStateManager(basePlayer) { 0L }
        val valuationService = com.jalmarquest.core.state.hoard.ShinyValuationService()
        val leaderboardService = com.jalmarquest.core.state.hoard.LeaderboardService()
        val hoardManager = com.jalmarquest.core.state.hoard.HoardRankManager(
            gameStateManager = manager,
            valuationService = valuationService,
            leaderboardService = leaderboardService,
            timestampProvider = { 0L }
        )
        
        val result = manager.grantCreatorCoffeeRewards(hoardManager)
        
        assertFalse(result, "Should return false when coffee not purchased")
        assertFalse(
            manager.playerState.value.playerSettings.hasReceivedCoffeeRewards,
            "hasReceivedCoffeeRewards flag should remain false"
        )
    }
    
    @Test
    fun grantCreatorCoffeeRewards_preventsMultipleGrants() = kotlinx.coroutines.test.runTest {
        val playerWithRewards = basePlayer.copy(
            playerSettings = com.jalmarquest.core.model.PlayerSettings(
                hasPurchasedCreatorCoffee = true,
                hasReceivedCoffeeRewards = true
            )
        )
        val manager = GameStateManager(playerWithRewards) { 0L }
        val valuationService = com.jalmarquest.core.state.hoard.ShinyValuationService()
        val leaderboardService = com.jalmarquest.core.state.hoard.LeaderboardService()
        val hoardManager = com.jalmarquest.core.state.hoard.HoardRankManager(
            gameStateManager = manager,
            valuationService = valuationService,
            leaderboardService = leaderboardService,
            timestampProvider = { 0L }
        )
        val npcManager = com.jalmarquest.core.state.npc.NpcRelationshipManager(
            timestampProvider = { 0L }
        )
        
        val initialAffinity = npcManager.getAffinity("npc_exhausted_coder")
        val result = manager.grantCreatorCoffeeRewards(hoardManager, npcManager)
        
        assertFalse(result, "Should return false when rewards already granted")
        
        // Verify no duplicate affinity
        val finalAffinity = npcManager.getAffinity("npc_exhausted_coder")
        assertEquals(initialAffinity, finalAffinity, "Affinity should not change on duplicate grant")
    }
}
