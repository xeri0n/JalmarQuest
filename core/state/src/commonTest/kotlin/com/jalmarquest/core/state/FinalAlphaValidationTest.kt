package com.jalmarquest.core.state

import kotlin.test.*
import kotlinx.coroutines.test.runTest
import com.jalmarquest.core.model.*
import com.jalmarquest.feature.companions.CompanionManager
import com.jalmarquest.core.state.faction.FactionManager

class FinalAlphaValidationTest {
    
    @Test
    fun `all core systems initialize without errors`() = runTest {
        // Create GameStateManager with all subsystems
        val gameStateManager = GameStateManager(
            initialPlayer = createTestPlayer(),
            accountManager = null,
            timestampProvider = { System.currentTimeMillis() }
        )
        
        assertNotNull(gameStateManager.playerState.value)
        assertEquals("test_player", gameStateManager.playerState.value.id)
    }
    
    @Test
    fun `inventory sorting returns correct order`() {
        val inventory = Inventory(
            items = mapOf(
                ItemId("seed_common") to 50,
                ItemId("seed_rare") to 10,
                ItemId("twig_sharp") to 1
            )
        )
        
        val catalog = mapOf(
            ItemId("seed_common") to ItemDetails(
                name = "Common Seed",
                description = "Basic seed",
                type = ItemType.CONSUMABLE,
                rarity = ItemRarity.COMMON,
                seedValue = 1
            ),
            ItemId("seed_rare") to ItemDetails(
                name = "Rare Seed",
                description = "Valuable seed",
                type = ItemType.CONSUMABLE,
                rarity = ItemRarity.RARE,
                seedValue = 10
            ),
            ItemId("twig_sharp") to ItemDetails(
                name = "Sharpened Twig",
                description = "Weapon",
                type = ItemType.EQUIPMENT,
                rarity = ItemRarity.UNCOMMON,
                seedValue = 5
            )
        )
        
        val sortedByRarity = inventory.getSortedItems(Inventory.SortType.BY_RARITY, catalog)
        assertEquals(3, sortedByRarity.size)
        assertEquals(ItemId("seed_common"), sortedByRarity[0].first)
        
        val sortedByValue = inventory.getSortedItems(Inventory.SortType.BY_VALUE, catalog)
        assertEquals(ItemId("seed_rare"), sortedByValue[0].first) // Highest total value
    }
    
    @Test
    fun `companion system manages relationships correctly`() = runTest {
        val gameStateManager = GameStateManager(
            initialPlayer = createTestPlayer(),
            accountManager = null,
            timestampProvider = { System.currentTimeMillis() }
        )
        
        val companionManager = CompanionManager(
            gameStateManager = gameStateManager,
            companionCatalog = MockCompanionCatalog()
        )
        
        // Recruit companion
        val companionId = CompanionManager.CompanionId("chirpy")
        companionManager.recruitCompanion(companionId, System.currentTimeMillis())
        
        val state = companionManager.state.value
        assertTrue(companionId in state.companions)
        assertEquals(companionId, state.activeCompanion)
        
        // Give gift
        val favoriteGift = ItemId("golden_seed")
        companionManager.giveGift(companionId, favoriteGift, System.currentTimeMillis())
        
        val companion = companionManager.state.value.companions[companionId]
        assertNotNull(companion)
        assertTrue(companion.affinity > 0)
    }
    
    @Test
    fun `faction reputation system handles relationships`() = runTest {
        val gameStateManager = GameStateManager(
            initialPlayer = createTestPlayer(),
            accountManager = null,
            timestampProvider = { System.currentTimeMillis() }
        )
        
        val factionManager = FactionManager(gameStateManager)
        
        // Modify reputation
        val factionId = FactionManager.FactionId("buttonburgh_citizens")
        factionManager.modifyReputation(
            factionId = factionId,
            amount = 25,
            reason = "Completed quest"
        )
        
        val reputation = factionManager.state.value.reputations[factionId]
        assertNotNull(reputation)
        assertEquals(25, reputation.currentReputation)
        assertEquals(FactionManager.ReputationRank.FRIENDLY, reputation.rank)
        
        // Test choice evaluation
        factionManager.evaluateChoiceReputation("helped_merchant")
        
        // Should have improved reputation
        val updatedRep = factionManager.state.value.reputations[factionId]
        assertNotNull(updatedRep)
        assertTrue(updatedRep.currentReputation > 25)
    }
    
