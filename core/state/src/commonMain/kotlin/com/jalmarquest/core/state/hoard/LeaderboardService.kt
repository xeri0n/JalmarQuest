package com.jalmarquest.core.state.hoard

import kotlinx.serialization.Serializable

/**
 * Leaderboard entry for a player's Hoard Rank.
 */
@Serializable
data class LeaderboardEntry(
    val playerId: String,
    val playerName: String,
    val hoardValue: Long,
    val shiniesCollected: Int,
    val rank: Int,
    val lastUpdated: Long
)

/**
 * Leaderboard service for managing player rankings.
 * Vertical slice implementation uses in-memory storage for local/guest players.
 * Post-launch: Replace with backend API integration for global leaderboards.
 */
class LeaderboardService {
    
    private val entries = mutableMapOf<String, LeaderboardEntry>()
    
    /**
     * Update a player's leaderboard entry.
     * Recalculates ranks after update.
     */
    fun updatePlayerEntry(
        playerId: String,
        playerName: String,
        hoardValue: Long,
        shiniesCollected: Int,
        timestamp: Long
    ) {
        entries[playerId] = LeaderboardEntry(
            playerId = playerId,
            playerName = playerName,
            hoardValue = hoardValue,
            shiniesCollected = shiniesCollected,
            rank = 0, // Will be recalculated
            lastUpdated = timestamp
        )
        recalculateRanks()
    }
    
    /**
     * Get the top N players by hoard value.
     */
    fun getTopPlayers(limit: Int = 100): List<LeaderboardEntry> {
        return entries.values
            .sortedByDescending { it.hoardValue }
            .take(limit)
    }
    
    /**
     * Get a specific player's rank.
     * Returns 0 if player is not on the leaderboard.
     */
    fun getPlayerRank(playerId: String): Int {
        return entries[playerId]?.rank ?: 0
    }
    
    /**
     * Get a player's leaderboard entry.
     */
    fun getPlayerEntry(playerId: String): LeaderboardEntry? {
        return entries[playerId]
    }
    
    /**
     * Get players around a specific player's rank (for context).
     * Returns the player and N entries above/below them.
     */
    fun getPlayersNearRank(playerId: String, context: Int = 5): List<LeaderboardEntry> {
        val entry = entries[playerId] ?: return emptyList()
        val rank = entry.rank
        
        return entries.values
            .sortedBy { it.rank }
            .filter { it.rank in (rank - context)..(rank + context) }
    }
    
    /**
     * Clear all entries (for testing).
     */
    fun clear() {
        entries.clear()
    }
    
    /**
     * Get total number of players on leaderboard.
     */
    fun getTotalPlayers(): Int = entries.size
    
    private fun recalculateRanks() {
        val sorted = entries.values.sortedByDescending { it.hoardValue }
        
        sorted.forEachIndexed { index, entry ->
            entries[entry.playerId] = entry.copy(rank = index + 1)
        }
    }
}
