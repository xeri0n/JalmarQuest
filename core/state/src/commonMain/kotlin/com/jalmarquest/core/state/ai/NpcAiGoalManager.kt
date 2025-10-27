package com.jalmarquest.core.state.ai

import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.catalogs.NpcCatalog
import com.jalmarquest.core.state.npc.NpcRelationshipManager
import com.jalmarquest.core.state.npc.NpcScheduleManager
import com.jalmarquest.core.state.npc.RelationshipLevel
import com.jalmarquest.core.state.time.InGameTimeManager
import com.jalmarquest.core.state.time.TimeOfDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * Phase 3: Advanced NPC AI - Goal-based behavior system.
 * NPCs can pursue multiple goals with priorities, success conditions,
 * and dynamic selection based on context (time, needs, relationships).
 */

/**
 * Types of goals NPCs can pursue.
 */
enum class GoalType {
    // Basic needs
    SEEK_FOOD,           // Find and consume food
    SEEK_REST,           // Go to resting location
    SEEK_SHELTER,        // Seek shelter during bad weather/night
    
    // Social goals
    SOCIALIZE,           // Interact with other NPCs
    SEEK_PLAYER,         // Actively look for the player
    AVOID_PLAYER,        // Stay away from the player
    VISIT_FRIEND,        // Visit an NPC they have high affinity with
    
    // Work/Profession
    WORK_PROFESSION,     // Do their job (merchant sell, guard patrol, etc.)
    GATHER_RESOURCES,    // Collect items for their profession
    CRAFT_ITEMS,         // Create items for sale/use
    
    // Exploration
    EXPLORE_AREA,        // Wander and explore
    INVESTIGATE_EVENT,   // Check out a world event/rumor
    
    // Quest-related
    OFFER_QUEST,         // Ready to give player a quest
    REQUEST_HELP,        // Actively seeking player assistance
    
    // Defensive
    FLEE_DANGER,         // Run from threats
    DEFEND_TERRITORY,    // Protect their area
    SEEK_SAFETY          // Find safe location
}

/**
 * Conditions that must be met for a goal to succeed.
 */
sealed class GoalCondition {
    /**
     * Goal succeeds when NPC reaches a specific location.
     */
    @Serializable
    data class ReachLocation(val locationId: String) : GoalCondition()
    
    /**
     * Goal succeeds when a certain amount of time passes.
     */
    @Serializable
    data class WaitDuration(val durationMinutes: Int) : GoalCondition()
    
    /**
     * Goal succeeds when NPC is near player.
     */
    @Serializable
    data object NearPlayer : GoalCondition()
    
    /**
     * Goal succeeds when NPC has a specific item.
     */
    @Serializable
    data class HasItem(val itemId: String, val quantity: Int = 1) : GoalCondition()
    
    /**
     * Goal succeeds when it's a specific time of day.
     */
    @Serializable
    data class TimeOfDayReached(val timeOfDay: TimeOfDay) : GoalCondition()
    
    /**
     * Goal succeeds when relationship with player reaches a threshold.
     */
    @Serializable
    data class PlayerAffinityAbove(val threshold: Int) : GoalCondition()
    
    /**
     * Goal succeeds when NPC interacts with another NPC.
     */
    @Serializable
    data class InteractWithNpc(val npcId: String) : GoalCondition()
    
    /**
     * Always succeeds (for passive goals).
     */
    @Serializable
    data object AlwaysSucceeds : GoalCondition()
}

/**
 * Factors that determine goal priority (higher = more urgent).
 */
enum class GoalPriorityFactor {
    CRITICAL,    // 100 - Survival needs (flee danger, seek shelter)
    HIGH,        // 75 - Important needs (food, rest)
    MEDIUM,      // 50 - Normal activities (work, socialize)
    LOW,         // 25 - Optional activities (explore, visit)
    MINIMAL      // 10 - Background activities
}

/**
 * A goal that an NPC is pursuing.
 */
