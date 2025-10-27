package com.jalmarquest.core.state.companions

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * Manages companion relationships and abilities.
 */

@Serializable
data class Companion(
    val id: String,
    val name: String,
    val description: String,
    val affinityLevel: Int = 0, // 0-100
    val abilities: List<String> = emptyList(),
    val giftPreferences: Map<String, Int> = emptyMap() // itemId -> affinity gain
)

@Serializable
data class CompanionProgress(
    val companionId: String,
    val affinity: Int = 0,
    val isActive: Boolean = false,
    val unlockedAbilities: List<String> = emptyList()
)

class CompanionManager {
    private val companions = mutableMapOf<String, Companion>()
    private val _companionProgress = MutableStateFlow<Map<String, CompanionProgress>>(emptyMap())
    val companionProgress: StateFlow<Map<String, CompanionProgress>> = _companionProgress.asStateFlow()
    
    private val _activeCompanion = MutableStateFlow<String?>(null)
    val activeCompanion: StateFlow<String?> = _activeCompanion.asStateFlow()
    
    init {
        registerDefaultCompanions()
    }
    
    fun registerCompanion(companion: Companion) {
        companions[companion.id] = companion
    }
    
    fun getCompanionById(id: String): Companion? = companions[id]
    
    fun unlockCompanion(companionId: String) {
        if (_companionProgress.value.containsKey(companionId)) return
        
        _companionProgress.value = _companionProgress.value + (companionId to CompanionProgress(
            companionId = companionId,
            affinity = 0
        ))
    }
    
    fun setActiveCompanion(companionId: String?) {
        // Deactivate current companion
        _companionProgress.value = _companionProgress.value.mapValues { (_, progress) ->
            progress.copy(isActive = false)
        }
        
        // Activate new companion
        if (companionId != null) {
            _companionProgress.value = _companionProgress.value.mapValues { (id, progress) ->
                if (id == companionId) {
                    progress.copy(isActive = true)
                } else {
                    progress
                }
            }
            _activeCompanion.value = companionId
        } else {
            _activeCompanion.value = null
        }
    }
    
    fun giveGift(companionId: String, itemId: String) {
        val companion = companions[companionId] ?: return
        val affinityGain = companion.giftPreferences[itemId] ?: 5 // Default small gain
        
        modifyAffinity(companionId, affinityGain)
    }
    
    fun modifyAffinity(companionId: String, amount: Int) {
        val current = _companionProgress.value[companionId] ?: return
        val newAffinity = (current.affinity + amount).coerceIn(0, 100)
        
        _companionProgress.value = _companionProgress.value + (companionId to current.copy(
            affinity = newAffinity
        ))
    }
    
    private fun registerDefaultCompanions() {
        // Quest 20: The Feathered Friend
        registerCompanion(Companion(
            id = "npc_chickadee",
            name = "Chickadee",
            description = "A young, energetic quail chick who follows you everywhere.",
            affinityLevel = 0,
            abilities = listOf("ability_companion_forage"),
            giftPreferences = mapOf(
                "acorn" to 10,
                "ingredient_common_herb" to 5
            )
        ))
    }
}
