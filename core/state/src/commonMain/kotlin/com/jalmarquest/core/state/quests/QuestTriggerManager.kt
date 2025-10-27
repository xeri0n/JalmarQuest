package com.jalmarquest.core.state.quests

import com.jalmarquest.core.model.Quest
import kotlinx.serialization.Serializable

/**
 * Quest triggers that can be discovered by exploring locations or talking to NPCs.
 * Integrates with LocationCatalog, NpcCatalog, and QuestManager.
 */

@Serializable
enum class QuestTriggerType {
    NPC_DIALOGUE,        // Talk to an NPC to start quest
    LOCATION_DISCOVERY,  // Enter a location to trigger quest
    ITEM_PICKUP,         // Pick up an item that starts a quest
    ENEMY_DEFEAT,        // Defeat a specific enemy
    TIME_BASED,          // Quest becomes available at certain time
    EVENT_BASED          // Triggered by game events
}

@Serializable
data class QuestTrigger(
    val questId: String,
    val triggerType: QuestTriggerType,
    val triggerId: String, // NPC ID, Location ID, Item ID, etc.
    val requirements: List<QuestTriggerRequirement> = emptyList(),
    val description: String? = null,
    val autoStart: Boolean = false // If true, quest starts automatically when triggered
)

@Serializable
sealed class QuestTriggerRequirement {
    @Serializable
    data class MinimumLevel(val level: Int) : QuestTriggerRequirement()
    
    @Serializable
    data class CompletedQuest(val questId: String) : QuestTriggerRequirement()
    
    @Serializable
    data class HasItem(val itemId: String, val quantity: Int = 1) : QuestTriggerRequirement()
    
    @Serializable
    data class MinimumAffinity(val npcId: String, val affinity: Int) : QuestTriggerRequirement()
    
    @Serializable
    data class ChoiceTag(val tag: String) : QuestTriggerRequirement()
    
    @Serializable
    data class TimeOfDay(val timeOfDay: com.jalmarquest.core.state.time.TimeOfDay) : QuestTriggerRequirement()
}

class QuestTriggerManager {
    private val triggers = mutableListOf<QuestTrigger>()
    
    init {
        registerDefaultTriggers()
    }
    
    /**
     * Register a quest trigger.
     */
    fun registerTrigger(trigger: QuestTrigger) {
        triggers.add(trigger)
    }
    
    /**
     * Get all triggers for a specific location.
     */
    fun getTriggersForLocation(locationId: String): List<QuestTrigger> {
        return triggers.filter { 
            it.triggerType == QuestTriggerType.LOCATION_DISCOVERY && 
            it.triggerId == locationId 
        }
    }
    
    /**
     * Get all triggers for a specific NPC.
     */
    fun getTriggersForNpc(npcId: String): List<QuestTrigger> {
        return triggers.filter { 
            it.triggerType == QuestTriggerType.NPC_DIALOGUE && 
            it.triggerId == npcId 
        }
    }
    
    /**
     * Get all triggers for a specific enemy.
     */
    fun getTriggersForEnemy(enemyId: String): List<QuestTrigger> {
        return triggers.filter { 
            it.triggerType == QuestTriggerType.ENEMY_DEFEAT && 
            it.triggerId == enemyId 
        }
    }
    
    /**
     * Check if a trigger's requirements are met.
     */
    fun isTriggerAvailable(
        trigger: QuestTrigger,
        playerLevel: Int,
        completedQuests: Set<String>,
        playerItems: Map<String, Int>,
        npcAffinities: Map<String, Int>,
        choiceTags: Set<String>,
        currentTime: com.jalmarquest.core.state.time.TimeOfDay
    ): Boolean {
        return trigger.requirements.all { requirement ->
            when (requirement) {
                is QuestTriggerRequirement.MinimumLevel -> 
                    playerLevel >= requirement.level
                is QuestTriggerRequirement.CompletedQuest -> 
                    completedQuests.contains(requirement.questId)
                is QuestTriggerRequirement.HasItem -> 
                    (playerItems[requirement.itemId] ?: 0) >= requirement.quantity
                is QuestTriggerRequirement.MinimumAffinity -> 
                    (npcAffinities[requirement.npcId] ?: 0) >= requirement.affinity
                is QuestTriggerRequirement.ChoiceTag -> 
                    choiceTags.contains(requirement.tag)
                is QuestTriggerRequirement.TimeOfDay -> 
                    currentTime == requirement.timeOfDay
            }
        }
    }
    
