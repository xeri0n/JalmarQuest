package com.jalmarquest.core.state.ecosystem

import com.jalmarquest.core.state.catalogs.LocationCatalog
import com.jalmarquest.core.state.time.InGameTimeManager
import com.jalmarquest.core.state.time.TimeOfDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * Phase 3: Ecosystem Simulation - Resource Respawns.
 * Manages resource regeneration with biome-specific respawn rates
 * and seasonal variations.
 */

/**
 * Seasons in the game world.
 */
enum class Season {
    SPRING,  // High growth, abundant resources
    SUMMER,  // Normal growth, hot weather
    AUTUMN,  // Harvest time, varied resources
    WINTER   // Low growth, scarce resources
}

/**
 * Types of resources that can respawn.
 */
enum class ResourceType {
    HERB,        // Forageable plants
    MUSHROOM,    // Fungi
    BERRY,       // Berries and fruits
    MINERAL,     // Crystals and stones
    INSECT,      // Catchable insects
    SEED,        // Plant seeds
    FLOWER,      // Rare flowers
    WOOD,        // Sticks and bark
    SHELL,       // Beach shells
    FEATHER      // Bird feathers
}

/**
 * A resource spawn point in the world.
 */
@Serializable
data class ResourceSpawn(
    val id: String,
    val locationId: String,
    val resourceType: ResourceType,
    val itemId: String,  // Item that spawns
    val baseRespawnMinutes: Int,  // Base respawn time
    val quantity: Int = 1,  // How many spawn at once
    val seasonalModifier: Map<Season, Float> = emptyMap(),  // Season multipliers
    val timeOfDayModifier: Map<TimeOfDay, Float> = emptyMap(),  // Time multipliers
    val isRare: Boolean = false,  // Rare spawns have lower chance
    val spawnChance: Float = 1.0f  // Chance to spawn (0.0-1.0)
)

/**
 * Current state of a resource spawn.
 */
@Serializable
data class ResourceSpawnState(
    val spawnId: String,
    val lastHarvestedTime: Long? = null,
    val nextRespawnTime: Long? = null,
    val isAvailable: Boolean = true,
    val harvestCount: Int = 0  // Track total harvests for analytics
)

/**
 * Biome-specific resource configuration.
 */
@Serializable
data class BiomeResourceConfig(
    val biomeId: String,
    val primaryResources: List<ResourceType>,  // Common in this biome
    val secondaryResources: List<ResourceType>, // Uncommon in this biome
    val rareResources: List<ResourceType>,      // Rare in this biome
    val respawnRateMultiplier: Float = 1.0f,    // Global modifier for biome
    val seasonalVariation: Boolean = true       // Whether seasons affect spawns
)

/**
 * Manager for resource respawning and regeneration.
 */