@Serializable
data class NpcGoal(
    val id: String,
    val npcId: String,
    val type: GoalType,
    val priority: Int,  // Calculated from base priority + modifiers
    val conditionType: String,  // Store condition as string for serialization
    val targetLocationId: String? = null,
    val targetNpcId: String? = null,
    val startedAtTime: Long,
    val expiresAtTime: Long? = null,  // Some goals expire
    val description: String? = null,
    val metadata: Map<String, String> = emptyMap()  // Flexible data storage
) {
    /**
     * Check if this goal has expired.
     */
    fun hasExpired(currentTime: Long): Boolean {
        return expiresAtTime != null && currentTime >= expiresAtTime
    }
    
    /**
     * Check if this goal is still valid based on time.
     */
    fun isValid(currentTime: Long): Boolean = !hasExpired(currentTime)
}

/**
 * Current state of an NPC's AI.
 */
@Serializable
data class NpcAiState(
    val npcId: String,
    val currentGoal: NpcGoal? = null,
    val pendingGoals: List<NpcGoal> = emptyList(),
    val completedGoalCount: Int = 0,
    val lastGoalChangeTime: Long = 0,
    val isStuck: Boolean = false,  // True if NPC can't complete goal
    val stuckDuration: Int = 0     // How long NPC has been stuck (seconds)
)

/**
 * Manager for NPC AI goals and autonomous behaviors.
 */
