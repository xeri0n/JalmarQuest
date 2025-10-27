package com.jalmarquest.ui.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.jalmarquest.feature.hub.HubAction
import com.jalmarquest.feature.hub.HubActionType
import com.jalmarquest.feature.hub.HubController
import com.jalmarquest.feature.hub.HubLocation
import com.jalmarquest.feature.hub.HubLocationId
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun HubSection(
    controller: HubController,
    modifier: Modifier = Modifier,
    actionContent: @Composable (HubActionType) -> Unit = {}
) {
    val state by controller.state.collectAsState()
    val sectionTitle = stringResource(MR.strings.hub_section_title)
    val overviewPrompt = stringResource(MR.strings.hub_overview_prompt)
    val backLabel = stringResource(MR.strings.hub_back_to_locations)
    val closeAction = stringResource(MR.strings.hub_close_action)

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = sectionTitle, style = MaterialTheme.typography.titleLarge)
            val selected = state.selectedLocation()
            val action = state.activeAction
            if (selected == null) {
                Text(text = overviewPrompt, style = MaterialTheme.typography.bodyMedium)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.locations, key = { it.id.value }) { location ->
                        LocationCard(
                            location = location,
                            onClick = { controller.selectLocation(location.id) }
                        )
                    }
                }
            } else {
                SelectedLocationView(
                    location = selected,
                    onSelectAction = { controller.selectAction(it.id) }
                )
                action?.let { active ->
                    Divider()
                    ActionHeader(active)
                    Spacer(modifier = Modifier.height(12.dp))
                    actionContent(active.type)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { controller.closeAction() }) {
                        Text(text = closeAction)
                    }
                }
                Button(onClick = { controller.returnToOverview() }) {
                    Text(text = backLabel)
                }
            }
        }
    }
}

@Composable
private fun LocationCard(
    location: HubLocation,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = iconForLocation(location.id),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = locationName(location.id),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = locationDescription(location.id),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SelectedLocationView(
    location: HubLocation,
    onSelectAction: (HubAction) -> Unit
) {
    Text(
        text = locationName(location.id),
        style = MaterialTheme.typography.titleMedium
    )
    Text(
        text = locationDescription(location.id),
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(modifier = Modifier.height(8.dp))
    if (location.actionOrder.isEmpty()) {
        Text(text = stringResource(MR.strings.hub_no_actions_available))
    } else {
        Text(text = stringResource(MR.strings.hub_action_prompt))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            location.actionOrder.forEach { action ->
                Button(onClick = { onSelectAction(action) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = iconForAction(action.type),
                            contentDescription = null
                        )
                        Column {
                            Text(text = actionLabel(action.type))
                            Text(
                                text = actionDescription(action.type),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionHeader(action: HubAction) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(imageVector = iconForAction(action.type), contentDescription = null)
        Column {
            Text(text = actionLabel(action.type), style = MaterialTheme.typography.titleMedium)
            Text(
                text = actionDescription(action.type),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun locationName(id: HubLocationId): String = when (id.value) {
    "pack_rat_hoard" -> stringResource(MR.strings.hub_location_pack_rat_hoard_name)
    "quailsmith" -> stringResource(MR.strings.hub_location_quailsmith_name)
    "quill_study" -> stringResource(MR.strings.hub_location_quill_study_name)
    "hen_pen" -> stringResource(MR.strings.hub_location_hen_pen_name)
    else -> id.value
}

@Composable
private fun locationDescription(id: HubLocationId): String = when (id.value) {
    "pack_rat_hoard" -> stringResource(MR.strings.hub_location_pack_rat_hoard_description)
    "quailsmith" -> stringResource(MR.strings.hub_location_quailsmith_description)
    "quill_study" -> stringResource(MR.strings.hub_location_quill_study_description)
    "hen_pen" -> stringResource(MR.strings.hub_location_hen_pen_description)
    else -> ""
}

@Composable
private fun iconForLocation(id: HubLocationId): ImageVector = when (id.value) {
    "pack_rat_hoard" -> Icons.Outlined.Storefront
    "quailsmith" -> Icons.Outlined.Construction
    "quill_study" -> Icons.Outlined.Book
    "hen_pen" -> Icons.Outlined.Home
    else -> Icons.Outlined.Explore
}

@Composable
private fun iconForAction(type: HubActionType): ImageVector = when (type) {
    HubActionType.SHOP -> Icons.Outlined.Storefront
    HubActionType.CHRONICLE -> Icons.Outlined.Book
    HubActionType.CRAFT -> Icons.Outlined.Construction
    HubActionType.NEST -> Icons.Outlined.Home
    HubActionType.SYSTEMIC -> Icons.Outlined.AutoAwesome
    HubActionType.EXPLORE -> Icons.Outlined.Explore
    HubActionType.ACTIVITIES -> Icons.Outlined.Flag
    HubActionType.HOARD -> Icons.Outlined.Star
    HubActionType.CONCOCTIONS -> Icons.Outlined.LocalDrink
    HubActionType.THOUGHTS -> Icons.Outlined.Psychology
    HubActionType.SKILLS -> Icons.Outlined.AutoAwesome
    HubActionType.QUESTS -> Icons.Outlined.Flag
}

@Composable
private fun actionLabel(type: HubActionType): String = when (type) {
    HubActionType.SHOP -> stringResource(MR.strings.hub_action_shop_title)
    HubActionType.CHRONICLE -> stringResource(MR.strings.hub_action_chronicle_title)
    HubActionType.CRAFT -> stringResource(MR.strings.hub_action_craft_title)
    HubActionType.NEST -> stringResource(MR.strings.hub_action_nest_title)
    HubActionType.SYSTEMIC -> stringResource(MR.strings.hub_action_systemic_title)
    HubActionType.EXPLORE -> stringResource(MR.strings.hub_action_explore_title)
    HubActionType.ACTIVITIES -> stringResource(MR.strings.hub_action_secondary_title)
    HubActionType.HOARD -> "Hoard" // TODO: Add localization
    HubActionType.CONCOCTIONS -> "Concoctions" // TODO: Add localization
    HubActionType.THOUGHTS -> "Thought Cabinet" // TODO: Add localization
    HubActionType.SKILLS -> "Skills" // TODO: Add localization
    HubActionType.QUESTS -> "Quests" // TODO: Add localization
}

@Composable
private fun actionDescription(type: HubActionType): String = when (type) {
    HubActionType.SHOP -> stringResource(MR.strings.hub_action_shop_description)
    HubActionType.CHRONICLE -> stringResource(MR.strings.hub_action_chronicle_description)
    HubActionType.CRAFT -> stringResource(MR.strings.hub_action_craft_description)
    HubActionType.NEST -> stringResource(MR.strings.hub_action_nest_description)
    HubActionType.SYSTEMIC -> stringResource(MR.strings.hub_action_systemic_description)
    HubActionType.EXPLORE -> stringResource(MR.strings.hub_action_explore_description)
    HubActionType.ACTIVITIES -> stringResource(MR.strings.hub_action_secondary_description)
    HubActionType.HOARD -> "Manage your collection of Shinies" // TODO: Add localization
    HubActionType.CONCOCTIONS -> "Brew potions and gather ingredients" // TODO: Add localization
    HubActionType.THOUGHTS -> "Internalize philosophical concepts" // TODO: Add localization
    HubActionType.SKILLS -> "Level up your abilities and unlock talents" // TODO: Add localization
    HubActionType.QUESTS -> "Track and complete quests" // TODO: Add localization
}
