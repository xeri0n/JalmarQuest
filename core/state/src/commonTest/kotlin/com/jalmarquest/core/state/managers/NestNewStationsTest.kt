package com.jalmarquest.core.state.managers

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.catalogs.NestUpgradeTierCatalog
import com.jalmarquest.core.state.monetization.GlimmerWalletManager
import com.jalmarquest.core.state.testutil.testPlayer
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Alpha 2.3 Part 2.2: Test suite for new nest stations.
 * 
 * Validates:
 * - COMPANION_ASSIGNMENT_BOARD (2/4/6 concurrent assignments)
 * - LORE_ARCHIVE (20/50/100 stored entries)
 * - AI_DIRECTOR_CONSOLE (10/25/50 event history depth)
 */
class NestNewStationsTest {
    private lateinit var gameStateManager: GameStateManager
    private lateinit var glimmerWalletManager: GlimmerWalletManager
    private lateinit var nestManager: NestCustomizationManager
    private lateinit var upgradeCatalog: NestUpgradeTierCatalog
    private var currentTime = 0L
    
    @BeforeTest
    fun setup() {
        currentTime = 1000L
        gameStateManager = GameStateManager(
            initialPlayer = createPlayerWithStation(FunctionalUpgradeType.COMPANION_ASSIGNMENT_BOARD),
            accountManager = null,
            timestampProvider = { currentTime }
        )
        glimmerWalletManager = GlimmerWalletManager(
            gameStateManager = gameStateManager,
            timestampProvider = { currentTime }
        )
        upgradeCatalog = NestUpgradeTierCatalog()
        nestManager = NestCustomizationManager(
            gameStateManager = gameStateManager,
            glimmerWalletManager = glimmerWalletManager,
            timestampProvider = { currentTime },
            cosmeticCatalog = emptyList(),
            upgradeTierCatalog = upgradeCatalog
        )
    }
    
    // ========================================
    // COMPANION_ASSIGNMENT_BOARD Tests
    // ========================================
    
    @Test
    fun `COMPANION_ASSIGNMENT_BOARD Tier 1 allows 2 concurrent assignments`() = runTest {
        val player = gameStateManager.playerState.value
        assertEquals(2, player.nestCustomization.getMaxCompanionAssignments())
    }
    
    @Test
    fun `COMPANION_ASSIGNMENT_BOARD upgrade to Tier 2 allows 4 concurrent assignments`() = runTest {
        // Arrange: Give player resources
        prepareResourcesForTier2()
        
        // Act: Upgrade to Tier 2
        val result = nestManager.upgradeFunctionalTier(FunctionalUpgradeType.COMPANION_ASSIGNMENT_BOARD)
        
        // Assert: Success and 4 assignments
        assertTrue(result is UpgradeTierResult.Success)
        assertEquals(4, gameStateManager.playerState.value.nestCustomization.getMaxCompanionAssignments())
    }
    
    @Test
    fun `COMPANION_ASSIGNMENT_BOARD upgrade to Tier 3 allows 6 concurrent assignments`() = runTest {
        // Arrange: Upgrade to Tier 2 first
        prepareResourcesForTier2()
        nestManager.upgradeFunctionalTier(FunctionalUpgradeType.COMPANION_ASSIGNMENT_BOARD)
        
        // Upgrade to Tier 3
        prepareResourcesForTier3(FunctionalUpgradeType.COMPANION_ASSIGNMENT_BOARD)
        
        // Act
        val result = nestManager.upgradeFunctionalTier(FunctionalUpgradeType.COMPANION_ASSIGNMENT_BOARD)
        
        // Assert: Success and 6 assignments
        assertTrue(result is UpgradeTierResult.Success)
        assertEquals(6, gameStateManager.playerState.value.nestCustomization.getMaxCompanionAssignments())
    }
    
