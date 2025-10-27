package com.jalmarquest.core.state.dialogue

import kotlinx.serialization.Serializable

/**
 * Basic dialogue tree system for NPC conversations.
 * Supports branching dialogues, choice consequences, and state-based responses.
 */

@Serializable
data class DialogueNode(
    val id: String,
    val npcId: String,
    val text: String,
    val choices: List<DialogueChoice> = emptyList(),
    val requirements: List<DialogueRequirement> = emptyList(),
    val consequences: List<DialogueConsequence> = emptyList(),
    val isEnding: Boolean = false
)

@Serializable
data class DialogueChoice(
    val id: String,
    val text: String,
    val nextNodeId: String?,
    val choiceTag: String? = null, // For analytics/quest tracking
    val requirements: List<DialogueRequirement> = emptyList()
) {
    /**
     * Check if this choice is available based on requirements.
     */
    fun isAvailable(state: DialogueState): Boolean {
        return requirements.all { it.isMet(state) }
    }
}

@Serializable
sealed class DialogueRequirement {
    abstract fun isMet(state: DialogueState): Boolean
    
    @Serializable
    data class MinimumAffinity(val npcId: String, val required: Int) : DialogueRequirement() {
        override fun isMet(state: DialogueState): Boolean {
            return state.npcAffinity >= required
        }
    }
    
    @Serializable
    data class HasItem(val itemId: String, val quantity: Int = 1) : DialogueRequirement() {
        override fun isMet(state: DialogueState): Boolean {
            return (state.playerItems[itemId] ?: 0) >= quantity
        }
    }
    
    @Serializable
    data class CompletedQuest(val questId: String) : DialogueRequirement() {
        override fun isMet(state: DialogueState): Boolean {
            return state.completedQuests.contains(questId)
        }
    }
    
    @Serializable
    data class HasChoiceTag(val tag: String) : DialogueRequirement() {
        override fun isMet(state: DialogueState): Boolean {
            return state.choiceTags.contains(tag)
        }
    }
    
    @Serializable
    data class MinimumLevel(val level: Int) : DialogueRequirement() {
        override fun isMet(state: DialogueState): Boolean {
            return state.playerLevel >= level
        }
    }
    
    @Serializable
    data class TimeOfDay(val requiredTime: com.jalmarquest.core.state.time.TimeOfDay) : DialogueRequirement() {
        override fun isMet(state: DialogueState): Boolean {
            return state.currentTimeOfDay == requiredTime
        }
    }
}

@Serializable
sealed class DialogueConsequence {
    @Serializable
    data class GiveItem(val itemId: String, val quantity: Int = 1) : DialogueConsequence()
    
    @Serializable
    data class TakeItem(val itemId: String, val quantity: Int = 1) : DialogueConsequence()
    
    @Serializable
    data class ModifyAffinity(val npcId: String, val change: Int) : DialogueConsequence()
    
    @Serializable
    data class StartQuest(val questId: String) : DialogueConsequence()
    
    @Serializable
    data class CompleteQuest(val questId: String) : DialogueConsequence()
    
    @Serializable
    data class AddChoiceTag(val tag: String) : DialogueConsequence()
    
    @Serializable
    data class GiveSeeds(val amount: Int) : DialogueConsequence()
    
    @Serializable
    data class UnlockLocation(val locationId: String) : DialogueConsequence()
}

/**
 * State information needed to evaluate dialogue requirements.
 */
data class DialogueState(
    val npcAffinity: Int = 0,
    val playerItems: Map<String, Int> = emptyMap(),
    val completedQuests: Set<String> = emptySet(),
    val choiceTags: Set<String> = emptySet(),
    val playerLevel: Int = 1,
    val currentTimeOfDay: com.jalmarquest.core.state.time.TimeOfDay = com.jalmarquest.core.state.time.TimeOfDay.MORNING
)

@Serializable
data class DialogueTree(
    val id: String,
    val npcId: String,
    val rootNodeId: String,
    val nodes: Map<String, DialogueNode>
) {
    /**
     * Get the starting dialogue node.
     */
    fun getRootNode(): DialogueNode? = nodes[rootNodeId]
    
    /**
     * Get a specific dialogue node by ID.
     */
    fun getNode(nodeId: String): DialogueNode? = nodes[nodeId]
    
    /**
     * Get available choices from a node based on dialogue state.
     */
    fun getAvailableChoices(nodeId: String, state: DialogueState): List<DialogueChoice> {
        val node = nodes[nodeId] ?: return emptyList()
        return node.choices.filter { it.isAvailable(state) }
    }
}

class DialogueManager {
    private val dialogueTrees = mutableMapOf<String, DialogueTree>()
    
    /**
     * Register a dialogue tree.
     */
    fun registerDialogueTree(tree: DialogueTree) {
        dialogueTrees[tree.id] = tree
    }
    
    /**
     * Get a dialogue tree by ID.
     */
    fun getDialogueTree(treeId: String): DialogueTree? = dialogueTrees[treeId]
    
    /**
     * Get a dialogue tree for a specific NPC.
     */
    fun getDialogueTreeForNpc(npcId: String): DialogueTree? {
        return dialogueTrees.values.firstOrNull { it.npcId == npcId }
    }
    
    /**
     * Start a dialogue with an NPC.
     */
    fun startDialogue(npcId: String, state: DialogueState): DialogueNode? {
        val tree = getDialogueTreeForNpc(npcId) ?: return null
        val rootNode = tree.getRootNode() ?: return null
        
        // Check if root node requirements are met
        if (rootNode.requirements.all { it.isMet(state) }) {
            return rootNode
        }
        
        return null
    }
    
    /**
     * Process a dialogue choice and get the next node.
     */
    fun processChoice(
        treeId: String,
        currentNodeId: String,
        choiceId: String,
        state: DialogueState
    ): DialogueNode? {
        val tree = dialogueTrees[treeId] ?: return null
        val currentNode = tree.getNode(currentNodeId) ?: return null
        val choice = currentNode.choices.firstOrNull { it.id == choiceId } ?: return null
        
        // Check if choice is available
        if (!choice.isAvailable(state)) return null
        
        // Get next node
        val nextNodeId = choice.nextNodeId ?: return null
        return tree.getNode(nextNodeId)
    }
    
    /**
     * Create a simple greeting dialogue for an NPC.
     */
    fun createSimpleGreeting(npcId: String, greetingText: String, farewellText: String): DialogueTree {
        val rootNode = DialogueNode(
            id = "greeting",
            npcId = npcId,
            text = greetingText,
            choices = listOf(
                DialogueChoice(
                    id = "continue",
                    text = "Tell me more.",
                    nextNodeId = "farewell"
                ),
                DialogueChoice(
                    id = "goodbye",
                    text = "Goodbye.",
                    nextNodeId = "farewell"
                )
            )
        )
        
        val farewellNode = DialogueNode(
            id = "farewell",
            npcId = npcId,
            text = farewellText,
            isEnding = true
        )
        
        return DialogueTree(
            id = "dialogue_${npcId}_greeting",
            npcId = npcId,
            rootNodeId = "greeting",
            nodes = mapOf(
                "greeting" to rootNode,
                "farewell" to farewellNode
            )
        )
    }
}
