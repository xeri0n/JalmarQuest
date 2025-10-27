package com.jalmarquest.core.model

data class World(
    val name: String,
    val locations: Map<String, Location> = emptyMap(),
    val startingLocationId: String
) {
    fun getLocation(locationId: String): Location? = locations[locationId]
    
    fun getConnectedLocation(fromLocationId: String, direction: Direction): Location? {
        val currentLocation = getLocation(fromLocationId) ?: return null
        val destinationId = currentLocation.connections[direction] ?: return null
        return getLocation(destinationId)
    }
    
    fun getStartingLocation(): Location? = getLocation(startingLocationId)
}
