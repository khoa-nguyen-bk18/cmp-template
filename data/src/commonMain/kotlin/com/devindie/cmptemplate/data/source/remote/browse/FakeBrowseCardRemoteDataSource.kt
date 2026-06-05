package com.devindie.cmptemplate.data.source.remote.browse

import com.devindie.cmptemplate.data.network.ApiResult
import com.devindie.cmptemplate.data.network.dto.BrowseCatalogPageDto
import com.devindie.cmptemplate.data.network.dto.BrowsePaginationDto
import com.devindie.cmptemplate.data.source.local.browse.BrowseCatalogSeeder
import kotlinx.coroutines.delay

/**
 * Default remote implementation: returns the same catalog as [BrowseCatalogSeeder] without HTTP.
 */
class FakeBrowseCardRemoteDataSource(private val simulateNetworkDelay: Boolean = true) : BrowseCardRemoteDataSource {
    override suspend fun fetchCatalogPage(page: Int, pageSize: Int): ApiResult<BrowseCatalogPageDto> {
        if (simulateNetworkDelay) {
            delay(FAKE_NETWORK_DELAY_MS)
        }
        val allCards = BrowseCatalogSeeder.seedEntities().map { it.toDto() }
        val startIndex = (page - 1) * pageSize
        if (startIndex >= allCards.size) {
            return ApiResult.Success(
                BrowseCatalogPageDto(
                    cards = emptyList(),
                    pagination = BrowsePaginationDto(
                        page = page,
                        pageSize = pageSize,
                        hasMore = false,
                    ),
                ),
            )
        }
        val pageCards = allCards.drop(startIndex).take(pageSize)
        val hasMore = startIndex + pageCards.size < allCards.size
        return ApiResult.Success(
            BrowseCatalogPageDto(
                cards = pageCards,
                pagination = BrowsePaginationDto(
                    page = page,
                    pageSize = pageSize,
                    hasMore = hasMore,
                ),
            ),
        )
    }

    private companion object {
        const val FAKE_NETWORK_DELAY_MS: Long = 2000L
    }
}
