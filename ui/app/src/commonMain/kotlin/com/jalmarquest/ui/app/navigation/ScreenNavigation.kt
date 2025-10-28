package com.jalmarquest.ui.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * Screen-based navigation system for mobile-first design.
 * 
 * Replaces bottom panel approach with full-screen transitions.
 * Each screen occupies 100% viewport - no scrolling required.
 * 
 * Design Principles:
 * - One screen visible at a time
 * - Push/pop navigation stack (like Android activities)
 * - Smooth slide transitions
 * - Back gesture support (TODO: platform-specific)
 */
sealed class Screen {
    data object Hub : Screen()
    data object Explore : Screen()
    data object Nest : Screen()
    data object Skills : Screen()
    data object Activities : Screen()
    data object Inventory : Screen()
    data object QuestLog : Screen()
    data object Shop : Screen()
    data object WorldInfo : Screen()
    data object WorldMap : Screen()
    data object Settings : Screen()
    data object CoffeeDonation : Screen() // Alpha 2.2 Phase 5B
    
    // Unique key for each screen type
    val key: String
        get() = this::class.simpleName ?: "Unknown"
}

/**
 * Navigation state manager using stack-based approach.
 */
class ScreenNavigator {
    private val _navigationStack = mutableStateListOf<Screen>(Screen.Hub)
    val navigationStack: List<Screen> = _navigationStack
    
    val currentScreen: Screen
        get() = _navigationStack.lastOrNull() ?: Screen.Hub
    
    /**
     * Navigate to a new screen, pushing it onto the stack.
     */
    fun navigateTo(screen: Screen) {
        // Don't add duplicate of current screen
        if (currentScreen != screen) {
            _navigationStack.add(screen)
        }
    }
    
    /**
     * Replace current screen with a new one (no back navigation).
     */
    fun replaceCurrent(screen: Screen) {
        if (_navigationStack.isNotEmpty()) {
            _navigationStack.removeLast()
        }
        _navigationStack.add(screen)
    }
    
    /**
     * Go back to previous screen. Returns false if already at root.
     */
    fun navigateBack(): Boolean {
        return if (_navigationStack.size > 1) {
            _navigationStack.removeLast()
            true
        } else {
            false
        }
    }
    
    /**
     * Clear stack and navigate to root screen.
     */
    fun navigateToRoot() {
        _navigationStack.clear()
        _navigationStack.add(Screen.Hub)
    }
    
    /**
     * Check if we can navigate back.
     */
    fun canNavigateBack(): Boolean = _navigationStack.size > 1
}

/**
 * Animated screen container with slide transitions.
 */
@Composable
fun AnimatedScreenContainer(
    screen: Screen,
    modifier: Modifier = Modifier,
    content: @Composable (Screen) -> Unit
) {
    val animationDuration = 300
    
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                // Slide in from right, slide out to left
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(animationDuration)
                ) + fadeIn(
                    animationSpec = tween(animationDuration)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth / 3 },
                    animationSpec = tween(animationDuration)
                ) + fadeOut(
                    animationSpec = tween(animationDuration)
                )
            },
            label = "screen_transition"
        ) { currentScreen ->
            content(currentScreen)
        }
    }
}

/**
 * Remember a ScreenNavigator instance across recompositions.
 */
@Composable
fun rememberScreenNavigator(): ScreenNavigator {
    return remember { ScreenNavigator() }
}
