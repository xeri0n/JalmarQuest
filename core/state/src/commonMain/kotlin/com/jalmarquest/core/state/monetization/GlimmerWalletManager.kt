package com.jalmarquest.core.state.monetization

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.perf.PerformanceLogger
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manages Glimmer Shard wallet operations with audit trail and anti-fraud checks.
 * 
 * All transactions are logged for compliance and fraud detection.
 * Thread-safe via Mutex for concurrent IAP completion scenarios.
 */
class GlimmerWalletManager(
    private val gameStateManager: GameStateManager,
    private val timestampProvider: () -> Long,
    private val entitlementManager: EntitlementManager? = null
) {
    private val mutex = Mutex()
    
    val playerState: StateFlow<Player> = gameStateManager.playerState
    
    /**
     * Purchase Glimmer Shards via IAP.
     * 
     * @param product The IAP product being purchased
     * @param receiptData Platform-specific receipt for verification
     * @param transactionId Unique transaction ID from platform
     * @return true if purchase was successful, false otherwise
     */
    suspend fun purchaseGlimmer(
        product: IapProduct,
        receiptData: String,
        transactionId: String
    ): PurchaseResult = mutex.withLock {
        val startTime = timestampProvider()
        
        // Validate product (entitlement-only products like character slots have 0 Glimmer)
        if (product.glimmerAmount < 0) {
            return PurchaseResult.InvalidProduct("Product has negative Glimmer amount")
        }
        
        // Handle entitlement-only products (character slots)
        if (product.glimmerAmount == 0) {
            // Extract entitlement from metadata
            val entitlementName = product.metadata["entitlement"] as? String
            if (entitlementName != null) {
                try {
                    val slot = CharacterSlotEntitlement.valueOf(entitlementName)
                    // Grant entitlement
                    entitlementManager?.grantCharacterSlot(
                        slot = slot,
                        transactionId = TransactionId(transactionId)
                    )
                    // Log analytics
                    gameStateManager.appendChoice("character_slot_purchase_${slot.name.lowercase()}")
                } catch (e: IllegalArgumentException) {
                    return PurchaseResult.InvalidProduct("Unknown entitlement: $entitlementName")
                }
            }
            
            return PurchaseResult.Success(
                amountAdded = 0,
                newBalance = gameStateManager.playerState.value.glimmerWallet.balance
            )
        }
        
        // Check for duplicate transaction
        val player = gameStateManager.playerState.value
        val existingTransaction = player.glimmerWallet.transactions.find { 
            it.receiptData == receiptData 
        }
        if (existingTransaction != null) {
            PerformanceLogger.logStateMutation(
                "GlimmerWalletManager",
                "purchaseGlimmer",
                mapOf("error" to "Duplicate IAP transaction detected: $transactionId")
            )
            return PurchaseResult.DuplicateTransaction
        }
        
        // Add Glimmer to wallet
        val updatedWallet = player.glimmerWallet.add(
            amount = product.glimmerAmount,
            type = TransactionType.IAP_PURCHASE,
            timestampMillis = timestampProvider(),
            transactionId = TransactionId(transactionId),
            productId = product.id,
            receiptData = receiptData,
            metadata = mapOf(
                "product_name" to product.name,
                "price_usd" to product.priceUsd.toString(),
                "bonus_percentage" to product.bonusPercentage.toString()
            )
        )
        
        // Update player state
        gameStateManager.updateGlimmerWallet { updatedWallet }
        
        // Log analytics
        gameStateManager.appendChoice("glimmer_purchase_${product.id.value}")
        PerformanceLogger.logStateMutation(
            "GlimmerWalletManager",
            "purchaseGlimmer",
            mapOf("duration_ms" to (timestampProvider() - startTime))
        )
        
        PurchaseResult.Success(
            amountAdded = product.glimmerAmount,
            newBalance = updatedWallet.balance
        )
    }
    
    /**
     * Spend Glimmer Shards on shop items, battle pass, etc.
     * 
     * @param amount Amount to spend
     * @param type Type of transaction (shop, battle pass, etc.)
     * @param itemId Optional item ID being purchased
     * @return true if spend was successful, false if insufficient funds
     */
    suspend fun spendGlimmer(
        amount: Int,
        type: TransactionType,
        itemId: String? = null
    ): SpendResult = mutex.withLock {
        val startTime = timestampProvider()
        val player = gameStateManager.playerState.value
        
        // Validate amount
        if (amount <= 0) {
            return SpendResult.InvalidAmount
        }
        
        // Check balance
        if (player.glimmerWallet.balance < amount) {
            return SpendResult.InsufficientFunds(
                required = amount,
                available = player.glimmerWallet.balance
            )
        }
        
        // Generate unique transaction ID
        val transactionId = TransactionId("spend_${timestampProvider()}_${type.name}")
        
        // Spend from wallet
        val metadata = buildMap {
            itemId?.let { put("item_id", it) }
            put("transaction_time", timestampProvider().toString())
        }
        
        val updatedWallet = player.glimmerWallet.spend(
            amount = amount,
            type = type,
            timestampMillis = timestampProvider(),
            transactionId = transactionId,
            metadata = metadata
        )
        
        if (updatedWallet == null) {
            return SpendResult.InsufficientFunds(amount, player.glimmerWallet.balance)
        }
        
        // Update player state
        gameStateManager.updateGlimmerWallet { updatedWallet }
        
        // Log analytics
        gameStateManager.appendChoice("glimmer_spend_${type.name}_${amount}")
        PerformanceLogger.logStateMutation(
            "GlimmerWalletManager",
            "spendGlimmer",
            mapOf("duration_ms" to (timestampProvider() - startTime))
        )
        
        SpendResult.Success(
            amountSpent = amount,
            newBalance = updatedWallet.balance
        )
    }
    
    /**
     * Grant Glimmer Shards for promotional/compensation purposes.
     * 
     * @param amount Amount to grant
     * @param reason Reason for grant (promotional, compensation, etc.)
     * @param metadata Additional context for audit trail
     */
    suspend fun grantGlimmer(
        amount: Int,
        reason: String,
        metadata: Map<String, String> = emptyMap()
    ): GrantResult = mutex.withLock {
        val startTime = timestampProvider()
        
        if (amount <= 0) {
            return GrantResult.InvalidAmount
        }
        
        val player = gameStateManager.playerState.value
        val transactionId = TransactionId("grant_${timestampProvider()}_${reason}")
        
        val type = when (reason.lowercase()) {
            "promotional", "promo" -> TransactionType.PROMOTIONAL_GRANT
            "compensation", "bug", "downtime" -> TransactionType.COMPENSATION
            else -> TransactionType.ADMIN_GRANT
        }
        
        val updatedWallet = player.glimmerWallet.add(
            amount = amount,
            type = type,
            timestampMillis = timestampProvider(),
            transactionId = transactionId,
            metadata = metadata + ("reason" to reason)
        )
        
        gameStateManager.updateGlimmerWallet { updatedWallet }
        gameStateManager.appendChoice("glimmer_grant_${reason}_${amount}")
        PerformanceLogger.logStateMutation(
            "GlimmerWalletManager",
            "grantGlimmer",
            mapOf("duration_ms" to (timestampProvider() - startTime))
        )
        
        GrantResult.Success(amount, updatedWallet.balance)
    }
    
    /**
     * Refund a Glimmer purchase (IAP refund scenario).
     * 
     * @param originalTransactionId The transaction ID to refund
     * @param receiptData Platform refund receipt
     */
    suspend fun refundPurchase(
        originalTransactionId: String,
        receiptData: String
    ): RefundResult = mutex.withLock {
        val player = gameStateManager.playerState.value
        
        // Find original transaction
        val originalTransaction = player.glimmerWallet.transactions.find {
            it.id.value == originalTransactionId
        }
        
        if (originalTransaction == null) {
            return RefundResult.TransactionNotFound
        }
        
        if (originalTransaction.status == TransactionStatus.REFUNDED) {
            return RefundResult.AlreadyRefunded
        }
        
        if (originalTransaction.type != TransactionType.IAP_PURCHASE) {
            return RefundResult.InvalidTransactionType
        }
        
        // Deduct the amount that was added
        val refundAmount = originalTransaction.amount
        val transactionId = TransactionId("refund_${timestampProvider()}_${originalTransactionId}")
        
        // Spend the refunded amount (removes from balance)
        val updatedWallet = player.glimmerWallet.spend(
            amount = refundAmount,
            type = TransactionType.REFUND,
            timestampMillis = timestampProvider(),
            transactionId = transactionId,
            metadata = mapOf(
                "original_transaction_id" to originalTransactionId,
                "refund_receipt" to receiptData
            )
        )
        
        if (updatedWallet == null) {
            // Balance already too low - flag for manual review
            return RefundResult.InsufficientBalance(refundAmount, player.glimmerWallet.balance)
        }
        
        gameStateManager.updateGlimmerWallet { updatedWallet }
        gameStateManager.appendChoice("glimmer_refund_$originalTransactionId")
        
        RefundResult.Success(refundAmount, updatedWallet.balance)
    }
    
    /**
     * Get wallet statistics for UI display.
     */
    fun getWalletStats(): WalletStats {
        val wallet = playerState.value.glimmerWallet
        return WalletStats(
            currentBalance = wallet.balance,
            totalEarned = wallet.totalEarned,
            totalSpent = wallet.totalSpent,
            transactionCount = wallet.transactions.size,
            recentTransactions = wallet.getRecentTransactions(5)
        )
    }
    
    /**
     * Get anti-fraud analytics.
     */
    fun getFraudAnalytics(): FraudAnalytics {
        val wallet = playerState.value.glimmerWallet
        val flaggedCount = wallet.getFlaggedTransactions().size
        val refundedCount = wallet.getTransactionsByType(TransactionType.REFUND).size
        val iapTransactions = wallet.getTransactionsByType(TransactionType.IAP_PURCHASE)
        
        return FraudAnalytics(
            flaggedTransactions = flaggedCount,
            refundedTransactions = refundedCount,
            totalIapPurchases = iapTransactions.size,
            totalIapValue = iapTransactions.sumOf { it.amount }
        )
    }
}

