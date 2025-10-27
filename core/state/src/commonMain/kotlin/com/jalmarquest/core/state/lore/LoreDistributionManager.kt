package com.jalmarquest.core.state.lore

import kotlinx.serialization.Serializable

/**
 * Distributes lore snippets as discoverable objects throughout the world.
 * Integrates with LocationCatalog, QuestManager, and LoreManager.
 */

@Serializable
enum class LoreDiscoveryMethod {
    LOCATION_EXPLORATION,  // Find lore by exploring a location
    ITEM_EXAMINATION,      // Examine an item to reveal lore
    NPC_CONVERSATION,      // Talk to NPC at high affinity
    QUEST_COMPLETION,      // Complete a quest to unlock lore
    ENEMY_DEFEAT,          // Defeat specific enemy to learn lore
    BOOK_READING,          // Read books found in locations
    INSCRIPTION_READING,   // Read wall inscriptions/plaques
    ARTIFACT_STUDY         // Study ancient artifacts
}

@Serializable
data class LoreObject(
    val id: String,
    val loreId: String,
    val title: String,
    val content: String,
    val category: String, // "history", "character", "world", "mystery", "faction"
    val discoveryMethod: LoreDiscoveryMethod,
    val locationId: String? = null,
    val npcId: String? = null,
    val itemId: String? = null,
    val questId: String? = null,
    val enemyId: String? = null,
    val requirements: List<LoreRequirement> = emptyList(),
    val visualDescription: String? = null // What the player sees before discovering
)

@Serializable
sealed class LoreRequirement {
    @Serializable
    data class MinimumLevel(val level: Int) : LoreRequirement()
    
    @Serializable
    data class CompletedQuest(val questId: String) : LoreRequirement()
    
    @Serializable
    data class MinimumAffinity(val npcId: String, val affinity: Int) : LoreRequirement()
    
    @Serializable
    data class DiscoveredLore(val loreId: String) : LoreRequirement()
    
    @Serializable
    data class ChoiceTag(val tag: String) : LoreRequirement()
}

class LoreDistributionManager {
    private val loreObjects = mutableListOf<LoreObject>()
    
    init {
        registerDefaultLoreObjects()
    }
    
    /**
     * Register a lore object.
     */
    fun registerLoreObject(loreObject: LoreObject) {
        loreObjects.add(loreObject)
    }
    
    /**
     * Get all lore objects at a specific location.
     */
    fun getLoreObjectsAtLocation(locationId: String): List<LoreObject> {
        return loreObjects.filter { it.locationId == locationId }
    }
    
    /**
     * Get lore objects associated with an NPC.
     */
    fun getLoreObjectsForNpc(npcId: String): List<LoreObject> {
        return loreObjects.filter { it.npcId == npcId }
    }
    
    /**
     * Get lore objects associated with a quest.
     */
    fun getLoreObjectsForQuest(questId: String): List<LoreObject> {
        return loreObjects.filter { it.questId == questId }
    }
    
    /**
     * Get lore objects by category.
     */
    fun getLoreObjectsByCategory(category: String): List<LoreObject> {
        return loreObjects.filter { it.category == category }
    }
    
    /**
     * Check if lore object requirements are met.
     */
    fun isLoreAvailable(
        loreObject: LoreObject,
        playerLevel: Int,
        completedQuests: Set<String>,
        npcAffinities: Map<String, Int>,
        discoveredLore: Set<String>,
        choiceTags: Set<String>
    ): Boolean {
        return loreObject.requirements.all { requirement ->
            when (requirement) {
                is LoreRequirement.MinimumLevel -> 
                    playerLevel >= requirement.level
                is LoreRequirement.CompletedQuest -> 
                    completedQuests.contains(requirement.questId)
                is LoreRequirement.MinimumAffinity -> 
                    (npcAffinities[requirement.npcId] ?: 0) >= requirement.affinity
                is LoreRequirement.DiscoveredLore -> 
                    discoveredLore.contains(requirement.loreId)
                is LoreRequirement.ChoiceTag -> 
                    choiceTags.contains(requirement.tag)
            }
        }
    }
    
