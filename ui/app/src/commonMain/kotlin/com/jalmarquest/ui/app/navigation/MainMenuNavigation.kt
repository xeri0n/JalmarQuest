package com.jalmarquest.ui.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jalmarquest.core.state.GameStateManager

/**
 * Main menu navigation state management
 */
class MainMenuNavigator(
    private val gameStateManager: GameStateManager
) {
    private var _currentScreen = mutableStateOf(MainMenuScreen.SPLASH)
    val currentScreen: MainMenuScreen get() = _currentScreen.value
    
    enum class MainMenuScreen {
        SPLASH,      // Initial splash screen
        MAIN_MENU,   // New Game / Continue / Settings
        GAME,        // Active gameplay
        SETTINGS,    // Settings menu
        CREDITS      // Credits screen
    }
    
    fun navigateToSplash() {
        // Save game state before returning to menu
        saveCurrentGame()
        _currentScreen.value = MainMenuScreen.SPLASH
    }
    
    fun navigateToMainMenu() {
        _currentScreen.value = MainMenuScreen.MAIN_MENU
    }
    
    fun navigateToGame() {
        _currentScreen.value = MainMenuScreen.GAME
    }
    
    fun navigateToSettings() {
        _currentScreen.value = MainMenuScreen.SETTINGS
    }
    
    fun navigateToCredits() {
        _currentScreen.value = MainMenuScreen.CREDITS
    }
    
    private fun saveCurrentGame() {
        // Trigger autosave through GameStateManager
        // This is a simplified version - real implementation would be async
        gameStateManager.triggerAutosave()
    }
}

/**
 * Extension function for GameStateManager to trigger autosave
 */
fun GameStateManager.triggerAutosave() {
    // Implementation would save to persistent storage
    // For now, just log the action
    println("Game autosaved at ${System.currentTimeMillis()}")
}
