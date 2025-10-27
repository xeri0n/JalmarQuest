package com.jalmarquest.core.state.optimization

import kotlinx.serialization.Serializable

/**
 * Spatial Partitioning System for Phase 6 Performance Optimization
 * 
 * Purpose: Optimize world updates by tracking entity proximity to player
 * and adjusting update frequencies based on distance.
 * 
 * Key Features:
 * - Grid-based spatial partitioning (simpler than quadtree for small worlds)
 * - Priority zones: IMMEDIATE (current location), NEAR (1-2 hops), FAR (3+ hops), INACTIVE (different region)
 * - Dynamic update frequency scaling based on proximity
 * - Entity tracking for NPCs, enemies, and resources
 */

/**
 * Priority zones for spatial updates
 */
enum class UpdatePriority {
    IMMEDIATE,  // Same location as player - update every cycle
    NEAR,       // Adjacent locations - update every 3-5 cycles  
    FAR,        // 2-3 locations away - update every 10-20 cycles
    INACTIVE    // Different region or 4+ locations - update every 60+ cycles or pause
}

/**
 * Spatial entity types
 */
enum class EntityType {
    NPC,
    ENEMY,
    RESOURCE,
    PATROL
}

/**
 * Entity tracked in spatial grid
 */
@Serializable
data class SpatialEntity(
    val id: String,
    val type: EntityType,
    val locationId: String,
    var priority: UpdatePriority = UpdatePriority.INACTIVE,
    var lastUpdateTime: Long = 0L,
    var updateCycleCounter: Int = 0
)

/**
 * Manages spatial partitioning and priority-based updates
 * 
 * Algorithm:
 * 1. Track player location changes
 * 2. When player moves, recalculate entity priorities based on distance
 * 3. Each update cycle, only process entities whose priority allows updates
 * 4. Use cycle counters to skip low-priority entities
 * 
 * Performance Impact:
 * - BEFORE: All 46 NPCs + 34 enemies + 10 resources = 90 entities updated every 5min
 * - AFTER: ~5-10 entities updated per cycle (90% reduction when player stays in one area)
 */
