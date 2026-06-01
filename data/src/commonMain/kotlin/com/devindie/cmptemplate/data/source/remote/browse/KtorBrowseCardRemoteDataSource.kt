package com.devindie.cmptemplate.data.source.remote.browse

import com.devindie.cmptemplate.data.network.ApiPaths
import com.devindie.cmptemplate.data.network.ApiResult
import com.devindie.cmptemplate.data.network.NetworkConfig
import com.devindie.cmptemplate.data.network.dto.BrowseCardDto
import com.devindie.cmptemplate.data.network.dto.BrowseCatalogResponseDto
import com.devindie.cmptemplate.data.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class KtorBrowseCardRemoteDataSource(private val httpClient: HttpClient, private val networkConfig: NetworkConfig) :
    BrowseCardRemoteDataSource {
    override suspend fun fetchCatalog(): ApiResult<List<BrowseCardDto>> = safeApiCall {
        httpClient
            .get(networkConfig.baseUrl.trimEnd('/') + ApiPaths.BROWSE_CARDS)
            .body<BrowseCatalogResponseDto>()
            .cards
    }
}
