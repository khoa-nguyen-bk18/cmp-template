package com.devindie.cmptemplate.data.auth

import eu.anifantakis.lib.ksafe.KSafe

/**
 * Hardware-backed encrypted token storage via [KSafe](https://github.com/ioannisa/KSafe).
 *
 * @see TokenStore
 * @see com.devindie.cmptemplate.data.network.client.HttpClientFactory
 */
const val AUTH_TOKENS_STORAGE_KEY: String = "auth_tokens"

class KSafeTokenStore(private val ksafe: KSafe) : TokenStore {
    override suspend fun getAccessToken(): String? =
        ksafe.get(AUTH_TOKENS_STORAGE_KEY, AuthTokens()).accessToken.takeIf { it.isNotEmpty() }

    override suspend fun getRefreshToken(): String? =
        ksafe.get(AUTH_TOKENS_STORAGE_KEY, AuthTokens()).refreshToken.takeIf { it.isNotEmpty() }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        ksafe.put(
            AUTH_TOKENS_STORAGE_KEY,
            AuthTokens(accessToken = accessToken, refreshToken = refreshToken),
        )
    }

    override suspend fun clear() {
        ksafe.put(AUTH_TOKENS_STORAGE_KEY, AuthTokens())
    }
}
