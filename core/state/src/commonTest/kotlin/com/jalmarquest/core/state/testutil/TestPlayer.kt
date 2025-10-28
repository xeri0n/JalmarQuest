package com.jalmarquest.core.state.testutil

import com.jalmarquest.core.model.*

/**
 * Create a test player with reasonable defaults for testing.
 */
fun testPlayer(
    id: String = "test_player_1",
    name: String = "Test Jalmar",
    level: Int = 5
): Player {
    return Player(
        id = id,
        name = name,
        level = level,
        choiceLog = ChoiceLog(emptyList()),
        questLog = QuestLog(),
        statusEffects = StatusEffects(emptyList()),
        inventory = Inventory(emptyList()),
        shinyCollection = ShinyCollection(emptyList()),
        hoardRank = HoardRank(),
        seedInventory = SeedInventory(storedSeeds = 1000, maxCapacity = 5000),
        ingredientInventory = IngredientInventory(),
        craftingInventory = CraftingInventory(
            ingredients = emptyMap(),
            knownRecipes = emptySet()
        ),
        recipeBook = RecipeBook(),
        activeConcoctions = ActiveConcoctions(),
        thoughtCabinet = ThoughtCabinet(),
        skillTree = SkillTree(),
        craftingKnowledge = CraftingKnowledge(),
        archetypeProgress = ArchetypeProgress(),
        companionState = CompanionState(),
        glimmerWallet = GlimmerWallet(balance = 5000),
        seasonalChronicle = SeasonalChronicleState(),
        shopState = ShopState(),
        entitlements = EntitlementState(),
        nestCustomization = NestCustomizationState(),
        factionReputations = emptyMap(),
        worldExploration = WorldExplorationState(),
        worldMapState = null,
        playerSettings = PlayerSettings(),
        aiDirectorState = AIDirectorState(),
        harvestingState = HarvestingState()
    )
}
