package com.jalmarquest.core.state.managers

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.catalogs.NestUpgradeTierCatalog
import com.jalmarquest.core.state.monetization.GlimmerWalletManager
import com.jalmarquest.core.state.testutil.testPlayer
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Alpha 2.3: Test suite for nest upgrade tier system.
 * 
 * Validates:
 * - Tier upgrade purchases with seed/Glimmer/ingredient costs
 * - Prerequisite tier validation (must have Tier 1 before Tier 2)
 * - Level requirements
 * - Bonus scaling (Tier 1 → Tier 2 → Tier 3)
 * - Affordability checking
 */
class NestUpgradeTierTest {
    private lateinit var gameStateManager: GameStateManager
    private lateinit var glimmerWalletManager: GlimmerWalletManager
    private lateinit var nestManager: NestCustomizationManager
    private lateinit var upgradeCatalog: NestUpgradeTierCatalog
    private var currentTime = 0L
    
    @BeforeTest
    fun setup() {
        currentTime = 1000L
        gameStateManager = GameStateManager(
            initialPlayer = createPlayerWithUpgrade(),
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
    
    // Test 1: Happy path - upgrade from Tier 1 to Tier 2
    @Test
    fun `upgrade from Tier 1 to Tier 2 succeeds with sufficient resources`() = runTest {
        // Arrange: Player has SEED_SILO at Tier 1 with enough resources
        val player = gameStateManager.playerState.value
        assertEquals(UpgradeTier.TIER_1, player.nestCustomization.functionalUpgrades[FunctionalUpgradeType.SEED_SILO]?.currentTier)
        
        // Act: Upgrade to Tier 2
        val result = nestManager.upgradeFunctionalTier(FunctionalUpgradeType.SEED_SILO)
        
        // Assert: Success and tier updated
        assertTrue(result is UpgradeTierResult.Success)
        val successResult = result as UpgradeTierResult.Success
        assertEquals(UpgradeTier.TIER_2, successResult.newTier)
        
        val updatedPlayer = gameStateManager.playerState.value
        assertEquals(UpgradeTier.TIER_2, updatedPlayer.nestCustomization.functionalUpgrades[FunctionalUpgradeType.SEED_SILO]?.currentTier)
        
        // Verify resources were deducted
        assertTrue(updatedPlayer.seedInventory.storedSeeds < 5000) // Had 5000, Tier 2 costs 2000
        assertTrue(updatedPlayer.glimmerWallet.balance < 10000) // Had 10000, Tier 2 costs 2500
    }
    
    // Test 2: Bonus scaling - verify Tier 2 provides double bonus
    @Test
    fun `Tier 2 upgrade doubles seed storage bonus from 50% to 100%`() = runTest {
        // Arrange: Tier 1 gives +50% bonus
        assertEquals(0.5f, gameStateManager.playerState.value.nestCustomization.getSeedStorageBonus())
        
        // Act: Upgrade to Tier 2
        nestManager.upgradeFunctionalTier(FunctionalUpgradeType.SEED_SILO)
        
        // Assert: Tier 2 gives +100% bonus
        assertEquals(1.0f, gameStateManager.playerState.value.nestCustomization.getSeedStorageBonus())
    }
    
    // Test 3: Upgrade from Tier 2 to Tier 3
    @Test
    fun `upgrade from Tier 2 to Tier 3 succeeds with sufficient resources`() = runTest {
        // Arrange: Upgrade to Tier 2 first
        nestManager.upgradeFunctionalTier(FunctionalUpgradeType.SEED_SILO)
        
        // Give player more resources for Tier 3
        gameStateManager.updateSeedInventory { it.copy(storedSeeds = 10000) }
        glimmerWalletManager.grantGlimmer(15000, "test")
        gameStateManager.updateCraftingInventory { inventory ->
            inventory.copy(
                ingredients = mapOf(
                    IngredientId("ingredient_mythril_alloy") to 10,
                    IngredientId("ingredient_life_crystal") to 5,
                    IngredientId("ingredient_ancient_scales") to 8
                )
            )
        }
        gameStateManager.updatePlayer { it.copy(level = 10) }
        
        // Act: Upgrade to Tier 3
        val result = nestManager.upgradeFunctionalTier(FunctionalUpgradeType.SEED_SILO)
        
        // Assert: Success and tier updated
        assertTrue(result is UpgradeTierResult.Success)
        assertEquals(UpgradeTier.TIER_3, (result as UpgradeTierResult.Success).newTier)
        assertEquals(UpgradeTier.TIER_3, gameStateManager.playerState.value.nestCustomization.functionalUpgrades[FunctionalUpgradeType.SEED_SILO]?.currentTier)
        
        // Tier 3 gives +150% bonus
        assertEquals(1.5f, gameStateManager.playerState.value.nestCustomization.getSeedStorageBonus())
    }
    
    // Test 4: Cannot upgrade beyond Tier 3
    @Test
    fun `cannot upgrade beyond Tier 3`() = runTest {
        // Arrange: Upgrade to Tier 3
        nestManager.upgradeFunctionalTier(FunctionalUpgradeType.SEED_SILO)
        
        gameStateManager.updateSeedInventory { it.copy(storedSeeds = 10000) }
        glimmerWalletManager.grantGlimmer(15000, "test")
        gameStateManager.updateCraftingInventory { inventory ->
            inventory.copy(
                ingredients = mapOf(
                    IngredientId("ingredient_mythril_alloy") to 10,
                    IngredientId("ingredient_life_crystal") to 5,
                    IngredientId("ingredient_ancient_scales") to 8
                )
            )
        }
        gameStateManager.updatePlayer { it.copy(level = 10) }
        
        nestManager.upgradeFunctionalTier(FunctionalUpgradeType.SEED_SILO)
        
        // Act: Try to upgrade beyond Tier 3
        val result = nestManager.upgradeFunctionalTier(FunctionalUpgradeType.SEED_SILO)
        
        // Assert: Fails with AlreadyMaxTier
        assertTrue(result is UpgradeTierResult.AlreadyMaxTier)
    }
    
    // Test 5: Insufficient seeds
    @Test
    fun `upgrade fails with insufficient seeds`() = runTest {
        // Arrange: Remove all seeds
        gameStateManager.updateSeedInventory { it.copy(storedSeeds = 100) }
        
        // Act: Try to upgrade (Tier 2 costs 2000 seeds)
        val result = nestManager.upgradeFunctionalTier(FunctionalUpgradeType.SEED_SILO)
        
        // Assert: Fails with InsufficientSeeds
        assertTrue(result is UpgradeTierResult.InsufficientSeeds)
        val failure = result as UpgradeTierResult.InsufficientSeeds
        assertEquals(2000, failure.required)
        assertEquals(100, failure.available)
    }
    
    // Test 6: Insufficient Glimmer
    @Test
    fun `upgrade fails with insufficient Glimmer`() = runTest {
        // Arrange: Give seeds but no Glimmer
        gameStateManager.updateSeedInventory { it.copy(storedSeeds = 5000) }
        glimmerWalletManager.spendGlimmer(9999, TransactionType.DEBUG_GRANT, "drain") // Leave 1 Glimmer
        
        // Act: Try to upgrade (Tier 2 costs 2500 Glimmer)
        val result = nestManager.upgradeFunctionalTier(FunctionalUpgradeType.SEED_SILO)
        
        // Assert: Fails with InsufficientGlimmer
        assertTrue(result is UpgradeTierResult.InsufficientGlimmer)
    }
    
    // Test 7: Insufficient ingredients
    @Test
    fun `upgrade fails with insufficient ingredients`() = runTest {
        // Arrange: Give seeds and Glimmer but no ingredients
        gameStateManager.updateSeedInventory { it.copy(storedSeeds = 5000) }
        gameStateManager.updateCraftingInventory { it.copy(ingredients = emptyMap()) }
        
        // Act: Try to upgrade (Tier 2 requires specific ingredients)
        val result = nestManager.upgradeFunctionalTier(FunctionalUpgradeType.SEED_SILO)
        
        // Assert: Fails with InsufficientIngredients
        assertTrue(result is UpgradeTierResult.InsufficientIngredients)
    }
    
    // Test 8: Level requirement not met
    @Test
    fun `upgrade fails with insufficient player level`() = runTest {
        // Arrange: Set player to level 1 (Tier 2 requires level 5)
        gameStateManager.updatePlayer { it.copy(level = 1) }
        gameStateManager.updateSeedInventory { it.copy(storedSeeds = 10000) }
        glimmerWalletManager.grantGlimmer(10000, "test")
        gameStateManager.updateCraftingInventory { inventory ->
            inventory.copy(
                ingredients = mapOf(
                    IngredientId("ingredient_iron_ingot") to 15,
                    IngredientId("ingredient_shell_armor_plate") to 10,
                    IngredientId("ingredient_nature_essence") to 5
                )
            )
        }
        
        // Act: Try to upgrade
        val result = nestManager.upgradeFunctionalTier(FunctionalUpgradeType.SEED_SILO)
        
        // Assert: Fails with LevelTooLow
        assertTrue(result is UpgradeTierResult.LevelTooLow)
        assertEquals(5, (result as UpgradeTierResult.LevelTooLow).requiredLevel)
    }
    
    // Test 9: Upgrade not owned
    @Test
    fun `cannot upgrade upgrade that is not owned`() = runTest {
        // Arrange: Remove the upgrade
        gameStateManager.updateNestCustomization { state ->
            state.copy(functionalUpgrades = emptyMap())
        }
        
        // Act: Try to upgrade
        val result = nestManager.upgradeFunctionalTier(FunctionalUpgradeType.SEED_SILO)
        
        // Assert: Fails with NotOwned
        assertTrue(result is UpgradeTierResult.NotOwned)
    }
    
    // Test 10: Upgrade not activated
    @Test
    fun `cannot upgrade inactive upgrade`() = runTest {
        // Arrange: Deactivate the upgrade
        nestManager.deactivateFunctionalUpgrade(FunctionalUpgradeType.SEED_SILO)
        
        // Act: Try to upgrade
        val result = nestManager.upgradeFunctionalTier(FunctionalUpgradeType.SEED_SILO)
        
        // Assert: Fails with NotActivated
        assertTrue(result is UpgradeTierResult.NotActivated)
    }
    
    // Test 11: Affordability checking (can afford)
    @Test
    fun `affordability check returns true when player has all resources`() = runTest {
        // Arrange: Player starts with sufficient resources
        val affordability = nestManager.canAffordUpgradeTier(FunctionalUpgradeType.SEED_SILO)
        
        // Assert: Can afford
        assertTrue(affordability.canAfford)
        assertEquals(0, affordability.missingSeeds)
        assertEquals(0, affordability.missingGlimmer)
        assertTrue(affordability.missingIngredients.isEmpty())
    }
    
    // Test 12: Affordability checking (cannot afford)
    @Test
    fun `affordability check returns false with missing resource details`() = runTest {
        // Arrange: Remove all resources
        gameStateManager.updateSeedInventory { it.copy(storedSeeds = 0) }
        glimmerWalletManager.spendGlimmer(10000, TransactionType.DEBUG_GRANT, "drain")
        gameStateManager.updateCraftingInventory { it.copy(ingredients = emptyMap()) }
        
        // Act: Check affordability
        val affordability = nestManager.canAffordUpgradeTier(FunctionalUpgradeType.SEED_SILO)
        
        // Assert: Cannot afford with detailed shortage info
        assertFalse(affordability.canAfford)
        assertEquals(2000, affordability.missingSeeds) // Tier 2 costs 2000 seeds
        assertEquals(2500, affordability.missingGlimmer) // Tier 2 costs 2500 Glimmer
        assertTrue(affordability.missingIngredients.isNotEmpty()) // Missing 3 ingredient types
    }
    
    // Test 13: All 6 functional upgrades have 3 tiers defined
    @Test
    fun `all 6 functional upgrades have complete tier definitions`() {
        val upgradeTypes = listOf(
            FunctionalUpgradeType.SHINY_DISPLAY,
            FunctionalUpgradeType.SEED_SILO,
            FunctionalUpgradeType.SMALL_LIBRARY,
            FunctionalUpgradeType.PERSONAL_ALCHEMY_STATION,
            FunctionalUpgradeType.SMALL_WORKBENCH,
            FunctionalUpgradeType.COZY_PERCH
        )
        
        for (type in upgradeTypes) {
            val upgrade = upgradeCatalog.getUpgrade(type)
            assertNotNull(upgrade, "Upgrade definition missing for $type")
            
            // Check all 3 tiers exist
            assertNotNull(upgradeCatalog.getTierDefinition(type, UpgradeTier.TIER_1), "$type missing Tier 1")
            assertNotNull(upgradeCatalog.getTierDefinition(type, UpgradeTier.TIER_2), "$type missing Tier 2")
            assertNotNull(upgradeCatalog.getTierDefinition(type, UpgradeTier.TIER_3), "$type missing Tier 3")
        }
    }
    
    // Test 14: Tier 2 prerequisites check (must have Tier 1)
    @Test
    fun `Tier 2 requires Tier 1 as prerequisite`() {
        val tier2Def = upgradeCatalog.getTierDefinition(FunctionalUpgradeType.SEED_SILO, UpgradeTier.TIER_2)
        assertNotNull(tier2Def)
        assertEquals(UpgradeTier.TIER_1, tier2Def.prerequisiteTier)
    }
    
    // Test 15: Tier 3 prerequisites check (must have Tier 2)
    @Test
    fun `Tier 3 requires Tier 2 as prerequisite`() {
        val tier3Def = upgradeCatalog.getTierDefinition(FunctionalUpgradeType.SEED_SILO, UpgradeTier.TIER_3)
        assertNotNull(tier3Def)
        assertEquals(UpgradeTier.TIER_2, tier3Def.prerequisiteTier)
    }
    
    // Test 16: Choice logging
    @Test
    fun `tier upgrade logs choice tag`() = runTest {
        // Act: Upgrade to Tier 2
        nestManager.upgradeFunctionalTier(FunctionalUpgradeType.SEED_SILO)
        
        // Assert: Choice was logged
        val choices = gameStateManager.playerState.value.choiceLog.entries
        assertTrue(choices.any { it.tag.value == "nest_upgrade_tier_SEED_SILO_TIER_2" })
    }
    
    // Test 17: Test LIBRARY upgrade (thought slot bonus scaling)
    @Test
    fun `LIBRARY upgrade scales thought slots 2-4-6`() = runTest {
        // Arrange: Create player with LIBRARY at Tier 1
        val player = createPlayerWithLibraryUpgrade()
        gameStateManager = GameStateManager(
            initialPlayer = player,
            accountManager = null,
            timestampProvider = { currentTime }
        )
        glimmerWalletManager = GlimmerWalletManager(
            gameStateManager = gameStateManager,
            timestampProvider = { currentTime }
        )
        nestManager = NestCustomizationManager(
            gameStateManager,
            glimmerWalletManager,
            { currentTime },
            emptyList(),
            upgradeCatalog
        )
        
        // Tier 1: +2 slots
        assertEquals(2, gameStateManager.playerState.value.nestCustomization.getExtraThoughtSlots())
        
        // Upgrade to Tier 2: +4 slots
        gameStateManager.updateSeedInventory { it.copy(storedSeeds = 10000) }
        glimmerWalletManager.grantGlimmer(15000, "test")
        gameStateManager.updateCraftingInventory { inventory ->
            inventory.copy(
                ingredients = mapOf(
                    IngredientId("ingredient_runic_stone") to 10,
                    IngredientId("item_basic_ink") to 20,
                    IngredientId("ingredient_ethereal_wisp") to 10
                )
            )
        }
        gameStateManager.updatePlayer { it.copy(level = 10) }
        
        nestManager.upgradeFunctionalTier(FunctionalUpgradeType.SMALL_LIBRARY)
        assertEquals(4, gameStateManager.playerState.value.nestCustomization.getExtraThoughtSlots())
        
        // Upgrade to Tier 3: +6 slots
        gameStateManager.updateSeedInventory { it.copy(storedSeeds = 10000) }
        glimmerWalletManager.grantGlimmer(15000, "test")
        gameStateManager.updateCraftingInventory { inventory ->
            inventory.copy(
                ingredients = mapOf(
                    IngredientId("ingredient_arcane_catalyst") to 5,
                    IngredientId("ingredient_ancient_magic_essence") to 10,
                    IngredientId("ingredient_shadow_core") to 5
                )
            )
        }
        
        nestManager.upgradeFunctionalTier(FunctionalUpgradeType.SMALL_LIBRARY)
        assertEquals(6, gameStateManager.playerState.value.nestCustomization.getExtraThoughtSlots())
    }
    
    // Helper: Create test player with SEED_SILO at Tier 1
    private fun createPlayerWithUpgrade(): Player {
        val basePlayer = testPlayer()
        val upgrade = FunctionalUpgrade(
            type = FunctionalUpgradeType.SEED_SILO,
            cosmeticItemId = CosmeticItemId("cosmetic_seed_silo"),
            currentTier = UpgradeTier.TIER_1,
            isActive = true
        )
        
        return basePlayer.copy(
            level = 10, // High enough for all upgrades
            seedInventory = basePlayer.seedInventory.copy(storedSeeds = 5000),
            glimmerWallet = basePlayer.glimmerWallet.copy(balance = 10000),
            craftingInventory = CraftingInventory(
                ingredients = mapOf(
                    IngredientId("ingredient_iron_ingot") to 15,
                    IngredientId("ingredient_shell_armor_plate") to 10,
                    IngredientId("ingredient_nature_essence") to 5
                ),
                knownRecipes = emptySet()
            ),
            nestCustomization = basePlayer.nestCustomization.copy(
                functionalUpgrades = mapOf(FunctionalUpgradeType.SEED_SILO to upgrade)
            )
        )
    }
    
    // Helper: Create test player with SMALL_LIBRARY at Tier 1
    private fun createPlayerWithLibraryUpgrade(): Player {
        val basePlayer = testPlayer()
        val upgrade = FunctionalUpgrade(
            type = FunctionalUpgradeType.SMALL_LIBRARY,
            cosmeticItemId = CosmeticItemId("cosmetic_library"),
            currentTier = UpgradeTier.TIER_1,
            isActive = true
        )
        
        return basePlayer.copy(
            level = 10,
            seedInventory = basePlayer.seedInventory.copy(storedSeeds = 10000),
            glimmerWallet = basePlayer.glimmerWallet.copy(balance = 15000),
            nestCustomization = basePlayer.nestCustomization.copy(
                functionalUpgrades = mapOf(FunctionalUpgradeType.SMALL_LIBRARY to upgrade)
            )
        )
    }
}
