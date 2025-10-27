package com.jalmarquest.core.state.coordinator

import com.jalmarquest.core.state.ai.NpcAiGoalManager
import com.jalmarquest.core.state.catalogs.NpcCatalog
import com.jalmarquest.core.state.catalogs.EnemyCatalog
import com.jalmarquest.core.state.catalogs.LocationCatalog
import com.jalmarquest.core.state.ecosystem.PredatorPatrolManager
import com.jalmarquest.core.state.ecosystem.ResourceRespawnManager
import com.jalmarquest.core.state.weather.WeatherSystem
import com.jalmarquest.core.state.weather.SeasonalCycleManager
import com.jalmarquest.core.state.optimization.SpatialPartitioningSystem
import com.jalmarquest.core.state.optimization.FrameBudgetMonitor
import com.jalmarquest.core.state.optimization.AdaptiveUpdateCoordinator
import com.jalmarquest.core.state.optimization.UpdatePlan
import com.jalmarquest.core.state.optimization.EntityType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Optimized World Update Coordinator with Spatial Partitioning (Phase 6)
 * 
 * Improvements over Phase 5 coordinator:
 * - Spatial partitioning: Only update entities near player
 * - Frame budget monitoring: Skip low-priority work when running slow
 * - Adaptive throttling: Automatically reduce update frequency under load
 * 
 * Performance targets:
 * - 60 FPS (16.67ms per frame)
 * - <5ms for world updates
 * - 90% reduction in entity updates when player stationary
 */
