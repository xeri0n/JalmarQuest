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
        
        // ========== FOREST REGIONAL QUESTS ==========
        
        // Quest 21: The Mycelial Network
        registerQuest(
            Quest(
                questId = QuestId("quest_mycelial_network"),
                title = "The Mycelial Network",
                description = "The Mushroom Sage speaks of an ancient fungal network beneath the forest. Help them map its extent.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "collect_spore_samples",
                        description = "Collect mushroom spores from 5 different locations",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "mushroom_spore",
                        targetQuantity = 5
                    ),
                    QuestObjective(
                        objectiveId = "plant_spore_markers",
                        description = "Plant spore markers at network nodes",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 3,
                        prerequisiteObjectives = listOf("collect_spore_samples")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_mycelial_network",
                        quantity = 1,
                        description = "Unlock: The Mycelial Network lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.SCHOLARSHIP.name,
                        quantity = 200,
                        description = "200 Scholarship XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 600,
                        description = "600 Seeds"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.MinimumSkill(SkillType.FORAGING, 4)
                ),
                questGiverNpc = "Mushroom Sage",
                turnInNpc = "Mushroom Sage",
                recommendedLevel = 4
            )
        )
        
        // Quest 22: Spirits of the Wood
        registerQuest(
            Quest(
                questId = QuestId("quest_spirits_wood"),
                title = "Spirits of the Wood",
                description = "The Whispering Pines speak of ancient forest spirits. Find evidence of their existence.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "find_spirit_inscription",
                        description = "Find the ancient inscription at Whispering Pines",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "whispering_pines",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "collect_spirit_essence",
                        description = "Collect Spirit Essence (only appears at dawn)",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "spirit_essence",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("find_spirit_inscription")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_spirits_wood",
                        quantity = 1,
                        description = "Unlock: Spirits of the Wood lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.THOUGHT,
                        targetId = "thought_forest_communion",
                        quantity = 1,
                        description = "Unlock thought: Forest Communion (+15% Foraging XP in forests)"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 700,
                        description = "700 Seeds"
                    )
                ),
                questGiverNpc = "Elder Bristle",
                turnInNpc = "Elder Bristle",
                recommendedLevel = 5
            )
        )
        
        // Quest 23: The Crow King's Decree
        registerQuest(
            Quest(
                questId = QuestId("quest_crow_king_decree"),
                title = "The Crow King's Decree",
                description = "A mysterious decree has been posted at the Crow's Perch. Investigate its meaning.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "read_decree",
                        description = "Read the decree at Crow's Perch",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "crow_perch",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "gather_tribute",
                        description = "Gather shiny tribute for the Crow King",
                        type = QuestObjectiveType.ACCUMULATE_SEEDS,
                        targetQuantity = 1000,
                        prerequisiteObjectives = listOf("read_decree")
                    ),
                    QuestObjective(
                        objectiveId = "deliver_tribute",
                        description = "Deliver tribute to the Crow King",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_crow_king",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("gather_tribute")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_crow_king_decree",
                        quantity = 1,
                        description = "Unlock: The Crow King's Decree lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.FACTION_REPUTATION,
                        targetId = "faction_crow_kingdom",
                        quantity = 30,
                        description = "+30 Crow Kingdom reputation"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 500,
                        description = "500 Seeds (net -500 after tribute)"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.PrerequisiteQuest(QuestId("quest_territorial_crow"))
                ),
                questGiverNpc = "Scout Whisper",
                turnInNpc = "Crow King",
                recommendedLevel = 6
            )
        )
        
        // ========== BEACH REGIONAL QUESTS ==========
        
        // Quest 24: The Captain's Last Entry
        registerQuest(
            Quest(
                questId = QuestId("quest_captain_last_entry"),
                title = "The Captain's Last Entry",
                description = "A wrecked ship on the beach holds a captain's logbook. Discover what happened to the crew.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "find_logbook",
                        description = "Find the Captain's Logbook in the Shipwreck",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "shipwreck",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "decipher_entries",
                        description = "Have the logbook deciphered (requires Scholarship 5)",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("find_logbook")
                    ),
                    QuestObjective(
                        objectiveId = "find_crew_remains",
                        description = "Find evidence of the crew's fate",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "crew_burial_site",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("decipher_entries")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_captain_last_entry",
                        quantity = 1,
                        description = "Unlock: Captain's Last Entry lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.SHINY,
                        targetId = "captain_compass",
                        quantity = 1,
                        description = "Captain's Compass (Epic shiny, 2200 Seeds)"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.SCHOLARSHIP.name,
                        quantity = 300,
                        description = "300 Scholarship XP"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.MinimumSkill(SkillType.SCHOLARSHIP, 5)
                ),
                questGiverNpc = "Old Sailor",
                turnInNpc = "Old Sailor",
                recommendedLevel = 5
            )
        )
        
        // Quest 25: The Eternal Keepers
        registerQuest(
            Quest(
                questId = QuestId("quest_eternal_keepers"),
                title = "The Eternal Keepers",
                description = "The Lighthouse Keeper speaks of an ancient order that protected the coast. Help restore their legacy.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "talk_to_keeper",
                        description = "Learn about the Eternal Keepers",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_lighthouse_keeper",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "find_old_beacons",
                        description = "Find 3 ancient beacon locations",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "ancient_beacon",
                        targetQuantity = 3,
                        prerequisiteObjectives = listOf("talk_to_keeper")
                    ),
                    QuestObjective(
                        objectiveId = "craft_beacon_fuel",
                        description = "Craft Eternal Flame fuel",
                        type = QuestObjectiveType.CRAFT_ITEM,
                        targetId = "recipe_eternal_flame",
                        targetQuantity = 3,
                        prerequisiteObjectives = listOf("find_old_beacons")
                    ),
                    QuestObjective(
                        objectiveId = "light_beacons",
                        description = "Light all 3 beacons",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 3,
                        prerequisiteObjectives = listOf("craft_beacon_fuel")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_eternal_keepers",
                        quantity = 1,
                        description = "Unlock: The Eternal Keepers lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.RECIPE,
                        targetId = "recipe_eternal_flame",
                        quantity = 1,
                        description = "Keep Eternal Flame recipe"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 800,
                        description = "800 Seeds"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.MinimumSkill(SkillType.ALCHEMY, 6)
                ),
                questGiverNpc = "Lighthouse Keeper",
                turnInNpc = "Lighthouse Keeper",
                recommendedLevel = 7
            )
        )
        
        // Quest 26: Miniature Oceans
        registerQuest(
            Quest(
                questId = QuestId("quest_miniature_oceans"),
                title = "Miniature Oceans",
                description = "Tide pools contain entire ecosystems. Study them for scientific understanding.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "observe_tide_pools",
                        description = "Observe 5 different tide pool creatures",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 5
                    ),
                    QuestObjective(
                        objectiveId = "collect_samples",
                        description = "Collect water samples from tide pools",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "tide_pool_sample",
                        targetQuantity = 10,
                        prerequisiteObjectives = listOf("observe_tide_pools")
                    ),
                    QuestObjective(
                        objectiveId = "analyze_samples",
                        description = "Analyze samples at Alchemy Lab",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("collect_samples")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_miniature_oceans",
                        quantity = 1,
                        description = "Unlock: Miniature Oceans lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.SCHOLARSHIP.name,
                        quantity = 250,
                        description = "250 Scholarship XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.FORAGING.name,
                        quantity = 150,
                        description = "150 Foraging XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 600,
                        description = "600 Seeds"
                    )
                ),
                questGiverNpc = "Beach Scholar",
                turnInNpc = "Beach Scholar",
                recommendedLevel = 4
            )
        )
        
        // ========== SWAMP REGIONAL QUESTS ==========
        
        // Quest 27: The Witch of the Swamp
        registerQuest(
            Quest(
                questId = QuestId("quest_witch_swamp"),
                title = "The Witch of the Swamp",
                description = "The Bog Witch is said to possess ancient alchemical knowledge. Earn her trust.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "find_bog_witch",
                        description = "Find the Bog Witch's hut deep in the swamp",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "bog_witch_hut",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "gather_rare_ingredients",
                        description = "Gather rare swamp ingredients for the witch",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "swamp_moss",
                        targetQuantity = 10,
                        prerequisiteObjectives = listOf("find_bog_witch")
                    ),
                    QuestObjective(
                        objectiveId = "brew_forgotten_potion",
                        description = "Learn and brew the Forgotten Remedy",
                        type = QuestObjectiveType.CRAFT_ITEM,
                        targetId = "recipe_forgotten_remedy",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("gather_rare_ingredients")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_bog_witch",
                        quantity = 1,
                        description = "Unlock: The Witch of the Swamp lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.RECIPE,
                        targetId = "recipe_forgotten_remedy",
                        quantity = 1,
                        description = "Learn Forgotten Remedy (removes all debuffs)"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.ALCHEMY.name,
                        quantity = 400,
                        description = "400 Alchemy XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 1000,
                        description = "1000 Seeds"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.MinimumSkill(SkillType.ALCHEMY, 8)
                ),
                questGiverNpc = "Bog Witch",
                turnInNpc = "Bog Witch",
                recommendedLevel = 9
            )
        )
        
        // Quest 28: The Language of Light
        registerQuest(
            Quest(
                questId = QuestId("quest_language_light"),
                title = "The Language of Light",
                description = "The Firefly Queen communicates through bioluminescent patterns. Learn her language.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "observe_fireflies",
                        description = "Observe firefly patterns at night (10 PM - 2 AM)",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 5
                    ),
                    QuestObjective(
                        objectiveId = "decode_patterns",
                        description = "Decode the light patterns (requires Scholarship 6)",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("observe_fireflies")
                    ),
                    QuestObjective(
                        objectiveId = "meet_firefly_queen",
                        description = "Meet the Firefly Queen in her grove",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_firefly_queen",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("decode_patterns")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_language_light",
                        quantity = 1,
                        description = "Unlock: The Language of Light lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.FACTION_REPUTATION,
                        targetId = "faction_insect_kingdom",
                        quantity = 50,
                        description = "+50 Insect Kingdom reputation"
                    ),
                    QuestReward(
                        type = QuestRewardType.ABILITY,
                        targetId = "ability_firefly_beacon",
                        quantity = 1,
                        description = "Unlock ability: Firefly Beacon (summon light at night)"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 900,
                        description = "900 Seeds"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.MinimumSkill(SkillType.SCHOLARSHIP, 6)
                ),
                questGiverNpc = "Swamp Guide",
                turnInNpc = "Firefly Queen",
                recommendedLevel = 7
            )
        )
        
        // Quest 29: The Ancient Guardian
        registerQuest(
            Quest(
                questId = QuestId("quest_ancient_guardian"),
                title = "The Ancient Guardian",
                description = "An ancient gator guards the deepest part of the swamp. Face this legendary creature.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "find_gator_den",
                        description = "Locate the Ancient Guardian's den",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "ancient_gator_den",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "defeat_guardian",
                        description = "Defeat or outwit the Ancient Guardian",
                        type = QuestObjectiveType.DEFEAT_ENEMIES,
                        targetId = "boss_ancient_gator",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("find_gator_den")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_ancient_guardian",
                        quantity = 1,
                        description = "Unlock: The Swamp's Ancient Guardian lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.SHINY,
                        targetId = "gator_scale_trophy",
                        quantity = 1,
                        description = "Gator Scale Trophy (Legendary shiny, 5000 Seeds)"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.COMBAT.name,
                        quantity = 500,
                        description = "500 Combat XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 1500,
                        description = "1500 Seeds"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.MinimumSkill(SkillType.COMBAT, 10)
                ),
                questGiverNpc = "Swamp Elder",
                turnInNpc = "Swamp Elder",
                recommendedLevel = 12
            )
        )
        
        // ========== RUINS REGIONAL QUESTS ==========
        
        // Quest 30: The Builders' Purpose
        registerQuest(
            Quest(
                questId = QuestId("quest_builders_purpose"),
                title = "The Builders' Purpose",
                description = "The ruins were built by an ancient civilization. Help the Archaeologist uncover their purpose.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "survey_ruins",
                        description = "Survey 5 different ruin sites",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "ruin_site",
                        targetQuantity = 5
                    ),
                    QuestObjective(
                        objectiveId = "collect_artifacts",
                        description = "Collect ancient artifacts",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "ancient_artifact",
                        targetQuantity = 8,
                        prerequisiteObjectives = listOf("survey_ruins")
                    ),
                    QuestObjective(
                        objectiveId = "analyze_artifacts",
                        description = "Have artifacts analyzed by the Archaeologist",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_archaeologist",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("collect_artifacts")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_builders_purpose",
                        quantity = 1,
                        description = "Unlock: The Builders' Purpose lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.SCHOLARSHIP.name,
                        quantity = 350,
                        description = "350 Scholarship XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 800,
                        description = "800 Seeds"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.MinimumSkill(SkillType.SCHOLARSHIP, 7)
                ),
                questGiverNpc = "Archaeologist",
                turnInNpc = "Archaeologist",
                recommendedLevel = 8
            )
        )
        
        // Quest 31: The Eternal Librarian
        registerQuest(
            Quest(
                questId = QuestId("quest_eternal_librarian"),
                title = "The Eternal Librarian",
                description = "A ghostly scribe haunts the ancient library. Help them complete their eternal task.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "find_library",
                        description = "Find the ancient library in the ruins",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "ancient_library",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "talk_to_scribe",
                        description = "Speak with the Ghost Scribe",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_ghost_scribe",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("find_library")
                    ),
                    QuestObjective(
                        objectiveId = "retrieve_lost_tomes",
                        description = "Retrieve 5 lost tomes from the ruins",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "ancient_tome",
                        targetQuantity = 5,
                        prerequisiteObjectives = listOf("talk_to_scribe")
                    ),
                    QuestObjective(
                        objectiveId = "restore_library",
                        description = "Return the tomes to the library",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_ghost_scribe",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("retrieve_lost_tomes")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_eternal_librarian",
                        quantity = 1,
                        description = "Unlock: The Eternal Librarian lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.THOUGHT,
                        targetId = "thought_ancient_knowledge",
                        quantity = 1,
                        description = "Unlock thought: Ancient Knowledge (+30% Scholarship XP)"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 1000,
                        description = "1000 Seeds"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.MinimumSkill(SkillType.SCHOLARSHIP, 8)
                ),
                questGiverNpc = "Archaeologist",
                turnInNpc = "Ghost Scribe",
                recommendedLevel = 9
            )
        )
        
        // Quest 32: The Forbidden Knowledge
        registerQuest(
            Quest(
                questId = QuestId("quest_forbidden_knowledge"),
                title = "That Which Should Not Be Known",
                description = "Deep in the library lies a book of forbidden knowledge. Will you dare to read it?",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "unlock_forbidden_section",
                        description = "Find the key to the Forbidden Section",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "forbidden_library_key",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "enter_forbidden_section",
                        description = "Enter the Forbidden Section",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "forbidden_library",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("unlock_forbidden_section")
                    ),
                    QuestObjective(
                        objectiveId = "read_forbidden_tome",
                        description = "Read the Forbidden Tome (WARNING: Dangerous)",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("enter_forbidden_section")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_forbidden_knowledge",
                        quantity = 1,
                        description = "Unlock: That Which Should Not Be Known lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.THOUGHT,
                        targetId = "thought_forbidden_insight",
                        quantity = 1,
                        description = "Unlock thought: Forbidden Insight (+50% XP, -20% health)"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.SCHOLARSHIP.name,
                        quantity = 500,
                        description = "500 Scholarship XP"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.PrerequisiteQuest(QuestId("quest_eternal_librarian")),
                    QuestRequirement.MinimumSkill(SkillType.SCHOLARSHIP, 12)
                ),
                questGiverNpc = "Ghost Scribe",
                turnInNpc = "Ghost Scribe",
                recommendedLevel = 13
            )
        )
        
        // Quest 33: The Mosaic Prophecy
        registerQuest(
            Quest(
                questId = QuestId("quest_mosaic_prophecy"),
                title = "The Mosaic Prophecy",
                description = "An ancient mosaic in the ruins depicts a prophecy. Decipher its meaning.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "find_mosaic_hall",
                        description = "Find the Mosaic Hall in the ruins",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "mosaic_hall",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "study_mosaic",
                        description = "Study the mosaic patterns",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("find_mosaic_hall")
                    ),
                    QuestObjective(
                        objectiveId = "find_matching_artifacts",
                        description = "Find artifacts matching the prophecy symbols",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "prophecy_artifact",
                        targetQuantity = 4,
                        prerequisiteObjectives = listOf("study_mosaic")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_mosaic_prophecy",
                        quantity = 1,
                        description = "Unlock: The Mosaic Prophecy lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.SCHOLARSHIP.name,
                        quantity = 300,
                        description = "300 Scholarship XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 900,
                        description = "900 Seeds"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.MinimumSkill(SkillType.SCHOLARSHIP, 10)
                ),
                questGiverNpc = "Archaeologist",
                turnInNpc = "Archaeologist",
                recommendedLevel = 11
            )
        )
        
        // ========== MOUNTAIN REGIONAL QUESTS ==========
        
        // Quest 34: The Singing Crystals
        registerQuest(
            Quest(
                questId = QuestId("quest_singing_crystals"),
                title = "The Singing Crystals",
                description = "The crystal caves in the mountains produce harmonic resonances. Study this phenomenon.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "find_crystal_caves",
                        description = "Find the Crystal Caves in the mountains",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "crystal_caves",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "record_crystal_songs",
                        description = "Record 5 different crystal harmonies",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 5,
                        prerequisiteObjectives = listOf("find_crystal_caves")
                    ),
                    QuestObjective(
                        objectiveId = "harvest_resonant_crystals",
                        description = "Harvest Resonant Crystals",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "resonant_crystal",
                        targetQuantity = 3,
                        prerequisiteObjectives = listOf("record_crystal_songs")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_singing_crystals",
                        quantity = 1,
                        description = "Unlock: The Singing Crystals lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.SHINY,
                        targetId = "harmonic_crystal",
                        quantity = 1,
                        description = "Harmonic Crystal (Rare shiny, 1500 Seeds)"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.FORAGING.name,
                        quantity = 250,
                        description = "250 Foraging XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 700,
                        description = "700 Seeds"
                    )
                ),
                questGiverNpc = "Mountain Guide",
                turnInNpc = "Mountain Guide",
                recommendedLevel = 7
            )
        )
        
        // Quest 35: Queen of the Skies
        registerQuest(
            Quest(
                questId = QuestId("quest_queen_skies"),
                title = "Queen of the Skies",
                description = "The legendary Eagle Queen rules the mountain peaks. Seek an audience with her.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "prepare_offering",
                        description = "Gather a worthy offering (10 shiny items)",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 10
                    ),
                    QuestObjective(
                        objectiveId = "climb_to_summit",
                        description = "Climb to the mountain summit",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "mountain_summit",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("prepare_offering")
                    ),
                    QuestObjective(
                        objectiveId = "meet_eagle_queen",
                        description = "Speak with the Eagle Queen",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_eagle_queen",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("climb_to_summit")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_queen_skies",
                        quantity = 1,
                        description = "Unlock: Queen of the Skies lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.FACTION_REPUTATION,
                        targetId = "faction_mountain_eagles",
                        quantity = 75,
                        description = "+75 Mountain Eagles reputation"
                    ),
                    QuestReward(
                        type = QuestRewardType.ABILITY,
                        targetId = "ability_eagle_vision",
                        quantity = 1,
                        description = "Unlock ability: Eagle Vision (reveal distant locations)"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 1200,
                        description = "1200 Seeds"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.MinimumSkill(SkillType.HOARDING, 10)
                ),
                questGiverNpc = "Mountain Guide",
                turnInNpc = "Eagle Queen",
                recommendedLevel = 12
            )
        )
        
        // Quest 36: The Hermit's Enlightenment
        registerQuest(
            Quest(
                questId = QuestId("quest_hermit_enlightenment"),
                title = "The Hermit's Enlightenment",
                description = "A wise hermit lives in isolation on the mountain. Seek their wisdom.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "find_hermit_cave",
                        description = "Find the Mountain Hermit's cave",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "hermit_cave",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "gain_hermit_trust",
                        description = "Gain the Hermit's trust (requires affinity 50+)",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_mountain_hermit",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("find_hermit_cave")
                    ),
                    QuestObjective(
                        objectiveId = "complete_meditation",
                        description = "Complete a meditation at the peak (requires thought cabinet)",
                        type = QuestObjectiveType.INTERNALIZE_THOUGHT,
                        targetId = "thought_mountain_meditation",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("gain_hermit_trust")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_hermit_enlightenment",
                        quantity = 1,
                        description = "Unlock: The Hermit's Enlightenment lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.THOUGHT,
                        targetId = "thought_mountain_wisdom",
                        quantity = 1,
                        description = "Unlock thought: Mountain Wisdom (+25% all XP at high altitudes)"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 1500,
                        description = "1500 Seeds"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.PrerequisiteQuest(QuestId("quest_queen_skies"))
                ),
                questGiverNpc = "Mountain Guide",
                turnInNpc = "Mountain Hermit",
                recommendedLevel = 13
            )
        )
        
        // Quest 37: The View from Above
        registerQuest(
            Quest(
                questId = QuestId("quest_view_above"),
                title = "The View from Above",
                description = "Reach the highest peak and witness the world from a quail's ultimate perspective.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "climb_highest_peak",
                        description = "Reach the highest mountain peak",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "highest_peak",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "survey_lands",
                        description = "Survey all 6 regions from the peak",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 6,
                        prerequisiteObjectives = listOf("climb_highest_peak")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_view_above",
                        quantity = 1,
                        description = "Unlock: The View from Above lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.ABILITY,
                        targetId = "ability_fast_travel",
                        quantity = 1,
                        description = "Unlock fast travel between discovered regions"
                    ),
                    QuestReward(
                        type = QuestRewardType.EXPERIENCE,
                        quantity = 1000,
                        description = "1000 XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 2000,
                        description = "2000 Seeds"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.PrerequisiteQuest(QuestId("quest_hermit_enlightenment"))
                ),
                questGiverNpc = "Mountain Hermit",
                turnInNpc = "Mountain Hermit",
                recommendedLevel = 14
            )
        )
        
        // ========== BUTTONBURGH FACTION QUESTS ==========
        
        // Quest 38: The Founding of Buttonburgh
        registerQuest(
            Quest(
                questId = QuestId("quest_founding_buttonburgh"),
                title = "The Founding of Buttonburgh",
                description = "Learn about the history of Buttonburgh and its founder, Corvus the Great Quail.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "read_town_hall_inscription",
                        description = "Read the Town Hall inscription",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "town_hall",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "talk_to_historian",
                        description = "Speak with the Town Historian",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_town_historian",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("read_town_hall_inscription")
                    ),
                    QuestObjective(
                        objectiveId = "find_corvus_statue",
                        description = "Find Corvus's statue in Scholars District",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "corvus_statue",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("talk_to_historian")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_founding_buttonburgh",
                        quantity = 1,
                        description = "Unlock: The Founding of Buttonburgh lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.FACTION_REPUTATION,
                        targetId = "faction_buttonburgh",
                        quantity = 25,
                        description = "+25 Buttonburgh reputation"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 400,
                        description = "400 Seeds"
                    )
                ),
                questGiverNpc = "Elder Quill",
                turnInNpc = "Town Historian",
                recommendedLevel = 2
            )
        )
        
        // Quest 39: The Ancients Who Came Before
        registerQuest(
            Quest(
                questId = QuestId("quest_ancients_before"),
                title = "The Ancients Who Came Before",
                description = "Discover what civilization existed before the quails in Buttonburgh.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "access_scholars_library",
                        description = "Gain access to Scholars District library (Level 3+)",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "scholars_library",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "read_ancient_history",
                        description = "Read the Ancient History volumes",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 3,
                        prerequisiteObjectives = listOf("access_scholars_library")
                    ),
                    QuestObjective(
                        objectiveId = "excavate_foundations",
                        description = "Excavate beneath Buttonburgh's foundations",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("read_ancient_history")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_ancients_before",
                        quantity = 1,
                        description = "Unlock: The Ancients Who Came Before lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.SCHOLARSHIP.name,
                        quantity = 400,
                        description = "400 Scholarship XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 800,
                        description = "800 Seeds"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.MinimumLevel(3),
                    QuestRequirement.PrerequisiteQuest(QuestId("quest_founding_buttonburgh"))
                ),
                questGiverNpc = "Town Historian",
                turnInNpc = "Town Historian",
                recommendedLevel = 5
            )
        )
        
        // Quest 40: Elder Quill's Youth
        registerQuest(
            Quest(
                questId = QuestId("quest_elder_quill_youth"),
                title = "Elder Quill's Youth",
                description = "Elder Quill has stories from their adventurous youth. Earn their trust to hear them.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "gain_quill_affinity",
                        description = "Gain Elder Quill's trust (affinity 50+)",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_elder_quill",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "visit_old_haunts",
                        description = "Visit locations from Quill's youth",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "quill_memory_location",
                        targetQuantity = 4,
                        prerequisiteObjectives = listOf("gain_quill_affinity")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_elder_quill_youth",
                        quantity = 1,
                        description = "Unlock: Elder Quill's Youth lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.THOUGHT,
                        targetId = "thought_elder_wisdom",
                        quantity = 1,
                        description = "Unlock thought: Elder's Wisdom (+15% XP from quests)"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 600,
                        description = "600 Seeds"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.ArchetypeRequirement(ArchetypeType.SCHOLAR)
                ),
                questGiverNpc = "Elder Quill",
                turnInNpc = "Elder Quill",
                recommendedLevel = 6
            )
        )
        
        // ========== DAILY/REPEATABLE QUESTS ==========
        
        // Quest 41: Daily Herb Gathering
        registerQuest(
            Quest(
                questId = QuestId("daily_herb_gathering"),
                title = "Daily Herb Gathering",
                description = "The Herbalist needs a steady supply of herbs. Help gather them daily.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "gather_herbs",
                        description = "Gather 15 herbs of any type",
                        type = QuestObjectiveType.COLLECT_ITEMS,
                        targetId = "herb",
                        targetQuantity = 15
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 200,
                        description = "200 Seeds"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.FORAGING.name,
                        quantity = 50,
                        description = "50 Foraging XP"
                    )
                ),
                questGiverNpc = "Herbalist",
                turnInNpc = "Herbalist",
                isRepeatable = true,
                recommendedLevel = 1
            )
        )
        
        // Quest 42: Daily Combat Training
        registerQuest(
            Quest(
                questId = QuestId("daily_combat_training"),
                title = "Daily Combat Training",
                description = "Train your combat skills by defeating enemies in the wild.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "defeat_enemies",
                        description = "Defeat 10 enemies",
                        type = QuestObjectiveType.DEFEAT_ENEMIES,
                        targetQuantity = 10
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 250,
                        description = "250 Seeds"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.COMBAT.name,
                        quantity = 100,
                        description = "100 Combat XP"
                    )
                ),
                questGiverNpc = "Combat Trainer",
                turnInNpc = "Combat Trainer",
                isRepeatable = true,
                recommendedLevel = 3
            )
        )
        
        // Quest 43: Daily Crafting Challenge
        registerQuest(
            Quest(
                questId = QuestId("daily_crafting_challenge"),
                title = "Daily Crafting Challenge",
                description = "The Quailsmith challenges you to craft items daily.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "craft_items",
                        description = "Craft 3 items of any type",
                        type = QuestObjectiveType.CRAFT_ITEM,
                        targetQuantity = 3
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 300,
                        description = "300 Seeds"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.ALCHEMY.name,
                        quantity = 75,
                        description = "75 Alchemy XP"
                    )
                ),
                questGiverNpc = "Quailsmith",
                turnInNpc = "Quailsmith",
                isRepeatable = true,
                recommendedLevel = 2
            )
        )
        
        // Quest 44: Weekly Hoard Challenge
        registerQuest(
            Quest(
                questId = QuestId("weekly_hoard_challenge"),
                title = "Weekly Hoard Challenge",
                description = "Pack Rat challenges you to increase your hoard value each week.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "increase_hoard_value",
                        description = "Increase your hoard value by 5000 Seeds",
                        type = QuestObjectiveType.ACCUMULATE_SEEDS,
                        targetQuantity = 5000
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 1000,
                        description = "1000 Seeds"
                    ),
                    QuestReward(
                        type = QuestRewardType.SKILL_POINTS,
                        targetId = SkillType.HOARDING.name,
                        quantity = 200,
                        description = "200 Hoarding XP"
                    ),
                    QuestReward(
                        type = QuestRewardType.SHINY,
                        targetId = "random_rare_shiny",
                        quantity = 1,
                        description = "Random rare shiny"
                    )
                ),
                questGiverNpc = "Pack Rat",
                turnInNpc = "Pack Rat",
                isRepeatable = true,
                recommendedLevel = 5
            )
        )
        
        // ========== FACTION ALLIANCE QUESTS ==========
        
        // Quest 45: The Insect Treaty
        registerQuest(
            Quest(
                questId = QuestId("quest_insect_treaty"),
                title = "The Insect Treaty",
                description = "Broker a peace treaty between Buttonburgh and the Insect Kingdom.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "gain_insect_respect",
                        description = "Gain Insect Kingdom reputation (50+)",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 50
                    ),
                    QuestObjective(
                        objectiveId = "deliver_treaty_proposal",
                        description = "Deliver treaty proposal to the Ant Queen",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_ant_queen",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("gain_insect_respect")
                    ),
                    QuestObjective(
                        objectiveId = "return_to_council",
                        description = "Present the signed treaty to Buttonburgh Council",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_council_member",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("deliver_treaty_proposal")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.FACTION_REPUTATION,
                        targetId = "faction_buttonburgh",
                        quantity = 50,
                        description = "+50 Buttonburgh reputation"
                    ),
                    QuestReward(
                        type = QuestRewardType.FACTION_REPUTATION,
                        targetId = "faction_insect_kingdom",
                        quantity = 50,
                        description = "+50 Insect Kingdom reputation"
                    ),
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_insect_treaty",
                        quantity = 1,
                        description = "Unlock: The Insect Treaty lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 1500,
                        description = "1500 Seeds"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.PrerequisiteQuest(QuestId("quest_antbassador")),
                    QuestRequirement.MinimumSkill(SkillType.BARTERING, 8)
                ),
                questGiverNpc = "Elder Quill",
                turnInNpc = "Council Member",
                recommendedLevel = 10
            )
        )
        
        // Quest 46: The Crow Alliance
        registerQuest(
            Quest(
                questId = QuestId("quest_crow_alliance"),
                title = "The Crow Alliance",
                description = "Form an alliance with the Crow Kingdom to protect against greater threats.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "prove_worth_to_crows",
                        description = "Prove your worth to the Crow King",
                        type = QuestObjectiveType.COMPLETE_QUEST,
                        targetId = "quest_crow_king_decree",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "gather_alliance_gifts",
                        description = "Gather 20 shinies as alliance gifts",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 20,
                        prerequisiteObjectives = listOf("prove_worth_to_crows")
                    ),
                    QuestObjective(
                        objectiveId = "formal_alliance_ceremony",
                        description = "Attend the formal alliance ceremony",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_crow_king",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("gather_alliance_gifts")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.FACTION_REPUTATION,
                        targetId = "faction_crow_kingdom",
                        quantity = 75,
                        description = "+75 Crow Kingdom reputation"
                    ),
                    QuestReward(
                        type = QuestRewardType.ABILITY,
                        targetId = "ability_crow_flight",
                        quantity = 1,
                        description = "Unlock ability: Crow's Flight (longer flight distance)"
                    ),
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_crow_alliance",
                        quantity = 1,
                        description = "Unlock: The Crow Alliance lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 2000,
                        description = "2000 Seeds"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.PrerequisiteQuest(QuestId("quest_crow_king_decree")),
                    QuestRequirement.MinimumSkill(SkillType.HOARDING, 12)
                ),
                questGiverNpc = "Scout Whisper",
                turnInNpc = "Crow King",
                recommendedLevel = 14
            )
        )
        
        // ========== ENDGAME/SPECIAL QUESTS ==========
        
        // Quest 47: The Legend of Buried Treasure
        registerQuest(
            Quest(
                questId = QuestId("quest_buried_treasure"),
                title = "The Legend of Buried Treasure",
                description = "Tavern rumors speak of a legendary treasure hidden by ancient quails. Find it.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "hear_tavern_rumors",
                        description = "Listen to the Wandering Minstrel's stories",
                        type = QuestObjectiveType.TALK_TO_NPC,
                        targetId = "npc_wandering_minstrel",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "decipher_treasure_map",
                        description = "Decipher the ancient treasure map",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("hear_tavern_rumors")
                    ),
                    QuestObjective(
                        objectiveId = "visit_map_locations",
                        description = "Visit all 6 locations on the treasure map",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "treasure_map_location",
                        targetQuantity = 6,
                        prerequisiteObjectives = listOf("decipher_treasure_map")
                    ),
                    QuestObjective(
                        objectiveId = "find_buried_treasure",
                        description = "Excavate the final treasure location",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("visit_map_locations")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.SHINY,
                        targetId = "legendary_treasure_chest",
                        quantity = 1,
                        description = "Legendary Treasure Chest (contains 5 legendary shinies)"
                    ),
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_buried_treasure",
                        quantity = 1,
                        description = "Unlock: The Legend of Buried Treasure lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 5000,
                        description = "5000 Seeds"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.MinimumSkill(SkillType.SCHOLARSHIP, 10),
                    QuestRequirement.MinimumSkill(SkillType.HOARDING, 10)
                ),
                questGiverNpc = "Wandering Minstrel",
                turnInNpc = "Pack Rat",
                recommendedLevel = 15
            )
        )
        
        // Quest 48: The Ultimate Challenge
        registerQuest(
            Quest(
                questId = QuestId("quest_ultimate_challenge"),
                title = "The Ultimate Challenge",
                description = "Prove yourself as the greatest quail by mastering all skills and completing legendary tasks.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "master_all_skills",
                        description = "Reach level 10 in all 6 skills",
                        type = QuestObjectiveType.REACH_SKILL_LEVEL,
                        targetQuantity = 60 // 6 skills * 10 levels
                    ),
                    QuestObjective(
                        objectiveId = "collect_all_essences",
                        description = "Craft all 6 Mastery Essences",
                        type = QuestObjectiveType.COMPLETE_QUEST,
                        targetId = "quest_enlightenment_project",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("master_all_skills")
                    ),
                    QuestObjective(
                        objectiveId = "defeat_apex_predator",
                        description = "Defeat the Apex Predator boss",
                        type = QuestObjectiveType.DEFEAT_ENEMIES,
                        targetId = "boss_apex_predator",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("collect_all_essences")
                    ),
                    QuestObjective(
                        objectiveId = "reach_max_hoard_tier",
                        description = "Reach maximum hoard rank (Legendary tier)",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("defeat_apex_predator")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.COSMETIC,
                        targetId = "cosmetic_legendary_crown",
                        quantity = 1,
                        description = "Legendary Crown cosmetic"
                    ),
                    QuestReward(
                        type = QuestRewardType.THOUGHT,
                        targetId = "thought_ultimate_quail",
                        quantity = 1,
                        description = "Unlock thought: Ultimate Quail (+50% all stats)"
                    ),
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_ultimate_challenge",
                        quantity = 1,
                        description = "Unlock: The Ultimate Challenge lore"
                    ),
                    QuestReward(
                        type = QuestRewardType.EXPERIENCE,
                        quantity = 10000,
                        description = "10000 XP"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.MinimumLevel(15)
                ),
                questGiverNpc = "Elder Quill",
                turnInNpc = "Elder Quill",
                recommendedLevel = 20
            )
        )
        
        // Quest 49: The Secret of the Ancients
        registerQuest(
            Quest(
                questId = QuestId("quest_secret_ancients"),
                title = "The Secret of the Ancients",
                description = "Uncover the ultimate secret hidden in the ruins: the true nature of the world.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "discover_all_lore",
                        description = "Discover all 30+ lore objects",
                        type = QuestObjectiveType.DISCOVER_LORE,
                        targetQuantity = 30
                    ),
                    QuestObjective(
                        objectiveId = "unlock_secret_chamber",
                        description = "Unlock the Secret Chamber in the deepest ruins",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "secret_chamber",
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("discover_all_lore")
                    ),
                    QuestObjective(
                        objectiveId = "solve_ancient_puzzle",
                        description = "Solve the Ancient Puzzle (requires all lore knowledge)",
                        type = QuestObjectiveType.CUSTOM,
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("unlock_secret_chamber")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_secret_ancients",
                        quantity = 1,
                        description = "Unlock: The Secret of the Ancients (reveals hidden ending)"
                    ),
                    QuestReward(
                        type = QuestRewardType.ABILITY,
                        targetId = "ability_new_game_plus",
                        quantity = 1,
                        description = "Unlock New Game+ mode"
                    ),
                    QuestReward(
                        type = QuestRewardType.SEEDS,
                        quantity = 10000,
                        description = "10000 Seeds"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.PrerequisiteQuest(QuestId("quest_forbidden_knowledge")),
                    QuestRequirement.PrerequisiteQuest(QuestId("quest_ultimate_challenge"))
                ),
                questGiverNpc = "Ghost Scribe",
                turnInNpc = "Ghost Scribe",
                recommendedLevel = 20
            )
        )
        
        // Quest 50: The Path to Enlightenment (Final Quest)
        registerQuest(
            Quest(
                questId = QuestId("quest_path_enlightenment"),
                title = "The Path to Enlightenment",
                description = "Complete the final step on your journey: internalize the Enlightenment thought and transcend.",
                objectives = listOf(
                    QuestObjective(
                        objectiveId = "internalize_enlightenment",
                        description = "Internalize the Enlightenment thought",
                        type = QuestObjectiveType.INTERNALIZE_THOUGHT,
                        targetId = "thought_enlightenment",
                        targetQuantity = 1
                    ),
                    QuestObjective(
                        objectiveId = "visit_all_regions",
                        description = "Visit the heart of each region one final time",
                        type = QuestObjectiveType.REACH_LOCATION,
                        targetId = "region_heart",
                        targetQuantity = 6,
                        prerequisiteObjectives = listOf("internalize_enlightenment")
                    ),
                    QuestObjective(
                        objectiveId = "make_final_choice",
                        description = "Make your final choice at the World's End",
                        type = QuestObjectiveType.MAKE_CHOICE,
                        targetQuantity = 1,
                        prerequisiteObjectives = listOf("visit_all_regions")
                    )
                ),
                rewards = listOf(
                    QuestReward(
                        type = QuestRewardType.LORE_UNLOCK,
                        targetId = "lore_enlightenment",
                        quantity = 1,
                        description = "Unlock: The Path to Enlightenment (game ending)"
                    ),
                    QuestReward(
                        type = QuestRewardType.COSMETIC,
                        targetId = "cosmetic_enlightened_aura",
                        quantity = 1,
                        description = "Enlightened Aura cosmetic"
                    )
                ),
                requirements = listOf(
                    QuestRequirement.PrerequisiteQuest(QuestId("quest_enlightenment_project")),
                    QuestRequirement.PrerequisiteQuest(QuestId("quest_secret_ancients"))
                ),
                questGiverNpc = "Quill",
                turnInNpc = "Quill",
                recommendedLevel = 20
            )
        )
    }
}
