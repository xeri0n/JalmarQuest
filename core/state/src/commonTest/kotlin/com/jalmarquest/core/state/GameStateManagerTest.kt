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
}
