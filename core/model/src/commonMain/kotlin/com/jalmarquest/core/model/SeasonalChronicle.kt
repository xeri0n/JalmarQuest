package com.jalmarquest.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Seasonal Chronicle - Battle Pass System for JalmarQuest.
 * 
 * Features:
 * - Free and Premium tracks with distinct rewards
 * - 50 tiers per season (3-month cycle)
 * - XP-based progression from daily/weekly objectives
 * - Exclusive cosmetics, Glimmer Shards, and lore unlocks
 * - Retroactive premium unlock (earn free track first, upgrade later)
 */

/**
 * Unique identifier for a season.
 */
@Serializable
@JvmInline
value class SeasonId(val value: String)

/**
 * Unique identifier for a tier within a season.
 */
@Serializable
@JvmInline
value class TierId(val value: String)

/**
 * Type of battle pass track.
 */
@Serializable
enum class TrackType {
    /** Free track available to all players */
    FREE,
    /** Premium track requires Glimmer Shards or IAP purchase */
    PREMIUM
}

/**
 * Type of season objective.
 */
@Serializable
enum class SeasonObjectiveType {
    /** Complete X daily quests */
    COMPLETE_DAILY_QUESTS,
    /** Complete X weekly quests */
    COMPLETE_WEEKLY_QUESTS,
    /** Defeat X enemies */
    DEFEAT_ENEMIES,
    /** Harvest X ingredients */
    HARVEST_INGREDIENTS,
    /** Craft X concoctions */
    CRAFT_CONCOCTIONS,
    /** Complete X dungeons */
    COMPLETE_DUNGEONS,
    /** Accumulate X Seeds */
    ACCUMULATE_SEEDS,
    /** Reach skill level X */
    REACH_SKILL_LEVEL,
    /** Discover X lore entries */
    DISCOVER_LORE,
    /** Visit X unique locations */
    VISIT_LOCATIONS,
    /** Custom objective */
    CUSTOM
}

/**
 * Frequency of season objective reset.
 */
@Serializable
enum class ObjectiveFrequency {
    /** Resets every 24 hours */
    DAILY,
    /** Resets every 7 days */
    WEEKLY,
    /** One-time seasonal objective */
    SEASONAL
}

/**
 * A single reward in the battle pass.
 */
@Serializable
data class SeasonReward(
    val type: SeasonRewardType,
    val itemId: String? = null,
    val quantity: Int = 1,
    val displayName: String,
    val description: String,
    val iconPath: String? = null
) {
    init {
        require(quantity > 0) { "Reward quantity must be positive" }
    }
}

/**
 * Type of season reward.
 */
@Serializable
enum class SeasonRewardType {
    /** Glimmer Shards premium currency */
    GLIMMER_SHARDS,
    /** Seeds in-game currency */
    SEEDS,
    /** Cosmetic item (nest theme, companion outfit) */
    COSMETIC,
    /** Quest item */
    ITEM,
    /** Skill XP boost */
    SKILL_XP,
    /** Thought unlock */
    THOUGHT,
    /** Recipe unlock */
    RECIPE,
    /** Lore entry unlock */
    LORE,
    /** Title/badge unlock */
    TITLE,
    /** Emote unlock */
    EMOTE
}

/**
 * A tier in the battle pass with free and premium rewards.
 */
@Serializable
data class SeasonTier(
    val tierId: TierId,
    val tierNumber: Int,
    val xpRequired: Int,
    val freeReward: SeasonReward?,
    val premiumReward: SeasonReward?
) {
    init {
        require(tierNumber > 0) { "Tier number must be positive" }
        require(xpRequired > 0) { "XP required must be positive" }
        require(freeReward != null || premiumReward != null) { "Tier must have at least one reward" }
    }
}

/**
 * A season objective for earning battle pass XP.
 */
