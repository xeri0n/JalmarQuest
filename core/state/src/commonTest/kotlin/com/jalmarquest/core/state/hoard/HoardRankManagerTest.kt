package com.jalmarquest.core.state.hoard

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import kotlin.test.*

class HoardRankManagerTest {
    
    private lateinit var gameStateManager: GameStateManager
    private lateinit var valuationService: ShinyValuationService
    private lateinit var leaderboardService: LeaderboardService
    private lateinit var hoardManager: HoardRankManager
    private var currentTime = 1000L
    
    @BeforeTest
    fun setup() {
        val initialPlayer = Player(
            id = "test-player",
            name = "Test Player"
        )
        gameStateManager = GameStateManager(initialPlayer) { currentTime }
        valuationService = ShinyValuationService()
        leaderboardService = LeaderboardService()
        hoardManager = HoardRankManager(
            gameStateManager, 
            valuationService, 
            leaderboardService, 
            timestampProvider = { currentTime },
            skillManager = null
        )
    }
    
    @Test
    fun testInitialState() {
        val viewState = hoardManager.viewState.value
        
        assertEquals(0, viewState.rank.totalValue)
        assertEquals(HoardRankTier.SCAVENGER, viewState.rank.tier)
        assertEquals(0, viewState.rank.shiniesCollected)
        assertEquals(0, viewState.collection.ownedShinies.size)
        assertTrue(viewState.catalog.isNotEmpty(), "Catalog should contain Shinies")
    }
    
    @Test
    fun testAcquireShiny() {
        val shinyId = ShinyId("acorn_cap")
        
        val success = hoardManager.acquireShiny(shinyId)
        
        assertTrue(success, "Should successfully acquire Shiny")
        
        val viewState = hoardManager.viewState.value
        assertEquals(1, viewState.collection.ownedShinies.size)
        assertEquals(1, viewState.rank.shiniesCollected)
        assertTrue(viewState.collection.hasShiny(shinyId))
        
        val shiny = viewState.collection.ownedShinies.first()
        assertEquals(currentTime, shiny.discoveredAt)
        assertEquals(shiny.baseValue, viewState.rank.totalValue)
    }
    
    @Test
    fun testAcquireDuplicateShiny() {
        val shinyId = ShinyId("acorn_cap")
        
        val firstAcquire = hoardManager.acquireShiny(shinyId)
        val secondAcquire = hoardManager.acquireShiny(shinyId)
        
        assertTrue(firstAcquire)
        assertFalse(secondAcquire, "Should not acquire duplicate Shiny")
        
        val viewState = hoardManager.viewState.value
        assertEquals(1, viewState.collection.ownedShinies.size)
    }
    
    @Test
    fun testAcquireInvalidShiny() {
        val invalidId = ShinyId("nonexistent_shiny")
        
        val success = hoardManager.acquireShiny(invalidId)
        
        assertFalse(success, "Should fail to acquire nonexistent Shiny")
        assertEquals(0, hoardManager.viewState.value.collection.ownedShinies.size)
    }
    
    @Test
    fun testTierProgression() {
        // Start at SCAVENGER (0-999)
        assertEquals(HoardRankTier.SCAVENGER, hoardManager.viewState.value.rank.tier)
        
        // Acquire Shinies to reach COLLECTOR tier (1000+)
        // Common Shinies: 50, 75, 100
        // Uncommon: 250, 300
        // Rare: 750
        // Total: 1525 (should be COLLECTOR tier)
        hoardManager.acquireShiny(ShinyId("acorn_cap")) // 50
        hoardManager.acquireShiny(ShinyId("smooth_pebble")) // 75
        hoardManager.acquireShiny(ShinyId("blue_feather")) // 100
        hoardManager.acquireShiny(ShinyId("copper_button")) // 250
        hoardManager.acquireShiny(ShinyId("glass_marble")) // 300
        hoardManager.acquireShiny(ShinyId("silver_thimble")) // 750
        
        val viewState = hoardManager.viewState.value
        assertEquals(1525, viewState.rank.totalValue)
        assertEquals(HoardRankTier.COLLECTOR, viewState.rank.tier)
        assertEquals(6, viewState.rank.shiniesCollected)
    }
    
