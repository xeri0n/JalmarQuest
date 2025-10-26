package com.jalmarquest.core.state.catalogs

import kotlinx.serialization.Serializable

/**
 * Catalog of all explorable locations in the game.
 */

@Serializable
data class Location(
    val id: String,
    val name: String,
    val description: String,
    val parentLocationId: String? = null,
    val isAccessible: Boolean = true,
    val requiresQuestId: String? = null
)

class LocationCatalog {
    private val locations = mutableMapOf<String, Location>()
    
    companion object {
        const val DEFAULT_SPAWN_LOCATION = "buttonburgh_centre"
    }
    
    init {
        registerDefaultLocations()
    }
    
    fun registerLocation(location: Location) {
        locations[location.id] = location
    }
    
    fun getLocationById(id: String): Location? = locations[id]
    
    fun getSubLocations(parentId: String): List<Location> {
        return locations.values.filter { it.parentLocationId == parentId }
    }
    
    fun getAllLocations(): List<Location> = locations.values.toList()
    
    private fun registerDefaultLocations() {
        // Main hub areas - Centre of Buttonburgh is the default spawn
        registerLocation(Location(
            id = "buttonburgh_centre",
            name = "Centre of Buttonburgh",
            description = "The heart of Buttonburgh, a bustling town square where quails gather. Safe and welcoming, this is where every adventure begins."
        ))
        
        registerLocation(Location(
            id = "buttonburgh_hub",
            name = "Buttonburgh Hub",
            description = "The central gathering place for quails in Buttonburgh."
        ))
        
        registerLocation(Location(
            id = "buttonburgh_quills_study",
            name = "Quill's Study",
            description = "A cozy library filled with ancient tomes and scholarly wisdom."
        ))
        
        registerLocation(Location(
            id = "buttonburgh_alchemy_lab",
            name = "The Alchemy Lab",
            description = "Glass vials bubble and shimmer with mysterious concoctions."
        ))
        
        registerLocation(Location(
            id = "buttonburgh_shop",
            name = "Wicker's General Store",
            description = "A well-stocked shop run by Worried Wicker."
        ))
        
        registerLocation(Location(
            id = "buttonburgh_hoard_vault",
            name = "The Hoard Vault",
            description = "Pack Rat's collection of shinies and treasures."
        ))
        
        registerLocation(Location(
            id = "buttonburgh_dust_bath",
            name = "Buttonburgh Dust Bath",
            description = "A communal dust bath for quails to clean and relax.",
            isAccessible = false, // Unlocked by quest_soothing_silence
            requiresQuestId = "quest_soothing_silence"
        ))
        
        registerLocation(Location(
            id = "buttonburgh_orphanage",
            name = "Buttonburgh Orphanage",
            description = "A safe haven for lost and orphaned quails.",
            requiresQuestId = "quest_feathered_friend"
        ))
        
        // Buttonburgh Districts - expanding the town
        registerLocation(Location(
            id = "buttonburgh_market_square",
            name = "Market Square",
            description = "A bustling marketplace where quails trade seeds, trinkets, and tales. Stalls overflow with colorful goods and the air rings with cheerful haggling.",
            parentLocationId = "buttonburgh_centre"
        ))
        
        registerLocation(Location(
            id = "buttonburgh_roost_apartments",
            name = "The Roost Apartments",
            description = "Towering shelves converted into cozy quail dwellings. Each level hums with the sounds of daily life - chirping chicks, rustling feathers, and gossiping neighbors.",
            parentLocationId = "buttonburgh_centre"
        ))
        
        registerLocation(Location(
            id = "buttonburgh_scholars_district",
            name = "Scholar's District",
            description = "Quiet nooks filled with books larger than quails themselves. The scent of old paper and ink permeates the air, punctuated by whispered debates and rustling pages.",
            parentLocationId = "buttonburgh_centre"
        ))
        
        registerLocation(Location(
            id = "buttonburgh_artisan_quarter",
            name = "Artisan Quarter",
            description = "Workshops where skilled quails craft everything from miniature tools to decorative feathers. The sound of tapping beaks and whirring wheels echoes through narrow passages.",
            parentLocationId = "buttonburgh_centre"
        ))
        
        registerLocation(Location(
            id = "buttonburgh_training_grounds",
            name = "Training Grounds",
            description = "A sandy clearing where young quails practice their skills - pecking accuracy, dust bathing technique, and the ancient art of seed-fu.",
            parentLocationId = "buttonburgh_centre"
        ))
        
        registerLocation(Location(
            id = "buttonburgh_garden_terraces",
            name = "Garden Terraces",
            description = "Tiered planters cascading with herbs, flowers, and edible greens. Butterflies dance between blooms while quails tend to their precious plants.",
            parentLocationId = "buttonburgh_centre"
        ))
        
        registerLocation(Location(
            id = "buttonburgh_message_post",
            name = "The Message Post",
            description = "A central board covered in feathers, each one carrying a message, quest notice, or community announcement. The town crier perches atop, delivering news.",
            parentLocationId = "buttonburgh_centre"
        ))
        
        registerLocation(Location(
            id = "buttonburgh_hatchling_nursery",
            name = "Hatchling Nursery",
            description = "A warm, soft space filled with downy chicks learning to chirp their first words. Patient teachers guide tiny talons through basic lessons.",
            parentLocationId = "buttonburgh_centre"
        ))
        
        registerLocation(Location(
            id = "buttonburgh_town_hall",
            name = "Town Hall",
            description = "An imposing (to a quail) structure where the Town Council meets. Important decisions about seed distribution, territorial boundaries, and festival planning happen here.",
            parentLocationId = "buttonburgh_centre"
        ))
        
        registerLocation(Location(
            id = "buttonburgh_tavern",
            name = "The Dusty Talon Tavern",
            description = "A lively gathering place where quails share fermented berry juice, swap stories, and occasionally break into song. The air smells of spilled nectar and companionship.",
            parentLocationId = "buttonburgh_centre"
        ))
        
        // Exploration zones
        registerLocation(Location(
            id = "forest",
            name = "The Forest",
            description = "A vast woodland filled with towering trees and hidden secrets."
        ))
        
        registerLocation(Location(
            id = "forest_entrance",
            name = "Forest Entrance",
            description = "The edge of the great forest.",
            parentLocationId = "forest"
        ))
        
        registerLocation(Location(
            id = "high_perch_stump",
            name = "High Perch Stump",
            description = "A tall stump that seems impossibly high for a quail.",
            parentLocationId = "forest"
        ))
        
        registerLocation(Location(
            id = "poisoned_grove",
            name = "The Poisoned Grove",
            description = "A once-beautiful grove now blighted and toxic.",
            parentLocationId = "forest"
        ))
        
        registerLocation(Location(
            id = "magpie_nest",
            name = "Magpie Nest",
            description = "A dangerous nest filled with stolen trinkets.",
            parentLocationId = "forest"
        ))
        
        registerLocation(Location(
            id = "crows_perch",
            name = "Crow's Perch",
            description = "A berry patch guarded by a territorial crow.",
            parentLocationId = "forest"
        ))
        
        registerLocation(Location(
            id = "ant_hill",
            name = "Ant Hill",
            description = "A massive ant colony bustling with activity.",
            parentLocationId = "forest"
        ))
        
        // Forest sub-locations - deep woodland exploration
        registerLocation(Location(
            id = "forest_whispering_pines",
            name = "Whispering Pines",
            description = "Ancient pines that creak and sway, their needles carpeting the ground. The silence here is profound, broken only by the distant call of owls.",
            parentLocationId = "forest"
        ))
        
        registerLocation(Location(
            id = "forest_mushroom_grove",
            name = "Mushroom Grove",
            description = "Giant toadstools tower overhead like umbrellas. Some glow faintly in the shadows, while others emit spores that tickle your beak.",
            parentLocationId = "forest"
        ))
        
        registerLocation(Location(
            id = "forest_babbling_brook",
            name = "Babbling Brook",
            description = "A crystal-clear stream that chatters over smooth pebbles. Minnows dart beneath the surface, and water striders dance atop it.",
            parentLocationId = "forest"
        ))
        
        registerLocation(Location(
            id = "forest_fallen_oak",
            name = "The Fallen Oak",
            description = "A massive trunk that crashed long ago, now covered in moss and fungi. Its hollow interior provides shelter for countless creatures.",
            parentLocationId = "forest"
        ))
        
        registerLocation(Location(
            id = "forest_canopy_heights",
            name = "Canopy Heights",
            description = "The upper reaches of the forest, accessible only by brave climbers. Sunlight filters through leaves, and the view stretches for miles.",
            parentLocationId = "forest"
        ))
        
        registerLocation(Location(
            id = "forest_spider_webs",
            name = "The Spider Webs",
            description = "Massive webs stretched between trees, glistening with morning dew. Each strand is thick as your talon, and the spinner is nowhere to be seen.",
            parentLocationId = "forest"
        ))
        
        registerLocation(Location(
            id = "forest_fern_tunnel",
            name = "Fern Tunnel",
            description = "A natural corridor formed by arching ferns. It's cool and damp here, with earthworms occasionally poking through the soil.",
            parentLocationId = "forest"
        ))
        
        registerLocation(Location(
            id = "forest_woodpecker_tree",
            name = "Woodpecker's Tree",
            description = "A dead tree riddled with holes, each one a potential treasure trove of grubs and beetles. The woodpecker who made them is very territorial.",
            parentLocationId = "forest"
        ))
        
        // Beach locations - coastal exploration
        // Beach locations - coastal exploration
        registerLocation(Location(
            id = "beach",
            name = "The Beach",
            description = "Sandy shores where the water meets the land."
        ))
        
        registerLocation(Location(
            id = "beach_tide_pools",
            name = "Tide Pools",
            description = "Shallow pools left by the receding tide, teeming with tiny crabs, anemones, and hermit crabs. Each pool is a miniature world of wonder.",
            parentLocationId = "beach"
        ))
        
        registerLocation(Location(
            id = "beach_driftwood_maze",
            name = "Driftwood Maze",
            description = "Bleached logs and branches tangled together, creating a labyrinth for small creatures. The wood is smooth and sun-warmed.",
            parentLocationId = "beach"
        ))
        
        registerLocation(Location(
            id = "beach_seashell_grotto",
            name = "Seashell Grotto",
            description = "A small cave adorned with countless shells. The echo of waves creates a soothing rhythm, and the scent of salt is overwhelming.",
            parentLocationId = "beach"
        ))
        
        registerLocation(Location(
            id = "beach_sandpiper_nests",
            name = "Sandpiper Nesting Grounds",
            description = "Shallow depressions in the sand where sandpipers lay their eggs. The birds are protective but can be reasoned with.",
            parentLocationId = "beach"
        ))
        
        registerLocation(Location(
            id = "beach_fishing_pier",
            name = "The Old Fishing Pier",
            description = "Weathered wooden planks stretching over the water. Humans once fished here, but now it's claimed by gulls and brave quails.",
            parentLocationId = "beach"
        ))
        
        registerLocation(Location(
            id = "beach_kelp_forest",
            name = "Kelp Forest Edge",
            description = "Where the ocean meets the shore, thick kelp sways in the shallows. Fish hide among the fronds, and sea otters float on their backs.",
            parentLocationId = "beach"
        ))
        
        registerLocation(Location(
            id = "beach_sand_dunes",
            name = "Sand Dunes",
            description = "Rolling hills of fine sand, constantly reshaped by the wind. Beach grass provides handholds for climbers, and lizards bask on warm slopes.",
            parentLocationId = "beach"
        ))
        
        registerLocation(Location(
            id = "beach_shipwreck",
            name = "The Shipwreck",
            description = "An ancient vessel half-buried in sand, its hull breached and rotting. Inside, treasures and dangers await equally.",
            parentLocationId = "beach"
        ))
        
        registerLocation(Location(
            id = "beach_lighthouse_base",
            name = "Lighthouse Base",
            description = "The foundation of a towering lighthouse. Stairs spiral upward into darkness, and the light above rotates endlessly.",
            parentLocationId = "beach"
        ))
        
        // Swamp locations - murky wetlands
        registerLocation(Location(
            id = "swamp",
            name = "The Swamp",
            description = "A murky wetland with strange plants and creatures."
        ))
        
        registerLocation(Location(
            id = "swamp_murky_pools",
            name = "Murky Pools",
            description = "Stagnant water covered in green algae. Frogs croak from lily pads, and the occasional bubble hints at something moving below.",
            parentLocationId = "swamp"
        ))
        
        registerLocation(Location(
            id = "swamp_cypress_knees",
            name = "Cypress Knees",
            description = "Knobbly tree roots jutting from the water like wooden stalagmites. They provide safe perches above the dangerous swamp water.",
            parentLocationId = "swamp"
        ))
        
        registerLocation(Location(
            id = "swamp_firefly_hollow",
            name = "Firefly Hollow",
            description = "A magical glade where thousands of fireflies dance at dusk. Their synchronized glow illuminates strange flowers and floating seeds.",
            parentLocationId = "swamp"
        ))
        
        registerLocation(Location(
            id = "swamp_quickmud_flats",
            name = "Quickmud Flats",
            description = "Treacherous patches of mud that can swallow a quail whole. Wooden planks and stones provide safe passage for those who know the route.",
            parentLocationId = "swamp"
        ))
        
        registerLocation(Location(
            id = "swamp_venus_garden",
            name = "Venus Flytrap Garden",
            description = "A cluster of carnivorous plants, each one large enough to snap at a quail. They pulse with a hypnotic rhythm, luring insects to their doom.",
            parentLocationId = "swamp"
        ))
        
        registerLocation(Location(
            id = "swamp_gator_den",
            name = "Gator's Den",
            description = "A half-submerged cave where a massive alligator sleeps. Its snores create ripples across the water, and its treasure hoard glitters in the depths.",
            parentLocationId = "swamp"
        ))
        
        registerLocation(Location(
            id = "swamp_mangrove_roots",
            name = "Mangrove Maze",
            description = "Tangled roots form a complex network above and below water. Fish dart between roots, and crabs scuttle sideways through the shadows.",
            parentLocationId = "swamp"
        ))
        
        registerLocation(Location(
            id = "swamp_witch_hut",
            name = "The Witch's Hut",
            description = "A ramshackle dwelling on stilts, surrounded by bizarre plants and bubbling cauldrons. Strange lights flicker in the windows at night.",
            parentLocationId = "swamp"
        ))
        
        registerLocation(Location(
            id = "swamp_poison_mist_valley",
            name = "Poison Mist Valley",
            description = "A low-lying area perpetually shrouded in toxic fog. Only those with proper protection dare enter, seeking rare ingredients.",
            parentLocationId = "swamp"
        ))
        
        // Ruins locations - ancient mysteries
        // Ruins locations - ancient mysteries
        registerLocation(Location(
            id = "ruins",
            name = "The Ruins",
            description = "Ancient structures from a forgotten time."
        ))
        
        registerLocation(Location(
            id = "ruins_crumbling_walls",
            name = "Crumbling Walls",
            description = "Stone walls covered in creeping vines and moss. Cracks reveal hidden alcoves where creatures nest and treasures hide.",
            parentLocationId = "ruins"
        ))
        
        registerLocation(Location(
            id = "ruins_forgotten_library",
            name = "Forgotten Library",
            description = "Shelves of rotting books, their pages yellowed and brittle. Some still hold legible text - secrets of the old world written in mysterious script.",
            parentLocationId = "ruins"
        ))
        
        registerLocation(Location(
            id = "ruins_statue_garden",
            name = "Statue Garden",
            description = "Weathered stone figures stand eternal vigil. Some depict humans, others show creatures never seen before. Vines embrace them like old friends.",
            parentLocationId = "ruins"
        ))
        
        registerLocation(Location(
            id = "ruins_underground_chamber",
            name = "Underground Chamber",
            description = "A dark vault accessible through a crack in the floor. Ancient mechanisms still tick and whir, and glowing crystals provide eerie light.",
            parentLocationId = "ruins"
        ))
        
        registerLocation(Location(
            id = "ruins_collapsed_tower",
            name = "Collapsed Tower",
            description = "What was once tall is now a pile of rubble. Climbing it offers a view of the surrounding area, but the stones shift treacherously.",
            parentLocationId = "ruins"
        ))
        
        registerLocation(Location(
            id = "ruins_sacred_altar",
            name = "Sacred Altar",
            description = "A stone platform carved with mystical symbols. The air here feels charged with ancient magic, and offerings left by travelers dot its surface.",
            parentLocationId = "ruins"
        ))
        
        registerLocation(Location(
            id = "ruins_mosaic_hall",
            name = "Mosaic Hall",
            description = "Colorful tiles cover every surface, depicting scenes of a civilization long gone. Some tiles are missing, creating gaps in the story.",
            parentLocationId = "ruins"
        ))
        
        registerLocation(Location(
            id = "ruins_echo_chamber",
            name = "Echo Chamber",
            description = "A circular room with perfect acoustics. A single chirp becomes a symphony, and whispered secrets reverberate for minutes.",
            parentLocationId = "ruins"
        ))
        
        registerLocation(Location(
            id = "ruins_treasury_vault",
            name = "Treasury Vault",
            description = "A reinforced room once filled with treasures. The door hangs ajar, revealing scattered coins and curious artifacts.",
            parentLocationId = "ruins"
        ))
        
        // Mountain locations - high altitude challenges
        // Mountain locations - high altitude challenges
        registerLocation(Location(
            id = "mountains",
            name = "The Mountains",
            description = "Towering peaks that challenge even the bravest quail."
        ))
        
        registerLocation(Location(
            id = "mountains_rocky_slopes",
            name = "Rocky Slopes",
            description = "Steep inclines covered in loose pebbles and boulders. Each step requires careful balance, and the view grows more spectacular with height.",
            parentLocationId = "mountains"
        ))
        
        registerLocation(Location(
            id = "mountains_crystal_caves",
            name = "Crystal Caves",
            description = "Glittering formations jut from walls and ceiling. Light refracts into rainbows, and the crystals hum with a resonant frequency.",
            parentLocationId = "mountains"
        ))
        
        registerLocation(Location(
            id = "mountains_eagles_aerie",
            name = "Eagle's Aerie",
            description = "A massive nest perched on the highest peak. Bones and treasures are scattered among the twigs, and the eagle returns at sunset.",
            parentLocationId = "mountains"
        ))
        
        registerLocation(Location(
            id = "mountains_frozen_falls",
            name = "Frozen Falls",
            description = "A waterfall locked in ice, creating a cascading sculpture of frost. The ice creaks and groans, and meltwater pools beneath.",
            parentLocationId = "mountains"
        ))
        
        registerLocation(Location(
            id = "mountains_hot_springs",
            name = "Mountain Hot Springs",
            description = "Steaming pools warmed by volcanic heat below. The water is soothing for tired talons, and rare minerals color the edges.",
            parentLocationId = "mountains"
        ))
        
        registerLocation(Location(
            id = "mountains_wind_tunnel",
            name = "Wind Tunnel Pass",
            description = "A narrow gorge where wind howls constantly. It can blow a quail off course or provide lift for brave gliders.",
            parentLocationId = "mountains"
        ))
        
        registerLocation(Location(
            id = "mountains_hermit_cave",
            name = "Hermit's Cave",
            description = "A small dwelling carved into the mountainside. An old sage quail lives here, surrounded by strange devices and scrolls.",
            parentLocationId = "mountains"
        ))
        
        registerLocation(Location(
            id = "mountains_cliff_face",
            name = "Sheer Cliff Face",
            description = "A vertical wall of stone with tiny cracks and ledges. Only master climbers attempt this route, but the treasures above are legendary.",
            parentLocationId = "mountains"
        ))
        
        registerLocation(Location(
            id = "mountains_summit",
            name = "The Summit",
            description = "The highest point in the land. From here, you can see everything - Buttonburgh, the forests, the ocean. The air is thin but exhilarating.",
            parentLocationId = "mountains"
        ))
        
        registerLocation(Location(
            id = "mountains_avalanche_zone",
            name = "Avalanche Zone",
            description = "A dangerous slope where snow and rocks can tumble without warning. Signs of past slides scar the mountainside.",
            parentLocationId = "mountains"
        ))
        
        // Special nodes (for harvestable/interactive objects)
        registerLocation(Location(
            id = "node_giga_seed_plant",
            name = "Ancient, Glowing Plant",
            description = "A mysterious plant that hums with intellectual energy.",
            parentLocationId = "forest"
        ))
        
        registerLocation(Location(
            id = "node_moondew_fern",
            name = "Moondew Fern Patch",
            description = "Silvery ferns that only appear under moonlight.",
            parentLocationId = "forest"
        ))
        
        registerLocation(Location(
            id = "node_hard_soil_pile",
            name = "Hard Soil Pile",
            description = "Compacted earth too hard for normal talons.",
            parentLocationId = "mountains"
        ))
        
        registerLocation(Location(
            id = "node_heavy_pebble",
            name = "Heavy Pebble",
            description = "A large stone with something glittering beneath.",
            parentLocationId = "mountains"
        ))
    }
}
