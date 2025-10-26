package com.jalmarquest.core.state.catalogs

import kotlinx.serialization.Serializable

/**
 * Catalog of all enemy types in the game.
 * Contains stats, behaviors, drops, and habitat information.
 */

@Serializable
data class Enemy(
    val id: String,
    val name: String,
    val description: String,
    val level: Int,
    val health: Int,
    val attack: Int,
    val defense: Int,
    val speed: Int,
    val habitat: List<String>, // Location IDs where this enemy can be found
    val behavior: EnemyBehavior,
    val lootTable: List<LootDrop>,
    val experienceReward: Int,
    val isAggressive: Boolean = true,
    val isBoss: Boolean = false
)

@Serializable
enum class EnemyBehavior {
    PASSIVE,      // Won't attack unless provoked
    TERRITORIAL,  // Attacks when player enters territory
    AGGRESSIVE,   // Always attacks on sight
    PATROL,       // Moves along set path, attacks when spotted
    AMBUSH,       // Hides and attacks when player gets close
    FLEE,         // Runs away when spotted
    DEFENSIVE     // Only defends its nest/treasure
}

@Serializable
data class LootDrop(
    val itemId: String,
    val dropChance: Float,  // 0.0 to 1.0
    val minQuantity: Int = 1,
    val maxQuantity: Int = 1
)

class EnemyCatalog {
    private val enemies = mutableMapOf<String, Enemy>()
    
    init {
        registerDefaultEnemies()
    }
    
    fun registerEnemy(enemy: Enemy) {
        enemies[enemy.id] = enemy
    }
    
    fun getEnemyById(id: String): Enemy? = enemies[id]
    
    fun getEnemiesByHabitat(locationId: String): List<Enemy> {
        return enemies.values.filter { it.habitat.contains(locationId) }
    }
    
    fun getAllEnemies(): List<Enemy> = enemies.values.toList()
    
    fun getBosses(): List<Enemy> = enemies.values.filter { it.isBoss }
    