class NpcAiGoalManager(
    private val npcCatalog: NpcCatalog,
    private val scheduleManager: NpcScheduleManager,
    private val relationshipManager: NpcRelationshipManager,
    private val timeManager: InGameTimeManager,
    private val gameStateManager: GameStateManager,
    private val timestampProvider: () -> Long
) {
    private val _aiStates = MutableStateFlow<Map<String, NpcAiState>>(emptyMap())
    val aiStates: StateFlow<Map<String, NpcAiState>> = _aiStates.asStateFlow()
    
    private var nextGoalId = 1
    
    init {
        initializeNpcAi()
    }
    
    /**
     * Initialize AI for all NPCs in the catalog.
     */
    private fun initializeNpcAi() {
        val allNpcs = npcCatalog.getAllNpcs()
        val initialStates = allNpcs.associate { npc ->
            npc.id to NpcAiState(
                npcId = npc.id,
                lastGoalChangeTime = timestampProvider()
            )
        }
        _aiStates.value = initialStates
    }
    
    /**
     * Get the current AI state for an NPC.
     */
    fun getAiState(npcId: String): NpcAiState? = _aiStates.value[npcId]
    
    /**
     * Assign a new goal to an NPC.
     */
    fun assignGoal(
        npcId: String,
        type: GoalType,
        basePriority: GoalPriorityFactor,
        conditionType: String = "AlwaysSucceeds",
        targetLocationId: String? = null,
        targetNpcId: String? = null,
        durationMinutes: Int? = null,
        description: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): NpcGoal {
        val currentTime = timestampProvider()
        val priority = calculateGoalPriority(npcId, type, basePriority)
        val expiresAt = durationMinutes?.let { currentTime + (it * 60 * 1000) }
        
        val goal = NpcGoal(
            id = "goal_${nextGoalId++}",
            npcId = npcId,
            type = type,
            priority = priority,
            conditionType = conditionType,
            targetLocationId = targetLocationId,
            targetNpcId = targetNpcId,
            startedAtTime = currentTime,
            expiresAtTime = expiresAt,
            description = description,
            metadata = metadata
        )
        
        addGoalToNpc(npcId, goal)
        return goal
    }
    
    /**
     * Calculate effective goal priority based on context.
     */
    private fun calculateGoalPriority(
        npcId: String,
        type: GoalType,
        basePriority: GoalPriorityFactor
    ): Int {
        var priority = when (basePriority) {
            GoalPriorityFactor.CRITICAL -> 100
            GoalPriorityFactor.HIGH -> 75
            GoalPriorityFactor.MEDIUM -> 50
            GoalPriorityFactor.LOW -> 25
            GoalPriorityFactor.MINIMAL -> 10
        }
        
        // Modify based on time of day
        val timeOfDay = timeManager.currentTime.value.getTimeOfDay()
        when (type) {
            GoalType.SEEK_REST -> {
                if (timeOfDay == TimeOfDay.NIGHT || timeOfDay == TimeOfDay.DUSK) {
                    priority += 20  // More urgent at night
                }
            }
            GoalType.WORK_PROFESSION -> {
                if (timeOfDay == TimeOfDay.MORNING || timeOfDay == TimeOfDay.AFTERNOON) {
                    priority += 15  // More important during work hours
                }
            }
            GoalType.SOCIALIZE -> {
                if (timeOfDay == TimeOfDay.DUSK) {
                    priority += 10  // Social time in evening
                }
            }
            else -> { /* No time modifier */ }
        }
        
        // Modify based on relationship with player
        val allRelationships = relationshipManager.relationships.value
        val relationship = allRelationships.relationships[npcId]
        if (relationship != null && relationship.getRelationshipLevel() != RelationshipLevel.STRANGER) {
            when (type) {
                GoalType.SEEK_PLAYER -> {
                    // Friends more likely to seek player
                    priority += (relationship.affinity / 10)
                }
                GoalType.AVOID_PLAYER -> {
                    // Low affinity = avoid player more
                    priority += ((100 - relationship.affinity) / 10)
                }
                GoalType.OFFER_QUEST -> {
                    // Only offer quests to friends
                    if (relationship.affinity < 20) {
                        priority -= 30
                    }
                }
                else -> { /* No relationship modifier */ }
            }
        }
        
        return priority.coerceIn(0, 150)
    }
    
    /**
     * Add a goal to an NPC's queue.
     */
    private fun addGoalToNpc(npcId: String, goal: NpcGoal) {
        _aiStates.value = _aiStates.value.toMutableMap().apply {
            val currentState = this[npcId] ?: NpcAiState(npcId)
            
            // Add to pending goals
            val newPending = (currentState.pendingGoals + goal)
                .sortedByDescending { it.priority }
            
            this[npcId] = currentState.copy(pendingGoals = newPending)
        }
    }
    
    /**
     * Process NPC AI - select and execute goals.
     * Should be called regularly (e.g., every game tick).
     */
    fun updateAi(npcId: String) {
        val state = _aiStates.value[npcId] ?: return
        val currentTime = timestampProvider()
        
        // Remove expired goals
        val validGoals = state.pendingGoals.filter { it.isValid(currentTime) }
        
        // Check if current goal is complete or invalid
        val currentGoal = state.currentGoal
        val isCurrentGoalComplete = currentGoal?.let { 
            checkGoalCondition(it) || !it.isValid(currentTime)
        } ?: true
        
        if (isCurrentGoalComplete) {
            // Select next goal from queue
            val nextGoal = validGoals.firstOrNull()
            
            _aiStates.value = _aiStates.value.toMutableMap().apply {
                this[npcId] = state.copy(
                    currentGoal = nextGoal,
                    pendingGoals = if (nextGoal != null) validGoals.drop(1) else validGoals,
                    completedGoalCount = if (currentGoal != null && checkGoalCondition(currentGoal)) {
                        state.completedGoalCount + 1
                    } else {
                        state.completedGoalCount
                    },
                    lastGoalChangeTime = currentTime,
                    isStuck = false,
                    stuckDuration = 0
                )
            }
        } else {
            // Continue current goal, check for stuck state
            val timeSinceChange = (currentTime - state.lastGoalChangeTime) / 1000
            if (timeSinceChange > 300) {  // 5 minutes without progress
                _aiStates.value = _aiStates.value.toMutableMap().apply {
                    this[npcId] = state.copy(
                        isStuck = true,
                        stuckDuration = timeSinceChange.toInt()
                    )
                }
            }
        }
    }
    
    /**
     * Check if a goal's condition is met.
     * Simplified version - uses string-based condition type.
     */
    private fun checkGoalCondition(goal: NpcGoal): Boolean {
        return when (goal.conditionType) {
            "ReachLocation" -> {
                goal.targetLocationId?.let {
                    scheduleManager.getCurrentLocation(goal.npcId) == it
                } ?: false
            }
            "TimeOfDayReached" -> {
                // Would need to parse target time from metadata
                false  // Simplified
            }
            "PlayerAffinityAbove" -> {
                val threshold = goal.metadata["affinityThreshold"]?.toIntOrNull() ?: 0
                val allRelationships = relationshipManager.relationships.value
                val relationship = allRelationships.relationships[goal.npcId]
                (relationship?.affinity ?: 0) >= threshold
            }
            "AlwaysSucceeds" -> true
            else -> false  // Unknown condition type
        }
    }
    
    /**
     * Update all NPC AIs.
     */
    fun updateAllAi() {
        _aiStates.value.keys.forEach { npcId ->
            updateAi(npcId)
        }
    }
    
    /**
     * Get the description of what an NPC is currently doing.
     */
    fun getCurrentActivity(npcId: String): String? {
        val state = _aiStates.value[npcId] ?: return null
        val goal = state.currentGoal ?: return null
        
        return goal.description ?: when (goal.type) {
            GoalType.SEEK_FOOD -> "looking for food"
            GoalType.SEEK_REST -> "resting"
            GoalType.SEEK_SHELTER -> "seeking shelter"
            GoalType.SOCIALIZE -> "socializing with others"
            GoalType.SEEK_PLAYER -> "looking for you"
            GoalType.AVOID_PLAYER -> "avoiding you"
            GoalType.VISIT_FRIEND -> "visiting a friend"
            GoalType.WORK_PROFESSION -> "working"
            GoalType.GATHER_RESOURCES -> "gathering resources"
            GoalType.CRAFT_ITEMS -> "crafting items"
            GoalType.EXPLORE_AREA -> "exploring the area"
            GoalType.INVESTIGATE_EVENT -> "investigating something"
            GoalType.OFFER_QUEST -> "ready to talk"
            GoalType.REQUEST_HELP -> "seeking help"
            GoalType.FLEE_DANGER -> "fleeing from danger"
            GoalType.DEFEND_TERRITORY -> "defending their territory"
            GoalType.SEEK_SAFETY -> "seeking safety"
        }
    }
    
    /**
     * Cancel all goals for an NPC.
     */
    fun cancelAllGoals(npcId: String) {
        _aiStates.value = _aiStates.value.toMutableMap().apply {
            val state = this[npcId]
            if (state != null) {
                this[npcId] = state.copy(
                    currentGoal = null,
                    pendingGoals = emptyList(),
                    isStuck = false,
                    stuckDuration = 0
                )
            }
        }
    }
    
    /**
     * Assign daily routine goals based on NPC schedule.
     */
    fun assignDailyRoutineGoals(npcId: String) {
        val schedule = scheduleManager.getSchedule(npcId) ?: return
        val npc = npcCatalog.getNpcById(npcId) ?: return
        
        // Create goals for each scheduled location
        TimeOfDay.entries.forEach { timeOfDay ->
            val locationId = schedule.getLocationAt(timeOfDay)
            val activity = schedule.getActivityAt(timeOfDay)
            
            assignGoal(
                npcId = npcId,
                type = GoalType.WORK_PROFESSION,
                basePriority = GoalPriorityFactor.MEDIUM,
                conditionType = "TimeOfDayReached",
                targetLocationId = locationId,
                description = activity ?: "at ${locationId}",
                metadata = mapOf("targetTime" to timeOfDay.name)
            )
        }
    }
}
