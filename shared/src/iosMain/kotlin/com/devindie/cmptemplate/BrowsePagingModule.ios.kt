package com.devindie.cmptemplate

import com.devindie.cmptemplate.data.source.local.browse.BrowseCardPagerFactoryImpl
import com.devindie.cmptemplate.feature.browse.BrowseCardPagerFactory
import org.koin.dsl.module

/** Wires [BrowseCardPagerFactoryImpl] to the presentation port at the composition root. */
val browsePagingModule =
    module {
        single<BrowseCardPagerFactory> {
            val impl = get<BrowseCardPagerFactoryImpl>()
            BrowseCardPagerFactory { query -> impl.pages(query) }
        }
    }
