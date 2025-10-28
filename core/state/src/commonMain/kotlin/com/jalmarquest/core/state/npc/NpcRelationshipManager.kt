package com.jalmarquest.core.state.npc

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

/**
 * Manages NPC relationships, gift preferences, and affinity levels.
 */

@Serializable
enum class RelationshipLevel {
    STRANGER,       // 0-20
    ACQUAINTANCE,   // 21-40
    FRIEND,         // 41-60
    CLOSE_FRIEND,   // 61-80
    BEST_FRIEND,    // 81-99
    SOULMATE        // 100
}

@Serializable
data class NpcRelationship(
    val npcId: String,
    val affinity: Int = 0, // 0-100
    val giftsGiven: Int = 0,
    val conversationsHad: Int = 0,
    val questsCompletedFor: List<String> = emptyList(),
    val lastInteractionTimestamp: Long = 0,
    val relationshipMilestones: List<String> = emptyList()
) {
    fun getRelationshipLevel(): RelationshipLevel {
        return when (affinity) {
            in 0..20 -> RelationshipLevel.STRANGER
            in 21..40 -> RelationshipLevel.ACQUAINTANCE
            in 41..60 -> RelationshipLevel.FRIEND
            in 61..80 -> RelationshipLevel.CLOSE_FRIEND
            in 81..99 -> RelationshipLevel.BEST_FRIEND
            else -> RelationshipLevel.SOULMATE
        }
    }
    
    fun canRomance(): Boolean {
        return affinity >= 60
    }
}

@Serializable
enum class GiftCategory {
    FOOD,
    TRINKET,
    BOOK,
    TOOL,
    FLOWER,
    MINERAL,
    ARTIFACT
}

@Serializable
data class GiftPreference(
    val npcId: String,
    val lovedCategories: List<GiftCategory> = emptyList(),
    val likedCategories: List<GiftCategory> = emptyList(),
    val dislikedCategories: List<GiftCategory> = emptyList(),
    val lovedItems: List<String> = emptyList(), // Specific item IDs
    val hatedItems: List<String> = emptyList()
)

enum class GiftReaction {
    LOVED,     // +10 affinity
    LIKED,     // +5 affinity
    NEUTRAL,   // +2 affinity
    DISLIKED,  // +0 affinity
    HATED      // -5 affinity
}

@Serializable
data class NpcRelationships(
    val relationships: Map<String, NpcRelationship> = emptyMap()
) {
    fun getRelationship(npcId: String): NpcRelationship {
        return relationships[npcId] ?: NpcRelationship(npcId)
    }
    
    fun hasRelationship(npcId: String): Boolean {
        return relationships.containsKey(npcId)
    }
}

