package com.jalmarquest.ui.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.auth.AuthController
import com.jalmarquest.core.state.concoctions.ConcoctionCrafter
import com.jalmarquest.core.state.hoard.HoardRankManager
import com.jalmarquest.core.state.managers.NestCustomizationManager
import dev.icerock.moko.resources.compose.stringResource
import com.jalmarquest.ui.app.MR

enum class HubFeature(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color
) {
    EXPLORE(
        "Explore",
        "Venture into the world",
        Icons.Outlined.Explore,
        androidx.compose.ui.graphics.Color(0xFF4CAF50)
    ),
    QUESTS(
        "Quests",
        "Track your adventures",
        Icons.Outlined.Flag,
        androidx.compose.ui.graphics.Color(0xFFFFC107)
    ),
    NEST(
        "My Nest",
        "Manage your home",
        Icons.Outlined.Home,
        androidx.compose.ui.graphics.Color(0xFF795548)
    ),
    HOARD(
        "Shinies Hoard",
        "Collect rare treasures",
        Icons.Outlined.Star,
        androidx.compose.ui.graphics.Color(0xFFFFEB3B)
    ),
    CONCOCTIONS(
        "Concoctions",
        "Brew magical potions",
        Icons.Outlined.LocalDrink,
        androidx.compose.ui.graphics.Color(0xFF9C27B0)
    ),
    SKILLS(
        "Skills",
        "Grow your abilities",
        Icons.Outlined.AutoAwesome,
        androidx.compose.ui.graphics.Color(0xFF2196F3)
    ),
    WORLD_MAP(
        "World Map",
        "Navigate the realm",
        Icons.Outlined.Map,
        androidx.compose.ui.graphics.Color(0xFF00BCD4)
    ),
    CHRONICLE(
        "Seasonal Chronicle",
        "Complete challenges",
        Icons.Outlined.EmojiEvents,
        androidx.compose.ui.graphics.Color(0xFFFF5722)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHubScreen(
    playerName: String,
    authController: AuthController,
    hoardManager: HoardRankManager,
    worldMapNavigationManager: com.jalmarquest.core.state.worldmap.WorldMapNavigationManager,
    concoctionCrafter: ConcoctionCrafter,
    questController: QuestController,
    nestCustomizationManager: NestCustomizationManager,
    gameStateManager: GameStateManager
) {
    var selectedFeature by remember { mutableStateOf<HubFeature?>(null) }
    val signOut = stringResource(MR.strings.auth_sign_out)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(MR.strings.hub_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(MR.strings.hub_welcome, playerName),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { authController.signOut() }) {
                        Icon(
                            imageVector = Icons.Outlined.ExitToApp,
                            contentDescription = signOut
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (selectedFeature == null) {
            // Main hub grid view
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Quick stats banner
                PlayerStatsCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    gameStateManager = gameStateManager
                )

                // Feature grid
                Text(
                    text = stringResource(MR.strings.hub_prompt_choose_action),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.SemiBold
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(600.dp) // Fixed height for grid
                ) {
                    items(HubFeature.entries) { feature ->
                        FeatureCard(
                            feature = feature,
                            onClick = { selectedFeature = feature }
                        )
                    }
                }
            }
        } else {
            // Feature detail view
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Back button
                OutlinedButton(
                    onClick = { selectedFeature = null },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(MR.strings.content_desc_back),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(MR.strings.hub_back_to_hub))
                }

                // Feature content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    FeatureContent(
                        feature = selectedFeature!!,
                        hoardManager = hoardManager,
                        worldMapNavigationManager = worldMapNavigationManager,
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
private fun PlayerStatsCard(
    modifier: Modifier = Modifier,
    gameStateManager: GameStateManager
) {
    val playerState by gameStateManager.playerState.collectAsState()
    val player = playerState

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                icon = Icons.Outlined.AutoAwesome,
                label = stringResource(MR.strings.label_skills),
                value = player.skillTree.totalSkillPoints.toString()
            )
            StatItem(
                icon = Icons.Outlined.Inventory,
                label = stringResource(MR.strings.label_items),
                value = player.inventory.items.size.toString()
            )
            StatItem(
                icon = Icons.Outlined.Star,
                label = stringResource(MR.strings.label_shinies),
                value = player.shinyCollection.ownedShinies.size.toString()
            )
            StatItem(
                icon = Icons.Outlined.Flag,
                label = stringResource(MR.strings.label_quests),
                value = player.questLog.activeQuests.size.toString()
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun FeatureCard(
    feature: HubFeature,
    onClick: () -> Unit
) {
    val title = when (feature) {
        HubFeature.EXPLORE -> stringResource(MR.strings.feature_explore_title)
        HubFeature.QUESTS -> stringResource(MR.strings.feature_quests_title)
        HubFeature.NEST -> stringResource(MR.strings.feature_nest_title)
        HubFeature.HOARD -> stringResource(MR.strings.feature_hoard_title)
        HubFeature.CONCOCTIONS -> stringResource(MR.strings.feature_concoctions_title)
        HubFeature.SKILLS -> stringResource(MR.strings.feature_skills_title)
        HubFeature.WORLD_MAP -> stringResource(MR.strings.feature_world_map_title)
        HubFeature.CHRONICLE -> stringResource(MR.strings.feature_chronicle_title)
    }
    val description = when (feature) {
        HubFeature.EXPLORE -> stringResource(MR.strings.feature_explore_desc)
        HubFeature.QUESTS -> stringResource(MR.strings.feature_quests_desc)
        HubFeature.NEST -> stringResource(MR.strings.feature_nest_desc)
        HubFeature.HOARD -> stringResource(MR.strings.feature_hoard_desc)
        HubFeature.CONCOCTIONS -> stringResource(MR.strings.feature_concoctions_desc)
        HubFeature.SKILLS -> stringResource(MR.strings.feature_skills_desc)
        HubFeature.WORLD_MAP -> stringResource(MR.strings.feature_world_map_desc)
        HubFeature.CHRONICLE -> stringResource(MR.strings.feature_chronicle_desc)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = feature.color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(feature.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = title,
                    tint = feature.color,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun FeatureContent(
    feature: HubFeature,
    hoardManager: HoardRankManager,
    worldMapNavigationManager: com.jalmarquest.core.state.worldmap.WorldMapNavigationManager,
    concoctionCrafter: ConcoctionCrafter,
    questController: QuestController,
    nestCustomizationManager: NestCustomizationManager,
    gameStateManager: GameStateManager
) {
    when (feature) {
        HubFeature.HOARD -> HoardSection(manager = hoardManager)
        HubFeature.CONCOCTIONS -> ConcoctionsSection(manager = concoctionCrafter)
        HubFeature.QUESTS -> QuestSection(controller = questController)
        HubFeature.NEST -> NestScreen(
            customizationManager = nestCustomizationManager,
            gameStateManager = gameStateManager
        )
        HubFeature.WORLD_MAP -> WorldMapScreen(
            worldMapNavigationManager = worldMapNavigationManager,
            onBack = { /* Will be handled by parent */ }
        )
        else -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(MR.strings.feature_coming_soon_title, when (feature) {
                        HubFeature.EXPLORE -> stringResource(MR.strings.feature_explore_title)
                        HubFeature.QUESTS -> stringResource(MR.strings.feature_quests_title)
                        HubFeature.NEST -> stringResource(MR.strings.feature_nest_title)
                        HubFeature.HOARD -> stringResource(MR.strings.feature_hoard_title)
                        HubFeature.CONCOCTIONS -> stringResource(MR.strings.feature_concoctions_title)
                        HubFeature.SKILLS -> stringResource(MR.strings.feature_skills_title)
                        HubFeature.WORLD_MAP -> stringResource(MR.strings.feature_world_map_title)
                        HubFeature.CHRONICLE -> stringResource(MR.strings.feature_chronicle_title)
                    }),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(MR.strings.feature_coming_soon_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}
