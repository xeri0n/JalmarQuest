package com.jalmarquest.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Glimmer Shards - Premium currency for JalmarQuest.
 * 
 * Used for:
 * - Seasonal Chronicle premium battle pass
 * - Cosmetic purchases (nest themes, companion outfits)
 * - Extra character slots
 * - Supporter packs
 * - Optional convenience items (NOT pay-to-win)
 */

/**
 * Unique identifier for a Glimmer Shard transaction.
 */
@Serializable
@JvmInline
value class TransactionId(val value: String)

/**
 * Unique identifier for an IAP product.
 */
@Serializable
@JvmInline
value class ProductId(val value: String)

/**
 * Type of transaction for audit trail.
 */
@Serializable
enum class TransactionType {
    /** Purchased via IAP */
    IAP_PURCHASE,
    /** Granted by admin/support */
    ADMIN_GRANT,
    /** Spent on shop item */
    SHOP_PURCHASE,
    /** Spent on seasonal chronicle */
    BATTLE_PASS_PURCHASE,
    /** Spent on character slot */
    CHARACTER_SLOT_PURCHASE,
    /** Spent on nest upgrade (Alpha 2.3) */
    NEST_UPGRADE,
    /** Refund for cancelled IAP */
    REFUND,
    /** Promotional grant */
    PROMOTIONAL_GRANT,
    /** Compensation for bugs/downtime */
    COMPENSATION,
    /** Debug grant for testing */
    DEBUG_GRANT
}

/**
 * Status of a transaction for anti-fraud tracking.
 */
@Serializable
enum class TransactionStatus {
    /** Transaction completed successfully */
    COMPLETED,
    /** Pending verification (e.g., IAP receipt validation) */
    PENDING,
    /** Transaction failed */
    FAILED,
    /** Transaction was refunded */
    REFUNDED,
    /** Transaction flagged as suspicious */
    FLAGGED
}

/**
 * A single Glimmer Shard transaction for audit trail.
 */
@Serializable
data class GlimmerTransaction(
    val id: TransactionId,
    val type: TransactionType,
    val amount: Int,
    val balanceAfter: Int,
    val timestampMillis: Long,
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val productId: ProductId? = null,
    val receiptData: String? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    init {
        require(amount != 0) { "Transaction amount cannot be zero" }
        require(balanceAfter >= 0) { "Balance after transaction cannot be negative" }
    }
}

/**
 * Player's Glimmer Shard wallet with transaction history.
 */
@Serializable
data class GlimmerWallet(
    val balance: Int = 0,
    val totalEarned: Int = 0,
    val totalSpent: Int = 0,
    val transactions: List<GlimmerTransaction> = emptyList()
) {
    init {
        require(balance >= 0) { "Balance cannot be negative" }
        require(totalEarned >= 0) { "Total earned cannot be negative" }
        require(totalSpent >= 0) { "Total spent cannot be negative" }
    }
    
    /**
     * Add Glimmer Shards to wallet (IAP, grant, refund).
     */
    fun add(
        amount: Int,
        type: TransactionType,
        timestampMillis: Long,
        transactionId: TransactionId,
        productId: ProductId? = null,
        receiptData: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): GlimmerWallet {
        require(amount > 0) { "Cannot add negative or zero amount" }
        val newBalance = balance + amount
        val newTotalEarned = totalEarned + amount
        val transaction = GlimmerTransaction(
            id = transactionId,
            type = type,
            amount = amount,
            balanceAfter = newBalance,
            timestampMillis = timestampMillis,
            productId = productId,
            receiptData = receiptData,
            metadata = metadata
        )
        return copy(
            balance = newBalance,
            totalEarned = newTotalEarned,
            transactions = transactions + transaction
        )
    }
    
    /**
     * Spend Glimmer Shards from wallet (shop, battle pass, etc.).
     */
    fun spend(
        amount: Int,
        type: TransactionType,
        timestampMillis: Long,
        transactionId: TransactionId,
        metadata: Map<String, String> = emptyMap()
    ): GlimmerWallet? {
        if (amount <= 0) return null
        if (balance < amount) return null // Insufficient funds
        
        val newBalance = balance - amount
        val newTotalSpent = totalSpent + amount
        val transaction = GlimmerTransaction(
            id = transactionId,
            type = type,
            amount = -amount,
            balanceAfter = newBalance,
            timestampMillis = timestampMillis,
            metadata = metadata
        )
        return copy(
            balance = newBalance,
            totalSpent = newTotalSpent,
            transactions = transactions + transaction
        )
    }
    
    /**
     * Get recent transactions (last N).
     */
    fun getRecentTransactions(count: Int = 10): List<GlimmerTransaction> {
        return transactions.takeLast(count).reversed()
    }
    
    /**
     * Get transactions by type.
     */
    fun getTransactionsByType(type: TransactionType): List<GlimmerTransaction> {
        return transactions.filter { it.type == type }
    }
    
    /**
     * Get flagged transactions for anti-fraud review.
     */
    fun getFlaggedTransactions(): List<GlimmerTransaction> {
        return transactions.filter { it.status == TransactionStatus.FLAGGED }
    }
}

