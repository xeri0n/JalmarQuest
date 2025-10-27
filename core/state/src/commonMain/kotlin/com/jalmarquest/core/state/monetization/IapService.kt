package com.jalmarquest.core.state.monetization

import com.jalmarquest.core.model.IapProduct
import com.jalmarquest.core.model.ProductId

/**
 * Platform-specific IAP service for handling in-app purchases.
 * 
 * Android: Google Play Billing Library
 * Desktop: Stub implementation for testing
 * iOS: StoreKit (future)
 */
expect class IapService() {
    /**
     * Initialize the IAP service with the platform billing client.
     * Must be called before any purchase operations.
     * 
     * @return true if initialization succeeded, false otherwise
     */
    suspend fun initialize(): Boolean
    
    /**
     * Query available products from the platform store.
     * 
     * @param productIds List of product IDs to query
     * @return Map of ProductId to platform-specific product details
     */
    suspend fun queryProducts(productIds: List<ProductId>): Map<ProductId, PlatformProduct>
    
    /**
     * Launch the purchase flow for a product.
     * 
     * @param product The product to purchase
     * @return PurchaseResponse with result and receipt data
     */
    suspend fun launchPurchaseFlow(product: IapProduct): PurchaseResponse
    
    /**
     * Verify a purchase receipt with the platform.
     * 
     * @param receiptData Platform-specific receipt data
     * @return true if receipt is valid, false otherwise
     */
    suspend fun verifyPurchase(receiptData: String): Boolean
    
    /**
     * Restore previous purchases (e.g., after app reinstall).
     * 
     * @return List of restored purchase receipts
     */
    suspend fun restorePurchases(): List<RestoredPurchase>
    
    /**
     * Consume a consumable product (e.g., Glimmer Shards).
     * Required on Android to allow repeat purchases.
     * 
     * @param purchaseToken Platform-specific purchase token
     */
    suspend fun consumePurchase(purchaseToken: String): Boolean
    
    /**
     * Acknowledge a non-consumable purchase (e.g., character slot).
     * Required on Android within 3 days of purchase.
     * 
     * @param purchaseToken Platform-specific purchase token
     */
    suspend fun acknowledgePurchase(purchaseToken: String): Boolean
    
    /**
     * Check if billing is supported on this device.
     */
    fun isBillingSupported(): Boolean
    
    /**
     * Clean up resources.
     */
    fun dispose()
}

/**
 * Platform-specific product details.
 */
data class PlatformProduct(
    val productId: ProductId,
    val name: String,
    val description: String,
    val priceString: String, // Localized price string (e.g., "$4.99")
    val priceMicros: Long,   // Price in micros (e.g., 4990000 for $4.99)
    val currencyCode: String // ISO 4217 currency code (e.g., "USD")
)

/**
 * Result of a purchase attempt.
 */
sealed class PurchaseResponse {
    data class Success(
        val productId: ProductId,
        val receiptData: String,
        val purchaseToken: String,
        val transactionId: String,
        val purchaseTimeMillis: Long
    ) : PurchaseResponse()
    
    data object Cancelled : PurchaseResponse()
    data class Error(val message: String) : PurchaseResponse()
    data object AlreadyOwned : PurchaseResponse()
    data object NetworkError : PurchaseResponse()
}

/**
 * Restored purchase from previous transaction.
 */
data class RestoredPurchase(
    val productId: ProductId,
    val receiptData: String,
    val purchaseToken: String,
    val transactionId: String,
    val purchaseTimeMillis: Long
)

/**
 * IAP service state.
 */
enum class IapServiceState {
    UNINITIALIZED,
    INITIALIZING,
    READY,
    DISCONNECTED,
    ERROR
}
