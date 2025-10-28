package com.jalmarquest.feature.companions

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

class CompanionManager(
    private val gameStateManager: GameStateManager,
    private val companionCatalog: CompanionCatalog
) {
    private val mutex = Mutex()
    private val _state = MutableStateFlow(CompanionState())
    val state: StateFlow<CompanionState> = _state.asStateFlow()
    
    @Serializable
    data class CompanionState(
        val companions: Map<CompanionId, Companion> = emptyMap(),
        val activeCompanion: CompanionId? = null,
        val relationshipLevels: Map<CompanionId, RelationshipLevel> = emptyMap(),
        val giftHistory: Map<CompanionId, List<GiftRecord>> = emptyMap(),
        val passiveIncomeLastCollection: Long = 0L
    )
    
    @Serializable
    data class Companion(
        val id: CompanionId,
        val name: String,
        val species: String,
        val personality: PersonalityType,
        val affinity: Int = 0, // 0-100
        val abilities: List<CompanionAbility> = emptyList(),
        val unlockedAbilities: Set<AbilityId> = emptySet(),
        val favoriteGifts: Set<ItemId> = emptySet(),
        val hatedGifts: Set<ItemId> = emptySet(),
        val currentMood: CompanionMood = CompanionMood.NEUTRAL,
        val joinedAt: Long = System.currentTimeMillis()
    )
    
    @Serializable
    enum class PersonalityType {
        CHEERFUL,    // +happiness from adventures
        CAUTIOUS,    // +defense bonuses
        CURIOUS,     // +discovery bonuses
        LOYAL,       // +affinity gain rate
        MISCHIEVOUS, // +luck bonuses
        WISE        // +experience bonuses
    }
    
    @Serializable
    enum class CompanionMood {
        ECSTATIC,   // >80 affinity, recent gift
        HAPPY,      // >60 affinity
        CONTENT,    // >40 affinity
        NEUTRAL,    // >20 affinity
        UNHAPPY,    // <20 affinity
        ANGRY       // Recent bad gift or neglect
    }
    
    @Serializable
    enum class RelationshipLevel(val requiredAffinity: Int) {
        STRANGER(0),
        ACQUAINTANCE(20),
        FRIEND(40),
        CLOSE_FRIEND(60),
        BEST_FRIEND(80),
        SOULMATE(100)
    }
    
    @Serializable
    data class CompanionAbility(
        val id: AbilityId,
        val name: String,
        val description: String,
        val type: AbilityType,
        val requiredAffinity: Int,
        val effect: AbilityEffect
    )
    
    @Serializable
    enum class AbilityType {
        PASSIVE_INCOME,      // Generates seeds over time
        COMBAT_ASSIST,       // Helps in combat
        FORAGING_BOOST,      // Better resource gathering
        LUCK_ENHANCEMENT,    // Improved RNG
        EXPERIENCE_SHARE,    // Bonus XP
        ITEM_FINDER,        // Chance to find items
        SOCIAL_BUTTERFLY,    // Better NPC relations
        PROTECTIVE          // Damage reduction
    }
    
    @Serializable
    data class AbilityEffect(
        val type: AbilityType,
        val magnitude: Float,
        val description: String
    )
    
    @Serializable
    data class GiftRecord(
        val itemId: ItemId,
        val timestamp: Long,
        val affinityChange: Int,
        val reaction: String
    )
    
    @Serializable
    @JvmInline
    value class CompanionId(val value: String)
    
    @Serializable
    @JvmInline
    value class AbilityId(val value: String)
    
    suspend fun recruitCompanion(companionId: CompanionId, currentTimeMillis: Long) = mutex.withLock {
        val template = companionCatalog.getCompanion(companionId) ?: return@withLock
        
        val companion = Companion(
            id = companionId,
            name = template.name,
            species = template.species,
            personality = template.personality,
            abilities = template.abilities,
            favoriteGifts = template.favoriteGifts,
            hatedGifts = template.hatedGifts,
            joinedAt = currentTimeMillis
        )
        
        val updatedCompanions = _state.value.companions + (companionId to companion)
        
        _state.value = _state.value.copy(
            companions = updatedCompanions,
            activeCompanion = _state.value.activeCompanion ?: companionId
        )
        
        gameStateManager.appendChoice("companion_recruited_${companionId.value}")
    }
    
    suspend fun giveGift(companionId: CompanionId, itemId: ItemId, currentTimeMillis: Long) = mutex.withLock {
        val companion = _state.value.companions[companionId] ?: return@withLock
        
        // Calculate affinity change based on gift preferences
        val affinityChange = when {
            itemId in companion.favoriteGifts -> 10
            itemId in companion.hatedGifts -> -10
            else -> 3
        }
        
        val newAffinity = (companion.affinity + affinityChange).coerceIn(0, 100)
        
        // Generate reaction message
        val reaction = when {
            itemId in companion.favoriteGifts -> "loves the gift and chirps happily!"
            itemId in companion.hatedGifts -> "seems upset by the gift..."
            affinityChange > 0 -> "appreciates the gift."
            else -> "doesn't seem interested."
        }
        
        // Update companion
        val updatedCompanion = companion.copy(
            affinity = newAffinity,
            currentMood = calculateMood(newAffinity, affinityChange > 0)
        )
        
        // Record gift
        val giftRecord = GiftRecord(itemId, currentTimeMillis, affinityChange, reaction)
        val updatedHistory = _state.value.giftHistory.getOrDefault(companionId, emptyList()) + giftRecord
        
        // Check for newly unlocked abilities
        val newlyUnlocked = companion.abilities
            .filter { it.requiredAffinity <= newAffinity }
            .filter { it.id !in companion.unlockedAbilities }
            .map { it.id }
        
        val finalCompanion = if (newlyUnlocked.isNotEmpty()) {
            updatedCompanion.copy(unlockedAbilities = companion.unlockedAbilities + newlyUnlocked)
        } else {
            updatedCompanion
        }
        
        _state.value = _state.value.copy(
            companions = _state.value.companions + (companionId to finalCompanion),
            giftHistory = _state.value.giftHistory + (companionId to updatedHistory),
            relationshipLevels = updateRelationshipLevel(companionId, newAffinity)
        )
        
        gameStateManager.appendChoice("companion_gift_${companionId.value}_${itemId.value}")
    }
    
    suspend fun setActiveCompanion(companionId: CompanionId?) = mutex.withLock {
        if (companionId != null && companionId !in _state.value.companions) return@withLock
        
        _state.value = _state.value.copy(activeCompanion = companionId)
        
        if (companionId != null) {
            gameStateManager.appendChoice("companion_active_${companionId.value}")
        }
    }
    
    suspend fun collectPassiveIncome(currentTimeMillis: Long): Int = mutex.withLock {
        val activeCompanion = _state.value.activeCompanion?.let { 
            _state.value.companions[it] 
        } ?: return@withLock 0
        
        // Check if passive income ability is unlocked
        val incomeAbility = activeCompanion.abilities
            .find { it.type == AbilityType.PASSIVE_INCOME }
            ?.takeIf { it.id in activeCompanion.unlockedAbilities }
            ?: return@withLock 0
        
        // Calculate time since last collection
        val timeSinceLastCollection = currentTimeMillis - _state.value.passiveIncomeLastCollection
        val hours = timeSinceLastCollection / (1000 * 60 * 60)
        
        if (hours < 1) return@withLock 0
        
        // Calculate income based on ability magnitude and affinity
        val baseIncome = (incomeAbility.effect.magnitude * hours).toInt()
        val affinityBonus = (activeCompanion.affinity / 100f) * 0.5f + 1f
        val totalIncome = (baseIncome * affinityBonus).toInt()
        
        _state.value = _state.value.copy(
            passiveIncomeLastCollection = currentTimeMillis
        )
        
        return@withLock totalIncome
    }
    
    fun getActiveCompanionBonuses(): CompanionBonuses {
        val companion = _state.value.activeCompanion?.let { 
            _state.value.companions[it] 
        } ?: return CompanionBonuses()
        
        val bonuses = CompanionBonuses()
        
        companion.abilities
            .filter { it.id in companion.unlockedAbilities }
            .forEach { ability ->
                when (ability.type) {
                    AbilityType.FORAGING_BOOST -> bonuses.foragingBonus = ability.effect.magnitude
                    AbilityType.LUCK_ENHANCEMENT -> bonuses.luckBonus = ability.effect.magnitude
                    AbilityType.EXPERIENCE_SHARE -> bonuses.experienceBonus = ability.effect.magnitude
                    AbilityType.PROTECTIVE -> bonuses.defenseBonus = ability.effect.magnitude
                    AbilityType.SOCIAL_BUTTERFLY -> bonuses.socialBonus = ability.effect.magnitude
                    else -> {} // Other types handled elsewhere
                }
            }
        
        return bonuses
    }
    
    private fun calculateMood(affinity: Int, receivedGoodGift: Boolean): CompanionMood {
        return when {
            affinity > 80 && receivedGoodGift -> CompanionMood.ECSTATIC
            affinity > 60 -> CompanionMood.HAPPY
            affinity > 40 -> CompanionMood.CONTENT
            affinity > 20 -> CompanionMood.NEUTRAL
            else -> CompanionMood.UNHAPPY
        }
    }
    
    private fun updateRelationshipLevel(
        companion