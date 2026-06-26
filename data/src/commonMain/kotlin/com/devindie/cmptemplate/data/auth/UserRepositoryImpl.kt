package com.devindie.cmptemplate.data.auth

import com.devindie.cmptemplate.domain.model.user.UserSession
import com.devindie.cmptemplate.domain.repository.UserRepository

/**
 * Maps domain [UserSession] to encrypted [TokenStore] persistence.
 *
 * @see UserRepository
 * @see KSafeTokenStore
 */
class UserRepositoryImpl(private val tokenStore: TokenStore) : UserRepository {
    override suspend fun getSession(): UserSession? {
        val access = tokenStore.getAccessToken() ?: return null
        val refresh = tokenStore.getRefreshToken() ?: return null
        return UserSession(accessToken = access, refreshToken = refresh)
    }

    override suspend fun saveSession(session: UserSession) {
        tokenStore.saveTokens(
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
        )
    }

    override suspend fun clearSession() {
        tokenStore.clear()
    }
}
