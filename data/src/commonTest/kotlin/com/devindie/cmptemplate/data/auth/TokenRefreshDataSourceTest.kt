package com.devindie.cmptemplate.data.auth

import com.devindie.cmptemplate.data.network.ApiPaths
import com.devindie.cmptemplate.data.network.ApiResult
import com.devindie.cmptemplate.data.network.NetworkConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TokenRefreshDataSourceTest {
    @Test
    fun refresh_parsesTokenResponse() = runTest {
        val engine =
            MockEngine { request ->
                assertTrue(request.url.encodedPath.endsWith(ApiPaths.AUTH_REFRESH))
                respond(
                    content = """{"access_token":"new-access","refresh_token":"new-refresh"}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        val client =
            HttpClient(engine) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
        val dataSource =
            TokenRefreshDataSource(
                refreshClient = client,
                networkConfig = NetworkConfig(baseUrl = "https://api.test"),
            )

        val result = dataSource.refresh("old-refresh")

        assertTrue(result is ApiResult.Success)
        assertEquals("new-access", result.data.accessToken)
        assertEquals("new-refresh", result.data.refreshToken)
    }
}
