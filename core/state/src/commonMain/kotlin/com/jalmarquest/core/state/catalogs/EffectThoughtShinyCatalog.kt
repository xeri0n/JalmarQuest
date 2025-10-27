package com.jalmarquest.core.state.catalogs

import kotlinx.serialization.Serializable

/**
 * Catalog of all status effects, buffs, and debuffs in the game.
 */

@Serializable
enum class EffectType {
    POSITIVE,
    NEGATIVE,
    NEUTRAL
}

@Serializable
data class GameEffect(
    val id: String,
    val name: String,
    val description: String,
    val type: EffectType,
    val modifiers: Map<String, Double> = emptyMap() // e.g., "xp_gain" -> 0.20, "stealth" -> 0.10
)

class EffectCatalog {
    private val effects = mutableMapOf<String, GameEffect>()
    
    init {
        registerDefaultEffects()
    }
    
    fun registerEffect(effect: GameEffect) {
        effects[effect.id] = effect
    }
    
    fun getEffectById(id: String): GameEffect? = effects[id]
    
    fun getAllEffects(): List<GameEffect> = effects.values.toList()
    
    private fun registerDefaultEffects() {
        // Basic effects
        registerEffect(GameEffect(
            id = "effect_health_regen",
            name = "Health Regeneration",
            description = "Slowly restores health over time.",
            type = EffectType.POSITIVE,
            modifiers = mapOf("health_regen" to 1.0)
        ))
        
        // Quest 1: The Giga-Seed
        registerEffect(GameEffect(
            id = "effect_giga_seed_insight",
            name = "Giga-Seed Insight",
            description = "Enhanced learning and wisdom.",
            type = EffectType.POSITIVE,
            modifiers = mapOf(
                "xp_gain" to 0.20,
                "scholarship_xp" to 0.50
            )
        ))
        
        // Quest 2: The High Perch
        registerEffect(GameEffect(
            id = "effect_short_flight",
            name = "Short Flight",
            description = "Your wings feel light, allowing short bursts of flight to reach high places.",
            type = EffectType.POSITIVE,
            modifiers = mapOf("can_fly" to 1.0)
        ))
        
        // Quest 3: The Night Forager
        registerEffect(GameEffect(
            id = "effect_calm",
            name = "Calm",
            description = "Your nerves are settled. You are less likely to be noticed by predators.",
            type = EffectType.POSITIVE,
            modifiers = mapOf("stealth" to 0.10)
        ))
        
        // Quest 6: The Lost Clutch
        registerEffect(GameEffect(
            id = "effect_keen_sight",
            name = "Keen Sight",
            description = "Your eyes are sharp, revealing hidden objects.",
            type = EffectType.POSITIVE,
            modifiers = mapOf("reveal_hidden" to 1.0)
        ))
        
        // Quest 10: The Antbassador
        registerEffect(GameEffect(
            id = "effect_ant_talk",
            name = "Ant Talk",
            description = "You can understand and communicate with ants.",
            type = EffectType.NEUTRAL,
            modifiers = mapOf("speak_ant" to 1.0)
        ))
        
        // Quest 11: The Stone-Stuck Seed
        registerEffect(GameEffect(
            id = "effect_quail_might",
            name = "Quail Might",
            description = "You feel unnaturally strong, for a quail.",
            type = EffectType.POSITIVE,
            modifiers = mapOf("strength" to 2.0)
        ))
        
        // Quest 13: The Chameleon's Challenge
        registerEffect(GameEffect(
            id = "effect_invisibility",
            name = "Invisibility",
            description = "You are completely invisible to the naked eye.",
            type = EffectType.POSITIVE,
            modifiers = mapOf("invisibility" to 1.0)
        ))
        
        // Quest 14: The Poisoned Grove
        registerEffect(GameEffect(
            id = "effect_poison",
            name = "Poison",
            description = "The toxic air damages you over time.",
            type = EffectType.NEGATIVE,
            modifiers = mapOf("damage_over_time" to 1.0)
        ))
        
        registerEffect(GameEffect(
            id = "effect_cleansing",
            name = "Cleansing",
            description = "Purifies the land and removes blight.",
            type = EffectType.POSITIVE,
            modifiers = mapOf("cleanse_area" to 1.0)
        ))
        
        // Quest 16: The Silent Scholar
        registerEffect(GameEffect(
            id = "effect_clarity",
            name = "Clarity",
            description = "Reduces internalizing time for your next Thought.",
            type = EffectType.POSITIVE,
            modifiers = mapOf("meditation_speed" to 0.25)
        ))
    }
}


