package com.devindie.cmptemplate.core.di

import com.devindie.cmptemplate.domain.usecase.carddetail.GetCardDetailUseCase
import com.devindie.cmptemplate.domain.usecase.user.ClearUserSessionUseCase
import com.devindie.cmptemplate.domain.usecase.user.GetUserSessionUseCase
import com.devindie.cmptemplate.domain.usecase.user.SaveUserSessionUseCase
import com.devindie.cmptemplate.feature.browse.BrowseViewModel
import com.devindie.cmptemplate.feature.carddetail.CardDetailViewModel
import com.devindie.cmptemplate.feature.main.MainViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appDomainModule =
    module {
        factoryOf(::GetCardDetailUseCase)
        factoryOf(::GetUserSessionUseCase)
        factoryOf(::SaveUserSessionUseCase)
        factoryOf(::ClearUserSessionUseCase)
        viewModelOf(::BrowseViewModel)
        viewModelOf(::MainViewModel)
        viewModel { (cardId: Long) ->
            CardDetailViewModel(
                getCardDetail = get(),
                cardId = cardId,
            )
        }
    }
