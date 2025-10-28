package com.jalmarquest.ui.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jalmarquest.core.model.AuthState
import com.jalmarquest.core.di.*
import com.jalmarquest.ui.app.components.*
import com.jalmarquest.ui.app.navigation.*
import com.jalmarquest.ui.app.sections.ExploreScreen
import com.jalmarquest.ui.app.sections.WorldMapScreen
import com.jalmarquest.ui.app.theme.JalmarQuestTheme
import dev.icerock.moko.resources.compose.stringResource
import com.jalmarquest.ui.app.MR
import kotlinx.coroutines.launch

/**
 * Refactored JalmarQuestApp with modern UI/UX.
 * 
 * NEW ARCHITECTURE:
 * - Screen-based navigation (no bottom panels/scrolling)
 * - Collapsible header for space optimization
 * - Persistent main menu access
 * - Professional theme with dark mode support
 * 
 * This demonstrates the Phase 5 UI/UX pattern.
 * Other screens (Explore, Nest, etc.) should follow this template.
 */
@Composable
fun JalmarQuestAppV2() {
    val scope = rememberCoroutineScope()
    
    // Controllers
    val authStateManager = remember { resolveAuthStateManager() }
    val authController = remember(scope) { resolveAuthController(scope) }
    val hubController = remember(scope) { resolveHubController(scope) }
    val gameStateManager = remember { resolveGameStateManager() }
    val questManager = remember { resolveQuestManager() }
    val questCatalog = remember { resolveQuestCatalog() }
    val hoardManager = remember { resolveHoardRankManager() }
    val concoctionCrafter = remember { resolveConcoctionCrafter() }
    val locationCatalog = remember { resolveLocationCatalog() }
    val regionCatalog = remember { resolveWorldRegionCatalog() }
    val worldMapManager = remember { resolveWorldMapManager() }
    val worldMapController = remember(scope) {
        org.koin.mp.KoinPlatformTools.defaultContext().get().get<com.jalmarquest.feature.worldmap.WorldMapController>(
            parameters = { org.koin.core.parameter.parametersOf(scope) }
        )
    }
    val exploreController = remember(scope) {
        org.koin.mp.KoinPlatformTools.defaultContext().get().get<com.jalmarquest.feature.explore.ExploreController>(
            parameters = { org.koin.core.parameter.parametersOf(scope) }
        )
    }
    
    // Quest controller
    val questController = remember { 
        QuestController(questManager, questCatalog, gameStateManager) 
    }
    
    // Alpha 2.2: AI Director integration
    val aiDirectorManager = remember { resolveAIDirectorManager() }
    val aiDirectorState by aiDirectorManager.state.collectAsState()
    
    // Auth state
    val authState by authController.authState.collectAsState()
    
    // Navigation
    val screenNavigator = rememberScreenNavigator()
    
    // Main menu drawer state
    var showMainMenu by remember { mutableStateOf(false) }
    
    // Alpha 2.2: AI Director debug panel toggle
    var showAIDirectorDebug by remember { mutableStateOf(false) }
    
    // Initial setup
    LaunchedEffect(Unit) { 
        authController.bootstrap()
    }
    
    JalmarQuestTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (authState) {
                AuthState.SignedOut -> {
                    // Show splash/login screen
                    MainMenuScreen(
                        onBeginJourney = {
                            authController.continueAsGuest()
                        },
                        onLoadGame = {
                            // TODO: Implement load game
                            authController.continueAsGuest()
                        }
                    )
                }
                
                is AuthState.Guest -> {
                    // Main game UI with new architecture
                    Box(modifier = Modifier.fillMaxSize()) {
                        Scaffold { paddingValues ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                            ) {
                                // Collapsible Header
                                CollapsibleHeader(
                                    gameName = stringResource(MR.strings.app_title),
                                    welcomeMessage = stringResource(
                                        MR.strings.auth_welcome_guest,
                                        (authState as AuthState.Guest).profile.displayName
                                    ),
                                    currentLocation = stringResource(MR.strings.hub_title),
                                    onLogout = { authController.signOut() },
                                    initiallyExpanded = false
                                )
                                
                                // Screen-based navigation content
                                AnimatedScreenContainer(
                                    screen = screenNavigator.currentScreen,
                                    modifier = Modifier.weight(1f)
                                ) { screen ->
                                    when (screen) {
                                        is Screen.Hub -> HubScreenV2(
                                            controller = hubController,
                                            hoardManager = hoardManager,
                                            concoctionCrafter = concoctionCrafter,
                                            questController = questController,
                                            gameStateManager = gameStateManager,
                                            locationCatalog = locationCatalog,
                                            onNavigateToScreen = { targetScreen ->
                                                screenNavigator.navigateTo(targetScreen)
                                            }
                                        )
                                        
                                        is Screen.Explore -> ExploreScreen(
                                            controller = exploreController,
                                            gameStateManager = gameStateManager,
                                            locationCatalog = locationCatalog,
                                            regionCatalog = regionCatalog,
                                            onBack = { screenNavigator.navigateBack() }
                                        )
                                        
                                        is Screen.Nest -> NestScreenPlaceholder(
                                            onBack = { screenNavigator.navigateBack() }
                                        )
                                        
                                        is Screen.Skills -> SkillsScreenPlaceholder(
                                            onBack = { screenNavigator.navigateBack() }
                                        )
                                        
                                        is Screen.Activities -> ActivitiesScreenPlaceholder(
                                            onBack = { screenNavigator.navigateBack() }
                                        )
                                        
                                        is Screen.Inventory -> InventoryScreenPlaceholder(
                                            onBack = { screenNavigator.navigateBack() }
                                        )
                                        
                                        is Screen.QuestLog -> QuestLogScreenPlaceholder(
                                            onBack = { screenNavigator.navigateBack() }
                                        )
                                        
                                        is Screen.Shop -> ShopScreenPlaceholder(
                                            onBack = { screenNavigator.navigateBack() }
                                        )
                                        
                                        is Screen.WorldInfo -> WorldInfoScreenPlaceholder(
                                            onBack = { screenNavigator.navigateBack() }
                                        )
                                        
                                        is Screen.WorldMap -> WorldMapScreen(
                                            controller = worldMapController,
                                            onNavigateBack = { screenNavigator.navigateBack() }
                                        )
                                        
                                        is Screen.Settings -> com.jalmarquest.ui.app.sections.SettingsScreen(
                                            showAIDirectorDebug = showAIDirectorDebug,
                                            onToggleAIDirectorDebug = { enabled ->
                                                showAIDirectorDebug = enabled
                                            },
                                            onNavigateToCoffeeDonation = {
                                                screenNavigator.navigateTo(Screen.CoffeeDonation)
                                            },
                                            onBack = { screenNavigator.navigateBack() }
                                        )
                                        
                                        is Screen.CoffeeDonation -> CoffeeDonationScreenWrapper(
                                            gameStateManager = gameStateManager,
                                            onBack = { screenNavigator.navigateBack() }
                                        )
                                    }
                                }
                                
                                // Bottom Navigation Bar (compact)
                                BottomNavigationBar(
                                    currentScreen = screenNavigator.currentScreen,
                                    onNavigate = { screen ->
                                        screenNavigator.navigateTo(screen)
                                    }
                                )
                            }
                        }
                        
                        // Main Menu Drawer (overlay)
                        MainMenuDrawer(
                            visible = showMainMenu,
                            onDismiss = { showMainMenu = false },
                            onSaveGame = {
                                scope.launch {
                                    // TODO: Implement save logic via GameStateManager
                                }
                            },
                            onLoadGame = {
                                // TODO: Implement load logic
                            },
                            onSettings = {
                                screenNavigator.navigateTo(Screen.Settings)
                            },
                            onQuitToMenu = {
                                authController.signOut()
                            },
                            onExitApp = {
                                // TODO: Platform-specific exit
                            }
                        )
                        
                        // Floating Main Menu Button (always visible)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(end = 16.dp, top = 72.dp)
                        ) {
                            MainMenuButton(
                                onClick = { showMainMenu = !showMainMenu },
                                modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd)
                            )
                        }
                        
                        // Alpha 2.2: AI Director HUD overlay (top-left corner)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 16.dp, top = 72.dp)
                        ) {
                            AIDirectorHUD(
                                difficulty = aiDirectorState.currentDifficulty,
                                playstyle = aiDirectorState.playstyle.getDominantStyle(),
                                eventsSinceRest = aiDirectorState.eventsSinceRest,
                                showFatigueMeter = true,
                                modifier = Modifier.align(androidx.compose.ui.Alignment.TopStart)
                            )
                        }
                        
                        // Alpha 2.2: AI Director debug panel (toggle via Settings)
                        if (showAIDirectorDebug) {
                            // Build playstyle scores map from PlaystyleProfile
                            val playstyleScores = mapOf(
                                com.jalmarquest.core.model.Playstyle.CAUTIOUS to aiDirectorState.playstyle.cautiousScore,
                                com.jalmarquest.core.model.Playstyle.AGGRESSIVE to aiDirectorState.playstyle.aggressiveScore,
                                com.jalmarquest.core.model.Playstyle.EXPLORER to aiDirectorState.playstyle.explorerScore,
                                com.jalmarquest.core.model.Playstyle.HOARDER to aiDirectorState.playstyle.hoarderScore,
                                com.jalmarquest.core.model.Playstyle.SOCIAL to aiDirectorState.playstyle.socialScore,
                                com.jalmarquest.core.model.Playstyle.BALANCED to 0 // Calculated, not stored
                            )
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                AIDirectorDebugPanel(
                                    difficulty = aiDirectorState.currentDifficulty,
                                    playstyle = aiDirectorState.playstyle.getDominantStyle(),
                                    combatWins = aiDirectorState.performance.combatWins,
                                    combatLosses = aiDirectorState.performance.combatLosses,
                                    questCompletions = aiDirectorState.performance.questCompletions,
                                    questFailures = aiDirectorState.performance.questFailures,
                                    deaths = aiDirectorState.performance.deaths,
                                    eventsSinceRest = aiDirectorState.eventsSinceRest,
                                    playstyleScores = playstyleScores,
                                    modifier = Modifier
                                        .align(androidx.compose.ui.Alignment.BottomStart)
                                        .widthIn(max = 400.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Placeholder screens to demonstrate navigation pattern.
 * These will be replaced with full implementations in future phases.
 */
@Composable
private fun ExploreScreenPlaceholder(onBack: () -> Unit) {
    PlaceholderScreen(
        title = stringResource(MR.strings.menu_title_explore),
        description = stringResource(MR.strings.placeholder_explore_desc),
        onBack = onBack
    )
}

@Composable
private fun NestScreenPlaceholder(onBack: () -> Unit) {
    PlaceholderScreen(
        title = stringResource(MR.strings.menu_title_nest),
        description = stringResource(MR.strings.placeholder_nest_desc),
        onBack = onBack
    )
}

@Composable
private fun SkillsScreenPlaceholder(onBack: () -> Unit) {
    PlaceholderScreen(
        title = stringResource(MR.strings.menu_title_skills),
        description = stringResource(MR.strings.placeholder_skills_desc),
        onBack = onBack
    )
}

@Composable
private fun ActivitiesScreenPlaceholder(onBack: () -> Unit) {
    PlaceholderScreen(
        title = stringResource(MR.strings.menu_title_activities),
        description = stringResource(MR.strings.placeholder_activities_desc),
        onBack = onBack
    )
}

@Composable
private fun InventoryScreenPlaceholder(onBack: () -> Unit) {
    PlaceholderScreen(
        title = stringResource(MR.strings.menu_title_inventory),
        description = stringResource(MR.strings.placeholder_inventory_desc),
        onBack = onBack
    )
}

@Composable
private fun QuestLogScreenPlaceholder(onBack: () -> Unit) {
    PlaceholderScreen(
        title = stringResource(MR.strings.menu_title_quest_log),
        description = stringResource(MR.strings.placeholder_quest_log_desc),
        onBack = onBack
    )
}

@Composable
private fun ShopScreenPlaceholder(onBack: () -> Unit) {
    PlaceholderScreen(
        title = stringResource(MR.strings.menu_title_shop),
        description = stringResource(MR.strings.placeholder_shop_desc),
        onBack = onBack
    )
}

@Composable
private fun WorldInfoScreenPlaceholder(onBack: () -> Unit) {
    PlaceholderScreen(
        title = stringResource(MR.strings.menu_title_world_info),
        description = stringResource(MR.strings.placeholder_world_info_desc),
        onBack = onBack
    )
}

@Composable
private fun SettingsScreenPlaceholder(onBack: () -> Unit) {
    PlaceholderScreen(
        title = stringResource(MR.strings.menu_title_settings),
        description = stringResource(MR.strings.placeholder_settings_desc),
        onBack = onBack
    )
}

/**
 * Wrapper for Coffee Donation screen with mock IAP service.
 * Alpha 2.2 Phase 5B: Coffee IAP Implementation
 * 
 * TODO: Replace MockIapService with platform-specific implementation
 */
@Composable
private fun CoffeeDonationScreenWrapper(
    gameStateManager: com.jalmarquest.core.state.GameStateManager,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val glimmerWalletManager = remember { resolveGlimmerWalletManager() }
    
    // TODO: Replace with actual platform IAP service
    val mockIapService = remember { createMockIapService() }
    
    val controller = remember(scope) {
        com.jalmarquest.ui.app.sections.CoffeeDonationController(
            scope = scope,
            glimmerWalletManager = glimmerWalletManager,
            iapService = mockIapService
        )
    }
    
    com.jalmarquest.ui.app.sections.CoffeeDonationSection(
        controller = controller,
        onBack = onBack
    )
}

/**
 * Create a mock IAP service for desktop testing.
 * Alpha 2.2 Phase 5B: Replace with platform-specific implementation.
 */
private fun createMockIapService(): com.jalmarquest.core.state.monetization.IIapService {
    return object : com.jalmarquest.core.state.monetization.IIapService {
        override suspend fun initialize(): Boolean = true
        override suspend fun queryProducts(productIds: List<com.jalmarquest.core.model.ProductId>): Map<com.jalmarquest.core.model.ProductId, com.jalmarquest.core.state.monetization.PlatformProduct> = emptyMap()
        override suspend fun launchPurchaseFlow(product: com.jalmarquest.core.model.IapProduct): com.jalmarquest.core.state.monetization.PurchaseResponse {
            // Simulate successful purchase for testing
            val timestamp = com.jalmarquest.core.state.perf.currentTimeMillis()
            return com.jalmarquest.core.state.monetization.PurchaseResponse.Success(
                productId = product.id,
                receiptData = "mock_receipt_${timestamp}",
                purchaseToken = "mock_token_${timestamp}",
                transactionId = "mock_transaction_${timestamp}",
                purchaseTimeMillis = timestamp
            )
        }
        override suspend fun verifyPurchase(receiptData: String): Boolean = true
        override suspend fun restorePurchases(): List<com.jalmarquest.core.state.monetization.RestoredPurchase> = emptyList()
        override suspend fun consumePurchase(purchaseToken: String): Boolean = true
        override suspend fun acknowledgePurchase(purchaseToken: String): Boolean = true
        override fun isBillingSupported(): Boolean = true
        override fun dispose() {}
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    description: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(MR.strings.common_back_arrow))
        }
    }
}