    @Test
    fun `COMPANION_ASSIGNMENT_BOARD inactive returns 0 assignments`() = runTest {
        // Arrange: Create player with inactive board
        val player = createPlayerWithStation(FunctionalUpgradeType.COMPANION_ASSIGNMENT_BOARD, isActive = false)
        gameStateManager = GameStateManager(player, null) { currentTime }
        
        // Assert: 0 assignments when not active
        assertEquals(0, gameStateManager.playerState.value.nestCustomization.getMaxCompanionAssignments())
    }
    
    // ========================================
    // LORE_ARCHIVE Tests
    // ========================================
    
    @Test
    fun `LORE_ARCHIVE Tier 1 stores 20 entries`() = runTest {
        // Arrange: Create player with LORE_ARCHIVE Tier 1
        val player = createPlayerWithStation(FunctionalUpgradeType.LORE_ARCHIVE)
        gameStateManager = GameStateManager(player, null) { currentTime }
        
        // Assert: 20 entries
        assertEquals(20, gameStateManager.playerState.value.nestCustomization.getMaxLoreArchiveEntries())
    }
    
    @Test
    fun `LORE_ARCHIVE upgrade to Tier 2 stores 50 entries`() = runTest {
        // Arrange: Create player with LORE_ARCHIVE Tier 1
        val player = createPlayerWithStation(FunctionalUpgradeType.LORE_ARCHIVE)
        gameStateManager = GameStateManager(player, null) { currentTime }
        glimmerWalletManager = GlimmerWalletManager(gameStateManager, { currentTime })
        nestManager = NestCustomizationManager(
            gameStateManager,
            glimmerWalletManager,
            { currentTime },
            emptyList(),
            upgradeCatalog
        )
        
        // Give resources
        prepareLoreArchiveTier2Resources()
        
        // Act
        val result = nestManager.upgradeFunctionalTier(FunctionalUpgradeType.LORE_ARCHIVE)
        
        // Assert: Success and 50 entries
        assertTrue(result is UpgradeTierResult.Success)
        assertEquals(50, gameStateManager.playerState.value.nestCustomization.getMaxLoreArchiveEntries())
    }
    
    @Test
    fun `LORE_ARCHIVE upgrade to Tier 3 stores 100 entries`() = runTest {
        // Arrange: Create player with LORE_ARCHIVE Tier 1
        val player = createPlayerWithStation(FunctionalUpgradeType.LORE_ARCHIVE)
        gameStateManager = GameStateManager(player, null) { currentTime }
        glimmerWalletManager = GlimmerWalletManager(gameStateManager, { currentTime })
        nestManager = NestCustomizationManager(
            gameStateManager,
            glimmerWalletManager,
            { currentTime },
            emptyList(),
            upgradeCatalog
        )
        
        // Upgrade to Tier 2
        prepareLoreArchiveTier2Resources()
        nestManager.upgradeFunctionalTier(FunctionalUpgradeType.LORE_ARCHIVE)
        
        // Upgrade to Tier 3
        prepareLoreArchiveTier3Resources()
        
        // Act
        val result = nestManager.upgradeFunctionalTier(FunctionalUpgradeType.LORE_ARCHIVE)
        
        // Assert: Success and 100 entries
        assertTrue(result is UpgradeTierResult.Success)
        assertEquals(100, gameStateManager.playerState.value.nestCustomization.getMaxLoreArchiveEntries())
    }
    
    @Test
    fun `LORE_ARCHIVE inactive returns 0 entries`() = runTest {
        // Arrange: Create player with inactive archive
        val player = createPlayerWithStation(FunctionalUpgradeType.LORE_ARCHIVE, isActive = false)
        gameStateManager = GameStateManager(player, null) { currentTime }
        
        // Assert: 0 entries when not active
        assertEquals(0, gameStateManager.playerState.value.nestCustomization.getMaxLoreArchiveEntries())
    }
    
    // ========================================
    // AI_DIRECTOR_CONSOLE Tests
    // ========================================
    
    @Test
    fun `AI_DIRECTOR_CONSOLE Tier 1 tracks 10 events`() = runTest {
        // Arrange: Create player with AI_DIRECTOR_CONSOLE Tier 1
        val player = createPlayerWithStation(FunctionalUpgradeType.AI_DIRECTOR_CONSOLE)
        gameStateManager = GameStateManager(player, null) { currentTime }
        
        // Assert: 10 events
        assertEquals(10, gameStateManager.playerState.value.nestCustomization.getAiDirectorHistoryDepth())
    }
    
