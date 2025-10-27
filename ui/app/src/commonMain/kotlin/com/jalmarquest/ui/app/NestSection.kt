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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jalmarquest.core.di.resolveNestConfig
import com.jalmarquest.feature.nest.NestConfig
import com.jalmarquest.feature.nest.NestController
import com.jalmarquest.feature.nest.availableCapacity
import com.jalmarquest.feature.nest.canUpgrade
import com.jalmarquest.feature.nest.passiveSeedsPerHour
import com.jalmarquest.core.model.NestAssignment
import com.jalmarquest.core.model.NestState
import com.jalmarquest.core.model.NestLevel
import com.jalmarquest.core.model.CritterTemperament
import com.jalmarquest.core.model.CritterRole
import dev.icerock.moko.resources.compose.stringResource
import kotlin.math.max
import kotlinx.datetime.Clock

@Composable
fun NestSection(
    controller: NestController,
    modifier: Modifier = Modifier,
    config: NestConfig = resolveNestConfig()
) {
    DisposableEffect(controller) {
        controller.start()
        onDispose { controller.stop() }
    }

    val nestState by controller.state.collectAsState()
    val passiveRate = nestState.passiveSeedsPerHour(config)
    val allowedRoles = config.specFor(nestState.level).allowedRoles
    val upgradeStatus = nestState.upgradeStatus
    val seedsLabel = stringResource(MR.strings.nest_seed_stock_label, nestState.seedStock)
    val passiveLabel = stringResource(MR.strings.nest_passive_rate_label, passiveRate)
    val assignmentHeader = stringResource(MR.strings.nest_assignments_header)
    val recruitmentHeader = stringResource(MR.strings.nest_recruitment_header)
    val sectionTitle = stringResource(MR.strings.nest_section_title)
    val emptyAssignments = stringResource(MR.strings.nest_no_assignments)
    val emptyOffers = stringResource(MR.strings.nest_no_offers)
    val upgradeCta = stringResource(MR.strings.nest_upgrade_button)
    val refreshCta = stringResource(MR.strings.nest_refresh_button)
    val levelLabel = stringResource(MR.strings.nest_level_label, nestState.level.readableName())

    val upgradeInProgressText = upgradeStatus.completesAtMillis?.let { completesAt ->
        val now = Clock.System.now().toEpochMilliseconds()
        val remainingMinutes = max(0, ((completesAt - now) / 60_000L).toInt())
        stringResource(MR.strings.nest_upgrade_in_progress, remainingMinutes)
    }

    Surface(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = sectionTitle, style = MaterialTheme.typography.titleLarge)
            Text(text = levelLabel, style = MaterialTheme.typography.bodyMedium)
            Text(text = seedsLabel, style = MaterialTheme.typography.bodyLarge)
            Text(text = passiveLabel, style = MaterialTheme.typography.bodyMedium)

            if (upgradeStatus.inProgress) {
                upgradeInProgressText?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
            } else if (nestState.canUpgrade(config)) {
                Button(onClick = { controller.requestUpgrade() }) {
                    Text(text = upgradeCta)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { controller.refreshRecruitment() }) {
                    Text(text = refreshCta)
                }
                Button(onClick = { controller.manualTick() }) {
                    Text(text = stringResource(MR.strings.nest_tick_button))
                }
            }

            Divider()
            Text(text = assignmentHeader, style = MaterialTheme.typography.titleMedium)
            if (nestState.assignments.isEmpty()) {
                Text(text = emptyAssignments, style = MaterialTheme.typography.bodyMedium)
            } else {
                AssignmentList(assignments = nestState.assignments, onUnassign = controller::unassign)
            }

            Divider()
            Text(text = recruitmentHeader, style = MaterialTheme.typography.titleMedium)

            if (nestState.recruitmentPool.isEmpty()) {
                Text(text = emptyOffers, style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(nestState.recruitmentPool, key = { it.id }) { offer ->
                        RecruitmentRow(
                            offer = offer,
                            allowedRoles = allowedRoles,
                            capacityRemaining = nestState.availableCapacity(config),
                            onRecruit = { role -> controller.recruit(offer.id, role) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignmentList(assignments: List<NestAssignment>, onUnassign: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        assignments.forEach { assignment ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = stringResource(
                        MR.strings.nest_assignment_role,
                        assignment.role.readableName(),
                        assignment.critter.displayName
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(onClick = { onUnassign(assignment.slotId) }) {
                    Text(text = stringResource(MR.strings.nest_unassign_button))
                }
            }
        }
    }
}

@Composable
private fun RecruitmentRow(
    offer: com.jalmarquest.core.model.CritterRecruitmentOffer,
    allowedRoles: Set<CritterRole>,
    capacityRemaining: Int,
    onRecruit: (CritterRole) -> Unit
) {
    val recruitCta = stringResource(MR.strings.nest_recruit_button)
    val fallbackRole = allowedRoles.firstOrNull() ?: offer.critter.roleAffinity
    val roleToAssign = if (offer.critter.roleAffinity in allowedRoles) offer.critter.roleAffinity else fallbackRole
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(
                MR.strings.nest_offer_summary,
                offer.critter.displayName,
                offer.critter.roleAffinity.readableName(),
                offer.seedCost
            ),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = stringResource(
                MR.strings.nest_offer_temperament,
                offer.critter.temperament.readableName()
            ),
            style = MaterialTheme.typography.bodySmall
        )
        if (capacityRemaining > 0 && roleToAssign != null) {
            Text(
                text = stringResource(MR.strings.nest_assign_prompt, roleToAssign.readableName()),
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = { onRecruit(roleToAssign) }) {
                Text(text = recruitCta)
            }
        } else {
            Text(
                text = stringResource(MR.strings.nest_no_capacity),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun NestLevel.readableName(): String = name.lowercase().replaceFirstChar { it.uppercase() }

private fun CritterRole.readableName(): String = name.lowercase().replaceFirstChar { it.uppercase() }

private fun CritterTemperament.readableName(): String = name.lowercase().replaceFirstChar { it.uppercase() }