    private fun registerDefaultEnemies() {
        // Harmless critters (Level 1-2)
        registerEnemy(Enemy(
            id = "enemy_ladybug",
            name = "Ladybug",
            description = "A small spotted beetle. Harmless and quite friendly.",
            level = 1,
            health = 5,
            attack = 1,
            defense = 2,
            speed = 3,
            habitat = listOf("buttonburgh_garden_terraces", "forest", "forest_mushroom_grove"),
            behavior = EnemyBehavior.PASSIVE,
            lootTable = listOf(
                LootDrop("item_beetle_shell_fragment", 0.3f)
            ),
            experienceReward = 5,
            isAggressive = false
        ))
        
        registerEnemy(Enemy(
            id = "enemy_pill_bug",
            name = "Pill Bug",
            description = "A small armored crustacean that rolls into a ball when threatened.",
            level = 1,
            health = 8,
            attack = 1,
            defense = 5,
            speed = 1,
            habitat = listOf("forest", "forest_fallen_oak", "swamp"),
            behavior = EnemyBehavior.DEFENSIVE,
            lootTable = listOf(
                LootDrop("item_chitin_plate", 0.4f)
            ),
            experienceReward = 5,
            isAggressive = false
        ))
        
        registerEnemy(Enemy(
            id = "enemy_earthworm",
            name = "Earthworm",
            description = "A wriggly worm that burrows through the soil. A favorite snack of many birds.",
            level = 1,
            health = 3,
            attack = 0,
            defense = 0,
            speed = 2,
            habitat = listOf("buttonburgh_garden_terraces", "forest", "forest_fern_tunnel"),
            behavior = EnemyBehavior.FLEE,
            lootTable = listOf(
                LootDrop("item_worm_protein", 0.8f)
            ),
            experienceReward = 3,
            isAggressive = false
        ))
        
        // Forest enemies (Level 2-5)
        registerEnemy(Enemy(
            id = "enemy_angry_ant",
            name = "Angry Ant",
            description = "A soldier ant defending its colony. Surprisingly strong for its size.",
            level = 2,
            health = 10,
            attack = 4,
            defense = 3,
            speed = 5,
            habitat = listOf("ant_hill", "forest"),
            behavior = EnemyBehavior.TERRITORIAL,
            lootTable = listOf(
                LootDrop("item_ant_mandible", 0.5f),
                LootDrop("item_formic_acid", 0.3f)
            ),
            experienceReward = 15
        ))
        
        registerEnemy(Enemy(
            id = "enemy_stag_beetle",
            name = "Stag Beetle",
            description = "An impressive beetle with large mandibles. Territorial but not overly aggressive.",
            level = 3,
            health = 20,
            attack = 6,
            defense = 8,
            speed = 3,
            habitat = listOf("forest", "forest_fallen_oak", "forest_woodpecker_tree"),
            behavior = EnemyBehavior.TERRITORIAL,
            lootTable = listOf(
                LootDrop("item_stag_horn", 0.6f),
                LootDrop("item_beetle_carapace", 0.4f)
            ),
            experienceReward = 25
        ))
        
        registerEnemy(Enemy(
            id = "enemy_jumping_spider",
            name = "Jumping Spider",
            description = "A fuzzy spider with excellent vision. Can leap surprising distances.",
            level = 3,
            health = 15,
            attack = 8,
            defense = 4,
            speed = 7,
            habitat = listOf("forest", "forest_spider_webs", "forest_canopy_heights"),
            behavior = EnemyBehavior.AMBUSH,
            lootTable = listOf(
                LootDrop("item_spider_silk", 0.7f),
                LootDrop("item_spider_fang", 0.3f)
            ),
            experienceReward = 30
        ))
        
        registerEnemy(Enemy(
            id = "enemy_poison_frog",
            name = "Poison Dart Frog",
            description = "Bright colors warn of deadly toxins. Best avoided or approached with caution.",
            level = 4,
            health = 18,
            attack = 10,
            defense = 5,
            speed = 6,
            habitat = listOf("swamp", "swamp_murky_pools", "forest_babbling_brook"),
            behavior = EnemyBehavior.TERRITORIAL,
            lootTable = listOf(
                LootDrop("item_poison_gland", 0.6f),
                LootDrop("item_bright_skin", 0.4f)
            ),
            experienceReward = 40
        ))
        
        registerEnemy(Enemy(
            id = "enemy_angry_crow",
            name = "Territorial Crow",
            description = "A large black bird that defends its territory viciously. Much bigger than a quail.",
            level = 5,
            health = 35,
            attack = 12,
            defense = 6,
            speed = 8,
            habitat = listOf("crows_perch", "forest"),
            behavior = EnemyBehavior.TERRITORIAL,
            lootTable = listOf(
                LootDrop("item_crow_feather", 0.8f),
                LootDrop("item_berry_seeds", 0.5f),
                LootDrop("item_shiny_trinket", 0.2f)
            ),
            experienceReward = 50
        ))
        
        // Beach enemies (Level 3-6)
        registerEnemy(Enemy(
            id = "enemy_hermit_crab",
            name = "Hermit Crab",
            description = "A crab hiding in a borrowed shell. Will pinch if provoked.",
            level = 3,
            health = 16,
            attack = 7,
            defense = 9,
            speed = 4,
            habitat = listOf("beach_tide_pools", "beach"),
            behavior = EnemyBehavior.DEFENSIVE,
            lootTable = listOf(
                LootDrop("item_crab_claw", 0.5f),
                LootDrop("item_seashell", 0.7f)
            ),
            experienceReward = 25
        ))
        
        registerEnemy(Enemy(
            id = "enemy_seagull",
            name = "Aggressive Seagull",
            description = "A large, noisy bird that will steal food and attack smaller creatures.",
            level = 5,
            health = 30,
            attack = 11,
            defense = 5,
            speed = 9,
            habitat = listOf("beach", "beach_fishing_pier", "beach_driftwood_maze"),
            behavior = EnemyBehavior.AGGRESSIVE,
            lootTable = listOf(
                LootDrop("item_gull_feather", 0.8f),
                LootDrop("item_fish_scrap", 0.4f)
            ),
            experienceReward = 45
        ))
        
        registerEnemy(Enemy(
            id = "enemy_sand_crab",
            name = "Sand Crab",
            description = "A fast-moving crab that burrows in the sand. Difficult to catch.",
            level = 4,
            health = 14,
            attack = 9,
            defense = 7,
            speed = 10,
            habitat = listOf("beach", "beach_sand_dunes"),
            behavior = EnemyBehavior.FLEE,
            lootTable = listOf(
                LootDrop("item_crab_claw", 0.6f),
                LootDrop("item_sand_pearl", 0.1f)
            ),
            experienceReward = 35
        ))
        
        registerEnemy(Enemy(
            id = "enemy_starfish",
            name = "Aggressive Starfish",
            description = "A surprisingly mobile starfish that can regenerate limbs.",
            level = 3,
            health = 20,
            attack = 5,
            defense = 8,
            speed = 2,
            habitat = listOf("beach_tide_pools", "beach_kelp_forest"),
            behavior = EnemyBehavior.PASSIVE,
            lootTable = listOf(
                LootDrop("item_starfish_arm", 0.9f)
            ),
            experienceReward = 20,
            isAggressive = false
        ))
        
        // Swamp enemies (Level 4-8)
        registerEnemy(Enemy(
            id = "enemy_mosquito_swarm",
            name = "Mosquito Swarm",
            description = "A cloud of bloodsucking insects. Individually weak, but overwhelming in numbers.",
            level = 4,
            health = 10,
            attack = 6,
            defense = 2,
            speed = 8,
            habitat = listOf("swamp", "swamp_murky_pools", "swamp_mangrove_roots"),
            behavior = EnemyBehavior.AGGRESSIVE,
            lootTable = listOf(
                LootDrop("item_mosquito_proboscis", 0.4f)
            ),
            experienceReward = 30
        ))
        
        registerEnemy(Enemy(
            id = "enemy_venus_flytrap",
            name = "Giant Venus Flytrap",
            description = "A carnivorous plant that snaps at anything that moves. Stationary but deadly.",
            level = 5,
            health = 40,
            attack = 15,
            defense = 3,
            speed = 0,
            habitat = listOf("swamp_venus_garden", "swamp"),
            behavior = EnemyBehavior.AMBUSH,
            lootTable = listOf(
                LootDrop("item_flytrap_nectar", 0.5f),
                LootDrop("item_plant_pod", 0.3f)
            ),
            experienceReward = 50
        ))
        
        registerEnemy(Enemy(
            id = "enemy_water_snake",
            name = "Water Snake",
            description = "A slithering predator that glides through swamp water. Quick and venomous.",
            level = 6,
            health = 28,
            attack = 13,
            defense = 6,
            speed = 9,
            habitat = listOf("swamp", "swamp_murky_pools", "swamp_mangrove_roots"),
            behavior = EnemyBehavior.AMBUSH,
            lootTable = listOf(
                LootDrop("item_snake_fang", 0.6f),
                LootDrop("item_snake_skin", 0.5f),
                LootDrop("item_venom_sac", 0.3f)
            ),
            experienceReward = 60
        ))
        
        registerEnemy(Enemy(
            id = "enemy_snapping_turtle",
            name = "Snapping Turtle",
            description = "An ancient turtle with a powerful bite. Slow but incredibly dangerous.",
            level = 7,
            health = 50,
            attack = 16,
            defense = 15,
            speed = 2,
            habitat = listOf("swamp", "swamp_murky_pools"),
            behavior = EnemyBehavior.TERRITORIAL,
            lootTable = listOf(
                LootDrop("item_turtle_shell", 0.7f),
                LootDrop("item_turtle_egg", 0.2f)
            ),
            experienceReward = 70
        ))
        
        registerEnemy(Enemy(
            id = "enemy_will_o_wisp",
            name = "Will-o'-Wisp",
            description = "A mysterious floating light that lures travelers astray. Magical in nature.",
            level = 6,
            health = 15,
            attack = 10,
            defense = 20,
            speed = 7,
            habitat = listOf("swamp_firefly_hollow", "swamp", "swamp_poison_mist_valley"),
            behavior = EnemyBehavior.AMBUSH,
            lootTable = listOf(
                LootDrop("item_spectral_essence", 0.8f),
                LootDrop("item_ghost_light", 0.4f)
            ),
            experienceReward = 65
        ))
        
        // Mountain enemies (Level 5-9)
        registerEnemy(Enemy(
            id = "enemy_mountain_goat",
            name = "Wild Mountain Goat",
            description = "A sure-footed herbivore. Normally peaceful, but will charge if threatened.",
            level = 5,
            health = 35,
            attack = 10,
            defense = 8,
            speed = 7,
            habitat = listOf("mountains", "mountains_rocky_slopes", "mountains_cliff_face"),
            behavior = EnemyBehavior.TERRITORIAL,
            lootTable = listOf(
                LootDrop("item_goat_horn", 0.5f),
                LootDrop("item_mountain_wool", 0.6f)
            ),
            experienceReward = 50
        ))
        
        registerEnemy(Enemy(
            id = "enemy_ice_bat",
            name = "Ice Bat",
            description = "A bat that roosts in frozen caves. Its bite carries a freezing touch.",
            level = 6,
            health = 22,
            attack = 12,
            defense = 6,
            speed = 11,
            habitat = listOf("mountains_crystal_caves", "mountains_frozen_falls"),
            behavior = EnemyBehavior.AGGRESSIVE,
            lootTable = listOf(
                LootDrop("item_bat_wing", 0.7f),
                LootDrop("item_frost_essence", 0.4f)
            ),
            experienceReward = 60
        ))
        
        registerEnemy(Enemy(
            id = "enemy_rock_lizard",
            name = "Rock Lizard",
            description = "A camouflaged reptile that blends perfectly with stone. Ambushes prey from above.",
            level = 7,
            health = 30,
            attack = 14,
            defense = 10,
            speed = 6,
            habitat = listOf("mountains", "mountains_rocky_slopes", "mountains_cliff_face"),
            behavior = EnemyBehavior.AMBUSH,
            lootTable = listOf(
                LootDrop("item_lizard_tail", 0.5f),
                LootDrop("item_camouflage_skin", 0.3f)
            ),
            experienceReward = 70
        ))
        
        registerEnemy(Enemy(
            id = "enemy_avalanche_elemental",
            name = "Avalanche Elemental",
            description = "A swirling mass of snow and ice given life. Unpredictable and powerful.",
            level = 8,
            health = 45,
            attack = 18,
            defense = 8,
            speed = 8,
            habitat = listOf("mountains_avalanche_zone", "mountains_frozen_falls"),
            behavior = EnemyBehavior.PATROL,
            lootTable = listOf(
                LootDrop("item_elemental_core", 0.6f),
                LootDrop("item_pure_ice", 0.5f)
            ),
            experienceReward = 85
        ))
        
        registerEnemy(Enemy(
            id = "enemy_mountain_hawk",
            name = "Mountain Hawk",
            description = "A predatory bird with keen eyesight. Quails are its natural prey.",
            level = 8,
            health = 38,
            attack = 20,
            defense = 7,
            speed = 12,
            habitat = listOf("mountains", "mountains_summit", "mountains_cliff_face"),
            behavior = EnemyBehavior.AGGRESSIVE,
            lootTable = listOf(
                LootDrop("item_hawk_feather", 0.8f),
                LootDrop("item_hawk_talon", 0.4f),
                LootDrop("item_keen_eye", 0.2f)
            ),
            experienceReward = 90
        ))
        
        // Ruins enemies (Level 6-10)
        registerEnemy(Enemy(
            id = "enemy_ancient_construct",
            name = "Ancient Construct",
            description = "A mechanical guardian still fulfilling its ancient duty. Rust hasn't slowed it much.",
            level = 7,
            health = 50,
            attack = 15,
            defense = 12,
            speed = 5,
            habitat = listOf("ruins", "ruins_underground_chamber", "ruins_treasury_vault"),
            behavior = EnemyBehavior.PATROL,
            lootTable = listOf(
                LootDrop("item_ancient_gear", 0.7f),
                LootDrop("item_rusted_metal", 0.8f),
                LootDrop("item_power_crystal", 0.3f)
            ),
            experienceReward = 75
        ))
        
        registerEnemy(Enemy(
            id = "enemy_shadow_wraith",
            name = "Shadow Wraith",
            description = "A ghostly figure that haunts the ruins. Passes through walls and strikes from darkness.",
            level = 8,
            health = 32,
            attack = 17,
            defense = 5,
            speed = 10,
            habitat = listOf("ruins", "ruins_forgotten_library", "ruins_echo_chamber"),
            behavior = EnemyBehavior.AMBUSH,
            lootTable = listOf(
                LootDrop("item_ectoplasm", 0.8f),
                LootDrop("item_soul_fragment", 0.4f)
            ),
            experienceReward = 85
        ))
        
        registerEnemy(Enemy(
            id = "enemy_cursed_statue",
            name = "Cursed Statue",
            description = "A stone sculpture animated by dark magic. Slow but incredibly strong.",
            level = 9,
            health = 70,
            attack = 20,
            defense = 18,
            speed = 3,
            habitat = listOf("ruins_statue_garden", "ruins"),
            behavior = EnemyBehavior.TERRITORIAL,
            lootTable = listOf(
                LootDrop("item_cursed_stone", 0.6f),
                LootDrop("item_ancient_rune", 0.5f)
            ),
            experienceReward = 95
        ))
        
        registerEnemy(Enemy(
            id = "enemy_tomb_scorpion",
            name = "Tomb Scorpion",
            description = "A giant scorpion that nests in ancient crypts. Its venom is legendary.",
            level = 9,
            health = 42,
            attack = 22,
            defense = 10,
            speed = 8,
            habitat = listOf("ruins", "ruins_underground_chamber"),
            behavior = EnemyBehavior.AGGRESSIVE,
            lootTable = listOf(
                LootDrop("item_scorpion_stinger", 0.7f),
                LootDrop("item_scorpion_venom", 0.6f),
                LootDrop("item_chitin_armor", 0.4f)
            ),
            experienceReward = 100
        ))
        
        // Boss enemies (Level 10+)
        registerEnemy(Enemy(
            id = "enemy_magpie_boss",
            name = "Magpie King",
            description = "The largest and most cunning magpie, hoarding countless treasures in a massive nest.",
            level = 10,
            health = 100,
            attack = 25,
            defense = 12,
            speed = 10,
            habitat = listOf("magpie_nest"),
            behavior = EnemyBehavior.TERRITORIAL,
            lootTable = listOf(
                LootDrop("item_magpie_crown", 1.0f),
                LootDrop("item_shiny_hoard", 1.0f),
                LootDrop("item_rare_trinket", 0.8f)
            ),
            experienceReward = 200,
            isBoss = true
        ))
        
        registerEnemy(Enemy(
            id = "enemy_swamp_gator",
            name = "Ancient Alligator",
            description = "A massive reptile that has ruled the swamp for decades. Its hide is nearly impenetrable.",
            level = 12,
            health = 150,
            attack = 30,
            defense = 20,
            speed = 4,
            habitat = listOf("swamp_gator_den"),
            behavior = EnemyBehavior.TERRITORIAL,
            lootTable = listOf(
                LootDrop("item_gator_hide", 1.0f),
                LootDrop("item_gator_tooth", 1.0f),
                LootDrop("item_swamp_treasure", 0.9f)
            ),
            experienceReward = 300,
            isBoss = true
        ))
        
        registerEnemy(Enemy(
            id = "enemy_eagle_matriarch",
            name = "Eagle Matriarch",
            description = "The undisputed ruler of the mountain skies. Her talons have claimed countless lives.",
            level = 13,
            health = 120,
            attack = 35,
            defense = 15,
            speed = 14,
            habitat = listOf("mountains_eagles_aerie"),
            behavior = EnemyBehavior.AGGRESSIVE,
            lootTable = listOf(
                LootDrop("item_eagle_crown_feather", 1.0f),
                LootDrop("item_legendary_talon", 1.0f),
                LootDrop("item_sky_gem", 0.7f)
            ),
            experienceReward = 350,
            isBoss = true
        ))
        
        registerEnemy(Enemy(
            id = "enemy_ruins_guardian",
            name = "The Eternal Guardian",
            description = "The final defense of the ancient civilization. A towering construct of magic and metal.",
            level = 15,
            health = 200,
            attack = 40,
            defense = 25,
            speed = 6,
            habitat = listOf("ruins_treasury_vault"),
            behavior = EnemyBehavior.TERRITORIAL,
            lootTable = listOf(
                LootDrop("item_guardian_core", 1.0f),
                LootDrop("item_ancient_artifact", 1.0f),
                LootDrop("item_legendary_weapon", 0.5f)
            ),
            experienceReward = 500,
            isBoss = true
        ))
    }
}
