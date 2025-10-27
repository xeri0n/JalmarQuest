package com.jalmarquest.feature.worldmap

import com.jalmarquest.core.state.catalogs.Location
import com.jalmarquest.core.state.managers.LocationWithStatus
import com.jalmarquest.core.state.managers.RegionWithStatus
import com.jalmarquest.core.state.managers.WorldMapManager
import com.jalmarquest.core.model.TravelResult
import com.jalmarquest.core.model.WorldRegionId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Controller for world map UI state and actions.
 * Follows Phase 5 MVI pattern.
 */
class WorldMapController(
    private val worldMapManager: WorldMapManager,
    private val coroutineScope: CoroutineScope
) {
    // View states
    private val _regions = MutableStateFlow<List<RegionWithStatus>>(emptyList())
    val regions: StateFlow<List<RegionWithStatus>> = _regions.asStateFlow()
    
    private val _selectedRegion = MutableStateFlow<WorldRegionId?>(null)
    val selectedRegion: StateFlow<WorldRegionId?> = _selectedRegion.asStateFlow()
    
    private val _locationsInRegion = MutableStateFlow<List<LocationWithStatus>>(emptyList())
    val locationsInRegion: StateFlow<List<LocationWithStatus>> = _locationsInRegion.asStateFlow()
    
    private val _fastTravelLocations = MutableStateFlow<List<LocationWithStatus>>(emptyList())
    val fastTravelLocations: StateFlow<List<LocationWithStatus>> = _fastTravelLocations.asStateFlow()
    
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()
    
    private val _travelResult = MutableStateFlow<TravelResult?>(null)
    val travelResult: StateFlow<TravelResult?> = _travelResult.asStateFlow()
    
    private val _viewMode = MutableStateFlow<ViewMode>(ViewMode.REGIONS)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()
    
    init {
        loadRegions()
        loadCurrentLocation()
        loadFastTravelLocations()
    }
    
    /**
     * Reload all regions with their status.
     */
    fun loadRegions() {
        val regionsWithStatus = worldMapManager.getRegionsWithStatus()
        _regions.value = regionsWithStatus
    }
    
    /**
     * Select a region to view its locations.
     */
    fun selectRegion(regionId: WorldRegionId) {
        _selectedRegion.value = regionId
        _locationsInRegion.value = worldMapManager.getLocationsInRegion(regionId)
        _viewMode.value = ViewMode.LOCATIONS
    }
    
    /**
     * Go back to region overview.
     */
    fun deselectRegion() {
        _selectedRegion.value = null
        _locationsInRegion.value = emptyList()
        _viewMode.value = ViewMode.REGIONS
    }
    
    /**
     * Attempt to travel to a location.
     */
    fun travelToLocation(locationId: String) {
        coroutineScope.launch {
            val result = worldMapManager.travelToLocation(locationId)
            _travelResult.value = result
            
            if (result is TravelResult.Success) {
                // Refresh UI after successful travel
                loadRegions()
                loadCurrentLocation()
                loadFastTravelLocations()
                
                // Refresh current region view if selected
                _selectedRegion.value?.let { regionId ->
                    _locationsInRegion.value = worldMapManager.getLocationsInRegion(regionId)
                }
            }
        }
    }
    
    /**
     * Fast travel to a previously visited location.
     */
    fun fastTravel(locationId: String) {
        travelToLocation(locationId) // Same logic, validation handled in manager
    }
    
    /**
     * Switch to fast travel view.
     */
    fun showFastTravelView() {
        _viewMode.value = ViewMode.FAST_TRAVEL
    }
    
    /**
     * Clear the travel result (dismiss notification).
     */
    fun clearTravelResult() {
        _travelResult.value = null
    }
    
    private fun loadCurrentLocation() {
        _currentLocation.value = worldMapManager.getCurrentLocation()
    }
    
    private fun loadFastTravelLocations() {
        _fastTravelLocations.value = worldMapManager.getFastTravelLocations()
    }
}

/**
 * View modes for the world map screen.
 */
enum class ViewMode {
    REGIONS,        // Show all regions
    LOCATIONS,      // Show locations within selected region
    FAST_TRAVEL     // Show fast travel destinations
}
