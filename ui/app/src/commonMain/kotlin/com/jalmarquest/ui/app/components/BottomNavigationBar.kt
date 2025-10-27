package com.jalmarquest.ui.app.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.jalmarquest.ui.app.navigation.Screen
import com.jalmarquest.ui.app.layout.AppSpacing

/**
 * Bottom navigation bar for quick access to main screens.
 * 
 * Shows 5 primary destinations:
 * - Hub (home base)
 * - Explore (wilderness adventures)
 * - Nest (housing)
 * - Skills (crafting/progression)
 * - Activities (secondary content)
 */
@Composable
fun BottomNavigationBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp
    ) {
        NavigationBarItem(
            icon = { 
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Hub",
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { Text("Hub") },
            selected = currentScreen is Screen.Hub,
            onClick = { onNavigate(Screen.Hub) }
        )
        
        NavigationBarItem(
            icon = { 
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = "Explore",
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { Text("Explore") },
            selected = currentScreen is Screen.Explore,
            onClick = { onNavigate(Screen.Explore) }
        )
        
        NavigationBarItem(
            icon = { 
                Icon(
                    imageVector = Icons.Default.House,
                    contentDescription = "Nest",
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { Text("Nest") },
            selected = currentScreen is Screen.Nest,
            onClick = { onNavigate(Screen.Nest) }
        )
        
        NavigationBarItem(
            icon = { 
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Skills",
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { Text("Skills") },
            selected = currentScreen is Screen.Skills,
            onClick = { onNavigate(Screen.Skills) }
        )
        
        NavigationBarItem(
            icon = { 
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = "More",
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { Text("More") },
            selected = currentScreen is Screen.Activities,
            onClick = { onNavigate(Screen.Activities) }
        )
    }
}
