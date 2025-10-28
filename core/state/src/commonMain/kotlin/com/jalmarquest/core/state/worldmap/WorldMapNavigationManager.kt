package com.jalmarquest.core.state.worldmap

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.worldmap.WorldGraphBuilder
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Sealed class representing the result of a travel attempt
 */
sealed class TravelResult {
    data object Success : TravelResult()
    data class RequirementNotMet(val reason: String) : TravelResult()
    data object NodeNotRevealed : TravelResult()
    data object NotConnected : TravelResult()
}

/**
 * Sealed class representing the result of a fast travel attempt
 */
sealed class FastTravelResult {
    data object Success : FastTravelResult()
    data class NestScrapeNotActivated(val nodeId: LocationNodeId) : FastTravelResult()
}

/**
 * World Map Navigation Manager - handles node-based world travel,
 * fog of war, and fast travel via Nest Scrapes.
 * 
 * This is the Alpha 2.0 navigation system that replaces the old location-based system.
 */
class WorldMapNavigationManager(
    private val gameStateManager: GameStateManager,
    private val timestampProvider: () -> Long
) {
    private val mutex = Mutex()
    
    /**
     * The complete world graph (100+ nodes)
     */
    val worldGraph: WorldGraph = WorldGraphBuilder.buildCompleteWorld()
    
    /**
     * Current world map state from player
     */
    val worldMapState: StateFlow<WorldMapState?> = gameStateManager.playerState
        .map { it.worldMapState }
        .stateIn(
            scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default),
            started = SharingStarted.Lazily,
            initialValue = gameStateManager.playerState.value.worldMapState
        )
    
    /**
     * Current location node (derived from worldMapState)
     */
    val currentNode: StateFlow<LocationNode?> = worldMapState
        .map { state ->
            state?.currentNodeId?.let { worldGraph.nodes[it] }
        }
        .stateIn(
            scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default),
            started = SharingStarted.Lazily,
            initialValue = worldMapState.value?.currentNodeId?.let { worldGraph.nodes[it] }
        )
    
    /**
     * Initialize world map state for a new player
     */
    suspend fun initializeForNewPlayer() = mutex.withLock {
        val currentState = worldMapState.value
        if (currentState == null) {
            // Create initial state with starting node revealed
            val startingNode = worldGraph.startingNodeId
            val initialFogOfWar = FogOfWarData(
                revealedNodes = setOf(startingNode),
                activatedNestScrapes = setOf(LocationNodeId("THE_NEST")) // Player's nest is always activated
            )
            
            val initialState = WorldMapState(
                currentNodeId = startingNode,
                fogOfWar = initialFogOfWar,
                visitedNodes = listOf(startingNode),
                totalNodesDiscovered = 1
            )
            
            gameStateManager.updateWorldMapState { initialState }
        }
    }
    
    /**
     * Attempt to travel to a connected node
     */
    suspend fun travelTo(targetNodeId: LocationNodeId): TravelResult = mutex.withLock {
        val state = worldMapState.value ?: return@withLock TravelResult.NodeNotRevealed
        val currentNodeData = currentNode.value ?: return@withLock TravelResult.NodeNotRevealed
        
        // Check if target is revealed
        if (!state.fogOfWar.revealedNodes.contains(targetNodeId)) {
            return@withLock TravelResult.NodeNotRevealed
        }
        
        // Check if target is connected to current node
        val connection = currentNodeData.connections.find { it.targetNodeId == targetNodeId }
            ?: return@withLock TravelResult.NotConnected
        
        // Check requirements
        connection.requirement?.let { req ->
            val met = checkRequirement(req, gameStateManager.playerState.value)
            
            if (!met) {
                return@withLock TravelResult.RequirementNotMet(
                    reason = "Missing requirement: $req"
                )
            }
        }
        
        // Successful travel - update state
        gameStateManager.updateWorldMapState { currentState ->
            (currentState ?: state).copy(
                currentNodeId = targetNodeId,
                visitedNodes = (currentState?.visitedNodes ?: state.visitedNodes) + targetNodeId,
                totalNodesDiscovered = (currentState?.fogOfWar?.revealedNodes ?: state.fogOfWar.revealedNodes).size
            )
        }
        
        // Reveal adjacent nodes (fog of war progression)
        revealAdjacentNodes(targetNodeId)
        
        // Log travel choice
        gameStateManager.appendChoice("travel_to_${targetNodeId.value}")
        
        TravelResult.Success
    }
    
    /**
     * Fast travel to an activated Nest Scrape
     */
    suspend fun fastTravelTo(nestScrapeId: LocationNodeId): FastTravelResult = mutex.withLock {
        val state = worldMapState.value ?: return@withLock FastTravelResult.NestScrapeNotActivated(nestScrapeId)
        
        // Check if Nest Scrape is activated
        if (!state.fogOfWar.activatedNestScrapes.contains(nestScrapeId)) {
            return@withLock FastTravelResult.NestScrapeNotActivated(nestScrapeId)
        }
        
        // Fast travel (instant)
        gameStateManager.updateWorldMapState { currentState ->
            (currentState ?: state).copy(
                currentNodeId = nestScrapeId,
                visitedNodes = (currentState?.visitedNodes ?: state.visitedNodes) + nestScrapeId
            )
        }
        
        // Log fast travel
        gameStateManager.appendChoice("fast_travel_to_${nestScrapeId.value}")
        
        FastTravelResult.Success
    }
    
    /**
     * Reveal adjacent nodes when arriving at a new node (fog of war)
     */
    suspend fun revealAdjacentNodes(nodeId: LocationNodeId) = mutex.withLock {
        val state = worldMapState.value ?: return@withLock
        val node = worldGraph.nodes[nodeId] ?: return@withLock
        
        // Get all connected node IDs
        val connectedNodes = node.connections.map { it.targetNodeId }
        
        // Add to revealed set
        gameStateManager.updateWorldMapState { currentState ->
            (currentState ?: state).copy(
                fogOfWar = (currentState?.fogOfWar ?: state.fogOfWar).copy(
                    revealedNodes = (currentState?.fogOfWar?.revealedNodes ?: state.fogOfWar.revealedNodes) + connectedNodes
                )
            )
        }
    }
    
    /**
     * Activate a Nest Scrape for fast travel (called when player discovers one)
     */
    suspend fun activateNestScrape(nodeId: LocationNodeId) = mutex.withLock {
        val state = worldMapState.value ?: return@withLock
        val node = worldGraph.nodes[nodeId]
        
        // Only activate if it's actually a Nest Scrape
        if (node?.isNestScrape == true) {
            gameStateManager.updateWorldMapState { currentState ->
                (currentState ?: state).copy(
                    fogOfWar = (currentState?.fogOfWar ?: state.fogOfWar).copy(
                        activatedNestScrapes = (currentState?.fogOfWar?.activatedNestScrapes ?: state.fogOfWar.activatedNestScrapes) + nodeId
                    )
                )
            }
            
            gameStateManager.appendChoice("activate_nest_scrape_${nodeId.value}")
        }
    }
    
    /**
     * Get all activated Nest Scrapes (for fast travel menu)
     */
    fun getActivatedNestScrapes(): List<LocationNode> {
        val state = worldMapState.value ?: return emptyList()
        return state.fogOfWar.activatedNestScrapes.mapNotNull { worldGraph.nodes[it] }
    }
    
    /**
     * Get all revealed nodes (for UI rendering)
     */
    fun getRevealedNodes(): List<LocationNode> {
        val state = worldMapState.value ?: return emptyList()
        return state.fogOfWar.revealedNodes.mapNotNull { worldGraph.nodes[it] }
    }
    
    /**
     * Check if a requirement string is met by the player.
     * Format: "quest_complete:quest_id", "item:item_id", "choice_tag:tag_value"
     */
    private fun checkRequirement(requirement: String, player: Player): Boolean {
        val parts = requirement.split(":", limit = 2)
        if (parts.size != 2) return true // Invalid format, allow passage
        
        val (type, value) = parts
        return when (type) {
            "quest_complete" -> player.questLog.completedQuests.any { it.value == value }
            "item" -> player.inventory.totalQuantity(ItemId(value)) > 0
            "choice_tag" -> player.choiceLog.entries.any { it.tag.value == value }
            else -> true // Unknown requirement type, allow passage
        }
    }
}
