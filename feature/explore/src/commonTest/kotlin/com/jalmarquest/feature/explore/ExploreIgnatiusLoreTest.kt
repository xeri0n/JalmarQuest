package com.jalmarquest.feature.explore

import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.ChoiceLogEntry
import com.jalmarquest.core.model.ChoiceTag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExploreIgnatiusLoreTest {
    private val repository = LoreSnippetRepository(IgnatiusLoreSnippets.getAllSnippets())

    @Test
    fun `no snippet without prerequisites`() {
        val id = repository.nextAvailableSnippetForLocation(
            choiceLog = ChoiceLog(emptyList()),
            locationId = "BUTTONBURGH_LABORATORY",
            biomeType = null
        )
        assertNull(id)
    }

    @Test
    fun `first meeting unlocks after intro accepted at lab`() {
        val log = ChoiceLog(listOf(
            ChoiceLogEntry(ChoiceTag("quest_ignatius_introduction_accepted"), 1000L)
        ))
        val id = repository.nextAvailableSnippetForLocation(
            choiceLog = log,
            locationId = "BUTTONBURGH_LABORATORY",
            biomeType = null
        )
        assertEquals("ignatius_first_meeting", id)
    }

    @Test
    fun `midnight delivery unlocks in ant tunnels after trust accepted`() {
        val log = ChoiceLog(listOf(
            ChoiceLogEntry(ChoiceTag("quest_ignatius_trust_accepted"), 2000L)
        ))
        val id = repository.nextAvailableSnippetForLocation(
            choiceLog = log,
            locationId = "ANT_COLONY_TUNNELS",
            biomeType = "UNDERGROUND"
        )
        assertEquals("ignatius_midnight_delivery", id)
    }

    @Test
    fun `confession unlocks after trust complete at lab`() {
        val log = ChoiceLog(listOf(
            ChoiceLogEntry(ChoiceTag("quest_ignatius_trust_complete"), 3000L)
        ))
        val id = repository.nextAvailableSnippetForLocation(
            choiceLog = log,
            locationId = "BUTTONBURGH_LABORATORY",
            biomeType = null
        )
        assertEquals("ignatius_confession", id)
    }

    @Test
    fun `border infiltration unlocks after secret accepted at border`() {
        val log = ChoiceLog(listOf(
            ChoiceLogEntry(ChoiceTag("quest_ignatius_secret_accepted"), 4000L)
        ))
        val id = repository.nextAvailableSnippetForLocation(
            choiceLog = log,
            locationId = "INSECT_BORDER_OUTPOST",
            biomeType = "WASTELAND"
        )
        assertEquals("ignatius_border_infiltration", id)
    }

    @Test
    fun `final choice unlocks after secret completed at lab`() {
        val log = ChoiceLog(listOf(
            ChoiceLogEntry(ChoiceTag("quest_ignatius_secret_complete"), 5000L)
        ))
        val id = repository.nextAvailableSnippetForLocation(
            choiceLog = log,
            locationId = "BUTTONBURGH_LABORATORY",
            biomeType = null
        )
        assertEquals("ignatius_final_choice", id)
    }
}
