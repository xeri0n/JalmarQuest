package com.jalmarquest.core.state.managers

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.catalogs.Location
import com.jalmarquest.core.state.catalogs.LocationCatalog
import com.jalmarquest.core.state.catalogs.WorldRegionCatalog
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manages world exploration, travel, and region discovery.
 */
class WorldMapManager(
    private val gameStateManager: GameStateManager,
    private val locationCatalog: LocationCatalog,
    private val regionCatalog: WorldRegionCatalog,
    private val discoveryRewardManager: DiscoveryRewardManager? = null,
    private val timestampProvider: () -> Long
) {
    private val mutex = Mutex()
    
    /**
     * Get all regions with their unlock status.
     */
    fun getRegionsWithStatus(): List<RegionWithStatus> {
        val player = gameStateManager.playerState.value
        val explorationState = player.worldExploration
        val completedQuests = player.questLog.completedQuests.map { it.value }.toSet()
        val playerLevel = player.archetypeProgress.archetypeLevel
        
        return regionCatalog.getAllRegions().map { region ->
            val requirement = region.unlockRequirement
            val isUnlocked = requirement == null || 
                isRequirementMet(requirement, explorationState, playerLevel, completedQuests)
            
            RegionWithStatus(
                region = region,
                isDiscovered = explorationState.hasDiscoveredRegion(region.id),
                isUnlocked = isUnlocked,
                visitedLocationCount = region.primaryLocationIds.count { 
                    explorationState.hasVisitedLocation(it) 
                },
                totalLocations = region.primaryLocationIds.size
            )
        }
    }
    
    /**
     * Get locations within a region with their status.
     */
    fun getLocationsInRegion(regionId: WorldRegionId): List<LocationWithStatus> {
        val player = gameStateManager.playerState.value
        val explorationState = player.worldExploration
        val region = regionCatalog.getRegionById(regionId) ?: return emptyList()
        
        return region.primaryLocationIds.mapNotNull { locationId ->
            val location = locationCatalog.getLocationById(locationId) ?: return@mapNotNull null
            
            LocationWithStatus(
                location = location,
                isVisited = explorationState.hasVisitedLocation(locationId),
                isCurrent = explorationState.currentLocationId == locationId,
                canFastTravel = explorationState.canFastTravel(locationId),
                isAccessible = location.isAccessible && 
                    (location.requiresQuestId == null || 
                     player.questLog.completedQuests.any { it.value == location.requiresQuestId })
            )
        }
    }
    
    /**
     * Attempt to travel to a location.
     */
    suspend fun travelToLocation(locationId: String): TravelResult = mutex.withLock {
        val player = gameStateManager.playerState.value
        val explorationState = player.worldExploration
        
        // Check if location exists
        val location = locationCatalog.getLocationById(locationId)
            ?: return TravelResult.InvalidLocation
        
        // Check if location is accessible
        if (!location.isAccessible) {
            return TravelResult.LocationLocked(location.requiresQuestId)
        }
        
        // Check quest requirements
        if (location.requiresQuestId != null) {
            val questCompleted = player.questLog.completedQuests
                .any { it.value == location.requiresQuestId }
            if (!questCompleted) {
                return TravelResult.LocationLocked(location.requiresQuestId)
            }
        }
        
        // Find which region this location belongs to
        val region = regionCatalog.getAllRegions()
            .find { locationId in it.primaryLocationIds }
        
        // Check region unlock requirements
        if (region != null && !explorationState.hasDiscoveredRegion(region.id)) {
            val requirement = region.unlockRequirement
            if (requirement != null) {
                val completedQuests = player.questLog.completedQuests.map { it.value }.toSet()
                val playerLevel = player.archetypeProgress.archetypeLevel
                
                if (!isRequirementMet(requirement, explorationState, playerLevel, completedQuests)) {
                    return TravelResult.RegionLocked(requirement)
                }
            }
        }
        
        // Travel successful - update exploration state
        val firstVisit = !explorationState.hasVisitedLocation(locationId)
        val firstRegionDiscovery = region != null && !explorationState.hasDiscoveredRegion(region.id)
        
        gameStateManager.updateWorldExploration { currentState ->
            var newState = currentState.copy(
                currentLocationId = locationId,
                visitedLocations = currentState.visitedLocations + locationId,
                explorationLog = currentState.explorationLog + ExplorationEntry(
                    locationId = locationId,
                    timestamp = timestampProvider(),
                    firstVisit = firstVisit
                )
            )
            
            // Discover region if not already discovered
            if (region != null && !currentState.hasDiscoveredRegion(region.id)) {
                newState = newState.copy(
                    discoveredRegions = currentState.discoveredRegions + region.id
                )
            }
            
            // Unlock fast travel after first visit to safe locations
            if (firstVisit && region?.biomeType == BiomeType.TOWN) {
                newState = newState.copy(
                    fastTravelUnlocked = currentState.fastTravelUnlocked + locationId
                )
            }
            
            newState
        }
        
        // Grant discovery rewards
        val discoveryResults = mutableListOf<DiscoveryResult>()
        
        // Region discovery rewards
        if (firstRegionDiscovery && region != null && discoveryRewardManager != null) {
            val result = discoveryRewardManager.processRegionDiscovery(
                regionId = region.id,
                regionName = region.name,
                difficultyLevel = region.difficultyLevel,
                biomeType = region.biomeType
            )
            if (result !is DiscoveryResult.AlreadyDiscovered) {
                discoveryResults.add(result)
            }
        }
        
        // Location discovery rewards
        if (firstVisit && discoveryRewardManager != null) {
            val result = discoveryRewardManager.processLocationDiscovery(
                location = location,
                isHidden = false // TODO: Mark hidden locations in LocationCatalog
            )
            if (result !is DiscoveryResult.AlreadyDiscovered) {
                discoveryResults.add(result)
            }
        }
        
        // Check for milestone achievements
        if (discoveryRewardManager != null) {
            val milestones = discoveryRewardManager.checkMilestoneAchievements()
            discoveryResults.addAll(milestones)
        }
        
        // Log the travel choice
        gameStateManager.appendChoice("travel_${locationId}")
        
        return TravelResult.SuccessWithRewards(locationId, discoveryResults)
    }
    
    /**
     * Get all locations available for fast travel.
     */
    fun getFastTravelLocations(): List<LocationWithStatus> {
        val player = gameStateManager.playerState.value
        val explorationState = player.worldExploration
        
        return explorationState.fastTravelUnlocked.mapNotNull { locationId ->
            val location = locationCatalog.getLocationById(locationId) ?: return@mapNotNull null
            
            LocationWithStatus(
                location = location,
                isVisited = true,
                isCurrent = explorationState.currentLocationId == locationId,
                canFastTravel = true,
                isAccessible = true
            )
        }
    }
    
    /**
     * Discover a region (used when entering for the first time).
     */
    suspend fun discoverRegion(regionId: WorldRegionId) = mutex.withLock {
        gameStateManager.updateWorldExploration { currentState ->
            currentState.copy(
                discoveredRegions = currentState.discoveredRegions + regionId
            )
        }
        
        gameStateManager.appendChoice("discover_region_${regionId.value}")
    }
    
    /**
     * Get current location details.
     */
    fun getCurrentLocation(): Location? {
        val player = gameStateManager.playerState.value
        return locationCatalog.getLocationById(player.worldExploration.currentLocationId)
    }
    
    private fun isRequirementMet(
        requirement: RegionUnlockRequirement,
        explorationState: WorldExplorationState,
        playerLevel: Int,
        completedQuests: Set<String>
    ): Boolean {
        return when (requirement) {
            is RegionUnlockRequirement.QuestCompletion -> 
                requirement.questId in completedQuests
            
            is RegionUnlockRequirement.MinimumLevel -> 
                playerLevel >= requirement.level
            
            is RegionUnlockRequirement.DiscoverRegion -> 
                explorationState.hasDiscoveredRegion(requirement.regionId)
            
            is RegionUnlockRequirement.AllOf -> 
                requirement.requirements.all { 
                    isRequirementMet(it, explorationState, playerLevel, completedQuests) 
                }
        }
    }
}

/**
 * A region with its unlock/discovery status.
 */
data class RegionWithStatus(
    val region: WorldRegion,
    val isDiscovered: Boolean,
    val isUnlocked: Boolean,
    val visitedLocationCount: Int,
    val totalLocations: Int
) {
    val explorationProgress: Float 
        get() = if (totalLocations > 0) visitedLocationCount.toFloat() / totalLocations else 0f
}

/**
 * A location with its visitation and accessibility status.
 */
data class LocationWithStatus(
    val location: Location,
    val isVisited: Boolean,
    val isCurrent: Boolean,
    val canFastTravel: Boolean,
    val isAccessible: Boolean
)
