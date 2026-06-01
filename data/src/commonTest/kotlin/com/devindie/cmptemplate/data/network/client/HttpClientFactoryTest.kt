package com.devindie.cmptemplate.data.network.client

import com.devindie.cmptemplate.data.auth.TokenRefreshDataSource
import com.devindie.cmptemplate.data.auth.TokenStore
import com.devindie.cmptemplate.data.network.ApiPaths
import com.devindie.cmptemplate.data.network.NetworkConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class HttpClientFactoryTest {
    private val networkConfig = NetworkConfig(baseUrl = "https://api.test")

    @Test
    fun authenticatedClient_attachesBearerTokenOnFirstRequest() = runTest {
        val tokenStore =
            FakeTokenStore(
                accessToken = "access-token",
                refreshToken = "refresh-token",
            )
        var authorizationHeader: String? = null
        val engine =
            MockEngine { request ->
                authorizationHeader = request.headers[HttpHeaders.Authorization]
                respond(
                    content = "{}",
                    status = HttpStatusCode.OK,
                    headers = jsonHeaders(),
                )
            }
        val client =
            HttpClientFactory(
                networkConfig = networkConfig,
                tokenStore = tokenStore,
                engineFactory = { engine },
            ).createAuthenticatedClient(
                tokenRefreshDataSource = tokenRefreshDataSource(refreshEngine = failingRefreshEngine()),
            )

        val response = client.get(ApiPaths.BROWSE_CARDS)

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Bearer access-token", authorizationHeader)
    }

    @Test
    fun authenticatedClient_refreshesAfterUnauthorizedAndSavesTokens() = runTest {
        val tokenStore =
            FakeTokenStore(
                accessToken = "old-access",
                refreshToken = "old-refresh",
            )
        val authorizationHeaders = mutableListOf<String?>()
        var apiCallCount = 0
        val engine =
            MockEngine { request ->
                apiCallCount += 1
                authorizationHeaders += request.headers[HttpHeaders.Authorization]

                if (apiCallCount == 1) {
                    respond(
                        content = "unauthorized",
                        status = HttpStatusCode.Unauthorized,
                        headers = bearerChallengeHeaders(),
                    )
                } else {
                    respond(
                        content = "{}",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }
            }
        var refreshCallCount = 0
        val refreshEngine =
            MockEngine { request ->
                refreshCallCount += 1
                assertEquals(ApiPaths.AUTH_REFRESH, request.url.encodedPath)
                respond(
                    content = """{"access_token":"new-access","refresh_token":"new-refresh"}""",
                    status = HttpStatusCode.OK,
                    headers = jsonHeaders(),
                )
            }
        val client =
            HttpClientFactory(
                networkConfig = networkConfig,
                tokenStore = tokenStore,
                engineFactory = { engine },
            ).createAuthenticatedClient(
                tokenRefreshDataSource = tokenRefreshDataSource(refreshEngine = refreshEngine),
            )

        val response = client.get(ApiPaths.BROWSE_CARDS)

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(2, apiCallCount)
        assertEquals(1, refreshCallCount)
        assertEquals(listOf<String?>("Bearer old-access", "Bearer new-access"), authorizationHeaders)
        assertEquals("new-access", tokenStore.accessToken)
        assertEquals("new-refresh", tokenStore.refreshToken)
    }

    @Test
    fun authenticatedClient_clearsTokensWhenRefreshIsUnauthorized() = runTest {
        val tokenStore =
            FakeTokenStore(
                accessToken = "old-access",
                refreshToken = "old-refresh",
            )
        val engine =
            MockEngine {
                respond(
                    content = "unauthorized",
                    status = HttpStatusCode.Unauthorized,
                    headers = bearerChallengeHeaders(),
                )
            }
        val refreshEngine =
            MockEngine {
                respond(
                    content = "refresh expired",
                    status = HttpStatusCode.Unauthorized,
                    headers = bearerChallengeHeaders(),
                )
            }
        val client =
            HttpClientFactory(
                networkConfig = networkConfig,
                tokenStore = tokenStore,
                engineFactory = { engine },
            ).createAuthenticatedClient(
                tokenRefreshDataSource = tokenRefreshDataSource(refreshEngine = refreshEngine),
            )

        assertFailsWith<ClientRequestException> {
            client.get(ApiPaths.BROWSE_CARDS)
        }
        assertNull(tokenStore.accessToken)
        assertNull(tokenStore.refreshToken)
    }

    private fun tokenRefreshDataSource(refreshEngine: MockEngine): TokenRefreshDataSource {
        val refreshClient =
            HttpClient(refreshEngine) {
                expectSuccess = true
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
        return TokenRefreshDataSource(
            refreshClient = refreshClient,
            networkConfig = networkConfig,
        )
    }

    private fun failingRefreshEngine(): MockEngine =
        MockEngine {
            error("Refresh should not be called")
        }

    private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun bearerChallengeHeaders() = headersOf(HttpHeaders.WWWAuthenticate, "Bearer")

    private class FakeTokenStore(
        accessToken: String? = null,
        refreshToken: String? = null,
    ) : TokenStore {
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
