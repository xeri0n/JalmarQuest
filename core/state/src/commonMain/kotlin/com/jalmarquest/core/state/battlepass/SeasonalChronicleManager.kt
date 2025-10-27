package com.jalmarquest.core.state.battlepass

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.monetization.GlimmerWalletManager
import com.jalmarquest.core.state.monetization.SpendResult
import com.jalmarquest.core.state.perf.PerformanceLogger
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manages Seasonal Chronicle battle pass progression.
 * 
 * Features:
 * - XP tracking and tier progression
 * - Free and premium reward claiming
 * - Daily/weekly objective tracking with auto-reset
 * - Premium track purchase with Glimmer Shards
 * - Retroactive premium unlock (claim free rewards first, upgrade later gets all premium)
 */
class SeasonalChronicleManager(
    private val gameStateManager: GameStateManager,
    private val glimmerWalletManager: GlimmerWalletManager?,
    private val seasonCatalog: SeasonCatalog,
    private val timestampProvider: () -> Long
) {
    private val mutex = Mutex()
    
    val playerState: StateFlow<Player> = gameStateManager.playerState
    
    /**
     * Get current active season.
     */
    fun getCurrentSeason(): Season? {
        val currentTime = timestampProvider()
        return seasonCatalog.getActiveSeason(currentTime)
    }
    
    /**
     * Get player's progress in current season.
     */
    fun getCurrentProgress(): SeasonProgress? {
        val season = getCurrentSeason() ?: return null
        return playerState.value.seasonalChronicle.getSeasonProgress(season.seasonId)
    }
    
    /**
     * Purchase premium track with Glimmer Shards.
     */
    suspend fun purchasePremiumTrack(): PremiumPurchaseResult = mutex.withLock {
        val season = getCurrentSeason()
            ?: return PremiumPurchaseResult.NoActiveSeason
        
        val chronicle = playerState.value.seasonalChronicle
        val progress = chronicle.getOrCreateProgress(season.seasonId)
        
        if (progress.hasPremiumTrack) {
            return PremiumPurchaseResult.AlreadyOwned
        }
        
        val walletManager = glimmerWalletManager
            ?: return PremiumPurchaseResult.NoWalletManager
        
        // Spend Glimmer Shards
        val spendResult = walletManager.spendGlimmer(
            amount = season.premiumCostGlimmer,
            type = TransactionType.BATTLE_PASS_PURCHASE,
            itemId = "season_${season.seasonId.value}_premium"
        )
        
        when (spendResult) {
            is SpendResult.Success -> {
                // Upgrade to premium
                val updatedProgress = progress.upgradeToPremium(timestampProvider())
                val updatedChronicle = chronicle.updateSeasonProgress(updatedProgress)
                
                gameStateManager.updatePlayer { player ->
                    player.copy(seasonalChronicle = updatedChronicle)
                }
                
                gameStateManager.appendChoice("season_premium_purchased_${season.seasonId.value}")
                PerformanceLogger.logStateMutation(
                    "SeasonalChronicle",
                    "purchasePremiumTrack",
                    mapOf("season" to season.seasonId.value)
                )
                
                PremiumPurchaseResult.Success
            }
            is SpendResult.InsufficientFunds -> {
                PremiumPurchaseResult.InsufficientGlimmer(
                    required = spendResult.required,
                    available = spendResult.available
                )
            }
            else -> PremiumPurchaseResult.Error("Unknown error")
        }
    }
    
    /**
     * Add XP to current season and update tier.
     */
    suspend fun addSeasonXp(amount: Int, source: String): XpResult = mutex.withLock {
        if (amount <= 0) return XpResult.InvalidAmount
        
        val season = getCurrentSeason() ?: return XpResult.NoActiveSeason
        val chronicle = playerState.value.seasonalChronicle
        val progress = chronicle.getOrCreateProgress(season.seasonId)
        
        val oldTier = progress.currentTier
        val updatedProgress = progress.addXp(amount, season)
        val newTier = updatedProgress.currentTier
        
        val updatedChronicle = chronicle.updateSeasonProgress(updatedProgress)
        gameStateManager.updatePlayer { player ->
            player.copy(seasonalChronicle = updatedChronicle)
        }
        
        gameStateManager.appendChoice("season_xp_gained_${source}_${amount}")
        
        XpResult.Success(
            xpGained = amount,
            newTotalXp = updatedProgress.currentXp,
            tierUp = newTier > oldTier,
            newTier = newTier
        )
    }
    
    /**
     * Complete an objective and award XP.
     */
    suspend fun completeObjective(objectiveId: String): ObjectiveResult = mutex.withLock {
        val season = getCurrentSeason() ?: return ObjectiveResult.NoActiveSeason
        val objective = season.objectives.find { it.objectiveId == objectiveId }
            ?: return ObjectiveResult.ObjectiveNotFound
        
        val chronicle = playerState.value.seasonalChronicle
        val progress = chronicle.getOrCreateProgress(season.seasonId)
        
        val currentProgress = progress.objectiveProgress[objectiveId]
        if (currentProgress?.completed == true) {
            return ObjectiveResult.AlreadyCompleted
        }
        
        // Update objective progress
        val updatedProgress = progress.updateObjectiveProgress(
            objectiveId = objectiveId,
            increment = 1,
            targetValue = objective.targetValue,
            currentTimestamp = timestampProvider()
        )
        
        val objectiveCompleted = updatedProgress.objectiveProgress[objectiveId]?.completed == true
        
        // If objective completed, award XP
        val finalProgress = if (objectiveCompleted) {
            updatedProgress.addXp(objective.xpReward, season)
        } else {
            updatedProgress
        }
        
        val updatedChronicle = chronicle.updateSeasonProgress(finalProgress)
        gameStateManager.updatePlayer { player ->
            player.copy(seasonalChronicle = updatedChronicle)
        }
        
        if (objectiveCompleted) {
            gameStateManager.appendChoice("season_objective_completed_${objectiveId}")
        }
        
        ObjectiveResult.Success(
            completed = objectiveCompleted,
            xpAwarded = if (objectiveCompleted) objective.xpReward else 0,
            currentValue = finalProgress.objectiveProgress[objectiveId]?.currentValue ?: 0,
            targetValue = objective.targetValue
        )
    }
    
    /**
     * Claim free reward for a tier.
     */
    suspend fun claimFreeReward(tierNumber: Int): ClaimResult = mutex.withLock {
        val season = getCurrentSeason() ?: return ClaimResult.NoActiveSeason
        val tier = season.tiers.find { it.tierNumber == tierNumber }
            ?: return ClaimResult.TierNotFound
        
        val freeReward = tier.freeReward
        if (freeReward == null) {
            return ClaimResult.NoRewardAtTier
        }
        
        val chronicle = playerState.value.seasonalChronicle
        val progress = chronicle.getOrCreateProgress(season.seasonId)
        
        if (progress.currentTier < tierNumber) {
            return ClaimResult.TierNotReached(
                currentTier = progress.currentTier,
                requiredTier = tierNumber
            )
        }
        
        if (progress.isFreeRewardClaimed(tierNumber)) {
            return ClaimResult.AlreadyClaimed
        }
        
        // Grant reward
        grantReward(freeReward)
        
        // Mark as claimed
        val updatedProgress = progress.claimFreeReward(tierNumber)
        val updatedChronicle = chronicle.updateSeasonProgress(updatedProgress)
        
        gameStateManager.updatePlayer { player ->
            player.copy(seasonalChronicle = updatedChronicle)
        }
        
        gameStateManager.appendChoice("season_free_reward_claimed_tier_${tierNumber}")
        
        ClaimResult.Success(freeReward)
    }
    
    /**
     * Claim premium reward for a tier.
     */
    suspend fun claimPremiumReward(tierNumber: Int): ClaimResult = mutex.withLock {
        val season = getCurrentSeason() ?: return ClaimResult.NoActiveSeason
        val tier = season.tiers.find { it.tierNumber == tierNumber }
            ?: return ClaimResult.TierNotFound
        
        val premiumReward = tier.premiumReward
        if (premiumReward == null) {
            return ClaimResult.NoRewardAtTier
        }
        
        val chronicle = playerState.value.seasonalChronicle
        val progress = chronicle.getOrCreateProgress(season.seasonId)
        
        if (!progress.hasPremiumTrack) {
            return ClaimResult.NoPremiumTrack
        }
        
        if (progress.currentTier < tierNumber) {
            return ClaimResult.TierNotReached(
                currentTier = progress.currentTier,
                requiredTier = tierNumber
            )
        }
        
        if (progress.isPremiumRewardClaimed(tierNumber)) {
            return ClaimResult.AlreadyClaimed
        }
        
        // Grant reward
        grantReward(premiumReward)
        
        // Mark as claimed
        val updatedProgress = progress.claimPremiumReward(tierNumber)
        val updatedChronicle = chronicle.updateSeasonProgress(updatedProgress)
        
        gameStateManager.updatePlayer { player ->
            player.copy(seasonalChronicle = updatedChronicle)
        }
        
        gameStateManager.appendChoice("season_premium_reward_claimed_tier_${tierNumber}")
        
        ClaimResult.Success(premiumReward)
    }
    
    /**
     * Check and reset daily objectives if needed.
     */
    suspend fun checkDailyReset(): ResetResult = mutex.withLock {
        val season = getCurrentSeason() ?: return ResetResult.NoActiveSeason
        val chronicle = playerState.value.seasonalChronicle
        val progress = chronicle.getOrCreateProgress(season.seasonId)
        
        val currentTime = timestampProvider()
        val lastReset = progress.lastDailyResetTimestamp
        val millisInDay = 24 * 60 * 60 * 1000L
        
        // Check if 24 hours have passed
        if (currentTime - lastReset < millisInDay) {
            return ResetResult.NotYetTime(lastReset + millisInDay)
        }
        
        val updatedProgress = progress.resetDailyObjectives(season, currentTime)
        val updatedChronicle = chronicle.updateSeasonProgress(updatedProgress)
        
        gameStateManager.updatePlayer { player ->
            player.copy(seasonalChronicle = updatedChronicle)
        }
        
        gameStateManager.appendChoice("season_daily_reset")
        
        ResetResult.Success(ObjectiveFrequency.DAILY)
    }
    
    /**
     * Check and reset weekly objectives if needed.
     */
    suspend fun checkWeeklyReset(): ResetResult = mutex.withLock {
        val season = getCurrentSeason() ?: return ResetResult.NoActiveSeason
        val chronicle = playerState.value.seasonalChronicle
        val progress = chronicle.getOrCreateProgress(season.seasonId)
        
        val currentTime = timestampProvider()
        val lastReset = progress.lastWeeklyResetTimestamp
        val millisInWeek = 7 * 24 * 60 * 60 * 1000L
        
        // Check if 7 days have passed
        if (currentTime - lastReset < millisInWeek) {
            return ResetResult.NotYetTime(lastReset + millisInWeek)
        }
        
        val updatedProgress = progress.resetWeeklyObjectives(season, currentTime)
        val updatedChronicle = chronicle.updateSeasonProgress(updatedProgress)
        
        gameStateManager.updatePlayer { player ->
            player.copy(seasonalChronicle = updatedChronicle)
        }
        
        gameStateManager.appendChoice("season_weekly_reset")
        
        ResetResult.Success(ObjectiveFrequency.WEEKLY)
    }
    
    /**
     * Grant a season reward to the player.
     */
    private fun grantReward(reward: SeasonReward) {
        when (reward.type) {
            SeasonRewardType.GLIMMER_SHARDS -> {
                // Granted via wallet manager if available
                // For now, just log
                PerformanceLogger.logStateMutation(
                    "SeasonalChronicle",
                    "grantReward",
                    mapOf("type" to "glimmer", "amount" to reward.quantity)
                )
            }
            SeasonRewardType.SEEDS -> {
                gameStateManager.grantItem("seeds", reward.quantity)
            }
            SeasonRewardType.COSMETIC,
            SeasonRewardType.ITEM -> {
                reward.itemId?.let { gameStateManager.grantItem(it, reward.quantity) }
            }
            else -> {
                // Other reward types handled elsewhere (thoughts, recipes, lore, etc.)
                PerformanceLogger.logStateMutation(
                    "SeasonalChronicle",
                    "grantReward",
                    mapOf("type" to reward.type.name, "id" to (reward.itemId ?: "none"))
                )
            }
        }
    }
}

