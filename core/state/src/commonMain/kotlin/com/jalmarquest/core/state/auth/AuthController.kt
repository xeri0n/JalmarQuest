package com.jalmarquest.core.state.auth

import com.jalmarquest.core.model.AuthState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AuthController(
    private val scope: CoroutineScope,
    private val stateManager: AuthStateManager
) {
    val authState = stateManager.authState

    fun continueAsGuest(displayName: String? = null) {
        scope.launch { stateManager.signInAsGuest(displayName) }
    }

    fun signOut() {
        scope.launch { stateManager.signOut() }
    }

    fun bootstrap() {
        scope.launch { stateManager.bootstrap() }
    }
}
