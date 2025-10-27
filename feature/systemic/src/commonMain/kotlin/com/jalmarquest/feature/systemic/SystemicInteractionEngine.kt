package com.jalmarquest.feature.systemic

import com.jalmarquest.core.model.AvailabilityMessage
import com.jalmarquest.core.model.ChoiceTag
import com.jalmarquest.core.model.EnvironmentTag
import com.jalmarquest.core.model.InteractionId
import com.jalmarquest.core.model.InteractionOptionId
import com.jalmarquest.core.model.ItemId
import com.jalmarquest.core.model.ItemStack
import com.jalmarquest.core.model.OptionTitle
import com.jalmarquest.core.model.ResolutionMessage
import com.jalmarquest.core.model.StatusKey
import kotlin.math.max
import kotlin.math.max

interface InteractionCatalog {
    val rules: List<InteractionRule>

    fun findRule(id: InteractionId): InteractionRule? = rules.firstOrNull { it.id == id }
}

class SystemicInteractionEngine(
    private val catalog: InteractionCatalog
) {
    fun evaluate(context: InteractionContext): List<ContextualOption> {
        return catalog.rules.map { rule ->
            val reasons = rule.unmetReasons(context)
            val available = reasons.isEmpty()
            val availabilityMessage = if (available) {
                AvailabilityMessage("Jalmar is poised to ${rule.title.value.lowercase()}.")
            } else {
                reasons.first()
            }
            ContextualOption(
                optionId = InteractionOptionId(rule.id.value),
                interactionId = rule.id,
                title = rule.title,
                available = available,
                availabilityMessage = availabilityMessage,
                blockedReasons = reasons
            )
        }
    }

    fun resolve(interactionId: InteractionId, context: InteractionContext): InteractionResolution {
        val rule = catalog.findRule(interactionId)
            ?: return InteractionResolution.Failure(
                interactionId = interactionId,
                message = AvailabilityMessage("No such interaction").toResolutionMessage(),
                reasons = listOf(AvailabilityMessage("Missing rule configuration."))
            )

        val reasons = rule.unmetReasons(context)
        return if (reasons.isEmpty()) {
            InteractionResolution.Success(
                interactionId = rule.id,
                message = rule.successMessage,
                effects = rule.effects
            )
        } else {
            InteractionResolution.Failure(
                interactionId = rule.id,
                message = rule.failureMessage,
                reasons = reasons
            )
        }
    }

    private fun InteractionRule.unmetReasons(context: InteractionContext): List<AvailabilityMessage> {
        val reasons = mutableListOf<AvailabilityMessage>()
        for (requirement in requirements.itemRequirements) {
            val total = context.inventory.totalQuantity(requirement.itemId)
            if (total < requirement.quantity) {
                val needed = requirement.quantity - total
                reasons += AvailabilityMessage(
                    "Needs ${requirement.quantity} ${requirement.itemId.value.replace('_', ' ')} (short ${max(needed, 0)})."
                )
            }
        }
        for (statusRequirement in requirements.requiresStatuses) {
            if (!context.hasStatus(statusRequirement.statusKey)) {
                reasons += AvailabilityMessage("Requires ${statusRequirement.statusKey.value.replace('_', ' ')} status.")
            }
        }
        for (forbidden in requirements.forbiddenStatuses) {
            if (context.hasStatus(forbidden.statusKey)) {
                reasons += AvailabilityMessage("Cannot act while ${forbidden.statusKey.value.replace('_', ' ')} is present.")
            }
        }
        for (tag in requirements.environment) {
            if (!context.environment.contains(tag)) {
                reasons += AvailabilityMessage("Needs to be near ${tag.value.replace('_', ' ')}.")
            }
        }
        return reasons
    }

    private fun InteractionContext.hasStatus(statusKey: StatusKey): Boolean {
        return statuses.entries.any { it.key == statusKey.value }
    }

    private fun AvailabilityMessage.toResolutionMessage(): com.jalmarquest.core.model.ResolutionMessage =
        com.jalmarquest.core.model.ResolutionMessage(this.value)
}

class InMemoryInteractionCatalog(
    override val rules: List<InteractionRule>
) : InteractionCatalog

fun defaultInteractionCatalog(): InteractionCatalog = InMemoryInteractionCatalog(
    rules = DefaultInteractionCatalogData.rules
)

private object DefaultInteractionCatalogData {
    private val craftTwigSpear = InteractionRule(
        id = InteractionId("craft_twig_spear"),
        title = OptionTitle("Bind a Twig Spear"),
        requirements = InteractionRequirements(
            itemRequirements = listOf(
                ItemRequirement(ItemId("twig"), 2),
                ItemRequirement(ItemId("acorn_cap"), 1)
            ),
            environment = listOf(EnvironmentTag("workbench"))
        ),
        effects = InteractionEffects(
            consumeItems = listOf(
                ItemRequirement(ItemId("twig"), 2),
                ItemRequirement(ItemId("acorn_cap"), 1)
            ),
            grantItems = listOf(
                ItemStack(ItemId("twig_spear"), 1)
            ),
            appendChoiceTags = listOf(ChoiceTag("crafted:twig_spear"))
        ),
        successMessage = ResolutionMessage("Jalmar lashes twigs and acorn cap into a proud Twig Spear."),
        failureMessage = ResolutionMessage("Without the right scraps, the spear remains a daydream.")
    )

    private val dustBath = InteractionRule(
        id = InteractionId("take_dust_bath"),
        title = OptionTitle("Kick up a Dust Bath"),
        requirements = InteractionRequirements(
            itemRequirements = listOf(ItemRequirement(ItemId("soft_dust"), 1)),
            environment = listOf(EnvironmentTag("resting_spot")),
            forbiddenStatuses = listOf(StatusRequirement(StatusKey("damp_plummage")))
        ),
        effects = InteractionEffects(
            consumeItems = listOf(ItemRequirement(ItemId("soft_dust"), 1)),
            grantStatuses = listOf(StatusGrant(StatusKey("calm"), durationMinutes = 45)),
            appendChoiceTags = listOf(ChoiceTag("ritual:dust_bath"))
        ),
        successMessage = ResolutionMessage("A cloud of fragrant dust settles and Jalmar feels utterly serene."),
        failureMessage = ResolutionMessage("The dust clumps against damp feathers—better wait to dry.")
    )

    private val midnightWhistle = InteractionRule(
        id = InteractionId("answer_midnight_whistle"),
        title = OptionTitle("Answer the Midnight Whistle"),
        requirements = InteractionRequirements(
            requiresStatuses = listOf(StatusRequirement(StatusKey("well_restored"))),
            environment = listOf(EnvironmentTag("moonlit_perch"))
        ),
        effects = InteractionEffects(
            appendChoiceTags = listOf(ChoiceTag("heard:midnight_whistle"))
        ),
        successMessage = ResolutionMessage("Jalmar trills back, the whistle echoing promises of hidden paths."),
        failureMessage = ResolutionMessage("Too drowsy to muster a reply—the whistle fades unanswered.")
    )

    val rules: List<InteractionRule> = listOf(
        craftTwigSpear,
        dustBath,
        midnightWhistle
    )
}