class NpcRelationshipManager(
    initialRelationships: NpcRelationships = NpcRelationships(),
    private val timestampProvider: () -> Long
) {
    private val mutex = Mutex()
    
    private val _relationships = MutableStateFlow(initialRelationships)
    val relationships: StateFlow<NpcRelationships> = _relationships.asStateFlow()
    
    private val giftPreferences = mutableMapOf<String, GiftPreference>()
    
    init {
        registerDefaultGiftPreferences()
    }
    
    /**
     * Register gift preferences for an NPC.
     */
    fun registerGiftPreference(preference: GiftPreference) {
        giftPreferences[preference.npcId] = preference
    }
    
    /**
     * Get the current affinity level with an NPC.
     */
    fun getAffinity(npcId: String): Int {
        return _relationships.value.getRelationship(npcId).affinity
    }
    
    /**
     * Get the relationship level with an NPC.
     */
    fun getRelationshipLevel(npcId: String): RelationshipLevel {
        return _relationships.value.getRelationship(npcId).getRelationshipLevel()
    }
    
    /**
     * Give a gift to an NPC and receive affinity based on their preferences.
     */
    suspend fun giveGift(npcId: String, itemId: String, itemCategory: GiftCategory): GiftReaction = mutex.withLock {
        val preference = giftPreferences[npcId]
        val reaction = determineGiftReaction(preference, itemId, itemCategory)
        
        val affinityChange = when (reaction) {
            GiftReaction.LOVED -> 10
            GiftReaction.LIKED -> 5
            GiftReaction.NEUTRAL -> 2
            GiftReaction.DISLIKED -> 0
            GiftReaction.HATED -> -5
        }
        
        modifyAffinity(npcId, affinityChange)
        incrementGiftsGiven(npcId)
        
        reaction
    }
    
    /**
     * Have a conversation with an NPC, increasing affinity slightly.
     */
    suspend fun haveConversation(npcId: String, affinityBonus: Int = 1) = mutex.withLock {
        modifyAffinity(npcId, affinityBonus)
        incrementConversations(npcId)
    }
    
    /**
     * Complete a quest for an NPC, significantly increasing affinity.
     */
    suspend fun completeQuestFor(npcId: String, questId: String, affinityBonus: Int = 15) = mutex.withLock {
        modifyAffinity(npcId, affinityBonus)
        
        val current = _relationships.value.getRelationship(npcId)
        val updated = current.copy(
            questsCompletedFor = current.questsCompletedFor + questId
        )
        
        updateRelationship(npcId, updated)
    }
    
    /**
     * Unlock a relationship milestone.
     */
    suspend fun unlockMilestone(npcId: String, milestoneId: String) = mutex.withLock {
        val current = _relationships.value.getRelationship(npcId)
        if (!current.relationshipMilestones.contains(milestoneId)) {
            val updated = current.copy(
                relationshipMilestones = current.relationshipMilestones + milestoneId
            )
            updateRelationship(npcId, updated)
        }
    }
    
    /**
     * Check if a relationship milestone has been unlocked.
     */
    fun hasMilestone(npcId: String, milestoneId: String): Boolean {
        return _relationships.value.getRelationship(npcId)
            .relationshipMilestones.contains(milestoneId)
    }
    
    /**
     * Check if romance is possible with this NPC.
     */
    fun canRomance(npcId: String): Boolean {
        return _relationships.value.getRelationship(npcId).canRomance()
    }
    
    /**
     * Alpha 2.2 Phase 5C: Directly add affinity points to an NPC.
     * Used for special events like Creator Coffee donation rewards.
     * 
     * @param npcId The NPC ID to add affinity to
     * @param amount The amount of affinity to add (can be negative)
     */
    suspend fun addAffinity(npcId: String, amount: Int) = mutex.withLock {
        modifyAffinity(npcId, amount)
    }
    
    private fun determineGiftReaction(
        preference: GiftPreference?,
        itemId: String,
        itemCategory: GiftCategory
    ): GiftReaction {
        if (preference == null) return GiftReaction.NEUTRAL
        
        return when {
            preference.lovedItems.contains(itemId) -> GiftReaction.LOVED
            preference.hatedItems.contains(itemId) -> GiftReaction.HATED
            preference.lovedCategories.contains(itemCategory) -> GiftReaction.LOVED
            preference.dislikedCategories.contains(itemCategory) -> GiftReaction.DISLIKED
            preference.likedCategories.contains(itemCategory) -> GiftReaction.LIKED
            else -> GiftReaction.NEUTRAL
        }
    }
    
    private suspend fun modifyAffinity(npcId: String, change: Int) {
        val current = _relationships.value.getRelationship(npcId)
        val newAffinity = (current.affinity + change).coerceIn(0, 100)
        
        val updated = current.copy(
            affinity = newAffinity,
            lastInteractionTimestamp = timestampProvider()
        )
        
        updateRelationship(npcId, updated)
    }
    
    private suspend fun incrementGiftsGiven(npcId: String) {
        val current = _relationships.value.getRelationship(npcId)
        val updated = current.copy(giftsGiven = current.giftsGiven + 1)
        updateRelationship(npcId, updated)
    }
    
    private suspend fun incrementConversations(npcId: String) {
        val current = _relationships.value.getRelationship(npcId)
        val updated = current.copy(conversationsHad = current.conversationsHad + 1)
        updateRelationship(npcId, updated)
    }
    
    private suspend fun updateRelationship(npcId: String, relationship: NpcRelationship) {
        val currentRelationships = _relationships.value.relationships.toMutableMap()
        currentRelationships[npcId] = relationship
        _relationships.value = NpcRelationships(currentRelationships)
    }
    
    /**
     * Register default gift preferences for NPCs.
     */
    private fun registerDefaultGiftPreferences() {
        // Scholars love books
        registerGiftPreference(GiftPreference(
            npcId = "npc_professor_beakman",
            lovedCategories = listOf(GiftCategory.BOOK, GiftCategory.ARTIFACT),
            likedCategories = listOf(GiftCategory.MINERAL),
            dislikedCategories = listOf(GiftCategory.FOOD)
        ))
        
        registerGiftPreference(GiftPreference(
            npcId = "npc_librarian_hush",
            lovedCategories = listOf(GiftCategory.BOOK),
            likedCategories = listOf(GiftCategory.FLOWER),
            dislikedCategories = listOf(GiftCategory.FOOD, GiftCategory.TOOL)
        ))
        
        // Merchants love trinkets and minerals
        registerGiftPreference(GiftPreference(
            npcId = "npc_clara_seedsworth",
            lovedCategories = listOf(GiftCategory.TRINKET, GiftCategory.MINERAL),
            likedCategories = listOf(GiftCategory.FOOD),
            dislikedCategories = listOf(GiftCategory.BOOK)
        ))
        
        // Crafters love tools
        registerGiftPreference(GiftPreference(
            npcId = "npc_tinker_cogsworth",
            lovedCategories = listOf(GiftCategory.TOOL, GiftCategory.MINERAL),
            likedCategories = listOf(GiftCategory.TRINKET),
            dislikedCategories = listOf(GiftCategory.FLOWER)
        ))
        
        // Gardeners love flowers
        registerGiftPreference(GiftPreference(
            npcId = "npc_gardener_bloom",
            lovedCategories = listOf(GiftCategory.FLOWER),
            likedCategories = listOf(GiftCategory.FOOD, GiftCategory.TOOL),
            dislikedCategories = listOf(GiftCategory.MINERAL)
        ))
        
        // Combat trainers love tools and food
        registerGiftPreference(GiftPreference(
            npcId = "npc_sergeant_talon",
            lovedCategories = listOf(GiftCategory.TOOL),
            likedCategories = listOf(GiftCategory.FOOD),
            dislikedCategories = listOf(GiftCategory.FLOWER, GiftCategory.BOOK)
        ))
        
        // Barkeep loves food
        registerGiftPreference(GiftPreference(
            npcId = "npc_barkeep_dusty",
            lovedCategories = listOf(GiftCategory.FOOD),
            likedCategories = listOf(GiftCategory.TRINKET),
            dislikedCategories = listOf(GiftCategory.BOOK, GiftCategory.TOOL)
        ))
        
        // Wandering Minstrel loves trinkets and books
        registerGiftPreference(GiftPreference(
            npcId = "npc_wandering_minstrel",
            lovedCategories = listOf(GiftCategory.TRINKET, GiftCategory.BOOK),
            likedCategories = listOf(GiftCategory.FLOWER),
            dislikedCategories = listOf(GiftCategory.TOOL)
        ))
        
        // Archaeologist loves artifacts
        registerGiftPreference(GiftPreference(
            npcId = "npc_archaeologist",
            lovedCategories = listOf(GiftCategory.ARTIFACT, GiftCategory.BOOK),
            likedCategories = listOf(GiftCategory.MINERAL, GiftCategory.TRINKET),
            dislikedCategories = listOf(GiftCategory.FOOD)
        ))
        
        // Fisher loves food
        registerGiftPreference(GiftPreference(
            npcId = "npc_brook_fisher",
            lovedCategories = listOf(GiftCategory.FOOD, GiftCategory.TOOL),
            likedCategories = listOf(GiftCategory.TRINKET),
            dislikedCategories = listOf(GiftCategory.BOOK, GiftCategory.FLOWER)
        ))
    }
}