@Serializable
data class SeasonObjective(
    val objectiveId: String,
    val type: SeasonObjectiveType,
    val description: String,
    val frequency: ObjectiveFrequency,
    val xpReward: Int,
    val targetValue: Int = 1,
    val metadata: Map<String, String> = emptyMap()
) {
    init {
        require(xpReward > 0) { "XP reward must be positive" }
        require(targetValue > 0) { "Target value must be positive" }
    }
}

/**
 * A complete season definition.
 */
@Serializable
data class Season(
    val seasonId: SeasonId,
    val seasonNumber: Int,
    val name: String,
    val description: String,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val tiers: List<SeasonTier>,
    val objectives: List<SeasonObjective>,
    val premiumCostGlimmer: Int = 1000,
    val themeColor: String = "#FFD700", // Gold default
    val bannerImagePath: String? = null
) {
    init {
        require(seasonNumber > 0) { "Season number must be positive" }
        require(startTimestamp < endTimestamp) { "Start must be before end" }
        require(tiers.isNotEmpty()) { "Season must have tiers" }
        require(objectives.isNotEmpty()) { "Season must have objectives" }
        require(premiumCostGlimmer >= 0) { "Premium cost cannot be negative" }
    }
    
    /**
     * Check if season is currently active.
     */
    fun isActive(currentTimestamp: Long): Boolean {
        return currentTimestamp in startTimestamp..<endTimestamp
    }
    
    /**
     * Get days remaining in season.
     */
    fun getDaysRemaining(currentTimestamp: Long): Int {
        if (currentTimestamp >= endTimestamp) return 0
        val millisRemaining = endTimestamp - currentTimestamp
        return (millisRemaining / (24 * 60 * 60 * 1000)).toInt()
    }
    
    /**
     * Get total XP required to complete all tiers.
     */
    fun getTotalXpRequired(): Int {
        return tiers.sumOf { it.xpRequired }
    }
}

/**
 * Player's progress in a specific season objective.
 */
@Serializable
data class ObjectiveProgress(
    val objectiveId: String,
    val currentValue: Int = 0,
    val completed: Boolean = false,
    val lastResetTimestamp: Long = 0
) {
    init {
        require(currentValue >= 0) { "Current value cannot be negative" }
    }
}

/**
 * Player's progress in a season.
 */