    @Test
    fun `save and load preserves all game state`() = runTest {
        val player = createTestPlayer().copy(
            inventory = Inventory(
                items = mapOf(
                    ItemId("test_item") to 5,
                    ItemId("golden_seed") to 100
                )
            ),
            choiceLog = ChoiceLog(
                entries = listOf(
                    ChoiceEntry(
                        tag = ChoiceTag("test_choice_1"),
                        timestamp = 1000L
                    ),
                    ChoiceEntry(
                        tag = ChoiceTag("companion_recruited_chirpy"),
                        timestamp = 2000L
                    )
                )
            )
        )
        
        // Serialize
        val json = kotlinx.serialization.json.Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
        
        val serialized = json.encodeToString(Player.serializer(), player)
        assertNotNull(serialized)
        assertTrue(serialized.contains("test_item"))
        assertTrue(serialized.contains("golden_seed"))
        assertTrue(serialized.contains("test_choice_1"))
        
        // Deserialize
        val deserialized = json.decodeFromString(Player.serializer(), serialized)
        assertEquals(player.id, deserialized.id)
        assertEquals(2, deserialized.inventory.items.size)
        assertEquals(5, deserialized.inventory.totalQuantity(ItemId("test_item")))
        assertEquals(100, deserialized.inventory.totalQuantity(ItemId("golden_seed")))
        assertEquals(2, deserialized.choiceLog.entries.size)
    }
    
    private fun createTestPlayer() = Player(
        id = "test_player",
        name = "Test Quail",
        choiceLog = ChoiceLog(),
        questLog = QuestLog(),
        inventory = Inventory(),
        statusEffects = StatusEffects(),
        explorationProgress = ExplorationProgress(),
        systemicInteractionProgress = SystemicInteractionProgress(),
        currentLocation = LocationId("buttonburgh_hub"),
        nestProgress = NestProgress(),
        activityProgress = ActivityProgress(),
        hoardRank = HoardRank(),
        ingredientInventory = IngredientInventory(),
        recipeBook = RecipeBook(),
        activeConcoctions = ActiveConcoctions(),
        thoughtCabinet = ThoughtCabinet(),
        archetypeProgress = null,
        skillProgress = SkillProgress(),
        craftingProgress = CraftingProgress(),
        equipmentLoadout = EquipmentLoadout(),
        worldMapState = WorldMapState(),
        shopState = ShopState(),
        seasonalChronicle = SeasonalChronicleState(),
        entitlements = EntitlementState()
    )
    
    // Mock catalog for testing
    private class MockCompanionCatalog : CompanionManager.CompanionCatalog {
        override fun getCompanion(id: CompanionManager.CompanionId): CompanionTemplate? {
            return if (id.value == "chirpy") {
                CompanionTemplate(
                    name = "Chirpy",
                    species = "Sparrow",
                    personality = CompanionManager.PersonalityType.CHEERFUL,
                    abilities = listOf(
                        CompanionManager.CompanionAbility(
                            id = CompanionManager.AbilityId("passive_income"),
                            name = "Seed Finder",
                            description = "Finds seeds while exploring",
                            type = CompanionManager.AbilityType.PASSIVE_INCOME,
                            requiredAffinity = 20,
                            effect = CompanionManager.AbilityEffect(
                                type = CompanionManager.AbilityType.PASSIVE_INCOME,
                                magnitude = 10f,
                                description = "10 seeds per hour"
                            )
                        )
                    ),
                    favoriteGifts = setOf(ItemId("golden_seed")),
                    hatedGifts = setOf(ItemId("rotten_berry"))
                )
            } else null
        }
    }
    
    data class CompanionTemplate(
        val name: String,
        val species: String,
        val personality: CompanionManager.PersonalityType,
        val abilities: List<CompanionManager.CompanionAbility>,
        val favoriteGifts: Set<ItemId>,
        val hatedGifts: Set<ItemId>
    )
}

// Extension for CompanionManager to support catalog
interface CompanionManager.CompanionCatalog {
    fun getCompanion(id: CompanionManager.CompanionId): FinalAlphaValidationTest.CompanionTemplate?
}
