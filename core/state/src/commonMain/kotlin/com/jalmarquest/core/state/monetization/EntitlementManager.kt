package com.jalmarquest.core.state.monetization

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * EntitlementManager handles permanent unlocks (character slots, premium access, etc.).
 * Unlike consumable Glimmer purchases, entitlements persist and must be restorable.
 */
class EntitlementManager(
    private val gameStateManager: GameStateManager
) {
    private val mutex = Mutex()
    
    /**
     * Check if player has a specific character slot entitlement.
     */
    fun hasCharacterSlot(slot: CharacterSlotEntitlement): Boolean {
        return gameStateManager.playerState.value.entitlements.hasSlot(slot)
    }
    
    /**
     * Check if a specific slot number is unlocked (1-based).
     * Slot 1 is always free.
     */
    fun isSlotUnlocked(slotNumber: Int): Boolean {
        return gameStateManager.playerState.value.entitlements.isSlotUnlocked(slotNumber)
    }
    
    /**
     * Get total number of unlocked character slots (1 base + purchased).
     */
    fun getTotalSlots(): Int {
        return gameStateManager.playerState.value.entitlements.totalSlots()
    }
    
    /**
     * Grant a character slot entitlement (called after successful IAP).
     */
    suspend fun grantCharacterSlot(
        slot: CharacterSlotEntitlement,
        transactionId: TransactionId
    ): EntitlementGrantResult {
        mutex.withLock {
            val currentEntitlements = gameStateManager.playerState.value.entitlements
            
            // Check if already owned
            if (currentEntitlements.hasSlot(slot)) {
                return EntitlementGrantResult.AlreadyOwned
            }
            
            // Grant entitlement
            val updatedEntitlements = currentEntitlements.grantSlot(slot, transactionId)
            gameStateManager.updateEntitlements { updatedEntitlements }
            
            return EntitlementGrantResult.Success(slot)
        }
    }
    
    /**
     * Parse entitlement from product metadata.
     */
    private fun getEntitlementFromProduct(product: IapProduct): CharacterSlotEntitlement? {
        val entitlementName = product.metadata["entitlement"] as? String ?: return null
        return try {
            CharacterSlotEntitlement.valueOf(entitlementName)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
    
    /**
     * Restore character slot entitlements from IAP receipts.
     * Called during restore purchases flow.
     */
    suspend fun restoreFromReceipts(receipts: List<ReceiptData>): RestoreResult {
        mutex.withLock {
            val currentEntitlements = gameStateManager.playerState.value.entitlements
            var updatedEntitlements = currentEntitlements
            val restoredSlots = mutableSetOf<CharacterSlotEntitlement>()
            
            // Process each receipt
            for (receipt in receipts) {
                // Find matching product
                val product = IapProductCatalog.getProductById(receipt.productId)
                if (product == null || product.glimmerAmount > 0) {
                    continue // Skip consumables or unknown products
                }
                
                // Extract entitlement
                val slot = getEntitlementFromProduct(product) ?: continue
                
                // Grant if not already owned
                if (!updatedEntitlements.hasSlot(slot)) {
                    updatedEntitlements = updatedEntitlements.grantSlot(slot, receipt.transactionId)
                    restoredSlots.add(slot)
                }
            }
            
            // Save if any changes
            if (restoredSlots.isNotEmpty()) {
                gameStateManager.updateEntitlements { updatedEntitlements }
            }
            
            return RestoreResult.Success(restoredSlots.toSet())
        }
    }
    
    /**
     * Get list of locked character slots that can be purchased.
     */
    fun getAvailableSlotsToPurchase(): List<CharacterSlotPurchaseOption> {
        val entitlements = gameStateManager.playerState.value.entitlements
        return CharacterSlotEntitlement.entries.mapNotNull { slot ->
            if (!entitlements.hasSlot(slot)) {
                val product = when (slot) {
                    CharacterSlotEntitlement.SLOT_2 -> IapProductCatalog.CHARACTER_SLOT_2
                    CharacterSlotEntitlement.SLOT_3 -> IapProductCatalog.CHARACTER_SLOT_3
                    CharacterSlotEntitlement.SLOT_4 -> IapProductCatalog.CHARACTER_SLOT_4
                    CharacterSlotEntitlement.SLOT_5 -> IapProductCatalog.CHARACTER_SLOT_5
                }
                CharacterSlotPurchaseOption(
                    slot = slot,
                    slotNumber = slot.ordinal + 2, // SLOT_2 is index 0, maps to slot number 2
                    product = product,
                    isUnlocked = false
                )
            } else {
                null
            }
        }
    }
    
    /**
     * Get list of all character slots with ownership status.
     */
    fun getAllCharacterSlots(): List<CharacterSlotStatus> {
        val entitlements = gameStateManager.playerState.value.entitlements
        return listOf(
            CharacterSlotStatus(1, true, null), // Slot 1 always free
            CharacterSlotStatus(
                2,
                entitlements.hasSlot(CharacterSlotEntitlement.SLOT_2),
                IapProductCatalog.CHARACTER_SLOT_2
            ),
            CharacterSlotStatus(
                3,
                entitlements.hasSlot(CharacterSlotEntitlement.SLOT_3),
                IapProductCatalog.CHARACTER_SLOT_3
            ),
            CharacterSlotStatus(
                4,
                entitlements.hasSlot(CharacterSlotEntitlement.SLOT_4),
                IapProductCatalog.CHARACTER_SLOT_4
            ),
            CharacterSlotStatus(
                5,
                entitlements.hasSlot(CharacterSlotEntitlement.SLOT_5),
                IapProductCatalog.CHARACTER_SLOT_5
            )
        )
    }
}

/**
 * Result of granting an entitlement.
 */
sealed interface EntitlementGrantResult {
    data class Success(val slot: CharacterSlotEntitlement) : EntitlementGrantResult
    data object AlreadyOwned : EntitlementGrantResult
}

/**
 * Result of restoring purchases.
 */
sealed interface RestoreResult {
    data class Success(val restoredSlots: Set<CharacterSlotEntitlement>) : RestoreResult
    data class Failure(val error: String) : RestoreResult
}

/**
 * Receipt data from platform IAP system.
 */
data class ReceiptData(
    val productId: ProductId,
    val transactionId: TransactionId,
    val purchaseTime: Long
)

/**
 * A character slot available for purchase.
 */
data class CharacterSlotPurchaseOption(
    val slot: CharacterSlotEntitlement,
    val slotNumber: Int,
    val product: IapProduct,
    val isUnlocked: Boolean
)

/**
 * Status of a character slot (unlocked or locked).
 */
data class CharacterSlotStatus(
    val slotNumber: Int,
    val isUnlocked: Boolean,
    val product: IapProduct? // Null for slot 1 (free)
)
