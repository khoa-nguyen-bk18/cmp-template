package com.devindie.cmptemplate.data.source.remote.browse

import com.devindie.cmptemplate.data.source.local.browse.BrowseCatalogSeeder
import com.devindie.cmptemplate.data.network.ApiResult
import com.devindie.cmptemplate.data.network.dto.BrowseCardDto
import kotlinx.coroutines.delay

/**
 * Default remote implementation: returns the same catalog as [BrowseCatalogSeeder] without HTTP.
 */
class FakeBrowseCardRemoteDataSource(private val simulateNetworkDelay: Boolean = false) : BrowseCardRemoteDataSource {
    override suspend fun fetchCatalog(): ApiResult<List<BrowseCardDto>> {
        if (simulateNetworkDelay) {
            delay(FAKE_NETWORK_DELAY_MS)
        }
        return ApiResult.Success(
            BrowseCatalogSeeder.seedEntities().map { it.toDto() },
        )
    }

    private companion object {
        const val FAKE_NETWORK_DELAY_MS: Long = 300L
    }
}
