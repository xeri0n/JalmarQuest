package com.jalmarquest.core.state.dialogue

import com.jalmarquest.core.model.Player
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.ai.NpcReactionManager
import com.jalmarquest.core.state.ai.ReactionType
import com.jalmarquest.core.state.catalogs.NpcCatalog
import com.jalmarquest.core.state.factions.FactionManager
import com.jalmarquest.core.state.npc.NpcRelationshipManager
import com.jalmarquest.core.state.npc.RelationshipLevel
import com.jalmarquest.core.state.quests.QuestManager
import com.jalmarquest.core.state.time.InGameTimeManager
import com.jalmarquest.core.state.time.TimeOfDay
import kotlinx.serialization.Serializable

/**
 * Phase 3: Enhanced dialogue system with context-aware dialogue generation.
 * Generates dynamic dialogue based on time, weather, quest progress,
 * relationship level, and recent world events.
 */

/**
 * Context factors that influence dialogue generation.
 */
@Serializable
data class DialogueContext(
    val timeOfDay: TimeOfDay,
    val weatherCondition: WeatherCondition = WeatherCondition.CLEAR,
    val relationshipLevel: RelationshipLevel,
    val affinity: Int,
    val activeQuests: Set<String> = emptySet(),
    val recentQuestsCompleted: List<String> = emptyList(),
    val recentEvents: List<String> = emptyList(),  // Recent world events
    val playerLocation: String? = null,
    val factionReputation: Map<String, Int> = emptyMap(),
    val daysKnown: Int = 0,  // How long player has known this NPC
    val lastConversationTime: Long? = null,  // Timestamp of last conversation
    val timeSinceLastConversation: Long? = null  // Milliseconds since last talk
)

/**
 * Weather conditions (simplified for now).
 */
enum class WeatherCondition {
    CLEAR,
    RAINY,
    STORMY,
    FOGGY,
    HOT,
    COLD
}

/**
 * Dynamic dialogue line with context requirements.
 */
@Serializable
data class ContextualDialogueLine(
    val id: String,
    val npcId: String,
    val text: String,
    val priority: Int = 0,  // Higher priority = more likely to be used
    val contextRequirementTypes: List<String> = emptyList(),  // Simplified for serialization
    val category: DialogueCategory = DialogueCategory.GENERIC
)

/**
 * Categories of dialogue.
 */
enum class DialogueCategory {
    GREETING,           // Opening lines
    FAREWELL,           // Closing lines
    SMALL_TALK,         // Casual conversation
    QUEST_RELATED,      // About quests
    LORE,               // World/story information
    REACTION,           // Reaction to player/event
    TRADE,              // Commerce-related
    LOCATION_COMMENT,   // Comment about current location
    TIME_COMMENT,       // Comment about time of day
    WEATHER_COMMENT,    // Comment about weather
    GENERIC             // Default fallback
}

/**
 * Requirements for contextual dialogue to be available.
 */
sealed class ContextRequirement {
    /**
     * Require specific time of day.
     */
    @Serializable
    data class TimeOfDayIs(val times: List<TimeOfDay>) : ContextRequirement()
    
    /**
     * Require specific weather.
     */
    @Serializable
    data class WeatherIs(val weather: WeatherCondition) : ContextRequirement()
    
    /**
     * Require minimum affinity.
     */
    @Serializable
    data class AffinityAtLeast(val minAffinity: Int) : ContextRequirement()
    
    /**
     * Require specific relationship level.
     */
    @Serializable
    data class RelationshipIs(val level: RelationshipLevel) : ContextRequirement()
    
    /**
     * Require active quest.
     */
    @Serializable
    data class QuestActive(val questId: String) : ContextRequirement()
    
    /**
     * Require recently completed quest.
     */
    @Serializable
    data class QuestRecentlyCompleted(val questId: String) : ContextRequirement()
    
    /**
     * Require player at location.
     */
    @Serializable
    data class AtLocation(val locationId: String) : ContextRequirement()
    
    /**
     * Require faction reputation threshold.
     */
    @Serializable
    data class FactionRep(val factionId: String, val minRep: Int) : ContextRequirement()
    
    /**
     * Require minimum days since first meeting.
     */
    @Serializable
    data class DaysKnown(val minDays: Int) : ContextRequirement()
    
    /**
     * Require this is first conversation of the day.
     */
    @Serializable
    data object FirstConversationToday : ContextRequirement()
    
    /**
     * Require this is first time meeting NPC.
     */
    @Serializable
    data object FirstTimeMeeting : ContextRequirement()
}

