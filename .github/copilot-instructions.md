# JalmarQuest Development Guide

## Project Overview
**JalmarQuest** is a text-based RPG built with **Kotlin Multiplatform (KMP)**, targeting Android, iOS, and Desktop. The game features a "tiny hero, big world" narrative starring Jalmar the button quail, with a sophisticated **Butterfly Effect Engine** AI system that tracks all player choices for long-term narrative consequences.

## Architecture Patterns

### Multi-Module Structure
This is a **Gradle multi-module project** with strict separation of concerns:

```
core/                    # Shared business logic (KMP)
├── model/              # Data classes, all @Serializable
├── state/              # State managers (GameStateManager, QuestManager, etc.)
└── di/                 # Koin dependency injection setup

feature/                # Feature-specific state machines (KMP)
├── eventengine/        # Hybrid narrative engine (snippet + AI)
├── explore/            # Exploration encounters
├── hub/                # Hub location navigation
├── nest/               # Nest management
├── activities/         # Secondary activities
├── skills/             # Skills & crafting
└── systemic/           # Systemic interactions

ui/app/                 # Compose Multiplatform UI layer
app/                    # Platform launchers
├── android/
└── desktop/

backend/                # JVM-only backend services
├── aidirector/         # Gemini AI integration
└── database/           # PostgreSQL migrations
```

**Critical Rules**:
- All cross-platform logic goes in `commonMain` source sets
- Platform-specific code uses `expect/actual` pattern (e.g., `AuthTokenStorage`, `SpeechSynthesizer`)
- Feature modules depend on `core:model` and `core:state`, never on each other
- UI modules depend on feature controllers, never directly on state machines

### State Management: GameStateManager is King
**Every player state mutation** flows through `GameStateManager` in `core/state`:

```kotlin
class GameStateManager(
    initialPlayer: Player,
    private val accountManager: AccountManager?,
    private val timestampProvider: () -> Long
) {
    private val _playerState = MutableStateFlow(initialPlayer)
    val playerState: StateFlow<Player> = _playerState
    
    fun appendChoice(tagValue: String) { /* logs all choices */ }
    fun updateQuestLog(updater: (QuestLog) -> QuestLog) { /* ... */ }
    // ... other mutation methods
}
```

**Why this matters**:
- The Butterfly Effect Engine requires **complete choice history** for AI narrative generation
- Centralized mutations enable autosave, crash recovery, and analytics
- Thread-safe via `Mutex` (see `GameStateManagerConcurrencyTest` - 100 concurrent updates, zero data loss)

**Pattern to follow**: Feature state machines (e.g., `NestStateMachine`, `ExploreStateMachine`) manage local UI state via `MutableStateFlow`, but call `gameStateManager.appendChoice()` for persistent actions.

### Data Modeling
All state classes are **`@Serializable` data classes** using `kotlinx.serialization`:

```kotlin
@Serializable
data class Player(
    val id: String,
    val name: String,
    val choiceLog: ChoiceLog,
    val questLog: QuestLog,
    val inventory: Inventory,
    val statusEffects: StatusEffects,
    // ... ~20+ nested state objects
)
```

**Never use mutable state** in model classes - copy on modify for immutability.

## Feature Module Pattern
Every feature follows this state machine architecture (see `feature/nest` or `feature/explore`):

1. **Models.kt** - Data classes for state (`NestState`, `ExploreState`)
2. **StateMachine.kt** - Reactive state machine with `StateFlow<State>`
3. **Controller.kt** - CoroutineScope wrapper for UI interaction
4. **UI Section** - Compose UI in `ui/app/src/commonMain/kotlin/com/jalmarquest/ui/app/`

Example from `NestStateMachine`:
```kotlin
class NestStateMachine(
    initialState: NestState,
    private val config: NestConfig,
    private val gameStateManager: GameStateManager?
) {
    private val mutex = Mutex()
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<NestState> = _state.asStateFlow()
    
    suspend fun startUpgrade() {
        mutex.withLock {
            // Mutate local state, log choice to GameStateManager
            gameStateManager?.appendChoice("nest_upgrade_${level}")
        }
    }
}
```

## AI Director Integration

### Environment-Based Configuration
AI Director supports **sandbox mode** (fixtures) and **live mode** (Gemini API):

```kotlin
val config = AiDirectorConfig.fromEnvironment()
val service = AiDirectorFactory.createService(config)
// Or use quick bootstrap:
val handles = AiDirectorDeployment.bootstrap()
```

Environment variables (`AI_DIRECTOR_MODE`, `GEMINI_API_KEY`, etc.) control behavior - see `README.md` table.

### Chapter Events
The `ChapterEventProvider` interface generates AI narrative moments:

```kotlin
fun interface ChapterEventProvider {
    fun generateChapterEvent(request: ChapterEventRequest): ChapterEventResponse
}
```

**How it works**:
1. `EventEngine.evaluateNextEncounter()` decides: snippet (static lore) vs chapter event (AI-generated)
2. If chapter event: builds `ChapterEventRequest` with `Player.choiceLog` + `questLog` + `statusEffects`
3. `GeminiPromptBuilder` constructs system prompt: "You are the AI Game Master for Jalmar Quest..."
4. Gemini returns `ChapterEventResponse` with `worldEventTitle`, `worldEventSummary`, and `snippets[]`
5. `ExploreStateMachine` presents to UI and applies consequences

