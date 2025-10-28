package com.jalmarquest.core.state.faction

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

class FactionManager(
    private val gameStateManager: GameStateManager
) {
    private val mutex = Mutex()
    private val _state = MutableStateFlow(FactionState())
    val state: StateFlow<FactionState> = _state.asStateFlow()
    
    @Serializable
    data class FactionState(
        val reputations: Map<FactionId, FactionReputation> = getDefaultReputations(),
        val unlockedFactions: Set<FactionId> = setOf(
            FactionId("buttonburgh_citizens"),
            FactionId("woodland_creatures")
        ),
        val factionRelationships: Map<Pair<FactionId, FactionId>, RelationshipType> = getDefaultRelationships()
    )
    
    @Serializable
    data class FactionReputation(
        val factionId: FactionId,
        val currentReputation: Int = 0, // -100 to 100
        val rank: ReputationRank = ReputationRank.NEUTRAL,
        val lifetimeEarned: Int = 0,
        val lifetimeLost: Int = 0,
        val lastChangeReason: String? = null
    )
    
    @Serializable
    enum class ReputationRank(val minReputation: Int, val displayName: String) {
        HOSTILE(-100, "Hostile"),
        UNFRIENDLY(-50, "Unfriendly"),
        NEUTRAL(-20, "Neutral"),
        FRIENDLY(20, "Friendly"),
        HONORED(50, "Honored"),
        REVERED(80, "Revered"),
        EXALTED(100, "Exalted")
    }
    
    @Serializable
    enum class RelationshipType {
        ALLIED,     // Reputation gains are shared 50%
        FRIENDLY,   // Reputation gains are shared 25%
        NEUTRAL,    // No interaction
        RIVAL,      // Reputation gains cause 25% loss in other
        HOSTILE     // Reputation gains cause 50% loss in other
    }
    
    @Serializable
    @JvmInline
    value class FactionId(val value: String)
    
    suspend fun modifyReputation(
        factionId: FactionId,
        amount: Int,
        reason: String,
        applyRelationships: Boolean = true
    ) = mutex.withLock {
        if (factionId !in _state.value.unlockedFactions) return@withLock
        
        val currentRep = _state.value.reputations[factionId]
            ?: FactionReputation(factionId = factionId)
        
        val newReputation = (currentRep.currentReputation + amount).coerceIn(-100, 100)
        val newRank = calculateRank(newReputation)
        
        val updatedRep = currentRep.copy(
            currentReputation = newReputation,
            rank = newRank,
            lifetimeEarned = if (amount > 0) currentRep.lifetimeEarned + amount else currentRep.lifetimeEarned,
            lifetimeLost = if (amount < 0) currentRep.lifetimeLost - amount else currentRep.lifetimeLost,
            lastChangeReason = reason
        )
        
        var updatedReputations = _state.value.reputations + (factionId to updatedRep)
        
        // Apply relationship effects
        if (applyRelationships) {
            _state.value.factionRelationships.forEach { (factionPair, relationship) ->
                when {
                    factionPair.first == factionId -> {
                        val relatedAmount = calculateRelatedChange(amount, relationship)
                        if (relatedAmount != 0) {
                            val relatedRep = updatedReputations[factionPair.second]
                                ?: FactionReputation(factionId = factionPair.second)
                            
                            updatedReputations = updatedReputations + (
                                factionPair.second to relatedRep.copy(
                                    currentReputation = (relatedRep.currentReputation + relatedAmount).coerceIn(-100, 100),
                                    rank = calculateRank((relatedRep.currentReputation + relatedAmount).coerceIn(-100, 100)),
                                    lastChangeReason = "Related to: $reason"
                                )
                            )
                        }
                    }
                }
            }
        }
        
        _state.value = _state.value.copy(reputations = updatedReputations)
        
        // Log choice for butterfly effect
        gameStateManager.appendChoice("faction_${factionId.value}_${if (amount > 0) "gain" else "loss"}_${kotlin.math.abs(amount)}")
    }
    
    suspend fun evaluateChoiceReputation(choiceTag: String) = mutex.withLock {
        // Parse choice tags for faction implications
        val factionEffects = parseFactionEffects(choiceTag)
        
        factionEffects.forEach { (factionId, amount) ->
            modifyReputation(
                factionId = factionId,
                amount = amount,
                reason = "Choice: $choiceTag",
                applyRelationships = true
            )
        }
    }
    
    suspend fun unlockFaction(factionId: FactionId) = mutex.withLock {
        if (factionId in _state.value.unlockedFactions) return@withLock
        
        _state.value = _state.value.copy(
            unlockedFactions = _state.value.unlockedFactions + factionId,
            reputations = _state.value.reputations + (
                factionId to FactionReputation(factionId = factionId)
            )
        )
        
        gameStateManager.appendChoice("faction_unlocked_${factionId.value}")
    }
    
    fun canAccessFactionContent(factionId: FactionId, requiredRank: ReputationRank): Boolean {
        val reputation = _state.value.reputations[factionId] ?: return false
        return reputation.rank.minReputation >= requiredRank.minReputation
    }
    
    fun getFactionDialogueVariant(npcFaction: FactionId?): DialogueVariant {
        if (npcFaction == null) return DialogueVariant.NEUTRAL
        
        val reputation = _state.value.reputations[npcFaction] ?: return DialogueVariant.NEUTRAL
        
        return when (reputation.rank) {
            ReputationRank.HOSTILE, ReputationRank.UNFRIENDLY -> DialogueVariant.HOSTILE
            ReputationRank.NEUTRAL -> DialogueVariant.NEUTRAL
            ReputationRank.FRIENDLY, ReputationRank.HONORED -> DialogueVariant.FRIENDLY
            ReputationRank.REVERED, ReputationRank.EXALTED -> DialogueVariant.REVERENT
        }
    }
    
    private fun calculateRank(reputation: Int): ReputationRank {
        return ReputationRank.entries
            .sortedByDescending { it.minReputation }
            .first { reputation >= it.minReputation }
    }
    
    private fun calculateRelatedChange(amount: Int, relationship: RelationshipType): Int {
        return when (relationship) {
            RelationshipType.ALLIED -> (amount * 0.5f).toInt()
            RelationshipType.FRIENDLY -> (amount * 0.25f).toInt()
            RelationshipType.NEUTRAL -> 0
            RelationshipType.RIVAL -> (-amount * 0.25f).toInt()
            RelationshipType.HOSTILE -> (-amount * 0.5f).toInt()
        }
    }
    
    private fun parseFactionEffects(choiceTag: String): Map<FactionId, Int> {
        // Parse choice tags for faction reputation changes
        // Format: "faction_buttonburgh_+10" or "helped_merchant" -> implies faction gain
        
        val effects = mutableMapOf<FactionId, Int>()
        
        when {
            choiceTag.startsWith("faction_") -> {
                // Direct faction tag
                val parts = choiceTag.split("_")
                if (parts.size >= 3) {
                    val factionId = FactionId(parts[1])
                    val amount = parts[2].toIntOrNull() ?: 0
                    effects[factionId] = amount
                }
            }
            choiceTag.contains("helped") || choiceTag.contains("saved") -> {
                // Helping actions generally improve reputation
                effects[FactionId("buttonburgh_citizens")] = 5
            }
            choiceTag.contains("stole") || choiceTag.contains("attacked") -> {
                // Hostile actions reduce reputation
                effects[FactionId("buttonburgh_citizens")] = -10
            }
            choiceTag.contains("merchant") -> {
                effects[FactionId("merchant_guild")] = 5
            }
            choiceTag.contains("scholar") -> {
                effects[FactionId("scholar_society")] = 5
            }
        }
        
        return effects
    }
    
    enum class DialogueVariant {
        HOSTILE,
        NEUTRAL,
        FRIENDLY,
        REVERENT
    }
    
    companion object {
        fun getDefaultReputations(): Map<FactionId, FactionReputation> {
            return mapOf(
                FactionId("buttonburgh_citizens") to FactionReputation(
                    factionId = FactionId("buttonburgh_citizens"),
                    currentReputation = 0,
                    rank = ReputationRank.NEUTRAL
                ),
                FactionId("woodland_creatures") to FactionReputation(
                    factionId = FactionId("woodland_creatures"),
                    currentReputation = 0,
                    rank = ReputationRank.NEUTRAL
                )
            )
        }
        
        fun getDefaultRelationships(): Map<Pair<FactionId, FactionId>, RelationshipType> {
            return mapOf(
                (FactionId("buttonburgh_citizens") to FactionId("merchant_guild")) 
                    to RelationshipType.ALLIED,
                (FactionId("woodland_creatures") to FactionId("predator_pack")) 
                    to RelationshipType.HOSTILE,
                (FactionId("scholar_society") to FactionId("mystic_order")) 
                    to RelationshipType.RIVAL
            )
        }
    }
}
