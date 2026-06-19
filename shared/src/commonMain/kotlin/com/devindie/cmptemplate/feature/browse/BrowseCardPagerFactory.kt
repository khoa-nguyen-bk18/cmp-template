package com.devindie.cmptemplate.feature.browse

import androidx.paging.PagingData
import com.devindie.cmptemplate.domain.model.browse.BrowseCardsQuery
import com.devindie.cmptemplate.domain.model.browse.CollectibleCard
import kotlinx.coroutines.flow.Flow

/**
 * Presentation port for paginated Browse catalog streams.
 *
 * Implementation lives in `:data` ([com.devindie.cmptemplate.data.source.local.browse.BrowseCardPagerFactoryImpl]);
 * bound at the app composition root (androidApp / iOS Koin bootstrap), not in
 * [com.devindie.cmptemplate.core.di.appDomainModule].
 */
fun interface BrowseCardPagerFactory {
    fun pages(query: BrowseCardsQuery): Flow<PagingData<CollectibleCard>>
}
