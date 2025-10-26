package com.jalmarquest.core.model

data class Location(
    val id: String,
    val name: String,
    val description: String,
    val connections: Map<Direction, String> = emptyMap() // Direction to Location ID
)

enum class Direction {
    NORTH, SOUTH, EAST, WEST, UP, DOWN
}
