package com.devindie.cmptemplate.data.source.remote.browse

import com.devindie.cmptemplate.data.network.ApiResult
import com.devindie.cmptemplate.data.network.dto.BrowseCardDto

/**
 * Remote catalog port; implementations live in this module (fake or Ktor).
 *
 * @see com.devindie.cmptemplate.data.source.local.browse.BrowseCardRepositoryImpl
 * @see FakeBrowseCardRemoteDataSource
 * @see KtorBrowseCardRemoteDataSource
 */
interface BrowseCardRemoteDataSource {
    suspend fun fetchCatalog(): ApiResult<List<BrowseCardDto>>
}
