package com.jalmarquest.core.state.weather

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlin.random.Random

/**
 * Weather conditions (aligned with DynamicDialogueManager)
 */
enum class WeatherCondition {
    CLEAR,
    RAINY,
    STORMY,
    FOGGY,
    HOT,
    COLD
}

/**
 * Current weather state
 */
@Serializable
data class WeatherState(
    val condition: String,  // WeatherCondition name
    val severity: Int,      // 1-10 scale
    val startedAt: Long,
    val durationMinutes: Int
)

/**
 * Manages dynamic weather system
 */
class WeatherSystem(
    private val timestampProvider: () -> Long
) {
    private val _currentWeather = MutableStateFlow(
        WeatherState(
            condition = WeatherCondition.CLEAR.name,
            severity = 1,
            startedAt = timestampProvider(),
            durationMinutes = 240  // 4 hours default
        )
    )
    val currentWeather: StateFlow<WeatherState> = _currentWeather.asStateFlow()
    
    /**
     * Update weather (call periodically, e.g., every game hour)
     */
    fun updateWeather() {
        val current = _currentWeather.value
        val now = timestampProvider()
        val minutesElapsed = (now - current.startedAt) / 60_000
        
        // Check if weather should change
        if (minutesElapsed >= current.durationMinutes) {
            changeWeather()
        }
    }
    
    /**
     * Force a weather change
     */
    fun changeWeather() {
        val newCondition = selectRandomWeather()
        val newSeverity = selectSeverity(newCondition)
        val newDuration = selectDuration(newCondition)
        
        _currentWeather.value = WeatherState(
            condition = newCondition.name,
            severity = newSeverity,
            startedAt = timestampProvider(),
            durationMinutes = newDuration
        )
    }
    
    /**
     * Set specific weather (for quests/events)
     */
    fun setWeather(condition: WeatherCondition, severity: Int, durationMinutes: Int) {
        _currentWeather.value = WeatherState(
            condition = condition.name,
            severity = severity.coerceIn(1, 10),
            startedAt = timestampProvider(),
            durationMinutes = durationMinutes
        )
    }
    
    /**
     * Get current weather condition enum
     */
    fun getCurrentCondition(): WeatherCondition {
        return try {
            WeatherCondition.valueOf(_currentWeather.value.condition)
        } catch (e: IllegalArgumentException) {
            WeatherCondition.CLEAR
        }
    }
    
    /**
     * Check if specific weather is active
     */
    fun isWeather(condition: WeatherCondition): Boolean {
        return getCurrentCondition() == condition
    }
    
    /**
     * Get weather description for UI
     */
    fun getWeatherDescription(): String {
        val weather = _currentWeather.value
        val condition = getCurrentCondition()
        val severity = weather.severity
        
        return when (condition) {
            WeatherCondition.CLEAR -> when {
                severity <= 3 -> "Clear skies with gentle breeze"
                severity <= 6 -> "Pleasant clear weather"
                else -> "Brilliant sunshine"
            }
            WeatherCondition.RAINY -> when {
                severity <= 3 -> "Light drizzle"
                severity <= 6 -> "Steady rain"
                else -> "Heavy downpour"
            }
            WeatherCondition.STORMY -> when {
                severity <= 3 -> "Distant thunder"
                severity <= 6 -> "Thunderstorm approaching"
                else -> "Violent storm"
            }
            WeatherCondition.FOGGY -> when {
                severity <= 3 -> "Light mist"
                severity <= 6 -> "Thick fog"
                else -> "Impenetrable fog"
            }
            WeatherCondition.HOT -> when {
                severity <= 3 -> "Warm day"
                severity <= 6 -> "Hot and sunny"
                else -> "Scorching heat"
            }
            WeatherCondition.COLD -> when {
                severity <= 3 -> "Cool breeze"
                severity <= 6 -> "Chilly wind"
                else -> "Freezing cold"
            }
        }
    }
    
    private fun selectRandomWeather(): WeatherCondition {
        // Weighted probability for natural weather distribution
        val roll = Random.nextDouble()
        return when {
            roll < 0.40 -> WeatherCondition.CLEAR      // 40% clear
            roll < 0.60 -> WeatherCondition.RAINY      // 20% rainy
            roll < 0.70 -> WeatherCondition.HOT        // 10% hot
            roll < 0.80 -> WeatherCondition.COLD       // 10% cold
            roll < 0.90 -> WeatherCondition.FOGGY      // 10% foggy
            else -> WeatherCondition.STORMY            // 10% stormy
        }
    }
    
    private fun selectSeverity(condition: WeatherCondition): Int {
        return when (condition) {
            WeatherCondition.CLEAR -> Random.nextInt(1, 4)       // 1-3 for clear
            WeatherCondition.HOT -> Random.nextInt(3, 8)         // 3-7 for hot
            WeatherCondition.COLD -> Random.nextInt(3, 8)        // 3-7 for cold
            WeatherCondition.RAINY -> Random.nextInt(2, 7)       // 2-6 for rain
            WeatherCondition.FOGGY -> Random.nextInt(4, 9)       // 4-8 for fog
            WeatherCondition.STORMY -> Random.nextInt(6, 11)     // 6-10 for storms
        }
    }
    
    private fun selectDuration(condition: WeatherCondition): Int {
        // Duration in game minutes
        return when (condition) {
            WeatherCondition.CLEAR -> Random.nextInt(180, 361)      // 3-6 hours
            WeatherCondition.HOT -> Random.nextInt(240, 481)        // 4-8 hours
            WeatherCondition.COLD -> Random.nextInt(180, 421)       // 3-7 hours
            WeatherCondition.RAINY -> Random.nextInt(60, 181)       // 1-3 hours
            WeatherCondition.FOGGY -> Random.nextInt(90, 241)       // 1.5-4 hours
            WeatherCondition.STORMY -> Random.nextInt(30, 91)       // 30min-1.5 hours
        }
    }
    
    /**
     * Load weather state
     */
    fun loadState(weatherState: WeatherState) {
        _currentWeather.value = weatherState
    }
}

