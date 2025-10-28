package com.jalmarquest.feature.explore

import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.LoreSnippet
import com.jalmarquest.core.state.aidirector.AIDirectorManager
import com.jalmarquest.core.state.narrative.ContentFilterManager
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.random.Random

/**
 * Alpha 2.2: Borken Chaos Event Trigger System
 * 
 * Randomly injects chaotic events featuring Borken the button quail during exploration.
 * - Base 10% chance to trigger per exploration cycle
 * - AI Director integration: boost chance for aggressive players, suppress for struggling cautious players
 * - Events adapt based on No Filter Mode setting (via ContentFilterManager)
 * - Filtered dialogue when No Filter Mode is OFF
 * - Darker humor/edgier tone when No Filter Mode is ON
 */
class BorkenEventTrigger(
    private val contentFilterManager: ContentFilterManager?,
    private val aiDirectorManager: AIDirectorManager? = null,
    private val triggerChance: Double = 0.10 // 10% base chance
) {
    private val random = Random.Default
    
    /**
     * Determines if a Borken chaos event should trigger.
     * Returns null if no event, or a LoreSnippet if chaos strikes.
     * 
     * AI Director Integration:
     * - Boosts chance +5% if player enjoys chaos (aggressive/balanced playstyle)
     * - Suppresses chance -7% if player is struggling (cautious + recent deaths)
     */
    fun evaluateChaosEvent(choiceLog: ChoiceLog): LoreSnippet? {
        // Don't trigger if player hasn't encountered Borken yet
        val hasMetBorken = choiceLog.entries.any { it.tag.value.contains("borken") }
        if (!hasMetBorken && random.nextDouble() > 0.05) {
            // Lower chance for first introduction (5%)
            return null
        }
        
        // AI Director adaptive tuning
        val adjustedChance = calculateAdjustedChance()
        
        if (random.nextDouble() > adjustedChance) {
            return null
        }
        
        // Select random chaos event
        val eventTemplates = if (hasMetBorken) getAllChaosEvents() else listOf(borkenIntroductionEvent())
        val selectedEvent = eventTemplates.randomOrNull(random) ?: return null
        
        // Apply dialogue filtering if No Filter Mode is OFF
        return if (isNoFilterEnabled()) {
            selectedEvent // Use unfiltered version
        } else {
            filterDialogue(selectedEvent) // Apply content filtering
        }
    }
    
    /**
     * Calculate trigger chance with AI Director adjustments.
     * - Base: 10%
     * - Boost: +5% if shouldBoostChaosEvents()
     * - Suppress: -7% if shouldSuppressChaosEvents()
     */
    private fun calculateAdjustedChance(): Double {
        var chance = triggerChance
        
        aiDirectorManager?.let { director ->
            when {
                director.shouldSuppressChaosEvents() -> {
                    chance -= 0.07 // Reduce to 3% for struggling cautious players
                }
                director.shouldBoostChaosEvents() -> {
                    chance += 0.05 // Increase to 15% for chaos-enjoying players
                }
            }
        }
        
        return chance.coerceIn(0.0, 1.0)
    }
    
    private fun isNoFilterEnabled(): Boolean {
        return contentFilterManager?.isNoFilterModeEnabled() ?: false
    }
    
    /**
     * Filters event dialogue to be more family-friendly when No Filter Mode is disabled.
     */
    private fun filterDialogue(event: LoreSnippet): LoreSnippet {
        // Replace edgy/dark elements with tamer alternatives
        val filteredText = event.eventText
            .replace("existential dread", "philosophical thoughts")
            .replace("meaningless void", "uncertain future")
            .replace("we're all doomed", "things are challenging")
            .replace("pointy stick of violence", "pointy stick of problem-solving")
        
        return event.copy(eventText = filteredText)
    }
    
    // ===== CHAOS EVENT TEMPLATES =====
    
    private fun borkenIntroductionEvent(): LoreSnippet {
        val noFilter = isNoFilterEnabled()
        
        return LoreSnippet(
            id = "borken_chaos_introduction",
            eventText = if (noFilter) {
                "*A disheveled button quail appears from behind a barrel, brandishing a stick*\n\n" +
                "\"Oh great, another optimist,\" Borken mutters, poking the ground with their stick. " +
                "\"Let me guess—you think collecting shiny pebbles will save us from the existential dread of being prey animals in a world designed to eat us?\"\n\n" +
                "They tap the stick thoughtfully. \"I'm Borken. This is my pointy stick. We've both lost our minds. Welcome to reality.\""
            } else {
                "*A quirky button quail appears from behind a barrel, holding a stick*\n\n" +
                "\"Oh, another adventurer!\" Borken chirps, poking the ground curiously. " +
                "\"Let me guess—you think collecting shiny treasures will make everything better?\"\n\n" +
                "They tap the stick thoughtfully. \"I'm Borken. This is my trusty stick. We're both a bit eccentric. Nice to meet you!\""
            },
            choiceOptions = listOf(
                "\"Are you... okay?\"",
                "\"Nice stick. Very pointy.\"",
                "\"I should probably go...\""
            ),
            consequences = buildJsonObject {
                put("okay", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("borken_met_concerned"))
                    put("narration", if (noFilter) {
                        "Borken laughs bitterly. \"Okay? Define 'okay' in a universe governed by entropy and indifferent to suffering. But sure, I'm peachy.\""
                    } else {
                        "Borken chuckles. \"Okay is relative! But I'm managing. The stick helps.\""
                    })
                })
                put("stick", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("borken_met_complimented"))
                    put("grant_items", buildItemArray("item_borkens_pointy_stick", 1))
                    put("narration", "Borken beams with pride. \"You get it! Here, take this spare stick. Pointy things solve problems.\"")
                })
                put("go", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("borken_met_fled"))
                    put("narration", "\"Smart quail,\" Borken calls after you. \"Self-preservation is underrated!\"")
                })
            },
            conditions = emptyConditions()
        )
    }
    
    private fun borkenStickPhilosophyEvent(): LoreSnippet {
        val noFilter = isNoFilterEnabled()
        
        return LoreSnippet(
            id = "borken_chaos_stick_philosophy",
            eventText = if (noFilter) {
                "Borken is jabbing their stick into the dirt aggressively.\n\n" +
                "\"You know what this stick represents? Agency. In a world where hawks, foxes, and entropy want to erase us, " +
                "this pointy stick says 'Not today, cruel universe.' It's simultaneously futile AND necessary. That's the joke—we're the punchline.\""
            } else {
                "Borken is poking their stick into the dirt playfully.\n\n" +
                "\"You know what this stick represents? Determination! In a world full of challenges, " +
                "this trusty stick says 'I can handle this.' It's simple but effective. That's the beauty of it!\""
            },
            choiceOptions = listOf(
                "\"That's... surprisingly deep.\"",
                "\"It's just a stick, Borken.\"",
                "\"Can I borrow your stick?\""
            ),
            consequences = buildJsonObject {
                put("deep", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("borken_philosophy_appreciated"))
                    put("grant_status_effects", buildStatusArray(buildStatusEffect("contemplative", 180)))
                    put("narration", if (noFilter) {
                        "\"Deep? We're 3 inches tall and existentially terrified. Everything's deep when you're this small.\""
                    } else {
                        "\"Thanks! Sometimes the simple things teach us the most important lessons.\""
                    })
                })
                put("just_stick", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("borken_philosophy_dismissed"))
                    put("narration", "Borken sighs. \"And you're 'just a quail.' Doesn't make you any less complicated inside.\"")
                })
                put("borrow", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("borken_stick_borrowed"))
                    put("consume_seeds", 50)
                    put("narration", "\"Sure! 50 seeds rental fee. Pointy wisdom isn't free, friend.\"")
                })
            },
            conditions = buildJsonObject {
                put("required_choice_tags", buildChoiceTags("borken_met"))
            }
        )
    }
    
    private fun borkenMidnightRambleEvent(): LoreSnippet {
        val noFilter = isNoFilterEnabled()
        
        return LoreSnippet(
            id = "borken_chaos_midnight_ramble",
            eventText = if (noFilter) {
                "*You find Borken muttering to their stick at 2 AM*\n\n" +
                "\"...and another thing! Why do we even HAVE a circadian rhythm when predators hunt 24/7? " +
                "It's like evolution said 'Good luck, tiny birds' and wandered off to work on something less depressing. " +
                "Stick, are you even listening? No? Great. Story of my life.\""
            } else {
                "*You find Borken chatting with their stick late at night*\n\n" +
                "\"...and that's why I think we should organize our seed storage better! " +
                "The current system lacks efficiency. Stick, you agree, right? No? Well, you're a stick, so...\""
            },
            choiceOptions = listOf(
                "\"Borken, it's 2 AM. Sleep exists.\"",
                "Join the rambling session",
                "\"I'll leave you two alone...\""
            ),
            consequences = buildJsonObject {
                put("sleep", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("borken_ramble_interrupted"))
                    put("narration", if (noFilter) {
                        "\"Sleep? In THIS economy? While hawks dream of us? Hard pass, well-rested fool.\""
                    } else {
                        "\"Sleep? But I have so many ideas! ...Fine, you're probably right. Rest is important.\""
                    })
                })
                put("join", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("borken_ramble_joined"))
                    put("grant_status_effects", buildStatusArray(buildStatusEffect("sleep_deprived", 300)))
                    put("grant_seeds", 100)
                    put("narration", "You spend 3 hours discussing the universe with Borken. You're exhausted but oddly enlightened. +100 seeds for your patience.")
                })
                put("leave", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("borken_ramble_avoided"))
                    put("narration", "Smart choice. Self-care is important. Borken doesn't notice your departure.")
                })
            },
            conditions = buildJsonObject {
                put("required_choice_tags", buildChoiceTags("borken_met"))
            }
        )
    }
    
    private fun borkenPredatorEncounterEvent(): LoreSnippet {
        val noFilter = isNoFilterEnabled()
        
        return LoreSnippet(
            id = "borken_chaos_predator_encounter",
            eventText = if (noFilter) {
                "*A hawk shadow passes overhead. Borken doesn't flinch.*\n\n" +
                "\"Ah yes, the sky's favorite murder machine,\" Borken says flatly, stick raised defiantly. " +
                "\"Come at me, feathered oblivion! My stick is pointy, my will is broken, and I've got NOTHING TO LOSE!\"\n\n" +
                "The hawk, possibly confused by this response, flies away.\n\n" +
                "\"Works every time. Predators expect fear, not suicidal bravado.\""
            } else {
                "*A hawk shadow passes overhead. Borken waves their stick!*\n\n" +
                "\"Hey hawk! Not today!\" Borken shouts cheerfully, stick raised. " +
                "\"I've got a stick and the power of positive thinking!\"\n\n" +
                "The hawk, possibly amused, circles away.\n\n" +
                "\"Confidence works wonders! Also, looking slightly unhinged helps.\""
            },
            choiceOptions = listOf(
                "\"That was... reckless but effective?\"",
                "\"Teach me your ways, Borken.\"",
                "\"I'm hiding next time.\""
            ),
            consequences = buildJsonObject {
                put("reckless", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("borken_tactics_questioned"))
                    put("narration", if (noFilter) {
                        "\"Reckless? Friend, we're ALREADY doomed. Might as well go out on our terms.\""
                    } else {
                        "\"Sometimes the unexpected approach works best! Keep them guessing!\""
                    })
                })
                put("teach", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("borken_tactics_learned"))
                    put("grant_status_effects", buildStatusArray(buildStatusEffect("fearless", 600)))
                    put("narration", "Borken teaches you the art of 'calculated chaos.' +Fearless status (10 min).")
                })
                put("hide", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("borken_tactics_declined"))
                    put("narration", "\"Also valid! Survival strategies are personal. You do you, cautious friend.\"")
                })
            },
            conditions = buildJsonObject {
                put("required_choice_tags", buildChoiceTags("borken_met"))
            }
        )
    }
    
    private fun borkenSeedHoardingCritiqueEvent(): LoreSnippet {
        val noFilter = isNoFilterEnabled()
        
        return LoreSnippet(
            id = "borken_chaos_seed_hoarding",
            eventText = if (noFilter) {
                "Borken watches you count seeds with obvious judgment.\n\n" +
                "\"Ah yes, the ancient quail tradition: hoard currency you can't spend when you're dead. " +
                "Very rational. What's the exchange rate on seeds in the afterlife? Oh wait—there ISN'T ONE because we're FOOD.\"\n\n" +
                "They poke a seed with their stick. \"But sure, keep counting. At least it's a hobby.\""
            } else {
                "Borken watches you organize seeds with curiosity.\n\n" +
                "\"Interesting! You really like organizing things, huh? I respect the dedication, though personally, " +
                "I think experiences matter more than accumulation. But hey, everyone has their own priorities!\"\n\n" +
                "They tap a seed thoughtfully with their stick."
            },
            choiceOptions = listOf(
                "\"Seeds are important for survival!\"",
                "\"You're probably right...\"",
                "\"Want some seeds, Borken?\""
            ),
            consequences = buildJsonObject {
                put("survival", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("borken_seed_defended"))
                    put("narration", if (noFilter) {
                        "\"True! Can't argue with basic economics. Carry on, capitalist quail.\""
                    } else {
                        "\"Fair point! Practicality matters. I respect your planning!\""
                    })
                })
                put("right", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("borken_seed_agreed"))
                    put("grant_status_effects", buildStatusArray(buildStatusEffect("introspective", 240)))
                    put("narration", "Borken nods sagely. You feel slightly more philosophical about material possessions.")
                })
                put("share", buildJsonObject {
                    put("add_choice_tags", buildChoiceTags("borken_seed_shared"))
                    put("consume_seeds", 200)
                    put("grant_items", buildItemArray("item_borkens_pointy_stick", 1))
                    put("narration", "Borken accepts gratefully. \"Generosity in a harsh world. Here, take a spare stick. You've earned it.\"")
                })
            },
            conditions = buildJsonObject {
                put("required_choice_tags", buildChoiceTags("borken_met"))
            }
        )
    }
    
    /**
     * Returns all chaos event templates (excluding introduction).
     */
    private fun getAllChaosEvents(): List<LoreSnippet> {
        return listOf(
            borkenStickPhilosophyEvent(),
            borkenMidnightRambleEvent(),
            borkenPredatorEncounterEvent(),
            borkenSeedHoardingCritiqueEvent()
        )
    }
    
    // Helper functions (matching existing patterns)
    private fun buildChoiceTags(vararg tags: String): kotlinx.serialization.json.JsonArray {
        return kotlinx.serialization.json.buildJsonArray {
            tags.forEach { tag -> 
                add(kotlinx.serialization.json.JsonPrimitive(tag))
            }
        }
    }
    
    private fun buildStatusArray(vararg effects: kotlinx.serialization.json.JsonObject): kotlinx.serialization.json.JsonArray {
        return kotlinx.serialization.json.buildJsonArray {
            effects.forEach { add(it) }
        }
    }
    
    private fun buildStatusEffect(effectId: String, durationSeconds: Int): kotlinx.serialization.json.JsonObject {
        return buildJsonObject {
            put("effect_id", effectId)
            put("duration_seconds", durationSeconds)
        }
    }
    
    private fun buildItemArray(itemId: String, quantity: Int): kotlinx.serialization.json.JsonArray {
        return kotlinx.serialization.json.buildJsonArray {
            add(buildJsonObject {
                put("item_id", itemId)
                put("quantity", quantity)
            })
        }
    }
    
    private fun emptyConditions(): kotlinx.serialization.json.JsonObject {
        return buildJsonObject {}
    }
}
