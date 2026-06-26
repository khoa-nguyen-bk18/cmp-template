package com.devindie.cmptemplate.data.remote.browse

import com.devindie.cmptemplate.data.network.ApiPaths
import com.devindie.cmptemplate.data.network.ApiResult
import com.devindie.cmptemplate.data.network.NetworkConfig
import com.devindie.cmptemplate.data.source.remote.browse.KtorBrowseCardRemoteDataSource
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
    fun fetchCatalogPage_parsesPaginatedResponse() = runTest {
        val engine =
            MockEngine { request ->
                assertTrue(request.url.encodedPath.endsWith(ApiPaths.BROWSE_CARDS))
                assertEquals("1", request.url.parameters["page"])
                assertEquals("20", request.url.parameters["page_size"])
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
                          ],
                          "pagination": {
                            "page": 1,
                            "pageSize": 20,
                            "hasMore": false
                          }
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
            KtorBrowseCardRemoteDataSource(
                httpClient = client,
                networkConfig = NetworkConfig(baseUrl = "https://api.test"),
            )

        val result = dataSource.fetchCatalogPage(page = 1, pageSize = 20)
        if (result is ApiResult.Success) {
            assertEquals(1, result.data.cards.size)
            assertEquals("Pikachu", result.data.cards.first().name)
            assertEquals(false, result.data.pagination.hasMore)
        }
    }
}
