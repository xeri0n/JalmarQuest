package com.jalmarquest.core.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

class CharacterSlotTest {
    
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }
    
    @Test
    fun testCreateCharacterSlot() {
        val timestamp = 1000000L
        val slot = CharacterSlot.create(
            characterName = "Jalmar",
            archetype = ArchetypeType.SCHOLAR,
            timestamp = timestamp
        )
        
        assertEquals("Jalmar", slot.characterName)
        assertEquals(ArchetypeType.SCHOLAR, slot.archetype)
        assertEquals(timestamp, slot.createdAt)
        assertEquals(timestamp, slot.lastPlayedAt)
        assertEquals(0L, slot.totalPlaytimeSeconds)
        assertFalse(slot.isDeleted)
        assertTrue(slot.slotId.value.startsWith("slot_"))
    }
    
    @Test
    fun testUpdatePlaytime() {
        val slot = CharacterSlot.create("Test", null, 1000L)
        val updated = slot.updatePlaytime(additionalSeconds = 3600, timestamp = 2000L)
        
        assertEquals(3600L, updated.totalPlaytimeSeconds)
        assertEquals(2000L, updated.lastPlayedAt)
        assertEquals(1000L, updated.createdAt) // Unchanged
    }
    
    @Test
    fun testUpdateStats() {
        val slot = CharacterSlot.create("Test", null, 1000L)
        val player = Player(
            id = "player-1",
            name = "Jalmar Updated",
            hoardRank = HoardRank(totalValue = 50000, tier = HoardRankTier.CURATOR, shiniesCollected = 10),
            inventory = Inventory(listOf(ItemStack(ItemId("seeds"), 500))),
            thoughtCabinet = ThoughtCabinet(
                internalized = setOf(ThoughtId("thought1"), ThoughtId("thought2"))
            ),
            archetypeProgress = ArchetypeProgress(
                selectedArchetype = ArchetypeType.COLLECTOR,
                archetypeLevel = 5
            )
        )
        
        val updated = slot.updateStats(player)
        
        assertEquals("Jalmar Updated", updated.characterName)
        assertEquals(ArchetypeType.COLLECTOR, updated.archetype)
        assertEquals(50000L, updated.displayStats.hoardValue)
        assertEquals(HoardRankTier.CURATOR, updated.displayStats.hoardTier)
        assertEquals(500, updated.displayStats.seedCount)
        assertEquals(2, updated.displayStats.thoughtsInternalized)
        assertEquals(5, updated.displayStats.archetypeLevel)
    }
    
    @Test
    fun testSoftDelete() {
        val slot = CharacterSlot.create("Test", null, 1000L)
        assertFalse(slot.isDeleted)
        
        val deleted = slot.markDeleted()
        assertTrue(deleted.isDeleted)
        
        val restored = deleted.restore()
        assertFalse(restored.isDeleted)
    }
    
    @Test
    fun testCharacterSlotSerialization() {
        val slot = CharacterSlot.create("Jalmar", ArchetypeType.SCHOLAR, 1000L)
        
        val serialized = json.encodeToString(slot)
        val deserialized = json.decodeFromString<CharacterSlot>(serialized)
        
        assertEquals(slot.slotId, deserialized.slotId)
        assertEquals(slot.characterName, deserialized.characterName)
        assertEquals(slot.archetype, deserialized.archetype)
    }
    
    @Test
    fun testCharacterAccountInitial() {
        val account = CharacterAccount(accountId = "test-account")
        
        assertEquals("test-account", account.accountId)
        assertEquals(0, account.characterSlots.size)
        assertEquals(0, account.purchasedExtraSlots)
        assertEquals(CharacterSlot.MAX_BASE_SLOTS, account.maxSlots())
        assertTrue(account.canCreateSlot())
        assertNull(account.currentSlotId)
    }
    
    @Test
    fun testAddCharacterSlot() {
        val account = CharacterAccount(accountId = "test")
        val slot = CharacterSlot.create("Jalmar", null, 1000L)
        
        val updated = account.addSlot(slot)
        
        assertEquals(1, updated.characterSlots.size)
        assertEquals(slot.slotId, updated.characterSlots[0].slotId)
    }
    
    @Test
    fun testMaxSlotsEnforced() {
        var account = CharacterAccount(accountId = "test")
        
        // Add max base slots (3)
        repeat(CharacterSlot.MAX_BASE_SLOTS) { i ->
            val slot = CharacterSlot.create("Character $i", null, 1000L + i)
            account = account.addSlot(slot)
        }
        
        assertFalse(account.canCreateSlot())
        
        // Attempting to add one more should fail
        val extraSlot = CharacterSlot.create("Extra", null, 2000L)
        assertFailsWith<IllegalArgumentException> {
            account.addSlot(extraSlot)
        }
    }
    
    @Test
    fun testPurchaseExtraSlots() {
        val account = CharacterAccount(accountId = "test")
        
        val updated = account.purchaseExtraSlots(5)
        
        assertEquals(5, updated.purchasedExtraSlots)
        assertEquals(CharacterSlot.MAX_BASE_SLOTS + 5, updated.maxSlots())
        assertTrue(updated.canCreateSlot())
    }
    
    @Test
    fun testGetActiveSlots() {
        var account = CharacterAccount(accountId = "test")
        
        val slot1 = CharacterSlot.create("Active1", null, 1000L)
        val slot2 = CharacterSlot.create("Deleted", null, 2000L).markDeleted()
        val slot3 = CharacterSlot.create("Active2", null, 3000L)
        
        account = account.addSlot(slot1).addSlot(slot2).addSlot(slot3)
        
        val activeSlots = account.getActiveSlots()
        
        assertEquals(2, activeSlots.size)
        assertTrue(activeSlots.none { it.isDeleted })
    }
    
    @Test
    fun testUpdateSlot() {
        var account = CharacterAccount(accountId = "test")
        val slot = CharacterSlot.create("Original", null, 1000L)
        account = account.addSlot(slot)
        
        val updated = account.updateSlot(slot.slotId) { it.copy(characterName = "Updated") }
        
        assertEquals("Updated", updated.getSlot(slot.slotId)?.characterName)
    }
    
    @Test
    fun testSetCurrentSlot() {
        var account = CharacterAccount(accountId = "test")
        val slot = CharacterSlot.create("Character", null, 1000L)
        account = account.addSlot(slot)
        
        val updated = account.setCurrentSlot(slot.slotId)
        
        assertEquals(slot.slotId, updated.currentSlotId)
    }
    
    @Test
    fun testSetCurrentSlotNotFound() {
        val account = CharacterAccount(accountId = "test")
        val invalidSlotId = CharacterSlotId("invalid")
        
        assertFailsWith<IllegalArgumentException> {
            account.setCurrentSlot(invalidSlotId)
        }
    }
    
    @Test
    fun testCharacterAccountSerialization() {
        val slot1 = CharacterSlot.create("Char1", ArchetypeType.SCHOLAR, 1000L)
        val slot2 = CharacterSlot.create("Char2", ArchetypeType.WARRIOR, 2000L)
        
        val account = CharacterAccount(
            accountId = "test-account",
            characterSlots = listOf(slot1, slot2),
            purchasedExtraSlots = 3,
            currentSlotId = slot1.slotId
        )
        
        val serialized = json.encodeToString(account)
        val deserialized = json.decodeFromString<CharacterAccount>(serialized)
        
        assertEquals(account.accountId, deserialized.accountId)
        assertEquals(account.characterSlots.size, deserialized.characterSlots.size)
        assertEquals(account.purchasedExtraSlots, deserialized.purchasedExtraSlots)
        assertEquals(account.currentSlotId, deserialized.currentSlotId)
    }
    
    @Test
    fun testDeletedSlotsCountTowardLimit() {
        var account = CharacterAccount(accountId = "test")
        
        // Add 2 active slots + 1 deleted
        repeat(2) { i ->
            val slot = CharacterSlot.create("Active $i", null, 1000L + i)
            account = account.addSlot(slot)
        }
        
        val deletedSlot = CharacterSlot.create("Deleted", null, 3000L).markDeleted()
        account = account.addSlot(deletedSlot)
        
        // Should have 1 slot available (2 active + 1 deleted out of 3 max)
        assertTrue(account.canCreateSlot())
        
        val lastSlot = CharacterSlot.create("Last", null, 4000L)
        account = account.addSlot(lastSlot)
        
        // Now at max capacity
        assertFalse(account.canCreateSlot())
        assertEquals(3, account.getActiveSlots().size) // 2 + 1 new (deleted one not counted)
    }
}
