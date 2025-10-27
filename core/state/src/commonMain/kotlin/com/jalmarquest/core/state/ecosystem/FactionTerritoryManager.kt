package com.jalmarquest.core.state.ecosystem

import com.jalmarquest.core.state.catalogs.LocationCatalog
import com.jalmarquest.core.state.factions.FactionManager
import com.jalmarquest.core.state.factions.FactionStanding
import com.jalmarquest.core.state.time.InGameTimeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * Phase 3: Ecosystem Simulation - Faction Territories.
 * Manages faction control zones, territory disputes, and dynamic influence.
 */

/**
 * A territory controlled by a faction.
 */
@Serializable
data class FactionTerritory(
    val id: String,
    val name: String,
    val factionId: String,
    val locationIds: List<String>,  // All locations in this territory
    val capitalLocationId: String,   // Main location (stronghold)
    val influenceStrength: Int = 50, // 0-100, how strongly faction controls it
    val isContested: Boolean = false,
    val contestingFactionId: String? = null,
    val description: String? = null,
    val specialRules: List<TerritoryRule> = emptyList()
)

/**
 * Special rules that apply in a territory.
 */
enum class TerritoryRule {
    SAFE_ZONE,          // No combat allowed
    TRADE_BONUS,        // Better prices for faction members
    EXPERIENCE_BONUS,   // Bonus XP for faction members
    RESTRICTED_ACCESS,  // Must have minimum reputation to enter
    HOSTILE_PATROLS,    // Enemy patrols if reputation too low
    FACTION_QUESTS_ONLY,// Only faction quests available
    NO_ENEMIES,         // Enemies don't spawn here
    TRIBUTE_REQUIRED    // Must pay seeds to enter
}

/**
 * Requirements to access a territory.
 */
@Serializable
data class TerritoryAccess(
    val territoryId: String,
    val minimumStanding: FactionStanding = FactionStanding.NEUTRAL,
    val minimumReputation: Int = 0,
    val tributeAmount: Int? = null,  // Seeds required to enter
    val unlockQuestId: String? = null // Quest that unlocks access
)

/**
 * A dispute between factions over territory.
 */
@Serializable
data class TerritoryDispute(
    val id: String,
    val territoryId: String,
    val attackingFactionId: String,
    val defendingFactionId: String,
    val startTime: Long,
    val attackerStrength: Int = 50,  // 0-100
    val defenderStrength: Int = 50,  // 0-100
    val playerSupportedFaction: String? = null,  // Which side player supports
    val resolution: DisputeResolution? = null
)

/**
 * Outcome of a territory dispute.
 */
enum class DisputeResolution {
    ATTACKER_VICTORY,    // Attacker takes territory
    DEFENDER_VICTORY,    // Defender keeps territory
    STALEMATE,           // Territory becomes neutral
    PLAYER_MEDIATED      // Player brokered peace
}

/**
 * Influence a faction has at a location.
 */
@Serializable
data class LocationInfluence(
    val locationId: String,
    val influences: Map<String, Int> = emptyMap()  // factionId -> influence (0-100)
) {
    /**
     * Get the faction with the most influence at this location.
     */
    fun getDominantFaction(): String? {
        return influences.maxByOrNull { it.value }?.key
    }
    
    /**
     * Check if a faction has majority control (>50%).
     */
    fun hasMajorityControl(factionId: String): Boolean {
        return (influences[factionId] ?: 0) > 50
    }
}

/**
 * Manager for faction territories and territorial control.
 */
