package com.jalmarquest.core.state.aidirector

import com.jalmarquest.core.model.AIDirectorState
import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.DifficultyLevel
import com.jalmarquest.core.model.PerformanceMetrics
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.model.Playstyle
import com.jalmarquest.core.model.PlaystyleProfile
import com.jalmarquest.core.state.GameStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Alpha 2.2: AI Director Core Manager
 * 
 * Monitors player behavior and adapts game difficulty and content recommendations.
 * 
 * **Core Responsibilities**:
 * - Track difficulty based on player performance (deaths, quest failures, combat wins/losses)
 * - Analyze playstyle from choice log (cautious, aggressive, explorer, hoarder, social)
 * - Recommend event types to EventEngine based on player preferences
 * - Adjust pacing to prevent player fatigue
 * 
 * **Integration Points**:
 * - GameStateManager: Monitors choice log for playstyle detection
 * - EventEngine: Receives event type recommendations
 * - QuestManager: Difficulty adjustments for quest rewards/challenges
 * - ExploreSystem: Chapter event frequency tuning
 * - BorkenEventTrigger: Chaos event suppression/boosting
 * 
 * **Usage**:
 * ```kotlin
 * val director = AIDirectorManager(gameStateManager)
 * 
 * // Get current difficulty level
 * val difficulty = director.getCurrentDifficulty()
 * 
 * // Analyze player's dominant playstyle
 * val playstyle = director.getPlaystyle()
 * 
 * // Get recommended event type
 * val eventType = director.recommendEventType()
 * ```
 */

/**
 * Event type recommendations for EventEngine.
 */
enum class EventRecommendation {
    /** Combat encounters */
    COMBAT,
    
    /** Exploration and discovery */
    EXPLORATION,
    
    /** NPC interactions and dialogue */
    SOCIAL,
    
    /** Resource gathering and crafting */
    RESOURCE,
    
    /** Lore and story-focused events */
    NARRATIVE,
    
    /** Chaotic/random events (Borken) */
    CHAOS,
    
    /** Any event type - no strong preference */
    BALANCED
}

/**
 * AI Director Manager - Adaptive difficulty and content recommendation system.
 */
