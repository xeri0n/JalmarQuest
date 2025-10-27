package com.jalmarquest.core.state.account

import com.jalmarquest.core.model.ArchetypeProgress
import com.jalmarquest.core.model.ArchetypeType
import com.jalmarquest.core.model.CharacterAccount
import com.jalmarquest.core.model.CharacterDisplayStats
import com.jalmarquest.core.model.CharacterSlot
import com.jalmarquest.core.model.CharacterSlotId
import com.jalmarquest.core.model.EntitlementState
import com.jalmarquest.core.model.HoardRank
import com.jalmarquest.core.model.HoardRankTier
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.state.perf.PerformanceLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manages character slot lifecycle for multi-character account support.
 * Handles character creation, deletion (soft delete), restoration, switching, and metadata updates.
 * Thread-safe operations via Mutex.
 */
class AccountManager(
    initialAccount: CharacterAccount,
    private val timestampProvider: () -> Long
) {
    private val mutex = Mutex()
    private val _accountState = MutableStateFlow(initialAccount)
    val accountState: StateFlow<CharacterAccount> = _accountState

    /**
     * Creates a new character in the next available slot.
     * @param entitlements Player's entitlement state for slot limits
     * @return CharacterSlotId of the newly created character, or null if max slots reached
     */
    suspend fun createCharacter(
        characterName: String,
        archetype: ArchetypeType,
        entitlements: EntitlementState? = null
    ): CharacterSlotId? = mutex.withLock {
        val account = _accountState.value
        if (!account.canCreateSlot(entitlements)) {
            PerformanceLogger.logStateMutation(
                "AccountManager",
                "createCharacter",
                mapOf("result" to "max_slots_reached", "active" to account.getActiveSlots().size, "max" to account.maxSlots(entitlements))
            )
            return null
        }

        val newSlot = CharacterSlot.create(
            characterName = characterName,
            archetype = archetype,
            timestamp = timestampProvider()
        )

        val updatedAccount = account.addSlot(newSlot)?.setCurrentSlot(newSlot.slotId)
        
        if (updatedAccount == null) {
            PerformanceLogger.logStateMutation(
                "AccountManager",
                "createCharacter",
                mapOf("result" to "failed_to_add_slot")
            )
            return null
        }

        _accountState.value = updatedAccount
        PerformanceLogger.logStateMutation(
            "AccountManager",
            "createCharacter",
            mapOf("slotId" to newSlot.slotId.value, "name" to characterName, "archetype" to archetype.name)
        )
        newSlot.slotId
    }

    /**
     * Soft deletes a character slot. Can be restored later.
     */
    suspend fun deleteCharacter(slotId: CharacterSlotId): Boolean = mutex.withLock {
        val account = _accountState.value
        val slot = account.characterSlots.find { it.slotId == slotId }
        
        if (slot == null || slot.isDeleted) {
            PerformanceLogger.logStateMutation(
                "AccountManager",
                "deleteCharacter",
                mapOf("slotId" to slotId.value, "result" to "not_found_or_already_deleted")
            )
            return false
        }

        val deletedSlot = slot.markDeleted()
        val updatedAccount = account.updateSlot(deletedSlot.slotId) { deletedSlot }
        
        if (updatedAccount == null) {
            PerformanceLogger.logStateMutation(
                "AccountManager",
                "deleteCharacter",
                mapOf("slotId" to slotId.value, "result" to "update_failed")
            )
            return false
        }

        // If we deleted the current character, switch to first available active slot
        val finalAccount = if (account.currentSlotId == slotId) {
            val firstActive = updatedAccount.getActiveSlots().firstOrNull()
            firstActive?.let { updatedAccount.setCurrentSlot(it.slotId) } ?: updatedAccount
        } else {
            updatedAccount
        }

        _accountState.value = finalAccount
        PerformanceLogger.logStateMutation(
            "AccountManager",
            "deleteCharacter",
            mapOf("slotId" to slotId.value, "result" to "success")
        )
        true
    }

    /**
     * Restores a previously deleted character slot.
     */
    suspend fun restoreCharacter(slotId: CharacterSlotId): Boolean = mutex.withLock {
        val account = _accountState.value
        val slot = account.characterSlots.find { it.slotId == slotId }
        
        if (slot == null || !slot.isDeleted) {
            PerformanceLogger.logStateMutation(
                "AccountManager",
                "restoreCharacter",
                mapOf("slotId" to slotId.value, "result" to "not_found_or_not_deleted")
            )
            return false
        }

        val restoredSlot = slot.restore()
        val updatedAccount = account.updateSlot(restoredSlot.slotId) { restoredSlot }
        
        if (updatedAccount == null) {
            PerformanceLogger.logStateMutation(
                "AccountManager",
                "restoreCharacter",
                mapOf("slotId" to slotId.value, "result" to "update_failed")
            )
            return false
        }

        _accountState.value = updatedAccount
        PerformanceLogger.logStateMutation(
            "AccountManager",
            "restoreCharacter",
            mapOf("slotId" to slotId.value, "result" to "success")
        )
        true
    }

    /**
     * Lists all active (non-deleted) character slots, ordered by last played (most recent first).
     */
    fun listCharacters(): List<CharacterSlot> {
        return _accountState.value.getActiveSlots()
            .sortedByDescending { it.lastPlayedAt }
    }

    /**
     * Switches to a different character slot.
     * @return true if switch was successful, false if slot not found or deleted
     */
    suspend fun switchCharacter(slotId: CharacterSlotId): Boolean = mutex.withLock {
        val account = _accountState.value
        val slot = account.characterSlots.find { it.slotId == slotId }
        
        if (slot == null || slot.isDeleted) {
            PerformanceLogger.logStateMutation(
                "AccountManager",
                "switchCharacter",
                mapOf("slotId" to slotId.value, "result" to "not_found_or_deleted")
            )
            return false
        }

        // Update last played timestamp when switching to a character
        val updatedSlot = slot.copy(lastPlayedAt = timestampProvider())
        val withUpdatedSlot = account.updateSlot(updatedSlot.slotId) { updatedSlot }
        if (withUpdatedSlot == null) return false
        val updatedAccount = withUpdatedSlot.setCurrentSlot(slotId)
        
        if (updatedAccount == null) {
            PerformanceLogger.logStateMutation(
                "AccountManager",
                "switchCharacter",
                mapOf("slotId" to slotId.value, "result" to "set_current_failed")
            )
            return false
        }

        _accountState.value = updatedAccount
        PerformanceLogger.logStateMutation(
            "AccountManager",
            "switchCharacter",
            mapOf("slotId" to slotId.value, "result" to "success")
        )
        true
    }

    /**
     * Updates character metadata from current Player state.
     * Should be called periodically during gameplay and before switching characters.
     */
    suspend fun updateCharacterMetadata(
        slotId: CharacterSlotId,
        player: Player,
        additionalPlaytimeSeconds: Long = 0
    ): Boolean = mutex.withLock {
        val account = _accountState.value
        val slot = account.characterSlots.find { it.slotId == slotId }
        
        if (slot == null || slot.isDeleted) {
            return false
        }

        // Update the slot using the updateStats method which takes a Player
        val updatedSlot = slot
            .updatePlaytime(additionalPlaytimeSeconds, timestampProvider())
            .updateStats(player)

        val updatedAccount = account.updateSlot(updatedSlot.slotId) { updatedSlot }
        if (updatedAccount == null) return false
        
        _accountState.value = updatedAccount
        
        PerformanceLogger.logStateMutation(
            "AccountManager",
            "updateCharacterMetadata",
            mapOf(
                "slotId" to slotId.value,
                "playtime" to updatedSlot.totalPlaytimeSeconds,
                "hoardValue" to updatedSlot.displayStats.hoardValue
            )
        )
        true
    }

    /**
     * Purchases additional character slots beyond the base limit.
     * In a real implementation, this would be called after successful IAP.
     */
    suspend fun purchaseExtraSlots(count: Int): Boolean = mutex.withLock {
        if (count <= 0) return false
        
        val account = _accountState.value
        val updatedAccount = account.purchaseExtraSlots(count)
        _accountState.value = updatedAccount
        
        PerformanceLogger.logStateMutation(
            "AccountManager",
            "purchaseExtraSlots",
            mapOf("count" to count, "newMax" to updatedAccount.maxSlots())
        )
        true
    }

    /**
     * Gets the currently active character slot, or null if no character is selected.
     */
    fun getCurrentCharacter(): CharacterSlot? {
        val account = _accountState.value
        return account.currentSlotId?.let { slotId ->
            account.characterSlots.find { it.slotId == slotId && !it.isDeleted }
        }
    }

    /**
     * Gets the current account state (for serialization/persistence).
     */
    fun getAccount(): CharacterAccount = _accountState.value

    /**
     * Replaces the entire account state (for deserialization/loading).
     */
    suspend fun loadAccount(account: CharacterAccount) = mutex.withLock {
        _accountState.value = account
        PerformanceLogger.logStateMutation(
            "AccountManager",
            "loadAccount",
            mapOf("slots" to account.characterSlots.size, "current" to (account.currentSlotId?.value ?: "none"))
        )
    }
}
