package com.devindie.cmptemplate.di

import com.devindie.cmptemplate.domain.usecase.browse.EnsureBrowseCatalogSeededUseCase
import com.devindie.cmptemplate.domain.usecase.browse.ObserveBrowseCardsUseCase
import com.devindie.cmptemplate.domain.usecase.carddetail.GetCardDetailUseCase
import com.devindie.cmptemplate.domain.usecase.user.ClearUserSessionUseCase
import com.devindie.cmptemplate.domain.usecase.user.GetUserSessionUseCase
import com.devindie.cmptemplate.domain.usecase.user.SaveUserSessionUseCase
import com.devindie.cmptemplate.screens.browse.BrowseViewModel
import com.devindie.cmptemplate.screens.carddetail.CardDetailViewModel
import com.devindie.cmptemplate.screens.main.MainViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appDomainModule =
    module {
        factoryOf(::ObserveBrowseCardsUseCase)
        factoryOf(::EnsureBrowseCatalogSeededUseCase)
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
