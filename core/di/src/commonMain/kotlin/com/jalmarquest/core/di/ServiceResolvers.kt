package com.jalmarquest.core.di

import com.jalmarquest.core.state.auth.AuthController
import com.jalmarquest.core.state.auth.AuthStateManager
import com.jalmarquest.feature.activities.ActivitiesController
import com.jalmarquest.feature.activities.ActivityStateMachine
import com.jalmarquest.feature.explore.ExploreController
import com.jalmarquest.feature.explore.ExploreStateMachine
import com.jalmarquest.feature.hub.HubController
import com.jalmarquest.feature.hub.HubStateMachine
import com.jalmarquest.feature.nest.NestConfig
import com.jalmarquest.feature.nest.NestController
import com.jalmarquest.feature.nest.NestStateMachine
import com.jalmarquest.feature.systemic.SystemicInteractionController
import com.jalmarquest.feature.systemic.SystemicInteractionEngine
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

/**
 * Convenience accessors for resolving scoped dependencies from the active Koin context.
 */
fun resolveAuthStateManager(): AuthStateManager = requireKoin().get()

fun resolveAuthController(scope: CoroutineScope): AuthController =
    requireKoin().get { parametersOf(scope) }

fun resolveNestStateMachine(): NestStateMachine = requireKoin().get()

fun resolveNestController(scope: CoroutineScope): NestController =
    requireKoin().get { parametersOf(scope) }

fun resolveNestConfig(): NestConfig = requireKoin().get()

fun resolveExploreStateMachine(): ExploreStateMachine = requireKoin().get()

fun resolveExploreController(scope: CoroutineScope): ExploreController =
    requireKoin().get { parametersOf(scope) }

fun resolveHubStateMachine(): HubStateMachine = requireKoin().get()

fun resolveHubController(scope: CoroutineScope): HubController =
    requireKoin().get { parametersOf(scope) }

fun resolveActivityStateMachine(): ActivityStateMachine = requireKoin().get()

fun resolveActivitiesController(scope: CoroutineScope): ActivitiesController =
    requireKoin().get { parametersOf(scope) }

fun resolveSystemicInteractionEngine(): SystemicInteractionEngine = requireKoin().get()

fun resolveSystemicInteractionController(scope: CoroutineScope): SystemicInteractionController =
    requireKoin().get { parametersOf(scope) }
