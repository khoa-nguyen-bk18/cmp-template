package com.devindie.cmptemplate.feature.carddetail.api

import com.devindie.cmptemplate.feature.carddetail.impl.CardDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val cardDetailFeatureModule =
    module {
        viewModel { (cardId: Long) ->
            CardDetailViewModel(
                getCardDetail = get(),
                cardId = cardId,
            )
        }
    }
