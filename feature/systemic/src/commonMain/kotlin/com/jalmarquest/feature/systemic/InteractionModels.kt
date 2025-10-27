package com.jalmarquest.feature.systemic

import com.jalmarquest.core.model.AvailabilityMessage
import com.jalmarquest.core.model.ChoiceTag
import com.jalmarquest.core.model.EnvironmentTag
import com.jalmarquest.core.model.InteractionId
import com.jalmarquest.core.model.InteractionOptionId
import com.jalmarquest.core.model.Inventory
import com.jalmarquest.core.model.ItemId
import com.jalmarquest.core.model.ItemStack
import com.jalmarquest.core.model.OptionTitle
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.model.ResolutionMessage
import com.jalmarquest.core.model.StatusEffects
import com.jalmarquest.core.model.StatusKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemRequirement(
    @SerialName("item_id") val itemId: ItemId,
    val quantity: Int
) {
    init {
        require(quantity > 0) { "Required quantity must be positive" }
    }
}

@Serializable
data class StatusRequirement(
    @SerialName("status_key") val statusKey: StatusKey
)

@Serializable
data class InteractionRequirements(
    @SerialName("items") val itemRequirements: List<ItemRequirement> = emptyList(),
    @SerialName("requires_status") val requiresStatuses: List<StatusRequirement> = emptyList(),
    @SerialName("forbids_status") val forbiddenStatuses: List<StatusRequirement> = emptyList(),
    @SerialName("requires_environment") val environment: List<EnvironmentTag> = emptyList()
)

@Serializable
data class StatusGrant(
    @SerialName("status_key") val statusKey: StatusKey,
    @SerialName("duration_minutes") val durationMinutes: Long? = null
) {
    init {
        require(durationMinutes == null || durationMinutes > 0) {
            "Duration minutes must be positive when provided"
        }
    }
}

@Serializable
data class InteractionEffects(
    @SerialName("grant_items") val grantItems: List<ItemStack> = emptyList(),
    @SerialName("consume_items") val consumeItems: List<ItemRequirement> = emptyList(),
    @SerialName("grant_status") val grantStatuses: List<StatusGrant> = emptyList(),
    @SerialName("clear_status") val clearStatuses: List<StatusKey> = emptyList(),
    @SerialName("append_choice_tags") val appendChoiceTags: List<ChoiceTag> = emptyList()
)

@Serializable
data class InteractionRule(
    val id: InteractionId,
    val title: OptionTitle,
    val requirements: InteractionRequirements = InteractionRequirements(),
    val effects: InteractionEffects = InteractionEffects(),
    @SerialName("success_message") val successMessage: ResolutionMessage,
    @SerialName("failure_message") val failureMessage: ResolutionMessage = ResolutionMessage("Jalmar decides against it.")
)

data class InteractionContext(
    val inventory: Inventory,
    val statuses: StatusEffects,
    val environment: Set<EnvironmentTag>
) {
    companion object {
        fun fromPlayer(player: Player, environment: Set<EnvironmentTag>): InteractionContext =
            InteractionContext(
                inventory = player.inventory,
                statuses = player.statusEffects,
                environment = environment
            )
    }
}

@Serializable
data class UnmetRequirement(
    val reason: AvailabilityMessage
)

@Serializable
data class ContextualOption(
    val optionId: InteractionOptionId,
    val interactionId: InteractionId,
    val title: OptionTitle,
    val available: Boolean,
    @SerialName("availability_message") val availabilityMessage: AvailabilityMessage,
    @SerialName("blocked_reasons") val blockedReasons: List<AvailabilityMessage>
)

sealed interface InteractionResolution {
    val interactionId: InteractionId

    data class Success(
        override val interactionId: InteractionId,
        val message: ResolutionMessage,
        val effects: InteractionEffects
    ) : InteractionResolution

    data class Failure(
        override val interactionId: InteractionId,
        val message: ResolutionMessage,
        val reasons: List<AvailabilityMessage>
    ) : InteractionResolution
}
