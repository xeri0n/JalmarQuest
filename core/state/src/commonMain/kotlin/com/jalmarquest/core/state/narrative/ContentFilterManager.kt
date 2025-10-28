package com.jalmarquest.core.state.narrative

import com.jalmarquest.core.model.Player
import com.jalmarquest.core.state.GameStateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Content Filter Manager - Alpha 2.2 Advanced Narrative & AI Systems
 * 
 * Centralized utility for checking "No Filter Mode" state across all narrative systems.
 * This ensures consistent content filtering throughout the game.
 * 
 * Systems that use this:
 * - DialogueManager (NPC dialogue variants)
 * - AIDirectorManager (dynamic event tone)
 * - BorkenEventTrigger (chaos intensity)
 * - LoreSnippetRepository (event text variations)
 * 
 * Usage:
 * ```kotlin
 * val filterManager = ContentFilterManager(gameStateManager)
 * 
 * if (filterManager.isNoFilterModeEnabled()) {
 *     // Use mature, edgy dialogue/events
 * } else {
 *     // Use standard family-friendly content
 * }
 * ```
 */
class ContentFilterManager(
    private val gameStateManager: GameStateManager
) {
    /**
     * Check if No Filter Mode is currently enabled.
     * This is the primary method all narrative systems should use.
     */
    fun isNoFilterModeEnabled(): Boolean {
        return gameStateManager.playerState.value.playerSettings.isNoFilterModeEnabled
    }
    
    /**
     * Reactive flow for observing No Filter Mode state changes.
     * Useful for UI components that need to update when settings change.
     */
    val noFilterModeFlow: Flow<Boolean> = 
        gameStateManager.playerState.map { 
            it.playerSettings.isNoFilterModeEnabled 
        }
    
    /**
     * Check if player has supported the developer via "Coffee" donation.
     * Used to trigger permanent NPC_EXHAUSTED_CODER dialogue changes.
     */
    fun hasPurchasedCreatorCoffee(): Boolean {
        return gameStateManager.playerState.value.playerSettings.hasPurchasedCreatorCoffee
    }
}
