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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.system.currentTimeMillis

/**
 * Centralized manager for player-facing state changes; guarantees every mutation is recorded.
 * Now supports multi-character save slots via AccountManager integration.
 */
class GameStateManager(
    initialPlayer: Player,
    private val accountManager: AccountManager? = null,
    private val timestampProvider: () -> Long = { currentTimeMillis() }
) {
    private val mutex = Mutex()
    private val _playerState = MutableStateFlow(initialPlayer)
    val playerState: StateFlow<Player> = _playerState.asStateFlow()
    
    // FIX: Add thread-safe batch update method for complex operations
    suspend fun batchUpdate(block: (Player) -> Player) = mutex.withLock {
        val startTime = timestampProvider()
        val updatedPlayer = block(_playerState.value)
        _playerState.value = updatedPlayer
        
        // Log performance for large updates
        val duration = timestampProvider() - startTime
        if (duration > 100) {
            PerformanceLogger.logSlowMutation("batchUpdate", duration)
        }
    }
    
    // Track when the current play session started for playtime calculation
    private var sessionStartTime: Long = timestampProvider()

    suspend fun appendChoice(tagValue: String) {
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
    
    /**
     * Alpha 2.3: Update seed inventory.
     */
    fun updateSeedInventory(transform: (com.jalmarquest.core.model.SeedInventory) -> com.jalmarquest.core.model.SeedInventory) {
        _playerState.update { player ->
            player.copy(seedInventory = transform(player.seedInventory))
        }
    }
    
    /**
     * Alpha 2.3: Update crafting inventory (reagents and known recipes).
     */
    fun updateCraftingInventory(transform: (com.jalmarquest.core.model.CraftingInventory) -> com.jalmarquest.core.model.CraftingInventory) {
        _playerState.update { player ->
            player.copy(craftingInventory = transform(player.craftingInventory))
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

    fun updateWorldMapState(transform: (com.jalmarquest.core.model.WorldMapState?) -> com.jalmarquest.core.model.WorldMapState) {
        _playerState.update { player ->
            player.copy(worldMapState = transform(player.worldMapState))
        }
    }
    
    /**
     * Alpha 2.3 Part 3.1: Update companion state (traits, affinity, etc.)
     */
    fun updateCompanionState(transform: (com.jalmarquest.core.model.CompanionState) -> com.jalmarquest.core.model.CompanionState) {
        _playerState.update { player ->
            player.copy(companionState = transform(player.companionState))
        }
    }

    /**
     * Alpha 2.3 Part 3.2: Update companion assignment state.
     */
    fun updateCompanionAssignments(transform: (com.jalmarquest.core.model.CompanionAssignmentState) -> com.jalmarquest.core.model.CompanionAssignmentState) {
        _playerState.update { player ->
            player.copy(companionAssignments = transform(player.companionAssignments))
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
     * Update player settings (No Filter Mode, donation flags, etc.)
     * Alpha 2.2 - Advanced Narrative & AI Systems
     */
    fun updatePlayerSettings(transform: (com.jalmarquest.core.model.PlayerSettings) -> com.jalmarquest.core.model.PlayerSettings) {
        _playerState.update { player ->
            player.copy(playerSettings = transform(player.playerSettings))
        }
    }
    
    /**
     * Update AI Director state for adaptive difficulty tracking.
     * Alpha 2.2 - AI Director Core Manager.
     */
    fun updateAIDirectorState(newState: com.jalmarquest.core.model.AIDirectorState) {
        _playerState.update { player ->
            player.copy(aiDirectorState = newState)
        }
    }
    
    /**
     * Alpha 2.2 Phase 5C: Grants one-time Creator Coffee donation rewards.
     * Called when interacting with npc_exhausted_coder after purchasing coffee.
     * 
     * Rewards:
     * - Golden Coffee Bean shiny (LEGENDARY, 5000 Seeds value)
     * - Patron's Crown cosmetic (exclusive, cannot be purchased)
     * - +50 affinity with npc_exhausted_coder
     * - Choice tag logged: "coffee_rewards_granted"
     * 
     * @param hoardManager HoardRankManager to grant shiny
     * @param npcRelationshipManager NpcRelationshipManager to add affinity (optional)
     * @return true if rewards were granted, false if already claimed
     */
    suspend fun grantCreatorCoffeeRewards(
        hoardManager: com.jalmarquest.core.state.hoard.HoardRankManager?,
        npcRelationshipManager: com.jalmarquest.core.state.npc.NpcRelationshipManager? = null
    ): Boolean {
        val player = _playerState.value
        
        // Check if player purchased coffee and hasn't received rewards yet
        if (!player.playerSettings.hasPurchasedCreatorCoffee || 
            player.playerSettings.hasReceivedCoffeeRewards) {
            return false
        }
        
        // Grant Golden Coffee Bean shiny
        hoardManager?.acquireShiny(com.jalmarquest.core.model.ShinyId("golden_coffee_bean"))
        
        // Grant Patron's Crown cosmetic (add to owned items without purchase)
        val patronCrownId = com.jalmarquest.core.model.ShopItemId("cosmetic_crown_patron")
        updateShopState { shopState ->
            shopState.addPurchase(patronCrownId)
        }
        
        // Add affinity with Exhausted Coder
        npcRelationshipManager?.addAffinity("npc_exhausted_coder", 50)
        
        // Mark rewards as claimed
        updatePlayerSettings { settings ->
            settings.copy(hasReceivedCoffeeRewards = true)
        }
        
        // Log analytics
        appendChoice("coffee_rewards_granted")
        
        PerformanceLogger.logStateMutation(
            "GameStateManager",
            "grantCreatorCoffeeRewards",
            mapOf(
                "shiny" to "golden_coffee_bean",
                "cosmetic" to "cosmetic_crown_patron",
                "affinity_bonus" to 50,
                "npc" to "npc_exhausted_coder"
            )
        )
        
        return true
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