    @Test
    fun `AI_DIRECTOR_CONSOLE upgrade to Tier 2 tracks 25 events`() = runTest {
        // Arrange: Create player with AI_DIRECTOR_CONSOLE Tier 1
        val player = createPlayerWithStation(FunctionalUpgradeType.AI_DIRECTOR_CONSOLE)
        gameStateManager = GameStateManager(player, null) { currentTime }
        glimmerWalletManager = GlimmerWalletManager(gameStateManager, { currentTime })
        nestManager = NestCustomizationManager(
            gameStateManager,
            glimmerWalletManager,
            { currentTime },
            emptyList(),
            upgradeCatalog
        )
        
        // Give resources
        prepareAiDirectorTier2Resources()
        
        // Act
        val result = nestManager.upgradeFunctionalTier(FunctionalUpgradeType.AI_DIRECTOR_CONSOLE)
        
        // Assert: Success and 25 events
        assertTrue(result is UpgradeTierResult.Success)
        assertEquals(25, gameStateManager.playerState.value.nestCustomization.getAiDirectorHistoryDepth())
    }
    
    @Test
    fun `AI_DIRECTOR_CONSOLE upgrade to Tier 3 tracks 50 events`() = runTest {
        // Arrange: Create player with AI_DIRECTOR_CONSOLE Tier 1
        val player = createPlayerWithStation(FunctionalUpgradeType.AI_DIRECTOR_CONSOLE)
        gameStateManager = GameStateManager(player, null) { currentTime }
        glimmerWalletManager = GlimmerWalletManager(gameStateManager, { currentTime })
        nestManager = NestCustomizationManager(
            gameStateManager,
            glimmerWalletManager,
            { currentTime },
            emptyList(),
            upgradeCatalog
        )
        
        // Upgrade to Tier 2
        prepareAiDirectorTier2Resources()
        nestManager.upgradeFunctionalTier(FunctionalUpgradeType.AI_DIRECTOR_CONSOLE)
        
        // Upgrade to Tier 3
        prepareAiDirectorTier3Resources()
        
        // Act
        val result = nestManager.upgradeFunctionalTier(FunctionalUpgradeType.AI_DIRECTOR_CONSOLE)
        
        // Assert: Success and 50 events
        assertTrue(result is UpgradeTierResult.Success)
        assertEquals(50, gameStateManager.playerState.value.nestCustomization.getAiDirectorHistoryDepth())
    }
    
    @Test
    fun `AI_DIRECTOR_CONSOLE inactive returns 0 events`() = runTest {
        // Arrange: Create player with inactive console
        val player = createPlayerWithStation(FunctionalUpgradeType.AI_DIRECTOR_CONSOLE, isActive = false)
        gameStateManager = GameStateManager(player, null) { currentTime }
        
        // Assert: 0 events when not active
        assertEquals(0, gameStateManager.playerState.value.nestCustomization.getAiDirectorHistoryDepth())
    }
    
    // ========================================
    // Catalog Tests
    // ========================================
    
    @Test
    fun `all 3 new stations have complete tier definitions in catalog`() {
        // COMPANION_ASSIGNMENT_BOARD
        val board = upgradeCatalog.getUpgrade(FunctionalUpgradeType.COMPANION_ASSIGNMENT_BOARD)
        assertNotNull(board)
        assertEquals(3, board.tiers.size)
        assertNotNull(board.tiers[UpgradeTier.TIER_1])
        assertNotNull(board.tiers[UpgradeTier.TIER_2])
        assertNotNull(board.tiers[UpgradeTier.TIER_3])
        
        // LORE_ARCHIVE
        val archive = upgradeCatalog.getUpgrade(FunctionalUpgradeType.LORE_ARCHIVE)
        assertNotNull(archive)
        assertEquals(3, archive.tiers.size)
        assertNotNull(archive.tiers[UpgradeTier.TIER_1])
        assertNotNull(archive.tiers[UpgradeTier.TIER_2])
        assertNotNull(archive.tiers[UpgradeTier.TIER_3])
        
        // AI_DIRECTOR_CONSOLE
        val console = upgradeCatalog.getUpgrade(FunctionalUpgradeType.AI_DIRECTOR_CONSOLE)
        assertNotNull(console)
        assertEquals(3, console.tiers.size)
        assertNotNull(console.tiers[UpgradeTier.TIER_1])
        assertNotNull(console.tiers[UpgradeTier.TIER_2])
        assertNotNull(console.tiers[UpgradeTier.TIER_3])
    }
    
