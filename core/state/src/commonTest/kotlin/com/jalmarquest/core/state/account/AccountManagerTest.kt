package com.jalmarquest.core.state.account

import com.jalmarquest.core.model.ArchetypeProgress
import com.jalmarquest.core.model.ArchetypeType
import com.jalmarquest.core.model.CharacterAccount
import com.jalmarquest.core.model.CharacterDisplayStats
import com.jalmarquest.core.model.CharacterSlot
import com.jalmarquest.core.model.CharacterSlotId
import com.jalmarquest.core.model.HoardRank
import com.jalmarquest.core.model.HoardRankTier
import com.jalmarquest.core.model.Inventory
import com.jalmarquest.core.model.ItemId
import com.jalmarquest.core.model.ItemStack
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.model.ThoughtCabinet
import com.jalmarquest.core.model.ThoughtId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AccountManagerTest {
    private var currentTime = 1000000L
    private val timestampProvider: () -> Long = { currentTime }

    private fun createTestAccount(): CharacterAccount {
        return CharacterAccount(accountId = "test-account-123")
    }

    private fun createTestPlayer(
        seedCount: Int = 100,
        hoardValue: Long = 5000,
        thoughtsCount: Int = 3,
        archetypeLevel: Int = 5
    ): Player {
        return Player(
            id = "test-player-id",
            name = "Test Hero",
            inventory = Inventory(
                listOf(ItemStack(id = ItemId("seeds"), quantity = seedCount))
            ),
            hoardRank = HoardRank(
                totalValue = hoardValue,
                tier = HoardRankTier.CURATOR,
                shiniesCollected = 10
            ),
            thoughtCabinet = ThoughtCabinet(
                internalized = (0 until thoughtsCount).map { ThoughtId("thought_$it") }.toSet()
            ),
            archetypeProgress = ArchetypeProgress(
                selectedArchetype = ArchetypeType.SCHOLAR,
                archetypeLevel = archetypeLevel,
                archetypeXP = 0,
                availableTalentPoints = 0
            )
        )
    }

    @Test
    fun testCreateCharacter() = runTest {
        val account = createTestAccount()
        val manager = AccountManager(account, timestampProvider)

        val slotId = manager.createCharacter("Hero", ArchetypeType.WARRIOR)
        assertNotNull(slotId, "Should successfully create character")

        val characters = manager.listCharacters()
        assertEquals(1, characters.size, "Should have 1 character")
        assertEquals("Hero", characters[0].characterName)
        assertEquals(ArchetypeType.WARRIOR, characters[0].archetype)
        assertEquals(slotId, manager.getCurrentCharacter()?.slotId, "New character should be current")
    }

    @Test
    fun testCreateCharacterMaxSlotsReached() = runTest {
        val account = createTestAccount()
        val manager = AccountManager(account, timestampProvider)

        // Create max base slots (3)
        val slot1 = manager.createCharacter("Hero1", ArchetypeType.WARRIOR)
        val slot2 = manager.createCharacter("Hero2", ArchetypeType.SCHOLAR)
        val slot3 = manager.createCharacter("Hero3", ArchetypeType.COLLECTOR)
        
        assertNotNull(slot1)
        assertNotNull(slot2)
        assertNotNull(slot3)

        // Attempt to create 4th character should fail
        val slot4 = manager.createCharacter("Hero4", ArchetypeType.ALCHEMIST)
        assertNull(slot4, "Should not create character when max slots reached")
        
        assertEquals(3, manager.listCharacters().size, "Should still have exactly 3 characters")
    }

    @Test
    fun testPurchaseExtraSlotsAllowsMoreCharacters() = runTest {
        val account = createTestAccount()
        val manager = AccountManager(account, timestampProvider)

        // Fill base slots
        manager.createCharacter("Hero1", ArchetypeType.WARRIOR)
        manager.createCharacter("Hero2", ArchetypeType.SCHOLAR)
        manager.createCharacter("Hero3", ArchetypeType.COLLECTOR)

        // Purchase 2 extra slots
        assertTrue(manager.purchaseExtraSlots(2), "Should purchase extra slots")
        assertEquals(5, manager.getAccount().maxSlots(), "Should have 5 max slots now")

        // Should now be able to create more characters
        val slot4 = manager.createCharacter("Hero4", ArchetypeType.ALCHEMIST)
        val slot5 = manager.createCharacter("Hero5", ArchetypeType.SCAVENGER)
        
        assertNotNull(slot4, "Should create 4th character after purchasing slots")
        assertNotNull(slot5, "Should create 5th character after purchasing slots")
        assertEquals(5, manager.listCharacters().size, "Should have 5 characters")
    }

    @Test
    fun testDeleteCharacter() = runTest {
        val account = createTestAccount()
        val manager = AccountManager(account, timestampProvider)

        val slotId = manager.createCharacter("Hero", ArchetypeType.WARRIOR)
        assertNotNull(slotId)

        assertTrue(manager.deleteCharacter(slotId), "Should successfully delete character")
        assertEquals(0, manager.listCharacters().size, "Should have no active characters")
        
        // Character should still exist but be marked deleted
        val allSlots = manager.getAccount().characterSlots
        assertEquals(1, allSlots.size, "Deleted character should still exist")
        assertTrue(allSlots[0].isDeleted, "Character should be marked as deleted")
    }

    @Test
    fun testDeleteNonexistentCharacter() = runTest {
        val account = createTestAccount()
        val manager = AccountManager(account, timestampProvider)

        val fakeSlotId = CharacterSlotId("nonexistent-slot")
        assertFalse(manager.deleteCharacter(fakeSlotId), "Should fail to delete nonexistent character")
    }

    @Test
    fun testDeleteCurrentCharacterSwitchesToNextActive() = runTest {
        val account = createTestAccount()
        val manager = AccountManager(account, timestampProvider)

        currentTime = 1000000
        val slot1 = manager.createCharacter("Hero1", ArchetypeType.WARRIOR)!!
        currentTime = 2000000
        val slot2 = manager.createCharacter("Hero2", ArchetypeType.SCHOLAR)!!
        currentTime = 3000000
        val slot3 = manager.createCharacter("Hero3", ArchetypeType.COLLECTOR)!!

        // Switch to slot2, then delete it
        manager.switchCharacter(slot2)
        assertEquals(slot2, manager.getCurrentCharacter()?.slotId, "Should be on slot2")

        manager.deleteCharacter(slot2)
        
        // Should auto-switch to another active character (most recent: slot3)
        val current = manager.getCurrentCharacter()
        assertNotNull(current, "Should have auto-switched to another character")
        assertTrue(current.slotId == slot1 || current.slotId == slot3, "Should switch to slot1 or slot3")
    }

    @Test
    fun testRestoreCharacter() = runTest {
        val account = createTestAccount()
        val manager = AccountManager(account, timestampProvider)

        val slotId = manager.createCharacter("Hero", ArchetypeType.WARRIOR)!!
        manager.deleteCharacter(slotId)
        
        assertEquals(0, manager.listCharacters().size, "Should have no active characters")
        
        assertTrue(manager.restoreCharacter(slotId), "Should successfully restore character")
        assertEquals(1, manager.listCharacters().size, "Should have 1 active character after restore")
        assertEquals("Hero", manager.listCharacters()[0].characterName)
    }

    @Test
    fun testRestoreNonDeletedCharacter() = runTest {
        val account = createTestAccount()
        val manager = AccountManager(account, timestampProvider)

        val slotId = manager.createCharacter("Hero", ArchetypeType.WARRIOR)!!
        
        // Attempt to restore a non-deleted character should fail
        assertFalse(manager.restoreCharacter(slotId), "Should not restore non-deleted character")
    }

    @Test
    fun testListCharactersOrderedByLastPlayed() = runTest {
        val account = createTestAccount()
        val manager = AccountManager(account, timestampProvider)

        currentTime = 1000000
        val slot1 = manager.createCharacter("Hero1", ArchetypeType.WARRIOR)!!
        currentTime = 2000000
        val slot2 = manager.createCharacter("Hero2", ArchetypeType.SCHOLAR)!!
        currentTime = 3000000
        val slot3 = manager.createCharacter("Hero3", ArchetypeType.COLLECTOR)!!

        // Switch to slot1 (updates lastPlayedAt)
        currentTime = 5000000
        manager.switchCharacter(slot1)

        val characters = manager.listCharacters()
        assertEquals(3, characters.size)
        // slot1 should be first (most recent), then slot3, then slot2
        assertEquals("Hero1", characters[0].characterName, "slot1 should be first (most recently played)")
        assertEquals("Hero3", characters[1].characterName, "slot3 should be second")
        assertEquals("Hero2", characters[2].characterName, "slot2 should be last")
    }

    @Test
    fun testSwitchCharacter() = runTest {
        val account = createTestAccount()
        val manager = AccountManager(account, timestampProvider)

        val slot1 = manager.createCharacter("Hero1", ArchetypeType.WARRIOR)!!
        val slot2 = manager.createCharacter("Hero2", ArchetypeType.SCHOLAR)!!

        assertEquals(slot2, manager.getCurrentCharacter()?.slotId, "Should start on slot2")
        
        currentTime = 5000000
        assertTrue(manager.switchCharacter(slot1), "Should successfully switch to slot1")
        assertEquals(slot1, manager.getCurrentCharacter()?.slotId, "Should now be on slot1")
        
        // Check that lastPlayedAt was updated
        val slot1Data = manager.getAccount().characterSlots.find { it.slotId == slot1 }
        assertEquals(5000000, slot1Data?.lastPlayedAt, "lastPlayedAt should be updated on switch")
    }

    @Test
    fun testSwitchToDeletedCharacterFails() = runTest {
        val account = createTestAccount()
        val manager = AccountManager(account, timestampProvider)

        val slot1 = manager.createCharacter("Hero1", ArchetypeType.WARRIOR)!!
        val slot2 = manager.createCharacter("Hero2", ArchetypeType.SCHOLAR)!!

        manager.deleteCharacter(slot1)
        assertFalse(manager.switchCharacter(slot1), "Should not switch to deleted character")
        assertEquals(slot2, manager.getCurrentCharacter()?.slotId, "Should still be on slot2")
    }

    @Test
    fun testUpdateCharacterMetadata() = runTest {
        val account = createTestAccount()
        val manager = AccountManager(account, timestampProvider)

        val slotId = manager.createCharacter("Hero", ArchetypeType.WARRIOR)!!
        val player = createTestPlayer(
            seedCount = 500,
            hoardValue = 15000,
            thoughtsCount = 7,
            archetypeLevel = 10
        )

        currentTime = 2000000
        assertTrue(manager.updateCharacterMetadata(slotId, player, additionalPlaytimeSeconds = 3600))

        val character = manager.getCurrentCharacter()
        assertNotNull(character)
        assertEquals(3600, character.totalPlaytimeSeconds, "Playtime should be updated")
        assertEquals(500, character.displayStats.seedCount, "Seed count should match player")
        assertEquals(15000, character.displayStats.hoardValue, "Hoard value should match player")
        assertEquals(HoardRankTier.CURATOR, character.displayStats.hoardTier, "Hoard tier should match player")
        assertEquals(7, character.displayStats.thoughtsInternalized, "Thoughts count should match player")
        assertEquals(10, character.displayStats.archetypeLevel, "Archetype level should match player")
        assertEquals(2000000, character.lastPlayedAt, "lastPlayedAt should be updated")
    }

    @Test
    fun testUpdateMetadataAccumulatesPlaytime() = runTest {
        val account = createTestAccount()
        val manager = AccountManager(account, timestampProvider)

        val slotId = manager.createCharacter("Hero", ArchetypeType.WARRIOR)!!
        val player = createTestPlayer()

        manager.updateCharacterMetadata(slotId, player, additionalPlaytimeSeconds = 1000)
        manager.updateCharacterMetadata(slotId, player, additionalPlaytimeSeconds = 500)
        manager.updateCharacterMetadata(slotId, player, additionalPlaytimeSeconds = 250)

        val character = manager.getCurrentCharacter()
        assertEquals(1750, character?.totalPlaytimeSeconds, "Playtime should accumulate")
    }

    @Test
    fun testUpdateMetadataForDeletedCharacterFails() = runTest {
        val account = createTestAccount()
        val manager = AccountManager(account, timestampProvider)

        val slotId = manager.createCharacter("Hero", ArchetypeType.WARRIOR)!!
        manager.deleteCharacter(slotId)
        
        val player = createTestPlayer()
        assertFalse(manager.updateCharacterMetadata(slotId, player), "Should not update deleted character")
    }

    @Test
    fun testLoadAccount() = runTest {
        val manager = AccountManager(createTestAccount(), timestampProvider)

        // Create some characters
        manager.createCharacter("Hero1", ArchetypeType.WARRIOR)
        manager.createCharacter("Hero2", ArchetypeType.SCHOLAR)
        
        // Serialize account
        val savedAccount = manager.getAccount()
        
        // Create new manager and load saved account
        val newManager = AccountManager(createTestAccount(), timestampProvider)
        newManager.loadAccount(savedAccount)
        
        // Verify state was restored
        assertEquals(2, newManager.listCharacters().size, "Should have 2 characters after load")
        assertEquals("Hero1", newManager.listCharacters().find { it.characterName == "Hero1" }?.characterName)
        assertEquals("Hero2", newManager.listCharacters().find { it.characterName == "Hero2" }?.characterName)
    }

    @Test
    fun testGetCurrentCharacterReturnsNullWhenNoCharacters() = runTest {
        val account = createTestAccount()
        val manager = AccountManager(account, timestampProvider)

        assertNull(manager.getCurrentCharacter(), "Should return null when no characters exist")
    }

    @Test
    fun testDeletedSlotsCountTowardLimit() = runTest {
        val account = createTestAccount()
        val manager = AccountManager(account, timestampProvider)

        // Create 2 characters
        val slot1 = manager.createCharacter("Hero1", ArchetypeType.WARRIOR)!!
        val slot2 = manager.createCharacter("Hero2", ArchetypeType.SCHOLAR)!!
        
        // Delete one - this frees up a slot (now 1 active: slot2)
        manager.deleteCharacter(slot1)
        
        // Should be able to create 2 more characters to reach max of 3 active
        val slot3 = manager.createCharacter("Hero3", ArchetypeType.COLLECTOR)
        assertNotNull(slot3, "Should create 3rd character (2 active now)")
        
        val slot4 = manager.createCharacter("Hero4", ArchetypeType.ALCHEMIST)
        assertNotNull(slot4, "Should create 4th character (3 active now = max)")
        
        // Now at limit (3 active slots: slot2, slot3, slot4)
        val slot5 = manager.createCharacter("Hero5", ArchetypeType.SCAVENGER)
        assertNull(slot5, "Should not create 5th character (already at 3 active slots = max)")
        
        // Verify we have 3 active and 1 deleted
        assertEquals(3, manager.listCharacters().size, "Should have 3 active characters")
        assertEquals(4, manager.getAccount().characterSlots.size, "Should have 4 total slots (3 active + 1 deleted)")
    }

    @Test
    fun testPurchaseZeroOrNegativeSlotsReturnsFalse() = runTest {
        val account = createTestAccount()
        val manager = AccountManager(account, timestampProvider)

        assertFalse(manager.purchaseExtraSlots(0), "Should not purchase 0 slots")
        assertFalse(manager.purchaseExtraSlots(-5), "Should not purchase negative slots")
        assertEquals(3, manager.getAccount().maxSlots(), "Max slots should remain at base value")
    }
}
