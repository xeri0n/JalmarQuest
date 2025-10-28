package com.jalmarquest.ui.app.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jalmarquest.ui.app.layout.AppSpacing
import dev.icerock.moko.resources.compose.stringResource
import com.jalmarquest.ui.app.MR

/**
 * Main menu drawer component accessible from anywhere in the app.
 * 
 * Provides quick access to:
 * - Save game
 * - Load game
 * - Settings
 * - Quit to main menu
 * - Exit application
 */
@Composable
fun MainMenuDrawer(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSaveGame: () -> Unit,
    onLoadGame: () -> Unit,
    onSettings: () -> Unit,
    onQuitToMenu: () -> Unit,
    onExitApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Scrim (dark overlay) when drawer is open
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(initialOffsetX = { -it }),
        exit = slideOutHorizontally(targetOffsetX = { -it })
    ) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            // Scrim - tapping dismisses drawer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                    .clickable(onClick = onDismiss)
            )
            
            // Drawer content
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .align(Alignment.CenterStart),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = AppSpacing.medium),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header
                    Column(
                        modifier = Modifier.padding(horizontal = AppSpacing.medium)
                    ) {
                        Text(
                            text = stringResource(MR.strings.app_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(AppSpacing.tiny))
                        
                        Text(
                            text = stringResource(MR.strings.drawer_main_menu),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(AppSpacing.large))
                    
                    // Menu items
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        MainMenuItem(
                            icon = Icons.Default.Save,
                            label = stringResource(MR.strings.drawer_save_game),
                            onClick = {
                                onSaveGame()
                                onDismiss()
                            }
                        )
                        
                        MainMenuItem(
                            icon = Icons.Default.FolderOpen,
                            label = stringResource(MR.strings.drawer_load_game),
                            onClick = {
                                onLoadGame()
                                onDismiss()
                            }
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = AppSpacing.small)
                        )
                        
                        MainMenuItem(
                            icon = Icons.Default.Settings,
                            label = stringResource(MR.strings.drawer_settings),
                            onClick = {
                                onSettings()
                                onDismiss()
                            }
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = AppSpacing.small)
                        )
                        
                        MainMenuItem(
                            icon = Icons.Default.Home,
                            label = stringResource(MR.strings.drawer_quit_to_main_menu),
                            onClick = {
                                onQuitToMenu()
                                onDismiss()
                            }
                        )
                        
                        MainMenuItem(
                            icon = Icons.Default.ExitToApp,
                            label = stringResource(MR.strings.drawer_exit_app),
                            onClick = {
                                onExitApp()
                                onDismiss()
                            },
                            destructive = true
                        )
                    }
                    
                    // Footer
                    Column(
                        modifier = Modifier.padding(horizontal = AppSpacing.medium)
                    ) {
                        HorizontalDivider()
                        
                        Spacer(modifier = Modifier.height(AppSpacing.small))
                        
                        Text(
                            text = "v1.0.0-alpha",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual menu item with icon and label.
 */
@Composable
private fun MainMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false
) {
    val contentColor = if (destructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.medium, vertical = AppSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor
        )
    }
}

/**
 * Floating action button to open the main menu.
 * Positioned in top-right corner, always visible.
 */
@Composable
fun MainMenuButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = stringResource(MR.strings.content_desc_open_main_menu),
            modifier = Modifier.size(24.dp)
        )
    }
}
