package com.jalmarquest.core.di

import com.jalmarquest.core.model.Player
import com.jalmarquest.core.model.CharacterAccount
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.analytics.AnalyticsLogger
import com.jalmarquest.core.state.auth.AuthController
import com.jalmarquest.core.state.auth.AuthStateManager
import com.jalmarquest.core.state.auth.AuthTokenStorage
import com.jalmarquest.core.state.auth.GuestAuthGateway
import com.jalmarquest.core.state.crash.CrashReporter
import com.jalmarquest.core.state.hoard.HoardRankManager
import com.jalmarquest.core.state.hoard.LeaderboardService
import com.jalmarquest.core.state.hoard.ShinyValuationService
import com.jalmarquest.core.state.concoctions.ConcoctionCrafter
import com.jalmarquest.core.state.concoctions.IngredientHarvestService
import com.jalmarquest.core.state.concoctions.RecipeLibraryService
import com.jalmarquest.core.state.thoughts.ThoughtCabinetManager
import com.jalmarquest.core.state.thoughts.ThoughtCatalogService
import com.jalmarquest.core.state.archetype.ArchetypeManager
import com.jalmarquest.core.state.archetype.TalentTreeCatalog
import com.jalmarquest.core.state.account.AccountManager
import com.jalmarquest.core.state.quests.QuestManager
import com.jalmarquest.core.state.quests.QuestCatalog
import com.jalmarquest.core.state.quests.QuestTriggerManager
import com.jalmarquest.core.state.quests.QuestFlowIntegrator
import com.jalmarquest.core.state.monetization.GlimmerWalletManager
import com.jalmarquest.core.state.monetization.EntitlementManager
import com.jalmarquest.core.state.battlepass.SeasonalChronicleManager
import com.jalmarquest.core.state.battlepass.SeasonCatalog
import com.jalmarquest.core.state.battlepass.registerSeason1
import com.jalmarquest.core.state.shop.ShopManager
import com.jalmarquest.core.state.shop.ShopCatalog
import com.jalmarquest.core.state.shop.registerDefaultItems
// import com.jalmarquest.core.state.companions.CompanionManager
// import com.jalmarquest.core.state.companions.CompanionCatalog
import com.jalmarquest.core.state.catalogs.NpcCatalog
import com.jalmarquest.core.state.catalogs.EnemyCatalog
import com.jalmarquest.core.state.catalogs.LocationCatalog
import com.jalmarquest.core.state.npc.NpcScheduleManager
import com.jalmarquest.core.state.npc.NpcRelationshipManager
import com.jalmarquest.core.state.time.InGameTimeManager
import com.jalmarquest.core.state.factions.FactionManager
import com.jalmarquest.core.state.dialogue.DialogueManager
import com.jalmarquest.core.state.ai.NpcAiGoalManager
import com.jalmarquest.core.state.ai.NpcReactionManager
import com.jalmarquest.core.state.dialogue.DynamicDialogueManager
import com.jalmarquest.core.state.ecosystem.PredatorPatrolManager
import com.jalmarquest.core.state.ecosystem.ResourceRespawnManager
import com.jalmarquest.core.state.ecosystem.FactionTerritoryManager
import com.jalmarquest.core.state.difficulty.RegionDifficultyManager
import com.jalmarquest.core.state.player.PlayerLocationTracker
import com.jalmarquest.core.state.weather.WeatherSystem
import com.jalmarquest.core.state.weather.SeasonalCycleManager
import com.jalmarquest.core.state.coordinator.WorldUpdateCoordinator
import com.jalmarquest.core.state.coordinator.OptimizedWorldUpdateCoordinator
import com.jalmarquest.feature.eventengine.ChapterEventProvider
import com.jalmarquest.feature.eventengine.DefaultChapterEventProvider
import com.jalmarquest.feature.eventengine.EventEngine
import com.jalmarquest.feature.eventengine.InMemoryEventEngine
import com.jalmarquest.feature.eventengine.SnippetSelector
import com.jalmarquest.feature.explore.ConsequencesParser
import com.jalmarquest.feature.explore.DefaultSnippetSelector
import com.jalmarquest.feature.explore.ExploreController
import com.jalmarquest.feature.explore.ExploreStateMachine
import com.jalmarquest.feature.explore.LoreSnippetRepository
import com.jalmarquest.feature.activities.ActivitiesController
import com.jalmarquest.feature.activities.ActivityStateMachine
import com.jalmarquest.feature.hub.HubController
import com.jalmarquest.feature.hub.HubStateMachine
import com.jalmarquest.feature.nest.NestConfig
import com.jalmarquest.feature.nest.NestController
import com.jalmarquest.feature.nest.NestStateMachine
import com.jalmarquest.feature.nest.NestCosmeticCatalog
import com.jalmarquest.core.state.managers.NestCustomizationManager
import com.jalmarquest.feature.systemic.SystemicInteractionController
import com.jalmarquest.feature.systemic.SystemicInteractionEngine
import com.jalmarquest.feature.systemic.defaultInteractionCatalog
import com.jalmarquest.feature.worldmap.WorldMapController
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Clock
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