    /**
     * Register default quest triggers throughout the world.
     */
    private fun registerDefaultTriggers() {
        // ========== BUTTONBURGH QUEST TRIGGERS ==========
        
        // Elder Quill - Tutorial quest giver
        registerTrigger(QuestTrigger(
            questId = "quest_first_flight",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_elder_quill",
            description = "Elder Quill teaches new quails the basics",
            autoStart = false
        ))
        
        // Professor Tessel - Giga-Seed quest
        registerTrigger(QuestTrigger(
            questId = "quest_giga_seed",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_professor_tessel",
            description = "Professor Tessel seeks the legendary Giga-Seed"
        ))
        
        // Artist Pip - High Perch quest
        registerTrigger(QuestTrigger(
            questId = "quest_high_perch",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_artist_pip",
            description = "Artist Pip needs a Sunpetal Flower from high places"
        ))
        
        // Herbalist Hoot - Night Forager quest
        registerTrigger(QuestTrigger(
            questId = "quest_night_forager",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_herbalist_hoot",
            requirements = listOf(QuestTriggerRequirement.TimeOfDay(com.jalmarquest.core.state.time.TimeOfDay.NIGHT)),
            description = "Herbalist Hoot needs Moondew Fern, only visible at night"
        ))
        
        // Professor Click - Beetle collection quest
        registerTrigger(QuestTrigger(
            questId = "quest_beetle_brouhaha",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_professor_click",
            description = "Professor Click collects rare beetles"
        ))
        
        // Worried Wicker - Ant diplomacy quest
        registerTrigger(QuestTrigger(
            questId = "quest_antbassador",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_worried_wicker",
            description = "The ants are taking seeds from Wicker's shop"
        ))
        
        // Town Crier - Daily bulletin board quests
        registerTrigger(QuestTrigger(
            questId = "quest_daily_bulletin",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_town_crier",
            description = "Check the bulletin board for daily tasks"
        ))
        
        // Gardener Bloom - Garden help quest
        registerTrigger(QuestTrigger(
            questId = "quest_garden_helper",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_gardener_bloom",
            description = "Help tend the garden terraces"
        ))
        
        // Sergeant Talon - Combat training quest
        registerTrigger(QuestTrigger(
            questId = "quest_combat_training",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_sergeant_talon",
            description = "Learn combat basics from the sergeant"
        ))
        
        // ========== FOREST QUEST TRIGGERS ==========
        
        // Discovering the Poisoned Grove
        registerTrigger(QuestTrigger(
            questId = "quest_poisoned_grove",
            triggerType = QuestTriggerType.LOCATION_DISCOVERY,
            triggerId = "poisoned_grove",
            requirements = listOf(QuestTriggerRequirement.MinimumLevel(3)),
            description = "The grove is sick. Someone must investigate.",
            autoStart = true
        ))
        
        // Mushroom Sage - Fungal wisdom quest
        registerTrigger(QuestTrigger(
            questId = "quest_fungal_wisdom",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_mushroom_sage",
            requirements = listOf(QuestTriggerRequirement.MinimumLevel(2)),
            description = "Learn the secrets of the mushrooms"
        ))
        
        // Territorial Crow - Berry patch quest
        registerTrigger(QuestTrigger(
            questId = "quest_territorial_crow",
            triggerType = QuestTriggerType.LOCATION_DISCOVERY,
            triggerId = "crows_perch",
            description = "A crow guards valuable berry patches"
        ))
        
        // Arachna the Spider - Web navigation quest
        registerTrigger(QuestTrigger(
            questId = "quest_web_walker",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_web_weaver",
            requirements = listOf(QuestTriggerRequirement.MinimumAffinity("npc_web_weaver", 20)),
            description = "Arachna teaches how to navigate her webs"
        ))
        
        // Canopy Scout - Heights exploration
        registerTrigger(QuestTrigger(
            questId = "quest_canopy_explorer",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_canopy_scout",
            requirements = listOf(QuestTriggerRequirement.MinimumLevel(4)),
            description = "Scale the canopy heights for rare treasures"
        ))
        
        // ========== BEACH QUEST TRIGGERS ==========
        
        // Shell Collector Sandy - Shell hunting quest
        registerTrigger(QuestTrigger(
            questId = "quest_shell_collection",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_shell_collector",
            description = "Collect rare shells from the beach"
        ))
        
        // Old Sailor Barnacle - Sea stories quest
        registerTrigger(QuestTrigger(
            questId = "quest_sailors_tales",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_old_sailor",
            requirements = listOf(QuestTriggerRequirement.MinimumAffinity("npc_old_sailor", 30)),
            description = "Hear the sailor's tales of the sea"
        ))
        
        // Shipwreck discovery
        registerTrigger(QuestTrigger(
            questId = "quest_shipwreck_salvage",
            triggerType = QuestTriggerType.LOCATION_DISCOVERY,
            triggerId = "beach_shipwreck",
            requirements = listOf(QuestTriggerRequirement.MinimumLevel(5)),
            description = "Explore the ancient shipwreck for treasures",
            autoStart = true
        ))
        
        // Lighthouse Keeper - Light the way quest
        registerTrigger(QuestTrigger(
            questId = "quest_lighthouse_keeper",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_lighthouse_keeper",
            description = "Help maintain the lighthouse beacon"
        ))
        
        // Sandpiper Chief - Bird diplomacy
        registerTrigger(QuestTrigger(
            questId = "quest_sandpiper_alliance",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_sandpiper_chief",
            requirements = listOf(QuestTriggerRequirement.CompletedQuest("quest_antbassador")),
            description = "Form an alliance with the sandpipers"
        ))
        
        // ========== SWAMP QUEST TRIGGERS ==========
        
        // Bog Witch - Potion mastery quest
        registerTrigger(QuestTrigger(
            questId = "quest_bog_witch_apprentice",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_bog_witch",
            requirements = listOf(QuestTriggerRequirement.MinimumLevel(6)),
            description = "Become the Bog Witch's apprentice"
        ))
        
        // Firefly Queen - Bioluminescent mystery
        registerTrigger(QuestTrigger(
            questId = "quest_firefly_dance",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_firefly_queen",
            requirements = listOf(
                QuestTriggerRequirement.TimeOfDay(com.jalmarquest.core.state.time.TimeOfDay.DUSK)
            ),
            description = "Learn the fireflies' ancient dance"
        ))
        
        // Gator's Den discovery
        registerTrigger(QuestTrigger(
            questId = "quest_gator_challenge",
            triggerType = QuestTriggerType.LOCATION_DISCOVERY,
            triggerId = "swamp_gator_den",
            requirements = listOf(QuestTriggerRequirement.MinimumLevel(10)),
            description = "Face the Ancient Alligator boss",
            autoStart = false
        ))
        
        // Hermit Mudfoot - Swamp survival
        registerTrigger(QuestTrigger(
            questId = "quest_swamp_survival",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_swamp_hermit",
            requirements = listOf(QuestTriggerRequirement.MinimumLevel(5)),
            description = "Learn to survive in the dangerous swamp"
        ))
        
        // ========== MOUNTAIN QUEST TRIGGERS ==========
        
        // Mountain Guide - Summit expedition
        registerTrigger(QuestTrigger(
            questId = "quest_summit_expedition",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_mountain_guide",
            requirements = listOf(QuestTriggerRequirement.MinimumLevel(7)),
            description = "Reach the mountain summit"
        ))
        
        // Crystal Mystic - Fortune telling
        registerTrigger(QuestTrigger(
            questId = "quest_crystal_prophecy",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_crystal_mystic",
            requirements = listOf(QuestTriggerRequirement.MinimumAffinity("npc_crystal_mystic", 40)),
            description = "The crystals reveal your destiny"
        ))
        
        // Eagle's Aerie discovery
        registerTrigger(QuestTrigger(
            questId = "quest_eagle_matriarch",
            triggerType = QuestTriggerType.LOCATION_DISCOVERY,
            triggerId = "mountains_eagles_aerie",
            requirements = listOf(QuestTriggerRequirement.MinimumLevel(12)),
            description = "Challenge the Eagle Matriarch boss",
            autoStart = false
        ))
        
        // Hermit Peakwise - Mountain wisdom
        registerTrigger(QuestTrigger(
            questId = "quest_peak_wisdom",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_mountain_hermit",
            requirements = listOf(QuestTriggerRequirement.MinimumLevel(8)),
            description = "Seek wisdom from the mountain hermit"
        ))
        
        // Hot Springs discovery
        registerTrigger(QuestTrigger(
            questId = "quest_healing_waters",
            triggerType = QuestTriggerType.LOCATION_DISCOVERY,
            triggerId = "mountains_hot_springs",
            description = "Discover the restorative hot springs"
        ))
        
        // ========== RUINS QUEST TRIGGERS ==========
        
        // Archaeologist Dustwing - Ancient knowledge
        registerTrigger(QuestTrigger(
            questId = "quest_ancient_excavation",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_archaeologist",
            requirements = listOf(QuestTriggerRequirement.MinimumLevel(6)),
            description = "Help excavate the ancient ruins"
        ))
        
        // Ghost Scribe - Lost knowledge
        registerTrigger(QuestTrigger(
            questId = "quest_forgotten_library",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_ghost_scribe",
            description = "Restore the forgotten library's texts"
        ))
        
        // Treasury Vault discovery
        registerTrigger(QuestTrigger(
            questId = "quest_eternal_guardian",
            triggerType = QuestTriggerType.LOCATION_DISCOVERY,
            triggerId = "ruins_treasury_vault",
            requirements = listOf(QuestTriggerRequirement.MinimumLevel(14)),
            description = "Face the Eternal Guardian boss",
            autoStart = false
        ))
        
        // Treasure Hunter - Treasure maps
        registerTrigger(QuestTrigger(
            questId = "quest_treasure_hunt",
            triggerType = QuestTriggerType.NPC_DIALOGUE,
            triggerId = "npc_treasure_hunter",
            requirements = listOf(QuestTriggerRequirement.MinimumLevel(7)),
            description = "Follow treasure maps to hidden riches"
        ))
        
        // Underground Chamber discovery
        registerTrigger(QuestTrigger(
            questId = "quest_ancient_mechanisms",
            triggerType = QuestTriggerType.LOCATION_DISCOVERY,
            triggerId = "ruins_underground_chamber",
            requirements = listOf(QuestTriggerRequirement.MinimumLevel(8)),
            description = "Reactivate the ancient mechanisms",
            autoStart = true
        ))
        
        // ========== SPECIAL TIME-BASED TRIGGERS ==========
        
        // Dawn patrol quest
        registerTrigger(QuestTrigger(
            questId = "quest_dawn_patrol",
            triggerType = QuestTriggerType.TIME_BASED,
            triggerId = "buttonburgh_training_grounds",
            requirements = listOf(
                QuestTriggerRequirement.TimeOfDay(com.jalmarquest.core.state.time.TimeOfDay.DAWN)
            ),
            description = "Join the morning patrol at dawn"
        ))
        
        // Night market quest
        registerTrigger(QuestTrigger(
            questId = "quest_night_market",
            triggerType = QuestTriggerType.TIME_BASED,
            triggerId = "buttonburgh_market_square",
            requirements = listOf(
                QuestTriggerRequirement.TimeOfDay(com.jalmarquest.core.state.time.TimeOfDay.NIGHT),
                QuestTriggerRequirement.MinimumLevel(5)
            ),
            description = "A secret market operates after dark"
        ))
    }
}
