package com.jalmarquest.core.state.ecosystem

import com.jalmarquest.core.state.catalogs.Enemy
import com.jalmarquest.core.state.catalogs.EnemyCatalog
import com.jalmarquest.core.state.catalogs.EnemyBehavior
import com.jalmarquest.core.state.time.InGameTimeManager
import com.jalmarquest.core.state.time.TimeOfDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * Phase 3: Ecosystem Simulation - Predator Patrols.
 * Manages enemy patrol routes and territorial behaviors using time-based movement.
 */

/**
 * A waypoint in a patrol route.
 */
@Serializable
data class PatrolWaypoint(
    val locationId: String,
    val arrivalTime: TimeOfDay,  // When enemy arrives at this location
    val waitDuration: Int = 0,   // Minutes to wait at this waypoint
    val guardRadius: Int = 5     // How far from waypoint enemy will chase (meters)
)

/**
 * A patrol route for an enemy.
 */
@Serializable
data class PatrolRoute(
    val id: String,
    val enemyId: String,
    val waypoints: List<PatrolWaypoint>,
    val isLoop: Boolean = true,  // True if patrol loops back to start
    val patrolSpeed: Float = 1.0f // Movement speed multiplier
) {
    /**
     * Get the current waypoint based on time of day.
     */
    fun getCurrentWaypoint(timeOfDay: TimeOfDay): PatrolWaypoint {
        // Find the most recent waypoint that the enemy has passed
        val orderedTimes = TimeOfDay.entries
        val currentIndex = orderedTimes.indexOf(timeOfDay)
        
        var nearestWaypoint = waypoints.first()
        for (waypoint in waypoints) {
            val waypointIndex = orderedTimes.indexOf(waypoint.arrivalTime)
            if (waypointIndex <= currentIndex) {
                nearestWaypoint = waypoint
            } else {
                break
            }
        }
        
        return nearestWaypoint
    }
    
    /**
     * Get the next waypoint in the route.
     */
    fun getNextWaypoint(current: PatrolWaypoint): PatrolWaypoint? {
        val currentIndex = waypoints.indexOf(current)
        if (currentIndex == -1) return waypoints.firstOrNull()
        
        val nextIndex = currentIndex + 1
        return if (nextIndex < waypoints.size) {
            waypoints[nextIndex]
        } else if (isLoop) {
            waypoints.first()
        } else {
            null
        }
    }
}

/**
 * Territory claimed by an enemy or group of enemies.
 */
@Serializable
data class Territory(
    val id: String,
    val name: String,
    val locationId: String,
    val controllingEnemyId: String,
    val threatLevel: Int,  // 1-10, how dangerous this territory is
    val isHostile: Boolean = true,
    val description: String? = null
)

/**
 * Current state of a patrolling enemy.
 */
@Serializable
data class PredatorState(
    val enemyId: String,
    val currentLocationId: String,
    val currentWaypointIndex: Int = 0,
    val isResting: Boolean = false,
    val isAggressive: Boolean = false,  // True if actively hunting player
    val lastSeenPlayerTime: Long? = null,
    val territoryId: String? = null
)

/**
 * Manager for predator patrols and territorial behaviors.
 */
