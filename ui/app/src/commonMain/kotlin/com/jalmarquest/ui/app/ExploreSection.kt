package com.jalmarquest.ui.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.jalmarquest.feature.explore.ExploreController
import com.jalmarquest.feature.explore.ExploreHistoryEntry
import com.jalmarquest.feature.explore.ExplorePhase
import com.jalmarquest.feature.explore.ExploreState
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ExploreSection(controller: ExploreController) {
    val state by controller.state.collectAsState()
    val synthesizer = rememberSpeechSynthesizer()
    val sectionTitle = stringResource(MR.strings.explore_section_title)
    val beginLabel = stringResource(MR.strings.explore_begin_button)
    val continueLabel = stringResource(MR.strings.explore_continue_button)
    val loadingLabel = stringResource(MR.strings.explore_loading)
    val historyHeader = stringResource(MR.strings.explore_history_header)
    val historyEmptyLabel = stringResource(MR.strings.explore_history_empty)
    val idleNarration = stringResource(MR.strings.explore_idle_narration)
    val idlePrompt = stringResource(MR.strings.explore_idle_prompt)
    val rewardPrefix = stringResource(MR.strings.explore_reward_prefix)
    val chapterHeader = stringResource(MR.strings.explore_chapter_header)

    LaunchedEffect(state.phase) {
        val narration = when (val phase = state.phase) {
            ExplorePhase.Idle -> idleNarration
            ExplorePhase.Loading -> loadingLabel
            is ExplorePhase.Encounter -> phase.snippet.eventText
            is ExplorePhase.Chapter -> phase.response.worldEventSummary
            is ExplorePhase.Resolution -> buildString {
                phase.summary.choiceText?.let { append(it).append('.').append(' ') }
                append(phase.summary.rewardSummaries.joinToString(", "))
            }
            is ExplorePhase.Error -> phase.message
            is ExplorePhase.RestNeeded -> "You need to rest. You've experienced ${phase.eventsSinceRest} events."
        }
        synthesizer.safeSpeak(narration)
    }

    Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 2.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = sectionTitle, style = MaterialTheme.typography.titleMedium)
            when (val phase = state.phase) {
                ExplorePhase.Idle -> {
                    Text(text = idlePrompt)
                    Button(onClick = { controller.beginExploration() }) {
                        Text(text = beginLabel)
                    }
                }
                ExplorePhase.Loading -> {
                    Text(text = loadingLabel)
                }
                is ExplorePhase.Encounter -> {
                    Text(text = phase.snippet.eventText)
                    Spacer(modifier = Modifier.height(8.dp))
                    phase.snippet.choiceOptions.forEachIndexed { index, option ->
                        Button(
                            onClick = { controller.chooseOption(index) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = option)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                is ExplorePhase.Chapter -> {
                    Text(text = chapterHeader)
                    Text(text = phase.response.worldEventTitle, style = MaterialTheme.typography.titleSmall)
                    Text(text = phase.response.worldEventSummary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { controller.continueAfterResolution() }) {
                        Text(text = continueLabel)
                    }
                }
                is ExplorePhase.Resolution -> {
                    Text(text = phase.summary.title, style = MaterialTheme.typography.titleSmall)
                    phase.summary.choiceText?.let { Text(text = it) }
                    if (phase.summary.rewardSummaries.isNotEmpty()) {
                        Text(text = rewardPrefix)
                        phase.summary.rewardSummaries.forEach { reward ->
                            Text(text = "• $reward")
                        }
                    }
                    Text(text = stringResource(MR.strings.explore_autosave, phase.summary.autosaveTag))
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { controller.continueAfterResolution() }) {
                        Text(text = continueLabel)
                    }
                }
                is ExplorePhase.Error -> {
                    Text(text = phase.message)
                    Button(onClick = { controller.beginExploration() }) {
                        Text(text = beginLabel)
                    }
                }
                is ExplorePhase.RestNeeded -> {
                    Text(text = "You need to rest")
                    Text(text = "You've experienced ${phase.eventsSinceRest} events without rest. Take a moment to recover.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { controller.rest() }) {
                        Text(text = "Rest")
                    }
                }
            }

            Divider()

            if (state.history.isEmpty()) {
                Text(text = historyEmptyLabel)
            } else {
                Text(text = historyHeader, style = MaterialTheme.typography.titleSmall)
                state.history.takeLast(4).asReversed().forEach { entry ->
                    HistoryEntry(entry)
                }
            }
        }
    }
}

@Composable
private fun HistoryEntry(entry: ExploreHistoryEntry) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = entry.title, style = MaterialTheme.typography.bodyLarge)
        Text(text = entry.narratedSummary, style = MaterialTheme.typography.bodyMedium)
        entry.choiceSummary?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall)
        }
        if (entry.rewardSummaries.isNotEmpty()) {
            entry.rewardSummaries.forEach { reward ->
                Text(text = reward, style = MaterialTheme.typography.bodySmall)
            }
        }
        Text(text = stringResource(MR.strings.explore_autosave, entry.autosaveTag), style = MaterialTheme.typography.labelSmall)
        Divider(modifier = Modifier.padding(vertical = 4.dp))
    }
}
