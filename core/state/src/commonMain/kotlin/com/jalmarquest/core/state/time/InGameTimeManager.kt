package com.jalmarquest.core.state.time

import com.jalmarquest.core.state.perf.currentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages in-game time of day for time-gated events and quests.
 * Time progresses based on real-world time or can be manually advanced.
 */

enum class TimeOfDay {
    DAWN,    // 6:00 - 8:00
    MORNING, // 8:00 - 12:00
    AFTERNOON, // 12:00 - 18:00
    DUSK,    // 18:00 - 20:00
    NIGHT    // 20:00 - 6:00
}

data class GameTime(
    val hour: Int, // 0-23
    val minute: Int = 0
) {
    fun getTimeOfDay(): TimeOfDay {
        return when (hour) {
            in 6..7 -> TimeOfDay.DAWN
            in 8..11 -> TimeOfDay.MORNING
            in 12..17 -> TimeOfDay.AFTERNOON
            in 18..19 -> TimeOfDay.DUSK
            else -> TimeOfDay.NIGHT
        }
    }
    
    fun isNight(): Boolean = getTimeOfDay() == TimeOfDay.NIGHT
    fun isDawn(): Boolean = getTimeOfDay() == TimeOfDay.DAWN
}

class InGameTimeManager(
    private val timestampProvider: () -> Long = { currentTimeMillis() }
) {
    private var startRealTime: Long = timestampProvider()
    private var startGameHour: Int = 6 // Start at dawn
    
    // Time multiplier: how many in-game minutes pass per real minute
    private var timeMultiplier: Double = 24.0 // 1 real hour = 1 full day
    
    private val _currentTime = MutableStateFlow(GameTime(startGameHour))
    val currentTime: StateFlow<GameTime> = _currentTime.asStateFlow()
    
    /**
     * Update the current game time based on elapsed real time.
     */
    fun updateTime() {
        val now = timestampProvider()
        val elapsedRealMinutes = (now - startRealTime) / 60_000.0
        val elapsedGameMinutes = (elapsedRealMinutes * timeMultiplier).toInt()
        
        val totalMinutes = (startGameHour * 60) + elapsedGameMinutes
        val hour = (totalMinutes / 60) % 24
        val minute = totalMinutes % 60
        
        _currentTime.value = GameTime(hour, minute)
    }
    
    /**
     * Set the current game time directly (for testing or debugging).
     */
    fun setTime(hour: Int, minute: Int = 0) {
        startRealTime = timestampProvider()
        startGameHour = hour
        _currentTime.value = GameTime(hour, minute)
    }
    
    /**
     * Check if current time matches a specific time of day.
     */
    fun isTimeOfDay(timeOfDay: TimeOfDay): Boolean {
        updateTime()
        return _currentTime.value.getTimeOfDay() == timeOfDay
    }
    
    /**
     * Check if it's currently night time.
     */
    fun isNight(): Boolean {
        updateTime()
        return _currentTime.value.isNight()
    }
    
    /**
     * Check if it's currently dawn.
     */
    fun isDawn(): Boolean {
        updateTime()
        return _currentTime.value.isDawn()
    }
}
