package com.jalmarquest.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Player(
    val id: String,
    val name: String,
    @SerialName("choice_log") val choiceLog: ChoiceLog = ChoiceLog(emptyList()),
    @SerialName("quest_log") val questLog: QuestLog = QuestLog(),
    @SerialName("status_effects") val statusEffects: StatusEffects = StatusEffects(emptyList()),
    @SerialName("inventory") val inventory: Inventory = Inventory(emptyList()),
    @SerialName("shiny_collection") val shinyCollection: ShinyCollection = ShinyCollection(emptyList()),
    @SerialName("hoard_rank") val hoardRank: HoardRank = HoardRank(),
    @SerialName("ingredient_inventory") val ingredientInventory: IngredientInventory = IngredientInventory(),
    @SerialName("recipe_book") val recipeBook: RecipeBook = RecipeBook(),
    @SerialName("active_concoctions") val activeConcoctions: ActiveConcoctions = ActiveConcoctions(),
    @SerialName("thought_cabinet") val thoughtCabinet: ThoughtCabinet = ThoughtCabinet(),
    @SerialName("skill_tree") val skillTree: SkillTree = SkillTree(),
    @SerialName("crafting_knowledge") val craftingKnowledge: CraftingKnowledge = CraftingKnowledge(),
    @SerialName("archetype_progress") val archetypeProgress: ArchetypeProgress = ArchetypeProgress()
)

@Serializable
@JvmInline
value class ChoiceTag(val value: String)

@Serializable
data class ChoiceLogEntry(
    val tag: ChoiceTag,
    val timestampMillis: Long
)

@Serializable
data class ChoiceLog(
    val entries: List<ChoiceLogEntry>
)

/**
 * Unique identifier for a quest.
 */
@Serializable
@JvmInline
value class QuestId(val value: String)

/**
 * Status of a quest in the player's quest log.
 */
@Serializable
enum class QuestStatus {
    @SerialName("available")
    AVAILABLE,
    @SerialName("active")
    ACTIVE,
    @SerialName("completed")
    COMPLETED,
    @SerialName("abandoned")
    ABANDONED,
    @SerialName("failed")
    FAILED
}

/**
 * Types of quest objectives that can be tracked.
 */
@Serializable
enum class QuestObjectiveType {
    @SerialName("collect_items")
    COLLECT_ITEMS,
    @SerialName("defeat_enemies")
    DEFEAT_ENEMIES,
    @SerialName("reach_location")
    REACH_LOCATION,
    @SerialName("talk_to_npc")
    TALK_TO_NPC,
    @SerialName("craft_item")
    CRAFT_ITEM,
    @SerialName("discover_lore")
    DISCOVER_LORE,
    @SerialName("make_choice")
    MAKE_CHOICE,
    @SerialName("reach_skill_level")
    REACH_SKILL_LEVEL,
    @SerialName("accumulate_seeds")
    ACCUMULATE_SEEDS,
    @SerialName("internalize_thought")
    INTERNALIZE_THOUGHT,
    @SerialName("complete_quest")
    COMPLETE_QUEST,
    @SerialName("custom")
    CUSTOM
}

/**
 * A single objective within a quest.
 */
@Serializable
data class QuestObjective(
    @SerialName("objective_id")
    val objectiveId: String,
    
    val description: String,
    
    val type: QuestObjectiveType,
    
    @SerialName("target_id")
    val targetId: String? = null,
    
    @SerialName("target_quantity")
    val targetQuantity: Int = 1,
    
    @SerialName("current_progress")
    val currentProgress: Int = 0,
    
    @SerialName("is_optional")
    val isOptional: Boolean = false,
    
    @SerialName("is_hidden")
    val isHidden: Boolean = false,
    
    @SerialName("prerequisite_objectives")
    val prerequisiteObjectives: List<String> = emptyList()
) {
    /**
     * Check if this objective is complete.
     */
    fun isComplete(): Boolean = currentProgress >= targetQuantity
    
    /**
     * Get progress as a percentage (0.0 to 1.0).
     */
    fun progressPercentage(): Double {
        return if (targetQuantity > 0) {
            currentProgress.toDouble() / targetQuantity.toDouble()
        } else {
            1.0
        }
    }
    
    /**
     * Update progress by adding the specified amount.
     */
    fun updateProgress(amount: Int): QuestObjective {
        val newProgress = (currentProgress + amount).coerceIn(0, targetQuantity)
        return copy(currentProgress = newProgress)
    }
}

