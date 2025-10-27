package com.jalmarquest.core.state.hoard

import com.jalmarquest.core.model.HoardRank
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.model.Shiny
import com.jalmarquest.core.model.ShinyCollection
import com.jalmarquest.core.model.ShinyId
import com.jalmarquest.core.model.AbilityType
import com.jalmarquest.core.model.TalentType
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.perf.PerformanceLogger
import com.jalmarquest.core.state.skills.SkillManager
import com.jalmarquest.core.state.archetype.ArchetypeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Manages Hoard Rank state including Shiny collection, tier progression, and Seed sink operations.
 * Integrates with GameStateManager for persistent player state.
 * Applies Hoarding skill bonuses and archetype talent bonuses to Shiny values.
 */
class HoardRankManager(
    private val gameStateManager: GameStateManager,
    private val valuationService: ShinyValuationService,
    private val leaderboardService: LeaderboardService,
    private val timestampProvider: () -> Long,
    private val skillManager: SkillManager? = null,
    private val archetypeManager: ArchetypeManager? = null
) {
    private val _viewState = MutableStateFlow(HoardViewState())
    val viewState: StateFlow<HoardViewState> = _viewState
    
    init {
        // Initialize view state from player state
        val player = gameStateManager.playerState.value
        refreshViewState(player)
    }
    
    /**
     * Acquire a Shiny and add it to the player's collection.
     * Returns true if successful (new Shiny), false if already owned.
     */
    fun acquireShiny(shinyId: ShinyId): Boolean {
        val shiny = valuationService.getShiny(shinyId) ?: return false
        val player = gameStateManager.playerState.value
        
        if (player.shinyCollection.hasShiny(shinyId)) {
            return false // Already owned
        }
        
        val now = timestampProvider()
        val updatedCollection = player.shinyCollection.addShiny(shiny, now)
        val updatedRank = recalculateHoardRank(updatedCollection)
        
        gameStateManager.updatePlayer { it.copy(
            shinyCollection = updatedCollection,
            hoardRank = updatedRank
        )}
        
        gameStateManager.appendChoice("hoard_shiny_acquired_${shinyId.value}")
        PerformanceLogger.logStateMutation("Hoard", "acquireShiny", mapOf(
            "shinyId" to shinyId.value,
            "rarity" to shiny.rarity.name,
            "newTotal" to updatedRank.totalValue
        ))
        
        refreshViewState(gameStateManager.playerState.value)
        return true
    }
    
    /**
     * Purchase a Shiny from the Pack Rat's Hoard using Seeds (Seed sink).
     * Returns true if purchase successful, false if insufficient Seeds or already owned.
     */
    fun purchaseShiny(shinyId: ShinyId): Boolean {
        val shiny = valuationService.getShiny(shinyId) ?: return false
        val player = gameStateManager.playerState.value
        
        if (player.shinyCollection.hasShiny(shinyId)) {
            return false // Already owned
        }
        
        // Check if player has enough Seeds (using inventory  quantity check)
        // For vertical slice, we'll use a simplified check
        // Future: Implement proper Seed currency tracking in inventory
        
        val success = acquireShiny(shinyId)
        if (success) {
            gameStateManager.appendChoice("hoard_shiny_purchased_${shinyId.value}")
        }
        
        return success
    }
    
    /**
     * Refresh the view state based on current player state.
     */
    fun refreshViewState(player: Player = gameStateManager.playerState.value) {
        val progress = valuationService.calculateTierProgress(player.hoardRank)
        val nextThreshold = HoardRank.nextTierThreshold(player.hoardRank.tier)
        
        _viewState.update {
            HoardViewState(
                collection = player.shinyCollection,
                rank = player.hoardRank,
                tierProgress = progress ?: 1f,
                nextTierThreshold = nextThreshold,
                catalog = valuationService.getAllShinies()
            )
        }
    }
    
    /**
     * Recalculate Hoard Rank with Hoarding skill bonuses and archetype bonuses applied to total value.
     */
    private fun recalculateHoardRank(collection: ShinyCollection): HoardRank {
        val baseValue = valuationService.calculateTotalValue(collection)
        
        // Apply Hoarding skill bonus (10% per bonus point)
        val hoardingBonus = skillManager?.getTotalBonus(AbilityType.HOARD_VALUE_BONUS) ?: 0
        
        // Apply archetype HOARD_VALUE_BONUS talent
        val archetypeBonus = archetypeManager?.getTotalBonus(TalentType.HOARD_VALUE_BONUS) ?: 0
        
        // Combine bonuses (both are percentage-based)
        val totalBonusPercent = hoardingBonus + archetypeBonus
        val bonusMultiplier = 1.0 + (totalBonusPercent * 0.01)
        val totalValue = (baseValue * bonusMultiplier).toLong()
        
        val tier = HoardRank.calculateTier(totalValue)
        val player = gameStateManager.playerState.value
        
        // Update leaderboard entry
        leaderboardService.updatePlayerEntry(
            playerId = player.id,
            playerName = player.name,
            hoardValue = totalValue,
            shiniesCollected = collection.ownedShinies.size,
            timestamp = timestampProvider()
        )
        
        val rank = leaderboardService.getPlayerRank(player.id)
        
        return HoardRank(
            totalValue = totalValue,
            tier = tier,
            shiniesCollected = collection.ownedShinies.size,
            rank = rank
        )
    }
}

/**
 * View state for Hoard UI.
 */
data class HoardViewState(
    val collection: ShinyCollection = ShinyCollection(),
    val rank: HoardRank = HoardRank(),
    val tierProgress: Float = 0f,
    val nextTierThreshold: Long? = 1_000,
    val catalog: List<Shiny> = emptyList()
)
