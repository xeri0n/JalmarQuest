package com.jalmarquest.core.state.difficulty

import kotlinx.serialization.Serializable

/**
 * Difficulty tier for a region
 */
enum class DifficultyTier(val level: Int) {
    SAFE(1),        // Buttonburgh - tutorial/safe zone
    EASY(2),        // Forest, Beach - beginner exploration
    MEDIUM(3),      // Swamp - intermediate challenge
    HARD(4),        // Mountains, Ruins - advanced content
    EXTREME(5);     // Boss encounters, special events
    
    companion object {
        fun fromLevel(level: Int): DifficultyTier = entries.firstOrNull { it.level == level } ?: SAFE
    }
}

/**
 * Scaling configuration for a difficulty tier
 */
@Serializable
data class DifficultyScaling(
    val tier: Int,
    val enemyHealthMultiplier: Double,
    val enemyDamageMultiplier: Double,
    val enemyXpMultiplier: Double,
    val resourceSpawnChanceMultiplier: Double,
    val resourceRespawnTimeMultiplier: Double,
    val questXpMultiplier: Double,
    val questSeedRewardMultiplier: Double,
    val lootQualityBonus: Double
)

/**
 * Manages difficulty scaling across different regions
 */
class RegionDifficultyManager {
    
    private val regionDifficulties = mutableMapOf<String, DifficultyTier>()
    private val scalingConfigs = mutableMapOf<DifficultyTier, DifficultyScaling>()
    
    init {
        registerDefaultScaling()
        registerDefaultRegionDifficulties()
    }
    
    /**
     * Register difficulty tier for a region/location
     */
    fun registerRegionDifficulty(regionId: String, tier: DifficultyTier) {
        regionDifficulties[regionId] = tier
    }
    
    /**
     * Get difficulty tier for a region
     */
    fun getDifficultyTier(regionId: String): DifficultyTier {
        return regionDifficulties[regionId] ?: DifficultyTier.SAFE
    }
    
    /**
     * Get scaling configuration for a tier
     */
    fun getScaling(tier: DifficultyTier): DifficultyScaling {
        return scalingConfigs[tier] ?: scalingConfigs[DifficultyTier.SAFE]!!
    }
    
    /**
     * Get scaling for a specific region
     */
    fun getRegionScaling(regionId: String): DifficultyScaling {
        val tier = getDifficultyTier(regionId)
        return getScaling(tier)
    }
    
    /**
     * Scale enemy stats for a region
     */
    fun scaleEnemyHealth(baseHealth: Int, regionId: String): Int {
        val scaling = getRegionScaling(regionId)
        return (baseHealth * scaling.enemyHealthMultiplier).toInt().coerceAtLeast(1)
    }
    
    fun scaleEnemyDamage(baseDamage: Int, regionId: String): Int {
        val scaling = getRegionScaling(regionId)
        return (baseDamage * scaling.enemyDamageMultiplier).toInt().coerceAtLeast(1)
    }
    
    fun scaleEnemyXp(baseXp: Int, regionId: String): Int {
        val scaling = getRegionScaling(regionId)
        return (baseXp * scaling.enemyXpMultiplier).toInt().coerceAtLeast(1)
    }
    
    /**
     * Scale resource availability
     */
    fun scaleResourceSpawnChance(baseChance: Double, regionId: String): Double {
        val scaling = getRegionScaling(regionId)
        return (baseChance * scaling.resourceSpawnChanceMultiplier).coerceIn(0.0, 1.0)
    }
    
    fun scaleResourceRespawnTime(baseMinutes: Int, regionId: String): Int {
        val scaling = getRegionScaling(regionId)
        return (baseMinutes * scaling.resourceRespawnTimeMultiplier).toInt().coerceAtLeast(1)
    }
    
    /**
     * Scale quest rewards
     */
    fun scaleQuestXp(baseXp: Int, regionId: String): Int {
        val scaling = getRegionScaling(regionId)
        return (baseXp * scaling.questXpMultiplier).toInt().coerceAtLeast(1)
    }
    
    fun scaleQuestSeeds(baseSeeds: Int, regionId: String): Int {
        val scaling = getRegionScaling(regionId)
        return (baseSeeds * scaling.questSeedRewardMultiplier).toInt().coerceAtLeast(1)
    }
    
    /**
     * Calculate loot quality bonus for region
     */
    fun getLootQualityBonus(regionId: String): Double {
        val scaling = getRegionScaling(regionId)
        return scaling.lootQualityBonus
    }
    
    /**
     * Check if player meets recommended level for region
     */
    fun meetsRecommendedLevel(playerLevel: Int, regionId: String): Boolean {
        val tier = getDifficultyTier(regionId)
        // Recommended level = tier.level * 5 (SAFE=5, EASY=10, MEDIUM=15, HARD=20, EXTREME=25)
        val recommendedLevel = tier.level * 5
        return playerLevel >= recommendedLevel
    }
    
