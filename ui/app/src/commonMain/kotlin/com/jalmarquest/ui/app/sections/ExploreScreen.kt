package com.jalmarquest.ui.app.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.catalogs.LocationCatalog
import com.jalmarquest.core.state.catalogs.WorldRegionCatalog
import com.jalmarquest.feature.explore.ExploreController
import com.jalmarquest.feature.explore.ExplorePhase
import com.jalmarquest.ui.app.layout.AppSpacing
import dev.icerock.moko.resources.compose.stringResource
import com.jalmarquest.ui.app.MR

/**
 * Full-screen exploration UI with location context.
 * 
 * Phase 6.5 Enhancement:
 * - Displays current location and biome
 * - Shows region context during encounters
 * - Location-aware encounter filtering happens in backend
 */
@Composable
fun ExploreScreen(
    controller: ExploreController,
    gameStateManager: GameStateManager,
    locationCatalog: LocationCatalog,
    regionCatalog: WorldRegionCatalog,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()
    val player by gameStateManager.playerState.collectAsState()
    
    // Get current location details
    val currentLocationId = player.worldExploration.currentLocationId
    val currentLocation = remember(currentLocationId) {
        locationCatalog.getLocationById(currentLocationId)
    }
    val currentRegion = remember(currentLocation) {
        currentLocation?.let { loc ->
            regionCatalog.getAllRegions().find { region ->
                region.primaryLocationIds.contains(loc.id)
            }
        }
    }
    val biomeName = currentRegion?.biomeType?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: stringResource(MR.strings.common_unknown)
    
    Scaffold(
        topBar = {
            ExploreTopBar(
                locationName = currentLocation?.name ?: stringResource(MR.strings.common_unknown_location),
                biomeName = biomeName,
                regionName = currentRegion?.name ?: "",
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            when (val phase = state.phase) {
                ExplorePhase.Idle -> IdleView(
                    onBeginExploration = { controller.beginExploration() }
                )
                
                ExplorePhase.Loading -> LoadingView()
                
                is ExplorePhase.Encounter -> EncounterView(
                    snippet = phase.snippet,
                    onChooseOption = { index -> controller.chooseOption(index) }
                )
                
                is ExplorePhase.Chapter -> ChapterView(
                    response = phase.response,
                    onChooseOption = { index -> controller.chooseOption(index) }
                )
                
                is ExplorePhase.Resolution -> ResolutionView(
                    summary = phase.summary,
                    onContinue = { controller.continueAfterResolution() }
                )
                
                is ExplorePhase.Error -> ErrorView(
                    message = phase.message,
                    onRetry = { controller.beginExploration() }
                )
                
                is ExplorePhase.RestNeeded -> RestNeededView(
                    eventsSinceRest = phase.eventsSinceRest,
                    onRest = { controller.rest() }
                )
            }
            
            // Exploration history
            if (state.history.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AppSpacing.large))
                Text(
                    text = stringResource(MR.strings.explore_history_header),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                state.history.takeLast(3).forEach { entry ->
                    HistoryEntryCard(entry)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExploreTopBar(
    locationName: String,
    biomeName: String,
    regionName: String,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = locationName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (regionName.isNotEmpty()) {
                    Text(
                        text = "$regionName • $biomeName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(MR.strings.content_desc_back))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun IdleView(onBeginExploration: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            Icon(
                imageVector = Icons.Default.Explore,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(MR.strings.explore_ready_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(MR.strings.explore_ready_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Button(
                onClick = onBeginExploration,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(stringResource(MR.strings.explore_begin_button))
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(MR.strings.explore_searching_area),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun EncounterView(
    snippet: com.jalmarquest.core.model.LoreSnippet,
    onChooseOption: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            // Event title
            Text(
                text = snippet.eventText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Divider()
            
            // Choice options
            Text(
                text = stringResource(MR.strings.explore_what_will_you_do),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            snippet.choiceOptions.forEachIndexed { index, choice ->
                OutlinedButton(
                    onClick = { onChooseOption(index) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp)
                ) {
                    Text(
                        text = choice,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ChapterView(
    response: com.jalmarquest.core.model.ChapterEventResponse,
    onChooseOption: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            // AI Director badge
            Surface(
                color = MaterialTheme.colorScheme.tertiary,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(MR.strings.explore_ai_director_badge),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = response.worldEventTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = response.worldEventSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            
            Divider()
            
            // Use first snippet for choices
            response.snippets.firstOrNull()?.let { snippet ->
                Text(
                    text = stringResource(MR.strings.explore_choose_your_path),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                snippet.choiceOptions.forEachIndexed { index, choice ->
                    OutlinedButton(
                        onClick = { onChooseOption(index) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Text(text = choice)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResolutionView(
    summary: com.jalmarquest.feature.explore.ResolutionSummary,
    onContinue: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = summary.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            summary.choiceText?.let {
                Text(
                    text = stringResource(MR.strings.explore_you_chose, it),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (summary.rewardSummaries.isNotEmpty()) {
                Divider()
                Text(
                    text = stringResource(MR.strings.explore_rewards_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                summary.rewardSummaries.forEach { reward ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = reward,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(stringResource(MR.strings.explore_continue_button))
            }
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = stringResource(MR.strings.common_error),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(MR.strings.common_retry))
            }
        }
    }
}

@Composable
private fun RestNeededView(
    eventsSinceRest: Int,
    onRest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            Text(
                text = "Time to Rest",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "You've experienced $eventsSinceRest events without rest. Take a moment to recover your energy.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(onClick = onRest) {
                Text("Rest and Recover")
            }
        }
    }
}

@Composable
private fun HistoryEntryCard(entry: com.jalmarquest.feature.explore.ExploreHistoryEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.small),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            entry.choiceSummary?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
