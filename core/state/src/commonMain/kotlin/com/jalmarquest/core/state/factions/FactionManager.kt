package com.jalmarquest.core.state.factions

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * Manages faction relationships and reputation.
 */

@Serializable
data class Faction(
    val id: String,
    val name: String,
    val description: String
)

@Serializable
data class FactionReputation(
    val factionId: String,
    val reputation: Int = 0 // Range: -100 (hostile) to 100 (exalted)
) {
    fun getStanding(): FactionStanding {
        return when {
            reputation >= 75 -> FactionStanding.EXALTED
            reputation >= 50 -> FactionStanding.REVERED
            reputation >= 25 -> FactionStanding.FRIENDLY
            reputation >= 0 -> FactionStanding.NEUTRAL
            reputation >= -25 -> FactionStanding.UNFRIENDLY
            reputation >= -50 -> FactionStanding.HOSTILE
            else -> FactionStanding.HATED
        }
    }
}

@Serializable
enum class FactionStanding {
    HATED,
    HOSTILE,
    UNFRIENDLY,
    NEUTRAL,
    FRIENDLY,
    REVERED,
    EXALTED
}

class FactionManager {
    private val factions = mutableMapOf<String, Faction>()
    private val _reputations = MutableStateFlow<Map<String, FactionReputation>>(emptyMap())
    val reputations: StateFlow<Map<String, FactionReputation>> = _reputations.asStateFlow()
    
    init {
        registerDefaultFactions()
    }
    
    fun registerFaction(faction: Faction) {
        factions[faction.id] = faction
    }
    
    fun getFactionById(id: String): Faction? = factions[id]
    
    fun getReputation(factionId: String): FactionReputation {
        return _reputations.value[factionId] ?: FactionReputation(factionId, 0)
    }
    
    fun modifyReputation(factionId: String, amount: Int) {
        val current = getReputation(factionId)
        val newReputation = (current.reputation + amount).coerceIn(-100, 100)
        
        _reputations.value = _reputations.value + (factionId to FactionReputation(factionId, newReputation))
    }
    
    fun setReputation(factionId: String, amount: Int) {
        val clamped = amount.coerceIn(-100, 100)
        _reputations.value = _reputations.value + (factionId to FactionReputation(factionId, clamped))
    }
    
    private fun registerDefaultFactions() {
        registerFaction(Faction(
            id = "faction_buttonburgh",
            name = "Buttonburgh",
            description = "The quail settlement where you call home."
        ))
        
        registerFaction(Faction(
            id = "faction_ant_colony",
            name = "The Ant Colony",
            description = "An organized colony of industrious ants."
        ))
        
        registerFaction(Faction(
            id = "faction_insects",
            name = "Insect Kingdom",
            description = "The broader alliance of insect species."
        ))
    }
}
