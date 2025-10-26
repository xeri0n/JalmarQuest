package com.jalmarquest.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LocationTest {
    @Test
    fun `location should have id name and description`() {
        val location = Location(
            id = "test_location",
            name = "Test Location",
            description = "A test location"
        )
        
        assertEquals("test_location", location.id)
        assertEquals("Test Location", location.name)
        assertEquals("A test location", location.description)
    }
    
    @Test
    fun `location can have connections`() {
        val location = Location(
            id = "test_location",
            name = "Test Location",
            description = "A test location",
            connections = mapOf(
                Direction.NORTH to "north_location",
                Direction.SOUTH to "south_location"
            )
        )
        
        assertEquals("north_location", location.connections[Direction.NORTH])
        assertEquals("south_location", location.connections[Direction.SOUTH])
        assertNull(location.connections[Direction.EAST])
    }
}
