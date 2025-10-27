package com.jalmarquest.core.state.managers

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.catalogs.Location
import com.jalmarquest.core.state.catalogs.WorldRegionCatalog
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manages discovery rewards and achievements for world exploration.
 * 
 * Phase 6.5: Grants rewards for discovering regions/locations,
 * tracks achievements, and provides milestone rewards.
 */
class DiscoveryRewardManager(
    private val regionCatalog: WorldRegionCatalog,
    private val gameStateManager: GameStateManager?,
    private val config: DiscoveryRewardConfig = DiscoveryRewardConfig(),
    private val timestampProvider: () -> Long
) {
    private val mutex = Mutex()
    
    /**
     * Process a region discovery and grant rewards.
     */
    suspend fun processRegionDiscovery(
        regionId: WorldRegionId,
        regionName: String,
        difficultyLevel: Int,
        biomeType: BiomeType
    ): DiscoveryResult = mutex.withLock {
        val player = gameStateManager?.playerState?.value ?: return DiscoveryResult.AlreadyDiscovered
        val explorationState = player.worldExploration
        
        // Check if already discovered
        if (explorationState.hasDiscoveredRegion(regionId)) {
            return DiscoveryResult.AlreadyDiscovered
        }
        
        // Calculate rewards
        val xpReward = config.regionDiscoveryBaseXp + (config.regionDiscoveryXpPerLevel * difficultyLevel)
        val seedReward = difficultyLevel * 10 // 10 seeds per difficulty level
        
        // Check for first biome discovery
        val biomeBonus = if (biomeType !in explorationState.achievementState.discoveredBiomes) {
            config.firstBiomeDiscoveryBonusXp
        } else {
            0
        }
        
        // Build reward
        val reward = DiscoveryReward(
            experiencePoints = xpReward + biomeBonus,
            seeds = seedReward,
            items = if (difficultyLevel >= 5) config.dangerousRegionItemRewards else emptyMap()
        )
        
        // Check for new achievements
        val newAchievements = mutableListOf<UnlockedAchievement>()
        val achievementState = explorationState.achievementState
        
        // First region discovery
        if (explorationState.discoveredRegions.size == 1) { // Buttonburgh already discovered
            newAchievements.add(createAchievement(DiscoveryAchievementType.FIRST_REGION_DISCOVERED))
        }
        
        // Dangerous region discovery
        if (difficultyLevel >= 6 && !achievementState.hasAchievement(DiscoveryAchievementType.DANGEROUS_REGION_DISCOVERED)) {
            newAchievements.add(createAchievement(DiscoveryAchievementType.DANGEROUS_REGION_DISCOVERED))
        }
        
        // First biome discovery achievements
        val biomeAchievement = when (biomeType) {
            BiomeType.FOREST -> DiscoveryAchievementType.FIRST_FOREST_DISCOVERED
            BiomeType.BEACH -> DiscoveryAchievementType.FIRST_BEACH_DISCOVERED
            BiomeType.MOUNTAIN -> DiscoveryAchievementType.FIRST_MOUNTAIN_DISCOVERED
            BiomeType.WETLAND -> DiscoveryAchievementType.FIRST_SWAMP_DISCOVERED
            BiomeType.RUINS -> DiscoveryAchievementType.FIRST_RUINS_DISCOVERED
            else -> null
        }
        
        if (biomeAchievement != null && !achievementState.hasAchievement(biomeAchievement)) {
            newAchievements.add(createAchievement(biomeAchievement))
        }
        
        // Update player state
        gameStateManager?.updateWorldExploration { state ->
            state.copy(
                discoveredRegions = state.discoveredRegions + regionId,
                achievementState = state.achievementState.copy(
                    unlockedAchievements = state.achievementState.unlockedAchievements + newAchievements,
                    discoveredBiomes = state.achievementState.discoveredBiomes + biomeType
                )
            )
        }
        
        // Apply rewards
        applyReward(reward)
        
        // Log discovery
        gameStateManager?.appendChoice("region_discovered_${regionId.value}")
        
        return DiscoveryResult.RegionDiscovered(regionId, regionName, reward, newAchievements)
    }
    
    /**
     * Process a location discovery and grant rewards.
     */
    suspend fun processLocationDiscovery(
        location: Location,
        isHidden: Boolean = false
    ): DiscoveryResult = mutex.withLock {
        val player = gameStateManager?.playerState?.value ?: return DiscoveryResult.AlreadyDiscovered
        val explorationState = player.worldExploration
        
        // Check if already visited
        if (explorationState.hasVisitedLocation(location.id)) {
            return DiscoveryResult.AlreadyDiscovered
        }
        
        // Calculate rewards
        val xpReward = config.locationDiscoveryBaseXp + if (isHidden) config.hiddenLocationBonusXp else 0
        val seedReward = config.locationDiscoveryBaseSeeds
        
        val reward = DiscoveryReward(
            experiencePoints = xpReward,
            seeds = seedReward
        )
        
        // Check for new achievements
        val newAchievements = mutableListOf<UnlockedAchievement>()
        val achievementState = explorationState.achievementState
        val visitedCount = explorationState.visitedLocations.size + 1 // +1 for current
        
        // Location milestone achievements
        when (visitedCount) {
            1 -> if (!achievementState.hasAchievement(DiscoveryAchievementType.FIRST_LOCATION_DISCOVERED)) {
                newAchievements.add(createAchievement(DiscoveryAchievementType.FIRST_LOCATION_DISCOVERED))
            }
            10 -> if (!achievementState.hasAchievement(DiscoveryAchievementType.TEN_LOCATIONS_DISCOVERED)) {
                newAchievements.add(createAchievement(DiscoveryAchievementType.TEN_LOCATIONS_DISCOVERED))
            }
            25 -> if (!achievementState.hasAchievement(DiscoveryAchievementType.TWENTY_FIVE_LOCATIONS_DISCOVERED)) {
                newAchievements.add(createAchievement(DiscoveryAchievementType.TWENTY_FIVE_LOCATIONS_DISCOVERED))
            }
            50 -> if (!achievementState.hasAchievement(DiscoveryAchievementType.FIFTY_LOCATIONS_DISCOVERED)) {
                newAchievements.add(createAchievement(DiscoveryAchievementType.FIFTY_LOCATIONS_DISCOVERED))
            }
        }
        
        // Hidden location achievement
        if (isHidden && !achievementState.hasAchievement(DiscoveryAchievementType.HIDDEN_LOCATION_DISCOVERED)) {
            newAchievements.add(createAchievement(DiscoveryAchievementType.HIDDEN_LOCATION_DISCOVERED))
        }
        
        // Update player state
        gameStateManager?.updateWorldExploration { state ->
            state.copy(
                visitedLocations = state.visitedLocations + location.id,
                achievementState = state.achievementState.copy(
                    unlockedAchievements = state.achievementState.unlockedAchievements + newAchievements,
                    hiddenLocationsFound = if (isHidden) {
                        state.achievementState.hiddenLocationsFound + location.id
                    } else {
                        state.achievementState.hiddenLocationsFound
                    }
                )
            )
        }
        
        // Apply rewards
        applyReward(reward)
        
        // Log discovery
        gameStateManager?.appendChoice("location_discovered_${location.id}")
        
        return DiscoveryResult.FirstDiscovery(location.id, location.name, reward, newAchievements)
    }
    
    /**
     * Check and grant milestone achievements based on current state.
     */
    suspend fun checkMilestoneAchievements(): List<DiscoveryResult.MilestoneReached> = mutex.withLock {
        val player = gameStateManager?.playerState?.value ?: return emptyList()
        val explorationState = player.worldExploration
        val achievementState = explorationState.achievementState
        val milestones = mutableListOf<DiscoveryResult.MilestoneReached>()
        
        // All regions discovered
        val allRegions = regionCatalog.getAllRegions()
        if (explorationState.discoveredRegions.size == allRegions.size &&
            !achievementState.hasAchievement(DiscoveryAchievementType.ALL_REGIONS_DISCOVERED)) {
            val achievement = createAchievement(DiscoveryAchievementType.ALL_REGIONS_DISCOVERED)
            val reward = DiscoveryReward(experiencePoints = 500, seeds = 1000)
            milestones.add(DiscoveryResult.MilestoneReached(
                DiscoveryAchievementType.ALL_REGIONS_DISCOVERED,
                reward,
                achievement
            ))
            unlockAchievement(achievement)
            applyReward(reward)
        }
        
        // All biomes discovered
        val allBiomes = setOf(BiomeType.FOREST, BiomeType.BEACH, BiomeType.MOUNTAIN, BiomeType.WETLAND, BiomeType.RUINS)
        if (achievementState.discoveredBiomes.containsAll(allBiomes) &&
            !achievementState.hasAchievement(DiscoveryAchievementType.ALL_BIOMES_DISCOVERED)) {
            val achievement = createAchievement(DiscoveryAchievementType.ALL_BIOMES_DISCOVERED)
            val reward = DiscoveryReward(experiencePoints = 300, seeds = 500)
            milestones.add(DiscoveryResult.MilestoneReached(
                DiscoveryAchievementType.ALL_BIOMES_DISCOVERED,
                reward,
                achievement
            ))
            unlockAchievement(achievement)
            applyReward(reward)
        }
        
        // World traveler (10 fast travel locations)
        if (explorationState.fastTravelUnlocked.size >= 10 &&
            !achievementState.hasAchievement(DiscoveryAchievementType.WORLD_TRAVELER)) {
            val achievement = createAchievement(DiscoveryAchievementType.WORLD_TRAVELER)
            val reward = DiscoveryReward(experiencePoints = 200, seeds = 300)
            milestones.add(DiscoveryResult.MilestoneReached(
                DiscoveryAchievementType.WORLD_TRAVELER,
                reward,
                achievement
            ))
            unlockAchievement(achievement)
            applyReward(reward)
        }
        
        return milestones
    }
    
    /**
     * Get all unlocked achievements.
     */
    fun getUnlockedAchievements(): List<UnlockedAchievement> {
        val player = gameStateManager?.playerState?.value ?: return emptyList()
        return player.worldExploration.achievementState.unlockedAchievements
    }
    
    /**
     * Get achievement progress statistics.
     */
    fun getAchievementProgress(): AchievementProgress {
        val player = gameStateManager?.playerState?.value ?: return AchievementProgress()
        val explorationState = player.worldExploration
        val achievementState = explorationState.achievementState
        
        return AchievementProgress(
            totalAchievements = DiscoveryAchievementType.entries.size,
            unlockedAchievements = achievementState.unlockedAchievements.size,
            regionsDiscovered = explorationState.discoveredRegions.size,
            totalRegions = regionCatalog.getAllRegions().size,
            locationsVisited = explorationState.visitedLocations.size,
            biomesDiscovered = achievementState.discoveredBiomes.size,
            totalBiomes = 5 // Forest, Beach, Mountain, Swamp, Ruins
        )
    }
    
    // Private helper methods
    
    private fun createAchievement(type: DiscoveryAchievementType): UnlockedAchievement {
        val (title, description) = when (type) {
            DiscoveryAchievementType.FIRST_REGION_DISCOVERED -> "Explorer" to "Discovered your first region beyond Buttonburgh"
            DiscoveryAchievementType.ALL_REGIONS_DISCOVERED -> "World Mapper" to "Discovered all regions in the world"
            DiscoveryAchievementType.DANGEROUS_REGION_DISCOVERED -> "Brave Quail" to "Ventured into a dangerous region"
            DiscoveryAchievementType.FIRST_LOCATION_DISCOVERED -> "First Steps" to "Visited your first location"
            DiscoveryAchievementType.TEN_LOCATIONS_DISCOVERED -> "Wanderer" to "Visited 10 different locations"
            DiscoveryAchievementType.TWENTY_FIVE_LOCATIONS_DISCOVERED -> "Adventurer" to "Visited 25 different locations"
            DiscoveryAchievementType.FIFTY_LOCATIONS_DISCOVERED -> "Veteran Explorer" to "Visited 50 different locations"
            DiscoveryAchievementType.ALL_LOCATIONS_DISCOVERED -> "Master Explorer" to "Visited every location in the world"
            DiscoveryAchievementType.FIRST_FOREST_DISCOVERED -> "Into the Woods" to "Discovered your first forest location"
            DiscoveryAchievementType.FIRST_BEACH_DISCOVERED -> "Beachcomber" to "Discovered your first beach location"
            DiscoveryAchievementType.FIRST_MOUNTAIN_DISCOVERED -> "Peak Seeker" to "Discovered your first mountain location"
            DiscoveryAchievementType.FIRST_SWAMP_DISCOVERED -> "Marsh Walker" to "Discovered your first swamp location"
            DiscoveryAchievementType.FIRST_RUINS_DISCOVERED -> "Archaeologist" to "Discovered your first ancient ruins"
            DiscoveryAchievementType.ALL_BIOMES_DISCOVERED -> "Biome Master" to "Discovered all biome types"
            DiscoveryAchievementType.FEARLESS_EXPLORER -> "Fearless Explorer" to "Visited 5 dangerous locations"
            DiscoveryAchievementType.WORLD_TRAVELER -> "World Traveler" to "Unlocked fast travel to 10 locations"
            DiscoveryAchievementType.COMPLETIONIST -> "Completionist" to "Achieved 100% exploration"
            DiscoveryAchievementType.HIDDEN_LOCATION_DISCOVERED -> "Secret Finder" to "Discovered a hidden location"
            DiscoveryAchievementType.LEGENDARY_LOCATION_DISCOVERED -> "Legend Seeker" to "Discovered a legendary location"
        }
        
        return UnlockedAchievement(
            achievementId = AchievementId("achievement_${type.name.lowercase()}"),
            achievementType = type,
            unlockedAt = timestampProvider(),
            title = title,
            description = description
        )
    }
    
    private suspend fun unlockAchievement(achievement: UnlockedAchievement) {
        gameStateManager?.updateWorldExploration { state ->
            state.copy(
                achievementState = state.achievementState.copy(
                    unlockedAchievements = state.achievementState.unlockedAchievements + achievement
                )
            )
        }
    }
    
    private suspend fun applyReward(reward: DiscoveryReward) {
        if (reward.experiencePoints > 0) {
            // TODO: Apply XP when player leveling system exists
        }
        
        if (reward.seeds > 0) {
            // Add seeds as item to inventory
            gameStateManager?.updateInventory { inventory ->
                inventory.add(ItemStack(ItemId("seed"), reward.seeds))
            }
        }
        
        if (reward.items.isNotEmpty()) {
            reward.items.forEach { (itemId, quantity) ->
                gameStateManager?.updateInventory { inventory ->
                    inventory.add(ItemStack(ItemId(itemId), quantity))
                }
            }
        }
    }
}

/**
 * Achievement progress statistics.
 */
data class AchievementProgress(
    val totalAchievements: Int = 0,
    val unlockedAchievements: Int = 0,
    val regionsDiscovered: Int = 0,
    val totalRegions: Int = 0,
    val locationsVisited: Int = 0,
    val biomesDiscovered: Int = 0,
    val totalBiomes: Int = 0
) {
    val achievementPercentage: Int
        get() = if (totalAchievements > 0) (unlockedAchievements * 100 / totalAchievements) else 0
    
    val regionPercentage: Int
        get() = if (totalRegions > 0) (regionsDiscovered * 100 / totalRegions) else 0
    
    val biomePercentage: Int
        get() = if (totalBiomes > 0) (biomesDiscovered * 100 / totalBiomes) else 0
}
