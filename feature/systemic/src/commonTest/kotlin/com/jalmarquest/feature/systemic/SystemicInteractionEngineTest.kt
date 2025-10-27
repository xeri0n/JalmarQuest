package com.jalmarquest.feature.systemic

import com.jalmarquest.core.model.EnvironmentTag
import com.jalmarquest.core.model.InteractionId
import com.jalmarquest.core.model.Inventory
import com.jalmarquest.core.model.ItemId
import com.jalmarquest.core.model.ItemStack
import com.jalmarquest.core.model.StatusEffect
import com.jalmarquest.core.model.StatusEffects
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SystemicInteractionEngineTest {
    private val catalog = defaultInteractionCatalog()
    private val engine = SystemicInteractionEngine(catalog)

    @Test
    fun craftTwigSpearSucceedsWithIngredients() {
        val context = InteractionContext(
            inventory = Inventory(
                listOf(
                    ItemStack(ItemId("twig"), 2),
                    ItemStack(ItemId("acorn_cap"), 1)
                )
            ),
            statuses = StatusEffects(emptyList()),
            environment = setOf(EnvironmentTag("workbench"))
        )

        val options = engine.evaluate(context)
        val spearOption = options.first { it.interactionId == InteractionId("craft_twig_spear") }
        assertTrue(spearOption.available)

        val resolution = engine.resolve(InteractionId("craft_twig_spear"), context)
        assertIs<InteractionResolution.Success>(resolution)
    }

    @Test
    fun missingTwigBlocksCrafting() {
        val context = InteractionContext(
            inventory = Inventory(
                listOf(
                    ItemStack(ItemId("twig"), 1),
                    ItemStack(ItemId("acorn_cap"), 1)
                )
            ),
            statuses = StatusEffects(emptyList()),
            environment = setOf(EnvironmentTag("workbench"))
        )

        val option = engine.evaluate(context).first { it.interactionId == InteractionId("craft_twig_spear") }
        assertFalse(option.available)
        assertTrue(option.blockedReasons.any { it.value.contains("Needs") })

        val resolution = engine.resolve(InteractionId("craft_twig_spear"), context)
        assertIs<InteractionResolution.Failure>(resolution)
    }

    @Test
    fun forbiddenStatusPreventsDustBath() {
        val context = InteractionContext(
            inventory = Inventory(listOf(ItemStack(ItemId("soft_dust"), 1))),
            statuses = StatusEffects(listOf(StatusEffect("damp_plummage", null))),
            environment = setOf(EnvironmentTag("resting_spot"))
        )

        val option = engine.evaluate(context).first { it.interactionId == InteractionId("take_dust_bath") }
        assertFalse(option.available)
        assertTrue(option.blockedReasons.any { it.value.contains("Cannot") })
    }
}
