package com.jalmarquest.core.state.monetization

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GlimmerWalletManagerTest {
    
    private fun createTestPlayer() = Player(
        id = "test_player",
        name = "Jalmar"
    )
    
    private var currentTime = 1000L
    private fun testTimestamp() = currentTime
    
    @Test
    fun testPurchaseGlimmerSuccess() {
        val gameStateManager = GameStateManager(createTestPlayer()) { testTimestamp() }
        val walletManager = GlimmerWalletManager(
            gameStateManager = gameStateManager,
            timestampProvider = { testTimestamp() },
            entitlementManager = null
        )
        
        val product = IapProductCatalog.SMALL_PACK
        kotlinx.coroutines.test.runTest {
            val result = walletManager.purchaseGlimmer(
                product = product,
                receiptData = "receipt_123",
                transactionId = "txn_123"
            )
            
            assertTrue(result is PurchaseResult.Success)
            val success = result as PurchaseResult.Success
            assertEquals(500, success.amountAdded)
            assertEquals(500, success.newBalance)
        }
        
        // Verify wallet state
        val wallet = gameStateManager.playerState.value.glimmerWallet
        assertEquals(500, wallet.balance)
        assertEquals(500, wallet.totalEarned)
        assertEquals(0, wallet.totalSpent)
        assertEquals(1, wallet.transactions.size)
        
        // Verify transaction details
        val transaction = wallet.transactions.first()
        assertEquals(TransactionType.IAP_PURCHASE, transaction.type)
        assertEquals(500, transaction.amount)
        assertEquals(500, transaction.balanceAfter)
        assertEquals("receipt_123", transaction.receiptData)
    }
    
    @Test
    fun testPurchaseGlimmerDuplicateTransaction() {
        val gameStateManager = GameStateManager(createTestPlayer()) { testTimestamp() }
        val walletManager = GlimmerWalletManager(gameStateManager = gameStateManager, timestampProvider = { testTimestamp() }, entitlementManager = null)
        
        val product = IapProductCatalog.STARTER_PACK
        
        // First purchase succeeds
        kotlinx.coroutines.test.runTest {
            val result1 = walletManager.purchaseGlimmer(
                product = product,
                receiptData = "receipt_duplicate",
                transactionId = "txn_001"
            )
            assertTrue(result1 is PurchaseResult.Success)
            
            // Second purchase with same receipt fails
            val result2 = walletManager.purchaseGlimmer(
                product = product,
                receiptData = "receipt_duplicate",
                transactionId = "txn_002"
            )
            assertTrue(result2 is PurchaseResult.DuplicateTransaction)
        }
        
        // Balance should only reflect first purchase
        val wallet = gameStateManager.playerState.value.glimmerWallet
        assertEquals(100, wallet.balance)
        assertEquals(1, wallet.transactions.size)
    }
    
    @Test
    fun testSpendGlimmerSuccess() {
        val gameStateManager = GameStateManager(createTestPlayer()) { testTimestamp() }
        val walletManager = GlimmerWalletManager(gameStateManager = gameStateManager, timestampProvider = { testTimestamp() }, entitlementManager = null)
        
        // Purchase first
        kotlinx.coroutines.test.runTest {
            walletManager.purchaseGlimmer(
                product = IapProductCatalog.MEDIUM_PACK,
                receiptData = "receipt_456",
                transactionId = "txn_456"
            )
            
            // Spend some Glimmer
            val spendResult = walletManager.spendGlimmer(
                amount = 200,
                type = TransactionType.SHOP_PURCHASE,
                itemId = "cosmetic_nest_theme_beach"
            )
            
            assertTrue(spendResult is SpendResult.Success)
            val success = spendResult as SpendResult.Success
            assertEquals(200, success.amountSpent)
            assertEquals(1000, success.newBalance) // 1200 - 200
        }
        
        val wallet = gameStateManager.playerState.value.glimmerWallet
        assertEquals(1000, wallet.balance)
        assertEquals(1200, wallet.totalEarned)
        assertEquals(200, wallet.totalSpent)
        assertEquals(2, wallet.transactions.size)
    }
    
    @Test
    fun testSpendGlimmerInsufficientFunds() {
        val gameStateManager = GameStateManager(createTestPlayer()) { testTimestamp() }
        val walletManager = GlimmerWalletManager(gameStateManager = gameStateManager, timestampProvider = { testTimestamp() }, entitlementManager = null)
        
        kotlinx.coroutines.test.runTest {
            // Try to spend without having any Glimmer
            val result = walletManager.spendGlimmer(
                amount = 100,
                type = TransactionType.SHOP_PURCHASE
            )
            
            assertTrue(result is SpendResult.InsufficientFunds)
            val failure = result as SpendResult.InsufficientFunds
            assertEquals(100, failure.required)
            assertEquals(0, failure.available)
        }
        
        // Balance unchanged
        val wallet = gameStateManager.playerState.value.glimmerWallet
        assertEquals(0, wallet.balance)
        assertEquals(0, wallet.transactions.size)
    }
    
    @Test
    fun testSpendGlimmerPartialBalance() {
        val gameStateManager = GameStateManager(createTestPlayer()) { testTimestamp() }
        val walletManager = GlimmerWalletManager(gameStateManager = gameStateManager, timestampProvider = { testTimestamp() }, entitlementManager = null)
        
        kotlinx.coroutines.test.runTest {
            // Purchase 500
            walletManager.purchaseGlimmer(
                product = IapProductCatalog.SMALL_PACK,
                receiptData = "receipt_789",
                transactionId = "txn_789"
            )
            
            // Try to spend 600 (more than balance)
            val result = walletManager.spendGlimmer(
                amount = 600,
                type = TransactionType.BATTLE_PASS_PURCHASE
            )
            
            assertTrue(result is SpendResult.InsufficientFunds)
            val failure = result as SpendResult.InsufficientFunds
            assertEquals(600, failure.required)
            assertEquals(500, failure.available)
        }
        
        // Balance unchanged from attempted overspend
        val wallet = gameStateManager.playerState.value.glimmerWallet
        assertEquals(500, wallet.balance)
    }
    
    @Test
    fun testGrantGlimmerPromotional() {
        val gameStateManager = GameStateManager(createTestPlayer()) { testTimestamp() }
        val walletManager = GlimmerWalletManager(gameStateManager = gameStateManager, timestampProvider = { testTimestamp() }, entitlementManager = null)
        
        kotlinx.coroutines.test.runTest {
            val result = walletManager.grantGlimmer(
                amount = 250,
                reason = "promotional",
                metadata = mapOf("campaign" to "launch_week")
            )
            
            assertTrue(result is GrantResult.Success)
            val success = result as GrantResult.Success
            assertEquals(250, success.amountGranted)
            assertEquals(250, success.newBalance)
        }
        
        val wallet = gameStateManager.playerState.value.glimmerWallet
        assertEquals(250, wallet.balance)
        val transaction = wallet.transactions.first()
        assertEquals(TransactionType.PROMOTIONAL_GRANT, transaction.type)
        assertEquals("promotional", transaction.metadata["reason"])
        assertEquals("launch_week", transaction.metadata["campaign"])
    }
    
    @Test
    fun testGrantGlimmerCompensation() {
        val gameStateManager = GameStateManager(createTestPlayer()) { testTimestamp() }
        val walletManager = GlimmerWalletManager(gameStateManager = gameStateManager, timestampProvider = { testTimestamp() }, entitlementManager = null)
        
        kotlinx.coroutines.test.runTest {
            val result = walletManager.grantGlimmer(
                amount = 1000,
                reason = "compensation",
                metadata = mapOf("incident" to "server_downtime_2025_10_27")
            )
            
            assertTrue(result is GrantResult.Success)
        }
        
        val transaction = gameStateManager.playerState.value.glimmerWallet.transactions.first()
        assertEquals(TransactionType.COMPENSATION, transaction.type)
    }
    
    @Test
    fun testRefundPurchaseSuccess() {
        val gameStateManager = GameStateManager(createTestPlayer()) { testTimestamp() }
        val walletManager = GlimmerWalletManager(gameStateManager = gameStateManager, timestampProvider = { testTimestamp() }, entitlementManager = null)
        
        kotlinx.coroutines.test.runTest {
            // Make a purchase
            walletManager.purchaseGlimmer(
                product = IapProductCatalog.LARGE_PACK,
                receiptData = "receipt_refund",
                transactionId = "txn_refund_001"
            )
            
            assertEquals(2600, gameStateManager.playerState.value.glimmerWallet.balance)
            
            // Refund it
            val refundResult = walletManager.refundPurchase(
                originalTransactionId = "txn_refund_001",
                receiptData = "refund_receipt_001"
            )
            
            assertTrue(refundResult is RefundResult.Success)
            val success = refundResult as RefundResult.Success
            assertEquals(2600, success.amountRefunded)
            assertEquals(0, success.newBalance)
        }
        
        val wallet = gameStateManager.playerState.value.glimmerWallet
        assertEquals(0, wallet.balance)
        assertEquals(2, wallet.transactions.size)
        
        // Second transaction should be refund
        val refundTx = wallet.transactions.last()
        assertEquals(TransactionType.REFUND, refundTx.type)
        assertEquals(-2600, refundTx.amount)
    }
    
    @Test
    fun testRefundTransactionNotFound() {
        val gameStateManager = GameStateManager(createTestPlayer()) { testTimestamp() }
        val walletManager = GlimmerWalletManager(gameStateManager = gameStateManager, timestampProvider = { testTimestamp() }, entitlementManager = null)
        
        kotlinx.coroutines.test.runTest {
            val result = walletManager.refundPurchase(
                originalTransactionId = "nonexistent",
                receiptData = "refund_receipt"
            )
            
            assertTrue(result is RefundResult.TransactionNotFound)
        }
    }
    
    @Test
    fun testRefundInsufficientBalance() {
        val gameStateManager = GameStateManager(createTestPlayer()) { testTimestamp() }
        val walletManager = GlimmerWalletManager(gameStateManager = gameStateManager, timestampProvider = { testTimestamp() }, entitlementManager = null)
        
        kotlinx.coroutines.test.runTest {
            // Purchase and spend most of it
            walletManager.purchaseGlimmer(
                product = IapProductCatalog.SMALL_PACK,
                receiptData = "receipt_spend_before_refund",
                transactionId = "txn_spend_001"
            )
            
            walletManager.spendGlimmer(
                amount = 450,
                type = TransactionType.SHOP_PURCHASE
            )
            
            // Try to refund original 500, but only 50 left
            val refundResult = walletManager.refundPurchase(
                originalTransactionId = "txn_spend_001",
                receiptData = "refund_receipt_insufficient"
            )
            
            assertTrue(refundResult is RefundResult.InsufficientBalance)
            val failure = refundResult as RefundResult.InsufficientBalance
            assertEquals(500, failure.required)
            assertEquals(50, failure.available)
        }
    }
    
    @Test
    fun testGetWalletStats() {
        val gameStateManager = GameStateManager(createTestPlayer()) { testTimestamp() }
        val walletManager = GlimmerWalletManager(gameStateManager = gameStateManager, timestampProvider = { testTimestamp() }, entitlementManager = null)
        
        kotlinx.coroutines.test.runTest {
            walletManager.purchaseGlimmer(
                product = IapProductCatalog.MEGA_PACK,
                receiptData = "receipt_stats",
                transactionId = "txn_stats"
            )
            walletManager.spendGlimmer(1000, TransactionType.SHOP_PURCHASE)
            walletManager.grantGlimmer(500, "promotional")
        }
        
        val stats = walletManager.getWalletStats()
        assertEquals(5000, stats.currentBalance) // 5500 - 1000 + 500
        assertEquals(6000, stats.totalEarned) // 5500 + 500
        assertEquals(1000, stats.totalSpent)
        assertEquals(3, stats.transactionCount)
        assertEquals(3, stats.recentTransactions.size)
    }
    
    @Test
    fun testGetFraudAnalytics() {
        val gameStateManager = GameStateManager(createTestPlayer()) { testTimestamp() }
        val walletManager = GlimmerWalletManager(gameStateManager = gameStateManager, timestampProvider = { testTimestamp() }, entitlementManager = null)
        
        kotlinx.coroutines.test.runTest {
            // Make 2 IAP purchases
            walletManager.purchaseGlimmer(
                product = IapProductCatalog.STARTER_PACK,
                receiptData = "receipt_fraud_1",
                transactionId = "txn_fraud_1"
            )
            walletManager.purchaseGlimmer(
                product = IapProductCatalog.SMALL_PACK,
                receiptData = "receipt_fraud_2",
                transactionId = "txn_fraud_2"
            )
            
            // Refund one
            walletManager.refundPurchase(
                originalTransactionId = "txn_fraud_1",
                receiptData = "refund_fraud_1"
            )
        }
        
        val analytics = walletManager.getFraudAnalytics()
        assertEquals(0, analytics.flaggedTransactions)
        assertEquals(1, analytics.refundedTransactions)
        assertEquals(2, analytics.totalIapPurchases)
        assertEquals(600, analytics.totalIapValue) // 100 + 500
    }
    
    @Test
    fun testMultiplePurchasesSumCorrectly() {
        val gameStateManager = GameStateManager(createTestPlayer()) { testTimestamp() }
        val walletManager = GlimmerWalletManager(gameStateManager = gameStateManager, timestampProvider = { testTimestamp() }, entitlementManager = null)
        
        kotlinx.coroutines.test.runTest {
            walletManager.purchaseGlimmer(
                product = IapProductCatalog.STARTER_PACK,
                receiptData = "r1",
                transactionId = "t1"
            )
            currentTime += 1000
            walletManager.purchaseGlimmer(
                product = IapProductCatalog.SMALL_PACK,
                receiptData = "r2",
                transactionId = "t2"
            )
            currentTime += 1000
            walletManager.purchaseGlimmer(
                product = IapProductCatalog.MEDIUM_PACK,
                receiptData = "r3",
                transactionId = "t3"
            )
        }
        
        val wallet = gameStateManager.playerState.value.glimmerWallet
        assertEquals(1800, wallet.balance) // 100 + 500 + 1200
        assertEquals(1800, wallet.totalEarned)
        assertEquals(0, wallet.totalSpent)
        assertEquals(3, wallet.transactions.size)
    }
    
    @Test
    fun testTransactionHistory() {
        val gameStateManager = GameStateManager(createTestPlayer()) { testTimestamp() }
        val walletManager = GlimmerWalletManager(gameStateManager = gameStateManager, timestampProvider = { testTimestamp() }, entitlementManager = null)
        
        kotlinx.coroutines.test.runTest {
            // Create 12 transactions (more than the 10 default limit)
            repeat(12) { i ->
                currentTime += 100
                walletManager.grantGlimmer(
                    amount = 50,
                    reason = "test_${i}",
                    metadata = mapOf("index" to i.toString())
                )
            }
        }
        
        val wallet = gameStateManager.playerState.value.glimmerWallet
        assertEquals(12, wallet.transactions.size)
        
        // Get recent transactions (last 5)
        val recent = wallet.getRecentTransactions(5)
        assertEquals(5, recent.size)
        // Should be in reverse order (most recent first)
        assertEquals("11", recent[0].metadata["index"])
        assertEquals("7", recent[4].metadata["index"])
    }
    
    @Test
    fun testProductCatalog() {
        val allProducts = IapProductCatalog.getAllProducts()
        // Alpha 2.2 Phase 5B: Added Creator Coffee donation product
        assertEquals(12, allProducts.size) // 6 glimmer + 4 character slots + 1 battle pass + 1 coffee
        
        val glimmerPacks = IapProductCatalog.getGlimmerPacks()
        assertEquals(6, glimmerPacks.size)
        
        val characterSlots = IapProductCatalog.getCharacterSlotProducts()
        assertEquals(4, characterSlots.size)
        
        val starterPack = IapProductCatalog.getProductById(ProductId("glimmer_starter_100"))
        assertNotNull(starterPack)
        assertEquals(100, starterPack.glimmerAmount)
        assertEquals(0.99, starterPack.priceUsd)
        
        val nonexistent = IapProductCatalog.getProductById(ProductId("nonexistent"))
        assertNull(nonexistent)
    }
    
    @Test
    fun testProductBonusPercentages() {
        // Verify bonus percentages are correctly set
        assertEquals(0, IapProductCatalog.STARTER_PACK.bonusPercentage)
        assertEquals(0, IapProductCatalog.SMALL_PACK.bonusPercentage)
        assertEquals(20, IapProductCatalog.MEDIUM_PACK.bonusPercentage)
        assertEquals(30, IapProductCatalog.LARGE_PACK.bonusPercentage)
        assertEquals(38, IapProductCatalog.MEGA_PACK.bonusPercentage)
        assertEquals(75, IapProductCatalog.SUPPORTER_PACK.bonusPercentage)
        
        // Large pack should be marked best value
        assertTrue(IapProductCatalog.LARGE_PACK.isBestValue)
        assertFalse(IapProductCatalog.MEDIUM_PACK.isBestValue)
    }
}