    @Test
    fun testTierThresholds() {
        assertEquals(HoardRankTier.SCAVENGER, HoardRank.calculateTier(0))
        assertEquals(HoardRankTier.SCAVENGER, HoardRank.calculateTier(999))
        assertEquals(HoardRankTier.COLLECTOR, HoardRank.calculateTier(1000))
        assertEquals(HoardRankTier.COLLECTOR, HoardRank.calculateTier(4999))
        assertEquals(HoardRankTier.CURATOR, HoardRank.calculateTier(5000))
        assertEquals(HoardRankTier.CURATOR, HoardRank.calculateTier(14999))
        assertEquals(HoardRankTier.MAGNATE, HoardRank.calculateTier(15000))
        assertEquals(HoardRankTier.MAGNATE, HoardRank.calculateTier(49999))
        assertEquals(HoardRankTier.LEGEND, HoardRank.calculateTier(50000))
        assertEquals(HoardRankTier.LEGEND, HoardRank.calculateTier(149999))
        assertEquals(HoardRankTier.MYTH, HoardRank.calculateTier(150000))
        assertEquals(HoardRankTier.MYTH, HoardRank.calculateTier(1000000))
    }
    
    @Test
    fun testNextTierThreshold() {
        assertEquals(1_000L, HoardRank.nextTierThreshold(HoardRankTier.SCAVENGER))
        assertEquals(5_000L, HoardRank.nextTierThreshold(HoardRankTier.COLLECTOR))
        assertEquals(15_000L, HoardRank.nextTierThreshold(HoardRankTier.CURATOR))
        assertEquals(50_000L, HoardRank.nextTierThreshold(HoardRankTier.MAGNATE))
        assertEquals(150_000L, HoardRank.nextTierThreshold(HoardRankTier.LEGEND))
        assertNull(HoardRank.nextTierThreshold(HoardRankTier.MYTH), "MYTH tier has no next threshold")
    }
    
    @Test
    fun testTierProgressCalculation() {
        // Empty hoard at SCAVENGER tier
        var progress = valuationService.calculateTierProgress(hoardManager.viewState.value.rank)
        assertEquals(0f, progress)
        
        // Acquire 500 Seeds worth (50% to COLLECTOR)
        hoardManager.acquireShiny(ShinyId("acorn_cap")) // 50
        hoardManager.acquireShiny(ShinyId("smooth_pebble")) // 75
        hoardManager.acquireShiny(ShinyId("blue_feather")) // 100
        hoardManager.acquireShiny(ShinyId("copper_button")) // 250
        // Total: 475
        
        progress = valuationService.calculateTierProgress(hoardManager.viewState.value.rank)
        assertNotNull(progress)
        assertTrue(progress!! > 0.4f && progress < 0.5f, "Should be around 47.5% progress")
    }
    
    @Test
    fun testMaxTierProgress() {
        // Acquire all Shinies to reach top tier
        // Total catalog value: 104,225 Seeds (LEGEND tier: 50,000-149,999)
        valuationService.getAllShinies().forEach { shiny ->
            hoardManager.acquireShiny(shiny.id)
        }
        
        val viewState = hoardManager.viewState.value
        assertEquals(HoardRankTier.LEGEND, viewState.rank.tier)
        // Alpha 2.2 Phase 5C: Added Golden Coffee Bean shiny (5000 Seeds)
        assertEquals(109225, viewState.rank.totalValue) // Was 104225, +5000 for new shiny
        
        // LEGEND tier still has progress to MYTH (150,000)
        val progress = valuationService.calculateTierProgress(viewState.rank)
        assertNotNull(progress, "LEGEND tier should show progress to MYTH")
        assertTrue(progress!! > 0.5f, "Should be over 50% to MYTH tier")
    }
    
