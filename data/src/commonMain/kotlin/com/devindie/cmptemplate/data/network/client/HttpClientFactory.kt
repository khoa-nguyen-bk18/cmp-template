package com.devindie.cmptemplate.data.network.client

import com.devindie.cmptemplate.data.auth.TokenRefreshDataSource
import com.devindie.cmptemplate.data.auth.TokenStore
import com.devindie.cmptemplate.data.network.ApiResult
import com.devindie.cmptemplate.data.network.NetworkConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

/**
 * Builds the refresh-only and bearer-authenticated Ktor clients.
 *
 * @see com.devindie.cmptemplate.data.auth.TokenRefreshDataSource
 */
class HttpClientFactory(
    private val networkConfig: NetworkConfig,
    private val tokenStore: TokenStore,
    private val engineFactory: () -> HttpClientEngine = ::createPlatformHttpClientEngine,
) {
    private val refreshMutex = Mutex()
    private val baseUrl = Url(networkConfig.baseUrl)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun createRefreshClient(): HttpClient = HttpClient(engineFactory()) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(json)
        }
        defaultRequest {
            url(networkConfig.baseUrl)
        }
    }

    fun createAuthenticatedClient(tokenRefreshDataSource: TokenRefreshDataSource): HttpClient =
        HttpClient(engineFactory()) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(json)
            }
            defaultRequest {
                url(networkConfig.baseUrl)
            }
            install(Auth) {
                bearer {
                    //using tokenStore as SOT
                    cacheTokens = false

                    loadTokens {
                        val access = tokenStore.getAccessToken()
                        val refresh = tokenStore.getRefreshToken()
                        if (access == null || refresh == null) {
                            null
                        } else BearerTokens(
                            access, refresh
                        )
                    }
                    //Send a Bearer token immediately for API calls
                    sendWithoutRequest { request ->
                        request.url.protocol == baseUrl.protocol && request.url.host == baseUrl.host && request.url.port == baseUrl.port
                    }
                    refreshTokens {
                        refreshMutex.withLock {
                            val requestTokens = oldTokens
                            val access = tokenStore.getAccessToken()
                            val refresh = tokenStore.getRefreshToken()

                            if (requestTokens != null && access != null && refresh != null && (access != requestTokens.accessToken || refresh != requestTokens.refreshToken)) {
                                return@withLock BearerTokens(access, refresh)
                            }

                            if (refresh == null) {
                                null
                            } else {
                                when (val result = tokenRefreshDataSource.refresh(refresh)) {
                                    is ApiResult.Success -> {
                                        tokenStore.saveTokens(
                                            accessToken = result.data.accessToken,
                                            refreshToken = result.data.refreshToken,
                                        )
                                        BearerTokens(
                                            accessToken = result.data.accessToken,
                                            refreshToken = result.data.refreshToken,
                                        )
                                    }

                                    is ApiResult.HttpError -> {
                                        if (result.statusCode == 400 || result.statusCode == 401 || result.statusCode == 403) {
                                            tokenStore.clear()
                                        }
                                        null
                                    }

                                    else -> null
                                }
                            }
                        }
                    }
                }
            }
        }
}