/**
 * Result of a purchase operation.
 */
sealed class PurchaseResult {
    data class Success(val amountAdded: Int, val newBalance: Int) : PurchaseResult()
    data class InvalidProduct(val reason: String) : PurchaseResult()
    data object DuplicateTransaction : PurchaseResult()
}

/**
 * Result of a spend operation.
 */
sealed class SpendResult {
    data class Success(val amountSpent: Int, val newBalance: Int) : SpendResult()
    data class InsufficientFunds(val required: Int, val available: Int) : SpendResult()
    data object InvalidAmount : SpendResult()
}

/**
 * Result of a grant operation.
 */
sealed class GrantResult {
    data class Success(val amountGranted: Int, val newBalance: Int) : GrantResult()
    data object InvalidAmount : GrantResult()
}

/**
 * Result of a refund operation.
 */
sealed class RefundResult {
    data class Success(val amountRefunded: Int, val newBalance: Int) : RefundResult()
    data object TransactionNotFound : RefundResult()
    data object AlreadyRefunded : RefundResult()
    data object InvalidTransactionType : RefundResult()
    data class InsufficientBalance(val required: Int, val available: Int) : RefundResult()
}

/**
 * Wallet statistics for UI display.
 */
data class WalletStats(
    val currentBalance: Int,
    val totalEarned: Int,
    val totalSpent: Int,
    val transactionCount: Int,
    val recentTransactions: List<GlimmerTransaction>
)

/**
 * Anti-fraud analytics.
 */
data class FraudAnalytics(
    val flaggedTransactions: Int,
    val refundedTransactions: Int,
    val totalIapPurchases: Int,
    val totalIapValue: Int
)
