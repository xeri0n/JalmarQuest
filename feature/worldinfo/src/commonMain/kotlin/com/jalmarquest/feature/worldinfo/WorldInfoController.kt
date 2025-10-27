package com.jalmarquest.feature.worldinfo

import com.jalmarquest.core.state.difficulty.DifficultyTier
import com.jalmarquest.core.state.difficulty.RegionDifficultyManager
import com.jalmarquest.core.state.player.PlayerLocationTracker
import com.jalmarquest.core.state.weather.Season
import com.jalmarquest.core.state.weather.SeasonalCycleManager
import com.jalmarquest.core.state.weather.WeatherCondition
import com.jalmarquest.core.state.weather.WeatherSystem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * WorldInfoController - Bridges Phase 4 managers to UI components
 * 
 * Aggregates state from:
 * - PlayerLocationTracker (current location and region)
 * - RegionDifficultyManager (difficulty tier, scaling, warnings)
 * - WeatherSystem (current weather, severity, description)
 * - SeasonalCycleManager (current season, day progression, description)
 */
class WorldInfoController(
    private val locationTracker: PlayerLocationTracker,
    private val difficultyManager: RegionDifficultyManager,
    private val weatherSystem: WeatherSystem,
    private val seasonalManager: SeasonalCycleManager
) {
    private val _state = MutableStateFlow(createInitialState())
    val state: StateFlow<WorldInfoState> = _state.asStateFlow()

    init {
        // Combine all manager states into UI state
        // In a real implementation, you'd use Flow combiners
        updateState()
    }

    /**
     * Update UI state from all Phase 4 managers
     */
    fun updateState() {
        val currentLocation = locationTracker.getCurrentLocationId()
        val currentRegion = locationTracker.getCurrentRegion()
        val difficultyTier = currentLocation?.let { difficultyManager.getDifficultyTier(it) } ?: DifficultyTier.SAFE
        val weatherState = weatherSystem.currentWeather.value
        val seasonState = seasonalManager.seasonalState.value

        val weatherConditionStr: String = getWeatherDisplayName(weatherState.condition)
        val weatherDescStr: String = weatherSystem.getWeatherDescription()
        val seasonStr: String = getSeasonDisplayName(seasonState.currentSeason)
        val seasonDescStr: String = seasonalManager.getSeasonDescription()
        
        _state.value = WorldInfoState(
            locationName = getLocationDisplayName(currentLocation),
            regionName = getRegionDisplayName(currentRegion),
            difficultyTier = getDifficultyDisplayName(difficultyTier),
            difficultyColorHex = getDifficultyColorHex(difficultyTier),
            difficultyWarning = getDifficultyWarning(difficultyTier),
            weatherDisplay = weatherConditionStr,
            weatherDesc = weatherDescStr,
            weatherSeverity = weatherState.severity,
            seasonDisplay = seasonStr,
            seasonDay = seasonState.dayOfSeason + 1,
            seasonDesc = seasonDescStr,
            resourceAvailability = getResourceAvailability(difficultyTier, seasonState.currentSeason)
        )
    }

    /**
     * Force refresh from all managers
     */
    fun refresh() {
        updateState()
    }

    private fun createInitialState(): WorldInfoState {
        return WorldInfoState(
            locationName = "Unknown",
            regionName = "Unknown Region",
            difficultyTier = "Unknown",
            difficultyColorHex = 0xFF9E9E9E, // Gray
            difficultyWarning = null,
            weatherDisplay = "Clear",
            weatherDesc = "The skies are clear",
            weatherSeverity = 1,
            seasonDisplay = "Spring",
            seasonDay = 1,
            seasonDesc = "New beginnings",
            resourceAvailability = "Normal"
        )
    }

    private fun getLocationDisplayName(locationId: String?): String {
        if (locationId == null) return "Unknown"
        // In a real implementation, look up from LocationCatalog
        return locationId.split("_")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }

    private fun getRegionDisplayName(regionId: String?): String {
        if (regionId == null) return "Unknown Region"
        return regionId.split("_")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }

    private fun getDifficultyDisplayName(tier: DifficultyTier): String {
        return when (tier) {
            DifficultyTier.SAFE -> "Safe"
            DifficultyTier.EASY -> "Easy"
            DifficultyTier.MEDIUM -> "Medium"
            DifficultyTier.HARD -> "Hard"
            DifficultyTier.EXTREME -> "Extreme"
        }
    }

    private fun getDifficultyColorHex(tier: DifficultyTier): Long {
        return when (tier) {
            DifficultyTier.SAFE -> 0xFF4CAF50 // Green
            DifficultyTier.EASY -> 0xFF8BC34A // Light Green
            DifficultyTier.MEDIUM -> 0xFFFFC107 // Amber
            DifficultyTier.HARD -> 0xFFFF9800 // Orange
            DifficultyTier.EXTREME -> 0xFFF44336 // Red
        }
    }

    private fun getDifficultyWarning(tier: DifficultyTier): String? {
        return when (tier) {
            DifficultyTier.SAFE, DifficultyTier.EASY -> null
            DifficultyTier.MEDIUM -> "⚠️ Moderate challenge - Prepare adequately"
            DifficultyTier.HARD -> "⚠️ High difficulty - Recommended level 20+"
            DifficultyTier.EXTREME -> "⚠️ EXTREME DANGER - Recommended level 25+, proceed with caution!"
        }
    }

    private fun getWeatherDisplayName(condition: WeatherCondition): String {
        return when (condition) {
            WeatherCondition.CLEAR -> "Clear"
            WeatherCondition.RAINY -> "Rainy"
            WeatherCondition.STORMY -> "Stormy"
            WeatherCondition.FOGGY -> "Foggy"
            WeatherCondition.HOT -> "Hot"
            WeatherCondition.COLD -> "Cold"
        }
    }

    private fun getSeasonDisplayName(season: Season): String {
        return when (season) {
            Season.SPRING -> "Spring"
            Season.SUMMER -> "Summer"
            Season.AUTUMN -> "Autumn"
            Season.WINTER -> "Winter"
        }
    }

    private fun getResourceAvailability(tier: DifficultyTier, season: Season): String {
        val baseAvailability = when (tier) {
            DifficultyTier.SAFE -> "Abundant"
            DifficultyTier.EASY -> "Plentiful"
            DifficultyTier.MEDIUM -> "Normal"
            DifficultyTier.HARD -> "Scarce"
            DifficultyTier.EXTREME -> "Very Scarce"
        }

        val seasonModifier = when (season) {
            Season.SPRING -> " (boosted by spring growth)"
            Season.SUMMER -> ""
            Season.AUTUMN -> " (enhanced by autumn harvest)"
            Season.WINTER -> " (reduced by winter)"
        }

        return baseAvailability + seasonModifier
    }
}