class AIDirectorManager(
    private val gameStateManager: GameStateManager,
    private val timestampProvider: () -> Long
) {
    private val mutex = Mutex()
    
    private val _state = MutableStateFlow(gameStateManager.playerState.value.aiDirectorState)
    val state: StateFlow<AIDirectorState> = _state.asStateFlow()
    
    init {
        // Sync state with GameStateManager
        _state.value = gameStateManager.playerState.value.aiDirectorState
    }
    
    /**
     * Get current difficulty level.
     */
    fun getCurrentDifficulty(): DifficultyLevel = _state.value.currentDifficulty
    
    /**
     * Get player's dominant playstyle.
     */
    fun getPlaystyle(): Playstyle = _state.value.playstyle.getDominantStyle()
    
    /**
     * Record combat win for difficulty tracking.
     */
    suspend fun recordCombatWin() {
        mutex.withLock {
            val currentState = _state.value
            val newMetrics = currentState.performance.copy(
                combatWins = currentState.performance.combatWins + 1
            )
            updateState(currentState.copy(performance = newMetrics))
            recalculateDifficulty()
        }
    }
    
    /**
     * Record combat loss for difficulty tracking.
     */
    suspend fun recordCombatLoss() {
        mutex.withLock {
            val currentState = _state.value
            val newMetrics = currentState.performance.copy(
                combatLosses = currentState.performance.combatLosses + 1
            )
            updateState(currentState.copy(performance = newMetrics))
            recalculateDifficulty()
        }
    }
    
    /**
     * Record quest completion for difficulty tracking.
     */
    suspend fun recordQuestCompletion() {
        mutex.withLock {
            val currentState = _state.value
            val newMetrics = currentState.performance.copy(
                questCompletions = currentState.performance.questCompletions + 1
            )
            updateState(currentState.copy(performance = newMetrics))
        }
    }
    
    /**
     * Record quest failure for difficulty tracking.
     */
    suspend fun recordQuestFailure() {
        mutex.withLock {
            val currentState = _state.value
            val newMetrics = currentState.performance.copy(
                questFailures = currentState.performance.questFailures + 1
            )
            updateState(currentState.copy(performance = newMetrics))
            recalculateDifficulty()
        }
    }
    
    /**
     * Record player death for difficulty tracking.
     */
    suspend fun recordDeath() {
        mutex.withLock {
            val currentState = _state.value
            val newMetrics = currentState.performance.copy(
                deaths = currentState.performance.deaths + 1
            )
            updateState(currentState.copy(performance = newMetrics))
            recalculateDifficulty()
        }
    }
    
    /**
     * Analyze player's choice log and update playstyle profile.
     */
    suspend fun analyzePlaystyle() {
        mutex.withLock {
            val choiceLog = gameStateManager.playerState.value.choiceLog
            val newProfile = calculatePlaystyleFromChoices(choiceLog)
            val currentState = _state.value
            updateState(currentState.copy(playstyle = newProfile))
        }
    }
    
    /**
     * Recommend event type based on playstyle and recent activity.
     */
    fun recommendEventType(): EventRecommendation {
        val playstyle = getPlaystyle()
        
        return when (playstyle) {
            Playstyle.AGGRESSIVE -> EventRecommendation.COMBAT
            Playstyle.EXPLORER -> EventRecommendation.EXPLORATION
            Playstyle.SOCIAL -> EventRecommendation.SOCIAL
            Playstyle.HOARDER -> EventRecommendation.RESOURCE
            Playstyle.CAUTIOUS -> EventRecommendation.NARRATIVE
            Playstyle.BALANCED -> EventRecommendation.BALANCED
        }
    }
    
    /**
     * Check if player is experiencing fatigue (too many events without rest).
     */
    fun isPlayerFatigued(): Boolean {
        val currentState = _state.value
        return currentState.eventsSinceRest >= 5
    }
    
    /**
     * Record that an event was triggered.
     */
    suspend fun recordEvent() {
        mutex.withLock {
            val currentState = _state.value
            updateState(
                currentState.copy(
                    lastEventTimestamp = timestampProvider(),
                    eventsSinceRest = currentState.eventsSinceRest + 1
                )
            )
        }
    }
    
    /**
     * Record that player rested (resets fatigue counter).
     */
    suspend fun recordRest() {
        mutex.withLock {
            val currentState = _state.value
            updateState(currentState.copy(eventsSinceRest = 0))
        }
    }
    
    /**
     * Get current events since rest count.
     * Used for UI display of fatigue level.
     */
    fun getEventsSinceRest(): Int {
        return _state.value.eventsSinceRest
    }
    
    /**
     * Get difficulty scaling multiplier for rewards/challenges.
     * 
     * @return Multiplier (0.7 for EASY, 1.0 for NORMAL, 1.3 for HARD, 1.6 for EXPERT)
     */
    fun getDifficultyMultiplier(): Float {
        return when (getCurrentDifficulty()) {
            DifficultyLevel.EASY -> 0.7f
            DifficultyLevel.NORMAL -> 1.0f
            DifficultyLevel.HARD -> 1.3f
            DifficultyLevel.EXPERT -> 1.6f
        }
    }
    
    /**
     * Should Borken chaos events be boosted?
     * Returns true if player enjoys chaos (aggressive/balanced playstyle).
     */
    fun shouldBoostChaosEvents(): Boolean {
        val playstyle = getPlaystyle()
        return playstyle == Playstyle.AGGRESSIVE || playstyle == Playstyle.BALANCED
    }
    
    /**
     * Should Borken chaos events be suppressed?
     * Returns true if player prefers calm experience (cautious playstyle + recent deaths).
     */
    fun shouldSuppressChaosEvents(): Boolean {
        val playstyle = getPlaystyle()
        val recentDeaths = _state.value.performance.deaths >= 3
        return playstyle == Playstyle.CAUTIOUS && recentDeaths
    }
    
    // ========== PRIVATE HELPERS ==========
    
    private fun updateState(newState: AIDirectorState) {
        _state.value = newState
        gameStateManager.updateAIDirectorState(newState)
    }
    
    private fun recalculateDifficulty() {
        val metrics = _state.value.performance
        
        // Calculate win rate
        val totalCombats = metrics.combatWins + metrics.combatLosses
        val combatWinRate = if (totalCombats > 0) {
            metrics.combatWins.toFloat() / totalCombats
        } else {
            0.5f // Default 50%
        }
        
        // Calculate quest success rate
        val totalQuests = metrics.questCompletions + metrics.questFailures
        val questSuccessRate = if (totalQuests > 0) {
            metrics.questCompletions.toFloat() / totalQuests
        } else {
            0.5f // Default 50%
        }
        
        // Death penalty
        val deathPenalty = metrics.deaths * 0.1f
        
        // Overall performance score (0.0 to 1.0+)
        val performanceScore = (combatWinRate + questSuccessRate) / 2 - deathPenalty
        
        // Determine difficulty
        val newDifficulty = when {
            performanceScore < 0.3f -> DifficultyLevel.EASY
            performanceScore < 0.6f -> DifficultyLevel.NORMAL
            performanceScore < 0.85f -> DifficultyLevel.HARD
            else -> DifficultyLevel.EXPERT
        }
        
        val currentState = _state.value
        if (newDifficulty != currentState.currentDifficulty) {
            updateState(currentState.copy(currentDifficulty = newDifficulty))
        }
    }
    
    private fun calculatePlaystyleFromChoices(choiceLog: ChoiceLog): PlaystyleProfile {
        var cautious = 0
        var aggressive = 0
        var explorer = 0
        var hoarder = 0
        var social = 0
        
        choiceLog.entries.forEach { entry ->
            val tag = entry.tag.value.lowercase()
            
            // Cautious patterns
            when {
                tag.contains("flee") || tag.contains("retreat") || tag.contains("avoid") -> cautious++
                tag.contains("hide") || tag.contains("sneak") || tag.contains("careful") -> cautious++
                
                // Aggressive patterns
                tag.contains("attack") || tag.contains("fight") || tag.contains("combat") -> aggressive++
                tag.contains("challenge") || tag.contains("confront") -> aggressive++
                
                // Explorer patterns
                tag.contains("explore") || tag.contains("discover") || tag.contains("travel") -> explorer++
                tag.contains("location") || tag.contains("lore") || tag.contains("investigate") -> explorer++
                
                // Hoarder patterns
                tag.contains("collect") || tag.contains("gather") || tag.contains("hoard") -> hoarder++
                tag.contains("shiny") || tag.contains("seeds") || tag.contains("resource") -> hoarder++
                
                // Social patterns
                tag.contains("talk") || tag.contains("conversation") || tag.contains("npc") -> social++
                tag.contains("gift") || tag.contains("affinity") || tag.contains("friend") -> social++
            }
        }
        
        return PlaystyleProfile(
            cautiousScore = cautious,
            aggressiveScore = aggressive,
            explorerScore = explorer,
            hoarderScore = hoarder,
            socialScore = social
        )
    }
}
