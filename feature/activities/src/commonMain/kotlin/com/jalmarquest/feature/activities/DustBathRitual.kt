package com.jalmarquest.feature.activities

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.random.Random

/**
 * Daily Dust Bath Ritual - A relaxing mini-game where players help Jalmar
 * take a satisfying dust bath by timing their movements correctly.
 * Provides stacking daily buffs for consistent play.
 */
class DustBathRitualManager(
    private val onRewardGranted: suspend (DustBathReward) -> Unit
) {
    private val mutex = Mutex()
    private val _state = MutableStateFlow(DustBathState())
    val state: StateFlow<DustBathState> = _state.asStateFlow()
    
    @Serializable
    data class DustBathState(
        val lastCompletionTime: Long = 0,
        val currentStreak: Int = 0,
        val bestStreak: Int = 0,
        val totalBathsTaken: Int = 0,
        val currentBuff: DustBathBuff? = null,
        val isRitualAvailable: Boolean = true,
        val currentRitual: ActiveRitual? = null
    )
    
    @Serializable
    data class ActiveRitual(
        val phase: RitualPhase = RitualPhase.PREPARATION,
        val targetPattern: List<DustAction> = emptyList(),
        val playerPattern: List<DustAction> = emptyList(),
        val score: Int = 0,
        val perfectMoves: Int = 0,
        val startTime: Long = System.currentTimeMillis()
    )
    
    @Serializable
    enum class RitualPhase {
        PREPARATION,    // Watch Jalmar prepare the dust bath spot
        DEMONSTRATION,  // Watch the pattern to replicate
        PERFORMANCE,    // Player performs the dust bath
        CELEBRATION,    // Success animation
        COMPLETE       // Ritual finished
    }
    
    @Serializable
    enum class DustAction(val displayName: String, val description: String) {
        WIGGLE("Wiggle", "Shake those tail feathers!"),
        FLUFF("Fluff", "Puff up for maximum dust coverage"),
        ROLL_LEFT("Roll Left", "Roll to the left side"),
        ROLL_RIGHT("Roll Right", "Roll to the right side"),
        SHAKE("Shake", "Shake off excess dust"),
        SCRATCH("Scratch", "Scratch the ground for fresh dust"),
        SETTLE("Settle", "Settle into the dust hollow")
    }
    
    @Serializable
    data class DustBathBuff(
        val level: Int,           // Streak level (1-7 for weekly max)
        val seedBonus: Float,      // +5% per level
        val luckBonus: Float,      // +2% per level
        val experienceBonus: Float,// +3% per level
        val expiryTime: Long,      // 24 hours from completion
        val glowIntensity: Int     // Visual effect strength (1-7)
    )
    
    @Serializable
    data class DustBathReward(
        val seeds: Int,
        val experience: Int,
        val buff: DustBathBuff,
        val bonusItem: BonusItem? = null
    )
    
    @Serializable
    data class BonusItem(
        val itemId: String,
        val quantity: Int,
        val reason: String
    )
    
    suspend fun checkDailyAvailability(currentTimeMillis: Long) = mutex.withLock {
        val lastCompletion = _state.value.lastCompletionTime
        val hoursSinceLastBath = (currentTimeMillis - lastCompletion) / (1000 * 60 * 60)
        
        // Reset availability at daily reset (4 AM local time)
        val isNewDay = hoursSinceLastBath >= 20 // Give 4-hour grace period
        
        if (isNewDay) {
            // Check if streak should be maintained or broken
            val shouldMaintainStreak = hoursSinceLastBath < 28 // Within 28 hours maintains streak
            
            _state.value = _state.value.copy(
                isRitualAvailable = true,
                currentStreak = if (!shouldMaintainStreak) 0 else _state.value.currentStreak,
                currentBuff = if (currentTimeMillis > (_state.value.currentBuff?.expiryTime ?: 0)) {
                    null
                } else {
                    _state.value.currentBuff
                }
            )
        }
    }
    
    suspend fun startRitual(currentTimeMillis: Long) = mutex.withLock {
        if (!_state.value.isRitualAvailable) return@withLock
        
        // Generate pattern based on streak (harder patterns for longer streaks)
        val patternLength = minOf(5 + (_state.value.currentStreak / 2), 10)
        val pattern = generateDustPattern(patternLength)
        
        _state.value = _state.value.copy(
            currentRitual = ActiveRitual(
                phase = RitualPhase.PREPARATION,
                targetPattern = pattern,
                startTime = currentTimeMillis
            )
        )
    }
    
    suspend fun progressToNextPhase() = mutex.withLock {
        val current = _state.value.currentRitual ?: return@withLock
        
        val nextPhase = when (current.phase) {
            RitualPhase.PREPARATION -> RitualPhase.DEMONSTRATION
            RitualPhase.DEMONSTRATION -> RitualPhase.PERFORMANCE
            RitualPhase.PERFORMANCE -> {
                // Calculate score
                val accuracy = calculateAccuracy(current.targetPattern, current.playerPattern)
                if (accuracy >= 0.7f) RitualPhase.CELEBRATION
                else RitualPhase.PERFORMANCE // Retry
            }
            RitualPhase.CELEBRATION -> RitualPhase.COMPLETE
            RitualPhase.COMPLETE -> return@withLock
        }
        
        _state.value = _state.value.copy(
            currentRitual = current.copy(phase = nextPhase)
        )
    }
    
    suspend fun performAction(action: DustAction) = mutex.withLock {
        val current = _state.value.currentRitual ?: return@withLock
        if (current.phase != RitualPhase.PERFORMANCE) return@withLock
        
        val updatedPattern = current.playerPattern + action
        val targetAction = current.targetPattern.getOrNull(current.playerPattern.size)
        
        val isPerfect = targetAction == action
        val updatedScore = current.score + if (isPerfect) 100 else 50
        val updatedPerfectMoves = if (isPerfect) current.perfectMoves + 1 else current.perfectMoves
        
        _state.value = _state.value.copy(
            currentRitual = current.copy(
                playerPattern = updatedPattern,
                score = updatedScore,
                perfectMoves = updatedPerfectMoves
            )
        )
        
        // Auto-complete if pattern is finished
        if (updatedPattern.size >= current.targetPattern.size) {
            progressToNextPhase()
        }
    }
    
    suspend fun completeRitual(currentTimeMillis: Long) = mutex.withLock {
        val current = _state.value.currentRitual ?: return@withLock
        if (current.phase != RitualPhase.CELEBRATION) return@withLock
        
        val newStreak = _state.value.currentStreak + 1
        val bestStreak = maxOf(_state.value.bestStreak, newStreak)
        
        // Calculate buff based on streak
        val buff = DustBathBuff(
            level = minOf(newStreak, 7),
            seedBonus = 0.05f * minOf(newStreak, 7),
            luckBonus = 0.02f * minOf(newStreak, 7),
            experienceBonus = 0.03f * minOf(newStreak, 7),
            expiryTime = currentTimeMillis + (24 * 60 * 60 * 1000), // 24 hours
            glowIntensity = minOf(newStreak, 7)
        )
        
        // Calculate rewards
        val baseSeeds = 50
        val streakBonus = newStreak * 10
        val perfectBonus = current.perfectMoves * 5
        
        val reward = DustBathReward(
            seeds = baseSeeds + streakBonus + perfectBonus,
            experience = 25 + (newStreak * 5),
            buff = buff,
            bonusItem = if (newStreak % 7 == 0) {
                // Weekly streak bonus
                BonusItem(
                    itemId = "golden_dust",
                    quantity = 1,
                    reason = "Perfect weekly dust bath streak!"
                )
            } else null
        )
        
        _state.value = _state.value.copy(
            lastCompletionTime = currentTimeMillis,
            currentStreak = newStreak,
            bestStreak = bestStreak,
            totalBathsTaken = _state.value.totalBathsTaken + 1,
            currentBuff = buff,
            isRitualAvailable = false,
            currentRitual = null
        )
        
        // Grant rewards through callback
        onRewardGranted(reward)
    }
    
    private fun generateDustPattern(length: Int): List<DustAction> {
        val actions = DustAction.entries
        return List(length) {
            actions[Random.nextInt(actions.size)]
        }
    }
    
    private fun calculateAccuracy(
        target: List<DustAction>,
        player: List<DustAction>
    ): Float {
        if (target.isEmpty()) return 0f
        
        var matches = 0
        for (i in target.indices) {
            if (player.getOrNull(i) == target[i]) {
                matches++
            }
        }
        
        return matches.toFloat() / target.size
    }
}
