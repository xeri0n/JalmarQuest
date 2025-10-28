package com.jalmarquest.feature.explore

import com.jalmarquest.core.model.LoreSnippet
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.add

/**
 * Ignatius Lore Chain - 5-part narrative arc revealing his secret identity
 * and the player's role in preventing war between Buttonburgh and the Insect Kingdom.
 * 
 * Triggers via quest completion choice tags and location-based encounters.
 */
object IgnatiusLoreSnippets {
    
    fun getAllSnippets(): List<SnippetRecord> = listOf(
        // Snippet 1: First Meeting (triggers after accepting quest_ignatius_introduction)
        SnippetRecord(
            snippet = LoreSnippet(
                id = "ignatius_first_meeting",
                eventText = "Master Ignatius stands hunched over ancient scrolls in his cluttered study. His feathers—once vibrant—are now faded and worn. 'Ah, young Jalmar,' he says without looking up. 'Your reputation precedes you. I have... research that requires a sharp mind and sharper discretion.' His eyes meet yours, and you see something haunted behind them.",
                choiceOptions = listOf(
                    "Ask about the research",
                    "Question his secrecy",
                    "Accept without questions"
                ),
                consequences = buildJsonObject {
                    put("research", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("ignatius_curious"))
                        put("narration", "'The texts I seek are scattered in the Ancient Ruins. They contain knowledge of... old alliances,' Ignatius explains carefully.")
                    })
                    put("secrecy", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("ignatius_suspicious"))
                        put("narration", "Ignatius's expression hardens. 'Discretion is survival, young one. You'll understand in time—if you choose to help me.'")
                    })
                    put("accept", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("ignatius_trusted"))
                        put("grant_items", buildJsonArray {
                            add(buildJsonObject {
                                put("item_id", "research_journal")
                                put("quantity", 1)
                            })
                        })
                        put("narration", "Ignatius nods approvingly and hands you a worn research journal. 'Good. Trust is rare in these times.'")
                    })
                },
                conditions = buildJsonObject {},
                allowedLocations = listOf("BUTTONBURGH_LABORATORY"),
                allowedBiomes = emptyList()
            ),
            choiceKeyByOptionIndex = mapOf(
                0 to "research",
                1 to "secrecy",
                2 to "accept"
            ),
            completionTag = "ignatius_lore_meeting_complete",
            prerequisites = setOf("quest_ignatius_introduction_accepted"),
            title = "The Scholar's Request",
            historySummary = "Met Master Ignatius and accepted his mysterious research task."
        ),
        
        // Snippet 2: Midnight Delivery Discovery (triggers during quest_ignatius_trust)
        SnippetRecord(
            snippet = LoreSnippet(
                id = "ignatius_midnight_delivery",
                eventText = "The Ant Colony tunnels are eerily quiet as you navigate by moonlight. Your sealed package feels heavier than its weight suggests. A guard emerges from the shadows, antennae twitching. 'The Defector sent you,' they state—not a question. 'The alliance holds. For now.' They take the package and vanish without another word. What alliance?",
                choiceOptions = listOf(
                    "Attempt to follow the guard",
                    "Inspect the delivery route for clues",
                    "Return to Ignatius immediately",
                    "Ask other Ants about 'The Defector'"
                ),
                consequences = buildJsonObject {
                    put("follow", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("ignatius_lore_investigated"))
                        put("grant_status_effects", buildJsonArray {
                            add(buildStatusEffect("suspicious", 120))
                        })
                        put("narration", "You catch glimpses of coded symbols carved into tunnel walls—marks you've seen in Ignatius's study. Your mind races with implications.")
                    })
                    put("inspect", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("ignatius_lore_observant"))
                        put("grant_items", buildJsonArray {
                            add(buildJsonObject {
                                put("item_id", "coded_symbol_rubbing")
                                put("quantity", 1)
                            })
                        })
                        put("narration", "You find peculiar scratch marks on the tunnel floor—deliberate patterns that seem to spell out coordinates.")
                    })
                    put("return", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("ignatius_lore_loyal"))
                        put("grant_seeds", 50)
                        put("narration", "You decide ignorance is safer. Some secrets aren't meant for tiny quails.")
                    })
                    put("ask", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("ignatius_lore_social"))
                        put("narration", "The Ants exchange nervous glances. 'We speak of no such thing,' one says firmly. But their fear is palpable.")
                    })
                },
                conditions = buildJsonObject {},
                allowedLocations = listOf("ANT_COLONY_TUNNELS"),
                allowedBiomes = listOf("UNDERGROUND")
            ),
            choiceKeyByOptionIndex = mapOf(
                0 to "follow",
                1 to "inspect",
                2 to "return",
                3 to "ask"
            ),
            completionTag = "ignatius_lore_delivery_complete",
            prerequisites = setOf("quest_ignatius_trust_accepted"),
            title = "The Midnight Delivery",
            historySummary = "Discovered mysterious references to 'The Defector' in the Ant Colony."
        ),
        
        // Snippet 3: The Confession (triggers when completing quest_ignatius_secret)
        SnippetRecord(
            snippet = LoreSnippet(
                id = "ignatius_confession",
                eventText = "Ignatius locks the laboratory door and draws the curtains. 'What I'm about to tell you could mean death for both of us.' He takes a shaking breath. 'I was the Third Councilor of the Insect Kingdom. I sat in chambers where war strategies were drawn—strategies to conquer Buttonburgh and enslave all bird-kind. But I couldn't... I defected. And now they hunt me.'",
                choiceOptions = listOf(
                    "Express shock and betrayal",
                    "Ask why he defected",
                    "Assure him your loyalty"
                ),
                consequences = buildJsonObject {
                    put("shock", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("ignatius_lore_betrayed"))
                        put("grant_faction_reputation", buildJsonArray {
                            add(buildJsonObject {
                                put("faction_id", "faction_buttonburgh")
                                put("amount", 5)
                            })
                        })
                        put("narration", "'I understand your anger,' Ignatius says quietly. 'But know this: my betrayal of the Kingdom saves thousands of lives.'")
                    })
                    put("why", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("ignatius_lore_understanding"))
                        put("unlock_thought", "thought_defectors_burden")
                        put("narration", "'I saw the war plans—the genocide they planned. I realized power built on suffering is no power at all. So I chose to help those who'd be destroyed.'")
                    })
                    put("loyalty", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("ignatius_lore_unwavering"))
                        put("grant_faction_reputation", buildJsonArray {
                            add(buildJsonObject {
                                put("faction_id", "faction_buttonburgh")
                                put("amount", 10)
                            })
                            add(buildJsonObject {
                                put("faction_id", "faction_insects")
                                put("amount", -15)
                            })
                        })
                        put("narration", "Tears well in Ignatius's eyes. 'You remind me why I chose this path. Together, we can prevent this war.'")
                    })
                },
                conditions = buildJsonObject {},
                allowedLocations = listOf("BUTTONBURGH_LABORATORY"),
                allowedBiomes = emptyList()
            ),
            choiceKeyByOptionIndex = mapOf(
                0 to "shock",
                1 to "why",
                2 to "loyalty"
            ),
            completionTag = "ignatius_lore_confession_complete",
            prerequisites = setOf("quest_ignatius_trust_complete"),
            title = "The Defector's Truth",
            historySummary = "Learned that Ignatius was the Third Councilor of the Insect Kingdom before defecting."
        ),
        
        // Snippet 4: The Border Infiltration (during quest_ignatius_secret)
        SnippetRecord(
            snippet = LoreSnippet(
                id = "ignatius_border_infiltration",
                eventText = "The Insect Kingdom border outpost looms before you—a fortress of chitin and malice. Guards patrol with military precision. You spot the war plans through a cracked window: detailed maps showing Buttonburgh surrounded, supply lines cut, civilians marked for 'labor reassignment.' Your blood runs cold. This isn't conquest—it's extermination.",
                choiceOptions = listOf(
                    "Steal the plans stealthily",
                    "Create a distraction and grab them",
                    "Sabotage their equipment before leaving",
                    "Memorize the plans instead of taking them"
                ),
                consequences = buildJsonObject {
                    put("stealth", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("ignatius_lore_stealth_master"))
                        put("grant_items", buildJsonArray {
                            add(buildJsonObject {
                                put("item_id", "war_plans")
                                put("quantity", 1)
                            })
                        })
                        put("narration", "You slip in and out like a shadow. The guards never knew you were there. Ignatius will have everything he needs.")
                    })
                    put("distraction", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("ignatius_lore_bold_move"))
                        put("grant_items", buildJsonArray {
                            add(buildJsonObject {
                                put("item_id", "war_plans")
                                put("quantity", 1)
                            })
                        })
                        put("grant_status_effects", buildJsonArray {
                            add(buildStatusEffect("hunted_by_insects", 720))
                        })
                        put("narration", "Alarms blare as you escape with the plans. The Insect Kingdom now knows someone is onto them.")
                    })
                    put("sabotage", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("ignatius_lore_saboteur"))
                        put("grant_items", buildJsonArray {
                            add(buildJsonObject {
                                put("item_id", "war_plans")
                                put("quantity", 1)
                            })
                        })
                        put("grant_faction_reputation", buildJsonArray {
                            add(buildJsonObject {
                                put("faction_id", "faction_insects")
                                put("amount", -25)
                            })
                        })
                        put("narration", "You disable their siege weapons before grabbing the plans. This will buy Buttonburgh precious time.")
                    })
                    put("memorize", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("ignatius_lore_memory"))
                        put("unlock_thought", "thought_perfect_recall")
                        put("narration", "You commit every detail to memory—no physical evidence, no trail. Ignatius taught you well.")
                    })
                },
                conditions = buildJsonObject {},
                allowedLocations = listOf("INSECT_BORDER_OUTPOST"),
                allowedBiomes = listOf("WASTELAND")
            ),
            choiceKeyByOptionIndex = mapOf(
                0 to "stealth",
                1 to "distraction",
                2 to "sabotage",
                3 to "memorize"
            ),
            completionTag = "ignatius_lore_infiltration_complete",
            prerequisites = setOf("quest_ignatius_secret_accepted"),
            title = "The Border Infiltration",
            historySummary = "Infiltrated the Insect Kingdom outpost and discovered their war plans."
        ),
        
        // Snippet 5: The Final Choice (triggers during quest_ignatius_alliance_choice)
        SnippetRecord(
            snippet = LoreSnippet(
                id = "ignatius_final_choice",
                eventText = "Ignatius spreads three documents before you. 'Your choice will shape the future,' he says gravely. Document 1: Full alliance with Buttonburgh—war is inevitable, but you'll have their full support. Document 2: Peace brokering—attempt diplomacy with all factions, risking trust of all. Document 3: Expose Ignatius and side with the Insect Kingdom—betray him to gain their favor. 'Choose wisely, Jalmar. There are no perfect answers.'",
                choiceOptions = listOf(
                    "Ally with Buttonburgh (WAR PATH)",
                    "Broker peace with all factions (PEACE PATH)",
                    "Expose Ignatius to the Insect Kingdom (BETRAYAL PATH)"
                ),
                consequences = buildJsonObject {
                    put("war", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("ignatius_finale_war_path", "faction_war_started"))
                        put("grant_faction_reputation", buildJsonArray {
                            add(buildJsonObject {
                                put("faction_id", "faction_buttonburgh")
                                put("amount", 50)
                            })
                            add(buildJsonObject {
                                put("faction_id", "faction_insects")
                                put("amount", -100)
                            })
                        })
                        put("unlock_quest", "quest_war_campaign")
                        put("narration", "Ignatius nods solemnly. 'Then we stand together. May the ancestors guide our wings in battle.' War is coming.")
                    })
                    put("peace", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("ignatius_finale_peace_path", "diplomatic_mission_active"))
                        put("grant_faction_reputation", buildJsonArray {
                            add(buildJsonObject {
                                put("faction_id", "faction_buttonburgh")
                                put("amount", 15)
                            })
                            add(buildJsonObject {
                                put("faction_id", "faction_insects")
                                put("amount", 10)
                            })
                            add(buildJsonObject {
                                put("faction_id", "faction_ant_colony")
                                put("amount", 20)
                            })
                        })
                        put("unlock_quest", "quest_peace_summit")
                        put("narration", "Ignatius smiles sadly. 'The hardest path, but the worthiest. I'll help however I can—even if it means facing my past.'")
                    })
                    put("betrayal", buildJsonObject {
                        put("add_choice_tags", buildChoiceTags("ignatius_finale_betrayal_path", "ignatius_betrayed"))
                        put("grant_faction_reputation", buildJsonArray {
                            add(buildJsonObject {
                                put("faction_id", "faction_buttonburgh")
                                put("amount", -75)
                            })
                            add(buildJsonObject {
                                put("faction_id", "faction_insects")
                                put("amount", 75)
                            })
                        })
                        put("unlock_quest", "quest_insect_rise")
                        put("narration", "Ignatius's face falls. 'I see. I misjudged you, Jalmar.' Guards burst in. He doesn't resist. You've chosen power over principle.")
                    })
                },
                conditions = buildJsonObject {},
                allowedLocations = listOf("BUTTONBURGH_LABORATORY"),
                allowedBiomes = emptyList()
            ),
            choiceKeyByOptionIndex = mapOf(
                0 to "war",
                1 to "peace",
                2 to "betrayal"
            ),
            completionTag = "ignatius_lore_finale_complete",
            prerequisites = setOf("quest_ignatius_secret_complete"),
            title = "The Three Paths",
            historySummary = "Made the ultimate choice that will shape the fate of Buttonburgh and the Insect Kingdom."
        )
    )
    
    private fun buildChoiceTags(vararg tags: String) = buildJsonArray {
        tags.forEach(::add)
    }
    
    private fun buildStatusEffect(key: String, durationMinutes: Int) = buildJsonObject {
        put("key", key)
        put("duration_minutes", durationMinutes)
    }
}
