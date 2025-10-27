package com.jalmarquest.feature.explore

import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.feature.eventengine.SnippetSelector

class DefaultSnippetSelector(
    private val repository: LoreSnippetRepository
) : SnippetSelector {
    override fun findMatchingSnippet(choiceLog: ChoiceLog): String? {
        return repository.nextAvailableSnippet(choiceLog)
    }
}
