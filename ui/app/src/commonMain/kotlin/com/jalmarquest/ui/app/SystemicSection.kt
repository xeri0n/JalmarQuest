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
import com.jalmarquest.feature.systemic.ContextualOption
import com.jalmarquest.feature.systemic.SystemicInteractionController
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun SystemicSection(controller: SystemicInteractionController) {
    val state by controller.state.collectAsState()
    val sectionTitle = stringResource(MR.strings.systemic_section_title)
    val refreshLabel = stringResource(MR.strings.systemic_refresh_button)
    val unavailableLabel = stringResource(MR.strings.systemic_unavailable_label)
    val lastEventSuccess = stringResource(MR.strings.systemic_last_event_success)
    val lastEventFailure = stringResource(MR.strings.systemic_last_event_failure)

    LaunchedEffect(controller) {
        controller.updateEnvironmentTags(setOf("workbench", "resting_spot", "moonlit_perch"))
    }

    Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 2.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = sectionTitle, style = MaterialTheme.typography.titleMedium)
            Button(onClick = { controller.refreshOptions() }) {
                Text(text = refreshLabel)
            }
            state.options.forEach { option ->
                InteractionOptionRow(option = option, unavailableLabel = unavailableLabel) {
                    controller.attemptInteraction(option.interactionId.value)
                }
            }
            state.lastEvent?.let { event ->
                Divider()
                val header = if (event.success) lastEventSuccess else lastEventFailure
                Text(text = header, style = MaterialTheme.typography.titleSmall)
                Text(text = event.message.value, style = MaterialTheme.typography.bodyMedium)
                if (event.reasons.isNotEmpty()) {
                    event.reasons.forEach { reason ->
                        Text(text = reason.value, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractionOptionRow(
    option: ContextualOption,
    unavailableLabel: String,
    onExecute: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = option.title.value, style = MaterialTheme.typography.bodyLarge)
        Text(text = option.availabilityMessage.value, style = MaterialTheme.typography.bodyMedium)
        if (option.available) {
            Button(onClick = onExecute) {
                Text(text = option.title.value)
            }
        } else {
            Text(text = unavailableLabel, style = MaterialTheme.typography.bodySmall)
            option.blockedReasons.forEach { reason ->
                Text(text = "• ${reason.value}", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Divider()
    }
}
