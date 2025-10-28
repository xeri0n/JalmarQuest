package com.jalmarquest.core.state.monetization

import com.jalmarquest.core.model.IapProduct
import com.jalmarquest.core.model.ProductId

/**
 * Interface for IAP service to enable testing with mock implementations.
 * Platform-specific IapService (expect class) should implement this interface.
 * 
 * Alpha 2.2 Phase 5B: Added to support Coffee IAP testing.
 */
interface IIapService {
    suspend fun initialize(): Boolean
    suspend fun queryProducts(productIds: List<ProductId>): Map<ProductId, PlatformProduct>
    suspend fun launchPurchaseFlow(product: IapProduct): PurchaseResponse
    suspend fun verifyPurchase(receiptData: String): Boolean
    suspend fun restorePurchases(): List<RestoredPurchase>
    suspend fun consumePurchase(purchaseToken: String): Boolean
    suspend fun acknowledgePurchase(purchaseToken: String): Boolean
    fun isBillingSupported(): Boolean
    fun dispose()
}