    /**
     * Get recommended level for region
     */
    fun getRecommendedLevel(regionId: String): Int {
        val tier = getDifficultyTier(regionId)
        return tier.level * 5
    }
    
    /**
     * Get danger description for region
     */
    fun getDangerDescription(regionId: String): String {
        return when (getDifficultyTier(regionId)) {
            DifficultyTier.SAFE -> "Safe Zone - Perfect for beginners"
            DifficultyTier.EASY -> "Low Danger - Suitable for new adventurers"
            DifficultyTier.MEDIUM -> "Moderate Danger - Experience recommended"
            DifficultyTier.HARD -> "High Danger - Advanced quails only"
            DifficultyTier.EXTREME -> "Extreme Danger - Deadly threats await"
        }
    }
    
    private fun registerDefaultScaling() {
        // SAFE tier - Buttonburgh (1.0x baseline, extra resources, low rewards)
        scalingConfigs[DifficultyTier.SAFE] = DifficultyScaling(
            tier = 1,
            enemyHealthMultiplier = 1.0,
            enemyDamageMultiplier = 1.0,
            enemyXpMultiplier = 1.0,
            resourceSpawnChanceMultiplier = 1.5,  // 50% more resources
            resourceRespawnTimeMultiplier = 0.7,   // 30% faster respawns
            questXpMultiplier = 1.0,
            questSeedRewardMultiplier = 1.0,
            lootQualityBonus = 0.0
        )
        
        // EASY tier - Forest, Beach (1.2x challenge, good resources, decent rewards)
        scalingConfigs[DifficultyTier.EASY] = DifficultyScaling(
            tier = 2,
            enemyHealthMultiplier = 1.2,
            enemyDamageMultiplier = 1.2,
            enemyXpMultiplier = 1.5,
            resourceSpawnChanceMultiplier = 1.2,  // 20% more resources
            resourceRespawnTimeMultiplier = 0.9,   // 10% faster respawns
            questXpMultiplier = 1.5,
            questSeedRewardMultiplier = 1.5,
            lootQualityBonus = 0.1
        )
        
        // MEDIUM tier - Swamp (1.5x challenge, normal resources, good rewards)
        scalingConfigs[DifficultyTier.MEDIUM] = DifficultyScaling(
            tier = 3,
            enemyHealthMultiplier = 1.5,
            enemyDamageMultiplier = 1.5,
            enemyXpMultiplier = 2.0,
            resourceSpawnChanceMultiplier = 1.0,  // Normal resource spawn
            resourceRespawnTimeMultiplier = 1.0,   // Normal respawn time
            questXpMultiplier = 2.0,
            questSeedRewardMultiplier = 2.0,
            lootQualityBonus = 0.25
        )
        
        // HARD tier - Mountains, Ruins (2.0x challenge, scarce resources, great rewards)
        scalingConfigs[DifficultyTier.HARD] = DifficultyScaling(
            tier = 4,
            enemyHealthMultiplier = 2.0,
            enemyDamageMultiplier = 2.0,
            enemyXpMultiplier = 3.0,
            resourceSpawnChanceMultiplier = 0.8,  // 20% fewer common resources
            resourceRespawnTimeMultiplier = 1.3,   // 30% slower respawns
            questXpMultiplier = 3.0,
            questSeedRewardMultiplier = 3.0,
            lootQualityBonus = 0.5
        )
        
        // EXTREME tier - Boss encounters (3.0x challenge, rare resources, legendary rewards)
        scalingConfigs[DifficultyTier.EXTREME] = DifficultyScaling(
            tier = 5,
            enemyHealthMultiplier = 3.0,
            enemyDamageMultiplier = 2.5,
            enemyXpMultiplier = 5.0,
            resourceSpawnChanceMultiplier = 0.5,  // 50% fewer common resources
            resourceRespawnTimeMultiplier = 2.0,   // 2x slower respawns
            questXpMultiplier = 5.0,
            questSeedRewardMultiplier = 5.0,
            lootQualityBonus = 1.0
        )
    }
    