/**
 * Types of rewards that quests can grant.
 */
@Serializable
enum class QuestRewardType {
    @SerialName("seeds")
    SEEDS,
    @SerialName("items")
    ITEMS,
    @SerialName("experience")
    EXPERIENCE,
    @SerialName("shiny")
    SHINY,
    @SerialName("recipe")
    RECIPE,
    @SerialName("thought")
    THOUGHT,
    @SerialName("ability")
    ABILITY,
    @SerialName("faction_reputation")
    FACTION_REPUTATION,
    @SerialName("archetype_talent_point")
    ARCHETYPE_TALENT_POINT,
    @SerialName("skill_points")
    SKILL_POINTS,
    @SerialName("lore_unlock")
    LORE_UNLOCK,
    @SerialName("companion_affinity")
    COMPANION_AFFINITY,
    @SerialName("cosmetic")
    COSMETIC
}

/**
 * A reward granted upon quest completion.
 */
@Serializable
data class QuestReward(
    val type: QuestRewardType,
    
    @SerialName("target_id")
    val targetId: String? = null,
    
    val quantity: Int = 1,
    
    val description: String
)

/**
 * Requirements that must be met before a quest can be accepted.
 */
@Serializable
sealed class QuestRequirement {
    @Serializable
    @SerialName("prerequisite_quest")
    data class PrerequisiteQuest(
        @SerialName("quest_id")
        val questId: QuestId
    ) : QuestRequirement()
    
    @Serializable
    @SerialName("minimum_level")
    data class MinimumLevel(
        val level: Int
    ) : QuestRequirement()
    
    @Serializable
    @SerialName("minimum_skill")
    data class MinimumSkill(
        @SerialName("skill_type")
        val skillType: SkillType,
        val level: Int
    ) : QuestRequirement()
    
    @Serializable
    @SerialName("minimum_faction_reputation")
    data class MinimumFactionReputation(
        @SerialName("faction_id")
        val factionId: String,
        val reputation: Int
    ) : QuestRequirement()
    
    @Serializable
    @SerialName("archetype_requirement")
    data class ArchetypeRequirement(
        @SerialName("archetype_type")
        val archetypeType: ArchetypeType
    ) : QuestRequirement()
    
    @Serializable
    @SerialName("choice_tag_requirement")
    data class ChoiceTagRequirement(
        val tag: ChoiceTag
    ) : QuestRequirement()
    
    @Serializable
    @SerialName("not_choice_tag_requirement")
    data class NotChoiceTagRequirement(
        val tag: ChoiceTag
    ) : QuestRequirement()
}

/**
 * Definition of a quest with all its properties.
 */
@Serializable
data class Quest(
    @SerialName("quest_id")
    val questId: QuestId,
    
    val title: String,
    
    val description: String,
    
    val objectives: List<QuestObjective>,
    
    val rewards: List<QuestReward>,
    
    val requirements: List<QuestRequirement> = emptyList(),
    
    @SerialName("quest_giver_npc")
    val questGiverNpc: String? = null,
    
    @SerialName("turn_in_npc")
    val turnInNpc: String? = null,
    
    @SerialName("time_limit_millis")
    val timeLimitMillis: Long? = null,
    
    @SerialName("is_repeatable")
    val isRepeatable: Boolean = false,
    
    @SerialName("faction_id")
    val factionId: String? = null,
    
    @SerialName("recommended_level")
    val recommendedLevel: Int? = null,
    
    @SerialName("lore_text")
    val loreText: String? = null
) {
    /**
     * Check if all required objectives are complete.
     */
    fun areRequiredObjectivesComplete(): Boolean {
        return objectives
            .filter { !it.isOptional }
            .all { it.isComplete() }
    }
    
    /**
     * Get overall progress percentage across all objectives.
     */
    fun overallProgress(): Double {
        if (objectives.isEmpty()) return 1.0
        
        val totalProgress = objectives.sumOf { it.progressPercentage() }
        return totalProgress / objectives.size
    }
}