/**
 * Catalog of all Thoughts (Disco Elysium-style) in the game.
 */

@Serializable
data class Thought(
    val id: String,
    val name: String,
    val description: String,
    val discoveryCondition: String, // e.g., "QUEST_REWARD", "ITEM_POSSESSION"
    val discoveryValue: String? = null, // e.g., quest ID or item ID
    val internalizationTimeMinutes: Int,
    val seedCost: Int,
    val effects: Map<String, Double> = emptyMap()
)

class ThoughtCatalog {
    private val thoughts = mutableMapOf<String, Thought>()
    
    init {
        registerDefaultThoughts()
    }
    
    fun registerThought(thought: Thought) {
        thoughts[thought.id] = thought
    }
    
    fun getThoughtById(id: String): Thought? = thoughts[id]
    
    fun getAllThoughts(): List<Thought> = thoughts.values.toList()
    
    private fun registerDefaultThoughts() {
        // Quest 1: The Giga-Seed
        registerThought(Thought(
            id = "thought_giga_seed_insight",
            name = "Giga-Seed Insight",
            description = "The ultimate seed of knowledge has taken root in your mind.",
            discoveryCondition = "ITEM_POSSESSION",
            discoveryValue = "quest_item_giga_seed",
            internalizationTimeMinutes = 120,
            seedCost = 1000,
            effects = mapOf(
                "xp_gain" to 0.20,
                "scholarship_xp" to 0.50
            )
        ))
        
        // Quest 4: The Beetle Brouhaha
        registerThought(Thought(
            id = "thought_insect_intuition",
            name = "Insect Intuition",
            description = "You understand the patterns and behaviors of insects.",
            discoveryCondition = "QUEST_REWARD",
            discoveryValue = "quest_beetle_brouhaha",
            internalizationTimeMinutes = 45,
            seedCost = 250,
            effects = mapOf(
                "foraging_xp" to 0.10,
                "item_drop_rate" to 0.05
            )
        ))
        
        // Quest 5: A Soothing Silence
        registerThought(Thought(
            id = "thought_communal_comfort",
            name = "Communal Comfort",
            description = "The well-being of the community strengthens you.",
            discoveryCondition = "QUEST_REWARD",
            discoveryValue = "quest_soothing_silence",
            internalizationTimeMinutes = 30,
            seedCost = 100,
            effects = mapOf(
                "faction_rep_buttonburgh" to 0.10,
                "defense_duration" to 0.05
            )
        ))
        
        // Quest 6: The Lost Clutch
        registerThought(Thought(
            id = "thought_parental_instinct",
            name = "Parental Instinct",
            description = "You understand the deep bonds of family and care.",
            discoveryCondition = "QUEST_REWARD",
            discoveryValue = "quest_lost_clutch",
            internalizationTimeMinutes = 30,
            seedCost = 100,
            effects = mapOf(
                "companion_affinity" to 0.10,
                "luck_duration" to 0.15
            )
        ))
        
        // Quest 7: The Coziest Nest
        registerThought(Thought(
            id = "thought_communal_comfort",
            name = "Communal Comfort",
            description = "The comfort of others brings you peace.",
            discoveryCondition = "QUEST_REWARD",
            discoveryValue = "quest_coziest_nest",
            internalizationTimeMinutes = 30,
            seedCost = 100,
            effects = mapOf(
                "faction_rep_buttonburgh" to 0.10,
                "defense_duration" to 0.05
            )
        ))
        
        // Quest 10: The Antbassador
        registerThought(Thought(
            id = "thought_insect_diplomacy",
            name = "Insect Diplomacy",
            description = "Even the smallest creatures deserve respect and negotiation.",
            discoveryCondition = "QUEST_REWARD",
            discoveryValue = "quest_antbassador",
            internalizationTimeMinutes = 60,
            seedCost = 300,
            effects = mapOf(
                "bartering_xp" to 0.10,
                "faction_rep_insects" to 0.10
            )
        ))
        
        // Quest 12: The Fading Elder
        registerThought(Thought(
            id = "thought_elder_wisdom",
            name = "Elder Wisdom",
            description = "The knowledge of generations flows through you.",
            discoveryCondition = "QUEST_REWARD",
            discoveryValue = "quest_fading_elder",
            internalizationTimeMinutes = 60,
            seedCost = 500,
            effects = mapOf(
                "scholarship_xp" to 0.20,
                "lore_unlock" to 1.0
            )
        ))
        
        // Quest 15: The Hoarder's Exam
        registerThought(Thought(
            id = "thought_hoarders_instinct",
            name = "Hoarder's Instinct",
            description = "You see value where others see junk.",
            discoveryCondition = "QUEST_REWARD",
            discoveryValue = "quest_hoarders_exam",
            internalizationTimeMinutes = 60,
            seedCost = 1000,
            effects = mapOf(
                "hoard_value" to 0.10,
                "luck_duration" to 0.10
            )
        ))
        
        // Quest 16: The Silent Scholar
        registerThought(Thought(
            id = "thought_shared_insight",
            name = "Shared Insight",
            description = "Knowledge shared is knowledge doubled.",
            discoveryCondition = "QUEST_REWARD",
            discoveryValue = "quest_silent_scholar",
            internalizationTimeMinutes = 1,
            seedCost = 0,
            effects = mapOf(
                "scholarship_xp" to 0.25
            )
        ))
        
        // Existing enlightenment thought
        registerThought(Thought(
            id = "thought_enlightenment",
            name = "Enlightenment",
            description = "The ultimate understanding of all things.",
            discoveryCondition = "QUEST_REWARD",
            discoveryValue = "quest_enlightenment_project",
            internalizationTimeMinutes = 300, // 5 hours, reduced to 5 min after quest
            seedCost = 5000,
            effects = mapOf(
                "xp_gain" to 0.50,
                "all_skills" to 0.25
            )
        ))
    }
}


