package com.jalmarquest.core.di

import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.Inventory
import com.jalmarquest.core.model.ItemId
import com.jalmarquest.core.model.ItemStack
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.model.QuestLog
import com.jalmarquest.core.model.StatusEffects

/**
 * Provides the baseline player model used when initializing the DI graph.
 */
fun defaultPlayer(): Player = Player(
    id = "jalmar",
    name = "Jalmar",
    choiceLog = ChoiceLog(emptyList()),
    questLog = QuestLog(),
    statusEffects = StatusEffects(emptyList()),
    inventory = Inventory(
        listOf(
            ItemStack(id = ItemId("twig"), quantity = 3),
            ItemStack(id = ItemId("acorn_cap"), quantity = 1)
        )
    )
)
