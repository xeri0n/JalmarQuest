package com.jalmarquest.core.state.battlepass

import com.jalmarquest.core.model.*

/**
 * Content definitions for all seasons.
 * 
 * This file contains the actual season data separate from the SeasonCatalog
 * infrastructure to make content updates easier to manage.
 */

/**
 * Create Season 1: "Autumn Harvest"
 * 
 * Theme: Celebrating the bounty of the forest as autumn arrives.
 * Duration: 90 days (3 months)
 * Premium Cost: 1000 Glimmer Shards
 */
fun createSeason1AutumnHarvest(startTimestamp: Long): Season {
    val endTimestamp = startTimestamp + 90L * 24 * 60 * 60 * 1000 // 90 days
    
    return Season(
        seasonId = SeasonId("season_1_autumn_harvest"),
        seasonNumber = 1,
        name = "Autumn Harvest",
        description = "Celebrate the bounty of the forest as autumn paints the world in golden hues. Gather ingredients, discover ancient recipes, and unlock exclusive cosmetics that capture the essence of the harvest season.",
        startTimestamp = startTimestamp,
        endTimestamp = endTimestamp,
        tiers = createAutumnHarvestTiers(),
        objectives = createAutumnHarvestObjectives(),
        premiumCostGlimmer = 1000,
        themeColor = "#D2691E", // Autumn orange
        bannerImagePath = "seasons/autumn_harvest_banner.png"
    )
}

/**
 * Create 50 tiers for Autumn Harvest season.
 * 
 * Progression curve:
 * - Tiers 1-10: 100 XP each (1,000 total)
 * - Tiers 11-20: 150 XP each (1,500 total)
 * - Tiers 21-30: 200 XP each (2,000 total)
 * - Tiers 31-40: 250 XP each (2,500 total)
 * - Tiers 41-50: 300 XP each (3,000 total)
 * Total: 10,000 XP for all 50 tiers
 */
private fun createAutumnHarvestTiers(): List<SeasonTier> {
    val tiers = mutableListOf<SeasonTier>()
    
    for (i in 1..50) {
        val xpRequired = when {
            i <= 10 -> 100
            i <= 20 -> 150
            i <= 30 -> 200
            i <= 40 -> 250
            else -> 300
        }
        
        tiers.add(
            SeasonTier(
                tierId = TierId("autumn_tier_$i"),
                tierNumber = i,
                xpRequired = xpRequired,
                freeReward = createFreeReward(i),
                premiumReward = createPremiumReward(i)
            )
        )
    }
    
    return tiers
}

/**
 * Create free track reward for a tier.
 * 
 * Free track focuses on:
 * - Seeds (every 5 tiers)
 * - Ingredients (common/uncommon, odd tiers)
 * - Recipes (tiers 10, 20, 30, 40)
 * - Thoughts (tiers 25, 50)
 * - Lore unlocks (milestone tiers)
 */