    @Test
    fun `COMPANION_ASSIGNMENT_BOARD choice tag logged on upgrade`() = runTest {
        prepareResourcesForTier2()
        nestManager.upgradeFunctionalTier(FunctionalUpgradeType.COMPANION_ASSIGNMENT_BOARD)
        
        val choices = gameStateManager.playerState.value.choiceLog.entries
        assertTrue(choices.any { it.tag.value == "nest_upgrade_tier_COMPANION_ASSIGNMENT_BOARD_TIER_2" })
    }
    
    @Test
    fun `LORE_ARCHIVE choice tag logged on upgrade`() = runTest {
        val player = createPlayerWithStation(FunctionalUpgradeType.LORE_ARCHIVE)
        gameStateManager = GameStateManager(player, null) { currentTime }
        glimmerWalletManager = GlimmerWalletManager(gameStateManager, { currentTime })
        nestManager = NestCustomizationManager(
            gameStateManager,
            glimmerWalletManager,
            { currentTime },
            emptyList(),
            upgradeCatalog
        )
        
        prepareLoreArchiveTier2Resources()
        nestManager.upgradeFunctionalTier(FunctionalUpgradeType.LORE_ARCHIVE)
        
        val choices = gameStateManager.playerState.value.choiceLog.entries
        assertTrue(choices.any { it.tag.value == "nest_upgrade_tier_LORE_ARCHIVE_TIER_2" })
    }
    
    @Test
    fun `AI_DIRECTOR_CONSOLE choice tag logged on upgrade`() = runTest {
        val player = createPlayerWithStation(FunctionalUpgradeType.AI_DIRECTOR_CONSOLE)
        gameStateManager = GameStateManager(player, null) { currentTime }
        glimmerWalletManager = GlimmerWalletManager(gameStateManager, { currentTime })
        nestManager = NestCustomizationManager(
            gameStateManager,
            glimmerWalletManager,
            { currentTime },
            emptyList(),
            upgradeCatalog
        )
        
        prepareAiDirectorTier2Resources()
        nestManager.upgradeFunctionalTier(FunctionalUpgradeType.AI_DIRECTOR_CONSOLE)
        
        val choices = gameStateManager.playerState.value.choiceLog.entries
        assertTrue(choices.any { it.tag.value == "nest_upgrade_tier_AI_DIRECTOR_CONSOLE_TIER_2" })
    }
    
    // ========================================
    // Helper Methods
    // ========================================
    
    private fun createPlayerWithStation(
        stationType: FunctionalUpgradeType,
        isActive: Boolean = true
    ): Player {
        val player = testPlayer()
        val upgrade = FunctionalUpgrade(
            type = stationType,
            cosmeticItemId = CosmeticItemId("station_${stationType.name.lowercase()}"),
            currentTier = UpgradeTier.TIER_1,
            isActive = isActive
        )
        return player.copy(
            level = 10,
            nestCustomization = player.nestCustomization.copy(
                functionalUpgrades = mapOf(stationType to upgrade)
            ),
            seedInventory = SeedInventory(storedSeeds = 20000, maxCapacity = 50000),
            glimmerWallet = player.glimmerWallet.add(
                amount = 50000,
                type = TransactionType.DEBUG_GRANT,
                timestampMillis = 1000L,
                transactionId = TransactionId("test_grant")
            ),
            craftingInventory = CraftingInventory(
                ingredients = mapOf(
                    IngredientId("ingredient_iron_ingot") to 50,
                    IngredientId("ingredient_ethereal_wisp") to 50,
                    IngredientId("ingredient_nature_essence") to 50
                )
            )
        )
    }
    