class OptimizedWorldUpdateCoordinator(
    private val npcCatalog: NpcCatalog,
    private val enemyCatalog: EnemyCatalog,
    private val locationCatalog: LocationCatalog,
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
    
    // Optimization systems
    private val spatialPartitioning = SpatialPartitioningSystem(locationCatalog, timestampProvider)
    private val frameBudgetMonitor = FrameBudgetMonitor(timestampProvider)
    private val adaptiveCoordinator = AdaptiveUpdateCoordinator(frameBudgetMonitor, spatialPartitioning)
    
    private var lastMinuteUpdate = 0L
    private var last5MinUpdate = 0L
    private var lastHourUpdate = 0L
    
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    private val _lastUpdateTime = MutableStateFlow(0L)
    val lastUpdateTime: StateFlow<Long> = _lastUpdateTime.asStateFlow()
    
    /**
     * Start the coordinator and register all entities
     */
    fun start() {
        val now = timestampProvider()
        lastMinuteUpdate = now
        last5MinUpdate = now
        lastHourUpdate = now
        _isRunning.value = true
        
        // Register all NPCs for spatial tracking
        npcCatalog.getAllNpcs().forEach { npc ->
            spatialPartitioning.registerEntity(
                id = npc.id,
                type = EntityType.NPC,
                locationId = npc.locationId
            )
        }
        
        // Register all enemies for spatial tracking
        enemyCatalog.getAllEnemies().forEach { enemy ->
            enemy.habitat.forEach { locationId ->
                spatialPartitioning.registerEntity(
                    id = "${enemy.id}_${locationId}",
                    type = EntityType.ENEMY,
                    locationId = locationId
                )
            }
        }
        
        // Register resource locations (simplified - assumes every location has potential resources)
        locationCatalog.getAllLocations().forEach { location ->
            spatialPartitioning.registerEntity(
                id = "resources_${location.id}",
                type = EntityType.RESOURCE,
                locationId = location.id
            )
        }
    }
    
    /**
     * Stop the coordinator
     */
    fun stop() {
        _isRunning.value = false
    }
    
    /**
     * Update player location for spatial partitioning
     */
    fun updatePlayerLocation(locationId: String) {
        spatialPartitioning.updatePlayerLocation(locationId)
    }
    
    /**
     * Main update loop with performance monitoring
     */
    fun update() {
        if (!_isRunning.value) return
        
        frameBudgetMonitor.startFrame()
        
        val now = timestampProvider()
        var updated = false
        
        // Get adaptive update plan based on current performance
        val updatePlan = adaptiveCoordinator.planUpdates()
        
        // Every 1 minute: Resource respawns (only for nearby locations)
        if (now - lastMinuteUpdate >= MINUTE_MS) {
            if (updatePlan.updateImmediateEntities || updatePlan.updateNearEntities) {
                frameBudgetMonitor.measureSystem("resources") {
                    updateResourcesSelective(updatePlan)
                }
            }
            lastMinuteUpdate = now
            updated = true
        }
        
        // Every 5 minutes: Patrols, Weather, NPC AI (spatially partitioned)
        if (now - last5MinUpdate >= FIVE_MINUTES_MS) {
            // Always update weather (critical system)
            if (updatePlan.updateWeather) {
                frameBudgetMonitor.measureSystem("weather") {
                    weatherSystem.updateWeather()
                }
            }
            
            // Update patrols (only near player)
            frameBudgetMonitor.measureSystem("patrols") {
                updatePatrolsSelective(updatePlan)
            }
            
            // Update NPC AI (spatially filtered)
            frameBudgetMonitor.measureSystem("npc_ai") {
                updateNpcAiSelective(updatePlan)
            }
            
            last5MinUpdate = now
            updated = true
        }
        
        // Every 1 hour: Seasons (low-priority, skip if over budget)
        if (now - lastHourUpdate >= HOUR_MS) {
            if (updatePlan.updateSeasons) {
                frameBudgetMonitor.measureSystem("seasons") {
                    seasonalCycleManager.updateSeason()
                }
            }
            lastHourUpdate = now
            updated = true
        }
        
        if (updated) {
            _lastUpdateTime.value = now
        }
        
        frameBudgetMonitor.endFrame()
    }
    
    /**
     * Update resources only for locations based on update plan
     */
    private fun updateResourcesSelective(updatePlan: UpdatePlan) {
        val entitiesToUpdate = spatialPartitioning.getEntitiesToUpdate()
            .filter { it.type == EntityType.RESOURCE }
        
        // Filter by priority levels
        val filtered = entitiesToUpdate.filter { entity ->
            when (entity.priority) {
                com.jalmarquest.core.state.optimization.UpdatePriority.IMMEDIATE -> true
                com.jalmarquest.core.state.optimization.UpdatePriority.NEAR -> updatePlan.updateNearEntities
                com.jalmarquest.core.state.optimization.UpdatePriority.FAR -> updatePlan.updateFarEntities
                com.jalmarquest.core.state.optimization.UpdatePriority.INACTIVE -> updatePlan.updateInactiveEntities
            }
        }
        
        // Update only filtered resources
        if (filtered.isNotEmpty()) {
            resourceRespawnManager.updateResourceSpawns()
        }
    }
    
    /**
     * Update patrols only for locations near player
     */
    private fun updatePatrolsSelective(updatePlan: UpdatePlan) {
        if (updatePlan.updateImmediateEntities || updatePlan.updateNearEntities) {
            predatorPatrolManager.updatePredatorPositions()
        }
    }
    
    /**
     * Update NPC AI only for NPCs near player
     */
    private fun updateNpcAiSelective(updatePlan: UpdatePlan) {
        val npcEntities = spatialPartitioning.getEntitiesToUpdate()
            .filter { it.type == EntityType.NPC }
        
        // Filter by priority
        val filtered = npcEntities.filter { entity ->
            when (entity.priority) {
                com.jalmarquest.core.state.optimization.UpdatePriority.IMMEDIATE -> true
                com.jalmarquest.core.state.optimization.UpdatePriority.NEAR -> updatePlan.updateNearEntities
                com.jalmarquest.core.state.optimization.UpdatePriority.FAR -> updatePlan.updateFarEntities
                com.jalmarquest.core.state.optimization.UpdatePriority.INACTIVE -> false  // Never update inactive NPCs
            }
        }
        
        // Update filtered NPCs
        filtered.forEach { entity ->
            npcAiGoalManager.updateAi(entity.id)
        }
    }
    
    /**
     * Force immediate update of all systems (for testing)
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
     * Get performance statistics
     */
    fun getPerformanceStats() = frameBudgetMonitor.getStatistics()
    
    /**
     * Get spatial statistics
     */
    fun getSpatialStats() = spatialPartitioning.getStatistics()
    
    /**
     * Get time until next scheduled update
     */
    fun getTimeUntilNextUpdate(): UpdateSchedule {
        val now = timestampProvider()
        return UpdateSchedule(
            minuteUpdate = MINUTE_MS - (now - lastMinuteUpdate),
            fiveMinuteUpdate = FIVE_MINUTES_MS - (now - last5MinUpdate),
            hourUpdate = HOUR_MS - (now - lastHourUpdate)
        )
    }
}

/**
 * Update schedule information
 */
data class UpdateSchedule(
    val minuteUpdate: Long,
    val fiveMinuteUpdate: Long,
    val hourUpdate: Long
)
