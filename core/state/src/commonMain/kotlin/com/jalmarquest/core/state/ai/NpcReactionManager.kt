package com.jalmarquest.core.state.ai

import com.jalmarquest.core.model.ChoiceTag
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.catalogs.NpcCatalog
import com.jalmarquest.core.state.factions.FactionManager
import com.jalmarquest.core.state.npc.NpcRelationshipManager
import com.jalmarquest.core.state.quests.QuestManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * Phase 3: Advanced NPC AI - Reaction system.
 * NPCs react to player choices, faction reputation changes, world events,
 * and quest outcomes with dynamic dialogue and behavior changes.
 */

/**
 * Types of events NPCs can react to.
 */
enum class WorldEventType {
    // Player actions
    PLAYER_CHOICE,           // Player made a significant choice
    PLAYER_QUEST_COMPLETE,   // Player completed a quest
    PLAYER_QUEST_FAILED,     // Player failed a quest
    PLAYER_KILLED_ENEMY,     // Player defeated an enemy
    PLAYER_DIED,             // Player was defeated
    
    // Faction events
    FACTION_REP_INCREASED,   // Player gained faction reputation
    FACTION_REP_DECREASED,   // Player lost faction reputation
    FACTION_STANDING_CHANGED,// Player's standing with faction changed tier
    FACTION_WAR_STARTED,     // Two factions went to war
    FACTION_ALLIANCE_FORMED, // Two factions became allies
    
    // World events
    LOCATION_DISCOVERED,     // New location found
    LOCATION_CHANGED,        // Location state changed (cleansed, destroyed, etc.)
    NPC_DIED,                // An NPC died
    NPC_JOINED_PLAYER,       // NPC became companion
    RARE_ITEM_ACQUIRED,      // Player got rare/legendary item
    
    // Time-based
    SEASON_CHANGED,          // New season started
    DAY_PASSED,              // A full day passed
    FESTIVAL_STARTED,        // Special event began
    
    // Custom
    CUSTOM                   // For specific scripted events
}

/**
 * A world event that NPCs can react to.
 */
@Serializable
data class WorldEvent(
    val id: String,
    val type: WorldEventType,
    val timestamp: Long,
    val sourceNpcId: String? = null,      // Who caused it (if an NPC)
    val targetNpcId: String? = null,      // Who it happened to (if an NPC)
    val locationId: String? = null,        // Where it happened
    val questId: String? = null,           // Related quest
    val factionId: String? = null,         // Related faction
    val choiceTag: String? = null,         // Related player choice
    val metadata: Map<String, String> = emptyMap()  // Additional data
)

/**
 * Types of reactions NPCs can have.
 */
enum class ReactionType {
    // Emotional
    HAPPY,           // Pleased by event
    SAD,             // Upset by event
    ANGRY,           // Angered by event
    FEARFUL,         // Scared by event
    GRATEFUL,        // Thankful for event
    DISAPPOINTED,    // Let down by event
    
    // Behavioral
    BECOME_FRIENDLY,  // Improves relationship with player
    BECOME_HOSTILE,   // Worsens relationship with player
    OFFER_REWARD,     // Gives player something
    REFUSE_SERVICE,   // Won't trade/interact
    FLEE_LOCATION,    // Leaves current location
    SEEK_PLAYER,      // Actively looks for player
    
    // Dialogue
    SPECIAL_DIALOGUE, // Triggers unique dialogue
    GOSSIP,           // Talks about the event
    WARNING,          // Warns player about consequences
    
    // Quest-related
    OFFER_NEW_QUEST,  // Unlocks new quest
    FAIL_QUEST,       // Cancels active quest
    UNLOCK_LOCATION,  // Opens new area
    
    // No reaction
    NEUTRAL           // Doesn't care
}

/**
 * An NPC's reaction to a world event.
 */
@Serializable
data class NpcReaction(
    val npcId: String,
    val eventId: String,
    val reactionType: ReactionType,
    val affinityChange: Int = 0,           // Change to relationship with player
    val dialogue: String? = null,           // Special dialogue to show
    val questIdToOffer: String? = null,     // Quest to unlock
    val questIdToFail: String? = null,      // Quest to cancel
    val locationIdToUnlock: String? = null, // Location to reveal
    val duration: Long? = null,             // How long reaction lasts (ms)
    val expiresAt: Long? = null,            // When reaction expires
    val hasTriggered: Boolean = false       // Whether reaction was shown to player
)

/**
 * Predefined reaction rules for NPCs.
 */