private var currentKoinInstance: Koin? = null

fun coreModule(
    initialPlayer: Player,
    initialCharacterAccount: CharacterAccount,
    guestGateway: GuestAuthGateway,
    chapterEventProvider: ChapterEventProvider = DefaultChapterEventProvider()
): Module = module {
    single { initialPlayer }
    single { initialCharacterAccount }
    single { guestGateway }
    single { AnalyticsLogger() }
    single { CrashReporter() }
    single { AuthTokenStorage() }
    single { NestConfig.default() }
    
    // Phase 1 Catalogs
    single { NpcCatalog() }
    single { EnemyCatalog() }
    single { LocationCatalog() }
    single { com.jalmarquest.core.state.catalogs.WorldRegionCatalog() }
    
    // Phase 2 NPC & World Systems
    single { InGameTimeManager(timestampProvider = ::currentTimeProvider) }
    single { NpcScheduleManager(npcCatalog = get(), timeManager = get()) }
    single { NpcRelationshipManager(timestampProvider = ::currentTimeProvider) }
    single { FactionManager() }
    single { DialogueManager() }
    
    // Phase 3 Advanced AI & Ecosystem
    single { NpcAiGoalManager(npcCatalog = get(), scheduleManager = get(), relationshipManager = get(), timeManager = get(), gameStateManager = get(), timestampProvider = ::currentTimeProvider) }
    single { NpcReactionManager(npcCatalog = get(), relationshipManager = get(), factionManager = get(), questManager = get(), gameStateManager = get(), timestampProvider = ::currentTimeProvider) }
    single { DynamicDialogueManager(baseDialogueManager = get(), npcCatalog = get(), relationshipManager = get(), reactionManager = get(), questManager = get(), factionManager = get(), timeManager = get(), gameStateManager = get(), timestampProvider = ::currentTimeProvider) }
    single { PredatorPatrolManager(enemyCatalog = get(), timeManager = get(), timestampProvider = ::currentTimeProvider) }
    single { ResourceRespawnManager(locationCatalog = get(), timeManager = get(), timestampProvider = ::currentTimeProvider) }
    single { FactionTerritoryManager(locationCatalog = get(), factionManager = get(), timeManager = get(), timestampProvider = ::currentTimeProvider) }
    
    // Phase 4 Polish & Balance
    single { RegionDifficultyManager() }
    single { PlayerLocationTracker(timestampProvider = ::currentTimeProvider) }
    single { WeatherSystem(timestampProvider = ::currentTimeProvider) }
    single { SeasonalCycleManager(timestampProvider = ::currentTimeProvider) }
    single { WorldUpdateCoordinator(npcCatalog = get(), npcAiGoalManager = get(), predatorPatrolManager = get(), resourceRespawnManager = get(), weatherSystem = get(), seasonalCycleManager = get(), timestampProvider = ::currentTimeProvider) }
    
    // Phase 6 Performance Optimization
    single { OptimizedWorldUpdateCoordinator(npcCatalog = get(), enemyCatalog = get(), locationCatalog = get(), npcAiGoalManager = get(), predatorPatrolManager = get(), resourceRespawnManager = get(), weatherSystem = get(), seasonalCycleManager = get(), timestampProvider = ::currentTimeProvider) }
    
    // Phase 5 Quest Flow Integration
    single { QuestTriggerManager() }
    single { QuestFlowIntegrator(scope = get(), locationTracker = get(), questTriggerManager = get(), questManager = get(), npcReactionManager = get(), gameStateManager = get(), timeManager = get()) }
    
    // State Machines & Controllers
    single { NestStateMachine(config = get(), gameStateManager = get()) }
    single { LoreSnippetRepository.defaultCatalog() }
    single { ConsequencesParser() }
    single<SnippetSelector> { DefaultSnippetSelector(get()) }
    single { defaultInteractionCatalog() }
    single { SystemicInteractionEngine(catalog = get()) }
    single { HubStateMachine(gameStateManager = get()) }
    single { ActivityStateMachine(gameStateManager = get()) }
    single { LeaderboardService() }
    single { ShinyValuationService() }
    single { HoardRankManager(gameStateManager = get(), valuationService = get(), leaderboardService = get(), timestampProvider = ::currentTimeProvider) }
    single { IngredientHarvestService() }
    single { RecipeLibraryService() }
    single { ConcoctionCrafter(gameStateManager = get(), recipeLibrary = get(), harvestService = get(), timestampProvider = ::currentTimeProvider) }
    single { ThoughtCatalogService() }
    single { ThoughtCabinetManager(gameStateManager = get(), thoughtCatalog = get(), timestampProvider = ::currentTimeProvider) }
    single { TalentTreeCatalog() }
    single { ArchetypeManager(gameStateManager = get(), talentTreeCatalog = get()) }
    single { AccountManager(initialAccount = get(), timestampProvider = ::currentTimeProvider) }
    single { QuestCatalog() }
    
    // Nest Customization (Housing System) - Must be before QuestManager
    single { NestCosmeticCatalog.apply { registerAllCosmetics() } }
    single { 
        NestCustomizationManager(
            gameStateManager = get(),
            glimmerWalletManager = get(),
            timestampProvider = ::currentTimeProvider,
            cosmeticCatalog = get<NestCosmeticCatalog>().allCosmetics
        )
    }
    
    single { QuestManager(
        questCatalog = get(), 
        gameStateManager = get(), 
        nestCustomizationManager = get(),
        timestampProvider = ::currentTimeProvider
    ) }
    
    // Monetization & Battle Pass
    single { EntitlementManager(gameStateManager = get()) }
    single { GlimmerWalletManager(gameStateManager = get(), timestampProvider = ::currentTimeProvider, entitlementManager = get()) }
    single { SeasonCatalog().apply { registerSeason1(currentTimeProvider()) } }
    single { resolveSeasonalChronicleManager() }
    
    // Shop & Cosmetics
    single { ShopCatalog().apply { registerDefaultItems() } }
    single { resolveShopManager() }
    
    // World Exploration System
    single {
        com.jalmarquest.core.state.managers.DiscoveryRewardManager(
            regionCatalog = get(),
            gameStateManager = get(),
            timestampProvider = ::currentTimeProvider
        )
    }
    single {
        com.jalmarquest.core.state.managers.WorldMapManager(
            gameStateManager = get(),
            locationCatalog = get(),
            regionCatalog = get(),
            discoveryRewardManager = get(),
            timestampProvider = ::currentTimeProvider
        )
    }
    
    // single { CompanionCatalog() }
    // single { CompanionManager(gameStateManager = get(), companionCatalog = get(), timestampProvider = ::currentTimeProvider) }
    single<EventEngine> {
        InMemoryEventEngine(
            snippetSelector = get(),
            chapterEventOdds = 0.25,
            chapterEventProvider = get()
        )
    }
    single {
        GameStateManager(
            initialPlayer = get(),
            accountManager = get(),
            timestampProvider = ::currentTimeProvider
        )
    }
    single { ExploreStateMachine(eventEngine = get(), snippetRepository = get(), consequencesParser = get(), gameStateManager = get()) }
    single {
        AuthStateManager(
            tokenStorage = get(),
            guestGateway = get(),
            analyticsLogger = get()
        )
    }
    factory { (scope: CoroutineScope) -> AuthController(scope = scope, stateManager = get()) }
    factory { (scope: CoroutineScope) -> NestController(scope = scope, stateMachine = get()) }
    factory { (scope: CoroutineScope) -> ExploreController(scope = scope, stateMachine = get()) }
    factory { (scope: CoroutineScope) -> HubController(scope = scope, stateMachine = get()) }
    factory { (scope: CoroutineScope) -> ActivitiesController(scope = scope, stateMachine = get()) }
    factory { (scope: CoroutineScope) -> SystemicInteractionController(scope = scope, engine = get(), gameStateManager = get()) }
    factory { (scope: CoroutineScope) -> 
        WorldMapController(
            worldMapManager = get(),
            coroutineScope = scope
        )
    }
    single<ChapterEventProvider> { chapterEventProvider }
}

