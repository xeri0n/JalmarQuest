package com.jalmarquest.feature.explore

import com.jalmarquest.core.model.LoreSnippet
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put

/**
 * Location and biome-specific lore snippets.
 * These encounters only trigger in specific regions or biomes.
 */
object BiomeSpecificSnippets {
    
    /**
     * Get all biome-specific snippet records.
     */
    fun getAllSnippets(): List<SnippetRecord> {
        return listOf(
            // FOREST BIOME SNIPPETS
            forestCrowPerch(),
            forestMushroomGrove(),
            forestSpiderWebs(),
            
            // BEACH BIOME SNIPPETS
            beachTidePools(),
            beachDriftwoodMaze(),
            
            // MOUNTAIN BIOME SNIPPETS
            mountainRockyOutcrop(),
            
            // WETLAND BIOME SNIPPETS
            wetlandFrogPond()
        )
    }
    
    private fun forestCrowPerch() = SnippetRecord(
        snippet = LoreSnippet(
            id = "forest_crow_encounter",
            eventText = "High above, a massive crow perches on a branch, its beady eyes tracking Jalmar's every movement. It tilts its head, considering whether this tiny quail is friend, foe, or snack.",
            choiceOptions = listOf(
                "Offer a seed as tribute",
                "Hide behind a fern",
                "Chirp a friendly greeting"
            ),
            consequences = buildJsonObject {
                put("offer", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("forest_crow_befriended"))
                    put("grant_items", buildItemArray("shiny_feather", 1))
                    put("narration", "The crow accepts the offering and drops a glossy black feather in exchange. An alliance has been formed.")
                })
                put("hide", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("forest_crow_avoided"))
                    put("grant_status_effects", buildStatusArray(
                        buildStatusEffect("cautious", 20)
                    ))
                    put("narration", "Jalmar stays perfectly still in the shadows. The crow eventually loses interest and flies away.")
                })
                put("chirp", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("forest_crow_amused"))
                    put("grant_status_effects", buildStatusArray(
                        buildStatusEffect("bold", 30)
                    ))
                    put("narration", "The crow seems amused by the tiny quail's audacity. It caws loudly before taking flight.")
                })
            },
            conditions = emptyConditions(),
            allowedBiomes = listOf("forest")
        ),
        choiceKeyByOptionIndex = mapOf(0 to "offer", 1 to "hide", 2 to "chirp"),
        completionTag = "biome_forest_crow_completed",
        prerequisites = emptySet(),
        title = "Crow's Perch Diplomacy",
        historySummary = "Navigated a tense encounter with a territorial crow."
    )
    
    private fun forestMushroomGrove() = SnippetRecord(
        snippet = LoreSnippet(
            id = "forest_mushroom_discovery",
            eventText = "Giant toadstools glow softly in the dim forest light. Strange spores drift through the air like golden snow, and small insects buzz around the caps.",
            choiceOptions = listOf(
                "Collect glowing spores",
                "Hunt the insects",
                "Rest beneath the largest cap"
            ),
            consequences = buildJsonObject {
                put("collect", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("forest_spores_collected"))
                    put("grant_items", buildItemArray("glowing_spore", 3))
                    put("narration", "Jalmar carefully gathers the luminescent spores. They'll make excellent crafting materials.")
                })
                put("hunt", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("forest_insects_hunted"))
                    put("grant_items", buildItemArray("forest_beetle", 2))
                    put("narration", "Quick pecks secure a tasty meal. The beetles are crunchy and satisfying.")
                })
                put("rest", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("forest_mushroom_rest"))
                    put("grant_status_effects", buildStatusArray(
                        buildStatusEffect("well_rested", 60)
                    ))
                    put("narration", "The massive mushroom provides excellent shelter. Jalmar feels refreshed and energized.")
                })
            },
            conditions = emptyConditions(),
            allowedBiomes = listOf("forest")
        ),
        choiceKeyByOptionIndex = mapOf(0 to "collect", 1 to "hunt", 2 to "rest"),
        completionTag = "biome_forest_mushroom_completed",
        prerequisites = emptySet(),
        title = "Mushroom Grove Bounty",
        historySummary = "Discovered the mysteries of the glowing mushroom grove."
    )
    
    private fun forestSpiderWebs() = SnippetRecord(
        snippet = LoreSnippet(
            id = "forest_spider_webs",
            eventText = "Massive spider webs stretch between trees, each strand thick as Jalmar's leg. Dew drops catch the light like diamonds, and something large moves in the shadows.",
            choiceOptions = listOf(
                "Quickly dash underneath",
                "Carefully navigate around",
                "Inspect the trapped prey"
            ),
            consequences = buildJsonObject {
                put("dash", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("forest_webs_dashed"))
                    put("grant_status_effects", buildStatusArray(
                        buildStatusEffect("sticky_feathers", 15)
                    ))
                    put("narration", "A few web strands stick to Jalmar's feathers, but escape is successful!")
                })
                put("navigate", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("forest_webs_navigated"))
                    put("grant_status_effects", buildStatusArray(
                        buildStatusEffect("nimble", 30)
                    ))
                    put("narration", "Jalmar picks a careful path through the maze of silk, emerging unscathed.")
                })
                put("inspect", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("forest_webs_inspected"))
                    put("grant_items", buildItemArray("spider_silk", 2))
                    put("narration", "Among the trapped insects, Jalmar finds valuable silk strands. The spider watches but doesn't approach.")
                })
            },
            conditions = emptyConditions(),
            allowedBiomes = listOf("forest")
        ),
        choiceKeyByOptionIndex = mapOf(0 to "dash", 1 to "navigate", 2 to "inspect"),
        completionTag = "biome_forest_webs_completed",
        prerequisites = emptySet(),
        title = "Spider Territory",
        historySummary = "Braved the massive spider webs of the deep forest."
    )
    
    private fun beachTidePools() = SnippetRecord(
        snippet = LoreSnippet(
            id = "beach_tide_pools",
            eventText = "Crystal-clear tide pools teem with tiny crabs and colorful anemones. The retreating tide has left treasures scattered across the sand.",
            choiceOptions = listOf(
                "Hunt hermit crabs",
                "Collect seashells",
                "Wade into the pools"
            ),
            consequences = buildJsonObject {
                put("hunt", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("beach_crabs_hunted"))
                    put("grant_items", buildItemArray("hermit_crab", 2))
                    put("narration", "Jalmar's quick reflexes secure several crabs. They'll make excellent food.")
                })
                put("collect", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("beach_shells_collected"))
                    put("grant_items", buildItemArray("seashell", 4))
                    put("narration", "Beautiful spiraled shells add to Jalmar's collection. Perfect for decorating the nest.")
                })
                put("wade", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("beach_pools_explored"))
                    put("grant_status_effects", buildStatusArray(
                        buildStatusEffect("refreshed", 30)
                    ))
                    put("narration", "The cool salt water is refreshing. Jalmar spots something glinting beneath the surface...")
                })
            },
            conditions = emptyConditions(),
            allowedBiomes = listOf("beach")
        ),
        choiceKeyByOptionIndex = mapOf(0 to "hunt", 1 to "collect", 2 to "wade"),
        completionTag = "biome_beach_pools_completed",
        prerequisites = emptySet(),
        title = "Tide Pool Treasures",
        historySummary = "Explored the tide pools left by the receding ocean."
    )
    
    private fun beachDriftwoodMaze() = SnippetRecord(
        snippet = LoreSnippet(
            id = "beach_driftwood_maze",
            eventText = "Bleached driftwood forms a labyrinth on the shore. Salt-crusted and sun-weathered, the wood provides countless hiding spots and passages.",
            choiceOptions = listOf(
                "Navigate to the center",
                "Climb to the highest log",
                "Search for hidden items"
            ),
            consequences = buildJsonObject {
                put("navigate", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("beach_maze_solved"))
                    put("grant_status_effects", buildStatusArray(
                        buildStatusEffect("pathfinder", 40)
                    ))
                    put("narration", "Jalmar successfully maps the driftwood maze, discovering a shortcut to the sea caves.")
                })
                put("climb", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("beach_maze_climbed"))
                    put("grant_status_effects", buildStatusArray(
                        buildStatusEffect("elevated_view", 20)
                    ))
                    put("narration", "From the top, Jalmar can see for miles. The ocean stretches to the horizon.")
                })
                put("search", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("beach_maze_searched"))
                    put("grant_items", buildItemArray("driftwood_treasure", 1))
                    put("narration", "Wedged in a crevice, Jalmar finds something shiny left by the tide.")
                })
            },
            conditions = emptyConditions(),
            allowedBiomes = listOf("beach")
        ),
        choiceKeyByOptionIndex = mapOf(0 to "navigate", 1 to "climb", 2 to "search"),
        completionTag = "biome_beach_maze_completed",
        prerequisites = emptySet(),
        title = "Driftwood Labyrinth",
        historySummary = "Navigated the complex maze of sun-bleached driftwood."
    )
    
    private fun mountainRockyOutcrop() = SnippetRecord(
        snippet = LoreSnippet(
            id = "mountain_rocky_outcrop",
            eventText = "Wind howls across the rocky outcrop. Below, the world spreads out like a tapestry. Above, eagles circle lazily on thermals.",
            choiceOptions = listOf(
                "Take shelter from the wind",
                "Observe the eagles",
                "Collect mountain herbs"
            ),
            consequences = buildJsonObject {
                put("shelter", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("mountain_sheltered"))
                    put("grant_status_effects", buildStatusArray(
                        buildStatusEffect("hardy", 45)
                    ))
                    put("narration", "Jalmar finds a wind-protected nook. The harsh mountain climate is survivable after all.")
                })
                put("observe", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("mountain_eagles_observed"))
                    put("grant_status_effects", buildStatusArray(
                        buildStatusEffect("keen_eye", 30)
                    ))
                    put("narration", "Watching the eagles teaches Jalmar about air currents and hunting patterns.")
                })
                put("collect", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("mountain_herbs_collected"))
                    put("grant_items", buildItemArray("mountain_herb", 2))
                    put("narration", "Rare alpine herbs grow in the rocky crevices. These will make powerful medicines.")
                })
            },
            conditions = emptyConditions(),
            allowedBiomes = listOf("mountain")
        ),
        choiceKeyByOptionIndex = mapOf(0 to "shelter", 1 to "observe", 2 to "collect"),
        completionTag = "biome_mountain_outcrop_completed",
        prerequisites = emptySet(),
        title = "Mountain Heights",
        historySummary = "Survived the harsh conditions of the rocky mountain outcrop."
    )
    
    private fun wetlandFrogPond() = SnippetRecord(
        snippet = LoreSnippet(
            id = "wetland_frog_pond",
            eventText = "A chorus of frogs fills the air. Water lilies dot the murky pond surface, and dragonflies dart between reeds. The mud is thick and treacherous.",
            choiceOptions = listOf(
                "Approach the lily pads",
                "Chase dragonflies",
                "Wade through the shallows"
            ),
            consequences = buildJsonObject {
                put("approach", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("wetland_lilies_explored"))
                    put("grant_items", buildItemArray("lily_seed", 3))
                    put("narration", "The lily pads provide stepping stones. Jalmar harvests some seeds for planting.")
                })
                put("chase", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("wetland_dragonflies_chased"))
                    put("grant_status_effects", buildStatusArray(
                        buildStatusEffect("agile", 35)
                    ))
                    put("narration", "The dragonflies are quick but predictable. Jalmar's reflexes sharpen with each leap.")
                })
                put("wade", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("wetland_explored"))
                    put("grant_status_effects", buildStatusArray(
                        buildStatusEffect("muddy", 25)
                    ))
                    put("narration", "The water is cool and filled with small fish. Jalmar's feet sink deep into the mud.")
                })
            },
            conditions = emptyConditions(),
            allowedBiomes = listOf("wetland")
        ),
        choiceKeyByOptionIndex = mapOf(0 to "approach", 1 to "chase", 2 to "wade"),
        completionTag = "biome_wetland_pond_completed",
        prerequisites = emptySet(),
        title = "Frog Pond Chorus",
        historySummary = "Explored the vibrant ecosystem of the wetland frog pond."
    )
    
    // Helper functions
    private fun buildChoiceTags(vararg tags: String) = buildJsonArray {
        tags.forEach(::add)
    }
    
    private fun buildItemArray(itemId: String, quantity: Int) = buildJsonArray {
        add(buildJsonObject {
            put("item_id", itemId)
            put("quantity", quantity)
        })
    }
    
    private fun buildStatusArray(vararg status: kotlinx.serialization.json.JsonObject) = buildJsonArray {
        status.forEach(::add)
    }
    
    private fun buildStatusEffect(key: String, durationMinutes: Int? = null) = buildJsonObject {
        put("key", key)
        durationMinutes?.let { put("duration_minutes", it) }
    }
    
    private fun emptyConditions() = buildJsonObject {
        put("requires", buildJsonArray { })
    }
}