@Serializable
data class NpcReactionRule(
    val npcId: String,
    val eventType: WorldEventType,
    val reactionType: ReactionType,
    val conditionType: String? = null,  // Simplified for serialization
    val affinityChange: Int = 0,
    val dialogue: String? = null,
    val questIdToOffer: String? = null,
    val questIdToFail: String? = null,
    val locationIdToUnlock: String? = null,
    val durationMinutes: Int? = null
)

/**
 * Conditions for NPC reactions.
 */
sealed class ReactionCondition {
    /**
     * React only if player has specific choice tag.
     */
    @Serializable
    data class HasChoiceTag(val tag: String) : ReactionCondition()
    
    /**
     * React only if affinity is above/below threshold.
     */
    @Serializable
    data class AffinityThreshold(val minAffinity: Int? = null, val maxAffinity: Int? = null) : ReactionCondition()
    
    /**
     * React only if faction reputation is above/below threshold.
     */
    @Serializable
    data class FactionRepThreshold(val factionId: String, val minRep: Int? = null, val maxRep: Int? = null) : ReactionCondition()
    
    /**
     * React only if quest is in specific state.
     */
    @Serializable
    data class QuestState(val questId: String, val isActive: Boolean) : ReactionCondition()
    
    /**
     * React only if player is at specific location.
     */
    @Serializable
    data class PlayerAtLocation(val locationId: String) : ReactionCondition()
}

/**
 * Manager for NPC reactions to world events.
 */