/**
 * Initialize optimization systems integration
 * Call this after Koin initialization to wire PlayerLocationTracker with OptimizedWorldUpdateCoordinator
 */
fun initializeOptimizationIntegration() {
    val tracker = resolvePlayerLocationTracker()
    val coordinator = resolveOptimizedWorldUpdateCoordinator()
    tracker.setOptimizedCoordinator(coordinator)
}

private fun currentTimeProvider(): Long = Clock.System.now().toEpochMilliseconds()

fun initKoin(
    initialPlayer: Player,
    initialCharacterAccount: CharacterAccount,
    guestGateway: GuestAuthGateway,
    chapterEventProvider: ChapterEventProvider = DefaultChapterEventProvider()
): Koin = startKoin {
    modules(coreModule(initialPlayer, initialCharacterAccount, guestGateway, chapterEventProvider))
}.koin.also { currentKoinInstance = it }

internal fun requireKoin(): Koin = currentKoinInstance
    ?: error("Koin has not been initialized. Call initKoin before accessing dependencies.")

fun resolveHoardRankManager(): HoardRankManager = requireKoin().get()

fun resolveConcoctionCrafter(): ConcoctionCrafter = requireKoin().get()

fun resolveThoughtCabinetManager(): ThoughtCabinetManager = requireKoin().get()

