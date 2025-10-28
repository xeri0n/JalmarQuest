package com.jalmarquest.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Unique identifier for a location node in the world graph.
 */
@Serializable
@JvmInline
value class LocationNodeId(val value: String)

/**
 * Type/category of a location node for visual representation.
 */
@Serializable
enum class NodeType {
    @SerialName("hub")
    HUB,              // Major settlement (Buttonburgh)
    
    @SerialName("nest")
    NEST,             // The player's nest
    
    @SerialName("key_location")
    KEY_LOCATION,     // Important location (quest hub, NPC town)
    
    @SerialName("point_of_interest")
    POINT_OF_INTEREST, // Notable location (ruins, landmarks)
    
    @SerialName("nest_scrape")
    NEST_SCRAPE,      // Fast travel point
    
    @SerialName("filler")
    FILLER,           // Generic traversal node (paths, clearings)
    
    @SerialName("danger")
    DANGER,           // Enemy territory, hazardous area
    
    @SerialName("resource")
    RESOURCE          // Rich foraging/harvesting location
}

/**
 * Visual position of a node on the 2D map canvas.
 * Coordinates are normalized 0.0-1.0 for responsive scaling.
 */
@Serializable
data class NodePosition(
    val x: Float, // 0.0 = left edge, 1.0 = right edge
    val y: Float  // 0.0 = top edge, 1.0 = bottom edge
) {
    init {
        require(x in 0.0f..1.0f) { "x must be between 0.0 and 1.0" }
        require(y in 0.0f..1.0f) { "y must be between 0.0 and 1.0" }
    }
}

/**
 * Connection between two location nodes.
 * Connections can be unidirectional or bidirectional.
 */
@Serializable
data class NodeConnection(
    @SerialName("target_node_id")
    val targetNodeId: LocationNodeId,
    
    @SerialName("is_bidirectional")
    val isBidirectional: Boolean = true,
    
    /**
     * Optional travel time in seconds (affects time of day advancement).
     */
    @SerialName("travel_time_seconds")
    val travelTimeSeconds: Int = 60,
    
    /**
     * Optional requirement to traverse this connection.
     * Examples: "quest_complete:quest_bridge_repair", "item:KEY_IRON_KEY"
     */
    @SerialName("requirement")
    val requirement: String? = null
)

/**
 * A single location node in the world graph.
 * This is the fundamental unit of world navigation.
 */
@Serializable
data class LocationNode(
    val id: LocationNodeId,
    
    /**
     * Display name shown on the map and in UI.
     */
    val name: String,
    
    /**
     * Rich description shown when inspecting the node.
     */
    val description: String,
    
    /**
     * Node type affects visual representation and behavior.
     */
    val type: NodeType,
    
    /**
     * Position on the 2D map canvas (normalized 0.0-1.0).
     */
    val position: NodePosition,
    
    /**
     * Biome/region this node belongs to for visual grouping.
     */
    val region: String,
    
    /**
     * Outgoing connections to adjacent nodes.
     */
    val connections: List<NodeConnection> = emptyList(),
    
    /**
     * NPCs present at this location (by NPC ID).
     */
    @SerialName("npc_ids")
    val npcIds: List<String> = emptyList(),
    
    /**
     * Enemies that can spawn at this location (by enemy ID).
     */
    @SerialName("enemy_ids")
    val enemyIds: List<String> = emptyList(),
    
    /**
     * Quest IDs that can be started or progressed at this location.
     */
    @SerialName("quest_ids")
    val questIds: List<String> = emptyList(),
    
    /**
     * Foraging/harvesting nodes available (item IDs or ingredient IDs).
     */
    @SerialName("resource_ids")
    val resourceIds: List<String> = emptyList(),
    
    /**
     * Ambient audio track ID for this location.
     */
    @SerialName("ambient_audio_id")
    val ambientAudioId: String? = null,
    
    /**
     * If true, this node is a Nest Scrape fast travel point.
     */
    @SerialName("is_nest_scrape")
    val isNestScrape: Boolean = false,
    
    /**
     * If true, this node has special scripted content or events.
     */
    @SerialName("has_special_event")
    val hasSpecialEvent: Boolean = false
) {
    /**
     * Check if this node connects to another node (in either direction).
     */
    fun connectsTo(targetId: LocationNodeId): Boolean =
        connections.any { it.targetNodeId == targetId }
    
    /**
     * Get the connection to a specific target node, if exists.
     */
    fun getConnectionTo(targetId: LocationNodeId): NodeConnection? =
        connections.firstOrNull { it.targetNodeId == targetId }
    
    /**
     * Check if this node has any NPCs.
     */
    fun hasNpcs(): Boolean = npcIds.isNotEmpty()
    
    /**
     * Check if this node has any enemies.
     */
    fun hasEnemies(): Boolean = enemyIds.isNotEmpty()
    
    /**
     * Check if this node has any quests.
     */
    fun hasQuests(): Boolean = questIds.isNotEmpty()
    
    /**
     * Check if this node has any harvestable resources.
     */
    fun hasResources(): Boolean = resourceIds.isNotEmpty()
}

