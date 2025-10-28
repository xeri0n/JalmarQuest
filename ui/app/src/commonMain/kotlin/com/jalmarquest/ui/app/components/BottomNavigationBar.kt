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
import dev.icerock.moko.resources.compose.stringResource
import com.jalmarquest.ui.app.MR

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
                    contentDescription = stringResource(MR.strings.bottom_nav_hub),
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { Text(stringResource(MR.strings.bottom_nav_hub)) },
            selected = currentScreen is Screen.Hub,
            onClick = { onNavigate(Screen.Hub) }
        )
        
        NavigationBarItem(
            icon = { 
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = stringResource(MR.strings.bottom_nav_explore),
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { Text(stringResource(MR.strings.bottom_nav_explore)) },
            selected = currentScreen is Screen.Explore,
            onClick = { onNavigate(Screen.Explore) }
        )
        
        NavigationBarItem(
            icon = { 
                Icon(
                    imageVector = Icons.Default.House,
                    contentDescription = stringResource(MR.strings.bottom_nav_nest),
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { Text(stringResource(MR.strings.bottom_nav_nest)) },
            selected = currentScreen is Screen.Nest,
            onClick = { onNavigate(Screen.Nest) }
        )
        
        NavigationBarItem(
            icon = { 
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = stringResource(MR.strings.bottom_nav_skills),
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { Text(stringResource(MR.strings.bottom_nav_skills)) },
            selected = currentScreen is Screen.Skills,
            onClick = { onNavigate(Screen.Skills) }
        )
        
        NavigationBarItem(
            icon = { 
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = stringResource(MR.strings.bottom_nav_more),
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { Text(stringResource(MR.strings.bottom_nav_more)) },
            selected = currentScreen is Screen.Activities,
            onClick = { onNavigate(Screen.Activities) }
        )
    }
}
