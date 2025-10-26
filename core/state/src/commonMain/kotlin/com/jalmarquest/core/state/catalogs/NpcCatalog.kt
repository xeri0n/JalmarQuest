package com.jalmarquest.core.state.catalogs

import kotlinx.serialization.Serializable

/**
 * Catalog of all NPCs in the game.
 * Contains NPCs for all quests and locations.
 */

@Serializable
data class Npc(
    val id: String,
    val name: String,
    val locationId: String,
    val dialogueStart: String? = null,
    val dialogueEnd: String? = null,
    val dialogueHint: String? = null,
    val questIds: List<String> = emptyList()
)

class NpcCatalog {
    private val npcs = mutableMapOf<String, Npc>()
    
    init {
        registerDefaultNpcs()
    }
    
    fun registerNpc(npc: Npc) {
        npcs[npc.id] = npc
    }
    
    fun getNpcById(id: String): Npc? = npcs[id]
    
    fun getNpcsByLocation(locationId: String): List<Npc> {
        return npcs.values.filter { it.locationId == locationId }
    }
    
    fun getAllNpcs(): List<Npc> = npcs.values.toList()
    
    private fun registerDefaultNpcs() {
        // Existing NPCs
        registerNpc(Npc(
            id = "npc_elder_quill",
            name = "Elder Quill",
            locationId = "buttonburgh_quills_study",
            dialogueStart = "Welcome, young one. Your journey begins here."
        ))
        
        registerNpc(Npc(
            id = "npc_ignatius",
            name = "Ignatius",
            locationId = "buttonburgh_quills_study",
            dialogueStart = "Alchemy is the art of transformation, young quail."
        ))
        
        registerNpc(Npc(
            id = "npc_cornelius",
            name = "Cornelius the Curator",
            locationId = "buttonburgh_hub",
            dialogueStart = "A fine collection you're building! Every shiny tells a story."
        ))
        
        registerNpc(Npc(
            id = "npc_nest_quartermaster",
            name = "Nest Quartermaster",
            locationId = "buttonburgh_hub",
            dialogueStart = "The Nest always needs more resources. Can you help?"
        ))
        
        // Quest 1: The Giga-Seed
        registerNpc(Npc(
            id = "npc_professor_tessel",
            name = "Professor Tessel",
            locationId = "buttonburgh_quills_study",
            dialogueStart = "Fascinating... the legends of the Giga-Seed. A seed of pure thought! If only I could find one...",
            questIds = listOf("quest_giga_seed")
        ))
        
        // Quest 2: The High Perch
        registerNpc(Npc(
            id = "npc_artist_pip",
            name = "Artist Pip",
            locationId = "buttonburgh_hub",
            dialogueStart = "Ah, *le chirp*! My masterpiece is missing its final touch... a Sunpetal Flower! But it grows so very high...",
            questIds = listOf("quest_high_perch")
        ))
        
        // Quest 3: The Night Forager
        registerNpc(Npc(
            id = "npc_herbalist_hoot",
            name = "Herbalist Hoot",
            locationId = "buttonburgh_hub",
            dialogueStart = "My old eyes aren't what they were... I need a Moondew Fern. They only show themselves at night, in the deep woods.",
            questIds = listOf("quest_night_forager", "quest_fading_elder", "quest_poisoned_grove")
        ))
        
        // Quest 4: The Beetle Brouhaha
        registerNpc(Npc(
            id = "npc_professor_click",
            name = "Professor Click",
            locationId = "buttonburgh_quills_study",
            dialogueStart = "Beetles! The jewels of the undergrowth! I must have the 'Big Five'! Bring me an Azure, Emerald, Ruby, Obsidian, and Opal beetle!",
            questIds = listOf("quest_beetle_brouhaha")
        ))
        
        // Quest 5: A Soothing Silence
        registerNpc(Npc(
            id = "npc_elder_bristle",
            name = "Elder Bristle",
            locationId = "buttonburgh_quills_study",
            dialogueStart = "Racket! Clanking! A quail can't hear himself think! My dust bath is ruined!",
            dialogueEnd = "Ah... silence. Soothing. Thank you, young'un.",
            questIds = listOf("quest_soothing_silence", "quest_fading_elder")
        ))
        
        registerNpc(Npc(
            id = "npc_tink",
            name = "Tink",
            locationId = "buttonburgh_dust_bath",
            dialogueStart = "What? Can't hear you! My Beetle-Harvester 5000 is a bit loud, I know! The muffler cog broke. If I had a new one...",
            questIds = listOf("quest_soothing_silence")
        ))
        
        // Quest 6: The Lost Clutch
        registerNpc(Npc(
            id = "npc_frantic_flora",
            name = "Frantic Flora",
            locationId = "buttonburgh_hub",
            dialogueStart = "Oh, chirp-chirp-chirp! My eggs! Gone! A shadow passed, I flew, and now... they're gone! All 6 of them! Can you help me? Please!",
            dialogueEnd = "You found them! You're a lifesaver! Thank you, thank you!",
            questIds = listOf("quest_lost_clutch")
        ))
        
        // Quest 7: The Coziest Nest
        registerNpc(Npc(
            id = "npc_old_man_thistle",
            name = "Old Man Thistle",
            locationId = "buttonburgh_hub",
            dialogueStart = "Brrr... chirp... so cold. My old nest is just sticks and dust. If only I had a proper lining...",
            questIds = listOf("quest_coziest_nest")
        ))
        
        // Quest 8: Practical Plumage
        registerNpc(Npc(
            id = "npc_scout_whisper",
            name = "Scout Whisper",
            locationId = "forest_entrance",
            dialogueStart = "Chirp! Too bright! My plumage... that hawk sees me every time! I need something... more practical.",
            questIds = listOf("quest_practical_plumage")
        ))
        
        // Quest 9: The Digger's Delight
        registerNpc(Npc(
            id = "npc_digger",
            name = "Digger",
            locationId = "mountains",
            dialogueHint = "Hah! That soil? You'll break a nail. You need metal, friend. Take this recipe, see if you can make it.",
            questIds = listOf("quest_diggers_delight", "quest_territorial_crow")
        ))
        
        // Quest 10: The Antbassador
        registerNpc(Npc(
            id = "npc_worried_wicker",
            name = "Worried Wicker",
            locationId = "buttonburgh_shop",
            dialogueStart = "The ants! They're... organized! Taking my seeds!",
            questIds = listOf("quest_antbassador", "quest_barters_challenge")
        ))
        
        registerNpc(Npc(
            id = "npc_ant_queen",
            name = "The Ant Queen",
            locationId = "ant_hill",
            dialogueStart = "We are... many. We... need. Feed. The. Colony.",
            questIds = listOf("quest_antbassador")
        ))
        
        // Quest 11: The Stone-Stuck Seed
        registerNpc(Npc(
            id = "npc_quaylsmith",
            name = "The Quailsmith",
            locationId = "buttonburgh_alchemy_lab",
            dialogueStart = "Potions and concoctions! What mysteries shall we unlock today?",
            questIds = listOf("quest_stone_stuck_seed", "quest_chameleon_challenge")
        ))
        
        // Quest 13: The Chameleon's Challenge
        registerNpc(Npc(
            id = "npc_pack_rat",
            name = "Pack Rat",
            locationId = "buttonburgh_hoard_vault",
            dialogueStart = "Beetles! Magpies! My precious lens!",
            questIds = listOf("quest_chameleon_challenge", "quest_hoarders_exam")
        ))
        
        // Quest 16: The Silent Scholar
        registerNpc(Npc(
            id = "npc_quill",
            name = "Quill",
            locationId = "buttonburgh_quills_study",
            dialogueStart = "[Quill is staring at a book, unblinking. He doesn't respond to your chirps.]",
            questIds = listOf("quest_silent_scholar")
        ))
        
        // Quest 18: The Territorial Crow
        registerNpc(Npc(
            id = "npc_bullied_barry",
            name = "Bullied Barry",
            locationId = "forest_entrance",
            dialogueStart = "That big crow... took my favorite berry patch!",
            questIds = listOf("quest_territorial_crow")
        ))
        
        // Quest 20: The Feathered Friend
        registerNpc(Npc(
            id = "npc_chickadee",
            name = "Chickadee",
            locationId = "companion_slot",
            dialogueStart = "Chirp! Chirp! Lost... cold...",
            questIds = listOf("quest_feathered_friend")
        ))
        
        registerNpc(Npc(
            id = "npc_matron_nester",
            name = "Matron Nester",
            locationId = "buttonburgh_orphanage",
            dialogueStart = "Oh, you found another one! Bless you. This little one seems to have bonded with you. Perhaps you can show him the ropes?",
            questIds = listOf("quest_feathered_friend")
        ))
        
        // Buttonburgh Town NPCs - merchants, citizens, and characters
        registerNpc(Npc(
            id = "npc_clara_seedsworth",
            name = "Clara Seedsworth",
            locationId = "buttonburgh_market_square",
            dialogueStart = "Fresh seeds! Best prices in Buttonburgh! Looking for something special?"
        ))
        
        registerNpc(Npc(
            id = "npc_bertram_the_broker",
            name = "Bertram the Broker",
            locationId = "buttonburgh_market_square",
            dialogueStart = "Trading goods and information. What brings you to my stall today?"
        ))
        
        registerNpc(Npc(
            id = "npc_penny_featherlight",
            name = "Penny Featherlight",
            locationId = "buttonburgh_roost_apartments",
            dialogueStart = "Welcome to The Roost! Looking for a room, or just passing through?"
        ))
        
        registerNpc(Npc(
            id = "npc_old_coop",
            name = "Old Coop",
            locationId = "buttonburgh_roost_apartments",
            dialogueStart = "Been living here since the old days. Seen a lot of changes, yes I have."
        ))
        
        registerNpc(Npc(
            id = "npc_professor_beakman",
            name = "Professor Beakman",
            locationId = "buttonburgh_scholars_district",
            dialogueStart = "Ah, a fellow seeker of knowledge! The mysteries of the world await study."
        ))
        
        registerNpc(Npc(
            id = "npc_librarian_hush",
            name = "Librarian Hush",
            locationId = "buttonburgh_scholars_district",
            dialogueStart = "Shhhh... welcome to the archives. Speak softly and the books will share their secrets."
        ))
        
        registerNpc(Npc(
            id = "npc_tinker_cogsworth",
            name = "Tinker Cogsworth",
            locationId = "buttonburgh_artisan_quarter",
            dialogueStart = "Gears and springs, that's what I work with! Need something fixed or built?"
        ))
        
        registerNpc(Npc(
            id = "npc_seamstress_plume",
            name = "Seamstress Plume",
            locationId = "buttonburgh_artisan_quarter",
            dialogueStart = "Feathers and fabrics! I can tailor anything you need, dearie."
        ))
        
        registerNpc(Npc(
            id = "npc_mason_rockbeak",
            name = "Mason Rockbeak",
            locationId = "buttonburgh_artisan_quarter",
            dialogueStart = "Stone and mortar - building things that last! What can I craft for you?"
        ))
        
        registerNpc(Npc(
            id = "npc_sergeant_talon",
            name = "Sergeant Talon",
            locationId = "buttonburgh_training_grounds",
            dialogueStart = "Stand up straight! If you want to survive out there, you'll need proper training!"
        ))
        
        registerNpc(Npc(
            id = "npc_young_flutter",
            name = "Young Flutter",
            locationId = "buttonburgh_training_grounds",
            dialogueStart = "I'm practicing my pecking! Watch this... *misses the target* ...almost had it!"
        ))
        
        registerNpc(Npc(
            id = "npc_gardener_bloom",
            name = "Gardener Bloom",
            locationId = "buttonburgh_garden_terraces",
            dialogueStart = "These plants are my pride and joy. Each one needs special care and attention."
        ))
        
        registerNpc(Npc(
            id = "npc_herbalist_sage",
            name = "Herbalist Sage",
            locationId = "buttonburgh_garden_terraces",
            dialogueStart = "Every herb has a purpose, every flower a healing property. Nature provides all we need."
        ))
        
        registerNpc(Npc(
            id = "npc_town_crier",
            name = "Town Crier Chirrup",
            locationId = "buttonburgh_message_post",
            dialogueStart = "Hear ye, hear ye! All the latest news and notices! Come check the board!"
        ))
        
        registerNpc(Npc(
            id = "npc_courier_swift",
            name = "Courier Swift",
            locationId = "buttonburgh_message_post",
            dialogueStart = "Got a message to deliver? I'm the fastest runner in Buttonburgh!"
        ))
        
        registerNpc(Npc(
            id = "npc_teacher_wisdom",
            name = "Teacher Wisdom",
            locationId = "buttonburgh_hatchling_nursery",
            dialogueStart = "Every chick has potential. We nurture their minds and hearts here."
        ))
        
        registerNpc(Npc(
            id = "npc_tiny_pip",
            name = "Tiny Pip",
            locationId = "buttonburgh_hatchling_nursery",
            dialogueStart = "Chirp chirp! *waddles over* Are you here to play?"
        ))
        
        registerNpc(Npc(
            id = "npc_councilor_gravitas",
            name = "Councilor Gravitas",
            locationId = "buttonburgh_town_hall",
            dialogueStart = "The council takes the needs of all Buttonburgh citizens seriously. What brings you here today?"
        ))
        
        registerNpc(Npc(
            id = "npc_clerk_quillton",
            name = "Clerk Quillton",
            locationId = "buttonburgh_town_hall",
            dialogueStart = "Forms in triplicate, please. Everything must be properly documented!"
        ))
        
        registerNpc(Npc(
            id = "npc_barkeep_dusty",
            name = "Barkeep Dusty",
            locationId = "buttonburgh_tavern",
            dialogueStart = "Welcome to The Dusty Talon! Pull up a perch and I'll get you something to drink!"
        ))
        
        registerNpc(Npc(
            id = "npc_wandering_minstrel",
            name = "Wandering Minstrel",
            locationId = "buttonburgh_tavern",
            dialogueStart = "*strums tiny lute* Got a song request? Or perhaps you have a story worth singing about?"
        ))
        
        // Forest NPCs - wild dwellers and hermits
        registerNpc(Npc(
            id = "npc_forest_ranger",
            name = "Ranger Greenfeather",
            locationId = "forest_entrance",
            dialogueStart = "Stay on the paths and you'll be safe. Wander off, and... well, the forest has its own rules."
        ))
        
        registerNpc(Npc(
            id = "npc_mushroom_sage",
            name = "Mushroom Sage",
            locationId = "forest_mushroom_grove",
            dialogueStart = "The fungi speak to those who listen. Each spore carries ancient wisdom..."
        ))
        
        registerNpc(Npc(
            id = "npc_brook_fisher",
            name = "Fisher Ripple",
            locationId = "forest_babbling_brook",
            dialogueStart = "Fishing's all about patience. The stream provides, but only to those who respect it."
        ))
        
        registerNpc(Npc(
            id = "npc_hollow_dweller",
            name = "The Hollow Dweller",
            locationId = "forest_fallen_oak",
            dialogueStart = "This old oak is my home. I share it with beetles, mice, and the occasional lost quail."
        ))
        
        registerNpc(Npc(
            id = "npc_canopy_scout",
            name = "Scout Skyview",
            locationId = "forest_canopy_heights",
            dialogueStart = "From up here, you can see everything! The forest reveals its secrets to climbers."
        ))
        
        registerNpc(Npc(
            id = "npc_web_weaver",
            name = "Arachna the Friendly Spider",
            locationId = "forest_spider_webs",
            dialogueStart = "*friendly wave of multiple legs* Don't worry, I only eat bugs! Care to chat?"
        ))
        
        // Beach NPCs - coastal characters
        registerNpc(Npc(
            id = "npc_tide_watcher",
            name = "Tide Watcher",
            locationId = "beach_tide_pools",
            dialogueStart = "The tides reveal treasures twice daily. Timing is everything on the beach."
        ))
        
        registerNpc(Npc(
            id = "npc_shell_collector",
            name = "Shell Collector Sandy",
            locationId = "beach_seashell_grotto",
            dialogueStart = "Each shell tells a story of the creature that lived within. I collect them all!"
        ))
        
        registerNpc(Npc(
            id = "npc_sandpiper_chief",
            name = "Chief Sandpiper",
            locationId = "beach_sandpiper_nests",
            dialogueStart = "Our nests are sacred. Approach with respect, and we can be allies."
        ))
        
        registerNpc(Npc(
            id = "npc_old_sailor",
            name = "Old Sailor Barnacle",
            locationId = "beach_fishing_pier",
            dialogueStart = "Arr, I've seen many things from this pier. The sea keeps its secrets, but I keep mine too."
        ))
        
        registerNpc(Npc(
            id = "npc_lighthouse_keeper",
            name = "Keeper Beacon",
            locationId = "beach_lighthouse_base",
            dialogueStart = "The light must never go out. It guides travelers home through the darkest nights."
        ))
        
        // Swamp NPCs - mysterious inhabitants
        registerNpc(Npc(
            id = "npc_swamp_hermit",
            name = "Hermit Mudfoot",
            locationId = "swamp_cypress_knees",
            dialogueStart = "City folk don't last long in the swamp. But you seem different... capable."
        ))
        
        registerNpc(Npc(
            id = "npc_firefly_queen",
            name = "Firefly Queen",
            locationId = "swamp_firefly_hollow",
            dialogueStart = "*glows rhythmically* Our dance is eternal. Join us, and you'll understand the swamp's heartbeat."
        ))
        
        registerNpc(Npc(
            id = "npc_bog_witch",
            name = "Bog Witch Murkmire",
            locationId = "swamp_witch_hut",
            dialogueStart = "Hehehe... a visitor! Come for a potion? A curse? Or just lost, little quail?"
        ))
        
        // Mountain NPCs - high altitude dwellers
        registerNpc(Npc(
            id = "npc_mountain_guide",
            name = "Guide Stonewing",
            locationId = "mountains_rocky_slopes",
            dialogueStart = "These peaks are unforgiving. Follow my lead and you'll reach the summit alive."
        ))
        
        registerNpc(Npc(
            id = "npc_crystal_mystic",
            name = "Crystal Mystic",
            locationId = "mountains_crystal_caves",
            dialogueStart = "The crystals sing of past and future. Listen carefully, and they'll guide your path."
        ))
        
        registerNpc(Npc(
            id = "npc_mountain_hermit",
            name = "Hermit Peakwise",
            locationId = "mountains_hermit_cave",
            dialogueStart = "I left civilization behind decades ago. Up here, only the mountain and I matter."
        ))
        
        // Ruins NPCs - scholars and explorers
        registerNpc(Npc(
            id = "npc_archaeologist",
            name = "Archaeologist Dustwing",
            locationId = "ruins_crumbling_walls",
            dialogueStart = "These ruins hold secrets of the ancients! Every artifact is a piece of the puzzle."
        ))
        
        registerNpc(Npc(
            id = "npc_ghost_scribe",
            name = "The Ghost Scribe",
            locationId = "ruins_forgotten_library",
            dialogueStart = "*translucent shimmer* I remain to preserve the knowledge... eternal guardian of forgotten words..."
        ))
        
        registerNpc(Npc(
            id = "npc_treasure_hunter",
            name = "Treasure Hunter Goldbeak",
            locationId = "ruins_treasury_vault",
            dialogueStart = "There's gold in these ruins, I know it! Just need to find the right door..."
        ))
    }
}