/**
 * IAP product definition for store integration.
 */
@Serializable
data class IapProduct(
    val id: ProductId,
    val name: String,
    val description: String,
    val glimmerAmount: Int,
    val priceUsd: Double,
    val isBestValue: Boolean = false,
    val bonusPercentage: Int = 0,
    val metadata: Map<String, String> = emptyMap()
) {
    init {
        require(glimmerAmount >= 0) { "Glimmer amount cannot be negative" }
        require(priceUsd > 0.0) { "Price must be positive" }
        require(bonusPercentage >= 0) { "Bonus percentage cannot be negative" }
    }
}

/**
 * Catalog of all IAP products available for purchase.
 */
object IapProductCatalog {
    val STARTER_PACK = IapProduct(
        id = ProductId("glimmer_starter_100"),
        name = "Starter Glimmer Pack",
        description = "100 Glimmer Shards",
        glimmerAmount = 100,
        priceUsd = 0.99
    )
    
    val SMALL_PACK = IapProduct(
        id = ProductId("glimmer_small_500"),
        name = "Small Glimmer Pack",
        description = "500 Glimmer Shards",
        glimmerAmount = 500,
        priceUsd = 4.99
    )
    
    val MEDIUM_PACK = IapProduct(
        id = ProductId("glimmer_medium_1200"),
        name = "Medium Glimmer Pack",
        description = "1,200 Glimmer Shards (+20% bonus)",
        glimmerAmount = 1200,
        priceUsd = 9.99,
        bonusPercentage = 20
    )
    
    val LARGE_PACK = IapProduct(
        id = ProductId("glimmer_large_2600"),
        name = "Large Glimmer Pack",
        description = "2,600 Glimmer Shards (+30% bonus)",
        glimmerAmount = 2600,
        priceUsd = 19.99,
        bonusPercentage = 30,
        isBestValue = true
    )
    
    val MEGA_PACK = IapProduct(
        id = ProductId("glimmer_mega_5500"),
        name = "Mega Glimmer Pack",
        description = "5,500 Glimmer Shards (+38% bonus)",
        glimmerAmount = 5500,
        priceUsd = 39.99,
        bonusPercentage = 38
    )
    
    val SUPPORTER_PACK = IapProduct(
        id = ProductId("supporter_pack_premium"),
        name = "Jalmar Supporter Pack",
        description = "14,000 Glimmer Shards + Exclusive Nest Theme + Companion Outfit",
        glimmerAmount = 14000,
        priceUsd = 99.99,
        bonusPercentage = 75,
        metadata = mapOf(
            "includes_cosmetic_nest_theme" to "supporter_golden_nest",
            "includes_cosmetic_companion" to "supporter_golden_plumage"
        )
    )
    
    val CHARACTER_SLOT = IapProduct(
        id = ProductId("character_slot_extra"),
        name = "Extra Character Slot",
        description = "Unlock one additional character slot",
        glimmerAmount = 0, // No Glimmer, direct entitlement
        priceUsd = 2.99,
        metadata = mapOf("slot_number" to "variable") // Deprecated: use specific slot products
    )
    
    // Character slot products (1 per slot for proper restore support)
    val CHARACTER_SLOT_2 = IapProduct(
        id = ProductId("character_slot_2"),
        name = "Character Slot 2",
        description = "Unlock your second character slot",
        glimmerAmount = 0,
        priceUsd = 2.99,
        metadata = mapOf(
            "slot_number" to "2",
            "entitlement" to CharacterSlotEntitlement.SLOT_2.name
        )
    )
    
    val CHARACTER_SLOT_3 = IapProduct(
        id = ProductId("character_slot_3"),
        name = "Character Slot 3",
        description = "Unlock your third character slot",
        glimmerAmount = 0,
        priceUsd = 4.99,
        metadata = mapOf(
            "slot_number" to "3",
            "entitlement" to CharacterSlotEntitlement.SLOT_3.name
        )
    )
    
    val CHARACTER_SLOT_4 = IapProduct(
        id = ProductId("character_slot_4"),
        name = "Character Slot 4",
        description = "Unlock your fourth character slot",
        glimmerAmount = 0,
        priceUsd = 6.99,
        metadata = mapOf(
            "slot_number" to "4",
            "entitlement" to CharacterSlotEntitlement.SLOT_4.name
        )
    )
    