    private suspend fun prepareResourcesForTier2() {
        gameStateManager.updateSeedInventory { it.copy(storedSeeds = 10000) }
        glimmerWalletManager.grantGlimmer(10000, "test")
        gameStateManager.updateCraftingInventory { inventory ->
            inventory.copy(
                ingredients = mapOf(
                    IngredientId("ingredient_iron_ingot") to 20,
                    IngredientId("ingredient_ethereal_wisp") to 15,
                    IngredientId("ingredient_runic_stone") to 10
                )
            )
        }
        gameStateManager.updatePlayer { it.copy(level = 10) }
    }
    
    private suspend fun prepareResourcesForTier3(stationType: FunctionalUpgradeType) {
        gameStateManager.updateSeedInventory { it.copy(storedSeeds = 20000) }
        glimmerWalletManager.grantGlimmer(20000, "test")
        gameStateManager.updateCraftingInventory { inventory ->
            inventory.copy(
                ingredients = mapOf(
                    IngredientId("ingredient_mythril_alloy") to 15,
                    IngredientId("ingredient_arcane_catalyst") to 10,
                    IngredientId("ingredient_ancient_magic_essence") to 8
                )
            )
        }
        gameStateManager.updatePlayer { it.copy(level = 10) }
    }
    
    private suspend fun prepareLoreArchiveTier2Resources() {
        gameStateManager.updateSeedInventory { it.copy(storedSeeds = 10000) }
        glimmerWalletManager.grantGlimmer(10000, "test")
        gameStateManager.updateCraftingInventory { inventory ->
            inventory.copy(
                ingredients = mapOf(
                    IngredientId("ingredient_runic_stone") to 15,
                    IngredientId("item_basic_ink") to 25,
                    IngredientId("ingredient_shell_armor_plate") to 10
                )
            )
        }
        gameStateManager.updatePlayer { it.copy(level = 10) }
    }
    
    private suspend fun prepareLoreArchiveTier3Resources() {
        gameStateManager.updateSeedInventory { it.copy(storedSeeds = 20000) }
        glimmerWalletManager.grantGlimmer(20000, "test")
        gameStateManager.updateCraftingInventory { inventory ->
            inventory.copy(
                ingredients = mapOf(
                    IngredientId("ingredient_arcane_catalyst") to 10,
                    IngredientId("ingredient_ancient_magic_essence") to 15,
                    IngredientId("ingredient_shadow_core") to 8
                )
            )
        }
        gameStateManager.updatePlayer { it.copy(level = 10) }
    }
    
    private suspend fun prepareAiDirectorTier2Resources() {
        gameStateManager.updateSeedInventory { it.copy(storedSeeds = 10000) }
        glimmerWalletManager.grantGlimmer(10000, "test")
        gameStateManager.updateCraftingInventory { inventory ->
            inventory.copy(
                ingredients = mapOf(
                    IngredientId("ingredient_mythril_alloy") to 15,
                    IngredientId("ingredient_life_crystal") to 10,
                    IngredientId("ingredient_runic_stone") to 12
                )
            )
        }
        gameStateManager.updatePlayer { it.copy(level = 10) }
    }
    
    private suspend fun prepareAiDirectorTier3Resources() {
        gameStateManager.updateSeedInventory { it.copy(storedSeeds = 20000) }
        glimmerWalletManager.grantGlimmer(20000, "test")
        gameStateManager.updateCraftingInventory { inventory ->
            inventory.copy(
                ingredients = mapOf(
                    IngredientId("ingredient_arcane_catalyst") to 12,
                    IngredientId("ingredient_ancient_magic_essence") to 18,
                    IngredientId("ingredient_legendary_feather") to 6
                )
            )
        }
        gameStateManager.updatePlayer { it.copy(level = 10) }
    }
}