class ResourceRespawnManager(
    private val locationCatalog: LocationCatalog,
    private val timeManager: InGameTimeManager,
    private val timestampProvider: () -> Long
) {
    private val _resourceSpawns = MutableStateFlow<Map<String, ResourceSpawn>>(emptyMap())
    val resourceSpawns: StateFlow<Map<String, ResourceSpawn>> = _resourceSpawns.asStateFlow()
    
    private val _spawnStates = MutableStateFlow<Map<String, ResourceSpawnState>>(emptyMap())
    val spawnStates: StateFlow<Map<String, ResourceSpawnState>> = _spawnStates.asStateFlow()
    
    private val _currentSeason = MutableStateFlow(Season.SPRING)
    val currentSeason: StateFlow<Season> = _currentSeason.asStateFlow()
    
    private val biomeConfigs = mutableMapOf<String, BiomeResourceConfig>()
    
    init {
        registerDefaultBiomeConfigs()
        registerDefaultResourceSpawns()
        initializeSpawnStates()
    }
    
    /**
     * Register a biome resource configuration.
     */
    fun registerBiomeConfig(config: BiomeResourceConfig) {
        biomeConfigs[config.biomeId] = config
    }
    
    /**
     * Register a resource spawn point.
     */
    fun registerResourceSpawn(spawn: ResourceSpawn) {
        _resourceSpawns.value = _resourceSpawns.value + (spawn.id to spawn)
        
        // Initialize state
        _spawnStates.value = _spawnStates.value + (spawn.id to ResourceSpawnState(
            spawnId = spawn.id,
            isAvailable = true
        ))
    }
    
    /**
     * Initialize spawn states for all registered spawns.
     */
    private fun initializeSpawnStates() {
        val states = _resourceSpawns.value.mapValues { (id, _) ->
            ResourceSpawnState(
                spawnId = id,
                isAvailable = true
            )
        }
        _spawnStates.value = states
    }
    
    /**
     * Get all available resources at a location.
     */
    fun getAvailableResourcesAtLocation(locationId: String): List<Pair<ResourceSpawn, ResourceSpawnState>> {
        return _resourceSpawns.value.values
            .filter { it.locationId == locationId }
            .mapNotNull { spawn ->
                val state = _spawnStates.value[spawn.id]
                if (state?.isAvailable == true) {
                    spawn to state
                } else {
                    null
                }
            }
    }
    
    /**
     * Harvest a resource.
     */
    fun harvestResource(spawnId: String): Boolean {
        val spawn = _resourceSpawns.value[spawnId] ?: return false
        val state = _spawnStates.value[spawnId] ?: return false
        
        if (!state.isAvailable) return false
        
        val currentTime = timestampProvider()
        val respawnDelay = calculateRespawnTime(spawn)
        val nextRespawn = currentTime + (respawnDelay * 60 * 1000)  // Convert minutes to ms
        
        _spawnStates.value = _spawnStates.value + (spawnId to state.copy(
            lastHarvestedTime = currentTime,
            nextRespawnTime = nextRespawn,
            isAvailable = false,
            harvestCount = state.harvestCount + 1
        ))
        
        return true
    }
    
    /**
     * Calculate respawn time for a resource based on modifiers.
     */
    private fun calculateRespawnTime(spawn: ResourceSpawn): Int {
        var respawnTime = spawn.baseRespawnMinutes.toFloat()
        
        // Apply seasonal modifier
        val seasonalMod = spawn.seasonalModifier[_currentSeason.value] ?: 1.0f
        respawnTime *= seasonalMod
        
        // Apply time of day modifier
        val timeOfDay = timeManager.currentTime.value.getTimeOfDay()
        val timeMod = spawn.timeOfDayModifier[timeOfDay] ?: 1.0f
        respawnTime *= timeMod
        
        // Apply biome modifier (would need Location.biome field - simplified for now)
        // val location = locationCatalog.getLocationById(spawn.locationId)
        // val biomeConfig = location?.let { biomeConfigs[it.biome] }
        
        // Rare resources take longer
        if (spawn.isRare) {
            respawnTime *= 2.0f
        }
        
        return respawnTime.toInt().coerceAtLeast(1)
    }
    
    /**
     * Update all resource spawns (check for respawns).
     */
    fun updateResourceSpawns() {
        val currentTime = timestampProvider()
        
        _spawnStates.value = _spawnStates.value.mapValues { (spawnId, state) ->
            if (!state.isAvailable && state.nextRespawnTime != null) {
                if (currentTime >= state.nextRespawnTime) {
                    val spawn = _resourceSpawns.value[spawnId]
                    val didSpawn = (Math.random().toFloat() <= (spawn?.spawnChance ?: 1.0f))
                    
                    if (didSpawn) {
                        state.copy(
                            isAvailable = true,
                            nextRespawnTime = null
                        )
                    } else {
                        // Didn't spawn, try again later
                        val spawn = _resourceSpawns.value[spawnId]!!
                        val retryDelay = calculateRespawnTime(spawn) / 4  // Retry in 1/4 time
                        state.copy(
                            nextRespawnTime = currentTime + (retryDelay * 60 * 1000)
                        )
                    }
                } else {
                    state
                }
            } else {
                state
            }
        }
    }
    
    /**
     * Change the current season.
     */
    fun setSeason(season: Season) {
        _currentSeason.value = season
        // Force respawn recalculation for all resources
        updateResourceSpawns()
    }
    
    /**
     * Get resources by type.
     */
    fun getResourcesByType(type: ResourceType): List<ResourceSpawn> {
        return _resourceSpawns.value.values.filter { it.resourceType == type }
    }
    
    /**
     * Register default biome configurations.
     */
    private fun registerDefaultBiomeConfigs() {
        // Forest biome
        registerBiomeConfig(
            BiomeResourceConfig(
                biomeId = "forest",
                primaryResources = listOf(ResourceType.HERB, ResourceType.MUSHROOM, ResourceType.WOOD),
                secondaryResources = listOf(ResourceType.BERRY, ResourceType.INSECT, ResourceType.FEATHER),
                rareResources = listOf(ResourceType.FLOWER),
                respawnRateMultiplier = 1.0f,
                seasonalVariation = true
            )
        )
        
        // Beach biome
        registerBiomeConfig(
            BiomeResourceConfig(
                biomeId = "beach",
                primaryResources = listOf(ResourceType.SHELL, ResourceType.MINERAL),
                secondaryResources = listOf(ResourceType.HERB, ResourceType.INSECT),
                rareResources = listOf(ResourceType.FLOWER),
                respawnRateMultiplier = 0.9f,  // Slightly slower respawn
                seasonalVariation = false  // Beach less affected by seasons
            )
        )
        
        // Swamp biome
        registerBiomeConfig(
            BiomeResourceConfig(
                biomeId = "swamp",
                primaryResources = listOf(ResourceType.MUSHROOM, ResourceType.HERB, ResourceType.INSECT),
                secondaryResources = listOf(ResourceType.WOOD, ResourceType.MINERAL),
                rareResources = listOf(ResourceType.FLOWER),
                respawnRateMultiplier = 1.2f,  // Faster respawn (fertile swamp)
                seasonalVariation = true
            )
        )
        
        // Mountains biome
        registerBiomeConfig(
            BiomeResourceConfig(
                biomeId = "mountains",
                primaryResources = listOf(ResourceType.MINERAL, ResourceType.HERB),
                secondaryResources = listOf(ResourceType.FLOWER, ResourceType.FEATHER),
                rareResources = listOf(ResourceType.MUSHROOM),
                respawnRateMultiplier = 0.8f,  // Slower respawn (harsh environment)
                seasonalVariation = true
            )
        )
        
        // Ruins biome
        registerBiomeConfig(
            BiomeResourceConfig(
                biomeId = "ruins",
                primaryResources = listOf(ResourceType.MINERAL, ResourceType.MUSHROOM),
                secondaryResources = listOf(ResourceType.HERB, ResourceType.INSECT),
                rareResources = listOf(ResourceType.FLOWER),
                respawnRateMultiplier = 0.7f,  // Slowest respawn (ancient place)
                seasonalVariation = false  // Ruins unaffected by seasons
            )
        )
    }
    
    /**
     * Register default resource spawns.
     */
    private fun registerDefaultResourceSpawns() {
        // Forest resources
        registerResourceSpawn(
            ResourceSpawn(
                id = "spawn_forest_common_herb_1",
                locationId = "forest",
                resourceType = ResourceType.HERB,
                itemId = "item_common_herb",
                baseRespawnMinutes = 30,
                quantity = 2,
                seasonalModifier = mapOf(
                    Season.SPRING to 0.7f,  // Faster in spring
                    Season.SUMMER to 1.0f,
                    Season.AUTUMN to 1.2f,
                    Season.WINTER to 2.0f   // Slower in winter
                ),
                spawnChance = 0.9f
            )
        )
        
        registerResourceSpawn(
            ResourceSpawn(
                id = "spawn_forest_rare_mushroom",
                locationId = "forest_mushroom_grove",
                resourceType = ResourceType.MUSHROOM,
                itemId = "item_rare_mushroom",
                baseRespawnMinutes = 120,
                quantity = 1,
                isRare = true,
                timeOfDayModifier = mapOf(
                    TimeOfDay.DAWN to 0.8f,   // Better spawn at dawn/night
                    TimeOfDay.NIGHT to 0.8f,
                    TimeOfDay.AFTERNOON to 1.5f
                ),
                spawnChance = 0.4f
            )
        )
        
        registerResourceSpawn(
            ResourceSpawn(
                id = "spawn_forest_moondew_fern",
                locationId = "forest_fern_tunnel",
                resourceType = ResourceType.HERB,
                itemId = "item_moondew_fern",
                baseRespawnMinutes = 240,  // 4 hours
                quantity = 1,
                isRare = true,
                timeOfDayModifier = mapOf(
                    TimeOfDay.NIGHT to 1.0f,  // Only spawns at night
                    TimeOfDay.DAWN to 3.0f,
                    TimeOfDay.MORNING to Float.POSITIVE_INFINITY,  // Won't spawn
                    TimeOfDay.AFTERNOON to Float.POSITIVE_INFINITY,
                    TimeOfDay.DUSK to 2.0f
                ),
                spawnChance = 0.6f
            )
        )
        
        // Beach resources
        registerResourceSpawn(
            ResourceSpawn(
                id = "spawn_beach_shells",
                locationId = "beach_seashell_grotto",
                resourceType = ResourceType.SHELL,
                itemId = "item_shell",
                baseRespawnMinutes = 45,
                quantity = 3,
                spawnChance = 0.95f
            )
        )
        
        registerResourceSpawn(
            ResourceSpawn(
                id = "spawn_beach_rare_pearl",
                locationId = "beach_tide_pools",
                resourceType = ResourceType.MINERAL,
                itemId = "item_pearl",
                baseRespawnMinutes = 360,  // 6 hours
                quantity = 1,
                isRare = true,
                spawnChance = 0.2f
            )
        )
        
        // Mountain resources
        registerResourceSpawn(
            ResourceSpawn(
                id = "spawn_mountain_crystal",
                locationId = "mountains_crystal_caves",
                resourceType = ResourceType.MINERAL,
                itemId = "item_crystal",
                baseRespawnMinutes = 90,
                quantity = 2,
                seasonalModifier = mapOf(
                    Season.WINTER to 0.8f,  // Crystals grow faster in cold
                    Season.SUMMER to 1.3f
                ),
                spawnChance = 0.7f
            )
        )
        
        registerResourceSpawn(
            ResourceSpawn(
                id = "spawn_mountain_sunsgrace_flower",
                locationId = "mountains_summit",
                resourceType = ResourceType.FLOWER,
                itemId = "item_sunsgrace_flower",
                baseRespawnMinutes = 480,  // 8 hours
                quantity = 1,
                isRare = true,
                timeOfDayModifier = mapOf(
                    TimeOfDay.DAWN to 1.0f,  // Only spawns at dawn
                    TimeOfDay.MORNING to 3.0f,
                    TimeOfDay.AFTERNOON to Float.POSITIVE_INFINITY,
                    TimeOfDay.DUSK to Float.POSITIVE_INFINITY,
                    TimeOfDay.NIGHT to Float.POSITIVE_INFINITY
                ),
                spawnChance = 0.5f
            )
        )
        
        // Swamp resources
        registerResourceSpawn(
            ResourceSpawn(
                id = "spawn_swamp_poison_moss",
                locationId = "swamp_poison_mist_valley",
                resourceType = ResourceType.HERB,
                itemId = "item_poison_moss",
                baseRespawnMinutes = 60,
                quantity = 2,
                seasonalModifier = mapOf(
                    Season.SUMMER to 0.8f,  // Thrives in heat
                    Season.WINTER to 1.5f
                ),
                spawnChance = 0.8f
            )
        )
        
        // Ruins resources
        registerResourceSpawn(
            ResourceSpawn(
                id = "spawn_ruins_ancient_artifact",
                locationId = "ruins_treasury_vault",
                resourceType = ResourceType.MINERAL,
                itemId = "item_ancient_gear",
                baseRespawnMinutes = 720,  // 12 hours
                quantity = 1,
                isRare = true,
                spawnChance = 0.15f
            )
        )
    }
}
