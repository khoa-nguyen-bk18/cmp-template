package com.devindie.cmptemplate.domain.repository

import com.devindie.cmptemplate.domain.model.user.UserSession

/**
 * Persists and reads the authenticated user session (OAuth tokens).
 *
 * **Flow:** use cases in `domain.usecase.user` → this → [com.devindie.cmptemplate.data.auth.UserRepositoryImpl].
 *
 * @see com.devindie.cmptemplate.domain.usecase.user.GetUserSessionUseCase
 * @see com.devindie.cmptemplate.domain.usecase.user.SaveUserSessionUseCase
 * @see com.devindie.cmptemplate.domain.usecase.user.ClearUserSessionUseCase
 */
interface UserRepository {
    /** Returns a session when both tokens are present; `null` when logged out. */
    suspend fun getSession(): UserSession?

    suspend fun saveSession(session: UserSession)

    suspend fun clearSession()
}
