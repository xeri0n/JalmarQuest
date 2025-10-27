package com.jalmarquest.core.state

import com.jalmarquest.core.model.ArchetypeType
import com.jalmarquest.core.model.CharacterAccount
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.state.account.AccountManager
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GameStateManagerCharacterSlotTest {
    private var currentTime = 1000000L
    private val timestampProvider: () -> Long = { currentTime }

    private fun createTestPlayer(): Player = Player(
        id = "test-player",
        name = "Test Hero"
    )

    @Test
    fun testSavePlayerToCurrentSlotWithNoAccountManager() = runTest {
        val player = createTestPlayer()
        val manager = GameStateManager(player, accountManager = null) { timestampProvider() }
        
        val result = manager.savePlayerToCurrentSlot()
        assertFalse(result, "Should return false when no AccountManager")
    }

    @Test
    fun testSavePlayerToCurrentSlotWithNoCurrentCharacter() = runTest {
        val player = createTestPlayer()
        val account = CharacterAccount(accountId = "test-account")
        val accountManager = AccountManager(account, timestampProvider)
        val gameManager = GameStateManager(player, accountManager) { timestampProvider() }
        
        val result = gameManager.savePlayerToCurrentSlot()
        assertFalse(result, "Should return false when no current character slot")
    }

    @Test
    fun testSavePlayerToCurrentSlotSuccess() = runTest {
        val player = createTestPlayer()
        val account = CharacterAccount(accountId = "test-account")
        val accountManager = AccountManager(account, timestampProvider)
        
        // Create a character slot
        currentTime = 1000000
        val slotId = accountManager.createCharacter("Hero", ArchetypeType.WARRIOR)
        assertNotNull(slotId)
        
        val gameManager = GameStateManager(player, accountManager) { timestampProvider() }
        
        // Simulate 60 seconds of play time
        currentTime = 1060000
        
        val result = gameManager.savePlayerToCurrentSlot()
        assertTrue(result, "Should successfully save player")
        
        // Verify playtime was recorded
        val slot = accountManager.getCurrentCharacter()
        assertNotNull(slot)
        assertEquals(60, slot.totalPlaytimeSeconds, "Should have 60 seconds playtime")
    }

    @Test
    fun testSavePlayerAccumulatesPlaytime() = runTest {
        val player = createTestPlayer()
        val account = CharacterAccount(accountId = "test-account")
        val accountManager = AccountManager(account, timestampProvider)
        
        currentTime = 1000000
        val slotId = accountManager.createCharacter("Hero", ArchetypeType.WARRIOR)
        assertNotNull(slotId)
        
        val gameManager = GameStateManager(player, accountManager) { timestampProvider() }
        
        // First save after 30 seconds
        currentTime = 1030000
        gameManager.savePlayerToCurrentSlot()
        
        // Second save after another 45 seconds
        currentTime = 1075000
        gameManager.savePlayerToCurrentSlot()
        
        // Verify total playtime
        val slot = accountManager.getCurrentCharacter()
        assertNotNull(slot)
        assertEquals(75, slot.totalPlaytimeSeconds, "Should have 75 seconds total playtime (30 + 45)")
    }

    @Test
    fun testSwitchToCharacterSlotWithNoAccountManager() = runTest {
        val player = createTestPlayer()
        val manager = GameStateManager(player, accountManager = null) { timestampProvider() }
        
        val result = manager.switchToCharacterSlot(com.jalmarquest.core.model.CharacterSlotId("fake"))
        assertFalse(result, "Should return false when no AccountManager")
    }

    @Test
    fun testSwitchToCharacterSlotSuccess() = runTest {
        val player = createTestPlayer()
        val account = CharacterAccount(accountId = "test-account")
        val accountManager = AccountManager(account, timestampProvider)
        
        // Create two characters
        currentTime = 1000000
        val slot1 = accountManager.createCharacter("Hero1", ArchetypeType.WARRIOR)
        currentTime = 2000000
        val slot2 = accountManager.createCharacter("Hero2", ArchetypeType.SCHOLAR)
        
        assertNotNull(slot1)
        assertNotNull(slot2)
        
        val gameManager = GameStateManager(player, accountManager) { timestampProvider() }
        
        // Simulate gameplay on slot2 (current slot)
        currentTime = 2030000 // 30 seconds of play
        
        // Switch to slot1
        val result = gameManager.switchToCharacterSlot(slot1)
        assertTrue(result, "Should successfully switch to slot1")
        
        // Verify slot2 has saved playtime
        val slot2Data = accountManager.getAccount().characterSlots.find { it.slotId == slot2 }
        assertNotNull(slot2Data)
        assertEquals(30, slot2Data.totalPlaytimeSeconds, "Slot2 should have 30 seconds playtime")
        
        // Verify current slot is now slot1
        assertEquals(slot1, accountManager.getCurrentCharacter()?.slotId)
    }

    @Test
    fun testGetCurrentSessionPlaytime() = runTest {
        val player = createTestPlayer()
        val account = CharacterAccount(accountId = "test-account")
        val accountManager = AccountManager(account, timestampProvider)
        
        currentTime = 1000000
        accountManager.createCharacter("Hero", ArchetypeType.WARRIOR)
        
        val gameManager = GameStateManager(player, accountManager) { timestampProvider() }
        
        // Simulate 90 seconds of play
        currentTime = 1090000
        
        val sessionTime = gameManager.getCurrentSessionPlaytime()
        assertEquals(90, sessionTime, "Should return 90 seconds of session playtime")
    }

    @Test
    fun testSessionPlaytimeResetsAfterSave() = runTest {
        val player = createTestPlayer()
        val account = CharacterAccount(accountId = "test-account")
        val accountManager = AccountManager(account, timestampProvider)
        
        currentTime = 1000000
        accountManager.createCharacter("Hero", ArchetypeType.WARRIOR)
        
        val gameManager = GameStateManager(player, accountManager) { timestampProvider() }
        
        // Play for 60 seconds
        currentTime = 1060000
        assertEquals(60, gameManager.getCurrentSessionPlaytime())
        
        // Save
        gameManager.savePlayerToCurrentSlot()
        
        // Session timer should reset
        assertEquals(0, gameManager.getCurrentSessionPlaytime())
        
        // Play for another 30 seconds
        currentTime = 1090000
        assertEquals(30, gameManager.getCurrentSessionPlaytime())
    }

    @Test
    fun testSwitchToNonexistentSlotFails() = runTest {
        val player = createTestPlayer()
        val account = CharacterAccount(accountId = "test-account")
        val accountManager = AccountManager(account, timestampProvider)
        
        currentTime = 1000000
        accountManager.createCharacter("Hero", ArchetypeType.WARRIOR)
        
        val gameManager = GameStateManager(player, accountManager) { timestampProvider() }
        
        val fakeSlotId = com.jalmarquest.core.model.CharacterSlotId("nonexistent")
        val result = gameManager.switchToCharacterSlot(fakeSlotId)
        assertFalse(result, "Should return false for nonexistent slot")
    }
}
