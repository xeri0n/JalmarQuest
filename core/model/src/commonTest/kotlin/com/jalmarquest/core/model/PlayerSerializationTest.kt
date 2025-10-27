package com.jalmarquest.core.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

/**
 * Comprehensive tests for Player model serialization and deserialization.
 * Ensures all player state can be safely persisted and restored.
 */
class PlayerSerializationTest {
    private val json = Json { 
        encodeDefaults = true
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Test
    fun roundTripSerializationKeepsChoiceLog() {
        val player = Player(
            id = "player-1",
            name = "Jalmar",
            choiceLog = ChoiceLog(
                entries = listOf(ChoiceLogEntry(tag = ChoiceTag("helped_ladybug_1"), timestampMillis = 42L))
            )
        )

        val encoded = json.encodeToString(player)
        val decoded = json.decodeFromString<Player>(encoded)

        assertEquals(player, decoded)
    }
    
    @Test
    fun testEmptyPlayerSerialization() {
        val player = Player(
            id = "test-player-1",
            name = "Empty Test Player"
        )
        
        val serialized = json.encodeToString(player)
        val deserialized = json.decodeFromString<Player>(serialized)
        
        assertEquals(player.id, deserialized.id)
        assertEquals(player.name, deserialized.name)
        assertTrue(deserialized.choiceLog.entries.isEmpty())
        assertTrue(deserialized.inventory.items.isEmpty())
        assertTrue(deserialized.shinyCollection.ownedShinies.isEmpty())
        assertEquals(HoardRankTier.SCAVENGER, deserialized.hoardRank.tier)
        assertTrue(deserialized.ingredientInventory.ingredients.isEmpty())
        assertTrue(deserialized.recipeBook.discoveredRecipes.isEmpty())
        assertTrue(deserialized.activeConcoctions.active.isEmpty())
        assertTrue(deserialized.thoughtCabinet.discoveredThoughts.isEmpty())
    }
    
    @Test
    fun testPlayerWithInventorySerialization() {
        val player = Player(
            id = "test-player-2",
            name = "Inventory Test Player",
            inventory = Inventory(
                items = listOf(
                    ItemStack(ItemId("seeds"), 1000),
                    ItemStack(ItemId("health_potion"), 5),
                    ItemStack(ItemId("rare_gem"), 1)
                )
            )
        )
        
        val serialized = json.encodeToString(player)
        val deserialized = json.decodeFromString<Player>(serialized)
        
        assertEquals(3, deserialized.inventory.items.size)
        assertEquals(1000, deserialized.inventory.totalQuantity(ItemId("seeds")))
        assertEquals(5, deserialized.inventory.totalQuantity(ItemId("health_potion")))
        assertEquals(1, deserialized.inventory.totalQuantity(ItemId("rare_gem")))
    }
    
    @Test
    fun testPlayerWithShinyCollectionSerialization() {
        val now = System.currentTimeMillis()
        val player = Player(
            id = "test-player-4",
            name = "Shiny Collection Test Player",
            shinyCollection = ShinyCollection(
                ownedShinies = listOf(
                    Shiny(ShinyId("polished_pebble"), "polished_pebble_name", "polished_pebble_desc", ShinyRarity.COMMON, 100, now),
                    Shiny(ShinyId("broken_compass"), "broken_compass_name", "broken_compass_desc", ShinyRarity.UNCOMMON, 500, now),
                    Shiny(ShinyId("rusty_key"), "rusty_key_name", "rusty_key_desc", ShinyRarity.RARE, 1000, now)
                )
            ),
            hoardRank = HoardRank(
                tier = HoardRankTier.CURATOR,
                totalValue = 5000,
                shiniesCollected = 3
            )
        )
        
        val serialized = json.encodeToString(player)
        val deserialized = json.decodeFromString<Player>(serialized)
        
        assertEquals(3, deserialized.shinyCollection.ownedShinies.size)
        assertTrue(deserialized.shinyCollection.hasShiny(ShinyId("polished_pebble")))
        assertTrue(deserialized.shinyCollection.hasShiny(ShinyId("broken_compass")))
        assertTrue(deserialized.shinyCollection.hasShiny(ShinyId("rusty_key")))
        assertEquals(HoardRankTier.CURATOR, deserialized.hoardRank.tier)
        assertEquals(5000, deserialized.hoardRank.totalValue)
        assertEquals(3, deserialized.hoardRank.shiniesCollected)
    }
    
    @Test
    fun testPlayerWithIngredientInventorySerialization() {
        val player = Player(
            id = "test-player-5",
            name = "Ingredient Test Player",
            ingredientInventory = IngredientInventory(
                ingredients = mapOf(
                    IngredientId("wildflower") to 10,
                    IngredientId("oak_bark") to 5,
                    IngredientId("moonpetal") to 3,
                    IngredientId("dragon_scale") to 1
                )
            )
        )
        
        val serialized = json.encodeToString(player)
        val deserialized = json.decodeFromString<Player>(serialized)
        
        assertEquals(4, deserialized.ingredientInventory.ingredients.size)
        assertEquals(10, deserialized.ingredientInventory.getQuantity(IngredientId("wildflower")))
        assertEquals(5, deserialized.ingredientInventory.getQuantity(IngredientId("oak_bark")))
        assertEquals(3, deserialized.ingredientInventory.getQuantity(IngredientId("moonpetal")))
        assertEquals(1, deserialized.ingredientInventory.getQuantity(IngredientId("dragon_scale")))
    }
    
    @Test
    fun testPlayerWithRecipeBookSerialization() {
        val now = System.currentTimeMillis()
        val player = Player(
            id = "test-player-6",
            name = "Recipe Book Test Player",
            recipeBook = RecipeBook(
                discoveredRecipes = setOf(
                    RecipeId("minor_health_potion"),
                    RecipeId("fortune_brew"),
                    RecipeId("wisdom_elixir")
                ),
                lastExperimentAt = now - 600000 // 10 minutes ago
            )
        )
        
        val serialized = json.encodeToString(player)
        val deserialized = json.decodeFromString<Player>(serialized)
        
        assertEquals(3, deserialized.recipeBook.discoveredRecipes.size)
        assertTrue(deserialized.recipeBook.hasRecipe(RecipeId("minor_health_potion")))
        assertTrue(deserialized.recipeBook.hasRecipe(RecipeId("fortune_brew")))
        assertTrue(deserialized.recipeBook.hasRecipe(RecipeId("wisdom_elixir")))
        assertEquals(now - 600000, deserialized.recipeBook.lastExperimentAt)
    }
    
    @Test
    fun testPlayerWithActiveConcoctionsSerialization() {
        val now = System.currentTimeMillis()
        
        // Create test concoction templates
        val healthPotionTemplate = ConcoctionTemplate(
            id = ConcoctionId("minor_health_potion"),
            nameKey = "minor_health_potion_name",
            descriptionKey = "minor_health_potion_desc",
            effects = listOf(ConcoctionEffect(EffectType.HEALTH_REGEN, 10, true)),
            durationSeconds = 300,
            stackLimit = 3
        )
        
        val fortuneBrewTemplate = ConcoctionTemplate(
            id = ConcoctionId("fortune_brew"),
            nameKey = "fortune_brew_name",
            descriptionKey = "fortune_brew_desc",
            effects = listOf(ConcoctionEffect(EffectType.SEED_BOOST, 20, true)),
            durationSeconds = 600,
            stackLimit = 1
        )
        
        val player = Player(
            id = "test-player-7",
            name = "Active Concoctions Test Player",
            activeConcoctions = ActiveConcoctions(
                active = listOf(
                    ActiveConcoction(
                        template = healthPotionTemplate,
                        appliedAt = now - 60000,
                        expiresAt = now + 240000,
                        stacks = 2
                    ),
                    ActiveConcoction(
                        template = fortuneBrewTemplate,
                        appliedAt = now - 30000,
                        expiresAt = now + 570000,
                        stacks = 1
                    )
                )
            )
        )
        
        val serialized = json.encodeToString(player)
        val deserialized = json.decodeFromString<Player>(serialized)
        
        assertEquals(2, deserialized.activeConcoctions.active.size)
        
        val healthPotion = deserialized.activeConcoctions.active.find { 
            it.template.id == ConcoctionId("minor_health_potion") 
        }
        assertNotNull(healthPotion)
        assertEquals(2, healthPotion.stacks)
        
        val fortuneBrew = deserialized.activeConcoctions.active.find { 
            it.template.id == ConcoctionId("fortune_brew") 
        }
        assertNotNull(fortuneBrew)
        assertEquals(1, fortuneBrew.stacks)
    }
    
    @Test
    fun testPlayerWithThoughtCabinetSerialization() {
        val now = System.currentTimeMillis()
        val player = Player(
            id = "test-player-8",
            name = "Thought Cabinet Test Player",
            thoughtCabinet = ThoughtCabinet(
                discoveredThoughts = setOf(
                    ThoughtId("curiosity_of_quailkind"),
                    ThoughtId("value_of_shinies"),
                    ThoughtId("nature_of_alchemy")
                ),
                activeSlots = listOf(
                    ThoughtSlot(
                        thoughtId = ThoughtId("nature_of_alchemy"),
                        startedAt = now - 600000, // 10 minutes ago
                        completesAt = now + 1200000 // 20 minutes remaining
                    )
                ),
                internalized = setOf(
                    ThoughtId("curiosity_of_quailkind"),
                    ThoughtId("value_of_shinies")
                ),
                maxSlots = 4 // One thought grants +1 slot
            )
        )
        
        val serialized = json.encodeToString(player)
        val deserialized = json.decodeFromString<Player>(serialized)
        
        assertEquals(3, deserialized.thoughtCabinet.discoveredThoughts.size)
        assertTrue(deserialized.thoughtCabinet.hasDiscovered(ThoughtId("curiosity_of_quailkind")))
        assertTrue(deserialized.thoughtCabinet.hasDiscovered(ThoughtId("value_of_shinies")))
        assertTrue(deserialized.thoughtCabinet.hasDiscovered(ThoughtId("nature_of_alchemy")))
        
        assertEquals(1, deserialized.thoughtCabinet.activeSlots.size)
        assertEquals(ThoughtId("nature_of_alchemy"), deserialized.thoughtCabinet.activeSlots[0].thoughtId)
        
        assertEquals(2, deserialized.thoughtCabinet.internalized.size)
        assertTrue(deserialized.thoughtCabinet.isInternalized(ThoughtId("curiosity_of_quailkind")))
        assertTrue(deserialized.thoughtCabinet.isInternalized(ThoughtId("value_of_shinies")))
        
        assertEquals(4, deserialized.thoughtCabinet.maxSlots)
    }
    
    @Test
    fun testFullyPopulatedPlayerSerialization() {
        val now = System.currentTimeMillis()
        
        val fortuneBrewTemplate = ConcoctionTemplate(
            id = ConcoctionId("fortune_brew"),
            nameKey = "fortune_brew_name",
            descriptionKey = "fortune_brew_desc",
            effects = listOf(ConcoctionEffect(EffectType.SEED_BOOST, 20, true)),
            durationSeconds = 600,
            stackLimit = 1
        )
        
        val player = Player(
            id = "test-player-full",
            name = "Fully Populated Test Player",
            choiceLog = ChoiceLog(
                entries = listOf(
                    ChoiceLogEntry(ChoiceTag("game_started"), now - 3600000),
                    ChoiceLogEntry(ChoiceTag("milestone_hoard_rank_collector"), now - 1800000)
                )
            ),
            inventory = Inventory(
                items = listOf(
                    ItemStack(ItemId("seeds"), 50000)
                )
            ),
            shinyCollection = ShinyCollection(
                ownedShinies = listOf(
                    Shiny(ShinyId("polished_pebble"), "pebble_name", "pebble_desc", ShinyRarity.COMMON, 100, now),
                    Shiny(ShinyId("broken_compass"), "compass_name", "compass_desc", ShinyRarity.UNCOMMON, 500, now)
                )
            ),
            hoardRank = HoardRank(
                tier = HoardRankTier.CURATOR,
                totalValue = 25000,
                shiniesCollected = 8
            ),
            ingredientInventory = IngredientInventory(
                ingredients = mapOf(
                    IngredientId("wildflower") to 15,
                    IngredientId("moonpetal") to 5
                )
            ),
            recipeBook = RecipeBook(
                discoveredRecipes = setOf(
                    RecipeId("minor_health_potion"),
                    RecipeId("fortune_brew")
                ),
                lastExperimentAt = now - 3600000
            ),
            activeConcoctions = ActiveConcoctions(
                active = listOf(
                    ActiveConcoction(
                        template = fortuneBrewTemplate,
                        appliedAt = now - 60000,
                        expiresAt = now + 540000,
                        stacks = 1
                    )
                )
            ),
            thoughtCabinet = ThoughtCabinet(
                discoveredThoughts = setOf(
                    ThoughtId("curiosity_of_quailkind"),
                    ThoughtId("value_of_shinies")
                ),
                activeSlots = emptyList(),
                internalized = setOf(
                    ThoughtId("curiosity_of_quailkind")
                ),
                maxSlots = 3
            )
        )
        
        // Serialize and deserialize
        val serialized = json.encodeToString(player)
        val deserialized = json.decodeFromString<Player>(serialized)
        
        // Verify all state preserved
        assertEquals(player.id, deserialized.id)
        assertEquals(player.name, deserialized.name)
        assertEquals(2, deserialized.choiceLog.entries.size)
        assertEquals(50000, deserialized.inventory.totalQuantity(ItemId("seeds")))
        assertEquals(2, deserialized.shinyCollection.ownedShinies.size)
        assertEquals(HoardRankTier.CURATOR, deserialized.hoardRank.tier)
        assertEquals(2, deserialized.ingredientInventory.ingredients.size)
        assertEquals(2, deserialized.recipeBook.discoveredRecipes.size)
        assertEquals(1, deserialized.activeConcoctions.active.size)
        assertEquals(2, deserialized.thoughtCabinet.discoveredThoughts.size)
        assertEquals(1, deserialized.thoughtCabinet.internalized.size)
    }
    
    @Test
    fun testSerializedJsonStructure() {
        val player = Player(
            id = "test-structure",
            name = "JSON Structure Test",
            inventory = Inventory(items = listOf(ItemStack(ItemId("seeds"), 100)))
        )
        
        val serialized = json.encodeToString(player)
        
        // Verify @SerialName annotations are working
        assertTrue(serialized.contains("\"choice_log\""))
        assertTrue(serialized.contains("\"quest_log\""))
        assertTrue(serialized.contains("\"status_effects\""))
        assertTrue(serialized.contains("\"inventory\""))
        assertTrue(serialized.contains("\"shiny_collection\""))
        assertTrue(serialized.contains("\"hoard_rank\""))
        assertTrue(serialized.contains("\"ingredient_inventory\""))
        assertTrue(serialized.contains("\"recipe_book\""))
        assertTrue(serialized.contains("\"active_concoctions\""))
        assertTrue(serialized.contains("\"thought_cabinet\""))
    }
    
    @Test
    fun testRoundTripPreservesAllData() {
        val now = System.currentTimeMillis()
        
        val testTemplate = ConcoctionTemplate(
            id = ConcoctionId("test_concoction"),
            nameKey = "test_name",
            descriptionKey = "test_desc",
            effects = listOf(ConcoctionEffect(EffectType.HEALTH_REGEN, 5, true)),
            durationSeconds = 300,
            stackLimit = 3
        )
        
        val original = Player(
            id = "roundtrip-test",
            name = "Round Trip Test Player",
            choiceLog = ChoiceLog(entries = listOf(ChoiceLogEntry(ChoiceTag("test"), now))),
            inventory = Inventory(items = listOf(ItemStack(ItemId("seeds"), 999))),
            shinyCollection = ShinyCollection(ownedShinies = listOf(
                Shiny(ShinyId("test_shiny"), "test_name", "test_desc", ShinyRarity.COMMON, 100, now)
            )),
            hoardRank = HoardRank(tier = HoardRankTier.MAGNATE, totalValue = 100000, shiniesCollected = 20),
            ingredientInventory = IngredientInventory(ingredients = mapOf(IngredientId("test_ingredient") to 42)),
            recipeBook = RecipeBook(discoveredRecipes = setOf(RecipeId("test_recipe")), lastExperimentAt = now),
            activeConcoctions = ActiveConcoctions(active = listOf(
                ActiveConcoction(testTemplate, now, now + 300000, 3)
            )),
            thoughtCabinet = ThoughtCabinet(
                discoveredThoughts = setOf(ThoughtId("test_thought")),
                activeSlots = listOf(ThoughtSlot(ThoughtId("test_thought"), now, now + 600000)),
                internalized = setOf(ThoughtId("another_thought")),
                maxSlots = 5
            ),
            archetypeProgress = ArchetypeProgress(
                selectedArchetype = ArchetypeType.COLLECTOR,
                archetypeLevel = 4,
                archetypeXP = 800,
                availableTalentPoints = 2,
                talentTree = TalentTree(
                    archetypeType = ArchetypeType.COLLECTOR,
                    talents = listOf(
                        Talent(
                            id = "collector_treasure_sense",
                            name = "Treasure Sense",
                            description = "+20% luck",
                            talentType = TalentType.LUCK_BONUS,
                            magnitude = 20,
                            costInPoints = 1,
                            requirements = emptyList()
                        )
                    ),
                    unlockedTalentIds = setOf("collector_treasure_sense")
                )
            )
        )
        
        // Multiple round trips
        var current = original
        for (i in 1..5) {
            val serialized = json.encodeToString(current)
            current = json.decodeFromString(serialized)
        }
        
        // Verify data integrity after 5 round trips
        assertEquals(original.id, current.id)
        assertEquals(original.name, current.name)
        assertEquals(original.choiceLog.entries.size, current.choiceLog.entries.size)
        assertEquals(original.inventory.items.size, current.inventory.items.size)
        assertEquals(original.shinyCollection.ownedShinies.size, current.shinyCollection.ownedShinies.size)
        assertEquals(original.hoardRank.tier, current.hoardRank.tier)
        assertEquals(original.ingredientInventory.ingredients.size, current.ingredientInventory.ingredients.size)
        assertEquals(original.recipeBook.discoveredRecipes.size, current.recipeBook.discoveredRecipes.size)
        assertEquals(original.activeConcoctions.active.size, current.activeConcoctions.active.size)
        assertEquals(original.thoughtCabinet.discoveredThoughts.size, current.thoughtCabinet.discoveredThoughts.size)
        assertEquals(original.archetypeProgress.selectedArchetype, current.archetypeProgress.selectedArchetype)
        assertEquals(original.archetypeProgress.archetypeLevel, current.archetypeProgress.archetypeLevel)
        assertEquals(original.archetypeProgress.talentTree?.unlockedTalentIds?.size, current.archetypeProgress.talentTree?.unlockedTalentIds?.size)
    }
    
    @Test
    fun testMaximumValuesSerialization() {
        val now = System.currentTimeMillis()
        
        val testTemplate = ConcoctionTemplate(
            id = ConcoctionId("test"),
            nameKey = "test_name",
            descriptionKey = "test_desc",
            effects = listOf(ConcoctionEffect(EffectType.HEALTH_REGEN, 100, true)),
            durationSeconds = 300,
            stackLimit = Int.MAX_VALUE
        )
        
        // Test with extreme values to ensure no overflow/corruption
        val player = Player(
            id = "test-max-values",
            name = "Maximum Values Test",
            inventory = Inventory(items = listOf(ItemStack(ItemId("seeds"), Int.MAX_VALUE))),
            hoardRank = HoardRank(
                tier = HoardRankTier.MYTH,
                totalValue = Long.MAX_VALUE,
                shiniesCollected = Int.MAX_VALUE
            ),
            ingredientInventory = IngredientInventory(
                ingredients = mapOf(IngredientId("test") to Int.MAX_VALUE)
            ),
            recipeBook = RecipeBook(
                discoveredRecipes = setOf(RecipeId("test")),
                lastExperimentAt = Long.MAX_VALUE
            ),
            activeConcoctions = ActiveConcoctions(
                active = listOf(
                    ActiveConcoction(
                        template = testTemplate,
                        appliedAt = Long.MAX_VALUE - 1000000,
                        expiresAt = Long.MAX_VALUE,
                        stacks = Int.MAX_VALUE
                    )
                )
            ),
            thoughtCabinet = ThoughtCabinet(
                discoveredThoughts = setOf(ThoughtId("test")),
                activeSlots = listOf(
                    ThoughtSlot(
                        thoughtId = ThoughtId("test"),
                        startedAt = Long.MAX_VALUE - 2000000,
                        completesAt = Long.MAX_VALUE
                    )
                ),
                internalized = emptySet(),
                maxSlots = Int.MAX_VALUE
            )
        )
        
        val serialized = json.encodeToString(player)
        val deserialized = json.decodeFromString<Player>(serialized)
        
        assertEquals(Int.MAX_VALUE, deserialized.inventory.totalQuantity(ItemId("seeds")))
        assertEquals(Long.MAX_VALUE, deserialized.hoardRank.totalValue)
        assertEquals(Long.MAX_VALUE, deserialized.recipeBook.lastExperimentAt)
        assertEquals(Int.MAX_VALUE, deserialized.thoughtCabinet.maxSlots)
    }
    
    @Test
    fun testPlayerWithArchetypeProgressSerialization() {
        val scholarTalent = Talent(
            id = "scholar_quick_study",
            name = "Quick Study",
            description = "+15% XP",
            talentType = TalentType.GENERAL_XP_BONUS,
            magnitude = 15,
            costInPoints = 1,
            requirements = emptyList()
        )
        
        val talentTree = TalentTree(
            archetypeType = ArchetypeType.SCHOLAR,
            talents = listOf(scholarTalent),
            unlockedTalentIds = setOf("scholar_quick_study")
        )
        
        val player = Player(
            id = "test-archetype",
            name = "Archetype Test Player",
            archetypeProgress = ArchetypeProgress(
                selectedArchetype = ArchetypeType.SCHOLAR,
                archetypeLevel = 5,
                archetypeXP = 1200,
                availableTalentPoints = 3,
                talentTree = talentTree
            )
        )
        
        val serialized = json.encodeToString(player)
        val deserialized = json.decodeFromString<Player>(serialized)
        
        assertEquals(ArchetypeType.SCHOLAR, deserialized.archetypeProgress.selectedArchetype)
        assertEquals(5, deserialized.archetypeProgress.archetypeLevel)
        assertEquals(1200, deserialized.archetypeProgress.archetypeXP)
        assertEquals(3, deserialized.archetypeProgress.availableTalentPoints)
        assertEquals(1, deserialized.archetypeProgress.talentTree?.talents?.size)
        assertEquals(1, deserialized.archetypeProgress.talentTree?.unlockedTalentIds?.size)
        assertTrue("scholar_quick_study" in (deserialized.archetypeProgress.talentTree?.unlockedTalentIds ?: emptySet()))
    }
}
