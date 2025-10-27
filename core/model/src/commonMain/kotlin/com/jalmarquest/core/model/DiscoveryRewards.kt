package com.jalmarquest.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Discovery rewards and achievements for world exploration.
 * 
 * Phase 6.5: Rewards players for discovering new regions and locations
 * with XP, items, lore unlocks, and tracked achievements.
 */

/**
 * Types of discovery achievements.
 */
@Serializable
enum class DiscoveryAchievementType {
    // Region discoveries
    FIRST_REGION_DISCOVERED,
    ALL_REGIONS_DISCOVERED,
    DANGEROUS_REGION_DISCOVERED,
    
    // Location discoveries
    FIRST_LOCATION_DISCOVERED,
    TEN_LOCATIONS_DISCOVERED,
    TWENTY_FIVE_LOCATIONS_DISCOVERED,
    FIFTY_LOCATIONS_DISCOVERED,
    ALL_LOCATIONS_DISCOVERED,
    
    // Biome discoveries
    FIRST_FOREST_DISCOVERED,
    FIRST_BEACH_DISCOVERED,
    FIRST_MOUNTAIN_DISCOVERED,
    FIRST_SWAMP_DISCOVERED,
    FIRST_RUINS_DISCOVERED,
    ALL_BIOMES_DISCOVERED,
    
    // Exploration milestones
    FEARLESS_EXPLORER,          // Visited 5 dangerous locations
    WORLD_TRAVELER,             // Fast travel to 10 locations
    COMPLETIONIST,              // 100% region exploration
    
    // Special discoveries
    HIDDEN_LOCATION_DISCOVERED,
    LEGENDARY_LOCATION_DISCOVERED
}

/**
 * Unique achievement ID.
 */
@Serializable
@JvmInline
value class AchievementId(val value: String)

/**
 * A discovered achievement with timestamp.
 */
@Serializable
data class UnlockedAchievement(
    @SerialName("achievement_id")
    val achievementId: AchievementId,
    
    @SerialName("achievement_type")
    val achievementType: DiscoveryAchievementType,
    
    @SerialName("unlocked_at")
    val unlockedAt: Long,
    
    @SerialName("title")
    val title: String,
    
    @SerialName("description")
    val description: String
)

/**
 * Rewards granted for a discovery.
 */
@Serializable
data class DiscoveryReward(
    @SerialName("experience_points")
    val experiencePoints: Int = 0,
    
    @SerialName("seeds")
    val seeds: Int = 0,
    
    @SerialName("items")
    val items: Map<String, Int> = emptyMap(), // itemId -> quantity
    
    @SerialName("lore_unlocks")
    val loreUnlocks: List<String> = emptyList(), // lore snippet IDs
    
    @SerialName("achievement_unlocks")
    val achievementUnlocks: List<DiscoveryAchievementType> = emptyList()
)

/**
 * Configuration for discovery rewards per region tier.
 */
@Serializable
data class DiscoveryRewardConfig(
    @SerialName("region_discovery_base_xp")
    val regionDiscoveryBaseXp: Int = 50,
    
    @SerialName("region_discovery_xp_per_level")
    val regionDiscoveryXpPerLevel: Int = 25, // Multiply by difficultyLevel
    
    @SerialName("location_discovery_base_xp")
    val locationDiscoveryBaseXp: Int = 10,
    
    @SerialName("location_discovery_base_seeds")
    val locationDiscoveryBaseSeeds: Int = 5,
    
    @SerialName("first_biome_discovery_bonus_xp")
    val firstBiomeDiscoveryBonusXp: Int = 100,
    
    @SerialName("hidden_location_bonus_xp")
    val hiddenLocationBonusXp: Int = 200,
    
    @SerialName("dangerous_region_item_rewards")
    val dangerousRegionItemRewards: Map<String, Int> = emptyMap() // itemId -> quantity
)

/**
 * Tracking state for discovery achievements.
 */
@Serializable
data class DiscoveryAchievementState(
    @SerialName("unlocked_achievements")
    val unlockedAchievements: List<UnlockedAchievement> = emptyList(),
    
    @SerialName("discovered_biomes")
    val discoveredBiomes: Set<BiomeType> = emptySet(),
    
    @SerialName("dangerous_locations_visited")
    val dangerousLocationsVisited: Int = 0,
    
    @SerialName("fast_travel_locations_count")
    val fastTravelLocationsCount: Int = 0,
    
    @SerialName("hidden_locations_found")
    val hiddenLocationsFound: Set<String> = emptySet()
) {
    /**
     * Check if an achievement has been unlocked.
     */
    fun hasAchievement(type: DiscoveryAchievementType): Boolean {
        return unlockedAchievements.any { it.achievementType == type }
    }
    
    /**
     * Get achievement count.
     */
    fun getAchievementCount(): Int = unlockedAchievements.size
    
    /**
     * Get achievements by type.
     */
    fun getAchievementsByType(type: DiscoveryAchievementType): List<UnlockedAchievement> {
        return unlockedAchievements.filter { it.achievementType == type }
    }
}

/**
 * Result of a discovery event.
 */
@Serializable
sealed class DiscoveryResult {
    @Serializable
    @SerialName("first_discovery")
    data class FirstDiscovery(
        val locationId: String,
        val locationName: String,
        val reward: DiscoveryReward,
        val newAchievements: List<UnlockedAchievement>
    ) : DiscoveryResult()
    
    @Serializable
    @SerialName("region_discovered")
    data class RegionDiscovered(
        val regionId: WorldRegionId,
        val regionName: String,
        val reward: DiscoveryReward,
        val newAchievements: List<UnlockedAchievement>
    ) : DiscoveryResult()
    
    @Serializable
    @SerialName("already_discovered")
    data object AlreadyDiscovered : DiscoveryResult()
    
    @Serializable
    @SerialName("milestone_reached")
    data class MilestoneReached(
        val milestoneType: DiscoveryAchievementType,
        val reward: DiscoveryReward,
        val achievement: UnlockedAchievement
    ) : DiscoveryResult()
}