private fun createFreeReward(tierNumber: Int): SeasonReward? {
    return when {
        // Milestone rewards
        tierNumber == 50 -> SeasonReward(
            type = SeasonRewardType.THOUGHT,
            itemId = "thought_autumn_wisdom",
            quantity = 1,
            displayName = "Autumn's Wisdom",
            description = "A profound thought about the cycle of seasons and renewal. +20% Ingredient harvest luck.",
            iconPath = "thoughts/autumn_wisdom.png"
        )
        tierNumber == 25 -> SeasonReward(
            type = SeasonRewardType.LORE,
            itemId = "lore_harvest_festival",
            quantity = 1,
            displayName = "The Harvest Festival",
            description = "Unlock lore about Buttonburgh's ancient harvest traditions.",
            iconPath = "lore/harvest_festival.png"
        )
        
        // Recipe rewards every 10 tiers
        tierNumber == 40 -> SeasonReward(
            type = SeasonRewardType.RECIPE,
            itemId = "recipe_golden_elixir",
            quantity = 1,
            displayName = "Golden Elixir Recipe",
            description = "A rare concoction that grants +50% Seeds for 1 hour.",
            iconPath = "recipes/golden_elixir.png"
        )
        tierNumber == 30 -> SeasonReward(
            type = SeasonRewardType.RECIPE,
            itemId = "recipe_harvest_brew",
            quantity = 1,
            displayName = "Harvest Brew Recipe",
            description = "Boosts ingredient yield by 30% for 30 minutes.",
            iconPath = "recipes/harvest_brew.png"
        )
        tierNumber == 20 -> SeasonReward(
            type = SeasonRewardType.RECIPE,
            itemId = "recipe_autumn_tonic",
            quantity = 1,
            displayName = "Autumn Tonic Recipe",
            description = "Restores 50 health and grants mild luck boost.",
            iconPath = "recipes/autumn_tonic.png"
        )
        tierNumber == 10 -> SeasonReward(
            type = SeasonRewardType.RECIPE,
            itemId = "recipe_forest_tea",
            quantity = 1,
            displayName = "Forest Tea Recipe",
            description = "A simple restorative brew using common forest ingredients.",
            iconPath = "recipes/forest_tea.png"
        )
        
        // Seed rewards every 5 tiers
        tierNumber % 5 == 0 -> {
            val amount = when {
                tierNumber <= 15 -> 200
                tierNumber <= 30 -> 300
                tierNumber <= 45 -> 500
                else -> 1000
            }
            SeasonReward(
                type = SeasonRewardType.SEEDS,
                quantity = amount,
                displayName = "$amount Seeds",
                description = "Seeds currency for purchases and crafting.",
                iconPath = "currency/seeds.png"
            )
        }
        
        // Ingredient rewards on odd tiers (not covered by other rewards)
        tierNumber % 2 == 1 && tierNumber !in listOf(25) -> {
            val rarity = if (tierNumber <= 25) "common" else "uncommon"
            SeasonReward(
                type = SeasonRewardType.ITEM,
                itemId = "ingredient_autumn_${rarity}_${tierNumber}",
                quantity = if (tierNumber <= 25) 5 else 3,
                displayName = "Autumn Ingredients",
                description = "Seasonal ingredients for crafting autumn-themed concoctions.",
                iconPath = "ingredients/autumn_bundle.png"
            )
        }
        
        // Lore on milestone tiers
        tierNumber in listOf(15, 35, 45) -> SeasonReward(
            type = SeasonRewardType.LORE,
            itemId = "lore_autumn_tier_$tierNumber",
            quantity = 1,
            displayName = "Autumn Lore Fragment",
            description = "A piece of forgotten knowledge about the harvest season.",
            iconPath = "lore/autumn_fragment.png"
        )
        
        // No reward for some tiers (premium-only tiers)
        else -> null
    }
}

/**
 * Create premium track reward for a tier.
 * 
 * Premium track focuses on:
 * - Glimmer Shards (every tier, scaling amounts)
 * - Cosmetics (nest themes, companion outfits, every 5 tiers)
 * - Exclusive thoughts (tiers 15, 30, 45)
 * - Titles/Emotes (milestone tiers)
 * - Skill XP boosts (even tiers)
 */
