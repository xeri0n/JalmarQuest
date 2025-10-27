package com.jalmarquest.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Unique identifier for a character slot.
 */
@Serializable
@JvmInline
value class CharacterSlotId(val value: String)

/**
 * Represents a character save slot with metadata for display and management.
 * The actual Player data is stored separately and loaded on demand.
 */
@Serializable
data class CharacterSlot(
    @SerialName("slot_id")
    val slotId: CharacterSlotId,
    
    @SerialName("character_name")
    val characterName: String,
    
    @SerialName("archetype")
    val archetype: ArchetypeType? = null,
    
    @SerialName("total_playtime_seconds")
    val totalPlaytimeSeconds: Long = 0,
    
    @SerialName("created_at")
    val createdAt: Long,
    
    @SerialName("last_played_at")
    val lastPlayedAt: Long,
    
    @SerialName("is_deleted")
    val isDeleted: Boolean = false,
    
    /**
     * Quick stats for display on character selection screen
     */
    @SerialName("display_stats")
    val displayStats: CharacterDisplayStats = CharacterDisplayStats()
) {
    companion object {
        /**
         * Maximum number of character slots per account (base limit).
         * Additional slots can be purchased via IAP.
         */
        const val MAX_BASE_SLOTS = 3
        
        /**
         * Create a new character slot with initial metadata.
         */
        fun create(
            characterName: String,
            archetype: ArchetypeType? = null,
            timestamp: Long
        ): CharacterSlot {
            val slotId = CharacterSlotId(generateSlotId(timestamp))
            return CharacterSlot(
                slotId = slotId,
                characterName = characterName,
                archetype = archetype,
                totalPlaytimeSeconds = 0,
                createdAt = timestamp,
                lastPlayedAt = timestamp,
                isDeleted = false,
                displayStats = CharacterDisplayStats()
            )
        }
        
        private fun generateSlotId(timestamp: Long): String {
            // Generate a unique ID using timestamp and random component
            val randomPart = (0..999999).random()
            return "slot_${timestamp}_${randomPart}"
        }
    }
    
    /**
     * Update playtime when character is unloaded.
     */
    fun updatePlaytime(additionalSeconds: Long, timestamp: Long): CharacterSlot {
        return copy(
            totalPlaytimeSeconds = totalPlaytimeSeconds + additionalSeconds,
            lastPlayedAt = timestamp
        )
    }
    
    /**
     * Update display stats from current player state.
     */
    fun updateStats(player: Player): CharacterSlot {
        return copy(
            characterName = player.name,
            archetype = player.archetypeProgress.selectedArchetype,
            displayStats = CharacterDisplayStats(
                hoardValue = player.hoardRank.totalValue,
                hoardTier = player.hoardRank.tier,
                seedCount = player.inventory.totalQuantity(ItemId("seeds")),
                thoughtsInternalized = player.thoughtCabinet.internalized.size,
                archetypeLevel = player.archetypeProgress.archetypeLevel
            )
        )
    }
    
    /**
     * Mark character as deleted (soft delete for restore capability).
     */
    fun markDeleted(): CharacterSlot = copy(isDeleted = true)
    
    /**
     * Restore a soft-deleted character.
     */
    fun restore(): CharacterSlot = copy(isDeleted = false)
}

/**
 * Quick stats displayed on character selection screen.
 */
@Serializable
data class CharacterDisplayStats(
    @SerialName("hoard_value")
    val hoardValue: Long = 0,
    
    @SerialName("hoard_tier")
    val hoardTier: HoardRankTier = HoardRankTier.SCAVENGER,
    
    @SerialName("seed_count")
    val seedCount: Int = 0,
    
    @SerialName("thoughts_internalized")
    val thoughtsInternalized: Int = 0,
    
    @SerialName("archetype_level")
    val archetypeLevel: Int = 1
)

/**
 * Account-level data managing all character slots.
 */
@Serializable
data class CharacterAccount(
    @SerialName("account_id")
    val accountId: String,
    
    @SerialName("character_slots")
    val characterSlots: List<CharacterSlot> = emptyList(),
    
    @SerialName("purchased_extra_slots")
    val purchasedExtraSlots: Int = 0,
    
    @SerialName("current_slot_id")
    val currentSlotId: CharacterSlotId? = null
) {
    /**
     * Maximum slots available (base + purchased).
     */
    fun maxSlots(): Int = CharacterSlot.MAX_BASE_SLOTS + purchasedExtraSlots
    
    /**
     * Get active (non-deleted) character slots.
     */
    fun getActiveSlots(): List<CharacterSlot> {
        return characterSlots.filter { !it.isDeleted }
    }
    
    /**
     * Check if can create a new character slot.
     */
    fun canCreateSlot(): Boolean {
        return getActiveSlots().size < maxSlots()
    }
    
    /**
     * Get a specific character slot.
     */
    fun getSlot(slotId: CharacterSlotId): CharacterSlot? {
        return characterSlots.find { it.slotId == slotId }
    }
    
    /**
     * Add a new character slot.
     */
    fun addSlot(slot: CharacterSlot): CharacterAccount {
        require(canCreateSlot()) { "Maximum character slots reached (${maxSlots()})" }
        return copy(characterSlots = characterSlots + slot)
    }
    
    /**
     * Update an existing character slot.
     */
    fun updateSlot(slotId: CharacterSlotId, updater: (CharacterSlot) -> CharacterSlot): CharacterAccount {
        val updatedSlots = characterSlots.map { slot ->
            if (slot.slotId == slotId) updater(slot) else slot
        }
        return copy(characterSlots = updatedSlots)
    }
    
    /**
     * Set the current active slot.
     */
    fun setCurrentSlot(slotId: CharacterSlotId): CharacterAccount {
        require(getSlot(slotId) != null) { "Character slot not found: $slotId" }
        return copy(currentSlotId = slotId)
    }
    
    /**
     * Purchase additional character slots via IAP.
     */
    fun purchaseExtraSlots(count: Int): CharacterAccount {
        require(count > 0) { "Must purchase at least 1 slot" }
        return copy(purchasedExtraSlots = purchasedExtraSlots + count)
    }
}
