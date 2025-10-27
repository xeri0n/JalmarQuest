package com.jalmarquest.core.state.thoughts

import com.jalmarquest.core.model.*

/**
 * Service providing the catalog of all available thoughts.
 * Thoughts are philosophical concepts that can be discovered and internalized
 * for gameplay benefits and narrative depth.
 */
class ThoughtCatalogService {
    
    private val thoughts: Map<ThoughtId, Thought> = buildThoughtCatalog()
    
    /**
     * Get all thoughts in the catalog.
     */
    fun getAllThoughts(): List<Thought> = thoughts.values.toList()
    
    /**
     * Get a specific thought by ID.
     */
    fun getThought(id: ThoughtId): Thought? = thoughts[id]
    
    /**
     * Get thoughts that match a specific discovery condition.
     */
    fun getThoughtsByCondition(condition: ThoughtDiscoveryCondition): List<Thought> =
        thoughts.values.filter { it.discoveryCondition == condition }
    
    /**
     * Get starting thoughts (automatically discovered).
     */
    fun getStartingThoughts(): List<Thought> =
        thoughts.values.filter { it.discoveryCondition is ThoughtDiscoveryCondition.StartingThought }
    
    /**
     * Check if a thought can be discovered based on player state.
     */
    fun canDiscover(thought: Thought, player: Player): Boolean =
        evaluateCondition(thought.discoveryCondition, player)
    
    /**
     * Find all thoughts the player can currently discover (but hasn't yet).
     */
    fun getDiscoverableThoughts(player: Player): List<Thought> =
        thoughts.values.filter { thought ->
            !player.thoughtCabinet.hasDiscovered(thought.id) && canDiscover(thought, player)
        }
    
    /**
     * Recursively evaluate a discovery condition against player state.
     */
    private fun evaluateCondition(condition: ThoughtDiscoveryCondition, player: Player): Boolean =
        when (condition) {
            is ThoughtDiscoveryCondition.StartingThought -> true
            
            is ThoughtDiscoveryCondition.Milestone -> {
                // Check if player has completed the milestone (via choice log)
                player.choiceLog.entries.any { it.tag.value == "milestone_${condition.milestoneId}" }
            }
            
            is ThoughtDiscoveryCondition.Choice -> {
                // Check if player has made the specific choice
                player.choiceLog.entries.any { it.tag.value == condition.choiceTag }
            }
            
            is ThoughtDiscoveryCondition.Archetype -> {
                // Check if player has the archetype (via choice log or status effect)
                player.choiceLog.entries.any { it.tag.value == "archetype_${condition.archetypeId}" }
            }
            
            is ThoughtDiscoveryCondition.Faction -> {
                // TODO: Implement faction reputation system
                // For now, always return false
                false
            }
            
            is ThoughtDiscoveryCondition.All -> {
                // All conditions must be met
                condition.conditions.all { evaluateCondition(it, player) }
            }
            
            is ThoughtDiscoveryCondition.Any -> {
                // At least one condition must be met
                condition.conditions.any { evaluateCondition(it, player) }
            }
        }
    