class FactionTerritoryManager(
    private val locationCatalog: LocationCatalog,
    private val factionManager: FactionManager,
    private val timeManager: InGameTimeManager,
    private val timestampProvider: () -> Long
) {
    private val _territories = MutableStateFlow<Map<String, FactionTerritory>>(emptyMap())
    val territories: StateFlow<Map<String, FactionTerritory>> = _territories.asStateFlow()
    
    private val _locationInfluences = MutableStateFlow<Map<String, LocationInfluence>>(emptyMap())
    val locationInfluences: StateFlow<Map<String, LocationInfluence>> = _locationInfluences.asStateFlow()
    
    private val _activeDisputes = MutableStateFlow<List<TerritoryDispute>>(emptyList())
    val activeDisputes: StateFlow<List<TerritoryDispute>> = _activeDisputes.asStateFlow()
    
    private val territoryAccessRules = mutableMapOf<String, TerritoryAccess>()
    
    init {
        registerDefaultTerritories()
        initializeLocationInfluences()
    }
    
    /**
     * Register a faction territory.
     */
    fun registerTerritory(territory: FactionTerritory) {
        _territories.value = _territories.value + (territory.id to territory)
        
        // Update location influences
        territory.locationIds.forEach { locationId ->
            modifyInfluence(locationId, territory.factionId, territory.influenceStrength)
        }
    }
    
    /**
     * Register territory access rules.
     */
    fun registerTerritoryAccess(access: TerritoryAccess) {
        territoryAccessRules[access.territoryId] = access
    }
    
    /**
     * Initialize influences for all locations.
     */
    private fun initializeLocationInfluences() {
        val allLocations = locationCatalog.getAllLocations()
        val influences = allLocations.associate { location ->
            location.id to LocationInfluence(location.id)
        }
        _locationInfluences.value = influences
    }
    
    /**
     * Get territory by location ID.
     */
    fun getTerritoryAtLocation(locationId: String): FactionTerritory? {
        return _territories.value.values.firstOrNull { 
            it.locationIds.contains(locationId) 
        }
    }
    
    /**
     * Get all territories controlled by a faction.
     */
    fun getFactionTerritories(factionId: String): List<FactionTerritory> {
        return _territories.value.values.filter { it.factionId == factionId }
    }
    
    /**
     * Check if player can access a territory.
     */
    fun canAccessTerritory(territoryId: String): Pair<Boolean, String?> {
        val territory = _territories.value[territoryId] ?: return true to null
        val access = territoryAccessRules[territoryId]
        
        if (access == null) {
            return true to null  // No restrictions
        }
        
        // Check reputation
        val reputation = factionManager.getReputation(territory.factionId)
        if (reputation.reputation < access.minimumReputation) {
            return false to "You need at least ${access.minimumReputation} reputation with ${territory.factionId}"
        }
        
        // Check standing
        if (reputation.getStanding() < access.minimumStanding) {
            return false to "You must be ${access.minimumStanding} with ${territory.factionId} to enter"
        }
        
        // Check tribute (would need to check player seeds - simplified)
        if (access.tributeAmount != null) {
            return true to "Entry costs ${access.tributeAmount} seeds"
        }
        
        return true to null
    }
    
    /**
     * Modify faction influence at a location.
     */
    fun modifyInfluence(locationId: String, factionId: String, change: Int) {
        val current = _locationInfluences.value[locationId] ?: LocationInfluence(locationId)
        val currentInfluence = current.influences[factionId] ?: 0
        val newInfluence = (currentInfluence + change).coerceIn(0, 100)
        
        val newInfluences = current.influences.toMutableMap()
        newInfluences[factionId] = newInfluence
        
        // Normalize so total doesn't exceed 100
        val total = newInfluences.values.sum()
        if (total > 100) {
            val scale = 100.0f / total
            newInfluences.clear()
            current.influences.forEach { (factionId, value) ->
                newInfluences[factionId] = (value * scale).toInt()
            }
        }
        
        _locationInfluences.value = _locationInfluences.value + (locationId to current.copy(
            influences = newInfluences
        ))
    }
    
    /**
     * Start a territory dispute.
     */
    fun startDispute(
        territoryId: String,
        attackingFactionId: String
    ): TerritoryDispute? {
        val territory = _territories.value[territoryId] ?: return null
        
        val dispute = TerritoryDispute(
            id = "dispute_${timestampProvider()}",
            territoryId = territoryId,
            attackingFactionId = attackingFactionId,
            defendingFactionId = territory.factionId,
            startTime = timestampProvider(),
            attackerStrength = 50,
            defenderStrength = territory.influenceStrength
        )
        
        _activeDisputes.value = _activeDisputes.value + dispute
        
        // Mark territory as contested
        _territories.value = _territories.value + (territoryId to territory.copy(
            isContested = true,
            contestingFactionId = attackingFactionId
        ))
        
        return dispute
    }
    
    /**
     * Player supports a faction in a dispute.
     */
    fun supportFactionInDispute(disputeId: String, factionId: String, support: Int) {
        _activeDisputes.value = _activeDisputes.value.map { dispute ->
            if (dispute.id == disputeId) {
                val newDispute = dispute.copy(
                    playerSupportedFaction = factionId,
                    attackerStrength = if (factionId == dispute.attackingFactionId) {
                        (dispute.attackerStrength + support).coerceIn(0, 100)
                    } else {
                        dispute.attackerStrength
                    },
                    defenderStrength = if (factionId == dispute.defendingFactionId) {
                        (dispute.defenderStrength + support).coerceIn(0, 100)
                    } else {
                        dispute.defenderStrength
                    }
                )
                newDispute
            } else {
                dispute
            }
        }
    }
    
    /**
     * Resolve a territory dispute.
     */
    fun resolveDispute(disputeId: String) {
        val dispute = _activeDisputes.value.firstOrNull { it.id == disputeId } ?: return
        val territory = _territories.value[dispute.territoryId] ?: return
        
        // Determine winner
        val resolution = when {
            dispute.attackerStrength > dispute.defenderStrength + 20 -> DisputeResolution.ATTACKER_VICTORY
            dispute.defenderStrength > dispute.attackerStrength + 20 -> DisputeResolution.DEFENDER_VICTORY
            dispute.playerSupportedFaction != null -> DisputeResolution.PLAYER_MEDIATED
            else -> DisputeResolution.STALEMATE
        }
        
        // Update dispute
        _activeDisputes.value = _activeDisputes.value.map { 
            if (it.id == disputeId) it.copy(resolution = resolution) else it
        }
        
        // Apply resolution
        when (resolution) {
            DisputeResolution.ATTACKER_VICTORY -> {
                // Transfer territory to attacker
                _territories.value = _territories.value + (dispute.territoryId to territory.copy(
                    factionId = dispute.attackingFactionId,
                    isContested = false,
                    contestingFactionId = null,
                    influenceStrength = 60  // New conqueror has moderate control
                ))
                
                // Update location influences
                territory.locationIds.forEach { locationId ->
                    modifyInfluence(locationId, dispute.attackingFactionId, 40)
                    modifyInfluence(locationId, dispute.defendingFactionId, -40)
                }
            }
            DisputeResolution.DEFENDER_VICTORY -> {
                // Territory stays with defender, influence strengthened
                _territories.value = _territories.value + (dispute.territoryId to territory.copy(
                    isContested = false,
                    contestingFactionId = null,
                    influenceStrength = (territory.influenceStrength + 10).coerceIn(0, 100)
                ))
            }
            DisputeResolution.STALEMATE -> {
                // Territory becomes neutral/contested
                _territories.value = _territories.value + (dispute.territoryId to territory.copy(
                    isContested = true,
                    influenceStrength = (territory.influenceStrength - 10).coerceIn(0, 100)
                ))
            }
            DisputeResolution.PLAYER_MEDIATED -> {
                // Peace brokered, both factions share influence
                _territories.value = _territories.value + (dispute.territoryId to territory.copy(
                    isContested = false,
                    contestingFactionId = null
                ))
                
                territory.locationIds.forEach { locationId ->
                    modifyInfluence(locationId, dispute.attackingFactionId, 25)
                    modifyInfluence(locationId, dispute.defendingFactionId, 25)
                }
            }
        }
        
        // Remove from active disputes
        _activeDisputes.value = _activeDisputes.value.filter { it.id != disputeId }
    }
    
    /**
     * Get the benefits of being in friendly territory.
     */
    fun getTerritoryBenefits(locationId: String): List<TerritoryRule> {
        val territory = getTerritoryAtLocation(locationId) ?: return emptyList()
        val reputation = factionManager.getReputation(territory.factionId)
        
        // Only get benefits if friendly with the faction
        return if (reputation.getStanding() >= FactionStanding.FRIENDLY) {
            territory.specialRules
        } else {
            emptyList()
        }
    }
    
    /**
     * Register default territories.
     */
    private fun registerDefaultTerritories() {
        // Buttonburgh territory (safe zone)
        registerTerritory(
            FactionTerritory(
                id = "territory_buttonburgh",
                name = "Buttonburgh",
                factionId = "faction_buttonburgh",
                locationIds = listOf(
                    "buttonburgh_centre",
                    "buttonburgh_market_square",
                    "buttonburgh_roost",
                    "buttonburgh_scholars_district",
                    "buttonburgh_artisan_quarter",
                    "buttonburgh_training_grounds",
                    "buttonburgh_garden_terraces",
                    "buttonburgh_message_post",
                    "buttonburgh_nursery",
                    "buttonburgh_town_hall",
                    "buttonburgh_tavern"
                ),
                capitalLocationId = "buttonburgh_town_hall",
                influenceStrength = 100,
                description = "The heart of quail civilization. A safe haven for all citizens.",
                specialRules = listOf(
                    TerritoryRule.SAFE_ZONE,
                    TerritoryRule.TRADE_BONUS,
                    TerritoryRule.NO_ENEMIES
                )
            )
        )
        
        // Ant Colony territory
        registerTerritory(
            FactionTerritory(
                id = "territory_ant_colony",
                name = "Ant Colony Territory",
                factionId = "faction_ant_colony",
                locationIds = listOf(
                    "forest_ant_hill"
                ),
                capitalLocationId = "forest_ant_hill",
                influenceStrength = 90,
                description = "The domain of the organized ant colony.",
                specialRules = listOf(
                    TerritoryRule.FACTION_QUESTS_ONLY,
                    TerritoryRule.RESTRICTED_ACCESS
                )
            )
        )
        
        registerTerritoryAccess(
            TerritoryAccess(
                territoryId = "territory_ant_colony",
                minimumStanding = FactionStanding.NEUTRAL,
                minimumReputation = 0
            )
        )
        
        // Insect Kingdom territory (broader forest area)
        registerTerritory(
            FactionTerritory(
                id = "territory_insect_kingdom",
                name = "Insect Kingdom",
                factionId = "faction_insects",
                locationIds = listOf(
                    "forest",
                    "forest_whispering_pines",
                    "forest_mushroom_grove",
                    "forest_babbling_brook"
                ),
                capitalLocationId = "forest",
                influenceStrength = 60,
                isContested = true,
                description = "Forest areas claimed by various insect factions.",
                specialRules = listOf(
                    TerritoryRule.HOSTILE_PATROLS
                )
            )
        )
        
        registerTerritoryAccess(
            TerritoryAccess(
                territoryId = "territory_insect_kingdom",
                minimumStanding = FactionStanding.UNFRIENDLY,  // Can enter but risky
                minimumReputation = -25
            )
        )
    }
}
