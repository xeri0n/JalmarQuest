package com.jalmarquest.core.state.narrative

/**
 * Alpha 2.2: Dialogue Variant Manager
 * 
 * Manages filtered and unfiltered dialogue variants for NPCs.
 * When No Filter Mode is enabled, returns edgier/darker dialogue.
 * When disabled, returns family-friendly dialogue.
 * 
 * Usage:
 * ```
 * val dialogue = dialogueVariantManager.getDialogue("npc_borken", DialogueType.GREETING)
 * ```
 */
class DialogueVariantManager(
    private val contentFilterManager: ContentFilterManager
) {
    private val dialogueVariants = mutableMapOf<String, Map<DialogueType, DialoguePair>>()
    
    init {
        registerDefaultDialogue()
    }
    
    /**
     * Get dialogue for an NPC, automatically selecting filtered or unfiltered version.
     * @param npcId The NPC identifier (e.g., "npc_borken")
     * @param type The type of dialogue (greeting, quest, random, etc.)
     * @return The appropriate dialogue text, or null if not registered
     */
    fun getDialogue(npcId: String, type: DialogueType): String? {
        val npcDialogue = dialogueVariants[npcId] ?: return null
        val dialoguePair = npcDialogue[type] ?: return null
        
        return if (contentFilterManager.isNoFilterModeEnabled()) {
            dialoguePair.unfiltered
        } else {
            dialoguePair.filtered
        }
    }
    
    /**
     * Register dialogue variants for an NPC.
     */
    fun registerDialogue(npcId: String, type: DialogueType, filtered: String, unfiltered: String) {
        val existing = dialogueVariants[npcId]?.toMutableMap() ?: mutableMapOf()
        existing[type] = DialoguePair(filtered, unfiltered)
        dialogueVariants[npcId] = existing.toMap()
    }
    
    /**
     * Check if an NPC has any registered dialogue variants.
     */
    fun hasVariants(npcId: String): Boolean {
        return dialogueVariants.containsKey(npcId)
    }
    
    private fun registerDefaultDialogue() {
        // ===== BORKEN DIALOGUE VARIANTS =====
        registerBorkenDialogue()
        
        // ===== PACK RAT DIALOGUE VARIANTS =====
        registerPackRatDialogue()
        
        // ===== WORRIED WICKER DIALOGUE VARIANTS =====
        registerWorriedWickerDialogue()
        
        // ===== Alpha 2.2: EXHAUSTED CODER DIALOGUE VARIANTS =====
        registerExhaustedCoderDialogue()
        registerExhaustedCoderCoffeeDialogue()
    }
    
    private fun registerBorkenDialogue() {
        val npcId = "npc_borken"
        
        // Greeting
        registerDialogue(
            npcId, DialogueType.GREETING,
            filtered = "Oh, another adventurer! Welcome to the tavern. I'm Borken. I like sticks and thinking about things.",
            unfiltered = "*brandishes stick* Oh look, another optimist. I'm Borken. This is my stick. We're both coping mechanisms."
        )
        
        // First meeting (alternative)
        registerDialogue(
            npcId, DialogueType.FIRST_MEETING,
            filtered = "Hello there! I'm Borken. I know I seem a bit unusual, but I promise I'm mostly harmless. Want to chat?",
            unfiltered = "Ah, fresh blood. I'm Borken. Fair warning: I'm what happens when a button quail reads too much philosophy and stops sleeping. Still interested in talking?"
        )
        
        // Random dialogue 1
        registerDialogue(
            npcId, DialogueType.RANDOM_1,
            filtered = "You know, I've been thinking about cloud shapes. They're quite fascinating if you really pay attention!",
            unfiltered = "You know what's funny? We spend our entire lives fleeing from death, but statistically, we're all losing. Anyway, how's your day?"
        )
        
        // Random dialogue 2
        registerDialogue(
            npcId, DialogueType.RANDOM_2,
            filtered = "My stick and I have been on many adventures. It's a good companion—never argues, always reliable!",
            unfiltered = "My stick represents the illusion of control in a chaotic universe. Also, it's pointy. Dual purpose."
        )
        
        // Random dialogue 3
        registerDialogue(
            npcId, DialogueType.RANDOM_3,
            filtered = "Sometimes I wonder if we're all just doing our best in a complicated world. Then I remember I have a stick, so everything's okay!",
            unfiltered = "Sometimes I wonder if existential dread is just spicy anxiety. Then I remember we're all prey animals in denial, so... yeah."
        )
        
        // Quest-related
        registerDialogue(
            npcId, DialogueType.QUEST_OFFER,
            filtered = "I could use some help with something! It's probably going to be strange, fair warning. Interested?",
            unfiltered = "Want to help me with something profoundly pointless yet oddly meaningful? It's the quail way—absurdist errands masked as purpose."
        )
        
        // Shop interaction
        registerDialogue(
            npcId, DialogueType.SHOP_GREETING,
            filtered = "Welcome to my... collection. I trade things. Seeds, mostly. The economy is weird but we make it work!",
            unfiltered = "Ah yes, capitalism among prey animals. We trade shiny pebbles while predators plan our demise. Want to buy something anyway?"
        )
        
        // Farewell
        registerDialogue(
            npcId, DialogueType.FAREWELL,
            filtered = "Take care out there! Stay safe and remember: you're braver than you think!",
            unfiltered = "Good luck surviving. Statistically, something will eat you eventually, but maybe not today. That's optimism!"
        )
        
        // Gift received
        registerDialogue(
            npcId, DialogueType.GIFT_RECEIVED,
            filtered = "For me? How thoughtful! I really appreciate this. Thank you!",
            unfiltered = "You're... giving me things? In this economy? Either you're genuinely kind or setting up an elaborate prank. I choose to believe the former."
        )
        
        // Low affinity
        registerDialogue(
            npcId, DialogueType.LOW_AFFINITY,
            filtered = "We don't know each other well yet. That's okay! Friendships take time.",
            unfiltered = "We're strangers engaging in mandatory social rituals. Isn't civilization fun? Anyway, what do you want?"
        )
        
        // High affinity
        registerDialogue(
            npcId, DialogueType.HIGH_AFFINITY,
            filtered = "You're one of my favorite people! Thanks for being a good friend.",
            unfiltered = "You get me. In a world of relentless horror, you're one of the few who acknowledges the absurdity. That's... actually really valuable."
        )
    }
    
    private fun registerPackRatDialogue() {
        val npcId = "npc_pack_rat"
        
        registerDialogue(
            npcId, DialogueType.GREETING,
            filtered = "Welcome, welcome! So many treasures, so little time! See anything you like?",
            unfiltered = "Ah, another potential victim of my 'fair' pricing! Welcome to the hoard. Your seeds are mine, friend."
        )
        
        registerDialogue(
            npcId, DialogueType.SHOP_GREETING,
            filtered = "Every item has a story! And a price! Mostly the price part, honestly.",
            unfiltered = "Everything's overpriced because I can. Supply, demand, and the fact that I'm the only merchant for miles. Economics!"
        )
        
        registerDialogue(
            npcId, DialogueType.RANDOM_1,
            filtered = "I've been collecting since I was young. Each treasure is special in its own way!",
            unfiltered = "I'm a hoarder with a business license. Society calls it 'entrepreneurship.' I call it 'organized dysfunction.'"
        )
        
        registerDialogue(
            npcId, DialogueType.RANDOM_2,
            filtered = "Some might say I have too many things, but I disagree! You can never have too many treasures!",
            unfiltered = "My collection isn't a cry for help, it's a sophisticated coping mechanism. There's a difference. Probably."
        )
        
        registerDialogue(
            npcId, DialogueType.FAREWELL,
            filtered = "Come back soon! New items arrive daily!",
            unfiltered = "Come back when you have more seeds to waste on my overpriced junk. I mean... treasures."
        )
        
        registerDialogue(
            npcId, DialogueType.LOW_AFFINITY,
            filtered = "Browser or buyer? Either way, welcome!",
            unfiltered = "You're not spending enough seeds. I'm judging you. But politely."
        )
        
        registerDialogue(
            npcId, DialogueType.HIGH_AFFINITY,
            filtered = "Ah, my best customer! What can I get for you today?",
            unfiltered = "My favorite financially irresponsible friend! Your poor budgeting skills fund my retirement. Bless you."
        )
    }
    
    private fun registerWorriedWickerDialogue() {
        val npcId = "npc_worried_wicker"
        
        registerDialogue(
            npcId, DialogueType.GREETING,
            filtered = "Oh! Hello. I'm a bit anxious today, but I'm managing. How are you?",
            unfiltered = "Oh good, another social interaction I'm underprepared for. Hello. I'm Wicker. I worry about literally everything."
        )
        
        registerDialogue(
            npcId, DialogueType.RANDOM_1,
            filtered = "I've been trying to stay calm, but there's just so much to think about!",
            unfiltered = "My anxiety has anxiety. It's recursive. I'm basically a biological panic loop with feathers."
        )
        
        registerDialogue(
            npcId, DialogueType.RANDOM_2,
            filtered = "What if something goes wrong? Actually, what if EVERYTHING goes wrong? Sorry, I'm just thinking out loud.",
            unfiltered = "I've catastrophized seventeen different scenarios before breakfast. It's not healthy, but it's efficient."
        )
        
        registerDialogue(
            npcId, DialogueType.RANDOM_3,
            filtered = "Deep breaths. It'll be okay. Probably. Maybe. I hope.",
            unfiltered = "Narrator: It was, in fact, not going to be okay. But Wicker smiled anyway because society demands performance."
        )
        
        registerDialogue(
            npcId, DialogueType.QUEST_OFFER,
            filtered = "I need help with something, but I'm worried about asking. Is that okay?",
            unfiltered = "I need help but I'm terrified you'll say no and then I'll spiral into self-doubt for three days. Want to help anyway?"
        )
        
        registerDialogue(
            npcId, DialogueType.FAREWELL,
            filtered = "Take care! Try not to worry too much, okay?",
            unfiltered = "Be safe out there. The world is terrifying and we're all improvising. Good luck!"
        )
        
        registerDialogue(
            npcId, DialogueType.LOW_AFFINITY,
            filtered = "I don't know you well yet. That's fine! No pressure!",
            unfiltered = "We're strangers and I'm already worried I've made a bad impression. Have I? Please be honest. Actually, don't."
        )
        
        registerDialogue(
            npcId, DialogueType.HIGH_AFFINITY,
            filtered = "I'm so glad we're friends. You make me feel a little less worried.",
            unfiltered = "You're one of the few people I don't immediately catastrophize around. That's... that's huge, actually. Thank you."
        )
        
        registerDialogue(
            npcId, DialogueType.GIFT_RECEIVED,
            filtered = "For me? You're so kind! I don't know what to say!",
            unfiltered = "A gift? For ME? Now I'm worried I didn't get YOU anything. Should I have? Is this a test? I'm spiraling. Thank you."
        )
    }
    
    // Alpha 2.2: Exhausted Coder - Meta-humor dev insert
    private fun registerExhaustedCoderDialogue() {
        val npcId = "npc_exhausted_coder"
        
        // Greeting
        registerDialogue(
            npcId, DialogueType.GREETING,
            filtered = "*typing frantically* Oh! Hello. I'm working on... *gestures vaguely at air* ...all of this. The world. Your adventures. Everything. Coffee helps.",
            unfiltered = "*dead eyes* Welcome to the nightmare. I'm the dev. This is line 47,832. I've forgotten what sunlight looks like. Send coffee or debug logs, I don't care anymore."
        )
        
        // First meeting
        registerDialogue(
            npcId, DialogueType.FIRST_MEETING,
            filtered = "*looks up from laptop* A new player! I mean... adventurer! Welcome to the game—er, the world! Everything is working as intended. Probably.",
            unfiltered = "*thousand-yard stare* Oh good, fresh code to break. I'm the one writing your reality. Yes, I'm aware of the bugs. No, I don't know why the physics do that. It's a feature now."
        )
        
        // Random dialogue 1
        registerDialogue(
            npcId, DialogueType.RANDOM_1,
            filtered = "*muttering* If I adjust the spawn rate by 0.3, carry the two, multiply by difficulty... Wait, what were you saying?",
            unfiltered = "*staring at compile errors* 'Expected semicolon on line 3847.' Which line 3847?! There are SIX FILES with 3847 lines! This is my personal hell."
        )
        
        // Random dialogue 2
        registerDialogue(
            npcId, DialogueType.RANDOM_2,
            filtered = "Did you know this entire world runs on something called 'Kotlin'? It's quite elegant, really! Though I may be biased.",
            unfiltered = "You're made of data classes and StateFlow. Your choices? Serialized JSON. Your consciousness? A coroutine. Does that bother you? It bothers me at 3 AM."
        )
        
        // Random dialogue 3
        registerDialogue(
            npcId, DialogueType.RANDOM_3,
            filtered = "Sometimes I dream about balance patches and optimization passes. Is that normal? Never mind, don't answer that.",
            unfiltered = "*maniacal laugh* I spent 8 hours debugging why NPCs walked through walls. It was a MINUS sign. ONE CHARACTER. I hate this. I love this. I can't stop."
        )
        
        // Random dialogue 4
        registerDialogue(
            npcId, DialogueType.RANDOM_4,
            filtered = "Every player choice gets logged and analyzed. It's fascinating! Data-driven narrative design is the future!",
            unfiltered = "Your every action is logged. Choice tags, timestamps, analytics. Big Brother? More like Big Quail. I see EVERYTHING. Also, why did you click that rock 47 times?"
        )
        
        // Random dialogue 5
        registerDialogue(
            npcId, DialogueType.RANDOM_5,
            filtered = "The AI Director is my proudest creation! It adapts to YOUR playstyle. Clever, right?",
            unfiltered = "The AI Director watches you. Learns from you. Judges you. It's like a god, except made of if-else statements and prayer. Mostly prayer."
        )
        
        // Gift received (BEFORE coffee donation)
        registerDialogue(
            npcId, DialogueType.GIFT_RECEIVED,
            filtered = "Oh! That's very kind of you. Thank you! Though... if you happen to find any coffee beans out there... just saying.",
            unfiltered = "A gift. For me. The NPC who generates NPCs. The meta is delicious. But you know what would be MORE delicious? Coffee. I'm begging you."
        )
        
        // Low affinity
        registerDialogue(
            npcId, DialogueType.LOW_AFFINITY,
            filtered = "We don't know each other yet. That's okay! I'm usually too busy coding anyway.",
            unfiltered = "You're a stranger. I'm a sleep-deprived code goblin. This relationship has 'professional boundaries' written all over it."
        )
        
        // High affinity (BEFORE coffee donation)
        registerDialogue(
            npcId, DialogueType.HIGH_AFFINITY,
            filtered = "You're one of the good ones! Thanks for being patient with all the... quirks... of this world.",
            unfiltered = "You keep coming back despite the bugs. You're either very patient or very forgiving. Either way, you're my favorite. Don't tell the others."
        )
        
        // Farewell
        registerDialogue(
            npcId, DialogueType.FAREWELL,
            filtered = "Good luck out there! Remember: save often, the world is unpredictable!",
            unfiltered = "Go forth and break things. I'll be here, fixing them at 2 AM while crying into instant ramen. It's fine. This is fine. Everything is fine."
        )
    }
    
    /**
     * Registers post-coffee donation dialogue for The Exhausted Coder.
     * Alpha 2.2: Special dialogue that appears after purchasing Creator Coffee IAP.
     * Emotionally charged variants expressing genuine gratitude and caffeinated energy.
     */
    private fun registerExhaustedCoderCoffeeDialogue() {
        val npcId = "npc_exhausted_coder"
        
        // Coffee Gratitude - First interaction after purchase
        registerDialogue(
            npcId, DialogueType.COFFEE_GRATITUDE,
            filtered = "*looks up with glistening eyes* You... you bought me coffee? I don't know what to say. This means more than you know. Thank you, truly.",
            unfiltered = "*genuine tears streaming down face* You bought me ACTUAL COFFEE? Not the burnt break room sludge? I... *voice breaks* This is the nicest thing anyone's done for me since launch day. I can face the merge conflicts now. I can BEAT the technical debt. Thank you, you beautiful, generous soul."
        )
        
        // Coffee Energized - Replaces low affinity when caffeinated
        registerDialogue(
            npcId, DialogueType.COFFEE_ENERGIZED,
            filtered = "*vibrating with energy* I feel ALIVE! The code is flowing, the bugs are fleeing! I've refactored THREE modules today! Is this what productivity feels like?!",
            unfiltered = "*eyes wide, typing at inhuman speed* I'VE FIXED 47 BUGS IN 3 HOURS. THE COFFEE HAS UNLOCKED MY TRUE POTENTIAL. IS THIS WHAT SLEEP FEELS LIKE? I DON'T NEED SLEEP. I NEED MORE FEATURES. THE BACKLOG TREMBLES BEFORE ME. *maniacal laughter* The stack overflow gods FEAR ME NOW."
        )
        
        // Random Coffee 1 - Productivity jokes
        registerDialogue(
            npcId, DialogueType.RANDOM_COFFEE_1,
            filtered = "*sipping contentedly* You know, with proper caffeine levels, my compile errors dropped by 60%. Correlation? Causation? Who cares, IT WORKS.",
            unfiltered = "*staring at screen with laser focus* The coffee lets me see the Matrix. Every semicolon. Every missing bracket. They reveal themselves to me now. I am become Debug, destroyer of bugs. Also I can taste colors now, is that normal?"
        )
        
        // Random Coffee 2 - Creator support meta-commentary
        registerDialogue(
            npcId, DialogueType.RANDOM_COFFEE_2,
            filtered = "Your support means I can work on features I actually care about, not just what pays the bills. Quality over quantity, you know?",
            unfiltered = "*philosophical tone* You've unlocked the secret ending to capitalism: voluntary support for creative work. It's like Patreon but with more bird puns. I'm using your coffee money to build features that make ME happy, which makes the game better, which makes YOU happy. It's a beautiful feedback loop of wholesome economic theory."
        )
        
        // Random Coffee 3 - Future features teasing
        registerDialogue(
            npcId, DialogueType.RANDOM_COFFEE_3,
            filtered = "*excited whisper* I've been working on something special. Can't say much, but... let's just say the next update will have 40% more bird chaos. Thanks to you!",
            unfiltered = "*leaning in conspiratorially* The coffee paid off my technical debt. I can finally build the ridiculous features I've been dreaming about. Quail mech suits? Sure. Musical numbers during boss fights? Why not. Existential dialogue about the nature of save files? ALREADY WRITING IT. You've unleashed a caffeinated monster of creativity."
        )
    }
}

/**
 * Types of dialogue that can have filtered/unfiltered variants.
 */
enum class DialogueType {
    GREETING,
    FIRST_MEETING,
    FAREWELL,
    QUEST_OFFER,
    QUEST_COMPLETE,
    SHOP_GREETING,
    SHOP_FAREWELL,
    GIFT_RECEIVED,
    GIFT_REJECTED,
    LOW_AFFINITY,
    HIGH_AFFINITY,
    RANDOM_1,
    RANDOM_2,
    RANDOM_3,
    RANDOM_4,
    RANDOM_5,
    COMBAT_START,
    COMBAT_WIN,
    COMBAT_LOSS,
    
    // Alpha 2.2: Post-coffee donation dialogue
    COFFEE_GRATITUDE,
    COFFEE_ENERGIZED,
    RANDOM_COFFEE_1,
    RANDOM_COFFEE_2,
    RANDOM_COFFEE_3
}

/**
 * Holds both filtered and unfiltered versions of dialogue.
 */
data class DialoguePair(
    val filtered: String,
    val unfiltered: String
)