fun resolveArchetypeManager(): ArchetypeManager = requireKoin().get()

fun resolveAccountManager(): AccountManager = requireKoin().get()

fun resolveQuestManager(): QuestManager = requireKoin().get()

fun resolveQuestCatalog(): QuestCatalog = requireKoin().get()

fun resolveGameStateManager(): GameStateManager = requireKoin().get()

// fun resolveCompanionCatalog(): CompanionCatalog = requireKoin().get()

// fun resolveCompanionManager(): CompanionManager = requireKoin().get()

// Phase 1-3 Manager Resolvers
fun resolveNpcCatalog(): NpcCatalog = requireKoin().get()

fun resolveEnemyCatalog(): EnemyCatalog = requireKoin().get()

fun resolveLocationCatalog(): LocationCatalog = requireKoin().get()

fun resolveInGameTimeManager(): InGameTimeManager = requireKoin().get()

fun resolveNpcScheduleManager(): NpcScheduleManager = requireKoin().get()

fun resolveNpcRelationshipManager(): NpcRelationshipManager = requireKoin().get()

fun resolveFactionManager(): FactionManager = requireKoin().get()

fun resolveDialogueManager(): DialogueManager = requireKoin().get()

fun resolveNpcAiGoalManager(): NpcAiGoalManager = requireKoin().get()

fun resolveNpcReactionManager(): NpcReactionManager = requireKoin().get()

fun resolveDynamicDialogueManager(): DynamicDialogueManager = requireKoin().get()

fun resolvePredatorPatrolManager(): PredatorPatrolManager = requireKoin().get()

fun resolveResourceRespawnManager(): ResourceRespawnManager = requireKoin().get()

fun resolveFactionTerritoryManager(): FactionTerritoryManager = requireKoin().get()

fun resolveRegionDifficultyManager(): RegionDifficultyManager = requireKoin().get()

fun resolvePlayerLocationTracker(): PlayerLocationTracker = requireKoin().get()

fun resolveWeatherSystem(): WeatherSystem = requireKoin().get()

fun resolveSeasonalCycleManager(): SeasonalCycleManager = requireKoin().get()

fun resolveWorldUpdateCoordinator(): WorldUpdateCoordinator = requireKoin().get()

fun resolveOptimizedWorldUpdateCoordinator(): OptimizedWorldUpdateCoordinator = requireKoin().get()

fun resolveQuestTriggerManager(): QuestTriggerManager = requireKoin().get()

fun resolveQuestFlowIntegrator(): QuestFlowIntegrator = requireKoin().get()

fun resolveGlimmerWalletManager(): GlimmerWalletManager = requireKoin().get()

fun resolveEntitlementManager(): EntitlementManager = requireKoin().get()

fun resolveNestCustomizationManager(): NestCustomizationManager = requireKoin().get()

fun resolveNestCosmeticCatalog(): NestCosmeticCatalog = requireKoin().get()

fun resolveWorldRegionCatalog(): com.jalmarquest.core.state.catalogs.WorldRegionCatalog = requireKoin().get()

fun resolveWorldMapManager(): com.jalmarquest.core.state.managers.WorldMapManager = requireKoin().get()

fun resolveDiscoveryRewardManager(): com.jalmarquest.core.state.managers.DiscoveryRewardManager = requireKoin().get()

fun resolveSeasonCatalog(): SeasonCatalog = requireKoin().get()

fun resolveSeasonalChronicleManager(): SeasonalChronicleManager {
    val koin = requireKoin()
    return SeasonalChronicleManager(
        gameStateManager = koin.get(),
        glimmerWalletManager = koin.get(),
        seasonCatalog = koin.get(),
        timestampProvider = ::currentTimeProvider
    )
}

fun resolveShopManager(): ShopManager {
    val koin = requireKoin()
    return ShopManager(
        gameStateManager = koin.get(),
        catalog = koin.get(),
        glimmerWalletManager = koin.get(),
        currentTimeProvider = ::currentTimeProvider
    )
}