/**
 * Seasonal cycle manager (aligned with ResourceRespawnManager)
 */
enum class Season {
    SPRING,
    SUMMER,
    AUTUMN,
    WINTER
}

/**
 * Seasonal cycle state
 */
@Serializable
data class SeasonalState(
    val currentSeason: String,  // Season name
    val dayOfSeason: Int,       // 0-89 (90 days per season)
    val seasonStartedAt: Long
)

/**
 * Manages seasonal progression
 */
class SeasonalCycleManager(
    private val timestampProvider: () -> Long
) {
    companion object {
        const val DAYS_PER_SEASON = 90
        const val REAL_HOURS_PER_GAME_DAY = 1  // 1 real hour = 1 game day for fast progression
    }
    
    private val _seasonalState = MutableStateFlow(
        SeasonalState(
            currentSeason = Season.SPRING.name,
            dayOfSeason = 0,
            seasonStartedAt = timestampProvider()
        )
    )
    val seasonalState: StateFlow<SeasonalState> = _seasonalState.asStateFlow()
    
    /**
     * Update seasonal progression
     */
    fun updateSeason() {
        val current = _seasonalState.value
        val now = timestampProvider()
        val realHoursElapsed = (now - current.seasonStartedAt) / 3_600_000.0
        val gameDaysElapsed = realHoursElapsed.toInt() / REAL_HOURS_PER_GAME_DAY
        
        if (gameDaysElapsed > current.dayOfSeason) {
            val newDay = gameDaysElapsed % DAYS_PER_SEASON
            val seasonsPassed = gameDaysElapsed / DAYS_PER_SEASON
            
            if (seasonsPassed > 0) {
                // Advance to next season
                val currentSeason = getCurrentSeason()
                val nextSeason = getNextSeason(currentSeason, seasonsPassed)
                _seasonalState.value = SeasonalState(
                    currentSeason = nextSeason.name,
                    dayOfSeason = newDay,
                    seasonStartedAt = now
                )
            } else {
                // Still in same season
                _seasonalState.value = current.copy(dayOfSeason = newDay)
            }
        }
    }
    
    /**
     * Get current season enum
     */
    fun getCurrentSeason(): Season {
        return try {
            Season.valueOf(_seasonalState.value.currentSeason)
        } catch (e: IllegalArgumentException) {
            Season.SPRING
        }
    }
    
    /**
     * Force set season (for testing/events)
     */
    fun setSeason(season: Season) {
        _seasonalState.value = SeasonalState(
            currentSeason = season.name,
            dayOfSeason = 0,
            seasonStartedAt = timestampProvider()
        )
    }
    
    /**
     * Get seasonal description
     */
    fun getSeasonDescription(): String {
        val state = _seasonalState.value
        val season = getCurrentSeason()
        val progress = (state.dayOfSeason.toDouble() / DAYS_PER_SEASON * 100).toInt()
        
        val phase = when (state.dayOfSeason) {
            in 0..29 -> "Early"
            in 30..59 -> "Mid"
            else -> "Late"
        }
        
        return "$phase ${season.name.lowercase().replaceFirstChar { it.uppercase() }} ($progress% through season)"
    }
    
    /**
     * Get days remaining in current season
     */
    fun getDaysRemainingInSeason(): Int {
        return DAYS_PER_SEASON - _seasonalState.value.dayOfSeason
    }
    
    private fun getNextSeason(current: Season, advanceBy: Int): Season {
        val seasons = Season.entries
        val currentIndex = seasons.indexOf(current)
        val nextIndex = (currentIndex + advanceBy) % seasons.size
        return seasons[nextIndex]
    }
    
    /**
     * Load seasonal state
     */
    fun loadState(seasonalState: SeasonalState) {
        _seasonalState.value = seasonalState
    }
}
