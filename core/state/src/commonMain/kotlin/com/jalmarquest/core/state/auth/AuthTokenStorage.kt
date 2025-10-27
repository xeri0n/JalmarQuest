package com.jalmarquest.core.state.auth

import com.jalmarquest.core.model.SessionToken

expect class AuthTokenStorage() {
    suspend fun loadToken(): SessionToken?
    suspend fun saveToken(token: SessionToken?)
}
