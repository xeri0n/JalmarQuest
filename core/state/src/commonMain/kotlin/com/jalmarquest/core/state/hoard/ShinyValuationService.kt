package com.jalmarquest.core.state.hoard

import com.jalmarquest.core.model.HoardRank
import com.jalmarquest.core.model.Shiny
import com.jalmarquest.core.model.ShinyCollection
import com.jalmarquest.core.model.ShinyId
import com.jalmarquest.core.model.ShinyRarity

/**
 * Service for valuing Shinies and managing the Shiny catalog.
 * In vertical slice, uses hardcoded catalog. Future: load from backend database.
 */
class ShinyValuationService {
    
    private val catalog: Map<ShinyId, Shiny> = buildShinyCatalog()
    
    /**
     * Get all available Shinies in the catalog.
     */
    fun getAllShinies(): List<Shiny> = catalog.values.toList()
    
    /**
     * Look up a Shiny by ID.
     */
    fun getShiny(id: ShinyId): Shiny? = catalog[id]
    
    /**
     * Calculate the total value of a Shiny collection.
     */
    fun calculateTotalValue(collection: ShinyCollection): Long =
        collection.calculateTotalValue()
    
    /**
     * Calculate progress to next tier as a percentage (0.0 to 1.0).
     * Returns null if already at max tier.
     */
    fun calculateTierProgress(hoardRank: HoardRank): Float? {
        val nextThreshold = HoardRank.nextTierThreshold(hoardRank.tier) ?: return null
        val currentTierMin = when (hoardRank.tier) {
            com.jalmarquest.core.model.HoardRankTier.SCAVENGER -> 0L
            com.jalmarquest.core.model.HoardRankTier.COLLECTOR -> 1_000L
            com.jalmarquest.core.model.HoardRankTier.CURATOR -> 5_000L
            com.jalmarquest.core.model.HoardRankTier.MAGNATE -> 15_000L
            com.jalmarquest.core.model.HoardRankTier.LEGEND -> 50_000L
            com.jalmarquest.core.model.HoardRankTier.MYTH -> 150_000L
        }
        
        val progress = (hoardRank.totalValue - currentTierMin).toFloat() / (nextThreshold - currentTierMin).toFloat()
        return progress.coerceIn(0f, 1f)
    }
    
    private fun buildShinyCatalog(): Map<ShinyId, Shiny> {
        val shinies = listOf(
            // Common Shinies (50-200 Seeds)
            Shiny(
                id = ShinyId("acorn_cap"),
                nameKey = "shiny_acorn_cap_name",
                descriptionKey = "shiny_acorn_cap_desc",
                rarity = ShinyRarity.COMMON,
                baseValue = 50
            ),
            Shiny(
                id = ShinyId("smooth_pebble"),
                nameKey = "shiny_smooth_pebble_name",
                descriptionKey = "shiny_smooth_pebble_desc",
                rarity = ShinyRarity.COMMON,
                baseValue = 75
            ),
            Shiny(
                id = ShinyId("blue_feather"),
                nameKey = "shiny_blue_feather_name",
                descriptionKey = "shiny_blue_feather_desc",
                rarity = ShinyRarity.COMMON,
                baseValue = 100
            ),
            
            // Uncommon Shinies (200-500 Seeds)
            Shiny(
                id = ShinyId("copper_button"),
                nameKey = "shiny_copper_button_name",
                descriptionKey = "shiny_copper_button_desc",
                rarity = ShinyRarity.UNCOMMON,
                baseValue = 250
            ),
            Shiny(
                id = ShinyId("glass_marble"),
                nameKey = "shiny_glass_marble_name",
                descriptionKey = "shiny_glass_marble_desc",
                rarity = ShinyRarity.UNCOMMON,
                baseValue = 300
            ),
            
            // Rare Shinies (500-1500 Seeds)
            Shiny(
                id = ShinyId("silver_thimble"),
                nameKey = "shiny_silver_thimble_name",
                descriptionKey = "shiny_silver_thimble_desc",
                rarity = ShinyRarity.RARE,
                baseValue = 750
            ),
            Shiny(
                id = ShinyId("emerald_shard"),
                nameKey = "shiny_emerald_shard_name",
                descriptionKey = "shiny_emerald_shard_desc",
                rarity = ShinyRarity.RARE,
                baseValue = 1200
            ),
            
            // Epic Shinies (1500-5000 Seeds)
            Shiny(
                id = ShinyId("golden_acorn"),
                nameKey = "shiny_golden_acorn_name",
                descriptionKey = "shiny_golden_acorn_desc",
                rarity = ShinyRarity.EPIC,
                baseValue = 2500
            ),
            Shiny(
                id = ShinyId("ancient_coin"),
                nameKey = "shiny_ancient_coin_name",
                descriptionKey = "shiny_ancient_coin_desc",
                rarity = ShinyRarity.EPIC,
                baseValue = 4000
            ),
            
            // Legendary Shinies (5000-15000 Seeds)
            Shiny(
                id = ShinyId("star_fragment"),
                nameKey = "shiny_star_fragment_name",
                descriptionKey = "shiny_star_fragment_desc",
                rarity = ShinyRarity.LEGENDARY,
                baseValue = 8000
            ),
            Shiny(
                id = ShinyId("phoenix_plume"),
                nameKey = "shiny_phoenix_plume_name",
                descriptionKey = "shiny_phoenix_plume_desc",
                rarity = ShinyRarity.LEGENDARY,
                baseValue = 12000
            ),
            
            // Alpha 2.2: Creator Coffee reward
            Shiny(
                id = ShinyId("golden_coffee_bean"),
                nameKey = "shiny_golden_coffee_bean_name",
                descriptionKey = "shiny_golden_coffee_bean_desc",
                rarity = ShinyRarity.LEGENDARY,
                baseValue = 5000
            ),
            
            // Mythic Shinies (15000+ Seeds)
            Shiny(
                id = ShinyId("crown_of_seasons"),
                nameKey = "shiny_crown_of_seasons_name",
                descriptionKey = "shiny_crown_of_seasons_desc",
                rarity = ShinyRarity.MYTHIC,
                baseValue = 25000
            ),
            Shiny(
                id = ShinyId("heart_of_the_forest"),
                nameKey = "shiny_heart_of_the_forest_name",
                descriptionKey = "shiny_heart_of_the_forest_desc",
                rarity = ShinyRarity.MYTHIC,
                baseValue = 50000
            )
        )
        
        return shinies.associateBy { it.id }
    }
}
