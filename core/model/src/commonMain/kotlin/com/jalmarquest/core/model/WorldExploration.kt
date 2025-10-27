package com.jalmarquest.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Represents a major region in the JalmarQuest world.
 * Regions contain multiple explorable locations.
 */
@Serializable
data class WorldRegion(
    val id: WorldRegionId,
    val name: String,
    val description: String,
    val biomeType: BiomeType,
    val difficultyLevel: Int, // 1-10, recommended player level
    val isDiscovered: Boolean = false,
    val unlockRequirement: RegionUnlockRequirement? = null,
    val primaryLocationIds: List<String> = emptyList()
)

@Serializable
@JvmInline
value class WorldRegionId(val value: String)

/**
 * Types of biomes in the world.
 */
@Serializable
enum class BiomeType {
    @SerialName("town")
    TOWN,           // Safe, civilized areas (Buttonburgh)
    
    @SerialName("forest")
    FOREST,         // Woodland areas with moderate danger
    
    @SerialName("grassland")
    GRASSLAND,      // Open fields and meadows
    
    @SerialName("wetland")
    WETLAND,        // Marshes, ponds, streams
    
    @SerialName("beach")
    BEACH,          // Coastal areas
    
    @SerialName("mountain")
    MOUNTAIN,       // Rocky, elevated terrain
    
    @SerialName("desert")
    DESERT,         // Arid, sandy areas
    
    @SerialName("cave")
    CAVE,           // Underground areas
    
    @SerialName("ruins")
    RUINS,          // Ancient structures
    
    @SerialName("garden")
    GARDEN          // Cultivated areas (human gardens)
}

/**
 * Requirements to unlock a world region.
 */
@Serializable
sealed class RegionUnlockRequirement {
    @Serializable
    @SerialName("quest")
    data class QuestCompletion(val questId: String) : RegionUnlockRequirement()
    
    @Serializable
    @SerialName("level")
    data class MinimumLevel(val level: Int) : RegionUnlockRequirement()
    
    @Serializable
    @SerialName("region")
    data class DiscoverRegion(val regionId: WorldRegionId) : RegionUnlockRequirement()
    
    @Serializable
    @SerialName("multiple")
    data class AllOf(val requirements: List<RegionUnlockRequirement>) : RegionUnlockRequirement()
}

/**
 * Player's current world exploration state.
 */
@Serializable
data class WorldExplorationState(
    @SerialName("current_location_id")
    val currentLocationId: String = "buttonburgh_centre",
    
    @SerialName("discovered_regions")
    val discoveredRegions: Set<WorldRegionId> = setOf(WorldRegionId("buttonburgh")),
    
    @SerialName("visited_locations")
    val visitedLocations: Set<String> = setOf("buttonburgh_centre"),
    
    @SerialName("fast_travel_unlocked")
    val fastTravelUnlocked: Set<String> = setOf("buttonburgh_centre"),
    
    @SerialName("exploration_log")
    val explorationLog: List<ExplorationEntry> = emptyList(),
    
    @SerialName("achievement_state")
    val achievementState: DiscoveryAchievementState = DiscoveryAchievementState()
) {
    /**
     * Check if a region has been discovered.
     */
    fun hasDiscoveredRegion(regionId: WorldRegionId): Boolean = regionId in discoveredRegions
    
    /**
     * Check if a location has been visited.
     */
    fun hasVisitedLocation(locationId: String): Boolean = locationId in visitedLocations
    
    /**
     * Check if fast travel is available to a location.
     */
    fun canFastTravel(locationId: String): Boolean = locationId in fastTravelUnlocked
}

/**
 * Entry in the player's exploration log.
 */
@Serializable
data class ExplorationEntry(
    @SerialName("location_id")
    val locationId: String,
    
    @SerialName("timestamp")
    val timestamp: Long,
    
    @SerialName("first_visit")
    val firstVisit: Boolean
)

/**
 * Result of attempting to travel to a location.
 */
@Serializable
sealed class TravelResult {
    @Serializable
    @SerialName("success")
    data class Success(val locationId: String) : TravelResult()
    
    @Serializable
    @SerialName("success_with_rewards")
    data class SuccessWithRewards(
        val locationId: String,
        val discoveries: List<DiscoveryResult>
    ) : TravelResult()
    
    @Serializable
    @SerialName("region_locked")
    data class RegionLocked(val requirement: RegionUnlockRequirement) : TravelResult()
    
    @Serializable
    @SerialName("location_locked")
    data class LocationLocked(val requiresQuestId: String?) : TravelResult()
    
    @Serializable
    @SerialName("invalid_location")
    object InvalidLocation : TravelResult()
}
