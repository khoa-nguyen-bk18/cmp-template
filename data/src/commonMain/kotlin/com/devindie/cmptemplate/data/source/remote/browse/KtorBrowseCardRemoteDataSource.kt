package com.devindie.cmptemplate.data.source.remote.browse

import com.devindie.cmptemplate.data.network.ApiPaths
import com.devindie.cmptemplate.data.network.ApiResult
import com.devindie.cmptemplate.data.network.NetworkConfig
import com.devindie.cmptemplate.data.network.dto.BrowseCatalogPageDto
import com.devindie.cmptemplate.data.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class KtorBrowseCardRemoteDataSource(private val httpClient: HttpClient, private val networkConfig: NetworkConfig) :
    BrowseCardRemoteDataSource {
    override suspend fun fetchCatalogPage(page: Int, pageSize: Int): ApiResult<BrowseCatalogPageDto> = safeApiCall {
        httpClient
            .get(networkConfig.baseUrl.trimEnd('/') + ApiPaths.BROWSE_CARDS) {
                parameter("page", page)
                parameter("page_size", pageSize)
            }.body<BrowseCatalogPageDto>()
    }
}
