package com.jalmarquest.feature.explore

import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.state.aidirector.AIDirectorManager
import com.jalmarquest.feature.eventengine.SnippetSelector

/**
 * Alpha 2.2: AI Director-enhanced snippet selector with adaptive event type recommendations.
 * 
 * Selects lore snippets based on player playstyle preferences with 60% weight to recommended
 * event types (COMBAT/EXPLORATION/SOCIAL/RESOURCE/NARRATIVE/CHAOS) and 40% to variety.
 */
class DefaultSnippetSelector(
    private val repository: LoreSnippetRepository,
    private val aiDirectorManager: AIDirectorManager? = null  // Alpha 2.2: Optional for backward compatibility
) : SnippetSelector {
    override fun findMatchingSnippet(choiceLog: ChoiceLog): String? {
        // Alpha 2.2: Use AI Director recommendation if available
        val recommendation = aiDirectorManager?.recommendEventType()
        val recommendedType = recommendation?.name
        
        return if (recommendedType != null) {
            repository.nextAvailableSnippetWithRecommendation(
                choiceLog = choiceLog,
                locationId = null,  // Location filtering deferred to future enhancement
                biomeType = null,   // Biome filtering deferred to future enhancement
                recommendedEventType = recommendedType
            )
        } else {
            repository.nextAvailableSnippet(choiceLog)
        }
    }
}
