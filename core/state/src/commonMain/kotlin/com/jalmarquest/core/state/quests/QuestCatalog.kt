package com.jalmarquest.core.state.quests

import com.jalmarquest.core.model.*

/**
 * Catalog of all available quests in the game.
 * Provides quest lookup and discovery.
 */
class QuestCatalog {
    private val quests = mutableMapOf<QuestId, Quest>()
    
    init {
        registerDefaultQuests()
    }
    
    /**
     * Register a quest in the catalog.
     */
    fun registerQuest(quest: Quest) {
        quests[quest.questId] = quest
    }
    
    /**
     * Get a quest by ID.
     */
    fun getQuestById(questId: QuestId): Quest? {
        return quests[questId]
    }
    
    /**
     * Get all registered quests.
     */
    fun getAllQuests(): List<Quest> {
        return quests.values.toList()
    }
    
    /**
     * Register default starter quests.
     */
    private fun registerDefaultQuests() {
        // Tutorial quest: First exploration
        registerQuest(
            Quest(
                questId = QuestId("tutorial_first_exploration"),
                title = "First Flight",
                description = "Venture out from the Nest and explore the surrounding area. Every quail must learn to navigate the world beyond their home.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "explore_area",
                        description = "Complete an exploration",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 1
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 50,
                        description = "50 Seeds for your bravery"
                    ),
                    QuestReward(
                        type = QuestRewardType.EXPERIENCE,
                        quantity = 25,
                        description = "25 XP"
                    )
                ),
                questGiverNpc = "Elder Quill",
                turnInNpc = "Elder Quill",
                recommendedLevel = 1
            )
        )
        
        // Tutorial quest: Collect resources
        registerQuest(
            Quest(
                questId = QuestId("tutorial_gather_resources"),
                title = "Feathered Forager",
                description = "Learn the art of gathering. Collect acorns and other resources to sustain yourself.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "collect_acorns",
                        description = "Collect 5 acorns",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "acorn",
                        targetQuantity = 5
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 30,
                        description = "30 Seeds"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.FORAGING.name,
                        quantity = 50,
                        description = "50 Foraging XP"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.PrerequisiteQuest(QuestId("tutorial_first_exploration"))
                ),
                questGiverNpc = "Elder Quill",
                turnInNpc = "Elder Quill",
                recommendedLevel = 1
            )
        )
        
        // Tutorial quest: Learn crafting
        registerQuest(
            Quest(
                questId = QuestId("tutorial_first_craft"),
                title = "Alchemical Beginnings",
                description = "Master Ignatius can teach you the basics of alchemy. Craft your first concoction.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "craft_concoction",
                        description = "Craft any concoction",
                        type = QuestObjectiveType.CRAFT_ITEM,
                        targetQuantity = 1
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 40,
                        description = "40 Seeds"
                    ),
                    QuestReward(
                        type = QuestRewardType.RECIPE,
                        targetId = "basic_healing_potion",
                        quantity = 1,
                        description = "Recipe: Basic Healing Potion"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.PrerequisiteQuest(QuestId("tutorial_gather_resources"))
                ),
                questGiverNpc = "Ignatius",
                turnInNpc = "Ignatius",
                recommendedLevel = 1
            )
        )
        
        // Archetype introduction quest
        registerQuest(
            Quest(
                questId = QuestId("choose_your_path"),
                title = "The Path of Feathers",
                description = "Every quail finds their calling. Speak with the Elder Council to discover your archetype.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "select_archetype",
                        description = "Choose your archetype",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 1
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 100,
                        description = "100 Seeds"
                    ),
                    QuestReward(
                        type = QuestRewardType.ARCHETYPE_TALENT_POINT,
                        quantity = 1,
                        description = "1 Talent Point"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.PrerequisiteQuest(QuestId("tutorial_first_craft")),
                    QuestRequirement.MinimumLevel(2)
                ),
                questGiverNpc = "Elder Quill",
                turnInNpc = "Elder Quill",
                recommendedLevel = 2
            )
        )
        
        // Hoard building quest
        registerQuest(
            Quest(
                questId = QuestId("building_the_hoard"),
                title = "Treasures of the Nest",
                description = "Begin building your hoard. Every quail needs a collection of shinies and valuables.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "accumulate_seeds",
                        description = "Accumulate 500 Seeds",
                        type = QuestObjectiveType.ACCUMULATE_SEEDS,
                        targetQuantity = 500
                    ),
                    QuestObjective(
                        objectiveId = "find_shiny",
                        description = "Find a shiny object",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "shiny",
                        targetQuantity = 1,
                        isOptional = true
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 200,
                        description = "200 Seeds"
                    ),
                    QuestReward(
                        type = QuestRewardType.ITEMS,
                        targetId = "decorative_feather",
                        quantity = 1,
                        description = "Decorative Feather (Nest decoration)"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.PrerequisiteQuest(QuestId("choose_your_path"))
                ),
                questGiverNpc = "Cornelius the Curator",
                turnInNpc = "Cornelius the Curator",
                recommendedLevel = 3
            )
        )
        
        // Repeatable daily quest
        registerQuest(
            Quest(
                questId = QuestId("daily_foraging"),
                title = "Daily Foraging Run",
                description = "The Nest always needs more resources. Gather ingredients for the community.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "gather_ingredients",
                        description = "Gather 10 ingredients",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetQuantity = 10
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 75,
                        description = "75 Seeds"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.FORAGING.name,
                        quantity = 30,
                        description = "30 Foraging XP"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.MinimumLevel(2)
                ),
                questGiverNpc = "Nest Quartermaster",
                turnInNpc = "Nest Quartermaster",
                isRepeatable = true,
                recommendedLevel = 2
            )
        )
        
        // Quest 1: The Giga-Seed
        registerQuest(
            Quest(
                questId = QuestId("quest_giga_seed"),
                title = "The Giga-Seed",
                description = "Professor Tessel speaks of a legendary 'Giga-Seed,' a seed of pure knowledge. Find it and internalize its wisdom.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "find_giga_seed",
                        description = "Find the Giga-Seed in the Forest",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "quest_item_giga_seed",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "bring_to_tessel",
                        description = "Bring the Giga-Seed to Professor Tessel",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_professor_tessel",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("find_giga_seed")
                    ),
                    QuestObjective(
                        objectiveId = "internalize_thought",
                        description = "Internalize the Giga-Seed Insight",
                        type = QuestObjectiveType.INTERNALIZE_THOUGHT,
                        targetId = "thought_giga_seed_insight",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("bring_to_tessel")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.THOUGHT,
                        targetId = "thought_giga_seed_insight",
                        quantity = 1,
                        description = "Unlocks Giga-Seed Insight thought"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.SCHOLARSHIP.name,
                        quantity = 500,
                        description = "500 Scholarship XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.FORAGING.name,
                        quantity = 300,
                        description = "300 Foraging XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 1000,
                        description = "1000 Seeds"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.MinimumSkill(SkillType.SCHOLARSHIP, 3),
                    QuestRequirement.MinimumSkill(SkillType.FORAGING, 5)
                ),
                questGiverNpc = "Professor Tessel",
                turnInNpc = "Professor Tessel",
                recommendedLevel = 5
            )
        )
        
        // Quest 2: The High Perch
        registerQuest(
            Quest(
                questId = QuestId("quest_high_perch"),
                title = "The High Perch",
                description = "Artist Pip needs a Sunpetal Flower from a high stump. You must find a way to reach it—through alchemy or exploration.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "get_sunpetal",
                        description = "Obtain the Sunpetal Flower",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "item_sunpetal_flower",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "return_to_pip",
                        description = "Return to Artist Pip",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_artist_pip",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("get_sunpetal")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.SHINY,
                        targetId = "shiny_sunpetal_painting",
                        quantity = 1,
                        description = "Sunpetal Painting"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 300,
                        description = "300 Seeds"
                    )
                ),
                questGiverNpc = "Artist Pip",
                turnInNpc = "Artist Pip",
                recommendedLevel = 3
            )
        )
        
        // Quest 3: The Night Forager
        registerQuest(
            Quest(
                questId = QuestId("quest_night_forager"),
                title = "The Night Forager",
                description = "Herbalist Hoot needs a Moondew Fern, which only appears in the forest at night.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "find_moondew_fern",
                        description = "Find a Moondew Fern at night",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "item_moondew_fern",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "return_to_hoot",
                        description = "Bring the fern to Herbalist Hoot",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_herbalist_hoot",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("find_moondew_fern")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.RECIPE,
                        targetId = "recipe_potion_of_calm",
                        quantity = 1,
                        description = "Recipe: Potion of Calm"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.FORAGING.name,
                        quantity = 200,
                        description = "200 Foraging XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 400,
                        description = "400 Seeds"
                    )
                ),
                questGiverNpc = "Herbalist Hoot",
                turnInNpc = "Herbalist Hoot",
                recommendedLevel = 3
            )
        )
        
        // Quest 4: The Beetle Brouhaha
        registerQuest(
            Quest(
                questId = QuestId("quest_beetle_brouhaha"),
                title = "The Beetle Brouhaha",
                description = "Professor Click is obsessed with beetles and needs you to collect 5 rare specimens.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "collect_azure_beetle",
                        description = "Collect Azure Beetle",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "item_beetle_azure",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "collect_emerald_beetle",
                        description = "Collect Emerald Beetle",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "item_beetle_emerald",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "collect_ruby_beetle",
                        description = "Collect Ruby Beetle",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "item_beetle_ruby",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "collect_obsidian_beetle",
                        description = "Collect Obsidian Beetle",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "item_beetle_obsidian",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "collect_opal_beetle",
                        description = "Collect Opal Beetle",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "item_beetle_opal",
                        targetQuantity = 1
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.THOUGHT,
                        targetId = "thought_insect_intuition",
                        quantity = 1,
                        description = "Unlocks Insect Intuition thought"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.FORAGING.name,
                        quantity = 300,
                        description = "300 Foraging XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 750,
                        description = "750 Seeds"
                    )
                ),
                questGiverNpc = "Professor Click",
                turnInNpc = "Professor Click",
                recommendedLevel = 4
            )
        )
        
        // Quest 5: A Soothing Silence
        registerQuest(
            Quest(
                questId = QuestId("quest_soothing_silence"),
                title = "A Soothing Silence",
                description = "The Dust Bath is unusable due to Tink's noisy machine. Find a way to quiet it.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "investigate_noise",
                        description = "Investigate the noise at the Dust Bath",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "buttonburgh_dust_bath",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "quiet_machine",
                        description = "Find a way to quiet Tink's machine",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("investigate_noise")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 500,
                        description = "500 Seeds"
                    )
                ),
                questGiverNpc = "Elder Bristle",
                turnInNpc = "Elder Bristle",
                recommendedLevel = 3
            )
        )
        
        // Quest 6: The Lost Clutch
        registerQuest(
            Quest(
                questId = QuestId("quest_lost_clutch"),
                title = "The Lost Clutch",
                description = "Frantic Flora has lost her clutch of eggs. Brew a perception potion to find them.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "brew_keen_sight",
                        description = "Brew Elixir of Keen Sight",
                        type = QuestObjectiveType.CRAFT_ITEM,
                        targetId = "recipe_elixir_of_keen_sight",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "collect_eggs",
                        description = "Find 6 hidden eggs",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "quest_hidden_egg",
                        targetQuantity = 6,
                        prerequisiteObjectives = listOf("brew_keen_sight")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.THOUGHT,
                        targetId = "thought_parental_instinct",
                        quantity = 1,
                        description = "Unlocks Parental Instinct thought"
                    ),
                    QuestReward(
                        type = QuestRewardType.SHINY,
                        targetId = "shiny_floras_gratitude",
                        quantity = 1,
                        description = "Flora's Gratitude"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.FORAGING.name,
                        quantity = 150,
                        description = "150 Foraging XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.ALCHEMY.name,
                        quantity = 50,
                        description = "50 Alchemy XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 500,
                        description = "500 Seeds"
                    )
                ),
                questGiverNpc = "Frantic Flora",
                turnInNpc = "Frantic Flora",
                recommendedLevel = 3
            )
        )
        
        // Quest 7: The Coziest Nest
        registerQuest(
            Quest(
                questId = QuestId("quest_coziest_nest"),
                title = "The Coziest Nest",
                description = "Old Man Thistle is cold. Craft an Insulated Nest Lining to help him.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "craft_nest_lining",
                        description = "Craft Insulated Nest Lining",
                        type = QuestObjectiveType.CRAFT_ITEM,
                        targetId = "recipe_insulated_nest_lining",
                        targetQuantity = 1
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.THOUGHT,
                        targetId = "thought_communal_comfort",
                        quantity = 1,
                        description = "Unlocks Communal Comfort thought"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.FORAGING.name,
                        quantity = 150,
                        description = "150 Crafting XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.FORAGING.name,
                        quantity = 100,
                        description = "100 Foraging XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 500,
                        description = "500 Seeds"
                    )
                ),
                questGiverNpc = "Old Man Thistle",
                turnInNpc = "Old Man Thistle",
                recommendedLevel = 2
            )
        )
        
        // Quest 8: Practical Plumage
        registerQuest(
            Quest(
                questId = QuestId("quest_practical_plumage"),
                title = "Practical Plumage",
                description = "Scout Whisper needs camouflage gear to observe a hawk's nest safely.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "craft_camo_cloak",
                        description = "Craft a Camouflage Cloak",
                        type = QuestObjectiveType.CRAFT_ITEM,
                        targetId = "recipe_camo_cloak",
                        targetQuantity = 1
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.RECIPE,
                        targetId = "recipe_camo_cloak",
                        quantity = 1,
                        description = "You keep the recipe"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.FORAGING.name,
                        quantity = 150,
                        description = "150 Crafting XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.FORAGING.name,
                        quantity = 100,
                        description = "100 Foraging XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 400,
                        description = "400 Seeds"
                    )
                ),
                questGiverNpc = "Scout Whisper",
                turnInNpc = "Scout Whisper",
                recommendedLevel = 2
            )
        )
        
        // Quest 9: The Digger's Delight
        registerQuest(
            Quest(
                questId = QuestId("quest_diggers_delight"),
                title = "The Digger's Delight",
                description = "You found hard soil with something valuable beneath. Craft Reinforced Talons to dig it up.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "craft_talons",
                        description = "Craft Reinforced Talons",
                        type = QuestObjectiveType.CRAFT_ITEM,
                        targetId = "recipe_reinforced_talons",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "dig_soil",
                        description = "Dig up the Hard Soil",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("craft_talons")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.SHINY,
                        targetId = "shiny_geode_fragment",
                        quantity = 1,
                        description = "Geode Fragment"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.FORAGING.name,
                        quantity = 150,
                        description = "150 Crafting XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.FORAGING.name,
                        quantity = 150,
                        description = "150 Foraging XP"
                    )
                ),
                questGiverNpc = "Digger",
                turnInNpc = "Digger",
                recommendedLevel = 3
            )
        )
        
        // Quest 10: The Antbassador
        registerQuest(
            Quest(
                questId = QuestId("quest_antbassador"),
                title = "The Antbassador",
                description = "Ants are stealing seeds from Wicker's shop. Negotiate a truce instead of violence.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "brew_ant_talk",
                        description = "Brew Potion of Ant Talk",
                        type = QuestObjectiveType.CRAFT_ITEM,
                        targetId = "recipe_potion_of_ant_talk",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "negotiate_with_ants",
                        description = "Negotiate with the Ant Queen",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_ant_queen",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("brew_ant_talk")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.THOUGHT,
                        targetId = "thought_insect_diplomacy",
                        quantity = 1,
                        description = "Unlocks Insect Diplomacy thought"
                    ),
                    QuestReward(
                        type = QuestRewardType.FACTION_REPUTATION,
                        targetId = "faction_ant_colony",
                        quantity = 50,
                        description = "50 Ant Colony reputation"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.ALCHEMY.name,
                        quantity = 100,
                        description = "100 Alchemy XP"
                    )
                ),
                questGiverNpc = "Worried Wicker",
                turnInNpc = "Worried Wicker",
                recommendedLevel = 3
            )
        )
        
        // Quest 11: The Stone-Stuck Seed
        registerQuest(
            Quest(
                questId = QuestId("quest_stone_stuck_seed"),
                title = "The Stone-Stuck Seed",
                description = "A beautiful shiny is stuck under a heavy pebble. Brew a strength potion to move it.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "brew_quail_might",
                        description = "Brew Potion of Quail Might",
                        type = QuestObjectiveType.CRAFT_ITEM,
                        targetId = "recipe_potion_of_quail_might",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "lift_pebble",
                        description = "Lift the heavy pebble",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("brew_quail_might")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.SHINY,
                        targetId = "shiny_compressed_crystal",
                        quantity = 1,
                        description = "Compressed Crystal"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.ALCHEMY.name,
                        quantity = 150,
                        description = "150 Alchemy XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.HOARDING.name,
                        quantity = 100,
                        description = "100 Hoarding XP"
                    )
                ),
                questGiverNpc = "The Quailsmith",
                recommendedLevel = 3
            )
        )
        
        // Quest 12: The Fading Elder
        registerQuest(
            Quest(
                questId = QuestId("quest_fading_elder"),
                title = "The Fading Elder",
                description = "Elder Bristle is sick and fading. Find the legendary Sunsgrace Flower to save him.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "find_sunsgrace",
                        description = "Find Sunsgrace Flower at dawn on the mountain",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "ingredient_sunsgrace_flower",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "brew_vitality",
                        description = "Brew Potion of Vitality",
                        type = QuestObjectiveType.CRAFT_ITEM,
                        targetId = "recipe_potion_of_vitality",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("find_sunsgrace")
                    ),
                    QuestObjective(
                        objectiveId = "give_to_bristle",
                        description = "Give the potion to Elder Bristle",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_elder_bristle",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("brew_vitality")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.THOUGHT,
                        targetId = "thought_elder_wisdom",
                        quantity = 1,
                        description = "Unlocks Elder Wisdom thought"
                    ),
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_snippet_the_first_quails",
                        quantity = 1,
                        description = "Unlocks 'The First Quails' lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.SCHOLARSHIP.name,
                        quantity = 200,
                        description = "200 Scholarship XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.ALCHEMY.name,
                        quantity = 150,
                        description = "150 Alchemy XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 1000,
                        description = "1000 Seeds"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.PrerequisiteQuest(QuestId("quest_soothing_silence"))
                ),
                questGiverNpc = "Elder Bristle",
                turnInNpc = "Elder Bristle",
                recommendedLevel = 4
            )
        )
        
        // Quest 13: The Chameleon's Challenge
        registerQuest(
            Quest(
                questId = QuestId("quest_chameleon_challenge"),
                title = "The Chameleon's Challenge",
                description = "A magpie stole Pack Rat's lens. Brew an invisibility potion to sneak into the nest and steal it back.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "brew_invisibility",
                        description = "Brew Potion of Invisibility",
                        type = QuestObjectiveType.CRAFT_ITEM,
                        targetId = "recipe_potion_of_invisibility",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "infiltrate_nest",
                        description = "Retrieve Pack Rat's Lens from the Magpie Nest",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "quest_item_pack_rats_lens",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("brew_invisibility")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.RECIPE,
                        targetId = "recipe_potion_of_invisibility",
                        quantity = 1,
                        description = "You keep the recipe"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.ALCHEMY.name,
                        quantity = 200,
                        description = "200 Alchemy XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.HOARDING.name,
                        quantity = 100,
                        description = "100 Hoarding XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 750,
                        description = "750 Seeds"
                    )
                ),
                questGiverNpc = "Pack Rat",
                turnInNpc = "Pack Rat",
                recommendedLevel = 5
            )
        )
        
        // Quest 14: The Poisoned Grove
        registerQuest(
            Quest(
                questId = QuestId("quest_poisoned_grove"),
                title = "The Poisoned Grove",
                description = "A beautiful grove has become toxic and blighted. Find a way to cleanse the land.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "find_catalyst",
                        description = "Find the Cleansing Catalyst in the Ruins",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "item_cleansing_catalyst",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "brew_cleansing",
                        description = "Brew Potion of Cleansing",
                        type = QuestObjectiveType.CRAFT_ITEM,
                        targetId = "recipe_potion_of_cleansing",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("find_catalyst")
                    ),
                    QuestObjective(
                        objectiveId = "cleanse_grove",
                        description = "Use the potion at the heart of the Grove",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("brew_cleansing")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.FORAGING.name,
                        quantity = 300,
                        description = "300 Foraging XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.ALCHEMY.name,
                        quantity = 150,
                        description = "150 Alchemy XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 500,
                        description = "500 Seeds"
                    )
                ),
                questGiverNpc = "Herbalist Hoot",
                recommendedLevel = 4
            )
        )
        
        // Quest 15: The Hoarder's Exam
        registerQuest(
            Quest(
                questId = QuestId("quest_hoarders_exam"),
                title = "The Hoarder's Exam",
                description = "Pack Rat challenges you to prove you're a true hoarder by collecting one shiny of each rarity.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "show_collection",
                        description = "Possess one shiny of each rarity (Common, Uncommon, Rare, Epic)",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 1
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.THOUGHT,
                        targetId = "thought_hoarders_instinct",
                        quantity = 1,
                        description = "Unlocks Hoarder's Instinct thought"
                    ),
                    QuestReward(
                        type = QuestRewardType.ITEMS,
                        targetId = "equipment_hoarders_eyeglass",
                        quantity = 1,
                        description = "Hoarder's Eyeglass"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.HOARDING.name,
                        quantity = 300,
                        description = "300 Hoarding XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 1000,
                        description = "1000 Seeds"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.MinimumSkill(SkillType.HOARDING, 4)
                ),
                questGiverNpc = "Pack Rat",
                turnInNpc = "Pack Rat",
                recommendedLevel = 4
            )
        )
        
        // Quest 16: The Silent Scholar
        registerQuest(
            Quest(
                questId = QuestId("quest_silent_scholar"),
                title = "The Silent Scholar",
                description = "Quill hasn't spoken in days, lost in deep contemplation. Brew a Potion of Clarity to help him.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "brew_clarity",
                        description = "Brew Potion of Clarity",
                        type = QuestObjectiveType.CRAFT_ITEM,
                        targetId = "recipe_potion_of_clarity",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "help_quill",
                        description = "Give the potion to Quill",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_quill",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("brew_clarity")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.THOUGHT,
                        targetId = "thought_shared_insight",
                        quantity = 1,
                        description = "Unlocks Shared Insight thought"
                    ),
                    QuestReward(
                        type = QuestRewardType.RECIPE,
                        targetId = "recipe_potion_of_clarity",
                        quantity = 1,
                        description = "You keep the recipe"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.SCHOLARSHIP.name,
                        quantity = 300,
                        description = "300 Scholarship XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.ALCHEMY.name,
                        quantity = 100,
                        description = "100 Alchemy XP"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.MinimumSkill(SkillType.SCHOLARSHIP, 3)
                ),
                questGiverNpc = "Quill",
                turnInNpc = "Quill",
                recommendedLevel = 3
            )
        )
        
        // Quest 17: The Barter's Challenge
        registerQuest(
            Quest(
                questId = QuestId("quest_barters_challenge"),
                title = "The Barter's Challenge",
                description = "Wicker challenges you to trade 10 Common Herbs up to 1 Rare Metal through a chain of NPCs.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "complete_trade_chain",
                        description = "Trade up to acquire 1 Rare Metal",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "rare_metal_ingredient",
                        targetQuantity = 1
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.BARTERING.name,
                        quantity = 400,
                        description = "400 Bartering XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 1000,
                        description = "1000 Seeds"
                    )
                ),
                questGiverNpc = "Worried Wicker",
                turnInNpc = "Worried Wicker",
                recommendedLevel = 3
            )
        )
        
        // Quest 18: The Territorial Crow
        registerQuest(
            Quest(
                questId = QuestId("quest_territorial_crow"),
                title = "The Territorial Crow",
                description = "A territorial crow has taken over a berry patch. Either craft armor to fight it or a distraction to lure it away.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "deal_with_crow",
                        description = "Deal with the crow at Crow's Perch",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 1
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.FORAGING.name,
                        quantity = 200,
                        description = "200 Crafting XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 600,
                        description = "600 Seeds"
                    )
                ),
                questGiverNpc = "Bullied Barry",
                turnInNpc = "Bullied Barry",
                recommendedLevel = 3
            )
        )
        
        // Quest 19: The Enlightenment Project
        registerQuest(
            Quest(
                questId = QuestId("quest_enlightenment_project"),
                title = "The Enlightenment Project",
                description = "Quill wants to help you achieve true Enlightenment. Prove your mastery by crafting 6 Essences.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "craft_foraging_essence",
                        description = "Craft Essence of Foraging",
                        type = QuestObjectiveType.CRAFT_ITEM,
                        targetId = "recipe_foraging_essence",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "craft_alchemy_essence",
                        description = "Craft Essence of Alchemy",
                        type = QuestObjectiveType.CRAFT_ITEM,
                        targetId = "recipe_alchemy_essence",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "craft_combat_essence",
                        description = "Craft Essence of Combat",
                        type = QuestObjectiveType.CRAFT_ITEM,
                        targetId = "recipe_combat_essence",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "craft_barter_essence",
                        description = "Craft Essence of Bartering",
                        type = QuestObjectiveType.CRAFT_ITEM,
                        targetId = "recipe_barter_essence",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "craft_hoard_essence",
                        description = "Craft Essence of Hoarding",
                        type = QuestObjectiveType.CRAFT_ITEM,
                        targetId = "recipe_hoard_essence",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "craft_scholar_essence",
                        description = "Craft Essence of Scholarship",
                        type = QuestObjectiveType.CRAFT_ITEM,
                        targetId = "recipe_scholar_essence",
                        targetQuantity = 1
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.EXPERIENCE,
                        quantity = 1000,
                        description = "1000 XP for all skills"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.MinimumSkill(SkillType.FORAGING, 10),
                    QuestRequirement.MinimumSkill(SkillType.ALCHEMY, 10),
                    QuestRequirement.MinimumSkill(SkillType.COMBAT, 10),
                    QuestRequirement.MinimumSkill(SkillType.BARTERING, 10),
                    QuestRequirement.MinimumSkill(SkillType.HOARDING, 10),
                    QuestRequirement.MinimumSkill(SkillType.SCHOLARSHIP, 10)
                ),
                questGiverNpc = "Quill",
                turnInNpc = "Quill",
                recommendedLevel = 10
            )
        )
        
        // Quest 20: The Feathered Friend
        registerQuest(
            Quest(
                questId = QuestId("quest_feathered_friend"),
                title = "The Feathered Friend",
                description = "You found a lost quail chick in the forest. Find a safe place for them.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "find_chick",
                        description = "Find the lost chick in the Forest",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "forest",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "bring_to_orphanage",
                        description = "Bring the chick to Matron Nester",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_matron_nester",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("find_chick")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.COMPANION_AFFINITY,
                        targetId = "npc_chickadee",
                        quantity = 1,
                        description = "Chickadee becomes your companion"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 300,
                        description = "300 Seeds"
                    )
                ),
                recommendedLevel = 1
            )
        )
    }
}