    @Test
    fun testChoiceAnalytics() {
        val shinyId = ShinyId("golden_acorn")
        
        hoardManager.acquireShiny(shinyId)
        
        val player = gameStateManager.playerState.value
        val choiceLog = player.choiceLog.entries
        
        assertTrue(choiceLog.any { it.tag.value == "hoard_shiny_acquired_golden_acorn" })
    }
    
    @Test
    fun testPurchaseShiny() {
        val shinyId = ShinyId("star_fragment")
        
        val success = hoardManager.purchaseShiny(shinyId)
        
        assertTrue(success, "Should successfully purchase Shiny")
        
        val player = gameStateManager.playerState.value
        assertTrue(player.shinyCollection.hasShiny(shinyId))
        
        val choiceLog = player.choiceLog.entries
        assertTrue(choiceLog.any { it.tag.value == "hoard_shiny_acquired_star_fragment" })
        assertTrue(choiceLog.any { it.tag.value == "hoard_shiny_purchased_star_fragment" })
    }
    
    @Test
    fun testShinyCollectionValueCalculation() {
        val collection = ShinyCollection()
        assertEquals(0, collection.calculateTotalValue())
        
        val shiny1 = Shiny(
            id = ShinyId("test1"),
            nameKey = "test1",
            descriptionKey = "desc1",
            rarity = ShinyRarity.COMMON,
            baseValue = 100
        )
        val shiny2 = Shiny(
            id = ShinyId("test2"),
            nameKey = "test2",
            descriptionKey = "desc2",
            rarity = ShinyRarity.RARE,
            baseValue = 500
        )
        
        val updated = collection
            .addShiny(shiny1, 1000L)
            .addShiny(shiny2, 2000L)
        
        assertEquals(600, updated.calculateTotalValue())
        assertEquals(2, updated.ownedShinies.size)
    }
    
    @Test
    fun testShinyCollectionRarityCount() {
        hoardManager.acquireShiny(ShinyId("acorn_cap")) // COMMON
        hoardManager.acquireShiny(ShinyId("smooth_pebble")) // COMMON
        hoardManager.acquireShiny(ShinyId("copper_button")) // UNCOMMON
        hoardManager.acquireShiny(ShinyId("silver_thimble")) // RARE
        
        val collection = hoardManager.viewState.value.collection
        
        assertEquals(2, collection.countByRarity(ShinyRarity.COMMON))
        assertEquals(1, collection.countByRarity(ShinyRarity.UNCOMMON))
        assertEquals(1, collection.countByRarity(ShinyRarity.RARE))
        assertEquals(0, collection.countByRarity(ShinyRarity.EPIC))
    }
    
    @Test
    fun testEmptyHoardEdgeCase() {
        val viewState = hoardManager.viewState.value
        
        assertEquals(0, viewState.rank.totalValue)
        assertEquals(HoardRankTier.SCAVENGER, viewState.rank.tier)
        assertEquals(0, viewState.rank.shiniesCollected)
        assertNotNull(viewState.nextTierThreshold)
        assertEquals(1_000L, viewState.nextTierThreshold)
    }
    
    @Test
    fun testPlayerStateIntegration() {
        val shinyId = ShinyId("phoenix_plume")
        
        hoardManager.acquireShiny(shinyId)
        
        val player = gameStateManager.playerState.value
        
        // Verify player state was updated
        assertTrue(player.shinyCollection.hasShiny(shinyId))
        assertTrue(player.hoardRank.totalValue > 0)
        assertEquals(1, player.hoardRank.shiniesCollected)
        
        // Verify HoardRankManager view state matches player state
        val viewState = hoardManager.viewState.value
        assertEquals(player.shinyCollection.ownedShinies.size, viewState.collection.ownedShinies.size)
        assertEquals(player.hoardRank.totalValue, viewState.rank.totalValue)
        assertEquals(player.hoardRank.tier, viewState.rank.tier)
    }
}