    /**
     * Build the hardcoded thought catalog.
     */
    private fun buildThoughtCatalog(): Map<ThoughtId, Thought> {
        val thoughts = mutableListOf<Thought>()
        
        // ===== STARTING THOUGHTS =====
        thoughts.add(
            Thought(
                id = ThoughtId("curiosity_of_quailkind"),
                nameKey = "thought_curiosity_of_quailkind_name",
                descriptionKey = "thought_curiosity_of_quailkind_desc",
                problemDescriptionKey = "thought_curiosity_problem",
                solutionDescriptionKey = "thought_curiosity_solution",
                internalizationTimeSeconds = 300, // 5 minutes
                discoveryCondition = ThoughtDiscoveryCondition.StartingThought,
                problemEffects = listOf(
                    ThoughtEffect(ThoughtEffectType.EXPERIENCE_GAIN, -10, "Your wandering mind slows learning")
                ),
                solutionEffects = listOf(
                    ThoughtEffect(ThoughtEffectType.EXPERIENCE_GAIN, 15, "Curiosity accelerates learning"),
                    ThoughtEffect(ThoughtEffectType.DIALOGUE_OPTION_UNLOCK, 1, "Unlocks curious dialogue options")
                )
            )
        )
        
        thoughts.add(
            Thought(
                id = ThoughtId("value_of_shinies"),
                nameKey = "thought_value_of_shinies_name",
                descriptionKey = "thought_value_of_shinies_desc",
                problemDescriptionKey = "thought_shinies_problem",
                solutionDescriptionKey = "thought_shinies_solution",
                internalizationTimeSeconds = 600, // 10 minutes
                discoveryCondition = ThoughtDiscoveryCondition.StartingThought,
                problemEffects = listOf(
                    ThoughtEffect(ThoughtEffectType.SEED_INCOME, -5, "Distracted by glittering objects")
                ),
                solutionEffects = listOf(
                    ThoughtEffect(ThoughtEffectType.HOARD_VALUE_MULTIPLIER, 10, "Better eye for valuable items"),
                    ThoughtEffect(ThoughtEffectType.SHOP_DISCOUNT, 5, "Merchants respect your discernment")
                )
            )
        )
        
        // ===== PROGRESSION THOUGHTS =====
        thoughts.add(
            Thought(
                id = ThoughtId("nature_of_alchemy"),
                nameKey = "thought_nature_of_alchemy_name",
                descriptionKey = "thought_nature_of_alchemy_desc",
                problemDescriptionKey = "thought_alchemy_problem",
                solutionDescriptionKey = "thought_alchemy_solution",
                internalizationTimeSeconds = 1800, // 30 minutes
                internalizationCostSeeds = 100,
                discoveryCondition = ThoughtDiscoveryCondition.Choice("concoctions_craft_minor_health_potion"),
                problemEffects = listOf(
                    ThoughtEffect(ThoughtEffectType.CRAFTING_SUCCESS_RATE, -10, "Overthinking the process")
                ),
                solutionEffects = listOf(
                    ThoughtEffect(ThoughtEffectType.RECIPE_DISCOVERY_CHANCE, 25, "Intuitive understanding of ingredients"),
                    ThoughtEffect(ThoughtEffectType.HARVEST_YIELD, 15, "Better harvesting techniques")
                )
            )
        )
        
        thoughts.add(
            Thought(
                id = ThoughtId("hoarders_paradox"),
                nameKey = "thought_hoarders_paradox_name",
                descriptionKey = "thought_hoarders_paradox_desc",
                problemDescriptionKey = "thought_hoarders_problem",
                solutionDescriptionKey = "thought_hoarders_solution",
                internalizationTimeSeconds = 3600, // 1 hour
                internalizationCostSeeds = 500,
                discoveryCondition = ThoughtDiscoveryCondition.Milestone("hoard_rank_connoisseur"),
                problemEffects = listOf(
                    ThoughtEffect(ThoughtEffectType.SEED_INCOME, -15, "Anxiety about spending"),
                    ThoughtEffect(ThoughtEffectType.SHOP_DISCOUNT, -10, "Indecisive purchasing")
                ),
                solutionEffects = listOf(
                    ThoughtEffect(ThoughtEffectType.SEED_INCOME, 20, "Wealth attracts wealth"),
                    ThoughtEffect(ThoughtEffectType.HOARD_VALUE_MULTIPLIER, 15, "Expert appraisal skills")
                )
            )
        )
        
        thoughts.add(
            Thought(
                id = ThoughtId("experimental_mindset"),
                nameKey = "thought_experimental_mindset_name",
                descriptionKey = "thought_experimental_mindset_desc",
                problemDescriptionKey = "thought_experimental_problem",
                solutionDescriptionKey = "thought_experimental_solution",
                internalizationTimeSeconds = 2700, // 45 minutes
                internalizationCostSeeds = 250,
                discoveryCondition = ThoughtDiscoveryCondition.Choice("concoctions_experiment_success_wisdom_elixir"),
                problemEffects = listOf(
                    ThoughtEffect(ThoughtEffectType.SEED_INCOME, -10, "Wasting ingredients on failed experiments")
                ),
                solutionEffects = listOf(
                    ThoughtEffect(ThoughtEffectType.RECIPE_DISCOVERY_CHANCE, 35, "Scientific approach to experimentation"),
                    ThoughtEffect(ThoughtEffectType.EXPERIENCE_GAIN, 10, "Learning from failures")
                )
            )
        )
        
        // ===== PHILOSOPHICAL THOUGHTS =====
        thoughts.add(
            Thought(
                id = ThoughtId("what_is_a_quail"),
                nameKey = "thought_what_is_a_quail_name",
                descriptionKey = "thought_what_is_a_quail_desc",
                problemDescriptionKey = "thought_quail_problem",
                solutionDescriptionKey = "thought_quail_solution",
                internalizationTimeSeconds = 5400, // 1.5 hours
                internalizationCostSeeds = 1000,
                discoveryCondition = ThoughtDiscoveryCondition.Milestone("chronicle_chapter_1"),
                problemEffects = listOf(
                    ThoughtEffect(ThoughtEffectType.COMBAT_DEFENSE, -20, "Existential dread weakens resolve"),
                    ThoughtEffect(ThoughtEffectType.EXPERIENCE_GAIN, -15, "Too distracted to focus")
                ),
                solutionEffects = listOf(
                    ThoughtEffect(ThoughtEffectType.DIALOGUE_OPTION_UNLOCK, 1, "Unlocks deep philosophical dialogue"),
                    ThoughtEffect(ThoughtEffectType.EXPERIENCE_GAIN, 25, "Understanding breeds wisdom"),
                    ThoughtEffect(ThoughtEffectType.THOUGHT_SLOT_INCREASE, 1, "Expanded consciousness")
                )
            )
        )
        
        thoughts.add(
            Thought(
                id = ThoughtId("death_of_quails"),
                nameKey = "thought_death_of_quails_name",
                descriptionKey = "thought_death_of_quails_desc",
                problemDescriptionKey = "thought_death_problem",
                solutionDescriptionKey = "thought_death_solution",
                internalizationTimeSeconds = 7200, // 2 hours
                internalizationCostSeeds = 2000,
                discoveryCondition = ThoughtDiscoveryCondition.Choice("activities_arena_death"),
                problemEffects = listOf(
                    ThoughtEffect(ThoughtEffectType.COMBAT_DAMAGE, -25, "Fear of mortality"),
                    ThoughtEffect(ThoughtEffectType.HEALTH_REGEN, -3, "Anxiety manifests physically")
                ),
                solutionEffects = listOf(
                    ThoughtEffect(ThoughtEffectType.COMBAT_DAMAGE, 30, "No fear in battle"),
                    ThoughtEffect(ThoughtEffectType.COMBAT_DEFENSE, 15, "Calculated risk-taking"),
                    ThoughtEffect(ThoughtEffectType.ACTIVITY_UNLOCK, 1, "Unlocks high-risk activities")
                )
            )
        )
        
        thoughts.add(
            Thought(
                id = ThoughtId("meaning_of_seeds"),
                nameKey = "thought_meaning_of_seeds_name",
                descriptionKey = "thought_meaning_of_seeds_desc",
                problemDescriptionKey = "thought_seeds_problem",
                solutionDescriptionKey = "thought_seeds_solution",
                internalizationTimeSeconds = 4500, // 1.25 hours
                internalizationCostSeeds = 1500,
                discoveryCondition = ThoughtDiscoveryCondition.All(listOf(
                    ThoughtDiscoveryCondition.Milestone("hoard_rank_magnate"),
                    ThoughtDiscoveryCondition.Milestone("nest_upgrade_tier_3")
                )),
                problemEffects = listOf(
                    ThoughtEffect(ThoughtEffectType.SEED_INCOME, -20, "Questioning the point of accumulation")
                ),
                solutionEffects = listOf(
                    ThoughtEffect(ThoughtEffectType.SEED_INCOME, 30, "Understanding the cycle of growth"),
                    ThoughtEffect(ThoughtEffectType.COMPANION_AFFECTION_GAIN, 20, "Generosity breeds loyalty"),
                    ThoughtEffect(ThoughtEffectType.FEATURE_UNLOCK, 1, "Unlocks Seed gifting")
                )
            )
        )
        
        // ===== ADVANCED/META THOUGHTS =====
        thoughts.add(
            Thought(
                id = ThoughtId("limits_of_thought"),
                nameKey = "thought_limits_of_thought_name",
                descriptionKey = "thought_limits_of_thought_desc",
                problemDescriptionKey = "thought_limits_problem",
                solutionDescriptionKey = "thought_limits_solution",
                internalizationTimeSeconds = 9000, // 2.5 hours
                internalizationCostSeeds = 5000,
                discoveryCondition = ThoughtDiscoveryCondition.All(listOf(
                    ThoughtDiscoveryCondition.Milestone("thoughts_internalized_5"),
                    ThoughtDiscoveryCondition.Choice("thought_completed_what_is_a_quail")
                )),
                problemEffects = listOf(
                    ThoughtEffect(ThoughtEffectType.INTERNALIZATION_SPEED, -25, "Meta-cognition is exhausting")
                ),
                solutionEffects = listOf(
                    ThoughtEffect(ThoughtEffectType.THOUGHT_SLOT_INCREASE, 1, "Expanded mental capacity"),
                    ThoughtEffect(ThoughtEffectType.INTERNALIZATION_SPEED, 50, "Efficient thinking"),
                    ThoughtEffect(ThoughtEffectType.EXPERIENCE_GAIN, 20, "Every thought teaches")
                )
            )
        )
        
        thoughts.add(
            Thought(
                id = ThoughtId("enlightenment"),
                nameKey = "thought_enlightenment_name",
                descriptionKey = "thought_enlightenment_desc",
                problemDescriptionKey = "thought_enlightenment_problem",
                solutionDescriptionKey = "thought_enlightenment_solution",
                internalizationTimeSeconds = 18000, // 5 hours (ultimate thought)
                internalizationCostSeeds = 10000,
                discoveryCondition = ThoughtDiscoveryCondition.All(listOf(
                    ThoughtDiscoveryCondition.Milestone("thoughts_internalized_10"),
                    ThoughtDiscoveryCondition.Milestone("chronicle_chapter_final"),
                    ThoughtDiscoveryCondition.Milestone("hoard_rank_tycoon")
                )),
                problemEffects = listOf(
                    ThoughtEffect(ThoughtEffectType.EXPERIENCE_GAIN, -30, "Burden of enlightenment"),
                    ThoughtEffect(ThoughtEffectType.SEED_INCOME, -25, "Material concerns fade"),
                    ThoughtEffect(ThoughtEffectType.COMBAT_DAMAGE, -20, "Pacifistic tendencies")
                ),
                solutionEffects = listOf(
                    ThoughtEffect(ThoughtEffectType.EXPERIENCE_GAIN, 100, "Ultimate wisdom"),
                    ThoughtEffect(ThoughtEffectType.DIALOGUE_OPTION_UNLOCK, 1, "Unlocks secret endings"),
                    ThoughtEffect(ThoughtEffectType.THOUGHT_SLOT_INCREASE, 2, "Transcendent consciousness"),
                    ThoughtEffect(ThoughtEffectType.FEATURE_UNLOCK, 1, "Unlocks New Game+")
                )
            )
        )
        
        return thoughts.associateBy { it.id }
    }
}
