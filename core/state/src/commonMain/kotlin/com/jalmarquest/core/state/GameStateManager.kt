package com.jalmarquest.core.state

import com.jalmarquest.core.model.CharacterSlotId
import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.ChoiceLogEntry
import com.jalmarquest.core.model.ChoiceTag
import com.jalmarquest.core.model.Inventory
import com.jalmarquest.core.model.ItemId
import com.jalmarquest.core.model.ItemStack
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.model.QuestLog
import com.jalmarquest.core.model.StatusEffects
import com.jalmarquest.core.state.account.AccountManager
import com.jalmarquest.core.state.perf.PerformanceLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration

/**
 * Centralized manager for player-facing state changes; guarantees every mutation is recorded.
 * Now supports multi-character save slots via AccountManager integration.
 */
class GameStateManager(
    initialPlayer: Player,
    private val accountManager: AccountManager? = null,
    private val timestampProvider: () -> Long
) {
    private val _playerState = MutableStateFlow(initialPlayer)
    val playerState: StateFlow<Player> = _playerState
    
    // Track when the current play session started for playtime calculation
    private var sessionStartTime: Long = timestampProvider()

    fun appendChoice(tagValue: String) {
        require(tagValue.isNotBlank()) { "Choice tag cannot be blank" }
        PerformanceLogger.logStateMutation("Player", "appendChoice", mapOf("tag" to tagValue))
        val newEntry = ChoiceLogEntry(tag = ChoiceTag(tagValue), timestampMillis = timestampProvider())
        _playerState.update { player ->
            player.copy(choiceLog = ChoiceLog(player.choiceLog.entries + newEntry))
        }
    }

    fun updateQuestLog(updater: (QuestLog) -> QuestLog) {
        _playerState.update { player ->
            player.copy(questLog = updater(player.questLog))
        }
    }

    fun applyStatusEffect(effectKey: String, duration: Duration?) {
        require(effectKey.isNotBlank()) { "Status effect key cannot be blank" }
        PerformanceLogger.logStateMutation("Player", "applyStatusEffect", mapOf("effect" to effectKey, "duration" to (duration?.inWholeSeconds?.toString() ?: "permanent")))
        val expiresAt = duration?.let { timestampProvider() + it.inWholeMilliseconds }
        _playerState.update { player ->
            val filtered = player.statusEffects.entries.filterNot { it.key == effectKey }
            player.copy(statusEffects = StatusEffects(filtered + com.jalmarquest.core.model.StatusEffect(effectKey, expiresAt)))
        }
    }

    fun clearStatus(effectKey: String) {
        require(effectKey.isNotBlank()) { "Status effect key cannot be blank" }
        _playerState.update { player ->
            val filtered = player.statusEffects.entries.filterNot { it.key == effectKey }
            player.copy(statusEffects = StatusEffects(filtered))
        }
    }

    fun grantItem(itemId: String, quantity: Int) {
        require(itemId.isNotBlank()) { "Item id cannot be blank" }
        require(quantity >= 0) { "Quantity cannot be negative" }
        if (quantity == 0) return
        PerformanceLogger.logStateMutation("Player", "grantItem", mapOf("item" to itemId, "qty" to quantity))
        val stack = ItemStack(id = ItemId(itemId), quantity = quantity)
        _playerState.update { player ->
            player.copy(inventory = player.inventory.add(stack))
        }
    }

    fun consumeItem(itemId: String, quantity: Int): Boolean {
        require(itemId.isNotBlank()) { "Item id cannot be blank" }
        require(quantity >= 0) { "Quantity cannot be negative" }
        if (quantity == 0) return true
        var consumed = false
        _playerState.update { player ->
            val currentQty = player.inventory.totalQuantity(ItemId(itemId))
            consumed = currentQty >= quantity
            if (!consumed) return@update player
            player.copy(inventory = player.inventory.remove(ItemId(itemId), quantity))
        }
        return consumed
    }

    fun updateInventory(transform: (Inventory) -> Inventory) {
        _playerState.update { player ->
            player.copy(inventory = transform(player.inventory))
        }
    }
    
    fun updateGlimmerWallet(transform: (com.jalmarquest.core.model.GlimmerWallet) -> com.jalmarquest.core.model.GlimmerWallet) {
        _playerState.update { player ->
            player.copy(glimmerWallet = transform(player.glimmerWallet))
        }
    }

    fun updateShopState(transform: (com.jalmarquest.core.model.ShopState) -> com.jalmarquest.core.model.ShopState) {
        _playerState.update { player ->
            player.copy(shopState = transform(player.shopState))
        }
    }

    fun updateEntitlements(transform: (com.jalmarquest.core.model.EntitlementState) -> com.jalmarquest.core.model.EntitlementState) {
        _playerState.update { player ->
            player.copy(entitlements = transform(player.entitlements))
        }
    }

    fun updateNestCustomization(transform: (com.jalmarquest.core.model.NestCustomizationState) -> com.jalmarquest.core.model.NestCustomizationState) {
        _playerState.update { player ->
            player.copy(nestCustomization = transform(player.nestCustomization))
        }
    }

    fun updateWorldExploration(transform: (com.jalmarquest.core.model.WorldExplorationState) -> com.jalmarquest.core.model.WorldExplorationState) {
        _playerState.update { player ->
            player.copy(worldExploration = transform(player.worldExploration))
        }
    }

    fun updateFactionReputation(factionId: String, amount: Int) {
        require(factionId.isNotBlank()) { "Faction id cannot be blank" }
        PerformanceLogger.logStateMutation("Player", "updateFactionReputation", mapOf("faction" to factionId, "amount" to amount))
        _playerState.update { player ->
            val currentRep = player.factionReputations[factionId] ?: 0
            val newRep = (currentRep + amount).coerceIn(-100, 100)
            player.copy(factionReputations = player.factionReputations + (factionId to newRep))
        }
    }

    fun setFactionReputation(factionId: String, reputation: Int) {
        require(factionId.isNotBlank()) { "Faction id cannot be blank" }
        val clampedRep = reputation.coerceIn(-100, 100)
        _playerState.update { player ->
            player.copy(factionReputations = player.factionReputations + (factionId to clampedRep))
        }
    }

    fun updatePlayer(transform: (Player) -> Player) {
        _playerState.update(transform)
    }
    
    /**
     * Saves the current player state to the current character slot.
     * Calculates playtime since session start and updates character metadata.
     * @return true if save was successful, false if no AccountManager or current slot
     */
    suspend fun savePlayerToCurrentSlot(): Boolean {
        val manager = accountManager ?: return false
        val currentSlot = manager.getCurrentCharacter() ?: return false
        
        val now = timestampProvider()
        val sessionDuration = (now - sessionStartTime) / 1000 // Convert to seconds
        val currentPlayer = _playerState.value
        
        val success = manager.updateCharacterMetadata(
            slotId = currentSlot.slotId,
            player = currentPlayer,
            additionalPlaytimeSeconds = sessionDuration
        )
        
        if (success) {
            // Reset session timer after successful save
            sessionStartTime = now
            PerformanceLogger.logStateMutation(
                "GameStateManager",
                "savePlayerToCurrentSlot",
                mapOf("slotId" to currentSlot.slotId.value, "playtime" to sessionDuration)
            )
        }
        
        return success
    }
    
    /**
     * Switches to a different character slot.
     * Saves current player first, then loads the player data for the new slot.
     * Note: This currently only switches the slot metadata. Loading actual Player data
     * from persistent storage will be implemented when save/load system is added.
     * 
     * @return true if switch was successful, false otherwise
     */
    suspend fun switchToCharacterSlot(slotId: CharacterSlotId): Boolean {
        val manager = accountManager ?: return false
        
        // Save current player before switching
        savePlayerToCurrentSlot()
        
        // Switch to the new slot
        val success = manager.switchCharacter(slotId)
        
        if (success) {
            // Reset session timer for new character
            sessionStartTime = timestampProvider()
            
            // TODO: Load Player data from persistent storage when save/load system exists
            // For now, we just update the slot metadata but keep the same Player instance
            
            PerformanceLogger.logStateMutation(
                "GameStateManager",
                "switchToCharacterSlot",
                mapOf("slotId" to slotId.value, "success" to true)
            )
        }
        
        return success
    }
    
    /**
     * Gets the playtime for the current session in seconds.
     */
    fun getCurrentSessionPlaytime(): Long {
        val now = timestampProvider()
        return (now - sessionStartTime) / 1000
    }
}
