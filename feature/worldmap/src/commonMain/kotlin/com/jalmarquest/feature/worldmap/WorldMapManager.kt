class WorldMapManager(
    private val gameStateManager: GameStateManager,
    private val mapData: WorldMapData
) {
    // FIX: Proper fog of war and travel restrictions
    suspend fun attemptTravel(targetNodeId: String): TravelResult {
        val currentNode = getCurrentNode()
        val targetNode = mapData.nodes[targetNodeId]
            ?: return TravelResult.InvalidDestination
        
        // Check if nodes are connected
        if (!areNodesConnected(currentNode.id, targetNodeId)) {
            return TravelResult.NotConnected
        }
        
        // Check fog of war
        if (!isNodeRevealed(targetNodeId)) {
            return TravelResult.Unexplored
        }
        
        // Check travel requirements
        val requirements = targetNode.requirements
        if (!checkRequirements(requirements)) {
            return TravelResult.RequirementsNotMet(requirements)
        }
        
        // FIX: Check for fast travel waypoints
        val hasFastTravel = hasActivatedWaypoint(currentNode.id) && 
                           hasActivatedWaypoint(targetNodeId)
        
        val travelTime = if (hasFastTravel) 0 else calculateTravelTime(currentNode, targetNode)
        
        gameStateManager.batchUpdate { player ->
            player.copy(
                currentLocation = targetNodeId,
                // FIX: Apply travel fatigue if not using fast travel
                statusEffects = if (!hasFastTravel) {
                    player.statusEffects.addEffect(
                        StatusEffect.Fatigue(duration = travelTime * 60)
                    )
                } else player.statusEffects
            )
        }
        
        return TravelResult.Success(travelTime, hasFastTravel)
    }
    
    private fun hasActivatedWaypoint(nodeId: String): Boolean {
        return gameStateManager.playerState.value.activatedWaypoints.contains(nodeId)
    }
}