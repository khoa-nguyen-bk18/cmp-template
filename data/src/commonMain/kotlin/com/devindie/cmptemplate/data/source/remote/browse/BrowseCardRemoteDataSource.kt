package com.devindie.cmptemplate.data.source.remote.browse

import com.devindie.cmptemplate.data.network.ApiResult
import com.devindie.cmptemplate.data.network.dto.BrowseCatalogPageDto

/**
 * Remote catalog port; implementations live in this module (fake or Ktor).
 *
 * @see FakeBrowseCardRemoteDataSource
 * @see KtorBrowseCardRemoteDataSource
 * @see com.devindie.cmptemplate.data.source.local.browse.BrowseCardRemoteMediator
 */
interface BrowseCardRemoteDataSource {
    suspend fun fetchCatalogPage(page: Int, pageSize: Int): ApiResult<BrowseCatalogPageDto>
}