private fun createPremiumReward(tierNumber: Int): SeasonReward? {
    return when {
        // Ultimate reward
        tierNumber == 50 -> SeasonReward(
            type = SeasonRewardType.COSMETIC,
            itemId = "cosmetic_golden_quail_outfit",
            quantity = 1,
            displayName = "Golden Harvest Regalia",
            description = "An exquisite golden outfit commemorating mastery of the Autumn Harvest season. Ultra-rare exclusive cosmetic.",
            iconPath = "cosmetics/golden_harvest_regalia.png"
        )
        
        // Exclusive thoughts
        tierNumber == 45 -> SeasonReward(
            type = SeasonRewardType.THOUGHT,
            itemId = "thought_bountiful_mind",
            quantity = 1,
            displayName = "Bountiful Mind",
            description = "Premium thought: +25% to all resource gathering, +1 thought slot.",
            iconPath = "thoughts/bountiful_mind.png"
        )
        tierNumber == 30 -> SeasonReward(
            type = SeasonRewardType.THOUGHT,
            itemId = "thought_merchants_eye",
            quantity = 1,
            displayName = "Merchant's Eye",
            description = "Premium thought: +20% shop discounts, +15% sell prices.",
            iconPath = "thoughts/merchants_eye.png"
        )
        tierNumber == 15 -> SeasonReward(
            type = SeasonRewardType.THOUGHT,
            itemId = "thought_crafters_focus",
            quantity = 1,
            displayName = "Crafter's Focus",
            description = "Premium thought: -25% crafting costs, +10% crafting speed.",
            iconPath = "thoughts/crafters_focus.png"
        )
        
        // Cosmetics every 5 tiers
        tierNumber == 40 -> SeasonReward(
            type = SeasonRewardType.COSMETIC,
            itemId = "cosmetic_autumn_nest_theme",
            quantity = 1,
            displayName = "Autumn Leaves Nest Theme",
            description = "Transform your nest with beautiful autumn foliage decorations.",
            iconPath = "cosmetics/autumn_nest.png"
        )
        tierNumber == 35 -> SeasonReward(
            type = SeasonRewardType.EMOTE,
            itemId = "emote_leaf_dance",
            quantity = 1,
            displayName = "Leaf Dance Emote",
            description = "Celebrate with a whimsical dance among falling leaves.",
            iconPath = "emotes/leaf_dance.png"
        )
        tierNumber == 25 -> SeasonReward(
            type = SeasonRewardType.COSMETIC,
            itemId = "cosmetic_harvest_basket",
            quantity = 1,
            displayName = "Harvest Basket Accessory",
            description = "A charming woven basket companion accessory.",
            iconPath = "cosmetics/harvest_basket.png"
        )
        tierNumber == 20 -> SeasonReward(
            type = SeasonRewardType.TITLE,
            itemId = "title_harvest_champion",
            quantity = 1,
            displayName = "Harvest Champion Title",
            description = "Display your dedication to the autumn season.",
            iconPath = "titles/harvest_champion.png"
        )
        tierNumber % 10 == 0 && tierNumber !in listOf(20, 30, 40, 50) -> SeasonReward(
            type = SeasonRewardType.COSMETIC,
            itemId = "cosmetic_tier_$tierNumber",
            quantity = 1,
            displayName = "Seasonal Cosmetic",
            description = "An exclusive autumn-themed cosmetic item.",
            iconPath = "cosmetics/seasonal_tier_$tierNumber.png"
        )
        
        // Glimmer refunds on milestone tiers (ROI: 450 Glimmer back from 1000 spent)
        tierNumber in listOf(10, 20, 30, 40) -> SeasonReward(
            type = SeasonRewardType.GLIMMER_SHARDS,
            quantity = when (tierNumber) {
                10 -> 100
                20 -> 100
                30 -> 125
                40 -> 125
                else -> 0
            },
            displayName = "${if (tierNumber <= 20) 100 else 125} Glimmer Shards",
            description = "Premium currency for shop purchases and future battle passes.",
            iconPath = "currency/glimmer.png"
        )
        
        // Skill XP boosts on even tiers
        tierNumber % 2 == 0 && tierNumber !in listOf(10, 20, 30, 40, 50) -> SeasonReward(
            type = SeasonRewardType.SKILL_XP,
            quantity = tierNumber * 10, // 20 XP at tier 2, up to 480 XP at tier 48
            displayName = "Skill XP Boost",
            description = "Bonus experience for all skills to accelerate progression.",
            iconPath = "rewards/skill_xp.png"
        )
        
        // Small Glimmer rewards on remaining tiers
        else -> SeasonReward(
            type = SeasonRewardType.GLIMMER_SHARDS,
            quantity = when {
                tierNumber <= 15 -> 25
                tierNumber <= 30 -> 35
                tierNumber <= 45 -> 50
                else -> 75
            },
            displayName = "${if (tierNumber <= 15) 25 else if (tierNumber <= 30) 35 else if (tierNumber <= 45) 50 else 75} Glimmer Shards",
            description = "Small Glimmer Shard bonus for premium track owners.",
            iconPath = "currency/glimmer.png"
        )
    }
}

/**
 * Create objectives for Autumn Harvest season.
 * 
 * Mix of daily (repeatable), weekly (repeatable), and seasonal (one-time) objectives.
 */