    /**
     * Register default lore objects throughout the world.
     */
    private fun registerDefaultLoreObjects() {
        // ========== BUTTONBURGH LORE ==========
        
        // Town Hall inscription
        registerLoreObject(LoreObject(
            id = "lore_obj_buttonburgh_founding",
            loreId = "lore_buttonburgh_founding",
            title = "The Founding of Buttonburgh",
            content = "Long ago, when the Great Quail Corvus led our ancestors from the Old Roost, they discovered this safe haven beneath the Button Tree. Thus began Buttonburgh, sanctuary of quails.",
            category = "history",
            discoveryMethod = LoreDiscoveryMethod.INSCRIPTION_READING,
            locationId = "buttonburgh_town_hall",
            visualDescription = "A bronze plaque on the town hall wall"
        ))
        
        // Scholar's District book
        registerLoreObject(LoreObject(
            id = "lore_obj_ancient_civilization",
            loreId = "lore_ancient_civilization",
            title = "The Ancients Who Came Before",
            content = "Before quails walked this land, another civilization thrived. Their ruins dot the landscape, filled with mechanisms beyond our understanding. What happened to them remains a mystery.",
            category = "world",
            discoveryMethod = LoreDiscoveryMethod.BOOK_READING,
            locationId = "buttonburgh_scholars_district",
            requirements = listOf(LoreRequirement.MinimumLevel(3)),
            visualDescription = "A dusty tome titled 'Pre-Avian History'"
        ))
        
        // Elder Quill dialogue
        registerLoreObject(LoreObject(
            id = "lore_obj_elder_quill_past",
            loreId = "lore_elder_quill_past",
            title = "Elder Quill's Youth",
            content = "Elder Quill wasn't always wise. In his youth, he was known as 'Reckless Quill' and once tried to fly to the moon. He learned wisdom through many mistakes.",
            category = "character",
            discoveryMethod = LoreDiscoveryMethod.NPC_CONVERSATION,
            npcId = "npc_elder_quill",
            requirements = listOf(LoreRequirement.MinimumAffinity("npc_elder_quill", 50)),
            visualDescription = "Ask Elder Quill about his past"
        ))
        
        // Tavern rumors
        registerLoreObject(LoreObject(
            id = "lore_obj_buried_treasure",
            loreId = "lore_buried_treasure",
            title = "The Legend of Buried Treasure",
            content = "Sailors speak of a great treasure buried near the old shipwreck. They say it belonged to the legendary pirate quail, Captain Featherbeard.",
            category = "mystery",
            discoveryMethod = LoreDiscoveryMethod.NPC_CONVERSATION,
            npcId = "npc_wandering_minstrel",
            locationId = "buttonburgh_tavern",
            visualDescription = "Listen to tavern gossip"
        ))
        
        // ========== FOREST LORE ==========
        
        // Poisoned Grove discovery
        registerLoreObject(LoreObject(
            id = "lore_obj_poisoned_grove_curse",
            loreId = "lore_poisoned_grove_curse",
            title = "The Grove's Blight",
            content = "This grove was once the most beautiful in the forest. Then the Blight came, a creeping poison that kills all it touches. Some say it was caused by human pollution, others whisper of dark magic.",
            category = "mystery",
            discoveryMethod = LoreDiscoveryMethod.LOCATION_EXPLORATION,
            locationId = "poisoned_grove",
            requirements = listOf(LoreRequirement.MinimumLevel(3)),
            visualDescription = "Sickly plants and dying trees tell a story"
        ))
        
        // Mushroom Sage wisdom
        registerLoreObject(LoreObject(
            id = "lore_obj_fungal_network",
            loreId = "lore_fungal_network",
            title = "The Mycelial Network",
            content = "Beneath the forest floor, all mushrooms are connected by invisible threads. They communicate, share resources, and remember. The forest thinks through its fungi.",
            category = "world",
            discoveryMethod = LoreDiscoveryMethod.NPC_CONVERSATION,
            npcId = "npc_mushroom_sage",
            requirements = listOf(LoreRequirement.MinimumAffinity("npc_mushroom_sage", 40)),
            visualDescription = "The Mushroom Sage reveals forest secrets"
        ))
        
        // Ancient tree carving
        registerLoreObject(LoreObject(
            id = "lore_obj_forest_spirits",
            loreId = "lore_forest_spirits",
            title = "Spirits of the Wood",
            content = "Ancient carvings depict beings of pure energy dwelling within old trees. The eldest trees are said to house forest spirits who protect the woods.",
            category = "world",
            discoveryMethod = LoreDiscoveryMethod.INSCRIPTION_READING,
            locationId = "forest_whispering_pines",
            visualDescription = "Strange symbols carved into ancient bark"
        ))
        
        // Crow's territory lore
        registerLoreObject(LoreObject(
            id = "lore_obj_crow_king",
            loreId = "lore_crow_king",
            title = "The Crow King's Decree",
            content = "Crows once ruled the forest under the Crow King. When he fell to the Eagle Matriarch, his descendants scatter but never forget. They guard their territories fiercely.",
            category = "history",
            discoveryMethod = LoreDiscoveryMethod.LOCATION_EXPLORATION,
            locationId = "crows_perch",
            visualDescription = "Crow feathers arranged in a pattern"
        ))
        
        // ========== BEACH LORE ==========
        
        // Shipwreck log book
        registerLoreObject(LoreObject(
            id = "lore_obj_shipwreck_captain",
            loreId = "lore_shipwreck_captain",
            title = "Captain's Last Entry",
            content = "The ship's log reveals Captain Featherbeard's final moments: 'The storm is upon us. If ye find this, know that I hid me greatest treasure where the lighthouse beam touches the sand at high noon.'",
            category = "mystery",
            discoveryMethod = LoreDiscoveryMethod.ITEM_EXAMINATION,
            locationId = "beach_shipwreck",
            itemId = "item_captains_log",
            requirements = listOf(LoreRequirement.MinimumLevel(5)),
            visualDescription = "A waterlogged leather journal"
        ))
        
        // Lighthouse history
        registerLoreObject(LoreObject(
            id = "lore_obj_lighthouse_keepers",
            loreId = "lore_lighthouse_keepers",
            title = "The Eternal Keepers",
            content = "For generations, the Beacon family has kept the lighthouse lit. They say the first Keeper made a pact with the sea itself to protect sailors.",
            category = "history",
            discoveryMethod = LoreDiscoveryMethod.NPC_CONVERSATION,
            npcId = "npc_lighthouse_keeper",
            requirements = listOf(LoreRequirement.MinimumAffinity("npc_lighthouse_keeper", 30)),
            visualDescription = "Ask about the lighthouse's history"
        ))
        
        // Tide pool ecology
        registerLoreObject(LoreObject(
            id = "lore_obj_tide_pools",
            loreId = "lore_tide_pools",
            title = "Miniature Oceans",
            content = "Each tide pool is a complete ecosystem. Hermit crabs, anemones, and tiny fish create societies as complex as any quail town. They adapt twice daily to the changing tides.",
            category = "world",
            discoveryMethod = LoreDiscoveryMethod.LOCATION_EXPLORATION,
            locationId = "beach_tide_pools",
            visualDescription = "Observe the bustling tide pool life"
        ))
        
        // Sandpiper alliance
        registerLoreObject(LoreObject(
            id = "lore_obj_sandpiper_tribe",
            loreId = "lore_sandpiper_tribe",
            title = "The Sandpiper Tribes",
            content = "Sandpipers are organized, disciplined, and fiercely territorial. Unlike quails who value individual freedom, sandpipers prize collective action. An alliance with them would be valuable.",
            category = "faction",
            discoveryMethod = LoreDiscoveryMethod.NPC_CONVERSATION,
            npcId = "npc_sandpiper_chief",
            requirements = listOf(LoreRequirement.MinimumLevel(4)),
            visualDescription = "Learn about sandpiper culture"
        ))
        
        // ========== SWAMP LORE ==========
        
        // Bog Witch origins
        registerLoreObject(LoreObject(
            id = "lore_obj_bog_witch_origin",
            loreId = "lore_bog_witch_origin",
            title = "The Witch of the Swamp",
            content = "She was once a quail alchemist named Muriel who sought forbidden knowledge. The swamp changed her, gave her power, but took her ability to leave. Now she is both prisoner and queen of the marsh.",
            category = "character",
            discoveryMethod = LoreDiscoveryMethod.NPC_CONVERSATION,
            npcId = "npc_bog_witch",
            requirements = listOf(
                LoreRequirement.MinimumAffinity("npc_bog_witch", 60),
                LoreRequirement.MinimumLevel(8)
            ),
            visualDescription = "The Bog Witch shares her tragic tale"
        ))
        
        // Firefly communication
        registerLoreObject(LoreObject(
            id = "lore_obj_firefly_language",
            loreId = "lore_firefly_language",
            title = "The Language of Light",
            content = "Fireflies don't speak with sound but with synchronized flashes of bioluminescence. Their patterns encode complex messages across vast distances. Learning their language opens new possibilities.",
            category = "world",
            discoveryMethod = LoreDiscoveryMethod.NPC_CONVERSATION,
            npcId = "npc_firefly_queen",
            requirements = listOf(LoreRequirement.CompletedQuest("quest_firefly_dance")),
            visualDescription = "The Firefly Queen teaches her language"
        ))
        
        // Ancient Alligator legend
        registerLoreObject(LoreObject(
            id = "lore_obj_gator_legend",
            loreId = "lore_gator_legend",
            title = "The Swamp's Ancient Guardian",
            content = "The Ancient Alligator has lived for centuries, growing larger with each passing decade. It remembers when humans first came to these lands. Some say it IS the swamp itself, given physical form.",
            category = "mystery",
            discoveryMethod = LoreDiscoveryMethod.LOCATION_EXPLORATION,
            locationId = "swamp_gator_den",
            requirements = listOf(LoreRequirement.MinimumLevel(10)),
            visualDescription = "Massive claw marks and shed scales tell a story"
        ))
        
        // ========== MOUNTAIN LORE ==========
        
        // Crystal cave formation
        registerLoreObject(LoreObject(
            id = "lore_obj_crystal_caves",
            loreId = "lore_crystal_caves",
            title = "The Singing Crystals",
            content = "These crystals formed over millions of years, growing from the mountain's heart. They vibrate at specific frequencies, creating the mountain's eternal song. Some believe they store memories.",
            category = "world",
            discoveryMethod = LoreDiscoveryMethod.LOCATION_EXPLORATION,
            locationId = "mountains_crystal_caves",
            visualDescription = "The crystals hum with ancient energy"
        ))
        
        // Eagle Matriarch history
        registerLoreObject(LoreObject(
            id = "lore_obj_eagle_matriarch",
            loreId = "lore_eagle_matriarch",
            title = "Queen of the Skies",
            content = "The Eagle Matriarch has ruled the mountain peaks for two decades. She defeated the previous Crow King in single combat, establishing eagles as the apex predators. None dare challenge her reign.",
            category = "history",
            discoveryMethod = LoreDiscoveryMethod.NPC_CONVERSATION,
            npcId = "npc_mountain_guide",
            requirements = listOf(LoreRequirement.MinimumLevel(10)),
            visualDescription = "Tales of the Eagle Matriarch's victories"
        ))
        
        // Hermit's wisdom
        registerLoreObject(LoreObject(
            id = "lore_obj_mountain_hermit_wisdom",
            loreId = "lore_mountain_hermit_wisdom",
            title = "The Hermit's Enlightenment",
            content = "Peakwise came to the mountain seeking answers after losing everything in the valley. Years of meditation and solitude brought him peace. He shares his wisdom with those who climb high enough.",
            category = "character",
            discoveryMethod = LoreDiscoveryMethod.NPC_CONVERSATION,
            npcId = "npc_mountain_hermit",
            requirements = listOf(
                LoreRequirement.MinimumAffinity("npc_mountain_hermit", 50),
                LoreRequirement.CompletedQuest("quest_summit_expedition")
            ),
            visualDescription = "The hermit shares his philosophy"
        ))
        
        // Summit revelation
        registerLoreObject(LoreObject(
            id = "lore_obj_summit_view",
            loreId = "lore_summit_view",
            title = "The View from Above",
            content = "From the summit, you can see everything: Buttonburgh is a tiny cluster of lights, the forest a green carpet, the ruins pale dots on the horizon. You realize how small you are, yet how far you've come.",
            category = "world",
            discoveryMethod = LoreDiscoveryMethod.LOCATION_EXPLORATION,
            locationId = "mountains_summit",
            requirements = listOf(LoreRequirement.CompletedQuest("quest_summit_expedition")),
            visualDescription = "The world spreads out below"
        ))
        
        // ========== RUINS LORE ==========
        
        // Ancient civilization
        registerLoreObject(LoreObject(
            id = "lore_obj_ruins_purpose",
            loreId = "lore_ruins_purpose",
            title = "The Builders' Purpose",
            content = "Archaeological evidence suggests the ancients were attempting something monumental - a great work that would transcend mortality. They failed, but their legacy endures in these crumbling stones.",
            category = "history",
            discoveryMethod = LoreDiscoveryMethod.NPC_CONVERSATION,
            npcId = "npc_archaeologist",
            requirements = listOf(
                LoreRequirement.MinimumAffinity("npc_archaeologist", 40),
                LoreRequirement.MinimumLevel(7)
            ),
            visualDescription = "The archaeologist shares discoveries"
        ))
        
        // Ghost Scribe's tale
        registerLoreObject(LoreObject(
            id = "lore_obj_ghost_scribe_story",
            loreId = "lore_ghost_scribe_story",
            title = "The Eternal Librarian",
            content = "The Ghost Scribe was the last librarian of the ancient civilization. When the end came, he chose to remain, binding his spirit to the library to preserve knowledge for future generations.",
            category = "character",
            discoveryMethod = LoreDiscoveryMethod.NPC_CONVERSATION,
            npcId = "npc_ghost_scribe",
            requirements = listOf(LoreRequirement.MinimumAffinity("npc_ghost_scribe", 30)),
            visualDescription = "The ghost reveals his sad history"
        ))
        
        // Forbidden text
        registerLoreObject(LoreObject(
            id = "lore_obj_forbidden_knowledge",
            loreId = "lore_forbidden_knowledge",
            title = "That Which Should Not Be Known",
            content = "Some knowledge is dangerous. The ancients discovered truths that drove them to madness. This book contains fragments of that knowledge. Read at your own peril.",
            category = "mystery",
            discoveryMethod = LoreDiscoveryMethod.BOOK_READING,
            locationId = "ruins_forgotten_library",
            requirements = listOf(
                LoreRequirement.MinimumLevel(12),
                LoreRequirement.CompletedQuest("quest_forgotten_library")
            ),
            visualDescription = "A book bound in strange metal, warm to the touch"
        ))
        
        // Treasury secret
        registerLoreObject(LoreObject(
            id = "lore_obj_eternal_guardian",
            loreId = "lore_eternal_guardian",
            title = "The Guardian's Oath",
            content = "The Eternal Guardian was the ancients' final creation - an unstoppable construct tasked with protecting their greatest treasures. It will fulfill its duty until time itself ends.",
            category = "mystery",
            discoveryMethod = LoreDiscoveryMethod.LOCATION_EXPLORATION,
            locationId = "ruins_treasury_vault",
            requirements = listOf(LoreRequirement.MinimumLevel(14)),
            visualDescription = "Inscriptions warn of the guardian within"
        ))
        
        // Mosaic prophecy
        registerLoreObject(LoreObject(
            id = "lore_obj_mosaic_prophecy",
            loreId = "lore_mosaic_prophecy",
            title = "The Mosaic Prophecy",
            content = "The mosaic depicts a small bird (possibly a quail) standing before a great light. Ancient text reads: 'From the smallest shall come the greatest change.' Could this refer to your destiny?",
            category = "mystery",
            discoveryMethod = LoreDiscoveryMethod.INSCRIPTION_READING,
            locationId = "ruins_mosaic_hall",
            requirements = listOf(LoreRequirement.MinimumLevel(10)),
            visualDescription = "Colorful tiles form a mysterious scene"
        ))
    }
}