class PredatorPatrolManager(
    private val enemyCatalog: EnemyCatalog,
    private val timeManager: InGameTimeManager,
    private val timestampProvider: () -> Long
) {
    private val _patrolRoutes = MutableStateFlow<Map<String, PatrolRoute>>(emptyMap())
    val patrolRoutes: StateFlow<Map<String, PatrolRoute>> = _patrolRoutes.asStateFlow()
    
    private val _territories = MutableStateFlow<Map<String, Territory>>(emptyMap())
    val territories: StateFlow<Map<String, Territory>> = _territories.asStateFlow()
    
    private val _predatorStates = MutableStateFlow<Map<String, PredatorState>>(emptyMap())
    val predatorStates: StateFlow<Map<String, PredatorState>> = _predatorStates.asStateFlow()
    
    init {
        registerDefaultPatrolRoutes()
        registerDefaultTerritories()
        initializePredatorStates()
    }
    
    /**
     * Register a patrol route for an enemy.
     */
    fun registerPatrolRoute(route: PatrolRoute) {
        _patrolRoutes.value = _patrolRoutes.value + (route.id to route)
    }
    
    /**
     * Register a territory.
     */
    fun registerTerritory(territory: Territory) {
        _territories.value = _territories.value + (territory.id to territory)
    }
    
    /**
     * Initialize states for all patrolling predators.
     */
    private fun initializePredatorStates() {
        val states = _patrolRoutes.value.values.associate { route ->
            val enemy = enemyCatalog.getEnemyById(route.enemyId)
            val currentWaypoint = route.getCurrentWaypoint(timeManager.currentTime.value.getTimeOfDay())
            
            route.enemyId to PredatorState(
                enemyId = route.enemyId,
                currentLocationId = currentWaypoint.locationId,
                isAggressive = enemy?.isAggressive ?: false
            )
        }
        _predatorStates.value = states
    }
    
    /**
     * Update all predator positions based on current time.
     */
    fun updatePredatorPositions() {
        val currentTime = timeManager.currentTime.value.getTimeOfDay()
        
        _predatorStates.value = _predatorStates.value.mapValues { (enemyId, state) ->
            val route = _patrolRoutes.value.values.firstOrNull { it.enemyId == enemyId }
            
            if (route != null) {
                val currentWaypoint = route.getCurrentWaypoint(currentTime)
                state.copy(
                    currentLocationId = currentWaypoint.locationId,
                    isResting = currentWaypoint.waitDuration > 0
                )
            } else {
                state
            }
        }
    }
    
    /**
     * Get all enemies currently at a location.
     */
    fun getEnemiesAtLocation(locationId: String): List<Enemy> {
        val enemyIds = _predatorStates.value
            .filter { it.value.currentLocationId == locationId }
            .keys
        
        return enemyIds.mapNotNull { enemyCatalog.getEnemyById(it) }
    }
    
    /**
     * Get the current location of a specific enemy.
     */
    fun getEnemyLocation(enemyId: String): String? {
        return _predatorStates.value[enemyId]?.currentLocationId
    }
    
    /**
     * Check if a location is in enemy territory.
     */
    fun isInTerritory(locationId: String): Territory? {
        return _territories.value.values.firstOrNull { it.locationId == locationId }
    }
    
    /**
     * Get all territories controlled by a specific enemy.
     */
    fun getEnemyTerritories(enemyId: String): List<Territory> {
        return _territories.value.values.filter { it.controllingEnemyId == enemyId }
    }
    
    /**
     * Notify that player entered a location (for aggro checking).
     */
    fun notifyPlayerEntered(locationId: String) {
        val currentTime = timestampProvider()
        val territory = isInTerritory(locationId)
        
        if (territory != null && territory.isHostile) {
            // Make controlling enemy aggressive
            _predatorStates.value = _predatorStates.value.mapValues { (enemyId, state) ->
                if (enemyId == territory.controllingEnemyId) {
                    state.copy(
                        isAggressive = true,
                        lastSeenPlayerTime = currentTime
                    )
                } else {
                    state
                }
            }
        }
    }
    
    /**
     * Get patrol route for an enemy.
     */
    fun getPatrolRoute(enemyId: String): PatrolRoute? {
        return _patrolRoutes.value.values.firstOrNull { it.enemyId == enemyId }
    }
    
    /**
     * Get description of enemy activity at a location.
     */
    fun getEnemyActivityDescription(locationId: String): String? {
        val enemies = getEnemiesAtLocation(locationId)
        if (enemies.isEmpty()) return null
        
        val enemy = enemies.first()
        val state = _predatorStates.value[enemy.id]
        
        return when {
            state?.isResting == true -> "${enemy.name} is resting here."
            state?.isAggressive == true -> "${enemy.name} is on high alert!"
            enemy.behavior == EnemyBehavior.PATROL -> "${enemy.name} is patrolling this area."
            enemy.behavior == EnemyBehavior.TERRITORIAL -> "${enemy.name} guards this territory."
            enemy.behavior == EnemyBehavior.AMBUSH -> "You sense danger lurking nearby..."
            else -> "${enemy.name} is present."
        }
    }
    
    /**
     * Register default patrol routes for common enemies.
     */
    private fun registerDefaultPatrolRoutes() {
        // Forest spider patrol
        registerPatrolRoute(
            PatrolRoute(
                id = "patrol_forest_spider",
                enemyId = "enemy_orb_weaver_spider",
                waypoints = listOf(
                    PatrolWaypoint("forest_spider_webs", TimeOfDay.DAWN),
                    PatrolWaypoint("forest_fern_tunnel", TimeOfDay.MORNING, waitDuration = 30),
                    PatrolWaypoint("forest_canopy_heights", TimeOfDay.AFTERNOON),
                    PatrolWaypoint("forest_spider_webs", TimeOfDay.DUSK, waitDuration = 60),
                    PatrolWaypoint("forest_spider_webs", TimeOfDay.NIGHT, waitDuration = 120)
                ),
                isLoop = true
            )
        )
        
        // Beach crab patrol
        registerPatrolRoute(
            PatrolRoute(
                id = "patrol_hermit_crab",
                enemyId = "enemy_hermit_crab",
                waypoints = listOf(
                    PatrolWaypoint("beach_tide_pools", TimeOfDay.DAWN),
                    PatrolWaypoint("beach_kelp_forest", TimeOfDay.MORNING, waitDuration = 45),
                    PatrolWaypoint("beach_driftwood_maze", TimeOfDay.AFTERNOON),
                    PatrolWaypoint("beach_sand_dunes", TimeOfDay.DUSK),
                    PatrolWaypoint("beach_tide_pools", TimeOfDay.NIGHT, waitDuration = 90)
                ),
                isLoop = true
            )
        )
        
        // Swamp alligator patrol (boss)
        registerPatrolRoute(
            PatrolRoute(
                id = "patrol_ancient_alligator",
                enemyId = "enemy_ancient_alligator",
                waypoints = listOf(
                    PatrolWaypoint("swamp_murky_pools", TimeOfDay.DAWN, waitDuration = 60),
                    PatrolWaypoint("swamp_gators_den", TimeOfDay.MORNING, waitDuration = 120),
                    PatrolWaypoint("swamp_mangrove_maze", TimeOfDay.AFTERNOON),
                    PatrolWaypoint("swamp_murky_pools", TimeOfDay.DUSK),
                    PatrolWaypoint("swamp_gators_den", TimeOfDay.NIGHT, waitDuration = 180)
                ),
                isLoop = true,
                patrolSpeed = 0.8f  // Slow patrol
            )
        )
        
        // Mountain eagle patrol (boss)
        registerPatrolRoute(
            PatrolRoute(
                id = "patrol_eagle_matriarch",
                enemyId = "enemy_eagle_matriarch",
                waypoints = listOf(
                    PatrolWaypoint("mountains_eagles_aerie", TimeOfDay.DAWN, waitDuration = 90),
                    PatrolWaypoint("mountains_cliff_face", TimeOfDay.MORNING),
                    PatrolWaypoint("mountains_wind_tunnel", TimeOfDay.AFTERNOON, waitDuration = 30),
                    PatrolWaypoint("mountains_summit", TimeOfDay.DUSK),
                    PatrolWaypoint("mountains_eagles_aerie", TimeOfDay.NIGHT, waitDuration = 240)
                ),
                isLoop = true
            )
        )
        
        // Ruins guardian patrol (boss)
        registerPatrolRoute(
            PatrolRoute(
                id = "patrol_eternal_guardian",
                enemyId = "enemy_eternal_guardian",
                waypoints = listOf(
                    PatrolWaypoint("ruins_crumbling_walls", TimeOfDay.DAWN),
                    PatrolWaypoint("ruins_statue_garden", TimeOfDay.MORNING, waitDuration = 45),
                    PatrolWaypoint("ruins_sacred_altar", TimeOfDay.AFTERNOON, waitDuration = 90),
                    PatrolWaypoint("ruins_echo_chamber", TimeOfDay.DUSK),
                    PatrolWaypoint("ruins_treasury_vault", TimeOfDay.NIGHT, waitDuration = 120)
                ),
                isLoop = true,
                patrolSpeed = 0.9f
            )
        )
        
        // Crow patrol (Buttonburgh threat)
        registerPatrolRoute(
            PatrolRoute(
                id = "patrol_territorial_crow",
                enemyId = "enemy_territorial_crow",
                waypoints = listOf(
                    PatrolWaypoint("forest_crows_perch", TimeOfDay.DAWN, waitDuration = 60),
                    PatrolWaypoint("buttonburgh_training_grounds", TimeOfDay.MORNING),
                    PatrolWaypoint("forest_crows_perch", TimeOfDay.AFTERNOON, waitDuration = 120),
                    PatrolWaypoint("buttonburgh_garden_terraces", TimeOfDay.DUSK),
                    PatrolWaypoint("forest_crows_perch", TimeOfDay.NIGHT, waitDuration = 180)
                ),
                isLoop = true
            )
        )
    }
    
    /**
     * Register default territories.
     */
    private fun registerDefaultTerritories() {
        // Spider territory
        registerTerritory(
            Territory(
                id = "territory_spider_webs",
                name = "Spider's Domain",
                locationId = "forest_spider_webs",
                controllingEnemyId = "enemy_orb_weaver_spider",
                threatLevel = 4,
                description = "Thick webs cover the trees. The giant spider that made them won't appreciate intruders."
            )
        )
        
        // Alligator territory
        registerTerritory(
            Territory(
                id = "territory_gators_den",
                name = "Alligator's Den",
                locationId = "swamp_gators_den",
                controllingEnemyId = "enemy_ancient_alligator",
                threatLevel = 8,
                description = "This murky area is the domain of an ancient predator. Enter at your own risk."
            )
        )
        
        // Eagle territory
        registerTerritory(
            Territory(
                id = "territory_eagles_aerie",
                name = "Eagle's Aerie",
                locationId = "mountains_eagles_aerie",
                controllingEnemyId = "enemy_eagle_matriarch",
                threatLevel = 9,
                description = "The nest of the Eagle Matriarch. She will defend her territory fiercely."
            )
        )
        
        // Guardian territory
        registerTerritory(
            Territory(
                id = "territory_sacred_altar",
                name = "Sacred Altar",
                locationId = "ruins_sacred_altar",
                controllingEnemyId = "enemy_eternal_guardian",
                threatLevel = 10,
                description = "An ancient guardian protects this sacred place. Few who challenge it survive."
            )
        )
        
        // Magpie territory
        registerTerritory(
            Territory(
                id = "territory_magpie_nest",
                name = "Magpie King's Hoard",
                locationId = "forest_magpie_nest",
                controllingEnemyId = "enemy_magpie_king",
                threatLevel = 7,
                description = "The Magpie King's treasure hoard. He won't let anyone near his shinies."
            )
        )
        
        // Crow territory
        registerTerritory(
            Territory(
                id = "territory_crows_perch",
                name = "Crow's Perch",
                locationId = "forest_crows_perch",
                controllingEnemyId = "enemy_territorial_crow",
                threatLevel = 5,
                description = "A territorial crow has claimed this area. It attacks anyone who comes too close."
            )
        )
        
        // Ant colony (neutral territory)
        registerTerritory(
            Territory(
                id = "territory_ant_hill",
                name = "Ant Colony",
                locationId = "forest_ant_hill",
                controllingEnemyId = "enemy_soldier_ant",
                threatLevel = 3,
                isHostile = false,  // Can become friendly
                description = "The entrance to a vast ant colony. The ants will defend their home if threatened."
            )
        )
    }
}