    val CHARACTER_SLOT_5 = IapProduct(
        id = ProductId("character_slot_5"),
        name = "Character Slot 5",
        description = "Unlock your fifth character slot",
        glimmerAmount = 0,
        priceUsd = 9.99,
        metadata = mapOf(
            "slot_number" to "5",
            "entitlement" to CharacterSlotEntitlement.SLOT_5.name
        )
    )
    
    val BATTLE_PASS_PREMIUM = IapProduct(
        id = ProductId("battle_pass_premium_current_season"),
        name = "Seasonal Chronicle Premium",
        description = "Unlock premium rewards for current season",
        glimmerAmount = 0, // No Glimmer, direct entitlement
        priceUsd = 9.99,
        metadata = mapOf("entitlement_type" to "battle_pass_premium")
    )
    
    // Alpha 2.2: Creator Coffee Donation
    val CREATOR_COFFEE = IapProduct(
        id = ProductId("creator_coffee_donation"),
        name = "A Cup of Creator's Coffee",
        description = "Support the developer with a coffee! Unlocks special thank-you rewards from the Exhausted Coder.",
        glimmerAmount = 0, // Pure donation, no currency
        priceUsd = 2.99,
        metadata = mapOf(
            "is_donation" to "true",
            "unlocks_exhausted_coder_rewards" to "true",
            "one_time_purchase" to "true"
        )
    )
    
    /**
     * Get all purchasable products.
     */
    fun getAllProducts(): List<IapProduct> = listOf(
        STARTER_PACK,
        SMALL_PACK,
        MEDIUM_PACK,
        LARGE_PACK,
        MEGA_PACK,
        SUPPORTER_PACK,
        CHARACTER_SLOT_2,
        CHARACTER_SLOT_3,
        CHARACTER_SLOT_4,
        CHARACTER_SLOT_5,
        BATTLE_PASS_PREMIUM,
        CREATOR_COFFEE
    )
    
    /**
     * Get character slot products only.
     */
    fun getCharacterSlotProducts(): List<IapProduct> = listOf(
        CHARACTER_SLOT_2,
        CHARACTER_SLOT_3,
        CHARACTER_SLOT_4,
        CHARACTER_SLOT_5
    )
    
    /**
     * Get Glimmer Shard packs only.
     */
    fun getGlimmerPacks(): List<IapProduct> = listOf(
        STARTER_PACK,
        SMALL_PACK,
        MEDIUM_PACK,
        LARGE_PACK,
        MEGA_PACK,
        SUPPORTER_PACK
    )
    
    /**
     * Get product by ID.
     */
    fun getProductById(id: ProductId): IapProduct? {
        return getAllProducts().firstOrNull { it.id == id }
    }
}

// ===== ENTITLEMENTS =====

/**
 * Character slot entitlement types.
 * Players start with 1 free slot, can purchase up to 4 additional slots.
 */
@Serializable
enum class CharacterSlotEntitlement {
    @SerialName("slot_2")
    SLOT_2,
    
    @SerialName("slot_3")
    SLOT_3,
    
    @SerialName("slot_4")
    SLOT_4,
    
    @SerialName("slot_5")
    SLOT_5
}

/**
 * Player's entitlement state.
 * Tracks non-consumable purchases (character slots, premium passes, etc.)
 */
@Serializable
data class EntitlementState(
    /** Purchased character slots (slot 1 is always free) */
    val characterSlots: Set<CharacterSlotEntitlement> = emptySet(),
    
    /** Transaction IDs for entitlement purchases (for restore verification) */
    val entitlementTransactions: Map<CharacterSlotEntitlement, TransactionId> = emptyMap()
) {
    /**
     * Check if player has a specific character slot entitlement.
     */
    fun hasSlot(slot: CharacterSlotEntitlement): Boolean {
        return slot in characterSlots
    }
    
    /**
     * Get total number of available character slots (including free slot 1).
     */
    fun totalSlots(): Int {
        return 1 + characterSlots.size // Slot 1 is always free
    }
    
    /**
     * Check if a slot number is unlocked (1-based).
     */
    fun isSlotUnlocked(slotNumber: Int): Boolean {
        return when (slotNumber) {
            1 -> true // Slot 1 always free
            2 -> hasSlot(CharacterSlotEntitlement.SLOT_2)
            3 -> hasSlot(CharacterSlotEntitlement.SLOT_3)
            4 -> hasSlot(CharacterSlotEntitlement.SLOT_4)
            5 -> hasSlot(CharacterSlotEntitlement.SLOT_5)
            else -> false
        }
    }
    
    /**
     * Grant a character slot entitlement.
     */
    fun grantSlot(slot: CharacterSlotEntitlement, transactionId: TransactionId): EntitlementState {
        return copy(
            characterSlots = characterSlots + slot,
            entitlementTransactions = entitlementTransactions + (slot to transactionId)
        )
    }
}
