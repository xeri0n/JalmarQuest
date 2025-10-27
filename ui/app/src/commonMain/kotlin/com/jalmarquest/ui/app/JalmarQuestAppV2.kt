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
    
    // Auth state
    val authState by authController.authState.collectAsState()
    
    // Navigation
    val screenNavigator = rememberScreenNavigator()
    
    // Main menu drawer state
    var showMainMenu by remember { mutableStateOf(false) }
    
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
                                    currentLocation = "Centre of Buttonburgh",
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
                                        
                                        is Screen.Settings -> SettingsScreenPlaceholder(
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
        title = "🗺️ Explore",
        description = "Full-screen exploration UI will be implemented here.\n\nNo scrolling required - all content fits viewport.",
        onBack = onBack
    )
}

@Composable
private fun NestScreenPlaceholder(onBack: () -> Unit) {
    PlaceholderScreen(
        title = "🏡 The Nest",
        description = "Nest management screen with housing cosmetics.\n\nFuture: 50+ cosmetic items, functional upgrades.",
        onBack = onBack
    )
}

@Composable
private fun SkillsScreenPlaceholder(onBack: () -> Unit) {
    PlaceholderScreen(
        title = "⚒️ Skills & Crafting",
        description = "Skills tree and crafting interface.",
        onBack = onBack
    )
}

@Composable
private fun ActivitiesScreenPlaceholder(onBack: () -> Unit) {
    PlaceholderScreen(
        title = "🎯 Activities",
        description = "Secondary activities interface.",
        onBack = onBack
    )
}

@Composable
private fun InventoryScreenPlaceholder(onBack: () -> Unit) {
    PlaceholderScreen(
        title = "🎒 Inventory",
        description = "Full inventory management screen.",
        onBack = onBack
    )
}

@Composable
private fun QuestLogScreenPlaceholder(onBack: () -> Unit) {
    PlaceholderScreen(
        title = "📜 Quest Log",
        description = "Active and completed quests.",
        onBack = onBack
    )
}

@Composable
private fun ShopScreenPlaceholder(onBack: () -> Unit) {
    PlaceholderScreen(
        title = "🛒 Shop",
        description = "In-game shop with purchases.",
        onBack = onBack
    )
}

@Composable
private fun WorldInfoScreenPlaceholder(onBack: () -> Unit) {
    PlaceholderScreen(
        title = "🌍 World Info",
        description = "Lore, factions, and world information.",
        onBack = onBack
    )
}

@Composable
private fun SettingsScreenPlaceholder(onBack: () -> Unit) {
    PlaceholderScreen(
        title = "⚙️ Settings",
        description = "Game settings, audio, accessibility options.",
        onBack = onBack
    )
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
            Text("← Back")
        }
    }
}
