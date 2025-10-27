package com.jalmarquest.core.state.hoard

import kotlin.test.*

class LeaderboardServiceTest {
    
    private lateinit var service: LeaderboardService
    
    @BeforeTest
    fun setup() {
        service = LeaderboardService()
    }
    
    @Test
    fun testInitialState() {
        assertEquals(0, service.getTotalPlayers())
        assertTrue(service.getTopPlayers().isEmpty())
    }
    
    @Test
    fun testUpdatePlayerEntry() {
        service.updatePlayerEntry(
            playerId = "player1",
            playerName = "Alice",
            hoardValue = 1000,
            shiniesCollected = 5,
            timestamp = 1000L
        )
        
        assertEquals(1, service.getTotalPlayers())
        assertEquals(1, service.getPlayerRank("player1"))
        
        val entry = service.getPlayerEntry("player1")
        assertNotNull(entry)
        assertEquals("Alice", entry.playerName)
        assertEquals(1000, entry.hoardValue)
        assertEquals(5, entry.shiniesCollected)
    }
    
    @Test
    fun testRankCalculation() {
        service.updatePlayerEntry("player1", "Alice", 1000, 5, 1000L)
        service.updatePlayerEntry("player2", "Bob", 2000, 10, 1001L)
        service.updatePlayerEntry("player3", "Charlie", 500, 3, 1002L)
        
        assertEquals(1, service.getPlayerRank("player2"), "Bob should be rank 1 (highest value)")
        assertEquals(2, service.getPlayerRank("player1"), "Alice should be rank 2")
        assertEquals(3, service.getPlayerRank("player3"), "Charlie should be rank 3 (lowest value)")
    }
    
    @Test
    fun testGetTopPlayers() {
        service.updatePlayerEntry("player1", "Alice", 1000, 5, 1000L)
        service.updatePlayerEntry("player2", "Bob", 2000, 10, 1001L)
        service.updatePlayerEntry("player3", "Charlie", 500, 3, 1002L)
        service.updatePlayerEntry("player4", "Diana", 1500, 7, 1003L)
        
        val top3 = service.getTopPlayers(limit = 3)
        
        assertEquals(3, top3.size)
        assertEquals("Bob", top3[0].playerName)
        assertEquals("Diana", top3[1].playerName)
        assertEquals("Alice", top3[2].playerName)
    }
    
    @Test
    fun testUpdateExistingPlayer() {
        service.updatePlayerEntry("player1", "Alice", 1000, 5, 1000L)
        service.updatePlayerEntry("player2", "Bob", 2000, 10, 1001L)
        
        assertEquals(2, service.getPlayerRank("player1"))
        
        // Alice acquires more shinies and increases value
        service.updatePlayerEntry("player1", "Alice", 3000, 15, 2000L)
        
        assertEquals(1, service.getPlayerRank("player1"), "Alice should now be rank 1")
        assertEquals(2, service.getPlayerRank("player2"), "Bob should now be rank 2")
    }
    
    @Test
    fun testGetNonexistentPlayer() {
        assertEquals(0, service.getPlayerRank("nonexistent"))
        assertNull(service.getPlayerEntry("nonexistent"))
    }
    
    @Test
    fun testGetPlayersNearRank() {
        for (i in 1..10) {
            service.updatePlayerEntry(
                "player$i",
                "Player$i",
                (11 - i) * 1000L, // Descending values
                i,
                1000L + i
            )
        }
        
        val nearPlayer5 = service.getPlayersNearRank("player5", context = 2)
        
        assertEquals(5, nearPlayer5.size, "Should get player5 ± 2 ranks")
        assertTrue(nearPlayer5.any { it.playerId == "player3" }) // Rank 3
        assertTrue(nearPlayer5.any { it.playerId == "player4" }) // Rank 4
        assertTrue(nearPlayer5.any { it.playerId == "player5" }) // Rank 5
        assertTrue(nearPlayer5.any { it.playerId == "player6" }) // Rank 6
        assertTrue(nearPlayer5.any { it.playerId == "player7" }) // Rank 7
    }
    
    @Test
    fun testGetPlayersNearRankEdgeCase() {
        service.updatePlayerEntry("player1", "Alice", 1000, 5, 1000L)
        
        val near = service.getPlayersNearRank("player1", context = 5)
        
        assertEquals(1, near.size)
        assertEquals("player1", near[0].playerId)
    }
    
    @Test
    fun testTieHandling() {
        service.updatePlayerEntry("player1", "Alice", 1000, 5, 1000L)
        service.updatePlayerEntry("player2", "Bob", 1000, 5, 1001L)
        service.updatePlayerEntry("player3", "Charlie", 2000, 10, 1002L)
        
        // Ties are handled by insertion order (stable sort)
        val ranks = listOf(
            service.getPlayerRank("player1"),
            service.getPlayerRank("player2"),
            service.getPlayerRank("player3")
        )
        
        assertTrue(ranks.contains(1), "Charlie should be rank 1")
        assertTrue(ranks.contains(2) || ranks.contains(3), "Alice and Bob should be ranks 2-3")
        assertEquals(3, service.getTotalPlayers())
    }
    
    @Test
    fun testClear() {
        service.updatePlayerEntry("player1", "Alice", 1000, 5, 1000L)
        service.updatePlayerEntry("player2", "Bob", 2000, 10, 1001L)
        
        assertEquals(2, service.getTotalPlayers())
        
        service.clear()
        
        assertEquals(0, service.getTotalPlayers())
        assertTrue(service.getTopPlayers().isEmpty())
    }
    
    @Test
    fun testLargeLeaderboard() {
        // Add 1000 players
        for (i in 1..1000) {
            service.updatePlayerEntry(
                "player$i",
                "Player$i",
                i.toLong() * 100,
                i,
                1000L + i
            )
        }
        
        assertEquals(1000, service.getTotalPlayers())
        
        val top10 = service.getTopPlayers(limit = 10)
        assertEquals(10, top10.size)
        
        // Highest value should be rank 1
        assertEquals("player1000", top10[0].playerId)
        assertEquals(100000, top10[0].hoardValue)
        assertEquals(1, top10[0].rank)
    }
}
