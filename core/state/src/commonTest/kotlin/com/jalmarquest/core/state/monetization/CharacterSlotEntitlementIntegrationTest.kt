package com.jalmarquest.core.state.monetization

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.account.AccountManager
import kotlin.test.*

/**
 * Integration test for character slot entitlement flow.
 * Tests purchase → unlock → create character → restore flow.
 */
class CharacterSlotEntitlementIntegrationTest {
    
    private lateinit var gameStateManager: GameStateManager
    private lateinit var entitlementManager: EntitlementManager
    private lateinit var glimmerWalletManager: GlimmerWalletManager
    private lateinit var accountManager: AccountManager
    private var currentTime = 1000L
    
    @BeforeTest
    fun setup() {
        val testPlayer = Player(id = "test_player", name = "Jalmar")
        val testAccount = CharacterAccount(accountId = "test_account")
        
        gameStateManager = GameStateManager(testPlayer) { currentTime }
        entitlementManager = EntitlementManager(gameStateManager)
        glimmerWalletManager = GlimmerWalletManager(
            gameStateManager = gameStateManager,
            timestampProvider = { currentTime },
            entitlementManager = entitlementManager
        )
        accountManager = AccountManager(
            initialAccount = testAccount,
            timestampProvider = { currentTime }
        )
    }
    
    @Test
    fun testPurchaseSlot2UnlocksSlot() = kotlinx.coroutines.test.runTest {
        // Initially slot 1 is free, slot 2 is locked
        assertFalse(entitlementManager.hasCharacterSlot(CharacterSlotEntitlement.SLOT_2))
        assertEquals(1, entitlementManager.getTotalSlots())
        assertTrue(entitlementManager.isSlotUnlocked(1))
        assertFalse(entitlementManager.isSlotUnlocked(2))
        
        // Purchase slot 2
        val product = IapProductCatalog.CHARACTER_SLOT_2
        val result = glimmerWalletManager.purchaseGlimmer(
            product = product,
            receiptData = "receipt_slot2",
            transactionId = "txn_slot2_123"
        )
        
        // Verify purchase succeeded
        assertTrue(result is PurchaseResult.Success)
        
        // Verify slot 2 is now unlocked
        assertTrue(entitlementManager.hasCharacterSlot(CharacterSlotEntitlement.SLOT_2))
        assertEquals(2, entitlementManager.getTotalSlots())
        assertTrue(entitlementManager.isSlotUnlocked(2))
        
        // Verify entitlement was logged
        val player = gameStateManager.playerState.value
        assertTrue(player.entitlements.hasSlot(CharacterSlotEntitlement.SLOT_2))
        assertTrue(player.choiceLog.entries.any { it.tag.value == "character_slot_purchase_slot_2" })
    }
    
    @Test
    fun testCannotPurchaseSameSlotTwice() = kotlinx.coroutines.test.runTest {
        // Purchase slot 2 first time
        val product = IapProductCatalog.CHARACTER_SLOT_2
        glimmerWalletManager.purchaseGlimmer(
            product = product,
            receiptData = "receipt_1",
            transactionId = "txn_1"
        )
        
        assertTrue(entitlementManager.hasCharacterSlot(CharacterSlotEntitlement.SLOT_2))
        
        // Try to purchase again with different transaction
        val grantResult = entitlementManager.grantCharacterSlot(
            slot = CharacterSlotEntitlement.SLOT_2,
            transactionId = TransactionId("txn_duplicate")
        )
        
        // Should return AlreadyOwned
        assertTrue(grantResult is EntitlementGrantResult.AlreadyOwned)
    }
    
    @Test
    fun testCreateCharacterInPurchasedSlot() = kotlinx.coroutines.test.runTest {
        // Initially can only create 1 character (slot 1 is free)
        val entitlements = gameStateManager.playerState.value.entitlements
        val account1 = accountManager.accountState.value
        assertTrue(account1.canCreateSlot(entitlements))
        
        // Create character in slot 1
        val slot1Id = accountManager.createCharacter(
            characterName = "Jalmar Jr",
            archetype = ArchetypeType.SCHOLAR,
            entitlements = entitlements
        )
        assertNotNull(slot1Id)
        
        // Now cannot create another character (no more free slots)
        val account2 = accountManager.accountState.value
        assertFalse(account2.canCreateSlot(entitlements))
        
        // Purchase slot 2
        glimmerWalletManager.purchaseGlimmer(
            product = IapProductCatalog.CHARACTER_SLOT_2,
            receiptData = "receipt_slot2",
            transactionId = "txn_slot2"
        )
        
        // Now can create character in slot 2
        val updatedEntitlements = gameStateManager.playerState.value.entitlements
        val account3 = accountManager.accountState.value
        assertTrue(account3.canCreateSlot(updatedEntitlements))
        
        // Create character in slot 2
        val slot2Id = accountManager.createCharacter(
            characterName = "Jalmar III",
            archetype = ArchetypeType.COLLECTOR,
            entitlements = updatedEntitlements
        )
        assertNotNull(slot2Id)
        
        // Verify 2 characters exist
        val finalAccount = accountManager.accountState.value
        assertEquals(2, finalAccount.getActiveSlots().size)
    }
    
