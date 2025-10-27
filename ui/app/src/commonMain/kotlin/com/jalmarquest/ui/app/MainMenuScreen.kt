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
    onBackToMenu: (() -> Unit)? = null
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
            contentDescription = "Main menu background",
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
                text = "Jalmar Quest",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 64.dp)
            )

            // Begin Journey Button
            MenuButton(
                text = "Begin Journey",
                onClick = onBeginJourney,
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Load Game Button
            MenuButton(
                text = "Load Game",
                onClick = onLoadGame,
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Options Button
            MenuButton(
                text = "Options",
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
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background
        Image(
            painter = painterResource("mainmenu.png"),
            contentDescription = "Options background",
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
                                contentDescription = "Back to main menu",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = "Options",
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
                                    text = "Text-to-Speech",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                                Text(
                                    text = "Enable voice narration (Default: OFF)",
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

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "More settings coming soon...",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// Platform-specific painter resource - this is a placeholder
// You'll need to implement this for each platform
@Composable
expect fun painterResource(resourcePath: String): androidx.compose.ui.graphics.painter.Painter
