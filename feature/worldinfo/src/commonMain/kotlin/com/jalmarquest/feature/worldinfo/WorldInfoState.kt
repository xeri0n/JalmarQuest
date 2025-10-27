package com.jalmarquest.feature.worldinfo

/**
 * World Info State - UI representation of world information
 * 
 * This data class aggregates state from Phase 4 managers:
 * - PlayerLocationTracker: Current location and region
 * - RegionDifficultyManager: Difficulty tier, scaling, warnings
 * - WeatherSystem: Current weather, severity, description
 * - SeasonalCycleManager: Current season, day progression, description
 */
data class WorldInfoState(
    val locationName: String,
    val regionName: String,
    val difficultyTier: String,
    val difficultyColorHex: Long, // Color as hex (e.g., 0xFF4CAF50)
    val difficultyWarning: String?,
    val weatherDisplay: String,
    val weatherDesc: String,
    val weatherSeverity: Int, // 1-10
    val seasonDisplay: String,
    val seasonDay: Int,
    val seasonDesc: String,
    val resourceAvailability: String
)
