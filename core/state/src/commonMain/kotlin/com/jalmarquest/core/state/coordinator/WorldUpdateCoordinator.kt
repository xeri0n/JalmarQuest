package com.jalmarquest.core.state.coordinator

import com.jalmarquest.core.state.ai.NpcAiGoalManager
import com.jalmarquest.core.state.catalogs.NpcCatalog
import com.jalmarquest.core.state.ecosystem.PredatorPatrolManager
import com.jalmarquest.core.state.ecosystem.ResourceRespawnManager
import com.jalmarquest.core.state.weather.WeatherSystem
import com.jalmarquest.core.state.weather.SeasonalCycleManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Coordinates periodic updates for all world simulation systems
 * Batches updates by frequency to optimize performance
 */
class WorldUpdateCoordinator(
    private val npcCatalog: NpcCatalog,
    private val npcAiGoalManager: NpcAiGoalManager,
    private val predatorPatrolManager: PredatorPatrolManager,
    private val resourceRespawnManager: ResourceRespawnManager,
    private val weatherSystem: WeatherSystem,
    private val seasonalCycleManager: SeasonalCycleManager,
    private val timestampProvider: () -> Long
) {
    companion object {
        const val MINUTE_MS = 60_000L
        const val FIVE_MINUTES_MS = 300_000L
        const val HOUR_MS = 3_600_000L
    }
    
    private var lastMinuteUpdate = 0L
    private var last5MinUpdate = 0L
    private var lastHourUpdate = 0L
    
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    private val _lastUpdateTime = MutableStateFlow(0L)
    val lastUpdateTime: StateFlow<Long> = _lastUpdateTime.asStateFlow()
    
    /**
     * Start the coordinator
     */
    fun start() {
        val now = timestampProvider()
        lastMinuteUpdate = now
        last5MinUpdate = now
        lastHourUpdate = now
        _isRunning.value = true
    }
    
    /**
     * Stop the coordinator
     */
    fun stop() {
        _isRunning.value = false
    }
    
    /**
     * Main update loop - call this periodically (e.g., every frame or every second)
     */
    fun update() {
        if (!_isRunning.value) return
        
        val now = timestampProvider()
        var updated = false
        
        // Every 1 minute: Resource respawns
        if (now - lastMinuteUpdate >= MINUTE_MS) {
            resourceRespawnManager.updateResourceSpawns()
            lastMinuteUpdate = now
            updated = true
        }
        
        // Every 5 minutes: Patrols, Weather, NPC AI
        if (now - last5MinUpdate >= FIVE_MINUTES_MS) {
            predatorPatrolManager.updatePredatorPositions()
            weatherSystem.updateWeather()
            // Update AI for all NPCs
            npcCatalog.getAllNpcs().forEach { npc ->
                npcAiGoalManager.updateAi(npc.id)
            }
            last5MinUpdate = now
            updated = true
        }
        
        // Every 1 hour: Seasons
        if (now - lastHourUpdate >= HOUR_MS) {
            seasonalCycleManager.updateSeason()
            lastHourUpdate = now
            updated = true
        }
        
        if (updated) {
            _lastUpdateTime.value = now
        }
    }
    
    /**
     * Force immediate update of all systems (for testing or manual triggers)
     */
    fun forceUpdate() {
        val now = timestampProvider()
        
        resourceRespawnManager.updateResourceSpawns()
        predatorPatrolManager.updatePredatorPositions()
        weatherSystem.updateWeather()
        // Update AI for all NPCs
        npcCatalog.getAllNpcs().forEach { npc ->
            npcAiGoalManager.updateAi(npc.id)
        }
        seasonalCycleManager.updateSeason()
        
        lastMinuteUpdate = now
        last5MinUpdate = now
        lastHourUpdate = now
        _lastUpdateTime.value = now
    }
    
    /**
     * Get time until next update for each frequency
     */
    fun getTimeUntilNextUpdate(): UpdateSchedule {
        val now = timestampProvider()
        return UpdateSchedule(
            minuteUpdate = (MINUTE_MS - (now - lastMinuteUpdate)).coerceAtLeast(0),
            fiveMinuteUpdate = (FIVE_MINUTES_MS - (now - last5MinUpdate)).coerceAtLeast(0),
            hourUpdate = (HOUR_MS - (now - lastHourUpdate)).coerceAtLeast(0)
        )
    }
    
    data class UpdateSchedule(
        val minuteUpdate: Long,      // ms until next 1-minute update
        val fiveMinuteUpdate: Long,  // ms until next 5-minute update
        val hourUpdate: Long         // ms until next hour update
    )
}