**Critical**: All choice tags from snippets get logged via `gameStateManager.appendChoice()` to feed future AI prompts.

## Testing Philosophy

### Required Test Coverage
Every new system needs:
1. **Happy path** - Standard inputs work correctly
2. **Edge cases** - Empty inputs, zero quantities, missing data
3. **Concurrency** - If using `Mutex`, test race conditions
4. **Serialization** - Round-trip JSON serialization test

Example test structure:
```kotlin
class NestStateMachineTest {
    private lateinit var gameStateManager: GameStateManager
    private lateinit var machine: NestStateMachine
    private var currentTime = 0L
    
    @BeforeTest
    fun setup() {
        gameStateManager = GameStateManager(testPlayer()) { currentTime }
        machine = NestStateMachine(gameStateManager = gameStateManager)
    }
    
    @Test
    fun `upgrade logs choice tag`() = runTest {
        machine.startUpgrade()
        val choices = gameStateManager.playerState.value.choiceLog.entries
        assertTrue(choices.any { it.tag.value.startsWith("nest_upgrade_") })
    }
}
```

Place tests in `commonTest` source sets, use `kotlin.test` library.

## Build & Run

### Development Commands
```bash
# Desktop app (fastest iteration)
./gradlew :app:desktop:run

# Android app
./gradlew :app:android:installDebug

# Run all tests (206 tests as of Milestone 3)
./gradlew allTests

# Build all modules
./gradlew build
```

### Gradle Structure
- Version catalog: `gradle/libs.versions.toml` defines all dependency versions
- Typesafe project accessors: `projects.core.model` instead of `":core:model"`
- Kotlin 2.0.0, Compose 1.7.0, Coroutines 1.9.0

## Key Dependencies
- **kotlinx.serialization** - JSON serialization for all data classes
- **kotlinx.coroutines** - Async operations, `Flow` for reactive state
- **Koin** - Dependency injection (`core/di/CoreModule.kt`)
- **Compose Multiplatform** - UI framework
- **Moko Resources** - Localization (EN/NO strings in `ui/app/src/commonMain/moko-resources/`)

## Narrative Systems

### Quest System
Quests defined in `QuestCatalog.kt` (46+ quests) with objectives, rewards, and prerequisites:

```kotlin
Quest(
    questId = QuestId("quest_founding_buttonburgh"),
    objectives = listOf(/* REACH_LOCATION, TALK_TO_NPC, COLLECT_ITEMS */),
    rewards = listOf(/* XP, SEEDS, LORE_UNLOCK, FACTION_REPUTATION */),
    requirements = listOf(/* PrerequisiteQuest, MinimumLevel, MinimumSkill */)
)
```

**State Tracking**: `QuestManager` updates `Player.questLog`, checks conditions via `canCompleteQuest()`.

### Lore Snippets
Static narrative events in `LoreSnippetRepository` with:
- `eventText` - Story text
- `choiceOptions` - 3-4 player choices
- `consequences` - JSON object defining outcomes (seeds, items, status effects, choice tags)
- `conditions` - JSON object defining requirements (choice tags, status effects)

**Dynamic Narrative**: AI-generated chapter events follow same `LoreSnippet` schema but are created by Gemini.

## Platform-Specific Notes

### Text-to-Speech (TTS)
The GDD mandates TTS narration for accessibility:

```kotlin
// expect/actual pattern
expect class SpeechSynthesizer() {
    fun speak(text: String)
}

// Android: uses android.speech.tts.TextToSpeech
// Desktop: uses FreeTTS library
```

Usage: `rememberSpeechSynthesizer()` in Compose, call `synthesizer.safeSpeak(text)` on state changes.

### File I/O
Use `expect/actual` for platform differences:
- Android: `context.filesDir`
- Desktop: `System.getProperty("user.home")/.jalmarquest/`
- iOS: `NSHomeDirectory()`

## Development Workflow Tips

1. **Adding a new feature**: Create `:feature:yourfeature` module, add to `settings.gradle.kts`, implement StateMachine + Controller pattern
2. **Modifying state**: Add field to `Player` data class, update serialization test, bump save version if breaking
3. **New quest**: Add to `QuestCatalog.registerQuest()`, define objectives/rewards, test with `QuestManagerTest`
4. **AI integration**: Ensure choice tags are logged, test sandbox mode first (`AI_DIRECTOR_MODE=sandbox`)

## Common Pitfalls

- **Forgetting `@Serializable`** - All model classes must be marked or save/load breaks
- **Skipping `gameStateManager.appendChoice()`** - Breaks AI Director's choice memory
- **Hardcoding strings** - Use Moko Resources keys, add EN/NO translations
- **Blocking main thread** - Wrap I/O in `suspend` functions, use `CoroutineScope.launch`
- **Feature module coupling** - Never import `:feature:X` from `:feature:Y`, go through `core:state` instead

## Performance Notes
- `PerformanceLogger.logStateMutation()` tracks state changes for analytics
- `WorldUpdateCoordinator` batches ecosystem updates (1min/5min/1hr intervals)
- Avoid calling `GameStateManager` updates in tight loops - batch mutations

## References
- **Game Design Document**: `.github/instructions/mobile app.instructions.md` (209 lines, authoritative source)
- **AI Director README**: `README.md` (environment config table)
- **Phase Summaries**: `docs/phase*-summary.md` (implementation details per milestone)
- **Test Examples**: `feature/*/src/commonTest/` (patterns for new features)
