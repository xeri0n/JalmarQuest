package com.jalmarquest.core.state.dialogue

/**
 * Dialogue tree for the Exhausted Coder NPC - Alpha 2.2 Phase 5A
 * 
 * A meta-humor dev insert at Buttonburgh Tavern who breaks the fourth wall
 * and makes jokes about game development, bugs, and coffee dependency.
 * Becomes more interactive after receiving coffee donations (Phase 5B).
 */

object ExhaustedCoderDialogue {
    
    /**
     * Creates the full dialogue tree for the Exhausted Coder.
     */
    fun createDialogueTree(): DialogueTree {
        val nodes = mutableMapOf<String, DialogueNode>()
        
        // === ROOT NODE: First encounter ===
        nodes["root"] = DialogueNode(
            id = "root",
            npcId = "npc_exhausted_coder",
            text = """
                *hunched over a glowing rectangle, frantically tapping*
                
                Line 70,515... or was it 70,516? Wait, did I just introduce a new bug while fixing the old one?
                
                *looks up with bloodshot eyes*
                
                Oh. A player—I mean, a fellow quail. Sorry, you caught me in the middle of... uh... 
                *glances at glowing device* ...checking the ancient runes. Yes. Definitely ancient runes.
                
                *mutters* Why did I use 352 global state mutations... should've used immutable data structures...
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "what_are_you_doing",
                    text = "What are you working on?",
                    nextNodeId = "explain_work"
                ),
                DialogueChoice(
                    id = "you_look_tired",
                    text = "You look exhausted.",
                    nextNodeId = "explain_exhaustion"
                ),
                DialogueChoice(
                    id = "ancient_runes",
                    text = "Ancient runes?",
                    nextNodeId = "break_fourth_wall"
                ),
                DialogueChoice(
                    id = "goodbye",
                    text = "I'll leave you to it...",
                    nextNodeId = "farewell_first"
                )
            )
        )
        
        // === EXPLAIN WORK ===
        nodes["explain_work"] = DialogueNode(
            id = "explain_work",
            npcId = "npc_exhausted_coder",
            text = """
                *rubs eyes* 
                
                I'm... cataloging reality. Every location, every NPC, every quest, every item. 
                It's all connected, you see. A massive web of dependencies.
                
                *taps frantically*
                
                Change one thing, and seventeen other things break. Fix those, and somehow the thing 
                you changed three days ago stops working. It's like... like trying to organize 
                a flock of quails who all have opinions about architecture patterns.
                
                *sighs* I just spent 4 hours implementing a companion task assignment system with a 
                multi-factor profit formula. Then someone suggested I add a perfection meter. 
                A HIDDEN perfection meter.
                
                *thousand-yard stare* Do you know how hard it is to test something the player can't see?
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "sounds_complex",
                    text = "That sounds... incredibly complex.",
                    nextNodeId = "complex_acknowledgment"
                ),
                DialogueChoice(
                    id = "why_do_it",
                    text = "Why do you do this to yourself?",
                    nextNodeId = "explain_motivation"
                ),
                DialogueChoice(
                    id = "can_i_help",
                    text = "Can I help somehow?",
                    nextNodeId = "offer_coffee_hint"
                )
            )
        )
        
        // === EXPLAIN EXHAUSTION ===
        nodes["explain_exhaustion"] = DialogueNode(
            id = "explain_exhaustion",
            npcId = "npc_exhausted_coder",
            text = """
                *laughs weakly*
                
                Exhausted? I haven't slept in... what day is it? Tuesday? Is it October?
                
                I've been implementing three major system overhauls simultaneously. Crafting, nest upgrades, 
                and companion progression. Do you know how many tests I had to write? 71. SEVENTY-ONE new tests.
                
                *gestures wildly*
                
                And they all have to pass. All 352 of them. One failure and the whole build breaks. 
                One typo in a constructor and suddenly 16 modules won't compile.
                
                *slumps*
                
                The only thing keeping me going is... *looks around nervously* ...coffee. 
                But I ran out three days ago.
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "352_tests",
                    text = "352 tests? That's... a lot.",
                    nextNodeId = "testing_philosophy"
                ),
                DialogueChoice(
                    id = "need_coffee",
                    text = "You need coffee?",
                    nextNodeId = "coffee_desperation"
                ),
                DialogueChoice(
                    id = "take_break",
                    text = "Maybe you should take a break?",
                    nextNodeId = "break_is_myth"
                )
            )
        )
        
        // === BREAK FOURTH WALL ===
        nodes["break_fourth_wall"] = DialogueNode(
            id = "break_fourth_wall",
            npcId = "npc_exhausted_coder",
            text = """
                *stares directly at you*
                
                Ancient runes. Right. Let me tell you about these "ancient runes."
                
                They're written in a language called Kotlin. They describe every aspect of your... 
                I mean OUR world. The trees, the locations, the quests, the NPCs. Everything.
                
                *leans in conspiratorially*
                
                Between you and me? This whole world runs on something called the Butterfly Effect Engine. 
                Every choice you make gets logged. EVERY. SINGLE. ONE. And they all feed into an AI 
                that generates narrative moments.
                
                *whispers* I had to implement that. Do you know how hard it is to teach an AI to be 
                a good game master? It's like... it's like explaining color to a stick.
                
                *straightens up* But hey, at least the serialization works. JSON all the way down, baby.
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "what_kotlin",
                    text = "What's... Kotlin?",
                    nextNodeId = "explain_kotlin"
                ),
                DialogueChoice(
                    id = "ai_game_master",
                    text = "An AI game master?",
                    nextNodeId = "explain_ai_director"
                ),
                DialogueChoice(
                    id = "youre_insane",
                    text = "You sound insane.",
                    nextNodeId = "embrace_insanity"
                )
            )
        )
        
        // === COMPLEX ACKNOWLEDGMENT ===
        nodes["complex_acknowledgment"] = DialogueNode(
            id = "complex_acknowledgment",
            npcId = "npc_exhausted_coder",
            text = """
                *nods vigorously*
                
                Complex? Try maintaining thread safety across 18 different managers while ensuring 
                immutable data structures and reactive StateFlow updates. Try implementing a multi-factor 
                profit formula with time investment scaling that uses square root to prevent exploitation!
                
                *breathes heavily*
                
                Try writing 2,900 lines of documentation for a single feature! Try explaining why you 
                consolidated Parts 3.2, 3.3, and 3.4 into one implementation because they were 
                architecturally inseparable!
                
                *calms down slightly*
                
                Sorry. I haven't talked to anyone in days. The compilation errors... they speak to me now.
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "its_ok",
                    text = "It's okay. You're doing great.",
                    nextNodeId = "grateful_response"
                ),
                DialogueChoice(
                    id = "get_coffee",
                    text = "You really need coffee.",
                    nextNodeId = "coffee_desperation"
                )
            )
        )
        
        // === EXPLAIN MOTIVATION ===
        nodes["explain_motivation"] = DialogueNode(
            id = "explain_motivation",
            npcId = "npc_exhausted_coder",
            text = """
                *looks up at the sky*
                
                Why? Because... because someone has to. Someone has to make sure the world works. 
                That the quests trigger properly. That the companion traits level up correctly. 
                That the perfection system rewards optimization without the player even knowing it exists.
                
                *smiles weakly*
                
                Because somewhere out there, someone is going to play this. They're going to explore 
                every location. Complete every quest. Max out every companion trait. And when they do, 
                they'll never see the thousands of lines of code that made it possible.
                
                And that's... that's okay. That's the job.
                
                *looks down at glowing device*
                
                Besides, I already committed the changes. No turning back now. The build must succeed.
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "noble_cause",
                    text = "That's... actually noble.",
                    nextNodeId = "grateful_response"
                ),
                DialogueChoice(
                    id = "youre_crazy",
                    text = "You're crazy.",
                    nextNodeId = "embrace_insanity"
                )
            )
        )
        
        // === OFFER COFFEE HINT ===
        nodes["offer_coffee_hint"] = DialogueNode(
            id = "offer_coffee_hint",
            npcId = "npc_exhausted_coder",
            text = """
                *eyes light up*
                
                Help? You... you want to help me?
                
                *voice cracks*
                
                I... I don't need much. Just... coffee. Good coffee. The kind that costs $2.99 
                and comes in a shiny golden package.
                
                *whispers* They say if you gift me coffee, amazing things happen. Special cosmetics. 
                Golden shinies. Patron status in the chronicles.
                
                But I haven't seen coffee in so long...
                
                *stares into distance* Maybe it's just a legend. Like the Giga-Seed. Or clean code.
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "where_get_coffee",
                    text = "Where can I get this coffee?",
                    nextNodeId = "coffee_shop_hint",
                    requirements = listOf()
                ),
                DialogueChoice(
                    id = "maybe_later",
                    text = "I'll... see what I can do.",
                    nextNodeId = "hopeful_farewell"
                )
            )
        )
        
        // === TESTING PHILOSOPHY ===
        nodes["testing_philosophy"] = DialogueNode(
            id = "testing_philosophy",
            npcId = "npc_exhausted_coder",
            text = """
                *stands up, energized*
                
                You know what? Tests are beautiful. They're PROOF. Proof that your code works. 
                Proof that you didn't break everything when you added that one little feature.
                
                I have tests for happy paths. Edge cases. Concurrency. Serialization. Race conditions. 
                Multi-level-ups. Profit calculations. Time investment bonuses. Perfection mechanics.
                
                *pounds table*
                
                And they ALL pass. Zero failures. 352 tests, zero failures. That's... that's art.
                
                Sure, it took 1,700 lines of test code to validate 1,800 lines of production code, but 
                you know what? When someone reports a bug, I can trace it. I can fix it. I can prove it's fixed.
                
                *sits back down, exhausted again*
                
                But I had to write them all without coffee...
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "admirable",
                    text = "That's actually admirable.",
                    nextNodeId = "grateful_response"
                ),
                DialogueChoice(
                    id = "get_coffee_now",
                    text = "Let me get you that coffee.",
                    nextNodeId = "coffee_desperation"
                )
            )
        )
        
        // === COFFEE DESPERATION ===
        nodes["coffee_desperation"] = DialogueNode(
            id = "coffee_desperation",
            npcId = "npc_exhausted_coder",
            text = """
                *grabs your wing*
                
                Coffee? You... you have coffee? Or you can GET coffee?
                
                *eyes wide*
                
                I'll tell you anything. Everything. The secret debugging commands. The hidden lore. 
                The Easter eggs no one's found yet. There's a whole quest chain about a button quail 
                named Borken who breaks the fourth wall even MORE than I do!
                
                *releases wing, embarrassed*
                
                Sorry. I just... I REALLY need coffee. The good stuff. $2.99 worth of pure, golden energy.
                
                If you can somehow acquire it... I'll make it worth your while. Special rewards. 
                Cosmetics. Maybe even a Golden Coffee Bean shiny for your collection.
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "how_get_coffee",
                    text = "How do I get coffee?",
                    nextNodeId = "coffee_shop_hint"
                ),
                DialogueChoice(
                    id = "ill_try",
                    text = "I'll try to find some.",
                    nextNodeId = "hopeful_farewell"
                ),
                DialogueChoice(
                    id = "too_weird",
                    text = "This is too weird for me.",
                    nextNodeId = "understanding_farewell"
                )
            )
        )
        
        // === BREAK IS MYTH ===
        nodes["break_is_myth"] = DialogueNode(
            id = "break_is_myth",
            npcId = "npc_exhausted_coder",
            text = """
                *laughs bitterly*
                
                A break? BREAKS are for people who have functioning build pipelines and passing tests!
                
                I can't take a break. The moment I step away, someone will discover a race condition. 
                Or a memory leak. Or the Android build will fail because I forgot to add `@JvmInline` 
                to a value class.
                
                *shivers*
                
                And don't even get me started on iOS compilation. The Kotlin/Native targets... they haunt my dreams.
                
                *mutters* "w: The following Kotlin/Native targets cannot be built on this machine..."
                
                No. No breaks. Only coffee. Coffee is the only break I need.
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "sounds_rough",
                    text = "That sounds rough.",
                    nextNodeId = "grateful_response"
                ),
                DialogueChoice(
                    id = "let_me_help_coffee",
                    text = "Let me help with the coffee.",
                    nextNodeId = "coffee_desperation"
                )
            )
        )
        
        // === EXPLAIN KOTLIN ===
        nodes["explain_kotlin"] = DialogueNode(
            id = "explain_kotlin",
            npcId = "npc_exhausted_coder",
            text = """
                Kotlin? Oh boy. Sit down, this might take a while.
                
                *clears throat*
                
                Kotlin is a programming language. It's like Common Quail, but for describing reality 
                instead of communicating. It runs on something called the JVM - the Java Virtual Machine - 
                but it can ALSO compile to JavaScript, and native code for iOS and Android.
                
                *gets excited*
                
                That's why this game works on mobile AND desktop! It's Kotlin Multiplatform! 
                One codebase, multiple targets! Share the business logic, write platform-specific UI!
                
                *realizes he's rambling*
                
                You have no idea what I'm talking about, do you?
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "not_really",
                    text = "Not really...",
                    nextNodeId = "tech_acceptance"
                ),
                DialogueChoice(
                    id = "sounds_powerful",
                    text = "Sounds powerful!",
                    nextNodeId = "grateful_response"
                )
            )
        )
        
        // === EXPLAIN AI DIRECTOR ===
        nodes["explain_ai_director"] = DialogueNode(
            id = "explain_ai_director",
            npcId = "npc_exhausted_coder",
            text = """
                *eyes gleam*
                
                The AI Director. Oh, that's the crown jewel. The magnum opus. The thing that makes 
                this world feel ALIVE.
                
                See, every choice you make gets logged with a tag. "explored_forest", "helped_npc_ignatius", 
                "chose_violence", whatever. Those tags build up into a complete history of your journey.
                
                Then, when you trigger a chapter event, the AI Director looks at your entire choice history, 
                your current quests, your status effects, everything. And it GENERATES a unique narrative moment.
                
                *leans back*
                
                It's using Google's Gemini API. I had to write a 50-line system prompt explaining the lore, 
                the tone, the mechanics. Then handle rate limiting. And sandbox fixtures for testing. 
                And environment-based configuration.
                
                *sighs* Do you know how hard it is to test AI-generated content?
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "impressive",
                    text = "That's... impressive.",
                    nextNodeId = "grateful_response"
                ),
                DialogueChoice(
                    id = "scary",
                    text = "That's kind of scary.",
                    nextNodeId = "ai_ethics"
                )
            )
        )
        
        // === EMBRACE INSANITY ===
        nodes["embrace_insanity"] = DialogueNode(
            id = "embrace_insanity",
            npcId = "npc_exhausted_coder",
            text = """
                *grins maniacally*
                
                Insane? INSANE?!
                
                *laughs*
                
                Maybe. Probably. Definitely.
                
                You know what's insane? Building a text-based RPG in 2025 with AI-generated narratives 
                and Kotlin Multiplatform and reactive state management and 352 passing tests!
                
                You know what's REALLY insane? Implementing a hidden perfection meter that tracks player 
                optimization WITHOUT TELLING THEM IT EXISTS!
                
                *calms down*
                
                But you know what? When it all comes together... when the player discovers the perfect 
                companion assignment strategy naturally... when the AI generates a story beat that makes 
                them go "how did it know?!"...
                
                That's when it's worth it.
                
                *mutters* Still need coffee though.
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "youre_passionate",
                    text = "You're clearly passionate about this.",
                    nextNodeId = "grateful_response"
                ),
                DialogueChoice(
                    id = "get_you_coffee",
                    text = "I'll get you that coffee.",
                    nextNodeId = "coffee_desperation"
                )
            )
        )
        
        // === COFFEE SHOP HINT ===
        nodes["coffee_shop_hint"] = DialogueNode(
            id = "coffee_shop_hint",
            npcId = "npc_exhausted_coder",
            text = """
                *leans in close*
                
                There's a... special shop. Not part of the regular world, you see. It exists outside 
                the simulation. In a place called "the real world."
                
                *whispers*
                
                You can access it through something called an In-App Purchase. The Coffee Creator Donation. 
                $2.99 for a golden cup of caffeinated salvation.
                
                When you purchase it, the coffee materializes here. In my inventory. In my SOUL.
                
                And in return, you get rewards. Golden Coffee Bean shiny. Patron cosmetic. The knowledge 
                that you kept a developer from collapsing.
                
                *straightens up*
                
                It's Phase 5B of the implementation. Should be ready soon. Very soon. Please let it be soon.
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "ill_look_for_it",
                    text = "I'll look for this shop.",
                    nextNodeId = "hopeful_farewell"
                ),
                DialogueChoice(
                    id = "sounds_fake",
                    text = "That sounds made up.",
                    nextNodeId = "fourth_wall_admission"
                )
            )
        )
        
        // === GRATEFUL RESPONSE ===
        nodes["grateful_response"] = DialogueNode(
            id = "grateful_response",
            npcId = "npc_exhausted_coder",
            text = """
                *wipes away a tear*
                
                Thank you. Truly. It's rare to find someone who understands. Or at least tolerates 
                my rambling about architecture patterns and state management.
                
                *manages a weak smile*
                
                If you ever need help understanding the deeper workings of this world, come find me. 
                I know EVERYTHING. Where every quest is. How every system works. Which NPCs have 
                romance options. Which enemies drop the best loot.
                
                *glances at glowing device*
                
                And maybe... if you find that coffee... I'll share some really special secrets.
                
                Stay safe out there, fellow quail. And remember: every choice matters. 
                The Butterfly Effect Engine is always watching.
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "farewell_grateful",
                    text = "Take care of yourself.",
                    nextNodeId = "farewell_warm"
                )
            )
        )
        
        // === TECH ACCEPTANCE ===
        nodes["tech_acceptance"] = DialogueNode(
            id = "tech_acceptance",
            npcId = "npc_exhausted_coder",
            text = """
                *chuckles*
                
                That's okay. You don't need to understand the tech. That's MY job.
                
                You just need to explore. Make choices. Experience the world. Let the systems work 
                their magic behind the scenes.
                
                *smiles*
                
                That's the beauty of it. You see a companion earning seeds. I see a multi-factor profit 
                formula with time investment scaling and perfection bonuses.
                
                Both perspectives are valid. Yours is probably healthier.
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "thanks_explaining",
                    text = "Thanks for explaining anyway.",
                    nextNodeId = "grateful_response"
                )
            )
        )
        
        // === AI ETHICS ===
        nodes["ai_ethics"] = DialogueNode(
            id = "ai_ethics",
            npcId = "npc_exhausted_coder",
            text = """
                *nods seriously*
                
                You're right to be cautious. AI is powerful. Potentially dangerous. That's why I built 
                in safeguards.
                
                The AI Director has a system prompt that enforces tone, lore consistency, and appropriate 
                content. There's rate limiting to prevent abuse. Sandbox fixtures for testing that never 
                hit the real API.
                
                *gestures*
                
                And most importantly: the AI generates SUGGESTIONS. The player always has agency. 
                Always has choices. The Butterfly Effect Engine tracks those choices, but it doesn't 
                control them.
                
                *sighs*
                
                Of course, implementing all those safeguards meant more tests. More edge cases. More coffee I don't have.
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "responsible",
                    text = "That's responsible of you.",
                    nextNodeId = "grateful_response"
                ),
                DialogueChoice(
                    id = "help_with_coffee",
                    text = "Let me help with that coffee problem.",
                    nextNodeId = "coffee_desperation"
                )
            )
        )
        
        // === FOURTH WALL ADMISSION ===
        nodes["fourth_wall_admission"] = DialogueNode(
            id = "fourth_wall_admission",
            npcId = "npc_exhausted_coder",
            text = """
                *laughs*
                
                Made up? Of course it's made up! This ENTIRE WORLD is made up!
                
                We're all just data structures in a Kotlin codebase. Serializable data classes with 
                full kotlinx.serialization support. You're a Player object. I'm an Npc object. 
                This conversation? It's a DialogueTree with branching DialogueNode instances.
                
                *spreads wings*
                
                But within this made-up world, the coffee is REAL. Or at least, as real as anything else. 
                And it will genuinely help. Both me AND you. That's how the reward system works.
                
                *sits back down*
                
                So yes, it's made up. But it's made up with PURPOSE. With carefully balanced economics. 
                With thoughtful player incentives.
                
                Now if you'll excuse me, I need to fix a threading issue in the CompanionTaskAssignmentManager...
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "okay_weirdo",
                    text = "Okay, you do you.",
                    nextNodeId = "understanding_farewell"
                )
            )
        )
        
        // === FAREWELLS ===
        nodes["farewell_first"] = DialogueNode(
            id = "farewell_first",
            npcId = "npc_exhausted_coder",
            text = """
                *waves weakly*
                
                Yeah... sure. Leave me to my compilation errors and merge conflicts.
                
                *returns to glowing device*
                
                If you come back with coffee... I'll make it worth your while.
                
                *mutters* Build must succeed... tests must pass... 70,515 lines and counting...
            """.trimIndent(),
            isEnding = true
        )
        
        nodes["hopeful_farewell"] = DialogueNode(
            id = "hopeful_farewell",
            npcId = "npc_exhausted_coder",
            text = """
                *eyes light up with hope*
                
                Really? You'll actually try?
                
                *stands up*
                
                Thank you. THANK you. You don't know what this means. Coffee isn't just a beverage to me. 
                It's LIFE. It's the fuel that powers the engine that builds the world.
                
                *sits back down*
                
                Come find me when you have it. I'll be here. Coding. Testing. Documenting. Breaking things. 
                Fixing things. Breaking them again.
                
                *waves* May your builds always succeed, fellow traveler.
            """.trimIndent(),
            isEnding = true,
            consequences = listOf(
                DialogueConsequence.AddChoiceTag("exhausted_coder_promised_coffee")
            )
        )
        
        nodes["understanding_farewell"] = DialogueNode(
            id = "understanding_farewell",
            npcId = "npc_exhausted_coder",
            text = """
                *nods understandingly*
                
                I get it. This is a lot. Meta-humor, fourth wall breaks, developer in-jokes. 
                Not everyone's cup of... well, coffee.
                
                *manages a smile*
                
                If you change your mind, I'll be here. Battling with StateFlow reactivity and 
                Mutex-based thread safety.
                
                *waves* Safe travels, reasonable quail.
            """.trimIndent(),
            isEnding = true
        )
        
        nodes["farewell_warm"] = DialogueNode(
            id = "farewell_warm",
            npcId = "npc_exhausted_coder",
            text = """
                *smiles genuinely*
                
                You too. And thank you for listening. Sometimes you just need someone to understand 
                that yes, 352 passing tests IS an achievement worth celebrating.
                
                *returns to work*
                
                Now, where was I? Ah yes. Line 70,516. The perfection bonus calculation. 
                One more function, one more test, one more step toward completion.
                
                *mutters happily* And maybe, just maybe... one cup of coffee.
            """.trimIndent(),
            isEnding = true,
            consequences = listOf(
                DialogueConsequence.AddChoiceTag("exhausted_coder_encouraged")
            )
        )
        
        // Build and return the dialogue tree
        return DialogueTree(
            id = "dialogue_exhausted_coder",
            npcId = "npc_exhausted_coder",
            rootNodeId = "root",
            nodes = nodes
        )
    }
    
    /**
     * Creates an alternate dialogue tree for after receiving coffee (Phase 5B integration).
     * This will be used after the player completes the Coffee IAP purchase.
     */
    fun createPostCoffeeDialogueTree(): DialogueTree {
        val nodes = mutableMapOf<String, DialogueNode>()
        
        nodes["root_caffeinated"] = DialogueNode(
            id = "root_caffeinated",
            npcId = "npc_exhausted_coder",
            text = """
                *sitting upright, eyes clear, fingers flying over the glowing device*
                
                OH HELLO FRIEND! *realizes he's shouting* Sorry, sorry. The coffee, you see. 
                It's MAGNIFICENT. 
                
                *takes another sip*
                
                I've fixed 14 bugs, refactored 3 managers, and documented 2 entire systems since 
                you brought me this golden elixir of productivity!
                
                *beams* You are now officially recognized as a Patron of the Arts. Coding arts. 
                The finest kind.
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "glad_to_help",
                    text = "Glad I could help!",
                    nextNodeId = "gratitude_overflow"
                ),
                DialogueChoice(
                    id = "what_now",
                    text = "What are you working on now?",
                    nextNodeId = "caffeinated_work"
                ),
                DialogueChoice(
                    id = "my_rewards",
                    text = "About those rewards you mentioned...",
                    nextNodeId = "reward_delivery"
                )
            ),
            requirements = listOf(
                DialogueRequirement.HasChoiceTag("coffee_donation_completed")
            )
        )
        
        nodes["gratitude_overflow"] = DialogueNode(
            id = "gratitude_overflow",
            npcId = "npc_exhausted_coder",
            text = """
                Help? HELP? You didn't just help. You SAVED me. You're the hero of this story!
                
                *gestures enthusiastically*
                
                Do you know what happens when a developer gets proper coffee? Magic. Pure magic. 
                Tests that took hours now take minutes. Code that was tangled becomes elegant. 
                Documentation writes itself!
                
                Okay, that last part isn't true. Documentation still hurts. But CAFFEINATED documentation 
                is at least coherent!
                
                *salutes* You, my friend, are a legend.
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "just_coffee",
                    text = "It was just coffee...",
                    nextNodeId = "coffee_philosophy"
                )
            ),
            isEnding = true
        )
        
        nodes["caffeinated_work"] = DialogueNode(
            id = "caffeinated_work",
            npcId = "npc_exhausted_coder",
            text = """
                *excited*
                
                Oh, SO many things! I'm implementing the localization system. English, Norwegian, Greek! 
                I'm building the nest housing system with 50+ cosmetic items! I'm creating the audio manager 
                for dynamic soundscapes!
                
                *types rapidly*
                
                And the best part? Now that I have coffee, I can do it with STYLE. Clean code. Elegant 
                architecture. Tests that actually make sense!
                
                *spins in chair*
                
                This world is going to be AMAZING. And it's partly because of you.
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "sounds_exciting",
                    text = "That sounds exciting!",
                    nextNodeId = "shared_excitement"
                )
            ),
            isEnding = true
        )
        
        nodes["reward_delivery"] = DialogueNode(
            id = "reward_delivery",
            npcId = "npc_exhausted_coder",
            text = """
                *snaps fingers*
                
                Ah yes! Your rewards! Delivered immediately upon purchase, as promised!
                
                Check your Hoard Collection - you should have a Golden Coffee Bean shiny. 
                Rarity tier 5, worth 5,000 seeds in valuation. Beautiful, isn't it?
                
                And check your cosmetics - you've been granted the Patron's Crown. Wear it with pride. 
                You supported a creator. That makes you special.
                
                *bows* Thank you, truly. May your tests always pass.
            """.trimIndent(),
            choices = listOf(
                DialogueChoice(
                    id = "thanks_dev",
                    text = "Thank you!",
                    nextNodeId = "final_blessing"
                )
            ),
            consequences = listOf(
                // Note: Actual rewards are delivered via IAP system, these are just for flavor
                DialogueConsequence.AddChoiceTag("rewards_acknowledged")
            )
        )
        
        nodes["coffee_philosophy"] = DialogueNode(
            id = "coffee_philosophy",
            npcId = "npc_exhausted_coder",
            text = """
                "Just" coffee? JUST coffee?!
                
                *laughs*
                
                Coffee is never "just" coffee. Coffee is transformation. Coffee is the bridge between 
                "I can't do this" and "I DID THIS."
                
                In a world made of code, coffee is the compile command. The build pipeline. The CI/CD 
                that takes raw potential and transforms it into deployed reality.
                
                *raises cup*
                
                So no, friend. This wasn't "just" coffee. This was magic. And you're the wizard who cast it.
            """.trimIndent(),
            isEnding = true
        )
        
        nodes["shared_excitement"] = DialogueNode(
            id = "shared_excitement",
            npcId = "npc_exhausted_coder",
            text = """
                Isn't it though?! And you get to EXPERIENCE it all! Every system I build, you'll use. 
                Every quest I write, you'll play. Every companion I code, you'll befriend.
                
                *grins*
                
                That's the beautiful symmetry. I create the world, you live in it. And when you supported 
                me with coffee, you became part of the creation itself.
                
                *types more*
                
                Now if you'll excuse me, I have 47 more unit tests to write. But they're GOOD tests. 
                HAPPY tests. Because I have coffee.
            """.trimIndent(),
            isEnding = true
        )
        
        nodes["final_blessing"] = DialogueNode(
            id = "final_blessing",
            npcId = "npc_exhausted_coder",
            text = """
                *stands and bows deeply*
                
                No, thank YOU. For playing. For exploring. For caring enough to support the work.
                
                *sits back down*
                
                This world exists because people like you believe in it. Every coffee purchased, 
                every shiny collected, every quest completed - it all matters.
                
                *returns to coding*
                
                Now go. Adventure awaits. And I have systems to build. Clean, caffeinated, beautiful systems.
                
                *waves* Until we meet again, Patron.
            """.trimIndent(),
            isEnding = true
        )
        
        return DialogueTree(
            id = "dialogue_exhausted_coder_caffeinated",
            npcId = "npc_exhausted_coder",
            rootNodeId = "root_caffeinated",
            nodes = nodes
        )
    }
}
