package com.jalmarquest.core.state.monetization

import com.jalmarquest.core.model.IapProduct
import com.jalmarquest.core.model.IapProductCatalog
import com.jalmarquest.core.model.ProductId
import kotlinx.coroutines.delay

/**
 * Desktop stub implementation of IapService for testing.
 * 
 * Simulates successful purchases without real payment processing.
 * Useful for development and testing flows without Google Play/App Store.
 */
actual class IapService {
    private var state = IapServiceState.UNINITIALIZED
    private val simulatedProducts = mutableMapOf<ProductId, PlatformProduct>()
    private val purchaseHistory = mutableListOf<RestoredPurchase>()
    
    actual suspend fun initialize(): Boolean {
        state = IapServiceState.INITIALIZING
        delay(100) // Simulate initialization delay
        
        // Populate simulated products from catalog
        IapProductCatalog.getAllProducts().forEach { product ->
            simulatedProducts[product.id] = PlatformProduct(
                productId = product.id,
                name = product.name,
                description = product.description,
                priceString = "$${product.priceUsd}",
                priceMicros = (product.priceUsd * 1_000_000).toLong(),
                currencyCode = "USD"
            )
        }
        
        state = IapServiceState.READY
        println("[IapService:Desktop] Initialized with ${simulatedProducts.size} simulated products")
        return true
    }
    
    actual suspend fun queryProducts(productIds: List<ProductId>): Map<ProductId, PlatformProduct> {
        delay(50) // Simulate network delay
        return productIds.mapNotNull { id ->
            simulatedProducts[id]?.let { id to it }
        }.toMap()
    }
    
    actual suspend fun launchPurchaseFlow(product: IapProduct): PurchaseResponse {
        if (state != IapServiceState.READY) {
            return PurchaseResponse.Error("IapService not ready")
        }
        
        delay(200) // Simulate purchase flow delay
        
        // Simulate successful purchase
        val timestamp = System.currentTimeMillis()
        val transactionId = "desktop_txn_${timestamp}_${product.id.value}"
        val purchaseToken = "desktop_token_${timestamp}"
        val receiptData = "desktop_receipt_${product.id.value}_${timestamp}"
        
        val restored = RestoredPurchase(
            productId = product.id,
            receiptData = receiptData,
            purchaseToken = purchaseToken,
            transactionId = transactionId,
            purchaseTimeMillis = timestamp
        )
        purchaseHistory.add(restored)
        
        println("[IapService:Desktop] Simulated purchase: ${product.name} for \$${product.priceUsd}")
        
        return PurchaseResponse.Success(
            productId = product.id,
            receiptData = receiptData,
            purchaseToken = purchaseToken,
            transactionId = transactionId,
            purchaseTimeMillis = timestamp
        )
    }
    
    actual suspend fun verifyPurchase(receiptData: String): Boolean {
        delay(50) // Simulate verification delay
        // In desktop mode, all receipts are valid
        println("[IapService:Desktop] Verified receipt: $receiptData")
        return true
    }
    
    actual suspend fun restorePurchases(): List<RestoredPurchase> {
        delay(100) // Simulate restore delay
        println("[IapService:Desktop] Restored ${purchaseHistory.size} purchases")
        return purchaseHistory.toList()
    }
    
    actual suspend fun consumePurchase(purchaseToken: String): Boolean {
        delay(50)
        println("[IapService:Desktop] Consumed purchase: $purchaseToken")
        return true
    }
    
    actual suspend fun acknowledgePurchase(purchaseToken: String): Boolean {
        delay(50)
        println("[IapService:Desktop] Acknowledged purchase: $purchaseToken")
        return true
    }
    
    actual fun isBillingSupported(): Boolean {
        return true // Desktop stub always supports billing
    }
    
    actual fun dispose() {
        state = IapServiceState.DISCONNECTED
        println("[IapService:Desktop] Disposed")
    }
    
    // Test helpers
    fun clearPurchaseHistory() {
        purchaseHistory.clear()
    }
    
    fun getState(): IapServiceState = state
}