/**
 * Fog of war data tracking which nodes have been revealed.
 */
@Serializable
data class FogOfWarData(
    /**
     * Set of revealed node IDs. Only these nodes and their connections are visible.
     */
    @SerialName("revealed_nodes")
    val revealedNodes: Set<LocationNodeId> = emptySet(),
    
    /**
     * Set of activated Nest Scrape IDs (subset of revealed nodes).
     */
    @SerialName("activated_nest_scrapes")
    val activatedNestScrapes: Set<LocationNodeId> = emptySet()
) {
    /**
     * Check if a node has been revealed.
     */
    fun isNodeRevealed(nodeId: LocationNodeId): Boolean =
        revealedNodes.contains(nodeId)
    
    /**
     * Check if a Nest Scrape has been activated.
     */
    fun isNestScrapeActivated(nodeId: LocationNodeId): Boolean =
        activatedNestScrapes.contains(nodeId)
    
    /**
     * Reveal a new node.
     */
    fun revealNode(nodeId: LocationNodeId): FogOfWarData =
        copy(revealedNodes = revealedNodes + nodeId)
    
    /**
     * Activate a Nest Scrape.
     */
    fun activateNestScrape(nodeId: LocationNodeId): FogOfWarData =
        copy(
            revealedNodes = revealedNodes + nodeId,
            activatedNestScrapes = activatedNestScrapes + nodeId
        )
}

/**
 * Player's current state in the world map.
 */
@Serializable
data class WorldMapState(
    /**
     * Current location node ID where the player is standing.
     */
    @SerialName("current_node_id")
    val currentNodeId: LocationNodeId,
    
    /**
     * Fog of war revelation tracking.
     */
    @SerialName("fog_of_war")
    val fogOfWar: FogOfWarData = FogOfWarData(),
    
    /**
     * History of visited nodes (for analytics and achievements).
     */
    @SerialName("visited_nodes")
    val visitedNodes: List<LocationNodeId> = emptyList(),
    
    /**
     * Total nodes discovered (revealed but not necessarily visited).
     */
    @SerialName("total_nodes_discovered")
    val totalNodesDiscovered: Int = 0
) {
    /**
     * Check if player can fast travel from current location.
     */
    fun canFastTravel(): Boolean =
        fogOfWar.isNestScrapeActivated(currentNodeId)
    
    /**
     * Get list of all activated fast travel destinations.
     */
    fun getFastTravelDestinations(): Set<LocationNodeId> =
        fogOfWar.activatedNestScrapes
    
    /**
     * Add a node to visit history.
     */
    fun recordVisit(nodeId: LocationNodeId): WorldMapState =
        if (visitedNodes.contains(nodeId)) {
            this
        } else {
            copy(visitedNodes = visitedNodes + nodeId)
        }
}

/**
 * Complete world graph containing all location nodes.
 */
@Serializable
data class WorldGraph(
    /**
     * All location nodes indexed by ID.
     */
    val nodes: Map<LocationNodeId, LocationNode> = emptyMap(),
    
    /**
     * ID of the starting node (typically Buttonburgh hub).
     */
    @SerialName("starting_node_id")
    val startingNodeId: LocationNodeId,
    
    /**
     * Total count of nodes for statistics.
     */
    @SerialName("total_nodes")
    val totalNodes: Int = 0
) {
    /**
     * Get a node by ID.
     */
    fun getNode(nodeId: LocationNodeId): LocationNode? =
        nodes[nodeId]
    
    /**
     * Get all adjacent nodes to a given node.
     */
    fun getAdjacentNodes(nodeId: LocationNodeId): List<LocationNode> {
        val node = getNode(nodeId) ?: return emptyList()
        return node.connections.mapNotNull { connection ->
            getNode(connection.targetNodeId)
        }
    }
    
    /**
     * Get all nodes of a specific type.
     */
    fun getNodesByType(type: NodeType): List<LocationNode> =
        nodes.values.filter { it.type == type }
    
    /**
     * Get all nodes in a specific region.
     */
    fun getNodesByRegion(region: String): List<LocationNode> =
        nodes.values.filter { it.region == region }
    
    /**
     * Get all Nest Scrape nodes.
     */
    fun getNestScrapes(): List<LocationNode> =
        nodes.values.filter { it.isNestScrape }
    
    /**
     * Validate graph integrity (all connections point to valid nodes).
     */
    fun validateIntegrity(): List<String> {
        val errors = mutableListOf<String>()
        
        nodes.values.forEach { node ->
            node.connections.forEach { connection ->
                if (!nodes.containsKey(connection.targetNodeId)) {
                    errors.add("Node ${node.id.value} has invalid connection to ${connection.targetNodeId.value}")
                }
            }
        }
        
        if (!nodes.containsKey(startingNodeId)) {
            errors.add("Starting node ${startingNodeId.value} does not exist in graph")
        }
        
        return errors
    }
}