class SpatialPartitioningSystem(
    private val locationCatalog: com.jalmarquest.core.state.catalogs.LocationCatalog,
    private val timestampProvider: () -> Long
) {
    private val entities = mutableMapOf<String, SpatialEntity>()
    private var currentPlayerLocation: String? = null
    private var updateCycle: Int = 0
    
    /**
     * Register an entity for spatial tracking
     */
    fun registerEntity(id: String, type: EntityType, locationId: String) {
        val entity = SpatialEntity(
            id = id,
            type = type,
            locationId = locationId,
            priority = UpdatePriority.INACTIVE,
            lastUpdateTime = timestampProvider()
        )
        entities[id] = entity
        
        // Recalculate priority if player location is known
        currentPlayerLocation?.let {
            entity.priority = calculatePriority(it, locationId)
        }
    }
    
    /**
     * Update entity location (when entity moves)
     */
    fun updateEntityLocation(entityId: String, newLocationId: String) {
        val entity = entities[entityId] ?: return
        entities[entityId] = entity.copy(locationId = newLocationId)
        
        // Recalculate priority
        currentPlayerLocation?.let {
            entities[entityId] = entities[entityId]!!.copy(
                priority = calculatePriority(it, newLocationId)
            )
        }
    }
    
    /**
     * Update player location and recalculate all entity priorities
     */
    fun updatePlayerLocation(newLocationId: String) {
        if (currentPlayerLocation == newLocationId) return
        
        currentPlayerLocation = newLocationId
        
        // Recalculate all entity priorities
        entities.values.forEach { entity ->
            entities[entity.id] = entity.copy(
                priority = calculatePriority(newLocationId, entity.locationId)
            )
        }
    }
    
    /**
     * Get entities that should be updated this cycle
     * 
     * Returns entities filtered by:
     * 1. Priority level allows update this cycle
     * 2. Cycle counter check (for throttling low-priority entities)
     */
    fun getEntitiesToUpdate(): List<SpatialEntity> {
        updateCycle++
        
        return entities.values.filter { entity ->
            when (entity.priority) {
                UpdatePriority.IMMEDIATE -> true  // Always update
                UpdatePriority.NEAR -> updateCycle % 3 == 0  // Every 3rd cycle
                UpdatePriority.FAR -> updateCycle % 10 == 0  // Every 10th cycle
                UpdatePriority.INACTIVE -> updateCycle % 60 == 0  // Every 60th cycle or skip
            }
        }.also { entitiesToUpdate ->
            // Update last update time for processed entities
            val now = timestampProvider()
            entitiesToUpdate.forEach { entity ->
                entity.lastUpdateTime = now
                entity.updateCycleCounter++
            }
        }
    }
    
    /**
     * Get entities by priority level
     */
    fun getEntitiesByPriority(priority: UpdatePriority): List<SpatialEntity> {
        return entities.values.filter { it.priority == priority }
    }
    
    /**
     * Get entity count statistics
     */
    fun getStatistics(): SpatialStatistics {
        val byCounted = entities.values.groupBy { it.priority }
        return SpatialStatistics(
            totalEntities = entities.size,
            immediateCount = byCounted[UpdatePriority.IMMEDIATE]?.size ?: 0,
            nearCount = byCounted[UpdatePriority.NEAR]?.size ?: 0,
            farCount = byCounted[UpdatePriority.FAR]?.size ?: 0,
            inactiveCount = byCounted[UpdatePriority.INACTIVE]?.size ?: 0,
            currentCycle = updateCycle
        )
    }
    
    /**
     * Calculate priority based on distance between locations
     * 
     * Distance calculation:
     * - Same location: IMMEDIATE
     * - Adjacent location (1-2 hops): NEAR  
     * - 2-3 hops: FAR
     * - Different parent or 4+ hops: INACTIVE
     */
    private fun calculatePriority(playerLocationId: String, entityLocationId: String): UpdatePriority {
        if (playerLocationId == entityLocationId) {
            return UpdatePriority.IMMEDIATE
        }
        
        // Get parent location info (simplified region check)
        val playerParent = locationCatalog.getLocationById(playerLocationId)?.parentLocationId
        val entityParent = locationCatalog.getLocationById(entityLocationId)?.parentLocationId
        
        // Different parent locations = INACTIVE
        if (playerParent != entityParent && playerParent != null && entityParent != null) {
            return UpdatePriority.INACTIVE
        }
        
        // Calculate hops (simplified distance - use location name similarity as proxy)
        // In production, this would use actual graph distance calculation
        val distance = calculateLocationDistance(playerLocationId, entityLocationId)
        
        return when {
            distance <= 1 -> UpdatePriority.NEAR
            distance <= 3 -> UpdatePriority.FAR
            else -> UpdatePriority.INACTIVE
        }
    }
    
    /**
     * Calculate simple distance between locations
     * Returns hop count (simplified implementation)
     */
    private fun calculateLocationDistance(loc1: String, loc2: String): Int {
        // Simplified distance calculation based on location name structure
        // e.g., "forest_clearing" vs "forest_grove" = 1 hop
        // e.g., "forest_clearing" vs "swamp_edge" = 5 hops
        
        val parts1 = loc1.split("_")
        val parts2 = loc2.split("_")
        
        // Same parent region part
        if (parts1.firstOrNull() == parts2.firstOrNull()) {
            // Same sub-location
            if (parts1.getOrNull(1) == parts2.getOrNull(1)) {
                return 1
            }
            return 2
        }
        
        // Different regions
        return 5
    }
}

/**
 * Statistics for spatial partitioning performance monitoring
 */
data class SpatialStatistics(
    val totalEntities: Int,
    val immediateCount: Int,
    val nearCount: Int,
    val farCount: Int,
    val inactiveCount: Int,
    val currentCycle: Int
) {
    val activeCount: Int get() = immediateCount + nearCount + farCount
    val reductionPercentage: Double get() = 
        if (totalEntities > 0) (inactiveCount.toDouble() / totalEntities) * 100.0 else 0.0
}
