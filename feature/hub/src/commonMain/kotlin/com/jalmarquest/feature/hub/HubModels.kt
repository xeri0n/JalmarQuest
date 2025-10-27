package com.jalmarquest.feature.hub

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class HubLocationId(val value: String)

@Serializable
enum class HubActionType {
    @SerialName("shop")
    SHOP,

    @SerialName("chronicle")
    CHRONICLE,

    @SerialName("craft")
    CRAFT,

    @SerialName("nest")
    NEST,

    @SerialName("systemic")
    SYSTEMIC,

    @SerialName("explore")
    EXPLORE,

    @SerialName("activities")
    ACTIVITIES,

    @SerialName("hoard")
    HOARD,

    @SerialName("concoctions")
    CONCOCTIONS,

    @SerialName("thoughts")
    THOUGHTS,

    @SerialName("skills")
    SKILLS,

    @SerialName("quests")
    QUESTS,

    @SerialName("battle_pass")
    BATTLE_PASS
}

@Serializable
@JvmInline
value class HubActionId(val value: String)

@Serializable
data class HubAction(
    val id: HubActionId,
    val type: HubActionType
)

@Serializable
data class HubLocation(
    val id: HubLocationId,
    val actionOrder: List<HubAction>
)

@Serializable
data class HubState(
    val locations: List<HubLocation>,
    val selectedLocationId: HubLocationId? = null,
    val activeAction: HubAction? = null
) {
    fun selectedLocation(): HubLocation? = locations.firstOrNull { it.id == selectedLocationId }
}

fun defaultHubLocations(): List<HubLocation> = listOf(
    HubLocation(
        id = HubLocationId("pack_rat_hoard"),
        actionOrder = listOf(
            HubAction(id = HubActionId("hoard"), type = HubActionType.HOARD),
            HubAction(id = HubActionId("shopfront"), type = HubActionType.SHOP)
        )
    ),
    HubLocation(
        id = HubLocationId("quailsmith"),
        actionOrder = listOf(
            HubAction(id = HubActionId("forge"), type = HubActionType.CRAFT),
            HubAction(id = HubActionId("alchemy"), type = HubActionType.CONCOCTIONS),
            HubAction(id = HubActionId("systemic"), type = HubActionType.SYSTEMIC)
        )
    ),
    HubLocation(
        id = HubLocationId("quill_study"),
        actionOrder = listOf(
            HubAction(id = HubActionId("chronicle"), type = HubActionType.CHRONICLE),
            HubAction(id = HubActionId("quests"), type = HubActionType.QUESTS),
            HubAction(id = HubActionId("battle_pass"), type = HubActionType.BATTLE_PASS),
            HubAction(id = HubActionId("thoughts"), type = HubActionType.THOUGHTS),
            HubAction(id = HubActionId("secondary"), type = HubActionType.ACTIVITIES),
            HubAction(id = HubActionId("expedition"), type = HubActionType.EXPLORE)
        )
    ),
    HubLocation(
        id = HubLocationId("hen_pen"),
        actionOrder = listOf(
            HubAction(id = HubActionId("nest"), type = HubActionType.NEST)
        )
    )
)
