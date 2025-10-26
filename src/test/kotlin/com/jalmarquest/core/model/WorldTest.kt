package com.jalmarquest.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WorldTest {
    @Test
    fun `world should have name and starting location`() {
        val location1 = Location(
            id = "loc1",
            name = "Location 1",
            description = "First location"
        )
        
        val world = World(
            name = "Test World",
            locations = mapOf("loc1" to location1),
            startingLocationId = "loc1"
        )
        
        assertEquals("Test World", world.name)
        assertEquals("loc1", world.startingLocationId)
    }
    
    @Test
    fun `getLocation should return location by id`() {
        val location1 = Location(
            id = "loc1",
            name = "Location 1",
            description = "First location"
        )
        
        val world = World(
            name = "Test World",
            locations = mapOf("loc1" to location1),
            startingLocationId = "loc1"
        )
        
        val retrieved = world.getLocation("loc1")
        assertNotNull(retrieved)
        assertEquals("loc1", retrieved.id)
        assertEquals("Location 1", retrieved.name)
    }
    
    @Test
    fun `getLocation should return null for non-existent id`() {
        val world = World(
            name = "Test World",
            locations = emptyMap(),
            startingLocationId = "loc1"
        )
        
        assertNull(world.getLocation("non_existent"))
    }
    
    @Test
    fun `getStartingLocation should return the starting location`() {
        val location1 = Location(
            id = "start",
            name = "Starting Point",
            description = "The beginning"
        )
        
        val world = World(
            name = "Test World",
            locations = mapOf("start" to location1),
            startingLocationId = "start"
        )
        
        val startLocation = world.getStartingLocation()
        assertNotNull(startLocation)
        assertEquals("start", startLocation.id)
        assertEquals("Starting Point", startLocation.name)
    }
    
    @Test
    fun `getConnectedLocation should return connected location`() {
        val location1 = Location(
            id = "loc1",
            name = "Location 1",
            description = "First location",
            connections = mapOf(Direction.NORTH to "loc2")
        )
        
        val location2 = Location(
            id = "loc2",
            name = "Location 2",
            description = "Second location"
        )
        
        val world = World(
            name = "Test World",
            locations = mapOf(
                "loc1" to location1,
                "loc2" to location2
            ),
            startingLocationId = "loc1"
        )
        
        val connected = world.getConnectedLocation("loc1", Direction.NORTH)
        assertNotNull(connected)
        assertEquals("loc2", connected.id)
    }
    
    @Test
    fun `getConnectedLocation should return null for non-existent direction`() {
        val location1 = Location(
            id = "loc1",
            name = "Location 1",
            description = "First location"
        )
        
        val world = World(
            name = "Test World",
            locations = mapOf("loc1" to location1),
            startingLocationId = "loc1"
        )
        
        assertNull(world.getConnectedLocation("loc1", Direction.NORTH))
    }
    
    @Test
    fun `getConnectedLocation should return null for non-existent source location`() {
        val world = World(
            name = "Test World",
            locations = emptyMap(),
            startingLocationId = "loc1"
        )
        
        assertNull(world.getConnectedLocation("non_existent", Direction.NORTH))
    }
}
