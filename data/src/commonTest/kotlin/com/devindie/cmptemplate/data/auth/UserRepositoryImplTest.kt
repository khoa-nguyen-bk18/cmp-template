package com.devindie.cmptemplate.data.auth

import com.devindie.cmptemplate.domain.model.user.UserSession
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserRepositoryImplTest {
    @Test
    fun getSession_returnsNullWhenAccessTokenMissing() = runTest {
        val tokenStore =
            FakeTokenStore(
                accessToken = null,
                refreshToken = "refresh",
            )
        val repository = UserRepositoryImpl(tokenStore = tokenStore)

        assertNull(repository.getSession())
    }

    @Test
    fun getSession_returnsNullWhenRefreshTokenMissing() = runTest {
        val tokenStore =
            FakeTokenStore(
                accessToken = "access",
                refreshToken = null,
            )
        val repository = UserRepositoryImpl(tokenStore = tokenStore)

        assertNull(repository.getSession())
    }

    @Test
    fun getSession_returnsUserSessionWhenBothTokensPresent() = runTest {
        val tokenStore =
            FakeTokenStore(
                accessToken = "access",
                refreshToken = "refresh",
            )
        val repository = UserRepositoryImpl(tokenStore = tokenStore)

        assertEquals(
            UserSession(accessToken = "access", refreshToken = "refresh"),
            repository.getSession(),
        )
    }

    @Test
    fun saveSession_delegatesToTokenStore() = runTest {
        val tokenStore = FakeTokenStore()
        val repository = UserRepositoryImpl(tokenStore = tokenStore)

        repository.saveSession(
            UserSession(accessToken = "new-access", refreshToken = "new-refresh"),
        )

        assertEquals("new-access", tokenStore.accessToken)
        assertEquals("new-refresh", tokenStore.refreshToken)
    }

    @Test
    fun clearSession_delegatesToTokenStore() = runTest {
        val tokenStore =
            FakeTokenStore(
                accessToken = "access",
                refreshToken = "refresh",
            )
        val repository = UserRepositoryImpl(tokenStore = tokenStore)

        repository.clearSession()

        assertNull(tokenStore.accessToken)
        assertNull(tokenStore.refreshToken)
    }

    private class FakeTokenStore(accessToken: String? = null, refreshToken: String? = null) : TokenStore {
        var accessToken: String? = accessToken
            private set
        var refreshToken: String? = refreshToken
            private set

        override suspend fun getAccessToken(): String? = accessToken

        override suspend fun getRefreshToken(): String? = refreshToken

        override suspend fun saveTokens(accessToken: String, refreshToken: String) {
            this.accessToken = accessToken
            this.refreshToken = refreshToken
        }

        override suspend fun clear() {
            accessToken = null
            refreshToken = null
        }
    }
}