@Serializable
data class SeasonProgress(
    val seasonId: SeasonId,
    val currentXp: Int = 0,
    val currentTier: Int = 0,
    val hasPremiumTrack: Boolean = false,
    val premiumPurchaseTimestamp: Long? = null,
    val claimedFreeRewards: Set<Int> = emptySet(),
    val claimedPremiumRewards: Set<Int> = emptySet(),
    val objectiveProgress: Map<String, ObjectiveProgress> = emptyMap(),
    val lastDailyResetTimestamp: Long = 0,
    val lastWeeklyResetTimestamp: Long = 0
) {
    init {
        require(currentXp >= 0) { "Current XP cannot be negative" }
        require(currentTier >= 0) { "Current tier cannot be negative" }
    }
    
    /**
     * Check if a specific tier's free reward has been claimed.
     */
    fun isFreeRewardClaimed(tierNumber: Int): Boolean {
        return claimedFreeRewards.contains(tierNumber)
    }
    
    /**
     * Check if a specific tier's premium reward has been claimed.
     */
    fun isPremiumRewardClaimed(tierNumber: Int): Boolean {
        return claimedPremiumRewards.contains(tierNumber)
    }
    
    /**
     * Mark free reward as claimed.
     */
    fun claimFreeReward(tierNumber: Int): SeasonProgress {
        return copy(claimedFreeRewards = claimedFreeRewards + tierNumber)
    }
    
    /**
     * Mark premium reward as claimed.
     */
    fun claimPremiumReward(tierNumber: Int): SeasonProgress {
        return copy(claimedPremiumRewards = claimedPremiumRewards + tierNumber)
    }
    
    /**
     * Add XP and update tier.
     */
    fun addXp(amount: Int, season: Season): SeasonProgress {
        if (amount <= 0) return this
        
        val newXp = currentXp + amount
        var newTier = currentTier
        var accumulatedXp = 0
        
        // Calculate new tier based on accumulated XP
        for (tier in season.tiers) {
            accumulatedXp += tier.xpRequired
            if (newXp >= accumulatedXp) {
                newTier = tier.tierNumber
            } else {
                break
            }
        }
        
        return copy(
            currentXp = newXp.coerceAtMost(season.getTotalXpRequired()),
            currentTier = newTier.coerceAtMost(season.tiers.size)
        )
    }
    
    /**
     * Upgrade to premium track.
     */
    fun upgradeToPremium(purchaseTimestamp: Long): SeasonProgress {
        return copy(
            hasPremiumTrack = true,
            premiumPurchaseTimestamp = purchaseTimestamp
        )
    }
    
    /**
     * Update objective progress.
     */
    fun updateObjectiveProgress(
        objectiveId: String,
        increment: Int = 1,
        targetValue: Int,
        currentTimestamp: Long
    ): SeasonProgress {
        val current = objectiveProgress[objectiveId] ?: ObjectiveProgress(objectiveId)
        val newValue = (current.currentValue + increment).coerceAtMost(targetValue)
        val completed = newValue >= targetValue
        
        val updated = current.copy(
            currentValue = newValue,
            completed = completed
        )
        
        return copy(
            objectiveProgress = objectiveProgress + (objectiveId to updated)
        )
    }
    
    /**
     * Reset daily objectives.
     */
    fun resetDailyObjectives(
        season: Season,
        currentTimestamp: Long
    ): SeasonProgress {
        val resetProgress = objectiveProgress.mapValues { (id, progress) ->
            val objective = season.objectives.find { it.objectiveId == id }
            if (objective?.frequency == ObjectiveFrequency.DAILY) {
                progress.copy(currentValue = 0, completed = false, lastResetTimestamp = currentTimestamp)
            } else {
                progress
            }
        }
        
        return copy(
            objectiveProgress = resetProgress,
            lastDailyResetTimestamp = currentTimestamp
        )
    }
    
    /**
     * Reset weekly objectives.
     */
    fun resetWeeklyObjectives(
        season: Season,
        currentTimestamp: Long
    ): SeasonProgress {
        val resetProgress = objectiveProgress.mapValues { (id, progress) ->
            val objective = season.objectives.find { it.objectiveId == id }
            if (objective?.frequency == ObjectiveFrequency.WEEKLY) {
                progress.copy(currentValue = 0, completed = false, lastResetTimestamp = currentTimestamp)
            } else {
                progress
            }
        }
        
        return copy(
            objectiveProgress = resetProgress,
            lastWeeklyResetTimestamp = currentTimestamp
        )
    }
}

/**
 * Player's overall seasonal chronicle state.
 */
@Serializable
data class SeasonalChronicleState(
    val activeSeasonId: SeasonId? = null,
    val seasonHistory: Map<String, SeasonProgress> = emptyMap()
) {
    /**
     * Get progress for a specific season.
     */
    fun getSeasonProgress(seasonId: SeasonId): SeasonProgress? {
        return seasonHistory[seasonId.value]
    }
    
    /**
     * Get or create progress for a season.
     */
    fun getOrCreateProgress(seasonId: SeasonId): SeasonProgress {
        return seasonHistory[seasonId.value] ?: SeasonProgress(seasonId)
    }
    
    /**
     * Update progress for a season.
     */
    fun updateSeasonProgress(progress: SeasonProgress): SeasonalChronicleState {
        return copy(
            seasonHistory = seasonHistory + (progress.seasonId.value to progress)
        )
    }
    
    /**
     * Set active season.
     */
    fun setActiveSeason(seasonId: SeasonId): SeasonalChronicleState {
        return copy(activeSeasonId = seasonId)
    }
}
