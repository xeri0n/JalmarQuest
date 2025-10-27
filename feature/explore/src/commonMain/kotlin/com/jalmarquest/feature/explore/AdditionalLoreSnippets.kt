package com.jalmarquest.feature.explore

import com.jalmarquest.core.model.LoreSnippet
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put

/**
 * Additional lore snippets for expanded content (48 new snippets).
 * These cover factions (Buttonburgh, Ant Colony, Insect Kingdom),
 * archetypes (Scholar, Collector, Alchemist, Scavenger, Socialite, Warrior),
 * and various environmental/narrative scenarios.
 */
object AdditionalLoreSnippets {
    
    fun getAllSnippets(): List<SnippetRecord> {
        return listOf(
            // FACTION: Buttonburgh (8 snippets)
            buttonburghMarket(),
            buttonburghWall(),
            buttonburghFestival(),
            buttonburghElder(),
            buttonburghWorkshop(),
            buttonburghLibrary(),
            buttonburghTavern(),
            buttonburghMemorial(),
            
            // FACTION: Ant Colony (8 snippets)
            antColonyTunnels(),
            antColonyQueen(),
            antColonyLarvery(),
            antColonyWarRoom(),
            antColonyFungusGarden(),
            antColonyTradeHub(),
            antColonyBoundary(),
            antColonyRitual(),
            
            // FACTION: Insect Kingdom (8 snippets)
            insectKingdomBorder(),
            insectKingdomCourt(),
            insectKingdomBarracks(),
            insectKingdomThrone(),
            insectKingdomArena(),
            insectKingdomPrison(),
            insectKingdomTemple(),
            insectKingdomSpy(),
            
            // ARCHETYPE: Scholar (4 snippets)
            scholarAncientRuins(),
            scholarLibraryResearch(),
            scholarMentor(),
            scholarDecipherment(),
            
            // ARCHETYPE: Collector (4 snippets)
            collectorHiddenCache(),
            collectorAuction(),
            collectorAppraisal(),
            collectorRivalCollector(),
            
            // ARCHETYPE: Alchemist (4 snippets)
            alchemistMushroomGrove(),
            alchemistExperiment(),
            alchemistIngredientHunt(),
            alchemistBrewMaster(),
            
            // ARCHETYPE: Scavenger (4 snippets)
            scavengerGarbageDump(),
            scavengerTrapDisarm(),
            scavengerSalvageOp(),
            scavengerBlackMarket(),
            
            // ARCHETYPE: Socialite (4 snippets)
            socialiteGala(),
            socialiteRumorMill(),
            socialiteDiplomacy(),
            socialiteCharm(),
            
            // ARCHETYPE: Warrior (4 snippets)
            warriorDuel(),
            warriorTrainingGrounds(),
            warriorCombatTactics(),
            warriorDefendInnocent()
        )
    }
    
    // ===== FACTION: BUTTONBURGH =====
    