    @Test
    fun testRestorePurchasesFromReceipts() = kotlinx.coroutines.test.runTest {
        // Simulate fresh install - no entitlements
        val freshPlayer = Player(id = "test_player_2", name = "Jalmar")
        val freshGameStateManager = GameStateManager(freshPlayer) { currentTime }
        val freshEntitlementManager = EntitlementManager(freshGameStateManager)
        
        // Initially no purchased slots
        assertFalse(freshEntitlementManager.hasCharacterSlot(CharacterSlotEntitlement.SLOT_2))
        assertFalse(freshEntitlementManager.hasCharacterSlot(CharacterSlotEntitlement.SLOT_3))
        
        // Simulate restored receipts from platform (user previously purchased slots 2 and 3)
        val receipts = listOf(
            ReceiptData(
                productId = IapProductCatalog.CHARACTER_SLOT_2.id,
                transactionId = TransactionId("restored_txn_slot2"),
                purchaseTime = currentTime - 86400000 // 1 day ago
            ),
            ReceiptData(
                productId = IapProductCatalog.CHARACTER_SLOT_3.id,
                transactionId = TransactionId("restored_txn_slot3"),
                purchaseTime = currentTime - 43200000 // 12 hours ago
            )
        )
        
        // Restore purchases
        val result = freshEntitlementManager.restoreFromReceipts(receipts)
        
        // Verify restore succeeded
        assertTrue(result is RestoreResult.Success)
        assertEquals(2, (result as RestoreResult.Success).restoredSlots.size)
        assertTrue(result.restoredSlots.contains(CharacterSlotEntitlement.SLOT_2))
        assertTrue(result.restoredSlots.contains(CharacterSlotEntitlement.SLOT_3))
        
        // Verify entitlements are now active
        assertTrue(freshEntitlementManager.hasCharacterSlot(CharacterSlotEntitlement.SLOT_2))
        assertTrue(freshEntitlementManager.hasCharacterSlot(CharacterSlotEntitlement.SLOT_3))
        assertEquals(3, freshEntitlementManager.getTotalSlots()) // 1 free + 2 restored
    }
    
    @Test
    fun testAllFiveSlots() = kotlinx.coroutines.test.runTest {
        // Purchase all 4 purchasable slots
        val products = listOf(
            IapProductCatalog.CHARACTER_SLOT_2,
            IapProductCatalog.CHARACTER_SLOT_3,
            IapProductCatalog.CHARACTER_SLOT_4,
            IapProductCatalog.CHARACTER_SLOT_5
        )
        
        products.forEachIndexed { index, product ->
            val result = glimmerWalletManager.purchaseGlimmer(
                product = product,
                receiptData = "receipt_slot_${index + 2}",
                transactionId = "txn_slot_${index + 2}"
            )
            assertTrue(result is PurchaseResult.Success)
        }
        
        // Verify all slots unlocked
        assertEquals(5, entitlementManager.getTotalSlots())
        for (slotNumber in 1..5) {
            assertTrue(entitlementManager.isSlotUnlocked(slotNumber))
        }
        
        // Verify all entitlements tracked
        val player = gameStateManager.playerState.value
        assertTrue(player.entitlements.hasSlot(CharacterSlotEntitlement.SLOT_2))
        assertTrue(player.entitlements.hasSlot(CharacterSlotEntitlement.SLOT_3))
        assertTrue(player.entitlements.hasSlot(CharacterSlotEntitlement.SLOT_4))
        assertTrue(player.entitlements.hasSlot(CharacterSlotEntitlement.SLOT_5))
    }
    
    @Test
    fun testGetAvailableSlotsToPurchase() {
        // Initially all 4 purchasable slots available
        val available1 = entitlementManager.getAvailableSlotsToPurchase()
        assertEquals(4, available1.size)
        assertTrue(available1.all { !it.isUnlocked })
        
        // Purchase slot 2
        kotlinx.coroutines.test.runTest {
            glimmerWalletManager.purchaseGlimmer(
                product = IapProductCatalog.CHARACTER_SLOT_2,
                receiptData = "receipt",
                transactionId = "txn"
            )
        }
        
        // Now only 3 available
        val available2 = entitlementManager.getAvailableSlotsToPurchase()
        assertEquals(3, available2.size)
        assertFalse(available2.any { it.slotNumber == 2 })
    }
    
    @Test
    fun testGetAllCharacterSlots() {
        // Get all slots (locked and unlocked)
        val allSlots = entitlementManager.getAllCharacterSlots()
        
        assertEquals(5, allSlots.size)
        
        // Slot 1 should be unlocked with no product
        assertTrue(allSlots[0].isUnlocked)
        assertEquals(1, allSlots[0].slotNumber)
        assertNull(allSlots[0].product)
        
        // Slots 2-5 should be locked with products
        for (i in 1..4) {
            assertFalse(allSlots[i].isUnlocked)
            assertEquals(i + 1, allSlots[i].slotNumber)
            assertNotNull(allSlots[i].product)
        }
    }
    
    @Test
    fun testCharacterSlotPricing() {
        // Verify pricing is correct
        assertEquals(2.99, IapProductCatalog.CHARACTER_SLOT_2.priceUsd)
        assertEquals(4.99, IapProductCatalog.CHARACTER_SLOT_3.priceUsd)
        assertEquals(6.99, IapProductCatalog.CHARACTER_SLOT_4.priceUsd)
        assertEquals(9.99, IapProductCatalog.CHARACTER_SLOT_5.priceUsd)
        
        // Verify all are entitlement-only (no Glimmer)
        assertEquals(0, IapProductCatalog.CHARACTER_SLOT_2.glimmerAmount)
        assertEquals(0, IapProductCatalog.CHARACTER_SLOT_3.glimmerAmount)
        assertEquals(0, IapProductCatalog.CHARACTER_SLOT_4.glimmerAmount)
        assertEquals(0, IapProductCatalog.CHARACTER_SLOT_5.glimmerAmount)
    }
}