private fun createAutumnHarvestObjectives(): List<SeasonObjective> {
    return listOf(
        // === DAILY OBJECTIVES (Reset every 24 hours) ===
        SeasonObjective(
            objectiveId = "daily_complete_quests",
            type = SeasonObjectiveType.COMPLETE_DAILY_QUESTS,
            description = "Complete 3 daily quests",
            frequency = ObjectiveFrequency.DAILY,
            xpReward = 50,
            targetValue = 3
        ),
        SeasonObjective(
            objectiveId = "daily_harvest_ingredients",
            type = SeasonObjectiveType.HARVEST_INGREDIENTS,
            description = "Harvest 10 ingredients from any location",
            frequency = ObjectiveFrequency.DAILY,
            xpReward = 40,
            targetValue = 10
        ),
        SeasonObjective(
            objectiveId = "daily_craft_concoctions",
            type = SeasonObjectiveType.CRAFT_CONCOCTIONS,
            description = "Craft 2 concoctions",
            frequency = ObjectiveFrequency.DAILY,
            xpReward = 60,
            targetValue = 2
        ),
        SeasonObjective(
            objectiveId = "daily_accumulate_seeds",
            type = SeasonObjectiveType.ACCUMULATE_SEEDS,
            description = "Earn 500 Seeds through any activity",
            frequency = ObjectiveFrequency.DAILY,
            xpReward = 30,
            targetValue = 500
        ),
        SeasonObjective(
            objectiveId = "daily_visit_locations",
            type = SeasonObjectiveType.VISIT_LOCATIONS,
            description = "Visit 5 different locations",
            frequency = ObjectiveFrequency.DAILY,
            xpReward = 35,
            targetValue = 5
        ),
        
        // === WEEKLY OBJECTIVES (Reset every 7 days) ===
        SeasonObjective(
            objectiveId = "weekly_complete_quests",
            type = SeasonObjectiveType.COMPLETE_WEEKLY_QUESTS,
            description = "Complete 10 quests this week",
            frequency = ObjectiveFrequency.WEEKLY,
            xpReward = 200,
            targetValue = 10
        ),
        SeasonObjective(
            objectiveId = "weekly_defeat_enemies",
            type = SeasonObjectiveType.DEFEAT_ENEMIES,
            description = "Defeat 25 enemies",
            frequency = ObjectiveFrequency.WEEKLY,
            xpReward = 250,
            targetValue = 25
        ),
        SeasonObjective(
            objectiveId = "weekly_discover_lore",
            type = SeasonObjectiveType.DISCOVER_LORE,
            description = "Discover 5 lore entries",
            frequency = ObjectiveFrequency.WEEKLY,
            xpReward = 150,
            targetValue = 5
        ),
        SeasonObjective(
            objectiveId = "weekly_reach_skill_level",
            type = SeasonObjectiveType.REACH_SKILL_LEVEL,
            description = "Gain 3 skill levels across any skills",
            frequency = ObjectiveFrequency.WEEKLY,
            xpReward = 300,
            targetValue = 3,
            metadata = mapOf("skillType" to "any")
        ),
        SeasonObjective(
            objectiveId = "weekly_complete_dungeons",
            type = SeasonObjectiveType.COMPLETE_DUNGEONS,
            description = "Complete 3 dungeons or arena runs",
            frequency = ObjectiveFrequency.WEEKLY,
            xpReward = 400,
            targetValue = 3
        ),
        
        // === SEASONAL OBJECTIVES (One-time, lasts entire season) ===
        SeasonObjective(
            objectiveId = "seasonal_harvest_rare",
            type = SeasonObjectiveType.HARVEST_INGREDIENTS,
            description = "Harvest 50 rare or exotic ingredients",
            frequency = ObjectiveFrequency.SEASONAL,
            xpReward = 500,
            targetValue = 50,
            metadata = mapOf("rarity" to "rare_or_exotic")
        ),
        SeasonObjective(
            objectiveId = "seasonal_craft_master",
            type = SeasonObjectiveType.CRAFT_CONCOCTIONS,
            description = "Craft 100 concoctions total",
            frequency = ObjectiveFrequency.SEASONAL,
            xpReward = 750,
            targetValue = 100
        ),
        SeasonObjective(
            objectiveId = "seasonal_skill_mastery",
            type = SeasonObjectiveType.REACH_SKILL_LEVEL,
            description = "Reach level 10 in Foraging skill",
            frequency = ObjectiveFrequency.SEASONAL,
            xpReward = 1000,
            targetValue = 10,
            metadata = mapOf("skillType" to "Foraging")
        ),
        SeasonObjective(
            objectiveId = "seasonal_lore_scholar",
            type = SeasonObjectiveType.DISCOVER_LORE,
            description = "Discover 25 unique lore entries",
            frequency = ObjectiveFrequency.SEASONAL,
            xpReward = 800,
            targetValue = 25
        ),
        SeasonObjective(
            objectiveId = "seasonal_exploration",
            type = SeasonObjectiveType.VISIT_LOCATIONS,
            description = "Visit all 55+ locations in the world",
            frequency = ObjectiveFrequency.SEASONAL,
            xpReward = 1200,
            targetValue = 55
        )
    )
}

/**
 * Register Season 1 in the provided SeasonCatalog.
 */
fun SeasonCatalog.registerSeason1(startTimestamp: Long) {
    registerSeason(createSeason1AutumnHarvest(startTimestamp))
}
