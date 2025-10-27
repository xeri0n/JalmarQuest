package com.jalmarquest.feature.explore

import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.state.catalogs.WorldRegionCatalog
import com.jalmarquest.feature.eventengine.SnippetSelector

/**
 * Location-aware snippet selector that filters encounters by current location and biome.
 * Falls back to generic snippets if no location-specific ones are available.
 */
class LocationAwareSnippetSelector(
    private val repository: LoreSnippetRepository,
    private val regionCatalog: WorldRegionCatalog
) : SnippetSelector {
    
    override fun findMatchingSnippet(choiceLog: ChoiceLog): String? {
        return repository.nextAvailableSnippet(choiceLog)
    }
    
    /**
     * Find matching snippet considering player's current location and region biome.
     * Priority: location-specific > biome-specific > generic
     */
    fun findMatchingSnippetForLocation(player: Player): String? {
        val currentLocationId = player.worldExploration.currentLocationId
        val choiceLog = player.choiceLog
        
        // Find current region to get biome type
        val currentRegion = regionCatalog.getAllRegions()
            .find { region -> currentLocationId in region.primaryLocationIds }
        
        val currentBiome = currentRegion?.biomeType?.name?.lowercase()
        
        // Try to find location-specific snippet first
        val locationSpecific = repository.nextAvailableSnippetForLocation(
            choiceLog = choiceLog,
            locationId = currentLocationId,
            biomeType = currentBiome
        )
        
        // Fallback to any available snippet
        return locationSpecific ?: repository.nextAvailableSnippet(choiceLog)
    }
}
