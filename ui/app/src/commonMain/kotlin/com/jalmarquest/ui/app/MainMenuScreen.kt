package com.jalmarquest.ui.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.icerock.moko.resources.compose.stringResource
import com.jalmarquest.ui.app.MR

enum class MenuScreen {
    MAIN,
    OPTIONS,
    GAME
}

// Shared TTS state that persists across the app
object AppSettings {
    var ttsEnabled by mutableStateOf(false)
}

@Composable
fun MainMenuScreen(
    onBeginJourney: () -> Unit,
    onLoadGame: () -> Unit,
    onBackToMenu: (() -> Unit)? = null,
    gameStateManager: com.jalmarquest.core.state.GameStateManager? = null
) {
    var currentScreen by remember { mutableStateOf(MenuScreen.MAIN) }

    when (currentScreen) {
        MenuScreen.MAIN -> {
            MainMenuContent(
                onBeginJourney = {
                    currentScreen = MenuScreen.GAME
                    onBeginJourney()
                },
                onLoadGame = {
                    currentScreen = MenuScreen.GAME
                    onLoadGame()
                },
                onOptions = { currentScreen = MenuScreen.OPTIONS }
            )
        }
        MenuScreen.OPTIONS -> {
            OptionsScreen(
                ttsEnabled = AppSettings.ttsEnabled,
                onTtsToggle = { AppSettings.ttsEnabled = it },
                gameStateManager = gameStateManager,
                onBack = { currentScreen = MenuScreen.MAIN }
            )
        }
        MenuScreen.GAME -> {
            // Game will be handled by parent
        }
    }
}

@Composable
private fun MainMenuContent(
    onBeginJourney: () -> Unit,
    onLoadGame: () -> Unit,
    onOptions: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background Image
        // Note: For Android, this will use the drawable resource
        // For multiplatform, you might need platform-specific implementations
        Image(
            painter = painterResource("mainmenu.png"),
            contentDescription = stringResource(MR.strings.content_desc_main_menu_background),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Menu Buttons
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Title (optional - can be removed if it's in the background image)
            Text(
                text = stringResource(MR.strings.app_title),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 64.dp)
            )

            // Begin Journey Button
            MenuButton(
                text = stringResource(MR.strings.mainmenu_begin_journey),
                onClick = onBeginJourney,
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Load Game Button
            MenuButton(
                text = stringResource(MR.strings.mainmenu_load_game),
                onClick = onLoadGame,
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Options Button
            MenuButton(
                text = stringResource(MR.strings.mainmenu_options),
                onClick = onOptions,
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MenuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(60.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2C3E50).copy(alpha = 0.9f),
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun OptionsScreen(
    ttsEnabled: Boolean,
    onTtsToggle: (Boolean) -> Unit,
    gameStateManager: com.jalmarquest.core.state.GameStateManager?,
    onBack: () -> Unit
) {
    // Observe player settings from GameStateManager
    val playerState = gameStateManager?.playerState?.collectAsState()
    val noFilterEnabled = playerState?.value?.playerSettings?.isNoFilterModeEnabled ?: false
    
    // State for warning dialog
    var showWarningDialog by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background
        Image(
            painter = painterResource("mainmenu.png"),
            contentDescription = stringResource(MR.strings.content_desc_options_background),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Semi-transparent overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Back button at top-left
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(MR.strings.content_desc_back_to_main_menu),
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = stringResource(MR.strings.options_title),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // TTS Toggle
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF34495E)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(MR.strings.options_tts_label),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                                Text(
                                    text = stringResource(MR.strings.options_tts_desc),
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            Switch(
                                checked = ttsEnabled,
                                onCheckedChange = onTtsToggle,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF3498DB),
                                    checkedTrackColor = Color(0xFF3498DB).copy(alpha = 0.5f)
                                )
                            )
                        }
                    }

                    // No Filter Mode Toggle (Alpha 2.2)
                    if (gameStateManager != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF34495E)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(MR.strings.options_no_filter_label),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = stringResource(MR.strings.options_no_filter_desc),
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                                Switch(
                                    checked = noFilterEnabled,
                                    onCheckedChange = { enabled ->
                                        if (enabled) {
                                            // Show warning dialog when enabling
                                            showWarningDialog = true
                                        } else {
                                            // Disable immediately (no confirmation needed)
                                            gameStateManager.updatePlayerSettings { settings ->
                                                settings.copy(isNoFilterModeEnabled = false)
                                            }
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFFE74C3C),
                                        checkedTrackColor = Color(0xFFE74C3C).copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = stringResource(MR.strings.options_more_settings_coming_soon),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
    
    // Warning Dialog (Alpha 2.2)
    if (showWarningDialog && gameStateManager != null) {
        AlertDialog(
            onDismissRequest = { showWarningDialog = false },
            title = {
                Text(
                    text = stringResource(MR.strings.options_no_filter_warning_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(MR.strings.options_no_filter_warning_body)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        gameStateManager.updatePlayerSettings { settings ->
                            settings.copy(isNoFilterModeEnabled = true)
                        }
                        showWarningDialog = false
                    }
                ) {
                    Text(stringResource(MR.strings.options_no_filter_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showWarningDialog = false }
                ) {
                    Text(stringResource(MR.strings.options_no_filter_cancel))
                }
            }
        )
    }
}

// Platform-specific painter resource - this is a placeholder
// You'll need to implement this for each platform
@Composable
expect fun painterResource(resourcePath: String): androidx.compose.ui.graphics.painter.Painter