    private fun registerDefaultRegionDifficulties() {
        // Buttonburgh - SAFE zone
        registerRegionDifficulty("buttonburgh_town_square", DifficultyTier.SAFE)
        registerRegionDifficulty("buttonburgh_market", DifficultyTier.SAFE)
        registerRegionDifficulty("buttonburgh_guard_post", DifficultyTier.SAFE)
        registerRegionDifficulty("buttonburgh_hen_pen", DifficultyTier.SAFE)
        registerRegionDifficulty("buttonburgh_quills_study", DifficultyTier.SAFE)
        registerRegionDifficulty("buttonburgh_pack_rats_hoard", DifficultyTier.SAFE)
        registerRegionDifficulty("buttonburgh_quailsmiths_forge", DifficultyTier.SAFE)
        registerRegionDifficulty("buttonburgh_watchtower", DifficultyTier.SAFE)
        registerRegionDifficulty("buttonburgh_granary", DifficultyTier.SAFE)
        registerRegionDifficulty("buttonburgh_nest_quarters", DifficultyTier.SAFE)
        
        // Forest - EASY tier
        registerRegionDifficulty("forest_entrance", DifficultyTier.EASY)
        registerRegionDifficulty("forest_old_oak", DifficultyTier.EASY)
        registerRegionDifficulty("forest_mushroom_grove", DifficultyTier.EASY)
        registerRegionDifficulty("forest_spider_den", DifficultyTier.EASY)
        registerRegionDifficulty("forest_hermits_cabin", DifficultyTier.EASY)
        registerRegionDifficulty("forest_moondew_clearing", DifficultyTier.EASY)
        registerRegionDifficulty("forest_brook", DifficultyTier.EASY)
        registerRegionDifficulty("forest_hollow_tree", DifficultyTier.EASY)
        
        // Beach - EASY tier
        registerRegionDifficulty("beach_shoreline", DifficultyTier.EASY)
        registerRegionDifficulty("beach_tide_pools", DifficultyTier.EASY)
        registerRegionDifficulty("beach_dunes", DifficultyTier.EASY)
        registerRegionDifficulty("beach_crab_territory", DifficultyTier.EASY)
        registerRegionDifficulty("beach_washed_up_boat", DifficultyTier.EASY)
        registerRegionDifficulty("beach_cliff_base", DifficultyTier.EASY)
        registerRegionDifficulty("beach_seagull_rocks", DifficultyTier.EASY)
        registerRegionDifficulty("beach_hidden_cove", DifficultyTier.EASY)
        registerRegionDifficulty("beach_pearl_shallows", DifficultyTier.EASY)
        
        // Swamp - MEDIUM tier
        registerRegionDifficulty("swamp_edge", DifficultyTier.MEDIUM)
        registerRegionDifficulty("swamp_murky_depths", DifficultyTier.MEDIUM)
        registerRegionDifficulty("swamp_alligator_lair", DifficultyTier.MEDIUM)
        registerRegionDifficulty("swamp_poison_moss_patch", DifficultyTier.MEDIUM)
        registerRegionDifficulty("swamp_old_stump", DifficultyTier.MEDIUM)
        registerRegionDifficulty("swamp_firefly_glade", DifficultyTier.MEDIUM)
        registerRegionDifficulty("swamp_quicksand_pools", DifficultyTier.MEDIUM)
        registerRegionDifficulty("swamp_willow_island", DifficultyTier.MEDIUM)
        registerRegionDifficulty("swamp_croaking_hollow", DifficultyTier.MEDIUM)
        
        // Mountains - HARD tier
        registerRegionDifficulty("mountains_foothills", DifficultyTier.HARD)
        registerRegionDifficulty("mountains_crystal_cave", DifficultyTier.HARD)
        registerRegionDifficulty("mountains_summit", DifficultyTier.HARD)
        registerRegionDifficulty("mountains_eagle_nest", DifficultyTier.HARD)
        registerRegionDifficulty("mountains_narrow_ledge", DifficultyTier.HARD)
        registerRegionDifficulty("mountains_waterfall", DifficultyTier.HARD)
        registerRegionDifficulty("mountains_hidden_grotto", DifficultyTier.HARD)
        registerRegionDifficulty("mountains_frozen_peak", DifficultyTier.HARD)
        registerRegionDifficulty("mountains_echo_valley", DifficultyTier.HARD)
        registerRegionDifficulty("mountains_thunderstone_altar", DifficultyTier.HARD)
        
        // Ruins - HARD tier
        registerRegionDifficulty("ruins_entrance", DifficultyTier.HARD)
        registerRegionDifficulty("ruins_crumbling_halls", DifficultyTier.HARD)
        registerRegionDifficulty("ruins_guardian_chamber", DifficultyTier.HARD)
        registerRegionDifficulty("ruins_throne_room", DifficultyTier.HARD)
        registerRegionDifficulty("ruins_vault", DifficultyTier.HARD)
        registerRegionDifficulty("ruins_library", DifficultyTier.HARD)
        registerRegionDifficulty("ruins_courtyard", DifficultyTier.HARD)
        registerRegionDifficulty("ruins_underground_passage", DifficultyTier.HARD)
        registerRegionDifficulty("ruins_forgotten_shrine", DifficultyTier.HARD)
        
        // Special/Boss locations - EXTREME tier
        registerRegionDifficulty("alligator_boss_arena", DifficultyTier.EXTREME)
        registerRegionDifficulty("eagle_boss_arena", DifficultyTier.EXTREME)
        registerRegionDifficulty("guardian_boss_arena", DifficultyTier.EXTREME)
        registerRegionDifficulty("ancient_colosseum", DifficultyTier.EXTREME)
    }
}
