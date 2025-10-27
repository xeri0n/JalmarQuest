package com.jalmarquest.ui.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jalmarquest.core.model.ItemStack
import com.jalmarquest.feature.activities.ActivitiesController
import com.jalmarquest.feature.activities.ActivityResolution
import com.jalmarquest.feature.activities.ActivityType
import com.jalmarquest.feature.activities.SecondaryActivity
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun SecondaryActivitiesSection(
    controller: ActivitiesController,
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()
    val synthesizer = rememberSpeechSynthesizer()
    val sectionTitle = stringResource(MR.strings.activities_section_title)
    val sectionPrompt = stringResource(MR.strings.activities_section_prompt)
    val attemptLabel = stringResource(MR.strings.activities_attempt_button)
    val resultHeader = stringResource(MR.strings.activities_last_result)
    val dismissLabel = stringResource(MR.strings.activities_acknowledge_button)
    val noResultLabel = stringResource(MR.strings.activities_result_none)

    LaunchedEffect(state.lastResolution) {
        state.lastResolution?.let { resolution ->
            synthesizer.safeSpeak(resultNarration(resolution))
        }
    }

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
            Text(text = sectionPrompt, style = MaterialTheme.typography.bodyMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.activities, key = { it.id.value }) { activity ->
                    ActivityCard(
                        activity = activity,
                        attemptLabel = attemptLabel,
                        onAttempt = { controller.attemptActivity(activity.id) }
                    )
                }
            }
            Divider()
            Text(text = resultHeader, style = MaterialTheme.typography.titleMedium)
            val resolution = state.lastResolution
            if (resolution == null) {
                Text(text = noResultLabel, style = MaterialTheme.typography.bodyMedium)
            } else {
                ResultCard(resolution = resolution)
                Button(onClick = { controller.clearResolution() }) {
                    Text(text = dismissLabel)
                }
            }
        }
    }
}

@Composable
private fun ActivityCard(
    activity: SecondaryActivity,
    attemptLabel: String,
    onAttempt: () -> Unit
) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = activityTitle(activity), style = MaterialTheme.typography.titleMedium)
            Text(text = activityDescription(activity), style = MaterialTheme.typography.bodyMedium)
            RewardSummary(activity.reward.items, activity.reward.statusEffectKey)
            Button(onClick = onAttempt) {
                Text(text = attemptLabel)
            }
        }
    }
}

@Composable
private fun RewardSummary(items: List<ItemStack>, statusEffectKey: String?) {
    val rewardHeader = stringResource(MR.strings.activities_reward_header)
    val noRewards = stringResource(MR.strings.activities_reward_none)
    Text(text = rewardHeader, style = MaterialTheme.typography.titleSmall)
    if (items.isEmpty() && statusEffectKey == null) {
        Text(text = noRewards, style = MaterialTheme.typography.bodySmall)
        return
    }
    if (items.isNotEmpty()) {
        val itemsLabel = stringResource(
            MR.strings.activities_reward_items,
            items.joinToString { stack -> rewardItemDescription(stack) }
        )
        Text(text = itemsLabel, style = MaterialTheme.typography.bodySmall)
    }
    statusEffectKey?.let {
        val statusLabel = stringResource(MR.strings.activities_reward_status, prettifyKey(it))
        Text(text = statusLabel, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ResultCard(resolution: ActivityResolution) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = resultTitle(resolution.type), style = MaterialTheme.typography.titleSmall)
            Text(
                text = stringResource(
                    MR.strings.activities_result_default,
                    activityName(resolution.type)
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            if (resolution.awardedItems.isNotEmpty()) {
                Text(
                    text = stringResource(
                        MR.strings.activities_result_items,
                        resolution.awardedItems.joinToString { stack -> rewardItemDescription(stack) }
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            resolution.appliedStatusEffect?.let {
                Text(
                    text = stringResource(
                        MR.strings.activities_result_status,
                        prettifyKey(it)
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun rewardItemDescription(stack: ItemStack): String = "${stack.quantity}× ${prettifyKey(stack.id.value)}"

private fun prettifyKey(raw: String): String = raw.replace('_', ' ').replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

@Composable
private fun activityTitle(activity: SecondaryActivity): String = stringByKey(activity.titleKey)

@Composable
private fun activityDescription(activity: SecondaryActivity): String = stringByKey(activity.descriptionKey)

@Composable
private fun stringByKey(key: String): String = when (key) {
    "hub_activity_dungeon_title" -> stringResource(MR.strings.hub_activity_dungeon_title)
    "hub_activity_dungeon_description" -> stringResource(MR.strings.hub_activity_dungeon_description)
    "hub_activity_arena_title" -> stringResource(MR.strings.hub_activity_arena_title)
    "hub_activity_arena_description" -> stringResource(MR.strings.hub_activity_arena_description)
    "hub_activity_nest_defense_title" -> stringResource(MR.strings.hub_activity_nest_defense_title)
    "hub_activity_nest_defense_description" -> stringResource(MR.strings.hub_activity_nest_defense_description)
    "hub_activity_apex_title" -> stringResource(MR.strings.hub_activity_apex_title)
    "hub_activity_apex_description" -> stringResource(MR.strings.hub_activity_apex_description)
    else -> key
}

@Composable
private fun resultTitle(type: ActivityType): String = when (type) {
    ActivityType.DUNGEON -> stringResource(MR.strings.activities_result_title_dungeon)
    ActivityType.ARENA -> stringResource(MR.strings.activities_result_title_arena)
    ActivityType.NEST_DEFENSE -> stringResource(MR.strings.activities_result_title_nest_defense)
    ActivityType.APEX_PREDATOR_TEASER -> stringResource(MR.strings.activities_result_title_apex)
}

@Composable
private fun activityName(type: ActivityType): String = when (type) {
    ActivityType.DUNGEON -> stringResource(MR.strings.hub_activity_dungeon_title)
    ActivityType.ARENA -> stringResource(MR.strings.hub_activity_arena_title)
    ActivityType.NEST_DEFENSE -> stringResource(MR.strings.hub_activity_nest_defense_title)
    ActivityType.APEX_PREDATOR_TEASER -> stringResource(MR.strings.hub_activity_apex_title)
}

private fun resultNarration(resolution: ActivityResolution): String {
    val activityName = resolution.type.name.lowercase().replace('_', ' ')
    val rewards = buildList {
        if (resolution.awardedItems.isNotEmpty()) {
            add(
                resolution.awardedItems.joinToString { stack ->
                    "${stack.quantity} ${stack.id.value.replace('_', ' ')}"
                }
            )
        }
        resolution.appliedStatusEffect?.let { add(it.replace('_', ' ')) }
    }
    val rewardPhrase = if (rewards.isEmpty()) "" else "; rewards: ${rewards.joinToString()}"
    return "Secondary activity ${activityName}$rewardPhrase"
}
