package com.jalmarquest.ui.app.utils

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.content.QuailPerspectiveRefactor

/**
 * Centralized text display utility that ensures all game content
 * shows user-friendly, localized names instead of internal IDs.
 */
object TextDisplay {
    
    /**
     * Get display name for any game entity, with fallback to ID if not found
     */
    fun getDisplayName(entity: Any): String {
        return when (entity) {
            is ItemId -> getItemDisplayName(entity)
            is QuestId -> getQuestDisplayName(entity)
            is LocationId -> getLocationDisplayName(entity)
            is NpcId -> getNpcDisplayName(entity)
            is EnemyId -> getEnemyDisplayName(entity)
            is RecipeId -> getRecipeDisplayName(entity)
            is ThoughtId -> getThoughtDisplayName(entity)
            is SkillType -> getSkillDisplayName(entity)
            is ConcoctionTemplate -> entity.name
            is Equipment -> entity.name
            is ShopItem -> entity.name
            else -> entity.toString()
        }
    }
    
    private fun getItemDisplayName(itemId: ItemId): String {
        // First check for quail-perspective rename
        val baseName = itemId.value.replace("_", " ")
            .split(" ")
            .joinToString(" ") { it.lowercase().capitalize() }
        
        return QuailPerspectiveRefactor.itemRenames[baseName] ?: baseName
    }
    
    private fun getQuestDisplayName(questId: QuestId): String {
        // Look up quest in catalog and get title
        val baseName = questId.value.replace("quest_", "")
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") { it.lowercase().capitalize() }
        
        return QuailPerspectiveRefactor.questRenames[baseName] ?: baseName
    }
    
    private fun getLocationDisplayName(locationId: LocationId): String {
        val baseName = locationId.value.replace("_", " ")
            .split(" ")
            .joinToString(" ") { it.lowercase().capitalize() }
        
        return QuailPerspectiveRefactor.locationRenames[baseName] ?: baseName
    }
    
    private fun getNpcDisplayName(npcId: NpcId): String {
        val baseName = npcId.value.replace("npc_", "")
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") { it.lowercase().capitalize() }
        
        return QuailPerspectiveRefactor.npcRenames[baseName] ?: baseName
    }
    
    private fun getEnemyDisplayName(enemyId: EnemyId): String {
        val baseName = enemyId.value.replace("enemy_", "")
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") { it.lowercase().capitalize() }
        
        return QuailPerspectiveRefactor.enemyRenames[baseName] ?: baseName
    }
    
    private fun getRecipeDisplayName(recipeId: RecipeId): String {
        return recipeId.value.replace("recipe_", "")
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") { it.lowercase().capitalize() }
    }
    
    private fun getThoughtDisplayName(thoughtId: ThoughtId): String {
        val baseName = thoughtId.value.replace("thought_", "")
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") { it.lowercase().capitalize() }
        
        return QuailPerspectiveRefactor.thoughtRenames[baseName] ?: baseName
    }
    
    private fun getSkillDisplayName(skillType: SkillType): String {
        val baseName = skillType.name.replace("_", " ")
            .split(" ")
            .joinToString(" ") { it.lowercase().capitalize() }
        
        return QuailPerspectiveRefactor.skillRenames[baseName] ?: baseName
    }
    
    /**
     * Format a number as seeds (game currency)
     */
    fun formatSeeds(amount: Int): String {
        return when {
            amount >= 1_000_000 -> "${amount / 1_000_000}M seeds"
            amount >= 1_000 -> "${amount / 1_000}k seeds"
            else -> "$amount seeds"
        }
    }
    
    /**
     * Format time remaining in human-readable format
     */
    fun formatTimeRemaining(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> "$days day${if (days > 1) "s" else ""}"
            hours > 0 -> "$hours hour${if (hours > 1) "s" else ""}"
            minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""}"
            else -> "$seconds second${if (seconds > 1) "s" else ""}"
        }
    }
}
