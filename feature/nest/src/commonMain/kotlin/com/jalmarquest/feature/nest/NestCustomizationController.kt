package com.jalmarquest.feature.nest

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.managers.NestCustomizationManager
import com.jalmarquest.core.state.monetization.GlimmerWalletManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Controller for Nest Customization UI.
 * Manages state and user interactions for the housing system.
 */
class NestCustomizationController(
    private val manager: NestCustomizationManager,
    private val gameStateManager: GameStateManager,
    private val glimmerWalletManager: GlimmerWalletManager,
    private val scope: CoroutineScope
) {
    // Expose player state
    val playerState: StateFlow<Player> = gameStateManager.playerState
    
    // UI state
    private val _selectedTab = MutableStateFlow(NestTab.SHOP)
    val selectedTab: StateFlow<NestTab> = _selectedTab.asStateFlow()
    
    private val _selectedShopCategory = MutableStateFlow(CosmeticCategory.THEME)
    val selectedShopCategory: StateFlow<CosmeticCategory> = _selectedShopCategory.asStateFlow()
    
    private val _purchaseResult = MutableStateFlow<CosmeticPurchaseResult?>(null)
    val purchaseResult: StateFlow<CosmeticPurchaseResult?> = _purchaseResult.asStateFlow()
    
    private val _placementResult = MutableStateFlow<PlacementResult?>(null)
    val placementResult: StateFlow<PlacementResult?> = _placementResult.asStateFlow()
    
    // Derived state for shop
    val availableCosmetics: StateFlow<List<CosmeticItem>> = combine(
        selectedShopCategory,
        playerState
    ) { category, _ ->
        manager.getAvailableCosmetics().filter { it.category == category }
    }.stateIn(scope, SharingStarted.Lazily, emptyList())
    
    val unplacedCosmetics: StateFlow<List<CosmeticItem>> = playerState.map {
        manager.getUnplacedCosmetics()
    }.stateIn(scope, SharingStarted.Lazily, emptyList())
    
    // Actions
    fun selectTab(tab: NestTab) {
        _selectedTab.value = tab
    }
    
    fun selectShopCategory(category: CosmeticCategory) {
        _selectedShopCategory.value = category
    }
    
    fun purchaseCosmetic(cosmeticId: CosmeticItemId) {
        scope.launch {
            val result = manager.purchaseCosmetic(cosmeticId)
            _purchaseResult.value = result
        }
    }
    
    fun placeCosmetic(cosmeticId: CosmeticItemId, x: Float, y: Float, rotation: Float = 0f) {
        scope.launch {
            val result = manager.placeCosmetic(cosmeticId, x, y, rotation)
            _placementResult.value = result
        }
    }
    
    fun removeCosmetic(instanceId: String) {
        scope.launch {
            manager.removeCosmetic(instanceId)
        }
    }
    
    fun moveCosmetic(instanceId: String, newX: Float, newY: Float, newRotation: Float = 0f) {
        scope.launch {
            manager.moveCosmetic(instanceId, newX, newY, newRotation)
        }
    }
    
    fun applyTheme(themeId: CosmeticItemId) {
        scope.launch {
            manager.applyTheme(themeId)
        }
    }
    
    fun activateFunctionalUpgrade(upgradeType: FunctionalUpgradeType, cosmeticItemId: CosmeticItemId) {
        scope.launch {
            manager.activateFunctionalUpgrade(upgradeType, cosmeticItemId)
        }
    }
    
    fun deactivateFunctionalUpgrade(upgradeType: FunctionalUpgradeType) {
        scope.launch {
            manager.deactivateFunctionalUpgrade(upgradeType)
        }
    }
    
    fun toggleEditMode() {
        scope.launch {
            val currentState = gameStateManager.playerState.value.nestCustomization
            manager.setEditMode(!currentState.editModeActive)
        }
    }
    
    fun clearPurchaseResult() {
        _purchaseResult.value = null
    }
    
    fun clearPlacementResult() {
        _placementResult.value = null
    }
}

/**
 * Tab navigation for Nest screen.
 */
enum class NestTab {
    SHOP,
    EDIT_MODE,
    TROPHY_ROOM
}
