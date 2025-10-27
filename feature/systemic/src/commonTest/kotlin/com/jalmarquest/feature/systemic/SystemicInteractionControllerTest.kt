package com.jalmarquest.feature.systemic

import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.EnvironmentTag
import com.jalmarquest.core.model.InteractionId
import com.jalmarquest.core.model.Inventory
import com.jalmarquest.core.model.ItemId
import com.jalmarquest.core.model.ItemStack
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.model.QuestLog
import com.jalmarquest.core.model.StatusEffects
import com.jalmarquest.core.state.GameStateManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SystemicInteractionControllerTest {
    private fun basePlayerWithInventory(stacks: List<ItemStack>): Player = Player(
        id = "jalmar",
        name = "Jalmar",
        choiceLog = ChoiceLog(emptyList()),
        questLog = QuestLog(),
        statusEffects = StatusEffects(emptyList()),
        inventory = Inventory(stacks)
    )

    @Test
    fun attemptInteractionAppliesEffects() = runTest {
        val player = basePlayerWithInventory(
            listOf(
                ItemStack(ItemId("twig"), 2),
                ItemStack(ItemId("acorn_cap"), 1)
            )
        )
        val manager = GameStateManager(player) { 0L }
        val controller = controller(this, manager)

        controller.updateEnvironmentTags(setOf("workbench"))
        advanceUntilIdle()

        controller.attemptInteraction("craft_twig_spear")
        advanceUntilIdle()

        val inventory = manager.playerState.value.inventory
        assertEquals(0, inventory.totalQuantity(ItemId("acorn_cap")))
        assertEquals(0, inventory.totalQuantity(ItemId("twig")))
        assertEquals(1, inventory.totalQuantity(ItemId("twig_spear")))
        val event = controller.state.value.lastEvent
        assertTrue(event?.success == true)
    }

    @Test
    fun attemptInteractionFailsWhenRequirementsShift() = runTest {
        val player = basePlayerWithInventory(
            listOf(
                ItemStack(ItemId("twig"), 1),
                ItemStack(ItemId("acorn_cap"), 1)
            )
        )
        val manager = GameStateManager(player) { 0L }
        val controller = controller(this, manager)

        controller.updateEnvironmentTags(setOf("workbench"))
        advanceUntilIdle()

        controller.attemptInteraction("craft_twig_spear")
        advanceUntilIdle()

        val event = controller.state.value.lastEvent
        assertTrue(event?.success == false)
    }

    private fun controller(scope: TestScope, manager: GameStateManager): SystemicInteractionController {
        val engine = SystemicInteractionEngine(defaultInteractionCatalog())
        return SystemicInteractionController(scope, engine, manager)
    }
}