/**
 * Result of premium track purchase.
 */
sealed class PremiumPurchaseResult {
    data object Success : PremiumPurchaseResult()
    data object NoActiveSeason : PremiumPurchaseResult()
    data object AlreadyOwned : PremiumPurchaseResult()
    data object NoWalletManager : PremiumPurchaseResult()
    data class InsufficientGlimmer(val required: Int, val available: Int) : PremiumPurchaseResult()
    data class Error(val message: String) : PremiumPurchaseResult()
}

/**
 * Result of XP addition.
 */
sealed class XpResult {
    data class Success(
        val xpGained: Int,
        val newTotalXp: Int,
        val tierUp: Boolean,
        val newTier: Int
    ) : XpResult()
    data object NoActiveSeason : XpResult()
    data object InvalidAmount : XpResult()
}

/**
 * Result of objective completion.
 */
sealed class ObjectiveResult {
    data class Success(
        val completed: Boolean,
        val xpAwarded: Int,
        val currentValue: Int,
        val targetValue: Int
    ) : ObjectiveResult()
    data object NoActiveSeason : ObjectiveResult()
    data object ObjectiveNotFound : ObjectiveResult()
    data object AlreadyCompleted : ObjectiveResult()
}

/**
 * Result of reward claim.
 */
sealed class ClaimResult {
    data class Success(val reward: SeasonReward) : ClaimResult()
    data object NoActiveSeason : ClaimResult()
    data object TierNotFound : ClaimResult()
    data object NoRewardAtTier : ClaimResult()
    data class TierNotReached(val currentTier: Int, val requiredTier: Int) : ClaimResult()
    data object AlreadyClaimed : ClaimResult()
    data object NoPremiumTrack : ClaimResult()
}

/**
 * Result of objective reset.
 */
sealed class ResetResult {
    data class Success(val frequency: ObjectiveFrequency) : ResetResult()
    data object NoActiveSeason : ResetResult()
    data class NotYetTime(val nextResetTimestamp: Long) : ResetResult()
}

/**
 * Catalog of all seasons.
 */
class SeasonCatalog {
    private val seasons = mutableMapOf<String, Season>()
    
    fun registerSeason(season: Season) {
        seasons[season.seasonId.value] = season
    }
    
    fun getSeason(seasonId: SeasonId): Season? {
        return seasons[seasonId.value]
    }
    
    fun getAllSeasons(): List<Season> {
        return seasons.values.sortedBy { it.seasonNumber }
    }
    
    fun getActiveSeason(currentTimestamp: Long): Season? {
        return seasons.values.find { it.isActive(currentTimestamp) }
    }
}
