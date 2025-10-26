package com.jalmarquest.core.state.npc

import com.jalmarquest.core.state.catalogs.Npc
import com.jalmarquest.core.state.catalogs.NpcCatalog
import com.jalmarquest.core.state.time.InGameTimeManager
import com.jalmarquest.core.state.time.TimeOfDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * Manages NPC schedules and locations throughout the day.
 * NPCs can move between locations based on time of day.
 */

@Serializable
data class NpcScheduleEntry(
    val npcId: String,
    val timeOfDay: TimeOfDay,
    val locationId: String,
    val activityDescription: String? = null
)

@Serializable
data class NpcSchedule(
    val npcId: String,
    val defaultLocationId: String,
    val schedule: List<NpcScheduleEntry> = emptyList()
) {
    /**
     * Get the location where this NPC should be at a given time of day.
     */
    fun getLocationAt(timeOfDay: TimeOfDay): String {
        return schedule.firstOrNull { it.timeOfDay == timeOfDay }?.locationId
            ?: defaultLocationId
    }
    
    /**
     * Get the activity description for this NPC at a given time.
     */
    fun getActivityAt(timeOfDay: TimeOfDay): String? {
        return schedule.firstOrNull { it.timeOfDay == timeOfDay }?.activityDescription
    }
}

class NpcScheduleManager(
    private val npcCatalog: NpcCatalog,
    private val timeManager: InGameTimeManager
) {
    private val schedules = mutableMapOf<String, NpcSchedule>()
    
    private val _npcLocations = MutableStateFlow<Map<String, String>>(emptyMap())
    val npcLocations: StateFlow<Map<String, String>> = _npcLocations.asStateFlow()
    
    init {
        registerDefaultSchedules()
        updateNpcLocations()
    }
    
    /**
     * Register a schedule for an NPC.
     */
    fun registerSchedule(schedule: NpcSchedule) {
        schedules[schedule.npcId] = schedule
    }
    
    /**
     * Get the schedule for a specific NPC.
     */
    fun getSchedule(npcId: String): NpcSchedule? = schedules[npcId]
    
    /**
     * Get the current location of an NPC based on the time of day.
     */
    fun getCurrentLocation(npcId: String): String {
        val schedule = schedules[npcId]
        val currentTimeOfDay = timeManager.currentTime.value.getTimeOfDay()
        
        return schedule?.getLocationAt(currentTimeOfDay)
            ?: npcCatalog.getNpcById(npcId)?.locationId
            ?: "buttonburgh_centre" // Fallback
    }
    
    /**
     * Get the current activity description for an NPC.
     */
    fun getCurrentActivity(npcId: String): String? {
        val schedule = schedules[npcId]
        val currentTimeOfDay = timeManager.currentTime.value.getTimeOfDay()
        return schedule?.getActivityAt(currentTimeOfDay)
    }
    
    /**
     * Get all NPCs currently at a specific location.
     */
    fun getNpcsAtLocation(locationId: String): List<Npc> {
        timeManager.updateTime()
        val currentTimeOfDay = timeManager.currentTime.value.getTimeOfDay()
        
        return npcCatalog.getAllNpcs().filter { npc ->
            val schedule = schedules[npc.id]
            val currentLocation = schedule?.getLocationAt(currentTimeOfDay) ?: npc.locationId
            currentLocation == locationId
        }
    }
    
    /**
     * Update NPC locations based on current time.
     * Should be called periodically to keep locations in sync.
     */
    fun updateNpcLocations() {
        timeManager.updateTime()
        val locations = npcCatalog.getAllNpcs().associate { npc ->
            npc.id to getCurrentLocation(npc.id)
        }
        _npcLocations.value = locations
    }
    
    /**
     * Register default schedules for key NPCs.
     */
    private fun registerDefaultSchedules() {
        // Town NPCs with daily routines
        
        // Clara Seedsworth - Market Merchant
        registerSchedule(NpcSchedule(
            npcId = "npc_clara_seedsworth",
            defaultLocationId = "buttonburgh_market_square",
            schedule = listOf(
                NpcScheduleEntry("npc_clara_seedsworth", TimeOfDay.DAWN, "buttonburgh_roost_apartments", "Preparing for the day"),
                NpcScheduleEntry("npc_clara_seedsworth", TimeOfDay.MORNING, "buttonburgh_market_square", "Setting up stall"),
                NpcScheduleEntry("npc_clara_seedsworth", TimeOfDay.AFTERNOON, "buttonburgh_market_square", "Trading seeds"),
                NpcScheduleEntry("npc_clara_seedsworth", TimeOfDay.DUSK, "buttonburgh_market_square", "Closing up shop"),
                NpcScheduleEntry("npc_clara_seedsworth", TimeOfDay.NIGHT, "buttonburgh_tavern", "Relaxing with a drink")
            )
        ))
        
        // Barkeep Dusty - Tavern Owner
        registerSchedule(NpcSchedule(
            npcId = "npc_barkeep_dusty",
            defaultLocationId = "buttonburgh_tavern",
            schedule = listOf(
                NpcScheduleEntry("npc_barkeep_dusty", TimeOfDay.DAWN, "buttonburgh_market_square", "Buying supplies"),
                NpcScheduleEntry("npc_barkeep_dusty", TimeOfDay.MORNING, "buttonburgh_tavern", "Cleaning and preparing"),
                NpcScheduleEntry("npc_barkeep_dusty", TimeOfDay.AFTERNOON, "buttonburgh_tavern", "Serving lunch"),
                NpcScheduleEntry("npc_barkeep_dusty", TimeOfDay.DUSK, "buttonburgh_tavern", "Evening rush"),
                NpcScheduleEntry("npc_barkeep_dusty", TimeOfDay.NIGHT, "buttonburgh_tavern", "Last call")
            )
        ))
        
        // Gardener Bloom - Tends gardens
        registerSchedule(NpcSchedule(
            npcId = "npc_gardener_bloom",
            defaultLocationId = "buttonburgh_garden_terraces",
            schedule = listOf(
                NpcScheduleEntry("npc_gardener_bloom", TimeOfDay.DAWN, "buttonburgh_garden_terraces", "Watering plants"),
                NpcScheduleEntry("npc_gardener_bloom", TimeOfDay.MORNING, "buttonburgh_garden_terraces", "Tending crops"),
                NpcScheduleEntry("npc_gardener_bloom", TimeOfDay.AFTERNOON, "buttonburgh_garden_terraces", "Harvesting"),
                NpcScheduleEntry("npc_gardener_bloom", TimeOfDay.DUSK, "buttonburgh_garden_terraces", "Final check"),
                NpcScheduleEntry("npc_gardener_bloom", TimeOfDay.NIGHT, "buttonburgh_roost_apartments", "Resting")
            )
        ))
        
        // Sergeant Talon - Combat Instructor
        registerSchedule(NpcSchedule(
            npcId = "npc_sergeant_talon",
            defaultLocationId = "buttonburgh_training_grounds",
            schedule = listOf(
                NpcScheduleEntry("npc_sergeant_talon", TimeOfDay.DAWN, "buttonburgh_training_grounds", "Morning drills"),
                NpcScheduleEntry("npc_sergeant_talon", TimeOfDay.MORNING, "buttonburgh_training_grounds", "Teaching recruits"),
                NpcScheduleEntry("npc_sergeant_talon", TimeOfDay.AFTERNOON, "buttonburgh_training_grounds", "Combat practice"),
                NpcScheduleEntry("npc_sergeant_talon", TimeOfDay.DUSK, "buttonburgh_training_grounds", "Evening exercises"),
                NpcScheduleEntry("npc_sergeant_talon", TimeOfDay.NIGHT, "buttonburgh_tavern", "Sharing war stories")
            )
        ))
        
        // Wandering Minstrel - Travels around
        registerSchedule(NpcSchedule(
            npcId = "npc_wandering_minstrel",
            defaultLocationId = "buttonburgh_tavern",
            schedule = listOf(
                NpcScheduleEntry("npc_wandering_minstrel", TimeOfDay.DAWN, "forest_entrance", "Seeking inspiration"),
                NpcScheduleEntry("npc_wandering_minstrel", TimeOfDay.MORNING, "buttonburgh_market_square", "Playing for crowds"),
                NpcScheduleEntry("npc_wandering_minstrel", TimeOfDay.AFTERNOON, "buttonburgh_message_post", "Sharing news in song"),
                NpcScheduleEntry("npc_wandering_minstrel", TimeOfDay.DUSK, "buttonburgh_tavern", "Preparing for evening show"),
                NpcScheduleEntry("npc_wandering_minstrel", TimeOfDay.NIGHT, "buttonburgh_tavern", "Performing")
            )
        ))
        
        // Town Crier - Announces news
        registerSchedule(NpcSchedule(
            npcId = "npc_town_crier",
            defaultLocationId = "buttonburgh_message_post",
            schedule = listOf(
                NpcScheduleEntry("npc_town_crier", TimeOfDay.DAWN, "buttonburgh_message_post", "Reading overnight messages"),
                NpcScheduleEntry("npc_town_crier", TimeOfDay.MORNING, "buttonburgh_centre", "Morning announcements"),
                NpcScheduleEntry("npc_town_crier", TimeOfDay.AFTERNOON, "buttonburgh_market_square", "Midday news"),
                NpcScheduleEntry("npc_town_crier", TimeOfDay.DUSK, "buttonburgh_town_hall", "Receiving official notices"),
                NpcScheduleEntry("npc_town_crier", TimeOfDay.NIGHT, "buttonburgh_roost_apartments", "Off duty")
            )
        ))
        
        // Librarian Hush - Scholar's District
        registerSchedule(NpcSchedule(
            npcId = "npc_librarian_hush",
            defaultLocationId = "buttonburgh_scholars_district",
            schedule = listOf(
                NpcScheduleEntry("npc_librarian_hush", TimeOfDay.DAWN, "buttonburgh_scholars_district", "Organizing books"),
                NpcScheduleEntry("npc_librarian_hush", TimeOfDay.MORNING, "buttonburgh_scholars_district", "Assisting researchers"),
                NpcScheduleEntry("npc_librarian_hush", TimeOfDay.AFTERNOON, "buttonburgh_scholars_district", "Cataloging new arrivals"),
                NpcScheduleEntry("npc_librarian_hush", TimeOfDay.DUSK, "buttonburgh_scholars_district", "Private study"),
                NpcScheduleEntry("npc_librarian_hush", TimeOfDay.NIGHT, "buttonburgh_scholars_district", "Closing the archive")
            )
        ))
        
        // Fisher Ripple - Forest Brook
        registerSchedule(NpcSchedule(
            npcId = "npc_brook_fisher",
            defaultLocationId = "forest_babbling_brook",
            schedule = listOf(
                NpcScheduleEntry("npc_brook_fisher", TimeOfDay.DAWN, "forest_babbling_brook", "Best fishing time"),
                NpcScheduleEntry("npc_brook_fisher", TimeOfDay.MORNING, "forest_babbling_brook", "Morning catch"),
                NpcScheduleEntry("npc_brook_fisher", TimeOfDay.AFTERNOON, "buttonburgh_market_square", "Selling fish"),
                NpcScheduleEntry("npc_brook_fisher", TimeOfDay.DUSK, "forest_babbling_brook", "Evening fishing"),
                NpcScheduleEntry("npc_brook_fisher", TimeOfDay.NIGHT, "buttonburgh_tavern", "Relaxing")
            )
        ))
        
        // Old Sailor Barnacle - Beach
        registerSchedule(NpcSchedule(
            npcId = "npc_old_sailor",
            defaultLocationId = "beach_fishing_pier",
            schedule = listOf(
                NpcScheduleEntry("npc_old_sailor", TimeOfDay.DAWN, "beach_fishing_pier", "Watching sunrise"),
                NpcScheduleEntry("npc_old_sailor", TimeOfDay.MORNING, "beach_fishing_pier", "Fishing"),
                NpcScheduleEntry("npc_old_sailor", TimeOfDay.AFTERNOON, "beach_driftwood_maze", "Exploring driftwood"),
                NpcScheduleEntry("npc_old_sailor", TimeOfDay.DUSK, "beach_fishing_pier", "Watching sunset"),
                NpcScheduleEntry("npc_old_sailor", TimeOfDay.NIGHT, "beach_lighthouse_base", "Keeping watch")
            )
        ))
        
        // Archaeologist Dustwing - Ruins
        registerSchedule(NpcSchedule(
            npcId = "npc_archaeologist",
            defaultLocationId = "ruins_crumbling_walls",
            schedule = listOf(
                NpcScheduleEntry("npc_archaeologist", TimeOfDay.DAWN, "buttonburgh_scholars_district", "Reviewing notes"),
                NpcScheduleEntry("npc_archaeologist", TimeOfDay.MORNING, "ruins_crumbling_walls", "Excavating"),
                NpcScheduleEntry("npc_archaeologist", TimeOfDay.AFTERNOON, "ruins_forgotten_library", "Studying texts"),
                NpcScheduleEntry("npc_archaeologist", TimeOfDay.DUSK, "ruins_crumbling_walls", "Final documentation"),
                NpcScheduleEntry("npc_archaeologist", TimeOfDay.NIGHT, "buttonburgh_scholars_district", "Writing reports")
            )
        ))
    }
}