/**
 * Tracks a player's progress on a specific quest.
 */
@Serializable
data class QuestProgress(
    @SerialName("quest_id")
    val questId: QuestId,
    
    val status: QuestStatus,
    
    val objectives: List<QuestObjective>,
    
    @SerialName("accepted_at")
    val acceptedAt: Long,
    
    @SerialName("completed_at")
    val completedAt: Long? = null,
    
    @SerialName("times_completed")
    val timesCompleted: Int = 0
) {
    /**
     * Check if the quest can be turned in.
     */
    fun canTurnIn(): Boolean {
        return status == QuestStatus.ACTIVE &&
               objectives.filter { !it.isOptional }.all { it.isComplete() }
    }
    
    /**
     * Update progress on a specific objective.
     */
    fun updateObjective(objectiveId: String, amount: Int): QuestProgress {
        val updatedObjectives = objectives.map { obj ->
            if (obj.objectiveId == objectiveId) {
                obj.updateProgress(amount)
            } else {
                obj
            }
        }
        return copy(objectives = updatedObjectives)
    }
}

/**
 * Player's quest log containing all quest progress.
 */
@Serializable
data class QuestLog(
    @SerialName("active_quests")
    val activeQuests: List<QuestProgress> = emptyList(),
    
    @SerialName("completed_quests")
    val completedQuests: List<QuestId> = emptyList(),
    
    @SerialName("abandoned_quests")
    val abandonedQuests: List<QuestId> = emptyList(),
    
    @SerialName("failed_quests")
    val failedQuests: List<QuestId> = emptyList()
) {
    /**
     * Check if a quest is active.
     */
    fun isQuestActive(questId: QuestId): Boolean {
        return activeQuests.any { it.questId == questId }
    }
    
    /**
     * Check if a quest is completed.
     */
    fun isQuestCompleted(questId: QuestId): Boolean {
        return completedQuests.contains(questId)
    }
    
    /**
     * Get active quest progress by ID.
     */
    fun getActiveQuest(questId: QuestId): QuestProgress? {
        return activeQuests.find { it.questId == questId }
    }
    
    /**
     * Add a new active quest.
     */
    fun addActiveQuest(progress: QuestProgress): QuestLog {
        return copy(activeQuests = activeQuests + progress)
    }
    
    /**
     * Update an active quest's progress.
     */
    fun updateActiveQuest(questId: QuestId, updater: (QuestProgress) -> QuestProgress): QuestLog {
        val updatedQuests = activeQuests.map { quest ->
            if (quest.questId == questId) {
                updater(quest)
            } else {
                quest
            }
        }
        return copy(activeQuests = updatedQuests)
    }
    
    /**
     * Remove an active quest.
     */
    fun removeActiveQuest(questId: QuestId): QuestLog {
        return copy(activeQuests = activeQuests.filter { it.questId != questId })
    }
    
    /**
     * Mark a quest as completed.
     */
    fun completeQuest(questId: QuestId): QuestLog {
        return removeActiveQuest(questId)
            .copy(completedQuests = completedQuests + questId)
    }
    
    /**
     * Mark a quest as abandoned.
     */
    fun abandonQuest(questId: QuestId): QuestLog {
        return removeActiveQuest(questId)
            .copy(abandonedQuests = abandonedQuests + questId)
    }
    
    /**
     * Mark a quest as failed.
     */
    fun failQuest(questId: QuestId): QuestLog {
        return removeActiveQuest(questId)
            .copy(failedQuests = failedQuests + questId)
    }
}

@Serializable
data class StatusEffect(
    val key: String,
    val expiresAtMillis: Long?
)

@Serializable
data class StatusEffects(
    val entries: List<StatusEffect>
)
