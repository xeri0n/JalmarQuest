package com.jalmarquest.ui.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jalmarquest.core.model.AuthState
import com.jalmarquest.core.di.resolveAuthController
import com.jalmarquest.core.di.resolveAuthStateManager
import com.jalmarquest.core.di.resolveActivitiesController
import com.jalmarquest.core.di.resolveNestController
import com.jalmarquest.core.di.resolveNestCustomizationManager
import com.jalmarquest.core.di.resolveExploreController
import com.jalmarquest.core.di.resolveSystemicInteractionController
import com.jalmarquest.core.di.resolveHubController
import com.jalmarquest.core.di.resolveHoardRankManager
import com.jalmarquest.core.di.resolveConcoctionCrafter
import com.jalmarquest.core.di.resolveThoughtCabinetManager
import com.jalmarquest.core.di.resolveQuestManager
import com.jalmarquest.core.di.resolveQuestCatalog
import com.jalmarquest.core.di.resolveGameStateManager
import com.jalmarquest.feature.hub.HubActionType
import dev.icerock.moko.resources.compose.stringResource
import androidx.compose.foundation.layout.Spacer

@Composable
fun JalmarQuestApp() {
    val synthesizer = rememberSpeechSynthesizer()
    val title = stringResource(MR.strings.app_title)
    val subtitle = stringResource(MR.strings.app_subtitle)
    val authStateManager = remember { resolveAuthStateManager() }
    val scope = rememberCoroutineScope()
    val authController = remember(scope) { resolveAuthController(scope) }
    val nestController = remember(scope) { resolveNestController(scope) }
    val nestCustomizationManager = remember { resolveNestCustomizationManager() }
    val exploreController = remember(scope) { resolveExploreController(scope) }
    val systemicController = remember(scope) { resolveSystemicInteractionController(scope) }
    val hubController = remember(scope) { resolveHubController(scope) }
    val activitiesController = remember(scope) { resolveActivitiesController(scope) }
    val hoardManager = remember { resolveHoardRankManager() }
    val concoctionCrafter = remember { resolveConcoctionCrafter() }
    val thoughtCabinetManager = remember { resolveThoughtCabinetManager() }
    
    // Quest system
    val questManager = remember { resolveQuestManager() }
    val questCatalog = remember { resolveQuestCatalog() }
    val gameStateManager = remember { resolveGameStateManager() }
    val questController = remember { QuestController(questManager, questCatalog, gameStateManager) }
    val authState by authController.authState.collectAsState()
    val guestCta = stringResource(MR.strings.auth_continue_guest)
    val guestHint = stringResource(MR.strings.auth_signed_out_hint)
    val signOut = stringResource(MR.strings.auth_sign_out)
    
    var showMainMenu by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) { 
        authController.bootstrap()
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when {
                showMainMenu && authState is AuthState.SignedOut -> {
                    // Show main menu
                    MainMenuScreen(
                        onBeginJourney = {
                            authController.continueAsGuest()
                            showMainMenu = false
                        },
                        onLoadGame = {
                            // TODO: Implement load game functionality
                            authController.continueAsGuest()
                            showMainMenu = false
                        }
                    )
                }
                else -> {
                    // Show the game UI
                    SimpleGameContent(
                        authState = authState,
                        authController = authController,
                        hubController = hubController,
                        hoardManager = hoardManager,
                        concoctionCrafter = concoctionCrafter,
                        questController = questController,
                        nestCustomizationManager = nestCustomizationManager,
                        gameStateManager = gameStateManager
                    )
                }
            }
        }
    }
}

@Composable
private fun SimpleGameContent(
    authState: AuthState,
    authController: com.jalmarquest.core.state.auth.AuthController,
    hubController: com.jalmarquest.feature.hub.HubController,
    hoardManager: com.jalmarquest.core.state.hoard.HoardRankManager,
    concoctionCrafter: com.jalmarquest.core.state.concoctions.ConcoctionCrafter,
    questController: QuestController,
    nestCustomizationManager: com.jalmarquest.core.state.managers.NestCustomizationManager,
    gameStateManager: com.jalmarquest.core.state.GameStateManager
) {
    val title = stringResource(MR.strings.app_title)
    val subtitle = stringResource(MR.strings.app_subtitle)
    val guestHint = stringResource(MR.strings.auth_signed_out_hint)
    val guestCta = stringResource(MR.strings.auth_continue_guest)
    val signOut = stringResource(MR.strings.auth_sign_out)
    
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            when (val state = authState) {
                AuthState.SignedOut -> {
                    Text(text = guestHint)
                    Button(onClick = { authController.continueAsGuest() }) {
                        Text(text = guestCta)
                    }
                }
                is AuthState.Guest -> {
                    Text(text = stringResource(MR.strings.auth_welcome_guest, state.profile.displayName))
                    Text(
                        text = "📍 Current Location: Centre of Buttonburgh",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Button(onClick = { authController.signOut() }) {
                        Text(text = signOut)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HubSection(
                        controller = hubController,
                        actionContent = { actionType ->
                            when (actionType) {
                                HubActionType.HOARD -> HoardSection(manager = hoardManager)
                                HubActionType.CONCOCTIONS -> ConcoctionsSection(manager = concoctionCrafter)
                                HubActionType.QUESTS -> QuestSection(controller = questController)
                                HubActionType.NEST -> NestScreen(
                                    customizationManager = nestCustomizationManager,
                                    gameStateManager = gameStateManager
                                )
                                else -> {
                                    Text(
                                        text = "This feature is not yet implemented.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GameContent(
    authState: AuthState,
    authController: com.jalmarquest.core.state.auth.AuthController,
    title: String,
    subtitle: String,
    guestHint: String,
    guestCta: String,
    signOut: String,
    hubController: com.jalmarquest.feature.hub.HubController
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            when (val state = authState) {
                AuthState.SignedOut -> {
                    Text(text = guestHint)
                    Button(onClick = { authController.continueAsGuest() }) {
                        Text(text = guestCta)
                    }
                }
                is AuthState.Guest -> {
                    Text(text = stringResource(MR.strings.auth_welcome_guest, state.profile.displayName))
                    Text(
                        text = "📍 Current Location: Centre of Buttonburgh",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Button(onClick = { authController.signOut() }) {
                        Text(text = signOut)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HubSection(controller = hubController)
                }
            }
        }
    }
}
