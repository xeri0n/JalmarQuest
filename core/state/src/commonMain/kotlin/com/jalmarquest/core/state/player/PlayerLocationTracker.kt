package com.jalmarquest.core.state.player

import com.jalmarquest.core.state.coordinator.OptimizedWorldUpdateCoordinator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * Tracks player's current location and movement history
 */
@Serializable
data class PlayerLocation(
    val locationId: String,
    val arrivedAt: Long,
    val previousLocationId: String? = null
)

/**
 * Location visit record for analytics and quest tracking
 */
@Serializable
data class LocationVisit(
    val locationId: String,
    val visitedAt: Long,
    val visitCount: Int = 1,
    val lastVisitDuration: Long = 0  // milliseconds spent at location
)

/**
 * Manages player location tracking and integration with world systems
 * 
 * Integration Points:
 * - OptimizedWorldUpdateCoordinator: Notifies spatial partitioning system of player movement
 * - QuestFlowIntegrator: Location-based quest triggers
 * - Analytics: Visit history tracking
 */
class PlayerLocationTracker(
    private val timestampProvider: () -> Long
) {
    private val _currentLocation = MutableStateFlow<PlayerLocation?>(null)
    val currentLocation: StateFlow<PlayerLocation?> = _currentLocation.asStateFlow()
    
    private val _visitHistory = MutableStateFlow<Map<String, LocationVisit>>(emptyMap())
    val visitHistory: StateFlow<Map<String, LocationVisit>> = _visitHistory.asStateFlow()
    
    private val _recentLocations = MutableStateFlow<List<String>>(emptyList())
    val recentLocations: StateFlow<List<String>> = _recentLocations.asStateFlow()
    
    // Optional coordinator for spatial optimization
    private var optimizedCoordinator: OptimizedWorldUpdateCoordinator? = null
    
    /**
     * Set the optimized coordinator for spatial partitioning integration
     * Called by DI initialization
     */
    fun setOptimizedCoordinator(coordinator: OptimizedWorldUpdateCoordinator) {
        this.optimizedCoordinator = coordinator
    }
    
    /**
     * Move player to a new location
     * 
     * Triggers:
     * - Location history update
     * - Spatial partitioning update (if OptimizedWorldUpdateCoordinator is set)
     * - Location change event for observers (via StateFlow)
     */
    fun moveToLocation(locationId: String) {
        val now = timestampProvider()
        val previous = _currentLocation.value
        
        // Record exit from previous location
        if (previous != null) {
            val duration = now - previous.arrivedAt
            recordVisit(previous.locationId, previous.arrivedAt, duration)
        }
        
        // Update current location
        _currentLocation.value = PlayerLocation(
            locationId = locationId,
            arrivedAt = now,
            previousLocationId = previous?.locationId
        )
        
        // Update recent locations (keep last 10)
        val recent = _recentLocations.value.toMutableList()
        recent.add(0, locationId)
        if (recent.size > 10) {
            recent.removeLast()
        }
        _recentLocations.value = recent.distinct()
        
        // Notify optimized coordinator for spatial partitioning
        optimizedCoordinator?.updatePlayerLocation(locationId)
    }
    
    /**
     * Get current location ID
     */
    fun getCurrentLocationId(): String? {
        return _currentLocation.value?.locationId
    }
    
    /**
     * Get previous location ID
     */
    fun getPreviousLocationId(): String? {
        return _currentLocation.value?.previousLocationId
    }
    
    /**
     * Get time spent at current location in milliseconds
     */
    fun getTimeAtCurrentLocation(): Long {
        val current = _currentLocation.value ?: return 0
        return timestampProvider() - current.arrivedAt
    }
    
    /**
     * Check if player has visited a location
     */
    fun hasVisited(locationId: String): Boolean {
        return _visitHistory.value.containsKey(locationId)
    }
    
    /**
     * Get number of times player has visited a location
     */
    fun getVisitCount(locationId: String): Int {
        return _visitHistory.value[locationId]?.visitCount ?: 0
    }
    
    /**
     * Get last visit timestamp for a location
     */
    fun getLastVisitTime(locationId: String): Long? {
        return _visitHistory.value[locationId]?.visitedAt
    }
    
    /**
     * Check if player is currently at a specific location
     */
    fun isAt(locationId: String): Boolean {
        return _currentLocation.value?.locationId == locationId
    }
    
    /**
     * Check if player was recently at a location (within last N visits)
     */
    fun wasRecentlyAt(locationId: String, recentCount: Int = 5): Boolean {
        return _recentLocations.value.take(recentCount).contains(locationId)
    }
    
    /**
     * Get region ID from location ID (extract prefix before underscore)
     */
    fun getCurrentRegion(): String? {
        val locationId = getCurrentLocationId() ?: return null
        return locationId.substringBefore("_")
    }
    
    /**
     * Check if player is in a specific region
     */
    fun isInRegion(regionId: String): Boolean {
        return getCurrentRegion() == regionId
    }
    
    /**
     * Get all visited locations in a region
     */
    fun getVisitedLocationsInRegion(regionId: String): List<String> {
        return _visitHistory.value.keys.filter { it.startsWith("${regionId}_") }
    }
    
    /**
     * Get total time spent in a region (sum of all visit durations)
     */
    fun getTotalTimeInRegion(regionId: String): Long {
        return _visitHistory.value
            .filterKeys { it.startsWith("${regionId}_") }
            .values
            .sumOf { it.lastVisitDuration }
    }
    
    /**
     * Record a location visit in history
     */
    private fun recordVisit(locationId: String, visitedAt: Long, duration: Long) {
        val history = _visitHistory.value.toMutableMap()
        val existing = history[locationId]
        
        history[locationId] = if (existing != null) {
            existing.copy(
                visitedAt = visitedAt,
                visitCount = existing.visitCount + 1,
                lastVisitDuration = duration
            )
        } else {
            LocationVisit(
                locationId = locationId,
                visitedAt = visitedAt,
                visitCount = 1,
                lastVisitDuration = duration
            )
        }
        
        _visitHistory.value = history
    }
    
    /**
     * Get total unique locations visited
     */
    fun getTotalLocationsVisited(): Int {
        return _visitHistory.value.size
    }
    
    /**
     * Get most visited location
     */
    fun getMostVisitedLocation(): String? {
        return _visitHistory.value.maxByOrNull { it.value.visitCount }?.key
    }
    
    /**
     * Get exploration percentage for a region (locations visited / total locations)
     * Note: Requires knowledge of total locations in region from LocationCatalog
     */
    fun getRegionExplorationCount(regionId: String): Int {
        return getVisitedLocationsInRegion(regionId).size
    }
    
    /**
     * Reset location tracking (for new game / character reset)
     */
    fun reset() {
        _currentLocation.value = null
        _visitHistory.value = emptyMap()
        _recentLocations.value = emptyList()
    }
    
    /**
     * Load location state from serialized data
     */
    fun loadState(
        currentLocation: PlayerLocation?,
        visitHistory: Map<String, LocationVisit>,
        recentLocations: List<String>
    ) {
        _currentLocation.value = currentLocation
        _visitHistory.value = visitHistory
        _recentLocations.value = recentLocations
    }
    
    /**
     * Get serializable state for saving
     */
    fun getState(): PlayerLocationState {
        return PlayerLocationState(
            currentLocation = _currentLocation.value,
            visitHistory = _visitHistory.value,
            recentLocations = _recentLocations.value
        )
    }
}

/**
 * Serializable state for PlayerLocationTracker
 */
@Serializable
data class PlayerLocationState(
    val currentLocation: PlayerLocation? = null,
    val visitHistory: Map<String, LocationVisit> = emptyMap(),
    val recentLocations: List<String> = emptyList()
)
