package com.devindie.cmptemplate.data.remote.browse

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

class KtorBrowseCardRemoteDataSourceTest {
    @Test
    fun fetchCatalog_parsesBrowseResponse() = runTest {
        val engine =
            MockEngine { request ->
                assertTrue(request.url.encodedPath.endsWith(ApiPaths.BROWSE_CARDS))
                respond(
                    content =
                    """
                        {
                          "cards": [
                            {
                              "name": "Pikachu",
                              "setName": "Base",
                              "condition": "NM",
                              "priceCents": 100,
                              "quantity": 1,
                              "category": "Pokemon"
                            }
                          ]
                        }
                    """.trimIndent(),
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
            _root_ide_package_.com.devindie.cmptemplate.data.source.remote.browse.KtorBrowseCardRemoteDataSource(
                httpClient = client,
                networkConfig = NetworkConfig(baseUrl = "https://api.test"),
            )

        val result = dataSource.fetchCatalog()

        assertTrue(result is ApiResult.Success)
        assertEquals(1, (result as ApiResult.Success).data.size)
        assertEquals("Pikachu", result.data.first().name)
    }
}