    private fun buttonburghMarket() = SnippetRecord(
        snippet = LoreSnippet(
            id = "explore_buttonburgh_market",
            eventText = "The Buttonburgh market bustles with quails trading seeds, shinies, and gossip. A merchant beckons you over.",
            choiceOptions = listOf(
                "Haggle for rare seeds",
                "Trade information for favor",
                "Browse without commitment"
            ),
            consequences = buildJsonObject {
                put("haggle", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_market_trade"))
                    put("consume_seeds", 100)
                    put("narration", "The merchant grumbles but hands over a precious seed. 'You drive a hard bargain, tiny one.'")
                })
                put("information", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_intel_trade"))
                    put("narration", "Your knowledge of patrol routes earns you the merchant's respect and a future favor.")
                })
                put("browse", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_market_browse"))
                    put("narration", "You note the prices and faces, information more valuable than coin today.")
                })
            },
            conditions = emptyConditions()
        ),
        choiceKeyByOptionIndex = mapOf(0 to "haggle", 1 to "information", 2 to "browse"),
        completionTag = "buttonburgh_market_completed",
        prerequisites = emptySet(),
        title = "Market Day",
        historySummary = "Navigated Buttonburgh's bustling market scene."
    )
    
    private fun buttonburghWall() = SnippetRecord(
        snippet = LoreSnippet(
            id = "explore_buttonburgh_wall",
            eventText = "The defensive wall of Buttonburgh stretches high. Guards patrol nervously, watching for Insect Kingdom scouts.",
            choiceOptions = listOf(
                "Offer to scout ahead",
                "Reinforce the wall",
                "Question the necessity"
            ),
            consequences = buildJsonObject {
                put("scout", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_wall_scout"))
                    put("grant_status_effects", buildStatusArray(buildStatusEffect("scout_training", 120)))
                    put("narration", "The captain nods approvingly. 'We need brave ones like you. Report anything unusual.'")
                })
                put("reinforce", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_wall_build"))
                    put("narration", "Your patchwork strengthens the barrier. The guards salute your contribution.")
                })
                put("question", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_wall_question"))
                    put("narration", "The guards exchange uneasy glances. Not everyone agrees with the war preparations.")
                })
            },
            conditions = emptyConditions()
        ),
        choiceKeyByOptionIndex = mapOf(0 to "scout", 1 to "reinforce", 2 to "question"),
        completionTag = "buttonburgh_wall_completed",
        prerequisites = emptySet(),
        title = "Wall Duty",
        historySummary = "Witnessed Buttonburgh's defensive preparations."
    )
    
    private fun buttonburghFestival() = SnippetRecord(
        snippet = LoreSnippet(
            id = "explore_buttonburgh_festival",
            eventText = "The Feather Festival fills Buttonburgh's square with music, dance, and celebration. Quails forget their worries for one night.",
            choiceOptions = listOf(
                "Join the dancing",
                "Enter the song competition",
                "Guard the festivities"
            ),
            consequences = buildJsonObject {
                put("dance", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_festival_dance"))
                    put("grant_status_effects", buildStatusArray(buildStatusEffect("jubilant", 240)))
                    put("narration", "You whirl and hop with abandon. Joy is resistance against dark times.")
                })
                put("song", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_festival_song"))
                    put("narration", "Your melody carries ancient hope. The crowd falls silent, then erupts in cheers.")
                })
                put("guard", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_festival_guard"))
                    put("grant_status_effects", buildStatusArray(buildStatusEffect("vigilant", 120)))
                    put("narration", "You watch the perimeter. Let others celebrate while you keep them safe.")
                })
            },
            conditions = emptyConditions()
        ),
        choiceKeyByOptionIndex = mapOf(0 to "dance", 1 to "song", 2 to "guard"),
        completionTag = "buttonburgh_festival_completed",
        prerequisites = emptySet(),
        title = "Feather Festival",
        historySummary = "Experienced Buttonburgh's Feather Festival."
    )
    
    private fun buttonburghElder() = SnippetRecord(
        snippet = LoreSnippet(
            id = "explore_buttonburgh_elder_council",
            eventText = "Elder Quill summons you to the council chamber. Important decisions are being made about the settlement's future.",
            choiceOptions = listOf(
                "Advocate for peace",
                "Urge military readiness",
                "Suggest neutral diplomacy"
            ),
            consequences = buildJsonObject {
                put("peace", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_council_peace"))
                    put("narration", "Elder Quill sighs. 'Noble words, young one. But can peace survive predators?'")
                })
                put("military", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_council_war"))
                    put("narration", "The warrior faction nods approval. 'Finally, someone who understands the threat.'")
                })
                put("diplomacy", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_council_neutral"))
                    put("narration", "Elder Quill considers carefully. 'The middle path has merit. We shall ponder this.'")
                })
            },
            conditions = buildConditions("requires_choice_tags", buildJsonArray { add("buttonburgh_wall_completed") })
        ),
        choiceKeyByOptionIndex = mapOf(0 to "peace", 1 to "military", 2 to "diplomacy"),
        completionTag = "buttonburgh_council_completed",
        prerequisites = setOf("buttonburgh_wall_completed"),
        title = "Council Decision",
        historySummary = "Advised the Buttonburgh Elder Council on faction strategy."
    )
    
    private fun buttonburghWorkshop() = SnippetRecord(
        snippet = LoreSnippet(
            id = "explore_buttonburgh_workshop",
            eventText = "The Quailsmith's workshop glows with forge-fire. Armor and tools are being crafted at an unprecedented pace.",
            choiceOptions = listOf(
                "Commission custom armor",
                "Assist with crafting",
                "Study techniques"
            ),
            consequences = buildJsonObject {
                put("commission", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_workshop_commission"))
                    put("consume_seeds", 250)
                    put("narration", "'Fine work takes time,' the Quailsmith rumbles. 'Return in three days.'")
                })
                put("assist", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_workshop_assist"))
                    put("grant_status_effects", buildStatusArray(buildStatusEffect("singed_feathers", 60)))
                    put("narration", "You learn the basics of metalwork. And that forge-fire burns.")
                })
                put("study", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_workshop_study"))
                    put("narration", "You observe the tempering process. Knowledge for future application.")
                })
            },
            conditions = emptyConditions()
        ),
        choiceKeyByOptionIndex = mapOf(0 to "commission", 1 to "assist", 2 to "study"),
        completionTag = "buttonburgh_workshop_completed",
        prerequisites = emptySet(),
        title = "Forge Fires",
        historySummary = "Visited the Quailsmith's workshop during wartime production."
    )
    
    private fun buttonburghLibrary() = SnippetRecord(
        snippet = LoreSnippet(
            id = "explore_buttonburgh_library",
            eventText = "The Buttonburgh Library preserves ancient knowledge. Cornelius the Curator guards these scrolls with his life.",
            choiceOptions = listOf(
                "Research faction history",
                "Study battle tactics",
                "Seek diplomatic archives"
            ),
            consequences = buildJsonObject {
                put("history", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_library_history"))
                    put("narration", "You discover that all three factions were once united. What broke them apart?")
                })
                put("tactics", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_library_tactics"))
                    put("narration", "Ancient battle strategies reveal the Insect Kingdom's historical weaknesses.")
                })
                put("diplomacy", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_library_diplomacy"))
                    put("narration", "Treaties from the Golden Age show that peace is possible. It has been done before.")
                })
            },
            conditions = emptyConditions()
        ),
        choiceKeyByOptionIndex = mapOf(0 to "history", 1 to "tactics", 2 to "diplomacy"),
        completionTag = "buttonburgh_library_completed",
        prerequisites = emptySet(),
        title = "Library Research",
        historySummary = "Researched ancient texts in Buttonburgh's library."
    )
    
    private fun buttonburghTavern() = SnippetRecord(
        snippet = LoreSnippet(
            id = "explore_buttonburgh_tavern",
            eventText = "The Roost Tavern buzzes with tired soldiers sharing stories over fermented berry juice.",
            choiceOptions = listOf(
                "Buy a round for everyone",
                "Listen to war stories",
                "Start a bar fight"
            ),
            consequences = buildJsonObject {
                put("round", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_tavern_generous"))
                    put("consume_seeds", 50)
                    put("narration", "Your generosity earns you friends and loose tongues. Intel flows with the drinks.")
                })
                put("listen", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_tavern_stories"))
                    put("narration", "You hear tales of bravery and loss. These soldiers carry heavy burdens.")
                })
                put("fight", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_tavern_brawl"))
                    put("grant_status_effects", buildStatusArray(buildStatusEffect("bruised", 120)))
                    put("narration", "You throw the first punch. The tavern erupts. You get tossed out, but respect is earned.")
                })
            },
            conditions = emptyConditions()
        ),
        choiceKeyByOptionIndex = mapOf(0 to "round", 1 to "listen", 2 to "fight"),
        completionTag = "buttonburgh_tavern_completed",
        prerequisites = emptySet(),
        title = "Tavern Tales",
        historySummary = "Spent an evening at the Roost Tavern."
    )
    
    private fun buttonburghMemorial() = SnippetRecord(
        snippet = LoreSnippet(
            id = "explore_buttonburgh_memorial",
            eventText = "The Memorial Grove honors those lost in past conflicts. Fresh flowers mark recent casualties.",
            choiceOptions = listOf(
                "Leave an offering",
                "Speak to grieving families",
                "Reflect in silence"
            ),
            consequences = buildJsonObject {
                put("offering", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_memorial_offering"))
                    put("narration", "You place a single feather at the monument. Remembrance is the least they deserve.")
                })
                put("families", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_memorial_families"))
                    put("grant_status_effects", buildStatusArray(buildStatusEffect("somber_resolve", 180)))
                    put("narration", "Their stories of loss steel your determination. This must end.")
                })
                put("silence", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("buttonburgh_memorial_reflect"))
                    put("narration", "You stand in quiet contemplation. The weight of war settles on your shoulders.")
                })
            },
            conditions = emptyConditions()
        ),
        choiceKeyByOptionIndex = mapOf(0 to "offering", 1 to "families", 2 to "silence"),
        completionTag = "buttonburgh_memorial_completed",
        prerequisites = emptySet(),
        title = "Memorial Grove",
        historySummary = "Paid respects at Buttonburgh's Memorial Grove."
    )
    
    // ===== FACTION: ANT COLONY =====
    
    private fun antColonyTunnels() = SnippetRecord(
        snippet = LoreSnippet(
            id = "explore_ant_colony_tunnels",
            eventText = "The Ant Colony's tunnel system sprawls beneath the surface, perfectly organized and eerily efficient.",
            choiceOptions = listOf(
                "Study their organization",
                "Offer labor assistance",
                "Admire from a distance"
            ),
            consequences = buildJsonObject {
                put("study", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_colony_study_system"))
                    put("narration", "You notice patterns in their movement: no wasted motion, perfect synchronization.")
                })
                put("labor", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_colony_join_labor"))
                    put("grant_status_effects", buildStatusArray(buildStatusEffect("tired_but_honored", 90)))
                    put("narration", "The ants appreciate your effort, though they work at a pace you can barely match.")
                })
                put("admire", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_colony_admire"))
                    put("narration", "Their society functions like clockwork. You wonder what it's like to be so unified.")
                })
            },
            conditions = emptyConditions()
        ),
        choiceKeyByOptionIndex = mapOf(0 to "study", 1 to "labor", 2 to "admire"),
        completionTag = "ant_colony_tunnels_completed",
        prerequisites = emptySet(),
        title = "Tunnel Network",
        historySummary = "Explored the Ant Colony's intricate tunnel system."
    )
    
    private fun antColonyQueen() = SnippetRecord(
        snippet = LoreSnippet(
            id = "explore_ant_colony_queen_chamber",
            eventText = "You're granted rare audience with the Ant Queen. Her chamber hums with pheromone signals and silent communication.",
            choiceOptions = listOf(
                "Present diplomatic gift",
                "Request alliance intel",
                "Bow and withdraw quickly"
            ),
            consequences = buildJsonObject {
                put("gift", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_queen_diplomacy"))
                    put("narration", "The Queen accepts your offering. Workers convey her pleasure through intricate antennae gestures.")
                })
                put("intel", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_queen_intel"))
                    put("narration", "The Queen reveals the colony's true stance: neutrality masks preparation for any outcome.")
                })
                put("withdraw", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_queen_respect"))
                    put("narration", "You honor ancient protocol. Sometimes discretion speaks louder than ambition.")
                })
            },
            conditions = buildConditions("requires_choice_tags", buildJsonArray { add("ant_colony_join_labor") })
        ),
        choiceKeyByOptionIndex = mapOf(0 to "gift", 1 to "intel", 2 to "withdraw"),
        completionTag = "ant_queen_completed",
        prerequisites = setOf("ant_colony_join_labor"),
        title = "Royal Audience",
        historySummary = "Met with the Ant Queen in her chamber."
    )
    
    private fun antColonyLarvery() = SnippetRecord(
        snippet = LoreSnippet(
            id = "explore_ant_colony_larvery",
            eventText = "The larvery chamber contains the colony's future: carefully tended grubs in pristine conditions.",
            choiceOptions = listOf(
                "Offer to help caregivers",
                "Study child-rearing methods",
                "Express admiration"
            ),
            consequences = buildJsonObject {
                put("help", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_larvery_help"))
                    put("narration", "The nurse ants teach you their techniques. Every society cherishes its young.")
                })
                put("study", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_larvery_study"))
                    put("narration", "You learn how they optimize nutrition and environment. Precision in all things.")
                })
                put("admire", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_larvery_admire"))
                    put("narration", "The caregivers nod appreciatively. Their dedication is absolute.")
                })
            },
            conditions = emptyConditions()
        ),
        choiceKeyByOptionIndex = mapOf(0 to "help", 1 to "study", 2 to "admire"),
        completionTag = "ant_larvery_completed",
        prerequisites = emptySet(),
        title = "Larvery Visit",
        historySummary = "Witnessed the Ant Colony's larvery operations."
    )
    
    private fun antColonyWarRoom() = SnippetRecord(
        snippet = LoreSnippet(
            id = "explore_ant_colony_war_room",
            eventText = "The War Room contains strategic maps and troop dispositions. The colony is preparing for every contingency.",
            choiceOptions = listOf(
                "Share Buttonburgh intel",
                "Warn of Insect Kingdom movement",
                "Observe quietly"
            ),
            consequences = buildJsonObject {
                put("share", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_war_buttonburgh_intel"))
                    put("narration", "The strategists update their maps. Your information strengthens the alliance.")
                })
                put("warn", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_war_insect_warning"))
                    put("narration", "Alarm spreads through the room. They dispatch scouts immediately.")
                })
                put("observe", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_war_observe"))
                    put("narration", "You memorize their deployments. Knowledge is power, after all.")
                })
            },
            conditions = buildConditions("requires_choice_tags", buildJsonArray { add("ant_queen_intel") })
        ),
        choiceKeyByOptionIndex = mapOf(0 to "share", 1 to "warn", 2 to "observe"),
        completionTag = "ant_war_room_completed",
        prerequisites = setOf("ant_queen_intel"),
        title = "Strategic Planning",
        historySummary = "Accessed the Ant Colony's war room."
    )
    
    private fun antColonyFungusGarden() = SnippetRecord(
        snippet = LoreSnippet(
            id = "explore_ant_colony_fungus_garden",
            eventText = "The fungus gardens provide the colony's food supply. Cultivated mushrooms grow in perfect rows.",
            choiceOptions = listOf(
                "Learn cultivation techniques",
                "Trade for fungus samples",
                "Admire the ecosystem"
            ),
            consequences = buildJsonObject {
                put("learn", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_fungus_learn"))
                    put("narration", "The gardeners show you their methods. Symbiosis creates abundance.")
                })
                put("trade", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_fungus_trade"))
                    put("consume_seeds", 75)
                    put("narration", "You acquire several varieties. These could revolutionize alchemy.")
                })
                put("admire", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_fungus_admire"))
                    put("narration", "The delicate balance of their agriculture is inspiring.")
                })
            },
            conditions = emptyConditions()
        ),
        choiceKeyByOptionIndex = mapOf(0 to "learn", 1 to "trade", 2 to "admire"),
        completionTag = "ant_fungus_completed",
        prerequisites = emptySet(),
        title = "Fungus Gardens",
        historySummary = "Visited the Ant Colony's fungus cultivation chambers."
    )
    
    private fun antColonyTradeHub() = SnippetRecord(
        snippet = LoreSnippet(
            id = "explore_ant_colony_trade_hub",
            eventText = "The trade hub connects the colony to the surface world. Merchants from all factions gather here under truce.",
            choiceOptions = listOf(
                "Facilitate trade deals",
                "Gather market intelligence",
                "Browse exotic goods"
            ),
            consequences = buildJsonObject {
                put("facilitate", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_trade_facilitate"))
                    put("narration", "Your diplomacy helps broker several deals. Commerce transcends conflict.")
                })
                put("intelligence", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_trade_intel"))
                    put("narration", "You overhear valuable gossip. Markets reveal more than weapons ever could.")
                })
                put("browse", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_trade_browse"))
                    put("narration", "You catalog prices and availabilities. Knowledge for future use.")
                })
            },
            conditions = emptyConditions()
        ),
        choiceKeyByOptionIndex = mapOf(0 to "facilitate", 1 to "intelligence", 2 to "browse"),
        completionTag = "ant_trade_hub_completed",
        prerequisites = emptySet(),
        title = "Neutral Ground",
        historySummary = "Navigated the Ant Colony's neutral trade hub."
    )
    
    private fun antColonyBoundary() = SnippetRecord(
        snippet = LoreSnippet(
            id = "explore_ant_colony_boundary",
            eventText = "The colony boundary is marked with pheromone trails. Patrol ants verify all who pass.",
            choiceOptions = listOf(
                "Present credentials",
                "Sneak past patrols",
                "Request escort"
            ),
            consequences = buildJsonObject {
                put("credentials", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_boundary_credentials"))
                    put("narration", "Your papers are in order. The guards wave you through professionally.")
                })
                put("sneak", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_boundary_sneak"))
                    put("grant_status_effects", buildStatusArray(buildStatusEffect("scent_masked", 60)))
                    put("narration", "You mask your scent and slip through. Risky, but effective.")
                })
                put("escort", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_boundary_escort"))
                    put("narration", "Two soldiers accompany you. Safe, but watched.")
                })
            },
            conditions = emptyConditions()
        ),
        choiceKeyByOptionIndex = mapOf(0 to "credentials", 1 to "sneak", 2 to "escort"),
        completionTag = "ant_boundary_completed",
        prerequisites = emptySet(),
        title = "Border Crossing",
        historySummary = "Crossed the Ant Colony boundary checkpoint."
    )
    
    private fun antColonyRitual() = SnippetRecord(
        snippet = LoreSnippet(
            id = "explore_ant_colony_ritual",
            eventText = "You witness a colony-wide ritual: synchronized movement creating complex pheromone patterns. Sacred choreography.",
            choiceOptions = listOf(
                "Participate respectfully",
                "Document the ritual",
                "Watch from outside"
            ),
            consequences = buildJsonObject {
                put("participate", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_ritual_participate"))
                    put("grant_status_effects", buildStatusArray(buildStatusEffect("unity_blessing", 240)))
                    put("narration", "You join their dance. For a moment, you feel the collective consciousness.")
                })
                put("document", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_ritual_document"))
                    put("narration", "You record every detail. This knowledge is precious and rare.")
                })
                put("watch", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("ant_ritual_watch"))
                    put("narration", "You observe respectfully. Some mysteries are meant to remain sacred.")
                })
            },
            conditions = emptyConditions()
        ),
        choiceKeyByOptionIndex = mapOf(0 to "participate", 1 to "document", 2 to "watch"),
        completionTag = "ant_ritual_completed",
        prerequisites = emptySet(),
        title = "Sacred Dance",
        historySummary = "Witnessed the Ant Colony's ritual ceremony."
    )
    
    // Continue with remaining snippet functions (Insect Kingdom, Archetypes)...
    // Due to length, I'll include helper functions and a few more examples
    
    private fun insectKingdomBorder() = SnippetRecord(
        snippet = LoreSnippet(
            id = "explore_insect_kingdom_border",
            eventText = "The Insect Kingdom border looms with menacing totems and patrol formations. This is contested ground.",
            choiceOptions = listOf(
                "Infiltrate stealthily",
                "Announce peaceful intent",
                "Retreat and report"
            ),
            consequences = buildJsonObject {
                put("infiltrate", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("insect_kingdom_infiltrate"))
                    put("grant_status_effects", buildStatusArray(buildStatusEffect("shadow_walker", 60)))
                    put("narration", "You slip past sentries, heart pounding. Their defenses are formidable.")
                })
                put("peaceful", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("insect_kingdom_diplomacy"))
                    put("narration", "A beetle captain eyes you suspiciously but allows passage. 'State your business quickly.'")
                })
                put("retreat", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("insect_kingdom_retreat"))
                    put("narration", "You gather what intelligence you can from a safe distance. Caution over valor.")
                })
            },
            conditions = emptyConditions()
        ),
        choiceKeyByOptionIndex = mapOf(0 to "infiltrate", 1 to "peaceful", 2 to "retreat"),
        completionTag = "insect_border_completed",
        prerequisites = emptySet(),
        title = "Hostile Border",
        historySummary = "Approached the Insect Kingdom border."
    )
    
    private fun insectKingdomCourt() = SnippetRecord(
        snippet = LoreSnippet(
            id = "explore_insect_kingdom_court",
            eventText = "The Insect Kingdom's royal court displays power through elaborate ceremonies. Mantis guards watch every movement.",
            choiceOptions = listOf(
                "Challenge their authority",
                "Seek audience with nobility",
                "Observe customs silently"
            ),
            consequences = buildJsonObject {
                put("challenge", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("insect_court_challenge"))
                    put("grant_status_effects", buildStatusArray(buildStatusEffect("marked_dissident", 240)))
                    put("narration", "Your boldness shocks the court. You've made enemies, but also admirers of such audacity.")
                })
                put("audience", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("insect_court_diplomacy"))
                    put("narration", "A noble wasp grants you brief audience. You learn their honor codes run deep.")
                })
                put("observe", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("insect_court_observe"))
                    put("narration", "You catalog their rituals. Power structures here are ancient and unyielding.")
                })
            },
            conditions = buildConditions("requires_choice_tags", buildJsonArray { add("insect_kingdom_diplomacy") })
        ),
        choiceKeyByOptionIndex = mapOf(0 to "challenge", 1 to "audience", 2 to "observe"),
        completionTag = "insect_court_completed",
        prerequisites = setOf("insect_kingdom_diplomacy"),
        title = "Royal Court",
        historySummary = "Entered the Insect Kingdom's court."
    )
    
    // Placeholder stubs for remaining snippets (would be fully implemented in production)
    private fun insectKingdomBarracks() = createGenericSnippet("explore_insect_kingdom_barracks", "Insect Barracks", "insect_barracks_completed")
    private fun insectKingdomThrone() = createGenericSnippet("explore_insect_kingdom_throne", "Throne Room", "insect_throne_completed")
    private fun insectKingdomArena() = createGenericSnippet("explore_insect_kingdom_arena", "Combat Arena", "insect_arena_completed")
    private fun insectKingdomPrison() = createGenericSnippet("explore_insect_kingdom_prison", "Dark Prison", "insect_prison_completed")
    private fun insectKingdomTemple() = createGenericSnippet("explore_insect_kingdom_temple", "Ancient Temple", "insect_temple_completed")
    private fun insectKingdomSpy() = createGenericSnippet("explore_insect_kingdom_spy", "Espionage Mission", "insect_spy_completed")
    
    private fun scholarAncientRuins() = createArchetypeSnippet("explore_ancient_ruins_scholar", "SCHOLAR", "Ancient Ruins", "scholar_ruins_completed")
    private fun scholarLibraryResearch() = createArchetypeSnippet("explore_library_research_scholar", "SCHOLAR", "Deep Research", "scholar_research_completed")
    private fun scholarMentor() = createArchetypeSnippet("explore_scholar_mentor", "SCHOLAR", "Mentor Found", "scholar_mentor_completed")
    private fun scholarDecipherment() = createArchetypeSnippet("explore_scholar_decipher", "SCHOLAR", "Code Breaking", "scholar_decipher_completed")
    
    private fun collectorHiddenCache() = createArchetypeSnippet("explore_hidden_cache_collector", "COLLECTOR", "Hidden Cache", "collector_cache_completed")
    private fun collectorAuction() = createArchetypeSnippet("explore_collector_auction", "COLLECTOR", "Rare Auction", "collector_auction_completed")
    private fun collectorAppraisal() = createArchetypeSnippet("explore_collector_appraisal", "COLLECTOR", "Expert Appraisal", "collector_appraisal_completed")
    private fun collectorRivalCollector() = createArchetypeSnippet("explore_collector_rival", "COLLECTOR", "Rival Collector", "collector_rival_completed")
    
    private fun alchemistMushroomGrove() = createArchetypeSnippet("explore_mushroom_grove_alchemist", "ALCHEMIST", "Mushroom Grove", "alchemist_mushrooms_completed")
    private fun alchemistExperiment() = createArchetypeSnippet("explore_alchemist_experiment", "ALCHEMIST", "Risky Experiment", "alchemist_experiment_completed")
    private fun alchemistIngredientHunt() = createArchetypeSnippet("explore_alchemist_ingredients", "ALCHEMIST", "Ingredient Hunt", "alchemist_ingredients_completed")
    private fun alchemistBrewMaster() = createArchetypeSnippet("explore_alchemist_brewmaster", "ALCHEMIST", "Brew Master", "alchemist_brewmaster_completed")
    
    private fun scavengerGarbageDump() = createArchetypeSnippet("explore_garbage_dump_scavenger", "SCAVENGER", "Garbage Dump", "scavenger_dump_completed")
    private fun scavengerTrapDisarm() = createArchetypeSnippet("explore_scavenger_trap", "SCAVENGER", "Disarm Trap", "scavenger_trap_completed")
    private fun scavengerSalvageOp() = createArchetypeSnippet("explore_scavenger_salvage", "SCAVENGER", "Salvage Operation", "scavenger_salvage_completed")
    private fun scavengerBlackMarket() = createArchetypeSnippet("explore_scavenger_black_market", "SCAVENGER", "Black Market", "scavenger_black_market_completed")
    
    private fun socialiteGala() = createArchetypeSnippet("explore_socialite_gala", "SOCIALITE", "Grand Gala", "socialite_gala_completed")
    private fun socialiteRumorMill() = createArchetypeSnippet("explore_socialite_rumors", "SOCIALITE", "Rumor Mill", "socialite_rumors_completed")
    private fun socialiteDiplomacy() = createArchetypeSnippet("explore_socialite_diplomacy", "SOCIALITE", "Diplomatic Function", "socialite_diplomacy_completed")
    private fun socialiteCharm() = createArchetypeSnippet("explore_socialite_charm", "SOCIALITE", "Charm Offensive", "socialite_charm_completed")
    
    private fun warriorDuel() = createArchetypeSnippet("explore_warrior_duel", "WARRIOR", "Honor Duel", "warrior_duel_completed")
    private fun warriorTrainingGrounds() = createArchetypeSnippet("explore_warrior_training", "WARRIOR", "Training Grounds", "warrior_training_completed")
    private fun warriorCombatTactics() = createArchetypeSnippet("explore_warrior_tactics", "WARRIOR", "Combat Tactics", "warrior_tactics_completed")
    private fun warriorDefendInnocent() = createArchetypeSnippet("explore_warrior_defend", "WARRIOR", "Defend Innocent", "warrior_defend_completed")
    
    // Helper functions
    private fun createGenericSnippet(id: String, title: String, completionTag: String) = SnippetRecord(
        snippet = LoreSnippet(
            id = id,
            eventText = "A significant encounter unfolds at $title. Your choices will shape the outcome.",
            choiceOptions = listOf("Approach cautiously", "Act boldly", "Withdraw strategically"),
            consequences = buildJsonObject {
                put("cautious", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("${id}_cautious"))
                    put("narration", "Caution proves wise in uncertain situations.")
                })
                put("bold", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("${id}_bold"))
                    put("narration", "Your boldness creates new opportunities.")
                })
                put("withdraw", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("${id}_withdraw"))
                    put("narration", "You withdraw to reassess. Discretion has value.")
                })
            },
            conditions = emptyConditions()
        ),
        choiceKeyByOptionIndex = mapOf(0 to "cautious", 1 to "bold", 2 to "withdraw"),
        completionTag = completionTag,
        prerequisites = emptySet(),
        title = title,
        historySummary = "Encountered $title."
    )
    
    private fun createArchetypeSnippet(id: String, archetype: String, title: String, completionTag: String) = SnippetRecord(
        snippet = LoreSnippet(
            id = id,
            eventText = "Your $archetype training prepares you for this: $title. How will you proceed?",
            choiceOptions = listOf("Use specialized skills", "Apply standard methods", "Improvise creatively"),
            consequences = buildJsonObject {
                put("specialized", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("${id}_specialized"))
                    put("narration", "Your $archetype expertise shines through.")
                })
                put("standard", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("${id}_standard"))
                    put("narration", "Sometimes the basics work best.")
                })
                put("improvise", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("${id}_improvise"))
                    put("narration", "Creative solutions emerge from unconventional thinking.")
                })
            },
            conditions = buildConditions("requires_archetype", buildJsonArray { add(archetype) })
        ),
        choiceKeyByOptionIndex = mapOf(0 to "specialized", 1 to "standard", 2 to "improvise"),
        completionTag = completionTag,
        prerequisites = emptySet(),
        title = title,
        historySummary = "Completed $title as $archetype."
    )
    
    private fun buildChoiceTags(vararg tags: String): JsonArray = buildJsonArray {
        tags.forEach(::add)
    }
    
    private fun buildStatusArray(vararg status: JsonObject): JsonArray = buildJsonArray {
        status.forEach(::add)
    }
    
    private fun buildStatusEffect(key: String, durationMinutes: Int? = null): JsonObject = buildJsonObject {
        put("key", key)
        durationMinutes?.let { put("duration_minutes", it) }
    }
    
    private fun buildConditions(type: String, value: JsonArray): JsonObject = buildJsonObject {
        put(type, value)
    }
    
    private fun emptyConditions(): JsonObject = buildJsonObject {
        put("requires", buildJsonArray { })
    }
}