/**
 * Catalog of all Shinies (collectibles with value).
 */

@Serializable
data class Shiny(
    val id: String,
    val name: String,
    val description: String,
    val rarity: ItemRarity,
    val value: Int
)

class ShinyCatalog {
    private val shinies = mutableMapOf<String, Shiny>()
    
    init {
        registerDefaultShinies()
    }
    
    fun registerShiny(shiny: Shiny) {
        shinies[shiny.id] = shiny
    }
    
    fun getShinyById(id: String): Shiny? = shinies[id]
    
    fun getAllShinies(): List<Shiny> = shinies.values.toList()
    
    private fun registerDefaultShinies() {
        // Quest 2: The High Perch
        registerShiny(Shiny(
            id = "shiny_sunpetal_painting",
            name = "Sunpetal Painting",
            description = "A beautiful, abstract painting of a Sunpetal. It seems to glow.",
            rarity = ItemRarity.RARE,
            value = 1000
        ))
        
        // Quest 6: The Lost Clutch
        registerShiny(Shiny(
            id = "shiny_floras_gratitude",
            name = "Flora's Gratitude",
            description = "A perfect, intricately speckled (but infertile) egg, given to you in immense gratitude. It reminds you of the fragility of life.",
            rarity = ItemRarity.EPIC,
            value = 2000
        ))
        
        // Quest 9: The Digger's Delight
        registerShiny(Shiny(
            id = "shiny_geode_fragment",
            name = "Geode Fragment",
            description = "A piece of rock, hollowed out and filled with glittering purple crystals.",
            rarity = ItemRarity.RARE,
            value = 1200
        ))
        
        // Quest 11: The Stone-Stuck Seed
        registerShiny(Shiny(
            id = "shiny_compressed_crystal",
            name = "Compressed Crystal",
            description = "A flawless crystal, formed under immense pressure. It thrums with power.",
            rarity = ItemRarity.EPIC,
            value = 2500
        ))
    }
}


/**
 * Catalog of lore snippets.
 */

@Serializable
data class LoreSnippet(
    val id: String,
    val title: String,
    val content: String,
    val category: String = "general"
)

class LoreCatalog {
    private val lore = mutableMapOf<String, LoreSnippet>()
    
    init {
        registerDefaultLore()
    }
    
    fun registerLore(snippet: LoreSnippet) {
        lore[snippet.id] = snippet
    }
    
    fun getLoreById(id: String): LoreSnippet? = lore[id]
    
    fun getAllLore(): List<LoreSnippet> = lore.values.toList()
    
    private fun registerDefaultLore() {
        // Quest 12: The Fading Elder
        registerLore(LoreSnippet(
            id = "lore_snippet_the_first_quails",
            title = "The First Quails",
            content = "In the beginning, there was the Great Nest, and from it hatched the First Quails. They were not like us—larger, stronger, wiser. They built Buttonburgh from nothing, laying the foundations we walk upon today. Elder Bristle remembers the tales his grandmother told: of quails who could speak to the trees, who understood the language of the wind. But that knowledge faded with each generation, lost like seeds scattered in a storm. We are but shadows of what once was, yet we endure.",
            category = "history"
        ))
    }
}