class NpcReactionManager(
    private val npcCatalog: NpcCatalog,
    private val relationshipManager: NpcRelationshipManager,
    private val factionManager: FactionManager,
    private val questManager: QuestManager,
    private val gameStateManager: GameStateManager,
    private val timestampProvider: () -> Long
) {
    private val _worldEvents = MutableStateFlow<List<WorldEvent>>(emptyList())
    val worldEvents: StateFlow<List<WorldEvent>> = _worldEvents.asStateFlow()
    
    private val _activeReactions = MutableStateFlow<Map<String, List<NpcReaction>>>(emptyMap())
    val activeReactions: StateFlow<Map<String, List<NpcReaction>>> = _activeReactions.asStateFlow()
    
    private val reactionRules = mutableListOf<NpcReactionRule>()
    private var nextEventId = 1
    
    init {
        registerDefaultReactionRules()
    }
    
    /**
     * Register a reaction rule for an NPC.
     */
    fun registerReactionRule(rule: NpcReactionRule) {
        reactionRules.add(rule)
    }
    
    /**
     * Record a world event and generate NPC reactions.
     */
    fun recordEvent(
        type: WorldEventType,
        sourceNpcId: String? = null,
        targetNpcId: String? = null,
        locationId: String? = null,
        questId: String? = null,
        factionId: String? = null,
        choiceTag: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): WorldEvent {
        val event = WorldEvent(
            id = "event_${nextEventId++}",
            type = type,
            timestamp = timestampProvider(),
            sourceNpcId = sourceNpcId,
            targetNpcId = targetNpcId,
            locationId = locationId,
            questId = questId,
            factionId = factionId,
            choiceTag = choiceTag,
            metadata = metadata
        )
        
        _worldEvents.value = _worldEvents.value + event
        
        // Generate reactions from all NPCs
        generateReactions(event)
        
        return event
    }
    
    /**
     * Generate NPC reactions to an event.
     */
    private fun generateReactions(event: WorldEvent) {
        val player = gameStateManager.playerState.value
        
        reactionRules.filter { it.eventType == event.type }.forEach { rule ->
            // Simplified condition checking for Phase 3
            val shouldReact = rule.conditionType == null || true  // Implement full checking later
            
            if (!shouldReact) return@forEach
            
            val currentTime = timestampProvider()
            val expiresAt = rule.durationMinutes?.let { currentTime + (it * 60 * 1000) }
            
            val reaction = NpcReaction(
                npcId = rule.npcId,
                eventId = event.id,
                reactionType = rule.reactionType,
                affinityChange = rule.affinityChange,
                dialogue = rule.dialogue,
                questIdToOffer = rule.questIdToOffer,
                questIdToFail = rule.questIdToFail,
                locationIdToUnlock = rule.locationIdToUnlock,
                duration = rule.durationMinutes?.let { it * 60 * 1000L },
                expiresAt = expiresAt
            )
            
            addReaction(reaction)
            applyReactionEffects(reaction)
        }
    }
    
    /**
     * Check if a reaction condition is met.
     */
    private fun checkReactionCondition(
        condition: ReactionCondition,
        event: WorldEvent,
        player: Player
    ): Boolean {
        return when (condition) {
            is ReactionCondition.HasChoiceTag -> {
                player.choiceLog.entries.any { it.tag.value == condition.tag }
            }
            is ReactionCondition.AffinityThreshold -> {
                // Would need to look up NPC from rule - simplified
                true  // Implement when we have full context
            }
            is ReactionCondition.FactionRepThreshold -> {
                val rep = factionManager.getReputation(condition.factionId).reputation
                (condition.minRep == null || rep >= condition.minRep) &&
                (condition.maxRep == null || rep <= condition.maxRep)
            }
            is ReactionCondition.QuestState -> {
                // Would need quest manager getQuestState method
                true  // Implement when quest API available
            }
            is ReactionCondition.PlayerAtLocation -> {
                // Would need player location tracking
                true  // Implement when location system exists
            }
        }
    }
    
    /**
     * Add a reaction to the active reactions list.
     */
    private fun addReaction(reaction: NpcReaction) {
        _activeReactions.value = _activeReactions.value.toMutableMap().apply {
            val npcReactions = this[reaction.npcId] ?: emptyList()
            this[reaction.npcId] = npcReactions + reaction
        }
    }
    
    /**
     * Apply the effects of a reaction immediately.
     * Note: Affinity changes are recorded but applied separately via suspend calls.
     */
    private fun applyReactionEffects(reaction: NpcReaction) {
        // Affinity changes are stored in reaction and applied when reaction is triggered
        // (requires suspend context, so handled in UI layer)
        
        // Other behavioral changes based on reaction type
        when (reaction.reactionType) {
            ReactionType.FLEE_LOCATION -> {
                // NPC would leave location - handled by goal system
            }
            else -> { /* Other reactions handled in UI/dialogue layer */ }
        }
    }
    
    /**
     * Get all active reactions for an NPC.
     */
    fun getActiveReactions(npcId: String): List<NpcReaction> {
        val currentTime = timestampProvider()
        val reactions = _activeReactions.value[npcId] ?: emptyList()
        
        // Filter out expired reactions
        return reactions.filter { reaction ->
            reaction.expiresAt == null || currentTime < reaction.expiresAt
        }
    }
    
    /**
     * Get the most recent untriggered reaction for an NPC (for dialogue).
     */
    fun getNextReaction(npcId: String): NpcReaction? {
        return getActiveReactions(npcId).firstOrNull { !it.hasTriggered }
    }
    
    /**
     * Mark a reaction as triggered (shown to player).
     */
    fun markReactionTriggered(npcId: String, eventId: String) {
        _activeReactions.value = _activeReactions.value.toMutableMap().apply {
            val reactions = this[npcId] ?: return
            this[npcId] = reactions.map { reaction ->
                if (reaction.eventId == eventId) {
                    reaction.copy(hasTriggered = true)
                } else {
                    reaction
                }
            }
        }
    }
    
    /**
     * Clear expired reactions.
     */
    fun cleanupExpiredReactions() {
        val currentTime = timestampProvider()
        
        _activeReactions.value = _activeReactions.value.mapValues { (_, reactions) ->
            reactions.filter { reaction ->
                reaction.expiresAt == null || currentTime < reaction.expiresAt
            }
        }.filterValues { it.isNotEmpty() }
    }
    
    /**
     * Register default reaction rules for common events.
     */
    private fun registerDefaultReactionRules() {
        // Example: NPCs react to player helping their faction
        registerReactionRule(
            NpcReactionRule(
                npcId = "elder_quill",
                eventType = WorldEventType.FACTION_REP_INCREASED,
                reactionType = ReactionType.GRATEFUL,
                conditionType = "FactionRep:buttonburgh:50",
                affinityChange = 5,
                dialogue = "Thank you for your service to Buttonburgh, young one!"
            )
        )
        
        // NPCs react to player killing important enemies
        registerReactionRule(
            NpcReactionRule(
                npcId = "sergeant_talon",
                eventType = WorldEventType.PLAYER_KILLED_ENEMY,
                reactionType = ReactionType.HAPPY,
                affinityChange = 3,
                dialogue = "Well fought! You're becoming quite the warrior."
            )
        )
        
        // NPCs react to player dying
        registerReactionRule(
            NpcReactionRule(
                npcId = "herbalist_sage",
                eventType = WorldEventType.PLAYER_DIED,
                reactionType = ReactionType.FEARFUL,
                dialogue = "You must be more careful! Here, take this healing herb.",
                durationMinutes = 60
            )
        )
        
        // Merchants react to high reputation
        registerReactionRule(
            NpcReactionRule(
                npcId = "clara_seedsworth",
                eventType = WorldEventType.FACTION_STANDING_CHANGED,
                reactionType = ReactionType.OFFER_REWARD,
                conditionType = "FactionRep:buttonburgh:75",
                dialogue = "You're a valued customer! Here's a special discount.",
                durationMinutes = 1440  // 24 hours
            )
        )
    }
}
