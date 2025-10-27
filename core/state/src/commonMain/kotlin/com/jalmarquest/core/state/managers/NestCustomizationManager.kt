package com.jalmarquest.core.state.managers

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.monetization.GlimmerWalletManager
import com.jalmarquest.core.state.monetization.SpendResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

/**
 * Manages nest customization including cosmetic purchases, placement, and functional upgrades.
 * 
 * Milestone 5 Phase 6: Player home personalization system.
 */
class NestCustomizationManager(
    private val gameStateManager: GameStateManager,
    private val glimmerWalletManager: GlimmerWalletManager,
    private val timestampProvider: () -> Long,
    private val cosmeticCatalog: List<CosmeticItem>
) {
    private val mutex = Mutex()
    
    private val catalogMap = cosmeticCatalog.associateBy { it.id }
    
    /**
     * Purchase a nest cosmetic with Glimmer.
     */
    suspend fun purchaseCosmetic(cosmeticId: CosmeticItemId): CosmeticPurchaseResult = mutex.withLock {
        val player = gameStateManager.playerState.value
        val cosmetic = catalogMap[cosmeticId] ?: return CosmeticPurchaseResult.InsufficientGlimmer
        
        // Check if already owned
        if (player.nestCustomization.ownsCosmetic(cosmeticId)) {
            return CosmeticPurchaseResult.AlreadyOwned
        }
        
        // Check unlock requirements
        val requirement = cosmetic.unlockRequirement
        if (requirement != null && !isUnlockRequirementMet(requirement, player)) {
            return CosmeticPurchaseResult.RequirementNotMet(requirement)
        }
        
        // Check Glimmer balance
        if (player.glimmerWallet.balance < cosmetic.glimmerCost) {
            return CosmeticPurchaseResult.InsufficientGlimmer
        }
        
        // Spend Glimmer
        val spendResult = glimmerWalletManager.spendGlimmer(
            amount = cosmetic.glimmerCost,
            type = TransactionType.SHOP_PURCHASE,
            itemId = cosmeticId.value
        )
        
        if (spendResult !is SpendResult.Success) {
            return CosmeticPurchaseResult.InsufficientGlimmer
        }
        
        // Grant cosmetic
        gameStateManager.updateNestCustomization { state ->
            state.copy(
                ownedCosmetics = state.ownedCosmetics + cosmeticId
            )
        }
        
        // Log purchase
        gameStateManager.appendChoice("nest_cosmetic_purchased_${cosmeticId.value}")
        
        return CosmeticPurchaseResult.Success
    }
    
    /**
     * Place a cosmetic in the nest at specified position.
     */
    suspend fun placeCosmetic(
        cosmeticId: CosmeticItemId,
        x: Float,
        y: Float,
        rotation: Float = 0f
    ): PlacementResult = mutex.withLock {
        val player = gameStateManager.playerState.value
        val customization = player.nestCustomization
        val cosmetic = catalogMap[cosmeticId] ?: return PlacementResult.NotOwned
        
        // Check ownership
        if (!customization.ownsCosmetic(cosmeticId)) {
            return PlacementResult.NotOwned
        }
        
        // Check if placeable
        if (!cosmetic.isPlaceable) {
            return PlacementResult.InvalidPosition
        }
        
        // Check bounds (0-10 range)
        if (x < 0f || x > 10f || y < 0f || y > 10f) {
            return PlacementResult.InvalidPosition
        }
        
        // Check max instances
        val currentCount = customization.getPlacedCount(cosmeticId)
        if (currentCount >= cosmetic.maxInstances) {
            return PlacementResult.MaxInstancesReached
        }
        
        // Generate unique instance ID
        val instanceId = "${cosmeticId.value}_${timestampProvider()}_${Random.nextInt()}"
        
        // Place cosmetic
        val placed = PlacedCosmetic(
            cosmeticId = cosmeticId,
            instanceId = instanceId,
            x = x,
            y = y,
            rotation = rotation
        )
        
        gameStateManager.updateNestCustomization { state ->
            state.copy(
                placedCosmetics = state.placedCosmetics + placed
            )
        }
        
        // Log placement
        gameStateManager.appendChoice("nest_cosmetic_placed_${cosmeticId.value}")
        
        return PlacementResult.Success(instanceId)
    }
    
    /**
     * Remove a cosmetic instance from the nest.
     */
    suspend fun removeCosmetic(instanceId: String): Boolean = mutex.withLock {
        val player = gameStateManager.playerState.value
        val customization = player.nestCustomization
        
        // Find cosmetic instance
        val toRemove = customization.placedCosmetics.find { 
            it.instanceId == instanceId
        } ?: return false
        
        // Remove cosmetic
        gameStateManager.updateNestCustomization { state ->
            state.copy(
                placedCosmetics = state.placedCosmetics.filter { it.instanceId != instanceId }
            )
        }
        
        // Log removal
        gameStateManager.appendChoice("nest_cosmetic_removed_${toRemove.cosmeticId.value}")
        
        return true
    }
    
    /**
     * Move a cosmetic instance to a new position.
     */
    suspend fun moveCosmetic(
        instanceId: String,
        newX: Float,
        newY: Float,
        newRotation: Float = 0f
    ): PlacementResult = mutex.withLock {
        val player = gameStateManager.playerState.value
        val customization = player.nestCustomization
        
        // Find cosmetic instance
        val cosmetic = customization.placedCosmetics.find { 
            it.instanceId == instanceId
        } ?: return PlacementResult.NotOwned
        
        // Check bounds
        if (newX < 0f || newX > 10f || newY < 0f || newY > 10f) {
            return PlacementResult.InvalidPosition
        }
        
        // Move cosmetic
        val moved = cosmetic.copy(x = newX, y = newY, rotation = newRotation)
        
        gameStateManager.updateNestCustomization { state ->
            state.copy(
                placedCosmetics = state.placedCosmetics.map { 
                    if (it.instanceId == instanceId) moved else it 
                }
            )
        }
        
        return PlacementResult.Success(instanceId)
    }
    
    /**
     * Apply a theme (sets wallpaper, flooring, and ambiance).
     */
    suspend fun applyTheme(themeId: CosmeticItemId): Boolean = mutex.withLock {
        val player = gameStateManager.playerState.value
        val theme = catalogMap[themeId] ?: return false
        
        // Verify it's a theme
        if (theme.category != CosmeticCategory.THEME) {
            return false
        }
        
        // Verify ownership
        if (!player.nestCustomization.ownsCosmetic(themeId)) {
            return false
        }
        
        // Apply theme
        gameStateManager.updateNestCustomization { state ->
            state.copy(activeTheme = themeId)
        }
        
        // Log theme change
        gameStateManager.appendChoice("nest_theme_applied_${themeId.value}")
        
        return true
    }
    
    /**
     * Activate a functional upgrade (must be owned and placed).
     */
    suspend fun activateFunctionalUpgrade(
        type: FunctionalUpgradeType,
        cosmeticItemId: CosmeticItemId
    ): Boolean = mutex.withLock {
        val player = gameStateManager.playerState.value
        val cosmetic = catalogMap[cosmeticItemId] ?: return false
        
        // Verify it's a functional upgrade
        if (cosmetic.category != CosmeticCategory.FUNCTIONAL) {
            return false
        }
        
        // Verify ownership
        if (!player.nestCustomization.ownsCosmetic(cosmeticItemId)) {
            return false
        }
        
        // Activate upgrade
        val upgrade = FunctionalUpgrade(
            type = type,
            cosmeticItemId = cosmeticItemId,
            isActive = true
        )
        
        gameStateManager.updateNestCustomization { state ->
            state.copy(
                functionalUpgrades = state.functionalUpgrades + (type to upgrade)
            )
        }
        
        // Log activation
        gameStateManager.appendChoice("nest_upgrade_activated_${type.name}")
        
        return true
    }
    
    /**
     * Deactivate a functional upgrade.
     */
    suspend fun deactivateFunctionalUpgrade(type: FunctionalUpgradeType): Boolean = mutex.withLock {
        val player = gameStateManager.playerState.value
        val currentUpgrade = player.nestCustomization.functionalUpgrades[type] ?: return false
        
        // Deactivate upgrade
        val updated = currentUpgrade.copy(isActive = false)
        
        gameStateManager.updateNestCustomization { state ->
            state.copy(
                functionalUpgrades = state.functionalUpgrades + (type to updated)
            )
        }
        
        return true
    }
    
    /**
     * Add a trophy to the trophy display.
     */
    suspend fun addTrophy(questId: String, displayName: String, description: String): Boolean = mutex.withLock {
        val trophy = TrophyDisplay(
            questId = questId,
            displayName = displayName,
            description = description,
            placedInRoom = false
        )
        
        gameStateManager.updateNestCustomization { state ->
            state.copy(
                trophyDisplay = state.trophyDisplay + trophy
            )
        }
        
        gameStateManager.appendChoice("nest_trophy_unlocked_${questId}")
        
        return true
    }
    
    /**
     * Place/unplace a trophy in the trophy room.
     */
    suspend fun toggleTrophyPlacement(questId: String): Boolean = mutex.withLock {
        val player = gameStateManager.playerState.value
        val trophy = player.nestCustomization.trophyDisplay.find { it.questId == questId } ?: return false
        
        gameStateManager.updateNestCustomization { state ->
            state.copy(
                trophyDisplay = state.trophyDisplay.map {
                    if (it.questId == questId) it.copy(placedInRoom = !it.placedInRoom) else it
                }
            )
        }
        
        return true
    }
    
    /**
     * Toggle edit mode for nest customization.
     */
    suspend fun setEditMode(active: Boolean) = mutex.withLock {
        gameStateManager.updateNestCustomization { state ->
            state.copy(editModeActive = active)
        }
    }
    
    /**
     * Check if a cosmetic is unlocked for the player.
     */
    fun isCosmeticUnlocked(cosmeticId: CosmeticItemId): Boolean {
        val cosmetic = catalogMap[cosmeticId] ?: return false
        val req = cosmetic.unlockRequirement ?: return true
        
        val player = gameStateManager.playerState.value
        return isUnlockRequirementMet(req, player)
    }
    
    /**
     * Get all cosmetics the player can currently purchase (unlocked but not owned).
     */
    fun getAvailableCosmetics(): List<CosmeticItem> {
        val player = gameStateManager.playerState.value
        
        return cosmeticCatalog.filter { cosmetic ->
            !player.nestCustomization.ownsCosmetic(cosmetic.id) &&
            (cosmetic.unlockRequirement?.let { req -> isUnlockRequirementMet(req, player) } != false)
        }
    }
    
    /**
     * Get cosmetics by category.
     */
    fun getCosmeticsByCategory(category: CosmeticCategory): List<CosmeticItem> {
        return cosmeticCatalog.filter { it.category == category }
    }
    
    /**
     * Get all cosmetics by rarity.
     */
    fun getCosmeticsByRarity(rarity: CosmeticRarity): List<CosmeticItem> {
        return catalogMap.values.filter { it.rarity == rarity }
    }
    
    /**
     * Get all owned cosmetics that are not currently placed in the nest.
     */
    fun getUnplacedCosmetics(): List<CosmeticItem> {
        val player = gameStateManager.playerState.value
        val nestState = player.nestCustomization
        val placedIds = nestState.placedCosmetics.map { it.cosmeticId }.toSet()
        
        return nestState.ownedCosmetics
            .filter { it !in placedIds }
            .mapNotNull { catalogMap[it] }
    }
    
    /**
     * Get seed storage bonus from functional upgrades.
     */
    fun getSeedStorageBonus(): Float {
        return gameStateManager.playerState.value.nestCustomization.getSeedStorageBonus()
    }
    
    /**
     * Get hoard XP bonus.
     */
    fun getHoardXpBonus(): Float {
        return gameStateManager.playerState.value.nestCustomization.getHoardXpBonus()
    }
    
    /**
     * Get extra Thought Cabinet slots.
     */
    fun getExtraThoughtSlots(): Int {
        return gameStateManager.playerState.value.nestCustomization.getExtraThoughtSlots()
    }
    
    /**
     * Check if player can craft in nest.
     */
    fun canCraftInNest(): Boolean {
        return gameStateManager.playerState.value.nestCustomization.canCraftInNest()
    }
    
    /**
     * Get companion XP bonus.
     */
    fun getCompanionXpBonus(): Float {
        return gameStateManager.playerState.value.nestCustomization.getCompanionXpBonus()
    }
    
    // Private helper methods
    
    private fun isUnlockRequirementMet(requirement: UnlockRequirement, player: Player): Boolean {
        return when (requirement) {
            is UnlockRequirement.QuestCompletion -> {
                player.questLog.completedQuests.any { it.value == requirement.questId }
            }
            is UnlockRequirement.PlayerLevel -> {
                // Player level system not implemented yet, always allow
                true
            }
            is UnlockRequirement.HoardRank -> {
                val hoardTier = player.hoardRank.tier
                hoardTier.ordinal >= requirement.minimumRank
            }
            is UnlockRequirement.AchievementUnlock -> {
                player.worldExploration.achievementState.unlockedAchievements
                    .any { achievement -> achievement.achievementType.name == requirement.achievementId }
            }
        }
    }
}