/**
 * Enhanced dialogue manager with context-aware generation.
 */
class DynamicDialogueManager(
    private val baseDialogueManager: DialogueManager,
    private val npcCatalog: NpcCatalog,
    private val relationshipManager: NpcRelationshipManager,
    private val reactionManager: NpcReactionManager,
    private val questManager: QuestManager,
    private val factionManager: FactionManager,
    private val timeManager: InGameTimeManager,
    private val gameStateManager: GameStateManager,
    private val timestampProvider: () -> Long
) {
    private val contextualDialogue = mutableMapOf<String, MutableList<ContextualDialogueLine>>()
    private val lastConversationTimes = mutableMapOf<String, Long>()
    
    init {
        registerDefaultContextualDialogue()
    }
    
    /**
     * Register a contextual dialogue line.
     */
    fun registerContextualDialogue(line: ContextualDialogueLine) {
        contextualDialogue.getOrPut(line.npcId) { mutableListOf() }.add(line)
    }
    
    /**
     * Build dialogue context for an NPC conversation.
     */
    fun buildContext(npcId: String): DialogueContext {
        val player = gameStateManager.playerState.value
        val allRelationships = relationshipManager.relationships.value
        val relationship = allRelationships.relationships[npcId]
        val currentTime = timestampProvider()
        val lastConversation = lastConversationTimes[npcId]
        
        // Get active quests (simplified - would need full quest integration)
        val activeQuests = emptySet<String>()
        
        // Get recently completed quests (simplified)
        val recentQuests = emptyList<String>()
        
        // Get recent events involving this NPC
        val recentEvents = reactionManager.worldEvents.value
            .filter { 
                it.sourceNpcId == npcId || it.targetNpcId == npcId 
            }
            .takeLast(5)
            .map { it.type.name }
        
        // Calculate days known (simplified - would use real date tracking)
        val daysKnown = 0  // Simplified
        
        return DialogueContext(
            timeOfDay = timeManager.currentTime.value.getTimeOfDay(),
            relationshipLevel = relationship?.getRelationshipLevel() ?: RelationshipLevel.STRANGER,
            affinity = relationship?.affinity ?: 0,
            activeQuests = activeQuests,
            recentQuestsCompleted = recentQuests,
            recentEvents = recentEvents,
            factionReputation = mapOf(
                "buttonburgh" to factionManager.getReputation("faction_buttonburgh").reputation
            ),
            daysKnown = daysKnown,
            lastConversationTime = lastConversation,
            timeSinceLastConversation = lastConversation?.let { currentTime - it }
        )
    }
    
    /**
     * Get a dynamic greeting based on context.
     */
    fun getDynamicGreeting(npcId: String): String {
        val context = buildContext(npcId)
        val lines = getMatchingDialogue(npcId, DialogueCategory.GREETING, context)
        
        // Update last conversation time
        lastConversationTimes[npcId] = timestampProvider()
        
        return lines.firstOrNull()?.text ?: getDefaultGreeting(npcId, context)
    }
    
    /**
     * Get a dynamic farewell based on context.
     */
    fun getDynamicFarewell(npcId: String): String {
        val context = buildContext(npcId)
        val lines = getMatchingDialogue(npcId, DialogueCategory.FAREWELL, context)
        return lines.firstOrNull()?.text ?: getDefaultFarewell(npcId, context)
    }
    
    /**
     * Get dialogue that matches context and category.
     * Simplified version without full requirement checking.
     */
    private fun getMatchingDialogue(
        npcId: String,
        category: DialogueCategory,
        context: DialogueContext
    ): List<ContextualDialogueLine> {
        val npcLines = contextualDialogue[npcId] ?: emptyList()
        
        return npcLines
            .filter { it.category == category }
            .sortedByDescending { it.priority }
    }
    
    /**
     * Check if a context requirement is met.
     * Simplified version for Phase 3.
     */
    private fun checkContextRequirement(
        requirement: ContextRequirement,
        context: DialogueContext
    ): Boolean {
        // Simplified - would fully implement all checks
        return true
    }
    
    /**
     * Generate a default greeting based on relationship and time.
     */
    private fun getDefaultGreeting(npcId: String, context: DialogueContext): String {
        val npc = npcCatalog.getNpcById(npcId)
        val name = npc?.name ?: "stranger"
        
        // First time meeting
        if (context.lastConversationTime == null) {
            return "Hello there! I don't believe we've met. I'm $name."
        }
        
        // Time-based greetings
        val timeGreeting = when (context.timeOfDay) {
            TimeOfDay.DAWN -> "Good morning"
            TimeOfDay.MORNING -> "Hello"
            TimeOfDay.AFTERNOON -> "Good afternoon"
            TimeOfDay.DUSK -> "Good evening"
            TimeOfDay.NIGHT -> "Evening"
        }
        
        // Relationship-based greetings
        return when (context.relationshipLevel) {
            RelationshipLevel.STRANGER -> "$timeGreeting. Can I help you?"
            RelationshipLevel.ACQUAINTANCE -> "$timeGreeting! How can I assist you?"
            RelationshipLevel.FRIEND -> "$timeGreeting, friend!"
            RelationshipLevel.CLOSE_FRIEND -> "$timeGreeting! Great to see you!"
            RelationshipLevel.BEST_FRIEND -> "$timeGreeting, dear friend! Always a pleasure!"
            RelationshipLevel.SOULMATE -> "$timeGreeting, my dearest! I was hoping to see you today!"
        }
    }
    
    /**
     * Generate a default farewell based on relationship.
     */
    private fun getDefaultFarewell(npcId: String, context: DialogueContext): String {
        return when (context.relationshipLevel) {
            RelationshipLevel.STRANGER -> "Goodbye."
            RelationshipLevel.ACQUAINTANCE -> "Take care!"
            RelationshipLevel.FRIEND -> "See you around!"
            RelationshipLevel.CLOSE_FRIEND -> "Come visit again soon!"
            RelationshipLevel.BEST_FRIEND -> "Until next time, friend!"
            RelationshipLevel.SOULMATE -> "I'll be thinking of you. Come back soon!"
        }
    }
    
    /**
     * Get dialogue that reacts to recent events.
     */
    fun getEventReactionDialogue(npcId: String): String? {
        val reaction = reactionManager.getNextReaction(npcId)
        
        if (reaction != null && reaction.dialogue != null) {
            // Mark reaction as triggered
            reactionManager.markReactionTriggered(npcId, reaction.eventId)
            
            // Add context based on reaction type
            val prefix = when (reaction.reactionType) {
                ReactionType.HAPPY -> "I'm so glad "
                ReactionType.SAD -> "I'm saddened that "
                ReactionType.ANGRY -> "I'm furious that "
                ReactionType.FEARFUL -> "I'm worried that "
                ReactionType.GRATEFUL -> "Thank you for "
                ReactionType.DISAPPOINTED -> "I'm disappointed that "
                else -> ""
            }
            
            return if (prefix.isNotEmpty()) {
                prefix + reaction.dialogue?.replaceFirstChar { it.lowercase() }
            } else {
                reaction.dialogue
            }
        }
        
        return null
    }
    
    /**
     * Register default contextual dialogue for common NPCs.
     */
    private fun registerDefaultContextualDialogue() {
        // Example: Elder Quill time-based greetings
        registerContextualDialogue(
            ContextualDialogueLine(
                id = "elder_quill_dawn",
                npcId = "elder_quill",
                text = "Ah, you're up early! The dawn is the best time for reflection, young one.",
                priority = 10,
                contextRequirementTypes = listOf("TimeOfDay:DAWN"),
                category = DialogueCategory.GREETING
            )
        )
        
        registerContextualDialogue(
            ContextualDialogueLine(
                id = "elder_quill_night",
                npcId = "elder_quill",
                text = "Out and about at this hour? Be careful, the night holds many mysteries.",
                priority = 10,
                contextRequirementTypes = listOf("TimeOfDay:NIGHT"),
                category = DialogueCategory.GREETING
            )
        )
        
        // Example: Merchant based on reputation
        registerContextualDialogue(
            ContextualDialogueLine(
                id = "clara_high_rep",
                npcId = "clara_seedsworth",
                text = "Welcome back, valued customer! You've been such a help to Buttonburgh!",
                priority = 15,
                contextRequirementTypes = listOf("FactionRep:75"),
                category = DialogueCategory.GREETING
            )
        )
        
        // Example: Best friend greeting
        registerContextualDialogue(
            ContextualDialogueLine(
                id = "generic_best_friend",
                npcId = "*",  // Wildcard for any NPC
                text = "There you are! I've been thinking about you!",
                priority = 12,
                contextRequirementTypes = listOf("Relationship:BEST_FRIEND", "FirstToday"),
                category = DialogueCategory.GREETING
            )
        )
    }
}
