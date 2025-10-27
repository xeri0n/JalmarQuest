package com.jalmarquest.ui.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jalmarquest.core.model.*
import kotlinx.coroutines.launch

/**
 * Main Quest section with tabbed interface.
 */
@Composable
fun QuestSection(
    controller: QuestController,
    modifier: Modifier = Modifier
) {
    val viewState by controller.viewState.collectAsState()
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(QuestTab.ACTIVE) }
    var showQuestDetails by remember { mutableStateOf(false) }
    
    // Show error dialog if present
    viewState.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { controller.clearError() },
            title = { Text("Quest Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { controller.clearError() }) {
                    Text("OK")
                }
            }
        )
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Text(
            text = "Quest Log",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        
        // Tab row
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            QuestTab.values().forEach { tab ->
                val count = when (tab) {
                    QuestTab.ACTIVE -> viewState.activeQuests.size
                    QuestTab.AVAILABLE -> viewState.availableQuests.size
                    QuestTab.COMPLETED -> viewState.completedQuests.size
                }
                
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text("${tab.name} ($count)") }
                )
            }
        }
        
        // Tab content
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                QuestTab.ACTIVE -> ActiveQuestsTab(
                    quests = viewState.activeQuests,
                    onQuestClick = { questWithDetails ->
                        controller.selectQuest(questWithDetails.quest.questId)
                        showQuestDetails = true
                    },
                    modifier = Modifier.fillMaxSize()
                )
                QuestTab.AVAILABLE -> AvailableQuestsTab(
                    quests = viewState.availableQuests,
                    onQuestClick = { questWithDetails ->
                        controller.selectQuest(questWithDetails.quest.questId)
                        showQuestDetails = true
                    },
                    modifier = Modifier.fillMaxSize()
                )
                QuestTab.COMPLETED -> CompletedQuestsTab(
                    quests = viewState.completedQuests,
                    onQuestClick = { questWithDetails ->
                        controller.selectQuest(questWithDetails.quest.questId)
                        showQuestDetails = true
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Quest detail overlay
            val selectedQuestSnapshot = viewState.selectedQuest
            if (showQuestDetails && selectedQuestSnapshot != null) {
                QuestDetailDialog(
                    questDetails = selectedQuestSnapshot,
                    onDismiss = {
                        showQuestDetails = false
                        controller.clearSelection()
                    },
                    onAccept = { questId ->
                        scope.launch {
                            controller.acceptQuest(questId)
                            showQuestDetails = false
                        }
                    },
                    onComplete = { questId ->
                        scope.launch {
                            controller.completeQuest(questId)
                            showQuestDetails = false
                        }
                    },
                    onAbandon = { questId ->
                        scope.launch {
                            controller.abandonQuest(questId)
                            showQuestDetails = false
                        }
                    }
                )
            }
        }
    }
}

/**
 * Tab showing active quests.
 */
@Composable
private fun ActiveQuestsTab(
    quests: List<QuestWithDetails>,
    onQuestClick: (QuestWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    if (quests.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No active quests.\nCheck the Available tab to start a new adventure!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        return
    }
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(quests) { questDetails ->
            ActiveQuestCard(
                questDetails = questDetails,
                onClick = { onQuestClick(questDetails) }
            )
        }
    }
}

/**
 * Card showing active quest summary.
 */
