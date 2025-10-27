package com.jalmarquest.core.state.shop

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.monetization.GlimmerWalletManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

/**
 * Shop manager for cosmetic purchases and rotation management
 * Part of Milestone 5 Phase 3: Shop & Cosmetic Storefront
 * 
 * Features:
 * - Daily rotation (3 items from pool, changes every 24 hours)
 * - Weekly featured (1 item from pool, changes every 7 days)
 * - Permanent store items (always available)
 * - Glimmer-based purchases with wallet integration
 * - Cosmetic equipment management
 */
class ShopManager(
    private val gameStateManager: GameStateManager?,
    private val catalog: ShopCatalog,
    private val glimmerWalletManager: GlimmerWalletManager?,
    private val currentTimeProvider: () -> Long
) {
    private val mutex = Mutex()
    
    private val _state = MutableStateFlow(ShopManagerState())
    val state: StateFlow<ShopManagerState> = _state.asStateFlow()
    
    init {
        // Initialize state from player data if available
        gameStateManager?.playerState?.value?.let { player ->
            _state.value = ShopManagerState(
                shopState = player.shopState,
                currentDailyDeals = calculateDailyDeals(player.shopState.lastDailyRotation),
                currentWeeklyFeatured = calculateWeeklyFeatured(player.shopState.lastWeeklyRotation)
            )
        }
    }
    
    /**
     * Get all currently available shop items
     * Includes: permanent items + current daily deals + current weekly featured
     */
    fun getAvailableItems(): List<ShopItem> {
        val currentState = _state.value
        val permanentItems = catalog.getPermanentItems()
        val dailyDeals = currentState.currentDailyDeals.mapNotNull { catalog.getItem(it) }
        val weeklyFeatured = currentState.currentWeeklyFeatured.mapNotNull { catalog.getItem(it) }
        
        return (permanentItems + dailyDeals + weeklyFeatured).distinctBy { it.id }
    }
    
    /**
     * Get permanent store items (always available)
     */
    fun getPermanentItems(): List<ShopItem> = catalog.getPermanentItems()
    
    /**
     * Get current daily deals
     */
    fun getCurrentDailyDeals(): List<ShopItem> {
        return _state.value.currentDailyDeals.mapNotNull { catalog.getItem(it) }
    }
    
    /**
     * Get current weekly featured items
     */
    fun getCurrentWeeklyFeatured(): List<ShopItem> {
        return _state.value.currentWeeklyFeatured.mapNotNull { catalog.getItem(it) }
    }
    
    /**
     * Check if player owns a specific item
     */
    fun ownsItem(itemId: ShopItemId): Boolean {
        return _state.value.shopState.ownsItem(itemId)
    }
    
    /**
     * Purchase a shop item with Glimmer Shards
     * Returns PurchaseResult indicating success or failure reason
     */
    suspend fun purchaseItem(itemId: ShopItemId): PurchaseResult = mutex.withLock {
        val item = catalog.getItem(itemId) ?: return PurchaseResult.ItemNotFound
        val currentState = _state.value.shopState
        
        // Check if already owned
        if (currentState.ownsItem(itemId)) {
            return PurchaseResult.AlreadyOwned
        }
        
        // Check if item is currently available
        val availableIds = getAvailableItems().map { it.id }.toSet()
        if (itemId !in availableIds) {
            return PurchaseResult.NotAvailable
        }
        
        // Attempt Glimmer purchase via GlimmerWalletManager
        val glimmerResult = glimmerWalletManager?.spendGlimmer(
            amount = item.glimmerCost,
            type = com.jalmarquest.core.model.TransactionType.SHOP_PURCHASE,
            itemId = itemId.value
        ) ?: return PurchaseResult.WalletUnavailable
        
        if (glimmerResult !is com.jalmarquest.core.state.monetization.SpendResult.Success) {
            return PurchaseResult.InsufficientGlimmer
        }
        
        // Add to purchased items
        val updatedShopState = currentState.addPurchase(itemId)
        
        // Update player state via GameStateManager
        gameStateManager?.updateShopState { updatedShopState }
        
        // Update local state
        _state.value = _state.value.copy(shopState = updatedShopState)
        
        // Log choice for AI Director
        gameStateManager?.appendChoice("shop_purchase_${itemId.value}")
        
        return PurchaseResult.Success(item)
    }
    
    /**
     * Equip a cosmetic item
     * Player must own the item to equip it
     */
    suspend fun equipCosmetic(itemId: ShopItemId): EquipResult = mutex.withLock {
        val currentState = _state.value.shopState
        
        // Check ownership
        if (!currentState.ownsItem(itemId)) {
            return EquipResult.NotOwned
        }
        
        // Get item details
        val item = catalog.getItem(itemId) ?: return EquipResult.ItemNotFound
        val cosmeticType = item.cosmeticType ?: return EquipResult.NotCosmetic
        
        // Equip item
        val updatedEquipped = currentState.equippedCosmetics.equip(itemId, cosmeticType)
        val updatedShopState = currentState.updateEquipped(updatedEquipped)
        
        // Update player state
        gameStateManager?.updateShopState { updatedShopState }
        
        // Update local state
        _state.value = _state.value.copy(shopState = updatedShopState)
        
        // Log choice for AI Director
        gameStateManager?.appendChoice("cosmetic_equipped_${itemId.value}")
        
        return EquipResult.Success
    }
    
    /**
     * Unequip a cosmetic from a specific slot
     */
    suspend fun unequipCosmetic(type: CosmeticType): EquipResult = mutex.withLock {
        val currentState = _state.value.shopState
        
        // Unequip item
        val updatedEquipped = currentState.equippedCosmetics.unequip(type)
        val updatedShopState = currentState.updateEquipped(updatedEquipped)
        
        // Update player state
        gameStateManager?.updateShopState { updatedShopState }
        
        // Update local state
        _state.value = _state.value.copy(shopState = updatedShopState)
        
        // Log choice for AI Director
        gameStateManager?.appendChoice("cosmetic_unequipped_${type.name.lowercase()}")
        
        return EquipResult.Success
    }
    
    /**
     * Check and rotate daily deals if 24 hours have passed
     * Should be called on app startup and periodically during gameplay
     */
    suspend fun checkDailyRotation() = mutex.withLock {
        val currentTime = currentTimeProvider()
        val currentState = _state.value.shopState
        val lastRotation = currentState.lastDailyRotation
        
        val hoursSinceRotation = (currentTime - lastRotation) / (1000 * 60 * 60)
        
        if (hoursSinceRotation >= 24 || lastRotation == 0L) {
            // Rotate daily deals
            val newDeals = calculateDailyDeals(currentTime)
            val updatedShopState = currentState.updateRotations(daily = currentTime)
            
            // Update player state
            gameStateManager?.updateShopState { updatedShopState }
            
            // Update local state
            _state.value = _state.value.copy(
                shopState = updatedShopState,
                currentDailyDeals = newDeals
            )
            
            // Log rotation event
            gameStateManager?.appendChoice("shop_daily_rotation")
        }
    }
    
    /**
     * Check and rotate weekly featured items if 7 days have passed
     * Should be called on app startup and periodically during gameplay
     */
    suspend fun checkWeeklyRotation() = mutex.withLock {
        val currentTime = currentTimeProvider()
        val currentState = _state.value.shopState
        val lastRotation = currentState.lastWeeklyRotation
        
        val hoursSinceRotation = (currentTime - lastRotation) / (1000 * 60 * 60)
        
        if (hoursSinceRotation >= 168 || lastRotation == 0L) { // 168 hours = 7 days
            // Rotate weekly featured
            val newFeatured = calculateWeeklyFeatured(currentTime)
            val updatedShopState = currentState.updateRotations(weekly = currentTime)
            
            // Update player state
            gameStateManager?.updateShopState { updatedShopState }
            
            // Update local state
            _state.value = _state.value.copy(
                shopState = updatedShopState,
                currentWeeklyFeatured = newFeatured
            )
            
            // Log rotation event
            gameStateManager?.appendChoice("shop_weekly_rotation")
        }
    }
    
    /**
     * Calculate daily deals using time-based deterministic seed
     * Returns 3 items from daily rotation pool
     */
    private fun calculateDailyDeals(timestamp: Long): List<ShopItemId> {
        val pool = catalog.getDailyRotationPool()
        if (pool.isEmpty()) return emptyList()
        
        // Use day number as seed for deterministic rotation
        val dayNumber = timestamp / (1000 * 60 * 60 * 24)
        val random = Random(dayNumber.toInt())
        
        // Select 3 random items from pool
        return pool.shuffled(random).take(3).map { it.id }
    }
    
    /**
     * Calculate weekly featured using time-based deterministic seed
     * Returns 1 item from weekly rotation pool
     */
    private fun calculateWeeklyFeatured(timestamp: Long): List<ShopItemId> {
        val pool = catalog.getWeeklyRotationPool()
        if (pool.isEmpty()) return emptyList()
        
        // Use week number as seed for deterministic rotation
        val weekNumber = timestamp / (1000 * 60 * 60 * 24 * 7)
        val random = Random(weekNumber.toInt())
        
        // Select 1 random item from pool
        return pool.shuffled(random).take(1).map { it.id }
    }
}

/**
 * Shop manager state
 * Contains player's shop state and current rotation selections
 */
data class ShopManagerState(
    val shopState: ShopState = ShopState(),
    val currentDailyDeals: List<ShopItemId> = emptyList(),
    val currentWeeklyFeatured: List<ShopItemId> = emptyList()
)

/**
 * Result of a purchase attempt
 */
sealed class PurchaseResult {
    data class Success(val item: ShopItem) : PurchaseResult()
    data object AlreadyOwned : PurchaseResult()
    data object ItemNotFound : PurchaseResult()
    data object NotAvailable : PurchaseResult()
    data object InsufficientGlimmer : PurchaseResult()
    data object WalletUnavailable : PurchaseResult()
}

/**
 * Result of an equip/unequip attempt
 */
sealed class EquipResult {
    data object Success : EquipResult()
    data object NotOwned : EquipResult()
    data object ItemNotFound : EquipResult()
    data object NotCosmetic : EquipResult()
}