@Composable
private fun ActiveQuestCard(
    questDetails: QuestWithDetails,
    onClick: () -> Unit
) {
    val quest = questDetails.quest
    val progress = questDetails.progress
    val progressPercentage = questDetails.progressPercentage
    val canTurnIn = questDetails.canComplete
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (canTurnIn) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quest.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    quest.recommendedLevel?.let { level ->
                        Text(
                            text = "Recommended Level: $level",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (canTurnIn) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Ready to complete",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = progressPercentage / 100f,
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = if (canTurnIn) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "$progressPercentage%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Objectives summary (show first 2)
            val visibleObjectives = progress?.objectives
                ?.filter { !it.isHidden }
                ?.take(2) ?: emptyList()
            
            visibleObjectives.forEach { objective ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (objective.isComplete()) {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Default.Circle
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (objective.isComplete()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "${objective.description} (${objective.currentProgress}/${objective.targetQuantity})",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (objective.isComplete()) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            if ((progress?.objectives?.size ?: 0) > 2) {
                Text(
                    text = "+${(progress?.objectives?.size ?: 0) - 2} more objectives",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 24.dp)
                )
            }
        }
    }
}

/**
 * Tab showing available quests.
 */
@Composable
private fun AvailableQuestsTab(
    quests: List<QuestWithDetails>,
    onQuestClick: (QuestWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    if (quests.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No quests available right now.\nComplete prerequisites or explore more to unlock new quests!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        return
    }
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(quests) { questWithDetails ->
            AvailableQuestCard(
                quest = questWithDetails.quest,
                onClick = { onQuestClick(questWithDetails) }
            )
        }
    }
}

/**
 * Card showing available quest summary.
 */
@Composable
private fun AvailableQuestCard(
    quest: Quest,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quest.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    quest.recommendedLevel?.let { level ->
                        Text(
                            text = "Level $level",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "New quest",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = quest.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Reward preview
            val rewardSummary = quest.rewards.take(3).joinToString(", ") { reward ->
                when (reward.type) {
                    QuestRewardType.SEEDS -> "${reward.quantity} Seeds"
                    QuestRewardType.EXPERIENCE -> "${reward.quantity} XP"
                    else -> reward.description
                }
            }
            
            if (rewardSummary.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = "Rewards",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = rewardSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

/**
 * Tab showing completed quests.
 */
@Composable
private fun CompletedQuestsTab(
    quests: List<QuestWithDetails>,
    onQuestClick: (QuestWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    if (quests.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No completed quests yet.\nStart your adventure in the Active or Available tabs!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        return
    }
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(quests) { questWithDetails ->
            CompletedQuestCard(
                quest = questWithDetails.quest,
                onClick = { onQuestClick(questWithDetails) }
            )
        }
    }
}

/**
 * Card showing completed quest.
 */
@Composable
private fun CompletedQuestCard(
    quest: Quest,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quest.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

/**
 * Dialog showing full quest details with actions.
 */
@Composable
private fun QuestDetailDialog(
    questDetails: QuestWithDetails,
    onDismiss: () -> Unit,
    onAccept: (QuestId) -> Unit,
    onComplete: (QuestId) -> Unit,
    onAbandon: (QuestId) -> Unit
) {
    val quest = questDetails.quest
    val progress = questDetails.progress
    val isActive = progress?.status == QuestStatus.ACTIVE
    val isAvailable = progress == null
    val canTurnIn = questDetails.canComplete
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = quest.title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Description
                item {
                    Text(
                        text = quest.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Lore text if present
                quest.loreText?.let { lore ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = lore,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
                
                // Objectives
                item {
                    Text(
                        text = "Objectives:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(progress?.objectives?.filter { !it.isHidden } ?: emptyList()) { objective ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = if (objective.isComplete()) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.Default.Circle
                            },
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (objective.isComplete()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column {
                            Text(
                                text = objective.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (isActive) {
                                Text(
                                    text = "Progress: ${objective.currentProgress}/${objective.targetQuantity}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (objective.isOptional) {
                                Text(
                                    text = "(Optional)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
                
                // Rewards
                if (quest.rewards.isNotEmpty()) {
                    item {
                        Text(
                            text = "Rewards:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    items(quest.rewards) { reward ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = reward.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Quest giver info
                quest.questGiverNpc?.let { npc ->
                    item {
                        Text(
                            text = "Quest Giver: $npc",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            when {
                isAvailable -> {
                    Button(onClick = { onAccept(quest.questId) }) {
                        Text("Accept Quest")
                    }
                }
                canTurnIn -> {
                    Button(onClick = { onComplete(quest.questId) }) {
                        Text("Complete Quest")
                    }
                }
                isActive -> {
                    TextButton(onClick = { onAbandon(quest.questId) }) {
                        Text("Abandon", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
